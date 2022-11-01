package ch.epfl.javelo.projection;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;


/**
 * représentation d'un point dans le système de coordonnées suisse
 * @author valentin dupraz (315995)
 * @author Quentin Anglio (313052)
 */
public record PointCh(double e, double n) {

    /* Le constructeur vérifie que le point soit contenu dans les limites de la suisse */
    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * retourne le carré de la distance en mètres séparant le récepteur (this) de l'argument that
     * @param that point d'arrivée du vecteur
     * @return carré de la distance entre this et that
     */
    public double squaredDistanceTo(PointCh that) {
        return Math2.squaredNorm(that.e()- this.e, that.n() - this.n);
    }

    /**
     * retourne la distance en mètres séparant le récepteur (this) de l'argument that
     * @param that point d'arrivée du vecteur
     * @return distance entre this et that
     */
    public double distanceTo(PointCh that) {
        return Math2.norm(that.e()- this.e, that.n() - this.n);
    }

    /**
     *  retourne la longitude du point, dans le système WGS84, en radians
     * @return la longitude du point, dans le système WGS84 (en rad)
     */
    public double lon(){
       return Ch1903.lon(e,n);
    }

    /**
     * retourne la latitude du point, dans le système WGS84, en radians
     * @return la latitude du point, dans le système WGS84 (en rad)
     */
    public double lat(){
        return Ch1903.lat(e,n);
    }
}