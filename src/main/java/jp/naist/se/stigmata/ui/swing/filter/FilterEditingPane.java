package jp.naist.se.stigmata.ui.swing.filter;

/*
 * $Id$
 */

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.naist.se.stigmata.ComparisonPairFilter;
import jp.naist.se.stigmata.spi.ComparisonPairFilterSpi;
import jp.naist.se.stigmata.spi.BirthmarkSpi;
import jp.naist.se.stigmata.ui.swing.BirthmarkServiceListener;
import jp.naist.se.stigmata.ui.swing.Messages;
import jp.naist.se.stigmata.ui.swing.StigmataFrame;
import jp.naist.se.stigmata.ui.swing.Utility;

/**
 * 
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class FilterEditingPane extends JPanel{
    private static final long serialVersionUID = -2607954525579006086L;

    private StigmataFrame stigmata;
    private ComparisonPairFilter filter;
    private JPanel cardComponent;
    private CardLayout card;
    private JComboBox combo;
    private JButton addButton;
    private JButton updateButton;
    private JButton removeButton;
    private ComparisonPairFilterPane currentPane = null;
    
    private Map<String, ComparisonPairFilterPane> paneMap = new HashMap<String, ComparisonPairFilterPane>();
    private List<ComparisonPairFilterListener> listeners = new ArrayList<ComparisonPairFilterListener>();

    public FilterEditingPane(StigmataFrame stigmata){
        this.stigmata = stigmata;

        initLayouts();
        initData();
        updateButtonEnabled();
    }

    public void reset(){
        initData();
    }

    public void addComparisonPairFilterListener(ComparisonPairFilterListener listener){
        listeners.add(listener);
    }

    public void removeComparisonPairFilterListener(ComparisonPairFilterListener listener){
        listeners.remove(listener);
    }

    public void setFilter(ComparisonPairFilter filter){
        resetOldComponent();
        this.filter = filter;
        if(filter != null && filter.getService() != null){
            ComparisonPairFilterSpi service = filter.getService();
            String name = service.getDisplayFilterName();

            combo.setSelectedItem(name);
            card.show(cardComponent, name);
            paneMap.get(name).setFilter(filter);
        }
        updateButtonEnabled();
    }

    private void resetOldComponent(){
        if(filter != null){
            paneMap.get(filter.getService().getDisplayFilterName()).resetComponents();
        }
    }

    private void updateButtonEnabled(){
        addButton.setEnabled(currentPane != null);
        removeButton.setEnabled(filter != null);
        updateButton.setEnabled(filter != null);
    }

    private void initData(){
        cardComponent.removeAll();
        combo.removeAllItems();
        paneMap.clear();

        JPanel dummyPanel = new JPanel();
        card.addLayoutComponent(dummyPanel, "");
        cardComponent.add(dummyPanel, "");
        combo.addItem("");

        for(Iterator<ComparisonPairFilterComponentService> i = ServiceRegistry.lookupProviders(ComparisonPairFilterComponentService.class); i.hasNext();){
            ComparisonPairFilterComponentService service = i.next();
            String name = service.getDisplayFilterName();

            ComparisonPairFilterPane pane = service.createComponent(service.getComparisonPairFilterService());
            if(pane instanceof BirthmarkServiceListener){
                for(BirthmarkSpi bs: stigmata.getContext().getServices()){
                    ((BirthmarkServiceListener)pane).serviceAdded(bs);
                }
                stigmata.addBirthmarkServiceListener((BirthmarkServiceListener)pane);
            }

            pane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));
            Box b = Box.createVerticalBox();
            b.add(pane);
            b.add(Box.createVerticalGlue());

            combo.addItem(name);
            card.addLayoutComponent(b, name);
            cardComponent.add(b, name);
            paneMap.put(name, pane);
        }
    }

    private void initLayouts(){
        cardComponent = new JPanel();
        combo = new JComboBox();
        addButton = Utility.createButton("newfilter");
        removeButton = Utility.createButton("removefilter");
        updateButton = Utility.createButton("updatefilter");

        combo.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e){
                String item = (String)combo.getSelectedItem();
                card.show(cardComponent, item);
                if(paneMap.get(item) != null){
                    if(filter != null){
                        String oldType = filter.getService().getDisplayFilterName();
                        if(item.equals(oldType)){
                            paneMap.get(item).setFilter(filter);
                        }
                        else{
                            currentPane.resetComponents();
                        }
                    }
                    currentPane = paneMap.get(item);
                }
                else{
                    currentPane = null;
                }
                updateButtonEnabled();
            }
        });
        ActionListener listener = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                String command = e.getActionCommand();
                ComparisonPairFilter newfilter = currentPane.getFilter();
                if(newfilter != null){
                    for(ComparisonPairFilterListener listener: listeners){
                        if(command.equals("updatefilter")){
                            listener.filterUpdated(filter, newfilter);
                        }
                        else{
                            listener.filterAdded(newfilter);
                        }
                    }
                }
                else{
                    showErrorMessage(currentPane.getErrors());
                }
            }
        };
        addButton.addActionListener(listener);
        updateButton.addActionListener(listener);
        removeButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                for(ComparisonPairFilterListener listener: listeners){
                    listener.filterRemoved(filter);
                }                
            }
        });

        Box south = Box.createHorizontalBox();
        south.add(Box.createHorizontalGlue());
        south.add(addButton);
        south.add(Box.createHorizontalGlue());
        south.add(updateButton);
        south.add(Box.createHorizontalGlue());
        south.add(removeButton);
        south.add(Box.createHorizontalGlue());

        cardComponent.setLayout(card = new CardLayout());
        cardComponent.setMaximumSize(new Dimension(Integer.MAX_VALUE, cardComponent.getMaximumSize().height));
        Box center = Box.createVerticalBox();
        center.add(cardComponent);
        center.add(Box.createVerticalGlue());

        setLayout(new BorderLayout());
        add(combo, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void showErrorMessage(String[] messages){
        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append("<p>").append(Messages.getString("error.filter.cannotcreate")).append("</p>");
        sb.append("<ul>");
        for(int i = 0; i < messages.length; i++){
            sb.append("<li>").append(messages[i]).append("</li>");
        }
        sb.append("</ul></body></html>");

        JOptionPane.showMessageDialog(
            stigmata, new String(sb), Messages.getString("error.dialog.title"),
            JOptionPane.ERROR_MESSAGE
        );
    }
}