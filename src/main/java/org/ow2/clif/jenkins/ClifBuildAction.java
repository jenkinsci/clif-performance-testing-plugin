/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
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
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.chart.*;
import org.ow2.clif.jenkins.model.ClifReport;
import hudson.model.AbstractBuild;

/**
 * Action used for Clif report on build level.
 *
 * @author Julien Coste
 */
public class ClifBuildAction
		extends AbstractClifAction {
	private final AbstractBuild<?, ?> build;

	private final ClifReport report;

	public ClifBuildAction(final AbstractBuild<?, ?> build, final ClifReport report, final ClifPublisher publisher,
	                       final PrintStream logger) {
		this.build = build;
		this.report = report;
		logger.println("Created Clif results");
	}

	public void doCallChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final CallChart chart = new CallChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doMovingStatChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final MovingStatChart chart =
				new MovingStatChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doFixedSliceNumberDistributionChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final FixedSliceNumberDistributionChart chart =
				new FixedSliceNumberDistributionChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null
				);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doFixedSliceSizeDistributionChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final FixedSliceSizeDistributionChart chart =
				new FixedSliceSizeDistributionChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public void doQuantileDistributionChart(final StaplerRequest request, final StaplerResponse response)
			throws IOException {

		final ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		final QuantileDistributionChart chart =
				new QuantileDistributionChart(params.getTestPlan(), params.getBladeId(), params.getLabel(), null);
		chart.doPng(build.getRootDir(), request, response);
	}

	public AbstractBuild<?, ?> getBuild() {
		return build;
	}

	public ClifReport getReport() {
		return report;
	}

	@Override
	public String getDisplayName() {
		return Messages.BuildAction_DisplayName();
	}
}
