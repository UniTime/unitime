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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.InstructorListBuilder;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller, Zuzana Mullerova
 */
@Action(value="instructorSearch", results = {
	@Result(name = "showSearch", type = "tiles", location = "instructorSearch.tiles"),
	@Result(name = "showList", type = "tiles", location = "instructorSearch.tiles"),
	@Result(name = "manageInstructorList", type = "redirect", location = "/instructorListUpdate.action"),
	@Result(name = "addNewInstructor", type = "redirect", location = "/instructorAdd.action")
})
@TilesDefinitions(value = {
	@TilesDefinition(name = "instructorSearch.tiles", extend =  "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Instructors"),
			@TilesPutAttribute(name = "body", value = "/user/instructorSearch.jsp")
	}),
})
public class InstructorSearchAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -7920936708671752660L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	private String iDeptId;
	private boolean iHasSurveys = false;
	
	public String getDeptId() { return iDeptId; }
	public void setDeptId(String deptId) { iDeptId = deptId; }
	
	public boolean getHasSurveys() { return iHasSurveys; }
	public void setHasSurveys(boolean hasSurveys) { iHasSurveys = hasSurveys; }
	
	public String execute() throws IOException {
		if (ApplicationProperty.LegacyInstructors.isFalse()) {
    		String url = "instructors";
    		boolean first = true;
    		for (Enumeration<String> e = getRequest().getParameterNames(); e.hasMoreElements(); ) {
    			String param = e.nextElement();
    			url += (first ? "?" : "&") + param + "=" + URLEncoder.encode(getRequest().getParameter(param), "utf-8");
    			first = false;
    		}
    		response.sendRedirect(url);
			return null;
    	}
		if (MSG.actionManageInstructorList().equals(getOp())) {
			return "manageInstructorList";
		}
		if (MSG.actionAddNewInstructor().equals(getOp())) {
			return "addNewInstructor";
		}
		
		sessionContext.checkPermission(Right.Instructors);

		setupManagerDepartments();
		
		if (getDeptId() == null || getDeptId().isEmpty())
			setDeptId((String)sessionContext.getAttribute(SessionAttribute.DepartmentId));

		if (MSG.actionSearchInstructors().equals(getOp()) && (getDeptId() == null || getDeptId().isEmpty())) {
			addActionError(MSG.errorRequiredDepartment());
		}
		
		if (getDeptId() == null || getDeptId().isEmpty() || !sessionContext.hasPermission(getDeptId(), "Department", Right.Instructors)) {
			return "showSearch";
		}
		
		sessionContext.setAttribute(SessionAttribute.DepartmentId, getDeptId());
		
		WebTable.setOrder(sessionContext,"instructorList.ord",request.getParameter("order"),2);
		InstructorListBuilder ilb = new InstructorListBuilder();
		String backId = ("PreferenceGroup".equals(request.getParameter("backType"))?request.getParameter("backId"):null);
		String tblData = ilb.htmlTableForInstructor(sessionContext, getDeptId(), WebTable.getOrder(sessionContext,"instructorList.ord"), backId);
		if (tblData == null || tblData.trim().isEmpty()) {
			addActionError(MSG.errorNoInstructorsFoundInSearch());
			return "showSearch";
		} else {
			request.setAttribute("instructorList", tblData);
			if (MSG.actionExportPdf().equals(op)) {
				PdfWebTable table = ilb.pdfTableForInstructor(sessionContext, getDeptId(), true);
				if (table != null) {
					try {
						ExportUtils.exportPDF(table, WebTable.getOrder(sessionContext,"instructorList.ord"), response, "instructors");
						return null;
					} catch (Exception e) {
						addActionError(MSG.exportFailed(e.getMessage()));
					}
				}
			} else if (MSG.actionExportCsv().equals(op)) {
				PdfWebTable table = ilb.pdfTableForInstructor(sessionContext, getDeptId(), false);
				if (table != null) {
					try {
						ExportUtils.exportCSV(table, WebTable.getOrder(sessionContext,"instructorList.ord"), response, "instructors");
						return null;
					} catch (Exception e) {
						addActionError(MSG.exportFailed(e.getMessage()));
					}
				}
			} else if (MSG.actionExportSurveysXLS().equals(op)) {
				response.sendRedirect(response.encodeURL("export?output=instructor-surveys.xls&department=" + getDeptId()));
				return null;
			}
		}
		
		if (getDeptId() != null && !getDeptId().isEmpty()) {
			setHasSurveys(InstructorSurvey.hasInstructorSurveys(Long.valueOf(getDeptId())));
		} else {
			setHasSurveys(false);	
		}
		
		if (getDeptId() != null && !getDeptId().isEmpty()) {
			Department d = DepartmentDAO.getInstance().get(Long.valueOf(getDeptId()));
			if (d!=null) {
				BackTracker.markForBack(
						request,
						"instructorSearch.action?deptId="+d.getUniqueId(),
						MSG.backInstructors(d.getDeptCode()+" - "+d.getName()),
						true, true
						);
			}
		} else if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null) {
			Department d = (DepartmentDAO.getInstance()).get(Long.valueOf(sessionContext.getAttribute(SessionAttribute.DepartmentId).toString()));
			if (d!=null) {
				BackTracker.markForBack(
						request,
						"instructorSearch.action?deptId="+d.getUniqueId(),
						MSG.backInstructors(d.getDeptCode()+" - "+d.getName()),
						true, true
						);
			}
		} else {
			BackTracker.markForBack(
					request,
					"instructorSearch.action",
					MSG.backInstructors2(),
					true, true
					);
		}		
		return "showList";
	}

	
    private Set<Department> setupManagerDepartments() {
    	Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());

		if (departments.isEmpty())
			addActionError(MSG.exceptionNoDepartmentToManage());
		
		List<IdValue> labelValueDepts = new ArrayList<IdValue>();
		for (Department d: departments)
			labelValueDepts.add(new IdValue(d.getUniqueId(), d.getDeptCode() + " - " + d.getName()));
		
		if (labelValueDepts.size() == 1)
			setDeptId(labelValueDepts.get(0).getId().toString());
		
		request.setAttribute(Department.DEPT_ATTR_NAME,labelValueDepts);
		return departments;
    }

}
