package jp.sourceforge.stigmata.birthmarks.smc;

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
public class SequentialMethodCallBirthmarkExtractorTest{
    private BirthmarkExtractor extractor; 

    @Before
    public void setup(){
        extractor = new SequentialMethodCallBirthmarkService().getExtractor();
        BirthmarkEnvironment env = BirthmarkEnvironment.getDefaultEnvironment();
        WellknownClassManager manager = env.getWellknownClassManager();
        manager.add(new WellknownClassJudgeRule("java.", MatchType.PREFIX, MatchPartType.FULLY_NAME));
        manager.add(new WellknownClassJudgeRule("javax.", MatchType.PREFIX, MatchPartType.FULLY_NAME));
    }


    @Test
    public void checkBirthmark() throws Exception{
        Birthmark birthmark = extractor.extract(new FileInputStream("target/test-classes/resources/HelloWorldFrame.class"));
        Assert.assertEquals("smc", birthmark.getType());

        BirthmarkElement[] elements = birthmark.getElements();
        Assert.assertEquals(10, elements.length);

        Assert.assertEquals("java.lang.Object#<init>!()V",   elements[0].getValue());
        Assert.assertEquals("javax.swing.JFrame#<init>!()V", elements[1].getValue());
        Assert.assertEquals("javax.swing.JLabel#<init>!(Ljava/lang/String;)V",  elements[2].getValue());
        Assert.assertEquals("java.awt.Font#<init>!(Ljava/lang/String;II)V",     elements[3].getValue());
        Assert.assertEquals("javax.swing.JLabel#setFont!(Ljava/awt/Font;)V",    elements[4].getValue());
        Assert.assertEquals("javax.swing.JFrame#setDefaultCloseOperation!(I)V", elements[5].getValue());
        Assert.assertEquals("javax.swing.JFrame#getContentPane!()Ljava/awt/Container;",
                            elements[6].getValue());
        Assert.assertEquals("java.awt.Container#add!(Ljava/awt/Component;Ljava/lang/Object;)V",
                            elements[7].getValue());
        Assert.assertEquals("javax.swing.JFrame#pack!()V",                      elements[8].getValue());
        Assert.assertEquals("javax.swing.JFrame#setVisible!(Z)V",               elements[9].getValue());
    }
}
