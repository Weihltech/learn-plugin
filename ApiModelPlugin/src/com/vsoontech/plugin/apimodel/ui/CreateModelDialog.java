package com.vsoontech.plugin.apimodel.ui;

import com.intellij.psi.*;
import com.vsoontech.plugin.apimodel.AnActionHelper;
import com.vsoontech.plugin.apimodel.ApiModelGenerate;
import com.vsoontech.plugin.apimodel.Logc;
import com.vsoontech.plugin.apimodel.ui.bean.ItemModel;
import com.vsoontech.plugin.apimodel.ui.comm.JTextFieldHintAction;
import com.vsoontech.plugin.apimodel.ui.renderer.ItemModelRenderer;

import javax.swing.*;
import java.awt.event.*;
import java.util.ArrayList;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class CreateModelDialog extends JDialog {

    private JTextField modelNameTextField;
    private JList respFileList;
    private JPanel panelRoot;
    private JButton cancelBtn;
    private JButton okBtn;
    private JLabel tipLabel;
    private ItemModel[] itemModels;
    private JTextFieldHintAction mJTextFieldHintAction;

    public CreateModelDialog(ArrayList<PsiClass> respPsiClassList) {
        setUndecorated(true);
        itemModels = new ItemModel[respPsiClassList.size()];
        int index = 0;
        for (PsiClass psiClass : respPsiClassList) {
            itemModels[index++] = new ItemModel(psiClass, psiClass.getName());
        }
        init();
    }

    private void init() {
        if (itemModels != null
                && itemModels.length > 0) {
            setModal(true);
            setContentPane(panelRoot);
            mJTextFieldHintAction = new JTextFieldHintAction(modelNameTextField, "Model Class Name");
            respFileList.setCellRenderer(new ItemModelRenderer());
            itemModels[0].isSel = true;
            respFileList.setListData(itemModels);
            respFileList.clearSelection();
            respFileList.setSelectedIndex(0);
            respFileList.addMouseListener(getListMouseListener());
            getRootPane().setDefaultButton(okBtn);
            cancelBtn.addActionListener(e -> setVisible(false));
            okBtn.addActionListener(okAction());
        }

    }

    private ActionListener okAction() {
        return new ActionListener() {
            boolean actionRun;

            @Override
            public void actionPerformed(ActionEvent e) {
                String modelClassName = mJTextFieldHintAction.getText();
                Logc.d("okAction ! ");
                if (!actionRun && !hasModelClassName(modelClassName)) {
                    ArrayList<PsiClass> selItemModel = new ArrayList<>();
                    for (ItemModel itemModel : itemModels) {
                        if (itemModel.isSel) {
                            selItemModel.add(itemModel.mPsiClass);
                        }
                    }
                    PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(AnActionHelper.getProject());
                    PsiClass modelPsiClass = psiElementFactory.createClass(modelClassName);
                    Logc.d(modelPsiClass.getQualifiedName());
                    new ApiModelGenerate(modelPsiClass, () -> {
                        AnActionHelper.getPsiDirectory().add(modelPsiClass);
                        setVisible(false);
                    }).generate(selItemModel);
                    actionRun = !selItemModel.isEmpty();
                } else {
                    tipLabel.setText("Has Current ModelClassName !");
                }
            }
        };
    }

    private boolean hasModelClassName(String modelClassName) {
        if (!isEmpty(modelClassName)) {
            PsiDirectory psiDirectory = AnActionHelper.getPsiDirectory();
            PsiFile[] psiFiles = psiDirectory.getFiles();
            for (PsiFile psiFile : psiFiles) {
                if (psiFile.getName().toLowerCase().equals(modelClassName.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private MouseListener getListMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ItemModel itemModel = itemModels[respFileList.getSelectedIndex()];
                itemModel.isSel = !itemModel.isSel;
                respFileList.repaint();
                Logc.d(itemModel.toString());
            }
        };
    }

}
