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
package com.sap.dpre.bootstrap;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLDeepSearchResultsManager;
import com.sap.dpre.mySQL.MySQLQueryFactory;
import com.sap.dpre.risk.RiskResult;

/**
 * saves the bootstrapping data in case it was modified
 * 
 * 
 * 
 */
class BootstrapSaver implements Runnable {

	private long gid;
	MyLogger logger = MyLogger.getInstance();

	/**
	 * constructor
	 */
	protected BootstrapSaver(long gid) {
		
		this.gid = gid;
	}

	/**
	 * save the DeepSearch results to a file
	 */
	public void run() {

			if (this.storeResults()) {

				logger.writeLog(Level.INFO, "bootstrap results saving operation succesfully completed");
				
			} else {
				
				MyLogger.getInstance().writeLog(Level.ALL,"Error saving bootstrapping data!");
			}
	}


	/**
	 * 
	 * @return
	 */
	public boolean storeResults() {

		HashMap<Integer, Float> results = new HashMap<Integer, Float>();
		


			// write a line for each RiskResult
			for (int i = 0; i < BootstrapRRList.getInstance().getSize(); i++) {
				RiskResult tmpRR = BootstrapRRList.getInstance().getRiskResult(
						i);

				results.put(
						Integer.valueOf(tmpRR.getBinaryCounter()), 
						Float.valueOf(tmpRR.getRisk())
						); 
				
			}

			boolean result = 
					new MySQLQueryFactory().storeBootstrapResult(
							gid, 
							results
							);

			if (result) {
				// everything OK
				return true;
			} else {
				MyLogger.getInstance().writeLog(Level.SEVERE, "error in BootstrapSaver.storeResults");
				return false;
			}
	}

	
}

