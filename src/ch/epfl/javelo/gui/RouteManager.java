package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Edge;
import ch.epfl.javelo.routing.Route;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe RouteManager gère l'affichage de l'itinéraire et une partie de l'interaction avec lui
 *
 * @author valentin dupraz (315995)
 */
public final class RouteManager {
    private final static String CSS_ROUTE = "route";
    private final static String CSS_HIGHLIGHT = "highlight";
    private final static double CIRCLE_RADIUS = 5.0;
    private final RouteBean routeBean;
    private final ObjectProperty<MapViewParameters> mapViewParameters;
    private final Pane pane;
    private final Polyline polyline;
    private final Circle circle;

    /**
     * constructeur public de RouteManager
     *
     * @param routeBean      le bean de l'itinéraire
     * @param objectProperty une propriété JavaFX, en lecture seule, contenant les paramètres de la carte affichée
     */
    public RouteManager(RouteBean routeBean, ObjectProperty<MapViewParameters> objectProperty) {
        this.routeBean = routeBean;
        this.mapViewParameters = objectProperty;

        polyline = new Polyline();
        polyline.setId(CSS_ROUTE);
        circle = new Circle();
        circle.setId(CSS_HIGHLIGHT);
        circle.setRadius(CIRCLE_RADIUS);
        circle.setVisible(false);
        pane = new Pane(polyline, circle);
        pane.setPickOnBounds(false);

        this.routeBean.highlightedPositionProperty().addListener((observable, oldValue, newValue) -> {
            setCircleVisibility();
            drawCircle();
        });

        this.routeBean.getWaypointObservableList().addListener((Observable o) -> {
            setCircleVisibility();
            setPolylineVisibility();
            drawCircle();
            drawPolyline();
        });

        listenMapView();
        listenMouse();
    }

    /**
     * retourne le panneau JavaFX contenant la ligne représentant l'itinéraire et le disque de mise en évidence
     *
     * @return le panneau JavaFX contenant la ligne représentant l'itinéraire et le disque de mise en évidence
     */
    public Pane pane() {
        return pane;
    }

    /* ================ p r i v e e ================== */

    /** gestion evenements de la souris*/
    private void listenMouse(){
        circle.setOnMouseClicked(e -> {
            Point2D point2D = circle.localToParent(e.getX(), e.getY());
            PointCh pointCh = map().pointAt(point2D.getX(), point2D.getY()).toPointCh();
            double position = routeBean.getHighlightedPosition();
            int closestId = routeBean.getRoute().nodeClosestTo(position);
            routeBean.getWaypointObservableList().add(routeBean.indexOfNonEmptySegmentAt(position) + 1,
                    new Waypoint(pointCh, closestId));
        });
    }

    /** auditeur de mapview*/
    private void listenMapView(){
        mapViewParameters.addListener((observable, oldValue, newValue) -> {
            setCircleVisibility();
            drawCircle();

            if (oldValue.zoomLevel() != newValue.zoomLevel()) {
                setPolylineVisibility();
                drawPolyline();
            } else {
                polyline.setLayoutX(-newValue.xTop());
                polyline.setLayoutY(-newValue.yTop());
            }
        });
    }

    /**
     * retourne la valeur actuelle de cet ObservableObjectValue
     *
     * @return la valeur actuelle de cet ObservableObjectValue
     */
    private MapViewParameters map() {
        return mapViewParameters.get();
    }

    /**
     * défini la visibilité du cercle
     */
    private void setCircleVisibility() {
        circle.setVisible(!Double.isNaN(routeBean.getHighlightedPosition()) && routeBean.getRoute() != null);
    }

    /**
     * défini la visibilité du cercle
     */
    private void setPolylineVisibility() {
        polyline.setVisible(routeBean.getRoute() != null);
    }

    /**
     * dessine la polyline
     */
    private void drawPolyline() {
        polyline.getPoints().clear();
        Route route = routeBean.getRoute();

        if (route != null) {
            List<Double> doublesList = new ArrayList<>();
            /* ajout du premier point */
            PointWebMercator firstPoint = PointWebMercator.ofPointCh(route.edges().get(0).fromPoint());
            doublesList.add(firstPoint.xAtZoomLevel(map().zoomLevel()));
            doublesList.add(firstPoint.yAtZoomLevel(map().zoomLevel()));
            /* ajout des autres points */
            for (Edge edge : route.edges()) {
                PointWebMercator pointWebMercator = PointWebMercator.ofPointCh(edge.toPoint());
                doublesList.add(pointWebMercator.xAtZoomLevel(map().zoomLevel()));
                doublesList.add(pointWebMercator.yAtZoomLevel(map().zoomLevel()));
            }
            polyline.setLayoutX(-map().xTop());
            polyline.setLayoutY(-map().yTop());
            polyline.getPoints().addAll(doublesList);
        }
    }

    /**
     * dessine le cercle
     */
    private void drawCircle() {
        if (routeBean.getRoute() != null) {
            PointWebMercator point = PointWebMercator
                    .ofPointCh(routeBean.getRoute().pointAt(routeBean.getHighlightedPosition()));
            circle.setLayoutX(map().viewX(point));
            circle.setLayoutY(map().viewY(point));
        }
    }
}