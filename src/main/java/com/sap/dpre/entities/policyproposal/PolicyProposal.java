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
package com.sap.dpre.entities.policyproposal;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sap.dpre.entities.jaxb.policy.Policy;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
		"proposalId",
		"computedRisk",
    "policyProposal"
    
})

@XmlRootElement(name = "PolicyProposal")
public class PolicyProposal {

    @XmlElement(name = "Policy", required = true)
    protected Policy policyProposal;

    @XmlElement(name = "ComputedRisk", required = true)
    protected float computedRisk;

    @XmlElement(name = "PolicyProposalID", required = true)
    protected int proposalId;
    
	/**
	 * @return the policyProposal
	 */
	public Policy getPolicyProposal() {
		return policyProposal;
	}

	/**
	 * @param policyProposal the policyProposal to set
	 */
	public void setPolicyProposal(Policy policyProposal) {
		this.policyProposal = policyProposal;
	}

	/**
	 * @return the computedRisk
	 */
	public float getComputedRisk() {
		return computedRisk;
	}

	/**
	 * @param computedRisk the computedRisk to set
	 */
	public void setComputedRisk(float computedRisk) {
		this.computedRisk = computedRisk;
	}

	/**
	 * @return the proposalID
	 */
	public int getProposalID() {
		return proposalId;
	}

	/**
	 * @param proposalID the proposalID to set
	 */
	public void setProposalID(int proposalID) {
		this.proposalId = proposalID;
	}
    

    
}

