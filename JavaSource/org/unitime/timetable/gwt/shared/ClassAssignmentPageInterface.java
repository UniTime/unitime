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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClassAssignmentPageInterface {
	
	public static enum Operation {
		INIT,
		UPDATE,
		LOCK,
		ASSIGN,
	}
	
	public static enum RoomOrder {
		NAME_ASC,
		NAME_DESC,
		SIZE_ASC,
		SIZE_DESC,
	}
	
	public static class ClassAssignmentPageRequest implements GwtRpcRequest<ClassAssignmentPageResponse> {
		private Long iSelectedClassId;
		private Long iPreviousClassId;
		private Long iOfferingId;
		private Operation iOperation;
		private List<ChangeInterface> iChanges;
		private Boolean iUseRealStudents = null;
		private Boolean iShowStudentConflicts = null;
		private Boolean iKeepConflictingAssignments = false;
		private Boolean iRoomAllowConflicts = false;
		private RoomFilterRpcRequest iRoomFilter;
		private RoomOrder iRoomOrder;
		
		public Long getSelectedClassId() { return iSelectedClassId; }
		public void setSelectedClassId(Long classId) { iSelectedClassId = classId; }
		public Long getPreviousClassId() { return iPreviousClassId; }
		public void setPreviousClassId(Long classId) { iPreviousClassId = classId; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }

		public boolean hasChanges() { return iChanges != null && !iChanges.isEmpty(); }
		public List<ChangeInterface> getChanges() { return iChanges; }
		public boolean hasChange(Long classId) {
			if (iChanges == null) return false;
			for (ChangeInterface ch: iChanges)
				if (ch.getClassId().equals(classId)) return true;
			return false;
		}
		public ChangeInterface getChange(Long classId) {
			if (iChanges == null) iChanges = new ArrayList<ChangeInterface>();
			for (ChangeInterface ch: iChanges)
				if (ch.getClassId().equals(classId)) return ch;
			ChangeInterface ch = new ChangeInterface();
			ch.setClassId(classId);
			iChanges.add(ch);
			return ch;
		}
		public ChangeInterface removeChange(Long classId) {
			if (iChanges == null) return null;
			for (Iterator<ChangeInterface> i = iChanges.iterator(); i.hasNext();) {
				ChangeInterface ch = i.next();
				if (ch.getClassId().equals(classId)) {
					i.remove();
					return ch;
				}
			}
			return null;
		}
		public void addChange(Long classId, String date, String time, String room) {
			if (iChanges == null) iChanges = new ArrayList<ChangeInterface>();
			ChangeInterface old = getChange(classId);
			if (old == null) {
				iChanges.add(new ChangeInterface(classId, date, time, room));
			} else {
				old.setDate(date); old.setTime(time); old.setRoom(room);
			}
		}
		
	    public void setUseRealStudents(boolean userReal) { iUseRealStudents = userReal; }
	    public Boolean isUseRealStudents() { return iUseRealStudents; }
	    public void setKeepConflictingAssignments(boolean keepConflictingAssignments) { iKeepConflictingAssignments = keepConflictingAssignments; }
	    public Boolean isKeepConflictingAssignments() { return iKeepConflictingAssignments; }
	    public void setShowStudentConflicts(boolean showStudentConflicts) { iShowStudentConflicts = showStudentConflicts; }
	    public Boolean isShowStudentConflicts() { return iShowStudentConflicts; }
	    public void setRoomAllowConflicts(boolean roomAllowConflicts) { iRoomAllowConflicts = roomAllowConflicts; }
	    public Boolean isRoomAllowConflicts() { return iRoomAllowConflicts; }
		public RoomFilterRpcRequest getRoomFilter() { return iRoomFilter; }
		public void setRoomFilter(RoomFilterRpcRequest filter) { iRoomFilter = filter; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
		public void setRoomOrder(RoomOrder ord) { iRoomOrder = ord; }
		public RoomOrder getRoomOrder() { return (iRoomOrder == null ? RoomOrder.NAME_ASC : iRoomOrder); }
	}
	
	public static class ClassAssignmentPageResponse implements GwtRpcResponse {
		private Long iSessionId;
	    private Long iSelectedClassId;
	    private String iClassName;
	    private boolean iShowStudentConflicts = true;
	    private boolean iUseRealStudents = true;
	    private boolean iKeepConflictingAssignments = false;
	    private boolean iCanAssign = false;
		private TableInterface iProperties;
		private TableInterface iAssignments;
		private TableInterface iStudentConflicts;
		private List<DomainItem> iDates, iTimes, iRooms;
		private String iUrl;
		private String iErrorMessage;
		private String iManagingDeptCode;
		private Integer iMinRoomCapacity, iNbrRooms;
		private String iDatesErrorMessage, iTimesErrorMessage, iRoomsErrorMessage;
		private Boolean iRoomSplitAttendance;

		public Long getSelectedClassId() { return iSelectedClassId; }
		public void setSelectedClassId(Long classId) { iSelectedClassId = classId; }
		
		public String getClassName() { return iClassName; }
		public void setClassName(String className) { iClassName = className; }
	    
	    public boolean isUseRealStudents() { return iUseRealStudents; }
	    public void setUseRealStudents(boolean userReal) { iUseRealStudents = userReal; }
	    
	    public boolean isShowStudentConflicts() { return iShowStudentConflicts; }
	    public void setShowStudentConflicts(boolean showConflicts) { iShowStudentConflicts = showConflicts; }
	    
	    public void setKeepConflictingAssignments(boolean keepConflictingAssignments) { iKeepConflictingAssignments = keepConflictingAssignments; }
	    public boolean isKeepConflictingAssignments() { return iKeepConflictingAssignments; }
	    
		public boolean hasProperties() { return iProperties != null && iProperties.hasProperties(); }
		public void addProperty(PropertyInterface property) {
			if (iProperties == null) iProperties = new TableInterface();
			iProperties.addProperty(property);
		}
		public TableInterface getProperties() { return iProperties; }
		public void setProperties(TableInterface properties) { iProperties = properties; }
		public CellInterface addProperty(String text) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(new CellInterface());
			addProperty(p);
			return p.getCell();
		}
		public void addProperty(String text, CellInterface cell) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(cell);
			addProperty(p);
		}
		
		public boolean hasAssignments() { return iAssignments != null; }
		public TableInterface getAssignments() { return iAssignments; }
		public void setAssignments(TableInterface assignments) { iAssignments = assignments; }

		public boolean hasStudentConflicts() { return iStudentConflicts != null; }
		public TableInterface getStudentConflicts() { return iStudentConflicts; }
		public void setStudentConflicts(TableInterface studentConflicts) { iStudentConflicts = studentConflicts; }
		
		public boolean hasDates() { return iDates != null && !iDates.isEmpty(); }
		public List<DomainItem> getDates() { return iDates; }
		public void setDates(List<DomainItem> dates) { iDates = dates; }
		public void addDate(DomainItem date) {
			if (iDates == null) iDates = new ArrayList<DomainItem>();
			iDates.add(date);
		}
		
		public boolean hasTimes() { return iTimes != null && !iTimes.isEmpty(); }
		public List<DomainItem> getTimes() { return iTimes; }
		public void setTimes(List<DomainItem> dates) { iTimes = dates; }
		public void addTime(DomainItem time) {
			if (iTimes == null) iTimes = new ArrayList<DomainItem>();
			iTimes.add(time);
		}

		public boolean hasRooms() { return iRooms != null && !iRooms.isEmpty(); }
		public List<DomainItem> getRooms() { return iRooms; }
		public void setRooms(List<DomainItem> dates) { iRooms = dates; }
		public void addRoom(DomainItem room) {
			if (iRooms == null) iRooms = new ArrayList<DomainItem>();
			iRooms.add(room);
		}
		
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }
		
		public boolean hasErrorMessage() { return iErrorMessage != null && !iErrorMessage.isEmpty(); }
		public void setErrorMessage(String message) { iErrorMessage = message; }
		public void addErrorMessage(String message) {
			if (iErrorMessage == null || iErrorMessage.isEmpty())
				iErrorMessage = message;
			else
				iErrorMessage += "\n" + message;
		}
		public String getErrorMessage() { return iErrorMessage; }
		
		public boolean hasDatesErrorMessage() { return iDatesErrorMessage != null && !iDatesErrorMessage.isEmpty(); }
		public void setDatesErrorMessage(String message) { iDatesErrorMessage = message; }
		public void addDatesErrorMessage(String message) {
			if (iDatesErrorMessage == null || iDatesErrorMessage.isEmpty())
				iDatesErrorMessage = message;
			else
				iDatesErrorMessage += "\n" + message;
		}
		public String getDatesErrorMessage() { return iErrorMessage; }
		
		public boolean hasTimesErrorMessage() { return iTimesErrorMessage != null && !iTimesErrorMessage.isEmpty(); }
		public void setTimesErrorMessage(String message) { iTimesErrorMessage = message; }
		public void addTimesErrorMessage(String message) {
			if (iTimesErrorMessage == null || iTimesErrorMessage.isEmpty())
				iTimesErrorMessage = message;
			else
				iTimesErrorMessage += "\n" + message;
		}
		public String getTimesErrorMessage() { return iTimesErrorMessage; }
		
		public boolean hasRoomsErrorMessage() { return iRoomsErrorMessage != null && !iRoomsErrorMessage.isEmpty(); }
		public void setRoomsErrorMessage(String message) { iRoomsErrorMessage = message; }
		public void addRoomsErrorMessage(String message) {
			if (iRoomsErrorMessage == null || iRoomsErrorMessage.isEmpty())
				iRoomsErrorMessage = message;
			else
				iRoomsErrorMessage += "\n" + message;
		}
		public String getRoomsErrorMessage() { return iRoomsErrorMessage; }
		
		public String getManagingDeptCode() { return iManagingDeptCode; }
		public void setManagingDeptCode(String deptCode) { iManagingDeptCode = deptCode; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Integer getMinRoomCapacity() { return iMinRoomCapacity; }
		public void setMinRoomCapacity(Integer cap) { iMinRoomCapacity = cap; }
		public Integer getNbrRooms() { return iNbrRooms; }
		public void setNbrRooms(Integer nbrRooms) { iNbrRooms = nbrRooms; }
		public Boolean isRoomSplitAttendance() { return iRoomSplitAttendance; }
		public void setRoomSplitAttendance(Boolean roomSplitAttendance) { iRoomSplitAttendance = roomSplitAttendance; }
		public boolean isCanAssign() { return iCanAssign; }
		public void setCanAssign(boolean canAssign) { iCanAssign = canAssign; }
	}
	
	public static class ChangeInterface implements IsSerializable {
		private Long iClassId;
		private String iDate;
		private String iTime;
		private String iRoom;
		
		public ChangeInterface() {}
		public ChangeInterface(Long classId, String date, String time, String room) {
			iClassId = classId;
			iDate = date;
			iTime = time;
			iRoom = room;
		}
		
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
		
		public String getDate() { return iDate; }
		public void setDate(String date) { iDate = date; }
		public boolean hasDate() { return iDate != null && !iDate.isEmpty(); }
		
		public String getTime() { return iTime; }
		public boolean hasTime() { return iTime != null && !iTime.isEmpty(); }
		public void setTime(String time) { iTime = time; }
		
		public String getRoom() { return iRoom; }
		public boolean hasRoom() { return iRoom != null && !iRoom.isEmpty(); }
		public void setRoom(String room) { iRoom = room; }
		
		@Override
		public int hashCode() { return getClassId().hashCode(); }
		
		@Override
		public String toString() { return iClassId + "/{date=" + iDate + ", time=" + iTime + ", room=" + iRoom + "}"; }
	}
	
	public static class DomainItem implements IsSerializable {
		private String iId;
		private CellInterface iCell, iExtra;
		private Integer iValue;
		private boolean iAssigned = false, iSelected = false;
		
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
		public CellInterface getCell() { return iCell; }
		public void setCell(CellInterface cell) { iCell = cell; }
		public CellInterface getExtra() { return iExtra; }
		public void setExtra(CellInterface cell) { iExtra = cell; }
		public Integer getValue() { return iValue; }
		public void setValue(Integer value) { iValue = value; }
		public boolean isAssigned() { return iAssigned; }
		public void setAssigned(boolean assigned) { iAssigned = assigned; }
		public boolean isSelected() { return iSelected; }
		public void setSelected(boolean selected) { iSelected = selected; }
	}

}
