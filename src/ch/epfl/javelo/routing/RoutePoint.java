package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;


/**
 * La classe représente le point d'un itinéraire le plus proche d'un point de référence donné, qui se trouve
 * dans le voisinage de l'itinéraire
 *
 * @author valentin dupraz (315995)
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

    /* une constante représentant un point inexistant */
    public static final RoutePoint NONE = new RoutePoint(null, NaN, POSITIVE_INFINITY);

    /**
     * destinée à être utilisée pour transformer un point dont la position est exprimée par rapport au segment qui
     * le contient en un point équivalent mais dont la position est exprimée par rapport à l'itinéraire complet
     *
     * @param positionDifference la différence donnée pour décaler la position
     * @return un point identique au récepteur (this) mais dont la position est décalée de la différence donnée
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        return new RoutePoint(point, position + positionDifference, distanceToReference);
    }

    /**
     * retourne this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     *
     * @param that la référence donnée
     * @return this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     */
    public RoutePoint min(RoutePoint that) {
        return (this.distanceToReference <= that.distanceToReference) ? this : that;
    }

    /**
     * retourne this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon
     *
     * @param thatPoint               le point sur l'itinéraire
     * @param thatPosition            la position du point le long de l'itinéraire (en m)
     * @param thatDistanceToReference la distance (en m) entre le point et la référence
     * @return this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return (this.distanceToReference <= thatDistanceToReference) ?
                this : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }
}