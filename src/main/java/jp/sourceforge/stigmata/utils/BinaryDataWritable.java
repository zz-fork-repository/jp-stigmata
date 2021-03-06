package jp.sourceforge.stigmata.utils;

import java.io.IOException;
import java.io.OutputStream;

import jp.sourceforge.stigmata.ui.swing.UnsupportedFormatException;

/**
 * @author Haruaki Tamada
 */
public interface BinaryDataWritable{
    public void writeBinaryData(OutputStream out, String format) throws IOException, UnsupportedFormatException;
}
