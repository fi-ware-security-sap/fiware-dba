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

import java.util.logging.Level;

import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLConnection;
import com.sap.dpre.mySQL.MySQLQueryFactory;
import com.sap.dpre.ws.DBA_utils;

/**
 * estimation of the risk by computing the ECM (expected number of correct
 * matches, see paper of Kounine & Bezzi) (no correlation between the columns
 * assumed)
 * 
 * 
 * 
 */
public class RiskEvaluator {

	/**
	 * constructor
	 */
	public RiskEvaluator() {
	}

	/**
	 * get the identifier risk when disclosing identifier columns
	 * 
	 * @param columns
	 *            identifier column names to be disclosed
	 * @return risk of a table disclosing identifier columns
	 */
	public float getIdentifierRisk(String[] columns) {

		MyLogger logger = MyLogger.getInstance();

		logger.writeLog(Level.ALL, "Method getIdentifierRisk");

		if (columns.length == 0) {

			// nothing to compute, all columns are hidden
			return 0;
		} else {

			// create new SQLQueryFactory and compute identifier data risk
			return new MySQLQueryFactory().computeIdentifierECM(columns)
					/ MySQLConnection.getInstance().getTableSize();
		}
	}

	/**
	 * get the sensitive risk when disclosing sensitive columns
	 * 
	 * @param columns
	 *            sensitive column names to be disclosed
	 * @return risk of a table disclosing sensitive columns
	 */
	public float getSensitiveRisk(String[] columns) {

		MyLogger logger = MyLogger.getInstance();
		
		logger.writeLog(Level.ALL, "Method getSensitiveRisk");

		if (columns.length == 0) {

			// nothing to compute, all columns are hidden
			return 0;
		} else {

			// create new SQLQueryFactory and compute sensitive data risk
			return new MySQLQueryFactory().computeSensitiveECM(columns)
					/ MySQLConnection.getInstance().getTableSize();
		}
	}

	/**
	 * get the individual column risk
	 * 
	 * @param columnName
	 *            name of the column
	 * @return individual column risk
	 */
	public float getColumnRisk(String columnName) {

		MyLogger logger = MyLogger.getInstance();
		
		logger.writeLog(Level.ALL,"Method getColumnRisk");

		// create new SQLQueryFactory and compute individual column risk
		return new MySQLQueryFactory().computeColumnECM(columnName)
				/ MySQLConnection.getInstance().getTableSize();
	}
}
