package com.vsoontech.plugin.apimodel.ui;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.vsoontech.plugin.apimodel.ApiModelGenerate;
import com.vsoontech.plugin.apimodel.Logc;
import com.vsoontech.plugin.apimodel.ui.bean.ItemModel;
import com.vsoontech.plugin.apimodel.ui.comm.JTextFieldHintAction;
import com.vsoontech.plugin.apimodel.ui.renderer.ItemModelRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Vector;

public class AssignRespsDialog extends JDialog {
    private JPanel rootPanel;
    private JList respPsiClassListView;
    private JButton okBtn;
    private JLabel tipLabel;
    private JButton cancelBtn;
    private JTextField searchTextField;
    private JTextFieldHintAction mSearchHintAction;

    private ItemModel[] itemModels;
    private Vector<ItemModel> tItemModels;
    private PsiClass modelPsiClass;
    private PsiDirectory mRespPsiDirectory;

    public AssignRespsDialog(PsiClass targetPsiClass, PsiDirectory respPsiDirectory) {
        setUndecorated(true);
        modelPsiClass = targetPsiClass;
        mRespPsiDirectory = respPsiDirectory;
        ArrayList<PsiClass> respPsiClassList = collectRespPsiClass(respPsiDirectory);
        sortList(respPsiClassList);
        if (modelPsiClass != null) {
            setModal(true);
            setContentPane(rootPanel);
            mSearchHintAction = new JTextFieldHintAction(searchTextField, "Search Resp File");
            mSearchHintAction.setInputCallBack(getInputCallBack());
            tipLabel.setForeground(new Color(177, 73, 48));
            tipLabel.setText("Target: " + modelPsiClass.getName());
            itemModels = new ItemModel[respPsiClassList.size()];
            tItemModels = new Vector<>();
            int index = 0;
            for (PsiClass psiClass : respPsiClassList) {
                ItemModel itemModel = new ItemModel(psiClass, psiClass.getName());
                tItemModels.add(itemModel);
                itemModels[index++] = itemModel;
            }

            if (itemModels.length > 0) {
                initListView();
                okBtn.addActionListener(okAction());
                cancelBtn.addActionListener(e -> setVisible(false));
            }
        }
    }

    private ArrayList<PsiClass> collectRespPsiClass(PsiDirectory pkgPsiDirectory) {
        if (pkgPsiDirectory != null) {
            Logc.d(pkgPsiDirectory.getName());
            ArrayList<PsiClass> psiClassList = new ArrayList<>();
            for (PsiFile psiFile : pkgPsiDirectory.getFiles()) {
                if (psiFile.getName().endsWith("Resp.java") && psiFile instanceof PsiJavaFile) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    psiClassList.add(psiJavaFile.getClasses()[0]);
                }
            }
            return psiClassList;
        }
        return null;
    }

    private JTextFieldHintAction.CallBack getInputCallBack() {
        return new JTextFieldHintAction.CallBack() {
            @Override
            public void inputChange(String input) {
                Logc.d("inputChange === " + input);
                tItemModels.clear();
                for (ItemModel itemModel : itemModels) {
                    itemModel.lightLength = 0;
                    itemModel.isSel = false;
                    if (mSearchHintAction.isHintText()
                            || itemModel.name.toLowerCase().startsWith(input.toLowerCase())) {
                        itemModel.lightLength = input.length();
                        tItemModels.add(itemModel);
                    }
                }
                respPsiClassListView.setListData(tItemModels);
            }
        };
    }

    private void sortList(ArrayList<PsiClass> respPsiClassList) {
        Collections.sort(respPsiClassList, (o1, o2) -> Objects.requireNonNull(o1.getName())
                .compareTo(Objects.requireNonNull(o2.getName())));
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
                    actionRun = !selItemModel.isEmpty();
                    if (actionRun) {
                        new ApiModelGenerate(mRespPsiDirectory,modelPsiClass, () -> setVisible(false))
                                .generate(selItemModel);
                    } else {
                        nonSelectTip();
                    }
                }
            }
        };
    }

    private void initListView() {
        respPsiClassListView.setCellRenderer(new ItemModelRenderer());
        respPsiClassListView.setListData(tItemModels);
        respPsiClassListView.addMouseListener(getListMouseListener());
        getRootPane().setDefaultButton(okBtn);
    }

    private MouseListener getListMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                targetTip();
                ItemModel itemModel = tItemModels.get(respPsiClassListView.getSelectedIndex());
                itemModel.isSel = !itemModel.isSel;
                respPsiClassListView.repaint();
                Logc.d(itemModel.toString());
            }
        };
    }

    private void targetTip() {
        tipLabel.setText("Target: " + modelPsiClass.getName());
    }

    private void nonSelectTip() {
        tipLabel.setText("选择 Resp File !");
    }

}
