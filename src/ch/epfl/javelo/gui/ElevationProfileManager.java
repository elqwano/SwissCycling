package ch.epfl.javelo.gui;

import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe ElevationProfileManager du sous-paquetage gui, publique et finale,
 * gère l'affichage et l'interaction avec le profil en long d'un itinéraire
 *
 * @author Quentin Anglio (313052)
 */
public final class ElevationProfileManager {

    private final static Insets INSETS = new Insets(10, 10, 20, 40);
    private final static int[] POS_STEPS = {1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000};
    private final static int[] ELE_STEPS = {5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000};
    private final static double MIN_STEP_POS = 50;
    private final static double MIN_STEP_ELE = 25;
    private final static String RECT_BIND_MIN = "minY";
    private final static String RECT_BIND_MAX = "maxY";
    private final static String CSS_STYLESHEET = "elevation_profile.css";
    private final static String CSS_VBOX = "profile_data";
    private final static String CSS_GRID = "grid";
    private final static String CSS_PROFILE = "profile";
    private final static String CSS_TEXT_GRID = "grid_label";
    private final static String CSS_HORIZONTAL = "horizontal";
    private final static String CSS_VERTICAL = "vertical";
    private final static Font LABEL_FONT = Font.font("Avenir", 10);
    private final static int LABEL_V_OFFSET_DIV = 2;
    private final static int LABEL_H_OFFSET_H = 5;  //5 -> should be 2
    private final static int LABEL_H_OFFSET_V = 8;  //8 -> should be 0
    private final static int M_TO_KM = 1000;
    private final static double NULL_POSITION = Double.NaN;

    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile;
    private final DoubleProperty highlightedPosition;
    private final DoubleProperty mousePositionOnProfile;
    private final BorderPane masterPane;
    private final Pane pane;
    private final Path grid;
    private final Group gridLabels;
    private final Polygon polygon;
    private final Line line;
    private final VBox textPane;
    private final ObjectProperty<Rectangle2D> rectangle2D;
    private final ObjectProperty<Transform> screenToWorld;
    private final ObjectProperty<Transform> worldToScreen;
    private final IntegerProperty stepPosition;
    private final IntegerProperty stepElevation;

    /**
     * constructeur public
     *
     * @param elevationProfile    une propriété, accessible en lecture seule, contenant le profil à afficher;
     *                            elle est de type ReadOnlyObjectProperty<ElevationProfile>
     *                            et contient null dans le cas où aucun profil n'est à afficher,
     * @param highlightedPosition une propriété, accessible en lecture seule, contenant la position le long du profil
     *                            à mettre en évidence; elle est de type ReadOnlyDoubleProperty
     *                            et contient NaN dans le cas où aucune position n'est à mettre en évidence.
     */
    public ElevationProfileManager(ReadOnlyObjectProperty<ElevationProfile> elevationProfile,
                                   DoubleProperty highlightedPosition) {
        /* initialisation des attributs, hiérarchie JavaFX et rectangle */
        this.elevationProfile = elevationProfile;
        this.highlightedPosition = highlightedPosition;
        masterPane = new BorderPane();
        masterPane.getStylesheets().add(CSS_STYLESHEET);
        pane = new Pane();
        textPane = new VBox();
        textPane.setId(CSS_VBOX);
        masterPane.setCenter(pane);
        masterPane.setBottom(textPane);
        rectangle2D = new SimpleObjectProperty<>();
        bindRectangle2D();
        /* Initialisation des transforms et des steps - TOUT ce qui suit a besoin des transforms */
        screenToWorld = new SimpleObjectProperty<>();
        worldToScreen = new SimpleObjectProperty<>();
        bindTransforms();
        stepPosition = new SimpleIntegerProperty();
        stepElevation = new SimpleIntegerProperty();
        /* Gestion de la souris  */
        mousePositionOnProfile = new SimpleDoubleProperty(NULL_POSITION);
        highlightedPosition.addListener((p, o, n) -> {

            mousePositionOnProfile.setValue(n);
        }
        );
        mouseHandler();
        /* On dessine tout */
        polygon = new Polygon();
        grid = new Path();
        gridLabels = new Group();
        line = new Line();
        pane.getChildren().addAll(grid, gridLabels, polygon, line);
        /* On écoute la modification de la taille de l'écran */
        newLine();
        listenScreenChange();
        listenProfile();
        /* On fini */
    }

    /**
     * retourne le panneau contenant le dessin du profil
     *
     * @return un pane avec le dessin du profil
     */
    public Pane pane() {
        return masterPane;
    }

    /**
     * retourne une propriété en lecture seule contenant la position du pointeur de la souris le long du profil
     * (en mètres, arrondie à l'entier le plus proche),
     * ou NaN si le pointeur de la souris ne se trouve pas au-dessus du profil.
     *
     * @return la position du pointeur de la souris le long du profil
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionOnProfile;
    }

    //================ P r i v é e ============================//

    /**
     * Calcule l'affine de l'écran au monde
     */
    private Transform computeTransformScreenWorld() {
        Affine transform = new Affine();
        double tX = getRectangle().getMinX();
        double tY = getRectangle().getMinY();
        double sX = getElevationProfile().length() / getRectangle().getWidth();
        double sY = getElevationProfile().totalElevation() / getRectangle().getHeight();
        double tX2 = 0.0;
        double tY2 = getElevationProfile().maxElevation();
        transform.prependTranslation(-tX, -tY);
        transform.prependScale(sX, -sY);
        transform.prependTranslation(tX2, tY2);
        return transform;
    }

    /**
     * Calcule l'affine du monde à l'écran'
     */
    private Transform computeTransformWorldScreen() throws NonInvertibleTransformException {
        return screenToWorld.getValue().createInverse();
    }

    /**
     * Bind les transforms au profil
     */
    private void bindTransforms() {
        screenToWorld.bind(Bindings.createObjectBinding(() ->
                invalidData() ? null : computeTransformScreenWorld(), rectangle2D, elevationProfile));

        worldToScreen.bind(Bindings.createObjectBinding(() ->
                invalidData() ? null : computeTransformWorldScreen(), rectangle2D, elevationProfile));
    }

    /**
     * choisi les steps pour l'axe horizontale
     */
    private int chooseStepPos() {
        int chosenStep = POS_STEPS[POS_STEPS.length - 1];
        double width = getRectangle().getWidth();
        double length = getElevationProfile().length();
        for (int step : POS_STEPS) {
            double divisor = length / step;
            if ((width / divisor) >= MIN_STEP_POS) {
                return step;
            }
        }
        return chosenStep;
    }

    /**
     * choisi les steps pour l'axe verticale
     */
    private int chooseStepElev() {
        int chosenStep = ELE_STEPS[ELE_STEPS.length - 1];
        double height = getRectangle().getHeight();
        double elevation = getElevationProfile().totalElevation();
        for (int step : ELE_STEPS) {
            double divisor = elevation / step;
            if ((height / divisor) >= MIN_STEP_ELE) {
                return step;
            }
        }
        return chosenStep;
    }

    /**
     * Gère la mise à jours de l'écran
     */
    private void updateScreen() {
        if (!invalidData()) {
            stepPosition.set(chooseStepPos());
            stepElevation.set(chooseStepElev());
            drawPolygon();
            newGrid();
            textPane.getChildren().setAll(newStats());
        } else {
            stepPosition.setValue(NULL_POSITION);
            stepElevation.setValue(NULL_POSITION);
            polygon.getPoints().clear();
            grid.getElements().clear();
            gridLabels.getChildren().clear();
            textPane.getChildren().clear();
        }
    }

    /**
     * crée la ligne de positionnement
     */
    private void newLine() {
        line.layoutXProperty().bind(Bindings.createDoubleBinding(() ->
                invalidData() ?
                        NULL_POSITION :
                        getWorldToScreen().transform(getMousePosition(), 0).getX(), mousePositionOnProfile));
        line.startYProperty().bind(Bindings.select(rectangle2D, RECT_BIND_MIN));
        line.endYProperty().bind(Bindings.select(rectangle2D, RECT_BIND_MAX));
        line.visibleProperty().bind(Bindings.greaterThanOrEqual(readOnlyHighlightedPosition(), 0));
    }

    /**
     * Dessine le polygone représentant le profile
     */
    private void drawPolygon() {
        polygon.getPoints().clear();
        polygon.setId(CSS_PROFILE);
        polygon.getPoints().addAll(getRectangle().getMaxX()
                , getRectangle().getMaxY()
                , getRectangle().getMinX()
                , getRectangle().getMaxY());
        double iteration = getRectangle().getWidth() + getRectangle().getMinX();
        for (double x = getRectangle().getMinX(); x <= iteration; x++) {
            double xWorld = getScreenToWorld()
                    .transform(new Point2D(x, 0))
                    .getX();
            Point2D worldPoint = new Point2D(xWorld, getElevationProfile().elevationAt(xWorld));
            Point2D screenPoint = getWorldToScreen().transform(worldPoint);
            polygon.getPoints()
                    .addAll(screenPoint.getX(), screenPoint.getY());
        }
    }

    /**
     * crée le grid
     */
    private void newGrid() {
        grid.getElements().clear();
        gridLabels.getChildren().clear();
        grid.setId(CSS_GRID);
        List<Text> textList = new ArrayList<>();
        /* Lignes verticales */
        int stepPos = stepPosition.get();
        double iteratorV = getElevationProfile().length() / stepPos;
        double height = getRectangle().getHeight();
        for (int i = 0; i <= iteratorV; i++) {
            Point2D move = getWorldToScreen()
                    .deltaTransform(i * stepPos, 0.0)
                    .add(INSETS.getLeft(), INSETS.getTop());
            grid.getElements().add(new MoveTo(move.getX(), move.getY()));
            grid.getElements().add(new LineTo(move.getX(), move.getY() + height));
            Text label = newText(false, (i * stepPos / M_TO_KM));
            double delta = label.prefWidth(0) / LABEL_V_OFFSET_DIV;
            label.setLayoutX(move.getX() - delta);
            label.setLayoutY(move.getY() + height);
            textList.add(label);
        }
        /* Lignes horizontales */
        int stepElev = stepElevation.get();
        double iteratorH = getElevationProfile().totalElevation() / stepElev;
        double width = getRectangle().getWidth();
        int startingBlock = (int) Math.ceil(getElevationProfile().minElevation());
        while (startingBlock % stepElev != 0) {
            startingBlock++;
        }
        for (int i = 0; i <= iteratorH; i++) {
            Point2D move = getWorldToScreen()
                    .transform(0, startingBlock + i * stepElev);
            double x = move.getX();
            double y = move.getY();
            if (y >= INSETS.getTop()) {
                grid.getElements().add(new MoveTo(x, y));
                grid.getElements().add(new LineTo(x + width, y));
                Text label = newText(true, startingBlock + i * stepElev);
                double delta = label.prefWidth(0) + LABEL_H_OFFSET_H;
                label.setLayoutX(x - delta);
                label.setLayoutY(y - LABEL_H_OFFSET_V);
                textList.add(label);
            }
        }
        gridLabels.getChildren().setAll(textList);
    }

    /**
     * Crée les étiquettes de la grille.
     *
     * @param horizontal Si true alors horizontale. Verticale sinon
     * @return l'étiquette
     */
    private Text newText(boolean horizontal, int label) {
        Text text = new Text(String.valueOf(label));
        text.setFont(LABEL_FONT);
        if (!horizontal) {
            text.textOriginProperty().set(VPos.TOP);
            text.getStyleClass().addAll(CSS_TEXT_GRID, CSS_VERTICAL);
        } else {
            text.getStyleClass().addAll(CSS_TEXT_GRID, CSS_HORIZONTAL);
            text.setTextOrigin(VPos.CENTER);
        }
        return text;
    }

    /**
     * écrit les statistiques en bas de l'écran
     */
    private Text newStats() {
        if (!invalidData()) {
            return new Text(String.format("Longueur : %.1f km" +
                            "     Montée : %.0f m" +
                            "     Descente : %.0f m" +
                            "     Altitude : de %.0f m à %.0f m",
                    getElevationProfile().length() / M_TO_KM,
                    getElevationProfile().totalAscent(),
                    getElevationProfile().totalDescent(),
                    getElevationProfile().minElevation(),
                    getElevationProfile().maxElevation())
            );
        } else
            return null;
    }

    /**
     * Gestion des évènements de la souris
     */
    private void mouseHandler() {
        pane.setOnMouseMoved(mouseEvent -> {
            Point2D mouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
            if (getRectangle().contains(mouse)) {
                double newX = screenToWorld.get().transform(mouse).getX();
                mousePositionOnProfile.set(newX);
            } else
                if (mousePositionOnProfile.get()!=NULL_POSITION){
                    mousePositionOnProfile.set(NULL_POSITION);
                }
        });
        pane.setOnMouseExited(mouseEvent -> {
            mousePositionOnProfile.set(NULL_POSITION);
        });
    }

    /**
     * Lie le rectangle 2d aux dimensions du pane
     */
    private void bindRectangle2D() {
        rectangle2D.bind(Bindings.createObjectBinding(() -> new Rectangle2D(INSETS.getLeft()
                        , INSETS.getTop()
                        , Math.max(pane.getWidth() - INSETS.getRight() - INSETS.getLeft(), 0)
                        , Math.max(pane.getHeight() - INSETS.getBottom() - INSETS.getTop(), 0))
                , pane.widthProperty()
                , pane.heightProperty()));
    }

    /**
     * On écoute la modification de la taille de l'écran. Update stepPosition, stepElevation, Polygon et Grid
     */
    private void listenScreenChange() {
        rectangle2D.addListener((j, a, m) -> updateScreen());
    }

    /**
     * Auditeur lors du changement de profile
     */
    private void listenProfile() {
        elevationProfile.addListener((b, o, n) -> updateScreen());
    }

    /**
     * vérifie si les datas sont bien valides
     */
    private boolean invalidData() {
        return (getElevationProfile() == null || getRectangle() == null);
    }

    //=================== Méthodes de propriétés ================//

    /**
     * Retourne le profile sous la forme d'une valeur de type ReadOnlyObjectProperty<…>
     *
     * @return le profile sous la forme d'une valeur de type ReadOnlyObjectProperty<…>
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileReadOnlyObjectProperty() {
        return elevationProfile;
    }

    /**
     * Retourne le profile
     *
     * @return le profile
     */
    public ElevationProfile getElevationProfile() {
        return elevationProfile.get();
    }

    /**
     * Retourne la position sous la forme d'une valeur de type ReadOnlyDoubleProperty<…>
     *
     * @return la position sous la forme d'une valeur de type ReadOnlyDoubleProperty<…>
     */
    public ReadOnlyDoubleProperty readOnlyHighlightedPosition() {
        return highlightedPosition;
    }

    /**
     * Retourne le contenu de la propriété du meme nom
     *
     * @return le contenu de la propriété du meme nom
     */
    public double getHighlightedPosition() {
        return highlightedPosition.get();
    }

    /**
     * Retourne le rectangle2d
     *
     * @return le rectangle2d
     */
    private Rectangle2D getRectangle() {
        return rectangle2D.get();
    }

    private Transform getScreenToWorld() {
        return screenToWorld.get();
    }

    private Transform getWorldToScreen() {
        return worldToScreen.get();
    }

    /**
     * la position de la souris
     */
    private double getMousePosition() {
        return mousePositionOnProfile.getValue();
    }
}