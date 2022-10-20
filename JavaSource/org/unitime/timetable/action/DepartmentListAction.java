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

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;


/**
 * @author Tomas Muller
 */
@Action(value = "departmentList", results = {
		@Result(name = "showDepartmentList", type = "tiles", location = "departmentList.tiles"),
		@Result(name = "add", type = "redirect", location="/departmentEdit.action", params = {
				"op", "${op}"})
	})
@TilesDefinition(name = "departmentList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Departments"),
		@TilesPutAttribute(name = "body", value = "/admin/departmentList.jsp")
	})
public class DepartmentListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = 1316912229218015591L;
	protected static final GwtMessages MSG = Localization.create(GwtMessages.class);
	
	private boolean showUnusedDepts = false;
	private String op2 = null;
	public boolean getShowUnusedDepts() { return showUnusedDepts; }
	public void setShowUnusedDepts(boolean showUnusedDepts) { this.showUnusedDepts = showUnusedDepts; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }


	@Override
	public String execute() throws Exception {

		sessionContext.checkPermission(Right.Departments);
		
        if ("Apply".equals(op2)) {
        	sessionContext.getUser().setProperty("Departments.showUnusedDepts", showUnusedDepts ? "1" : "0");
        } else {
        	showUnusedDepts = "1".equals(sessionContext.getUser().getProperty("Departments.showUnusedDepts", "0"));
        }

        boolean dispLastChanges = isDisplayLastChanges();
        
        if (stripAccessKey(MSG.buttonAddDepartment()).equals(request.getParameter("op"))) {
        	setOp(stripAccessKey(MSG.buttonAddDepartment()));
        	return "add";
        }

        if (stripAccessKey(MSG.buttonExportPDF()).equals(request.getParameter("op"))) {
            PdfWebTable webTable = new PdfWebTable((dispLastChanges ? 13 : 12),
            		MSG.propDepartmentlist(sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel()),
                    "departmentList.action?ord=%%",
                    (dispLastChanges ? new String[] {
                    		MSG.colCode(), MSG.colAbbv(), MSG.colName(), MSG.colExternalManager().replace("<br>", "\n"),
                    		MSG.colSubjects(), MSG.colRooms(), MSG.colStatus(), MSG.colDistPrefPriority().replace("<br>", "\n"),
                    		MSG.colAllowRequired().replace("<br>", "\n"), MSG.colInstructorPref().replace("<br>", "\n"),
                    		MSG.colEvents(), MSG.colStudentScheduling().replace("<br>", "\n"), MSG.colLastChange()
                    }
                    : new String[] {
                    		MSG.colCode(), MSG.colAbbv(), MSG.colName(), MSG.colExternalManager().replace("<br>", "\n"),
                    		MSG.colSubjects(), MSG.colRooms(), MSG.colStatus(), MSG.colDistPrefPriority().replace("<br>", "\n"),
                    		MSG.colAllowRequired().replace("<br>", "\n"), MSG.colInstructorPref().replace("<br>", "\n"),
                    		MSG.colEvents(), MSG.colStudentScheduling().replace("<br>", "\n")
                    		}),
                    new String[] { "left", "left", "left", "left", "right", "right", "left", "right", "left", "left", "left", "left", "left" },
                    new boolean[] { true, true, true, true, true, true, true, true, true, true, true, (dispLastChanges ? true: false), false });
            for (Department d: getDepartments()) {
                if (getShowUnusedDepts() || !d.getSubjectAreas().isEmpty() || !d.getTimetableManagers().isEmpty() || d.isExternalManager().booleanValue()) {
                    DecimalFormat df5 = new DecimalFormat("####0");

                    String lastChangeStr = null;
                    Long lastChangeCmp = null;
                    if (dispLastChanges) {
                            List changes = ChangeLog.findLastNChanges(d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
                            ChangeLog lastChange = (changes==null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
                            lastChangeStr = (lastChange==null?"":ChangeLog.sDFdate.format(lastChange.getTimeStamp())+" by "+lastChange.getManager().getShortName());
                            lastChangeCmp = Long.valueOf(lastChange==null?0:lastChange.getTimeStamp().getTime());
                    }
                    String allowReq = "";
                    int allowReqOrd = 0;
                    if (d.isAllowReqRoom() != null && d.isAllowReqRoom().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += MSG.colRoom();
                    	allowReqOrd += 1;
                    }
                    if (d.isAllowReqTime() != null && d.isAllowReqTime().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += MSG.colTime();
                    	allowReqOrd += 2;
                    }
                    if (d.isAllowReqDistribution() != null && d.isAllowReqDistribution().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += MSG.colDistribution();
                    	allowReqOrd += 4;
                    }
                    if (allowReqOrd == 7) allowReq = MSG.colAll();
                    
                    String dependentStatuses = null;
                    if (d.isExternalManager() && d.getExternalStatusTypes() != null && !d.getExternalStatusTypes().isEmpty()) {
                    	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
            				@Override
            				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
            					return e1.getDepartment().compareTo(e2.getDepartment());
            				}
            			});
                    	set.addAll(d.getExternalStatusTypes());
                    	for (ExternalDepartmentStatusType t: set) {
                    		if (dependentStatuses == null)
                    			dependentStatuses = "    " + t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                    		else
                    			dependentStatuses += "\n    " + t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                    	}
                    }

                    webTable.addLine(null,
                            new String[] {
                                d.getDeptCode(),
                                d.getAbbreviation(),
                                d.getName(),
                                (d.isExternalManager().booleanValue()?d.getExternalMgrAbbv():""),
                                df5.format(d.getSubjectAreas().size()),
                                df5.format(d.getRoomDepts().size()),
                                (d.getStatusType() == null ? "@@ITALIC " : "")+d.effectiveStatusType().getLabel()+(d.getStatusType() == null?"@@END_ITALIC " : "") + (dependentStatuses == null ? "" : "\n" + dependentStatuses),
                                (d.getDistributionPrefPriority()==null && d.getDistributionPrefPriority().intValue()!=0 ? "" : d.getDistributionPrefPriority().toString()),
                                allowReq,
                                d.isInheritInstructorPreferences() ? MSG.exportTrue() : MSG.exportFalse(),
                                d.isAllowEvents() ? MSG.exportTrue() : MSG.exportFalse(),
                                d.isAllowStudentScheduling() ? MSG.exportTrue() : MSG.exportFalse(),
                                lastChangeStr },
                           new Comparable[] {
                            d.getDeptCode(),
                            d.getAbbreviation(),
                            d.getName(),
                            (d.isExternalManager().booleanValue() ? d.getExternalMgrAbbv() : ""),
                            Integer.valueOf(d.getSubjectAreas().size()),
                            Integer.valueOf(d.getRoomDepts().size()),
                            d.effectiveStatusType().getOrd(),
                            d.getDistributionPrefPriority(),
                            Integer.valueOf(allowReqOrd),
                            d.isInheritInstructorPreferences(),
                            d.isAllowEvents(),
                            d.isAllowStudentScheduling(),
                            lastChangeCmp });
                }
            }
            
            ExportUtils.exportPDF(
            		webTable,
            		WebTable.getOrder(sessionContext, "DepartmentList.ord"),
            		response, "departments");
            return null;
        }
        
		WebTable webTable = new WebTable((dispLastChanges ? 13 : 12), "",
				"departmentList.action?ord=%%",
				(dispLastChanges 
					? new String[] {
							MSG.colCode(), MSG.colAbbreviation(), MSG.colName(), MSG.colExternalManager(),
                    		MSG.colSubjects(), MSG.colRooms(), MSG.colStatus(), MSG.colDistPrefPriority(),
                    		MSG.colAllowRequired(), MSG.colInstructorPref(),
                    		MSG.colEvents(), MSG.colStudentScheduling(), MSG.colLastChange()} 
					: new String[] {
							MSG.colCode(), MSG.colAbbreviation(), MSG.colName(), MSG.colExternalManager(),
                    		MSG.colSubjects(), MSG.colRooms(), MSG.colStatus(), MSG.colDistPrefPriority(),
                    		MSG.colAllowRequired(), MSG.colInstructorPref(),
                    		MSG.colEvents(), MSG.colStudentScheduling() }),
				new String[] { "left", "left", "left", "left", "right", "right", "left", "right", "left", "left", "left", "left", "left" },
                new boolean[] { true, true, true, true, true, true, true, true, true, true, true, (dispLastChanges ? true: false), false });
		WebTable.setOrder(sessionContext, "DepartmentList.ord", request.getParameter("ord"), 1);
        webTable.enableHR("#9CB0CE");
        webTable.setRowStyle("white-space: nowrap");
        
        for (Department d: getDepartments()) {
    		if (getShowUnusedDepts() || !d.getSubjectAreas().isEmpty() || !d.getTimetableManagers().isEmpty() || d.isExternalManager().booleanValue()) {
    				
    			DecimalFormat df5 = new DecimalFormat("####0");

    			String lastChangeStr = null;
    			Long lastChangeCmp = null;
    			if (dispLastChanges) {
    					List changes = ChangeLog.findLastNChanges(d
    							.getSession().getUniqueId(), null, null, d
    							.getUniqueId(), 1);
    					ChangeLog lastChange = (changes == null
    							|| changes.isEmpty() ? null
    							: (ChangeLog) changes.get(0));
    					lastChangeStr = (lastChange == null ? "&nbsp;"
    							: "<span title='"
    							+ lastChange.getLabel()
    							+ "'>"
    							+ ChangeLog.sDFdate.format(lastChange
    							.getTimeStamp())
    							+ " by "
    							+ lastChange.getManager()
    							.getShortName() + "</span>");
    					lastChangeCmp = Long.valueOf(lastChange == null ? 0
    							: lastChange.getTimeStamp().getTime());
    			}
    			
                        String allowReq = "";
                        int allowReqOrd = 0;
                        if (d.isAllowReqRoom() != null && d.isAllowReqRoom().booleanValue()) {
                        	if (!allowReq.isEmpty()) allowReq += ", ";
                        	allowReq += MSG.colRoom();
                        	allowReqOrd += 1;
                        }
                        if (d.isAllowReqTime() != null && d.isAllowReqTime().booleanValue()) {
                        	if (!allowReq.isEmpty()) allowReq += ", ";
                        	allowReq += MSG.colTime();
                        	allowReqOrd += 2;
                        }
                        if (d.isAllowReqDistribution() != null && d.isAllowReqDistribution().booleanValue()) {
                        	if (!allowReq.isEmpty()) allowReq += ", ";
                        	allowReq += MSG.colDistribution();
                        	allowReqOrd += 4;
                        }
                        if (allowReqOrd == 7) allowReq = MSG.colAll();
                        if (allowReqOrd == 0) allowReq = "&nbsp;";
                        
                String dependentStatuses = null;
                if (d.isExternalManager() && d.getExternalStatusTypes() != null && !d.getExternalStatusTypes().isEmpty()) {
                	TreeSet<ExternalDepartmentStatusType> set = new TreeSet<ExternalDepartmentStatusType>(new Comparator<ExternalDepartmentStatusType>() {
        				@Override
        				public int compare(ExternalDepartmentStatusType e1, ExternalDepartmentStatusType e2) {
        					return e1.getDepartment().compareTo(e2.getDepartment());
        				}
        			});
                	set.addAll(d.getExternalStatusTypes());
                	for (ExternalDepartmentStatusType t: set) {
                		if (dependentStatuses == null)
                			dependentStatuses = t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                		else
                			dependentStatuses += "<br>" + t.getDepartment().getDeptCode() + ": " + t.getStatusType().getLabel();
                	}
                }
                
                boolean editable = sessionContext.hasPermission(d, Right.DepartmentEdit) || sessionContext.hasPermission(d, Right.DepartmentLimitedEdit);

                WebTableLine line = webTable.addLine(
    				(editable ? "onClick=\"document.location='departmentEdit.action?op=Edit&id=" + d.getUniqueId() + "';\"" : null),
    				new String[] {
    						d.getDeptCode(),
    						d.getAbbreviation()==null ? "&nbsp;" : d.getAbbreviation(),
    						d.getName(),
    						(d.isExternalManager().booleanValue() 
    							? "<span title='" + d.getExternalMgrLabel()	+ "'>" + d.getExternalMgrAbbv()	+ "</span>"
    							: "&nbsp;"),
    						df5.format(d.getSubjectAreas().size()),
    						df5.format(d.getRoomDepts().size()),
    						(d.getStatusType() == null ? "<i>" : "&nbsp;")
    							+ d.effectiveStatusType().getLabel()
    							+ (d.getStatusType() == null ? "</i>" : "") + (dependentStatuses == null ? "" : "<div style='padding-left:30px;'>" + dependentStatuses + "</div>"),
    						(d.getDistributionPrefPriority() == null && d.getDistributionPrefPriority().intValue() != 0 
    							? "&nbsp;" : d.getDistributionPrefPriority().toString()),
    						allowReq,
    						(d.isInheritInstructorPreferences() ? "<IMG border='0' align='absmiddle' src='images/accept.png'>" : ""),
    						(d.isAllowEvents() ? "<IMG border='0' align='absmiddle' src='images/accept.png'>" : ""),
    						(d.isAllowStudentScheduling() ? "<IMG border='0' align='absmiddle' src='images/accept.png'>" : ""),
    						lastChangeStr },
    				new Comparable[] {
    						d.getDeptCode(),
    						d.getAbbreviation()==null ? "&nbsp;" : d.getAbbreviation(),
    						d.getName(),
    						(d.isExternalManager()
    						.booleanValue() ? d
    						.getExternalMgrAbbv() : ""),
    						Integer.valueOf(d.getSubjectAreas()
    						.size()),
    						Integer.valueOf(d.getRoomDepts().size()),
    						d.effectiveStatusType().getOrd(),
    						d.getDistributionPrefPriority(),
    						Integer.valueOf(allowReqOrd),
    						d.isInheritInstructorPreferences(),
    						d.isAllowEvents(),
    						d.isAllowStudentScheduling(),
    						lastChangeCmp });
                line.setUniqueId(d.getUniqueId().toString());
    		}
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext, "DepartmentList.ord")));
        
		return "showDepartmentList";
		
	}
	
	public TreeSet<Department> getDepartments() {
		return Department.findAll(sessionContext.getUser().getCurrentAcademicSessionId());
	}
	
	public boolean isDisplayLastChanges() {
		return CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(sessionContext.getUser()));
	}
	
	public String getTitle() {
		return MSG.propDepartmentlist(sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
	}
}
