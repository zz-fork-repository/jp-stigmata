package jp.naist.se.stigmata.birthmarks;

/*
 * $Id$
 */

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class LocalizedDescriptionManager{
    private Map<Locale, ResourceBundle> resources = new HashMap<Locale, ResourceBundle>();

    /**
     * only one instance of singleton pattern.
     */
    private static LocalizedDescriptionManager manager = new LocalizedDescriptionManager();

    private LocalizedDescriptionManager(){
    }

    public String getDisplayType(Locale locale, String birthmarkType){
        try{
            return getBundle(locale).getString("birthmark." + birthmarkType + ".display.type");
        } catch(MissingResourceException e){
            return null;
        }
    }

    public String getDescription(Locale locale, String birthmarkType){
        try{
            return getBundle(locale).getString("birthmark." + birthmarkType + ".description");
        } catch(MissingResourceException e){
            return null;
        }
    }

    private ResourceBundle getBundle(Locale locale){
        ResourceBundle bundle = resources.get(locale);
        if(bundle == null){
            bundle = ResourceBundle.getBundle("resources.description", locale);
            resources.put(locale, bundle);
        }
        return bundle;
    }

    public static LocalizedDescriptionManager getInstance(){
        return manager;
    }
}
