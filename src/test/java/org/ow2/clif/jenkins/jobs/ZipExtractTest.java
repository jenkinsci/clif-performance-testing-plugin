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
package org.ow2.clif.jenkins.jobs;


import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class ZipExtractTest {
	private Zip zip;
	private String path;
	private File workspaces;

	@Before
	public void setUp() throws Exception {
		path = "target/workspaces";
		workspaces = new File(path);
		FileUtils.forceMkdir(workspaces);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(workspaces);
	}

	@Test
	public void extractsToDirectoryDeflatesAllZip() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		zip.extractTo(path);
		assertThat(new File(workspaces + "/samples")).isDirectory();
		assertThat(new File(workspaces + "/samples/post.ctp")).isFile();
	}
}