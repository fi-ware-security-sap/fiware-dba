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

import com.sap.dpre.policy.Policy;

/**
 * representation of the binary counter and its corresponding risk
 * 
 * 
 * 
 */
public class RiskResult implements Comparable<RiskResult> {

	private int binaryCounter;
	private float risk;
	private int distance;
	private boolean isNegativeDistance;

	/**
	 * constructor
	 * 
	 * @param binaryCounter
	 *            column combination represented in a binary counter
	 * @param risk
	 *            computed risk
	 */
	public RiskResult(int binaryCounter, float risk) {

		// initialization
		this.binaryCounter = binaryCounter;
		this.risk = risk;
		this.distance = -1;
		this.isNegativeDistance = false;
	}

	/**
	 * get the binary counter
	 * 
	 * @return binary counter
	 */
	public int getBinaryCounter() {

		return this.binaryCounter;
	}

	/**
	 * get the risk
	 * 
	 * @return risk
	 */
	public float getRisk() {

		return this.risk;
	}

	/**
	 * set the risk
	 * 
	 * @param risk
	 */
	public void setRisk(float risk) {

		this.risk = risk;
	}

	/**
	 * get the distance towards an other previously computed binary counter
	 * 
	 * @return distance between the binary counters
	 */
	public int getDistance() {

		return this.distance;
	}

	/**
	 * indicates that if a column was hidden in the original binary counter and
	 * is disclosed in this binary counter
	 * 
	 * @return true if the distance is negative
	 */
	public boolean isNegativeDistance() {

		return this.isNegativeDistance;
	}

	/**
	 * compute distance between the original binary counter and this one
	 * 
	 * @param binaryCounter
	 *            original binary counter
	 */
	public void computeDistance(int binaryCounter) {

		// reset distance
		this.distance = 0;

		// convert from integer to binary String of a certain length (append '0'
		// if needed)
		String i1 = Integer.toBinaryString(binaryCounter);
		String i2 = Integer.toBinaryString(this.binaryCounter);

		while (i1.length() < Policy.getInstance().getBinaryCounterSize()) {
			i1 = "0" + i1;
		}
		while (i2.length() < Policy.getInstance().getBinaryCounterSize()) {
			i2 = "0" + i2;
		}

		// compare each char ,1:hidden, 0: disclosed
		for (int i = 0; i < Policy.getInstance().getBinaryCounterSize(); i++) {

			if (i1.charAt(i) == '0' && i2.charAt(i) == '1') {
				this.distance++;
			} else if (i1.charAt(i) == '1' && i2.charAt(i) == '0') {
				this.distance++;
				this.isNegativeDistance = true;
			}
		}
	}

	/**
	 * comparison between two RiskResult objects
	 */
	public int compareTo(RiskResult rr) {

		// compare by risk
		if (this.risk == rr.risk) {
			if (this.binaryCounter > rr.binaryCounter) {
				return 1;
			} else if (this.binaryCounter < rr.binaryCounter) {
				return -1;
			}
			return 0;
		} else if (this.risk > rr.risk) {
			return 1;
		} else {
			return -1;
		}
	}
}
