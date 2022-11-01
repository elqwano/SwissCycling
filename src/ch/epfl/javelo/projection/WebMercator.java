package ch.epfl.javelo.projection;
import ch.epfl.javelo.Math2;

/**
 * offre des méthodes statiques, qui permettent de convertir entre les coordonnées WGS 84 et Web Mercator
 * @author valentin dupraz (315995)
 */
public final class WebMercator {
    private WebMercator() {}

        /**
         * retourne la coordonnée x de la projection d'un point se trouvant à la longitude lon selon WGS 84,
         * @param lon in rad
         * @return Coordonnée x en mètres selon Web Mercator
         */
        public static double x(double lon){
            return 1/(2 * Math.PI) * (lon+ Math.PI);
        }

        /**
         * retourne la coordonnée y de la projection d'un point se trouvant à la latitude lat selon WGS 84,
         * @param lat in rad
         * @return Coordonnée y en mètres selon Web Mercator
         */
        public static double y(double lat){
            return 1/(2 * Math.PI) * (Math.PI - Math2.asinh( Math.tan(lat) ));
    }

        /**
         * retourne la longitude, en radians, d'un point dont la projection se trouve à la coordonnée x selon Web Mercator
         * @param x en mètres
         * @return longitude en rad selon WGS 84
         */
        public static double lon(double x){
            return 2 * Math.PI * x - Math.PI;
    }

        /**
         * retourne la latitude, en radians, d'un point dont la projection se trouve à la coordonnée y selon Web Mercator
         * @param y en mètres
         * @return latitude en rad selon WGS84
         */
        public static double lat(double y){
            return  Math.atan(Math
                .sinh(Math.fma(-2 *  Math.PI, y, Math.PI)));
    }
}