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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sap.dpre.entities.TransferredFile;

/**
 * Creates temporary files to be used during the computation
 * 
 *
 */
public class TempFiles {
	
	
	/**
	 * Creates the policy temporary file
	 * @return The policy temporary file
	 * @throws IOException
	 */
	public File createTempFile(TransferredFile policy) throws IOException{

		File policyTempFile = copyTransferredFile2File(policy);
		
		return policyTempFile;
	}

	/**
	 * @param policy
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public File copyTransferredFile2File(TransferredFile policy)
			throws IOException, FileNotFoundException {
		File policyTempFile = File.createTempFile(policy.getFileName(), null);
		policyTempFile.deleteOnExit();

		InputStream fis = policy.getFileData().getDataSource().getInputStream();

		copyInputStream2File(policyTempFile, fis);
		return policyTempFile;
	}

	/**
	 * @param fileToCopy
	 * @param fis
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public File copyInputStream2File(File fileToCopy, InputStream fis)
			throws FileNotFoundException, IOException {
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileToCopy)));
		int m;

		while((m = fis.read()) > -1){
		
			out.writeByte(m);
		
		}
		fis.close();
		out.close();
		
		return fileToCopy;
	}
	

}
