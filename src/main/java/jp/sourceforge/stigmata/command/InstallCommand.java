package jp.sourceforge.stigmata.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.utils.Utility;

/**
 * 
 * @author Haruaki Tamada
 */
public class InstallCommand extends AbstractStigmataCommand{
    @Override
    public boolean isAvailableArguments(String[] args){
        return args.length > 0;
    }

    @Override
    public String getCommandString(){
        return "install";
    }

    @Override
    public boolean perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        File pluginsDir = new File(BirthmarkEnvironment.getStigmataHome(), "plugins");
        BirthmarkEnvironment env = context.getEnvironment();
        List<String> messages = new ArrayList<String>();

        for(int i = 0; i < args.length; i++){
            File pluginSource = new File(args[i]);
            File pluginDest = new File(pluginsDir, pluginSource.getName());

            if(!Utility.isStigmataPluginJarFile(pluginSource, messages)){
                for(String msg: messages){
                    putMessage(msg);
                }
                throw new IllegalArgumentException(pluginSource + ": not stigmata plugin file.");
            }
            if(pluginDest.exists()){
                String override = env.getProperty("override.exists.plugin");
                if(override != null &&
                   (override.equalsIgnoreCase("true") || override.equalsIgnoreCase("yes"))){
                    pluginDest.delete();
                }
                else{
                    File backupFile = new File(pluginDest.getParent(), pluginDest.getName() + ".back");
                    if(backupFile.exists()) backupFile.delete();
                    pluginDest.renameTo(backupFile);
                }
            }
            copyFile(pluginSource, pluginDest);
            File parent = pluginSource.getParentFile();
            File destParent = pluginDest.getParentFile();
            String[] myDependencies = null;
            for(String dependency: Utility.getDependencies(pluginSource)){
                if(myDependencies == null){
                    myDependencies = findStigmataDependencies();
                }
                boolean include = false;
                for(String systemDependency: myDependencies){
                    if(dependency.equals(systemDependency)){
                        include = true;
                        break;
                    }
                }
                if(!include){
                    File dependencyFile = new File(parent, dependency);
                    if(dependencyFile.exists()){
                        copyFile(dependencyFile, new File(destParent, dependency));
                    }
                    else{
                        putMessage(dependency + ": not found. Install this jar file into plugin directory");
                    }
                }
            }
        }
        return getMessageSize() == 0;
    }

    private String[] findStigmataDependencies(){
        URL url = getClass().getResource("/jp/sourceforge/stigmata/command/InstallCommand.class");
        String jarfilePath = url.toString();
        String[] deps = new String[0];
        if(jarfilePath.startsWith("jar:")){
            jarfilePath = jarfilePath.substring("jar:".length(), jarfilePath.lastIndexOf("!"));
            try{
                deps = Utility.getDependencies(new File(new URI(jarfilePath)));
            } catch(URISyntaxException e){
                e.printStackTrace();
            }
        }
        return deps;
    }

    private void copyFile(File source, File dest){
        byte[] data = new byte[256];
        int read;
        InputStream in = null;
        OutputStream out = null;
        
        try{
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);

            while((read = in.read(data)) != -1){
                out.write(data, 0, read);
            }
            in.close();
            out.close();
        } catch(IOException e){
        } finally{
            String message = null;
            if(in != null){
                try{
                    in.close();
                } catch(IOException e){
                    message = e.getMessage();
                }
            }
            if(out != null){
                try{
                    out.close();
                } catch(IOException e){
                    if(message != null){
                        message += ", " + e.getMessage();
                    }
                    else{
                        message = e.getMessage();
                    }
                }
            }
            if(message != null){
                throw new InternalError(message);
            }
        }
    }
}
