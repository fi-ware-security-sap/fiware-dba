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

//import com.sap.dpre.DisclosurePolicyRiskEvaluator;
import java.util.logging.Level;

import com.sap.dpre.bootstrap.BootstrapRRList;
import com.sap.dpre.log.MyLogger;
//import com.sap.dpre.gui.GUI;
//import com.sap.dpre.gui.progressDialog.ProgressDialog;
//import com.sap.dpre.gui.progressDialog.ProgressDialog.ProgressBarType;
import com.sap.dpre.risk.RiskEvaluator;

/**
 * main processing class: collect policy information, compute risk, check if
 * risk < policy risk (otherwise choose additional column(s) to be hidden)
 * 
 * 
 * 
 * 
 */
public class PolicyComplianceChecker /*implements Runnable*/ {

//	private JTextField myCurrentRiskTF;
	
	MyLogger logger = MyLogger.getInstance();

	/**
	 * constructor
	 */
	public PolicyComplianceChecker(float maxRisk/*, JTextField currentRisk*/) {

		// initialization
//		this.myCurrentRiskTF = currentRisk;
		Policy.getInstance().setMaxRisk(maxRisk);
	}

	public void computeECM() {

		// get the columns to be disclosed
		String[] tmpDisclosedIdentifierColumns = Policy.getInstance()
				.getDisclosedIdentifierColumnNames();
		String[] tmpDisclosedSensitiveColumns = Policy.getInstance()
				.getDisclosedSensitiveColumnNames();

		// create new RiskEstimator and get both risks
		RiskEvaluator re = new RiskEvaluator();

		float currentIdentifierRisk = re
				.getIdentifierRisk(tmpDisclosedIdentifierColumns);

		float currentSensitiveRisk = re
				.getSensitiveRisk(tmpDisclosedSensitiveColumns);

		// compute total risk (& check that it is not > 1)
		float currentTotalRisk = currentIdentifierRisk + currentSensitiveRisk;
		currentTotalRisk = currentTotalRisk > 1 ? 1 : (float) (Math
				.round(currentTotalRisk * 1000.0f) / 1000.0f);

//		this.myCurrentRiskTF.setText(String.valueOf(currentTotalRisk));

//		if (DisclosurePolicyRiskEvaluator.IS_DEBUG) {
			logger.writeLog(Level.INFO, "identifier risk: " + currentIdentifierRisk
					+ " sensitive risk: " + currentSensitiveRisk);
//		}

		// update bootstrapping list
		BootstrapRRList.getInstance().updateBootstrappingList(currentTotalRisk);

		// check if the current risk is smaller than the maximal risk (given by
		// user)
		if (currentTotalRisk > Policy.getInstance().getMaxRisk()) {

			// check that bootstrapping data exists, otherwise return
			if (BootstrapRRList.getInstance().isEmpty()) {

				return;
			}

			// compute the distance of all RiskResults with lower risk
			BootstrapRRList.getInstance().computeDistances();

			// reset list walker
			BootstrapRRList.getInstance().resetListWalker();
			
		}
	}
}
