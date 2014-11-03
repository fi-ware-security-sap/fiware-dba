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


import com.sap.dpre.mySQL.MySQLQueryFactory;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.primelife.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
//import com.sap.dpre.gui.riskIllustrationFrame.RiskIllustrationFrame;
import com.sap.dpre.policy.Policy;


/**
 * compute the individual column risks for all columns
 * 
 * 
 * 
 */
public class RiskyColumnFinder implements Runnable {

	public RiskyColumnFinder_computeColumnRisks columnRiskComputer = null;
	long gid;
	MySQLQueryFactory mySQLFactory = null;
	
	/**
	 * constructor
	 */
	public RiskyColumnFinder(long gid, MySQLQueryFactory mySQLFactory) {

		// initialization
		this.columnRiskComputer = new RiskyColumnFinder_computeColumnRisks(Policy.getInstance().getAllColumnNames().length, gid);
		this.mySQLFactory = mySQLFactory;
	}

	/**
	 * find risky columns
	 */
	public void run() {



		// compute column risks
		this.columnRiskComputer.computeAndStoreColumnRisks();
		
		mySQLFactory.dropWorkingViews(gid);
		mySQLFactory.dropWorkingTable(gid);
	}

	
}