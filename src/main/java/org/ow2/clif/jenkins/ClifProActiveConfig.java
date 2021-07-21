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

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class ClifProActiveConfig
		implements Serializable, Describable<ClifProActiveConfig> {

	private static final long serialVersionUID = 1L;

	/**
	 * The url of the ProActive Scheduler;
	 */
	private String schedulerURL;

	/**
	 * The absolute path of the scheduler credentials file on the Jenkins server file system
	 */
	private String schedulerCredentialsFile;

	/**
	 * Alternative to the credentials file
	 */
	private String schedulerLogin;
	private String schedulerPassword;

	public ClifProActiveConfig() {
	}


	@DataBoundConstructor
	public ClifProActiveConfig(
		String schedulerURL,
		String schedulerCredentialsFile,
		String schedulerLogin,
		String schedulerPassword)
	{
		super();
		this.schedulerURL = Util.fixEmptyAndTrim(schedulerURL);
		this.schedulerCredentialsFile = Util.fixEmptyAndTrim(schedulerCredentialsFile);
		this.schedulerLogin = Util.fixEmptyAndTrim(schedulerLogin);
		this.schedulerPassword = Util.fixEmptyAndTrim(schedulerPassword);
	}

	public Descriptor<ClifProActiveConfig> getDescriptor() {
		return Hudson.get().getDescriptorByType(ProActiveConfigDescriptor.class);
	}

	@Extension
	public static final class ProActiveConfigDescriptor
			extends Descriptor<ClifProActiveConfig> {

		@Override
		public String getDisplayName() {
			return "";
		}


	}

	public String getSchedulerURL() {
		return schedulerURL;
	}

	public String getSchedulerCredentialsFile() {
		return schedulerCredentialsFile;
	}

	public String getSchedulerLogin() {
		return schedulerLogin;
	}

	public String getSchedulerPassword() {
		return schedulerPassword;
	}
}
