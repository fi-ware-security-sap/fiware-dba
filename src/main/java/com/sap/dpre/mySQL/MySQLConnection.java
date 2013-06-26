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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.sap.dpre.log.MyLogger;
//import com.sap.dpre.ws.DBA_utils;

/**
 * keeps the connection information
 * 
 * 
 * 
 */
public class MySQLConnection {

	private static MySQLConnection MY_SQL_CONNECTION = null; 
	//	private static MySQLConnection MY_SQL_CONNECTION = new MySQLConnection();

	private Connection myConnection;
	private DataSource dataSource = null;

	//	private String myUrl = "jdbc:mysql://10.55.133.210:3306/";
	private String myDatabaseName = "census";
	public void setMyDatabaseName(String myDatabaseName) {
		this.myDatabaseName = myDatabaseName;
	}

	public void setMyTableName(String myTableName) {
		this.myTableName = myTableName;
	}

	//	private String myTableName = "census_medium";
	private String myTableName = "working_table";
	//	private String myUser = "root";
	//	private String myPwd = "";

	private boolean isOk;
	private int myTableSize=-1;


	/**
	 * constructor
	 */
	private MySQLConnection(InitialContext initContext) {

		try {
			// Get DataSource
			//			Context initContext  = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/censusdb");

			// Get Connection and Statement
			myConnection = dataSource.getConnection();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}    catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public MySQLConnection(Connection conn) {
		myConnection = conn;
	}

	public boolean verifyConnection() {
		
		if (myConnection != null) {
			
			try {
				if (myConnection.isValid(1)) {
					return true;
				} else {
					myConnection.close();
					myConnection = dataSource.getConnection();
					return true;
				}
			} catch (Exception e) {
				System.err.println("Error in verifyConnection: "+e.getMessage());
				e.printStackTrace();
				MyLogger.getInstance().writeLog(Level.ALL, "error detected in DB connection verification: "+e.getMessage());
				return false;
			}
							
			
		} else {
			return false;
		}
		
	}
	

	/**
	 * get the MYSQLConnection instance
	 * 
	 * @param initContext the initial context of the application container
	 * @param noConnCheck do not perform the initial check (i.e., on a void database) 
	 * @return MYSQLConnection instance
	 */
	public static MySQLConnection getInstance(InitialContext initContext, boolean noConnCheck) {

		if (MY_SQL_CONNECTION == null) {
			MY_SQL_CONNECTION = new MySQLConnection(initContext);

			if(!noConnCheck) {
				// check connection
				MY_SQL_CONNECTION.isOk = new MySQLQueryFactory().checkConnection();

				if (!MY_SQL_CONNECTION.isOk) {
					MyLogger.getInstance().writeLog(Level.ALL, "DB connection is NOT ok!");
					System.err.println("DB connection is NOT ok!");
					return null;
				}


				// initialization
				MY_SQL_CONNECTION.myTableSize = new MySQLQueryFactory().getTableSize();
			}

		} else {
			if (MY_SQL_CONNECTION.verifyConnection() == false) {
				MY_SQL_CONNECTION = null;
				MY_SQL_CONNECTION = MySQLConnection.getInstance(initContext, noConnCheck);
			}
		}

		return MY_SQL_CONNECTION;
	}

	/**
	 * injection of MySQLConnection in case of unit tests
	 *  
	 * @param conn
	 * @return
	 */
	public static MySQLConnection getInstance(java.sql.Connection conn) {
		
		if (MY_SQL_CONNECTION == null) {
			try {
				MY_SQL_CONNECTION = new MySQLConnection(conn);
			} catch (Exception e) {
				e.printStackTrace();
				MyLogger.getInstance().writeLog(Level.ALL, "problem in getting MySQL connection!");
				return null;
			}
		} else {
			if (MY_SQL_CONNECTION.verifyConnection() == false) {
				MY_SQL_CONNECTION = null;
				MY_SQL_CONNECTION = MySQLConnection.getInstance(conn);
			}
		}
		
		return MY_SQL_CONNECTION;
	}

	public static MySQLConnection getInstance() {

		if (MY_SQL_CONNECTION == null) {
			try {
				MY_SQL_CONNECTION = new MySQLConnection(new InitialContext());
			} catch (NamingException e) {
				e.printStackTrace();
				MyLogger.getInstance().writeLog(Level.ALL, "problem in getting MySQL connection!");
				return null;
			}
		} else {
			if (MY_SQL_CONNECTION.verifyConnection() == false) {
				MY_SQL_CONNECTION = null;
				MY_SQL_CONNECTION = MySQLConnection.getInstance();
			}
		}

		return MY_SQL_CONNECTION;
	}


	/**
	 * Opens the  DB Connection.
	 * Only tableName is used.
	 * with respect to the original 
	 * implementation, other credentials are
	 * not used, as they are provided by 
	 * context.xml in META-INF directory
	 * 
	 * @param tableName
	 *            name of table to use
	 * @param databaseName
	 * 			  name of database to use          
	 * 
	 */
	public void setConnectionCredentials(String databaseName,
			String tableName) {


		this.myDatabaseName = databaseName;
		this.myTableName = tableName;

		// check connection
		this.isOk = new MySQLQueryFactory().checkConnection();

		if (!this.isOk) {
			return;
		}

		// initialization
		this.myTableSize = new MySQLQueryFactory().getTableSize();

	}

	/**
	 * Opens the  DB Connection.
	 * Only tableName is used.
	 * with respect to the original 
	 * implementation, other credentials are
	 * not used, as they are provided by 
	 * context.xml in META-INF directory
	 * VERY IMPORTANT: it sets isOK field
	 * to true, assuming that the user knows 
	 * what she is doing if she uses noCheckConnection! 
	 * 
	 * @param tableName
	 *            name of table to use
	 * @param databaseName
	 * 			  name of database to use          
	 * 
	 */
	public void setConnectionCredentials(String databaseName,
			String tableName, boolean noCheckConnection) {

		MyLogger logger = MyLogger.getInstance();
		
		logger.writeLog(Level.ALL, "Method setConnectionCredentials, databaseName:"+databaseName+", tablaName:"+tableName);


		this.myDatabaseName = databaseName;
		this.myTableName = tableName;

		if(!noCheckConnection) {
			// check connection
			this.isOk = new MySQLQueryFactory().checkConnection();

			if (!this.isOk) {
				return;
			}

			// initialization
			this.myTableSize = new MySQLQueryFactory().getTableSize();


		} else {
			this.isOk = true;
		}


	}


	/**
	 * get the size of the table (row count )
	 * 
	 * @return size of the table (row count
	 */
	public int getTableSize() {

		/**
		 *  if myTableSize == -1, it means that was not
		 *  previously initialized 
		 */
		if (this.myTableSize == -1) {
			// initialization
			this.myTableSize = new MySQLQueryFactory().getTableSize();
		}
		return this.myTableSize;
	}

	/**
	 * get the connection status
	 * 
	 * @return true if the connection is OK
	 */
	public boolean getConnectionStatus() {

		return this.isOk;
	}

	/**
	 * get the mySQL connection
	 * 
	 * @return mySQL connection
	 */
	protected Connection getConnection() {

		return this.myConnection;
	}

	/**
	 * get the table name
	 * 
	 * @return table name
	 */
	public String getTableName() {

		return this.myTableName;
	}

	/**
	 * get the database name
	 * 
	 * @return database name
	 */
	public String getDatabaseName() {

		return this.myDatabaseName;
	}

	public boolean closeConnection() {
		try {
			myConnection.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
