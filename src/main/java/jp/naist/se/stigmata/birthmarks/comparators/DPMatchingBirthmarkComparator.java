package jp.naist.se.stigmata.birthmarks.comparators;

/*
 * $Id$
 */

import jp.naist.se.stigmata.Birthmark;
import jp.naist.se.stigmata.BirthmarkComparator;
import jp.naist.se.stigmata.BirthmarkElement;
import jp.naist.se.stigmata.spi.BirthmarkSpi;

/**
 * calculate similarities between two birthmarks by DP matching algorithm.
 *
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class DPMatchingBirthmarkComparator implements BirthmarkComparator{
    private BirthmarkSpi spi;
    private int mismatchPenalty = 5;
    private int shiftPenalty = 1;

    public DPMatchingBirthmarkComparator(){
    }

    public DPMatchingBirthmarkComparator(BirthmarkSpi spi){
        this.spi = spi;
    }

    public BirthmarkSpi getProvider(){
        return spi;
    }

    public String getType(){
        return spi.getType();
    }

    public int getMismatchPenalty(){
        return mismatchPenalty;
    }

    public void setMismatchPenalty(int mismatchPenalty){
        this.mismatchPenalty = mismatchPenalty;
    }

    public int getShiftPenalty(){
        return shiftPenalty;
    }

    public void setShiftPenalty(int shiftPenalty){
        this.shiftPenalty = shiftPenalty;
    }

    public double compare(Birthmark b1, Birthmark b2) {
        if(!b1.getType().equals(b2.getType())){
            return Double.NaN;
        }

        BirthmarkElement[] element1 = b1.getElements();
        BirthmarkElement[] element2 = b2.getElements();
        if(element1.length > 0 && element2.length > 0){
            int[][] cost = createCostMatrics(element1, element2);

            int max = (element1.length + element2.length) * (getMismatchPenalty() + getShiftPenalty());
            int distance = cost[element1.length - 1][element2.length - 1];

            return (double)(max - distance) / max;
        }
        else if(element1.length == 0 && element2.length == 0){
            return 1d;
        }
        else{
            return 0d;
        }
    }

    public int getCompareCount(Birthmark b1, Birthmark b2){
        return b1.getElementCount() + b2.getElementCount();
    }

    private int[][] createCostMatrics(BirthmarkElement[] targetX, BirthmarkElement[] targetY){
        int[][] mismatches = getMismatchMatrics(targetX, targetY);
        int[][] cost = new int[targetX.length][targetY.length];

        cost[0][0] = mismatches[0][0] * getMismatchPenalty();

        for(int i = 1; i < targetX.length; i++){
            cost[i][0] = cost[i - 1][0] + getShiftPenalty() + mismatches[i][0] * getMismatchPenalty();
        }
        for(int i = 1; i < targetY.length; i++){
            cost[0][i] = cost[0][i - 1] + getShiftPenalty() + mismatches[0][i] * getMismatchPenalty();
        }
        for(int i = 1; i < targetX.length; i++){
            for(int j = 1; j < targetY.length; j++){
                int crossCost      = cost[i - 1][j - 1] + mismatches[i][j] * getMismatchPenalty();
                int horizontalCost = cost[i - 1][j    ] + mismatches[i][j] * getMismatchPenalty() + getShiftPenalty();
                int verticalCost   = cost[i    ][j - 1] + mismatches[i][j] * getMismatchPenalty() + getShiftPenalty();

                if(crossCost <= horizontalCost && crossCost <= verticalCost){
                    cost[i][j] = crossCost;
                }
                else if(horizontalCost <= verticalCost){
                    cost[i][j] = horizontalCost;
                }
                else{
                    cost[i][j] = verticalCost;
                }
            }
        }
        return cost;
    }

    private int[][] getMismatchMatrics(BirthmarkElement[] targetX, BirthmarkElement[] targetY){
        int[][] mismatches = new int[targetX.length][targetY.length];

        for(int i = 0; i < mismatches.length; i++){
            for(int j = 0; j < mismatches[i].length; j++){
                if(targetX[i] == null){
                    if(targetY[j] == null)            mismatches[i][j] = 0;
                    else                              mismatches[i][j] = 1;
                }
                else{
                    if(targetX[i].equals(targetY[j])) mismatches[i][j] = 0;
                    else                              mismatches[i][j] = 1;
                }
            }
        }
        return mismatches;
    }
}
