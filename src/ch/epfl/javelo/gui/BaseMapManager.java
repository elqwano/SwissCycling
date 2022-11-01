package ch.epfl.javelo.gui;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * La classe BaseMapManager gère l'affichage et l'interaction avec le fond de carte.
 *
 * @author valentin dupraz (315995)
 */
public final class BaseMapManager {
    private final static int TILES_SIDE = 256;
    private final static int MIN_ZOOM = 8;
    private final static int MAX_ZOOM = 19;
    private final static String ERROR_MESSAGE = "\"image pas trouvée\"";

    private final TileManager tileManager;
    private final WaypointsManager waypointsManager;
    private final ObjectProperty<MapViewParameters> mapViewParametersObjectProperty;
    private final Canvas canvas;
    private final Pane pane;
    private final GraphicsContext graphicsContext;
    private final ObjectProperty<Point2D> clickPosition;

    private boolean redrawNeeded = false;

    /**
     * Le constructeur offre une seule méthode publique, pane, retournant le panneau JavaFX affichant le fond de carte
     *
     * @param waypointsManager le gestionnaire des points de passage
     * @param tileManager      le gestionnaire de tuiles à utiliser pour obtenir les tuiles de la carte
     * @param objectProperty   une propriété JavaFX contenant les paramètres de la carte affichée
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> objectProperty) {
        this.tileManager = tileManager;
        this.mapViewParametersObjectProperty = objectProperty;
        this.waypointsManager = waypointsManager;

        clickPosition = new SimpleObjectProperty<>();
        /* Hiérarchie JavaFX */
        canvas = new Canvas();
        pane = new Pane(canvas);
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        graphicsContext = canvas.getGraphicsContext2D();
        /* Gestion des événements */
        updateCanevas();
        zoomManager();
        mouseHandler();
    }

    /**
     * retourne pane le panneau JavaFX affichant le fond de carte
     *
     * @return pane le panneau JavaFX affichant le fond de carte
     */
    public Pane pane() {
        return pane;
    }

    /* ================= privates ======================== */

    /**
     * Gestion du redessin du canevas
     */
    private void updateCanevas() {
        //vérifie que JavaFX redraw  à chaque battement
        canvas.sceneProperty().addListener((observable, oldValue, newValue) -> {
            assert oldValue == null;
            newValue.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
        mapViewParametersObjectProperty.addListener(n -> redrawOnNextPulse());
        canvas.widthProperty().addListener(n -> redrawOnNextPulse());
        canvas.heightProperty().addListener(n -> redrawOnNextPulse());
    }

    /**
     * Gestion du zoom
     */
    private void zoomManager() {
        //l'utilisation de la molette de la souris permet de changer le niveau de zoom de la carte
        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            if (e.getDeltaY() == 0d) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            PointWebMercator pointWebMercator = map().pointAt(e.getX(), e.getY());
            int newZoom = Math2.clamp(MIN_ZOOM, map().zoomLevel() + zoomDelta, MAX_ZOOM);
            Point2D coordinates = new Point2D(pointWebMercator.xAtZoomLevel(newZoom) - e.getX(),
                    pointWebMercator.yAtZoomLevel(newZoom) - e.getY());
            //nouvelle instance de MapViewsParameters
            mapViewParametersObjectProperty.set(new MapViewParameters(newZoom, coordinates.getX(), coordinates.getY()));
        });
    }

    /**
     * Gestion de la souris
     */
    private void mouseHandler() {
        //le déplacement de la souris lorsque le bouton gauche est maintenu pressé permet de faire glisser la carte
        pane.setOnMousePressed(e -> clickPosition.set(new Point2D(e.getX(), e.getY())));
        pane.setOnMouseDragged(e -> {
            Point2D dragPosition = new Point2D(e.getX(), e.getY());
            Point2D difference = dragPosition.subtract(clickPosition.get());
            Point2D newTop = map().topLeft().subtract(difference);
            mapViewParametersObjectProperty.set(map().withMinXY(newTop.getX(), newTop.getY()));
            //actualisation de la position
            clickPosition.set(dragPosition);
        });
        pane.setOnMouseReleased(e -> clickPosition.set(null));
        //le clic sur la carte permet d'ajouter un point de passage
        pane.setOnMouseClicked(e -> {
            if (e.isStillSincePress()) {
                waypointsManager.addWayPoint(e.getX(), e.getY());
            }
        });
    }

    /**
     * retourne la valeur actuelle de cet ObservableObjectValue
     *
     * @return la valeur actuelle de cet ObservableObjectValue
     */
    private MapViewParameters map() {
        return mapViewParametersObjectProperty.get();
    }

    /**
     * effectue le redessin si nécessaire
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;
        //index 1ere tuile
        Point2D first = map().topLeft().multiply(1d / TILES_SIDE);
        //index derniere
        Point2D last = map().topLeft().add(canvas.getWidth(), canvas.getHeight()).multiply(1d / TILES_SIDE);

        for (int i = (int) first.getX(); i <= last.getX(); i++) {
            for (int j = (int) first.getY(); j <= last.getY(); j++) {
                try {
                    graphicsContext
                            .drawImage(tileManager.imageForTileAt(new TileManager.TileId(map().zoomLevel(), i, j)),
                                    i * TILES_SIDE - map().topLeft().getX(),
                                    j * TILES_SIDE - map().topLeft().getY());
                } catch (IOException e) {
                    System.out.println(ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * demande un redessin au prochain battement
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }
}