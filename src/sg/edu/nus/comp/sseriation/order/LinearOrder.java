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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

import sg.edu.nus.comp.sseriation.util.Utilities;

public abstract class LinearOrder {

	private class minObject {
		public double min;
		public int argmin;

		minObject(double min, int argmin) {
			this.min = min;
			this.argmin = argmin;
		}
	}

	protected HashSet<Integer> remainingElements;
	protected ArrayList<Integer> order;
	protected String model;
	protected String filename;
	protected int nInstances;
	
	public LinearOrder(String filename, String model) {
		this.model = model;
		this.filename = filename;
	}

	public double[] calculateConsecutiveDistances() {
		double[] result = new double[nInstances - 1];
		for (int i = 0; i < nInstances - 1; i++) {
			result[i] = getDistance(order.get(i), order.get(i + 1));
		}
		return result;
	}

	public double calculateSumOfDistances() {
		double result = 0;
		if (!order.isEmpty()) {
		for (int i = 0; i < order.size()-1; i++) {

					result += getDistance(order.get(i), order.get(i + 1));
				} 
		} else {
			for (int i = 0; i < nInstances-1; i++) {
				result += getDistance(i, i + 1);
			}
		}
		return result;
	}

	/**
	 * Finds the best slot to insert an element in the order computed so far.
	 * Used by the insert heuristic.
	 * 
	 * @param x
	 *            the element to insert
	 * @return the best slot
	 */
	protected Integer findBestSlot(int x) {
		// If the order has zero or one element,
		// the solution is trivial
		if (order.size() == 0 || order.size() == 1) {
			return 0;
		}

		// Two separate cases have to deal with the end points
		double d = getDistance(x, order.get(0));
		double min = d;
		int argmin = -1;
		d = getDistance(order.get(order.size() - 1), x);
		if (d < min) {
			argmin = order.size() - 1;
			min = d;
		}
		// The main loop
		for (int i = 0; i < order.size() - 1; i++) {
			// It is the relative increase that matters!
			d = getDistance(order.get(i), x) + getDistance(x, order.get(i + 1))-
					getDistance(order.get(i), order.get(i+1));
			if (d < min) {
				argmin = i;
				min = d;
			}
		}		
		return argmin + 1;
	}

	/**
	 * Finds the next candidate from the remaining set of elements that has the
	 * minimum distance to one end of the order computed so far. Used by the
	 * left-right heuristic.
	 * 
	 * @param x
	 *            the current left or right element
	 * @return the optimal element and its distance from x
	 */
	private minObject findNextCandidate(int x) {
		double min = Integer.MAX_VALUE;
		int argmin = -1;
		for (Iterator<Integer> iter = remainingElements.iterator(); iter
				.hasNext();) {
			int tmpi = iter.next();
			double tmpd = getDistance(x, tmpi);
			if (tmpd < min) {
				argmin = tmpi;
				min = tmpd;
			}
		}
		minObject result = new minObject(min, argmin);
		return result;
	}

	protected abstract int findSeed();

	/**
	 * Generates the order by the insert heuristic
	 */
	public void generateOrderInsert() {
		int progress = 0;
		for (int i = 0; i < nInstances; i++) {
			order.add(findBestSlot(i), i);
			remainingElements.remove(i);
			printProgress(++progress);
		}
		System.out.println();
	}

	/**
	 * Generates the order by the left-right heuristic
	 */
	public void generateOrderLeftRight() throws IOException {
		if (order.size()==0) {
			System.out.println("Finding seed...");
			int seed = findSeed();
			order.add(seed);
			remainingElements.remove(seed);
			writeOne(filename.substring(0, filename.length() - 4) + "_" + model
					+ "_order-tmp.txt", seed);
			System.out.println("Finding left seed...");
			int tl = findNextCandidate(seed).argmin;
			remainingElements.remove(tl);
			order.add(0, tl);
			writeOne(filename.substring(0, filename.length() - 4) + "_" + model
					+ "_order-tmp.txt", tl);
			System.out.println("Finding right seed...");
			int tr = findNextCandidate(seed).argmin;
			remainingElements.remove(tr);
			order.add(tr);
			writeOne(filename.substring(0, filename.length() - 4) + "_" + model
					+ "_order-tmp.txt", tr);
		}
		System.out.println("Generating order...");
		int progress = 0;
		minObject tlMinObject = findNextCandidate(order.get(0));
		minObject trMinObject = findNextCandidate(order.get(order.size() - 1));
		boolean changeLeft = false;
		boolean changeRight = false;
		while (!remainingElements.isEmpty()) {
			if (changeLeft) {
				tlMinObject = findNextCandidate(order.get(0));
				changeLeft = false;
			}
			if (changeRight) {
				trMinObject = findNextCandidate(order.get(order.size() - 1));
				changeRight = false;
			}
			if (tlMinObject.min <= trMinObject.min && tlMinObject.argmin != -1) {
				remainingElements.remove(tlMinObject.argmin);
				order.add(0, tlMinObject.argmin);
				writeOne(filename.substring(0, filename.length() - 4) + "_"
						+ model + "_order-tmp.txt", tlMinObject.argmin);
				changeLeft = true;
			} else if (trMinObject.argmin != -1) {
				remainingElements.remove(trMinObject.argmin);
				order.add(trMinObject.argmin);
				writeOne(filename.substring(0, filename.length() - 4) + "_"
						+ model + "_order-tmp.txt", trMinObject.argmin);
				changeRight = true;
			}
			if (trMinObject.argmin == tlMinObject.argmin) {
				changeLeft = true;
				changeRight = true;
			}
			printProgress(++progress);
			if (trMinObject.argmin == -1 && tlMinObject.argmin == -1) {
				/*
				 * rightSide.removeElementAt(rightSide.size() - 1); snapshotV();
				 * V.remove(trMinObject.argmin); changeRight = true; if
				 * (!V.isEmpty()){ V.remove(tlMinObject.argmin); changeLeft =
				 * true; }
				 */
				break;
			}
		}
		System.out.println();
	}

	protected abstract double getDistance(int x, int y);

	public String getModel() {
		return model;
	}

	public ArrayList<Integer> getOrder() {
		return order;
	}

	public double[] getScale() throws IOException {
		double[] consecDists = calculateConsecutiveDistances();
		double[] scale = new double[consecDists.length + 1];
		scale[0] = 0;
		for (int i = 0; i < consecDists.length; i++) {
			scale[i + 1] = scale[i] + consecDists[i];
		}
		return scale;
	}

	protected void initialize(boolean reset) throws IOException {
		remainingElements = new HashSet<Integer>();
		for (int i = 0; i < nInstances; i++) {
			remainingElements.add((Integer) i);
		}
		order = new ArrayList<Integer>();
		if (reset) {
			Utilities.resetFile(filename.substring(0, filename.length() - 4)
					+ "_" + model + "_order-tmp.txt");
		} else {
			order = Utilities.readIntArrayList(filename.substring(0,
					filename.length() - 4)
					+ "_" + model + "_order-tmp.txt");
			for (int i = 0; i < order.size(); i++) {
				remainingElements.remove(order.get(i));
			}
		}
	}

	abstract protected void printInstance(int x);
	
	private void printProgress(int progress) {
		if (progress % 1000 == 0) {
			System.out.print(progress + "..");
		}
	}
	
	public void setOrder(ArrayList<Integer> order) {
		this.order = order;
	}

	/**
	 * Takes a snapshot of the remaining elements. Useful in debugging.
	 */
	protected void takeSnapshotOfRemainingElements() throws IOException {
		FileWriter out = new FileWriter(new File(filename.substring(0,
				filename.length() - 4)
				+ "_" + model + "_remaining_elements_snapshot.txt"), false);
		for (Iterator<Integer> iter = remainingElements.iterator(); iter
				.hasNext();) {
			out.write(iter.next() + "\n");
		}
		out.close();
	}

	/**
	 * Updates the order with the insert heuristic
	 */
	public void updateOrderInsert(int i) {
		for (int j=0;j<order.size();j++){
			if (order.get(j)==i){
				return;
			}
		}
		order.add(findBestSlot(i), i);
	}

	private void writeOne(String filename, int x) throws IOException {
		FileWriter out = new FileWriter(new File(filename), true);
		out.write(x + "\n");
		out.close();
	}

	public void writeOrder() throws IOException {
		FileWriter out = new FileWriter(new File(filename.substring(0,
				filename.length() - 4)
				+ "_" + model + "_order.txt"));
		for (int i = 0; i < order.size(); i++) {
			out.write(order.get(i) + "\n");
		}
		out.close();
	}

	public void writeScale() throws IOException {
		Utilities.writeDoubleList(getScale(),
				filename.substring(0, filename.length() - 4) + "_" + model
						+ "_scale.dat");
	}

}
