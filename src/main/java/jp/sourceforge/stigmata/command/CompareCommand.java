package jp.sourceforge.stigmata.command;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEngine;
import jp.sourceforge.stigmata.ComparisonMethod;
import jp.sourceforge.stigmata.ComparisonResultSet;
import jp.sourceforge.stigmata.ExtractionResultSet;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.event.BirthmarkEngineAdapter;
import jp.sourceforge.stigmata.event.BirthmarkEngineEvent;
import jp.sourceforge.stigmata.event.WarningMessages;
import jp.sourceforge.stigmata.printer.ComparisonResultSetPrinter;
import jp.sourceforge.stigmata.spi.ResultPrinterService;

/**
 * 
 * @author Haruaki Tamada
 */
public class CompareCommand extends AbstractStigmataCommand{
    @Override
    public boolean isAvailableArguments(String[] args){
        return args.length > 0;
    }

    @Override
    public String getCommandString(){
        return "compare";
    }

    @Override
    public boolean perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        try{
            BirthmarkEngine engine = new BirthmarkEngine(context.getEnvironment());
            context.setComparisonMethod(ComparisonMethod.ROUND_ROBIN_SAME_PAIR);
            engine.addBirthmarkEngineListener(new BirthmarkEngineAdapter(){
                @Override
                public void operationDone(BirthmarkEngineEvent e){
                    WarningMessages warnings = e.getMessage();
                    for(Iterator<Exception> i = warnings.exceptions(); i.hasNext(); ){
                        i.next().printStackTrace();
                    }
                }
            });

            ExtractionResultSet rs = engine.extract(args, context);
            ComparisonResultSet resultset = engine.compare(rs);
            if(context.hasFilter()){
                resultset = engine.filter(resultset);
            }

            ResultPrinterService spi = stigmata.getPrinterManager().getService(context.getFormat());
            ComparisonResultSetPrinter formatter = spi.getComparisonResultSetPrinter();
            String encoding = getProperty(context, new String[] { "encoding.output", "encoding" }, "utf-8");
            formatter.printResult(new PrintWriter(new OutputStreamWriter(System.out, encoding)), resultset);
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
