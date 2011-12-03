package jp.sourceforge.stigmata.ui.swing.mds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.sourceforge.talisman.mds.ui.swing.GeometoryType;

/**
 * @author Haruaki TAMADA
 */
class LabelMap{
    private List<String> labels = new ArrayList<String>();
    private Map<String, String> groups = new HashMap<String, String>();
    private Map<String, Integer> gids = new HashMap<String, Integer>();
    private boolean groupEnabled = true;

    public LabelMap(){
        gids.put("", 0);
    }

    public LabelMap(String[] labels){
        this();
        for(String label: labels){
            addLabel(label);
        }
    }

    public boolean isAvailableLabel(int index){
        return index >= 0 && index < labels.size();
    }

    public void setGroupEnabled(boolean flag){
        this.groupEnabled = flag;
    }

    public boolean isGroupEnabled(){
        return groupEnabled && getGroupCount() < GeometoryType.getMaxGroupCount();
    }

    public void addLabel(String label){
        labels.add(label);
    }

    public String getLabel(int index){
        return labels.get(index);
    }

    public void setGroup(String label, String groupLabel){
        groups.put(label, groupLabel);
        if(gids.get(groupLabel) == null){
            gids.put(groupLabel, gids.size());
        }
    }

    public String getGroup(String label){
        String group = groups.get(label);
        if(group == null){
            group = "";
        }
        return group;
    }

    public int getGroupIdFromElementName(String label){
        return getGroupId(groups.get(label));
    }

    public int getGroupId(String groupLabel){
        Integer i = 0;
        if(isGroupEnabled()){
            i = gids.get(groupLabel);
            if(i == null){
                i = 0;
            }
        }
        return i;
    }

    public int getGroupCount(){
        return gids.size();
    }

    public String[] getGroupNames(){
        String[] names = new String[gids.size()];
        int index = 0;
        for(String name: gids.keySet()){
            names[index] = name;
            index++;
        }

        return names;
    }

    public synchronized Map<String, Integer> getGroupElementCounts(){
        Map<String, Integer> map = new HashMap<String, Integer>();

        for(Map.Entry<String, String> entry: groups.entrySet()){
            Integer i = map.get(entry.getValue());
            if(i == null){
                i = 0;
            }
            map.put(entry.getValue(), i + 1);
        }
        return Collections.unmodifiableMap(map);
    }

    public synchronized int getGroupElementCount(String group){
        int count = 0;
        for(Map.Entry<String, String> entry: groups.entrySet()){
            if(group.equals(entry.getValue())){
                count++;
            }
        }
        return count;
    }

    public synchronized String[] getGroupElements(String group){
        List<String> elements = new ArrayList<String>();
        for(Map.Entry<String, String> entry: groups.entrySet()){
            if(group.equals(entry.getValue())){
                elements.add(entry.getKey());
            }
        }
        return elements.toArray(new String[elements.size()]);
    }
}
