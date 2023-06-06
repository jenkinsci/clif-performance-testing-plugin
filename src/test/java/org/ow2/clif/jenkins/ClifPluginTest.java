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

import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class ClifPluginTest extends HudsonTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
/* En commentaire tant que Clif-core embarque un Xalan 2.5.1
		hudson.setSecurityRealm(new HudsonPrivateSecurityRealm(true));
		webClient = createWebClient();
*/
	}

	@Test
	public void testGetClifRootDirAbsolutePath() throws Exception {
		final ClifPlugin clifPlugin = ClifPlugin.get();
		try {
			// create a unique name, then delete the empty file - will be recreated later
			final File root = File.createTempFile("clifPlugin.test_abs_path", null);
			final String absolutePath = root.getPath();
			root.delete();


/* En commentaire tant que Clif-core embarque un Xalan 2.5.1

			final HtmlForm form = webClient.goTo("configure").getFormByName("config");
			form.getInputByName("clifRootDir").setValue(absolutePath);
			submit(form);

*/
			// En attendant, on change le test
			clifPlugin.setClifRootDir(absolutePath);
			assertEquals("Verify clif root configured at absolute path.", root, clifPlugin.dir());

			root.delete();
			// not really needed, but helpful so we don't clutter the test host with unnecessary files
			assertFalse("Verify cleanup of history files: " + root, root.exists());

		}
		catch (Exception e) {
			fail("Unable to complete clif root absolute path test: " + e);
		}
	}

	@Test
	public void testGetClifRootDirRelativePath() throws Exception {
		final ClifPlugin clifPlugin = ClifPlugin.get();
		try {
			final String relativePath = "clifPlugin.test_rel_path";
			final File root = new File(hudson.root.getPath() + File.separator + relativePath);
			root.delete();

/* En commentaire tant que Clif-core embarque un Xalan 2.5.1

			final HtmlForm form = webClient.goTo("configure").getFormByName("config");
			form.getInputByName("clifRootDir").setValue(relativePath);
			submit(form);

*/
			// En attendant, on change le test
			clifPlugin.setClifRootDir(relativePath);
			assertEquals("Verify clif root configured at relative path.", root, clifPlugin.dir());

		}
		catch (Exception e) {
			fail("Unable to complete clif root absolute path test: " + e);
		}
	}

	@Test
	public void testGetClifRootDefaults() {
		final ClifPlugin clifPlugin = ClifPlugin.get();

		Assert.assertNotNull("Bad default clif root dir", clifPlugin.getClifRootDir());
		Assert.assertEquals("Bad default clif root dir", "clif", clifPlugin.getClifRootDir());
	}
}
