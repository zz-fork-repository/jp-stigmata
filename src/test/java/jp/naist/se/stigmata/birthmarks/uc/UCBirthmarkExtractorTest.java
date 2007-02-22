package jp.naist.se.stigmata.birthmarks.uc;

/*
 * $Id$
 */

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jp.naist.se.stigmata.Birthmark;
import jp.naist.se.stigmata.BirthmarkElement;
import jp.naist.se.stigmata.BirthmarkSet;
import jp.naist.se.stigmata.Stigmata;

/**
 *
 * @author Haruaki TAMADA
 * @version$Revision$ $Date$
 */
public class UCBirthmarkExtractorTest{
    private Stigmata stigmata;

    @Before
    public void setup(){
        stigmata = Stigmata.getInstance();
    }

    @Test
    public void checkUCBirthmark() throws Exception{
        BirthmarkSet[] array = stigmata.extract(
            new String[] { "uc", },
            new String[] { "target/classes/jp/naist/se/stigmata/Stigmata.class", }
        );

        Assert.assertEquals(array.length, 1);
        Assert.assertNotNull(array[0].getBirthmark("uc"));

        Birthmark birthmark = array[0].getBirthmark("uc");
        Assert.assertEquals(birthmark.getType(), "uc");
        Assert.assertEquals(birthmark.getElementCount(), 16);

        BirthmarkElement[] elements = birthmark.getElements();
        Assert.assertEquals(elements[ 0].getValue(), "java.io.ByteArrayInputStream");
        Assert.assertEquals(elements[ 1].getValue(), "java.io.ByteArrayOutputStream");
        Assert.assertEquals(elements[ 2].getValue(), "java.io.File");
        Assert.assertEquals(elements[ 3].getValue(), "java.io.FileInputStream");
        Assert.assertEquals(elements[ 4].getValue(), "java.io.InputStream");
        Assert.assertEquals(elements[ 5].getValue(), "java.lang.Class");
        Assert.assertEquals(elements[ 6].getValue(), "java.lang.Double");
        Assert.assertEquals(elements[ 7].getValue(), "java.lang.Object");
        Assert.assertEquals(elements[ 8].getValue(), "java.lang.String");
        Assert.assertEquals(elements[ 9].getValue(), "java.lang.System");
        Assert.assertEquals(elements[10].getValue(), "java.net.URI");
        Assert.assertEquals(elements[11].getValue(), "java.net.URL");
        Assert.assertEquals(elements[12].getValue(), "java.util.ArrayList");
        Assert.assertEquals(elements[13].getValue(), "java.util.Iterator");
        Assert.assertEquals(elements[14].getValue(), "java.util.List");
        Assert.assertEquals(elements[15].getValue(), "javax.imageio.spi.ServiceRegistry");
    }

    @Test
    public void checkISBirthmark2() throws Exception{
        BirthmarkSet[] array = stigmata.extract(
            new String[] { "uc", },
            new String[] { "target/classes/jp/naist/se/stigmata/ConfigFileParser.class", }
        );

        Assert.assertEquals(array.length, 1);
        Assert.assertNotNull(array[0].getBirthmark("uc"));

        Birthmark birthmark = array[0].getBirthmark("uc");
        Assert.assertEquals(birthmark.getType(), "uc");
        Assert.assertEquals(birthmark.getElementCount(), 8);

        BirthmarkElement[] elements = birthmark.getElements();
        Assert.assertEquals(elements[0].getValue(), "java.io.InputStream");
        Assert.assertEquals(elements[1].getValue(), "java.lang.Object");
        Assert.assertEquals(elements[2].getValue(), "java.lang.String");
        Assert.assertEquals(elements[3].getValue(), "java.net.URL");
        Assert.assertEquals(elements[4].getValue(), "javax.xml.parsers.SAXParser");
        Assert.assertEquals(elements[5].getValue(), "javax.xml.parsers.SAXParserFactory");
        Assert.assertEquals(elements[6].getValue(), "org.xml.sax.Attributes");
        Assert.assertEquals(elements[7].getValue(), "org.xml.sax.helpers.DefaultHandler");

    }
}