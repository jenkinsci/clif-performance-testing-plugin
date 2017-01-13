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

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Action;
import hudson.model.Run;

/**
 * Abstract class with functionality common to all Clif actions.
 *
 * @author Julien Coste
 */
public abstract class AbstractClifAction
		implements Action {
	public String getIconFileName() {
		return ClifPlugin.ICON_FILE_NAME;
	}

	public abstract String getDisplayName();


	public String getUrlName() {
		return ClifPlugin.URL;
	}

	protected boolean shouldReloadGraph(StaplerRequest request, StaplerResponse response, Run build) {
		return !request.checkIfModified(build.getTimestamp(), response);
	}
}
