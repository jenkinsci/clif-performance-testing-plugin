/*
 * CLIF is a Load Injection Framework
 * Copyright (C) 2012 France Telecom R&D
 * Copyright (C) 2026 Orange SA
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
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.localizer.LocaleProvider;
import hudson.model.Hudson;
import hudson.Util;
import hudson.util.FormValidation;
import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Checking a variety of valid and invalid CLIF installations
 * @author Bruno Dillenseger
 */
@WithJenkins
class ClifInstallationTest {

	private static final File GOOD_INSTALLATION = new File("target/test-classes/goodProActiveInstallation");
	private static final File GOOD_CREDENTIALS_FILE = new File(
			"target/test-classes/goodProActiveInstallation/credentialsFile.cred");
	private static final String SAMPLE_SCHEDULER_URL = "http://localhost:2345/rest";
	private ClifInstallation.DescriptorImpl desc;
	private JenkinsRule j;

	@BeforeEach
	void setUp(JenkinsRule rule) {
		j = rule;
		desc = new ClifInstallation.DescriptorImpl();
		LocaleProvider.setProvider(
				new LocaleProvider()
				{
					@Override
					public Locale get()
					{
						return Locale.getDefault();
					}
				});
	}

	@Test
	void testDoCheckInstallationGoodInstall() {
		assertNotNull(Hudson.getInstanceOrNull(), "The Jenkins instance should not be null");
		doCheckInstallation(
				GOOD_INSTALLATION,
				SAMPLE_SCHEDULER_URL,
				GOOD_CREDENTIALS_FILE,
				OK,
				Messages.ClifInstallation_ProactiveInstallationValid());
	}

	@Test
	void testDoCheckInstallationBadHome() {
		File home = new File("");
		doCheckInstallation(
				home,
				SAMPLE_SCHEDULER_URL,
				GOOD_CREDENTIALS_FILE,
				ERROR,
				Messages.Clif_HomeRequired());

		home = new File("target/test-classes/org/ow2/clif/jenkins/ClifInstallationTest.class");
		doCheckInstallation(
				home,
				SAMPLE_SCHEDULER_URL,
				GOOD_CREDENTIALS_FILE,
				ERROR,
				Messages.Clif_NotADirectory(home));

		home = new File("target/test-classes/badClifInstallation");
		doCheckInstallation(
				home,
				SAMPLE_SCHEDULER_URL,
				GOOD_CREDENTIALS_FILE,
				ERROR,
				Messages.Clif_NotClifDirectory(home));

		home = new File("target/test-classes/badProActiveInstallation");
		doCheckInstallation(
				home,
				SAMPLE_SCHEDULER_URL,
				GOOD_CREDENTIALS_FILE,
				ERROR,
				Messages.ClifInstallation_BadProactiveInstallation());
	}

	@Test
	void testDoCheckInstallationBadURL() {
		doCheckInstallation(
				GOOD_INSTALLATION,
				null,
				GOOD_CREDENTIALS_FILE,
				ERROR,
				Messages.ClifInstallation_SchedulerURLMissing());

		doCheckInstallation(
				GOOD_INSTALLATION,
				" ",
				GOOD_CREDENTIALS_FILE,
				ERROR,
				Messages.ClifInstallation_SchedulerURLMissing());
	}

	@Test
	void testDoCheckInstallationBadCredentialsFile() {
		File schedulerCredentialsFile = new File("");
		doCheckInstallation(
				GOOD_INSTALLATION,
				SAMPLE_SCHEDULER_URL,
				schedulerCredentialsFile,
				ERROR,
				Messages.ClifInstallation_CredentialsMissing());

		schedulerCredentialsFile = new File("target/test-classes/unknownFile");
		doCheckInstallation(
				GOOD_INSTALLATION,
				SAMPLE_SCHEDULER_URL,
				schedulerCredentialsFile,
				ERROR,
				Messages.ClifInstallation_CredentialsFileNotFound());
	}

	private void doCheckInstallation(
			final File home,
			final String schedulerURL,
			final File schedulerCredentialsFile,
			final FormValidation.Kind expectedKind,
			final String expectedMessage) {
		final FormValidation res = desc.doCheckInstallation(home, schedulerURL, schedulerCredentialsFile, null, null);
		assertEquals(Util.escape(expectedMessage), res.getMessage());
		assertEquals(expectedKind, res.kind);
	}
}
