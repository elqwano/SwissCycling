package ch.epfl.javelo;

/**
 * Classe permetant de faciliter l'écriture de préconditions
 *
 * @author Quentin Anglio (313052)
 * @author valentin dupraz (315995)
 */
public final class Preconditions {

    /* Classe non instantiable */
    private Preconditions(){}

    /**
     * Throws un IllegalArgumentException si la condition n'est pas vérifiée
     * @param shouldBeTrue la condition à vérifier
     */
   public static void checkArgument(boolean shouldBeTrue){
        if (!shouldBeTrue){
            throw new IllegalArgumentException();
        }
   }
}
