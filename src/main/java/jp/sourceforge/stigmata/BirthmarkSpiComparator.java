package jp.sourceforge.stigmata;

import java.io.Serializable;
import java.util.Comparator;

import jp.sourceforge.stigmata.spi.BirthmarkService;

/**
 * @author Haruaki TAMADA
 */
class BirthmarkSpiComparator implements Comparator<BirthmarkService>, Serializable{
    private static final long serialVersionUID = 8194452116506240274L;

    /**
     * default constructor
     */
    public BirthmarkSpiComparator(){
    }

    @Override
    public int hashCode(){
        return System.identityHashCode(this);
    }

    @Override
    public int compare(BirthmarkService s1, BirthmarkService s2){
        if(s1.isExperimental() && !s2.isExperimental()){
            return 1;
        }
        else if(!s1.isExperimental() && s2.isExperimental()){
            return -1;
        }
        else{
            return s1.getType().compareTo(s2.getType());
        }
    }

    @Override
    public boolean equals(Object o){
        String className = null;
        if(o != null){
            className = o.getClass().getName();
        }
        return o != null && className.equals(getClass().getName());
    }
}