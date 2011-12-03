package jp.sourceforge.stigmata.ui.swing;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import jp.sourceforge.stigmata.ui.swing.actions.PopupShowAction;
import jp.sourceforge.talisman.i18n.Messages;

/**
 * 
 * @author Haruaki TAMADA
 */
public class TargetSelectionPane extends JPanel{
    private static final long serialVersionUID = 3209435745432235432L;

    private StigmataFrame stigmata;
    private FileFilter[] filters;
    private List<String> extensions = new ArrayList<String>();
    private String description;
    private DefaultListModel model = new DefaultListModel();
    private List<DataChangeListener> listeners = new ArrayList<DataChangeListener>();
    private boolean directorySelectable = false;
    private boolean multipleSelectable = true;
    private JList list;
    private Action addAction;
    private Action removeAction;

    public TargetSelectionPane(StigmataFrame stigmata){
        this.stigmata = stigmata;
        initComponents();
        list.setModel(model);

        DropTarget dropTarget = new TargetSelectionDropTarget();
        list.setDropTarget(dropTarget);
        DragSource dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(
            list, DnDConstants.ACTION_MOVE,
            new TargetDragGestureListener(list, model)
        );
    }

    public void addDataChangeListener(DataChangeListener listener){
        listeners.add(listener);
    }

    public void addTargetExtension(String ext){
        if(filters != null){
            filters = null;
        }
        extensions.add(ext);
    }

    public void addTargetExtensions(String[] exts){
        if(filters != null){
            filters = null;
        }
        for(String ext: exts){
            extensions.add(ext);
        }
    }

    public void addValue(String value){
        model.addElement(value);
        fireEvent();
    }

    public void addValues(String[] values){
        for(String value: values){
            addValue(value);
        }
    }

    public String getDescription(){
        return description;
    }

    public synchronized String[] getExtensions(){
        return extensions.toArray(new String[extensions.size()]);
    }

    public FileFilter[] getFileFilters(){
        if(filters == null){
            String[] exts = getExtensions();
            filters = new FileFilter[exts.length];
            for(int i = 0; i < exts.length; i++){
                filters[i] = new ExtensionFilter(
                    exts[i], MessageFormat.format(getSelectDescription(), exts[i])
                );
            }
        }
        FileFilter[] newFilters = new FileFilter[filters.length];
        System.arraycopy(filters, 0, newFilters, 0, filters.length);

        return newFilters;
    }

    public String getSelectDescription(){
        return description;
    }

    public String[] getSelectedValues(){
        int[] indeces = list.getSelectedIndices();
        String[] strings = new String[indeces.length];
        for(int i = 0; i < strings.length; i++){
            strings[i] = (String)model.getElementAt(indeces[i]);
        }
        return strings;
    }

    public String[] getValues(){
        String[] strings = new String[model.getSize()];
        for(int i = 0; i < strings.length; i++){
            strings[i] = (String)model.getElementAt(i);
        }
        return strings;
    }

    public boolean isDirectorySelectable(){
        return directorySelectable;
    }

    public boolean isMultipleSelectable(){
        return multipleSelectable;
    }

    public void removeAllElements(){
        model.removeAllElements();
        fireEvent();
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setDirectorySelectable(boolean flag){
        directorySelectable = flag;
    }

    @Override
    public void setEnabled(boolean flag){
        super.setEnabled(flag);
        addAction.setEnabled(flag);
        removeAction.setEnabled(false);
        list.setEnabled(flag);
    }

    public void setFileFilter(FileFilter filter){
        setFileFilters(new FileFilter[] { filter, });
    }

    public void setFileFilters(FileFilter[] filters){
        this.filters = new FileFilter[filters.length];
        System.arraycopy(filters, 0, this.filters, 0, filters.length);
    }

    public void setMultipleSelectable(boolean flag){
        multipleSelectable = flag;
    }

    public void setSelectDescription(String description){
        this.description = description;
    }

    private void addButtonActionPerformed(ActionEvent evt){
        File[] files = stigmata.openFiles(getFileFilters(), true, true);
        for(File file: files){
            addValue(file.getPath());
        }
    }

    private void fireEvent(){
        for(int i = listeners.size() - 1; i >= 0; i--){
            DataChangeListener listener = listeners.get(i);
            listener.valueChanged(model);
        }
    }

    private void initComponents(){
        final Messages messages = stigmata.getMessages();
        JScrollPane scroll = new JScrollPane();
        JComponent south = Box.createHorizontalBox();
        list = new JList();

        addAction = new AbstractAction(){
            private static final long serialVersionUID = 1406018312294328837L;

            @Override
            public void actionPerformed(ActionEvent e){
                addButtonActionPerformed(e);
            }
        };
        removeAction = new AbstractAction(){
            private static final long serialVersionUID = 7910621850068803554L;

            @Override
            public void actionPerformed(ActionEvent e){
                removeButtonActionPerformed(e);
            }
        };

        JButton addButton = GUIUtility.createButton(messages, "addpackage", addAction);
        JButton removeButton = GUIUtility.createButton(messages, "removepackage", removeAction);
        removeAction.setEnabled(false);

        final JPopupMenu popup = new JPopupMenu();
        popup.add(GUIUtility.createJMenuItem(messages, "addpackage", addAction));
        popup.add(GUIUtility.createJMenuItem(messages, "removepackage", removeAction));

        setLayout(new BorderLayout());

        scroll.setViewportView(list);
        south.add(Box.createHorizontalGlue());
        south.add(addButton);
        south.add(Box.createHorizontalGlue());
        south.add(removeButton);
        south.add(Box.createHorizontalGlue());

        list.addListSelectionListener(new ListSelectionListener(){
            @Override
            public void valueChanged(ListSelectionEvent evt){
                listValueChanged(evt);
            }
        });

        list.addMouseListener(new PopupShowAction(popup));

        add(scroll, java.awt.BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    private void listValueChanged(ListSelectionEvent evt){
        int[] indeces = list.getSelectedIndices();
        removeAction.setEnabled(isEnabled() && indeces != null);
    }

    private void removeButtonActionPerformed(ActionEvent evt){
        int[] indeces = list.getSelectedIndices();
        if(indeces != null && indeces.length >= 1){
            for(int i = indeces.length - 1; i >= 0; i--){
                model.removeElementAt(indeces[i]);
                fireEvent();
            }
        }
        removeAction.setEnabled(false);
    }

    private class DataRemoveWhenDragSuccessfulAdapter extends DragSourceAdapter{
        private DefaultListModel model;
        private int[] indeces;

        public DataRemoveWhenDragSuccessfulAdapter(DefaultListModel model,
            int[] indeces){
            this.model = model;
            this.indeces = indeces;
        }

        @Override
        public void dragDropEnd(DragSourceDropEvent dsde){
            for(int i = indeces.length - 1; i >= 0; i--){
                model.removeElementAt(indeces[i]);
            }
            fireEvent();
            super.dragDropEnd(dsde);
        }
    };

    private class TargetDragGestureListener implements DragGestureListener{
        private JList list;

        public TargetDragGestureListener(JList list, DefaultListModel model){
            this.list = list;
        }

        @Override
        public void dragGestureRecognized(DragGestureEvent dge){
            int[] indeces = list.getSelectedIndices();
            if(indeces.length > 0){
                String[] strings = new String[indeces.length];
                for(int i = 0; i < strings.length; i++){
                    strings[i] = (String)model.getElementAt(indeces[i]);
                }
                Transferable transferable = new TargetTransferable(strings);
                DragSourceListener listener = new DataRemoveWhenDragSuccessfulAdapter(
                    model, indeces);

                dge.startDrag(DragSource.DefaultMoveDrop, transferable,
                    listener);
            }
        }
    };

    private class TargetSelectionDropTarget extends DropTarget{
        private static final long serialVersionUID = 3204457621345L;

        @Override
        public void dragEnter(DropTargetDragEvent arg0){
        }

        @Override
        public void dragExit(DropTargetEvent arg0){
        }

        @Override
        public void dragOver(DropTargetDragEvent arg0){
        }

        @Override
        public void drop(DropTargetDropEvent dtde){
            dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
            Transferable trans = dtde.getTransferable();
            try{
                if(trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
                    @SuppressWarnings("rawtypes")
                    List list = (List)trans.getTransferData(DataFlavor.javaFileListFlavor);
                    List<String> errorList = checkAndAddDroppedFile(list);
                    if(errorList.size() > 0){
                        showError(errorList);
                    }
                }
            } catch(UnsupportedFlavorException e){
            } catch(IOException e){
            }
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent arg0){
        }

        @SuppressWarnings("rawtypes")
        private List<String> checkAndAddDroppedFile(List list){
            List<String> errorList = new ArrayList<String>();

            FileFilter[] filters = getFileFilters();
            for(int i = 0; i < list.size(); i++){
                File file = (File)list.get(i);
                boolean neverAddedFlag = true;
                for(FileFilter filter: filters){
                    if(filter.accept(file)){
                        addValue(file.getPath());
                        neverAddedFlag = false;
                    }
                }
                if(neverAddedFlag){
                    errorList.add(file.getName());
                }
            }
            return errorList;
        }

        private void showError(List<String> errorList){
            StringBuilder builder = new StringBuilder("<html><body>");
            builder.append(stigmata.getMessages().get("unsupportedfiletype.dialog.message"));
            builder.append("<ul>");
            for(int i = 0; i < errorList.size(); i++){
                builder.append("<li>").append(errorList.get(i)).append("</li>");
            }
            builder.append("</ul></body></html>");
            JOptionPane.showMessageDialog(
                TargetSelectionPane.this, new String(builder),
                stigmata.getMessages().get("unsupportedfiletype.dialog.title"),
                JOptionPane.ERROR_MESSAGE
            );
        }
    };

    private static class TargetTransferable implements Transferable{
        private String[] strings;

        public TargetTransferable(String[] strings){
            this.strings = strings;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,
                                                        IOException{
            List<File> list = new ArrayList<File>();
            for(int i = 0; i < strings.length; i++){
                list.add(new File(strings[i]));
            }
            return list;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors(){
            return new DataFlavor[] { DataFlavor.javaFileListFlavor, };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor){
            return flavor.equals(DataFlavor.javaFileListFlavor);
        }
    }
}
