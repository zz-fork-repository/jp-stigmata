package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.sourceforge.stigmata.ExtractionResultSet;
import jp.sourceforge.stigmata.result.history.ExtractedBirthmarkHistory;
import jp.sourceforge.stigmata.result.history.ExtractedBirthmarkServiceManager;
import jp.sourceforge.stigmata.ui.swing.actions.PopupShowAction;
import jp.sourceforge.talisman.i18n.Messages;

/**
 * Birthmark extraction history viewer.
 * 
 * @author Haruaki Tamada
 */
public class ExtractedHistoryPane extends JPanel{
    private static final long serialVersionUID = 4070750464486981964L;

    private StigmataFrame stigmata;
    private JComboBox combo;
    private JList list;
    private DefaultListModel model;
    private ExtractedBirthmarkServiceManager historyManager;
    private ExtractedBirthmarkHistory currentHistory;

    public ExtractedHistoryPane(StigmataFrame stigmata){
        this.stigmata = stigmata;

        initLayouts();
        initData();
    }

    private void updateList(){
        String historyId = (String)combo.getSelectedItem();
        currentHistory = historyManager.getHistory(historyId);
        model.clear();

        for(String id: currentHistory){
            model.addElement(id);
        }
    }

    private void initData(){
        historyManager = new ExtractedBirthmarkServiceManager(stigmata.getEnvironment());

        for(String id: historyManager.getHistoryIds()){
            combo.addItem(id);
        }
    }

    private void showAction(String id){
        ExtractionResultSet ers = currentHistory.getResultSet(id);
        stigmata.showExtractionResult(ers);
    }

    private void initLayouts(){
        final Messages messages = stigmata.getMessages();
        setLayout(new BorderLayout());

        final Action showAction = new AbstractAction(){
            private static final long serialVersionUID = 2156350514762218963L;

            @Override
            public void actionPerformed(ActionEvent e){
                showAction((String)model.get(list.getSelectedIndex()));
            }
        };
        final Action refreshAction = new AbstractAction(){
            private static final long serialVersionUID = 214765021455345371L;

            @Override
            public void actionPerformed(ActionEvent e){
                updateList();
            }
        };
        final Action deleteAction = new AbstractAction(){
            private static final long serialVersionUID = 8145188292702648924L;

            @Override
            public void actionPerformed(ActionEvent e){
                int[] indeces = list.getSelectedIndices();
                for(int i = indeces.length - 1; i >= 0; i--){
                    String id = (String)model.get(indeces[i]);
                    currentHistory.deleteResultSet(id);
                    model.remove(indeces[i]);
                }
                list.clearSelection();
            }
        };
        model = new DefaultListModel();
        list = new JList(model);
        combo = new JComboBox();
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        GUIUtility.decorateJComponent(messages, list, "historylist");
        GUIUtility.decorateJComponent(messages, combo, "historylocation");

        JButton showButton = GUIUtility.createButton(messages, "showhistory", showAction);
        JButton refreshButton = GUIUtility.createButton(messages, "refreshhistory", refreshAction);
        JButton deleteButton = GUIUtility.createButton(messages, "deletehistory", deleteAction);
        deleteAction.setEnabled(false);
        showAction.setEnabled(false);

        list.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent e){
                int[] indeces = list.getSelectedIndices();
                showAction.setEnabled(currentHistory != null && indeces.length == 1);
                deleteAction.setEnabled(currentHistory != null && indeces.length > 0);
            }
        });
        list.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                int index = list.locationToIndex(e.getPoint());
                if(index >= 0 && e.getClickCount() == 2){
                    showAction((String)model.get(index));
                }
            }
        });
        combo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                updateList();
            }
        });
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(list);

        Box south = Box.createHorizontalBox();
        south.add(Box.createHorizontalGlue());
        south.add(showButton);
        south.add(Box.createHorizontalGlue());
        south.add(refreshButton);
        south.add(Box.createHorizontalGlue());
        south.add(deleteButton);
        south.add(Box.createHorizontalGlue());

        add(combo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        JPopupMenu popup = new JPopupMenu();
        popup.add(GUIUtility.createJMenuItem(messages, "showhistory"), showAction);
        popup.add(GUIUtility.createJMenuItem(messages, "refreshhistory"), refreshAction);
        popup.add(GUIUtility.createJMenuItem(messages, "deletehistory"), deleteAction);
        list.addMouseListener(new PopupShowAction(popup));
    }
}
