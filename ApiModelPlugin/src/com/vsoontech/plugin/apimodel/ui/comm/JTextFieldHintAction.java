package com.vsoontech.plugin.apimodel.ui.comm;

import com.vsoontech.plugin.apimodel.Logc;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class JTextFieldHintAction {

    private String hintText;
    private JTextField jTextField;
    private Color mForeground;

    public JTextFieldHintAction(JTextField textField, String hintText) {
        this.jTextField = textField;
        this.hintText = hintText;
        if (jTextField != null) {
            mForeground = jTextField.getForeground();
            jTextField.setDocument(new HintDocument());
            jTextField.setText(hintText);  //默认直接显示
            jTextField.setCaretPosition(0);
            jTextField.setForeground(Color.GRAY);
            jTextField.addMouseListener(getMouseListener());

        }
    }

    private MouseListener getMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Logc.d("mousePressed !" + e.paramString());
                String txt = getText();
                if (isEmpty(txt)) {
                    jTextField.setCaretPosition(0);
                }
            }
        };
    }

    class HintDocument extends PlainDocument {
        boolean ignoreRemove = false;

        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            Logc.d("insertString ! " + str);
            if (hintText.equals(jTextField.getText())) {
                ignoreRemove = true;
                remove(0, hintText.length());
                jTextField.setForeground(mForeground);
            }
            super.insertString(offs, str, a);
        }

        @Override
        protected void fireRemoveUpdate(DocumentEvent e) {
            super.fireRemoveUpdate(e);
            String txt = jTextField.getText();
            Logc.d("fireRemoveUpdate ! " + txt);
            if (isEmpty(txt) && !ignoreRemove) {
                jTextField.setText(hintText);
                jTextField.setForeground(Color.GRAY);
                jTextField.setCaretPosition(0);
            }
            ignoreRemove = false;
        }
    }

    private boolean isHintText() {
        String temp = jTextField.getText();
        return temp.equals(hintText);
    }

    public String getText() {
        return jTextField.getText().replace(hintText, "").trim();
    }
}
