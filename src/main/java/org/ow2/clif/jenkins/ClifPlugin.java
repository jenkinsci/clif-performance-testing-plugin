/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 * Copyright (C) 2016 Orange SA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: clif@ow2.org
 */
package org.ow2.clif.jenkins;

import java.io.File;
import java.nio.file.Files;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import hudson.Extension;
import org.jenkinsci.Symbol;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

/**
 * Global configuration parameters for the Clif plugin.
 * As of today, there is a single parameter: clifRootDir.
 * It specifies the folder where to unzip imported CLIF project archives.
 * Such imported archives will possibly result is having multiple jobs
 * (one per imported test plan) sharing a single workspace containing all
 * files extracted from one CLIF project archive.
 * Each imported archive gets its own sub-folder created in the
 * configured root folder. This sub-folder is named according to the
 * CLIF project folder's name in the archive.
 *
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
@Extension @Symbol("clifGlobalConfig")
public class ClifPlugin extends GlobalConfiguration {

	static final String ICON_FILE_NAME = "graph.gif";

	static final String DISPLAY_NAME = Messages.Plugin_DisplayName();

	static public final String DEFAULT_ROOT_DIR = "clif";

	static final String URL = "clif";

	/**
	 * Root directory for storing imported Clif projects.
	 */
	private String clifRootDir = DEFAULT_ROOT_DIR;

	/**
	 * Get the ClifPlugin singleton managing CLIF's global parameters
	 * @return the ClifPlugin singleton
	 */
	public static @CheckForNull ClifPlugin get()
	{
		return GlobalConfiguration.all().get(ClifPlugin.class);
	}


	/**
	 * Just calls {@link #load()}
	 */
	public ClifPlugin()
	{
		load();
	}


	/**
	 * Loads the currently configured values
	 */
	@Override
	public synchronized void load()
	{
		super.load();
		setDefaultClifRootDirIfNecessary();
	}


	/**
	 * Gets current value of CLIF root directory
	 * where CLIF archives are unzipped.
	 * @return The configured Clif root directory.
	 */
	public String getClifRootDir()
	{
		return clifRootDir;
	}


	/**
	 * Configures a new value for CLIF root directory
	 * where CLIF archives are unzipped.
	 * @param clifRootDir the new root directory for
	 * imported CLIF projects. If blank, the new value is set
	 * to the default value {@value #DEFAULT_ROOT_DIR}
	 */
	public void setClifRootDir(final String clifRootDir)
	{
		this.clifRootDir = clifRootDir;
		setDefaultClifRootDirIfNecessary();
		save();
	}


	/**
	 * Resets the door directory of imported CLIF projects
	 * to the default value {@value #DEFAULT_ROOT_DIR}
	 * if current value is blank.
	 */
	private void setDefaultClifRootDirIfNecessary() {
		if (StringUtils.isBlank(this.clifRootDir)) {
			this.clifRootDir = DEFAULT_ROOT_DIR;
		}
	}


	/**
	 * Returns the File object representing the configured clif root directory
	 * in its absolute path form. When the configured root folder is a relative
	 * path, it is resolved from the Jenkins' installation root directory.
	 *
	 * @return A file object representing the configured Clif root parameter
	 */
	@NonNull
	public File dir()
	{
		File rootFile = new File(clifRootDir);
		if (! rootFile.isAbsolute())
		{
			rootFile = new File(Jenkins.getInstance().root.getPath() + File.separator + clifRootDir);
		}
		return rootFile;
	}

	/**
	 * Validates a CLIF root folder. The folder is valid either if it exists,
	 * is actually a folder and is writable, or if the closest existing
	 * parent file in its absolute path is a writable folder. In other words,
	 * validation relies on the availability of the CLIF root folder or the
	 * ability to create it.
	 * @param value the new value for CLIF root folder
	 * @return validation result (ok or error) with an explicit, localized message.
	 */
	public FormValidation doCheckClifRootDir(@QueryParameter String value)
	{
		FormValidation result = null;
		File candidateFile = new File(value);
		if (! candidateFile.isAbsolute())
		{
			candidateFile = new File(
				Jenkins.getInstance().root.getPath()
				+ File.separator
				+ value);
		}
		File checkFile = candidateFile;
		while (checkFile != null && result == null)
		{
			if (checkFile.exists())
			{
				if (checkFile.isDirectory() && checkFile.canWrite())
				{
					result = FormValidation.ok(
						Messages.ClifPlugin_GoodDirectory(candidateFile));
				}
				else
				{
					result = FormValidation.error(
						Messages.ClifPlugin_BadDirectory(candidateFile));
				}
			}
			else
			{
				checkFile = checkFile.getParentFile();
			}
		}
		if (result == null)
		{
			FormValidation.error(
				Messages.ClifPlugin_BadDirectory(candidateFile));
		}
		return result;
	}
}
