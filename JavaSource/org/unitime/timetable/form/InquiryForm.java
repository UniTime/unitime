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
import org.apache.struts.upload.FormFile;
import org.unitime.timetable.model.ContactCategory;
import org.unitime.timetable.model.dao.ContactCategoryDAO;
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

	private long iType;

	private List carbonCopy;

	private String puid=null;
	
	private boolean iNoRole;

	private transient FormFile iFile;

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

		if (iType < 0)
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
		iType = -1;
		puid = null;
		carbonCopy = DynamicList.getInstance(new ArrayList(), factoryEmails);
		iFile = null;
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

	public long getType() {
		return iType;
	}

	public void setType(long type) {
		iType = type;
	}

	public String getTypeMsg(long type) {
		ContactCategory cc = ContactCategoryDAO.getInstance().get(type);
		return cc.getLabel();
	}

	public Vector getTypeOptions() {
		Vector ret = new Vector();
		for (ContactCategory cc: (List<ContactCategory>)ContactCategoryDAO.getInstance().getSession().createQuery(
				"from ContactCategory order by reference").setCacheable(true).list()) {
			if (cc.getHasRole() && iNoRole) continue;
			ret.add(new IdValue(cc.getUniqueId(), cc.getLabel()));
		}
		return ret;
	}

	public void updateMessage() {
		if (iMessage != null && !iMessage.isEmpty()) {
			String message = null;
			boolean eq = false;
			for (ContactCategory cc: (List<ContactCategory>)ContactCategoryDAO.getInstance().getSession().createQuery(
					"from ContactCategory order by reference").setCacheable(true).list()) {
				if (cc.getMessage() != null && cc.getMessage().replaceAll("\\s+","").equals(iMessage.replaceAll("\\s+",""))) eq = true;
				if (cc.getUniqueId().equals(iType)) message = cc.getMessage();
			}
			if (!eq) return;
			iMessage = message;
		} else {
			ContactCategory cc = ContactCategoryDAO.getInstance().get(iType);
			iMessage = (cc == null ? null : cc.getMessage());
		}
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
    
	public FormFile getFile() { return iFile; }
	public void setFile(FormFile file) { iFile = file; }
    
}
