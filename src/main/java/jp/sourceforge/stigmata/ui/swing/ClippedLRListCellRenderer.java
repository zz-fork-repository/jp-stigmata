package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * copy from tempura memo available at
 * http://terai.xrea.jp/Swing/ClippedLRComboBox.html
 * 
 * @author Haruaki Tamada
 */
public class ClippedLRListCellRenderer extends JPanel implements ListCellRenderer{
    private static final long serialVersionUID = 32943674625674235L;

    private final JLabel left = new JLabel();
    private final JLabel right = new JLabel();

    public ClippedLRListCellRenderer(Dimension dim, int rightWidth){
        super(new BorderLayout());
        left.setOpaque(true);
        right.setOpaque(true);
        left.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        right.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        right.setPreferredSize(new Dimension(rightWidth, 0));

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        setPreferredSize(dim);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus){
        LRItem item = (LRItem)value;
        if(item != null){
            left.setText(String.valueOf(item.getLeft()));
            right.setText(String.valueOf(item.getRight()));
        }

        if(item != null && item.getIcon() != null){
            left.setIcon(item.getIcon());
        }

        setBackground(isSelected ? SystemColor.textHighlight: Color.white);
        left.setBackground(isSelected ? SystemColor.textHighlight: Color.white);
        right.setBackground(isSelected ? SystemColor.textHighlight: Color.white);
        left.setForeground(isSelected ? Color.white: Color.black);
        right.setForeground(isSelected ? Color.gray.brighter(): Color.gray);

        return this;
    }

    public static class LRItem{
        private Object left;
        private Object right;
        private Icon icon;

        public LRItem(Icon icon, Object left, Object right){
            this(left, right);
            setIcon(icon);
        }

        public LRItem(Object left, Object right){
            setLeft(left);
            setRight(right);
        }

        public Icon getIcon(){
            return icon;
        }

        public void setIcon(Icon icon){
            this.icon = icon;
        }

        public Object getLeft(){
            return left;
        }

        public void setLeft(Object left){
            this.left = left;
        }

        public Object getRight(){
            return right;
        }

        public void setRight(Object right){
            this.right = right;
        }
    }
}