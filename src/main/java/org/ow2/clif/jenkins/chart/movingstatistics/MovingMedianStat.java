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

import java.util.Arrays;
import org.apache.commons.math.util.ResizableDoubleArray;
import org.jfree.data.xy.XYSeries;


/**
 * Calculate the moving median of time series data.
 */
public class MovingMedianStat extends AbstractMovingStat {
	ResizableDoubleArray values;
	int n = 0;


	@Override
	public void resetMovingStat() {
		this.values = new ResizableDoubleArray();
		this.n = 0;
	}

	@Override
	protected void calculateMovingStatInPeriod(double xx, double yy) {
		values.addElement(yy);
		n = n + 1;
	}

	@Override
	protected void addMovingStatForPeriod(XYSeries result, double x) {
		if (n > 0) {
			double[] valuesArrayfromResisable = values.getValues();
			// ResizableDoubleArray has a bug (at least in v1.1 which is shipped with Clif-core)
			// . It returns an array with too many values
			double[] valuesArray = Arrays.copyOf(valuesArrayfromResisable, n);
			Arrays.sort(valuesArray);
			if (valuesArray.length % 2 == 0) {
				result.add(x, valuesArray[(valuesArray.length / 2) - 1]);
			}
			else {
				result.add(x, valuesArray[(valuesArray.length - 1) / 2]);
			}
		}
		else {
			result.add(x, null);
		}
	}

}
