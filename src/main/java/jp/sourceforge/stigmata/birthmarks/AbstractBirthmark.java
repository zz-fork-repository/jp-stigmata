package jp.sourceforge.stigmata.birthmarks;

/*
 * $Id$
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkElement;

/**
 * Abstract class for concrete {@link Birthmark <code>Birthmark</code>}
 *
 * @author  Haruaki TAMADA
 * @version  $Revision$ 
 */
public abstract class AbstractBirthmark implements Birthmark, Serializable{
	private static final long serialVersionUID = -1300436185045832554L;

	/**
     * collections for elements.
     */
    protected List<BirthmarkElement> elements = new ArrayList<BirthmarkElement>();

    public void addElement(BirthmarkElement element){
        elements.add(element);
    }

    /**
     * returns the number of elements.
     */
    public int getElementCount(){
        int numberOfElement = 0;
        BirthmarkElement[] elements = getElements();
        if(elements != null){
            numberOfElement = elements.length;
        }
        return numberOfElement;
    }

    /**
     * returns elements.
     * @return  elements
     */
    public BirthmarkElement[] getElements(){
        return elements.toArray(new BirthmarkElement[elements.size()]);
    }

    /**
     * returns elements.
     */
    public Iterator<BirthmarkElement> iterator(){
        return elements.iterator();
    }

    /**
     * returns the type of this birthmark.
     */
    public abstract String getType();

    /**
     * Is given birthmark the same type.
     */
    public boolean isSameType(Birthmark b){
        return getType().equals(b.getType());
    }
}
