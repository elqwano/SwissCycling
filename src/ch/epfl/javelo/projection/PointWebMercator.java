package ch.epfl.javelo.projection;
import ch.epfl.javelo.Preconditions;

/**
 * La classe WebMercator du paquetage ch.epfl.javelo.projection, publique, finale et non instanciable,
 * offre les méthodes statiques suivantes, qui permettent de convertir entre les coordonnées WGS 84
 * et les coordonnées Web Mercator
 * @author Quentin Anglio (313052)
 */
public record PointWebMercator(double x, double y) {

    public final static int MIN_ZOOM = 0;       //todo déplacer
    public final static int MAX_ZOOM = 19;
    public final static int OFFSET_ZOOM = 8;

    /**
     * Le constructeur valide les coordonnées qu'il reçoit et lève une IllegalArgumentException si l'une d'entre elles n'est pas comprise dans l'intervalle [0;1]
     * @param x La coordonnée X
     * @param y La coordonnée Y
     */
    public PointWebMercator{
        Preconditions.checkArgument( (x >= 0) && (x <= 1) && (y >= 0) && (y <=1) );
    }

    /**
     * retourne le point dont les coordonnées sont x et y au niveau de zoom zoomLevel.
     * On divise simplement x et y par 2^(8+zoomlevel)
     * @param zoomLevel Niveau de zoom
     * @param x La coordonnée X
     * @param y La coordonnée Y
     * @return le point dont les coordonnées sont x et y au niveau de zoom zoomLevel
     */
    public static PointWebMercator of(int zoomLevel, double x, double y){
        //Preconditions.checkArgument((zoomLevel >= MIN_ZOOM) && (zoomLevel <= MAX_ZOOM));
        return new PointWebMercator(Math.scalb(x,-(OFFSET_ZOOM+zoomLevel)), Math.scalb(y,-(OFFSET_ZOOM+zoomLevel)));

    }

    /**
     * retourne le point Web Mercator correspondant au point du système de coordonnées suisse donné.
     * On converti d'abord (e,n) de pointCh en (lon,lat) de WGS et finalement en (x,y) de webmercator
     * @param pointCh point du système de coordonnées suisse donné
     * @return le point Web Mercator correspondant au point du système de coordonnées suisse donné
     */
    public static PointWebMercator ofPointCh (PointCh pointCh){
        return new PointWebMercator(WebMercator.x(pointCh.lon()),WebMercator.y(pointCh.lat()));
    }

    /**
     * retourne la coordonnée x au niveau de zoom donné,
     * @param zoomLevel Niveau de zoom
     * @return x*2^(8+z)
     */
    public double xAtZoomLevel (int zoomLevel){
        return Math.scalb(x(),OFFSET_ZOOM + zoomLevel);  // scalb(x,y) calcule x*2^y de manière plus précise
    }

    /**
     * retourne la coordonnée y au niveau de zoom donné,
     * @param zoomLevel Niveau de zoom
     * @return y*2^(8+z)
     */
   public double yAtZoomLevel (int zoomLevel){
       return Math.scalb(y(),OFFSET_ZOOM + zoomLevel);
   }

    /**
     * retourne la longitude du point, en radians
     * @return  la longitude du point, en radians
     */
   public double lon(){
        return WebMercator.lon(x());
   }

    /**
     * retourne la latitude du point, en radians,
     * @return la latitude du point, en radians,
     */
    public double lat(){
        return WebMercator.lat(y());
    }

    /**
     * retourne le point de coordonnées suisses se trouvant à la même position que le récepteur (this)
     * ou null si ce point n'est pas dans les limites de la Suisse définies par SwissBounds.
     * @return le point de coordonnées suisses se trouvant à la même position que le récepteur (this)
     */
   public PointCh toPointCh(){
       double lon = lon();
       double lat = lat();
       double east = Ch1903.e(lon,lat);
       double nord = Ch1903.n(lon,lat);

       return SwissBounds.containsEN(east, nord) ? new PointCh(east, nord) : null;
    }

    /**
     * ajoute les valeurs x y au point mercator
     * @param x value to add
     * @param y
     * @return
     */
    public PointWebMercator addXY(double x, double y){
       return new PointWebMercator(this.x + x , this.y + y );
    }
}
