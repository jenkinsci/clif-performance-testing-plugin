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

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.ow2.clif.jenkins.chart.ChartConfiguration;
import org.ow2.clif.jenkins.model.ClifReport;
import org.ow2.clif.jenkins.parser.clif.ClifParser;
import org.ow2.clif.jenkins.parser.clif.ClifParserException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;

/**
 * The publisher creates the results we want from the Clif execution.
 * =======
 * <p>An extension point to execute a post build report generation for Clif result.</p>
 * <p>It defines {@link ClifProjectAction} as Project Action and
 * {@link ClifBuildAction} as an action for each build.</p>
 * <p>This publisher is not executed when the build status is ABORTED or
 * FAILURE.</p>
 *
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class ClifPublisher
		extends Recorder {

	private final String clifReportDirectory;

	private final boolean dateFiltering;

	private final String minTimestamp;

	private final String maxTimestamp;

	private List<ClifAlias> alias;

	private List<ClifResultConfig> successPatterns;

	private ClifDataCleanup dataCleanupConfig;

	private final int chartWidth;

	private final int chartHeight;

	private final int distributionSliceNumber;

	private final int distributionSliceSize;

	private final int statisticalPeriod;

	public ClifPublisher(String clifReportDirectory) {
		// duplication of default values of
		// src/main/resources/org/ow2/clif/jenkins/ClifPublisher/config.jelly
		this(clifReportDirectory, false, "", "", 1200, 600, 50, 15, 5);
		this.dataCleanupConfig = new ClifDataCleanup();
	}

	@DataBoundConstructor
	public ClifPublisher(String clifReportDirectory, boolean dateFiltering, String minTimestamp,
	                     String maxTimestamp, int chartWidth, int chartHeight, int distributionSliceSize,
	                     int distributionSliceNumber, int statisticalPeriod) {
		this.clifReportDirectory = clifReportDirectory;
		this.dateFiltering = dateFiltering;
		this.minTimestamp = minTimestamp;
		this.maxTimestamp = maxTimestamp;
		this.chartWidth = chartWidth;
		this.chartHeight = chartHeight;
		this.distributionSliceSize = distributionSliceSize;
		this.distributionSliceNumber = distributionSliceNumber;
		this.statisticalPeriod = statisticalPeriod;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		PrintStream logger = listener.getLogger();

		// if build's status is not ABORTED or FAILURE
		if (this.canContinue(build.getResult())) {
			logger.println("Reading CLIF report directory: " + this.clifReportDirectory);
			FilePath buildWorkspace = build.getWorkspace();
			if (buildWorkspace != null && buildWorkspace.child(this.clifReportDirectory).exists()) {
				try {
					// Create clif parser
					FilePath reportDir = buildWorkspace.child(this.clifReportDirectory);
					ClifParser parser = new ClifParser(reportDir.getRemote(), build.getRootDir());

					ChartConfiguration chartConfig =
							new ChartConfiguration(this.chartHeight, this.chartWidth, this.distributionSliceNumber,
							                       this.distributionSliceSize, this.statisticalPeriod);
					parser.setChartConfiguration(chartConfig);

					if (this.dateFiltering) {
						parser.addDateFilter(getLong(this.minTimestamp), getLong(this.maxTimestamp));
					}

					if (this.dataCleanupConfig.isEnabled()) {
						parser.enableDataCleanup(this.dataCleanupConfig.getKeepFactor(),
						                         this.dataCleanupConfig.getKeepPercentage());
					}

					if (CollectionUtils.isNotEmpty(this.successPatterns)) {
						for (ClifResultConfig resultConfig : this.successPatterns) {
							parser.addSuccessfulResultPattern(resultConfig.getActionType(),
							                                  resultConfig.getPatternSuccessfulResult());
						}
					}

					if (CollectionUtils.isNotEmpty(this.alias)) {
						for (ClifAlias anAlias : this.alias) {
							parser.addActionAliasPattern(anAlias.getValue(), anAlias.getPattern());
						}
					}

					// Parse Clif report directory
					ClifReport report = parser.parse(listener.getLogger());
					build.addAction(new ClifBuildAction(build, report, this, logger));

				}
				catch (ClifParserException cpe) {
					logger.println("Clif report failed!");
					build.setResult(Result.FAILURE);
				}
			}
			else {
				logger.println("Clif report directory not found!");
				build.setResult(Result.FAILURE);
			}
		}
		else {
			logger.println(Messages.Publisher_WrongProjectStatus());
		}

		return true;
	}

	@Override
	public Action getProjectAction(AbstractProject<?, ?> project) {
		return project instanceof Project ? new ClifProjectAction((Project<?, ?>) project) : null;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	/**
	 * Clif publisher doesn't need to continue if the build's status is ABORTED or FAILURE.
	 *
	 * @param result build status object
	 * @return true if build status is not ABORTED or FAILURE.
	 */
	protected boolean canContinue(final Result result) {
		return result != Result.ABORTED && result != Result.FAILURE;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		// see Descriptor javadoc for more about what a descriptor is.
		return DESCRIPTOR;
	}

	/**
	 * Descriptor should be singleton.
	 */
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	public static final class DescriptorImpl
			extends BuildStepDescriptor<Publisher> {


		public DescriptorImpl() {
			super(ClifPublisher.class);
		}

		@Override
		public String getDisplayName() {
			return Messages.Publisher_DisplayName();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			req.bindParameters(this, "clif.");
			save();
			return super.configure(req, formData);
		}

		/**
		 * Creates a new instance of {@link ClifPublisher} from a submitted form.
		 */
		@Override
		public ClifPublisher newInstance(final StaplerRequest req, JSONObject formData)
		throws FormException
		{
			if (req != null)
			{
				ClifPublisher instance = req.bindParameters(ClifPublisher.class, "clif.");
				instance.alias = req.bindParametersToList(ClifAlias.class, "clif.alias.");
				instance.successPatterns = req.bindParametersToList(ClifResultConfig.class, "clif.successPatterns.");
				instance.dataCleanupConfig = req.bindParameters(ClifDataCleanup.class, "clif.datacleanup.");
				return instance;
			}
			else
			{
				return null;
			}
		}

		public FormValidation doCheckClifReportDirectory(@QueryParameter String value) {
			if (StringUtils.isNotBlank(value)) {
				return FormValidation.ok();
			}
			return FormValidation.error(Messages.Publisher_ClifReportDirectory_Mandatory());
		}

		public FormValidation doCheckMinTimestamp(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_MinTimeStamp_Format());
		}

		public FormValidation doCheckMaxTimestamp(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_MaxTimeStamp_Format());
		}

		public FormValidation doCheckPattern(@QueryParameter String value) {
			if (StringUtils.isNotBlank(value)) {
				try {
					Pattern.compile(value);
					return FormValidation.ok();
				}
				catch (Exception e) {
					return FormValidation.error(Messages.Publisher_Pattern_Invalid());
				}
			}
			return FormValidation.error(Messages.Publisher_ActionAlias_Pattern_Mandatory());
		}

		public FormValidation doCheckAlias(@QueryParameter String value) {
			if (StringUtils.isNotBlank(value)) {
				return FormValidation.ok();
			}
			return FormValidation.error(Messages.Publisher_ActionAlias_Alias_Mandatory());
		}

		public FormValidation doCheckActionType(@QueryParameter String value) {
			if (StringUtils.isNotBlank(value)) {
				return FormValidation.ok();
			}
			return FormValidation.error(Messages.Publisher_ResultConfig_ActionType_Mandatory());
		}

		public FormValidation doCheckChartWidth(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_ChartWidth_Format());
		}

		public FormValidation doCheckChartHeight(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_ChartHeight_Format());
		}

		public FormValidation doCheckDistributionSliceSize(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_DistributionSliceSize_Format());
		}

		public FormValidation doCheckDistributionSliceNumber(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_DistributionSliceNumber_Format());
		}

		public FormValidation doCheckStatisticalPeriod(@QueryParameter String value) {
			return checkPositiveLongValue(value, Messages.Publisher_StatisticalPeriod_Format());
		}

		public FormValidation doCheckKeepFactor(@QueryParameter String value) {
			return checkPositiveDoubleValue(value, Messages.Publisher_KeepFactor_Format());
		}

		public FormValidation doCheckKeepPercentage(@QueryParameter String value) {
			FormValidation res = checkPositiveDoubleValue(value, Messages.Publisher_KeepPercentage_Format());
			if (FormValidation.ok().equals(res)) {
				if (getDouble(value) > 100) {
					return FormValidation.error(Messages.Publisher_KeepPercentage_Invalid());
				}
			}
			return res;
		}

	}

	protected static boolean isLong(String value) {
		if (StringUtils.isNotBlank(value)) {
			try {
				Long.valueOf(value);
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	protected static long getLong(String value) {
		if (StringUtils.isNotBlank(value)) {
			try {
				return Long.parseLong(value);
			}
			catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	protected static boolean isDouble(String value) {
		if (StringUtils.isNotBlank(value)) {
			try {
				Double.valueOf(value);
			}
			catch (NumberFormatException e) {
				return false;
			}
		}
		return true;
	}

	protected static double getDouble(String value) {
		if (StringUtils.isNotBlank(value)) {
			try {
				return Double.parseDouble(value);
			}
			catch (NumberFormatException e) {
				return -1;
			}
		}
		return -1;
	}

	private static FormValidation checkPositiveLongValue(String value, String errorMsg) {
		if (isLong(value)) {
			if (getLong(value) < 0) {
				return FormValidation.error(errorMsg);
			}
			return FormValidation.ok();
		}
		return FormValidation.error(errorMsg);
	}

	private static FormValidation checkPositiveDoubleValue(String value, String errorMsg) {
		if (isDouble(value)) {
			if (getDouble(value) < 0) {
				return FormValidation.error(errorMsg);
			}
			return FormValidation.ok();
		}
		return FormValidation.error(errorMsg);
	}

	public boolean getDateFiltering() {
		return dateFiltering;
	}

	public String getMinTimestamp() {
		return minTimestamp;
	}

	public String getMaxTimestamp() {
		return maxTimestamp;
	}

	public String getClifReportDirectory() {
		return this.clifReportDirectory;
	}

	public List<ClifAlias> getAlias() {
		return this.alias;
	}

	public List<ClifResultConfig> getSuccessPatterns() {
		return this.successPatterns;
	}

	public ClifDataCleanup getDataCleanupConfig() {
		return dataCleanupConfig;
	}

	public int getChartWidth() {
		return chartWidth;
	}

	public int getChartHeight() {
		return chartHeight;
	}

	public int getDistributionSliceNumber() {
		return distributionSliceNumber;
	}

	public int getDistributionSliceSize() {
		return distributionSliceSize;
	}

	public boolean isDateFiltering() {
		return dateFiltering;
	}

	public int getStatisticalPeriod() {
		return statisticalPeriod;
	}

}
