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
package org.ow2.clif.jenkins;

import java.io.File;
import java.util.Locale;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.localizer.LocaleProvider;
import hudson.Util;
import hudson.util.FormValidation;
import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static org.fest.assertions.Assertions.assertThat;


/**
 * Checking a variety of valid and invalid CLIF installations
 * @author Bruno Dillenseger
 */
public class ClifInstallationTest extends HudsonTestCase
{
	static final private File goodInstallation =
		new File("target/test-classes/goodProActiveInstallation");
	static final private File goodCredentialsFile =
		new File("target/test-classes/goodProActiveInstallation/credentialsFile.cred");
	static final private String sampleSchedulerUrl = "http://localhost:2345/rest";
	private ClifInstallation.DescriptorImpl desc;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
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
	public void testDoCheckInstallationGoodInstall()
	{
		doCheckInstallation(
			goodInstallation,
			sampleSchedulerUrl,
			goodCredentialsFile,
			OK,
			Messages.ClifInstallation_ProactiveInstallationValid());
	}

	@Test
	public void testDoCheckInstallationBadHome()
	{
		File home = new File("");
		doCheckInstallation(
			home,
			sampleSchedulerUrl,
			goodCredentialsFile,
			ERROR,
			Messages.Clif_HomeRequired());

		home = new File("target/test-classes/org/ow2/clif/jenkins/ClifInstallationTest.class");
		doCheckInstallation(
			home,
			sampleSchedulerUrl,
			goodCredentialsFile,
			ERROR,
			Messages.Clif_NotADirectory(home));

		home = new File("target/test-classes/badClifInstallation");
		doCheckInstallation(
			home,
			sampleSchedulerUrl,
			goodCredentialsFile,
			ERROR,
			Messages.Clif_NotClifDirectory(home));

		home = new File("target/test-classes/badProActiveInstallation");
		doCheckInstallation(
			home,
			sampleSchedulerUrl,
			goodCredentialsFile,
			ERROR,
			Messages.ClifInstallation_BadProactiveInstallation());
	}

	@Test
	public void testDoCheckInstallationBadURL()
	{
		doCheckInstallation(
			goodInstallation,
			null,
			goodCredentialsFile,
			ERROR,
			Messages.ClifInstallation_SchedulerURLMissing());

		doCheckInstallation(
			goodInstallation,
			" ",
			goodCredentialsFile,
			ERROR,
			Messages.ClifInstallation_SchedulerURLMissing());
	}

	@Test
	public void testDoCheckInstallationBadCredentialsFile()
	{
		File schedulerCredentialsFile = new File("");
		doCheckInstallation(
			goodInstallation,
			sampleSchedulerUrl,
			schedulerCredentialsFile,
			ERROR,
			Messages.ClifInstallation_CredentialsMissing());

		schedulerCredentialsFile = new File("target/test-classes/unknownFile");
		doCheckInstallation(
			goodInstallation,
			sampleSchedulerUrl,
			schedulerCredentialsFile,
			ERROR,
			Messages.ClifInstallation_CredentialsFileNotFound());
	}

	private void doCheckInstallation(
		final File home,
		final String schedulerURL,
		final File schedulerCredentialsFile,
		final FormValidation.Kind expectedKind,
		final String expectedMessage)
	{
		final FormValidation res = desc.doCheckInstallation(home, schedulerURL, schedulerCredentialsFile, null, null);
		assertThat(res.getMessage()).isEqualTo(Util.escape(expectedMessage));
		assertThat(res.kind).isEqualTo(expectedKind);
	}
}
