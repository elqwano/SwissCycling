package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Représente les paramètres du fond de carte présenté dans l'interface graphique. Les coordonnées x et y
 * sont exprimées dans le système de coordonnées Web Mercator de l'image au niveau de zoom donné.
 *
 * @param zoomLevel le niveau de zoom
 * @param xTop      la coordonnée x du coin haut-gauche de la portion de carte affichée
 * @param yTop      la coordonnée y du coin haut-gauche de la portion de carte affichée.
 * @author Quentin Anglio (313052)
 */
public record MapViewParameters(int zoomLevel, double xTop, double yTop) {

    /**
     * Le constructeur valide les paramètres
     *
     * @throws IllegalArgumentException mauvais argument
     */
    public MapViewParameters {
        Preconditions.checkArgument(zoomLevel >= PointWebMercator.MIN_ZOOM
                && zoomLevel <= PointWebMercator.MAX_ZOOM
                && xTop >= 0
                && yTop >= 0);
    }

    /**
     * retourne les coordonnées du coin haut-gauche sous la forme d'un objet de type Point2D, le type utilisé
     * par JavaFX pour représenter les points,
     *
     * @return un Point2D
     */
    public Point2D topLeft() {
        return new Point2D(xTop(), yTop());
    }

    /**
     * retourne une instance de MapViewParameters identique au récepteur,
     * si ce n'est que les coordonnées du coin haut-gauche sont celles passées en arguments à la méthode,
     *
     * @param x nouvelle coordonnéee x
     * @param y nouvelle coordonnéee y
     * @return instance de MapViewParameters avec autres x, y
     */
    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoomLevel(), x, y);
    }

    /**
     * prend en arguments les coordonnées x et y d'un point, exprimées par rapport au coin haut-gauche
     * de la portion de carte affichée à l'écran, et retourne ce point sous la forme d'une instance de PointWebMercator,
     *
     * @param x la coordonnée x
     * @param y la coordonnée y
     * @return un PointMercator
     */
    public PointWebMercator pointAt(double x, double y) {
        return PointWebMercator.of(zoomLevel(), x + xTop(), y + yTop());
    }

    /**
     * prennent en argument un point Web Mercator et retournent la position x correspondante,
     * exprimée par rapport au coin haut-gauche de la portion de carte affichée à l'écran.
     *
     * @param point le point
     * @return la position x correspondante
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel()) - xTop;
    }

    /**
     * prennent en argument un point Web Mercator et retournent yla position y correspondante,
     * exprimée par rapport au coin haut-gauche de la portion de carte affichée à l'écran.
     *
     * @param point le point
     * @return y correspondante
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - yTop;
    }
}
