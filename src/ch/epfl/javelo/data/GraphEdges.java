package ch.epfl.javelo.data;

import ch.epfl.javelo.Bits;
import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * représente le tableau de toutes les arêtes du graphe JaVelo.
 * @author Quentin Anglio (313052)
 * @author valentin dupraz (315995)
 *
 * @param edgesBuffer la mémoire tampon contenant pour chaque arête du graphe un entier de type int (sens de l'arête
 *                    et identité du nœud destination), un entier de type short (longueur de l'arête), un entier
 *                    de type short (dénivelé positif total) et un entier de type short
 *                    (identité de l'ensemble des attributs OSM).
 * @param profileIds la mémoire tampon contenant pour chaque arête du graphe, un seul entier de type int
 *                   (type du profil et index du premier échantillon).
 * @param elevations  la mémoire tampon contenant la totalité des échantillons des profils, compressés ou non
 */
public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    private final static int OFFSET_ID = 0;
    private final static int OFFSET_LENGTH = OFFSET_ID + Integer.BYTES;
    private final static int OFFSET_DENIV = OFFSET_LENGTH + Short.BYTES;
    private final static int OFFSET_OSM = OFFSET_DENIV + Short.BYTES;
    private final static int OFFSET_EDGE_SIZE = OFFSET_OSM + Short.BYTES;
    private final static int OFFSET_PROFIL_ID = 30;
    private final static int OFFSET_PROFIL_TYPE = 2;
    private final static int SIZE_Q44 = 8;
    private final static int SIZE_Q04 = 4;

    /**
     * retourne vrai ssi l'arête d'identité donnée va dans le sens inverse de la voie OSM dont elle provient,
     * @param edgeId L'identifiant de l'edge
     * @return  vrai ssi l'arête d'identité donnée va dans le sens inverse de la voie OSM dont elle provient
     */
    public boolean isInverted(int edgeId){
        return edgesBuffer
                .getInt(edgeId * OFFSET_EDGE_SIZE + OFFSET_ID) < 0;
    }

    /**
     *  retourne l'identité du nœud destination de l'arête d'identité donnée,
     * @param edgeId L'identifiant de l'edge
     * @return l'identité du nœud destination de l'arête d'identité donnée
     */
    public int targetNodeId(int edgeId){
        int nodeId = edgesBuffer
                .getInt(edgeId * OFFSET_EDGE_SIZE + OFFSET_ID);
        return isInverted(edgeId) ?  ~nodeId: nodeId ;
    }

    /**
     * retourne la longueur, en mètres, de l'arête d'identité donnée,
     * @param edgeId L'identifiant de l'edge
     * @return la longueur, en mètres, de l'arête d'identité donnée
     */
    public double length(int edgeId){
        return Q28_4.asDouble(Short
                .toUnsignedInt(edgesBuffer
                    .getShort(edgeId * OFFSET_EDGE_SIZE + OFFSET_LENGTH)));
    }

    /**
     * retourne le dénivelé positif, en mètres, de l'arête d'identité donnée,
     * @param edgeId L'identifiant de l'edge
     * @return le dénivelé positif, en mètres, de l'arête d'identité donnée,
     */
    public double elevationGain(int edgeId){
        return  Q28_4.asDouble(Short
                .toUnsignedInt(edgesBuffer
                        .getShort(edgeId * OFFSET_EDGE_SIZE + OFFSET_DENIV )));
    }

    /**
     * retourne l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée.
     * @param edgeId L'identifiant de l'edge
     * @return l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée.
     */
    public int attributesIndex(int edgeId){
        return Short.toUnsignedInt(edgesBuffer
                .getShort(edgeId * OFFSET_EDGE_SIZE + OFFSET_OSM ));
    }

    /**
     * retourne vrai ssi l'arête d'identité donnée possède un profil,
     * @param edgeId L'identifiant de l'edge
     * @return vrai ssi l'arête d'identité donnée possède un profil
     */
    public boolean hasProfile(int edgeId){
        return (Bits.extractUnsigned(profileIds
                .get(edgeId),OFFSET_PROFIL_ID,OFFSET_PROFIL_TYPE) != 0);
    }

    /**
     * retourne le tableau des échantillons du profil de l'arête d'identité donnée,
     * qui est vide si l'arête ne possède pas de profil,
     * @param edgeId L'identifiant de l'edge
     * @return le tableau des échantillons du profil de l'arête d'identité donnée
     */
    public float[] profileSamples(int edgeId){

        if (!hasProfile(edgeId)){
            return new float[]{};
        }

        int profilType = Bits.extractUnsigned(profileIds
                .get(edgeId), OFFSET_PROFIL_ID, OFFSET_PROFIL_TYPE);
        int index = Bits.extractUnsigned(profileIds
                .get(edgeId),0, OFFSET_PROFIL_ID);
        int nbSample =  ( 1 + Math2
                .ceilDiv(lengthQ28(edgeId), Q28_4.ofInt( OFFSET_PROFIL_TYPE )));
        float[] samples = new float[nbSample];

        samples[0] = Q28_4.asFloat(Short.toUnsignedInt(elevations
                        .get(index)));

        switch (profilType) {
            case 1 -> fillProfileSamplesType1(nbSample, samples, index);
            case 2 -> fillProfileSamplesType2(nbSample, samples, index);
            case 3 -> fillProfileSamplesType3(nbSample, samples, index);
        }

        return isInverted(edgeId) ? invertTab(samples)  : samples;
    }

    //================================================//

    /* Méthodes privées utilisées pour la création d'échantillons de profils*/
    private void fillProfileSamplesType1(int nbSample, float[] samples, int index ){
        for (int i = 1; i< nbSample;++i){
            samples[i] = Q28_4.asFloat(Short.toUnsignedInt(elevations
                            .get(index+i)));
        }
    }
    private void fillProfileSamplesType2(int nbSample, float[] samples, int index){
        int indexo = 1;
        int var = 1;
        while (indexo < nbSample ) {
            for (int i = 0; i < 2; i++) {
                if (indexo < nbSample){
                    samples[indexo] = samples[indexo - 1]
                            + Q28_4.asFloat(Bits.extractSigned((elevations
                                .get(index + var)), SIZE_Q44 * (1 - i), SIZE_Q44));
                    indexo++;
                }
            }
            var++;
        }
    }
    private void fillProfileSamplesType3(int nbSample, float[] samples, int index){
        int indexo = 1;
        int var = 1;
        while (indexo < nbSample){
            for (int i = 0; i < 4; i++) {
                if (indexo < nbSample) {
                    samples[indexo] = samples[indexo - 1]
                            + Q28_4.asFloat(Bits.extractSigned((elevations
                                .get(index + var)), SIZE_Q04 * (3 - i), SIZE_Q04));
                    indexo++;
                }
            }
            var++;
        }
    }

    /**
     * retourne la longueur, en Q28_4, de l'arête d'identité donnée,
     * @param edgeId L'identifiant de l'edge
     * @return retourne la longueur, en Q28_4, de l'arête d'identité donnée,
     */
    private int lengthQ28(int edgeId) {
        return Short.toUnsignedInt(edgesBuffer.getShort(edgeId * OFFSET_EDGE_SIZE + OFFSET_LENGTH));
    }

    /**
     * Inverse un tableau donné
     * @param tab tableau à inverser
     * @return tableau inversé
     */
    private float[] invertTab(float[] tab){
        for (int i = 0; i < tab.length / 2; i++) {
            float tmp = tab[i];
            tab[i] = tab[tab.length - i - 1];
            tab[tab.length - i - 1] = tmp;
        }
        return tab;
    }


}
