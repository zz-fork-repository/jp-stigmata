package jp.sourceforge.stigmata.ui.swing.mds;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.BirthmarkSet;
import jp.sourceforge.stigmata.ComparisonPair;
import jp.sourceforge.stigmata.ui.swing.ClippedLRListCellRenderer;
import jp.sourceforge.stigmata.ui.swing.GUIUtility;
import jp.sourceforge.stigmata.ui.swing.PopupButton;
import jp.sourceforge.stigmata.ui.swing.StigmataFrame;
import jp.sourceforge.stigmata.ui.swing.actions.SaveAction;
import jp.sourceforge.talisman.i18n.MessageManager;
import jp.sourceforge.talisman.i18n.Messages;
import jp.sourceforge.talisman.i18n.ResourceNotFoundException;
import jp.sourceforge.talisman.mds.Item;
import jp.sourceforge.talisman.mds.MdsMethod;
import jp.sourceforge.talisman.mds.Table;
import jp.sourceforge.talisman.mds.ui.MdsGraphSetting;
import jp.sourceforge.talisman.mds.ui.mark.DrawerFactory;
import jp.sourceforge.talisman.mds.ui.swing.MdsPane;
import jp.sourceforge.talisman.mds.ui.swing.actions.AntiClockwiseRotateAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.ClearAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.ClockwiseRotateAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.ClusteringAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.HorizontalFlipAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.VerticalFlipAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.ZoomEnabler;
import jp.sourceforge.talisman.mds.ui.swing.actions.ZoomInAction;
import jp.sourceforge.talisman.mds.ui.swing.actions.ZoomOutAction;

/**
 * 
 * @author Haruaki TAMADA
 */
public class MdsViewerPane extends JPanel implements ZoomEnabler, MessageManager{
    private static final long serialVersionUID = -7256554014379112897L;
    private static final int[] ZOOM_PATTERN = { 30, 40, 50, 75, 100, 125, 150,
            200, 300, 400, };

    private StigmataFrame stigmata;
    private BirthmarkSet[] set;
    private BirthmarkContext context;
    private MdsPane mdspane;
    private MdsGraphSetting setting;
    private LabelMap labels;

    private int currentZoomPattern = 4;
    private boolean userInputtedValue = false;
    private JTextField zoomRatio;
    private ZoomInAction zoomin;
    private ZoomOutAction zoomout;

    public MdsViewerPane(StigmataFrame stigmata, BirthmarkSet[] bset, BirthmarkContext context){
        this.stigmata = stigmata;
        this.context = context;
        this.set = new BirthmarkSet[bset.length];
        System.arraycopy(bset, 0, set, 0, bset.length);

        try{
            initLayouts();
        } catch(ResourceNotFoundException e){
            e.printStackTrace();
            throw new InternalError(e.getMessage());
        }
    }

    @Override
    public void zoomIn(){
        currentZoomPattern++;
        userInputtedValue = false;
        zoom(ZOOM_PATTERN[currentZoomPattern]);
    }

    @Override
    public void zoomOut(){
        if(!userInputtedValue){
            currentZoomPattern--;
        }
        userInputtedValue = false;
        zoom(ZOOM_PATTERN[currentZoomPattern]);
    }

    @Override
    public void zoom(int ratio){
        for(int i = 0; i < ZOOM_PATTERN.length; i++){
            if(ratio <= ZOOM_PATTERN[i]){
                currentZoomPattern = i;
                break;
            }
        }
        if(userInputtedValue && ratio < ZOOM_PATTERN[0]){
            currentZoomPattern = -1;
        }
        if(userInputtedValue && ratio > ZOOM_PATTERN[ZOOM_PATTERN.length - 1]){
            currentZoomPattern = ZOOM_PATTERN.length - 1;
        }
        zoomin.setEnabled(currentZoomPattern < (ZOOM_PATTERN.length - 1));
        zoomout.setEnabled(currentZoomPattern != 0);

        zoomRatio.setText(ratio + " %");
        mdspane.setZoomRatio(ratio);
    }

    @Override
    public Messages getMessages(){
        return stigmata.getMessages();
    }

    private Table<String> initData(BirthmarkSet[] set, BirthmarkContext context){
        Table<String> table = new Table<String>();

        for(int i = 0; i < set.length; i++){
            for(int j = 0; j <= i; j++){
                ComparisonPair pair = new ComparisonPair(set[i], set[j], context);
                table.addValue(set[i].getName(), set[j].getName(), 1d - pair.calculateSimilarity());
            }
        }
        return table;
    }

    private String getGroupName(URI location){
        String url = location.toString();
        if(url.startsWith("jar:")){
            url = url.substring("jar:".length(), url.lastIndexOf('!'));
        }
        return url;
    }

    private void setGroups(){
        labels = new LabelMap();
        labels.setGroupEnabled(true);
        Item[] items = mdspane.getItems();
        Map<String, BirthmarkSet> map = new HashMap<String, BirthmarkSet>();
        for(BirthmarkSet s: set) map.put(s.getName(), s);
        
        for(Item item: items){
            BirthmarkSet s = map.get(item.getName());
            if(s != null){
                labels.addLabel(s.getName());
                labels.setGroup(s.getName(), getGroupName(s.getLocation()));
            }
            item.setGroupId(labels.getGroupIdFromElementName(item.getName()));
        }
    }

    /**
     * This method must called after
     * {@link #initData(BirthmarkSet[], BirthmarkEnvironment) <code>initData</code>}.
     * Because this method uses calculated value in initData method.
     */
    private void initLayouts() throws ResourceNotFoundException{
        Table<String> table = initData(set, context);

        final Messages messages = stigmata.getMessages();
        setting = new MdsGraphSetting();
        mdspane = new MdsPane(new MdsMethod<String>(table), setting, messages);
        setting.setShowLabels(true);
        setGroups();

        JCheckBox check = new JCheckBox(stigmata.getMessages().get("showlabel.button.label"), true);
        check.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                JCheckBox c = (JCheckBox)e.getSource();
                setting.setShowLabels(c.isSelected());
            }
        });
        Action openSelection = new OpenItemsAction(mdspane, this);

        Action clusteringAction = new ClusteringAction(mdspane);

        SaveAction exportMdsImageAction = new SaveAction(stigmata,
                new MdsImageExporter(mdspane));
        exportMdsImageAction.setExtensions(stigmata.getMessages().getArray("savemds.extensions"));
        exportMdsImageAction.setDescrpition(stigmata.getMessages().get("savemds.description"));

        SaveAction exportItemsAction = new SaveAction(stigmata, new MdsItemsLocationExporter(mdspane));
        exportItemsAction.setExtensions(stigmata.getMessages().getArray("savelocation.extensions"));
        exportItemsAction.setDescrpition(stigmata.getMessages().get("savelocation.description"));

        PopupButton saveButton = new PopupButton(GUIUtility.createButton(messages, "savemds", exportMdsImageAction));
        saveButton.addMenuItem(GUIUtility.createJMenuItem(messages, "savelocation", exportItemsAction));

        JLabel numberOfDotsLabel = new JLabel(String.valueOf(set.length));
        GUIUtility.decorateJComponent(messages, numberOfDotsLabel, "mdsgraph.count");

        zoomRatio = new JTextField("100%", 5);
        GUIUtility.decorateJComponent(messages, zoomRatio, "mdszoomratio");
        zoomRatio.setColumns(5);
        zoomRatio.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    String label = zoomRatio.getText();
                    if(label.endsWith("%")){
                        label = label.substring(0, label.lastIndexOf('%'));
                    }
                    label = label.trim();
                    int ratio = Integer.parseInt(label);
                    userInputtedValue = true;
                    zoom(ratio);
                } catch(NumberFormatException exception){
                    zoomRatio.setText(mdspane.getZoomRatio() + " %");
                }
            }
        });
        JComboBox combo = new JComboBox();
        combo.setRenderer(new ClippedLRListCellRenderer(new Dimension(100, combo.getPreferredSize().height), 50));
        DrawerFactory factory = DrawerFactory.getInstance();
        for(Map.Entry<String, Integer> entry: labels.getGroupElementCounts().entrySet()){
            int groupId = labels.getGroupId(entry.getKey());
            ClippedLRListCellRenderer.LRItem item = new ClippedLRListCellRenderer.LRItem(entry.getKey(), entry.getValue());
            item.setIcon(factory.createIcon(groupId));
            combo.addItem(item);
        }
        GUIUtility.decorateJComponent(messages, combo, "mdsgraph.group");

        JToolBar toolbar = new JToolBar();
        toolbar.add(new ClockwiseRotateAction(mdspane));
        toolbar.add(new AntiClockwiseRotateAction(mdspane));
        toolbar.add(new HorizontalFlipAction(mdspane));
        toolbar.add(new VerticalFlipAction(mdspane));
        toolbar.add(zoomin = new ZoomInAction(this, this));
        toolbar.add(zoomout = new ZoomOutAction(this, this));
        toolbar.add(new ClearAction(mdspane));

        JPanel south1 = new JPanel(new GridLayout(1, 3));
        south1.add(numberOfDotsLabel);
        south1.add(zoomRatio);
        south1.add(combo);

        Box south2 = Box.createHorizontalBox();
        south2.add(Box.createHorizontalGlue());
        south2.add(saveButton);
        south2.add(Box.createHorizontalGlue());
        south2.add(new JButton(openSelection));
        south2.add(Box.createHorizontalGlue());
        south2.add(new JButton(clusteringAction));
        south2.add(Box.createHorizontalGlue());
        south2.add(check);
        south2.add(Box.createHorizontalGlue());

        JPanel south = new JPanel(new GridLayout(2, 1));
        south.add(south1);
        south.add(south2);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
        center.addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e){
                Dimension d = e.getComponent().getSize();
                mdspane.setSize(d.width - 10, d.height - 10);
            }
        });
        setLayout(new BorderLayout());

        center.add(mdspane);
        JScrollPane scroll = new JScrollPane(center);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }
}