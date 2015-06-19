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
import java.util.HashSet;

import sg.edu.nus.comp.sseriation.util.SparseVector;
import sg.edu.nus.comp.sseriation.util.Utilities;
import sg.edu.nus.comp.sseriation.util.VectorNode;

public abstract class DistributionalOrder extends LinearOrder {

	protected VectorNode[][] mx;
	protected int nDimensions;
	private boolean isTransposed;

	DistributionalOrder(String filename, String model, boolean isTransposed)
			throws IOException {
		super(filename, model);
		this.isTransposed = isTransposed;
		mx = SparseVector.readSparseMatrix(filename);
		if (isTransposed) {
			mx = SparseVector.transpose(mx);
		}
		nInstances = mx.length;
		nDimensions = SparseVector.findMaxColumnIndex(mx);
		if (SparseVector.findMinColumnIndex(mx) == 0) {
			nDimensions++;
		}
		boolean reset = true;
		initialize(reset);
	}

	@Override
	protected int findSeed() {
		return nInstances / 2;
	}

	public void mergeUpdates(VectorNode[][] updates) {
		VectorNode[][] newMx = new VectorNode[nInstances+updates.length][];
		for (int i = 0; i < mx.length; ++i) {
			newMx[i] = mx[i];
		}
		for (int i = 0; i < updates.length; ++i) {
			newMx[mx.length + i] = updates[i];
		}
		mx = newMx;
	}
	
	public void foldInNewInstance(VectorNode[] newInstance) {
		nInstances++;
		order.add(findBestSlot(nInstances - 1), nInstances - 1);
	}

	public VectorNode[][] getMx() {
		return mx;
	}

	public VectorNode[][] getRearranged() {
		return rearrangeMatrix(mx);
	}

	protected void initialize(boolean reset) throws IOException {
		remainingElements = new HashSet<Integer>();
		for (int i = 0; i < nInstances; i++) {
			if (mx[i] != null) {
					remainingElements.add(i);
			}
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

	protected void printInstance(int x) {
		for (int i = 0; i < mx[x].length; i++) {
			System.out.print(mx[x][i].index + ":" + mx[x][i].value + " ");
		}
		System.out.println();
	}

	private VectorNode[][] rearrangeMatrix(VectorNode[][] mx) {
		return SparseVector.rearrangeRowVectors(mx, getOrder());
	}

	public void setMx(VectorNode[][] mx) {
		this.mx = mx;
		nInstances = mx.length;
		nDimensions = SparseVector.findMaxColumnIndex(mx);
	}

	public void writeNewOrder() throws IOException {
		writeNewOrder(filename.substring(0, filename.length() - 4) + "_"
				+ model + ".dat");
	}

	public void writeNewOrder(String filename) throws IOException {
		writeNewOrder(this.mx, null, filename);
	}

	public void writeNewOrder(VectorNode[][] mx, String[] classes,
			String filename) {
		if (isTransposed) {
			try {
				mx = SparseVector.transpose(rearrangeMatrix(mx));
				if (classes != null) {
					Utilities.writeTable(
							Utilities.insertColumn(mx, classes, 0), filename);
				} else {
					Utilities.writeTable(mx, filename);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				if (classes != null) {
					Utilities.writeTable(
							Utilities.rearrangeRows(
									Utilities.insertColumn(mx, classes, 0),
									getOrder()), filename);
				} else {
					mx = rearrangeMatrix(mx);
					Utilities.writeTable(mx, filename);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
