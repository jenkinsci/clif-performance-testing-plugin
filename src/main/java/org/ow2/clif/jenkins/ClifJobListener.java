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

import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.ow2.clif.jenkins.jobs.FileSystem;
import hudson.Extension;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.listeners.ItemListener;

/**
 * Delete all reports measures attached to a deleted job if it's a Clif Job.
 *
 * @author jcoste
 */
@Extension
public class ClifJobListener extends ItemListener {
	private static final Logger LOG = Logger.getLogger(ClifJobListener.class.getName());

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDeleted(Item item) {
		LOG.finest("In onDeleted for " + item);
		if (item instanceof FreeStyleProject) {
			FreeStyleProject project = (FreeStyleProject) item;
			ClifBuilder clifBuilder = project.getBuildersList().get(ClifBuilder.class);
			if (clifBuilder != null) {
				// It's a Clif job
				ClifJobProperty clifJobProperty = project.getProperty(ClifJobProperty.class);

				if (clifJobProperty == null || clifJobProperty.isDeleteReport()) {
					FileSystem fs = new FileSystem(project.getCustomWorkspace());
					String glob = buildGlobForDeletion(clifBuilder);
					LOG.info(
							"Deleting report measures under " + project.getCustomWorkspace() + " with pattern " + glob);
					fs.rm_rf(glob);
				}
			}
		}
		LOG.finest("onDeleted for " + item + " done.");
	}

	protected static String buildGlobForDeletion(ClifBuilder clifBuilder) {
		return clifBuilder.getReportDir() + "/" + FilenameUtils.removeExtension(clifBuilder.getTestPlanFile()) + "_*";
	}
}
