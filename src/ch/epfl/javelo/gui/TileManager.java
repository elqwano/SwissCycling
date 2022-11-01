package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.scene.image.Image;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * La classe TileManager du sous-paquetage gui, publique et finale, représente un gestionnaire de tuiles OSM.
 * Son rôle est d'obtenir les tuiles depuis un serveur de tuile ("https://tile.openstreetmap.org/")
 * et de les stocker dans un cache mémoire et dans un cache disque.
 *  @author Quentin Anglio (313052)
 */
public final class TileManager {

    private final static int CACHE_CAPACITY = 100;
    private final static String URL_PREFIX = "https://";
    private final static String IMAGE_EXTENSION = ".png";
    private final static String URL_KEY = "User-Agent";
    private final static String URL_VALUE = "JaVelo";

    private final Map<TileId, Image> memoryCache;
    private final Path accessPath;
    private final String tileServerName;

    /**
     * The constructor of a TileManager
     *
     * @param accessPath     local path of access to disk cache
     * @param tileServerName the server name
     */
    public TileManager(Path accessPath, String tileServerName) {
        memoryCache = new LinkedHashMap<>(CACHE_CAPACITY, .75f, true) {
            /* On défini le comportement des methods put() et putall() dans le cas ou le cache est plein.*/
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return memoryCache.size() > CACHE_CAPACITY;
            }
        };
        this.accessPath = accessPath;
        this.tileServerName = tileServerName;
    }

    /**
     * retourne l'image de la tile d'id ID (de type Image de la bibliothèque JavaFX)
     *
     * @param tileId identité de la tuile
     * @return une imagge de type Image
     * @throws IOException gère les exceptions liées aux flots
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        /* L'image est en mémoire dans le cache */
        if (memoryCache.containsKey(tileId)) {
            return memoryCache.get(tileId);
        }
        final Path tilePath = accessPath.resolve(getPath(tileId));
        /* L'image est en mémoire sur le disk */
        if (Files.exists(tilePath)) {
            Image image = downloadTileFromDisk(tilePath);
            memoryCache.put(tileId, image);
            return image;
        }
        /* L'image n'existe pas encore */
        downloadAndSaveTileOnDisk(tilePath, tileId);

        return imageForTileAt(tileId);
    }

    /**
     * L'enregistrement TileId, imbriqué dans la classe TileManager, représente l'identité d'une tuile OSM.
     *
     * @param zoomLevel le niveau de zoom de la tuile
     * @param xIndex    l'index X de la tuile
     * @param yIndex    l'index Y de la tuile
     */
    record TileId(int zoomLevel, int xIndex, int yIndex) {
        /**
         * Constructeur compact vérifie les arguments
         *
         * @throws IllegalArgumentException quand les arguments sont invalides
         */
        public TileId {
            Preconditions.checkArgument(isValid(zoomLevel, xIndex, yIndex));
        }

        /**
         * La méthode isValid, prenant en argument ces trois attributs (zoom et index X/Y), retourne vrai si et
         * seulement si ils constituent une identité de tuile valide.
         *
         * @param zoomLevel le niveau de zoom de la tuile
         * @param xIndex    l'index X de la tuile
         * @param yIndex    l'index Y de la tuile
         * @return true si les arguments sont valides
         */
        public static boolean isValid(int zoomLevel, int xIndex, int yIndex) {
//            double maxIndex = Math.pow(2, zoomLevel);
            double maxIndex = 2 << zoomLevel;   //==2^zoomLevel

            return (zoomLevel >= PointWebMercator.MIN_ZOOM
                    && zoomLevel <= PointWebMercator.MAX_ZOOM
                    && xIndex >= 0 && xIndex <= maxIndex
                    && yIndex >= 0 && yIndex <= maxIndex);
        }
    }

    //=============================== private ==================================//

    /**
     * Return a path of the form zoomLevel/xIndex/yIndex.IMAGE_EXTENTION
     *
     * @param tileId identity of the tile
     * @return the path
     */
    private Path getPath(TileId tileId) {
        return Path.of(String.valueOf(tileId.zoomLevel))
                .resolve(String.valueOf(tileId.xIndex))
                .resolve(tileId.yIndex + IMAGE_EXTENSION);
    }

    /**
     * Télécharge l'image depuis le disk cache
     *
     * @param tilePath l'id de la tuile
     * @return l'image
     * @throws IOException exception de flot d'entrée
     */
    private Image downloadTileFromDisk(Path tilePath) throws IOException {
        try (InputStream io = new FileInputStream(String.valueOf(tilePath))) {
            return new Image(io);
        }
    }

    /**
     * Télécharge l'image depuis le serveur de tuiles et la place dans le cache disque
     *
     * @throws IOException exception de flot d'entrée/sortie
     */
    private void downloadAndSaveTileOnDisk(Path path, TileId tileId) throws IOException {
        URL url = new URL(URL_PREFIX + tileServerName + "/" +
                tileId.zoomLevel + "/" + tileId.xIndex + "/" + tileId.yIndex + IMAGE_EXTENSION);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty(URL_KEY, URL_VALUE);
        Files.createDirectories(path.getParent());
        try (InputStream io = urlConnection.getInputStream();
             OutputStream os = new FileOutputStream(String.valueOf(path))) {
            io.transferTo(os);
        }
    }
}
