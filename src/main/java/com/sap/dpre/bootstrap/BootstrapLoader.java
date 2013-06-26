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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLQueryFactory;
import com.sap.dpre.risk.RiskResult;

/**
 * loads the bootstrapping data from a file
 * 
 * 
 * TODO to update retrieving info from the DB 
 */
public class BootstrapLoader implements Runnable {

	private long  gid;

	/**
	 * constructor
	 * 
	 * @param riskFile
	 *            riskFile containing bootstrapping information
	 */
	public BootstrapLoader(long  gid) {

		// initialization
		this.gid = gid;
	}

	/**
	 * load bootstrapping information
	 */
	public void run() {


		// clear the list from previous entries
		BootstrapRRList.getInstance().clear();

		// load information from file
		this.loadFromFile();

	}

	/**
	 * load the information from a file
	 */
	private boolean loadFromFile() {

		try {

			HashMap<Integer, Float> results = new MySQLQueryFactory().getBootstrapResult(gid);
			
			for (Map.Entry<Integer, Float> e : results.entrySet()) {
				// add new RiskResult
				BootstrapRRList.getInstance().addRiskResult(
						new RiskResult(e.getKey(), 
								e.getValue()
								)
						);

				
			}


		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			if (true) {
				e.printStackTrace();
			}

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());

			// create and pop up error dialog
			String msg = "Erronous bootstrapping risk file!";
			MyLogger.getInstance().writeLog(Level.ALL, msg);

			return false;
		}

		return true;
	}
}
