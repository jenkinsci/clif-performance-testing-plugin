/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012-2013 France Telecom R&D
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

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.model.ClifReport;
import org.ow2.clif.jenkins.model.Measure;
import org.ow2.clif.jenkins.model.TestPlan;
import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.util.ChartUtil;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;

/**
 * Action used for Clif report on project level.
 *
 * @author Julien Coste
 * @author Bruno Dillenseger
 */
public class ClifProjectAction
		extends AbstractClifAction {

	private final Project project;

	public Map<String, Set<String>> getActionsAvailable() {
		return computeActionsAvailable();
	}

	public ClifProjectAction(Project project) {
		this.project = project;

	}

	private Map<String, Set<String>> computeActionsAvailable() {
		Map<String, Set<String>> res = new HashMap<String, Set<String>>();
		List<?> builds = getProject().getBuilds();
		for (Object build : builds) {
			AbstractBuild<?, ?> currentBuild = (AbstractBuild<?, ?>) build;
			ClifBuildAction clifBuildAction = currentBuild.getAction(ClifBuildAction.class);
			if (clifBuildAction == null) {
				continue;
			}
			ClifReport clifReport = clifBuildAction.getReport();
			if (clifReport == null) {
				continue;
			}
			for (TestPlan tp : clifReport.getTestplans()) {
				Set<String> actions = res.get(tp.getName());
				if (actions == null) {
					actions = new HashSet<String>();
					res.put(tp.getName(), actions);
				}
				if (tp.getAggregatedMeasures() != null) {
					for (Measure m : tp.getAggregatedMeasures()) {
						actions.add(m.getName());
					}
				}
			}
		}
		return res;
	}

	public Project getProject() {
		return project;
	}

	@Override
	public String getDisplayName() {
		return Messages.ProjectAction_DisplayName();
	}

	public ClifBuildAction getActionByBuildNumber(int number) {
		return project.getBuildByNumber(number).getAction(ClifBuildAction.class);
	}

	public void doActionGraph(StaplerRequest request, StaplerResponse response)
			throws IOException {

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		if (shouldReloadGraph(request, response)) {
			ChartUtil.generateGraph(request, response, createActionGraph(params), 400, 250);
		}
	}

	public void doActionErrorGraph(StaplerRequest request, StaplerResponse response)
			throws IOException {

		ClifGraphParam params = new ClifGraphParam();
		request.bindParameters(params);

		if (shouldReloadGraph(request, response)) {
			ChartUtil.generateGraph(request, response, createActionErrorGraph(params), 400, 250);
		}
	}

	private JFreeChart createActionGraph(ClifGraphParam params) {
		DefaultStatisticalCategoryDataset timeDS = new DefaultStatisticalCategoryDataset();
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> minmaxDS =
			new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

		List<AbstractBuild> builds = new ArrayList<AbstractBuild>(getProject().getBuilds());
		Collections.sort(builds);
		for (Run<?,?> currentBuild : builds) {
			Result buildResult = currentBuild.getResult();
			if (buildResult != null	&& buildResult.isBetterOrEqualTo(Result.SUCCESS))
			{
				ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(currentBuild);
				ClifBuildAction clifBuildAction = currentBuild.getAction(ClifBuildAction.class);
				if (clifBuildAction == null) {
					continue;
				}
				ClifReport clifReport = clifBuildAction.getReport();
				if (clifReport == null) {
					continue;
				}
				TestPlan tp = clifReport.getTestplan(params.getTestPlan());
				if (tp == null) {
					continue;
				}
				if (tp.getAggregatedMeasures() != null) {
					Measure m = tp.getAggregatedMeasure(params.getLabel());
					if (m == null) {
						continue;
					}
					timeDS.add(m.getAverage(), m.getStdDev(), Messages.ProjectAction_Mean(), label);
					minmaxDS.add(m.getMax(), Messages.ProjectAction_Max(), label);
					minmaxDS.add(m.getMin(), Messages.ProjectAction_Min(), label);
				}
			}
		}

		final CategoryAxis xAxis = new CategoryAxis(Messages.ProjectAction_BuildAxis());
		xAxis.setLowerMargin(0.01);
		xAxis.setUpperMargin(0.01);
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		xAxis.setMaximumCategoryLabelLines(3);

		final ValueAxis timeAxis = new NumberAxis(Messages.ProjectAction_TimeAxis());
		timeAxis.setUpperMargin(0.1);
		// final ValueAxis minmaxTimeAxis = new NumberAxis("Time (ms)");
		final BarRenderer timeRenderer = new StatisticalBarRenderer();
		timeRenderer.setSeriesPaint(2, ColorPalette.RED);
		timeRenderer.setSeriesPaint(1, ColorPalette.YELLOW);
		timeRenderer.setSeriesPaint(0, ColorPalette.BLUE);
		timeRenderer.setItemMargin(0.0);

		final CategoryPlot plot = new CategoryPlot(timeDS, xAxis, timeAxis, timeRenderer);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		final CategoryItemRenderer minmaxRenderer = new LineAndShapeRenderer();
		// plot.setRangeAxis(1, timeAxis);
		plot.setDataset(1, minmaxDS.build());
		plot.mapDatasetToRangeAxis(1, 0);
		plot.setRenderer(1, minmaxRenderer);

		JFreeChart chart = new JFreeChart(params.getLabel(), plot);
		chart.setBackgroundPaint(Color.WHITE);

		return chart;
	}

	private JFreeChart createActionErrorGraph(ClifGraphParam params) {
		DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> errorsDS =
			new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();

		List<AbstractBuild> builds = new ArrayList<AbstractBuild>(getProject().getBuilds());
		Collections.sort(builds);
		for (Run<?,?> currentBuild : builds)
		{
			Result buildResult = currentBuild.getResult();
			if (buildResult != null	&& buildResult.isBetterOrEqualTo(Result.SUCCESS))
			{
				ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(currentBuild);
				ClifBuildAction clifBuildAction = currentBuild.getAction(ClifBuildAction.class);
				if (clifBuildAction == null) {
					continue;
				}
				ClifReport clifReport = clifBuildAction.getReport();
				if (clifReport == null) {
					continue;
				}
				TestPlan tp = clifReport.getTestplan(params.getTestPlan());
				if (tp == null) {
					continue;
				}
				if (tp.getAggregatedMeasures() != null) {
					Measure m = tp.getAggregatedMeasure(params.getLabel());
					if (m == null) {
						continue;
					}
					errorsDS.add(m.errorPercent() * 100, Messages.ProjectAction_Errors(), label);
				}
			}
		}

		final CategoryAxis xAxis = new CategoryAxis(Messages.ProjectAction_BuildAxis());
		xAxis.setLowerMargin(0.01);
		xAxis.setUpperMargin(0.01);
		xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
		xAxis.setMaximumCategoryLabelLines(3);

		final ValueAxis errorsAxis = new NumberAxis(Messages.ProjectAction_ErrorAxis());
		errorsAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		errorsAxis.setUpperMargin(0.1);

		final LineAndShapeRenderer errorRenderer = new LineAndShapeRenderer();
		errorRenderer.setItemMargin(0.0);

		final CategoryPlot plot = new CategoryPlot(errorsDS.build(), xAxis, errorsAxis, errorRenderer);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlinePaint(null);
		plot.setForegroundAlpha(0.8f);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);

		JFreeChart chart = new JFreeChart(Messages.ProjectAction_PercentageOfErrors(), plot);
		chart.setBackgroundPaint(Color.WHITE);

		return chart;
	}

	private boolean shouldReloadGraph(StaplerRequest request, StaplerResponse response) {
		return true; // shouldReloadGraph(request, response,
		// project.getLastSuccessfulBuild());
	}
}
