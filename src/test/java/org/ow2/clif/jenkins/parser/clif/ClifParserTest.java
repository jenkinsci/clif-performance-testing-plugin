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
package org.ow2.clif.jenkins.parser.clif;

import java.io.File;
import java.util.Date;
import org.junit.Test;
import org.ow2.clif.jenkins.chart.ChartConfiguration;
import org.ow2.clif.jenkins.model.ClifReport;
import org.ow2.clif.jenkins.model.TestPlan;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: bvjr5731
 * Date: 27/03/12
 * Time: 16:52
 * To change this template use File | Settings | File Templates.
 */
public class ClifParserTest {
	@Test
	public void testParse() throws Exception {

		long start = -System.currentTimeMillis();
		File reportDir = new File("src/test/resources/reports");
		File buildDir = new File("target/clif");
		ClifParser parser = new ClifParser(reportDir.getAbsolutePath(), buildDir.getAbsoluteFile());

		ChartConfiguration chartConfig = new ChartConfiguration(600, 1200, 15, 50, 2);
		parser.setChartConfiguration(chartConfig);
		parser.enableDataCleanup(2, 95);
		parser.addActionAliasPattern("random", ".*dummy.*");

		parser.setGenerateCharts(false);
		// Parse Clif report directory
		ClifReport report = parser.parse(System.out);

		assertNotNull(report);
		assertThat(report.getTestplans()).isNotEmpty().containsExactly(new TestPlan("random", new Date()));

		TestPlan testPlanRead = report.getTestplan("random");
		assertThat(testPlanRead.getAggregatedMeasures()).hasSize(1);
		assertThat(testPlanRead.getAlarms()).isNullOrEmpty();
		assertThat(testPlanRead.getInjectors()).hasSize(1);
		assertThat(testPlanRead.getProbes()).isNullOrEmpty();
		assertThat(testPlanRead.getServers()).hasSize(1);

		start += System.currentTimeMillis();
		System.out.println(start);
	}
}
