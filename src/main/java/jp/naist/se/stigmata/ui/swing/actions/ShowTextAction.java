package jp.naist.se.stigmata.ui.swing.actions;

/*
 * $Id$
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
abstract class ShowTextAction extends AbstractAction{
    private Component parent;

    public ShowTextAction(Component parent){
        this.parent = parent;
    }

    public abstract String getMessage();

    public void updatePanel(JPanel panel){
    }

    public abstract String getTitle();

    public void actionPerformed(ActionEvent e){
        String message = getMessage();

        JTextArea text = new JTextArea(message);
        text.setEditable(false);
        text.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(text);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(500, 300));

        updatePanel(panel);

        JOptionPane.showMessageDialog(
            parent, panel, getTitle(),
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    protected String loadStringFromFile(URL url){
        try{
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);
            while((line = in.readLine()) != null){
                out.println(line);
            }
            out.close();
            in.close();

            return writer.toString();
        }catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
