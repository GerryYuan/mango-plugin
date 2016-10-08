package com.hujiang.generator.panel;

import java.awt.Container;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.hujiang.generator.listener.GenneratorListener;

public class GeneratorPanel extends JFrame {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 4121015905800239976L;

	// table
	private JLabel tableLabel = new JLabel("Mysql表名：");
	public static JTextField tableTextField = new JTextField(10);

	// package
	private JLabel packageLabel = new JLabel("Java类包名：");
	public static JTextField packageTextLabel = new JTextField(10);

	public void generatorPanel() throws FileNotFoundException, IOException {
		this.setTitle("代码生成器");
		this.setVisible(true);
		this.setResizable(false);
		this.setSize(400, 400);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container con = this.getContentPane();
		GridLayout myLayout = new GridLayout(3, 1);
		myLayout.setHgap(10);
		myLayout.setVgap(10);

		JPanel jPanelMain = new JPanel();
		jPanelMain.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
		jPanelMain.setLayout(myLayout);

		// 第1行
		JPanel jPanel1 = new JPanel();
		jPanel1.setLayout(new GridLayout(1, 2));
		jPanel1.add(tableLabel);
		jPanel1.add(tableTextField);

		// 第2行
		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new GridLayout(1, 2));
		jPanel2.add(packageLabel);
		jPanel2.add(packageTextLabel);

		// 第4行
		JButton button = new JButton("生成");
		button.addActionListener(new GenneratorListener());

		jPanelMain.add(jPanel1);
		jPanelMain.add(jPanel2);
		jPanelMain.add(button);
		con.add(jPanelMain);
		this.pack();
	}

}
