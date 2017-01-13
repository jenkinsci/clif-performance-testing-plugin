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
package jenkins.model;

import java.io.File;
import javax.servlet.ServletContext;
import hudson.PluginManager;
import static org.mockito.Mockito.mock;


public class Fake extends Jenkins {

	public Fake(File root, ServletContext context, PluginManager pluginManager)
			throws Exception {
		super(root, context, pluginManager);
	}

	private static JenkinsHolder previous;

	/**
	 * run once in @Before
	 * <p/>
	 * method is fragile : when run twice, then reset will not undo properly
	 *
	 * @return
	 */
	public static Jenkins install() {
		previous = Jenkins.HOLDER;
		final Jenkins jenkins = mock(Jenkins.class);
		Jenkins.HOLDER = new JenkinsHolder() {
			public Jenkins getInstance() {
				return jenkins;
			}
		};
		return jenkins;
	}

	/**
	 * run once in @After
	 *
	 * @return
	 */
	public static Jenkins uninstall() {
		Jenkins.HOLDER = previous;
		return previous.getInstance();
	}
}
