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
package org.unitime.timetable.server.departments;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsPage.SingleTeachingAssingment;
import org.unitime.timetable.gwt.client.instructor.TeachingAssignmentsTable.COLUMN;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentsColumn;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;

@Service("org.unitime.timetable.export.Exporter:departments.pdf")
public class ExportDepartmentsPDF implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public String reference() {
		return "departments.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		//System.out.println ("export");
		SessionContext context = helper.getSessionContext();
		boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(context.getUser()));
		System.out.println("dispLastChanges"+ dispLastChanges); 
		helper.getSessionContext().checkPermission(Right.Departments);
		
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		
        DecimalFormat df5 = new DecimalFormat("####0.######");
        out.printHeader( "Department List - " + context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());

        out.printHeader(dispLastChanges ? new String[] { "Number", "Abbv", "Name", "External\nManager", "Subjects", "Rooms",
                "Status", "Dist Pref\nPriority", "Allow\nRequired", "Instructor\nPref", "Events", "Student\nScheduling", "Ext Funding\nDept","Last\nChange" } 
        : new String[] { "Number", "Abbreviation", "Name", "External\nManager", "Subjects", "Rooms", "Status",
                "Dist Pref\nPriority", "Allow\nRequired", "Instructor\nPref", "Events", "Student\nScheduling" , "Ext Funding\nDept","Last\nChange" });
        
        
        List<Department> departments = new ArrayList(Department.findAll(helper.getAcademicSessionId()));
		String sort = helper.getParameter("sort");
		String showAllDept = helper.getParameter("showAllDept");

		if (sort != null && !"0".equals(sort)) {
			int sortBy = Integer.valueOf(sort);
			DepartmentComparator cmp = null;
			if (sortBy == 0) {
				// no sort
			} else if (sortBy > 0) {
				cmp = new DepartmentComparator(DepartmentsColumn.values()[sortBy - 1], true);
			} else {
				cmp = new DepartmentComparator(DepartmentsColumn.values()[-1 - sortBy], false);
			}
			if (cmp != null)
				Collections.sort(departments, cmp);
		}

            for (Department d: departments) { 
                if (showAllDept.trim().equalsIgnoreCase("true") || !d.getSubjectAreas().isEmpty() || !d.getTimetableManagers().isEmpty() || d.isExternalManager().booleanValue()) {

                    String lastChangeStr = null;
                    Long lastChangeCmp = null;
                    if (dispLastChanges) {
                            List changes = ChangeLog.findLastNChanges(d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
                            ChangeLog lastChange = (changes==null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
                            lastChangeStr = (lastChange==null?"":ChangeLog.sDFdate.format(lastChange.getTimeStamp())+" by "+lastChange.getManager().getShortName());
                            lastChangeCmp = new Long(lastChange==null?0:lastChange.getTimeStamp().getTime());
                    }
                    String allowReq = "";
                    int allowReqOrd = 0;
                    if (d.isAllowReqRoom() != null && d.isAllowReqRoom().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += "room";
                    	allowReqOrd += 1;
                    }
                    if (d.isAllowReqTime() != null && d.isAllowReqTime().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += "time";
                    	allowReqOrd += 2;
                    }
                    if (d.isAllowReqDistribution() != null && d.isAllowReqDistribution().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += "distribution";
                    	allowReqOrd += 4;
                    }
                    if (allowReqOrd == 7) allowReq = "all";
                    
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

                    out.printLine(
                                d.getDeptCode(),
                                d.getAbbreviation(),
                                d.getName(),
                                (d.isExternalManager().booleanValue()?d.getExternalMgrAbbv():""),
                                df5.format(d.getSubjectAreas().size()),
                                df5.format(d.getRoomDepts().size()),
                                d.effectiveStatusType().getLabel()+ (dependentStatuses == null ? "" : "\n" + dependentStatuses),
                                (d.getDistributionPrefPriority()==null && d.getDistributionPrefPriority().intValue()!=0 ? "" : d.getDistributionPrefPriority().toString()),
                                allowReq,
                                d.isInheritInstructorPreferences() ? "Yes" : "No",
                                d.isAllowEvents() ? "Yes" : "No",
                                d.isAllowStudentScheduling() ? "Yes" : "No",
                                d.isExternalFundingDept()!=null ? (d.isExternalFundingDept()==true ? "Yes" : "No" ): "No",
                                lastChangeStr );
                }
            }
            
		out.close();
	}
	
	public static class DepartmentComparator implements Comparator<Department>{
		private DepartmentsColumn iColumn;
		private boolean iAsc;
		
		public DepartmentComparator(DepartmentsColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		public int compareById(Department r1, Department r2) {
			return compare(r1.getUniqueId(), r2.getUniqueId());
		}
		
		public int compareByName(Department r1, Department r2) {
			return compare(r1.getName(), r2.getName());
		}

		public int compareByAbbreviation(Department r1, Department r2) {
			return compare(r1.getAbbreviation(), r2.getAbbreviation());
		}
		
		public int compareByExternalId(Department r1, Department r2) {
			return compare(r1.getExternalUniqueId(), r2.getExternalUniqueId());
		}
		
		protected int compareByColumn(Department r1, Department r2) {
			switch (iColumn) {
			case NAME: return compareByName(r1, r2);
			case ABBV: return compareByAbbreviation(r1, r2);
			case EXTERNAL_MANAGER: return compareByExternalId(r1, r2);
			default: return compareByAbbreviation(r1, r2);
			}
		}
		
		public static boolean isApplicable(DepartmentsColumn column) {
			switch (column) {
			case ABBV:
			case NAME:
			case EXTERNAL_MANAGER:
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(Department r1, Department r2) {
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByAbbreviation(r1, r2);
			if (cmp == 0) cmp = compareById(r1, r2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
	}

}
