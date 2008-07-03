package jp.sourceforge.stigmata.printer.xml;

/*
 * $Id$
 */

import java.io.PrintWriter;
import java.util.Iterator;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkElement;
import jp.sourceforge.stigmata.BirthmarkSet;
import jp.sourceforge.stigmata.ExtractionResultSet;
import jp.sourceforge.stigmata.ExtractionTarget;
import jp.sourceforge.stigmata.printer.AbstractExtractionResultSetPrinter;

/**
 * 
 * 
 *
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class ExtractionResultSetXmlPrinter extends AbstractExtractionResultSetPrinter{
    public void printResult(PrintWriter out, ExtractionResultSet ers){
        printHeader(out);

        out.printf("    <unit>%s</unit>%n", ers.getExtractionUnit());
        out.printf("    <birthmark-types>%n");
        for(String type: ers.getBirthmarkTypes()){
            out.printf("      <birthmark-type>%s</birthmark-type>%n", type);
        }
        out.printf("    </birthmark-types>%n");
        for(Iterator<BirthmarkSet> i = ers.birthmarkSets(ExtractionTarget.TARGET_BOTH); i.hasNext(); ){
            printBirthmarkSet(out, i.next());
        }
        printFooter(out);
    }

    public void printHeader(PrintWriter out){
        out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        out.println("<birthmark-result-set>");
        out.println("  <extracted-birthmarks>");
    }

    public void printFooter(PrintWriter out){
        out.println("  </extracted-birthmarks>");
        out.println("</birthmark-result-set>");
        out.flush();
    }

    public void printBirthmarkSet(PrintWriter out, BirthmarkSet set){
        out.println("    <extracted-birthmark>");
        out.printf("      <name>%s</name>%n", escapeToXmlString(set.getName()));
        out.printf("      <location>%s</location>%n", escapeToXmlString(set.getLocation()));
        for(Iterator<String> i = set.birthmarkTypes(); i.hasNext(); ){
            String type = i.next();
            Birthmark birthmark = set.getBirthmark(type);
            out.printf("      <birthmark type=\"%s\" count=\"%d\">%n",
                       birthmark.getType(), birthmark.getElementCount());
            for(Iterator<BirthmarkElement> elements = birthmark.iterator(); elements.hasNext(); ){
                out.printf("        <element>%s</element>%n",
                           escapeToXmlString(String.valueOf(elements.next())));
            }
            out.println("      </birthmark>");
        }
        out.println("    </extracted-birthmark>");
    }

    public String escapeToXmlString(Object o){
        if(o != null){
            return escapeToXmlString(o.toString());
        }
        return null;
    }

    public String escapeToXmlString(String string){
        string = string.replaceAll("&",  "&amp;");
        string = string.replaceAll("\"", "&quot;");
        string = string.replaceAll("<",  "&lt;");
        string = string.replaceAll(">",  "&gt;");

        return string;
    }
}
