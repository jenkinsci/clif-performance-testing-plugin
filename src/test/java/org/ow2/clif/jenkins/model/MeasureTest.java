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
import static org.junit.Assert.assertEquals;

/**
 * @author Julien Coste
 */
public class MeasureTest {
	@Test
	public void testErrorPercent() throws Exception {
		Measure m = new Measure();
		m.setSize(9); // number of successful requests
		m.setCountErrors(1); // number of failed requests
		assertEquals(0.1, m.errorPercent(), 0.01);
		assertEquals("10%", m.errorPercentFormated());
	}
}
