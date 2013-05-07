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
package se.hb.cd.biseriation;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sg.edu.nus.comp.sseriation.order.DistributionalOrder;

public class Biseriation {

	protected String collection;
	protected DistributionalOrder rowSeriation;
	protected DistributionalOrder columnSeriation;

	enum heuristicTypes {
		leftright, insert
	};

	protected heuristicTypes heuristic;

	private Constructor<DistributionalOrder> distanceBasedConstructor;

	@SuppressWarnings("unchecked")
	public Biseriation(String collection, String distanceType,
			String heuristicType) throws IOException, RuntimeException,
			NoSuchMethodException, ClassNotFoundException {
		this.collection = collection;
		if (heuristicType.equals("leftright")) {
			heuristic = heuristicTypes.leftright;
		} else {
			heuristic = heuristicTypes.insert;
		}

		if (this.collection.lastIndexOf(".") > 0) {
			this.collection = this.collection.substring(0,
					this.collection.lastIndexOf("."));
		}
		distanceBasedConstructor = (Constructor<DistributionalOrder>) Class
				.forName("sg.edu.nus.comp.sseriation.order." + distanceType)
				.getConstructor(String.class, Boolean.TYPE);
	}

	public void calculateBiseriationOfStaticPart() throws IOException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		boolean isTransposed = false;
		rowSeriation = (DistributionalOrder) distanceBasedConstructor
				.newInstance(collection + ".dat", isTransposed);
		if (heuristic == heuristicTypes.leftright) {
			rowSeriation.generateOrderLeftRight();
		} else {
			rowSeriation.generateOrderInsert();
		}
		rowSeriation.writeNewOrder(collection + "-row-seriated.dat");
		isTransposed = true;
		columnSeriation = (DistributionalOrder) distanceBasedConstructor
				.newInstance(collection + "-row-seriated.dat", isTransposed);
		if (heuristic == heuristicTypes.leftright) {
			columnSeriation.generateOrderLeftRight();
		} else {
			columnSeriation.generateOrderInsert();
		}
		columnSeriation.writeNewOrder(collection + "-biseriated.dat");
	}

}
