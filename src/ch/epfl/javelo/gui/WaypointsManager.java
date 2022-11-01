package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

import java.util.function.Consumer;

/**
 * La classe WaypointsManager, du sous-paquetage gui, publique et finale, gère l'affichage et l'interaction
 * avec les points de passage.
 *
 * @author Quentin Anglio (313052)
 */
public final class WaypointsManager {

    private final static String MARKER_OUT = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
    private final static String MARKER_IN = "M0-23A1 1 0 000-29 1 1 0 000-23";
    private final static String CSS_PIN = "pin";
    private final static String CSS_OUT = "pin_outside";
    private final static String CSS_IN = "pin_inside";
    private final static String CSS_FST = "first";
    private final static String CSS_MID = "middle";
    private final static String CSS_LST = "last";
    private final static String ERROR_MESSAGE = "\"Aucune route à proximité !\"";
    private final static String ERROR_MESSAGE_OUT = "\"Vous vous trouvez hors de Suisse !\"";
    private final static int SEARCH_DISTANCE = 500;

    private final Pane pane;
    private final Graph graph;
    private final ObjectProperty<MapViewParameters> mapParameters;
    private final ObservableList<Waypoint> waypointObservableList;
    private final Consumer<String> errorSignal;
    private final ObjectProperty<Point2D> clicked2D = new SimpleObjectProperty<>();
    private final ObjectProperty<Point2D> clickPosition = new SimpleObjectProperty<>();

    /**
     * Constructeur public de WaypointsManager
     *
     * @param graph                  le graphe du réseau routier,
     * @param mapParameters          une propriété JavaFX contenant les paramètres de la carte affichée,
     * @param waypointObservableList la liste (observable) de tous les points de passage,
     * @param errorSignal            un objet permettant de signaler les erreurs
     */
    public WaypointsManager(Graph graph
            , ObjectProperty<MapViewParameters> mapParameters
            , ObservableList<Waypoint> waypointObservableList
            , Consumer<String> errorSignal) {
        /* Initialisation des attributs */
        this.graph = graph;
        this.mapParameters = mapParameters;
        this.waypointObservableList = waypointObservableList;
        this.errorSignal = errorSignal;
        pane = new Pane();
        /* auditeur détectant les changements de mapParameters et repositionnant les marqueurs*/
        this.mapParameters.addListener((b, e, c) -> replaceAllPins());
        /* auditeur détectant les changements de waypointObservableList et recréant la totalité des marqueurs */
        this.waypointObservableList.addListener((ListChangeListener<Waypoint>) c -> draw());
        /* Initialisation du gestionnaire évènement & dessin des marqueurs donnés au constructeur */
        pane.setPickOnBounds(false);
        draw();
    }

    /**
     * retourne le panneau contenant les points de passage,
     *
     * @return le panneau contenant les points de passage,
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Prend en arguments les coordonnées x et y d'un point et ajoute un nouveau point de passage au nœud du graphe
     * qui en est le plus proche. La méthode addWaypoint recherche le nœud le plus proche dans un cercle de 1000 m
     * de diamètre centré sur le point donné. Si aucun nœud n'y est trouvé, elle signale une erreur
     *
     * @param x coordonnées x d'un point
     * @param y coordonnées y d'un point
     */
    public void addWayPoint(double x, double y) {
        PointCh clickedPointCh = mapParameters.get()
                .pointAt(x, y)
                .toPointCh();
        /* On vérifie si le point est en Suisse*/
        if (clickedPointCh==null){
            errorSignal.accept(ERROR_MESSAGE_OUT);
            return;
        }
        int nodeClosestTo = graph.nodeClosestTo(clickedPointCh, SEARCH_DISTANCE);
        /* On vérifie s'il y a une route à proximité */
        if (nodeClosestTo < 0) {
            errorSignal.accept(ERROR_MESSAGE);
            return;
        }
        PointCh closestPointCh = graph.nodePoint(nodeClosestTo);
        waypointObservableList.add(new Waypoint(closestPointCh, nodeClosestTo));
    }

    //========================== méthodes privées =========================================//

    /**
     * Dessine les WayPoint de waypointObservableList
     */
    private void draw() {
        pane.getChildren().clear();
        final int last = waypointObservableList.size() - 1;
        for (int i = 0; i <= last; i++) {
            PointWebMercator mercator = PointWebMercator.ofPointCh(
                    waypointObservableList
                            .get(i)
                            .pointCh());
            /* Création et dessin des marqueurs */
            Group pin = newPin();
            placePin(pin, mercator);
            colorPins(i, pin, last);
            /* Event Handler installation */
            handlePin(pin, i);
            /* Ajout à la scène */
            pane.getChildren().add(pin);
        }
    }

    /**
     * Placement du pin
     */
    private void placePin(Node pin, PointWebMercator mercator) {
        pin.setLayoutX(mapParameters.get()
                .viewX(mercator));
        pin.setLayoutY(mapParameters.get().
                viewY(mercator));
    }

    /**
     * Replace tous les marqueurs sur la carte
     */
    private void replaceAllPins() {
        final int last = waypointObservableList.size() - 1;
        for (int i = 0; i <= last; i++) {
            PointWebMercator mercator = PointWebMercator.ofPointCh
                    (waypointObservableList
                            .get(i)
                            .pointCh());
            placePin(pane.getChildren().get(i), mercator);
        }
    }

    /**
     * crée un nouveau marqueur de type Group
     */
    private Group newPin() {
        SVGPath pathIn = new SVGPath();
        SVGPath pathOut = new SVGPath();
        pathIn.setContent(MARKER_IN);
        pathOut.setContent(MARKER_OUT);
        pathIn.getStyleClass().setAll(CSS_IN);
        pathOut.getStyleClass().setAll(CSS_OUT);
        Group pin = new Group(pathOut, pathIn);
        pin.getStyleClass().add(CSS_PIN);
        return pin;
    }

    /**
     * Coloration des pins
     */
    private void colorPins(int i, Node pin, int last) {
        if (i == 0) pin.getStyleClass().add(CSS_FST);
        else if (i == last) pin.getStyleClass().add(CSS_LST);
        else pin.getStyleClass().add(CSS_MID);
    }

    /**
     * Ajout des gestionnaires d'événements aux pins
     */
    private void handlePin(Group pin, int wayPointIndex) {
        /* press */
        pin.setOnMousePressed(mouseEvent -> {
            clickPosition.set(new Point2D(mouseEvent.getX(), mouseEvent.getY()));
            clicked2D.set(new Point2D(pin.getLayoutY(), pin.getLayoutY()));
        });
        /* drag */
        pin.setOnMouseDragged(mouseEvent -> {
            Point2D target = new Point2D(mouseEvent.getX(), mouseEvent.getY())
                    .subtract(clickPosition.get());
            pin.setLayoutX(target.getX() + pin.getLayoutX());
            pin.setLayoutY(target.getY() + pin.getLayoutY());
        });
        /* release */
        pin.setOnMouseReleased(mouseEvent -> {
            /* Supprimer pin et Waypoint si click */
            if (mouseEvent.isStillSincePress()) {
                waypointObservableList.remove(wayPointIndex);
                return;
            }
            /* sinon déplacer pin */
            PointWebMercator targetMercator = mapParameters
                    .get()
                    .pointAt(pin.getLayoutX(), pin.getLayoutY());
            PointCh pointCh = targetMercator.toPointCh();
            int nodeClosestTo = graph
                    .nodeClosestTo(pointCh, SEARCH_DISTANCE);
            /* si pas trouvé de noeud on revient à celui d'avant */
            if (nodeClosestTo < 0) {
                pin.setLayoutX(clicked2D.get().getX());
                pin.setLayoutY(clicked2D.get().getY());
                replaceAllPins();
                errorSignal.accept(ERROR_MESSAGE);
                return;
            }
            /* sinon on place un pin sur le noeud et on change le waypoint*/
            Waypoint newWayPoint = new Waypoint(pointCh, nodeClosestTo);
            waypointObservableList.set(wayPointIndex, newWayPoint);
        });
    }
}
