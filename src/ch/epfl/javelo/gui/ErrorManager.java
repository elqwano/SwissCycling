package ch.epfl.javelo.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * gère l'affichage de messages d'erreur
 *
 * @author valentin dupraz (315995)
 */
public final class ErrorManager {
    private final VBox pane;
    private final Text text;
    private final static double TOTALLY_TRANSPARENT = 0.0;
    private final static double ALMOST_OPAQUE = 0.8;
    private final static int DUREE1 = 200;
    private final static int DUREE2 = 2000;
    private final static int DUREE3 = 500;


    /**
     * le constructeur de ErrorManager
     */
    public ErrorManager() {
        text = new Text();
        pane = new VBox(text);
        pane.getStylesheets().add("error.css");
        pane.setMouseTransparent(true);
    }

    /**
     * retournant le panneau, de type Pane, sur lequel apparaissent les messages d'erreur
     *
     * @return le panneau, sur lequel apparaissent les messages d'erreur
     */
    public Pane pane() {
        return pane;
    }

    /**
     * fait apparaître temporairement à l'écran une chaîne de caractères représentant un message d'erreur,
     * accompagné d'un son indiquant l'erreur
     *
     * @param message une chaîne de caractères représentant un (court) message d'erreur
     */
    public void displayError(String message) {
        java.awt.Toolkit.getDefaultToolkit().beep();

        text.setText(message);
        FadeTransition ft = new FadeTransition(Duration.millis(DUREE1), pane);
        ft.setFromValue(TOTALLY_TRANSPARENT);
        ft.setToValue(ALMOST_OPAQUE);

        PauseTransition pt = new PauseTransition(Duration.millis(DUREE2));

        FadeTransition ft2 = new FadeTransition(Duration.millis(DUREE3), pane);
        ft2.setFromValue(ALMOST_OPAQUE);
        ft2.setToValue(TOTALLY_TRANSPARENT);

        SequentialTransition seqT = new SequentialTransition(pane, ft, pt, ft2);
        seqT.stop();
        seqT.play();
    }
}