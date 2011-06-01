package jp.sourceforge.stigmata.spi;

import jp.sourceforge.stigmata.BirthmarkComparator;
import jp.sourceforge.stigmata.BirthmarkExtractor;
import jp.sourceforge.stigmata.BirthmarkPreprocessor;

/**
 * Birthmark service provider interface.
 *
 * @author Haruaki TAMADA
 */
public interface BirthmarkService{
    /**
     * returns a type of the birthmark this service provides.
     */
    public String getType();

    /**
     * returns a description of the birthmark this service provides.
     */
    public String getDescription();

    /**
     * returns a preprocessor for the birthmark of this service.
     */
    public BirthmarkPreprocessor getPreprocessor();

    /**
     * returns a extractor for the birthmark of this service.
     */
    public BirthmarkExtractor getExtractor();

    /**
     * returns a comparator for the birthmark of this service.
     */
    public BirthmarkComparator getComparator();

    public boolean isExperimental();

    public boolean isUserDefined();
}

