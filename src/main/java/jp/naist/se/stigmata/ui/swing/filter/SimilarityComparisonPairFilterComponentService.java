package jp.naist.se.stigmata.ui.swing.filter;

/*
 * $Id$
 */

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jp.naist.se.stigmata.ComparisonPairFilter;
import jp.naist.se.stigmata.filter.SimilarityComparisonPairFilter;
import jp.naist.se.stigmata.filter.SimilarityComparisonPairFilterService;
import jp.naist.se.stigmata.spi.ComparisonPairFilterSpi;
import jp.naist.se.stigmata.ui.swing.Messages;

/**
 * 
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class SimilarityComparisonPairFilterComponentService extends AbstractComparisonPairFilterComponentService{

    public ComparisonPairFilterPane createComponent(ComparisonPairFilterSpi service){
        return new Pane(service);
    }

    public String getFilterName(){
        return "similarity";
    }

    public ComparisonPairFilterSpi getComparisonPairFilterService(){
        return new SimilarityComparisonPairFilterService();
    }

    private static class Pane extends ComparisonPairFilterPane{
        private static final long serialVersionUID = 8912037614500713027L;
        private ComparisonPairFilterSpi service;
        private JComboBox criterionType;
        private JTextField threshold;

        public Pane(ComparisonPairFilterSpi service){
            this.service = service;
            initLayouts();
        }

        @Override
        public String[] getErrors(){
            List<String> errors = new ArrayList<String>();
            if(threshold.getText().trim().equals("")){
                errors.add(Messages.getString("error.empty.threshold"));
            }
            try{
                double v = Double.parseDouble(threshold.getText());
                if(v < 0d){
                    errors.add(Messages.getString("error.negative.value", v));
                }
                else if(v > 1.0d){
                    errors.add(Messages.getString("error.over.range", "0-1"));
                }
            } catch(NumberFormatException e){
                errors.add(Messages.getString("error.invalid.format.double", threshold.getText()));
            }

            return errors.toArray(new String[errors.size()]);
        }

        @Override
        public ComparisonPairFilter getFilter(){
            try{
                SimilarityComparisonPairFilter filter = new SimilarityComparisonPairFilter(service);
                filter.setThreshold(Double.parseDouble(threshold.getText()));
                filter.setCriterion(getCriterion((String)criterionType.getSelectedItem()));

                return filter;
            } catch(Exception e){
            }
            return null;
        }

        @Override
        public void resetComponents(){
            threshold.setText("");
            criterionType.setSelectedIndex(0);
        }

        @Override
        public void setFilter(ComparisonPairFilter filter){
            if(filter != null){
                SimilarityComparisonPairFilter sf = (SimilarityComparisonPairFilter)filter;
                criterionType.setSelectedItem(getDisplayCriterion(sf.getCriterion()));
                threshold.setText(Double.toString(sf.getThreshold()));
            }
            else{
                resetComponents();
            }
        }

        private void initLayouts(){
            JLabel label = new JLabel(Messages.getString("filter.similarity.label"));
            threshold = new JTextField();
            criterionType = createCriteriaBox(SimilarityComparisonPairFilter.CRITERIA);

            setLayout(new GridLayout(3, 1));
            add(label);
            add(criterionType);
            add(threshold);

            threshold.getDocument().addDocumentListener(new DocumentListener(){
                public void changedUpdate(DocumentEvent e){
                }

                public void insertUpdate(DocumentEvent e){
                }

                public void removeUpdate(DocumentEvent e){
                }
            });
        }
    };
}