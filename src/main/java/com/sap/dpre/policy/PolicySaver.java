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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.JOptionPane;

//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
import com.sap.dpre.log.MyLogger;

/**
 * save the policy to a file
 * 
 * 
 * 
 */
public class PolicySaver implements Runnable {

	private File myPolicyFile;

	public PolicySaver(File f) {

		// initialize
		this.myPolicyFile = f;
	}

	/**
	 * save the policy
	 */
	public void run() {

		// save it
		String panelMsg = "Saving policy file... Finished!\n";
		if (!this.savePolicy()) {
			panelMsg = "Saving policy file... Error!\n";
		}

	}

	/**
	 * save the policy
	 */
	private boolean savePolicy() {

		try {

			// create the objects necessary for writing to a file
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					this.myPolicyFile, false));

			// write to file
			writer.write(Policy.getInstance().getPolicyString());

			// close the file (this is important)
			writer.close();
		} catch (IOException e) {

			if (true) {
				e.printStackTrace();
			}

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());

			// something went wrong
			return false;
		}

		// everything OK
		return true;
	}
}
