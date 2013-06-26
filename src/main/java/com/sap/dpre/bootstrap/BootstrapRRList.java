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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JOptionPane;

//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.policy.Policy;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.policy.Policy;
import com.sap.dpre.risk.RiskResult;

/**
 * this object contains all RiskResults loaded from the bootstrapping file
 * 
 * 
 * 
 */
public class BootstrapRRList {

	private static BootstrapRRList MYBOOTSTRAPRRLIST = new BootstrapRRList();

	private List<RiskResult> myRRList; // list containing all RiskResults
	private int myListWalker; // actual position in the list
	private boolean isMyListWalkerNext; // true if the last move was 'Next'
	private boolean isBootstrappingModified; // indicates if bootstrapping data
	
	// has been modified

	/**
	 * constructor
	 */
	private BootstrapRRList() {

		// initialization
		this.myRRList = new ArrayList<RiskResult>();
		this.myListWalker = 0;
		this.isMyListWalkerNext = true;
		this.isBootstrappingModified = false;
	}

	/**
	 * get the BootstrapRRList instance
	 * 
	 * @return BootstrapRRList instance
	 */
	public static BootstrapRRList getInstance() {

		return MYBOOTSTRAPRRLIST;
	}

	/**
	 * add a RiskResult to the array
	 * 
	 * @param rr
	 *            RiskResult to be added
	 */
	protected void addRiskResult(RiskResult rr) {

		this.myRRList.add(rr);
	}

	/**
	 * search on all RiskResults and find the one with the same binary counter
	 * 
	 * @param i
	 *            position in the list
	 * @return corresponding binary counter RiskResult
	 */
	public RiskResult getRiskResult(int i) {

		return this.myRRList.get(i);
	}

	/**
	 * clear the list and the ListWalker
	 */
	protected void clear() {

		this.myRRList.clear();
		this.myListWalker = 0;
		this.isMyListWalkerNext = true;
	}

	/**
	 * compute the binary distances from the current proposal for all risk
	 * computations
	 */
	public void computeDistances() {

		// get the current binaryCounter
		int binaryCounter = Policy.getInstance()
				.getDisclosedIdentifierColumnsCounter();

		// compute binary distance for all
		for (int i = 0; i < this.myRRList.size(); i++) {
			this.myRRList.get(i).computeDistance(binaryCounter);
		}

		// sort the list according to the bit distance
		Collections.sort(this.myRRList, new Comparator<RiskResult>() {

			public int compare(RiskResult rr1, RiskResult rr2) {
				if (rr1.getDistance() == rr2.getDistance()) {
					if (!rr1.isNegativeDistance() && rr2.isNegativeDistance()) {
						return -1;
					} else if (rr1.isNegativeDistance()
							&& !rr2.isNegativeDistance()) {
						return 1;
					} else {
						return 0;
					}
				} else if (rr1.getDistance() > rr2.getDistance()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		MyLogger logger = MyLogger.getInstance();
		
		if (true) {

			// for debug only... display list order
			for (int i = 0; i < this.myRRList.size(); i++) {

				RiskResult rr = this.myRRList.get(i);
				if (rr.getRisk() <= Policy.getInstance().getMaxRisk()) {
					logger.writeLog(Level.INFO, Integer.toBinaryString(rr
							.getBinaryCounter())
							+ "\tBin: "
							+ rr.getBinaryCounter()
							+ " risk: "
							+ rr.getRisk()
							+ " distance: "
							+ rr.getDistance()
							+ " isNegative: " + rr.isNegativeDistance());
				}
			}
		}
	}

	/**
	 * get the binary counter for the next valid (risk <= max risk) proposal
	 * 
	 * @return binary counter for the next valid (risk <= max risk) proposal
	 */
	public RiskResult getNextRR() {

		// if direction was changed, increase by 2
		if (!this.isMyListWalkerNext) {
			this.myListWalker += 2;
			this.isMyListWalkerNext = true;
		}

		// search the next valid proposal
		int tmpLW = this.myListWalker;
		for (int i = this.myListWalker; i < tmpLW + this.myRRList.size(); i++) {

			// compute the array number
			int tmpNb = i % this.myRRList.size();
			tmpNb = tmpNb < 0 ? tmpNb + this.myRRList.size() : tmpNb;

			// get the RiskResult
			RiskResult rr = this.myRRList.get(tmpNb);

			// increment the list walker
			this.myListWalker++;

			// return binary counter if it is under the max risk threshold
			if (rr.getRisk() < Policy.getInstance().getMaxRisk()) {
				return rr;
			}
		}

		// return null if there is no valid proposal
		return null;
	}

	/**
	 * get the binary counter for the previous valid (risk <= max risk) proposal
	 * 
	 * @return binary counter for the previous valid (risk <= max risk) proposal
	 */
	public RiskResult getPreviousRR() {

		// if direction was changed, decrease by 2
		if (this.isMyListWalkerNext) {
			this.myListWalker -= 2;
			this.isMyListWalkerNext = false;
		}

		// search the previous valid proposal
		int tmpLW = this.myListWalker;
		for (int i = this.myListWalker; i > tmpLW - this.myRRList.size(); i--) {

			// compute the array number
			int tmpNb = i % this.myRRList.size();
			tmpNb = tmpNb < 0 ? tmpNb + this.myRRList.size() : tmpNb;

			// get the RiskResult
			RiskResult rr = this.myRRList.get(tmpNb);

			// decrement the list walker
			this.myListWalker--;

			// return binary counter if it is under the max risk threshold
			if (rr.getRisk() < Policy.getInstance().getMaxRisk()) {

				return rr;
			}
		}

		// return null if there is no valid proposal
		return null;
	}

	/**
	 * reset the ListWalker
	 */
	public void resetListWalker() {

		this.myListWalker = 0;
		this.isMyListWalkerNext = true;
	}

	/**
	 * check the emptiness of the list
	 * 
	 * @return true if the list is empty
	 */
	public boolean isEmpty() {

		// check if the list is empty or contains no valid proposals
		if (this.myRRList.isEmpty()) {

			return true;
		} else {
			// check if there are valid entries (< r_MAX)
			for (int i = 0; i < this.myRRList.size(); i++) {
				if (this.myRRList.get(i).getRisk() < Policy.getInstance().getMaxRisk()) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * get the bootstrapping list size
	 * 
	 * @return bootstrapping list size
	 */
	public int getSize() {

		return this.myRRList.size();
	}

	/**
	 * update bootstrapping data
	 * 
	 * @param newRisk
	 *            risk to be updated
	 */
	public void updateBootstrappingList(float newRisk) {

		// set flag to true
		this.isBootstrappingModified = true;

		// get the current binary counter
		int rrBinaryCounter = Policy.getInstance()
				.getDisclosedIdentifierColumnsCounter();

		// search it on the list and update it
		for (int i = 0; i < this.myRRList.size(); i++) {
			RiskResult rrTmp = this.myRRList.get(i);

			if (rrTmp.getBinaryCounter() == rrBinaryCounter) {

				rrTmp.setRisk(newRisk);

				return;
			}
		}

		// is not on the list, so create & add new RiskResult
		this.myRRList.add(new RiskResult(rrBinaryCounter, newRisk));
	}

	/**
	 * check if the bootstrap data has been changed and ask to save it if it is
	 * the case
	 */
	public boolean checkBeforeClose(long gid) {

		if (this.isBootstrappingModified) {

				// sort the list
				Collections.sort(this.myRRList);

				// save modified bootstrap list
				Thread bSaver = new Thread(new BootstrapSaver(gid));
				bSaver.setDaemon(true);
				bSaver.run();
			}

		// everything OK, return
		return true;
	}
}
