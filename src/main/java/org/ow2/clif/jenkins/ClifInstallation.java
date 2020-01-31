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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;

/**
 * Represents the Clif installation on the system.
 *
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public final class ClifInstallation
		extends ToolInstallation
		implements EnvironmentSpecific<ClifInstallation>, NodeSpecific<ClifInstallation> {

	private final ClifProActiveConfig clifProActiveConfig;
	private final String options;

	@DataBoundConstructor
	public ClifInstallation(
		String name,
		String home,
		String options,
		ClifProActiveConfig clifProActiveConfig,
		List<? extends ToolProperty<?>> properties)
	{
		super(name, launderHome(home), properties);
		this.options = Util.fixEmptyAndTrim(options);
		this.clifProActiveConfig = clifProActiveConfig;
	}

	public String getOptions()
	{
		return options;
	}

	public ClifProActiveConfig getClifProActiveConfig() {
		return clifProActiveConfig;
	}

	public boolean isRunWithScheduler() {
		return clifProActiveConfig != null;
	}

	private static String launderHome(String home) {
		if (home.endsWith("/") || home.endsWith("\\")) {
			// see https://issues.apache.org/bugzilla/show_bug.cgi?id=26947
			// Ant doesn't like the trailing slash, especially on Windows
			return home.substring(0, home.length() - 1);
		}
		else {
			return home;
		}
	}

	/**
	 * Gets the executable path of this Clif on the given target system.
	 * @param launcher object
	 * @return the executable path
	 * @throws InterruptedException the call was interrupted
	 * @throws IOException exception trying to resolve the path
	 */
	public String getExecutable(Launcher launcher)
			throws IOException, InterruptedException {
		VirtualChannel channel = launcher.getChannel();
		if (channel == null)
		{
			throw new IOException("The target node is not configured for remote execution");
		}
		return channel.call(new Callable<String, IOException>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String call()
					throws IOException {
				File exe = getExeFile();
				if (exe.exists()) {
					return exe.getPath();
				}
				throw new IOException("Could not find CLIF installation " + exe.getPath());
			}

			@Override
			public void checkRoles(RoleChecker checker) throws SecurityException {
				// TODO Auto-generated method stub
			}
		});
	}

	private File getExeFile() {
		String execName = Functions.isWindows() ? "clifcmd.bat" : "clifcmd";
		String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);

		return new File(home, "bin/" + execName);
	}

	/**
	 * Checks executable existence
	 * @return true if the executable exists.
	 * @throws InterruptedException the call was interrupted
	 * @throws IOException exception trying to find the executable
	 */
	public boolean getExists()
			throws IOException, InterruptedException {
		return getExecutable(new Launcher.LocalLauncher(TaskListener.NULL)) != null;
	}

	private static final long serialVersionUID = 1L;

	public ClifInstallation forEnvironment(EnvVars environment) {
		return new ClifInstallation(
			getName(),
			environment.expand(getHome()),
			options,
			clifProActiveConfig,
			getProperties().toList());
	}

	public ClifInstallation forNode(Node node, TaskListener log)
			throws IOException, InterruptedException {
		return new ClifInstallation(
			getName(),
			translateFor(node, log),
			options,
			clifProActiveConfig,
			getProperties().toList());
	}

	@Extension
	public static class DescriptorImpl
			extends ToolDescriptor<ClifInstallation> {

		@Override
		public String getDisplayName() {
			return "Clif";
		}

		// for compatibility reasons, persistence is done by ClifBuilder.DescriptorImpl
		@Override
		public ClifInstallation[] getInstallations() {
			return Hudson.get().getDescriptorByType(ClifBuilder.DescriptorImpl.class).getInstallations();
		}

		@Override
		public void setInstallations(ClifInstallation... installations) {
			Hudson.get().getDescriptorByType(ClifBuilder.DescriptorImpl.class).setInstallations(
					installations);
		}

		/**
		 * Checks if the CLIF_HOME is valid.
		 * @param value file object representing the path to the home directory
		 * of a CLIF installation
		 * @return validation result (ok or error)
		 */
		public FormValidation doCheckHome(@QueryParameter File value) {
			// this can be used to check the existence of a file on the server, so needs to be protected
			if (!Hudson.get().hasPermission(Hudson.ADMINISTER)) {
				return FormValidation.ok();
			}

			if (value.getPath().isEmpty()) {
				return FormValidation.error(Messages.Clif_HomeRequired());
			}

			if (!value.isDirectory()) {
				return FormValidation.error(Messages.Clif_NotADirectory(value));
			}

			File[] libFiles = new File(value, "lib").listFiles(
				new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name)
					{
						return name.startsWith("clif-core") && name.endsWith(".jar");
					}
				});
			if (libFiles == null || libFiles.length == 0)
			{
				return FormValidation.error(Messages.Clif_NotClifDirectory(value));
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckName(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_NameRequired());
			}
			return FormValidation.ok();
		}

		/**
		 * Checks if the Scheduler url is valid.
		 *
		 * @param value scheduler URL, as a string
		 * @return validation result (ok or error)
		 */
		public FormValidation doCheckSchedulerURL(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_SchedulerURLRequired());
			}
			return FormValidation.ok();
		}


		public FormValidation doCheckInstallation(
				@QueryParameter File home,
				@QueryParameter String schedulerURL,
				@QueryParameter File schedulerCredentialsFile,
				@QueryParameter String schedulerLogin,
				@QueryParameter String schedulerPassword) {
			try {
				FormValidation homeValid = doCheckHome(home);
				if (homeValid.kind.equals(Kind.ERROR))
				{
					return homeValid;
				}
				File[] libextFiles = new File(home, "lib" + File.separator + "ext").listFiles(
					new FilenameFilter()
					{
						@Override
						public boolean accept(File dir, String name)
						{
							return name.startsWith("proactive-programming") && name.endsWith(".jar");
						}
					});
				if (libextFiles == null || libextFiles.length == 0)
				{
					return FormValidation.error(Messages.ClifInstallation_BadProactiveInstallation());
				}

				if (schedulerCredentialsFile != null && !schedulerCredentialsFile.getPath().isEmpty())
				{
					if (!schedulerCredentialsFile.exists())
					{
						return FormValidation.error(Messages.ClifInstallation_CredentialsFileNotFound());
					}
				}
				else if (schedulerLogin == null || schedulerLogin.trim().isEmpty())
				{
					return FormValidation.error(Messages.ClifInstallation_CredentialsMissing());
				}

				if (StringUtils.isBlank(schedulerURL)) {
					return FormValidation.error(Messages.ClifInstallation_SchedulerURLMissing());
				}

				return FormValidation.ok(Messages.ClifInstallation_ProactiveInstallationValid());
			}
			catch (Exception e) {
				//return FormValidation.errorWithMarkup("<p>"+Messages.Mailer_FailedToSendEmail()+"</p><pre>"+Util.escape(Functions.printThrowable(e))+"</pre>");
				return FormValidation.error(Messages.ClifInstallation_BadProactiveInstallation());
			}
		}
	}

}
