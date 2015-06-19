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
import java.lang.reflect.InvocationTargetException;

import sg.edu.nus.comp.sseriation.util.SparseVector;
import sg.edu.nus.comp.sseriation.util.VectorNode;

public class DynamicBiseriation extends Biseriation {

	private VectorNode[][] updates;
	private int global_update_iteration;

	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws RuntimeException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void main(String[] args) throws IOException,
			RuntimeException, NoSuchMethodException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {

		// Defaults
		String distance = "Euclidean";
		String heuristic = "leftright";

		int argc = 0;
		while (args.length>argc && args[argc].charAt(0) == '-') {
			String flagName = args[argc];
			// Ignore trivial flags (without raising an error).
			if (flagName.equals("-"))
				continue;
			// Strip off initial "-" repeatedly to get desired flag name.
			while (flagName.charAt(0) == '-') {
				flagName = flagName.substring(1, flagName.length());
			}

			if (flagName.equals("d") | flagName.equals("distance")) {
				String flagValue;
				try {
					flagValue = args[argc + 1].toLowerCase();
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("option -" + flagName
							+ " requires an argument");
				}
				flagValue = (Character.toString(flagValue.charAt(0))
						.toUpperCase()).concat(flagValue.substring(1));
				if (flagValue.equals("Mutualinformation")) {
					flagValue = "MutualInformation";
				}
				distance = flagValue;
				argc += 2;
			}
			if (flagName.equals("h") | flagName.equals("heuristic")) {
				String flagValue;
				try {
					flagValue = args[argc + 1].toLowerCase();
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException("option -" + flagName
							+ " requires an argument");
				}
				heuristic = flagValue;
				argc += 2;
			}

		}
		if (args.length-argc<=0){
			throw new IllegalArgumentException("At least one filename is required.");
		}

		String collection = args[argc++];
		DynamicBiseriation dynamicBiseriation = new DynamicBiseriation(
				collection, distance, heuristic);
		dynamicBiseriation.calculateBiseriationOfStaticPart();
		
		while (args.length>argc){
			String collectionUpdate = args[argc++];
			dynamicBiseriation.addCollectionUpdate(collectionUpdate);
			dynamicBiseriation.iterativelyUpdate();
		}
	}

	/**
	 * Instantiates a new dynamic biseriation.
	 * 
	 * @param collection
	 *            the collection to index
	 * @param distanceType
	 *            the distance type to use in biseriation
	 * @param heuristicType
	 *            the heuristic type to use in biseriation
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws RuntimeException
	 *             the runtime exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 */
	public DynamicBiseriation(String collection, String distanceType,
			String heuristicType) throws IOException, RuntimeException,
			NoSuchMethodException, ClassNotFoundException {
		super(collection, distanceType, heuristicType);
		global_update_iteration = 0;
	}

	public void addCollectionUpdate(String collectionUpdate)
			throws IOException {
		updates = SparseVector.readSparseMatrix(collectionUpdate);
		rowSeriation.mergeUpdates(updates);
	}

	/**
	 * Iteratively update the biseration.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void iterativelyUpdate() throws IOException {
		for (int i = 0; i < updates.length; i++) {
			rowSeriation.foldInNewInstance(updates[i]);
			updateFeatureSpaceSeriation();
		}
		global_update_iteration += updates.length;
		rowSeriation
		.writeNewOrder(SparseVector.transpose(columnSeriation
				.getRearranged()), null, collection+"-biseriated-updated-"
				+ (global_update_iteration) + ".dat");

	}

	private void updateFeatureSpaceSeriation() {
		columnSeriation.setMx(SparseVector.transpose(rowSeriation.getMx()));
		for (int i = 0; i < columnSeriation.getMx().length; i++) {
			columnSeriation.updateOrderInsert(i);
		}
	}

}
