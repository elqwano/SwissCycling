package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;
import static java.util.Arrays.binarySearch;

/**
 * La classe représente un itinéraire simple, c.-à-d. reliant un point de départ à un point d'arrivée,
 * sans point de passage intermédiaire
 * @author valentin dupraz (315995)
 * @author Quentin Anglio (313052)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges;
    private final double[] positionEdges;

    /**
     * Contructeur d'un itinéraire simple composé des arêtes données.
     * Vérifie que la liste ne soit pas vide
     *
     * @param edges Liste d'arête
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(!edges.isEmpty());
        this.edges = List.copyOf(edges);
        this.positionEdges = initializePositionEdges();
    }

    /**
     * Retourne l'index du segment de l'itinéraire contenant la position donnée, qui vaut 0 pour un itinéraire simple
     * @param position la position de recherche
     * @return l'index du segment de l'itinéraire contenant la position donnée, qui vaut 0 pour un itinéraire simple
     */
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * Retourne la longueur de l'itinéraire, (en m)
     * @return la longueur de l'itinéraire, (en m)
     */
    public double length() {
        double sum = 0;
        for (Edge edge : edges) {
            sum += edge.length();
        }
        return sum;
    }

    /**
     * Retourne la totalité des arêtes de l'itinéraire
     * @return la totalité des arêtes de l'itinéraire
     */
    public List<Edge> edges() {
        return edges;
    }

    /** Retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire
     */
    public List<PointCh> points() {
        List<PointCh> liste = new ArrayList<>();
        liste.add(edges
                .get(0)
                .fromPoint());
        for (Edge edge : edges) {
            liste.add(edge
                    .toPoint());
        }
        return liste;
    }

    /** Retourne le point se trouvant à la position donnée le long de l'itinéraire
     * @param position Distance de recherche
     * @return le point se trouvant à la position donnée le long de l'itinéraire
     */
    public PointCh pointAt(double position) {
        double pos = clampedAndReducedPosition(position);
        return edges.get(index(position))
                    .pointAt(pos);
    }

    /** Retourne l'altitude à la position donnée le long de l'itinéraire
     * (ou NaN si l'arête contenant cette position n'a pas de profil)
     * @param position Distance de recherche
     * @return l'altitude à la position donnée le long de l'itinéraire
     *      (ou NaN si l'arête contenant cette position n'a pas de profil)
     */
    public double elevationAt(double position) {
        double pos = clampedAndReducedPosition(position);
        return edges.get(index(position))
                .elevationAt(pos);
    }

    /** Retourne l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     * @param position Distance de recherche
     * @return l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     */
    public int nodeClosestTo(double position) {
        boolean condition = position - positionEdges[index(position)] <= edges.get(index(position)).length() / 2.0;
        return condition ? edges.get(index(position)).fromNodeId() : edges.get(index(position)).toNodeId();
    }

    /**
     * Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donné
     * @param point Point de recherche
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné
     */
    public RoutePoint pointClosestTo(PointCh point) {
        RoutePoint closest = RoutePoint.NONE;
        int i = 0;
        for (Edge edge : edges) {
            double position = Math2.clamp(0, edge.positionClosestTo(point), edge.length());
            double thatPosition = Math.abs(position) + positionEdges[i];
            double thatDistanceToRef = point
                    .distanceTo(edge
                            .pointAt(position));
            closest = closest.min(edge.pointAt(position), thatPosition, thatDistanceToRef);
            ++i;
        }
        return closest;
    }


    //===========================================================//
    private double[] initializePositionEdges(){
        double[] edgesPositionTab = new double[edges.size()+1];
        edgesPositionTab[0] = 0.0;
        for (int i = 1; i < edges.size()+1; i++) {
            edgesPositionTab[i] = edgesPositionTab[i-1] + edges.get(i-1).length();
        }
        return edgesPositionTab;
    }

    private double clampedAndReducedPosition(double position){
        return Math2.clamp(0, position, length())- positionEdges[index(position)];
    }

    /* Retourne l'index de l'arrête dans tableau */
    private int index(double position) {
        int x = binarySearch(positionEdges, Math2.clamp(0, position, length()));

        if (x >= positionEdges.length - 1)
            return x-1;
        else
            return (x >= 0) ? x : (-x - 2);
    }
}