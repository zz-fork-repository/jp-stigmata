package jp.sourceforge.stigmata.ui.swing.filter;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jp.sourceforge.stigmata.ComparisonPairFilter;
import jp.sourceforge.stigmata.filter.BirthmarkElementCountComparisonPairFilter;
import jp.sourceforge.stigmata.filter.BirthmarkElementCountComparisonPairFilterService;
import jp.sourceforge.stigmata.filter.FilterTarget;
import jp.sourceforge.stigmata.spi.BirthmarkService;
import jp.sourceforge.stigmata.spi.ComparisonPairFilterService;
import jp.sourceforge.stigmata.ui.swing.BirthmarkServiceListCellRenderer;
import jp.sourceforge.stigmata.ui.swing.BirthmarkServiceListener;
import jp.sourceforge.stigmata.ui.swing.StigmataFrame;

/**
 * 
 * @author Haruaki TAMADA
 */
public class BirthmarkElementCountComparisonPairFilterComponentService extends AbstractComparisonPairFilterComponentService implements BirthmarkServiceListener{
    private Pane pane;

    @Override
    public ComparisonPairFilterPane createComponent(StigmataFrame frame, ComparisonPairFilterService service){
        pane = new Pane(frame, service);
        return pane;
    }

    @Override
    public String getFilterName(){
        return "elementcount";
    }

    @Override
    public void serviceAdded(BirthmarkService service){
        pane.serviceAdded(service);
    }

    @Override
    public void serviceRemoved(BirthmarkService service){
        pane.serviceRemoved(service);
    }

    @Override
    public ComparisonPairFilterService getComparisonPairFilterService(){
        return new BirthmarkElementCountComparisonPairFilterService();
    }

    private static class Pane extends ComparisonPairFilterPane implements BirthmarkServiceListener{
        private static final long serialVersionUID = -6398073942592186671L;

        private ComparisonPairFilterService service;
        private JComboBox criterionType;
        private JTextField threshold;
        private JComboBox targetType;
        private JComboBox birthmarks;

        public Pane(StigmataFrame frame, ComparisonPairFilterService service){
            super(frame);
            this.service = service;
            initLayouts();
        }

        @Override
        public void serviceAdded(BirthmarkService service){
            birthmarks.addItem(service);
        }

        @Override
        public void serviceRemoved(BirthmarkService service){
            birthmarks.removeItem(service);
        }

        @Override
        public String[] getErrors(){
            List<String> errors = new ArrayList<String>();
            if(threshold.getText().trim().equals("")){
                errors.add(getMessages().get("error.empty.threshold"));
            }
            try{
                int v = Integer.parseInt(threshold.getText());
                if(v < 0){
                    errors.add(getMessages().format("error.negative.value", v));
                }
            } catch(NumberFormatException e){
                errors.add(getMessages().format("error.invalid.format.integer", threshold.getText()));
            }

            return errors.toArray(new String[errors.size()]);
        }

        @Override
        public ComparisonPairFilter getFilter(){
            try{
                BirthmarkElementCountComparisonPairFilter filter = new BirthmarkElementCountComparisonPairFilter(service);
                filter.setBirthmarkType(getBirthmarkType());
                filter.setCriterion(getCriterion((String)criterionType.getSelectedItem()));
                filter.setThreshold(Integer.parseInt(threshold.getText()));
                filter.setTarget(getTarget((String)targetType.getSelectedItem()));

                return filter;
            } catch(Exception e){
            }
            return null;
        }

        @Override
        public void resetComponents(){
            threshold.setText("");
            criterionType.setSelectedIndex(0);
            targetType.setSelectedItem(getDisplayTarget(FilterTarget.BOTH_TARGETS));
        }

        @Override
        public void setFilter(ComparisonPairFilter cpf){
            BirthmarkElementCountComparisonPairFilter filter = (BirthmarkElementCountComparisonPairFilter)cpf;
            criterionType.setSelectedItem(getDisplayCriterion(filter.getCriterion()));
            threshold.setText(String.valueOf(filter.getThreshold()));
            targetType.setSelectedItem(getDisplayTarget(filter.getTarget()));
        }

        private void initLayouts(){
            JLabel label = new JLabel(getMessages().get("filter.elementcount.label"));
            threshold = new JTextField();
            criterionType = createCriteriaBox(BirthmarkElementCountComparisonPairFilter.getValidCriteria());
            birthmarks = new JComboBox();
            birthmarks.setRenderer(new BirthmarkServiceListCellRenderer(new Dimension(200, 20), 60));
            JLabel label2 = new JLabel(getMessages().get("filter.elementcount.label.next"));
            targetType = createTargetBox();

            setLayout(new GridLayout(6, 1));
            add(label);
            add(birthmarks);
            add(label2);
            add(targetType);
            add(criterionType);
            add(threshold);
        }

        private String getBirthmarkType(){
            BirthmarkService service = (BirthmarkService)birthmarks.getSelectedItem();
            if(service != null){
                return service.getType();
            }
            throw new IllegalStateException("invalid birthmarks");
        }
    }
}
