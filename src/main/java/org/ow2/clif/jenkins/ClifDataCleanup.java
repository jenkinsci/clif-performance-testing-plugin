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

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 * @author Julien Coste
 */
public class ClifDataCleanup
		implements Serializable, Describable<ClifDataCleanup> {

	private static final long serialVersionUID = 1L;

	protected boolean enabled;

	protected double keepFactor;

	protected double keepPercentage;

	public ClifDataCleanup() {
		this.keepFactor = 2;
		this.keepPercentage = 95;
	}

	@DataBoundConstructor
	public ClifDataCleanup(boolean enabled, double keepFactor, double keepPercentage) {
		this.enabled = enabled;
		this.keepFactor = keepFactor;
		this.keepPercentage = keepPercentage;
	}

	public Descriptor<ClifDataCleanup> getDescriptor() {
		return Hudson.get().getDescriptorByType(DataCleanupDescriptor.class);
	}

	@Extension
	public static final class DataCleanupDescriptor
			extends Descriptor<ClifDataCleanup> {

		@Override
		public String getDisplayName() {
			return "";
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public double getKeepFactor() {
		return keepFactor;
	}

	public double getKeepPercentage() {
		return keepPercentage;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setKeepFactor(double keepFactor) {
		this.keepFactor = keepFactor;
	}

	public void setKeepPercentage(double keepPercentage) {
		this.keepPercentage = keepPercentage;
	}
}
