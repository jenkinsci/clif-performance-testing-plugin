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

import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import jenkins.model.Jenkins;
import static org.apache.commons.io.FilenameUtils.removeExtension;

public class Jobs {

	public static String toJob(String plan) {
		return removeExtension(plan.replace('/', '-'));
	}

	public static String toPlan(String job) {
		return job.replace("-", "/") + ".ctp";
	}

	public static FreeStyleProject
	newJob(ItemGroup<? extends Item> jenkins, String name) {
		FreeStyleProject project = new FreeStyleProject(
				jenkins,
				name
		);
		return project;
	}

	public static FreeStyleProject newJob(String name) {
		return newJob(Jenkins.getInstance(), name);
	}

}
