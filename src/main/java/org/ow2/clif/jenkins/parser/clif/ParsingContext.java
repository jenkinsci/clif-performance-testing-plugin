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
package org.ow2.clif.jenkins.parser.clif;

import org.apache.commons.lang.StringUtils;
import org.ow2.clif.storage.api.BladeDescriptor;
import org.ow2.clif.storage.api.TestDescriptor;

/**
 * @author Julien Coste
 */
public class ParsingContext {

	protected TestDescriptor test;

	protected BladeDescriptor blade;

	protected String eventType;

	protected boolean dataCleanup;

	protected double keepFactor;

	protected double keepPercentage;

	public ParsingContext() {
	}

	public ParsingContext(ParsingContext ctx) {
		this.test = ctx.test;
		this.blade = ctx.blade;
		this.eventType = ctx.eventType;
		this.dataCleanup = ctx.dataCleanup;
		this.keepFactor = ctx.keepFactor;
		this.keepPercentage = ctx.keepPercentage;
	}

	public boolean isDataCleanup() {
		return dataCleanup;
	}

	public void setDataCleanup(boolean dataCleanup) {
		this.dataCleanup = dataCleanup;
	}

	public double getKeepFactor() {
		return keepFactor;
	}

	public void setKeepFactor(double keepFactor) {
		this.keepFactor = keepFactor;
	}

	public double getKeepPercentage() {
		return keepPercentage;
	}

	public void setKeepPercentage(double keepPercentage) {
		this.keepPercentage = keepPercentage;
	}

	public TestDescriptor getTest() {
		return test;
	}

	public void setTest(TestDescriptor test) {
		this.test = test;
	}

	public BladeDescriptor getBlade() {
		return blade;
	}

	public void setBlade(BladeDescriptor blade) {
		this.blade = blade;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	protected String getTestPlanShortName() {
		int nbUnderScore = StringUtils.countMatches(this.test.getName(), "_");
		if (nbUnderScore < 2) {
			return this.test.getName();
		}
		int dateUnderScore = lastOrdinalIndexOf(this.test.getName(), "_", 2);
		return this.test.getName().substring(0, dateUnderScore);
	}

	protected static int lastOrdinalIndexOf(String str, String searchStr, int ordinal) {
		if (str == null || searchStr == null || ordinal <= 0) {
			return -1;
		}
		if (searchStr.length() == 0) {
			return str.length();
		}
		int found = 0;
		int index = str.length();
		do {
			index = str.lastIndexOf(searchStr, index - 1);
			if (index < 0) {
				return index;
			}
			found++;
		}
		while (found < ordinal);
		return index;
	}

}
