package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;

/**
 * La classe représente le profil en long d'un itinéraire simple ou multiple
 * @author valentin dupraz (315995)
 *
 */
public final class ElevationProfile {       //todo refaire constructeur

    private final double length;
    private final float[] elevationSamples;
    private final DoubleSummaryStatistics statistics;
    private final double totalAscent;
    private final double totalDescent;

    /**
     * construit le profil en long d'un itinéraire de longueur length (en m) avec les échantillons d'altitude,
     * répartis uniformément le long de l'itinéraire
     * @param length la longueur de l'itinéraire
     * @param elevationSamples les échantillons d'altitude, répartis uniformément le long de l'itinéraire
     */
    public ElevationProfile(double length, float[] elevationSamples){
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);

        this.length= length;
        this.elevationSamples = Arrays.copyOf(elevationSamples, elevationSamples.length);

        statistics = new DoubleSummaryStatistics();
        for (float elevationSample : elevationSamples) {
            statistics.accept(elevationSample);
        }
        double asc = 0;
        double dsc = 0;
        for (int i = 0; i < elevationSamples.length - 1; i++) {
            if (elevationSamples[i+1] - elevationSamples[i] > 0){
                asc += elevationSamples[i+1] - elevationSamples[i];
            }
            if (elevationSamples[i+1] - elevationSamples[i] < 0){
                dsc -= elevationSamples[i+1] - elevationSamples[i];
            }
        }
        totalAscent = asc;
        totalDescent = dsc;
    }

    /**
     * retourne  la longueur du profil (en m)
     * @return  la longueur du profil
     */
    public double length() {
        return this.length;
    }

    /**
     * retourne l'altitude minimum du profil (en m)
     * @return l'altitude minimum du profil
     */
    public double minElevation(){
        return this.statistics
                .getMin();
    }

    /**
     * retourne l'altitude maximum du profil (en m)
     * @return l'altitude maximum du profil
     */
    public double maxElevation(){
        return this.statistics
                .getMax();
    }

    /**
     *  retourne l'élevation totale du profile
     * @return l'élevation totale du profile
     */
    public double totalElevation(){
        return maxElevation()-minElevation();
    }

    /**
     * retourne le dénivelé positif total du profil (en m)
     * @return le dénivelé positif total du profil
     */
    public double totalAscent(){
        return totalAscent;
    }

    /**
     * retourne le dénivelé négatif total du profil (en m)
     * @return le dénivelé négatif total du profil
     */
    public double totalDescent(){
        return totalDescent;
        }

    /**
     * Retourne l'altitude du profil (en m) qui n'est pas forcément comprise entre 0 et la longueur du profil le
     * premier échantillon est retourné lorsque la position est négative,
     * le dernier lorsqu'elle est supérieure à la longueur.
     * @param position la position donnée
     * @return l'altitude du profil
     */
    public double elevationAt(double position){

        return Functions
                .sampled(elevationSamples,length)
                .applyAsDouble(position);
    }
}