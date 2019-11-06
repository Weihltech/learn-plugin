package com.vsoontech.plugin.apimodel.ui;

import com.intellij.psi.*;
import com.vsoontech.plugin.apimodel.AnActionHelper;
import com.vsoontech.plugin.apimodel.ApiModelGenerate;
import com.vsoontech.plugin.apimodel.Logc;
import com.vsoontech.plugin.apimodel.ui.bean.ItemModel;
import com.vsoontech.plugin.apimodel.ui.comm.JTextFieldHintAction;
import com.vsoontech.plugin.apimodel.ui.renderer.ItemModelRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class CreateModelDialog extends JDialog {

    private JTextField modelNameTextField;
    private JList respFileList;
    private JPanel panelRoot;
    private JButton cancelBtn;
    private JButton okBtn;
    private JLabel tipLabel;
    private JTextField searchTextField;
    private ItemModel[] itemModels;
    private Vector<ItemModel> tItemModels;
    private JTextFieldHintAction mModelNameHintAction;
    private JTextFieldHintAction mSearchHintAction;
    private PsiDirectory mRespPsiDirectory;

    public CreateModelDialog(PsiDirectory respPsiDirectory) {
        setUndecorated(true);
        tipLabel.setForeground(new Color(177, 73, 48));
        mRespPsiDirectory = respPsiDirectory;
        ArrayList<PsiClass> respPsiClassList = collectRespPsiClass(mRespPsiDirectory);
        sortList(respPsiClassList);
        itemModels = new ItemModel[respPsiClassList.size()];
        tItemModels = new Vector<>();
        int index = 0;
        for (PsiClass psiClass : respPsiClassList) {
            ItemModel itemModel = new ItemModel(psiClass, psiClass.getName());
            tItemModels.add(itemModel);
            itemModels[index++] = itemModel;
        }
        init();
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

    private void sortList(ArrayList<PsiClass> respPsiClassList) {
        Collections.sort(respPsiClassList, (o1, o2) -> Objects.requireNonNull(o1.getName())
                .compareTo(Objects.requireNonNull(o2.getName())));
    }

    private void init() {
        if (itemModels != null
                && itemModels.length > 0) {
            setModal(true);
            setContentPane(panelRoot);
            mModelNameHintAction = new JTextFieldHintAction(modelNameTextField, "Input Model ClassName");
            mSearchHintAction = new JTextFieldHintAction(searchTextField, "Search Resp File");
            mSearchHintAction.setInputCallBack(getInputCallBack());
            respFileList.setCellRenderer(new ItemModelRenderer());
            itemModels[0].isSel = true;
            respFileList.setListData(tItemModels);
            respFileList.addMouseListener(getListMouseListener());
            getRootPane().setDefaultButton(okBtn);
            cancelBtn.addActionListener(e -> setVisible(false));
            okBtn.addActionListener(okAction());
        }

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
                respFileList.setListData(tItemModels);
            }
        };
    }

    private ActionListener okAction() {
        return new ActionListener() {
            boolean actionRun;

            @Override
            public void actionPerformed(ActionEvent e) {
                String modelClassName = mModelNameHintAction.getText();
                Logc.d("okAction ! ");
                if (!actionRun) {
                    if (isEmpty(modelClassName)) {
                        modelNameTextField.requestFocus();
                        tipLabel.setText("ModelClassName is Null !");
                    } else if (hasModelClassName(modelClassName)) {
                        tipLabel.setText("Has Current ModelClassName !");
                    } else {
                        StringBuilder newPsiClassDesc = new StringBuilder();
                        ArrayList<PsiClass> selItemModel = new ArrayList<>();
                        for (ItemModel itemModel : itemModels) {
                            if (itemModel.isSel) {
                                selItemModel.add(itemModel.mPsiClass);
                                newPsiClassDesc.append("* " + itemModel.desc).append("\n");
                            }
                        }
                        actionRun = !selItemModel.isEmpty();
                        if (actionRun) {
                            PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(AnActionHelper.getProject());
                            PsiClass modelPsiClass = psiElementFactory.createClassFromText(
                                    createNewPsiClassText(modelClassName, newPsiClassDesc.toString()), null);
                            modelPsiClass = modelPsiClass.getInnerClasses()[0];
                            Logc.d(modelPsiClass.getQualifiedName());
                            PsiClass finalModelPsiClass = modelPsiClass;
                            new ApiModelGenerate(mRespPsiDirectory, finalModelPsiClass, () -> {
                                AnActionHelper.getPsiDirectory().add(finalModelPsiClass);
                                setVisible(false);
                            }).generate(selItemModel);
                        } else {
                            tipLabel.setText("选择 Resp File !");
                        }

                    }
                }
            }
        };
    }

    private String createNewPsiClassText(String modelClassName, String desc) {
//        /**
//         * Automatically generated file. DO NOT MODIFY !
//         *
//         * @desc 移动端直播 - 安卓用户维护收货地址 !
//         * @author ApiGenerate
//         * @since 2019-11-04
//         */
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        return new StringBuilder().append("/** \n")
                .append("* Automatically generated file. DO NOT MODIFY ! \n")
                .append(desc)
                .append("* \n")
                .append("* @author ApiGenerate \n")
                .append("* @since \n")
                .append(dateFormat.format(new Date()))
                .append("\n")
                .append("*/\n")
                .append("public class ")
                .append(modelClassName)
                .append("{}")
                .toString();
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
                ItemModel itemModel = tItemModels.get(respFileList.getSelectedIndex());
                itemModel.isSel = !itemModel.isSel;
                respFileList.repaint();
                Logc.d(itemModel.toString());
            }
        };
    }

}
