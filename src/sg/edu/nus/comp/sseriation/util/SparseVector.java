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
package sg.edu.nus.comp.sseriation.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * The utility class SparseVector deals with libsvm-formatted sparse matrices.
 */
public class SparseVector {

	private static VectorNode[] addNode(VectorNode[] v, int index, double value) {
		if (v == null || v[0] == null) {
			VectorNode[] w = new VectorNode[1];
			w[0] = new VectorNode();
			w[0].index = index;
			w[0].value = value;
			return w;
		}
		VectorNode[] w = new VectorNode[v.length + 1];
		boolean inserted = false;
		int j = 0;
		for (int i = 0; i < v.length; i++) {
			w[j] = new VectorNode();
			w[j].index = v[i].index;
			w[j].value = v[i].value;
			if (v[i].index == index) {
				w[j].value = w[j].value + value;
				inserted = true;
			}
			if (!inserted && index < v[i].index) {
				w[j].index = index;
				w[j].value = value;
				inserted = true;
				i--;
			}
			j++;
		}
		if (!inserted) {
			w[j] = new VectorNode(index, value);
			j++;
		}
		if (j == v.length + 1)
			return w;
		VectorNode[] result = new VectorNode[v.length];
		for (int i = 0; i < v.length; i++) {
			result[i] = new VectorNode();
			result[i].index = w[i].index;
			result[i].value = w[i].value;
		}
		return result;
	}

	private static VectorNode[] addVectors(VectorNode[] v, VectorNode[] z) {
		Vector<VectorNode> x = new Vector<VectorNode>();
		VectorNode tmp = new VectorNode();
		if (v == null) {
			return z;
		} else if (z == null) {
			return v;
		}
		int vai = 0;
		int zai = 0;
		while (vai < v.length || zai < z.length) {
			if (vai < v.length && zai < z.length
					&& v[vai].index == z[zai].index) {
				tmp.index = v[vai].index;
				tmp.value = v[vai].value + z[zai].value;
				x.add(tmp);
				tmp = new VectorNode();
				vai++;
				zai++;
			} else if (vai < v.length && zai < z.length
					&& v[vai].index < z[zai].index) {
				tmp.index = v[vai].index;
				tmp.value = v[vai].value;
				x.add(tmp);
				tmp = new VectorNode();
				vai++;
			} else if (vai >= v.length && zai < z.length) {
				for (; zai < z.length; zai++) {
					tmp.index = z[zai].index;
					tmp.value = z[zai].value;
					x.add(tmp);
					tmp = new VectorNode();
				}
			} else if (vai < v.length && zai < z.length
					&& z[zai].index < v[vai].index) {
				tmp.index = z[zai].index;
				tmp.value = z[zai].value;
				x.add(tmp);
				tmp = new VectorNode();
				zai++;
			} else if (zai >= z.length && vai < v.length) {
				for (; vai < v.length; vai++) {
					tmp.index = v[vai].index;
					tmp.value = v[vai].value;
					x.add(tmp);
					tmp = new VectorNode();
				}
			}
		}
		VectorNode[] result = new VectorNode[x.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new VectorNode();
			result[i] = x.elementAt(i);
		}
		return result;
	}

	/**
	 * Puts matrix elements into a fixed number of bins
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @param nBins
	 *            the number of bins
	 * @return the matrix in bins
	 */

	public static VectorNode[][] binify(VectorNode[][] mx, int nBins) {
		VectorNode[][] result = new VectorNode[mx.length][];
		double max = findMax(mx);
		double min = findMin(mx);

		// Note that 0 is always the minimum for sparse vectors
		// An adjustment avoids the problem of too wide intervals
		double binLength = Math.ceil((max - min) / (nBins - 1));
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] != null) {
				result[i] = new VectorNode[mx[i].length];
				for (int j = 0; j < mx[i].length; j++) {
					int bin = (int) ((mx[i][j].value - min) / binLength);
					result[i][j] = new VectorNode(mx[i][j].index, bin + 1);
				}
			}
		}
		return result;
	}

	
	public static double calculateNorm(VectorNode[] v) {
		double result = 0;
		if (v == null) {
			return 0;
		}
		for (int i = 0; i < v.length; i++) {
			result += v[i].value * v[i].value;
		}
		result = Math.sqrt(result);
		return result;
	}

	/**
	 * Extracts a column vector.
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @param columnIndex
	 *            the column index
	 * @return sparse vector of the requested column
	 */
	public static VectorNode[] columnVector(VectorNode[][] mx, int columnIndex) {
		ArrayList<VectorNode> v = new ArrayList<VectorNode>();
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] != null) {
				for (int j = 0; j < mx[i].length; j++) {
					if (mx[i][j].index == columnIndex) {
						v.add(new VectorNode(i, mx[i][j].value));
						break;
					}
				}
			}
		}
		VectorNode[] result = new VectorNode[v.size()];
		v.toArray(result);
		return result;
	}

	/**
	 * Correlation of two sparse vectors. Note: not computationally stable
	 * 
	 * @param x
	 *            the x vector
	 * @param y
	 *            the y vector
	 * @param n
	 *            the dimension
	 * @return the correlation
	 */
	public static double correlation(VectorNode[] x, VectorNode[] y, int n) {
		if (x == null || y == null) {
			return 0;
		}
		double sumx = sum(x);
		double sumy = sum(y);
		return (n * dotProduct(x, y) - sumx * sumy)
				/ (Math.sqrt(n * sumSquare(x) - sumx * sumx) * Math.sqrt(n
						* sumSquare(y) - sumy * sumy));
	}

	public static double cosine(VectorNode[] v, VectorNode[] z) {
		double result=dotProduct(v, z) / (calculateNorm(v) * calculateNorm(z));
		if (result>1.0){
			result=1.0;
		}else if (result<-1.0){
			result=-1.0;
		}
		return result;
	}
	
	private static int countNonZeroEntries(VectorNode[] v) {
		if (v == null)
			return 0;
		int result = 0;
		for (int i = 0; i < v.length; i++) {
			result++;
		}
		return result;
	}

	/**
	 * Dot product of two sparse vectors.
	 * 
	 * @param x
	 *            the x vector
	 * @param y
	 *            the y vector
	 * @return the dot product
	 */
	public static double dotProduct(VectorNode[] x, VectorNode[] y) {
		double sum = 0;
		if (x == null || y == null) {
			return 0;
		}
		int xlen = x.length;
		int ylen = y.length;
		int i = 0;
		int j = 0;
		while (i < xlen && j < ylen) {
			if (x[i].index == y[j].index)
				sum += x[i++].value * y[j++].value;
			else {
				if (x[i].index > y[j].index)
					++j;
				else
					++i;
			}
		}
		return sum;
	}

	private static double entropy(VectorNode[] x, int nBins, int n) {
		if (x == null) {
			return 0;
		}
		double result = 0;
		for (int i = 0; i < nBins; i++) {
			result += logOccurances(x, i, n);
		}
		return -result;
	}

	/**
	 * Euclidean distance of two sparse vectors.
	 * 
	 * @param x
	 *            the x vector
	 * @param y
	 *            the y vector
	 * @return the Euclidean distance
	 */
	public static double euclidean(VectorNode[] x, VectorNode[] y) {
		if (x == null || y == null) {
			return 0;
		}
		VectorNode[] z = multiplyByScalar(y, -1);
		z = addVectors(x, z);
		return Math.sqrt(dotProduct(z, z));
	}

	public static VectorNode[][] filterNullVectors(VectorNode[][] mx){
		int nNullVectors = 0;
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] == null) {
				nNullVectors++;
			}
		}
		VectorNode[][] filteredMx = new VectorNode[mx.length
				- nNullVectors][];
		int j = 0;
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] != null) {
				filteredMx[j++] = mx[i];
			}
		}
		return filteredMx;
	}

	private static double findMax(VectorNode[] x) {
		double result = Double.NEGATIVE_INFINITY;
		if (x == null) {
			return 0;
		}
		for (int i = 0; i < x.length; i++) {
			if (x[i].value > result) {
				result = x[i].value;
			}
		}
		return result;
	}

	public static double findMax(VectorNode[][] mx) {
		double result = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < mx.length; i++) {
			double tmpMax = findMax(mx[i]);
			if (tmpMax > result) {
				result = tmpMax;
			}
		}
		return result;
	}

	/**
	 * Finds the maximum column index (often the dimension of the space).
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @return the maximum column index
	 */
	public static int findMaxColumnIndex(VectorNode[][] mx) {
		int result = 0;
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] != null) {
				for (int j = 0; j < mx[i].length; j++) {
					if (mx[i][j].index > result)
						result = mx[i][j].index;
				}
			}
		}
		return result;
	}

	// Finds the non-zero min of a sparse vector
	private static double findMin(VectorNode[] x) {
		double result = Double.POSITIVE_INFINITY;
		if (x == null) {
			return 0;
		}
		for (int i = 0; i < x.length; i++) {
			if (x[i].value < result) {
				result = x[i].value;
			}
		}
		return result;
	}

	private static double findMin(VectorNode[][] mx) {
		double result = Double.POSITIVE_INFINITY;
		for (int i = 0; i < mx.length; i++) {
			double tmpMin = findMin(mx[i]);
			if (tmpMin < result) {
				result = tmpMin;
			}
		}
		return result;
	}

	/**
	 * Finds the minimum column index. This is important to determine whether
	 * the matrix elements are zero-indexed.
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @return the minimum index
	 */
	public static int findMinColumnIndex(VectorNode[][] mx) {
		int result = Integer.MAX_VALUE;
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] != null) {
				if (mx[i][0].index < result)
					result = mx[i][0].index;
			}
		}
		return result;
	}

	private static double jointEntropy(VectorNode[] x, VectorNode[] y,
			int nBins, int n) {
		double result = 0;
		for (int i = 0; i < nBins; i++) {
			for (int j = 0; j < nBins; j++) {
				result += logMutualOccurances(x, y, i, j, n);
			}
		}
		return -result;
	}

	private static double logMutualOccurances(VectorNode[] x, VectorNode[] y,
			double X, double Y, int n) {
		if (x == null && y == null) {
			return 0;
		}
		if (y == null) {
			y = x;
			x = null;
			double tmp = X;
			X = Y;
			Y = tmp;
		}
		double result = 0;
		int i = 0;
		int j = 0;
		if (X == 0 && Y == 0) {
			result = n - countNonZeroEntries(addVectors(x, y));
		} else if (X == 0) {
			int lastIndex = 0;
			if (x != null) {
				for (i = 0; i < x.length; i++) {
					while (j < y.length && y[j].index < x[i].index) {
						if (y[j].index > lastIndex && y[j].value == Y) {
							result++;
						}
						j++;
					}
					lastIndex = x[i].index;
				}
			}
			while (j < y.length) {
				if (y[j].index > lastIndex && y[j].value == Y) {
					result++;
				}
				j++;
			}
		} else if (X != 0 && x != null) {
			while (i < x.length && j < y.length) {
				if (x[i].index == y[j].index) {
					if (x[i].value == X && y[j].value == Y) {
						result++;
					}
					i++;
					j++;
				} else {
					if (x[i].index > y[j].index) {
						++j;
					} else {
						++i;
					}
				}
			}
		}
		result = result / n;
		if (result == 0) {
			return 0;
		} else {
			return result * Math.log(result);
		}
	}

	private static double logOccurances(VectorNode[] x, double X, int n) {
		if (x == null) {
			return 0;
		}
		double result = 0;
		if (X == 0) {
			result = (double) n - (double) x.length;
		} else {
			for (int i = 0; i < x.length; i++) {
				if (x[i].value == X) {
					result++;
				}
			}
		}
		result = result / n;
		if (result == 0) {
			return 0;
		} else {
			return result * Math.log(result);
		}
	}

	/**
	 * Manhattan distance of two sparse vectors.
	 * 
	 * @param x
	 *            the x vector
	 * @param y
	 *            the y vector
	 * @return the Manhattan distance
	 */

	public static double manhattan(VectorNode[] x, VectorNode[] y) {
		if (x == null || y == null) {
			return 0;
		}
		VectorNode[] z = multiplyByScalar(y, -1);
		z = addVectors(x, z);
		return sumAbsolute(z);
	}

	/**
	 * Sparse matrix multiply with a transpose of the second sparse matrix.
	 * 
	 * @param mx1
	 *            the first sparse matrix
	 * @param mx2
	 *            the second sparse matrix
	 * @return the product sparse matrix
	 */
	public static VectorNode[][] matrixMultiplyWithTranspose(
			VectorNode[][] mx1, VectorNode[][] mx2) {
		int m = mx1.length;
		if (findMaxColumnIndex(mx1) != findMaxColumnIndex(mx2)) {
			return null;
		}
		VectorNode[][] result = new VectorNode[m][];
		for (int i = 0; i < m; i++) {
			result[i] = null;
			for (int j = 0; j < m; j++) {
				double tmp = 0;
				tmp = dotProduct(mx1[i], mx2[j]);
				if (tmp != 0) {
					result[i] = addNode(result[i], j, tmp);
				}
			}
		}
		return result;
	}

	private static VectorNode[] multiplyByScalar(VectorNode[] v, double c) {
		VectorNode[] result = new VectorNode[v.length];
		for (int i = 0; i < v.length; i++) {
			result[i] = new VectorNode();
			result[i].index = v[i].index;
			result[i].value = v[i].value * c;
		}
		return result;
	}

	/**
	 * Mutual information of two sparse vectors.
	 * 
	 * @param x
	 *            the x vector
	 * @param y
	 *            the y vector
	 * @param nBins
	 *            the number of pre-allocated bins
	 * @param n
	 *            the dimension
	 * @return the mutual information
	 */

	public static double mutualInformationMetric(VectorNode[] x,
			VectorNode[] y, int nBins, int n) {

		return 2 * jointEntropy(x, y, nBins, n) - entropy(x, nBins, n)
				- entropy(y, nBins, n);
	}

	private static VectorNode[] parseSparseVectorString(String s) {
		StringTokenizer st = new StringTokenizer(s, "[ :]");
		int nTokens = st.countTokens();
		if (nTokens % 2 != 0) {
			st.nextToken();
			nTokens--;
		}
		VectorNode[] result = new VectorNode[nTokens / 2];
		int n = 0;
		while (st.hasMoreTokens()) {
			result[n] = new VectorNode();
			result[n].index = Integer.valueOf(st.nextToken());
			result[n].value = Double.valueOf(st.nextToken());
			n++;
		}
		return result;
	}

	/**
	 * Reads the class list from a sparse matrix file
	 * 
	 * @param filename
	 *            the file containing the sparse matrix
	 * @return the list of classes
	 */

	public static String[] readClasses(String filename) throws IOException {
		String[] result = new String[Utilities.countRowsInFile(filename)];
		Scanner scn = new Scanner(new BufferedReader(new FileReader(new File(
				filename)))).useDelimiter("[\n\r]");
		int m = 0;
		while (scn.hasNext()) {
			String tmp = scn.next();
			if (tmp.length() > 0) {
				StringTokenizer st = new StringTokenizer(tmp, " ");
				result[m++] = st.nextToken();
			}
		}
		scn.close();
		return result;
	}

	/**
	 * Reads a sparse matrix.
	 * 
	 * @param filename
	 *            the file name
	 * @return the sparse matrix
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static VectorNode[][] readSparseMatrix(String filename)
			throws IOException {
		ArrayList<VectorNode[]> result = new ArrayList<VectorNode[]>();
		Scanner scn = new Scanner(new BufferedReader(new FileReader(filename)))
				.useDelimiter("[\n\r]");
		while (scn.hasNext()) {
			String tmp = scn.next();
			if (tmp.length() > 0) {
				result.add(parseSparseVectorString(tmp));
			}
		}
		scn.close();
		VectorNode[][] resultArray = new VectorNode[result.size()][];
		for (int i = 0; i < result.size(); i++) {
			resultArray[i] = result.get(i);
		}
		return resultArray;
	}

	/**
	 * Rearranges row vectors according to a seriation
	 * 
	 * @param mx
	 *            the sparse matrix to rearrange
	 * @param newOrder
	 *            the seriation
	 * @return the reordered matrix
	 */
	public static VectorNode[][] rearrangeRowVectors(VectorNode[][] mx,
			ArrayList<Integer> newOrder) {
		VectorNode[][] result = new VectorNode[mx.length][];
		for (int i = 0; i < newOrder.size(); i++) {
//			System.out.println("DEBUG "+newOrder.get(i));
			if (mx[newOrder.get(i)] != null) {
				result[i] = new VectorNode[mx[newOrder.get(i)].length];
				for (int j = 0; j < result[i].length; j++) {
					result[i][j] = new VectorNode(mx[newOrder.get(i)][j].index,
							mx[newOrder.get(i)][j].value);
				}
			}
		}
		System.out.println();
		return result;
	}

	/**
	 * Shift columns to the right by one. This method is useful when converting
	 * from a zero-indexed matrix to a one-indexed matrix.
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @param k
	 *            the number of shifts to the right
	 * @return the shifted matrix
	 */

	public static VectorNode[][] shiftColumns(VectorNode[][] mx) {
		return shiftColumns(mx, 1);
	}

	/**
	 * Shift columns to the right. This method is useful when converting from a
	 * zero-indexed matrix to a one-indexed matrix.
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @param k
	 *            the number of shifts to the right
	 * @return the shifted matrix
	 */
	public static VectorNode[][] shiftColumns(VectorNode[][] mx, int k) {
		VectorNode[][] result = new VectorNode[mx.length][];
		for (int i = 0; i < mx.length; i++) {
			result[i] = null;
			if (mx[i] != null) {
				result[i] = new VectorNode[mx[i].length];
				for (int j = 0; j < mx[i].length; j++) {
					result[i][j] = new VectorNode(mx[i][j].index + k,
							mx[i][j].value);
				}
			}
		}
		return result;
	}

	private static double sum(VectorNode[] v) {
		if (v == null)
			return 0;
		double result = 0;
		for (int i = 0; i < v.length; i++) {
			result += v[i].value;
		}
		return result;
	}

	private static double sumAbsolute(VectorNode[] v) {
		if (v == null)
			return 0;
		double result = 0;
		for (int i = 0; i < v.length; i++) {
			result += Math.abs(v[i].value);
		}
		return result;
	}

	private static double sumSquare(VectorNode[] v) {
		if (v == null)
			return 0;
		double result = 0;
		for (int i = 0; i < v.length; i++) {
			result += v[i].value * v[i].value;
		}
		return result;
	}
	
	/**
	 * Transposes a sparse matrix. Note that (A')' may not be equal to A,
	 * because null vectors are eliminated.
	 * 
	 * @param mx
	 *            the sparse matrix
	 * @return the transposed matrix
	 */
	public static VectorNode[][] transpose(VectorNode[][] mx) {
		int minCol = findMinColumnIndex(mx);
		int adjust = 0;
		if (minCol > 0) {
			adjust = 1;
		}
		int mci = findMaxColumnIndex(mx) + 1 - adjust;
		VectorNode[][] result = new VectorNode[mci][];
		for (int i = 0; i < mx.length; i++) {
			if (mx[i] != null) {
				for (int j = 0; j < mx[i].length; j++) {
					result[mx[i][j].index - adjust] = addNode(
							result[mx[i][j].index - adjust], i, mx[i][j].value);
				}
			}
		}
		return result;
	}

}
