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
import org.apache.tools.ant.types.ZipScanner;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class ZipTest {
	private Zip zip;

	@Test
	public void namesAreZipEntriesFileName() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.entries(".*"))
				.containsExactly("foo.rb", "py.py", "get.rb");

		assertThat(zip.entries(".*")).isEqualTo(zip.entries(null));
		assertThat(zip.entries()).isEqualTo(zip.entries(null));
	}

	@Test
	public void namesCanBeFiltered() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		// OH! "foo.rb" does not match /rb$/
		// it does in rb, erl, js
		assertThat(zip.entries(".*rb$"))
				.containsExactly("foo.rb", "get.rb");
	}

	@Test
	public void namesAreIdempotent() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.entries(".*rb$")).isEqualTo(zip.entries(".*rb$"));
	}

	@Test
	public void namesAreRelativePathFromZip() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.entries("(.*)\\.coffee$"))
				.containsExactly("samples/http/brute.coffee");
	}

	@Test
	public void nestedTestPlanCanBeFilteredUsingCleverRegularExpression()
			throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.entries("(.*)\\.ctp"))
				.containsExactly("samples/http/brute.ctp", "samples/post.ctp");
		assertThat(zip.entries("([^/]*)/([^/]*)\\.ctp"))
				.containsExactly("samples/post.ctp");
	}

	@Test
	public void basedirIsFirstEntryWhenDirectory() throws Exception {
		zip = new Zip("src/test/resources/zips/nested.zip");
		assertThat(zip.basedir()).isEqualTo("samples");
	}

	@Test
	public void basedirIsFirstEntryLeadingDirectoryWhenFile() throws Exception {
		zip = new Zip("src/test/resources/zips/clif-examples-1.zip");
		assertThat(zip.basedir()).isEqualTo("examples");
	}

	@Test
	public void dirIsEmptyOtherwise() throws Exception {
		zip = new Zip("src/test/resources/zips/sources.zip");
		assertThat(zip.basedir()).isEmpty();
	}

	@Test
	public void antLearningTest() throws Exception {
		ZipScanner zip = new ZipScanner();
		zip.setSrc(new File("src/test/resources/zips/clif-examples-1.zip"));

		String[] files = zip.getIncludedFiles();
		assertThat(files).contains("examples/dummy.ctp");
	}
}
