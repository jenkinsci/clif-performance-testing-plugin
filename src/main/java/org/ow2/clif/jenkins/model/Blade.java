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
package org.ow2.clif.jenkins.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julien Coste
 */
public abstract class Blade {

	private String id;
	private String name;
	private String server;
	private String argument;
	private String type;
	private String comment;

	private List<Measure> measures;

	private List<Alarm> alarms;

	public Blade() {
		super();
	}

	public Blade(String id, String name, String server, String argument, String type, String comment) {
		super();
		this.id = id;
		this.name = name;
		this.server = server;
		this.argument = argument;
		this.type = type;
		this.comment = comment;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getArgument() {
		return argument;
	}

	public void setArgument(String argument) {
		this.argument = argument;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<Measure> getMeasures() {
		return measures;
	}

	public void setMeasures(List<Measure> measures) {
		this.measures = measures;
	}

	public void addMeasure(Measure measure) {
		if (this.measures == null) {
			this.measures = new ArrayList<Measure>();
		}
		this.measures.add(measure);
	}

	public Measure getMeasure(String measureName) {
		if (this.measures == null) {
			return null;
		}
		for (Measure measure : this.measures) {
			if (measure.getName().equals(measureName)) {
				return measure;
			}
		}
		return null;
	}

	public List<Alarm> getAlarms() {
		return alarms;
	}

	public void setAlarms(List<Alarm> alarms) {
		this.alarms = alarms;
		for (Alarm alarm : this.alarms) {
			alarm.setOwner(this.server, this.name, this.id);
		}
	}

	public void addAlarm(Alarm alarm) {
		if (this.alarms == null) {
			this.alarms = new ArrayList<Alarm>();
		}
		alarm.setOwner(this.server, this.name, this.id);
		this.alarms.add(alarm);
	}

	public List<Alarm> getAlarms(Alarm.Severity sev) {
		List<Alarm> res = new ArrayList<Alarm>();
		if (this.alarms == null) {
			return null;
		}
		for (Alarm a : this.alarms) {
			if (a.getSeverity().equals(sev)) {
				res.add(a);
			}
		}
		if (res.isEmpty()) {
			return null;
		}
		return res;
	}

	public List<Alarm> getAlarms(String sev) {
		Alarm.Severity severity = Alarm.Severity.valueOf(sev);
		return getAlarms(severity);
	}


}
