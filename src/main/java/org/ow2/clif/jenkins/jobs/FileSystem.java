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
import org.apache.tools.ant.DirectoryScanner;

public class FileSystem {
	private final String dir;

	public FileSystem(String dir) {
		this.dir = dir;
	}

	/**
	 * Deletes sub-directories and files matching a ant-compliant pattern
	 * from the directory given in the constructor {@link #FileSystem(String)}
	 *
	 * @param glob file/directory inclusion pattern
	 */
	public void rm_rf(String glob) {
		DirectoryScanner scanner = scan(glob);
		for (String f : scanner.getIncludedDirectories()) {
			_rm_f(f);
		}
		for (String f : scanner.getIncludedFiles()) {
			_rm_f(f);
		}
	}


	public void rm(String glob) {
		for (String f : scan(glob).getIncludedFiles()) {
			_rm_f(f);
		}
	}


	private void _rm_f(String file) {
		FileUtils.deleteQuietly(new File(dir + "/" + file));
	}

	private DirectoryScanner scan(String glob) {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(dir());
		scanner.setIncludes(new String[]{glob});
		scanner.scan();
		return scanner;
	}

	public String dir() {
		return dir;
	}
}