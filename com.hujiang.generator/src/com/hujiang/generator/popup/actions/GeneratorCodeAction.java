package com.hujiang.generator.popup.actions;

import java.io.IOException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.hujiang.generator.config.PropertiesOperate;
import com.hujiang.generator.main.GeneratorMain;

public class GeneratorCodeAction implements IObjectActionDelegate {

	public Shell shell;

	/**
	 * Constructor for Action1.
	 */
	public GeneratorCodeAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		try {
			PropertiesOperate.init();
			new GeneratorMain().generatorPanel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*try {
			MessageDialog.openInformation(shell, "Generator", PropertiesOperate.getString("generator.file.package"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
