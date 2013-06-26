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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;


import com.sap.dpre.log.MyLogger;
//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;
//import com.sap.primelife.dpre.gui.GUI;
//import com.sap.primelife.dpre.policy.DataColumn;
//import com.sap.primelife.dpre.policy.Policy;
import com.sap.dpre.policy.DataColumn;
import com.sap.dpre.policy.Policy;
import com.sap.dpre.ws.DBA_utils;
import com.sun.istack.logging.Logger;
import com.sun.xml.fastinfoset.util.StringArray;

/**
 * builds different kinds of SQL queries and calls them afterwards
 * 
 * 
 * 
 */
public class MySQLQueryFactory {

	/**
	 * constructor
	 */
	public MySQLQueryFactory() {
	}


	/**
	 * takes a db dump and applies it
	 * 
	 * @param dbDump
	 * @return
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public String executeTransaction(InputStream dbDump, long gid) throws IOException, SQLException {

		MyLogger logger = MyLogger.getInstance();
		
		logger.writeLog(Level.ALL, "Method executeTransaction");

		
		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();

		// create a Statement
		Statement stmt = null;

		try {
			// enable transaction support

			/**
			 * we impose to work in a specific DB, chosen by developers
			 */
			Statement useDBStatement = con.createStatement();

			useDBStatement.executeUpdate("use "+MySQLConnection.getInstance().getDatabaseName()+";");

			con.setAutoCommit(false);

			stmt = con.createStatement();

		} catch (Exception e) {
			MyLogger.getInstance().writeLog(Level.ALL, "Problem with DB, cannot select db: " + e.getMessage());
			e.printStackTrace();
		}


		// we read data from dump file line by line, and we append it to 
		// any previously read incomplete SQL statement fragment
		String workingTableName = MySQLQueryExecutor_Util.parseAndApplyTransaction(dbDump, stmt, gid);
		
		con.commit();

		return workingTableName;

	}

	/**
	 * Drops the table used to store the DB dump during the evaluation
	 * @return
	 */
	public boolean dropWorkingTable(long gid) {
		
		
		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();

		// create a Statement
		Statement stmt = null;
		
		String tmpTable = MySQLConnection.getInstance().getTableName();
				
		try {
			stmt = con.createStatement();

					
			stmt.executeUpdate("drop table "+tmpTable+";");

		} catch (Exception e) {
			MyLogger.getInstance().writeLog(Level.ALL, "Problem with DB, cannot drop table: " + e.getMessage());
			e.printStackTrace();
		}


		return true;	
	}
	
	/**
	 * Drops the views created during the evaluation
	 * @return
	 */
	public boolean dropWorkingViews(long gid){
				
		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();

		// create a Statement
		Statement stmt = null;
		String tmpTable = MySQLConnection.getInstance().getTableName();
		
		
		try {
			stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM "+tmpTable);
			StringArray columns = new StringArray();
			while(rs.next()){
				columns.add(rs.getString("Field"));
			}
			for(int i=0; i<columns.getSize(); i++){
				stmt.executeUpdate("DROP VIEW IF EXISTS view_"+MySQLConnection.getInstance().getTableName()+"_"+columns.get(i).toLowerCase());
			}

		} catch (Exception e) {
			MyLogger.getInstance().writeLog(Level.ALL, "Problem with DB, cannot drop views: " + e.getMessage());
			e.printStackTrace();
		}


		return true;
		
	}


	/**
	 * check the connection status
	 * 
	 * @return true if connection is OK
	 */
	public boolean checkConnection() {

		// create and execute query
		String query = "SELECT 1 AS test FROM "
				+ MySQLConnection.getInstance().getTableName() + " LIMIT 1;";
		try {

			// execute query
			ResultSet rs = new MySQLQueryExecutor().executeSelect(query);

			// parse result
			while (rs.next()) {
				Policy.getInstance().addDataColumn(
						new DataColumn(rs.getString("test")));
			}

			// close result set
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());

			return false;
		}

		return true;
	}

	/**
	 * get the column names of the database
	 * @throws SQLException 
	 */
	public void loadDataColumns() throws SQLException {

		// create and execute query
		String query = "SHOW COLUMNS FROM "
				+ MySQLConnection.getInstance().getTableName() + ";";
		// execute query
		ResultSet rs = new MySQLQueryExecutor().executeSelect(query);

		
		// parse result
		while (rs.next()) {
			Policy.getInstance().addDataColumn(
					new DataColumn(rs.getString("Field")));
		}

		// close result set
		rs.close();

	}

	/**
	 * check for policy column if the view exists, if not, create it
	 * 
	 * @param columnname
	 *            name of the column to be checked
	 */
	public void checkView(String columnname) {

		// create and execute query
		String query = "SELECT table_name FROM information_schema.tables "
				+ "WHERE table_schema = '"
				+ MySQLConnection.getInstance().getDatabaseName()
				+ "' AND table_name = 'view_"
				+ MySQLConnection.getInstance().getTableName() + "_"
				+ columnname + "';";

		int rowCount = 0;
		try {

			// execute query and count the rows
			ResultSet rs = new MySQLQueryExecutor().executeSelect(query);

			rs.last();
			rowCount = rs.getRow();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}

		// if there are no rows returned, create view
		if (rowCount == 0) {
			this.createView(columnname);
		}
	}

	/**
	 * create view
	 * 
	 * @param columnname
	 *            name of the column which the view will be created
	 */
	private void createView(String columnname) {

		// prepare query
		String query = "CREATE VIEW view_"
				+ MySQLConnection.getInstance().getTableName() + "_"
				+ columnname + " AS SELECT " + columnname + ", COUNT("
				+ columnname + ") AS nb" + columnname + " FROM "
				+ MySQLConnection.getInstance().getTableName() + " GROUP BY "
				+ columnname + " ORDER BY NULL;";

		// execute query
		try {
			new MySQLQueryExecutor().executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}
	}

	/**
	 * computes the identifier risk when disclosing certain identifier columns
	 * 
	 * @param columns
	 *            identifier columns to be disclosed
	 * @return computed identifier risk
	 */
	public float computeIdentifierECM(String[] columns) {

		// get the column String from the String array
		String columnStr = this.createStringFromArray(columns);

		// prepare query that counts the total of different rows (by a specific
		// grouping) in the table
		String query = "SELECT COUNT(*) AS ecm FROM (SELECT COUNT(*) FROM "
				+ MySQLConnection.getInstance().getTableName() + " GROUP BY "
				+ columnStr + " ORDER BY NULL) AS myQuery;";

		// execute the query
		try {
			ResultSet rs = new MySQLQueryExecutor().executeSelect(query);

			// parse the computed risk
			rs.next();
			float risk = rs.getFloat("ecm");
			rs.close();

			return risk;
		} catch (SQLException e) {
			e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}

		// if we get here, there is a problem computing the risk
		return Float.NaN;
	}

	/**
	 * computes the sensitive risk when disclosing certain sensitive columns
	 * 
	 * @param columns
	 *            sensitive columns to be disclosed
	 * @return computed sensitive risk
	 */
	public float computeSensitiveECM(String[] columns) {

		// prepare query
		StringBuilder strB = new StringBuilder("SELECT SUM(");

		for (int i = 0; i < columns.length; i++) {
			strB.append("(SELECT 1/nb" + columns[i] + " from view_");
			strB.append(MySQLConnection.getInstance().getTableName() + "_");
			strB.append(columns[i] + " where view_");
			strB.append(MySQLConnection.getInstance().getTableName() + "_");
			strB.append(columns[i] + "." + columns[i] + " = ");
			strB.append(MySQLConnection.getInstance().getTableName() + ".");
			strB.append(columns[i] + ")");

			if (i < columns.length - 1) {
				strB.append(" + ");
			}
		}

		strB.append(") AS ecm FROM ");
		strB.append(MySQLConnection.getInstance().getTableName() + ";");

		// execute the query
		try {
			ResultSet rs = new MySQLQueryExecutor().executeSelect(strB
					.toString());

			// parse the computed risk
			float risk = Float.NaN;

			rs.next();
			risk = rs.getFloat("ecm");
			rs.close();
			return risk;
		} catch (SQLException e) {
				e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}

		// if we get here, there is a problem computing the risk
		return -1;
	}

	/**
	 * get the number of rows of a table
	 * 
	 * @return number of rows of a table
	 */
	public int getTableSize() {

		// prepare query that counts the number of rows
		String query = "SELECT COUNT(*) AS size FROM "
				+ MySQLConnection.getInstance().getTableName() + ";";

		// execute the query
		try {
			ResultSet rs = new MySQLQueryExecutor().executeSelect(query);

			// parse the computed risk
			rs.next();
			int size = rs.getInt("size");
			rs.close();

			return size;
		} catch (SQLException e) {
				e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}

		// if we get here, there is a problem counting the table size
		return -1;
	}

	/**
	 * export table information of specified columns
	 * 
	 * @param columns
	 *            columns of the table to be disclosed
	 */
	public ResultSet exportQuery2File(String[] columns) {

		// get the column String from the String array
		String columnStr = this.createStringFromArray(columns);

		// prepare query that selects the specified columns
		String query = "SELECT " + columnStr + " FROM "
				+ MySQLConnection.getInstance().getTableName() + ";";

		// execute the query and return the ResultSet
		try {
			return new MySQLQueryExecutor().executeSelect(query);
		} catch (SQLException e) {
				e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}

		// if we get here something with the query went wrong
		return null;
	}

	/**
	 * create a comma delimited String from a String array
	 * 
	 * @param strArray
	 *            String array to be transformed to a String
	 * @return comma delimited String
	 */
	private String createStringFromArray(String[] strArray) {

		// build a String from the String array
		String str = "";
		for (int i = 0; i < strArray.length; i++) {

			// add the array item to the String
			str += strArray[i];

			// append a comma if it is not the last one
			if (i < strArray.length - 1) {
				str += ", ";
			}
		}

		return str;
	}

	/**
	 * compute the estimated correct match value for a column
	 * 
	 * @param columnName
	 *            name of the column
	 * @return estimated correct match value for a column
	 */
	public float computeColumnECM(String columnName) {

		// prepare query that counts the total of different rows (by a specific
		// grouping) in the table
		String query = "SELECT COUNT(*) AS ecm FROM (SELECT COUNT(*) FROM "
				+ MySQLConnection.getInstance().getTableName() + " GROUP BY "
				+ columnName + " ORDER BY NULL) AS myQuery;";

		// execute the query
		try {
			ResultSet rs = new MySQLQueryExecutor().executeSelect(query);

			// parse the computed risk
			rs.next();
			float risk = rs.getFloat("ecm");
			rs.close();

			return risk;
		} catch (SQLException e) {
				e.printStackTrace();

			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}

		// if we get here, there is a problem computing the risk
		return Float.NaN;
	}

	/**
	 * Stores the result of the computation in the DB 
	 * @param GID
	 * @param result
	 * @param computed
	 * @return true if the operation is done in the right way, false otherwise
	 */
	public boolean storeResult(long GID, String result, boolean computed){
		String query = "INSERT INTO results (GID, result, computed) VALUES ("+GID+", '"+result+"', "+computed+")";
		boolean executed = false;
				
		try {
			executed = new MySQLQueryExecutor().executeInsert(query);
		} catch (SQLException e) {
			e.printStackTrace();
			MyLogger.getInstance().writeLog(Level.SEVERE, e.getLocalizedMessage());
			return executed;
		}


		return executed;
	}


	public String getResult(long gid) throws SQLException {
		
		String result = new MySQLQueryExecutor().getResult(gid);
		return result;
	}
	
	/**
	 * Stores the result of the computation in the DB 
	 * @param GID
	 * @param result
	 * @param computed
	 * @return true if the operation is done in the right way, false otherwise
	 */
	public boolean storeDeepSearchResult(long gid, HashMap<Integer, Float> deepsearchResults ){
		boolean result = false ;
		

		 try {
			result = new MySQLQueryExecutor().storeResult(gid, "deepsearchsave", deepsearchResults);
		} catch (SQLException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in mysqlqueryfactory storeDeepSearchResult: "+e.getMessage());
		}


		 return result;
	}
	
	public boolean storeBootstrapResult(long gid, HashMap<Integer, Float> bootstrapResults ){
		boolean result = false ;
		

		 try {
			result = new MySQLQueryExecutor().storeResult(gid, "bootstrapsave", bootstrapResults);
		} catch (SQLException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in mysqlqueryfactory bootstrapResult: "+e.getMessage());
		}


		 return result;
	}
	
	public boolean storeRiskResult(long gid, HashMap<Integer, Float> bootstrapResults ){
		boolean result = false ;
		

		 try {
			result = new MySQLQueryExecutor().storeResult(gid, "riskcolumnsave", bootstrapResults);
		} catch (SQLException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in mysqlqueryfactory storeRiskResult: "+e.getMessage());
		}


		 return result;
	}
	
	
	public HashMap<Integer, Float> getDeepSearchResult(long gid) {

		try {
			HashMap<Integer, Float> result = new MySQLQueryExecutor().loadResult(gid, "deepsearchsave");
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			MyLogger.getInstance().writeLog(Level.SEVERE, "error in getDeepSearchResult retrieving: "+e.getMessage());
			return null;
		}
	}
	
	public HashMap<Integer, Float> getBootstrapResult(long gid) {
		
		try {
			HashMap<Integer, Float> result = new MySQLQueryExecutor().loadResult(gid, "bootstrapsave");
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			MyLogger.getInstance().writeLog(Level.SEVERE, "error in getDeepSearchResult retrieving: "+e.getMessage());
			return null;
		}

	}
	
	public HashMap<Integer, Float> getRiskColumnResult(long gid) {
		
		try {
			HashMap<Integer, Float> result = new MySQLQueryExecutor().loadResult(gid, "riskcolumnsave");
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			MyLogger.getInstance().writeLog(
					Level.SEVERE,
					"error in getRiskColumnResult retrieving: "
							+ e.getMessage());
			return null;
		}

	}


	public int deleteRiskColumnResult(long gid) {
		try {
			int result = new MySQLQueryExecutor().deleteResult(gid, "riskcolumnsave");
			
			return result;
			
		} catch (SQLException e) {

			e.printStackTrace();
			MyLogger.getInstance().writeLog(Level.SEVERE,
					"error in deleteRiskColumnResult : " + e.getMessage());
		}
		return -1;
	}
	
	public boolean savePolicy(long gid, 
				com.sap.dpre.entities.jaxb.policy.Policy policyToSave)  {
		try {
		return new MySQLQueryExecutor().savePolicy(gid, policyToSave);
		} catch (SQLException e ) {
			e.printStackTrace();
			MyLogger.getInstance().writeLog(Level.SEVERE,
					"error in MySQLFactory.savePolicy: " + e.getMessage());
			return false;
		}
	}
	
	public com.sap.dpre.entities.jaxb.policy.Policy loadPolicy(long gid)  {
		try {
			com.sap.dpre.entities.jaxb.policy.Policy  result = 
					new MySQLQueryExecutor().loadPolicy(gid);
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			MyLogger.getInstance().writeLog(
					Level.SEVERE,
					"error in loadPolicy retrieving: "
							+ e.getMessage());
			return null;
		}

	
	}
	
	public boolean checkDBExistence() {
		
		MySQLQueryExecutor sqlFactory = new MySQLQueryExecutor();

		if (sqlFactory.checkTableExistance("results")
				& sqlFactory.checkTableExistance("deepsearchsave")
				& sqlFactory.checkTableExistance("bootstrapsave")
				& sqlFactory.checkTableExistance("riskcolumnsave")
				& sqlFactory.checkTableExistance("policies")) {
			return true;
		} else {
			return false;
		}
	} 
	
}
