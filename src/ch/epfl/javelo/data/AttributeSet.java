package ch.epfl.javelo.data;
import ch.epfl.javelo.Preconditions;
import java.util.StringJoiner;

/**
 * L'enregistrement AttributeSet représente un ensemble d'attributs OpenStreetMap
 * @author valentin dupraz (315995)
 * @author Quentin Anglio (313052)
 *
 * @param bits représente le contenu de l'ensemble au moyen d'un bit par valeur possible; c'est-à-dire que le bit d'index b de cette valeur vaut 1 ssi l'attribut b est contenu dans l'ensemble
 */
public record AttributeSet(long bits) {

    /**
     * le constructeur compact de la classe
     * @param bits le contenu de l'ensemble au moyen d'un bit par valeur possible
     */
    public AttributeSet {
        boolean validAttribute = bits >> Attribute.COUNT == 0;
        Preconditions.checkArgument(validAttribute);
    }

    /**
     * permet de construire un ensemble contenant un certain nombre d'attributs
     * @param attributes les attributs
     * @return un ensemble contenant uniquement les attributs donnés en argument
     */
    public static AttributeSet of(Attribute... attributes) {
        long temp = 0;

        for (Attribute attribute : attributes) {
            temp |= 1L << attribute.ordinal();       //mask pour contenir uniquement les bons attributs
        }
        return new AttributeSet(temp);

    }

    /**
     * retourne vrai ssi l'ensemble récepteur (this) contient l'attribut donné
     * @param attribute l'attribut donné
     * @return vrai ssi l'ensemble récepteur contient l'attribut donné
     */
    public boolean contains(Attribute attribute) {
        long temp = 1L << attribute.ordinal();
        return (temp & this.bits()) != 0;
    }

    /**
     * retourne vrai ssi l'intersection de l'ensemble récepteur (this)
     * avec celui passé en argument (that) n'est pas vide
     * @param that l'ensemble passé en argument
     * @return vrai ssi l'intersection de this avec that n'est pas vide
     */
    public boolean intersects(AttributeSet that) {
        return (bits() & that.bits()) != 0;
    }

    /**
     * retourne une chaîne composée de la représentation textuelle des éléments de l'ensemble entourés
     * d'accolades ({}) et séparés par des virgules
     * @return la chaîne composée de la représentation textuelle des éléments de l'ensemble entourés
     * d'accolades ({}) et séparés par des virgules
     */
    public String toString() {
        StringJoiner j = new StringJoiner(",", "{", "}");
        for (int i = 0; i < Attribute.COUNT; i ++) {
            if (contains(Attribute.ALL.get(i))) {
                j.add(Attribute.ALL.get(i).keyValue());
            }
        }
        return j.toString();
    }
}