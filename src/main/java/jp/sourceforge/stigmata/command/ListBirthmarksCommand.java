package jp.sourceforge.stigmata.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.printer.BirthmarkServicePrinter;
import jp.sourceforge.stigmata.spi.BirthmarkService;
import jp.sourceforge.stigmata.spi.ResultPrinterService;

/**
 * 
 * @author Haruaki Tamada
 */
public class ListBirthmarksCommand extends AbstractStigmataCommand{
    @Override
    public String getCommandString(){
        return "list-birthmarks";
    }

    @Override
    public boolean perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        BirthmarkService[] spis = context.getEnvironment().findServices();
        ResultPrinterService spi = stigmata.getPrinterManager().getService(context.getFormat());
        BirthmarkServicePrinter formatter = spi.getBirthmarkServicePrinter();

        try{
            PrintWriter out;
            String encoding = getProperty(context, new String[] { "encoding.output", "encoding", }, "utf-8");
            if(args.length == 0){
                out = new PrintWriter(new OutputStreamWriter(System.out, encoding));
            }
            else{
                String target = validateTarget(args[0], context.getFormat());
                out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(target), encoding));
            }
            formatter.printResult(out, spis);
        }catch(IOException e){
            e.printStackTrace();
        }
        return true;
    }

    private String validateTarget(String fileName, String format){
        if(!fileName.endsWith("." + format)){
            fileName = fileName + "." + format;
        }
        return fileName;
    }
}
