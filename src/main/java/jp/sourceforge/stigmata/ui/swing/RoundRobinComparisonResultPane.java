package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import jp.sourceforge.stigmata.Birthmark;
import jp.sourceforge.stigmata.BirthmarkComparator;
import jp.sourceforge.stigmata.BirthmarkContext;
import jp.sourceforge.stigmata.BirthmarkEnvironment;
import jp.sourceforge.stigmata.BirthmarkSet;
import jp.sourceforge.stigmata.BirthmarkStoreException;
import jp.sourceforge.stigmata.ComparisonResultSet;
import jp.sourceforge.stigmata.ExtractionResultSet;
import jp.sourceforge.stigmata.ExtractionTarget;
import jp.sourceforge.stigmata.filter.FilteredComparisonResultSet;
import jp.sourceforge.stigmata.printer.PrinterManager;
import jp.sourceforge.stigmata.result.CertainPairComparisonResultSet;
import jp.sourceforge.stigmata.result.RoundRobinComparisonResultSet;
import jp.sourceforge.stigmata.spi.ResultPrinterService;
import jp.sourceforge.stigmata.ui.swing.actions.SaveAction;
import jp.sourceforge.stigmata.ui.swing.actions.UpdateBirthmarkCellColorAction;
import jp.sourceforge.stigmata.utils.AsciiDataWritable;
import jp.sourceforge.talisman.i18n.Messages;

/**
 * 
 * @author Haruaki TAMADA
 */
public class RoundRobinComparisonResultPane extends JPanel{
    private static final long serialVersionUID = 2134574576543623L;

    private StigmataFrame stigmata;
    private ExtractionResultSet extraction;
    private JTable table;
    private DefaultTableModel model;
    private JLabel classCount, comparisonCount, distinctionRatio;
    private JLabel average, minimum, maximum;

    public RoundRobinComparisonResultPane(StigmataFrame stigmata, ExtractionResultSet resultset){
        this.stigmata = stigmata;
        this.extraction = resultset;
        initialize();
        compare(model);
    }

    private void compare(DefaultTableModel model){
        ExtractionTarget targetX = ExtractionTarget.TARGET_X;
        ExtractionTarget targetY = ExtractionTarget.TARGET_Y;
        int countX = extraction.getBirthmarkSetSize(targetX);
        int countY = extraction.getBirthmarkSetSize(targetY);
        int comparisonCount = countX * countY;
        if(countX == 0 && countY == 0){
            targetX = ExtractionTarget.TARGET_XY;
            targetY = ExtractionTarget.TARGET_XY;
            countX = extraction.getBirthmarkSetSize(targetX);
            countY = extraction.getBirthmarkSetSize(targetY);
            comparisonCount = countX * (countY - 1) / 2;
        }
        else if(countX == 0 || countY == 0){
            targetX = ExtractionTarget.TARGET_BOTH;
            targetY = ExtractionTarget.TARGET_BOTH;
            countX = extraction.getBirthmarkSetSize(targetX);
            countY = extraction.getBirthmarkSetSize(targetY);
            comparisonCount = countX * (countY - 1) / 2;
        }

        classCount.setText(Integer.toString(countX + countY));
        this.comparisonCount.setText(Integer.toString(comparisonCount));
        int correct = 0;
        double avg = 0d;
        double max = 0d;
        double min = 100d;
        model.addColumn("");
        for(Iterator<BirthmarkSet> i = extraction.birthmarkSets(targetX); i.hasNext(); ){
            BirthmarkSet x = i.next();
            model.addColumn(x.getName());
        }
        int sizeX = extraction.getBirthmarkSetSize(targetX);
        for(Iterator<BirthmarkSet> i = extraction.birthmarkSets(targetY); i.hasNext(); ){
            Object[] rows = new Object[sizeX + 1];
            BirthmarkSet setY = i.next();
            rows[0] = setY.getName();

            int index = 0;
            for(Iterator<BirthmarkSet> j = extraction.birthmarkSets(targetX); j.hasNext(); ){
                BirthmarkSet setX = j.next();
                double similarity = compare(setX, setY, extraction.getContext());
                rows[index + 1] = new Double(similarity);

                if(Math.abs(similarity - 1) < 1E-8){
                    correct += 1;
                }
                avg += similarity;
                if(max < similarity) max = similarity;
                if(min > similarity) min = similarity;
                index++;
            }
            model.addRow(rows);
        }
        distinctionRatio.setText(
            Double.toString((double)(comparisonCount - correct) / (double)comparisonCount)
        );
        avg = avg / comparisonCount;
        average.setText(Double.toString(avg));
        minimum.setText(Double.toString(min));
        maximum.setText(Double.toString(max));
    }

    private double compare(BirthmarkSet x, BirthmarkSet y, BirthmarkContext context){
        double similarity = 0d;
        int count = 0;

        for(String type: x.getBirthmarkTypes()){
            Birthmark b1 = x.getBirthmark(type);
            Birthmark b2 = y.getBirthmark(type);
            if(b1 != null && b2 != null){
                BirthmarkComparator comparator = context.getEnvironment().getService(type).getComparator();
                double result = comparator.compare(b1, b2, context);
                if(!Double.isNaN(result)){
                    similarity += result;
                    count++;
                }
            }
        }
        return similarity / count;
    }

    private Component getMainPane(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        model = new RoundRobinComparisonResultSetTableModel();
        table = new JTable(model);
        table.setDefaultRenderer(Double.class, new CompareTableCellRenderer(extraction.getEnvironment()));
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getClickCount() == 2){
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());
                    if(col >= 1 && col < table.getColumnCount() && row >= 0
                            && row < table.getRowCount()){
                        BirthmarkSet b1 = extraction.getBirthmarkSet(ExtractionTarget.TARGET_X, col - 1);
                        BirthmarkSet b2 = extraction.getBirthmarkSet(ExtractionTarget.TARGET_Y, row);

                        stigmata.compareDetails(b1, b2, extraction.getContext());
                    }
                }
            }
        });
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setCellSelectionEnabled(true);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(table);
        scroll.setWheelScrollingEnabled(true);
        panel.add(scroll, BorderLayout.CENTER);
        JComponent south = Box.createVerticalBox();
        JPanel box1 = new JPanel(new GridLayout(1, 3));
        box1.add(classCount = new JLabel());
        box1.add(comparisonCount = new JLabel());
        box1.add(distinctionRatio = new JLabel());
        south.add(box1);
        JPanel box2 = new JPanel(new GridLayout(1, 3));
        box2.setBorder(new TitledBorder(stigmata.getMessages().get("similarity.border")));
        box2.add(average = new JLabel());
        box2.add(minimum = new JLabel());
        box2.add(maximum = new JLabel());
        south.add(box2);

        classCount.setBorder(new TitledBorder(stigmata.getMessages().get("numberofclasses.border")));
        comparisonCount.setBorder(new TitledBorder(stigmata.getMessages().get("comparisoncount.border")));
        distinctionRatio.setBorder(new TitledBorder(stigmata.getMessages().get("distinctionratio.border")));
        average.setBorder(new TitledBorder(stigmata.getMessages().get("average.border")));
        minimum.setBorder(new TitledBorder(stigmata.getMessages().get("minimum.border")));
        maximum.setBorder(new TitledBorder(stigmata.getMessages().get("maximum.border")));

        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void mdsButtonActionPerformed(ActionEvent e){
        Map<URI, BirthmarkSet> map = new HashMap<URI, BirthmarkSet>();
        for(Iterator<BirthmarkSet> i = extraction.birthmarkSets(ExtractionTarget.TARGET_X); i.hasNext(); ){
            BirthmarkSet bs = i.next();
            map.put(bs.getLocation(), bs);
        }
        for(Iterator<BirthmarkSet> i = extraction.birthmarkSets(ExtractionTarget.TARGET_Y); i.hasNext(); ){
            BirthmarkSet bs = i.next();
            map.put(bs.getLocation(), bs);
        }
        int index = 0;
        BirthmarkSet[] set = new BirthmarkSet[map.size()];
        for(Map.Entry<URI, BirthmarkSet> entry: map.entrySet()){
            set[index] = entry.getValue();
            index++;
        }
        
        stigmata.showMdsGraph(set, extraction.getContext());
    }

    private void graphButtonActionPerformed(ActionEvent e){
        Map<Integer, Integer> values = new HashMap<Integer, Integer>();
        for(int i = 0; i < table.getRowCount(); i++){
            for(int j = 1; j < table.getColumnCount(); j++){
                Double d = (Double)table.getValueAt(i, j);
                int similarity = (int)Math.round(d.doubleValue() * 100);
                Integer dist = values.get(similarity);
                if(dist == null){
                    dist = 0;
                }
                dist = dist.intValue() + 1;
                values.put(similarity, dist);
            }
        }
        stigmata.showSimilarityDistributionGraph(values);
    }

    private void initialize(){
        final Messages messages = stigmata.getMessages();
        JButton save = GUIUtility.createButton(
            messages, "savecomparison", new SaveAction(stigmata, new AsciiDataWritable(){
                @Override
                public void writeAsciiData(PrintWriter out, String format){
                    ResultPrinterService service = PrinterManager.getInstance().getService(format);
                    if(service == null){
                        service = PrinterManager.getDefaultFormatService();
                    }

                    service.getComparisonResultSetPrinter().printResult(
                        out, new RoundRobinComparisonResultSet(extraction)
                    );
                }
            }
        ));
        JButton graph = GUIUtility.createButton(messages, "showgraph");
        JButton obfuscate = GUIUtility.createButton(messages, "obfuscate");
        JButton compare = GUIUtility.createButton(messages, "guessedpair");
        JButton updateColor = GUIUtility.createButton(
            messages, "updatecellcolor",
            new UpdateBirthmarkCellColorAction(stigmata, extraction.getEnvironment())
        );
        JMenuItem mdsMenu = GUIUtility.createJMenuItem(messages, "mdsmap");

        PopupButton comparePopup = new PopupButton(compare);
        PopupButton graphPopup = new PopupButton(graph);

        JComponent southPanel = Box.createHorizontalBox();

        setLayout(new BorderLayout());
        add(getMainPane(), BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(save);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(updateColor);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(graphPopup);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(obfuscate);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(comparePopup);
        southPanel.add(Box.createHorizontalGlue());

        graph.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                graphButtonActionPerformed(e);
            }
        });
        mdsMenu.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                mdsButtonActionPerformed(e);
            }
        });

        obfuscate.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                obfuscateClassNames();
            }
        });

        ActionListener compareListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String item = e.getActionCommand();
                if(item.equals("guessedpair")){
                    compareGuessedPair();
                }
                else if(item.equals("specifiedpair")){
                    compareSpecifiedPair();
                }
                else if(item.equals("roundrobin.filtering")){
                    compareRoundRobinWithFiltering();
                }
            }
        };

        compare.addActionListener(compareListener);
        String[] comparisonMethods = stigmata.getMessages().getArray("comparison.methods.inroundrobinresult");
        for(int i = 1; i < comparisonMethods.length; i++){
            JMenuItem item = GUIUtility.createJMenuItem(messages, comparisonMethods[i]);
            comparePopup.addMenuItem(item);
            item.addActionListener(compareListener);
        }
        graphPopup.addMenuItem(mdsMenu);
    }

    private void compareRoundRobinWithFiltering(){
        FilterSelectionPane pane = new FilterSelectionPane(
            stigmata, extraction.getEnvironment().getFilterManager()
        );
        int returnValue = JOptionPane.showConfirmDialog(
            stigmata, pane, stigmata.getMessages().get("filterselection.dialog.title"),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if(returnValue == JOptionPane.OK_OPTION){
            String[] filterSetList = pane.getSelectedFilters();

            ComparisonResultSet rs = new RoundRobinComparisonResultSet(extraction);
            
            ComparisonResultSet filterResultSet = new FilteredComparisonResultSet(
                rs, extraction.getEnvironment().getFilterManager().getFilterSets(filterSetList)
            );
            stigmata.showComparisonResultSet(filterResultSet);
        }
    }

    private void compareGuessedPair(){
        ComparisonResultSet rs = new CertainPairComparisonResultSet(extraction);
        stigmata.showComparisonResultSet(rs);
    }

    private void compareSpecifiedPair(){
        File file = stigmata.getOpenFile(
            stigmata.getMessages().getArray("comparemapping.extension"),
            stigmata.getMessages().get("comparemapping.description")
        );
        if(file != null){
            Map<String, String> mapping = stigmata.constructMapping(file);

            ComparisonResultSet comparison = new CertainPairComparisonResultSet(extraction, mapping);
            stigmata.showComparisonResultSet(comparison);
        }
    }

    private void obfuscateClassNames(){
        ClassNameObfuscator obfuscator = new ClassNameObfuscator();

        try{
            File file = stigmata.getSaveFile(
                stigmata.getMessages().getArray("obfuscationmapping.extension"),
                stigmata.getMessages().get("obfuscationmapping.description")
            );
            if(file != null){
                BirthmarkContext context = extraction.getContext();
                BirthmarkEnvironment env = context.getEnvironment();
                ExtractionResultSet ers = env.getHistoryManager().createDefaultResultSet(context);
                try{
                    for(Iterator<BirthmarkSet> i = extraction.birthmarkSets(ExtractionTarget.TARGET_X); i.hasNext(); ){
                        BirthmarkSet bs = i.next();
                        ers.addBirthmarkSet(ExtractionTarget.TARGET_X, obfuscator.obfuscateClassName(bs));
                    }
                    for(Iterator<BirthmarkSet> i = extraction.birthmarkSets(ExtractionTarget.TARGET_Y); i.hasNext(); ){
                        BirthmarkSet bs = i.next();
                        ers.addBirthmarkSet(ExtractionTarget.TARGET_Y, obfuscator.obfuscateClassName(bs));
                    }
                } catch(BirthmarkStoreException e){
                }
                this.extraction = ers;
                obfuscator.outputNameMappings(file);
            }
        }catch(IOException e){
            JOptionPane.showMessageDialog(
                this, e.getMessage(), stigmata.getMessages().get("error.dialog.title"),
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        DefaultTableModel model = new RoundRobinComparisonResultSetTableModel();
        compare(model);
        table.setModel(model);
        this.model = model;
    }

    private static class RoundRobinComparisonResultSetTableModel extends DefaultTableModel{
        private static final long serialVersionUID = 765435324523543242L;

        @Override
        public boolean isCellEditable(int row, int col){
            return false;
        }

        @Override
        public Class<?> getColumnClass(int column){
            if(column == 0){
                return String.class;
            }
            else{
                return Double.class;
            }
        }
    }
}
