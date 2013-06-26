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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

import com.sap.dpre.entities.jaxb.evaluateresponse.Result;
import com.sap.dpre.entities.jaxb.riskcolumnresult.RiskColumnResult;
import com.sap.dpre.entities.policyproposal.PolicyProposalResult;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.mySQL.MySQLQueryFactory;
import com.sap.dpre.policy.Policy;


public class DbAnonymizerJAX_RS_Impl implements DbAnonymizerJAX_RS {

	private DBA_factory dba_factory;	

	public DbAnonymizerJAX_RS_Impl(DBA_factory dba_factory) {
		this.dba_factory = dba_factory;
		
		MySQLQueryFactory sqlFactory = new 	MySQLQueryFactory();
		if (!sqlFactory.checkDBExistence())	{
			MyLogger.getInstance().writeLog(Level.SEVERE,
					"***** DB not correctly configured *****");
			// set dba_factory to null, so no operations can be instructed.
			dba_factory = null;
			return;
		}

	}
	/**
	 * this method is invoked by any computation activity: it forces
	 * a reload of the shared Policy method
	 *  
	 * @return
	 */
	private boolean commonMethod() {
		
		Policy.getInstance().clearPolicy();
		
		return true;
	}
	

	/**
	 * @see com.sap.dpre.ws.DbAnonymizerJAX_RS#evaluatePolicy(float, com.sap.dpre.entities.TransferredFile, com.sap.dpre.entities.TransferredFile)
	 */
	@Override
	public Response evaluatePolicy(byte [] policyFileToTransferByte,
			byte [] dbSQLDumpFileToTransferByte) {
		
		File policyFileToTransfer = null;
		File dbSQLDumpFileToTransfer = null;

		
		try {
		
			policyFileToTransfer = byteToTemporaryFile(policyFileToTransferByte, "policy");
			dbSQLDumpFileToTransfer = byteToTemporaryFile(dbSQLDumpFileToTransferByte, "dbDump");
		
		} catch (IOException e) {
			return Response.serverError().build();
			
		}
		

		String result = evaluatePolicy_internalComputation(policyFileToTransfer,
				dbSQLDumpFileToTransfer);
		
		if (result.contains("ID: --")) {
			Result res = new Result();
			res.setRequestID(result.split("--")[1]);
			return Response.ok(res).build();
		} else {
			return Response.serverError().build();
		}
		
	}

	/**
	 * @param policyFileToTransferByte
	 * @param fileName 
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private File byteToTemporaryFile(byte[] policyFileToTransferByte, String fileName)
			throws IOException, FileNotFoundException {
		File fileToReturn;
		fileToReturn = File.createTempFile(fileName, null);

		fileToReturn.deleteOnExit();

		FileOutputStream outPolicy = new FileOutputStream(fileToReturn);
		outPolicy.write(policyFileToTransferByte);
		outPolicy.close();
		return fileToReturn;
	}

	
	public File transformMultipartAttachmentToFile(MultipartBody multipart, String fieldName) {
		
		File fileToReturn = null;
		
		TempFiles tmpfiles = new TempFiles();
		
		try {
			fileToReturn = File.createTempFile(fieldName, null);
			fileToReturn.deleteOnExit();
		
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

		Attachment policyAttachment = multipart.getAttachment(fieldName);
		
		try {
			
			tmpfiles.copyInputStream2File(fileToReturn, 
						policyAttachment.getDataHandler().getInputStream());
		
		} catch (Exception e) {
			System.err.println("Problem in file copy: "+e.getMessage());
			e.printStackTrace();
			return  null;
		}
		
		return fileToReturn;

	}

	/**
	 * @see com.sap.dpre.ws.DbAnonymizerJAX_RS#evaluatePolicySimple(float max_risk, 
	 * 				File policyFileToTransfer, File dbSQLDumpFileToTransfer)
	 */
	@Override
	public String evaluatePolicy(MultipartBody multipart) {

		List<Attachment> atts = multipart.getAllAttachments();
		if (atts.size() < 3) {
			System.err.println("Please check input parameters, expected " +
					"at least 3, received only: "+atts.size());
			throw new WebApplicationException();
		}

		File policyFileToTransfer = 
				transformMultipartAttachmentToFile(multipart, "policyFile");
		File dbSQLDumpFileToTransfer = 
				transformMultipartAttachmentToFile(multipart, "dbDump");
		
		if ((policyFileToTransfer == null)||(dbSQLDumpFileToTransfer == null)) {
			return  Response.status(999).toString();
		}
				
		return evaluatePolicy_internalComputation(policyFileToTransfer,
				dbSQLDumpFileToTransfer);
	}

	protected String evaluatePolicy_internalComputation(
			File policyFileToTransfer, File dbSQLDumpFileToTransfer) {

		// common initialization method
		commonMethod();
		
		Random generator = new Random(System.currentTimeMillis());

		//Generates the ID which will be associated to the evaluation
		long gid = generator.nextLong();

		if ((policyFileToTransfer == null) || 
				(dbSQLDumpFileToTransfer == null)) {
			return "Error: one or more files incorrectly received";
		}

		if(gid<0)
			gid = gid*(-1);
		
		dba_factory.evaluatePolicy(policyFileToTransfer,
				dbSQLDumpFileToTransfer, null, gid, "working_table", false);
		
		return "ID: --"+gid+"--";
	}


	protected String evaluateDeepSearch_internalComputation(
			File policyFileToTransfer, File dbSQLDumpFileToTransfer, float maxRisk) {

		// common initialization method
		commonMethod();
		
		Random generator = new Random(System.currentTimeMillis());

	
		//Generates the ID which will be associated to the evaluation
		long gid = generator.nextLong();

		if ((policyFileToTransfer == null) || 
				(dbSQLDumpFileToTransfer == null)) {
			return "Error: one or more files incorrectly received";
		}

		if(gid<0)
			gid = gid*(-1);
		
		dba_factory.evaluateDeepSearch(policyFileToTransfer,
				dbSQLDumpFileToTransfer, null, gid, "working_table", false, maxRisk);
		
		return "ID: --"+gid+"--";
	}

	
	private String evaluateColumns_internalComputation(
			 File dbSQLDumpFileToTransfer) {

		// common initialization method
		commonMethod();
		
		Random generator = new Random(System.currentTimeMillis());

		//Generates the ID which will be associated to the evaluation
		long gid = generator.nextLong();

		if (dbSQLDumpFileToTransfer == null) {
			return "Error: DB dump file incorrectly received";
		}

		if(gid<0)
			gid = gid*(-1);
		
		dba_factory.getRiskperColumn(dbSQLDumpFileToTransfer, null, gid, "working_table");
		
		return "ID: --"+gid+"--";
	}

	
	
	/**
	 * @see com.sap.dpre.ws.DbAnonymizerJAX_RS_unused#getPolicyResult(long gid)
	 */
	@Override
	public Response getPolicyResult(String gid) {
		
		String result = "The result of the computation is: ";

		try {
			result += dba_factory.getResult(Long.parseLong(gid));	
		} catch (java.lang.NumberFormatException numEx) {
			numEx.printStackTrace();
			return Response.status(400).entity("Error in Request ID.").build();
		} 
		
		if (result.contains("Error in retrieving the requested result")) {
			// in this case, a result can not yet ready or not existing.
			return Response.noContent().entity(result).build();
					
		} else if (result.contains("Error")) {
			return Response.serverError().entity(result).build();
		}

		
		
		return Response.ok(result, MediaType.TEXT_PLAIN).build();
	}
	
	@Override
	public String getPolicyResultURL(String gid) {
		
		return (String) getPolicyResult(gid).getEntity();
	}

	

	@Override
	public Response evaluateColumns(
			@Multipart("dbDump") byte[] dbSQLDumpFileToTransferByte) {


		File dbSQLDumpFileToTransfer = null;

		
		try {
		
			dbSQLDumpFileToTransfer = byteToTemporaryFile(dbSQLDumpFileToTransferByte, "dbDump");
		
		} catch (IOException e) {
			return Response.serverError().build();
			
		}
		
		String result = evaluateColumns_internalComputation(dbSQLDumpFileToTransfer);
		
		
		if (result.contains("ID: --")) {
			Result res = new Result();
			res.setRequestID(result.split("--")[1]);
			return Response.ok(res).build();
		} else {
			return Response.serverError().build();
		}
	}

	@Override
	public String evaluateColumns(MultipartBody multipart) {
		List<Attachment> atts = multipart.getAllAttachments();
		if (atts.size() < 2) {
			System.err.println("Please check input parameters, expected " +
					"at least 2, received only: "+atts.size());
			throw new WebApplicationException();
		}

		File dbSQLDumpFileToTransfer = 
				transformMultipartAttachmentToFile(multipart, "dbDump");
	
		
		if (dbSQLDumpFileToTransfer == null) {
			return  Response.status(999).toString();
		}

		String result = evaluateColumns_internalComputation(
				dbSQLDumpFileToTransfer);
		
		return result;
	}

	
	
	@Override
	public Response evaluateDeepSearchAnalysis(
			@Multipart("policyFile") byte[] policyFileToTransferByte,
			@Multipart("dbDump") byte[] dbSQLDumpFileToTransferByte,
			@Multipart("maxRisk") String maxRiskString) {
		
		File policyFileToTransfer = null;
		File dbSQLDumpFileToTransfer = null;

		float maxRisk = Float.valueOf(maxRiskString);
		
		
		try {
		
			policyFileToTransfer = byteToTemporaryFile(policyFileToTransferByte, "policy");
			dbSQLDumpFileToTransfer = byteToTemporaryFile(dbSQLDumpFileToTransferByte, "dbDump");
		
		} catch (IOException e) {
			return Response.serverError().build();
			
		}

		String result = evaluateDeepSearch_internalComputation(policyFileToTransfer,
				dbSQLDumpFileToTransfer, maxRisk);
		
		if (result.contains("ID: --")) {
			Result res = new Result();
			res.setRequestID(result.split("--")[1]);
			return Response.ok(res).build();
		} else {
			return Response.serverError().build();
		}
		
	}

	@Override
	@GET
	@Path("version")
	@Produces("text/plain")
	public String getVersion() {
		return "FIWARE.Release.2.2.3";
	}

	@Override
	@GET
	@Path("extensions")
	@Produces("text/plain")
	public String getExtensions() {

		return "No extensions available";
	}

	@Override
	@GET
	@Path("getColumnRisk")
	@Produces("text/plain")
	public String getColumnRiskResultURL(@QueryParam("gid") String gid) {
		
		String result = null; 
		result = dba_factory.getRiskColumnResult(Long.parseLong(gid));
		
		return result;
	}

	@Override
	@GET
	@Path("getColumnRisk/{gid}")
	@Produces("text/xml")
	public Response getColumnRiskResult(@PathParam("gid") String gid) {

		RiskColumnResult result = null ;
		
		try {
			result = dba_factory.getRiskColumnResultXML(Long.parseLong(gid));
		} catch (NumberFormatException e) {
			return Response.status(400).entity("Error in Request ID.").build();
		}
		return Response.ok(result).build();
	}

	@Override
	@GET
	@Path("getDeepSearch")
	@Produces("text/plain")
	public String getDeepSearchResultURL(@QueryParam("gid") String gid, 
			@QueryParam("count") int count, @QueryParam("offset") int offset) {

		PolicyProposalResult result = (PolicyProposalResult) this.getDeepSearchkResult(gid, count, offset).getEntity();


		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(PolicyProposalResult.class);

			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter resultStringWriter = new StringWriter();


			jaxbMarshaller.marshal(result, resultStringWriter);

			return resultStringWriter.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
			return "error -1";
		}
	}

	@Override
	@GET
	@Path("getDeepSearch/{gid}/{offset}/{count}")
	@Produces("text/xml")
	public Response getDeepSearchkResult(@PathParam("gid") String gid, @PathParam("count") int count, @PathParam("offset") int offset) {
		
		PolicyProposalResult result = null ;
	
		// maximum number of returned policies is 20
		count = count>0?count:-1;
		count = count < 21? count: 20;
		// regulation of offset
		offset = offset>=0?offset:-1;
		
		try {
			result = dba_factory.getDeepSearchkResultXML(Long.parseLong(gid), count, offset);
		} catch (NumberFormatException e) {
			return Response.status(400).entity("Error in Request ID.").build();
		}
		return Response.ok(result).build();
	}

	@Override
	@POST
	@Produces("text/plain")
	@Consumes("multipart/form-data")
	@Path("evaluateDeepSearch")
	public String evaluateDeepSearchAnalysisURL(MultipartBody multipart) {
		
		List<Attachment> atts = multipart.getAllAttachments();
		if (atts.size() < 4) {
			System.err.println("Please check input parameters, expected " +
					"at least 3, received only: "+atts.size());
			throw new WebApplicationException();
		}

		File policyFileToTransfer = 
				transformMultipartAttachmentToFile(multipart, "policyFile");
		File dbSQLDumpFileToTransfer = 
				transformMultipartAttachmentToFile(multipart, "dbDump");
		
		float max_risk = extractFloatFromMultipart(multipart, "max_risk");
		
		if ((policyFileToTransfer == null)||(dbSQLDumpFileToTransfer == null)) {
			return  Response.status(999).toString();
		}

		String result = evaluateDeepSearch_internalComputation(policyFileToTransfer,
				dbSQLDumpFileToTransfer, max_risk);
		
		return result;

	}



	/**
	 * @param multipart
	 */
	private int extractIntFromMultipart(MultipartBody multipart, String fieldName) {
		Attachment gidAttachment = multipart.getAttachment(fieldName);
		return Integer.parseInt((String)gidAttachment.getObject());
	}
	
	/**
	 * @param multipart
	 */
	private float extractFloatFromMultipart(MultipartBody multipart, String fieldName) {
		Attachment gidAttachment = multipart.getAttachment(fieldName);
		return Float.parseFloat((String)gidAttachment.getObject());
	}
}
