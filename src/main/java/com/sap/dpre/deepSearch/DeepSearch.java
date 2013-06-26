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
package com.sap.dpre.deepSearch;

import java.util.logging.Level;

//import com.sap.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.dpre.gui.GUI;
//import com.sap.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.policy.Policy;
import com.sap.dpre.risk.RiskEvaluator;
import com.sap.dpre.risk.RiskResult;

/**
 * compute the identifier risk for all possible disclosed identifier column
 * combinations
 * 
 * 
 * 
 * 
 */
public class DeepSearch implements Runnable {

	private RiskEvaluator myRE; // RiskEstimator for computing identifier risk
	private String[] myIdentifierColumns; // disclosed identifier columns
	private int binaryCounterMax; // binary counter maximal
	private boolean isComputation; // used to abort risk computation

	
	MyLogger logger = MyLogger.getInstance();
	
	float maxRisk = -1.0f;
	
	long requestGID; 
	
	
	/**
	 * constructor
	 * @param gid
	 */
	public DeepSearch(long gid, float maxRisk) {
	
		requestGID = gid;
		this.maxRisk = maxRisk;
	}

	/**
	 * compute the identifier risk for all column combinations
	 */
	public void run() {

		// start timing
		long start = System.currentTimeMillis();

		// initialization
		this.initialize();

		// write initial text to the GUI
		String text = "Starting identifier risk computation for all disclosed identifier columns (total combinations: "
				+ this.binaryCounterMax + ")\n";
		
		System.err.println(text);
		
		// compute all combinations
		this.runComputation(/*pb*/);

		// sort the list
		DeepSearchRRList.getInstance().sort();

		// stop timing
		long stop = System.currentTimeMillis();

		// compute duration and LOG it
		

		logger.writeLog(Level.INFO, 
				"Time required: " + this.computeTiming((stop - start)));

		// store the result on the DB
		
		new DeepSearchSaver(requestGID, maxRisk).run();
	}

	/**
	 * get the size of the binary counter max
	 * 
	 * @return size of the binary counter max
	 */
	public int getMaxCounter() {

		return this.binaryCounterMax;
	}

	/**
	 * cancel the DeepSearch identifier risk computation
	 */
	public void cancelComputation() {

		this.isComputation = false;
	}

	/**
	 * initialization
	 */
	private void initialize() {

		// initialization
		this.myRE = new RiskEvaluator();
		this.isComputation = true;

		// clear the list
		DeepSearchRRList.getInstance().clear();

		// get the identifier columns to be disclosed
		this.myIdentifierColumns = Policy.getInstance()
				.getAllIdentifierColumnNames();

		// compute the max of the binary counter (2^#columns)
		this.binaryCounterMax = (int) Math.pow(2,
				this.myIdentifierColumns.length) - 1;
	}

	/**
	 * compute risk for all possible column combinations
	 */
	private void runComputation(/*ProgressDialog pb*/) {

		long counter = 0;
		
		// while the binary counter didn't reach the max, get new combination
		// and compute risk
		int binaryCounter = 0;
		while (binaryCounter < this.binaryCounterMax && this.isComputation) {

			// get new column combination
			String[] tmpColumns = this.getColumnCombination(binaryCounter);

			// compute identifier risk
			float tmpRisk = this.myRE.getIdentifierRisk(tmpColumns);
			
			// store result in list
			DeepSearchRRList.getInstance().addRiskResult(
					new RiskResult(binaryCounter, tmpRisk));

			
			
			// increment binary and ProgressBar counter
			binaryCounter++;

		}
	}

	/**
	 * compute calculation duration
	 * 
	 * @param duration
	 *            time in milliseconds
	 * @return TODO
	 */
	private String computeTiming(long duration) {

		int hours, minutes, seconds, milli;

		// compute number of hours
		hours = (int) (duration / 3600000);
		duration = duration - (hours * 3600000);

		// compute number of minutes
		minutes = (int) (duration / 60000);
		duration = duration - (minutes * 60000);

		// compute number of seconds
		seconds = (int) (duration / 1000);
		duration = (int) (duration - seconds * 1000);

		// compute number of milliseconds
		milli = (int) duration;

		// print out duration
//		GUI.getInstance().addText2MessagePanel(
		return new String(
				hours + " hour(s) " + minutes + " minute(s) " + seconds
						+ " second(s) " + milli + " milliseconds\n");

	}

	/**
	 * get column combination according to the binary counter
	 * 
	 * @param binaryCounter
	 *            binary counter
	 * @return String array of columns
	 */
	private String[] getColumnCombination(int binaryCounter) {

		// convert the binaryCounter to a binary string
		String strBinaryCounter = Integer.toBinaryString(binaryCounter);

		// make the binary string of the length of the original array
		while (strBinaryCounter.length() < this.myIdentifierColumns.length) {
			strBinaryCounter = "0" + strBinaryCounter;
		}

		// count the number of elements the new array will have
		int arrayLength = this.myIdentifierColumns.length
				- strBinaryCounter.replaceAll("0", "").length();

		// initialize and build new string array
		String[] tmpDisclosedColumns = new String[arrayLength];
		int j = 0;
		for (int i = 0; i < this.myIdentifierColumns.length; i++) {

			// add the column to the column array if the char is 0
			if (strBinaryCounter.charAt(i) == '0') {
				tmpDisclosedColumns[j] = this.myIdentifierColumns[i];

				j++;
			}
		}

		return tmpDisclosedColumns;
	}
}
