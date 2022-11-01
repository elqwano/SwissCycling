package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * cet enregistrement est uniquement destiné à être utilisé pour représenter les arêtes appartenant à un itinéraire,
 * et non la totalité des arêtes du graphe. Son but est de collecter toutes les informations relatives à une arête
 * d'itinéraire, qui pourraient être obtenues par des appels aux méthodes de Graph mais qu'il est plus commode
 * d'avoir à disposition directement dans un objet.
 * @author Quentin Anglio (313052)
 *
 * @param fromNodeId l'identité du nœud de départ de l'arête,
 * @param toNodeId l'identité du nœud d'arrivée de l'arête,
 * @param fromPoint le point de départ de l'arête,
 * @param toPoint le point d'arrivée de l'arête,
 * @param length  la longueur de l'arête, en mètres,
 * @param profile  le profil en long de l'arête.
 */
public record Edge(int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint, double length, DoubleUnaryOperator profile) {


    /**
     * retourne une instance de Edge dont les attributs fromNodeId et toNodeId sont ceux donnés,
     * les autres étant ceux de l'arête d'identité edgeId dans le graphe Graph.
     * @param graph le graph
     * @param edgeId l'identité de l'arête
     * @param fromNodeId noeud de départ
     * @param toNodeId noeud d'arrivée
     * @return une instance de Edge dont les attributs fromNodeId et toNodeId sont ceux donnés
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId){
        return new Edge(fromNodeId, toNodeId,
                graph.nodePoint(fromNodeId), graph.nodePoint(toNodeId),
                graph.edgeLength(edgeId), graph.edgeProfile(edgeId) );
    }

    /**
     * retourne la position le long de l'arête, en mètres, qui se trouve la plus proche du point donné,
     * @param point point donné
     * @return la position le long de l'arête, en mètres, qui se trouve la plus proche du point donné
     */
    public double positionClosestTo(PointCh point){
        return Math2.projectionLength(
                this.fromPoint.e(),this.fromPoint.n(),
                this.toPoint.e(), this.toPoint.n(),
                point.e(),point.n());
    }

    /**
     * retourne le point se trouvant à la position donnée sur l'arête, exprimée en mètres,
     * @param position position de recherche
     * @return le point se trouvant à la position donnée sur l'arête
     */
    public PointCh pointAt(double position){
        // le poucentage de la longeur de l'edge correspondant à position
        double percent = position/this.length;
        //On ajoute ce même poucentage pour les coordonnées e et n
        double eNew = ((toPoint.e()-fromPoint.e()) * percent) + fromPoint.e();
        double nNew = ((toPoint.n()-fromPoint.n()) * percent) + fromPoint.n();
        return new PointCh(eNew,nNew);
    }

    /**
     * retourne l'altitude, en mètres, à la position donnée sur l'arête.
     * @param position position de recherche
     * @return l'altitude, en mètres, à la position donnée sur l'arête.
     */
    public double elevationAt(double position){
        return profile.applyAsDouble(position);

    }


}

