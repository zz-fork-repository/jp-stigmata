package jp.naist.se.stigmata.filter;

/*
 * $Id$
 */

import jp.naist.se.stigmata.ComparisonPair;
import jp.naist.se.stigmata.spi.ComparisonPairFilterSpi;

/**
 * 
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class TotalElementCountComparisonPairFilter extends AbstractComparisonPairFilter{
    public static final Criterion[] CRITERIA = new Criterion[] {
        Criterion.GREATER_EQUALS, Criterion.GREATER_THAN,
        Criterion.LESS_EQUALS, Criterion.LESS_THAN,
        Criterion.EQUALS_AS, Criterion.NOT_EQUALS_AS, 
        Criterion.MATCH, Criterion.NOT_MATCH, 
    };

    private int threshold = 0;
    private Target target;

    public TotalElementCountComparisonPairFilter(ComparisonPairFilterSpi service){
        super(service);
    }

    public Criterion[] getAcceptableCriteria(){
        return CRITERIA;
    }

    public boolean isFiltered(ComparisonPair pair){
        boolean flag;
        if(getTarget() == Target.TARGET_1){
            flag = checkFiltered(pair.getTarget1().getSumOfElementCount());
        }
        else if(getTarget() == Target.TARGET_2){
            flag = checkFiltered(pair.getTarget2().getSumOfElementCount());
        }
        else{
            flag = checkFiltered(pair.getTarget1().getSumOfElementCount(), pair.getTarget2().getSumOfElementCount());
        }
        return flag;
    }

    private boolean checkFiltered(int e1, int e2){
        boolean flag1;
        boolean flag2;
        int t = getThreshold();
        switch(getCriterion()){
        case GREATER_EQUALS:
            flag1 = e1 >= t; flag2 = e2 >= t;
            break;
        case GREATER_THAN:
            flag1 = e1 > t;  flag2 = e2 > t;
            break;
        case LESS_EQUALS:
            flag1 = e1 <= t; flag2 = e2 <= t;
            break;
        case LESS_THAN:
            flag1 = e1 < t;  flag2 = e2 < t;
            break;
        case EQUALS_AS:
            flag1 = e1 == t; flag2 = e2 == t;
            break;
        case NOT_EQUALS_AS:
            flag1 = e1 != t; flag2 = e2 != t;
            break;
        case MATCH:
            flag1 = e1 == e2; flag2 = flag1;
            break;
        case NOT_MATCH:
            flag1 = e1 != e2; flag2 = flag1;
            break;
        default:
            flag1 = false;
            flag2 = false;
            break;
        }
        return (getTarget() == Target.BOTH_TARGET && flag1 && flag2) ||
            (getTarget() == Target.ONE_OF_TARGET && (flag1 || flag2));
    }

    private boolean checkFiltered(int total){
        boolean flag = false;
        switch(getCriterion()){
        case GREATER_EQUALS:
            flag = total >= getThreshold();
            break;
        case GREATER_THAN:
            flag = total > getThreshold();
            break;
        case LESS_EQUALS:
            flag = total <= getThreshold();
            break;
        case LESS_THAN:
            flag = total < getThreshold();
            break;
        case EQUALS_AS:
            flag = total == getThreshold();
            break;
        case NOT_EQUALS_AS:
            flag = total != getThreshold();
            break;
        default:
            flag = false;
            break;
        }
        return flag;
    }

    public int getThreshold(){
        return threshold;
    }

    public void setThreshold(int threshold){
        if(threshold < 0){
            throw new IllegalArgumentException("threshold must be positive value: " + threshold);
        }
        this.threshold = threshold;
    }

    public Target getTarget(){
        return target;
    }

    public void setTarget(Target target){
        this.target = target;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        switch(getTarget()){
        case TARGET_1:      sb.append("target1"); break;
        case TARGET_2:      sb.append("target2"); break;
        case BOTH_TARGET:   sb.append("(target1&target2)"); break;
        case ONE_OF_TARGET: sb.append("(target1|target2)"); break;
        }
        sb.append(".element");
        switch(getCriterion()){
        case GREATER_EQUALS: sb.append(" >= "); break;
        case GREATER_THAN:   sb.append(" >  "); break;
        case LESS_EQUALS:    sb.append(" <= "); break;
        case LESS_THAN:      sb.append(" <  "); break;
        case EQUALS_AS:      sb.append(" == "); break;
        case NOT_EQUALS_AS:  sb.append(" != "); break;
        }
        sb.append(Integer.toString(getThreshold()));

        return new String(sb);
    }
}