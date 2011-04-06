package jp.sourceforge.stigmata.filter;

import java.util.Locale;

import jp.sourceforge.stigmata.spi.AbstractServiceProvider;
import jp.sourceforge.stigmata.spi.ComparisonPairFilterSpi;
import jp.sourceforge.stigmata.utils.LocalizedDescriptionManager;

/**
 * 
 * @author Haruaki TAMADA
 */
abstract class AbstractComparisonPairFilterService extends AbstractServiceProvider implements ComparisonPairFilterSpi{
    @Override
    public String getDescription(Locale locale){
        return LocalizedDescriptionManager.getInstance().getDescription(
                locale, getFilterName(), LocalizedDescriptionManager.ServiceCategory.filter
            );
    }

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
