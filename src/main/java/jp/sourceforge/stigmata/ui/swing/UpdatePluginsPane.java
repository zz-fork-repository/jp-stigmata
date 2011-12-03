package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import jp.sourceforge.stigmata.Main;
import jp.sourceforge.stigmata.utils.HermesUtility;
import jp.sourceforge.talisman.hermes.HermesEvent;
import jp.sourceforge.talisman.hermes.HermesException;
import jp.sourceforge.talisman.hermes.HermesPercentageListener;
import jp.sourceforge.talisman.hermes.InvalidHermesConfigException;
import jp.sourceforge.talisman.hermes.maven.Artifact;
import jp.sourceforge.talisman.hermes.maven.License;
import jp.sourceforge.talisman.i18n.Messages;

import org.apache.commons.cli.ParseException;

public class UpdatePluginsPane extends JPanel{
    private static final long serialVersionUID = 7595296740059360819L;

    private StigmataFrame stigmata;
    private HermesUtility hermes;
    private DefaultTableModel model;
    private JTable table;

    public UpdatePluginsPane(StigmataFrame stigmata){
        this.stigmata = stigmata;
        hermes = new HermesUtility();

        initLayout();
        reload();
    }

    public void reload(){
        try{
            hermes.loadHermesContext(stigmata.getEnvironment());
        } catch(InvalidHermesConfigException e){
            GUIUtility.showErrorDialog(stigmata, stigmata.getMessages(), e);
        } catch(IOException e){
            GUIUtility.showErrorDialog(stigmata, stigmata.getMessages(), e);
        }
    }

    private void updateArtifacts() throws IOException, HermesException{
        UpdatePluginsPaneHermesListener listener = new UpdatePluginsPaneHermesListener(this, model);
        hermes.getHermes().addHermesListener(listener);
        hermes.update();
        hermes.getHermes().removeHermesListener(listener);
        hermes.updateContext(stigmata.getEnvironment());
        Messages m = stigmata.getMessages();
        int value = JOptionPane.showOptionDialog(
            stigmata, m.get("restart.stigmata.requrested"),
            m.get("message.dialog.title"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
            new String[] { m.get("restart.now"), m.get("restart.later"), }, null
        );
        if(value == JOptionPane.YES_OPTION){
            stigmata.dispose();
            try{
                new Main(new String[0]);
            } catch(ParseException e){
            }
        }
    }

    private boolean applyLicenses() throws IOException, HermesException{
        LicensePane licensePane = new LicensePane(stigmata, hermes.getUpdateTarget());
        JOptionPane.showMessageDialog(stigmata, licensePane);
        return licensePane.isApply();
    }

    private void showLists() throws IOException, HermesException{
        Artifact[] artifacts = hermes.getUpdateTarget();
        showLists(artifacts);
    }

    private void showLists(Artifact[] artifacts){
        model.setRowCount(0);
        for(Artifact artifact: artifacts){
            Artifact original = hermes.getCurrentArtifact(artifact.getGroupId(), artifact.getArtifactId());
            model.addRow(new Object[] { 
                artifact.getGroupId(), artifact.getArtifactId(),
                original.getVersion(), artifact.getVersion(), 
                artifact.getScope(), ProgressRenderer.NOT_STARTED
            });
        }
        if(artifacts.length == 0){
            JOptionPane.showMessageDialog(stigmata, stigmata.getMessages().get("availabe.artifacts.notfound"));
        }
    }

    private void initLayout(){
        Box buttonPane = Box.createHorizontalBox();

        JButton checkButton = GUIUtility.createButton(stigmata.getMessages(), "hermes.check");
        final JButton updateButton = GUIUtility.createButton(stigmata.getMessages(), "hermes.update");

        checkButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    showLists();
                    updateButton.setEnabled(model.getRowCount() > 0);
                } catch(IOException e1){
                    GUIUtility.showErrorDialog(stigmata, stigmata.getMessages(), e1);
                } catch(HermesException e1){
                    GUIUtility.showErrorDialog(stigmata, stigmata.getMessages(), e1);
                }
            }
        });
        updateButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    if(applyLicenses()){
                        updateArtifacts();
                        updateButton.setEnabled(false);
                    }
                } catch(IOException e1){
                    GUIUtility.showErrorDialog(stigmata, stigmata.getMessages(), e1);
                } catch(HermesException e1){
                    GUIUtility.showErrorDialog(stigmata, stigmata.getMessages(), e1);
                }
            }
        });
        updateButton.setEnabled(false);

        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(checkButton);
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(updateButton);
        buttonPane.add(Box.createHorizontalGlue());

        UneditableDefaultTableModel uneditableModel = new UneditableDefaultTableModel();
        uneditableModel.setColumnClass(ProgressRenderer.PROGRESS_COLUMN, Integer.class);
        uneditableModel.setColumnIdentifiers(stigmata.getMessages().getArray("hermes.artifacts.labels"));

        table = new JTable(uneditableModel);
        table.setDefaultRenderer(Integer.class, new ProgressRenderer());

        setLayout(new BorderLayout());
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.CENTER);
        add(buttonPane, BorderLayout.SOUTH);

        this.model = uneditableModel;
    }

    private static class UpdatePluginsPaneHermesListener implements HermesPercentageListener{
        private DefaultTableModel model;
        private UpdatePluginsPane pane;
        private Map<String, Map<String, Integer>> map;

        public UpdatePluginsPaneHermesListener(UpdatePluginsPane pane, DefaultTableModel model){
            this.model = model;
            this.pane = pane;
        }

        @Override
        public void downloadDone(HermesEvent e){
            Artifact a = e.getArtifact();
            model.setValueAt(ProgressRenderer.DONE, map.get(a.getGroupId()).get(a.getArtifactId()), ProgressRenderer.PROGRESS_COLUMN);
        }

        @Override
        public void fileSizeGotten(HermesEvent e){
        }

        @Override
        public void progress(HermesEvent e, double progress){
            Artifact a = e.getArtifact();
            model.setValueAt(
                (int)(progress * 100), map.get(a.getGroupId()).get(a.getArtifactId()),
                ProgressRenderer.PROGRESS_COLUMN
            );
        }

        @Override
        public void finish(HermesEvent e){
        }

        @Override
        public void targetResolved(HermesEvent e){
            Artifact[] artifacts = e.getArtifacts();
            map = buildArtifactsIndexMap(artifacts);
            pane.showLists(artifacts);
        }

        private Map<String, Map<String, Integer>> buildArtifactsIndexMap(Artifact[] artifacts){
            Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();
        
            for(int i = 0; i < artifacts.length; i++){
                String groupId = artifacts[i].getGroupId();
                Map<String, Integer> submap = map.get(groupId);
                if(submap == null){
                    submap = new HashMap<String, Integer>();
                    map.put(groupId, submap);
                }
                submap.put(artifacts[i].getArtifactId(), i);
            }
        
            return map;
        }
    }


    private static class ProgressRenderer extends DefaultTableCellRenderer{
        private static final long serialVersionUID = 3098530332351108648L;

        private static final int PROGRESS_COLUMN = 5;
        public static final int DONE = 100;
        public static final int NOT_STARTED = -1;
        public static final int CANCELED = -2;

        private JProgressBar progressBar = new JProgressBar(0, 100);

        public ProgressRenderer(){
            super();
            setOpaque(true);
            progressBar.setBorder(new EmptyBorder(1, 1, 1, 1));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object originalValue, boolean isSelected, boolean hasFocus, int row, int column){
            int value = ((Integer)originalValue).intValue();
            progressBar.setValue(value);

            return progressBar;
        }
    }

    private static class LicensePane extends JPanel{
        private static final long serialVersionUID = -1992258036940405393L;

        private StigmataFrame parent;
        private Map<License, String> map = new HashMap<License, String>();
        private Artifact[] artifacts;
        private DefaultTableModel model = new UneditableDefaultTableModel();
        private JTextArea area;
        private JRadioButton applyButton;
        private JRadioButton discardButton;
        

        public LicensePane(StigmataFrame parent, Artifact[] artifacts){
            this.parent = parent;
            this.artifacts = artifacts;

            initLayout();
        }

        public boolean isApply(){
            return applyButton.isSelected();
        }

        private void showLicense(License license){
            String licenseTerm = map.get(license);
            if(licenseTerm == null){
                try{
                    licenseTerm = loadLicenseTerm(license);
                } catch(IOException e){
                    GUIUtility.showErrorDialog(parent, parent.getMessages(), e);
                }
            }
            area.setText(licenseTerm);
        }

        private String loadLicenseTerm(License license) throws IOException{
            URL url = new URL(license.getUrl());
            StringWriter out = new StringWriter();
            PrintWriter writer = new PrintWriter(out);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
            String line;
            while((line = in.readLine()) != null){
                writer.println(line);
            }
            in.close();
            writer.close();

            String term = out.toString();

            map.put(license, term);
            return term;
        }

        private void initLayout(){
            Messages messages = parent.getMessages();
            JTable table = new JTable(model);
            final JComboBox licenseNames = new JComboBox();

            model.setColumnIdentifiers(messages.getArray("hermes.artifacts.basic.labels"));
            area = new JTextArea();

            applyButton = new JRadioButton(messages.get("apply.licenses"));
            discardButton = new JRadioButton(messages.get("discard.licenses"), true);
            ButtonGroup group = new ButtonGroup();
            group.add(applyButton);
            group.add(discardButton);

            for(Artifact artifact: artifacts){
                model.addRow(new String[] { artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), });
            }
            table.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent e){
                    int row = ((JTable)e.getSource()).rowAtPoint(e.getPoint());
                    License[] licenses = artifacts[row].getPom().getLicenses();

                    licenseNames.removeAllItems();
                    for(int i = 0; i < licenses.length; i++){
                        licenseNames.addItem(licenses[i].getName());
                    }
                    if(licenses.length > 0){
                        licenseNames.setSelectedIndex(0);
                    }
                    else{
                        licenseNames.addItem(parent.getMessages().get("no.valid.licenses"));
                    }
                }
            });
            licenseNames.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    String name = (String)((JComboBox)e.getSource()).getSelectedItem();
                    boolean missingLicenseFlag = true;
                    for(Map.Entry<License, String> entry: map.entrySet()){
                        License license = entry.getKey();
                        if(license.getName().equals(name)){
                            missingLicenseFlag = false;
                            showLicense(license);
                        }
                    }
                    if(missingLicenseFlag){
                        area.setText(parent.getMessages().get("no.valid.licenses"));
                    }
                }
            });

            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            gbc.gridwidth = 1;
            gbc.weightx = 0.5d;
            gbc.weighty = 1d;
            gbc.fill = GridBagConstraints.BOTH;
            add(new JScrollPane(table), gbc);
            gbc.gridheight = 1;
            gbc.gridx = 1;
            gbc.weightx = 0.5d;
            gbc.weighty = 0d;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(licenseNames, gbc);
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            add(new JScrollPane(area), gbc);
            gbc.gridwidth = 2;
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.weightx = 1d;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            add(applyButton, gbc);
            gbc.gridy = 3;
            add(discardButton, gbc);
        }
    }
}
