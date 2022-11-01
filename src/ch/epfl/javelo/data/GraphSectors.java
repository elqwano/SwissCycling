package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.SwissBounds;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le tableau contenant les 16384 secteurs de JaVelo
 * @author Quentin Anglio (313052)
 *
 * @param buffer La mémoire tampon contenant la valeur des attributs de la totalité des secteurs
 */
public record GraphSectors(ByteBuffer buffer) {

    private final static int NUMBER_OF_SECTOR_PER_LINE =128;
    private final static double INDIVIDUAL_SECTOR_WIDTH = SwissBounds.WIDTH/ NUMBER_OF_SECTOR_PER_LINE;
    private final static double INDIVIDUAL_SECTOR_HEIGHT = SwissBounds.HEIGHT/ NUMBER_OF_SECTOR_PER_LINE;
    private final static int OFFSET_START =0;
    private final static int OFFSET_SHORT = OFFSET_START + Integer.BYTES;
    private final static int OFFSET_SECTOR_SIZE =  OFFSET_SHORT + Short.BYTES;

    /**
     * Retourne la liste de tous les secteurs ayant une intersection avec le carré centré au point donné et
     * de côté égal au double (!) de la distance donnée.
     * @param center Centre du carré considéré
     * @param distance Distance de recherche
     * @return ArrayList<Sector> la liste de tous les secteurs ayant une intersection avec le carré centré
     *      au point donné et de côté égal au double (!) de la distance donnée.
     */
    public List<Sector> sectorsInArea(PointCh center, double distance){

        int xMin = Math2.clamp(0,(int)((center.e() - distance - SwissBounds.MIN_E) / INDIVIDUAL_SECTOR_WIDTH),
                NUMBER_OF_SECTOR_PER_LINE-1);        //On clamp  dans les limites du grand secteur
        int xMax = Math2.clamp(0,(int)((center.e() + distance - SwissBounds.MIN_E) / INDIVIDUAL_SECTOR_WIDTH),
                NUMBER_OF_SECTOR_PER_LINE-1);        //idem
        int yMin = Math2.clamp(0,(int)((center.n() - distance -SwissBounds.MIN_N) / INDIVIDUAL_SECTOR_HEIGHT),
                NUMBER_OF_SECTOR_PER_LINE-1);        //idem
        int yMax = Math2.clamp(0,(int)((center.n() + distance -SwissBounds.MIN_N) / INDIVIDUAL_SECTOR_HEIGHT),
                NUMBER_OF_SECTOR_PER_LINE-1);        //idem

        ArrayList<Sector> sectors = new ArrayList<>();

        for (int x = xMin; x <= xMax; ++x){
            for (int y =yMin; y <= yMax;++y){
                int index = x + NUMBER_OF_SECTOR_PER_LINE * y;
                int startNodeId = buffer.getInt(index * OFFSET_SECTOR_SIZE);
                int nbNodeInSector = Short.toUnsignedInt(buffer
                        .getShort(index * OFFSET_SECTOR_SIZE + OFFSET_SHORT));
                sectors.add(new Sector(startNodeId,startNodeId+nbNodeInSector));
            }
        }
        return sectors;
    }

    /**
     * Contrairement au buffer où les secteurs sont représentés par un index de nœud et une longueur,
     * ils sont représentés par deux index de nœuds dans cette classe
     * @param startNodeId l'identité (index) du premier nœud du secteur,
     * @param endNodeId l'identité (index) du nœud situé juste après le dernier nœud du secteur.
     */
    public record Sector(int startNodeId, int endNodeId){}
}
