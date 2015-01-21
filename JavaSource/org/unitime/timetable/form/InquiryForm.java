/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;
import org.unitime.timetable.util.IdValue;


/**
 * @author Tomas Muller
 */
public class InquiryForm extends ActionForm {
	private static final long serialVersionUID = -2461671741219768003L;

	private String iOp;

	private String iSubject;

	private String iMessage;

	private int iType;

	private List carbonCopy;

	private String puid=null;
	
	private boolean iNoRole;

	public static String[] sTypeMsgs = new String[] { Constants.BLANK_OPTION_LABEL,
			"Ask a question", "Report an error", "Make a suggestion",
			"Request addition of a time pattern",
			"Request addition of an exact time",
			"Request addition of a date pattern",
			"Request a change of owner on class and/or scheduling subpart",
			"Request a course cross-listing", "Request a room to be shared",
			"Request any other administrative change",
			"LLR/LAB data entry is done", "Other" };
	
	public static String[] sTypeMsgsNoRole = new String[] { Constants.BLANK_OPTION_LABEL,
        "Ask a question", "Report an error", "Make a suggestion"};

	public static String[] sDefaultMessage = new String[] {
			"",

			"",

			"",

			"",

			"1. Specify the list of classes or scheduling subparts on which the requested time pattern need to be set "
					+ "(e.g., AB 100 Lec 1, AB 100 Lec 2) : \r\n"
					+ "\r\n"
					+ "2. Specify the time pattern (number of meetings and number of minutes per meeting, e.g., 2x75) : \r\n"
					+ "\r\n"
					+ "3. Specify the available days "
					+ "(e.g., MW, WF, MF, TTh) : \r\n"
					+ "\r\n"
					+ "4. Specify the available starting times "
					+ "(e.g., 7:30am, 8:30am, 9:30am, ... 4:30pm) : \r\n",

			"1. Specify the list of classes on which the requested time pattern need to be set "
					+ "(e.g., AB 100 Lec 1, AB 100 Lec 2) : \r\n"
					+ "\r\n"
					+ "2. Specify the time "
					+ "(e.g., MWF 8:15am - 10:20am) : ",

			"1. Specify the list of classes or scheduling subparts on which the requested date pattern need to be set "
					+ "(e.g., AB 100 Lec 1, AB 100 Lec 2) : \r\n"
					+ "\r\n"
					+ "2. Specify the list weeks for the date pattern (e.g., Weeks 2-8; alternatively, you can specify start and end date) : \r\n",

			"1. Specify the list of classes or scheduling subparts on which the requested date pattern need to be set "
					+ "(e.g., AB 100 Lec 1, AB 100 Lec 2) : \r\n"
					+ "\r\n"
					+ "2. Specify the new owner " + "(e.g., LLR) : \r\n",

			"1. Specify the controlling course (e.g., AB 100) : \r\n"
					+ "\r\n"
					+ "2. Specify courses that should be cross-listed with the controlling course (e.g., CDFS 100) : \r\n",

			"1. Specify the room that needs to be shared "
					+ "(e.g., GRIS 100) : \r\n"
					+ "\r\n"
					+ "2. Specify the departments between which the room needs to be shared "
					+ "(e.g., 1282-Aeronautics & Astronautics and 1287-Industrial Engineering) : \r\n"
					+ "\r\n"
					+ "3. Specify the times when the room is to be allocated for one of the departments or when the room is not available "
					+ "(e.g., MWF for Aero, TTh for I E, not available after 3:00pm) : \r\n",

			"", "1. Specify the managing department (e.g., LLR or LAB) : \r\n",
			"" };

	// --------------------------------------------------------- Classes

	/** Factory to create dynamic list element for email addresses */
	protected DynamicListObjectFactory factoryEmails = new DynamicListObjectFactory() {
		public Object create() {
			return new String(Constants.BLANK_OPTION_VALUE);
		}
	};

	// --------------------------------------------------------- Methods

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (iType == 0)
			errors.add("type", new ActionMessage("errors.generic",
					"Please specify category of this inquiry:"));

		if (iMessage.trim().length() == 0)
			errors.add("message", new ActionMessage("errors.generic",
					"Message is required."));

		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iSubject = null;
		iMessage = null;
		iType = 0;
		puid = null;
		carbonCopy = DynamicList.getInstance(new ArrayList(), factoryEmails);
	}

	public String getOp() {
		return iOp;
	}

	public void setOp(String op) {
		iOp = op;
	}

	public String getSubject() {
		return iSubject;
	}

	public void setSubject(String subject) {
		iSubject = subject;
	}

	public String getMessage() {
		return iMessage;
	}

	public void setMessage(String message) {
		iMessage = message;
	}

	public int getType() {
		return iType;
	}

	public void setType(int type) {
		iType = type;
	}

	public String getTypeMsg(int type) {
		return sTypeMsgs[iType];
	}

	public Vector getTypeOptions() {
		Vector ret = new Vector();
		for (int i = 0; i < (iNoRole?sTypeMsgsNoRole:sTypeMsgs).length; i++)
			ret.add(new IdValue(new Long(i), (iNoRole?sTypeMsgsNoRole:sTypeMsgs)[i]));
		return ret;
	}

	public void updateMessage() {
		boolean eq = false;
		for (int i = 0; i < sTypeMsgs.length; i++)
			if (sDefaultMessage[i].equals(iMessage))
				eq = true;
		if (!eq)
			return;
		iMessage = sDefaultMessage[iType];
	}

	public String getPuid() {
		return puid;
	}

	public void setPuid(String puid) {
		if (puid!=null && puid.trim().length()==0)
			this.puid = null;
		else
			this.puid = puid;
	}
	
	public boolean getNoRole() { return  iNoRole; }
	public void setNoRole(boolean noRole) { iNoRole = noRole; }

	public List getCarbonCopy() {
		return carbonCopy;
	}

	public void setCarbonCopy(List carbonCopy) {
		this.carbonCopy = carbonCopy;
	}

	public String getCarbonCopy(int key) {
		return carbonCopy.get(key).toString();
	}

	public void setCarbonCopy(int key, Object value) {
		this.carbonCopy.set(key, value);
	}

    public void addToCarbonCopy(String carbonCopy) {
        this.carbonCopy.add(carbonCopy);
    }

    public void removeCarbonCopy(int rowNum) {
        if (rowNum>=0) {
        	carbonCopy.remove(rowNum);
        }
    }
    
}
