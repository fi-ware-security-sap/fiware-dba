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
package com.sap.dpre.log;

import java.io.IOException;

import java.util.logging.FileHandler;
import java.util.logging.Level;
//import java.util.logging.Logger;
import org.apache.log4j.Logger;
import java.util.logging.SimpleFormatter;

//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;

/**
 * used to log errors and other important events
 * 
 * 
 * 
 */
public class MyLogger {

	private static final MyLogger MY_LOGGER = new MyLogger();

	private static Logger myLogger = Logger.getLogger(MyLogger.class);

	/**
	 * log location
	 */
	private final String LOG_LOCATION = System.getProperty("user.dir")
			+ System.getProperty("file.separator") + "DPRE_log.txt";

	/**
	 * constructor
	 */
	private MyLogger() {

		// initialization
//		this.myLogger = 
	}

	/**
	 * get the MyLogger instance
	 * 
	 * @return MyLogger instance
	 */
	public static MyLogger getInstance() {

		return MY_LOGGER;
	}

	/**
	 * configure the logger
	 */
	public void configureLogger() {

		
		// write initial line to distinguish each application start
		this.writeLog(Level.CONFIG,
			"####################### START ###########################");
	
	}

	/**
	 * write log message to file. 2 Levels are supported:
	 * <ul>
	 * <li>{@link Level#ALL}: for debug messages,</li>
	 * <li>{@link Level#INFO}: for relevant messages</li>
	 * </ul>
	 * Default Level is: INFO.
	 * 
	 * @param level
	 *            level of the message
	 * @param msg
	 *            message to be written
	 */
	public synchronized void writeLog(Level level, String msg) {

		if (level == Level.ALL) {
			myLogger.debug(msg);
		} else {
			myLogger.info(msg);
		}
	}

	/**
	 * create new FileHander for logging
	 * 
	 * @return new FileHander for logging
	 */
	private FileHandler createLogFileHandler() {

		// try to create the FileHandler
		try {
			return new FileHandler(this.LOG_LOCATION, true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			if (true) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (true) {
				e.printStackTrace();
			}
		}


		return null;
	}
}
