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

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifPublisher;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ConfigurerTest {

	private Installations installations;
	private Configurer configurer;
	private FreeStyleProject project;
	private File dir;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		installations = mock(Installations.class);
		configurer = new Configurer();
		configurer.installations = installations;

		project = mock(FreeStyleProject.class);
		DescribableList<Builder, Descriptor<Builder>> builders =
				mock(DescribableList.class);
		when(project.getBuildersList()).thenReturn(builders);

		DescribableList<Publisher, Descriptor<Publisher>> publishers =
				mock(DescribableList.class);
		when(project.getPublishersList()).thenReturn(publishers);

		dir = new File("target/workspaces");
	}

	@Test
	public void configureAddsOneClifBuilderToProjectBuilderList()
			throws Exception {
		configurer.configure(project, dir, "nowhere/noTestPlan.ctp");
		verify(project.getBuildersList()).add(any(ClifBuilder.class));
	}

	@Test
	public void configurePublisher() throws Exception {
		configurer.configure(project, dir, "nowhere/noTestPlan.ctp");
		verify(project.getPublishersList()).add(any(ClifPublisher.class));
	}

	@Test
	public void configurePrivateWorkspace() throws Exception {
		configurer.configure(project, dir, "examples/http.ctp");
		verify(project).setCustomWorkspace(new File("target/workspaces/examples").getPath());

	}

	@Test
	public void newClifBuilderHasTestPlan() throws Exception {
		ClifBuilder builder = configurer.newClifBuilder("http.ctp");
		assertThat(builder.getTestPlanFile()).isEqualTo("http.ctp");
	}

	@Test
	public void newClifBuilderHasReportDir() throws Exception {
		ClifBuilder builder = configurer.newClifBuilder("http.ctp");
		assertThat(builder.getReportDir()).isEqualTo("report");
	}

	@Test
	public void newClifPublisherHasReportDirectory() throws Exception {
		ClifPublisher publisher = configurer.newClifPublisher();
		assertThat(publisher.getClifReportDirectory())
				.isEqualTo("report");
	}

}