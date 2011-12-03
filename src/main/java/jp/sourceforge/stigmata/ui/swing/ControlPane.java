package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.BirthmarkStoreTarget;
import jp.sourceforge.stigmata.ComparisonMethod;
import jp.sourceforge.stigmata.ExtractionUnit;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.digger.ClasspathContext;
import jp.sourceforge.stigmata.filter.ComparisonPairFilterManager;
import jp.sourceforge.stigmata.utils.ConfigFileExporter;
import jp.sourceforge.stigmata.utils.WellknownClassManager;

/**
 * 
 * @author Haruaki TAMADA
 */
public class ControlPane extends JPanel{
    private static final long serialVersionUID = 983547612346543645L;

    private StigmataFrame stigmata;
    private BirthmarkSelectablePane birthmarks;
    private TargetSelectionPane targetX;
    private TargetSelectionPane targetY;
    private ClasspathSettingsPane classpath;
    private WellknownClassesSettingsPane wellknownClassses;
    private BirthmarkDefinitionPane definition;
    private PropertyEditPane properties;
    private FilterManagementPane filters;
    private ExtractedHistoryPane history;
    private JTabbedPane controlTab;
    private JButton compareButton;
    private JButton extractButton;
    private JButton resetButton;
    private PopupButton comparePopup;
    private JComboBox unitBox;
    private Map<String, String> unitLabels = new HashMap<String, String>();
    private boolean experimentalMode = false;

    public ControlPane(StigmataFrame stigmata){
        this.stigmata = stigmata;
    }

    void inititalize(){
        definition = new BirthmarkDefinitionPane(stigmata);
        birthmarks = new BirthmarkSelectionCheckSetPane(stigmata);
        properties = new PropertyEditPane(stigmata);
        JComponent control = createControlPane();
        wellknownClassses = new WellknownClassesSettingsPane(stigmata);
        classpath = new ClasspathSettingsPane(stigmata);
        filters = new FilterManagementPane(stigmata);
        history = new ExtractedHistoryPane(stigmata);
        initComponents();

        GUIUtility.addNewTab(stigmata.getMessages(), "targets", controlTab, control);
        GUIUtility.addNewTab(stigmata.getMessages(), "wellknown", controlTab, wellknownClassses);
        GUIUtility.addNewTab(stigmata.getMessages(), "classpath", controlTab, classpath);
        GUIUtility.addNewTab(stigmata.getMessages(), "property", controlTab, properties);
        controlTab.setSelectedIndex(0);

        reset();
    }

    public void addBirthmarkServiceListener(BirthmarkServiceListener listener){
        definition.addBirthmarkServiceListener(listener);
    }

    public void removeBirthmarkServiceListener(BirthmarkServiceListener listener){
        definition.removeBirthmarkServiceListener(listener);
    }

    public void reset(){
        this.experimentalMode = false;
        definition.reset();
        birthmarks.reset();
        stigmata.setExperimentalMode(false);
        targetX.removeAllElements();
        targetY.removeAllElements();

        classpath.reset();
        wellknownClassses.reset();
        filters.reset();
        updateEnable();

        int definitionTabIndex = controlTab.indexOfTab(stigmata.getMessages().get("definition.tab.label"));
        if(definitionTabIndex >= 0){
            controlTab.removeTabAt(definitionTabIndex);
        }
        int filterTabIndex = controlTab.indexOfTab(stigmata.getMessages().get("filter.tab.label"));
        if(filterTabIndex >= 0){
            controlTab.removeTabAt(filterTabIndex);
        }
    }

    public void saveSettings(File file){
        BirthmarkEnvironment environment = generateEnvironment();
        ConfigFileExporter bce = new ConfigFileExporter(environment);
        try{
            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
            bce.export(out);
            out.close();
        } catch(IOException e){
            JOptionPane.showMessageDialog(
                stigmata,
                stigmata.getMessages().get("error.io", e.getMessage()),
                stigmata.getMessages().get("error.dialog.title"),
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void exportSettings(){
        File file = stigmata.getSaveFile(
            stigmata.getMessages().getArray("export.extensions"), 
            stigmata.getMessages().get("export.description")
        );

        if(file != null){
            if(!file.getName().endsWith(".xml")){
                file = new File(file.getParent(), file.getName() + ".xml");
            }
            saveSettings(file);
        }
    }

    public void setExperimentalMode(boolean experimentalModeFlag){
        this.experimentalMode = experimentalModeFlag;
        birthmarks.setExperimentalMode(experimentalModeFlag);
        stigmata.setExperimentalMode(experimentalModeFlag);

        if(experimentalModeFlag){
            int index = controlTab.getSelectedIndex();
            GUIUtility.addNewTab(stigmata.getMessages(), "definition", controlTab, definition);
            GUIUtility.addNewTab(stigmata.getMessages(), "filter", controlTab, filters);
            GUIUtility.addNewTab(stigmata.getMessages(), "history", controlTab, history);
            controlTab.setSelectedIndex(index);
        }
        else{
            removeTabByName(stigmata.getMessages().get("definition.tab.label"));
            removeTabByName(stigmata.getMessages().get("filter.tab.label"));
            removeTabByName(stigmata.getMessages().get("history.tab.label"));
        }
        updateEnable();
    }

    public boolean isExperimentalMode(){
        return experimentalMode;
    }

    private void removeTabByName(String tabname){
        int index = controlTab.indexOfTab(tabname);
        if(index >= 0){
            controlTab.removeTabAt(index);
        }
    }

    private JComponent createControlPane(){
        JComponent mainPane = new Box(BoxLayout.X_AXIS);
        JPanel center = new JPanel(new BorderLayout());
        targetX = new TargetSelectionPane(stigmata);
        targetY = new TargetSelectionPane(stigmata);

        birthmarks.setBorder(new TitledBorder(stigmata.getMessages().get("birthmarkspane.border")));

        targetX.addTargetExtensions(stigmata.getMessages().getArray("targets.extensions"));
        targetX.setDescription(stigmata.getMessages().get("targets.description"));
        targetX.setBorder(new TitledBorder(stigmata.getMessages().get("targetx.border")));

        targetY.addTargetExtensions(stigmata.getMessages().getArray("targets.extensions"));
        targetY.setDescription(stigmata.getMessages().get("targets.description"));
        targetY.setBorder(new TitledBorder(stigmata.getMessages().get("targety.border")));

        center.add(mainPane, BorderLayout.CENTER);
        center.add(birthmarks, BorderLayout.SOUTH);

        mainPane.add(Box.createHorizontalGlue());
        mainPane.add(targetX);
        mainPane.add(Box.createHorizontalGlue());
        mainPane.add(targetY);
        mainPane.add(Box.createHorizontalGlue());

        DataChangeListener dcl = new DataChangeListener(){
            @Override
            public void valueChanged(Object source){
                updateEnable();
            }
        };
        targetX.addDataChangeListener(dcl);
        targetY.addDataChangeListener(dcl);
        birthmarks.addDataChangeListener(dcl);

        return center;
    }

    private void updateEnable(){
        String[] valueX = targetX.getValues();
        String[] valueY = targetY.getValues();
        String[] targets = birthmarks.getSelectedServiceTypes();

        extractButton.setEnabled(
            ((valueX != null && valueX.length > 0) || (valueY != null && valueY.length > 0))
            && (targets != null && targets.length > 0)
        );

        comparePopup.setEnabled(
            (valueX != null && valueX.length > 0) &&
            (valueY != null && valueY.length > 0) &&
            (targets != null && targets.length > 0)
        );
    }

    private void extractButtonActionPerformed(ActionEvent e){
        BirthmarkContext context = generateContext();
        context.setComparisonMethod(ComparisonMethod.ROUND_ROBIN_XY);

        String[] fileX = targetX.getValues();
        String[] fileY = targetY.getValues();

        stigmata.extract(fileX, fileY, context);
    }

    private void compareRoundRobinWithFiltering(){
        BirthmarkContext context = generateContext();
        context.setComparisonMethod(ComparisonMethod.ROUND_ROBIN_XY);
        FilterSelectionPane pane = new FilterSelectionPane(
            stigmata, context.getEnvironment().getFilterManager()
        );
        int returnValue = JOptionPane.showConfirmDialog(
            stigmata, pane, stigmata.getMessages().get("filterselection.dialog.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if(returnValue == JOptionPane.OK_OPTION){
            String[] filterSetList = pane.getSelectedFilters();
            context.setFilterTypes(filterSetList);

            String[] fileX = targetX.getValues();
            String[] fileY = targetY.getValues();
            stigmata.compareRoundRobinFilter(fileX, fileY, context);
        }
    }

    private void compareRoundRobin(){
        BirthmarkContext context = generateContext();
        context.setComparisonMethod(ComparisonMethod.ROUND_ROBIN_XY);
        String[] fileX = targetX.getValues();
        String[] fileY = targetY.getValues();

        stigmata.compareRoundRobin(fileX, fileY, context);
    }

    private void compareSpecifiedPair(){
        BirthmarkContext context = generateContext();
        context.setComparisonMethod(ComparisonMethod.SPECIFIED_PAIR);
        String[] fileX = targetX.getValues();
        String[] fileY = targetY.getValues();
        stigmata.compareSpecifiedPair(fileX, fileY, context);
    }

    private void compareGuessedPair(){
        BirthmarkContext context = generateContext();
        context.setComparisonMethod(ComparisonMethod.GUESSED_PAIR);
        String[] fileX = targetX.getValues();
        String[] fileY = targetY.getValues();

        stigmata.compareGuessedPair(fileX, fileY, context);
    }

    private BirthmarkContext generateContext(){
        BirthmarkContext context = new BirthmarkContext(generateEnvironment());
        context.setBirthmarkTypes(birthmarks.getSelectedServiceTypes());
        context.setExtractionUnit(parseExtractionUnit());
        BirthmarkStoreTarget bst = BirthmarkStoreTarget.MEMORY;
        if(context.getEnvironment().getProperty("birthmark.store.target") != null){
            bst = BirthmarkStoreTarget.valueOf(context.getEnvironment().getProperty("birthmark.store.target"));
        }
        if(bst == null){
            bst = BirthmarkStoreTarget.MEMORY;
        }
        context.setStoreTarget(bst);

        return context;
    }

    private BirthmarkEnvironment generateEnvironment(){
        BirthmarkEnvironment environment = Stigmata.getInstance().createEnvironment();
        ClasspathContext bytecode = environment.getClasspathContext();
        WellknownClassManager manager = environment.getWellknownClassManager();
        ComparisonPairFilterManager filterManager = environment.getFilterManager();

        // environment.setExtractionUnit(parseExtractionUnit());
        classpath.updateClasspathContext(bytecode);
        wellknownClassses.setWellknownClasses(manager);
        filters.updateFilterManager(filterManager);
        definition.updateEnvironment(environment);
        properties.updateEnvironment(environment);

        return environment;
    }

    private ExtractionUnit parseExtractionUnit(){
        String label = (String)unitBox.getSelectedItem();
        String key = unitLabels.get(label);
        ExtractionUnit unit = ExtractionUnit.CLASS;

        if(key.equals("unit.archive")){
            unit = ExtractionUnit.ARCHIVE;
        }
        else if(key.equals("unit.package")){
            unit = ExtractionUnit.PACKAGE;
        }
        else{
            unit = ExtractionUnit.CLASS;
        }
        return unit;
    }

    private void initComponents(){
        controlTab = new JTabbedPane();
        resetButton = GUIUtility.createButton(stigmata.getMessages(), "reset");
        extractButton = GUIUtility.createButton(stigmata.getMessages(), "extract");
        compareButton = GUIUtility.createButton(stigmata.getMessages(), "roundrobin");
        comparePopup = new PopupButton(compareButton);
        unitBox = new JComboBox();

        JPanel south = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1d;
        gbc.weighty = 0d;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        south.add(resetButton, gbc);   gbc.gridx++;
        south.add(extractButton, gbc); gbc.gridx++;
        south.add(comparePopup, gbc);  gbc.gridx++;
        south.add(unitBox, gbc);

        setLayout(new BorderLayout());
        add(south, BorderLayout.SOUTH);
        add(controlTab, BorderLayout.CENTER);

        extractButton.setEnabled(false);
        comparePopup.setEnabled(false);

        resetButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                reset();
            }
        });

        extractButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                extractButtonActionPerformed(e);
            }
        });

        ActionListener compareListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String command = e.getActionCommand();

                if(command.equals("roundrobin")){
                    compareRoundRobin();
                }
                else if(command.equals("guessedpair")){
                    compareGuessedPair();
                }
                else if(command.equals("specifiedpair")){
                    compareSpecifiedPair();
                }
                else if(command.equals("roundrobin.filtering")){
                    compareRoundRobinWithFiltering();
                }
            }
        };
        compareButton.addActionListener(compareListener);

        String[] comparisonMethods = stigmata.getMessages().getArray("comparison.methods");
        for(int i = 1; i < comparisonMethods.length; i++){
            JMenuItem item = GUIUtility.createJMenuItem(stigmata.getMessages(), comparisonMethods[i]);
            comparePopup.addMenuItem(item);
            item.addActionListener(compareListener);
        }

        String[] extractionUnits = stigmata.getMessages().getArray("extraction.units");
        for(int i = 0; i < extractionUnits.length; i++){
            String label = stigmata.getMessages().get(extractionUnits[i]);
            unitLabels.put(label, extractionUnits[i]);
            unitBox.addItem(label);
        }
    }
}
