/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-15-2007
 * 
 * XDoclet definition:
 * @struts.form name="subjectAreaEditForm"
 */
public class SubjectAreaEditForm extends ActionForm {
	/*
	 * Generated fields
	 */

	/** op property */
	private Long uniqueId;
	private String op;
	private String abbv;
	private String shortTitle;
	private String longTitle;
	private String externalId;
	private Long department ;
	private Boolean scheduleBkOnly;
	private Boolean pseudo;
	
	/*
	 * Generated Methods
	 */

	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		// Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        
		if (op.equals(rsc.getMessage("button.deleteSubjectArea"))) {
	        SubjectArea sa = new SubjectAreaDAO().get(getUniqueId());
			if (sa.hasOfferedCourses()) {
				errors.add("uniqueid", new ActionMessage("errors.generic", "A subject area with offered classes cannot be deleted") );
			}
		}
		else {
			if(abbv==null || abbv.trim().length()==0) {
	        	errors.add("abbv", new ActionMessage("errors.required", "Abbreviation") );
	        }

			if(shortTitle==null || shortTitle.trim().length()==0) {
	        	errors.add("shortTitle", new ActionMessage("errors.required", "Short Title") );
	        }

			if(longTitle==null || longTitle.trim().length()==0) {
	        	errors.add("longTitle", new ActionMessage("errors.required", "Long Title") );
	        }

			if(department==null || department.longValue()<=0) {
	        	errors.add("department", new ActionMessage("errors.required", "Department") );
	        }
			
			if (errors.size()==0) {
				Long sessionId = (Long) ((Web.getUser(request.getSession())).getAttribute(Constants.SESSION_ID_ATTR_NAME));
				SubjectArea sa = SubjectArea.findByAbbv(sessionId, abbv);
				if (uniqueId==null && sa!=null) 
		        	errors.add("abbv", new ActionMessage("errors.generic", "A subject area with the abbreviation exists for the academic session") );
				if (uniqueId!=null && sa!=null && !sa.getUniqueId().equals(uniqueId)) 
		        	errors.add("abbv", new ActionMessage("errors.generic", "A subject area with the abbreviation exists for the academic session") );
			}
		}
		
		return errors;
	}

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		uniqueId=null;
		op=null;
		abbv=null;
		shortTitle=null;
		longTitle=null;
		externalId=null;
		department=null;
		scheduleBkOnly=null;
		pseudo=null;
	}

	/** 
	 * Returns the op.
	 * @return String
	 */
	public String getOp() {
		return op;
	}

	/** 
	 * Set the op.
	 * @param op The op to set
	 */
	public void setOp(String op) {
		this.op = op;
	}

	public String getAbbv() {
		return abbv;
	}

	public void setAbbv(String abbv) {
		if (abbv!=null)
			this.abbv = abbv.toUpperCase();
		else
			this.abbv = abbv;
	}

	public Long getDepartment() {
		return department;
	}

	public void setDepartment(Long department) {
		this.department = department;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getLongTitle() {
		return longTitle;
	}

	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	public Boolean getPseudo() {
		return pseudo;
	}

	public void setPseudo(Boolean pseudo) {
		if (pseudo==null)
			this.pseudo = Boolean.FALSE;
		else
			this.pseudo = pseudo;
	}

	public Boolean getScheduleBkOnly() {
		return scheduleBkOnly;
	}

	public void setScheduleBkOnly(Boolean scheduleBkOnly) {
		if (scheduleBkOnly==null)
			this.scheduleBkOnly = Boolean.FALSE;
		else
			this.scheduleBkOnly = scheduleBkOnly;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	public Long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(Long uniqueId) {
		if (uniqueId!=null && uniqueId.longValue()<=0)
			this.uniqueId = null;
		else
			this.uniqueId = uniqueId;
	}
	
	
}