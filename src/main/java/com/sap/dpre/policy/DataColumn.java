/*******************************************************************************
 * Copyright (c) 2013, SAP AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *  
 *     - Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *     - Neither the name of the SAP AG nor the names of its contributors may
 *      be used to endorse or promote products derived from this software 
 *      without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.sap.dpre.policy;

import com.sap.dpre.mySQL.MySQLQueryFactory;

/**
 * representation of the column object
 * 
 * 
 * 
 */
public class DataColumn {

	private String name;
	private ColumnType colType;
	private boolean isHide;
	private boolean isPropose;
	private float colRisk;

	/**
	 * enumeration of all kind of column types
	 */
	public enum ColumnType {
		IDENTIFIER, SENSITIVE, UNKNOWN,
	}

	/**
	 * constructor
	 * 
	 * @param name
	 *            name of the column
	 */
	public DataColumn(String name) {

		// variable initialization
		this.name = name;
		this.colType = ColumnType.UNKNOWN;
		this.isHide = true;
		this.isPropose = false;
		this.colRisk = 2;
	}

	/**
	 * get the name of the column
	 * 
	 * @return name of the column
	 */
	public String getName() {

		return this.name;
	}

	/**
	 * set the name of the column
	 * 
	 */
	public void setName(String name) {

		this.name = name;
	}

	/**
	 * get the type of the column
	 * 
	 * @return type of the column
	 */
	public ColumnType getColumnType() {

		return this.colType;
	}

	/**
	 * set the type of the column
	 * 
	 */
	public void setColumnType(ColumnType colType) {

		this.colType = colType;

		// if sensitive, check that view exists for this column
		if (colType == ColumnType.SENSITIVE) {
			new MySQLQueryFactory().checkView(this.name);
		} else if (colType == ColumnType.UNKNOWN) {

			// set hidden to true
			this.isHide = true;
		}
	}

	/**
	 * get the hidden status of the column
	 * 
	 * @return hidden status of the column
	 */
	public boolean getIsHide() {

		return this.isHide;
	}

	/**
	 * set the hidden status of the column
	 * 
	 */
	public void setIsHide(boolean hide) {

		this.isHide = hide;
	}

	/**
	 * get the propose status of the column
	 * 
	 * @return propose status of the column
	 */
	public boolean getIsPropose() {

		return this.isPropose;
	}

	/**
	 * set the propose status of the column
	 * 
	 */
	public void setIsPropose(boolean propose) {

		this.isPropose = propose;
	}

	/**
	 * get the column risk
	 * 
	 * @return column risk
	 */
	public float getColRisk() {

		return this.colRisk;
	}

	/**
	 * set the column risk
	 * 
	 * @param colRisk
	 *            column risk
	 */
	public void setColRisk(float colRisk) {

		this.colRisk = colRisk;
	}
}
