package jp.sourceforge.stigmata.birthmarks.kgram;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.ExtractionUnit;
import jp.sourceforge.stigmata.birthmarks.ASMBirthmarkExtractor;
import jp.sourceforge.stigmata.birthmarks.BirthmarkExtractVisitor;
import jp.sourceforge.stigmata.spi.BirthmarkSpi;

import org.objectweb.asm.ClassWriter;

/**
 * @author Haruaki TAMADA
 */
public class KGramBasedBirthmarkExtractor extends ASMBirthmarkExtractor{
    private int kvalue = 4;

    public KGramBasedBirthmarkExtractor(BirthmarkSpi spi){
        super(spi);
    }

    public KGramBasedBirthmarkExtractor(){
        super();
    }

    public void setKValue(int kvalue){
        this.kvalue = kvalue;
    }

    public int getKValue(){
        return kvalue;
    }

    @Override
    public BirthmarkExtractVisitor createExtractVisitor(ClassWriter writer, Birthmark birthmark, BirthmarkContext context){
        KGramBasedBirthmarkExtractVisitor extractor = new KGramBasedBirthmarkExtractVisitor(writer, birthmark, context);
        extractor.setKValue(getKValue());
        return extractor;
    }

    @Override
    public ExtractionUnit[] getAcceptableUnits(){
        return new ExtractionUnit[] {
            ExtractionUnit.CLASS, ExtractionUnit.PACKAGE, ExtractionUnit.ARCHIVE, 
        };
    }
}
