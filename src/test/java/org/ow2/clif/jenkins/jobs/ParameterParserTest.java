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
package org.ow2.clif.jenkins.jobs;

import org.junit.Before;
import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class ParameterParserTest {
	private ParameterParser parser;

	@Before
	public void setUp() {
		parser = new ParameterParser();
	}

	@Test
	public void parsesHashNotationFor() throws Exception {
		// convention like this one for parameter name
		// http://guides.rubyonrails.org/action_controller_overview.html#hash-and-array-parameters
		assertThat(
				parser.parse("examples/synchro.ctp[uninstall]").get("examples/synchro.ctp")
		).isEqualTo("uninstall");
	}

	@Test
	public void isSilentOtherwise() throws Exception {
		assertThat(
				parser.parse("json")
		).isEmpty();
	}
}
