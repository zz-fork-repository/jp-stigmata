package jp.sourceforge.stigmata.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.talisman.hermes.Hermes;
import jp.sourceforge.talisman.hermes.HermesContext;
import jp.sourceforge.talisman.hermes.HermesContextExporter;
import jp.sourceforge.talisman.hermes.HermesContextParser;
import jp.sourceforge.talisman.hermes.HermesException;
import jp.sourceforge.talisman.hermes.InvalidHermesConfigException;
import jp.sourceforge.talisman.hermes.maven.Artifact;

/**
 * 
 * @author Haruaki Tamada
 */
public class HermesUtility{
    private HermesContext context;
    private Hermes hermes;
    private Artifact[] updateTargets;

    public HermesUtility(BirthmarkEnvironment env) throws IOException, InvalidHermesConfigException{
        loadHermesContext(env);
    }

    public HermesUtility(){
    }

    public void updateContext(BirthmarkEnvironment env) throws IOException{
        String path = env.getProperty("location.hermes.config");
        OutputStream out = null;
        try{
            if(path != null && path.startsWith("file:")){
                out = new URL(path).openConnection().getOutputStream();
            }
            else{
                File file = new File(BirthmarkEnvironment.getStigmataHome(), "plugins/hermes.xml");
                out = new FileOutputStream(file);
            }
            HermesContextExporter exporter = new HermesContextExporter();
            exporter.export(out, context);
        } finally{
            if(out != null){
                out.close();
            }
        }
    }

    public void loadHermesContext(BirthmarkEnvironment env) throws IOException, InvalidHermesConfigException{
        HermesContextParser parser = new HermesContextParser();
        InputStream in = getInputStream(env);
        context = parser.parse(in);
        if(in != null){
            try{
                in.close();
            } catch(IOException e){
                // ignore exception.
            }
        }
        if(context.getDestination().contains("${stigmata.home}")){
            String dest = context.getDestination();
            dest = dest.replace("${stigmata.home}", BirthmarkEnvironment.getStigmataHome());
            context.setDestination(dest);
        }

        hermes = new Hermes(context);
    }

    public boolean canUpdate() throws IOException, HermesException{
        return getUpdateTarget().length > 0;
    }

    public Artifact getCurrentArtifact(String groupId, String artifactId){
        return context.getDependency(groupId, artifactId);
    }

    public Artifact[] getUpdateTarget() throws IOException, HermesException{
        if(updateTargets == null){
            updateTargets = hermes.getUpdateTarget();
        }
        Artifact[] artifacts = new Artifact[updateTargets.length];
        System.arraycopy(updateTargets, 0, artifacts, 0, artifacts.length);
        return artifacts;
    }

    public void update() throws IOException, HermesException{
        hermes.update();
        updateTargets = null;
    }

    public Hermes getHermes(){
        if(hermes == null){
            throw new IllegalStateException("call loadHermesContext first!");
        }
        return hermes;
    }

    private InputStream getInputStream(BirthmarkEnvironment env) throws IOException{
        InputStream in = null;
        if(env.getProperty("location.hermes.config") != null){
            try{
                URL url = new URL(env.getProperty("location.hermes.config"));
                in = url.openStream();
            } catch(MalformedURLException e){
                // ignore exception.
            }
        }
        else{
            File file = new File(BirthmarkEnvironment.getStigmataHome(), "plugins/hermes.xml");
            if(!file.exists()){
                copyFile(getClass().getResource("/resources/hermes.xml"), file);
            }

            in = new FileInputStream(file);
        }
        return in;
    }

    private void copyFile(URL from, File to) throws IOException{
        InputStream in = null;
        OutputStream out = null;
        try{
            in = from.openStream();
            out = new FileOutputStream(to);
            byte[] data = new byte[256];
            int read = 0;
            while((read = in.read(data)) != -1){
                out.write(data, 0, read);
            }
        } finally{
            if(in != null){
                try{
                    in.close();
                } catch(IOException e){
                }
            }
            if(out != null){
                try{
                    out.close();
                } catch(IOException e){
                }
            }
        }
    }
}
