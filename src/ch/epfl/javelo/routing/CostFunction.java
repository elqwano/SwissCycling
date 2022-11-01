package ch.epfl.javelo.routing;

/**
 * représente une fonction de coût.
 * @author valentin dupraz (315995)
 */
public interface CostFunction {
    /**
     *  retourne le facteur par lequel la longueur de l'arête d'identité edgeId, partant du nœud d'identité nodeId,
     *  doit être multipliée; ce facteur doit impérativement être supérieur ou égal à 1.
     * @param nodeId l'arête edgeId doit être l'une des arêtes sortant du nœud d'identité nodeId
     * @param edgeId l'identité de l'arête, et pas son index dans la liste des arêtes sortant du nœud d'identité nodeId.
     *              En d'autres termes, il s'agit d'un entier compris entre 0 et le nombre d'arêtes dans le graphe.
     * @return  le facteur par lequel la longueur de l'arête d'identité edgeId
     */
    double costFactor(int nodeId, int edgeId);
}
