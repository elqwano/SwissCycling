package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.PriorityQueue;

/** Calcule l'itinéraire d'un ségment
 * @author valentin dupraz (315995)
 *
 */
public final class RouteComputer {
    private final static float UNREACHABLE = Float.NEGATIVE_INFINITY;
    private final Graph graph;
    private final CostFunction costFunction;

    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * @param startNodeId le noeud de départ
     * @param endNodeId le noeud d'arrivé
     * @return un itinéraire de coût total minimal allant de startNodeId à endNodeId dans le graphe passé au
     * @throws IllegalArgumentException mauvais argument
     * constructeur ou null si aucun itinéraire n'existe
     */
    public Route bestRouteBetween(int startNodeId, int endNodeId) {
        Preconditions.checkArgument(startNodeId != endNodeId);

        /* contient à la fois l'identité d'un nœud et sa distance. */
        record WeightedNode(int nodeId, float distance) implements Comparable<WeightedNode> {
            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.distance, that.distance);
            }
        }

        /* initialisation des structures de données */
        PriorityQueue<WeightedNode> exploring = new PriorityQueue<>();
        exploring.add(new WeightedNode(startNodeId, 0));
        int nbNodes = graph.nodeCount();
        float[] distance = new float[nbNodes];
        Arrays.fill(distance, Float.POSITIVE_INFINITY);
        distance[startNodeId] = 0f;
        int[] predecessor = new int[nbNodes];
        
        while (!exploring.isEmpty()) {
            WeightedNode w = exploring.remove();
            int currentNode = w.nodeId();
            if (distance[currentNode] != UNREACHABLE) {

                /* si le plus court chemin a été trouvé : reconstruction de l'itinéraire à partir des noeuds */
                if (currentNode == endNodeId) {
                    ArrayList<Edge> edges = new ArrayList<>();

                    while (currentNode != startNodeId) {
                        int predecesseur = predecessor[currentNode];
                        int outEdges = graph.nodeOutDegree(predecesseur);
                        for (int i = 0; i < outEdges; i++) {
                            if (graph.edgeTargetNodeId(graph.nodeOutEdgeId(predecesseur, i)) == currentNode) {
                                edges
                                    .add(Edge
                                        .of(graph, graph.nodeOutEdgeId(predecesseur, i), predecesseur, currentNode));
                                break;
                            }
                        }
                        currentNode = predecessor[currentNode];
                    }
                    Collections.reverse(edges);
                    return new SingleRoute(edges);
                }

                /* nombre d'arêtes sortant de node */
                int outEdges = graph.nodeOutDegree(currentNode);
                
                for (int i = 0; i < outEdges; i++) {
                    /* l'identitée de l'arrête actuelle */
                    int currentEdge = graph.nodeOutEdgeId(currentNode, i);
                    /* le nœud d'arrivée de l'arrête actuelle */
                    int currentEdgeOut = graph.edgeTargetNodeId(currentEdge);

                    if(distance[currentEdgeOut] == UNREACHABLE){
                        continue;
                    }

                    double cost = costFunction.costFactor(currentEdgeOut, currentEdge);
                    float dist = (float) (distance[currentNode] + cost * graph.edgeLength(currentEdge));
                    float volOiseau = (float) graph
                            .nodePoint(currentEdgeOut)
                            .distanceTo(graph
                                    .nodePoint(endNodeId));

                    if (dist < distance[currentEdgeOut]) {
                        distance[currentEdgeOut] = dist;
                        predecessor[currentEdgeOut] = currentNode;
                        exploring.add(new WeightedNode(currentEdgeOut, dist + volOiseau));
                    }
                }
                distance[currentNode] = UNREACHABLE;
            }
        }
            return null;
    }
}