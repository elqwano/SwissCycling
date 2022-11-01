package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;


/**
 * Représente le graphe JaVelo
 * @author Quentin Anglio (313052)
 */
public final class Graph {

    private final GraphNodes graphNodes;
    private final GraphSectors graphSectors;
    private final GraphEdges graphEdges;
    private final List<AttributeSet> attributeSets;

    private final static String PATH_NODES = "nodes.bin";
    private final static String PATH_SECTORS = "sectors.bin";
    private final static String PATH_EDGES = "edges.bin";
    private final static String PATH_PROFILE_IDS = "profile_ids.bin";
    private final static String PATH_ELEVATIONS = "elevations.bin";
    private final static String PATH_ATTRIBUTES = "attributes.bin";

    /**
     * Constructeur retournant le graphe avec les nœuds, secteurs, arêtes et ensembles d'attributs donnés.
     * @param nodes Ensemble de noeuds du Graph
     * @param sectors Ensemble de secteurs du Graph
     * @param edges Ensemble de arêtes du Graph
     * @param attributeSets Ensemble d'attributs du Graph
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets){
        this.graphNodes = nodes;
        this.graphSectors = sectors;
        this.graphEdges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * retourne le graphe JaVelo obtenu à partir des fichiers se trouvant
     * dans le répertoire dont le chemin d'accès est basePath
     * @param basePath chemin d'accès
     * @return le graphe JaVelo
     * @throws IOException en cas d'erreur d'entrée/sortie, p. ex. si l'un des fichiers attendu n'existe pas
     */
    public static Graph loadFrom(Path basePath) throws IOException {
        /*contient les nœuds du graphe JaVelo*/
        IntBuffer nodesBuffer = mapIntBuffer(basePath.resolve(PATH_NODES));
        GraphNodes graphNodes = new GraphNodes(nodesBuffer);
        /*contient les secteurs*/
        ByteBuffer sectorBuffer = mapByteBuffer(basePath.resolve(PATH_SECTORS));
        GraphSectors graphSectors = new GraphSectors(sectorBuffer);
        /*contient les arêtes du graphe JaVelo (premier ensemble d'attributs)*/
        ByteBuffer edgesBuffer = mapByteBuffer(basePath.resolve(PATH_EDGES));
        /*contient les types et index de premier échantillon des profils des arêtes*/
        IntBuffer profilesBuffer = mapIntBuffer(basePath.resolve(PATH_PROFILE_IDS));
        /*contient les échantillons des profils*/
        ShortBuffer elevationsBuffer = mapShortBuffer(basePath.resolve(PATH_ELEVATIONS));
        GraphEdges graphEdges = new GraphEdges(edgesBuffer, profilesBuffer, elevationsBuffer);
        /*contient les ensembles d'attributs OSM des arêtes*/
        LongBuffer attributesBuffer = mapLongBuffer(basePath.resolve(PATH_ATTRIBUTES));
        List<AttributeSet> attributeSets = new ArrayList<>() ;
        /* On ajoute un attributeSet pour chaque entrée du buffer*/
        for (int i = 0; i < attributesBuffer.capacity(); i++){
            attributeSets.add(new AttributeSet(attributesBuffer.get(i)));   //
        }
        return new Graph(graphNodes,graphSectors,graphEdges,attributeSets );
    }

    /**
     * retourne le nombre total de nœuds dans le graphe
     * @return le nombre total de nœuds dans le graphe
     */
    public int nodeCount(){
        return this.graphNodes
                .count();
    }

    /**
     *  retourne la position du nœud d'identité donnée,
     * @param nodeId Identité du noeud
     * @return la position du nœud d'identité donnée
     */
    public PointCh nodePoint(int nodeId){
        return new PointCh(this.graphNodes.nodeE(nodeId), this.graphNodes.nodeN(nodeId));
    }

    /**
     * retourne le nombre d'arêtes sortant du nœud d'identité donnée,
     * @param nodeId Identité du noeud
     * @return le nombre d'arêtes sortant du nœud d'identité donnée
     */
    public int nodeOutDegree(int nodeId){
        return this.graphNodes
                .outDegree(nodeId);
    }

    /**
     * retourne l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId,
     * @param nodeId Identité du noeud
     * @param edgeIndex index de l'arête
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex){
        return this.graphNodes
                .edgeId(nodeId,edgeIndex);
    }

    /**
     * retourne l'identité du nœud se trouvant le plus proche du point donné, à la distance maximale donnée
     * (en mètres), ou -1 si aucun nœud ne correspond à ces critères,
     * @param point Point de recherche
     * @param searchDistance Distance de recherche
     * @return l'identité du nœud se trouvant le plus proche du point donné
     */
    public int nodeClosestTo(PointCh point, double searchDistance){

        int node = -1;
        double squaredDistance = searchDistance * searchDistance;

        List<GraphSectors.Sector> sectors = graphSectors.sectorsInArea(point,searchDistance);
        for (GraphSectors.Sector s : sectors) {
            for (int id = s.startNodeId(); id < s.endNodeId(); id++){
                PointCh p = nodePoint(id); //new PointCh(graphNodes.nodeE(id), graphNodes.nodeN(id));
                if (point.squaredDistanceTo(p) <= squaredDistance){
                    node = id;
                    squaredDistance = point.squaredDistanceTo(p);
                }
            }
        }
        return node;
    }

    /**
     * retourne l'identité du nœud destination de l'arête d'identité donnée,
     * @param edgeId Identité de l'arête
     * @return l'identité du nœud destination de l'arête d'identité donnée
     */
    public int edgeTargetNodeId(int edgeId){
        return this.graphEdges
                .targetNodeId(edgeId);
    }

    /**
     * retourne vrai ssi l'arête d'identité donnée va dans le sens contraire de la voie OSM dont elle provient,
     * @param edgeId Identité de l'arête
     * @return vrai ssi l'arête d'identité donnée va dans le sens contraire de la voie OSM dont elle provient
     */
    public boolean edgeIsInverted(int edgeId){
        return this.graphEdges
                .isInverted(edgeId);
    }

    /**
     * retourne l'ensemble des attributs OSM attachés à l'arête d'identité donnée,
     * @param edgeId Identité de l'arête
     * @return l'ensemble des attributs OSM attachés à l'arête d'identité donnée
     */
    public AttributeSet edgeAttributes(int edgeId){
        return attributeSets
                .get(this.graphEdges.attributesIndex(edgeId));
    }

    /**
     * retourne la longueur, en mètres, de l'arête d'identité donnée,
     * @param edgeId Identité de l'arête
     * @return longueur, en mètres, de l'arête d'identité donnée
     */
    public double edgeLength(int edgeId){
        return this.graphEdges
                .length(edgeId);
    }

    /**
     * retourne le dénivelé positif total de l'arête d'identité donnée,
     * @param edgeId Identité de l'arête
     * @return dénivelé positif total de l'arête d'identité donnée
     */
    public double edgeElevationGain(int edgeId){
        return this.graphEdges
                .elevationGain(edgeId);
    }

    /**
     * retourne le profil en long de l'arête d'identité donnée, sous la forme d'une fonction;
     * si l'arête ne possède pas de profil, alors cette fonction doit retourner Double.NaN pour n'importe quel argument.
     * @param edgeId Identité de l'arête
     * @return le profil en long de l'arête d'identité donnée
     */
    public DoubleUnaryOperator edgeProfile(int edgeId){
        return graphEdges.hasProfile(edgeId) ?
                Functions.sampled(this.graphEdges.profileSamples(edgeId),edgeLength(edgeId)) :
                Functions.constant(Double.NaN) ;
    }

    //==================================//
    private static IntBuffer mapIntBuffer(Path path) throws IOException{
        IntBuffer buffer;
        try (FileChannel channel = FileChannel.open(path)) {
            buffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0 , channel.size())
                    .asIntBuffer();
        }
        return buffer;
    }
    private static ByteBuffer mapByteBuffer(Path path) throws IOException{
        ByteBuffer buffer;
        try (FileChannel channel = FileChannel.open(path)) {
            buffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0 , channel.size()); //La méthode map retourne un MappedByteBuffer qui est une sous-classe de ByteBuffer, donc il n’y a pas besoin d’utiliser de méthodes de conversion dans ce cas.
        }
        return buffer;
    }
    private static ShortBuffer mapShortBuffer(Path path) throws IOException{
        ShortBuffer buffer;
        try (FileChannel channel = FileChannel.open(path)) {
            buffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0 , channel.size())
                    .asShortBuffer();
        }
        return buffer;
    }
    private static LongBuffer mapLongBuffer(Path path) throws IOException{
        LongBuffer buffer;
        try (FileChannel channel = FileChannel.open(path)) {
            buffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0 , channel.size())
                    .asLongBuffer();
        }
        return buffer;
    }


}
