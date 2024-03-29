/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.itemis.eclipse.exportdecorator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * @author holger willebrandt - Initial contribution and API
 */
public class PluginExportDecorator implements ILabelDecorator /* IColorDecorator */{

	private String currentProject;

	public void addListener(final ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(final Object element, final String property) {
		return false;
	}

	public void removeListener(final ILabelProviderListener listener) {
	}

	public Image decorateImage(final Image image, final Object element) {
		return null;
	}

	public String decorateText(final String text, final Object element) {
		if (element instanceof IPackageFragment) {
			if (isExported((IPackageFragment) element)) {
				return text + " +"; //$NON-NLS-1$
			}
		}
		return text;
	}

	private final List<String> exportedPackages = new ArrayList<String>();

	/**
	 * @param element
	 * @return
	 */
	private boolean isExported(final IPackageFragment element) {
		final IProject javaProject = element.getResource().getProject();
		if (!javaProject.getName().equals(currentProject)) {
			currentProject = javaProject.getName();
			cacheExportedPackages(javaProject);
		}
		return exportedPackages.contains(element.getElementName());
	}

	private void cacheExportedPackages(final IProject javaProject) {
		exportedPackages.clear();
		final IFile findElement = javaProject.getFile(new Path("META-INF/MANIFEST.MF")); //$NON-NLS-1$
		if (findElement.exists()) {
			try {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(findElement.getContents()));
				String line = null;
				boolean exportBlock = false;
				while ((line = reader.readLine()) != null) {
					if (exportBlock || line.matches("Export-Package:.*")) { //$NON-NLS-1$
						exportBlock = true;
						String packageName = null;
						if (line.matches("Export-Package:.*")) { //$NON-NLS-1$
							packageName = line.replaceAll(
									"Export-Package:\\ ?(([a-zA-Z_][a-zA-Z0-9_]*\\.)+[a-zA-Z_][a-zA-Z0-9_]*)\\ ?,?", //$NON-NLS-1$
									"$1"); //$NON-NLS-1$
						} else if (line.matches("\\ ?(([a-zA-Z_][a-zA-Z0-9_]*\\.)+[a-zA-Z_][a-zA-Z0-9_]*)\\ ?,?")) { //$NON-NLS-1$
							packageName = line.replaceAll("[\\ ,]", ""); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							exportBlock = false;
						}
						if (packageName != null) {
							exportedPackages.add(packageName);
						}
					} else {
						exportBlock = false;
					}
				}
				reader.close();
			} catch (final CoreException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}
}
