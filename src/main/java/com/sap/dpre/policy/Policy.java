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

import java.util.ArrayList;
import java.util.List;

//import javax.swing.JOptionPane;
//import javax.swing.JTextField;

import com.sap.dpre.bootstrap.BootstrapRRList;
//import com.sap.primelife.dpre.gui.GUI;
import com.sap.dpre.policy.DataColumn.ColumnType;
import com.sap.dpre.risk.RiskResult;

/**
 * contains the parsed information about a policy
 * 
 * 
 * 
 */
public class Policy {

	private static Policy MY_POLICY = new Policy();

	private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private float maxRisk; // maximal risk defined by user
	private List<DataColumn> MY_COLUMNS = new ArrayList<DataColumn>();
	private RiskResult myCurrentProposalRR;

	/**
	 * constructor
	 */
	private Policy() {

		// initialization
		this.maxRisk = 0.0f;
		this.myCurrentProposalRR = null;
	}

	/**
	 * get the class instance
	 * 
	 * @return class instance
	 */
	public static Policy getInstance() {

		return MY_POLICY;
	}

	/**
	 * set the maximal risk defined by the user
	 * 
	 * @param maxRisk
	 *            maximal risk
	 */
	public void setMaxRisk(float maxRisk) {

		this.maxRisk = maxRisk;
	}

	/**
	 * get the maximal risk defined by the user
	 * 
	 * @return maximal risk
	 */
	public float getMaxRisk() {

		return this.maxRisk;
	}

	/**
	 * add a DataColumn
	 * 
	 * @param dataCol
	 *            DataColumn to be added
	 */
	public void addDataColumn(DataColumn dataCol) {

		this.MY_COLUMNS.add(dataCol);
	}

	/**
	 * clear all policy information
	 */
	public void clearPolicy() {

		this.MY_COLUMNS.clear();
	}

	/**
	 * get the information represented in a 2D object array
	 * 
	 * @return information represented in a 2D object array
	 */
	public Object[][] getDataColumnsData() {

		// create new object array
		Object[][] tmpObj = new Object[this.MY_COLUMNS.size()][4];

		// fill the array with all the column information
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			tmpObj[i][0] = this.MY_COLUMNS.get(i).getName();
			tmpObj[i][1] = this.MY_COLUMNS.get(i).getColumnType();
			tmpObj[i][2] = this.MY_COLUMNS.get(i).getIsHide();
			tmpObj[i][3] = this.MY_COLUMNS.get(i).getIsPropose();
		}

		return tmpObj;
	}

	/**
	 * get a DataColumn by name
	 * 
	 * @param name
	 *            name of the column
	 * @return DataColumn
	 */
	public DataColumn getDataColumnByName(String name) {

		// go through all the array and return the corresponding DataColumn on a
		// match
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			DataColumn tmp = this.MY_COLUMNS.get(i);
			if (tmp.getName().toLowerCase().compareTo(name.toLowerCase()) == 0) {
				return tmp;
			}
		}

		return null;
	}

	/**
	 * get a DataColumn by index
	 * 
	 * @param index
	 *            index of the column
	 * @return DataColumn
	 */
	public DataColumn getDataColumnByIndex(int index) {

		if (this.MY_COLUMNS.size() > 0) {
			return this.MY_COLUMNS.get(index);
		}

		return null;
	}

	/**
	 * get the names of all the disclosed columns
	 * 
	 * @return names of all the disclosed columns
	 */
	public String[] getDisclosedAllColumnNames() {

		// create temporary list and add all DatColumn names with the hidden
		// flag set to false and type != unknown
		List<String> tmpList = new ArrayList<String>();
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			if (!this.MY_COLUMNS.get(i).getIsHide()) {
				tmpList.add(this.MY_COLUMNS.get(i).getName());
			}
		}

		// convert and return a String array
		return (String[]) tmpList.toArray(new String[tmpList.size()]);
	}

	/**
	 * get the binary counter of the current disclosed identifier columns
	 * 
	 * @return binary counter of the current disclosed identifier columns
	 */
	public int getDisclosedIdentifierColumnsCounter() {

		String tmpCounter = "0";

		// go through all DataColumns and add a '0' or a '1' if the type ==
		// identifier
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			if (this.MY_COLUMNS.get(i).getColumnType() == ColumnType.IDENTIFIER) {
				if (this.MY_COLUMNS.get(i).getIsHide()) {
					tmpCounter += "1";
				} else {
					tmpCounter += "0";
				}
			}
		}

		return Integer.parseInt(tmpCounter, 2);
	}

	/**
	 * get the names of the disclosed identifier columns
	 * 
	 * @return names of the disclosed identifier columns
	 */
	public String[] getDisclosedIdentifierColumnNames() {

		// create temporary list and add all column names with the hidden flag
		// set to false and type == identifier
		List<String> tmpList = new ArrayList<String>();
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			if (!this.MY_COLUMNS.get(i).getIsHide()
					&& this.MY_COLUMNS.get(i).getColumnType() == ColumnType.IDENTIFIER) {
				tmpList.add(this.MY_COLUMNS.get(i).getName());
			}
		}

		// convert and return a String array
		return (String[]) tmpList.toArray(new String[tmpList.size()]);
	}

	/**
	 * get the names of all the identifier columns
	 * 
	 * @return names of all the identifier columns
	 */
	public String[] getAllIdentifierColumnNames() {

		// create temporary list and add all column names type == identifier
		List<String> tmpList = new ArrayList<String>();
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			if (this.MY_COLUMNS.get(i).getColumnType() == ColumnType.IDENTIFIER) {
				tmpList.add(this.MY_COLUMNS.get(i).getName());
			}
		}

		// convert and return a String array
		return (String[]) tmpList.toArray(new String[tmpList.size()]);
	}

	/**
	 * get the names of the disclosed sensitive columns
	 * 
	 * @return names of the disclosed sensitive columns
	 */
	public String[] getDisclosedSensitiveColumnNames() {

		// create temporary list and add all column names with the hidden flag
		// set to false and type = sensitive
		List<String> tmpList = new ArrayList<String>();
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			if (!this.MY_COLUMNS.get(i).getIsHide()
					&& this.MY_COLUMNS.get(i).getColumnType() == ColumnType.SENSITIVE) {
				tmpList.add(this.MY_COLUMNS.get(i).getName());
			}
		}

		// convert and return a String array
		return (String[]) tmpList.toArray(new String[tmpList.size()]);
	}

	/**
	 * tell if the policy contains no information
	 * 
	 * @return true if there is no information
	 */
	public boolean isEmpty() {

		if (this.MY_COLUMNS.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * get the size of the binary counter
	 * 
	 * @return size of the binary counter
	 */
	public int getBinaryCounterSize() {

		// increment counter IIF column type == identifier
		int counterSize = 0;
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			if (this.MY_COLUMNS.get(i).getColumnType() == ColumnType.IDENTIFIER) {
				counterSize++;
			}
		}

		return counterSize;
	}

	/**
	 * switch to the next identifier column disclosure proposal
	 * 
	 * @param forward
	 *            true if 'Next' was pressed, false if 'Previous' was pressed
	 */
	public boolean switchProposal(boolean forward/*, JTextField approxRiskTxtF,
			JTextField distanceTxtF*/) {

		// get the binaryCounter for the proposal
		if (forward) {
			this.myCurrentProposalRR = BootstrapRRList.getInstance()
					.getNextRR();
		} else {
			this.myCurrentProposalRR = BootstrapRRList.getInstance()
					.getPreviousRR();
		}
		
		// check if the proposal is null
		if (this.myCurrentProposalRR == null) {
						
			return false;
		}

		// set the proposal
		String i1 = Integer.toBinaryString(this.myCurrentProposalRR
				.getBinaryCounter());
		while (i1.length() < this.getBinaryCounterSize()) {
			i1 = "0" + i1;
		}

		// set the proposal column according to the proposal
		for (int i = 0; i < this.getBinaryCounterSize(); i++) {

			boolean propose = i1.charAt(i) == '1' ? true : false;

			Policy.getInstance().getDataColumnByIndex(i).setIsPropose(propose);
		}

		return true;

	}

	/**
	 * apply the current identifier column proposal
	 */
	public boolean applyProposal() {

		// create & set binary String to appropriate length
		String i1 = Integer.toBinaryString(this.myCurrentProposalRR
				.getBinaryCounter());
		while (i1.length() < this.getBinaryCounterSize()) {

			// append '0' until the correct length
			i1 = "0" + i1;
		}

		// set hide to true if char at position i is '1'
		for (int i = 0; i < this.getBinaryCounterSize(); i++) {

			boolean hide = i1.charAt(i) == '1' ? true : false;

			Policy.getInstance().getDataColumnByIndex(i).setIsHide(hide);
		}
		
		return true;
	}

	/**
	 * return the policy formatted in a String
	 * 
	 * @return the policy formatted in a String
	 */
	protected String getPolicyString() {

		// construct new StringBuilder
		StringBuilder strB = new StringBuilder();

		// append header and main node
		strB.append(this.XML_HEADER);
		strB.append("<Policy>\n");

		// add all policy columns where the type is not UNKNOWN
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			DataColumn tmpDC = this.MY_COLUMNS.get(i);

			if (tmpDC.getColumnType() != ColumnType.UNKNOWN) {
				strB.append("<Column>\n");
				strB.append("<Name>");
				strB.append(tmpDC.getName());
				strB.append("</Name>\n");
				strB.append("<Type>");
				strB.append(tmpDC.getColumnType().toString().toLowerCase());
				strB.append("</Type>\n");
				strB.append("<Hide>");
				strB.append(tmpDC.getIsHide());
				strB.append("</Hide>\n");
				strB.append("</Column>\n");
			}
		}

		// close main node
		strB.append("</Policy>");

		return strB.toString();
	}

	/**
	 * get all column names independent of their hide status and type
	 * 
	 * @return all column names independent of their hide status and type
	 */
	public String[] getAllColumnNames() {

		// create temporary list and add all column names to it
		List<String> tmpList = new ArrayList<String>();
		for (int i = 0; i < this.MY_COLUMNS.size(); i++) {
			tmpList.add(this.MY_COLUMNS.get(i).getName());
		}

		// convert and return a String array
		return (String[]) tmpList.toArray(new String[tmpList.size()]);
	}
}
