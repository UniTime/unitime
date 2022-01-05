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
package org.unitime.timetable.gwt.client.departments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.google.gwt.i18n.client.NumberFormat;
import org.unitime.timetable.gwt.client.admin.AdminCookie;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.rooms.RoomsTable.IntegerCell;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentsColumn;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DepartmentsTable extends UniTimeTable<DepartmentInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private DepartmentsColumn iSortBy = null;
	private boolean iAsc = true;	
	private boolean iSelectable = true;
	
	public DepartmentsTable() {
		setHeaderData(false);
	}
	
	public void selectDept(int row, boolean value) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			((CheckBox)w).setValue(value);
		}
	}
	
	public String getColumnName(DepartmentsColumn column) {
		switch (column) {
		case CODE: return MESSAGES.colCode();
		case ABBV: return MESSAGES.colAbbv();
		case NAME: return MESSAGES.colName();
		case EXTERNAL_MANAGER: return MESSAGES.colExternalManager();
		case SUBJECTS: return MESSAGES.colSubjects();
		case ROOMS: return MESSAGES.colRooms();
		case STATUS: return MESSAGES.colStatus();
		case DIST_PREF_PRIORITY: return MESSAGES.colDistPrefPriority();
		case ALLOW_REQUIRED: return MESSAGES.colAllowRequired();
		case INSTRUCTOR_PREF: return MESSAGES.colInstructorPref();
		case EVENTS: return MESSAGES.colEvents();
		case STUDENT_SCHEDULING: return MESSAGES.colStudentScheduling();
		case EXT_FUNDING_DEPT: return MESSAGES.colExternalFundingDept();
		case LAST_CHANGE: return MESSAGES.colLastChange();		
		
		default: return column.name();
		}
	}
	
	public String getUser() { return UniTimePageHeader.getInstance().getMiddle().getText(); }
	
	public Widget getColumnWidget(DepartmentsColumn column, DepartmentInterface department) {;
		NumberFormat df5 = NumberFormat.getFormat("####0");
		switch (column) {
		case CODE:
			return new Label(department.getDeptCode() == null ? "" : department.getDeptCode());
		case ABBV:
			return new Label(department.getAbbreviation() == null ? "" : department.getAbbreviation());
		case NAME :
			return new HTML(
					department.getName() == null ? "" : department.getName(), false);
		case EXTERNAL_MANAGER:
			return new Label(department.isExternalManager().booleanValue()? department.getExternalMgrAbbv():"");
		case SUBJECTS:
			return new IntegerCell(department.getSubjectAreaCount());
		//	return new Label( df5.format(department.getSubjectAreaCount()));			
		case ROOMS:
			return new IntegerCell(department.getRoomDeptCount());
			//return new Label(df5.format(department.getRoomDeptCount()));
		case STATUS:
			P widget = new P("departments-status");
				P ext = new P("ext-status"); ext.setText(department.effectiveStatusType());
				ext.addStyleName("department-StatusItalics");
				widget.add(ext);
				for (String dependentStatus: department.getDependentStatusesStr()) {
					P ext1 = new P("ext-status"); ext1.setText(dependentStatus);
					ext1.addStyleName("department-Status");
					widget.add(ext1);

				}				
				return widget;
		case DIST_PREF_PRIORITY :
			return new IntegerCell(department.getDistributionPrefPriority()==null && department.getDistributionPrefPriority()==0 ? 0 : department.getDistributionPrefPriority());
		case ALLOW_REQUIRED:
			return new Label(department.allowReq());
		case INSTRUCTOR_PREF:
			P instrucPrefWidget = new P("instruc-pref");
			if(department.isInheritInstructorPreferences()){
				instrucPrefWidget.addStyleName("department-accept");
			}
			return instrucPrefWidget;
		case EVENTS:
			P eventWidget = new P("events");
			if(department.isAllowEvents()){
				eventWidget.addStyleName("department-accept");
			}
			return eventWidget;
		case STUDENT_SCHEDULING:
			P allowStudentSchedulingWidget = new P("allowStudentSchedulingWidget");
			if(department.isAllowStudentScheduling()){
				allowStudentSchedulingWidget.addStyleName("department-accept");
			}
			return allowStudentSchedulingWidget;
		case EXT_FUNDING_DEPT:
			P extFundingDeptWidget = new P("extFundingDeptWidget");
			if (department.isCoursesFundingDepartmentsEnabled() == true) {
				if(department.isExternalFundingDept() != null && department.isExternalFundingDept() == true )
				extFundingDeptWidget.addStyleName("department-accept");						
				return extFundingDeptWidget;		
			}else
				return null;
		case LAST_CHANGE:
			return new HTML(department.getLastChangeStr(), false);			
		default:
			return null;
		}
	}
	
	protected void addRow(DepartmentInterface department) {
		List<Widget> line = new ArrayList<Widget>();
		for (DepartmentsColumn col: DepartmentsColumn.values()){
			if(getColumnWidget(col, department) != null){
				line.add(getColumnWidget(col, department));
			}
			
		}
		addRow(department, line);
	}

	public void setHeaderData (boolean fundingDepartmentsEnabled) {
		clearTable();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final DepartmentsColumn col: DepartmentsColumn.values()) {
						
			if (DepartmentComparator.isApplicable(col) &&(col != DepartmentsColumn.EXT_FUNDING_DEPT || (col == DepartmentsColumn.EXT_FUNDING_DEPT  && fundingDepartmentsEnabled == true))){
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col));
				Operation op = new SortOperation() {
					@Override
					public void execute() { doSort(col); }
					@Override
					public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
					@Override
					public boolean hasSeparator() { return false; }
					@Override
					public String getName() { return MESSAGES.opSortBy(getColumnName()); }
					@Override
					public String getColumnName() { return h.getHTML().replace("<br>", " "); }
				};
			
				h.addOperation(op);
				header.add(h);
			}
			
		}
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		
		if (iSelectable) {
			setAllowSelection(true);
			addMouseClickListener(new MouseClickListener<DepartmentInterface>() {
				@Override
				public void onMouseClick(TableEvent<DepartmentInterface> event) {
					selectDept(event.getRow(), isSelected(event.getRow()));
				}
			});
		}
		
		setSortBy(AdminCookie.getInstance().getSortDepartmentsBy());
	}
	
	public void setData(List<DepartmentInterface> departments, boolean showAlldept) {
		clearTable(1);
		if (departments != null)
			for (DepartmentInterface department: departments)
	             if (showAlldept|| department.getSubjectAreaCount() != 0 || department.getTimetableManagersCount() != 0  || department.isExternalManager().booleanValue()) 
	             {
	            	 addRow(department);
	             }
		sort();
	}
	protected void doSort(DepartmentsColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
		AdminCookie.getInstance().setSortDepartmentsBy(getSortBy());		
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = DepartmentsColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = DepartmentsColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = getHeader(iSortBy.ordinal());
		sort(header, new DepartmentComparator(iSortBy, true), iAsc);
	}
	public static interface SortOperation extends Operation, HasColumnName {}
	
	public static class DepartmentComparator implements Comparator<DepartmentInterface>{
		private DepartmentsColumn iColumn;
		private boolean iAsc;
		
		public DepartmentComparator(DepartmentsColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		public int compareById(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getUniqueId(), r2.getUniqueId());
		}
			
		public int compareByDeptCode(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getDeptCode(), r2.getDeptCode());
		}
		public int compareByName(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getName(), r2.getName());
		}
		public int compareByAbbreviation(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getAbbreviation(), r2.getAbbreviation());
		}
		public int compareByExtMgr(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getExternalMgrAbbv(), r2.getExternalMgrAbbv());
		}
		public int compareBySubjectCount(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getSubjectAreaCount(), r2.getSubjectAreaCount());
		}
		public int compareByRoomCount(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getRoomDeptCount(), r2.getRoomDeptCount());
		}	
		public int compareByStatus(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getStatusTypeStr(), r2.getStatusTypeStr());
		}	
		public int compareByDistPrefPriority(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getDistributionPrefPriority(), r2.getDistributionPrefPriority());
		}	
		public int compareByAllowReqd(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.allowReq(), r2.allowReq());
		}
		public int compareByInstrucPref(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.isInheritInstructorPreferences(), r2.isInheritInstructorPreferences());
		}		
		public int compareByEvent(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.isAllowEvents(), r2.isAllowEvents());
		}		
		public int compareByStdntSched(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.isAllowStudentScheduling(), r2.isAllowStudentScheduling());
		}		
		public int compareByLastChange(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.getLastChangeStr(), r2.getLastChangeStr());
		}

		public int compareByExtFundingDept(DepartmentInterface r1, DepartmentInterface r2) {
			return compare(r1.isExternalFundingDept(), r2.isExternalFundingDept());
		}
		protected int compareByColumn(DepartmentInterface r1, DepartmentInterface r2) {
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
			case LAST_CHANGE: return compareByLastChange(r1, r2);
						
			default: return compareByAbbreviation(r1, r2);
			}
		}
		
		public static boolean isApplicable(DepartmentsColumn column) {
			switch (column) {
			case CODE: 
			case ABBV: 
			case NAME: 
			case EXTERNAL_MANAGER:
			case SUBJECTS: 
			case ROOMS: 
			case STATUS: 
			case DIST_PREF_PRIORITY: 
			case ALLOW_REQUIRED: 
			case INSTRUCTOR_PREF:
			case EVENTS: 
			case STUDENT_SCHEDULING: 	
			case EXT_FUNDING_DEPT:
			case LAST_CHANGE: 
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(DepartmentInterface r1, DepartmentInterface r2) {
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
		
		protected int compare(Boolean b1, Boolean b2) {
			return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? 1 : Boolean.compare(b1.booleanValue(), b2.booleanValue())); 
		}
	}
}
