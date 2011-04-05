package jp.sourceforge.stigmata.ui.swing.tab;

/*
 * $Id$
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

import javax.swing.JTextField;

/**
 * This program is copied from below URL.
 * http://terai.xrea.jp/Swing/EditTabTitle.html
 * 
 * @author Haruaki Tamada
 * @author Terai Atsuhiro
 */
class EditableTabbedPaneFocusTraversalPolicy extends FocusTraversalPolicy{
    private JTextField editor;

    public EditableTabbedPaneFocusTraversalPolicy(JTextField editor){
        this.editor = editor;
    }

    @Override
    public Component getFirstComponent(Container focusCycleRoot){
        return null;
    }

    @Override
    public Component getLastComponent(Container focusCycleRoot){
        return editor;
    }

    @Override
    public Component getComponentAfter(Container focusCycleRoot, Component cmp){
        return editor;
    }

    @Override
    public Component getComponentBefore(Container focusCycleRoot, Component cmp){
        return editor;
    }

    @Override
    public Component getDefaultComponent(Container focusCycleRoot){
        return editor;
    }
}
