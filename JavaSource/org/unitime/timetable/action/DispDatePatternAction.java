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
package org.unitime.timetable.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;

@Action(value = "dispDatePattern", results = {
		@Result(name = "display", location = "/user/dispDatePattern.jsp")
	})
public class DispDatePatternAction extends UniTimeAction<BlankForm>{
	private static final long serialVersionUID = -4812922736216024977L;
	private Long id, classId, subpartId;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getClassId() { return classId; }
	public void setClassId(Long classId) { this.classId = classId; }
	public Long getSubpartId() { return subpartId; }
	public void setSubpartId(Long subpartId) { this.subpartId = subpartId; }
	
	@Override
	public String execute() {
		return "display";
	}
	
	public DatePattern getDatePattern() {
		if (id != null) {
			DatePattern dp = DatePatternDAO.getInstance().get(id);
			if (dp != null) return dp;
		}
		if (classId != null) {
			Class_ clazz = Class_DAO.getInstance().get(classId);
			if (clazz != null) 
				return clazz.effectiveDatePattern();
		}
		if (subpartId != null) {
			SchedulingSubpart ss = SchedulingSubpartDAO.getInstance().get(subpartId);
			if (ss != null) 
				return ss.effectiveDatePattern();
		}
		return SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()).getDefaultDatePatternNotNull();
	}

}
