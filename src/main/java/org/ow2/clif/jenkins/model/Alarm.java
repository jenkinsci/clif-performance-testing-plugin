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

/**
 * Alarm that Clif generates during a test
 *
 * @author Julien Coste
 */
public class Alarm {
	protected long date;
	protected Severity severity;
	protected String message;
	protected String ownerServer;
	protected String ownerType;
	protected String ownerId;


	public Alarm() {
	}

	public Alarm(long date, Severity severity, String message) {
		this.date = date;
		this.severity = severity;
		this.message = message;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOwnerServer() {
		return ownerServer;
	}

	public void setOwnerServer(String ownerServer) {
		this.ownerServer = ownerServer;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setOwner(String server, String type, String id) {
		setOwnerServer(server);
		setOwnerType(type);
		setOwnerId(id);
	}


	public enum Severity {
		INFO(0),
		WARNING(1),
		ERROR(2),
		FATAL(3);

		private final int value;

		Severity(int value) {
			this.value = value;
		}

		public static Severity fromValue(int value) {
			for (Severity sev : Severity.values()) {
				if (sev.value == value) {
					return sev;
				}
			}
			throw new IllegalArgumentException("Unknown alarm severity value : " + value);
		}
	}


}
