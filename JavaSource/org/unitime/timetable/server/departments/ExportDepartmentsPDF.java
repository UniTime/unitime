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
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentsColumn;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.security.SessionContext;

@Service("org.unitime.timetable.export.Exporter:departments.pdf")
public class ExportDepartmentsPDF implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public String reference() {
		return "departments.pdf";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		SessionContext context = helper.getSessionContext();
		boolean dispLastChanges = CommonValues.Yes.eq(UserProperty.DisplayLastChanges.get(context.getUser()));
		helper.getSessionContext().checkPermission(Right.Departments);
		
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		
        DecimalFormat df5 = new DecimalFormat("####0.######");
        out.printHeader( MESSAGES.propDepartmentlist(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel()));
  			
        List<Department> departments = new ArrayList<Department>(Department.findAll(helper.getAcademicSessionId()));
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

		/*
		 * write to pdf
		 */
		Boolean printHeaderText = false;
            for (Department d: departments) { 
            	/*
            	 * Header
            	 */
            	if (!printHeaderText) {
                	ArrayList<String> headerText = new ArrayList<String>(); 
                	headerText.add(MESSAGES.colNumber());
                	headerText.add(MESSAGES.colAbbv());
                	headerText.add(MESSAGES.colName());
                	headerText.add(MESSAGES.colExternalManager().replace("<br>","\n"));
                	headerText.add(MESSAGES.colSubjects());
                	headerText.add(MESSAGES.colRooms());
                	headerText.add(MESSAGES.colStatus());
                	headerText.add(MESSAGES.colDistPrefPriority().replace("<br>","\n"));
                	headerText.add(MESSAGES.colAllowRequired().replace("<br>","\n"));
                	headerText.add(MESSAGES.colInstructorPref().replace("<br>","\n"));
                	headerText.add(MESSAGES.colEvents());
                	headerText.add(MESSAGES.colStudentScheduling().replace("<br>","\n"));
                	if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue())
                		headerText.add(MESSAGES.colExternalFundingDept().replace("<br>","\n"));
                	if (dispLastChanges)
                	headerText.add(MESSAGES.colLastChange().replace("<br>","\n"));

                	String[] headerTextStr = new String[headerText.size()]; 
                	for (int i = 0; i < headerText.size(); i++) {
                		headerTextStr[i] = headerText.get(i);
                    }
                	
            		out.printHeader(headerTextStr);
            		printHeaderText = true;
            	}
            	
                
                if (showAllDept.trim().equalsIgnoreCase("true") || !d.getSubjectAreas().isEmpty() || !d.getTimetableManagers().isEmpty() || d.isExternalManager().booleanValue()) {

                    String lastChangeStr = null;
                    
                    if (dispLastChanges) {
                            List<ChangeLog> changes = ChangeLog.findLastNChanges(d.getSession().getUniqueId(), null, null, d.getUniqueId(), 1);
                            ChangeLog lastChange = (changes==null || changes.isEmpty() ? null : (ChangeLog) changes.get(0));
                            lastChangeStr = (lastChange==null?"":MESSAGES.lastChange(
                            	    ChangeLog.sDFdate.format(lastChange.getTimeStamp()),
                            	    lastChange.getManager().getShortName()));

                    }
                    String allowReq = "";
                    int allowReqOrd = 0;
                    if (d.isAllowReqRoom() != null && d.isAllowReqRoom().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += MESSAGES.colRooms();
                    	allowReqOrd += 1;
                    }
                    if (d.isAllowReqTime() != null && d.isAllowReqTime().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += MESSAGES.colTime();
                    	allowReqOrd += 2;
                    }
                    if (d.isAllowReqDistribution() != null && d.isAllowReqDistribution().booleanValue()) {
                    	if (!allowReq.isEmpty()) allowReq += ", ";
                    	allowReq += MESSAGES.colDistribution();
                    	allowReqOrd += 4;
                    }
                    if (allowReqOrd == 7) allowReq = MESSAGES.colAll();
                    
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

                    /*
                     * Body
                     */
                    ArrayList<String> bodyText = new ArrayList<String>(); 
                    
                    bodyText.add(d.getDeptCode());
                    bodyText.add(d.getAbbreviation());
                    bodyText.add(d.getName());
                    bodyText.add(d.isExternalManager().booleanValue()?d.getExternalMgrAbbv():"");
                    bodyText.add(df5.format(d.getSubjectAreas().size()));
                    bodyText.add(df5.format(d.getRoomDepts().size()));
                    bodyText.add(d.effectiveStatusType().getLabel()+ (dependentStatuses == null ? "" : "\n" + dependentStatuses));
                    bodyText.add((d.getDistributionPrefPriority()==null && d.getDistributionPrefPriority().intValue()!=0 ? "" : d.getDistributionPrefPriority().toString()));
                    bodyText.add(allowReq);
                    bodyText.add(d.isInheritInstructorPreferences() ? MESSAGES.exportTrue()  : MESSAGES.exportFalse() );
                    bodyText.add( d.isAllowEvents() ? MESSAGES.exportTrue()  : MESSAGES.exportFalse() );
                    bodyText.add(d.isAllowStudentScheduling() ? MESSAGES.exportTrue()  : MESSAGES.exportFalse() );
                	if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue())
                		bodyText.add(d.isExternalFundingDept()!=null ? (d.isExternalFundingDept()==true ? MESSAGES.exportTrue()  : MESSAGES.exportFalse() ): MESSAGES.exportFalse() );
                	if (dispLastChanges)
                		bodyText.add(lastChangeStr);
                	
                	String[] bodyTextStr = new String[bodyText.size()]; 
                	for (int i = 0; i < bodyText.size(); i++) {
                		bodyTextStr[i] = bodyText.get(i);
                    }
                	out.printLine(bodyTextStr);
 
                }
            }
            
		out.close();
	}
	
	class DepartmentComparator implements Comparator<Department>{
		private DepartmentsColumn iColumn;
		private boolean iAsc;
		
		public DepartmentComparator(DepartmentsColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		public int compareByExternalId(Department r1, Department r2) {
			return compare(r1.getExternalUniqueId(), r2.getExternalUniqueId());
		}
		
		public int compareById(Department r1, Department r2) {
			return compare(r1.getUniqueId(), r2.getUniqueId());
		}
			
		public int compareByDeptCode(Department r1, Department r2) {
			return compare(r1.getDeptCode(), r2.getDeptCode());
		}
		public int compareByName(Department r1, Department r2) {
			return compare(r1.getName(), r2.getName());
		}
		public int compareByAbbreviation(Department r1, Department r2) {
			return compare(r1.getAbbreviation(), r2.getAbbreviation());
		}
		public int compareByExtMgr(Department r1, Department r2) {
			return compare(r1.getExternalMgrAbbv(), r2.getExternalMgrAbbv());
		}	
		public int compareByStatus(Department r1, Department r2) {
			return compare(r1.getStatusType().getLabel(), r2.getStatusType().getLabel());
		}	
		public int compareByDistPrefPriority(Department r1, Department r2) {
			return compare(r1.getDistributionPrefPriority(), r2.getDistributionPrefPriority());
		}	
		public int compareByAllowReqd(Department r1, Department r2) {
			return compare(r1.getAllowReqTime(), r2.getAllowReqTime());
		}
		public int compareByInstrucPref(Department r1, Department r2) {
			return compare(r1.isInheritInstructorPreferences(), r2.isInheritInstructorPreferences());
		}		
		public int compareByEvent(Department r1, Department r2) {
			return compare(r1.isAllowEvents(), r2.isAllowEvents());
		}		
		public int compareByStdntSched(Department r1, Department r2) {
			return compare(r1.isAllowStudentScheduling(), r2.isAllowStudentScheduling());
		}		

		public int compareByExtFundingDept(Department r1, Department r2) {
			return compare(r1.isExternalFundingDept(), r2.isExternalFundingDept());
		}
		
		public int compareBySubjectCount(Department r1, Department r2) {
			return compare(r1.getSubjectAreas().size(), r2.getSubjectAreas().size());
		}
		public int compareByRoomCount(Department r1, Department r2) {
			return compare(r1.getRoomDepts().size(), r2.getRoomDepts().size());
		}		
				
		protected int compareByColumn(Department r1, Department r2) {
			switch (iColumn) {					
			case CODE: return compareByDeptCode(r1, r2);
			case ABBV: return compareByAbbreviation(r1, r2);
			case NAME: return compareByName(r1, r2);
			case EXTERNAL_MANAGER: return compareByExtMgr(r1, r2);	
			case SUBJECTS: return compareBySubjectCount(r1, r2);
			case ROOMS: return compareByRoomCount(r1, r2);
			case STATUS: return compareByStatus(r1, r2);
			case DIST_PREF_PRIORITY: return compareByDistPrefPriority(r1, r2);
			case ALLOW_REQUIRED: return compareByAllowReqd(r1, r2);
			case INSTRUCTOR_PREF: return compareByInstrucPref(r1, r2);
			case EVENTS: return compareByEvent(r1, r2);
			case STUDENT_SCHEDULING: return compareByStdntSched(r1, r2);	
			case EXT_FUNDING_DEPT: return compareByExtFundingDept(r1, r2);

						
			default: return compareByAbbreviation(r1, r2);
			}
		}
		
		protected int compare(Boolean b1, Boolean b2) {
			return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? 1 : Boolean.compare(b1.booleanValue(), b2.booleanValue())); 
		}
	
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
