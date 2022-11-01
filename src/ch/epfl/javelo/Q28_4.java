package ch.epfl.javelo;

/**
 * La classe Q28_4 contient les méthodes statiques qui permettent de convertir des nombres entre la représentation
 * Q28.4 et d'autres représentations. La notation Q28.4 sigifie que les 32 bits sont représentés avec une
 * vitrgule fixe; avec 28 bits à gauche et 4 bits à droite de la virgule.
 * @author Quentin Anglio (313052)
 * @author valentin dupraz (315995)
 */
public final class Q28_4 {

    private static int BITS_AFTER_POINT = 4;

    private Q28_4() {}

    /**
     * retourne la valeur Q28.4 correspondant à l'entier donné
     * @param i l'entier donné
     * @return la valeur Q28.4
     */
    public static int ofInt(int i){
        return i << BITS_AFTER_POINT;
    }

    /**
     * retourne la valeur de type double égale à la valeur Q28.4 donnée
     * @param q28_4 l'entier donné
     * @return la valeur de type double
     */
    public static double asDouble(int q28_4){
        return Math.scalb((double)q28_4, -BITS_AFTER_POINT);
    }

    /**
     * retourne la valeur de type float correspondant à la valeur Q28.4 donnée.
     * @param q28_4 l'entier donné
     * @return la valeur de type float
     */
    public static float asFloat(int q28_4){
        return Math.scalb(q28_4, -BITS_AFTER_POINT);
    }
}