package ch.epfl.javelo;

/**
 * extrait une séquence de bits d'un vecteur de 32 bits
 * @author Quentin Anglio (313052)
 * @author valentin dupraz (315995)
 */
public final class Bits {
    private Bits() {}

    /**
     * extrait du vecteur la plage de length bits (commençant au bit d'index start), qu'elle interprète comme
     * une valeur signée en 2's complément
     * @param value le vecteur
     * @param start où commence la plage
     * @param length la taille de la plage
     * @return la plage de bits allant de start à start+length
     */
    public static int extractSigned(int value, int start, int length) {
        int end = start + length;
        boolean isInRange = start >= 0
                && start < Integer.SIZE
                && end <= Integer.SIZE
                && end >= 0 && length > 0 ;
        Preconditions.checkArgument(isInRange);
        return (value << (Integer.SIZE - end)) >> (Integer.SIZE - length);
    }

    /**
     * fait la même chose que la méthode précédente, à deux différences près: d'une part, la valeur extraite
     * est interpretée de manière non signée, et d'autre part l'exception
     * IllegalArgumentException est également levée si length vaut 32.
     * @param value le vecteur
     * @param start où commence la plage
     * @param length la taille de la plage
     * @return la plage de bits allant de start à start+length
     */
     public static int extractUnsigned(int value, int start, int length){
         int end = start + length;
         boolean isInRange = start < Integer.SIZE
                 && start >= 0
                 && end <= Integer.SIZE
                 && length > 0
                 && length < Integer.SIZE;
         Preconditions.checkArgument(isInRange);
         return (value << Integer.SIZE - end) >>> (Integer.SIZE - length);
    }
}