package jp.sourceforge.stigmata.birthmarks.cvfv;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkElement;
import jp.sourceforge.stigmata.ExtractionUnit;
import jp.sourceforge.stigmata.birthmarks.ASMBirthmarkExtractor;
import jp.sourceforge.stigmata.birthmarks.BirthmarkExtractVisitor;
import jp.sourceforge.stigmata.spi.BirthmarkService;

import org.objectweb.asm.ClassWriter;

/**
 * 
 * @author Haruaki TAMADA
 */
public class ConstantValueOfFieldVariableBirthmarkExtractor extends ASMBirthmarkExtractor{
    public ConstantValueOfFieldVariableBirthmarkExtractor(BirthmarkService spi){
        super(spi);
    }

    public ConstantValueOfFieldVariableBirthmarkExtractor(){
        super();
    }

    @Override
    public BirthmarkExtractVisitor createExtractVisitor(ClassWriter writer, Birthmark birthmark, BirthmarkContext context) {
        return new ConstantValueOfFieldVariableBirthmarkExtractVisitor(writer, birthmark, context);
    }

    @Override
    public ExtractionUnit[] getAcceptableUnits(){
        return new ExtractionUnit[] {
            ExtractionUnit.CLASS, ExtractionUnit.PACKAGE, ExtractionUnit.ARCHIVE, 
        };
    }

    @Override
    public BirthmarkElement buildElement(String value) {
    	String signature = value.substring(0, value.indexOf('='));
    	String subValue = value.substring(value.indexOf('=') + 1);
    	Object elementValue = subValue;
    
        if(subValue.equals("null")){
            elementValue = null;
        }
        else{
            switch(signature.charAt(0)){
            case 'Z':{
                if(value.equals("true")) elementValue = Boolean.TRUE;
                else                     elementValue = Boolean.FALSE;
                break;
            }
            case 'C': elementValue = subValue.charAt(0); break;
            case 'D': elementValue = subValue;           break;
            case 'F': elementValue = subValue;           break;
            case 'S': elementValue = subValue;           break;
            case 'B': elementValue = subValue;           break;
            case 'I': elementValue = subValue;           break;
            default:  elementValue = value;              break;
            }
    	}
    	return new TypeAndValueBirthmarkElement(signature, elementValue);
    }
}
