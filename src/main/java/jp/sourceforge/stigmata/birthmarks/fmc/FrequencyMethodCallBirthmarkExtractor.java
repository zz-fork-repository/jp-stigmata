package jp.sourceforge.stigmata.birthmarks.fmc;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkElement;
import jp.sourceforge.stigmata.ExtractionUnit;
import jp.sourceforge.stigmata.birthmarks.ASMBirthmarkExtractor;
import jp.sourceforge.stigmata.birthmarks.BirthmarkExtractVisitor;
import jp.sourceforge.stigmata.birthmarks.FrequencyBirthmark;
import jp.sourceforge.stigmata.birthmarks.FrequencyBirthmarkElement;
import jp.sourceforge.stigmata.birthmarks.smc.SequentialMethodCallBirthmarkExtractVisitor;
import jp.sourceforge.stigmata.spi.BirthmarkService;

import org.objectweb.asm.ClassWriter;

/**
 * @author Haruaki TAMADA
 */
public class FrequencyMethodCallBirthmarkExtractor extends ASMBirthmarkExtractor{
    public FrequencyMethodCallBirthmarkExtractor(BirthmarkService spi){
        super(spi);
    }

    public FrequencyMethodCallBirthmarkExtractor(){
        super();
    }

    @Override
    public BirthmarkExtractVisitor createExtractVisitor(ClassWriter writer, Birthmark birthmark, BirthmarkContext context){
        return new SequentialMethodCallBirthmarkExtractVisitor(writer, birthmark, context){
            @Override
            protected void addElement(String className, String methodName, String description){
                addElement(new FrequencyBirthmarkElement(className + "#" + methodName + description));
            }
        };
    }

    @Override
    public ExtractionUnit[] getAcceptableUnits(){
        return new ExtractionUnit[] {
            ExtractionUnit.CLASS, ExtractionUnit.PACKAGE, ExtractionUnit.ARCHIVE, 
        };
    }

    @Override
    public Birthmark createBirthmark(){
        return new FrequencyBirthmark(getProvider().getType());
    }


    @Override
    public BirthmarkElement buildElement(String value) {
        return new FrequencyBirthmarkElement(value);
    }
}
