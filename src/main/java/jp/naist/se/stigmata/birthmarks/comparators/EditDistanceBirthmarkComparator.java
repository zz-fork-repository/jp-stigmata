package jp.naist.se.stigmata.birthmarks.comparators;

/*
 * $Id: DPMatchingBirthmarkComparator.java 53 2007-02-22 02:46:40Z tama3 $
 */

import jp.naist.se.stigmata.Birthmark;
import jp.naist.se.stigmata.BirthmarkComparator;
import jp.naist.se.stigmata.BirthmarkElement;
import jp.naist.se.stigmata.spi.BirthmarkSpi;

/**
 * calculate similarities between two birthmarks by edit distance
 * algorithm (levenshtein distance).
 *
 * @author Haruaki TAMADA
 * @version $Revision: 53 $ $Date: 2007-02-22 11:46:40 +0900 (Thu, 22 Feb 2007) $
 */
public class EditDistanceBirthmarkComparator implements BirthmarkComparator{
    private BirthmarkSpi spi;

    public EditDistanceBirthmarkComparator(){
    }

    public EditDistanceBirthmarkComparator(BirthmarkSpi spi){
        this.spi = spi;
    }

    public BirthmarkSpi getProvider(){
        return spi;
    }

    public String getType(){
        return spi.getType();
    }

    public double compare(Birthmark b1, Birthmark b2) {
        if(!b1.getType().equals(b2.getType())){
            return Double.NaN;
        }

        BirthmarkElement[] element1 = b1.getElements();
        BirthmarkElement[] element2 = b2.getElements();
        int[][] distance = createDistanceMatrics(element1, element2);

        int length = element1.length;
        if(length < element2.length){
            length = element2.length;
        }
        int d = distance[element1.length][element2.length];

        if(element1.length == 0 && element2.length == 0){
            return 1d;
        }
        return (double)(length - d) / length;
    }

    public int getCompareCount(Birthmark b1, Birthmark b2){
        return b1.getElementCount() + b2.getElementCount();
    }

    protected int[][] createDistanceMatrics(BirthmarkElement[] element1,
                                            BirthmarkElement[] element2){
        int[][] distance = new int[element1.length + 1][element2.length + 1];
        for(int i = 0; i <= element1.length; i++) distance[i][0] = i;
        for(int i = 0; i <= element2.length; i++) distance[0][i] = i;

        for(int i = 1; i <= element1.length; i++){
            for(int j = 1; j <= element2.length; j++){
                int cost = 1;
                if(element1[i - 1] == null){
                    if(element2[j - 1] == null) cost = 0;
                    else                        cost = 1;
                }
                else{
                    if(element1[i - 1].equals(element2[j - 1])) cost = 0;
                    else                                        cost = 1;
                }
                int insertion = distance[i - 1][j    ] + 1;
                int deletion  = distance[i    ][j - 1] + 1;
                int replace   = distance[i - 1][j - 1] + cost;

                if(insertion <= deletion && insertion <= replace) distance[i][j] = insertion;
                else if(deletion <= replace)                      distance[i][j] = deletion;
                else                                              distance[i][j] = replace;
            }
        }
        return distance;
    }
}