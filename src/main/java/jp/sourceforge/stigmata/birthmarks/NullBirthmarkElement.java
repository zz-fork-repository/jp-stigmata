package jp.sourceforge.stigmata.birthmarks;

import jp.sourceforge.stigmata.BirthmarkElement;

/**
 * Null birthmark element.
 *
 * @author Haruaki TAMADA
 */
public class NullBirthmarkElement extends BirthmarkElement{
    private static final long serialVersionUID = -92345638932523L;

    private static final NullBirthmarkElement ELEMENT = new NullBirthmarkElement();

    private NullBirthmarkElement(){
        super(null);
    }

    public static BirthmarkElement getInstance(){
        return ELEMENT;
    }

    @Override
    public int hashCode(){
        return 0;
    }

    @Override
    public boolean equals(Object o){
        boolean flag = o != null && o.getClass().equals(getClass());
        flag = flag && this == o;

        return flag;
    }

    @Override
    public String toString(){
        return "<null>";
    }
}
