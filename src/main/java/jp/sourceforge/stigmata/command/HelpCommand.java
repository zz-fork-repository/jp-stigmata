package jp.sourceforge.stigmata.command;

import java.util.ResourceBundle;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.ComparisonPairFilter;
import jp.sourceforge.stigmata.ComparisonPairFilterSet;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.spi.BirthmarkSpi;
import jp.sourceforge.talisman.xmlcli.ResourceHelpFormatter;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * 
 * @author Haruaki Tamada
 */
public class HelpCommand extends AbstractStigmataCommand{
    private Options options;

    public HelpCommand(Options options){
        this.options = options;
    }

    @Override
    public String getCommandString(){
        return "help";
    }

    @Override
    public void perform(Stigmata stigmata, BirthmarkContext context, String[] args){
        BirthmarkEnvironment env = context.getEnvironment();
        Package p = getClass().getPackage();

        ResourceBundle helpResource = ResourceBundle.getBundle("resources.options");
        HelpFormatter formatter = new ResourceHelpFormatter(helpResource);
        formatter.printHelp(
            String.format(
                helpResource.getString("cli.interface"),
                p.getImplementationVersion()
            ),
            options
        );
        System.out.println();
        System.out.println(helpResource.getString("cli.interface.birthmarks"));
        for(BirthmarkSpi service: env.getServices()){
            if(!service.isExperimental()){
                System.out.printf("    %-5s (%s): %s%n", service.getType(),
                        service.getDisplayType(), service.getDescription());
            }
        }
        System.out.println();
        System.out.println(helpResource.getString("cli.interface.filters"));
        for(ComparisonPairFilterSet filterset: env.getFilterManager().getFilterSets()){
            String matchString = helpResource.getString("cli.interface.filter.matchall");
            if(filterset.isMatchAny()) matchString = helpResource.getString("cli.interface.filter.matchany");
            System.out.printf("    %s (%s)%n", filterset.getName(), matchString);
            for(ComparisonPairFilter filter: filterset){
                System.out.printf("        %s%n", filter);
            }
        }
        System.out.println();
        System.out.println(helpResource.getString("cli.interface.copyright"));
        System.out.println(helpResource.getString("cli.interface.mailto"));
    }
}
