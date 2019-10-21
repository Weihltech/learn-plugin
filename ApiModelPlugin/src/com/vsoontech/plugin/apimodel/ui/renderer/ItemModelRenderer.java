package com.vsoontech.plugin.apimodel.ui.renderer;

import com.vsoontech.plugin.apimodel.ui.bean.ItemModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ItemModelRenderer extends JLabel implements ListCellRenderer {

    private ItemModel itemModel;
    private static Color mBackgroundNorColor = new Color(60, 63, 65);
    private static Color mForegroundNorColor = new Color(60, 63, 65);
    private static Color mBackgroundSelColor = new Color(85, 116, 175);
    private static Color mForegroundSelColor = new Color(242, 251, 255);

    private String getLabelText(String name, String desc) {

        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("<html><body>");

        if (!isEmpty(name)) {
            textBuilder.append(name);
        }

        if (!isEmpty(desc)) {
            textBuilder.append("<br/>  ").append("描述").append(desc);
        }

        textBuilder.append("</body></html>");

        return textBuilder.toString();
    }

    public ItemModelRenderer() {
        setText(getLabelText("ErrorResp.Java ", "UnKnow Resp.Java File !"));
        mBackgroundNorColor = getBackground();
        mForegroundNorColor = getForeground();
        setOpaque(true);
        setIconTextGap(12);
        setFont(new Font(null, Font.PLAIN, 18));
        setHorizontalAlignment(SwingConstants.LEFT);
        setVerticalAlignment(SwingConstants.CENTER);
        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        setOpaque(true);

        itemModel = (ItemModel) value;
        if (itemModel != null) {
            setText(getLabelText(itemModel.name, itemModel.desc));
        }

        setForeground(itemModel.isSel ? mForegroundSelColor : mForegroundNorColor);
        setBackground(itemModel.isSel ? mBackgroundSelColor : mBackgroundNorColor);
        return this;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(96, 101, 103));
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}
