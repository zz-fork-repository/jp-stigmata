package jp.sourceforge.stigmata.ui.swing.filter;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jp.sourceforge.stigmata.ComparisonPairFilter;
import jp.sourceforge.stigmata.filter.FilterTarget;
import jp.sourceforge.stigmata.filter.TargetNameComparisonPairFilter;
import jp.sourceforge.stigmata.filter.TargetNameComparisonPairFilterService;
import jp.sourceforge.stigmata.spi.ComparisonPairFilterService;
import jp.sourceforge.stigmata.ui.swing.StigmataFrame;

/**
 * @author Haruaki TAMADA
 */
public class TargetNameComparisonPairFilterComponentService extends AbstractComparisonPairFilterComponentService{

    @Override
    public ComparisonPairFilterPane createComponent(StigmataFrame frame, ComparisonPairFilterService service){
        return new Pane(frame, service);
    }

    @Override
    public String getFilterName(){
        return "name";
    }

    @Override
    public ComparisonPairFilterService getComparisonPairFilterService(){
        return new TargetNameComparisonPairFilterService();
    }

    private static class Pane extends ComparisonPairFilterPane{
        private static final long serialVersionUID = 8912037614500713027L;
        private ComparisonPairFilterService service;
        private JComboBox criterionType;
        private JTextField value;
        private JComboBox targetType;

        public Pane(StigmataFrame frame, ComparisonPairFilterService service){
            super(frame);
            this.service = service;
            initLayouts();
        }

        @Override
        public String[] getErrors(){
            List<String> errors = new ArrayList<String>();
            if(value.getText().trim().equals("")){
                errors.add(getMessages().get("error.empty.value"));
            }

            return errors.toArray(new String[errors.size()]);
        }

        @Override
        public ComparisonPairFilter getFilter(){
            try{
                TargetNameComparisonPairFilter filter = new TargetNameComparisonPairFilter(service);
                filter.setCriterion(getCriterion((String)criterionType.getSelectedItem()));
                filter.setValue(value.getText());
                filter.setTarget(getTarget((String)targetType.getSelectedItem()));

                return filter;
            } catch(Exception e){
            }
            return null;
        }

        @Override
        public void resetComponents(){
            value.setText("");
            criterionType.setSelectedIndex(0);
            targetType.setSelectedItem(getDisplayTarget(FilterTarget.BOTH_TARGETS));
        }

        @Override
        public void setFilter(ComparisonPairFilter cpf){
            TargetNameComparisonPairFilter filter = (TargetNameComparisonPairFilter)cpf;
            criterionType.setSelectedItem(getDisplayCriterion(filter.getCriterion()));
            value.setText(filter.getValue());
            targetType.setSelectedItem(getDisplayTarget(filter.getTarget()));
        }

        private void initLayouts(){
            JLabel label = new JLabel(getMessages().get("filter.name.label"));
            value = new JTextField();
            criterionType = createCriteriaBox(TargetNameComparisonPairFilter.getValidCriteria());
            targetType = createTargetBox();

            setLayout(new GridLayout(4, 1));
            add(label);
            add(targetType);
            add(criterionType);
            add(value);
        }
    };
}
