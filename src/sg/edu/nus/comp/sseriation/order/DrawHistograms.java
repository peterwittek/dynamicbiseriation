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
import java.util.ArrayList;

import sg.edu.nus.comp.sseriation.util.Utilities;

public class DrawHistograms {

	public static void main(String[] args) throws IOException {
		String database = args[0];
		String trainingFile = database + "_train.dat";
		LinearOrder lo = new Euclidean(trainingFile,true);
		lo.setOrder(original(lo.nInstances));
		double data[][] = new double[2][];
		data[0] = lo.calculateConsecutiveDistances();
		Utilities.writeDoubleList(data[0], database + "_train_histogram.dat");
		lo.setOrder(Utilities.readIntArrayList(database + "_train_" + lo.model
				+ "_order.txt"));
		data[1] = lo.calculateConsecutiveDistances();
		Utilities.writeDoubleList(data[1], database + "_train_" + lo.model
				+ "_histogram.dat");
	}

	public static ArrayList<Integer> original(int m) throws IOException {
		ArrayList<Integer> order = new ArrayList<Integer>();
		for (int i = 0; i < order.size(); i++) {
			order.add(i);
		}
		return order;
	}

}
