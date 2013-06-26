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
package com.sap.dpre.ws;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.naming.Context;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sap.dpre.entities.jaxb.riskcolumnresult.Column;
import com.sap.dpre.entities.jaxb.riskcolumnresult.RiskColumnResult;
import com.sap.dpre.entities.policyproposal.PolicyProposalResult;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLQueryFactory;

public class DBA_factory {
	
	private String operationDbName;
	private String resultDbName;

	public DBA_factory(String operationDbName, String resultDbName) {
		this.operationDbName = operationDbName;
		this.resultDbName = resultDbName;
	}

	/**
	 * Create a DBA_utils object to start the policy evaluation
	 * @param policyTempFileToTransfer
	 * @param dbSQLDumpTempFileToTransfer
	 * @param initialContext
	 * @param gid
	 * @param tableName
	 * @param noDetach if true, computation will be started synchronously
	 */
	public void evaluatePolicy(File policyTempFileToTransfer,
			File dbSQLDumpTempFileToTransfer,
			Context initialContext, long gid, String tableName, boolean noDetach){

		new DBA_utils(operationDbName, resultDbName).startThread(
					policyTempFileToTransfer, 
					dbSQLDumpTempFileToTransfer, 
					initialContext, 
					gid, 
					tableName,
					noDetach);
	}

	/**
	 * Create a DBA_utils object to start the policy evaluation
	 * @param policyTempFileToTransfer
	 * @param dbSQLDumpTempFileToTransfer
	 * @param initialContext
	 * @param gid
	 * @param tableName
	 * @param noDetach if true, computation will be started synchronously
	 */
	public void evaluateDeepSearch(File policyTempFileToTransfer,
			File dbSQLDumpTempFileToTransfer,
			Context initialContext, long gid, String tableName, boolean noDetach, float maxRisk){

		new DBA_utils(operationDbName, resultDbName).startDeepSearchThread(
					policyTempFileToTransfer, 
					dbSQLDumpTempFileToTransfer, 
					initialContext, 
					gid, 
					tableName,
					noDetach,
					maxRisk);
	}

	
	/**
	 * Create a DBA_utils object to retrieve the result of the policy evaluation associated to the give ID
	 * @param gid
	 * @return Result of the policy evaluation
	 */
	public String getResult(long gid){
		return new DBA_utils(operationDbName, resultDbName).getPolicyEvaluationResult(gid, null);
	}
	
	public String getRiskperColumn(File dbSQLDumpTempFileToTransfer,
			Context initialContext, long gid, String tableName) {
		
		new DBA_utils(operationDbName, resultDbName).startRiskThread(
				dbSQLDumpTempFileToTransfer,
				initialContext,
				gid,
				tableName
				);
		
		return null;
	}

	public String getRiskColumnResult(long gid) {
		
		HashMap<Integer, Float> results = 
				new DBA_utils(operationDbName, resultDbName).getRiskColumnResult(gid, null);
		
		String toReturn = "Risk per column for GID: "+gid;
		toReturn += "\n";
		
		   for (Map.Entry<Integer, Float> e : results.entrySet()) {
			   toReturn += "Column #"+e.getKey().intValue();
			   toReturn += ", risk: "+e.getValue().floatValue();
			   toReturn += "\n";
	        }
		   return toReturn;
	}
	
	public RiskColumnResult getRiskColumnResultXML(long gid) {

		
		HashMap<Integer, Float> results = 
				new DBA_utils(operationDbName, resultDbName).getRiskColumnResult(gid, null);

		
		RiskColumnResult result = new RiskColumnResult();

		for (Map.Entry<Integer, Float> e : results.entrySet()) {

			Column column = new Column();

			column.setColumnId(e.getKey().intValue());
			column.setRisk(e.getValue().floatValue());

			result.getColumn().add(column);

		}

		return result;
		
	}
	/**
	 * default values: count = 10, offset = 0.
	 * @param gid
	 * @param count
	 * @param offset
	 * @return
	 */
	public PolicyProposalResult getDeepSearchkResultXML(long gid, int count, int offset ) {
		
		if (count == -1) {
			count = 10;
		}
		
		return new DBA_utils(operationDbName, resultDbName).getDeepSearchResults(gid, count, offset);
	}
	
}
