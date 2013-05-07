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

/**
 * The Class VectorNode is the basic data element in a row-first sparse matrix.
 */
public class VectorNode implements java.io.Serializable {

	/** The Constant serialVersionUID. */
	static final long serialVersionUID = 1L;

	/** The index of the element. */
	public int index;

	/** The value of the element. */
	public double value;

	/**
	 * Instantiates a new vector node.
	 */
	public VectorNode() {

	}

	/**
	 * Instantiates a new vector node.
	 * 
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	public VectorNode(int index, double value) {
		setNode(index, value);
	}

	/**
	 * Sets the node.
	 * 
	 * @param index
	 *            the index
	 * @param value
	 *            the value
	 */
	public void setNode(int index, double value) {
		this.index = index;
		this.value = value;
	}

	/**
	 * Sets the node.
	 * 
	 * @param v
	 *            the new node
	 */
	public void setNode(VectorNode v) {
		this.index = v.index;
		this.value = v.value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return index + ":" + value;
	}

}
