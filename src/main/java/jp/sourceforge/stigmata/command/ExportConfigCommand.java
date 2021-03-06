package jp.sourceforge.stigmata.command;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.utils.ConfigFileExporter;

/**
 * 
 * @author Haruaki Tamada
 */
public class ExportConfigCommand extends AbstractStigmataCommand{

    @Override
    public String getCommandString(){
        return "export-config";
    }

    @Override
    public boolean perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        PrintWriter out = null;
        try{
            if(args == null || args.length == 0){
                out = new PrintWriter(System.out);
            }
            else{
                if(!args[0].endsWith(".xml")){
                    args[0] = args[0] + ".xml";
                }
                out = new PrintWriter(new FileWriter(args[0]));
            }

            new ConfigFileExporter(context.getEnvironment()).export(out);
            return true;
        }catch(IOException e){
            return false;
        } finally{
            if(out != null){
                out.close();
            }
        }
    }
}
