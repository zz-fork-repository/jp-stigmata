package jp.naist.se.stigmata.ui.swing;

/*
 * $Id$
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import jp.naist.se.stigmata.ExtractionResultSet;
import jp.naist.se.stigmata.ExtractionTarget;
import jp.naist.se.stigmata.printer.ExtractionResultSetPrinter;
import jp.naist.se.stigmata.printer.PrinterManager;
import jp.naist.se.stigmata.spi.ResultPrinterSpi;
import jp.naist.se.stigmata.ui.swing.actions.PopupShowAction;
import jp.naist.se.stigmata.ui.swing.actions.SaveAction;
import jp.naist.se.stigmata.utils.AsciiDataWritable;

/**
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public class BirthmarkExtractionResultPane extends JPanel{
    private static final long serialVersionUID = 239084365756236543L;

    private StigmataFrame frame;
    private ExtractionResultSet extraction;

    public BirthmarkExtractionResultPane(StigmataFrame stigmataFrame, ExtractionResultSet ers){
        this.frame = stigmataFrame;
        this.extraction = ers;

        initLayouts();
    }

    private void initLayouts(){
        JComponent southPanel = Box.createHorizontalBox();
        Action saveAction = new SaveAction(frame, new AsciiDataWritable(){
            public void writeAsciiData(PrintWriter out, String format){
                ResultPrinterSpi service = PrinterManager.getInstance().getService(format);
                if(service == null){
                    service = PrinterManager.getDefaultFormatService();
                }

                ExtractionResultSetPrinter list = service.getExtractionResultSetPrinter();
                list.printResult(new PrintWriter(out), extraction);
            }
        });
        Action compareAction = new AbstractAction(){
            private static final long serialVersionUID = -1938101718384412339L;

            public void actionPerformed(ActionEvent e){
                frame.compareExtractionResult(extraction);
            }
        };
        JButton saveButton = GUIUtility.createButton("savebirthmark", saveAction);
        JButton compareButton = GUIUtility.createButton("comparebirthmark", compareAction);

        JPopupMenu popup = new JPopupMenu();
        popup.add(GUIUtility.createJMenuItem("savebirthmark", saveAction));
        popup.add(GUIUtility.createJMenuItem("comparebirthmark", compareAction));

        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(new BirthmarkTree(extraction.getBirthmarkSets(ExtractionTarget.TARGET_BOTH)));

        setLayout(new BorderLayout());
        add(popup);
        add(scroll, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(saveButton);
        southPanel.add(Box.createHorizontalGlue());
        southPanel.add(compareButton);
        southPanel.add(Box.createHorizontalGlue());

        addMouseListener(new PopupShowAction(popup));
    }
}
