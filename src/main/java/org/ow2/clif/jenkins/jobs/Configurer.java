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
import java.io.IOException;
import javax.annotation.Nonnull;
import org.ow2.clif.jenkins.ClifBuilder;
import org.ow2.clif.jenkins.ClifInstallation;
import org.ow2.clif.jenkins.ClifJobProperty;
import org.ow2.clif.jenkins.ClifPublisher;
import hudson.model.FreeStyleProject;

public class Configurer {
	Installations installations = new Installations();
	private String version;

	public FreeStyleProject	configure(FreeStyleProject job, File dir, @Nonnull String plan)
	throws IOException
	{
		String directory = dir.getPath();
		String p = null;
		String[] strings = plan.split("/");
		directory += File.separator + strings[0];
		p = strings[1];
		job.getBuildersList().add(newClifBuilder(p));
		job.getPublishersList().add(newClifPublisher());
		job.setCustomWorkspace(directory);
		job.addProperty(new ClifJobProperty(true));
		return job;
	}

	ClifPublisher newClifPublisher() {
		return new ClifPublisher("report");
	}

	@Nonnull
	ClifBuilder newClifBuilder(String plan) {
		String clifName = version == null
				? installations.firstName(ClifInstallation.DescriptorImpl.class)
				: version;
		ClifBuilder builder = new ClifBuilder(clifName, null, plan, "report");
		return builder;
	}

	public void use(String version) {
		// FIXME use(ClifInstallation) could be a better prototype
		// would require work in ClifBuilder though...
		this.version = version;
	}

}