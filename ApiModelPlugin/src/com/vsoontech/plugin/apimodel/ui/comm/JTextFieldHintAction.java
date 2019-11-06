package com.vsoontech.plugin.apimodel.ui.comm;

import com.vsoontech.plugin.apimodel.Logc;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class JTextFieldHintAction {

    private String hintText;
    private JTextField jTextField;
    private Color mForeground;
    private CallBack mCallBack;

    public interface CallBack {
        void inputChange(String input);
    }

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
                String txt = getText();
                Logc.d("mousePressed !" + txt);
                jTextField.setCaretPosition(isEmpty(txt) ? 0 : txt.length());
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Logc.d("mouseWheelMoved !" );
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Logc.d("mouseMoved !" );
            }
        };
    }

    public void setInputCallBack(CallBack callBack) {
        this.mCallBack = callBack;
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

            onInputChange();
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

            onInputChange();
        }
    }

    private void onInputChange() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCallBack != null) {
                    mCallBack.inputChange(getText());
                }
            }
        }).start();

    }

    public boolean isHintText() {
        String temp = jTextField.getText();
        return temp.equals(hintText);
    }

    public String getText() {
        return jTextField.getText().replace(hintText, "").trim();
    }
}
