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

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Julien Coste
 */
public class InjectorTest {
	@Test
	public void testGetMeasureNoMeasure() throws Exception {
		Injector injectorWithoutMeasure = new Injector();
		assertNull(injectorWithoutMeasure.getMeasure("test"));

	}

	@Test
	public void testGetMeasureWithMeasure() throws Exception {
		Measure m = new Measure();
		m.setName("test");

		Injector injectorWithMeasure = new Injector();
		injectorWithMeasure.addMeasure(m);
		assertNotNull(injectorWithMeasure.getMeasure("test"));
		assertNull(injectorWithMeasure.getMeasure("test2"));
	}

	@Test
	public void testGetAlarms() throws Exception {
		Injector injectorWithoutAlarm = new Injector();

		assertNull(injectorWithoutAlarm.getAlarms(Alarm.Severity.INFO));
		assertNull(injectorWithoutAlarm.getAlarms(Alarm.Severity.WARNING));
		assertNull(injectorWithoutAlarm.getAlarms(Alarm.Severity.ERROR));
		assertNull(injectorWithoutAlarm.getAlarms(Alarm.Severity.FATAL));

		Injector injectorWithAlarms = new Injector();
		injectorWithAlarms.addAlarm(new Alarm(1, Alarm.Severity.INFO, "msg1"));
		injectorWithAlarms.addAlarm(new Alarm(2, Alarm.Severity.WARNING, "msg2"));
		injectorWithAlarms.addAlarm(new Alarm(3, Alarm.Severity.WARNING, "msg3"));

		assertNotNull(injectorWithAlarms.getAlarms(Alarm.Severity.INFO));
		assertNotNull(injectorWithAlarms.getAlarms(Alarm.Severity.WARNING));
		assertNull(injectorWithAlarms.getAlarms(Alarm.Severity.ERROR));
		assertNull(injectorWithAlarms.getAlarms(Alarm.Severity.FATAL));

		assertEquals(1, injectorWithAlarms.getAlarms(Alarm.Severity.INFO).size());
		assertEquals(2, injectorWithAlarms.getAlarms(Alarm.Severity.WARNING).size());

		// Recherche par String
		assertNotNull(injectorWithAlarms.getAlarms("INFO"));
		assertNotNull(injectorWithAlarms.getAlarms("WARNING"));
		assertNull(injectorWithAlarms.getAlarms("ERROR"));
		assertNull(injectorWithAlarms.getAlarms("FATAL"));

		assertEquals(1, injectorWithAlarms.getAlarms("INFO").size());
		assertEquals(2, injectorWithAlarms.getAlarms("WARNING").size());
	}
}
