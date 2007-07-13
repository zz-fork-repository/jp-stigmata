package jp.naist.se.stigmata.ui.swing.actions;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.naist.se.stigmata.BirthmarkEnvironment;
import jp.naist.se.stigmata.ui.swing.CompareTableCellRenderer;
import jp.naist.se.stigmata.ui.swing.Messages;
import jp.naist.se.stigmata.ui.swing.Utility;

public class UpdateBirthmarkCellColorAction extends AbstractAction{
    private static final long serialVersionUID = 2390797591047570440L;

    private Component parent;
    private BirthmarkEnvironment context;
    private JColorChooser chooser;

    public UpdateBirthmarkCellColorAction(Component parent, BirthmarkEnvironment context){
        this.parent = parent;
        this.context = context;
    }

    public UpdateBirthmarkCellColorAction(Component parent){
        this(parent, BirthmarkEnvironment.getDefaultContext());
    }

    public void actionPerformed(ActionEvent e){
        JComponent c = createPanel();
        JOptionPane.showMessageDialog(
            parent, c, Messages.getString("updatecellcolor.dialog.title"),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private JComponent createPanel(){
        Box panel = Box.createVerticalBox();
        for(int i = 0; i <= 5; i++){
            Color fore = CompareTableCellRenderer.getForegroundColor(i, context);
            Color back = CompareTableCellRenderer.getBackgroundColor(i, context);

            UpdateColorPane pane = new UpdateColorPane(i, fore, back);
            panel.add(pane);
        }

        return panel;
    }

    private Color updateColor(Color c, int rank, boolean foreground){
        if(chooser == null){
            chooser = new JColorChooser();
        }
        chooser.setColor(c);
        String l = Messages.getString((foreground? "forecolor_": "backcolor_") + rank + ".label");
        int returnValue = JOptionPane.showConfirmDialog(
            parent, chooser, Messages.getString("updatecell.title", l),
            JOptionPane.INFORMATION_MESSAGE
        );
        if(returnValue == JOptionPane.OK_OPTION){
            c = chooser.getColor();
        }

        return c;
    }

    private class UpdateColorPane extends JPanel{
        private static final long serialVersionUID = 8271684478406307685L;

        private int rank;
        private JLabel label;

        public UpdateColorPane(int rank, Color fore, Color back){
            this.rank = rank;
            initLayouts();

            label.setForeground(fore);
            label.setBackground(back);
        }

        public int getRank(){
            return rank;
        }

        private void initLayouts(){
            label = new JLabel(Messages.getString("rank_" + rank + ".label"));
            label.setOpaque(true);
            JButton fore = Utility.createButton("updatecellfore");
            JButton back = Utility.createButton("updatecellback");

            ActionListener listener = new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    String command = e.getActionCommand();
                    boolean foreground = command.equals("updatecellfore");

                    Color c = label.getBackground();
                    if(foreground){
                        c = label.getForeground();
                    }
                    c = updateColor(c, getRank(), foreground);
                    if(foreground){
                        label.setForeground(c);
                        context.addProperty(
                            "forecolor_" + getRank(), String.format("%06x", c.getRGB() & 0xffffff)
                        );
                    }
                    else{
                        label.setBackground(c);
                        context.addProperty(
                            "backcolor_" + getRank(), String.format("%06x", c.getRGB() & 0xffffff)
                        );
                    }
                }
            };
            fore.addActionListener(listener);
            back.addActionListener(listener);

            setLayout(new GridLayout(1, 3));
            add(label);
            add(fore);
            add(back);
        }
    }
}
