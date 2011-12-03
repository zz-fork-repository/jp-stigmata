package jp.sourceforge.stigmata.ui.swing.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jp.sourceforge.stigmata.ui.swing.GUIUtility;
import jp.sourceforge.stigmata.ui.swing.StigmataFrame;
import jp.sourceforge.stigmata.ui.swing.actions.ChangeColorAction;

/**
 * 
 * @author Haruaki TAMADA
 */
public class SimilarityDistributionGraphPane extends JPanel{
    private static final long serialVersionUID = 2314463453465L;

    private StigmataFrame stigmata;
    private Map<Integer, Integer> distributions;
    private int totalCount = 0;
    private int maxFrequency = 0;
    private JLabel iconLabel;
    private BufferedImage image;

    public SimilarityDistributionGraphPane(StigmataFrame stigmata, Map<Integer, Integer> distributions){
        this.stigmata = stigmata;

        this.distributions = distributions;
        initializeLayouts();
        initializeData();

        drawGraph(Color.RED);
    }

    public String[] getSupportedFormats(){
        String[] formats = ImageIO.getWriterFormatNames();
        Set<String> set = new HashSet<String>();
        for(String f: formats){
            if(f != null){
                set.add(f.toLowerCase());
            }
        }
        return set.toArray(new String[set.size()]);
    }

    private void drawGraph(Color color){
        Graphics2D g = image.createGraphics();
        g.setColor(getBackground());
        g.fillRect(0, 0, image.getWidth(), image.getHeight());

        int width = image.getWidth();
        int height = image.getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBorder(g, width, height);

        g.setColor(color);
        Dimension d = new Dimension(width - 20, height - 20);
        double w = (d.width / 100d);

        Integer v1 = distributions.get(0);
        if(v1 == null) v1 = 0;
        double x = 20;
        for(int i = 0; i <= 100; i++){
            Integer v2 = distributions.get(i);
            if(v2 == null) v2 = 0;

            double hh1 = v1 * ((double)height / totalCount);
            double hh2 = v2 * ((double)height / totalCount);

            g.draw(new Line2D.Double(x, d.height - hh1, x + w, d.height - hh2));
            x += w;
            v1 = v2;
        }
        iconLabel.setIcon(new ImageIcon(image));
    }

    private void drawBorder(Graphics2D g, int width, int height){
        g.setColor(Color.BLACK);
        g.draw(new Line2D.Double(0, height - 20, width, height - 20));
        g.draw(new Line2D.Double(20, 0, 20, height));

        g.setColor(Color.GRAY);
        // x axis
        double h = (height - 20) / 2d;
        g.draw(new Line2D.Double(20, h, width, h));
        h = h / 2;
        g.draw(new Line2D.Double(20, h, width, h));
        g.draw(new Line2D.Double(20, h * 3, width, h * 3));

        // y axis
        double w = (width - 20d) / 2d;
        g.draw(new Line2D.Double(w + 20, 0, w + 20, height - 20));
        w = w / 2;
        g.draw(new Line2D.Double(w + 20, 0, w + 20, height - 20));
        g.draw(new Line2D.Double(w * 3 + 20, 0, w * 3 + 20, height - 20));

        g.drawString("0", 10, height - 5);
        g.drawString("50%", (width - 20) / 2 + 10, height - 5);
        g.drawString(stigmata.getMessages().get("similarity.label"), width - 60, height - 5);
        g.drawString("50%", 0, (height - 20) / 2);
    }

    private void initializeData(){
        maxFrequency = 0;
        for(int i = 0; i <= 100; i++){
            Integer frequency = distributions.get(i);
            if(frequency == null){
                frequency = 0;
                distributions.put(i, frequency);
            }
            if(maxFrequency < frequency.intValue()){
                maxFrequency = frequency.intValue();
            }
            totalCount += frequency.intValue();
        }
    }

    private void initializeLayouts(){
        image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconLabel = new JLabel();
        Box south = Box.createHorizontalBox();
        JButton storeImageButton = GUIUtility.createButton(stigmata.getMessages(), "savegraph");
        JButton switchColorButton = new JButton(new ChangeColorAction(stigmata, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                ChangeColorAction action = (ChangeColorAction)e.getSource();
                if(action.isColorSelected()){
                    drawGraph(action.getColor());
                }
            }
        }));
        storeImageButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                storeGraphImage();
            }
        });

        setLayout(new BorderLayout());
        center.add(iconLabel = new JLabel());
        south.add(Box.createHorizontalGlue());
        south.add(switchColorButton);
        south.add(Box.createHorizontalGlue());
        south.add(storeImageButton);
        south.add(Box.createHorizontalGlue());

        add(center, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void storeGraphImage(){
        String[] exts = getSupportedFormats();
        File file = stigmata.getSaveFile(
            exts, stigmata.getMessages().get("savegraph.description")
        );
        try{
            if(file != null){
                String format = file.getName();
                format = format.substring(format.lastIndexOf('.') + 1);

                ImageIO.write(image, format, file);
            }
        } catch(IOException e){
            JOptionPane.showMessageDialog(
                this,
                stigmata.getMessages().get("error.io", e.getMessage()),
                stigmata.getMessages().get("error.dialog.title"),
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
