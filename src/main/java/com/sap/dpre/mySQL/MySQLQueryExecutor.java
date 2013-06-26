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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.PreparedStatement;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.sap.dpre.entities.jaxb.policy.Column;
import com.sap.dpre.entities.jaxb.policy.Policy;
import com.sap.dpre.log.MyLogger;
import com.sun.istack.logging.Logger;

/**
 * executes different SQL queries on a MySQL database
 * 
 * 
 * 
 */
class MySQLQueryExecutor {

	/**
	 * constructor
	 */
	protected MySQLQueryExecutor() {
	}

	/**
	 * execute a select query
	 * 
	 * @param selectQuery
	 *            query to be executed
	 * @return ResultSet containing query result
	 * @throws SQLException
	 */
	protected ResultSet executeSelect(String selectQuery) throws SQLException {

		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();

		// create a Statement
		Statement stmt = con.createStatement();

		// execute and return query
		return stmt.executeQuery(selectQuery);
	}

	
	/**
	 * execute a update query
	 * 
	 * @param updateQuery
	 *            update query
	 * @throws SQLException
	 */
	protected void executeUpdate(String updateQuery) throws SQLException {

		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();

		// create a Statement
		Statement stmt = con.createStatement();

		// execute query
		stmt.executeUpdate(updateQuery);

		stmt.close();
	}

	
	/**
	 * execute an insert query
	 * @param insertQuery
	 * @throws SQLException
	 */
	protected boolean executeInsert(String insertQuery) throws SQLException{

		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();
		con.setAutoCommit(false);
		
		// create a Statement
		Statement stmt = con.createStatement();

		// execute query
		boolean executed = stmt.execute(insertQuery);

		stmt.close();
		con.commit();
		return executed;
	}
	
	/**
	 * GetResult uses a PreparedStatement, in order to protect 
	 * from SQL injection
	 * 
	 * @param query
	 * @param gid
	 * @return
	 * @throws SQLException
	 */
	public String getResult(long gid) throws SQLException {

		String query = "SELECT result FROM results WHERE GID = ?";
		
		// open connection to the MySQL database
		Connection con = MySQLConnection.getInstance().getConnection();
		con.setAutoCommit(false);
	    
		// create a PreparedStatement
		PreparedStatement pstmt = con.prepareStatement(query);
		pstmt.setLong(1, gid);
		pstmt.addBatch();
		
		ResultSet rs = pstmt.executeQuery();
		con.commit();
		rs.next();
		String result = "empty set";
//		if (rs != null && rs.getFetchSize() > 0) {
		if (rs != null ) {
			try {
				result = rs.getString("result");
			} catch (SQLException e) {
				MyLogger.getInstance().writeLog(Level.SEVERE,
						"error in retrieving MySQLQueryExecutor.getResult: "+e.getMessage());
				System.err.println("error in retrieving MySQLQueryExecutor.getResult: "+e.getMessage());
			}
		}
		pstmt.close();
		
		return result;
	}
	
	
	public boolean checkTableExistance(String tableName) {
	
		Connection con = MySQLConnection.getInstance().getConnection();

		try {
			DatabaseMetaData metaData = con.getMetaData();
			
			ResultSet rs =  metaData.getTables(null, null, tableName, null);
			
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
			
		} catch (SQLException e) {

			e.printStackTrace();
			

			
			return false;
		}
		
	}

	
	
	public boolean storeResult(long gid, String tableName, HashMap<Integer, Float> deepsearchResults) throws SQLException {

		String query = "INSERT INTO "+ tableName +" (GID, columncounter, columnrisk) VALUES (?, ?, ?)";
		
		Connection con = MySQLConnection.getInstance().getConnection();
		
		PreparedStatement insert = null;

		boolean oldautocommit = con.getAutoCommit();

	    try {
	        con.setAutoCommit(false);
	        insert = con.prepareStatement(query);

	        for (Map.Entry<Integer, Float> e : deepsearchResults.entrySet()) {
	        	insert.setLong(1, gid);
	        	insert.setInt(2, e.getKey().intValue());
	        	insert.setFloat(3, e.getValue().floatValue());
	        	insert.executeUpdate();
	            con.commit();
	        }
	        
	        con.setAutoCommit(oldautocommit);
	        
			return true;
	        
	    } catch (SQLException e ) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in inserting " +
					" results in " + tableName + " for GID:"+
					gid+" : "+e.getMessage());
	    	
	        if (con != null) {
	            try {
					System.err
							.print("Transaction is being rolled back in" +
									" MySQLQueryExecutor.storeResult");
	                con.rollback();
	            } catch(SQLException excep) {
	    	    	MyLogger.getInstance().writeLog(Level.ALL,
	    	    			"error in *reverting* the insertion of " +
	    	    			tableName + " results for GID:"+
	    	    			gid+" : "+e.getMessage());
	            }
	        }
	    } finally {
	        if (insert != null) {
	            insert.close();
	        }
	        con.setAutoCommit(oldautocommit);
	    }	
	    
		return false;
	}
		
	
	public HashMap<Integer, Float> loadResult(long gid, String tableName) throws SQLException {
		String query = "SELECT columncounter, columnrisk from "+ 
				tableName + 
				" WHERE gid = ? ORDER BY columncounter;";
		
		Connection con = MySQLConnection.getInstance().getConnection();
		
		PreparedStatement selectStatement = null;
		
		HashMap<Integer, Float> results = new HashMap<Integer, Float>();

		try {

			selectStatement = con.prepareStatement(query);

			selectStatement.setLong(1, gid);
			
			ResultSet rs = selectStatement.executeQuery();
			
			while (rs.next()) {
				results.put(
						Integer.valueOf(rs.getInt("columncounter")), 
						Float.valueOf(rs.getFloat("columnrisk"))
						);
			}
			
			return results;

		} catch (SQLException e ) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in selecting " +
					" results in " + tableName + " for GID:"+
					gid+" : "+e.getMessage());

			if (con != null) {
				try {
					System.err
							.print("Transaction is being rolled back " +
									"in MySQLQueryExecutor.loadResult");
					con.rollback();
				} catch(SQLException excep) {
					MyLogger.getInstance().writeLog(Level.ALL,
							"error in *reverting* the selection of " +
									tableName +" results for GID:"+
									gid+" : "+e.getMessage());
				}
			}
		} finally {
			if (selectStatement != null) {
				selectStatement.close();
			}
		}	

		return null;
	}

	/**
	 * 
	 * @param gid
	 * @return
	 * @throws SQLException
	 */
	public com.sap.dpre.entities.jaxb.policy.Policy loadPolicy(long gid) throws SQLException {

		String query = "SELECT `GID`, `columncounter`, `columnname`," +
				" `columntype`, `hidden` from `policies`"+  
				" WHERE gid = ? ORDER BY columncounter;";
		
		Connection con = MySQLConnection.getInstance().getConnection();
		
		PreparedStatement selectStatement = null;
		
		com.sap.dpre.entities.jaxb.policy.Policy policyToReturn = 
				new Policy();
		
		ArrayList<Column> columns = (ArrayList<Column>) policyToReturn.getColumn();
		
//		HashMap<Integer, Float> results = new HashMap<Integer, Float>();

		try {

			selectStatement = con.prepareStatement(query);

			selectStatement.setLong(1, gid);
			
			ResultSet rs = selectStatement.executeQuery();
			
			while (rs.next()) {
				
				Column column = new Column();
				
				column.setName(rs.getString("columnname"));
				column.setType(rs.getString("columntype"));
				column.setHide(rs.getBoolean("hidden"));
				
				columns.add(column);
				
			}
			
			return policyToReturn;

		} catch (SQLException e ) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in selecting " +
					" results in `policies` for GID:"+
					gid+" : "+e.getMessage());

			if (con != null) {
				try {
					System.err
							.print("Transaction is being rolled back " +
									"in MySQLQueryExecutor.loadResult");
					con.rollback();
				} catch(SQLException excep) {
					MyLogger.getInstance().writeLog(Level.ALL,
							"error in *reverting* the selection of " +
									"`policies` results for GID:"+
									gid+" : "+e.getMessage());
				}
			}
		} finally {
			if (selectStatement != null) {
				selectStatement.close();
			}
		}	

		return null;

	}
	
	/**
	 * 
	 * @param policyToSave
	 * @return
	 * @throws SQLException
	 */
	public boolean savePolicy(long gid, 
			com.sap.dpre.entities.jaxb.policy.Policy policyToSave) throws SQLException {
	
		
		String query = "INSERT INTO `policies` (`GID`, `columncounter`, `columnname`, `columntype`, `hidden`) " +
				"VALUES (?, ?, ?, ?, ?)";
				
		Connection con = MySQLConnection.getInstance().getConnection();
		
		PreparedStatement insert = null;

		boolean oldautocommit = con.getAutoCommit();

	    try {
	        con.setAutoCommit(false);
	        insert = con.prepareStatement(query);

	        int counter = 0;
	        
	        ArrayList<Column> list = (ArrayList<Column>) policyToSave.getColumn();
	        
	        for (Column column : list) {
	        	insert.setLong(1, gid);
	        	insert.setInt(2, counter++);
	        	insert.setString(3, column.getName());
	        	insert.setString(4, column.getType());
	        	insert.setBoolean(5, column.isHide());
	        	insert.executeUpdate();
	            con.commit();
	        }
	        
	        con.setAutoCommit(oldautocommit);
	        
			return true;
	        
	    } catch (SQLException e ) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in inserting " +
					" results in `policies` for GID:"+
					gid+" : "+e.getMessage());
	    	
	        if (con != null) {
	            try {
					System.err
							.print("Transaction is being rolled back in" +
									" MySQLQueryExecutor.storeResult");
	                con.rollback();
	            } catch(SQLException excep) {
	    	    	MyLogger.getInstance().writeLog(Level.ALL,
	    	    			"error in *reverting* the insertion of " +
	    	    			"`policies` results for GID:"+
	    	    			gid+" : "+e.getMessage());
	            }
	        }
	    } finally {
	        if (insert != null) {
	            insert.close();
	        }
	        con.setAutoCommit(oldautocommit);
	    }	
	    
		return false;
	}
	
	
	/**
	 * 
	 * @param gid
	 * @param tableName
	 * @return an int with the number of deleted rows, or 0 otherwise
	 * @throws SQLException
	 */
	public int deleteResult (long gid, String tableName) throws SQLException {
		String query = "DELETE from "+ 
				tableName + 
				" WHERE gid = ? ;";
		
		Connection con = MySQLConnection.getInstance().getConnection();
		
		PreparedStatement deleteStatement = null;
		
		boolean oldautocommit = con.getAutoCommit();
		
		try {
			con.setAutoCommit(false);
			deleteStatement = con.prepareStatement(query);

			deleteStatement.setLong(1, gid);
			
			int result = deleteStatement.executeUpdate();
			
			con.commit();
			
			con.setAutoCommit(oldautocommit);
			
			return result;

		} catch (SQLException e ) {
			MyLogger.getInstance().writeLog(Level.ALL, "error in deleting " +
					" results in " + tableName + " for GID:"+
					gid+" : "+e.getMessage());

			if (con != null) {
				try {
					System.err
							.print("Transaction is being rolled back " +
									"in MySQLQueryExecutor.deleteResult");
					con.rollback();
				} catch(SQLException excep) {
					MyLogger.getInstance().writeLog(Level.ALL,
							"error in *reverting* the deleting of " +
									tableName +" results for GID:"+
									gid+" : "+e.getMessage());
				}
			}
		} finally {
			if (deleteStatement != null) {
				deleteStatement.close();
			}
			con.setAutoCommit(oldautocommit);
		}	

		return -1;
	}
		
	
}
