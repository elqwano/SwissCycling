package ch.epfl.javelo.projection;

/**
 * contient les constantes et méthodes liées aux limites de la Suisse
 * @author valentin dupraz (315995)
 */
public final class SwissBounds {
    private SwissBounds() {}

    public final static double MIN_E = 2485000;
    public final static double MAX_E = 2834000;
    public final static double MIN_N = 1075000;
    public final static double MAX_N = 1296000;
    public final static double WIDTH = MAX_E - MIN_E;
    public final static double HEIGHT = MAX_N - MIN_N;

    /**
     * retourne vrai ssi (si et seulement si) les coordonnées E et N données sont dans les limites de la Suisse.
     * @param e Coordonnée E
     * @param n Coordonnée N
     * @return true si et seulement si les coordonnées E et N données sont dans les limites de la Suisse.
     */
    public static boolean containsEN(double e, double n) {
        return ((e <= MAX_E) && (MIN_E <= e)) && ((n <= MAX_N) && (MIN_N <= n));
    }
}