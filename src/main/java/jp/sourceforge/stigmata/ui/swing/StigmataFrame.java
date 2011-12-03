package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkElementClassNotFoundException;
import jp.sourceforge.stigmata.BirthmarkEngine;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.BirthmarkExtractionFailedException;
import jp.sourceforge.stigmata.BirthmarkSet;
import jp.sourceforge.stigmata.ComparisonPair;
import jp.sourceforge.stigmata.ComparisonResultSet;
import jp.sourceforge.stigmata.ExtractionResultSet;
import jp.sourceforge.stigmata.Main;
import jp.sourceforge.stigmata.Stigmata;
import jp.sourceforge.stigmata.StigmataCommand;
import jp.sourceforge.stigmata.command.StigmataCommandFactory;
import jp.sourceforge.stigmata.event.BirthmarkEngineAdapter;
import jp.sourceforge.stigmata.event.BirthmarkEngineEvent;
import jp.sourceforge.stigmata.event.WarningMessages;
import jp.sourceforge.stigmata.result.CertainPairComparisonResultSet;
import jp.sourceforge.stigmata.ui.swing.actions.AboutAction;
import jp.sourceforge.stigmata.ui.swing.actions.LicenseAction;
import jp.sourceforge.stigmata.ui.swing.actions.OpenSettingDirAction;
import jp.sourceforge.stigmata.ui.swing.graph.SimilarityDistributionGraphPane;
import jp.sourceforge.stigmata.ui.swing.mds.MdsViewerPane;
import jp.sourceforge.stigmata.ui.swing.tab.EditableTabbedPane;
import jp.sourceforge.stigmata.utils.Utility;
import jp.sourceforge.talisman.i18n.Messages;
import jp.sourceforge.talisman.i18n.ResourceNotFoundException;

import org.apache.commons.cli.ParseException;

/**
 *
 * @author Haruaki TAMADA
 */
public class StigmataFrame extends JFrame{
    private static final long serialVersionUID = 92345543665342134L;

    private Messages messages;
    private JTabbedPane tabPane;
    private JMenuItem closeTabMenu;
    private JMenuItem saveMenu;
    private JCheckBoxMenuItem experimentalMode;
    private Stigmata stigmata;
    private BirthmarkEnvironment environment;
    private ControlPane control;
    private FileIOManager fileio;
    private Map<String, Integer> countmap = new HashMap<String, Integer>();

    public StigmataFrame(){
        this(Stigmata.getInstance());
    }

    public StigmataFrame(Stigmata stigmata){
        this(stigmata, BirthmarkEnvironment.getDefaultEnvironment());
    }

    public StigmataFrame(Stigmata stigmata, BirthmarkEnvironment environment){
        this.stigmata = stigmata;
        this.environment = environment;
        this.fileio = new FileIOManager(this, environment);
        try{
            this.messages = new Messages("resources.messages");
        } catch(ResourceNotFoundException e){
            throw new InternalError(e.getMessage());
        }
        Image iconImage = GUIUtility.getImage(getMessages(), "stigmata.icon");
        if(iconImage != null){
            setIconImage(iconImage);
        }

        stigmata.addBirthmarkEngineListener(new BirthmarkEngineAdapter(){
            @Override
            public void operationDone(BirthmarkEngineEvent e){
                showWarnings(e.getMessage());
            }
        });

        initLayouts();
    }

    public Messages getMessages(){
        return messages;
    }

    public boolean isNeedToSaveSettings(){
        return saveMenu.isEnabled();
    }

    public void setNeedToSaveSettings(boolean flag){
        saveMenu.setEnabled(flag);
    }

    public Stigmata getStigmata(){
        return stigmata;
    }

    public BirthmarkEnvironment getEnvironment(){
        return environment;
    }

    public File getCurrentDirectory(){
        return fileio.getCurrentDirectory();
    }

    public void setCurrentDirectory(File file){
        try{
            fileio.setCurrentDirectory(file);
        } catch(IllegalArgumentException e){
            JOptionPane.showMessageDialog(
                this,
                getMessages().get("notdirectory.dialog.message", file.getName()),
                getMessages().get("notdirectory.dialog.title"),
                JOptionPane.ERROR_MESSAGE
            );
        } catch(Exception e){
            showExceptionMessage(e);
        }
    }

    public File[] openFiles(FileFilter[] filters, boolean multipleSelectable, boolean directorySelectable){
        return fileio.openFiles(filters, multipleSelectable, directorySelectable);
    }

    /**
     * Find file to open it.
     */
    public File getOpenFile(String[] exts, String desc){
        return fileio.findFile(true, exts, desc);
    }

    /**
     * Find file for storing data to it.
     * Extension of found file is correct as selected extension.
     */
    public File getSaveFile(String[] exts, String desc){
        return fileio.findFile(false, exts, desc);
    }

    public void addBirthmarkServiceListener(BirthmarkServiceListener listener){
        control.addBirthmarkServiceListener(listener);
    }

    public void removeBirthmarkServiceListener(BirthmarkServiceListener listener){
        control.removeBirthmarkServiceListener(listener);
    }

    public void compareDetails(BirthmarkSet target1, BirthmarkSet target2, BirthmarkContext context){
        PairComparisonPane detail = new PairComparisonPane(
            this, new ComparisonPair(target1, target2, context)
        );
        int compareDetail = getNextCount("compare_detail");

        GUIUtility.addNewTab(getMessages(), "comparedetail", tabPane, detail,
            new Object[] { Integer.valueOf(compareDetail), },
            new Object[] {
                Utility.array2String(target1.getBirthmarkTypes()),
                target1.getName(),
                target2.getName(),
            }, true
        );
    }

    public void compareRoundRobin(String[] targetX, String[] targetY, 
            BirthmarkContext context){
        try{
            BirthmarkEngine engine = getStigmata().createEngine(context.getEnvironment());
            ExtractionResultSet ers = engine.extract(targetX, targetY, context);

            RoundRobinComparisonResultPane compare = new RoundRobinComparisonResultPane(this, ers);
            int compareCount = getNextCount("compare");
            GUIUtility.addNewTab(
                getMessages(), "compare", tabPane, compare,
                new Object[] { Integer.valueOf(compareCount), },
                new Object[] {
                    Utility.array2String(context.getBirthmarkTypes()),
                    Utility.array2String(targetX),
                    Utility.array2String(targetY),
                }, true
            );
        } catch(Throwable e){
            showExceptionMessage(e);
        }
    }

    public void compareRoundRobinFilter(String[] targetX, String[] targetY, 
            BirthmarkContext context){
        try{
            BirthmarkEngine engine = getStigmata().createEngine(context.getEnvironment());

            ExtractionResultSet ers = engine.extract(targetX, targetY, context);
            ComparisonResultSet resultset = engine.compare(ers);
            if(context.hasFilter()){
                resultset = engine.filter(resultset);
            }
            int compareCount = getNextCount("compare");
            GUIUtility.addNewTab(
                getMessages(), "compare", tabPane, new PairComparisonResultSetPane(this, resultset),
                new Object[] { Integer.valueOf(compareCount), },
                new Object[] {
                    Utility.array2String(context.getBirthmarkTypes()),
                    Utility.array2String(targetX),
                    Utility.array2String(targetY),
                }, true
            );
        } catch(Throwable e){
            showExceptionMessage(e);
        }
    }

    public void compareGuessedPair(String[] targetX, String[] targetY, BirthmarkContext context){
        try{
            BirthmarkEngine engine = getStigmata().createEngine(context.getEnvironment());
            ExtractionResultSet extraction = engine.extract(targetX, targetY, context);
            int comparePair = getNextCount("compare_pair");

            ComparisonResultSet resultset = new CertainPairComparisonResultSet(extraction);
            GUIUtility.addNewTab(
                getMessages(), "comparepair", tabPane,
                new PairComparisonResultSetPane(this, resultset),
                new Object[] { Integer.valueOf(comparePair), },
                new Object[] {
                    Utility.array2String(context.getBirthmarkTypes()),
                    Utility.array2String(targetX),
                    Utility.array2String(targetY),
                }, true
            );
            tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
        }catch(Throwable e){
            showExceptionMessage(e);
        }
    }

    public void compareSpecifiedPair(String[] targetX, String[] targetY, BirthmarkContext context){
        File file = getOpenFile(
            getMessages().getArray("comparemapping.extension"),
            getMessages().get("comparemapping.description")
        );

        if(file != null){
            Map<String, String> mapping = constructMapping(file);

            try{
                BirthmarkEngine engine = getStigmata().createEngine(context.getEnvironment());
                context.setNameMappings(mapping);
                ComparisonResultSet crs = engine.compare(targetX, targetY, context);
                int comparePair = getNextCount("compare_pair");

                GUIUtility.addNewTab(
                    getMessages(), "comparepair", tabPane,
                    new PairComparisonResultSetPane(this, crs),
                    new Object[] { Integer.valueOf(comparePair), },
                    new Object[] {
                        Utility.array2String(context.getBirthmarkTypes()),
                        Utility.array2String(targetX),
                        Utility.array2String(targetY),
                    }, true
                );
            }catch(Exception e){
                showExceptionMessage(e);
            }
        }
    }

    public void showComparisonResultSet(ComparisonResultSet resultset){
        int comparePair = getNextCount("compare_pair");
        GUIUtility.addNewTab(
            getMessages(), "comparisonresultset", tabPane,
            new PairComparisonResultSetPane(this, resultset),
            new Object[] { Integer.valueOf(comparePair), }, null, true
        );
    }

    public void showMdsGraph(BirthmarkSet[] set, BirthmarkContext context){
        try{
            MdsViewerPane panel = new MdsViewerPane(this, set, context);
            int mappingGraphCount = getNextCount("mds_graph");
            GUIUtility.addNewTab(
                getMessages(), "mappinggraph", tabPane, panel,
                new Object[] { Integer.valueOf(mappingGraphCount), }, null, true
            );
        } catch(Exception e){
            showExceptionMessage(e);
        }
    }

    public void showSimilarityDistributionGraph(Map<Integer, Integer> distributions){
        SimilarityDistributionGraphPane graph = new SimilarityDistributionGraphPane(this, distributions);

        int similarityGraphCount = getNextCount("similarity_graph");
        GUIUtility.addNewTab(
            getMessages(), "similaritygraph", tabPane, graph,
            new Object[] { Integer.valueOf(similarityGraphCount), }, null, true
        );
    }

    public void compareExtractionResult(ExtractionResultSet ers){
        RoundRobinComparisonResultPane compare = new RoundRobinComparisonResultPane(this, ers);
        int compareCount = getNextCount("compare");
        GUIUtility.addNewTab(
            getMessages(), "compare", tabPane, compare,
            new Object[] { Integer.valueOf(compareCount), },
            new Object[] {
                Utility.array2String(ers.getBirthmarkTypes()),
                Utility.array2String(new String[0]),
                Utility.array2String(new String[0]),
            }, true
        );
    }

    public void showExtractionResult(ExtractionResultSet ers){
        int extractCount = getNextCount("extract");
        BirthmarkExtractionResultPane viewer = new BirthmarkExtractionResultPane(this, ers);
        GUIUtility.addNewTab(
            getMessages(), "extract", tabPane, viewer,
            new Object[] { Integer.valueOf(extractCount), },
            new Object[] { Utility.array2String(ers.getBirthmarkTypes()), }, true
        );
    }

    public void extract(String[] targetX, String[] targetY, BirthmarkContext context){
        try{
            BirthmarkEngine engine = getStigmata().createEngine(context.getEnvironment());
            ExtractionResultSet ers = engine.extract(targetX, targetY, context);
            showExtractionResult(ers);
        }catch(Throwable e){
            showExceptionMessage(e);
        }
    }

    /**
     * csv file to Map.
     */
    public Map<String, String> constructMapping(File file){
        Map<String, String> mapping = new HashMap<String, String>();
        BufferedReader in = null;
        try{
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            String line;
            while((line = in.readLine()) != null){
                String[] tokens = line.split(", *");
                if(tokens.length >= 2){
                    mapping.put(tokens[0], tokens[1]);
                }
            }

        }catch(Exception e){
            showExceptionMessage(e);
        }finally{
            if(in != null){
                try{
                    in.close();
                }catch(IOException e){
                }
            }
        }
        return mapping;
    }

    private void reloadSettings(String[] args){
        try{
            setVisible(false);
            dispose();
            new Main(args);
        } catch(ParseException e){
        }
    }

    private void clearSettings(){
        Utility.deleteDirectory(new File(BirthmarkEnvironment.getStigmataHome()));
        reloadSettings(new String[] { "--reset-config", "gui", });
    }

    private void initLayouts(){
        setTitle(getMessages().get("stigmata.frame.title"));
        initComponents();

        GUIUtility.addNewTab(getMessages(), "control", tabPane, control = new ControlPane(this), null, null, false);
        control.inititalize();
        tabPane.setSelectedIndex(tabPane.getTabCount() - 1);

        setNeedToSaveSettings(false);
        setSize(900, 600);
    }

    private void showWarnings(WarningMessages warnings){
        if(warnings.getWarningCount() > 0){
            StringBuilder sb = new StringBuilder("<html><body><dl>");
            for(Iterator<Exception> i = warnings.exceptions(); i.hasNext(); ){
                Exception e = i.next();
                sb.append("<dt>").append(e.getClass().getName()).append("</dt>");
                sb.append("<dd>").append(e.getMessage()).append("</dd>");
                sb.append("<dd>").append(warnings.getString(e)).append("</dd>");
            }
            sb.append("</dl></body></html>");

            JOptionPane.showMessageDialog(
                this, new String(sb), getMessages().get("warning.dialog.title"),
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void onlineUpdate(){
        UpdatePluginsPane pane = new UpdatePluginsPane(this);
        JOptionPane.showMessageDialog(this, pane);
    }

    private void installPlugin(){
        File pluginFile = getOpenFile(
            new String[] { "jar", },
            messages.get("installplugin.fileopen.description")
        );
        if(pluginFile == null){
            return;
        }

        StigmataCommand command = StigmataCommandFactory.getInstance().getCommand("install");
        String path = pluginFile.getPath();
        boolean installFlag = command.perform(getStigmata(), new String[] { path });

        if(installFlag){
            int flag = JOptionPane.showConfirmDialog(
                this, getMessages().get("reload.after.installplugin"),
                getMessages().get("reload.after.installplugin.title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
            );

            if(flag == JOptionPane.YES_OPTION){
                reloadSettings(new String[] { "gui", });
            }
            else{
                JOptionPane.showMessageDialog(
                    this, getMessages().get("reload.manually"),
                    getMessages().get("reload.manually.title"),
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
        else{
            StringBuilder sb = new StringBuilder("<html><body>");
            sb.append("<p>").append(getMessages().format("install.error", pluginFile.getPath())).append("</p>");
            sb.append("<ul>");
            for(String message: command.getMessages()){
                sb.append("<li>").append(message).append("</li>");
            }
            sb.append("</ul></body></html>");

            JOptionPane.showMessageDialog(
                this, new String(sb),
                getMessages().get("install.error.title"),
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void initComponents(){
        setDefaultUI();
        JMenuBar menubar = new JMenuBar();
        menubar.add(createFileMenu());
        menubar.add(createPluginsMenu());
        menubar.add(createHelpMenu());

        setJMenuBar(menubar);

        tabPane = new EditableTabbedPane(this);
        add(tabPane, BorderLayout.CENTER);

        tabPane.addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent arg0){
                String title = tabPane.getTitleAt(tabPane.getSelectedIndex());
                closeTabMenu.setEnabled(!title.equals(getMessages().get("control.tab.label")));
            }
        });
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                boolean closeFlag = true;
                if(isNeedToSaveSettings()){
                    int returnValue = JOptionPane.showConfirmDialog(
                        StigmataFrame.this,
                        getMessages().get("needtosave.settings.message"),
                        getMessages().get("needtosave.settings.title"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE
                    );
                    closeFlag = returnValue != JOptionPane.CANCEL_OPTION;
                    if(returnValue == JOptionPane.YES_OPTION){
                        control.saveSettings(new File(BirthmarkEnvironment.getStigmataHome(), "stigmata.xml"));
                    }
                }
                if(closeFlag){
                    setVisible(false);
                    dispose();
                }
            }
        });
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private JMenu createPluginsMenu(){
        JMenu pluginsMenu = GUIUtility.createJMenu(getMessages(), "pluginsMenu");
        JMenuItem installMenu = GUIUtility.createJMenuItem(getMessages(), "installplugin");
        JMenuItem openSettingDirMenu = GUIUtility.createJMenuItem(getMessages(), "opensettingdir", new OpenSettingDirAction(this, getMessages()));
        JMenuItem onlineUpdate = GUIUtility.createJMenuItem(getMessages(), "onlineupdate");

        pluginsMenu.add(openSettingDirMenu);
        pluginsMenu.add(installMenu);
        pluginsMenu.add(new JSeparator());
        pluginsMenu.add(onlineUpdate);

        installMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                installPlugin();
            }
        });
        onlineUpdate.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                onlineUpdate();
            }
        });

        return pluginsMenu;
    }

    private JMenu createFileMenu(){
        JMenu fileMenu = GUIUtility.createJMenu(getMessages(), "fileMenu");
        JMenuItem newFrameMenu = GUIUtility.createJMenuItem(getMessages(), "newframe");
        JMenuItem saveMenu = GUIUtility.createJMenuItem(getMessages(), "savesetting");
        JMenuItem exportMenu = GUIUtility.createJMenuItem(getMessages(), "exportsetting");
        JMenuItem clearMenu = GUIUtility.createJMenuItem(getMessages(), "clearsetting");
        JMenuItem refreshMenu = GUIUtility.createJMenuItem(getMessages(), "refreshsetting");
        JMenuItem closeTabMenu = GUIUtility.createJMenuItem(getMessages(), "closetab");
        JMenuItem closeMenu = GUIUtility.createJMenuItem(getMessages(), "closeframe");
        JMenuItem exitMenu = GUIUtility.createJMenuItem(getMessages(), "exit");
        this.closeTabMenu = closeTabMenu;
        this.saveMenu = saveMenu;
        saveMenu.setEnabled(false);

        fileMenu.add(newFrameMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(saveMenu);
        fileMenu.add(exportMenu);
        fileMenu.add(refreshMenu);
        fileMenu.add(clearMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(closeTabMenu);
        fileMenu.add(closeMenu);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitMenu);

        newFrameMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                StigmataFrame frame = new StigmataFrame(stigmata, environment);
                frame.setVisible(true);
            }
        });
        saveMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                control.saveSettings(new File(BirthmarkEnvironment.getStigmataHome(), "stigmata.xml"));
                setNeedToSaveSettings(false);
            }
        });

        exportMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                control.exportSettings();
            }
        });

        closeTabMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                closeTabMenuActionPerformed();
            }
        });

        clearMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                clearSettings();
            }
        });

        refreshMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                reloadSettings(new String[] { "gui", });
            }
        });

        closeMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                setVisible(false);
                dispose();
            }
        });

        exitMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent evt){
                System.exit(0);
            }
        });
        return fileMenu;
    }

    private JMenu createHelpMenu(){
        JMenu menu = GUIUtility.createJMenu(getMessages(), "helpmenu");
        JMenuItem about = GUIUtility.createJMenuItem(getMessages(), "about", new AboutAction(this));
        JMenuItem license = GUIUtility.createJMenuItem(getMessages(), "license", new LicenseAction(this));
        JMenuItem help = GUIUtility.createJMenuItem(getMessages(), "helpmenu");
        experimentalMode = GUIUtility.createJCheckBoxMenuItem(getMessages(), "experimentalmenu");

        menu.add(about);
        menu.add(license);
        menu.add(help);
        menu.add(new JSeparator());
        menu.add(createLookAndFeelMenu());
        menu.add(new JSeparator());
        menu.add(experimentalMode);

        experimentalMode.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                updateExperimentalModeState(((JCheckBoxMenuItem)e.getSource()).getState());
            }
        });
        help.setEnabled(false);

        return menu;
    }

    private JMenu createLookAndFeelMenu(){
        JMenu laf = GUIUtility.createJMenu(getMessages(), "lookandfeel");
        ButtonGroup bg = new ButtonGroup();
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        LookAndFeel lookfeel = UIManager.getLookAndFeel();

        ActionListener listener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    String command = e.getActionCommand();
                    UIManager.setLookAndFeel(command);
                    SwingUtilities.updateComponentTreeUI(StigmataFrame.this);
                } catch(Exception ee){
                }
            }
        };
        for(int i = 0; i < info.length; i++){
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(info[i].getName());
            item.setActionCommand(info[i].getClassName());
            item.addActionListener(listener);
            bg.add(item);
            laf.add(item);

            if(info[i].getClassName().equals(lookfeel.getClass().getName())){
                item.setState(true);
            }
        }

        return laf;
    }

    public void setExperimentalMode(boolean experimentalModeFlag){
        experimentalMode.setState(experimentalModeFlag);
    }

    private void updateExperimentalModeState(boolean status){
        control.setExperimentalMode(status);
    }

    private void showExceptionMessage(Throwable e){
        if(e instanceof BirthmarkElementClassNotFoundException){
            showClassNotFoundMessage((BirthmarkElementClassNotFoundException)e);
        }
        else if(e instanceof OutOfMemoryError){
            showOutOfMemoryError();
        }
        else{
            JTextArea area = new JTextArea(20, 60);
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            e.printStackTrace(out);
            if(e instanceof BirthmarkExtractionFailedException){
                out.println("Causes:");
                for(Throwable t: ((BirthmarkExtractionFailedException)e).getCauses()){
                    t.printStackTrace(out);
                }
            }
            out.close();
            area.setText(writer.toString());
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("<html><body><p>" + getMessages().get("error.message.contactus") + "</p></body></html>"), BorderLayout.NORTH);
            panel.add(new JScrollPane(area), BorderLayout.CENTER);

            JOptionPane.showMessageDialog(
                this, panel, getMessages().get("error.dialog.title"),
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void showOutOfMemoryError(){
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body><p>");
        sb.append(getMessages().get("error.message.outofmemory"));
        sb.append("</p></body></html>");
        JOptionPane.showMessageDialog(
            this, new String(sb), getMessages().get("error.dialog.title"),
            JOptionPane.WARNING_MESSAGE
        );
    }

    private void showClassNotFoundMessage(BirthmarkElementClassNotFoundException e){
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body><p>");
        sb.append(getMessages().get("error.message.classpath"));
        sb.append("</p><ul>");
        for(String name: e.getClassNames()){
            sb.append("<li>").append(name).append("</li>");
        }
        sb.append("</ul></body></html>");
        JOptionPane.showMessageDialog(
            this, new String(sb), getMessages().get("error.dialog.title"),
            JOptionPane.WARNING_MESSAGE
        );
    }

    private void closeTabMenuActionPerformed(){
        int index = tabPane.getSelectedIndex();
        if(index == 0){
            JOptionPane.showMessageDialog(
                this, getMessages().get("cannotclosecontroltab.dialog.message"),
                getMessages().get("cannotclosecontroltab.dialog.title"),
                JOptionPane.ERROR_MESSAGE
            );
        }
        else{
            tabPane.removeTabAt(index);
        }
    }

    private int getNextCount(String label){
        Integer i = countmap.get(label);
        if(i == null){
            i = 0;
        }
        i = i + 1;
        countmap.put(label, i);
        return i;
    }

    private void setDefaultUI(){
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e){
        }
    }
}
