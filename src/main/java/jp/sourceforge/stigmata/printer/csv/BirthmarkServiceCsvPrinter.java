package jp.sourceforge.stigmata.printer.csv;

import java.io.PrintWriter;

import jp.sourceforge.stigmata.printer.AbstractBirthmarkServicePrinter;
import jp.sourceforge.stigmata.spi.BirthmarkService;

/**
 * 
 * 
 *
 * @author Haruaki TAMADA
 */
public class BirthmarkServiceCsvPrinter extends AbstractBirthmarkServicePrinter{
    @Override
    public void printResult(PrintWriter out, BirthmarkService[] spilist){
        printHeader(out);
        for(BirthmarkService spi: spilist){
            out.print(spi.getType());
            out.print(",");
            out.print(spi.getType());
            out.print(",");
            out.print(spi.getClass().getName());
            out.print(",");
            out.print(spi.getDescription());
            out.println();
        }
        printFooter(out);
    }
}
