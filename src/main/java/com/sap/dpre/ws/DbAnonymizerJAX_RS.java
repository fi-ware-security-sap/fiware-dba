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

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;

public interface DbAnonymizerJAX_RS {

	/**
	 * Starts the evaluation of the given policy
	 * @return The generated ID associated to the policy evaluation
	 */
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("multipart/form-data")
	@Path("evaluatePolicy")
	public abstract String evaluatePolicy(
			MultipartBody multipart);
	
	
	@POST
	@Path("evaluatePolicy")
	@Consumes("multipart/mixed")
	@Produces(MediaType.TEXT_XML)
	public abstract Response evaluatePolicy(
			@Multipart("policyFile") byte [] policyFileToTransfer,
			@Multipart("dbDump") byte [] dbSQLDumpFileToTransfer);
	

	/**
	 * Get the policy evaluation result
	 * @return The result of the policy evaluation associated to the given ID
	 */
	@GET
	@Path("getPolicyResult")
	@Produces(MediaType.TEXT_PLAIN)
	public abstract String getPolicyResultURL(@QueryParam("gid") String gid);
	
	/**
	 * Get the policy evaluation result
	 * @return The result of the policy evaluation associated to the given ID
	 */
	@GET
	@Path("getPolicyResult/{gid}")
	@Produces(MediaType.TEXT_PLAIN)
	public abstract Response getPolicyResult(@PathParam("gid") String gid);

	
	/**
	 * Get the policy evaluation result
	 * @return The result of the policy evaluation associated to the given ID
	 */
	@GET
	@Path("getColumnRisk")
	@Produces(MediaType.TEXT_PLAIN)
	public abstract String getColumnRiskResultURL(@QueryParam("gid") String gid);
	
	/**
	 * Get the policy evaluation result
	 * @return The result of the policy evaluation associated to the given ID
	 */
	@GET
	@Path("getColumnRisk/{gid}")
	@Produces(MediaType.TEXT_XML)
	public abstract Response getColumnRiskResult(@PathParam("gid") String gid);

	
	/**
	 * Get the policy evaluation result
	 * @return The result of the policy evaluation associated to the given ID
	 */
	@GET
	@Path("getDeepSearch")
	@Produces(MediaType.TEXT_PLAIN)
	public abstract String getDeepSearchResultURL(@QueryParam("gid") String gid,  
			@QueryParam("count") int count, @QueryParam("offset") int offset);
	
	/**
	 * Get the policy evaluation result
	 * @return The result of the policy evaluation associated to the given ID
	 */
	@GET
	@Path("getDeepSearch/{gid}/{offset}/{count}")
	@Produces(MediaType.TEXT_XML)
	public abstract Response getDeepSearchkResult(@PathParam("gid") String gid,  
			@PathParam("count") int count, @PathParam("offset") int offset);

	
	
	@POST
	@Path("evaluateColumnRisk")
	@Consumes("multipart/mixed")
	@Produces(MediaType.TEXT_XML)
	public abstract Response evaluateColumns(
			@Multipart("dbDump") byte [] dbSQLDumpFileToTransfer);

	
	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("multipart/form-data")
	@Path("evaluateColumnRisk")
	public abstract String evaluateColumns(
			MultipartBody multipart);
	

	@POST
	@Path("evaluateDeepSearch")
	@Consumes("multipart/mixed")
	@Produces(MediaType.TEXT_XML)
	public abstract Response evaluateDeepSearchAnalysis(
			@Multipart("policyFile") byte [] policyFileToTransfer,
			@Multipart("dbDump") byte [] dbSQLDumpFileToTransfer,
			@Multipart("maxRisk") String maxRiskString);
	

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes("multipart/form-data")
	@Path("evaluateDeepSearch")
	public abstract String evaluateDeepSearchAnalysisURL(
			MultipartBody multipart);
	
	
	///
	
	@GET
	@Path("version")
	@Produces(MediaType.TEXT_PLAIN)
	public abstract String getVersion();
	
	
	@GET
	@Path("extensions")
	@Produces(MediaType.TEXT_PLAIN)
	public abstract String getExtensions();
	
	
}
