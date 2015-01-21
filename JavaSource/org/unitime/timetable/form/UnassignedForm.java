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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class UnassignedForm extends ActionForm {
	private static final long serialVersionUID = -3419420049976489695L;
	private String iOp = null;
	private Long iSubjectArea = null;
	private Collection iSubjectAreas = null;


	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iSubjectArea = null;
		iSubjectAreas = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }

	public Long getSubjectArea() { return iSubjectArea; }
	public String getSubjectAreaAbbv() { return new SubjectAreaDAO().get(iSubjectArea).getSubjectAreaAbbreviation(); }
	public void setSubjectArea(Long subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
}

