package jp.sourceforge.stigmata.birthmarks.cvfv;

import java.io.FileInputStream;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkElement;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.BirthmarkExtractor;
import jp.sourceforge.stigmata.utils.WellknownClassJudgeRule;
import jp.sourceforge.stigmata.utils.WellknownClassJudgeRule.MatchPartType;
import jp.sourceforge.stigmata.utils.WellknownClassJudgeRule.MatchType;
import jp.sourceforge.stigmata.utils.WellknownClassManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Haruaki TAMADA
 */
public class CVFVBirthmarkExtractorTest{
    private BirthmarkExtractor extractor; 

    @Before
    public void setup(){
        extractor = new ConstantValueOfFieldVariableBirthmarkService().getExtractor();
        BirthmarkEnvironment env = BirthmarkEnvironment.getDefaultEnvironment();
        WellknownClassManager manager = env.getWellknownClassManager();
        manager.add(new WellknownClassJudgeRule("java.", MatchType.PREFIX, MatchPartType.FULLY_NAME));
        manager.add(new WellknownClassJudgeRule("javax.", MatchType.PREFIX, MatchPartType.FULLY_NAME));
    }


    @Test
    public void checkBirthmark() throws Exception{
        Birthmark birthmark = extractor.extract(new FileInputStream("target/test-classes/resources/HelloWorldFrame.class"));
        Assert.assertEquals("cvfv", birthmark.getType());

        BirthmarkElement[] elements = birthmark.getElements();
        Assert.assertEquals(2, elements.length);

        Assert.assertTrue(elements[0] instanceof TypeAndValueBirthmarkElement);
        Assert.assertTrue(elements[1] instanceof TypeAndValueBirthmarkElement);

        Assert.assertEquals("Ljava/lang/String;", ((TypeAndValueBirthmarkElement)elements[0]).getSignature());
        Assert.assertEquals("Ljava/lang/String;", ((TypeAndValueBirthmarkElement)elements[1]).getSignature());

        Assert.assertEquals("Hello World", elements[0].getValue());
        Assert.assertEquals("Lucida Regular", elements[1].getValue());
    }
}
