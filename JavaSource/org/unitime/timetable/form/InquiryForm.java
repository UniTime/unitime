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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ContactCategory;
import org.unitime.timetable.model.dao.ContactCategoryDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;
import org.unitime.timetable.util.IdValue;


/**
 * @author Tomas Muller
 */
public class InquiryForm implements UniTimeForm {
	private static final long serialVersionUID = -2461671741219768003L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	private String iOp;

	private String iSubject;

	private String iMessage;

	private long iType;

	private List<String> carbonCopy;
	private List<String> carbonCopyName;

	private String puid=null;
	
	private boolean iNoRole;

	private transient File iFile;  
    private String iFileContentType;  
    private String iFileFileName;

	/** Factory to create dynamic list element for email addresses */
	protected DynamicListObjectFactory<String> factoryEmails;
	
	public InquiryForm() {
		factoryEmails = new DynamicListObjectFactory() {
			public Object create() {
				return new String(Constants.BLANK_OPTION_VALUE);
			}
		};
		reset();
	}

	public void validate(UniTimeAction action) {
		if (iType < 0)
			action.addFieldError("form.type", MSG.errorInquiryPleaseSpecifyCategory());

		if (iMessage.trim().length() == 0)
			action.addFieldError("form.message", MSG.errorInquiryMessageRequired());
	}

	public void reset() {
		iOp = null;
		iSubject = null;
		iMessage = null;
		iType = -1;
		puid = null;
		carbonCopy = DynamicList.getInstance(new ArrayList<String>(), factoryEmails);
		carbonCopyName = DynamicList.getInstance(new ArrayList<String>(), factoryEmails);
		iFile = null; iFileContentType = null; iFileFileName = null;
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

	public List<IdValue> getTypeOptions() {
		List<IdValue> ret = new ArrayList<IdValue>();
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

	public List<String> getCarbonCopy() {
		return carbonCopy;
	}

	public void setCarbonCopy(List<String> carbonCopy) {
		this.carbonCopy = carbonCopy;
	}

	public String getCarbonCopy(int key) {
		return carbonCopy.get(key);
	}

	public void setCarbonCopy(int key, String value) {
		this.carbonCopy.set(key, value);
	}
	
	public List<String> getCarbonCopyName() {
		return carbonCopyName;
	}

	public void setCarbonCopyName(List<String> carbonCopyName) {
		this.carbonCopyName = carbonCopyName;
	}

	public String getCarbonCopyName(int key) {
		return carbonCopyName.get(key);
	}

	public void setCarbonCopyName(int key, String value) {
		this.carbonCopyName.set(key, value);
	}

    public void addToCarbonCopyName(String email, String name) {
    	this.carbonCopy.add(email);
        this.carbonCopyName.add(name);
    }

    public void removeCarbonCopy(int rowNum) {
        if (rowNum>=0) {
        	carbonCopy.remove(rowNum);
        	carbonCopyName.remove(rowNum);
        }
    }
    
	public File getFile() { return iFile; }
	public void setFile(File file) { iFile = file; }
	public String getFileContentType() { return iFileContentType; }
	public void setFileContentType(String contentType) { iFileContentType = contentType; }
	public String getFileFileName() { return iFileFileName; }
	public void setFileFileName(String fileName) { iFileFileName = fileName; }
}
