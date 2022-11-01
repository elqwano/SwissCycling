package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Représente un calculateur de profil en long. C'est-à-dire qu'elle contient le code permettant de calculer
 * le profil en long d'un itinéraire donné.
 * @author Quentin Anglio (313052)
 */
public final class ElevationProfileComputer {
    /*
    Classe non instanciable
     */
    private ElevationProfileComputer(){}

    /**
     * retourne le profil en long de l'itinéraire route, en garantissant que l'espacement entre les échantillons
     * du profil est d'au maximum maxStepLength mètres;
     * lève IllegalArgumentException si cet espacement n'est pas strictement positif.
     * @param route La route dont on veut calculer le profile en long
     * @param maxStepLength espacement maximum
     * @return le profil en long de l'itinéraire route
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength){
        Preconditions.checkArgument(maxStepLength>0);

        /* On crée et remplis un tableau avec les elevations le long de la route */
        int nbSamples = (int) Math.ceil(route.length()/maxStepLength) + 1 ;
        float[] samples = new float[nbSamples];
        double gap = route.length() / (nbSamples-1);
        /* On crée une ArrayList rempli avec les index des NaN du tableau précédent(.add ajoute à la fin de la liste) */
        LinkedList<Integer> nanIndex = new LinkedList<>();

        for (int i =0; i< nbSamples;++i){
            samples[i]=(float) route.elevationAt(i*gap);

            if (Float.isNaN(samples[i])){
                nanIndex.add(i);
            }
        }

        /*  Si l'ArrayList est vide (aucun NaN) on retourne simplement le tableau */
        if (nanIndex.isEmpty()){
            return new ElevationProfile(route.length(),samples);
        }
        /* Si le tableau ne contient que des NaN on le rempli de zero et on retourne le profile */
        if(nanIndex.size()==nbSamples){
            Arrays.fill(samples,0);
            return new ElevationProfile(route.length(),samples);
        }
        /* Si le tableau commence par des NaN on vérifie jusqu'à quel Index et on les remplace par la valeur d'après */
        if (nanIndex.getFirst() == 0){
            int temp;
            do {
                temp = nanIndex.removeFirst();
                if (nanIndex.isEmpty())
                    break;
            } while (nanIndex.getFirst() == temp +1);

            Arrays.fill(samples,0,temp+1,samples[temp+1]);
        }
        /* Si le tableau fini par des NaN on vérifie jusqu'à quel Index et on les remplace par la valeur d'avant */
        if (!nanIndex.isEmpty() && ((nanIndex.getLast()) == samples.length-1)){
            int temp;
            do {
                temp = nanIndex.removeLast();
                if (nanIndex.isEmpty())
                    break ;
            } while (nanIndex.getLast()==temp-1);

            Arrays.fill(samples,temp,nbSamples ,samples[temp-1]);
        }
        /* Pour les NaN restant on interpole entre les deux valeurs adjacentes */
        while (!nanIndex.isEmpty()){
            final int i = nanIndex.getFirst();
            final int start = i-1;
            double y0 = samples[start];
            int id = i;
            int nbGap = 2;                  // il y a par défaut 2 gaps entre y0 et y1
            if (nanIndex.size()>1){
                while  (nanIndex.get(1) == id+1){
                    id ++; nbGap++;
                    nanIndex.removeFirst();
                    if (nanIndex.size()==1)
                        break;
                }
            }
            final int end = id + 1;
            double y1 = samples[end];

            for (int j =1; j < nbGap;++j){
                samples[start+j] = (float) Math2.interpolate(y0, y1,(double)j/nbGap);
            }

            nanIndex.removeFirst();
        }

        return new ElevationProfile(route.length(),samples);
    }
}
