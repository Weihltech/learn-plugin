package com.vsoontech.plugin.apimodel.ui;

import com.intellij.psi.PsiClass;
import com.vsoontech.plugin.apimodel.ApiModelGenerate;
import com.vsoontech.plugin.apimodel.Logc;
import com.vsoontech.plugin.apimodel.ui.bean.ItemModel;
import com.vsoontech.plugin.apimodel.ui.renderer.ItemModelRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class AssignRespsDialog extends JDialog {
    private JPanel rootPanel;
    private JList respPsiClassListView;
    private JButton okBtn;
    private JLabel tipLabel;
    private JButton cancelBtn;

    private ItemModel[] itemModels;
    private PsiClass modelPsiClass;

    public AssignRespsDialog(PsiClass targetPsiClass, ArrayList<PsiClass> respPsiClassList) {
        setUndecorated(true);
        modelPsiClass = targetPsiClass;
        if (modelPsiClass != null) {
            setModal(true);
            setContentPane(rootPanel);
            tipLabel.setForeground(Color.RED);
            tipLabel.setText("Target: " + modelPsiClass.getName());
            itemModels = new ItemModel[respPsiClassList.size()];
            int index = 0;
            for (PsiClass psiClass : respPsiClassList) {
                itemModels[index++] = new ItemModel(psiClass, psiClass.getName());
            }

            if (itemModels.length > 0) {
                initListView();
                okBtn.addActionListener(okAction());
                cancelBtn.addActionListener(e -> setVisible(false));
            }
        }
    }

    private ActionListener okAction() {
        return new ActionListener() {
            boolean actionRun;

            @Override
            public void actionPerformed(ActionEvent e) {
                Logc.d("okAction !");
                if (!actionRun) {
                    ArrayList<PsiClass> selItemModel = new ArrayList<>();
                    for (ItemModel itemModel : itemModels) {
                        if (itemModel.isSel) {
                            selItemModel.add(itemModel.mPsiClass);
                        }
                    }

                    new ApiModelGenerate(modelPsiClass, () -> setVisible(false))
                            .generate(selItemModel);
                    actionRun = !selItemModel.isEmpty();
                }
            }
        };
    }

    private void initListView() {
        respPsiClassListView.setCellRenderer(new ItemModelRenderer());
        itemModels[0].isSel = true;
        respPsiClassListView.setListData(itemModels);
        respPsiClassListView.clearSelection();
        respPsiClassListView.setSelectedIndex(0);
        respPsiClassListView.addMouseListener(getListMouseListener());
        getRootPane().setDefaultButton(okBtn);
    }

    private MouseListener getListMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ItemModel itemModel = itemModels[respPsiClassListView.getSelectedIndex()];
                itemModel.isSel = !itemModel.isSel;
                respPsiClassListView.repaint();
                Logc.d(itemModel.toString());
            }
        };
    }

}
