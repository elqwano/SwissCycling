package ch.epfl.javelo.gui;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.CityBikeCF;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.GpxGenerator;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * la classe principale de l'application
 * @author Quentin Anglio (313052)
 * @author valentin dupraz (315995)
 */
public final class JaVelo extends Application {
    private final static int MINWIDTH = 800;
    private final static int MINHEIGHT = 600;
    private final static String DATA_REP = "javelo-data";
    private final static String OSM_CACHE = "osm-cache" ;
    private final static String TILE_SERVER = "tile.openstreetmap.org";
    private final static String TITLE = "SwissCycling";

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * construis l'interface graphique finale en combinant les parties gérées par les classes écrites précédemment
     * et en y ajoutant le menu
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Graph graph = Graph.loadFrom(Path.of(DATA_REP));
        CityBikeCF costFunction = new CityBikeCF(graph);
        RouteComputer routeComputer = new RouteComputer(graph, costFunction);
        RouteBean routeBean = new RouteBean(routeComputer);

        Path cacheBasePath = Path.of(OSM_CACHE);
        String tileServerHost = TILE_SERVER;
        TileManager tileManager = new TileManager(cacheBasePath, tileServerHost);

        ErrorManager errorManager = new ErrorManager();
        Consumer<String> errorSignal = errorManager::displayError;
        AnnotatedMapManager annotatedMapManager = new AnnotatedMapManager(graph, tileManager, routeBean, errorSignal);

        BorderPane borderPane = new BorderPane();
        SplitPane splitPane = new SplitPane(annotatedMapManager.pane());
        StackPane stackPane = new StackPane(splitPane, errorManager.pane());

        Menu menu = new Menu("Fichier");
        MenuBar menuBar = new MenuBar(menu);
        MenuItem menuItem = new MenuItem("Exporter GPX");
        menu.getItems().add(menuItem);
        menuItem.disableProperty().set(true);

        ReadOnlyObjectProperty<ElevationProfile> elevationProfile = routeBean.elevationProfileReadOnlyObjectProperty();
        DoubleProperty highlightedPosition = routeBean.highlightedPositionProperty();
        ElevationProfileManager elevationProfileManager =
                new ElevationProfileManager(elevationProfile, highlightedPosition);

        SplitPane.setResizableWithParent(splitPane, false);
        borderPane.setCenter(stackPane);
        borderPane.setTop(menuBar);
        splitPane.setOrientation(Orientation.VERTICAL);

        routeBean.routeReadOnlyObjectProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue == null & newValue != null) {
                splitPane.getItems().add(elevationProfileManager.pane());
                menuItem.disableProperty().set(false);
            }
            if (oldValue != null & newValue == null) {
                splitPane.getItems().remove(elevationProfileManager.pane());
                menuItem.disableProperty().set(true);
            }
        });

        menu.setOnAction(event -> {
            try {
                GpxGenerator.writeGpx("javelo.gpx", routeBean.getRoute(), elevationProfile.get());
            } catch (UncheckedIOException | IOException ignored) {
            }
        });

                routeBean.highlightedPositionProperty().bind(Bindings
                .when(annotatedMapManager.mousePositionOnRouteProperty().greaterThanOrEqualTo(0d))
                .then(annotatedMapManager.mousePositionOnRouteProperty())
                .otherwise(elevationProfileManager.mousePositionOnProfileProperty()));

        primaryStage.setTitle(TITLE);
        primaryStage.setMinWidth(MINWIDTH);
        primaryStage.setMinHeight(MINHEIGHT);
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.show();
    }
}
