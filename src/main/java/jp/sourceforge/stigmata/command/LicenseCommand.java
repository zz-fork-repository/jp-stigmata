package jp.sourceforge.stigmata.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.Stigmata;

/**
 * 
 * @author Haruaki Tamada
 */
public class LicenseCommand extends AbstractStigmataCommand{
    @Override
    public String getCommandString(){
        return "license";
    }

    @Override
    public boolean perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        BufferedReader reader = null;
        try{
            InputStream in = getClass().getResourceAsStream("/META-INF/license.txt");
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
            String line;

            while((line = reader.readLine()) != null){
                System.out.println(line);
            }
            reader.close();
            return true;
        }catch(IOException ex){
            return false;
        } finally{
            if(reader != null){
                try{
                    reader.close();
                } catch(IOException e){
                    throw new InternalError(e.getMessage());
                }
            }
        }
    }
}
