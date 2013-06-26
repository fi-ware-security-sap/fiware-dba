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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.text.StyledEditorKit.BoldAction;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

//import org.apache.catalina.util.Enumerator;
import org.apache.commons.io.IOUtils;

//import sun.jdbc.odbc.ee.DataSource;

import com.sap.dpre.bootstrap.BootstrapRRList;
import com.sap.dpre.deepSearch.DeepSearch;
import com.sap.dpre.entities.TransferredFile;
import com.sap.dpre.entities.jaxb.policy.Column;
import com.sap.dpre.entities.policyproposal.PolicyProposal;
import com.sap.dpre.entities.policyproposal.PolicyProposalResult;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLConnection;
import com.sap.dpre.mySQL.MySQLQueryFactory;
import com.sap.dpre.mySQL.MySQLResultsManager;
import com.sap.dpre.policy.Policy;
import com.sap.dpre.policy.PolicyComplianceChecker;
import com.sap.dpre.policy.PolicyComplianceChecker_ComputeECM;
import com.sap.dpre.policy.PolicyParser_Utilities;
import com.sap.dpre.policy.DataColumn.ColumnType;
import com.sap.dpre.risk.RiskyColumnFinder;
import com.sap.dpre.risk.RiskyColumnFinder_computeColumnRisks;
import com.sun.xml.bind.v2.schemagen.xmlschema.List;

public class DBA_utils {

	private String workingDbName;
	private String resultsDbName;
	private String tableName;
	private long gid;
	MyLogger logger = MyLogger.getInstance();
	

	/**
	 * Constructor, receives as input the table where the dbDump has to be stored and
	 * the name of the database where the results has to be stored
	 * 
	 * @param workingDbName name of the DB where temporary tables are created and analysed
	 * @param resultsDbName name of the DB where computation results are stored 
	 */
	public DBA_utils(String workingDbName, String resultsDbName) {
		this.workingDbName = workingDbName;
		this.resultsDbName = resultsDbName;
	}

	/**
	 * Creates the TransferredFiled from a given File
	 * 
	 * @param fileToBeSent a File to be used for the creation of a TransferredFile
	 * @return an instance of the TransferredFile class
	 */
	private TransferredFile generateTransferredFile(File fileToBeSent) {

		logger.writeLog(Level.ALL,"Method generateTransferredFile, file name:"+fileToBeSent.getName());
		
		FileDataSource source = new FileDataSource(fileToBeSent);
		TransferredFile fileToTransfer = new TransferredFile();
		fileToTransfer.setFileData(new DataHandler(source));
		fileToTransfer.setFileName(fileToBeSent.getName());
		return fileToTransfer;
	}

	/**
	 * Starts the new thread in which the policy is evaluated
	 * @param policyTempFileToTransfer
	 * @param dbSQLDumpTempFileToTransfer
	 * @param initialContext
	 * @param gid
	 * @param tableName
	 * @param noDetach if true, operations will be launched synchronously
	 */
	public void startThread(File policyTempFileToTransfer,
			File dbSQLDumpTempFileToTransfer,
			Context initialContext, long gid, String tableName, boolean noDetach){

		logger.writeLog(Level.ALL,"Method startThread, gid:"+gid);

		
		this.tableName = tableName;
		TransferredFile policyFileToTransfer = null;
		TransferredFile dbSQLDumpFileToTransfer = null;

		if(policyTempFileToTransfer != null && dbSQLDumpTempFileToTransfer != null){
			policyFileToTransfer = generateTransferredFile(policyTempFileToTransfer);
			dbSQLDumpFileToTransfer = generateTransferredFile(dbSQLDumpTempFileToTransfer);
		}
		else{
			return;
		}
		if (!noDetach) {
			new Thread(new DBA_Thread(policyFileToTransfer, dbSQLDumpFileToTransfer, initialContext, gid)).start();
		} else {
			new DBA_Thread(policyFileToTransfer, dbSQLDumpFileToTransfer, initialContext, gid).run();
		}

	}


	public void startDeepSearchThread(File policyTempFileToTransfer,
			File dbSQLDumpTempFileToTransfer, Context initialContext,
			long gid, String tableName, boolean noDetach, float maxRisk) {
		
		logger.writeLog(Level.ALL,"Method startThread, gid:"+gid);

		
		this.tableName = tableName;
		TransferredFile policyFileToTransfer = null;
		TransferredFile dbSQLDumpFileToTransfer = null;

		if(policyTempFileToTransfer != null && dbSQLDumpTempFileToTransfer != null){
			policyFileToTransfer = generateTransferredFile(policyTempFileToTransfer);
			dbSQLDumpFileToTransfer = generateTransferredFile(dbSQLDumpTempFileToTransfer);
		}
		else{
			logger.writeLog(Level.SEVERE, "startDeepSearchThread: Error in input files!");
			return;
		}
		if (!noDetach) {
			new Thread(new DBA_DeepSearchThread(policyFileToTransfer, dbSQLDumpFileToTransfer, initialContext, gid, maxRisk)).start();
		} else {
			new DBA_DeepSearchThread(policyFileToTransfer, dbSQLDumpFileToTransfer, initialContext, gid, maxRisk).run();
		}

		
	}

	
	
	/**
	 * Given the generated ID returns the result of the policy evaluation associated to such ID 
	 * @param gid
	 * @param initialContext
	 * @return
	 */
	public String getPolicyEvaluationResult(long gid, Context initialContext){
		this.gid = gid;
		Context initContext = null;
		if (initialContext == null) {
			try {
				initContext = new InitialContext();
			} catch (NamingException e1) {
				logger.writeLog(Level.INFO, e1.getLocalizedMessage());
//				e1.printStackTrace();
			} 
		} else {
			
			 //we are not in an application server context!
			 initContext = initialContext;
		}
		if (initContext == null) {
			return "-4, Error: Null context";
		}

		MySQLConnection mySqlConnection = null;
		
		if (mySqlConnection.getInstance().verifyConnection() == false) {
			mySqlConnection = MySQLConnection.getInstance((InitialContext)initContext, true);
		} else {
			mySqlConnection = MySQLConnection.getInstance();
		}
		
		mySqlConnection.setConnectionCredentials(resultsDbName, tableName, true);
		String result = new MySQLResultsManager().getResult(gid);
		return result;
	}
	

	public HashMap<Integer, Float> getRiskColumnResult(long gid, Context initialContext) {

		this.gid = gid;
		Context initContext = null;
		if (initialContext == null) {
			try {
				initContext = new InitialContext();
			} catch (NamingException e1) {
				logger.writeLog(Level.INFO, e1.getLocalizedMessage());
//				e1.printStackTrace();
			} 
		} else {
			
			 initContext = initialContext;
		}
		if (initContext == null) {
			return null;
		}

		MySQLConnection mySqlConnection = null;
		
		if (MySQLConnection.getInstance().verifyConnection() == false) {
			mySqlConnection = MySQLConnection.getInstance((InitialContext)initContext, true);
		} else {
			mySqlConnection = MySQLConnection.getInstance();
		}
		
		mySqlConnection.setConnectionCredentials(resultsDbName, tableName, true);

		MySQLQueryFactory sqlFactory = new MySQLQueryFactory();
		
		HashMap<Integer, Float> resultMap = sqlFactory.getRiskColumnResult(gid);
		
		return resultMap;
	}

	private MySQLQueryFactory setupAndApplyDBDump(Context initContext, InputStream dbDumpInputStream) {
		MySQLConnection mySqlConnection = null;
		
		if (mySqlConnection.getInstance().verifyConnection() == false) {
			mySqlConnection = MySQLConnection.getInstance((InitialContext)initContext, true);
		} else {
			mySqlConnection = MySQLConnection.getInstance();
		}
		
		logger.writeLog(Level.ALL, "setUpAndApplyDBDump tableName: "+tableName);
		
		mySqlConnection.setConnectionCredentials(workingDbName, tableName, true);
		
		MySQLQueryFactory mySQLFactory = null;
		try {
			mySQLFactory = manageDBDump(dbDumpInputStream, initContext, mySqlConnection);
		} catch (IOException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "problem in I/O: "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			MyLogger.getInstance().writeLog(Level.ALL, "problem in getting mySqlConnection: "+e.getMessage());
			e.printStackTrace();
		}
		
		return mySQLFactory;
		
	}	
	
	/**
	 * Starts the policy evaluation procedure
	 * @param policyFileToTransfer
	 * @param dbSQLDumpFileToTransfer
	 * @param initialContext
	 * @param gid
	 * @return
	 */
	public String evaluatePolicy_innerMethod(
			TransferredFile policyFileToTransfer,
			TransferredFile dbSQLDumpFileToTransfer,
			Context initialContext, long gid) {


		logger.writeLog(Level.ALL,"Method evaluatePolicy_innerMethod, gid:"+gid+", thread number:"+Thread.currentThread().getId());
		logger.writeLog(Level.ALL,("DBA_utils-Instance #"+this.toString()));

		
		this.gid = gid;

		DataHandler dbDumpFileDataHandler;
		try {
			dbDumpFileDataHandler = convertZipFile(dbSQLDumpFileToTransfer, dbSQLDumpFileToTransfer.getFileName());
		} catch (ZipException e2) {
			e2.printStackTrace();
			return "-1, Error: The given file is not a Zip file";
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			return "-2, Error: Impossible to find the specified file";
		} catch (IOException e2) {
			e2.printStackTrace();
			return "-3, I/O Error";
		}

		Context initContext = null;

		/*
		 * if initialContext == null
		 * we expect the execution to be hosted in
		 * an application server
		 */
		if (initialContext == null) {
			try {
				initContext = new InitialContext();
			} catch (NamingException e1) {
				e1.printStackTrace();
			} 
		} else {
			// we are not in an application server context!
			initContext = initialContext;
		}

		if (initContext == null) {
			return "-4, Error: Null context";
		}

		
			MySQLQueryFactory mySQLFactory = null;
			try {
				mySQLFactory = setupAndApplyDBDump(initContext, dbDumpFileDataHandler.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return "-5, Problem with input DB dump";
			}
		
		if (mySQLFactory == null) {
			return "-5, Problem with input DB dump";
		}
		
		
		String result = policyRiskEvaluator(policyFileToTransfer, "", "", true);
		
		mySQLFactory.dropWorkingViews(gid);
		mySQLFactory.dropWorkingTable(gid);
		
		MySQLResultsManager rs = new MySQLResultsManager();
		rs.storeResult(gid, result);
		
		return result;
	}



	public PolicyProposalResult evaluateDeepSearch_innerMethod(
			TransferredFile policyFileToTransfer,
			TransferredFile dbSQLDumpFileToTransfer, Context initialContext,
			long gid2, float maxRisk) {

		logger.writeLog(Level.ALL,"Method evaluateDeepSearch_innerMethod, gid:"+gid+", thread number:"+Thread.currentThread().getId());
		logger.writeLog(Level.ALL,("DBA_utils-Instance #"+this.toString()));

		
		this.gid = gid2;

		DataHandler dbDumpFileDataHandler;
		try {
			dbDumpFileDataHandler = convertZipFile(dbSQLDumpFileToTransfer, dbSQLDumpFileToTransfer.getFileName());
		} catch (ZipException e2) {
			e2.printStackTrace();
//			return "-1, Error: The given file is not a Zip file";
			return null;
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
//			return "-2, Error: Impossible to find the specified file";
			return null;
		} catch (IOException e2) {
			e2.printStackTrace();
//			return "-3, I/O Error";
			return null;
		}

		Context initContext = null;

		/*
		 * if initialContext == null
		 * we expect the execution to be hosted in
		 * an application server
		 */
		if (initialContext == null) {
			try {
				initContext = new InitialContext();
			} catch (NamingException e1) {
				e1.printStackTrace();
			} 
		} else {
			// we are not in an application server context!
			initContext = initialContext;
		}

		if (initContext == null) {
//			return "-4, Error: Null context";
			return null;
		}		
		
			MySQLQueryFactory mySQLFactory = null;
			try {
				mySQLFactory = setupAndApplyDBDump(initContext, dbDumpFileDataHandler.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
//				return "-5, Problem with input DB dump";
				return null;
			}
		
		if (mySQLFactory == null) {
//			return "-5, Problem with input DB dump";
			return null;
		}
		
		
		PolicyProposalResult result = deepSearchEvaluator(policyFileToTransfer, "", "", true, gid, maxRisk);

		// results are automatically saved by DeepSearch class
		
		return result;
	}

	

	/**
	 * Starts the policy evaluation procedure
	 * @param policyFileToTransfer
	 * @param dbSQLDumpFileToTransfer
	 * @param initialContext
	 * @param gid
	 * @return
	 * 
	 * 
	 */
	public String evaluateRiskColumn_innerMethod(
			TransferredFile dbSQLDumpFileToTransfer,
			Context initialContext, long gid) {


		System.err.println("Method evaluatePolicy_RiskinnerMethod, gid:"+gid+", thread number:"+Thread.currentThread().getId());
		
		logger.writeLog(Level.ALL,"Method evaluatePolicy_RiskinnerMethod, gid:"+gid+", thread number:"+Thread.currentThread().getId());
		logger.writeLog(Level.ALL,("DBA_utils-Instance #"+this.toString()));

		
		this.gid = gid;

		DataHandler dbDumpFileDataHandler;
		try {
			dbDumpFileDataHandler = convertZipFile(dbSQLDumpFileToTransfer, dbSQLDumpFileToTransfer.getFileName());
		} catch (ZipException e2) {
			e2.printStackTrace();
			return "-1, Error: The given file is not a Zip file";
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			return "-2, Error: Impossible to find the specified file";
		} catch (IOException e2) {
			e2.printStackTrace();
			return "-3, I/O Error";
		}

		Context initContext = null;

		/*
		 * if initialContext == null
		 * we expect the execution to be hosted in
		 * an application server
		 */
		if (initialContext == null) {
			try {
				initContext = new InitialContext();
			} catch (NamingException e1) {
				e1.printStackTrace();
			} 
		} else {
			// we are not in an application server context!
			initContext = initialContext;
		}

		if (initContext == null) {
			return "-4, Error: Null context";
		}

		MySQLQueryFactory mySQLFactory = null; 
		
		try {
			mySQLFactory = setupAndApplyDBDump(initContext, dbDumpFileDataHandler.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return "-5, Problem with input DB dump";
		}
		
		if (mySQLFactory == null) {
			return "-5, Problem with input DB dump";
		}
		
		String result = columnRiskEvaluator(mySQLFactory);
		
		return result;
	}

	
	

	/**
	 * Loads the dbDump into the database
	 * @param dbDumpFileDataHandler
	 * @param initContext
	 * @param mySqlConnection
	 * @throws IOException
	 * @throws NamingException
	 * 
	 */
	public MySQLQueryFactory manageDBDump(InputStream dbDumpInputStream, Context initContext, MySQLConnection mySqlConnection)
			throws IOException, NamingException {


		logger.writeLog(Level.ALL,"Method manageDBDump, tableName:"+tableName);

			
		if (initContext == null) {
			initContext  = new InitialContext();
		}

		MySQLQueryFactory mySQLFactory = new MySQLQueryFactory();
		try {
			String workingTableName = mySQLFactory.executeTransaction(dbDumpInputStream, gid);
			if (workingTableName == null) {
				logger.writeLog(Level.ALL,"Method manageDBDump, tableName:"+tableName);
				System.err.println("Method manageDBDump, tableName:"+tableName);
				return null;
			} 

			mySQLFactory.loadDataColumns();
		} catch (SQLException e) {	
			MyLogger.getInstance().writeLog(Level.ALL, "Problem with DB, error in SQL statements? " + e.getMessage());
			e.printStackTrace();
			mySQLFactory.dropWorkingTable(gid);
			mySQLFactory = null;

		} catch (IOException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "Problem with DB dump parsing, " + e.getMessage());
			e.printStackTrace();
			mySQLFactory.dropWorkingTable(gid);
			mySQLFactory = null;
		} 

		return mySQLFactory;
	}



	private String columnRiskEvaluator(MySQLQueryFactory sqlFactory) {

		
		Policy.getInstance().clearPolicy();
		
		
		if (!setUpDBConnection()) {
			return "-6, Error in DB setup";
		}
		
		new RiskyColumnFinder(gid, sqlFactory).run();
		
		return null;
	}

	
	
	/**
	 * Calculates the final policy evaluation
	 * @param policyFileToTransfer
	 * @param dbToUse
	 * @param tableToUse
	 * @param noCheckDBConn
	 * @return
	 */
	private String policyRiskEvaluator(TransferredFile policyFileToTransfer,
			String dbToUse, String tableToUse, boolean noCheckDBConn) {


//		logger.writeLog(Level.ALL,"Method policyRiskEvaluator, tableToUse:"+tableToUse);
		
		String result = "";
		if(!noCheckDBConn) {
			if (!setUpDBConnection()) {
				return "-6, Error in DB setup";
			}
		} 
		if ((dbToUse != "") && (tableToUse != "")) {
			MySQLConnection.getInstance().setConnectionCredentials(dbToUse, tableToUse, noCheckDBConn);
		} else {
			System.err.println("the expected tableName to use is: "+workingDbName);
			logger.writeLog(Level.ALL, "the expected tableName to use is: "+workingDbName);
		}
		if(!applyReceivedPolicy(policyFileToTransfer)) {
			return "-7, Error in policy parsing and setting";
		}

		String[] tmpDisclosedIdentifierColumns = Policy.getInstance()
				.getDisclosedIdentifierColumnNames();
		String[] tmpDisclosedSensitiveColumns = Policy.getInstance()
				.getDisclosedSensitiveColumnNames();

		result += PolicyComplianceChecker_ComputeECM.computeECM(tmpDisclosedIdentifierColumns, tmpDisclosedSensitiveColumns);
		
//		MySQLConnection.getInstance().closeConnection(); 
		
		return result;
	}

	/**
	 * Calculates the final policy evaluation
	 * @param policyFileToTransfer
	 * @param dbToUse
	 * @param tableToUse
	 * @param noCheckDBConn
	 * @return
	 */
	private PolicyProposalResult deepSearchEvaluator(TransferredFile policyFileToTransfer,
			String dbToUse, String tableToUse, boolean noCheckDBConn, long gid, float maxRisk) {


//		logger.writeLog(Level.ALL,"Method policyRiskEvaluator, tableToUse:"+tableToUse);
		
	
		String result = "";
		if(!noCheckDBConn) {
			if (!setUpDBConnection()) {
//				return "-6, Error in DB setup";
				return null;
			}
		} 
		if ((dbToUse != "") && (tableToUse != "")) {
			MySQLConnection.getInstance().setConnectionCredentials(dbToUse, tableToUse, noCheckDBConn);
		} else {
			System.err.println("the expected tableName to use is: "+workingDbName);
			logger.writeLog(Level.ALL, "the expected tableName to use is: "+workingDbName);
		}
		if(!applyReceivedPolicy(policyFileToTransfer)) {
//			return "-7, Error in policy parsing and setting";
			return null;
		}

		com.sap.dpre.entities.jaxb.policy.Policy tmpPol = serializePolicy();
		
		if (!new MySQLQueryFactory().savePolicy(gid, tmpPol)) {
			 
			return null;
		}
			
		
		// compute and save deepsearch results, only those over maxRisk				
		DeepSearch ds = new DeepSearch(gid, maxRisk);
		System.err.println("start DeepSearch computation: "+gid);

		// deliberately running DeepSearch synchronously

		// ds.run() performs computation and saves results
		ds.run();

		return null;
		
	}

	
	public PolicyProposalResult getDeepSearchResults(long gid, int count, int offset) {
		
		PolicyProposalResult toReturn = new PolicyProposalResult();
		
		com.sap.dpre.entities.jaxb.policy.Policy policy = 
				new com.sap.dpre.entities.jaxb.policy.Policy();
		
		MySQLQueryFactory sqlFactory = new MySQLQueryFactory();
		
		
		policy = sqlFactory.loadPolicy(gid);
		
		HashMap<Integer, Float> results = sqlFactory.getDeepSearchResult(gid);
		
		int counter = offset;
		
		count = results.size()<=count?results.size():count;
		
		for (Map.Entry<Integer, Float> entry : results.entrySet()) {
			
			PolicyProposal proposal = new PolicyProposal();
			
			if (counter ++ < count) {
				
				String binaryCounter = Integer.toBinaryString(entry.getKey());
				
				proposal.setComputedRisk(entry.getValue().floatValue());
				
				proposal.setProposalID(counter);
				
				while (binaryCounter.length() < getBinaryCounterSize(policy)) {
					binaryCounter = "0" + binaryCounter;
				}

				// set the proposal column according to the proposal
				for (int i = 0; i < Policy.getInstance().getBinaryCounterSize(); i++) {

					boolean isHidden = binaryCounter.charAt(i) == '1' ? true : false;

					policy.getColumn().get(i).setHide(isHidden);
				}
				
				proposal.setPolicyProposal(policy);
				
				toReturn.getPolicyProposalResult().add(proposal);
				
			
			}
		}
		
		return toReturn;		
		
	}
	
	private int getBinaryCounterSize(com.sap.dpre.entities.jaxb.policy.Policy policy) {

		ArrayList<Column> columns = (ArrayList<Column>) policy.getColumn();
		
		// increment counter IIF column type == identifier
		int counterSize = 0;
		for (int i = 0; i < columns.size(); i++) {
			if (columns.get(i).getType().equalsIgnoreCase("identifier")) {
				counterSize++;
			}
		}

		return counterSize;
	}
	
	
	public String computePseudoHash(com.sap.dpre.entities.jaxb.policy.Policy policy) {
		String toReturn = "";
		
		ArrayList<Column> columns = (ArrayList<Column>) policy.getColumn();
		
		for (Column column : columns) {
			if (column.isHide() == true) {
				toReturn += "1";
			} else {
				toReturn += "0";
			}
		}
		
		return toReturn;
		
	}
	
	public com.sap.dpre.entities.jaxb.policy.Policy serializePolicy() {
		com.sap.dpre.entities.jaxb.policy.Policy serializePolicy = 
				new com.sap.dpre.entities.jaxb.policy.Policy();
		
		ArrayList<Column> columns = (ArrayList<Column>) serializePolicy.getColumn();
		
		Object[][] policyColumns = Policy.getInstance().getDataColumnsData();
		
		for (int i = 0; i < policyColumns.length; i++) {
			Object[] tmpCol = policyColumns[i];
			Column col = new Column();
			
			
			col.setName((String)tmpCol[0]); 
			col.setType(tmpCol[1].toString()) ;
			col.setHide(Boolean.getBoolean(String.valueOf(tmpCol[2])));
			
			columns.add(col);
		}
		
		return serializePolicy;
		
	}
	
	
	/**
	 * Applies the received policy file
	 * @param policyFileToTransfer
	 * @return
	 */
	private boolean applyReceivedPolicy(TransferredFile policyFileToTransfer) {

		DataHandler policyFileDataHandler = policyFileToTransfer.getFileData();
		File policyFile = null;

		try {
			policyFile = File.createTempFile("tmpPolicyFile.xml", null);
			policyFile.deleteOnExit();
			IOUtils.copy( policyFileDataHandler.getInputStream(), new FileOutputStream(policyFile));		
		} catch (FileNotFoundException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "Problem in temporary file creation");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			MyLogger.getInstance().writeLog(Level.ALL, "Problem in temporary file creation");
			e.printStackTrace();
			return false;
		} 
		

		PolicyParser_Utilities parserUtility = new PolicyParser_Utilities(policyFile);
		if (!parserUtility.parsePolicyDocument()) {
			MyLogger.getInstance().writeLog(Level.ALL, "policy file not correctly parsed");
			return false;
		}

		parserUtility.applyPolicy();
		return true;
	}

	/**
	 * Sets up the DB connection, and invokes
	 * MySQLQueryFactory().loadDataColumns() to load
	 * data from DB dump
	 * @return
	 */
	private boolean setUpDBConnection() {
		
		Context initContext = null;
		
		MySQLConnection mySqlConnection = null;

		try {
			initContext  = new InitialContext();
			
			
			if (mySqlConnection.getInstance().verifyConnection() == false) {
				mySqlConnection = MySQLConnection.getInstance((InitialContext)initContext, true);
			} else {
				mySqlConnection = MySQLConnection.getInstance();
			}

		} catch (Exception e) {
			MyLogger.getInstance().writeLog(Level.ALL, "problem in getting mySqlConnection");

			e.printStackTrace();
			//
			return false;
		}

		if (mySqlConnection.getConnectionStatus()) {

			// clear previous policy and load new one
			Policy.getInstance().clearPolicy();

			try {
				new MySQLQueryFactory().loadDataColumns();

			} catch (SQLException e) {
				e.printStackTrace();
				// log exception
				MyLogger.getInstance().writeLog(Level.SEVERE,
						e.getLocalizedMessage());
				return false;
			}

			return true;

		} else {
			String msg = "Unable to connect!\n(wrong connection credentials?)";
			MyLogger.getInstance().writeLog(Level.ALL, msg);
			return false;
		}
	}

	
	/**
	 * Extract the DB dump from the received zip file and convert it into a DataHandler
	 * @param data
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws ZipException
	 * @throws FileNotFoundException
	 */
	private DataHandler convertZipFile(TransferredFile data, String fileName) throws IOException, ZipException, FileNotFoundException{
				
		long init = System.currentTimeMillis();

		logger.writeLog(Level.ALL,"Start method convertZipFile");

		DataHandler newDataHandler = null;
		ZipFile zf = null;

		File tempFile = File.createTempFile(fileName, null);
		tempFile.deleteOnExit();
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
		InputStream fis = data.getFileData().getDataSource().getInputStream();
		int m;
		while((m = fis.read()) > -1){
			out.writeByte(m);
		}
		fis.close();
		out.close();

		zf = new ZipFile(tempFile);
		

		ZipInputStream zis = new ZipInputStream(fis);
		ZipEntry entry;
		File newFile = null;
		Enumeration e = zf.entries();
		// while there are entries I process them
		while (e.hasMoreElements())
		{
			entry = (ZipEntry) e.nextElement();
			byte[] buf = new byte[1024];
			int n;
			String entryName = entry.getName();
			FileOutputStream fileoutputstream;
			newFile = File.createTempFile(entryName, null);
			newFile.deleteOnExit();

			fileoutputstream = new FileOutputStream(newFile);
			BufferedInputStream is = new BufferedInputStream (zf.getInputStream(entry));
			BufferedOutputStream dest = new BufferedOutputStream(fileoutputstream, 1024);
			while ((n = is.read()) != -1) {
				dest.write(n);
			}
			dest.flush();
			dest.close();
			zis.closeEntry();
		}
		zf.close();

		FileDataSource source = new FileDataSource(newFile);
		newDataHandler = new DataHandler(source);

		try {
			if (zf != null) {
				zf.close();
				zf = null;
			}
		} catch (IOException ex) {
		}

		long end = System.currentTimeMillis();
		
		logger.writeLog(Level.ALL,"End method convertZipFile in time: "+(end-init));

			
		return newDataHandler;
	}

	/**
	 * Returns the DB name
	 * @return
	 */
	public String getDbName() {
		return workingDbName;
	}

	/**
	 * Sets the DB name
	 * @param dbName
	 */
	public void setDbName(String dbName) {
		this.workingDbName = dbName;
	}

	/**
	 * Returns the table name
	 * @return
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Sets the table name
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	public void startRiskThread(File dbSQLDumpTempFileToTransfer,
			Context initialContext, long gid, String tableName) {
		
		logger.writeLog(Level.ALL,"Method startThread, gid:"+gid);
	
		this.tableName = tableName;
		TransferredFile dbSQLDumpFileToTransfer = null;

		if(dbSQLDumpTempFileToTransfer != null){
			dbSQLDumpFileToTransfer = generateTransferredFile(dbSQLDumpTempFileToTransfer);
		}

		else{
			return;
		}
		
		new Thread(new DBA_RiskThread(dbSQLDumpFileToTransfer, initialContext, gid)).start();

		
	}

	///// Inner Classes
	
	private class DBA_RiskThread implements Runnable {

		TransferredFile dbSQLDumpFileToTransfer;
		Context initialContext;
		long gid;

		/**
		 * Thread constructor, takes as input the TransferredFile of the DB dump,
		 * the context and the generated ID associated to the evaluation
		 * @param dbSQLDumpFileToTransfer
		 * @param initialContext
		 * @param gid
		 */
		public DBA_RiskThread(
				TransferredFile dbSQLDumpFileToTransfer, Context initialContext, long gid) {
			super();
			this.dbSQLDumpFileToTransfer = dbSQLDumpFileToTransfer;
			this.initialContext = initialContext;
			this.gid = gid;
		}

		@Override
		public void run() {
			
			MySQLQueryFactory sqlFactory = new MySQLQueryFactory();
			
			try {
				DataHandler uncompressedFile = 
						convertZipFile(
								dbSQLDumpFileToTransfer, 
								dbSQLDumpFileToTransfer.getFileName());


			} catch (FileNotFoundException e) {

				e.printStackTrace();
				logger.writeLog(Level.SEVERE, "error in DBA_RiskThread: "+e.getMessage());

				return;
			} catch (IOException e) {
				logger.writeLog(Level.SEVERE, "error in DBA_RiskThread: "+e.getMessage());
				e.printStackTrace();

				return;
			} 
			
			String result = 
					evaluateRiskColumn_innerMethod( 
						dbSQLDumpFileToTransfer, 
						initialContext, 
						gid);
		}
		
	};
	

	/**
	 * Runnable class to start the DBA_utils in a separated thread
	 * 
	 *
	 */
	private class DBA_Thread implements Runnable {

		TransferredFile policyFileToTransfer;
		TransferredFile dbSQLDumpFileToTransfer;
		Context initialContext;
		long gid;

		/**
		 * Thread constructor, takes as input the TransferredFile of the policy and the DB dump,
		 * the context and the generated ID associated to the evaluation
		 * @param policyFileToTransfer
		 * @param dbSQLDumpFileToTransfer
		 * @param initialContext
		 * @param gid
		 */
		public DBA_Thread(TransferredFile policyFileToTransfer,
				TransferredFile dbSQLDumpFileToTransfer, Context initialContext, long gid) {
			super();
			this.policyFileToTransfer = policyFileToTransfer;
			this.dbSQLDumpFileToTransfer = dbSQLDumpFileToTransfer;
			this.initialContext = initialContext;
			this.gid = gid;
		}


		@Override
		public void run() {
			String result = 
					evaluatePolicy_innerMethod(
						policyFileToTransfer, 
						dbSQLDumpFileToTransfer, 
						initialContext, 
						gid);
			
		
		}
	}

	private class DBA_DeepSearchThread implements Runnable {

		TransferredFile policyFileToTransfer;
		TransferredFile dbSQLDumpFileToTransfer;
		Context initialContext;
		long gid;
		private float maxRisk;

		/**
		 * Thread constructor, takes as input the TransferredFile of the policy and the DB dump,
		 * the context and the generated ID associated to the evaluation
		 * @param policyFileToTransfer
		 * @param dbSQLDumpFileToTransfer
		 * @param initialContext
		 * @param gid
		 */
		public DBA_DeepSearchThread(TransferredFile policyFileToTransfer,
				TransferredFile dbSQLDumpFileToTransfer,
				Context initialContext, long gid, float maxRisk) {
			super();
			this.policyFileToTransfer = policyFileToTransfer;
			this.dbSQLDumpFileToTransfer = dbSQLDumpFileToTransfer;
			this.initialContext = initialContext;
			this.gid = gid;
			this.maxRisk = maxRisk;
		}


		@Override
		public void run() {
			PolicyProposalResult result = 
					evaluateDeepSearch_innerMethod(
						policyFileToTransfer, 
						dbSQLDumpFileToTransfer, 
						initialContext, 
						gid,
						maxRisk);
								
			MySQLQueryFactory sqlFactory = new MySQLQueryFactory();
			sqlFactory.dropWorkingViews(gid);
			sqlFactory.dropWorkingTable(gid);
		}

	}

	public void saveToDBPolicyProposalResult(PolicyProposalResult result) {
		
	}
	
}
