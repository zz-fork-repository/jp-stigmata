package jp.sourceforge.stigmata.ui.swing.filter;

import java.util.Locale;

import jp.sourceforge.stigmata.utils.LocalizedDescriptionManager;

/**
 * 
 * @author Haruaki TAMADA
 */
public abstract class AbstractComparisonPairFilterComponentService implements ComparisonPairFilterComponentService{
    @Override
    public String getDisplayFilterName(){
        return getDisplayFilterName(Locale.getDefault());
    }
    
    @Override
    public String getDisplayFilterName(Locale locale){
        return LocalizedDescriptionManager.getInstance().getDisplayType(
            locale, getFilterName(), LocalizedDescriptionManager.ServiceCategory.filter
        );
    }
}
