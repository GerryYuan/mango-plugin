package com.hujiang.generator.popup.actions;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.google.common.collect.Lists;

public class RefreshAction {

	static public List<IProject> projects = Lists.newArrayList();

	public static void refresh() {
		projects.forEach(project -> {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (Exception e) {
				e.getStackTrace();
			}
		});
	}

	public static void register(IProject project) {
		projects.add(project);
	}
}
