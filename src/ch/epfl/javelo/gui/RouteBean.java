package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.*;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Représente un bean JavaFX regroupant les propriétés relatives aux points de passage et à l'itinéraire correspondant.
 * @author Quentin Anglio (313052)
 */
public final class RouteBean {

    private final static int CACHE_CAPACITY = 50 ;
    private final static double STEP_LENGTH = 5;
    private final static double NO_HIGHLIGHT = Double.NaN;

    private final RouteComputer routeComputer;
    private final ObjectProperty<Route> route;
    private final ObjectProperty<ElevationProfile> elevationProfile;
    private final DoubleProperty highlightedPosition;
    private final ObservableList<Waypoint> waypointObservableList;
    private final Map<WaypointPair, Route> memoryCache;

    private final record WaypointPair(int startNode, int endNode){}

    /**
     * Constructeur de RouteBean. Lors d'un changement de la liste de waypoints, le meilleur itinéraire (simple)
     * reliant chaque point de passage à son successeur est déterminé
     * et ces itinéraires sont combinés en un unique itinéraire multiple.
     *
     * @param routeComputer calculateur d'itinéraire, de type RouteComputer,
     *                      utilisé pour déterminer le meilleur itinéraire reliant deux points de passage.
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        waypointObservableList = FXCollections.observableArrayList();
        elevationProfile = new SimpleObjectProperty<>();
        route = new SimpleObjectProperty<>();
        highlightedPosition = new SimpleDoubleProperty(NO_HIGHLIGHT);
        memoryCache = new LinkedHashMap<>(CACHE_CAPACITY) {
            /* On défini le comportement des methods put() et putall() dans le cas où le cache est plein.*/
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return memoryCache.size() > CACHE_CAPACITY;
            }
        };
        this.waypointObservableList.addListener((Observable o) -> updateRoute());
    }

    /**
     *  retourne l'index du segment contenant la position, en ignorant les segments vides
     * @param position la position
     * @return l'index du segment contenant la position, en ignorant les segments vides
     */
    public int indexOfNonEmptySegmentAt(double position) {
        int index = route.get().indexOfSegmentAt(position);
        for (int i = 0; i <= index; i += 1) {
            int n1 = waypointObservableList.get(i).closestNodeId();
            int n2 = waypointObservableList.get(i + 1).closestNodeId();
            if (n1 == n2) index += 1;
        }
        return index;
    }

    //================ P r i v é e ============================//

    /** Mise à jour de l'itinéraire pour l'auditeur de waypointObservableList */
    private void updateRoute(){
        /* si waypointList ne contient pas au moins 2 elements  */
        if (waypointObservableList.size() < 2 ) {
            setAttributesOfInvalidRoute();
            return;
        }
        /* Début construction multiroute */
        ArrayList<Route> routeList = new ArrayList<>();
        for (int i = 1; i < waypointObservableList.size(); i++) {
            /* calcule du WaypointPair */
            Waypoint first = waypointObservableList.get(i - 1);
            Waypoint last = waypointObservableList.get(i);
            WaypointPair waypointPair = new WaypointPair(first.closestNodeId(), last.closestNodeId());
            /* calcule de la route */
            Route route;
            if (!memoryCache.containsKey(waypointPair)) {
                route = routeComputer
                        .bestRouteBetween(waypointPair.startNode, waypointPair.endNode);
                if (route == null) {
                    /* un segment de route invalide rend toute la route invalide*/
                    routeList.clear();
                    break;
                }
            } else {
                route = memoryCache.get(waypointPair);
            }
            /* ajout dans le cache et dans la liste de route */
            memoryCache.putIfAbsent(waypointPair, route);
            routeList.add(route);
        }
        /* on crée la multiroute (erreur si la liste est vide) et on set la propriété */
        if (routeList.isEmpty()){
            setAttributesOfInvalidRoute();
            return;
        }
        /* Calcule de la route et du profile */
        MultiRoute multiRoute = new MultiRoute(routeList);
        this.route.set(multiRoute);
        setElevationProfile(multiRoute);
//        try {
//            /* Calcule de la route et du profile */
//            MultiRoute multiRoute = new MultiRoute(routeList);
//            this.route.set(multiRoute);
//            setElevationProfile(multiRoute);
//            /* si la Multiroute est créée avec une liste vide on reçoit une IllegalArgument Exception*/
//        } catch (IllegalArgumentException noRouteError) {
//            /* dans ce cas on set les attributs à null/NaN */
//            setAttributesOfInvalidRoute();
//        }
//    }
    }

    /** pour set elevationProfile et route à null */
    private void setAttributesOfInvalidRoute(){
        elevationProfile.set(null);
        route.set(null);
    }

    //=================== Méthodes de propriétés ================//

    /**
     * Retourne la propriété elle-même, de type DoubleProperty
     * @return la propriété elle-même, de type DoubleProperty
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPosition;
    }
    /**
     * Retourne le contenu de la propriété, de type double
     * @return le contenu de la propriété, de type double
     */
    public double getHighlightedPosition() {
        return highlightedPosition.get();
    }
    /**
     * Prend une valeur de type double et la stockant dans la propriété
     * @param position la nouvelle valeur de la position
     */
    public void setHighlightedPosition(double position){
        highlightedPosition.set(position);
    }
    /**
     * Retourne la liste de waypoints
     * @return la liste de waypoints
     */
    public ObservableList<Waypoint> getWaypointObservableList(){
        return waypointObservableList;
    }
    /**
     *  Retourne l'itinéraire sous la forme d'une valeur de type ReadOnlyObjectProperty<…>
     * @return l'itinéraire sous la forme d'une valeur de type ReadOnlyObjectProperty<…>
     */
    public ReadOnlyObjectProperty<Route> routeReadOnlyObjectProperty() {
        return route;
    }
    /**
     * Retourne l'itinéraire
     * @return l'itinéraire
     */
    public Route getRoute() {
        return route.get();
    }
    /**
     * Retourne le profile sous la forme d'une valeur de type ReadOnlyObjectProperty<…>
     * @return le profile sous la forme d'une valeur de type ReadOnlyObjectProperty<…>
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileReadOnlyObjectProperty(){
        return elevationProfile ;
    }
    /**
     * Retourne le profile
     * @return le profile
     */
    public ElevationProfile getElevationProfile() {
        return elevationProfile.get();
    }

    /** pour définir le profile à partir de la route en argument */
    private void setElevationProfile(Route route){
        this.elevationProfile
                .set(ElevationProfileComputer
                        .elevationProfile(route, RouteBean.STEP_LENGTH));
    }
}