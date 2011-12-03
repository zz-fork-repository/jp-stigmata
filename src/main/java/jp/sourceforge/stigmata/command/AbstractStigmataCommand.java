package jp.sourceforge.stigmata.command;

import java.util.ArrayList;
import java.util.List;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.StigmataCommand;
import jp.sourceforge.stigmata.hook.Phase;
import jp.sourceforge.stigmata.hook.StigmataHookManager;

/**
 * 
 * @author Haruaki Tamada
 */
public abstract class AbstractStigmataCommand implements StigmataCommand{
    private List<String> messages = new ArrayList<String>();

    @Override
    public abstract String getCommandString();

    @Override
    public String[] getMessages(){
        return messages.toArray(new String[messages.size()]);
    }

    @Override
    public int getMessageSize(){
        return messages.size();
    }

    @Override
    public boolean isAvailableArguments(String[] args){
        return true;
    }

    /**
     * {@link perform(Stigmata, BirthmarkContext, String[]) <code>perform(stigmata, stigmata.createContext(), args)</code>}.
     */
    @Override
    public boolean perform(Stigmata stigmata, String[] args){
        return perform(stigmata, stigmata.createContext(), args);
    }

    @Override
    public void putMessage(String message){
        messages.add(message);
    }

    @Override
    public void setUp(BirthmarkEnvironment env){
        StigmataHookManager.getInstance().runHook(Phase.SETUP, env);
    }

    @Override
    public void tearDown(BirthmarkEnvironment env){
        StigmataHookManager.getInstance().runHook(Phase.TEAR_DOWN, env);
    }

    public String getProperty(BirthmarkEnvironment env, String[] keys, String defaultValue){
        String value = null;
        for(String key: keys){
            if(value != null){
                break;
            }
            value = env.getProperty(key);
        }
        if(value == null){
            value = defaultValue;
        }
        return value;
    }

    public String getProperty(BirthmarkContext context, String[] keys, String defaultValue){
        String value = null;
        for(String key: keys){
            if(value != null){
                break;
            }
            value = (String)context.getProperty(key);
        }
        value = getProperty(context.getEnvironment(), keys, defaultValue);

        return value;
    }
}
