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
package org.ow2.clif.jenkins.chart;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * @author Julien Coste
 */
public abstract class AbstractChart {
	protected final ChartId chartId;

	protected final ChartConfiguration chartConfiguration;

	public AbstractChart(String chartType, String bladeId, String testplan, String event,
	                     ChartConfiguration chartConfiguration) {
		this.chartId = new ChartId(chartType, testplan, bladeId, event);
		this.chartConfiguration = chartConfiguration;
	}

	protected String getBasicTitle() {
		return this.chartId.getTestplan() + " - " + this.chartId.getBladeId() + " - " + this.chartId.getEvent();
	}

	private void saveImageFile(File imageFile, BufferedImage bImage) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(imageFile);
			ImageIO.write(bImage, "png", os);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			IOUtils.closeQuietly(os);
		}
	}

	private File getImageFile(File rootDir) {
		File clifImageDir = new File(rootDir, "clif");
		if (clifImageDir.exists()) {
			if (!clifImageDir.isDirectory()) {
				throw new IllegalStateException(clifImageDir.getAbsolutePath() + " is not a directory");
			}
			if (!clifImageDir.canWrite()) {
				throw new IllegalStateException(clifImageDir.getAbsolutePath() + " is not writable");
			}
		}
		else {
			if (!clifImageDir.mkdirs()) {
				throw new IllegalStateException(
						"Impossible to create directory " + clifImageDir.getAbsolutePath());
			}
		}

		return new File(clifImageDir, this.chartId.getId() + ".png");
	}


	public void doPng(File rootDir, StaplerRequest req, StaplerResponse rsp)
			throws IOException {

		File imageFile = getImageFile(rootDir);

		try {
			BufferedImage bufferedImage = ImageIO.read(imageFile);
			rsp.setContentType("image/png");
			ServletOutputStream os = rsp.getOutputStream();
			ImageIO.write(bufferedImage, "PNG", os);
			os.close();
		}
		catch (Error e) {
			/* OpenJDK on ARM produces an error like this in case of headless error
							Caused by: java.lang.Error: Probable fatal error:No fonts found.
									at sun.font.FontManager.getDefaultPhysicalFont(FontManager.java:1088)
									at sun.font.FontManager.initialiseDeferredFont(FontManager.java:967)
									at sun.font.CompositeFont.doDeferredInitialisation(CompositeFont.java:254)
									at sun.font.CompositeFont.getSlotFont(CompositeFont.java:334)
									at sun.font.CompositeStrike.getStrikeForSlot(CompositeStrike.java:77)
									at sun.font.CompositeStrike.getFontMetrics(CompositeStrike.java:93)
									at sun.font.Font2D.getFontMetrics(Font2D.java:387)
									at java.awt.Font.defaultLineMetrics(Font.java:2082)
									at java.awt.Font.getLineMetrics(Font.java:2152)
									at org.jfree.chart.axis.NumberAxis.estimateMaximumTickLabelHeight(NumberAxis.java:974)
									at org.jfree.chart.axis.NumberAxis.selectVerticalAutoTickUnit(NumberAxis.java:1104)
									at org.jfree.chart.axis.NumberAxis.selectAutoTickUnit(NumberAxis.java:1048)
									at org.jfree.chart.axis.NumberAxis.refreshTicksVertical(NumberAxis.java:1249)
									at org.jfree.chart.axis.NumberAxis.refreshTicks(NumberAxis.java:1149)
									at org.jfree.chart.axis.ValueAxis.reserveSpace(ValueAxis.java:788)
									at org.jfree.chart.plot.CategoryPlot.calculateRangeAxisSpace(CategoryPlot.java:2650)
									at org.jfree.chart.plot.CategoryPlot.calculateAxisSpace(CategoryPlot.java:2669)
									at org.jfree.chart.plot.CategoryPlot.draw(CategoryPlot.java:2716)
									at org.jfree.chart.JFreeChart.draw(JFreeChart.java:1222)
									at org.jfree.chart.JFreeChart.createBufferedImage(JFreeChart.java:1396)
									at org.jfree.chart.JFreeChart.createBufferedImage(JFreeChart.java:1376)
									at org.jfree.chart.JFreeChart.createBufferedImage(JFreeChart.java:1361)
									at hudson.util.ChartUtil.generateGraph(ChartUtil.java:116)
									at hudson.util.ChartUtil.generateGraph(ChartUtil.java:99)
									at hudson.tasks.test.AbstractTestResultAction.doPng(AbstractTestResultAction.java:196)
									at hudson.tasks.test.TestResultProjectAction.doTrend(TestResultProjectAction.java:97)
									... 37 more
						 */
			if (e.getMessage().contains("Probable fatal error:No fonts found")) {
				rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
				return;
			}
			throw e; // otherwise let the caller deal with it
		}
		catch (HeadlessException e) {
			// not available. send out error message
			rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
		}

	}

	public void createChart(File rootDir) {
		JFreeChart chart = createChart();

		File imageFile = getImageFile(rootDir);
		BufferedImage bImage = chart.createBufferedImage(this.chartConfiguration.getChartWidth(),
		                                                 this.chartConfiguration.getChartHeight());
		saveImageFile(imageFile, bImage);

	}

	protected abstract JFreeChart createChart();
}
