package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * La classe Functions du paquetage ch.epfl.javelo, publique, finale et non instanciable, contient des méthodes
 * permettant de créer des objets représentant des fonctions mathématiques des réels vers les réels.
 * @author Quentin Anglio (313052)
 *
 */
public final class Functions {

    /* La classe est non instantiable */
    private Functions(){}

    /**
     * retourne une fonction constante, dont la valeur est toujours y
     * @param y paramètre y
     * @return retourne une fonction constante, dont la valeur est toujours y
     */
    public static DoubleUnaryOperator constant(double y){
        return new Constant(y);
    }

    /**
     * retourne une fonction obtenue par interpolation linéaire entre les échantillons samples, espacés régulièrement
     * et couvrant la plage allant de 0 à xMax ; lève IllegalArgumentException si le tableau samples contient moins
     * de deux éléments, ou si xMax est inférieur ou égal à 0.
     * @param samples échantillons de profils
     * @param xMax valeur maximale de X
     * @return  une fonction obtenue par interpolation linéaire entre les échantillons samples,
     * espacés régulièrement et couvrant la plage allant de 0 à xMax
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax){
        Preconditions.checkArgument((samples.length > 1) && (xMax > 0));
        return new Sampled(samples,xMax);
    }

    //==================================//

    /**
     * Cette classe implémente l'interface DoubleUnaryOperator et sa redéfinition de la méthode applyAsDouble
     * retourne toujours la même valeur, passée au constructeur de Constant.
     */
    private static final record Constant(double y) implements DoubleUnaryOperator{

        @Override
        public double applyAsDouble(double y) {
            return this.y;
        }
    }

    /**
     * Cette classe implémente l'interface DoubleUnaryOperator
     * Sa redéfinition de la méthode applyAsDouble retourne une fonction obtenue par interpolation linéaire entre les échantillons samples.
     */
    private static final record Sampled(float[] samples, double xMax) implements DoubleUnaryOperator{

        @Override
        public double applyAsDouble(double operand) {
            double x = Math2.clamp(0,operand,xMax);
            double gap = xMax/(samples.length-1);
            int start = (int)(x/gap);
            double moduloX = Math.fma(x, 1/gap, -start);

            return (x == xMax) ?
                    samples[samples().length-1] :
                    Math2.interpolate(samples[start], samples[start + 1], moduloX);

        }
    }
}
