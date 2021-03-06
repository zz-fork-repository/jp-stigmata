package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.spi.BirthmarkComparatorService;
import jp.sourceforge.stigmata.spi.BirthmarkExtractorService;
import jp.sourceforge.stigmata.spi.BirthmarkService;
import jp.sourceforge.stigmata.spi.ReflectedBirthmarkService;
import jp.sourceforge.talisman.i18n.Messages;

/**
 * 
 * @author Haruaki TAMADA
 */
public class BirthmarkDefinitionPane extends JPanel{
    private static final long serialVersionUID = 3932637653297802978L;

    private StigmataFrame stigmata;
    private DefaultListModel model;
    private InformationPane information;
    private JList serviceList;
    private JButton newService;
    private JButton removeService;
    private List<BirthmarkService> addedService = new ArrayList<BirthmarkService>();
    private List<BirthmarkServiceListener> listeners = new ArrayList<BirthmarkServiceListener>();

    public BirthmarkDefinitionPane(StigmataFrame stigmata){
        this.stigmata = stigmata;

        initLayouts();
        initData();

        updateView();
    }

    public void addBirthmarkServiceListener(BirthmarkServiceListener listener){
        listeners.add(listener);
    }

    public void removeBirthmarkServiceListener(BirthmarkServiceListener listener){
        listeners.remove(listener);
    }

    public void reset(){
        for(BirthmarkService service: addedService){
            model.removeElement(service);
        }
    }

    public void updateEnvironment(BirthmarkEnvironment environment){
        for(BirthmarkService service: addedService){
            if(environment.getService(service.getType()) == null){
                environment.addService(service);
            }
        }
    }

    private void initData(){
        information.initData();
        model.addElement(stigmata.getMessages().get("newservice.definition.label"));

        for(BirthmarkService service: stigmata.getEnvironment().findServices()){
            model.addElement(service);
        }
    }

    private void initLayouts(){
        Messages messages = stigmata.getMessages();
        JPanel panel = new JPanel(new BorderLayout());
        serviceList = new JList(model = new DefaultListModel());
        serviceList.setCellRenderer(new BirthmarkServiceListCellRenderer(new Dimension(250, 20), 60));
        JScrollPane scroll = new JScrollPane(serviceList);

        scroll.setBorder(new TitledBorder(messages.get("servicelist.border")));
        serviceList.setToolTipText(messages.get("servicelist.tooltip"));

        panel.add(scroll, BorderLayout.WEST);
        panel.add(information = new InformationPane(stigmata, this), BorderLayout.CENTER);

        Box buttonPanel = Box.createHorizontalBox();
        newService = GUIUtility.createButton(messages, "newservice");
        removeService = GUIUtility.createButton(messages, "removeservice");
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(newService);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(removeService);
        buttonPanel.add(Box.createHorizontalGlue());

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        removeService.setEnabled(false);

        serviceList.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e){
                listValueChanged(e);
            }
        });

        newService.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                addNewService();
            }
        });

        removeService.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                removeService();
            }
        });
    }

    private void removeService(){
        int index = serviceList.getSelectedIndex();
        if(index > 0){
            BirthmarkService service = (BirthmarkService)model.getElementAt(index);
            if(service != null && service.isUserDefined()){
                model.remove(index);
                for(BirthmarkServiceListener listener: listeners){
                    listener.serviceRemoved(service);
                }
            }
        }
        stigmata.setNeedToSaveSettings(true);
        updateView();
    }

    private void addNewService(){
        BirthmarkService service = information.createService();
        model.addElement(service);
        addedService.add(service);

        for(BirthmarkServiceListener listener: listeners){
            listener.serviceAdded(service);
        }
        stigmata.setNeedToSaveSettings(true);
        updateView();
    }

    private void listValueChanged(ListSelectionEvent e){
        int index = serviceList.getSelectedIndex();
        if(index > 0){
            BirthmarkService service = (BirthmarkService)model.getElementAt(index);
            if(service != null){
                information.setService(service);
            }
        }
        else if(index == 0){
            information.clear();
        }
        updateView();
    }

    /**
     * remove: enabled when selected index is greater than 0 and selected service is defined by user.
     * new service: enabled when selected index is less equals than 0 or selected service is defined by user and information pane is show available service.
     *
     */
    private void updateView(){
        int index = serviceList.getSelectedIndex();
        ListModel model = serviceList.getModel();
        BirthmarkService service = null;
        if(index > 0){
            service = (BirthmarkService)model.getElementAt(index); 
        }
        newService.setEnabled(
            (index <= 0 || service.isUserDefined()) && 
            information.isAvailableService() &&
            isAvailableServiceName(information.getType())
        );
        removeService.setEnabled(index > 0 && service.isUserDefined());
        information.setEnabled(index <= 0 || service.isUserDefined());
    }

    private boolean isAvailableServiceName(String name){
        for(BirthmarkService service: addedService){
            if(service.getType().equals(name)){
                return false;
            }
        }
        return true;
    }

    private static class InformationPane extends JPanel{
        private static final long serialVersionUID = 37906542932362L;

        private StigmataFrame stigmata;
        private BirthmarkDefinitionPane thisPane;
        private JTextField type;
        private JTextArea description;
        private JComboBox extractor;
        private JComboBox comparator;
        private JCheckBox experimental;
        private JCheckBox userDefined;

        public InformationPane(StigmataFrame stigmata, BirthmarkDefinitionPane thisPane){
            this.stigmata = stigmata;
            this.thisPane = thisPane;
            initLayouts();
        }

        public String getType(){
            return type.getText();
        }

        @Override
        public void setEnabled(boolean flag){
            super.setEnabled(flag);

            type.setEnabled(flag);
            description.setEnabled(flag);
            extractor.setEnabled(flag);
            comparator.setEnabled(flag);
        }

        public BirthmarkService createService(){
            BirthmarkService service = new ReflectedBirthmarkService(
                type.getText(), description.getText(),
                extractor.getSelectedItem().toString(),
                comparator.getSelectedItem().toString()
            );

            return service;
        }

        public void clear(){
            type.setText("");
            description.setText("");
            extractor.getModel().setSelectedItem(null);
            comparator.getModel().setSelectedItem(null);
            userDefined.setSelected(true);
            experimental.setSelected(true);
        }

        public boolean isAvailableService(){
            String newType = type.getText();
            Object selectedExtractor = extractor.getSelectedItem();
            String extractorClass = "";;
            if(selectedExtractor != null){
                extractorClass = selectedExtractor.toString();
            }
            Object selectedComparator = comparator.getSelectedItem();
            String comparatorClass = "";
            if(selectedComparator != null){
                comparatorClass = selectedComparator.toString();
            }
            BirthmarkEnvironment environment = stigmata.getEnvironment();

            boolean flag = newType.length() > 0
                    && extractorClass.length() > 0
                    && comparatorClass.length() > 0;

            // check inputed type is free
            flag = flag && environment.getService(newType) == null;

            // check extractor/comparator classes are available
            flag = flag
                && environment.getClasspathContext().findEntry(extractorClass) != null
                && environment.getClasspathContext().findEntry(comparatorClass) != null;

            return flag;
        }

        public void setService(BirthmarkService service){
            type.setText(service.getType());
            description.setText(service.getDescription());
            selectComboBoxItem(extractor, service.getExtractor().getClass().getName());
            selectComboBoxItem(comparator, service.getComparator().getClass().getName());
            userDefined.setSelected(service.isUserDefined());
            experimental.setSelected(service.isExperimental());

            setEnabled(service.isUserDefined());
        }

        public void initData(){
            comparator.addItem("");
            for(Iterator<BirthmarkComparatorService> i = stigmata.getEnvironment().lookupProviders(BirthmarkComparatorService.class); i.hasNext();){
                BirthmarkComparatorService service = i.next();
                comparator.addItem(service.getType());
                // TODO: 比較器の名前を引っ張る方法を考える．
            }
            extractor.addItem("");
            for(Iterator<BirthmarkExtractorService> i = stigmata.getEnvironment().lookupProviders(BirthmarkExtractorService.class); i.hasNext();){
                BirthmarkExtractorService service = i.next();
                extractor.addItem(service.getType());
                // TODO: 考える．
            }
        }

        private void selectComboBoxItem(JComboBox box, String item){
            box.getModel().setSelectedItem(item);
        }

        private void initLayouts(){
            Messages messages = stigmata.getMessages();
            type = new JTextField();
            extractor = new JComboBox();
            comparator = new JComboBox();
            experimental = new JCheckBox(messages.get("define.experimental.label"));
            userDefined = new JCheckBox(messages.get("define.userdef.label"));
            description = new JTextArea();
            JScrollPane scroll = new JScrollPane(description);
            type.setColumns(10);
            description.setColumns(40);
            description.setRows(10);

            JPanel typePane = new JPanel(new BorderLayout());
            JPanel displayTypePane = new JPanel(new BorderLayout());
            typePane.add(type, BorderLayout.CENTER);

            JPanel box1 = new JPanel(new BorderLayout());
            box1.add(typePane, BorderLayout.WEST);
            box1.add(displayTypePane, BorderLayout.CENTER);

            Box box2 = Box.createHorizontalBox();
            box2.add(Box.createHorizontalGlue());
            box2.add(experimental);
            box2.add(Box.createHorizontalGlue());
            box2.add(userDefined);
            box2.add(Box.createHorizontalGlue());

            JPanel extractorPane = new JPanel(new BorderLayout());
            extractorPane.add(extractor, BorderLayout.CENTER);
            JPanel comparatorPane = new JPanel(new BorderLayout());
            comparatorPane.add(comparator, BorderLayout.CENTER);

            Box panel = Box.createVerticalBox();
            panel.add(box1);
            panel.add(extractorPane);
            panel.add(comparatorPane);
            panel.add(box2);

            setLayout(new BorderLayout());
            add(panel, BorderLayout.NORTH);
            add(scroll, BorderLayout.CENTER);

            GUIUtility.decorateJComponent(messages, typePane, "define.type");
            GUIUtility.decorateJComponent(messages, displayTypePane, "define.displaytype");
            GUIUtility.decorateJComponent(messages, scroll, "define.description");
            GUIUtility.decorateJComponent(messages, extractorPane, "define.extractor");
            GUIUtility.decorateJComponent(messages, comparatorPane, "define.comparator");
            GUIUtility.decorateJComponent(messages, experimental, "define.experimental");
            GUIUtility.decorateJComponent(messages, userDefined, "define.userdef");

            userDefined.setEnabled(false);
            experimental.setEnabled(false);
            userDefined.setSelected(true);
            experimental.setSelected(true);

            extractor.setEditable(true);
            comparator.setEditable(true);

            DocumentListener listener = new DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e){
                    thisPane.updateView();
                }

                @Override
                public void removeUpdate(DocumentEvent e){
                    thisPane.updateView();
                }

                @Override
                public void changedUpdate(DocumentEvent e){
                    thisPane.updateView();
                }
            };

            type.getDocument().addDocumentListener(listener);
            description.getDocument().addDocumentListener(listener);
            ItemListener itemListener = new ItemListener(){
                @Override
                public void itemStateChanged(ItemEvent e){
                    thisPane.updateView();
                }
            };
            comparator.addItemListener(itemListener);
            extractor.addItemListener(itemListener);
            ActionListener actionListener = new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    thisPane.updateView();
                }
            };
            comparator.getEditor().addActionListener(actionListener);
            extractor.getEditor().addActionListener(actionListener);
        }
    }
}
