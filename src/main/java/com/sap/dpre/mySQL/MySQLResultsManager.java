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
package com.sap.dpre.mySQL;

import java.sql.SQLException;

/**
 * Class used to manage the results of the evaluations
 * 
 *
 */
public class MySQLResultsManager {

	private long gid;
	private String result;
	private boolean computed;
	
	/**
	 * Constructor
	 */
	public MySQLResultsManager() {
	}
	
	/**
	 * Set the variable computed to true if the evaluation is correctly computed, false otherwise
	 */
	private void setComputedValue(){
		if(result.startsWith("-"))
			computed = false;
		else
			computed = true;
	}
	
	/**
	 * Store the result of the evaluation associated to the given gid
	 * @param gid
	 * @param result
	 * @return true if correctly stored, false otherwise
	 */
	public boolean storeResult(long gid, String result){
		this.gid = gid;
		this.result = result;
		
		setComputedValue();
		
		MySQLQueryFactory mySQLFactory = new MySQLQueryFactory();
		
		return mySQLFactory.storeResult(gid, result, computed);
	}
	
	/**
	 * Get the result of the evaluation associated to the given gid
	 * @param gid
	 * @return the result of the evaluation
	 */
	public String getResult(long gid){
		
		MySQLQueryFactory mySQLFactory = new MySQLQueryFactory();
		
		try {
			return mySQLFactory.getResult(gid);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Error in retrieving the requested result";
	}
	
}
