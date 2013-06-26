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
package com.sap.dpre.policy;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import com.sap.primelife.dpre.DisclosurePolicyRiskEvaluator;
import com.sap.dpre.log.MyLogger;
import com.sap.dpre.policy.DataColumn.ColumnType;

public class PolicyParser_Utilities {
	private File myPolicyFile;
	private Document myPolicyDoc;

	public PolicyParser_Utilities(File policyFile) {
			myPolicyFile = policyFile;
	}

	public File getMyPolicyFile() {
		return myPolicyFile;
	}

	public void setMyPolicyFile(File myPolicyFile) {
		this.myPolicyFile = myPolicyFile;
	}

	public Document getMyPolicyDoc() {
		return myPolicyDoc;
	}

	public void setMyPolicyDoc(Document myPolicyDoc) {
		this.myPolicyDoc = myPolicyDoc;
	}

	/**
	 * parse the policy document
	 * 
	 * @return true if everything OK
	 */
	public boolean parsePolicyDocument() {
	
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
	
			// parse using builder to get DOM representation of the XML file
			setMyPolicyDoc(db.parse(getMyPolicyFile()));
	
			// everything OK
			return true;
		} catch (ParserConfigurationException e) {
			if (true) {
				e.printStackTrace();
			}
	
			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			if (true) {
				e.printStackTrace();
			}
	
			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (true) {
				e.printStackTrace();
			}
	
			// log exception
			MyLogger.getInstance().writeLog(Level.SEVERE,
					e.getLocalizedMessage());
		}
	
		// something went wrong
		return false;
	}

	/**
	 * apply policy
	 */
	public void applyPolicy() {
	
		// get the root element
		Element docEle = getMyPolicyDoc().getDocumentElement();
	
		// get the node list of elements
		NodeList nl = docEle.getElementsByTagName("Column");
	
		// load all policy columns
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
	
				// load the column
				this.loadPolicyColumn((Element) nl.item(i));
			}
		}
	}

	void loadPolicyColumn(Element el) {
	
		// for each <column> element get values of name, type and hide
		String name = this.getTextValue(el, "Name");
		ColumnType type = this.getTypeValue(el, "Type");
		boolean hide = this.getBooleanValue(el, "Hide");
	
		// set the policy column
		DataColumn dc = Policy.getInstance().getDataColumnByName(name);
		if (dc != null) {
			dc.setColumnType(type);
			dc.setIsHide(hide);
		}
	}

	/**
	 * take a xml element and the tag name, look for the tag and get the text
	 * content
	 * @param ele TODO
	 * @param tagName TODO
	 */
	String getTextValue(Element ele, String tagName) {
	
		String textVal = null;
	
		NodeList nl = ele.getElementsByTagName(tagName);
	
		if (nl != null && nl.getLength() > 0) {
	
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}
	
		return textVal.replaceAll("\\s\\s+|\\n|\\r|\\t", "");
	}

	/**
	 * get the column type
	 * @param ele
	 * @param tagName
	 * 
	 * @return column type
	 */
	ColumnType getTypeValue(Element ele, String tagName) {
	
		// get the type
		String type = getTextValue(ele, tagName);
	
		// switch on the type
		if (type.toLowerCase().compareTo(
				ColumnType.IDENTIFIER.name().toLowerCase()) == 0) {
			return ColumnType.IDENTIFIER;
		} else if (type.toLowerCase().compareTo(
				ColumnType.SENSITIVE.name().toLowerCase()) == 0) {
			return ColumnType.SENSITIVE;
		} else {
			return ColumnType.UNKNOWN;
		}
	}

	/**
	 * parse boolean
	 * @param ele
	 *            element to be parsed from
	 * @param tagName
	 *            name of the tag
	 * 
	 * @return parsed boolean
	 */
	boolean getBooleanValue(Element ele, String tagName) {
	
		// in production application you would catch the exception
		return Boolean.parseBoolean(getTextValue(ele, tagName));
	}
}
