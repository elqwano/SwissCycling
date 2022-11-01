package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un itinéraire multiple, c.-à-d. composé d'une séquence d'itinéraires contigus nommés segments.
 * @author Quentin Anglio (313052)
 */
public final class MultiRoute implements Route{

    private final List<Route> segments;

    /**
     * Construit un itinéraire multiple composé des segments donnés,ou lève IllegalArgumentException
     * si la liste des segments est vide.
     * Cette dernière propriété est notamment utilisée dans RouteBean
     * @param segments Liste des segments (Routes)
     */
    public MultiRoute(List<Route> segments){
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);
    }

    /**
     * retourne l'index du segment de l'itinéraire contenant la position donnée;
     * @param position Position de recherche
     * @return l'index du segment de l'itinéraire contenant la position donnée;
     */
    @Override
    public int indexOfSegmentAt(double position) {
        double pos = clampedPosition(position);
        int index = 0;

        for (Route s : segments){
            if (s.length() < pos ){
                index += s.indexOfSegmentAt(s.length()) + 1;
                pos -= s.length();
            } else {
                index += s.indexOfSegmentAt(pos);
                return index;
            }
        }
        return index;
    }


    /**
     * retourne la longueur de l'itinéraire, en mètres
     * @return la longueur de l'itinéraire, en mètres
     */
    @Override
    public double length() {
        double length = 0;
        for (Route s : segments){
            length += s.length();
        }
        return length;
    }

    /**
     * retourne la totalité des arêtes de l'itinéraire
     * @return la totalité des arêtes de l'itinéraire
     */
    @Override
    public List<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (Route s : segments){
            edges.addAll(s.edges());
        }
        return edges;
    }

    /**
     * retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire, sans doublons,
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire, sans doublons,
     */
    @Override
    public List<PointCh> points() {
        List<PointCh> points = new ArrayList<>();
        List<Edge> edges = edges();
        points.add(edges
                .get(0)
                .fromPoint());
        for (Edge edge : edges) {
            PointCh pTo = edge.toPoint();
            if (!points.contains(pTo))
                points.add(pTo);
        }
        return points;
    }

    /**
     * retourne le point se trouvant à la position donnée le long de l'itinéraire,
     * @param position Position de recherche
     * @return le point se trouvant à la position donnée le long de l'itinéraire,
     */
    @Override
    public PointCh pointAt(double position) {
        double pos = clampedPosition(position);
        int index = indexOfSegmentAtSimplified(pos);
        pos = reducedPosition(position,index);
        return segments.get(index)
                .pointAt(pos);
    }

    /**
     * retourne l'altitude à la position donnée le long de l'itinéraire, qui peut valoir NaN si l'arête
     * contenant cette position n'a pas de profil,
     * @param position Position de recherche
     * @return l'altitude à la position donnée le long de l'itinéraire, qui peut valoir NaN si l'arête
     * contenant cette position n'a pas de profil,
     */
    @Override
    public double elevationAt(double position) {
        double pos = clampedPosition(position);
        int index = indexOfSegmentAtSimplified(pos);
        pos = reducedPosition (position, index);
        return segments.get(index)
                .elevationAt(pos);
    }

    /**
     * retourne l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée,
     * @param position Position de recherche
     * @return l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée,
     */
    @Override
    public int nodeClosestTo(double position) {
        double pos = clampedPosition(position);
        int index = indexOfSegmentAtSimplified(pos);
        pos = reducedPosition (position, index);
        return segments.get(index)
                .nodeClosestTo(pos);
    }

    /**
     * retourne le point de l'itinéraire se trouvant le plus proche du point de référence donné.
     * @param point Point de recherche
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné.
     */
    @Override
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint current = RoutePoint.NONE;
        double length = 0;
        for (Route segment : segments) {
            RoutePoint temp = segment.pointClosestTo(point);
            current = current.min(temp.withPositionShiftedBy(length));
            length += segment.length();
        }
        return current;
    }


    //===========================================================//
    /**
     * les méthodes prenant des positions doivent en accepter une quelconque: une position négative est considérée
     * comme équivalente à 0, tandis qu'une position supérieure à la longueur de l'itinéraire est considérée
     * comme équivalente à cette longueur.
     * @param position Position de recherche
     * @return la position clampée
     */
    private double clampedPosition(double position){
        return Math2.clamp(0,position,this.length());
    }

    /**
     * Soustrait à "position" les longueurs des routes précédant celle d'index "index"
     * @param position Position de recherche
     * @param index index donné
     * @return position réduite
     */
    private double reducedPosition(double position, int index){
        for (int i =0; i < index; i++){
            position -= segments.get(i).length();
        }
        return position;
    }

    /**
     * Une autre methode indexOfSegmentAt qui renvoi l'index dans la liste ¨Segments"
     * sans rajouter des index pour les multiroutes
     * @param position Position de recherche
     * @return l'index dans la liste ¨Segments" sans rajouter des index pour les multiroutes
     */
    private int indexOfSegmentAtSimplified(double position) {
        double pos = clampedPosition(position);         //TODO on utilise 2x clampedPosition()
        int index = 0;

        for (Route s : segments){
            if (s.length() < pos ){
                index ++;
                pos -= s.length();
            } else {
                break;
            }
        }
        return index;
    }

}
