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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.Maps;

public class ParameterParser {
	Pattern re = Pattern.compile("^(.*)\\[(.*)\\]$");

	/**
	 * suited to parse hash notation of boolean input values
	 * @param name for example "examples/synchro.ctp[uninstall]"
	 * @return for example ["examples/synchro.ctp", "uninstall"] 
	 */
	public Map<String, String> parse(String name) {
		Matcher matcher = re.matcher(name);
		HashMap<String, String> results = Maps.newHashMapWithExpectedSize(1);
		if (matcher.matches()) {
			results.put(matcher.group(1), matcher.group(2));
		}
		return results;
	}
}
