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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerResponse;
import org.ow2.clif.jenkins.jobs.Configurer;
import org.ow2.clif.jenkins.jobs.FakeConfigurer;
import org.ow2.clif.jenkins.jobs.Installations;
import org.ow2.clif.jenkins.jobs.Zip;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import jenkins.model.Fake;
import jenkins.model.Jenkins;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreviewZipActionTest {
	private Jenkins jenkins;
	private PreviewZipAction preview;
	private Zip zip;
	private Configurer configurer;
	private ImportZipAction parent;

	@Before
	public void setUp() throws Exception {
		jenkins = Fake.install();
		configurer = new FakeConfigurer();

		zip = mock(Zip.class);
		parent = new ImportZipAction();
		preview = new PreviewZipAction(zip, null);
		preview.parent = parent;
		preview.clif = configurer;
		preview.jenkins = jenkins;
		preview.installations = mock(Installations.class);
	}

	@After
	public void reset() {
		Fake.uninstall();
	}

	List<Item> jobs(String... names) {
		List<Item> jobs = new ArrayList<>();
		for (String name : names) {
			FreeStyleProject job = job(name);
			jobs.add(job);
		}
		when(jenkins.getAllItems()).thenReturn(jobs);
		return jobs;
	}

	FreeStyleProject job(String name) {
		FreeStyleProject job = mock(FreeStyleProject.class);

		when(job.getName()).thenReturn(name);
		when(jenkins.getItem(name)).thenReturn(job);
		return job;
	}

	@Test
	public void doesNotListNestedTestPlanInZip() throws Exception {
		assertThat(preview.pattern, equalTo("([^/]*)/([^/]*)\\.ctp"));
	}

	@Test
	public void jobNameIsDasherizedFileNameWithoutExtension() throws Exception {
		FreeStyleProject project = preview.create("red/tomato.erl");
		assertThat(project.getName(), equalTo("red-tomato"));
	}

	@Test
	public void installedJobReplacesPreviousOne() throws Exception {
		FreeStyleProject project = preview.create("red/tomato.erl");
		verify(jenkins).putItem(project);
	}

	@Test
	public void uninstalledJobIsDeleted() throws Exception {
		FreeStyleProject project = job("red-tomato");

		preview.delete("red/tomato.erl");

		verify(project).delete();
	}

	@Test
	public void redirectsToPreview() throws Exception {
		when(zip.id()).thenReturn("123");
		StaplerResponse response = mock(StaplerResponse.class);

		preview.process(response);

		verify(response).sendRedirect2("previews/123");
		assertThat(parent.getPreviews(preview.id()), equalTo(preview));
	}

	@Test
	public void diffingZipAgainstJobs() throws Exception {
		jobs("examples-dummy", "examples-synchro", "rebar");
		when(zip.entries(anyString())).thenReturn(
				Arrays.asList("examples/dummy.ctp", "examples/ftp.ctp")
		);
		when(zip.basedir()).thenReturn("examples");

		preview.diff();

		assertThat(preview.installs, contains("examples/ftp.ctp"));
		assertThat(preview.uninstalls, contains("examples/synchro.ctp"));
		assertThat(preview.upgrades, contains("examples/dummy.ctp"));
	}

}
