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
package org.ow2.clif.jenkins.jobs;

import hudson.model.FreeStyleProject;
import java.io.File;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPublisher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

public class ConfigurerTest {

	@Rule public JenkinsRule j = new JenkinsRule();

	private Configurer configurer;
	private File dir;

	@Before
	public void setUp() {
		configurer = new Configurer();
		dir = new File("target/workspaces");
	}

	@Test
	public void configureAddsOneClifBuilderToProjectBuilderList() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		configurer.configure(project, dir, "nowhere/noTestPlan.ctp");
		assertThat(project.getBuildersList(), hasSize(1));
		assertThat(project.getBuildersList().get(0), instanceOf(ClifBuilder.class));
	}

	@Test
	public void configurePublisher() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		configurer.configure(project, dir, "nowhere/noTestPlan.ctp");
		assertThat(project.getBuildersList(), hasSize(1));
		assertThat(project.getBuildersList().get(0), instanceOf(ClifBuilder.class));
	}

	@Test
	public void configurePrivateWorkspace() throws Exception {
		FreeStyleProject project = j.createFreeStyleProject();
		configurer.configure(project, dir, "examples/http.ctp");
		assertEquals("target" + File.separator + "workspaces" + File.separator + "examples", project.getCustomWorkspace());
	}

	@Test
	public void newClifBuilderHasTestPlan() {
		ClifBuilder builder = configurer.newClifBuilder("http.ctp");
		assertEquals("http.ctp", builder.getTestPlanFile());
	}

	@Test
	public void newClifBuilderHasReportDir() {
		ClifBuilder builder = configurer.newClifBuilder("http.ctp");
		assertEquals("report", builder.getReportDir());
	}

	@Test
	public void newClifPublisherHasReportDirectory() {
		ClifPublisher publisher = configurer.newClifPublisher();
		assertEquals("report", publisher.getClifReportDirectory());
	}

}
