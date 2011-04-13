package jp.sourceforge.stigmata.spi;

import jp.sourceforge.stigmata.hook.StigmataHook;
import jp.sourceforge.stigmata.hook.StigmataRuntimeHook;

/**
 * 
 * @author Haruaki Tamada
 */
public interface StigmataHookService{
    public String getType();

    public String getDescription();

    public StigmataHook onSetup();

    public StigmataHook onTearDown();

    public StigmataRuntimeHook beforeExtraction();

    public StigmataRuntimeHook afterExtraction();

    public StigmataRuntimeHook beforeComparison();

    public StigmataRuntimeHook afterComparison();

    public StigmataRuntimeHook beforeFiltering();

    public StigmataRuntimeHook afterFiltering();
}