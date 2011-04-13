package jp.sourceforge.stigmata;

import java.util.ArrayList;
import java.util.List;

/**
 * Thrown if extracting birthmark is failed to find a class definition.
 * 
 * @author Haruaki TAMADA
 */
public class BirthmarkElementClassNotFoundException extends
               BirthmarkExtractionFailedException {
    private static final long serialVersionUID = 3256723476854L;

    /**
     * class name list, which names are failed to load.
     */
    private List<String> classnames = new ArrayList<String>();

    /**
     * 
     */
    public void addClassName(String name){
       classnames.add(name);
    }

    @Override
    public boolean isFailed(){
       return super.isFailed() || classnames.size() > 0;
    }

    /**
     * returns an array of class names which are failed loading.
     */
    public synchronized String[] getClassNames(){
       return classnames.toArray(new String[classnames.size()]);
    }

    @Override
    public String getMessage(){
       StringBuffer sb = new StringBuffer();
       boolean first = true;
       for(String value: classnames){
           if(!first){
               sb.append(", ");
           }
           sb.append(value);
       }
       return new String(sb);
    }
}