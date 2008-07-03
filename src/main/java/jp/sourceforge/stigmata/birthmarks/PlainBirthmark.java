package jp.sourceforge.stigmata.birthmarks;

/*
 * $Id$
 */

import jp.sourceforge.stigmata.BirthmarkElement;

/**
 * Implementation of plain birthmark.
 *
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class PlainBirthmark extends AbstractBirthmark{
    private static final long serialVersionUID = 2370435723234463456L;

    private String type;

    public PlainBirthmark(String type){
        this.type = type;
    }

    public PlainBirthmark(){
    }

    public void addElement(int index, BirthmarkElement element){
        elements.add(index, element);
    }

    public BirthmarkElement getElement(int index){
        return elements.get(index);
    }

    public void setType(String type){
        this.type = type;
    }

    @Override
    public String getType(){
        return type;
    }
}
