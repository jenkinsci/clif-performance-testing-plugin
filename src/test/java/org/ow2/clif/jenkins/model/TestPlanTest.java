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

import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Julien Coste
 */
public class TestPlanTest {
	private TestPlan tp1;
	private Probe cpu;
	private Injector action;
	private Injector uri;
	private Measure uri2;

	@Before
	public void initData() {
		tp1 = new TestPlan("TestPlan1", new Date());

		cpu = new Probe("0", "CPU", "localhost", "60 1000", "CPU", "");
		Measure cpuPercent = new Measure("% CPU", 10, 5, 6, 1, 10, 0.4, 0, 0);
		cpu.addMeasure(cpuPercent);
		Measure cpuPercentKernel = new Measure("% CPU Kernel", 11, 6, 7, 2, 11, 0.5, 0, 1);
		cpu.addMeasure(cpuPercentKernel);

		tp1.addProbe(cpu);

		action = new Injector("1", "Action", "localhost", "60 1000", "scenario", "");
		Measure action1 = new Measure("URI1", 10, 5, 6, 1, 10, 0.4, 0, 0);
		action.addMeasure(action1);
		Measure action2 = new Measure("RUI2", 11, 6, 7, 2, 11, 0.5, 0, 1);
		action.addMeasure(action2);

		tp1.addInjector(action);

		uri = new Injector("2", "Action", "server2", "60 1000", "scenario", "");
		Measure uri1 = new Measure("URI1", 10, 5, 6, 1, 10, 0.4, 0, 0);
		uri.addMeasure(uri1);
		uri2 = new Measure("RUI2", 11, 6, 7, 2, 11, 0.5, 0, 1);
		uri.addMeasure(uri2);

		tp1.addInjector(uri);
	}

	@Test
	public void test_getServers() {
		Set<String> servers = tp1.getServers();
		assertNotNull(servers);
		assertEquals(2, servers.size());
		assertTrue(servers.contains("localhost"));
		assertTrue(servers.contains("server2"));
	}

	@Test
	public void test_getInjectorsByServer() {
		List<Injector> injectors = tp1.getInjectorsByServer("localhost");
		assertNotNull(injectors);
		assertEquals(1, injectors.size());
		assertTrue(injectors.contains(action));

		injectors = tp1.getInjectorsByServer("server2");
		assertNotNull(injectors);
		assertEquals(1, injectors.size());
		assertTrue(injectors.contains(uri));
	}

	@Test
	public void test_getProbesByServer() {
		List<Probe> probes = tp1.getProbesByServer("localhost");
		assertNotNull(probes);
		assertEquals(1, probes.size());
		assertTrue(probes.contains(cpu));
	}

}
