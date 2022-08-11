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
package org.ow2.clif.jenkins.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.annotation.Nonnull;
import hudson.Util;

public class TestPlan {

	private String name;
	private Date date;
	private List<Probe> probes;
	private List<Injector> injectors;
	private List<Measure> aggregatedMeasures;

	private transient boolean initDone = false;
	private transient Map<String, List<Probe>> probesByServer;
	private transient Map<String, List<Injector>> injectorsByServer;

	public TestPlan() {
		super();
	}

	public TestPlan(String name, @Nonnull Date date) {
		this();
		this.name = name;
		this.date = new Date(date.getTime());
	}

	public String getNameURL()
	{
		try
		{
			return URLEncoder.encode(name, "UTF-8");
		}
		catch (UnsupportedEncodingException ex)
		{
			// fall back to Util.rawEncode(), which partially does the job,
			// but should never occur as long as UTF-8 exists 
			return Util.rawEncode(name);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getDate() {
		return new Date(date.getTime());
	}

	public void setDate(@Nonnull Date date) {
		this.date = new Date(date.getTime());
	}

	public List<Probe> getProbes() {
		return probes;
	}

	public void setProbes(List<Probe> probes) {
		this.probes = probes;
	}

	public void addProbe(Probe probe) {
		if (probes == null) {
			probes = new ArrayList<Probe>();
		}
		probes.add(probe);
	}

	public List<Injector> getInjectors() {
		return injectors;
	}

	public void setInjectors(List<Injector> injectors) {
		this.injectors = injectors;
	}

	public void addInjector(Injector injector) {
		if (injectors == null) {
			injectors = new ArrayList<Injector>();
		}
		injectors.add(injector);
	}


	public List<Measure> getAggregatedMeasures() {
		return aggregatedMeasures;
	}

	public Measure getAggregatedMeasure(String measureName) {
		for (Measure measure : aggregatedMeasures) {
			if (measureName.equals(measure.getName())) {
				return measure;
			}

		}
		return null;
	}

	public void setAggregatedMeasures(List<Measure> aggregatedMeasures) {
		this.aggregatedMeasures = aggregatedMeasures;
	}

	public void addAggregatedMeasure(Measure measure) {
		if (aggregatedMeasures == null) {
			aggregatedMeasures = new ArrayList<Measure>();
		}
		aggregatedMeasures.add(measure);
	}

	public Set<String> getServers() {
		Set<String> servers = new HashSet<String>();
		probesByServer = new HashMap<String, List<Probe>>();
		injectorsByServer = new HashMap<String, List<Injector>>();

		if (getProbes() != null) {
			for (Probe p : getProbes()) {
				servers.add(p.getServer());
				List<Probe> serverProbes = probesByServer.get(p.getServer());
				if (serverProbes == null) {
					serverProbes = new ArrayList<Probe>();
					probesByServer.put(p.getServer(), serverProbes);
				}
				serverProbes.add(p);
			}
		}
		if (getInjectors() != null) {
			for (Injector i : getInjectors()) {
				servers.add(i.getServer());
				List<Injector> serverInjectors = injectorsByServer.get(i.getServer());
				if (serverInjectors == null) {
					serverInjectors = new ArrayList<Injector>();
					injectorsByServer.put(i.getServer(), serverInjectors);
				}
				serverInjectors.add(i);
			}
		}
		initDone = true;
		return servers;
	}

	public List<Probe> getProbesByServer(String serverName) {
		if (!initDone) {
			getServers();
		}
		return probesByServer.get(serverName);
	}

	public List<Injector> getInjectorsByServer(String serverName) {
		if (!initDone) {
			getServers();
		}
		return injectorsByServer.get(serverName);
	}

	public List<Alarm> getAlarms(Alarm.Severity sev) {
		List<Alarm> aggregatedAlarms = getAlarms();
		if (aggregatedAlarms == null) {
			return null;
		}
		List<Alarm> res = new ArrayList<Alarm>();
		for (Alarm a : aggregatedAlarms) {
			if (a.getSeverity().equals(sev)) {
				res.add(a);
			}
		}
		if (res.isEmpty()) {
			return null;
		}
		return res;
	}

	public List<Alarm> getAlarms() {
		List<Alarm> aggregatedAlarms = new ArrayList<Alarm>();
		if (probes != null) {
			for (Probe probe : probes) {
				if (probe.getAlarms() != null) {
					aggregatedAlarms.addAll(probe.getAlarms());
				}
			}
		}
		if (injectors != null) {
			for (Injector injector : injectors) {
				if (injector.getAlarms() != null) {
					aggregatedAlarms.addAll(injector.getAlarms());
				}
			}
		}
		if (aggregatedAlarms.isEmpty()) {
			return null;
		}
		return aggregatedAlarms;
	}

	public List<Alarm> getAlarms(String sev) {
		Alarm.Severity severity = Alarm.Severity.valueOf(sev);
		return getAlarms(severity);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TestPlan testPlan = (TestPlan) o;

		return name.equals(testPlan.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
