/**
 * Two-way Incremental Seriation based on Hamiltonian Path
 *  Copyright (C) 2012-2013 Peter Wittek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package sg.edu.nus.comp.sseriation.order;

import java.io.IOException;

import sg.edu.nus.comp.sseriation.util.SparseVector;

public class Correlation extends DistributionalOrder {
	
	private static final String MODEL_NAME="corr";
	
	public Correlation(String filename, boolean isTransposed) throws IOException {
		super(filename, MODEL_NAME, isTransposed);
	}
	
	@Override
	protected double getDistance(int x, int y) {
		return 1 - Math.abs(SparseVector.correlation(mx[x], mx[y], nDimensions));
	}

}
