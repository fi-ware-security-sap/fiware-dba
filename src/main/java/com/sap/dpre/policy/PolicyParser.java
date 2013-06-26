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

import java.io.File;
import java.util.logging.Level;

import com.sap.dpre.log.MyLogger;

/**
 * parse the policy file to get the column information
 * 
 * 
 * 
 */
public class PolicyParser implements Runnable {

	PolicyParser_Utilities policyParserUtility = null;

	/**
	 * constructor
	 */
	public PolicyParser(File policyFile) {

		// initialize
		this.policyParserUtility = new PolicyParser_Utilities(policyFile);
	}

	/**
	 * parse the policies from a file
	 * 
	 */
	public void run() {

		// parse the policy document
		if (!this.policyParserUtility.parsePolicyDocument()) {

			// end ProgressBar thread
//			pb.endThread();

			// create and pop up error dialog
			String msg = "Error parsing policy file!";
			MyLogger.getInstance().writeLog(Level.ALL, msg);

			return;
		}

		// apply the policy to the columns
		this.policyParserUtility.applyPolicy();

	}
}
