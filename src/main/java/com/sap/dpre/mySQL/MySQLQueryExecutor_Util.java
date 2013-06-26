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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import com.sap.dpre.log.MyLogger;
import com.sap.dpre.ws.DBA_utils;

public class MySQLQueryExecutor_Util {

	/**
	 * Reads the DB dump and store the data into the database using the passed statement stmt. The gid is used to create the table name
	 * @param dbDump
	 * @param stmt
	 * @param gid
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	static String parseAndApplyTransaction (InputStream dbDump, Statement stmt, long gid) throws IOException, SQLException {

		MyLogger logger = MyLogger.getInstance();
		

		logger.writeLog(Level.ALL,"Method parseAndApplyTransaction");

		
		BufferedInputStream inputStream = new BufferedInputStream(dbDump);
		BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));
		String bufferString ="", tmpString ="";
		int currentIndex=-1, previousIndex=0;
		String dumpTableName = "";
		String dbTableName = "";
		String createStatement = "CREATE TABLE `";
		String insertStatement = "INSERT INTO `";

		while ((tmpString = inputStreamReader.readLine())!= null) {
			if (tmpString.indexOf("--", previousIndex) == 0) {
				// this line is a comment! let's forget about it
				//				bufferString = "";
			} else if (tmpString.length() != 0) 	{
				// we look for ';', that means end of SQL statement
				if(tmpString.startsWith("CREATE TABLE") && dumpTableName.equals("")){
					boolean startTableName = false;
					for(int i=0; i<tmpString.length(); i++){
						if(tmpString.charAt(i) == '`'){
							startTableName = !startTableName;
							if(!startTableName) break;
						}
						if(startTableName && tmpString.charAt(i) != '`')
							dumpTableName = dumpTableName+tmpString.charAt(i);
					}
					dbTableName = createNewName(dumpTableName, gid);
					MySQLConnection.getInstance().setMyTableName(dbTableName);
				}

				if(!dumpTableName.equals("")){
					bufferString += tmpString;

					currentIndex=bufferString.indexOf(";", previousIndex);				

					// we ensure that a valid SQL command is contained into
					// bufferString, i.e., the String must contain at least a ";"

					while (currentIndex - previousIndex <= 0) {
						tmpString += inputStreamReader.readLine();
						if (tmpString.contains(";")) {
							bufferString = tmpString;
							currentIndex=bufferString.indexOf(";", previousIndex);
						}
					}

					// at this point, in previousIndex there is the last location where
					// SQL statement started, and in currentIndex there is the current
					// ';' location

					// this function manages all the ";" that are found on the
					// bufferString

					if(tmpString.startsWith(createStatement+dumpTableName+"`") || tmpString.startsWith(insertStatement+dumpTableName+"`"))
						parseSQLStatementsInString(bufferString.replaceFirst(dumpTableName, dbTableName), stmt, gid);
					previousIndex =0;
					bufferString="";
				}
			}
		}

		return dbTableName;
	}

	/**
	 * Creates the name of the table where the DB dump has to be stored
	 * @param tableName
	 * @param gid
	 * @return Table name
	 */
	public static String createNewName(String tableName, long gid) {

		MyLogger logger = MyLogger.getInstance();
		logger.writeLog(Level.ALL, "Method createNewName, tableName:"+tableName+", gid:"+gid);
		
		String compositeTableName = tableName+gid;
		int hash = compositeTableName.hashCode();
		if(hash < 0) hash = -1 * hash;
		String newTableName = tableName+"_"+hash;
		return newTableName;
	}

	/**
	 * this function manages all the ";" that are found on the bufferString
	 * @param bufferString
	 * @param previousIndex
	 * @param currentIndex
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */

	public static String[] parseSQLStatementsInString(String bufferString, Statement stmt, long gid) throws SQLException {

		MyLogger logger = MyLogger.getInstance();
		
		logger.writeLog(Level.ALL, "Start method parseSQLStatementsInString," +
				" gid:"+gid+
				" bufferString:"+bufferString+
				", thread number:"+
				Thread.currentThread().getId());

		int currentIndex = bufferString.length()-1;
		int previousIndex = 0;
		Vector<String> returnArray = new Vector<String>();
		int j=0;
		while (bufferString.contains(";")) {

			if (previousIndex != currentIndex) {
				String currentSQLStatement = null;
				try{
				currentSQLStatement = bufferString.substring(previousIndex, currentIndex+1);
				}catch(StringIndexOutOfBoundsException e){
					logger.writeLog(Level.ALL, "Error:"+e.getLocalizedMessage());
					logger.writeLog(Level.ALL, "bufferString:"+bufferString +
							"previousIndex:"+previousIndex +
							"currentIndex:"+currentIndex);
				}

				// execute query
				if (stmt != null) {
					stmt.execute(currentSQLStatement);
				}

				returnArray.add(currentSQLStatement);


				// adjust any remainder data
				bufferString = bufferString.substring(currentIndex+1);
			}
			// we check if there are still ";" in the bufferString, 
			// that in case can be parsed now

			currentIndex = bufferString.indexOf(";");
			previousIndex =0;

		}

		String [] toReturn = new String[returnArray.size()];
		int i =0;


		for (Iterator iterator = returnArray.iterator(); iterator.hasNext();) {
			toReturn[i++]= (String) iterator.next();

		}

		logger.writeLog(Level.ALL, "End method parseSQLStatementsInString");

		return toReturn;

	}


}
