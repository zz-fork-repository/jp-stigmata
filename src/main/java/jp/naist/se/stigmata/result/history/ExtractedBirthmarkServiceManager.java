package jp.naist.se.stigmata.result.history;

/*
 * $Id$
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import jp.naist.se.stigmata.BirthmarkContext;
import jp.naist.se.stigmata.BirthmarkEnvironment;
import jp.naist.se.stigmata.BirthmarkStoreTarget;
import jp.naist.se.stigmata.ExtractionResultSet;
import jp.naist.se.stigmata.spi.ExtractedBirthmarkSpi;

/**
 * 
 * @author Haruaki Tamada
 * @version $Revision$ $Date$
 */
public class ExtractedBirthmarkServiceManager{
    private ExtractedBirthmarkServiceManager parent;
    private Map<BirthmarkStoreTarget, ExtractedBirthmarkSpi> targets = new HashMap<BirthmarkStoreTarget, ExtractedBirthmarkSpi>();
    private BirthmarkEnvironment env;

    public ExtractedBirthmarkServiceManager(BirthmarkEnvironment env){
        this.env = env;
        this.parent = null;
    }

    public ExtractedBirthmarkServiceManager(BirthmarkEnvironment env, ExtractedBirthmarkServiceManager parent){
        this(env);
        this.parent = parent;
    }

    public ExtractionResultSet createDefaultResultSet(BirthmarkContext context){
        BirthmarkStoreTarget bst = context.getStoreTarget();
        if(bst == null){
            String type = env.getProperty("birthmark.store.target");
            if(type == null){
                type = "XMLFILE";
            }
            bst = BirthmarkStoreTarget.valueOf(type);
        }
        if(bst == null){
            bst = BirthmarkStoreTarget.XMLFILE;
        }

        ExtractedBirthmarkSpi service = findService(bst);

        return service.createResultSet(context);
    }

    public ExtractedBirthmarkHistory getHistory(String id){
        ExtractedBirthmarkHistory history = null;
        if(parent != null){
            history = parent.getHistory(id);
        }
        if(history == null){
            int index = id.indexOf(":");
            String type = id.substring(0, index);
            BirthmarkStoreTarget bst = BirthmarkStoreTarget.valueOf(type);
            String path = id.substring(index + 1);

            ExtractedBirthmarkSpi service = findService(bst);
            if(service != null){
                history = service.getHistory(path);
            }
        }
        return history;
    }

    public synchronized String[] getHistoryIds(){
        Set<String> values = new LinkedHashSet<String>();
        if(parent != null){
            for(String id: parent.getHistoryIds()){
                values.add(id);
            }
        }
        addValuesFromProperty(values);
        addValuesFromSystemFile(values);

        char separator = File.separatorChar;
        values.add(
            "XMLFILE:" + BirthmarkEnvironment.getStigmataHome()
            + separator + "extracted_birthmarks"
        );
        return values.toArray(new String[values.size()]);
    }

    private synchronized ExtractedBirthmarkSpi findService(BirthmarkStoreTarget bst){
        ExtractedBirthmarkSpi spi = targets.get(bst);
        if(spi == null){
            refreshService();
        }
        spi = targets.get(bst);

        return spi;
    }

    private synchronized void refreshService(){
        for(Iterator<ExtractedBirthmarkSpi> i = env.lookupProviders(ExtractedBirthmarkSpi.class); i.hasNext(); ){
            ExtractedBirthmarkSpi service = i.next();
            targets.put(service.getTarget(), service);
        }
    }

    private void addValuesFromSystemFile(Set<String> values){
        File file = new File(BirthmarkEnvironment.getStigmataHome(), "storelocations.txt");
        if(file.exists()){
            try{
                BufferedReader in = new BufferedReader(new FileReader(file));
                String line;
                while((line = in.readLine()) != null){
                    values.add(line);
                }
            } catch(IOException e){
            }
        }
    }

    private void addValuesFromProperty(Set<String> values){
        String path = env.getProperty("extracted.birthmark.store.locations");
        if(path != null){
            addValuesFromProperty(values);
            String[] paths = path.split(", *");
            for(String p: paths){
                values.add(p);
            }
        }
    }
}
