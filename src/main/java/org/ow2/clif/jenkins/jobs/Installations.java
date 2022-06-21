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

import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;

/**
 * boilerplate
 */
public class Installations {

	public Installations() {
	}

	public <I extends ToolInstallation> I[]
	all(Class<? extends ToolDescriptor<I>> descriptor) {
		return Jenkins.get().getDescriptorByType(descriptor).getInstallations();
	}

	public <I extends ToolInstallation> I
	first(Class<? extends ToolDescriptor<I>> descriptor, String name) {
		for (I installation : all(descriptor)) {
			if (name == null || name.equals(installation.getName())) {
				return installation;
			}
		}
		return null;
	}

	public <I extends ToolInstallation> I
	first(Class<? extends ToolDescriptor<I>> descriptor) {
		return first(descriptor, null);
	}

	public <I extends ToolInstallation> String
	firstName(Class<? extends ToolDescriptor<I>> descriptor) {
		I install = first(descriptor, null);
		return install == null ? null : install.getName();
	}

}
