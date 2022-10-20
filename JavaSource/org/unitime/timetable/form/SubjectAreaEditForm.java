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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/** 
 * @author Heston Fernandes, Tomas Muller
 */
public class SubjectAreaEditForm implements UniTimeForm {
	private static final long serialVersionUID = -8093172074512485680L;
	protected static final GwtMessages MSG = Localization.create(GwtMessages.class);
	
	private Long uniqueId;
	private String abbv;
	private String title;
	private String externalId;
	private Long department ;
	
	public SubjectAreaEditForm() {
		reset();
	}

	@Override
	public void validate(UniTimeAction action) {
        if (UniTimeAction.stripAccessKey(MSG.buttonDelete()).equals(action.getOp())) {
            SubjectArea sa = new SubjectAreaDAO().get(getUniqueId());
			if (sa.hasOfferedCourses()) {
				action.addFieldError("form.uniqueId", MSG.errorCannotDeleteSubjectAreaWithClasses());
			}
        } else {
			if(abbv==null || abbv.trim().isEmpty()) {
				action.addFieldError("form.abbv", MSG.errorRequired(MSG.fieldAbbreviation()));
	        }

			if(title==null || title.trim().isEmpty()) {
				action.addFieldError("form.title", MSG.errorRequired(MSG.fieldTitle()));
	        }

			if(department==null || department.longValue()<=0) {
				action.addFieldError("form.department", MSG.errorRequired(MSG.fieldDepartment()));
	        }
			
			if (!action.hasFieldErrors()) {
				Long sessionId = action.getSessionContext().getUser().getCurrentAcademicSessionId();
				SubjectArea sa = SubjectArea.findByAbbv(sessionId, abbv);
				if (uniqueId==null && sa!=null) 
					action.addFieldError("form.abbv", MSG.errorMustBeUnique(MSG.fieldAbbreviation()));
				if (uniqueId!=null && sa!=null && !sa.getUniqueId().equals(uniqueId))
					action.addFieldError("form.abbv", MSG.errorMustBeUnique(MSG.fieldAbbreviation()));
			}
		}
	}

	@Override
	public void reset() {
		uniqueId=null;
		abbv=null;
		title=null;
		externalId=null;
		department=null;
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
