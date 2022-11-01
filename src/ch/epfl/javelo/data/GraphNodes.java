package ch.epfl.javelo.data;
import ch.epfl.javelo.Bits;

import java.nio.IntBuffer;
import static ch.epfl.javelo.Q28_4.asDouble;

/**
 * L'enregistrement représente le tableau de tous les nœuds du graphe JaVelo.
 * @author valentin dupraz (315995)
 *
 * @param buffer la mémoire tampon contenant la valeur des attributs de la totalité des nœuds du graphe
 */
public record GraphNodes(IntBuffer buffer) {

    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;
    private static final int START_OUT_EDGES = 28;
    private static final int LENGTH_OUT_EDGES = 4;
    private static final int START_EDGES_ID = 0 ;
    private static final int LENGTH_EDGES_ID = 28 ;

    /**
     * Retourne le nombre total de nœuds
     * @return le nombre total de nœuds
     */
    public int count(){
        return buffer
                .capacity()/NODE_INTS;
    }

    /**
     * Retourne la 1ere valeure de la table c-à-d la coordonnée E du nœud d'identité donnée
     * @param nodeId Identifiant du noeud
     * @return la coordonnée E du nœud d'identité donnée
     */
    public double nodeE(int nodeId){
        return asDouble(buffer
                .get(nodeId * NODE_INTS + OFFSET_E));
    }

    /**
     * Retourne la 2e valeure de la table c-à-d la coordonnée N du nœud d'identité donnée
     * @param nodeId Identifiant du noeud
     * @return la coordonnée N du nœud d'identité donnée
     */
    public double nodeN(int nodeId){
        return asDouble(buffer
                .get(nodeId * NODE_INTS + OFFSET_N));
    }

    /**
     * Retourne les 4  bits de poids fort de la 3e valeure (de 32 bits) de la table c-à-d le nombre
     * d'arêtes sortant du nœud d'identité donné
     * @param nodeId Identifiant du noeud
     * @return le nombre d'arêtes sortant du nœud d'identité donné
     */
    public int outDegree(int nodeId){
        return Bits.extractUnsigned(buffer
                .get(nodeId * NODE_INTS + OFFSET_OUT_EDGES),START_OUT_EDGES,LENGTH_OUT_EDGES);
    }

    /**
     * Retourne les 28 bits de poids faible de la troisième valeure (de 32 bits) de la table c-à-d l'identité de
     * la edgeIndex-ième arête sortant du nœud d'identité nodeId. Vérifie égallement que l'index de l'arête qu'on lui
     * passe est valide, c.-à-d. qu'il est compris entre 0 (inclus) et le nombre d'arêtes sortant du nœud (exclus).
     * @param nodeId Identifiant du noeud
     * @param edgeIndex Identifiant de l'edge
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     */
    public int edgeId(int nodeId, int edgeIndex){
        assert 0 <= edgeIndex && edgeIndex < outDegree(nodeId);
        return Bits.extractUnsigned(buffer
                .get(nodeId * NODE_INTS + OFFSET_OUT_EDGES), START_EDGES_ID, LENGTH_EDGES_ID)
                + edgeIndex;
    }
}