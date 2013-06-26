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
import java.io.IOException;
import java.util.Random;

import javax.jws.WebService;

import com.sap.dpre.entities.TransferredFile;

@WebService(
		endpointInterface="com.sap.dpre.ws.DbAnonymizerJAX_WS",
		serviceName = "DbAnonymizer")
public class DbAnonymizerJAX_WS_Impl implements DbAnonymizerJAX_WS {

	private DBA_factory dba_factory;

	public DbAnonymizerJAX_WS_Impl(DBA_factory dba_factory) {
		this.dba_factory = dba_factory;
	}

	/**
	 * @see com.sap.dpre.ws.DbAnonymizerJAX_WS#evaluatePolicySimple(float max_risk,
			TransferredFile policyFileToTransfer, TransferredFile dbSQLDumpFileToTransfer, String table_name)
	 */
	@Override
	public String evaluatePolicy(float max_risk,
			TransferredFile policyFileToTransfer,
			TransferredFile dbSQLDumpFileToTransfer, String table_name){

		Random generator = new Random(System.currentTimeMillis());
		
		//Generates the ID which will be associated to the evaluation
		long gid = generator.nextLong();
		if(gid<0)
			gid = gid*(-1);

		TempFiles tf = new TempFiles();
		File dbDumpTempFile = null;
		File policyTempFile = null;
		try{
			dbDumpTempFile = tf.createTempFile(dbSQLDumpFileToTransfer);
			policyTempFile = tf.createTempFile(policyFileToTransfer);
			
			dbDumpTempFile.deleteOnExit();
			policyTempFile.deleteOnExit();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		dba_factory.evaluatePolicy(policyTempFile, dbDumpTempFile, null, gid, table_name, false);

				return "ID: --"+gid+"--";
	}

	/**
	 * @see com.sap.dpre.ws.DbAnonymizerJAX_WS#getPolicyResult(long gid)
	 */
	@Override
	public String getPolicyResult(long gid){
		return dba_factory.getResult(gid);
	}

}
