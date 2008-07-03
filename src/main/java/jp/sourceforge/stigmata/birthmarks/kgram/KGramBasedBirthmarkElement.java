package jp.sourceforge.stigmata.birthmarks.kgram;

/*
 * $Id$
 */

import jp.sourceforge.stigmata.BirthmarkElement;

/**
 * 
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class KGramBasedBirthmarkElement<T> extends BirthmarkElement{
    private static final long serialVersionUID = 28546543857543634L;

    private KGram<T> kgram;

    public KGramBasedBirthmarkElement(KGram<T> kgram){
        super(kgram.toString());
        this.kgram = kgram;
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object o){
        return o instanceof KGramBasedBirthmarkElement &&
            kgram.equals(((KGramBasedBirthmarkElement)o).kgram);
    }

    public int hashCode(){
        int v = kgram.hashCode();

        return (v & 0xff << 24) | (v >> 8); 
    }
}