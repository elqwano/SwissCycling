package ch.epfl.javelo;

/**
 * La classe Math2, du paquetage ch.epfl.javelo, publique, finale et non instanciable, offre des méthodes statiques
 * permettant d'effectuer certains calculs mathématiques.
 * Elle est donc similaire à la classe Math de la bibliothèque standard Java.
 * @author Quentin Anglio (313052)
 */
public final class Math2 {

    /* La classe est non instantiable */
    private Math2(){}

    /**
     * retourne la partie entière par excès de la division de x par y, ou lève IllegalArgumentException
     * si x est négatif ou si y est négatif ou nul
     * @param x Dividende de la division
     * @param y Diviseur de la division
     * @return la partie entière par excès de la division de x par y
     * */
    public static int ceilDiv(int x, int y){
        Preconditions.checkArgument (x>=0 && y>0);
        return (x + y - 1)/y;
    }

    /**
     * retourne la coordonnée y du point se trouvant sur la droite passant par (0,y0) et (1,y1)  de coordonnée x donnée
     * @param y0 coordonnée y0
     * @param y1 coordonnée y1
     * @param x coordonnée x
     * @return la coordonnée y du point se trouvant sur la droite passant par (0,y0) et (1,y1)  de coordonnée x donnée
     * */
    public static double interpolate(double y0, double y1, double x){
        return (Math.fma(y1-y0,x,y0));
    }

    /**
     * limite la valeur v à l'intervalle allant de min à max
     * @param min  Minimum de l'intervalle
     * @param v Valeur à limiter
     * @param max Maximum de l'intervalle
     * @return min si v est inférieure à min, max si v est supérieure à max et v sinon ; lève IllegalArgumentException si min est (strictement) supérieur à max,
     */
    public static int clamp(int min, int v, int max){
        Preconditions.checkArgument(min <= max);
        if (min <= v && v <= max){
            return v;
        }
        return v < min ? min : max;
    }

    /**
     * limite la valeur v à l'intervalle allant de min à max
     * @param min Minimum de l'intervalle
     * @param v Valeur à limiter
     * @param max Maximum de l'intervalle
     * @return min si v est inférieure à min, max si v est supérieure à max, et v sinon; lève IllegalArgumentException si min est (strictement) supérieur à max,
     */
    public static double clamp(double min, double v, double max){
        Preconditions.checkArgument(min <= max);
        if (min <= v && v <= max){
            return v;
        }
        return v < min ? min : max;
    }

    /**
     * retourne le sinus hyperbolique inverse de son argument x
     * @param x argument de la fonction
     * @return le sinus hyperbolique inverse de l'argument x
     */
    public static double asinh(double x){
        return Math.log(x + Math.sqrt(Math.fma(x, x, 1) ));          // means ln[x + sqrt(x^2+1)]
    }

    /**
     * retourne le produit scalaire entre le vecteur (de composantes uX et uY) et le vecteur(de composantes vX et vY)
     *  @param uX Composant X du vecteur U
     * @param uY Composant Y du vecteur U
     * @param vX Composant X du vecteur V
     * @param vY Composant Y du vecteur V
     * @return le produit scalaire entre le vecteur U et V */
    public static  double dotProduct(double uX, double uY, double vX, double vY){
        return Math.fma(uX, vX, uY * vY);
    }

    /**
     * retourne le carré de la norme du vecteur u, uX et uY étant les composantes de ce vecteur
     * @param uX Composant X du Vecteur U
     * @param uY Composant X du Vecteur U
     * @return le carré de la norme du vecteur
     */
    public static double squaredNorm(double uX, double uY){
        return dotProduct(uX, uY, uX, uY);
    }

    /**
     * retourne la norme du vecteur u. uX et uY étant les composantes de ce vecteur
     * @param uX Composant X du Vecteur U
     * @param uY Composant X du Vecteur U
     * @return La norme du vecteur U
     */
    public static double norm(double uX, double uY){
        return Math.sqrt(squaredNorm(uX, uY));
    }

    /**
     * retourne la longueur de la projection du vecteur allant du point A (de coordonnées aX et aY)
     * au point P (de coordonnées pX et pY)
     * sur le vecteur allant du point A au point B (de composantes bY et bY)
     * @param aX Composant X du Vecteur A
     * @param aY Composant Y du Vecteur U
     * @param bX Composant X du Vecteur B
     * @param bY Composant Y du Vecteur U
     * @param pX Composant X du point U
     * @param pY Composant Y du Vecteur U
     * @return la projection du vecteur AP sur AB
     */
    public static double projectionLength(double aX, double aY, double bX, double bY, double pX, double pY){
        return dotProduct(pX-aX,pY-aY,bX-aX,bY-aY)/norm(bX-aX,bY-aY);
    }
}
