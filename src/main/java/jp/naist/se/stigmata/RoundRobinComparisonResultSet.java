package jp.naist.se.stigmata;

/*
 * $Id$
 */

import java.util.Iterator;

/**
 * Concrete class for ComparisonResultSet. This instance compare class files by round robin.
 *
 * @author  Haruaki TAMADA
 * @version  $Revision$ $Date$
 */
public class RoundRobinComparisonResultSet implements ComparisonResultSet{
    private BirthmarkSet[] holders1;
    private BirthmarkSet[] holders2;
    private BirthmarkContext context;

    private int compareCount;
    private boolean tablePair = true;
    private boolean samePair = false;

    /**
     * constructor.  if user gives { a, b, c, } as holders1, then
     * the instance (created by this constructor) compares { a<->b, a<->c,
     * b<->c, }.
     */
    public RoundRobinComparisonResultSet(BirthmarkSet[] holders1, BirthmarkContext context){
        this(holders1, context, false);
    }

    /**
     * constructor.  if user gives { a, b, c, } as holders1, then the
     * instance (created by this constructor when samePair is true)
     * compares { a<->a, a<->b, a<->c, b<->b, b<->c, c<->c, }.
     * Otherwise, the instance compares { a<->b, a<->c, b<->c, } when
     * samePair is false.
     */
    public RoundRobinComparisonResultSet(BirthmarkSet[] holders1, BirthmarkContext context,
                                         boolean samePair){
        this.holders1 = holders1;
        this.holders2 = holders1;
        this.context = context;

        tablePair = false;
        setCompareSamePair(samePair);
    }

    /**
     * constructor.  if user gives { a, b, c, } as holders1 and { x,
     * y, z, } as holders2, then the instance compares { a<->x, a<->y,
     * a<->z, b<->x, b<->y, b<->z, c<->x, c<->y, c<->z, }.
     */
    public RoundRobinComparisonResultSet(BirthmarkSet[] holders1, BirthmarkSet[] holders2,
                                         BirthmarkContext context){
        this.holders1 = holders1;
        this.holders2 = holders2;
        this.context = context;
        tablePair = true;

        this.compareCount = holders1.length * holders2.length;
    }

    /**
     * @return  context
     */
    public BirthmarkContext getContext(){
        return context;
    }

    /**
     * update same pair comparing flag unless two birthmark array is setted.
     */
    public void setCompareSamePair(boolean flag){
        samePair = flag;
        if(samePair){
            compareCount = holders1.length * (holders1.length + 1) / 2;
        }
        else{
            compareCount = holders1.length * (holders1.length - 1) / 2;
        }
    }

    public boolean isCompareSamePair(){
        return samePair;
    }

    /**
     * returns the compare count of birthmark sets.
     */
    public int getComparisonCount(){
        return compareCount;
    }

    /**
     * return a iterator of whole comparison.
     */
    public Iterator<ComparisonPair> iterator(){
        return new ComparisonIterator();
    }

    /**
     * iterator class.
     */
    private class ComparisonIterator implements Iterator<ComparisonPair>{
        private int i = 0;

        private int j = 0;

        private int count = 0;

        public boolean hasNext(){
            return count < getComparisonCount();
        }

        public ComparisonPair next(){
            if((tablePair && i == holders1.length) || (!tablePair && !samePair && i == j)
                    || (!tablePair && samePair && i > j)){
                i = 0;
                j++;
            }
            ComparisonPair pair = new ComparisonPair(holders1[i], holders2[j], context);
            count++;
            i++;
            return pair;
        }

        public void remove(){
        }
    }
}
