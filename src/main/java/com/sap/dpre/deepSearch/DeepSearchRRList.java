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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.dpre.risk.RiskResult;

/**
 * object containing all RiskResults found by DeepSearch
 * 
 * 
 * 
 */
class DeepSearchRRList {

	private static DeepSearchRRList MYDEEPSEARCHRRLIST = new DeepSearchRRList();

	private List<RiskResult> myRRList; // list containing all found RiskResults

	/**
	 * constructor
	 */
	private DeepSearchRRList() {

		// initialization
		this.myRRList = new ArrayList<RiskResult>();
	}

	/**
	 * get the DeepSearchRRList instance
	 * 
	 * @return DeepSearchRRList instance
	 */
	protected static DeepSearchRRList getInstance() {

		return MYDEEPSEARCHRRLIST;
	}

	/**
	 * add a RiskResult to the list
	 * 
	 * @param rr
	 *            RiskResult to be added
	 */
	protected void addRiskResult(RiskResult rr) {

		this.myRRList.add(rr);
	}

	/**
	 * clear the RiskResult list
	 */
	protected void clear() {

		this.myRRList.clear();
	}

	/**
	 * sort the RiskResult list
	 */
	protected void sort() {

		Collections.sort(this.myRRList);
	}

	/**
	 * get the size of the RiskResult list
	 * 
	 * @return size of the RiskResult list
	 */
	protected int getSize() {

		return this.myRRList.size();
	}

	/**
	 * get an element of the RiskResult list
	 * 
	 * @param i
	 *            position of the element
	 * @return element of the RiskResult list
	 */
	protected RiskResult getRiskResult(int i) {

		return this.myRRList.get(i);
	}
}
