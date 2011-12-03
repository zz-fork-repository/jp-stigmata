package jp.sourceforge.stigmata.filter;

import java.util.Arrays;

import jp.sourceforge.stigmata.ComparisonPairFilter;
import jp.sourceforge.stigmata.spi.ComparisonPairFilterService;

/**
 * 
 * @author Haruaki TAMADA
 */
public abstract class AbstractComparisonPairFilter implements ComparisonPairFilter{
    private ComparisonPairFilterService service;
    private Criterion criterion = Criterion.EQUALS_AS;

    public AbstractComparisonPairFilter(ComparisonPairFilterService service){
        this.service = service;
    }

    @Override
    public ComparisonPairFilterService getService(){
        return service;
    }

    @Override
    public Criterion getCriterion(){
        return criterion;
    }

    @Override
    public void setCriterion(Criterion criterion){
        if(!isAcceptable(criterion)){
            throw new IllegalArgumentException(
                "illegal criterion: " + criterion +
                ": accepts only " + Arrays.toString(getAcceptableCriteria())
            );
        }
        this.criterion = criterion;
    }

    @Override
    public boolean isAcceptable(Criterion criterion){
        Criterion[] criteria = getAcceptableCriteria();
        for(int i = 0; i < criteria.length; i++){
            if(criteria[i] == criterion){
                return true;
            }
        }
        return false;
    }
}
