package jp.sourceforge.stigmata.ui.swing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import jp.sourceforge.stigmata.spi.BirthmarkService;
import jp.sourceforge.talisman.i18n.Messages;

/**
 * 
 * @author Haruaki TAMADA
 */
public abstract class BirthmarkSelectablePane extends JPanel implements BirthmarkServiceListener{
    private static final long serialVersionUID = 7057130952947891635L;

    private StigmataFrame stigmata;
    private Set<String> selectedServices = new HashSet<String>();
    private Map<String, BirthmarkSelection> services;
    private List<DataChangeListener> listeners = new ArrayList<DataChangeListener>();
    private boolean experimentalMode;

    public BirthmarkSelectablePane(StigmataFrame stigmata){
        this.stigmata = stigmata;

        initServices();
        stigmata.addBirthmarkServiceListener(this);
    }

    protected abstract void updateLayouts();

    public Messages getMessages(){
        return stigmata.getMessages();
    }

    public void setExperimentalMode(boolean experimentalMode){
        this.experimentalMode = experimentalMode;
        updateLayouts();
    }

    public boolean isExperimentalMode(){
        return experimentalMode;
    }

    public String[] getSelectedServiceTypes(){
        return selectedServices.toArray(new String[selectedServices.size()]);
    }

    public BirthmarkService getService(String type){
        BirthmarkSelection elem = services.get(type);
        if(elem != null){
            return elem.getService();
        }
    
        return null;
    }

    public String[] getServiceTypes(){
        String[] serviceArray = new String[services.size()];
        int index = 0;
        for(String key: services.keySet()){
            BirthmarkService service = services.get(key).getService();
            serviceArray[index] = service.getType();
            index++;
        }
        return serviceArray;
    }

    public boolean hasService(String type){
        return services.get(type) != null;
    }

    public void select(String type, boolean flag){
        if(flag){
            selectedServices.add(type);
        }
        else{
            selectedServices.remove(type);
        }
        fireEvent();
    }

    public void select(BirthmarkService service, boolean flag){
        select(service.getType(), flag);
    }

    public void reset(){
        selectedServices.clear();
        initServices();
        experimentalMode = false;
        updateLayouts();
        fireEvent();
    }

    public void addDataChangeListener(DataChangeListener listener){
        listeners.add(listener);
    }

    @Override
    public void serviceAdded(BirthmarkService service){
        if(services.get(service.getType()) == null){
            BirthmarkSelection elem = new BirthmarkSelection(service);
            selectedServices.add(service.getType());
            services.put(service.getType(), elem);
        }
        updateLayouts();
        fireEvent();
    }

    @Override
    public void serviceRemoved(BirthmarkService service){
        BirthmarkSelection elem = services.get(service.getType());
        if(elem != null){
            selectedServices.remove(service.getType());
            services.remove(service.getType());
        }
        fireEvent();
    }

    public Iterator<String> serviceNames(){
        return services.keySet().iterator();
    }

    protected BirthmarkSelection getSelection(String type){
        return services.get(type);
    }

    protected Iterator<BirthmarkSelection> birthmarkSelections(){
        return services.values().iterator();
    }

    protected void fireEvent(){
        for(DataChangeListener listener: listeners){
            listener.valueChanged(this);
        }
    }

    private void initServices(){
        BirthmarkService[] serviceArray = stigmata.getEnvironment().getServices();

        services = new LinkedHashMap<String, BirthmarkSelection>();
        for(BirthmarkService service: serviceArray){
            BirthmarkSelection elem = new BirthmarkSelection(service);
            services.put(service.getType(), elem);
        }
    }
}