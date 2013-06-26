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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

//import javax.swing.JFileChooser;
//import javax.swing.JOptionPane;

//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLDeepSearchResultsManager;
import com.sap.dpre.risk.RiskResult;

/**
 * saves the DeepSearch result
 * 
 * 
 * 
 */
class DeepSearchSaver implements Runnable {

//	private String filename;
	private long gid;
	MyLogger logger = MyLogger.getInstance();

	float maxRisk = -1.0f;
	
	/**
	 * Constructor. If we specify a maxRisk, only results below this upper 
	 * bound will be
	 * saved. 
	 * 
	 * @param gid
	 * @param maxRisk an upper bound for re-identification risk, or a negative 
	 * number to 
	 * save every result.
	 */
	protected DeepSearchSaver(long gid, float maxRisk) {

		this.gid = gid;
		
		if (maxRisk > 0 ) {
			this.maxRisk = maxRisk;
		}

	}

	/**
	 * save the DeepSearch result to a file
	 */
	public void run() {
			// save DeepSearch results
			if (this.storeResults()) {
				
				logger.writeLog(Level.INFO, "deep search results saving operation succesfully completed");

			} else {

				logger.writeLog(Level.ALL, "error in deep search results saving operation");
				
			}
		}

	
	public boolean storeResults() {

		HashMap<Integer, Float> results = new HashMap<Integer, Float>();

		// iterate over all deep search results 
		for (int i = 0; i < DeepSearchRRList.getInstance().getSize(); i++) {
			RiskResult tmpRR = DeepSearchRRList.getInstance()
					.getRiskResult(i);

			// shall we save all results, or only those over a certain threshold?
			if (tmpRR.getRisk() < maxRisk) {
			
			// create a new hashmap entry for each result
			results.put(
					Integer.valueOf(tmpRR.getBinaryCounter()), 
					Float.valueOf(tmpRR.getRisk())
					); 
			}
		}

		boolean result = 
				MySQLDeepSearchResultsManager.storeDeepSearchResult(
						gid, 
						results
						);

		if (result) {
			// everything OK
			return true;
		} else {
			logger.writeLog(Level.SEVERE, "error in DeepSearchSaver.storeResults");
			return false;
		}

	}

}
