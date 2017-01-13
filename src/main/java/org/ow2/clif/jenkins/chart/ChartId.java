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
package org.ow2.clif.jenkins.chart;

import org.apache.commons.codec.digest.DigestUtils;

/**
 */
public class ChartId {
	protected final String chartType;

	protected final String testplan;

	protected final String bladeId;

	protected final String event;

	public ChartId(String chartType, String testplan, String bladeId, String event) {
		this.chartType = chartType;
		this.testplan = testplan;
		this.bladeId = bladeId;
		this.event = event;
	}

	public String getId() {
		return DigestUtils.shaHex(chartType + testplan + bladeId + event);
	}

	public String getBladeId() {
		return bladeId;
	}

	public String getChartType() {
		return chartType;
	}

	public String getEvent() {
		return event;
	}

	public String getTestplan() {
		return testplan;
	}
}
