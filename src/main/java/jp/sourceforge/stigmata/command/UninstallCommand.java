package jp.sourceforge.stigmata.command;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.spi.BirthmarkService;
import jp.sourceforge.stigmata.utils.ConfigFileExporter;

/**
 * 
 * @author Haruaki Tamada
 */
@Deprecated
public class UninstallCommand extends AbstractStigmataCommand{

    @Override
    public boolean isAvailableArguments(String[] args){
        return args.length > 0;
    }

    @Override
    public String getCommandString(){
        return "uninstall";
    }

    @Override
    public boolean perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        File pluginsDir = new File(BirthmarkEnvironment.getStigmataHome(), "plugins");
        BirthmarkEnvironment env = context.getEnvironment();
        boolean removeServiceInConfigFile = false;

        for(int i = 0; i < args.length; i++){
            BirthmarkService service = env.getService(args[i]);
            env.removeService(args[i]);
            removeServiceInConfigFile = true;

            String fileName = getPluginFileNameOfService(context, service);
            if(fileName != null){
                File pluginFile = new File(pluginsDir, fileName);
                pluginFile.renameTo(new File(pluginFile.getParentFile(), pluginFile.getName() + ".back"));
            }
        }
        if(removeServiceInConfigFile){
            updateConfigFile(env);
        }
        return true;
    }

    private void updateConfigFile(BirthmarkEnvironment env){
        File configFile = new File(BirthmarkEnvironment.getStigmataHome(), "stigmata.xml");
        try{
            String encoding = getProperty(env, new String[] { "encoding.output", "encoding", }, "utf-8");
            new ConfigFileExporter(env).export(
                new PrintWriter(new OutputStreamWriter(new FileOutputStream(configFile), encoding))
            );
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private String getPluginFileNameOfService(BirthmarkContext context, BirthmarkService service){
        Class<?> serviceClass = service.getClass();
        URL location = serviceClass.getResource("/" + serviceClass.getName().replace('.', '/') + ".class");

        if(location != null){
            Pattern pattern = Pattern.compile("jar:(.*)/plugins/(.*.jar)!([a-zA-Z0-9$/.]+.class)");
            Matcher matcher = pattern.matcher(location.toString());

            if(matcher.matches()){
                try{
                    URL homeLocation = new File(BirthmarkEnvironment.getStigmataHome()).toURI().toURL();
                    String matchedLocation = matcher.group(1) + "/";
                    if(matchedLocation.equals(homeLocation.toString())){
                        return matcher.group(2);
                    }
                } catch(MalformedURLException e){
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
