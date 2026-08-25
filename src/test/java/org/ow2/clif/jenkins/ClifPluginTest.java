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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WithJenkins
class ClifPluginTest {

	private JenkinsRule j;

	@BeforeEach
	void setUp(JenkinsRule rule) {
		j = rule;
/* disabled as long as clif-core embeds Xalan 2.5.1
		hudson.setSecurityRealm(new HudsonPrivateSecurityRealm(true));
		webClient = createWebClient();
*/
	}

	@Test
	void testGetClifRootDirAbsolutePath() throws Exception {
		final ClifPlugin clifPlugin = ClifPlugin.get();
		// create a unique name, then delete the empty file - will be recreated later
		final File root = File.createTempFile("clifPlugin.test_abs_path", null);
		final String absolutePath = root.getPath();
		root.delete();

/* disabled as long as clif-core embeds Xalan 2.5.1

		final HtmlForm form = webClient.goTo("configure").getFormByName("config");
		form.getInputByName("clifRootDir").setValueAttribute(absolutePath);
		submit(form);

*/
		// In the meanwhile, the test is changed
		clifPlugin.setClifRootDir(absolutePath);
		assertEquals(root, clifPlugin.dir(), "Verify clif root configured at absolute path.");

		root.delete();
		// not really needed, but helpful so we don't clutter the test host with unnecessary files
		assertFalse(root.exists(), "Verify cleanup of history files: " + root);
	}

	@Test
	void testGetClifRootDirRelativePath() {
		final ClifPlugin clifPlugin = ClifPlugin.get();
		final String relativePath = "clifPlugin.test_rel_path";
		final File root = new File(j.jenkins.root.getPath() + File.separator + relativePath);
		root.delete();

/* disabled as long as clif-core embeds Xalan 2.5.1

		final HtmlForm form = webClient.goTo("configure").getFormByName("config");
		form.getInputByName("clifRootDir").setValueAttribute(relativePath);
		submit(form);

*/
		// In the meanwhile, the test is changed
		clifPlugin.setClifRootDir(relativePath);
		assertEquals(root, clifPlugin.dir(), "Verify Clif root is configured as a relative path.");
	}

	@Test
	void testGetClifRootDefaults() {
		final ClifPlugin clifPlugin = ClifPlugin.get();

		assertNotNull(clifPlugin.getClifRootDir(), "Bad default clif root dir");
		assertEquals("clif", clifPlugin.getClifRootDir(), "Bad default clif root dir");
	}
}
