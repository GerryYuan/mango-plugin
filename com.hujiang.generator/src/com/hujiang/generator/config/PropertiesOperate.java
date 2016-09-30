package com.hujiang.generator.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class PropertiesOperate {

	static final String DEFAULT_PROPERTIES_URL = File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "application.properties";

	static Map<String, String> propertyMaps = Maps.newHashMap();

	static String projectPath = "";

	public static Map<String, String> readProperties(String path) throws FileNotFoundException, IOException {
		if (!propertyMaps.isEmpty()) {
			return propertyMaps;
		}
		Properties properties = new Properties();
		properties.load(new FileInputStream(new File(path)));
		propertyMaps.putAll(Maps.fromProperties(properties));
		return propertyMaps;
	}

	public static String getString(String key) throws FileNotFoundException, IOException {
		return PropertiesOperate.readProperties(getProjectPath() + DEFAULT_PROPERTIES_URL).get(key);
	}

	public static void init() throws FileNotFoundException, IOException{
		PropertiesOperate.readProperties(getProjectPath() + DEFAULT_PROPERTIES_URL);
	}
	
	public static String getProjectPath() {
		if (!Strings.isNullOrEmpty(projectPath)) {
			return projectPath;
		}
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable) {
				IProject project = (IProject) ((IAdaptable) firstElement).getAdapter(IProject.class);
				projectPath = project.getLocation().toFile().getAbsolutePath();
			}
		}
		return projectPath;
	}

}
