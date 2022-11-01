package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.function.Consumer;

/**
 * La classe gère l'affichage de la carte «annotée», c.-à-d. le fond de carte au-dessus duquel sont superposés
 * l'itinéraire et les points de passage.
 *
 * @author Quentin Anglio (313052)
 * @author valentin dupraz (315995)
 */
public final class AnnotatedMapManager {

    private final static String CSS_MAP = "map.css";
    private final static int ZOOM_LEVEL = 12;
    private final static int X_TOP = 543200;
    private final static int Y_TOP = 370650;
    private final static int MIN_DISTANCE = 15;
    private final static double NOT_ON_ROUTE = Double.NaN;

    private final StackPane stackPane;
    private final ObjectProperty<RouteBean> routeBeanProperty;
    private final ObjectProperty<MapViewParameters> mapViewParametersProperty;
    private final DoubleProperty mousePositionOnRoute;
    private final ObjectProperty<Point2D> mouseProperty;

    /**
     * le constructeur crée un BaseMapManager, un WaypointsManager, un RouteManager et combine leurs panneaux respectifs
     *
     * @param graph       le graphe du réseau routier
     * @param tileManager le gestionnaire de tuiles OpenStreetMap
     * @param routeBean   le bean de l'itinéraire
     * @param errorSignal un «consommateur d'erreurs» permettant de signaler une erreur
     */
    public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean routeBean, Consumer<String> errorSignal) {

        mapViewParametersProperty = new SimpleObjectProperty<>(new MapViewParameters(ZOOM_LEVEL, X_TOP, Y_TOP));
        routeBeanProperty = new SimpleObjectProperty<>(routeBean);
        WaypointsManager waypointsManager = new WaypointsManager(
                graph,
                mapViewParametersProperty,
                routeBean.getWaypointObservableList(),
                errorSignal);
        RouteManager routeManager = new RouteManager(routeBean, mapViewParametersProperty);
        BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapViewParametersProperty);
        mousePositionOnRoute = new SimpleDoubleProperty(NOT_ON_ROUTE);
        mouseProperty = new SimpleObjectProperty<>();
        stackPane = new StackPane(baseMapManager.pane(), routeManager.pane(), waypointsManager.pane());
        stackPane.getStylesheets().setAll(CSS_MAP);

        /* Listener et binding */
        listenToTheMouse();
        bindMousePositionOnRoute();
    }

    /**
     * Gestion des événements de la souris
     */
    private void listenToTheMouse() {
        stackPane.setOnMouseMoved(mouseEvent -> mouseProperty.set(new Point2D(mouseEvent.getX(), mouseEvent.getY())));
        stackPane.setOnMouseExited(mouseEvent -> {
            mouseProperty.set(null);

        });
    }

    /**
     * Binding de la position de la souris
     */
    private void bindMousePositionOnRoute() {
        mousePositionOnRoute.bind(Bindings.createDoubleBinding(() -> {
            if (mouseProperty.get() == null || routeBeanProperty.get().getRoute() == null) {
                return NOT_ON_ROUTE;
            } else {
                PointWebMercator mousePoint = mapViewParametersProperty
                        .get()
                        .pointAt(mouseProperty.get().getX(), mouseProperty.get().getY());
                PointCh mouseCh = mousePoint.toPointCh();
//                if (mouseCh==null){
//                    mousePositionOnRoute.set(NOT_ON_ROUTE);
//                }
                RoutePoint routePoint = routeBeanProperty.get()
                        .getRoute()
                        .pointClosestTo(mouseCh);
                PointWebMercator closest = PointWebMercator.ofPointCh(routePoint.point());
                double deltaX = mapViewParametersProperty.get().viewX(mousePoint)
                        - mapViewParametersProperty.get().viewX(closest);
                double deltaY = mapViewParametersProperty.get().viewY(mousePoint)
                        - mapViewParametersProperty.get().viewY(closest);
                return Math2.norm(deltaX, deltaY) <= MIN_DISTANCE ? routePoint.position() : NOT_ON_ROUTE;
            }
        }, mouseProperty, mapViewParametersProperty, routeBeanProperty.get().routeReadOnlyObjectProperty()));
    }

    /**
     * retourne le panneau contenant la carte annotée
     *
     * @return le panneau contenant la carte annotée
     */
    public Pane pane() {
        return stackPane;
    }

    /**
     * retourne la propriété contenant la position du pointeur de la souris le long de l'itinéraire
     *
     * @return la propriété contenant la position du pointeur de la souris le long de l'itinéraire
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRoute;
    }

    /*================ P r I v a T E ================ */

    private Point2D position(MouseEvent mouseEvent) {
        return new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }
}


