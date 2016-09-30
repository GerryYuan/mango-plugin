package com.hujiang.generator.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import com.google.common.base.Strings;
import com.hujiang.generator.code.Generator4BeanAndDao;
import com.hujiang.generator.config.PropertiesOperate;
import com.hujiang.generator.main.GeneratorPanel;
import com.hujiang.generator.popup.actions.RefreshAction;

public class GenneratorListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		String table = GeneratorPanel.tableTextField.getText();
		String packagText = GeneratorPanel.packageTextLabel.getText();
		String filePath = PropertiesOperate.getProjectJavaPath();
		if (Strings.isNullOrEmpty(table) || Strings.isNullOrEmpty(packagText) || Strings.isNullOrEmpty(filePath)) {
			JOptionPane.showMessageDialog(null, "各个参数为必填项，请认真填写！", "友情提示", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Generator4BeanAndDao generator = new Generator4BeanAndDao();
		try {
			generator.generateCode(table, packagText, filePath);
			JOptionPane.showMessageDialog(null, "文件已生成。", "友情提示", JOptionPane.PLAIN_MESSAGE);
			RefreshAction.refresh();
			return;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "生成文件失败！", "友情提示", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}

