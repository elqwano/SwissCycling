package ch.epfl.javelo.projection;

/**
 * La classe Ch1903, du paquetage ch.epfl.javelo.projection, publique, finale et non instanciable,
 * offre des méthodes statiques permettant de convertir entre les coordonnées WGS 84 et les coordonnées suisses.
 * @author Quentin Anglio (313052)
 */
public final class Ch1903 {
    private Ch1903 (){}

    /**
     * retourne la coordonnée E (est) du point de longitude lon et latitude lat dans le système WGS84,
     * @param lon longitude en rad
     * @param lat latitude en rad
     * @return Coordonnée E en mètres selon CH1903+
     */
    public static double e(double lon, double lat){
        double lon1 = 1e-4 * Math.fma(3600, Math.toDegrees(lon), -26_782.5);
        double lat1 = 1e-4 * Math.fma(3600, Math.toDegrees(lat), -169_028.66);
        return 2_600_072.37 + lon1
                * (211_455.93 - 10_938.51 * lat1 - 0.36 * Math.pow(lat1, 2)
                - 44.54 * Math.pow(lon1, 2));
    }

    /**
     * retourne la coordonnée N (nord) du point de longitude lon et latitude lat dans le système WGS84
     * @param lon longitude en rad
     * @param lat latitude en rad
     * @return Coordonnée N en mètres selon CH1903+
     */
    public static double n(double lon, double lat) {
        double lon1 = 1e-4 * Math.fma(3600, Math.toDegrees(lon), -26_782.5);
        double lat1 = 1e-4 * Math.fma(3600, Math.toDegrees(lat), -169_028.66);
        return 1_200_147.070 + 3_745.25
                * Math.pow(lon1, 2) + lat1
                * (308_807.95 + 76.63 * lat1 - 194.56 * Math.pow(lon1, 2) + 119.79 * Math.pow(lat1, 2));
    }

    /**
     * retourne la longitude dans le système WGS84 du point dont les coordonnées sont e et n dans le système suisse
     * @param e en mètres (CH1903+)
     * @param n en mètres (CH1903+)
     * @return longitude en rad selon WGS84
     */
    public static double lon(double e, double n){
        double x = 1e-6 * (e - 2_600_000);
        double y = 1e-6 * (n - 1_200_000);
        return Math.toRadians(100.0/36.0
                * (2.6779094 + x * (4.728982 + y * 0.791484 + Math.pow(y,2)
                * 0.1306 - Math.pow(x,2) * 0.0436)));
    }

    /**
     * retourne la latitude dans le système WGS84 du point dont les coordonnées sont e et n dans le système suisse.
     * @param e en mètres (CH1903+)
     * @param n en mètres (CH1903+)
     * @return latitude en rad selon WGS84
     */
    public static double lat(double e, double n){
        double x = 1e-6 * (e - 2_600_000);
        double y = 1e-6 * (n - 1_200_000);
        return  Math.toRadians(100.0/36.0
                * ( 16.9023892 - 0.270978 * Math.pow(x,2)
                + y * (3.238272 - 0.002528 * y -0.0447 * Math.pow(x,2)
                - 0.014 * Math.pow(y,2))));
    }
}
