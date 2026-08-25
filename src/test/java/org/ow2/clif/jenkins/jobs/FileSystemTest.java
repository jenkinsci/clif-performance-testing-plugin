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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemTest {

	private String path;
	private File workspaces;
	private FileSystem fs;

	@BeforeEach
	void setUp() throws Exception {
		path = "target/workspaces";
		workspaces = new File(path);
		FileUtils.forceMkdir(workspaces);
		fs = new FileSystem(path);
	}

	@AfterEach
	void tearDown() throws Exception {
		FileUtils.deleteDirectory(workspaces);
	}

	private File mkdir(String p) throws Exception {
		File f = new File(fs.dir() + "/" + p);
		FileUtils.forceMkdir(f);
		return f;
	}

	private File touch(String p) throws Exception {
		File f = new File(fs.dir() + "/" + p);
		FileUtils.touch(f);
		return f;
	}

	@Test
	void removesDir() throws Exception {
		File f = mkdir("report/synchro_2012-04-11_10h53m40");
		File monster = touch("report/synchro_2012-04-11_10h53m40.ctp");
		touch("synchro.ctp");
		File elvis = mkdir("report/elvis_2012-04-10_17h58m38");

		fs.rm_rf("**/synchro*");

		assertFalse(f.exists());
		assertFalse(monster.exists());
		assertTrue(elvis.exists());
	}
}