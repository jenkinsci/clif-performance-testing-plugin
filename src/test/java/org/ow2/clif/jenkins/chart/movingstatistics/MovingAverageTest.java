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
package org.ow2.clif.jenkins.chart.movingstatistics;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: bvjr5731
 * Date: 26/03/12
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
class MovingAverageTest {

	@Test
	void testCreateMovingMin() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);
		dataSeries.add(50, 3);
		dataSeries.add(100, 3);

		AbstractMovingStat ms = new MovingMinStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "min", 3, 0);

		assertNotNull(res);
		printSeries(res, "res");
		assertEquals(6, res.getItemCount(), "Bad size");
		System.out.println(res);
		double[] expectedMins = {1, 2, 2, 1, 3, 3};
		for (int i = 0; i < expectedMins.length; i++) {
			assertEquals(expectedMins[i], res.getY(i), "Bad min at index " + i);
		}
	}

	@Test
	void testCreateMovingMax() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		AbstractMovingStat ms = new MovingMaxStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "max", 3, 3);

		assertNotNull(res);
		assertEquals(3, res.getItemCount(), "Bad size");
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedMaxs = {5, 6, 4};
		for (int i = 0; i < expectedMaxs.length; i++) {
			assertEquals(expectedMaxs[i], res.getY(i), "Bad max at index " + i);
		}
	}

	@Test
	void testCreateMovingAverage() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		MovingAverageStat ms = new MovingAverageStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "average", 3, 3);

		assertNotNull(res);
		assertEquals(3, res.getItemCount(), "Bad size");
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedAverages = {10d / 3, 13d / 3, 8d / 3};
		for (int i = 0; i < expectedAverages.length; i++) {
			assertEquals(expectedAverages[i], res.getY(i), "Bad average at index " + i);
		}
	}

	@Test
	void testCreateMovingMedian() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		AbstractMovingStat ms = new MovingMedianStat();
		XYSeries res = ms.calculateMovingStat(dataset, 0, "median", 3, 3);

		assertNotNull(res);
		assertEquals(3, res.getItemCount(), "Bad size");
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedMedians = {3, 5, 3};
		for (int i = 0; i < expectedMedians.length; i++) {
			assertEquals(expectedMedians[i], res.getY(i), "Bad median at index " + i);
		}
	}

	@Test
	void testCreateMovingThroughput() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries dataSeries = new XYSeries("data");
		dataset.addSeries(dataSeries);
		dataSeries.add(1, 1);
		dataSeries.add(2, 3);
		dataSeries.add(3, 5);
		dataSeries.add(4, 2);
		dataSeries.add(5, 2);
		dataSeries.add(6, 5);
		dataSeries.add(7, 6);
		dataSeries.add(8, 1);
		dataSeries.add(9, 4);
		dataSeries.add(10, 3);

		AbstractMovingStat ms = new MovingThroughputStat(3);
		XYSeries res = ms.calculateMovingStat(dataset, 0, "throughput", 3, 3);

		assertNotNull(res);
		assertEquals(3, res.getItemCount(), "Bad size");
		printSeries(res, "res");
		System.out.println(res);
		double[] expectedMedians = {1000, 1000, 1000};
		for (int i = 0; i < expectedMedians.length; i++) {
			assertEquals(expectedMedians[i], res.getY(i), "Bad throughput at index " + i);
		}
	}

	private void printSeries(XYSeries res, final String seriesName) {
		for (int i = 0; i < res.getItemCount(); i++) {
			System.out.println(seriesName + "[" + i + "]=(" + res.getX(i) + "," + res.getY(i) + ")");

		}
	}

}
