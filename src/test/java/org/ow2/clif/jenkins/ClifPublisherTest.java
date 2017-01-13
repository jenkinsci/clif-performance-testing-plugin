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
package org.ow2.clif.jenkins;

import org.junit.Test;
import static org.fest.assertions.Assertions.assertThat;

public class ClifPublisherTest {

	@Test
	public void unboundConstructor() throws Exception {
		ClifPublisher publisher = new ClifPublisher("bar");
		assertThat(publisher.getChartHeight()).isEqualTo(600);
		assertThat(publisher.getChartWidth()).isEqualTo(1200);
		assertThat(publisher.getDistributionSliceSize()).isEqualTo(50);

		ClifDataCleanup cleanup = publisher.getDataCleanupConfig();
		assertThat(cleanup.getKeepFactor()).isEqualTo(2);
		assertThat(cleanup.getKeepPercentage()).isEqualTo(95);
	}
}
