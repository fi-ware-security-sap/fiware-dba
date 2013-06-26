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
package com.sap.dpre.risk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;

import javax.swing.JOptionPane;

//import com.sap.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
import com.sap.dpre.log.MyLogger;

/**
 * compare the RiskResults between two DeepSearchs
 * 
 * 
 * 
 */
public class RiskComparator implements Runnable {

	private File my1File;
	private File my2File;

	private int totalRisks;
	private int totalRisksBiased;
	private int totalSameOrder;

	private float maxDiff;
	private float totalDiff;

	private ArrayList<RiskResult> rrList1;
	private ArrayList<RiskResult> rrList2;

	/**
	 * constructor
	 */
	public RiskComparator(File file1, File file2) {

		// initialization
		this.my1File = file1;
		this.my2File = file2;

		this.totalRisks = 0;
		this.totalRisksBiased = 0;
		this.totalSameOrder = 0;

		this.maxDiff = 0.0f;
		this.totalDiff = 0.0f;

		this.rrList1 = new ArrayList<RiskResult>();
		this.rrList2 = new ArrayList<RiskResult>();
	}

	/**
	 * thread run method
	 */
	public void run() {

		// load risk files
		this.loadFiles();

		// compute statistics
		this.computeStats();

	}

	/**
	 * compute the statistics of two deep search results
	 */
	private void loadFiles() {

		try {

			// create FileReader & BufferedReader
			FileReader fr1 = new FileReader(this.my1File);
			FileReader fr2 = new FileReader(this.my2File);

			BufferedReader bf1 = new BufferedReader(fr1);
			BufferedReader bf2 = new BufferedReader(fr2);

			// read first line
			String lineF1 = bf1.readLine();
			String lineF2 = bf2.readLine();

			// compute statistics
			while (lineF1 != null && lineF2 != null) {

				// increment counter
				this.totalRisks++;

				String[] tmp1 = lineF1.split("\t");
				String[] tmp2 = lineF2.split("\t");

				// check format
				if (tmp1.length != 2 || tmp2.length != 2) {

					// create and pop up error dialog
					String msg = "Erronous risk file!";
					
					MyLogger.getInstance().writeLog(Level.ALL, msg.toString());

					break;
				}

				// add RiskResults to the lists
				this.rrList1.add(new RiskResult(Integer.parseInt(tmp1[0]),
						Float.parseFloat(tmp1[1])));
				this.rrList2.add(new RiskResult(Integer.parseInt(tmp2[0]),
						Float.parseFloat(tmp2[1])));

				// count the number of results that are at the same position
				if (Integer.parseInt(tmp1[0]) == Integer.parseInt(tmp2[0])) {
					this.totalSameOrder++;
				}

				// read new line from each file
				lineF1 = bf1.readLine();
				lineF2 = bf2.readLine();
			}

			// close streams
			bf1.close();
			fr1.close();
			bf2.close();
			fr2.close();
		} catch (FileNotFoundException e) {

			if (true) {
				e.printStackTrace();
			}

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		} catch (IOException e) {

			if (true) {
				e.printStackTrace();
			}

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		} catch (NumberFormatException e) {

			if (true) {
				e.printStackTrace();
			}

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());

		}
	}

	/**
	 * compute all statistics
	 */
	private void computeStats() {

		// create new comparator (sort by binary counter) & sort the lists
		Comparator<RiskResult> myComparator = new Comparator<RiskResult>() {
			public int compare(RiskResult rr1, RiskResult rr2) {
				if (rr1.getBinaryCounter() == rr2.getBinaryCounter()) {
					return 0;
				} else if (rr1.getBinaryCounter() > rr2.getBinaryCounter()) {
					return 1;
				} else {
					return -1;
				}
			}
		};
		Collections.sort(this.rrList1, myComparator);
		Collections.sort(this.rrList2, myComparator);

		// compute maximal & average risk difference
		for (int i = 0; i < this.rrList1.size(); i++) {

			// get the RiskResults
			RiskResult rre1 = this.rrList1.get(i);
			RiskResult rre2 = this.rrList2.get(i);

			// compute difference
			float tmpDiff = Math.abs(rre1.getRisk() - rre2.getRisk());

			// used to compute the (biased) average difference
			if (tmpDiff > 0) {
				this.totalDiff += tmpDiff;
				this.totalRisksBiased++;
			}

			// keep the maximal difference
			if (tmpDiff > this.maxDiff) {
				this.maxDiff = tmpDiff;

				if (true) {
					MyLogger logger = MyLogger.getInstance();
					logger.writeLog(Level.ALL,"counter: "
							+ Integer.toBinaryString(rre1.getBinaryCounter())
							+ " tmp max diff: " + this.maxDiff);
				}
			}
		}

		// print out results
		String msgStr = "max diff: " + this.maxDiff * 100 + "%\navg diff: "
				+ this.totalDiff / this.totalRisks * 100
				+ "%\nbiased avg diff: " + this.totalDiff
				/ this.totalRisksBiased * 100 + "%\nsame rank: "
				+ this.totalSameOrder / this.totalRisks * 100 + "%\n";
		MyLogger.getInstance().writeLog(Level.ALL, msgStr);
		
	}
}
