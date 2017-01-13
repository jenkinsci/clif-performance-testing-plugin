/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012, 2013 France Telecom R&D
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang.StringUtils;
import org.ow2.clif.storage.lib.filestorage.FileStorageCommons;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import hudson.tools.ToolInstallation;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.util.VariableResolver;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import net.sf.json.JSONObject;

/**
 * Clif task builder, based on CommandInterpreter, and finally on
 * BatchFile or Shell depending on the operating system.
 * 
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class ClifBuilder extends Builder
{
	/**
	 * Identifies {@link ClifInstallation} to be used.
	 */
	private final String clifName;

	/**
	 * Java options for clifcmd
	 */
	private final String clifOpts;

	/**
	 * Target directory for storing CLIF's measurements
	 */
	private final String reportDir;

	/**
	 * TestPlan file (ctp) to run
	 */
	@Nonnull
	private final String testPlanFile;

	private CommandInterpreter delegate = null;


	@DataBoundConstructor
	public ClifBuilder(
		@Nonnull String clifName,
		String clifOpts,
		@Nonnull String testPlanFile,
		String reportDir)
	{
		this.clifName = clifName;
		this.clifOpts = Util.fixEmptyAndTrim(clifOpts);
		this.testPlanFile = testPlanFile.trim();
		this.reportDir = reportDir == null ? FileStorageCommons.REPORT_DIR_DEFAULT : reportDir.trim();
	}

	@Nonnull
	public String getTestPlanFile() {
		return testPlanFile;
	}

	/**
	 * Gets the Clif installation to use
	 * @return the specific Clif installation to use, or null if
	 * no Clif installation is specified
	 */
	public ClifInstallation getClif() {
		for (ClifInstallation i : getDescriptor().getInstallations()) {
			if (clifName != null && clifName.equals(i.getName())) {
				return i;
			}
		}
		return null;
	}

	/**
	 * Gets the CLIF_OPTS parameter
	 * @return the CLIF_OPTS parameter, or null if not set.
	 */
	public String getClifOpts() {
		return clifOpts;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
	throws InterruptedException, IOException
	{
		ClifInstallation clifInst = getClif();
		if (clifInst == null) {
			listener.fatalError(Messages.Clif_ClifInstallationNotFound());
			return false;
		}
		String javaOpts = "-Dclif.report.dir=\"" + reportDir + "\"";
		if (clifOpts != null)
		{
			javaOpts += " " + clifOpts;
		}
		if (clifInst.isRunWithScheduler())
		{
			javaOpts += " " + "-Dsched.url=" + clifInst.getClifProActiveConfig().getSchedulerURL();
			if (clifInst.getClifProActiveConfig().getSchedulerCredentialsFile() != null)
			{
				javaOpts += " " + "-Dsched.credentials=" + clifInst.getClifProActiveConfig().getSchedulerCredentialsFile();
			}
			else
			{
				javaOpts += " " + "-Dsched.login=" + clifInst.getClifProActiveConfig().getSchedulerLogin();
				javaOpts += " " + "-Dsched.password=" + clifInst.getClifProActiveConfig().getSchedulerPassword();
			}
			if (clifInst.getOptions() != null)
			{
				javaOpts += " " + clifInst.getOptions();
			}
		}
		String clifCmd = "\"" + clifInst.getHome(); 
		if (Functions.isWindows())
		{
			javaOpts = "set JAVA_OPTS=" + javaOpts + " & ";
			clifCmd +=  "\\bin\\clifcmd.bat\"";
		}
		else
		{
			javaOpts = "JAVA_OPTS=\"" + javaOpts + "\" ";
			clifCmd += "/bin/clifcmd\"";
		}
		String testName = new File(testPlanFile).getName();
		if (testName.endsWith(".ctp"))
		{
			testName = testName.substring(0, testName.length() - 4);
		}
		String command = javaOpts + clifCmd + " launch \"" + testName + "\" \"" + testPlanFile + "\" \"" + testName
			+ "\" && " + clifCmd + " quickstats";
		if (Functions.isWindows())
		{
			delegate = new BatchFile(command);
		}
		else
		{
			delegate = new Shell(command);
		}
		return delegate.perform(build, launcher, listener);
	}

	private void addSensitiveVariables(AbstractBuild<?, ?> build, ArgumentListBuilder args) {
		args.addKeyValuePairs("-D", build.getBuildVariables(), build.getSensitiveBuildVariables());
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static class DescriptorImpl
			extends BuildStepDescriptor<Builder> {
		@CopyOnWrite
		private volatile ClifInstallation[] installations = new ClifInstallation[0];

		public DescriptorImpl() {
			load();
		}

		protected DescriptorImpl(Class<? extends ClifBuilder> clazz) {
			super(clazz);
		}

		/**
		 * Gets the {@link ClifInstallation.DescriptorImpl} instance.
		 * @return the {@link ClifInstallation.DescriptorImpl} instance
		 */
		public ClifInstallation.DescriptorImpl getToolDescriptor() {
			return ToolInstallation.all().get(ClifInstallation.DescriptorImpl.class);
		}

		@Override
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getHelpFile() {
			return "/plugin/clif-jenkins-plugin/help-clif.html";
		}

		@Override
		public String getDisplayName() {
			return Messages.Clif_DisplayName();
		}

		public ClifInstallation[] getInstallations() {
			return installations.clone();
		}

		public void setInstallations(ClifInstallation... clifInstallations) {
			this.installations = clifInstallations;
			save();
		}

		/**
		 * Checks if the ReportDir is valid.
		 * @param value the path to CLIF report directory
		 * @return validation result (ok or error)
		 */
		public FormValidation doCheckReportDir(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_ReportDirRequired());
			}
			return FormValidation.ok();
		}

		/**
		 * Checks if the TestPlanFile is valid.
		 * @param value the test plan file
		 * @return validation result (ok or error)
		 */
		public FormValidation doCheckTestPlanFile(@QueryParameter String value) {
			if (Util.fixEmptyAndTrim(value) == null) {
				return FormValidation.error(Messages.Clif_TestPlanFileRequired());
			}
			if (!StringUtils.endsWithIgnoreCase(value, ".ctp")) {
				return FormValidation.error(Messages.Clif_TestPlanFileNotCTP());
			}
			return FormValidation.ok();
		}
	}

	public String getReportDir()
	{
		return reportDir;
	}
}
