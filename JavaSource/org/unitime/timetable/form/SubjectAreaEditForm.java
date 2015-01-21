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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.context.HttpSessionContext;


/** 
 * MyEclipse Struts
 * Creation date: 05-15-2007
 * 
 * XDoclet definition:
 * @struts.form name="subjectAreaEditForm"
 *
 * @author Heston Fernandes, Tomas Muller
 */
public class SubjectAreaEditForm extends ActionForm {
	private static final long serialVersionUID = -8093172074512485680L;

	private Long uniqueId;
	private String op;
	private String abbv;
	private String title;
	private String externalId;
	private Long department ;
	
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

			if(title==null || title.trim().length()==0) {
	        	errors.add("title", new ActionMessage("errors.required", "Title") );
	        }

			if(department==null || department.longValue()<=0) {
	        	errors.add("department", new ActionMessage("errors.required", "Department") );
	        }
			
			if (errors.size()==0) {
				Long sessionId = HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser().getCurrentAcademicSessionId();
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
		title=null;
		externalId=null;
		department=null;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
