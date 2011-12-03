package jp.sourceforge.stigmata.ui.swing.actions;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import jp.sourceforge.stigmata.ui.swing.StigmataFrame;
import jp.sourceforge.stigmata.ui.swing.UnsupportedFormatException;
import jp.sourceforge.stigmata.utils.AsciiDataWritable;
import jp.sourceforge.stigmata.utils.BinaryDataWritable;

/**
 * 
 * @author Haruaki TAMADA
 */
public class SaveAction extends AbstractAction{
    private static final long serialVersionUID = 1427912047636729211L;

    private AsciiDataWritable asciiWritable;
    private BinaryDataWritable binaryWritable;
    private StigmataFrame stigmata;
    private String[] extensions;
    private String description;

    public SaveAction(StigmataFrame stigmata, AsciiDataWritable writable){
        this.stigmata = stigmata;
        this.asciiWritable = writable;
    }

    public SaveAction(StigmataFrame stigmata, BinaryDataWritable writable){
        this.stigmata = stigmata;
        this.binaryWritable = writable;
    }

    public void setExtensions(String[] extensions){
        this.extensions = new String[extensions.length];
        System.arraycopy(extensions, 0, this.extensions, 0, extensions.length);
    }

    public void setDescrpition(String description){
        this.description = description;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        if(extensions == null || description == null){
            extensions = stigmata.getMessages().getArray("store.extensions");
            description = stigmata.getMessages().get("store.description");
        }
        File file = stigmata.getSaveFile(extensions, description);
        if(file != null){
            String name = file.getName();
            String format = name.substring(name.lastIndexOf('.') + 1, name.length());

            OutputStream out = null;
            PrintWriter writer = null;
            try{
                if(asciiWritable != null){
                    writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
                    asciiWritable.writeAsciiData(writer, format);
                }
                else{
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    binaryWritable.writeBinaryData(out, format);
                }
            }catch(IOException ee){
                JOptionPane.showMessageDialog(
                    stigmata, ee.getMessage(), stigmata.getMessages().get("error.dialog.title"),
                    JOptionPane.ERROR_MESSAGE
                );
            }catch(UnsupportedFormatException ee){
                JOptionPane.showMessageDialog(
                    stigmata, ee.getMessage(), stigmata.getMessages().get("error.dialog.title"),
                    JOptionPane.ERROR_MESSAGE
                );
            }finally{
                if(out != null){
                    try{
                        out.close();
                    } catch(IOException ee){
                        throw new InternalError(ee.getMessage());
                    }
                }
                if(writer != null){
                    writer.close();
                }
            }
        }
    }
}
