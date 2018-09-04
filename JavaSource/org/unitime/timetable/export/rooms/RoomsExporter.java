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
package org.unitime.timetable.export.rooms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.rooms.RoomsComparator;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EventServiceProviderInterface;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.AttachmentTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.AttachmentType.VisibilityFlag;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.rooms.RoomDetailsBackend;
import org.unitime.timetable.server.rooms.RoomPicturesBackend;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
public abstract class RoomsExporter implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		RoomFilterRpcRequest request = new RoomFilterRpcRequest();
		request.setCommand(FilterRpcRequest.Command.ENUMERATE);
    	request.setSessionId(sessionId);
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.addOption(command.substring(2), value);
    		}
    	}
    	request.addOption("flag", "plain");
    	
    	EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
    	if (context.isAuthenticated() && context.getUser().getExternalUserId() != null)
    		request.setOption("user", context.getUser().getExternalUserId());
    	context.checkPermission(Right.Rooms);
    	
    	ExportContext ec = new ExportContext();
    	if (helper.getParameter("dm") != null)
    		ec.setDepartmentMode(Integer.parseInt(helper.getParameter("dm")));
    	ec.setNrDepartments(Department.findAllBeingUsed(sessionId).size());
    	
    	FilterRpcResponse response = new FilterRpcResponse();
    	new RoomDetailsBackend().enumarate(request, response, context); 
    	
    	List<RoomDetailInterface> rooms = new ArrayList<RoomDetailInterface>();
    	if (response.hasResults()) {
    		for (Entity e: response.getResults())
    			rooms.add((RoomDetailInterface)e);
    	}
    	
    	if (helper.getParameter("sort") != null) {
    		RoomsComparator cmp = null;
    		try {
    			int sort = Integer.parseInt(helper.getParameter("sort"));
    			if (sort > 0) {
    				cmp = new RoomsComparator(RoomsColumn.values()[sort - 1], true);
    			} else if (sort < 0) {
    				cmp = new RoomsComparator(RoomsColumn.values()[-1 - sort], false);
    			}
    		} catch (Exception e) {}
    		if (cmp != null)
    			Collections.sort(rooms, cmp);
    	}
    	
    	ec.setRoomCookieFlags(helper.getParameter("flags") == null ? 0xffff : Integer.parseInt(helper.getParameter("flags")));
    	
    	if (helper.getParameter("orientation") != null) {
    		ec.setVertical("vertical".equals(helper.getParameter("orientation")));
    		ec.setGridAsText("text".equals(helper.getParameter("orientation")));
    	} else if (context.getUser() != null) {
    		ec.setVertical(CommonValues.VerticalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
    		ec.setGridAsText(context.getUser() == null ? false : CommonValues.TextGrid.eq(UserProperty.GridOrientation.get(context.getUser())));
    	}
    	
    	ec.setMode(helper.getParameter("mode"));
    	if (ec.getMode() == null && context.getUser() != null)
    		ec.setMode(RequiredTimeTable.getTimeGridSize(context.getUser()));
    	
    	for (RoomFeatureType type: new TreeSet<RoomFeatureType>(RoomFeatureTypeDAO.getInstance().findAll()))
    		ec.addRoomFeatureType(new FeatureTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.isShowInEventManagement()));
    	
    	for (AttachmentType type: AttachmentType.listTypes(AttachmentType.VisibilityFlag.ROOM_PICTURE_TYPE, VisibilityFlag.SHOW_ROOMS_TABLE))
    		ec.addPictureType(RoomPicturesBackend.getPictureType(type));
    	
    	ec.setDepartment(request.getOption("department"));
    	
    	print(helper, rooms, ec);
	}
	
	protected boolean hasShowHideOperation(RoomsColumn column) {
		switch (column) {
		case NAME:
			return false;
		default:
			return true;
		}
	}
	
	protected int getWebTableNbrCells(RoomsColumn column, ExportContext context) {
		switch (column) {
		case PICTURES:
			return 1 + context.getPictureTypes().size();
		case FEATURES:
			return 1 + context.getRoomFeatureTypes().size();
		default:
			return 1;
		}
	}
	
	protected int getNbrCells(RoomsColumn column, ExportContext context) {
		return getWebTableNbrCells(column, context);
	}
	
	protected boolean isColumnVisible(Column column, ExportContext context) {
		if (!hasShowHideOperation(column.getColumn())) return true;
		int colIndex = 0;
		for (RoomsColumn c: RoomsColumn.values()) {
			if (c.ordinal() < column.getColumn().ordinal()) colIndex += getWebTableNbrCells(c, context);
		}
		if (column.getIndex() < getWebTableNbrCells(column.getColumn(), context)) colIndex += column.getIndex();
		return (context.getRoomCookieFlags() & (1 << colIndex)) != 0;
	}
	
	protected String getColumnName(Column column, ExportContext ec) {
		switch (column.getColumn()) {
		case NAME: return MESSAGES.colName();
		case TYPE: return MESSAGES.colType();
		case EXTERNAL_ID: return MESSAGES.colExternalId();
		case CAPACITY: return MESSAGES.colCapacity();
		case EXAM_CAPACITY: return MESSAGES.colExaminationCapacity();
		case AREA: return MESSAGES.colArea(ApplicationProperty.RoomAreaUnitsMetric.isTrue() ? CONSTANTS.roomAreaMetricUnitsShortPlainText() : CONSTANTS.roomAreaUnitsShortPlainText());
		case COORDINATES: return MESSAGES.colCoordinates();
		case DISTANCE_CHECK: return MESSAGES.colDistances();
		case ROOM_CHECK: return MESSAGES.colRoomCheck();
		case MAP: return MESSAGES.colMap();
		case PICTURES:
			if (column.getIndex() == 0) return MESSAGES.colPictures();
			else return ec.getPictureTypes().get(column.getIndex() - 1).getAbbreviation();
		case PREFERENCE: return MESSAGES.colPreference();
		case AVAILABILITY: return MESSAGES.colAvailability();
		case DEPARTMENTS: return MESSAGES.colDepartments();
		case CONTROL_DEPT: return MESSAGES.colControl();
		case EXAM_TYPES: return MESSAGES.colExamTypes();
		case PERIOD_PREF: return MESSAGES.colPeriodPreferences();
		case EVENT_DEPARTMENT: return MESSAGES.colEventDepartment();
		case EVENT_STATUS: return MESSAGES.colEventStatus();
		case EVENT_AVAILABILITY: return MESSAGES.colEventAvailability();
		case EVENT_MESSAGE: return MESSAGES.colEventMessage();
		case BREAK_TIME: return MESSAGES.colBreakTime();
		case GROUPS: return MESSAGES.colGroups();
		case FEATURES:
			if (column.getIndex() == 0) return MESSAGES.colFeatures();
			else return ec.getRoomFeatureTypes().get(column.getIndex() - 1).getAbbreviation();
		case SERVICES: return MESSAGES.colAvailableServices();
		default: return column.getColumn().name();
		}
	}
	
	protected abstract void print(ExportHelper helper, List<RoomDetailInterface> rooms, ExportContext context) throws IOException;
	
	protected static class ExportContext {
		private String iDepartment = null;
		private List<FeatureTypeInterface> iFeatureTypes = new ArrayList<FeatureTypeInterface>();
		private List<AttachmentTypeInterface> iPictureTypes = new ArrayList<AttachmentTypeInterface>();
		private int iNrDepartments = 0;
		private int iDepartmentMode = 0;
		private int iRoomCookieFlags = 0;
		private boolean iGridAsText = false;
		private boolean iVertical = true;
		private String iMode = null;
		private String iSeparator = "\n";
		private Format<Number> iAreaFormat = Formats.getNumberFormat(CONSTANTS.roomAreaFormat());
		private Format<Number> iCoordinateFormat = Formats.getNumberFormat(CONSTANTS.roomCoordinateFormat());
		
		public void setDepartment(String department) { iDepartment = department; }
		public String getDepartment() { return iDepartment; }
		
		public int getDepartmentMode() { return iDepartmentMode; }
		public void setDepartmentMode(int deptMode) { iDepartmentMode = deptMode; }
		
		public void addRoomFeatureType(FeatureTypeInterface type) { iFeatureTypes.add(type); }
		public List<FeatureTypeInterface> getRoomFeatureTypes() { return iFeatureTypes; }
		
		public void addPictureType(AttachmentTypeInterface type) { iPictureTypes.add(type); }
		public List<AttachmentTypeInterface> getPictureTypes() { return iPictureTypes; }

		public void setRoomCookieFlags(int flags) { iRoomCookieFlags = flags; }
		public int getRoomCookieFlags() { return iRoomCookieFlags; }
		
		public void setGridAsText(boolean gridAsText) { iGridAsText = gridAsText; }
		public boolean isGridAsText() { return iGridAsText; }
		
		public void setVertical(boolean vertical) { iVertical = vertical; }
		public boolean isVertical() { return iVertical; }
		
		public void setMode(String mode) { iMode = mode; }
		public String getMode() { return iMode; }
		
		public void setSeparator(String separator) { iSeparator = separator; }
		public String getSeparator() { return iSeparator; }
		
		public Format<Number> getAreaFormat() { return iAreaFormat; }
		public Format<Number> getCoordinateFormat() { return iCoordinateFormat; }
		
		public void setNrDepartments(int nrDepartments) { iNrDepartments = nrDepartments; }
		
		protected String dept2string(DepartmentInterface d, boolean ext) {
			if (d == null) return "";
			switch (getDepartmentMode()) {
			case 0: return d.getDeptCode();
			case 1: return ext ? d.getExtAbbreviationWhenExist() : d.getAbbreviationOrCode();
			case 2: return ext ? d.getExtLabelWhenExist() : d.getLabel();
			case 3: return ext ? d.getExtAbbreviationWhenExist() + " - " + d.getExtLabelWhenExist() : d.getAbbreviationOrCode() + " - " + d.getLabel();
			case 4: return ext ? d.getDeptCode() + " - " + d.getExtLabelWhenExist() : d.getDeptCode() + " - " + d.getLabel();
			default: return d.getDeptCode();
			}
		}
		
		protected boolean isAllDepartments(Collection<DepartmentInterface> departments) {
			return departments.size() > 3 && iNrDepartments == departments.size();
		}
		
		protected String dept2string(Collection<DepartmentInterface> departments, boolean ext) {
			if (departments == null || departments.isEmpty()) return "";
			if (isAllDepartments(departments)) return MESSAGES.departmentsAllLabel();
			String ret = "";
			for (DepartmentInterface d: departments) {
				ret += (ret.isEmpty() ? "" : getSeparator()) + dept2string(d, ext);
			}
			return ret;
		}
		
		protected String pref2string(Collection<DepartmentInterface> departments) {
			if (departments == null || departments.isEmpty()) return "";
			String ret = "";
			for (DepartmentInterface d: departments) {
				if (d.getPreference() == null) continue;
				ret += (ret.isEmpty() ? "" : getSeparator()) + d.getPreference().getName() + " " + dept2string(d, true);
			}
			return ret;
		}
		
		protected String examTypes2string(Collection<ExamTypeInterface> types) {
			if (types == null || types.isEmpty()) return "";
			String ret = "";
			for (ExamTypeInterface t: types) {
				ret += (ret.isEmpty() ? "" : getSeparator()) + t.getLabel();
			}
			return ret;
		}
		
		protected String features2string(Collection<FeatureInterface> features, FeatureTypeInterface type) {
			if (features == null || features.isEmpty()) return "";
			String ret = "";
			for (FeatureInterface f: features) {
				if (type == null && f.getType() == null)
					ret += (ret.isEmpty() ? "" : getSeparator()) + f.getLabel();
				if (type != null && type.equals(f.getType())) {
					if (f.getDepartment() != null)
						ret += (ret.isEmpty() ? "" : getSeparator()) + f.getLabel() + " (" + dept2string(f.getDepartment(), true) + ")";
					else
						ret += (ret.isEmpty() ? "" : getSeparator()) + f.getLabel();
				}
			}
			return ret;
		}
		
		protected String groups2string(Collection<GroupInterface> groups) {
			if (groups == null || groups.isEmpty()) return "";
			String ret = "";
			for (GroupInterface g: groups) {
				ret += (ret.isEmpty() ? "" : getSeparator()) + g.getLabel() + (g.getDepartment() == null ? "" : " (" + dept2string(g.getDepartment(), true) + ")");
			}
			return ret;
		}
		
		protected String services2string(Collection<EventServiceProviderInterface> services, DepartmentInterface eventDept) {
			if (services == null || services.isEmpty()) return "";
			String ret = "";
			for (EventServiceProviderInterface s: services) {
				ret += (ret.isEmpty() ? "" : getSeparator()) + s.getLabel() + (s.getDepartmentId() == null || eventDept == null ? "" : " (" + dept2string(eventDept, true) + ")");
			}
			return ret;
		}
	}
	
	protected static class Column {
		private RoomsColumn iColumn;
		private int iIndex;
		
		Column(RoomsColumn column, int index) { iColumn = column; iIndex = index; }
		
		public int getIndex() { return iIndex; }
		public RoomsColumn getColumn() { return iColumn; }
	}
}