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
package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.HasPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionProperties;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ExamAssignmentInterface {
	public static enum Operation {
		INIT,
		UPDATE,
		ASSIGN,
		SUGGESTIONS,
	}
	
	public static enum RoomOrder {
		NAME_ASC,
		NAME_DESC,
		SIZE_ASC,
		SIZE_DESC,
	}

	public static class ExamAssignmentRequest implements GwtRpcRequest<ExamAssignmentResponse> {
		private Long iSelectedExamId;
		private Long iPreviousExamId;
		private Operation iOperation;
		private List<ChangeInterface> iChanges;
		private Boolean iRoomAllowConflicts = false;
		private RoomFilterRpcRequest iRoomFilter;
		private RoomOrder iRoomOrder;
		private String iSuggestionFilter;
		private int iSuggestionMax = 30;
		private int iSuggestionTimeOut = 5;
		private int iSuggestionDepth = 2;

		public Long getSelectedExamId() { return iSelectedExamId; }
		public void setSelectedExamId(Long examId) { iSelectedExamId = examId; }
		public Long getPreviousExamId() { return iPreviousExamId; }
		public void setPreviousExamId(Long examId) { iPreviousExamId = examId; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }

		public boolean hasChanges() { return iChanges != null && !iChanges.isEmpty(); }
		public List<ChangeInterface> getChanges() { return iChanges; }
		public boolean hasChange(Long examId) {
			if (iChanges == null) return false;
			for (ChangeInterface ch: iChanges)
				if (ch.getExamId().equals(examId)) return true;
			return false;
		}
		public ChangeInterface getChange(Long examId) {
			if (iChanges == null) iChanges = new ArrayList<ChangeInterface>();
			for (ChangeInterface ch: iChanges)
				if (ch.getExamId().equals(examId)) return ch;
			ChangeInterface ch = new ChangeInterface();
			ch.setExamId(examId);
			iChanges.add(ch);
			return ch;
		}
		public ChangeInterface removeChange(Long examId) {
			if (iChanges == null) return null;
			for (Iterator<ChangeInterface> i = iChanges.iterator(); i.hasNext();) {
				ChangeInterface ch = i.next();
				if (ch.getExamId().equals(examId)) {
					i.remove();
					return ch;
				}
			}
			return null;
		}
		public void addChange(Long examId, String period, String room) {
			if (iChanges == null) iChanges = new ArrayList<ChangeInterface>();
			ChangeInterface old = getChange(examId);
			if (old == null) {
				iChanges.add(new ChangeInterface(examId, period, room));
			} else {
				old.setPeriod(period); old.setRoom(room);
			}
		}
		public void clearChanges() {
			if (iChanges != null) iChanges.clear();
		}

	    public void setRoomAllowConflicts(boolean roomAllowConflicts) { iRoomAllowConflicts = roomAllowConflicts; }
	    public Boolean isRoomAllowConflicts() { return iRoomAllowConflicts; }
		public RoomFilterRpcRequest getRoomFilter() { return iRoomFilter; }
		public void setRoomFilter(RoomFilterRpcRequest filter) { iRoomFilter = filter; }
		public void setRoomOrder(RoomOrder ord) { iRoomOrder = ord; }
		public RoomOrder getRoomOrder() { return (iRoomOrder == null ? RoomOrder.NAME_ASC : iRoomOrder); }
		
		public String getSuggestionFilter() { return iSuggestionFilter; }
		public void setSuggestionFilter(String filter) { iSuggestionFilter = filter; }
		public int getSuggestionMax() { return iSuggestionMax; }
		public void setSuggestionMax(int limit) { iSuggestionMax = limit; }
		public int getSuggestionTimeOut() { return iSuggestionTimeOut; }
		public void setSuggestionTimeOut(int limit) { iSuggestionTimeOut = limit; }
		public int getSuggestionDepth() { return iSuggestionDepth; }
		public void setSuggestionDepth(int depth) { iSuggestionDepth = depth; }
	}
	
	public static class ExamAssignmentResponse implements GwtRpcResponse, HasPageMessages {
		private Long iSessionId;
	    private Long iSelectedExamId;
	    private String iExamName, iExamType;
	    private boolean iCanAssign = false, iCanShowSuggestions = false;
		private TableInterface iProperties;
		private TableInterface iAssignments;
		private TableInterface iDistributionConflicts, iStudentConflicts, iInstructorConflicts;
		private TableInterface iPeriods;
		private TableInterface iSuggestions;
		private List<DomainItem> iRooms;
		private String iUrl;
		private String iErrorMessage;
		private Integer iMinRoomCapacity, iMaxRooms;
		private String iPeriodsErrorMessage, iRoomsErrorMessage;
		private String iAssignConfirmation;
		private String iSuggestionsMessage;
		private Boolean iSuggestionsTimeoutReached;
		private SuggestionProperties iSuggestionProperties;
		private List<PageMessage> iPageMessages = null;
		
		
		public Long getSelectedExamId() { return iSelectedExamId; }
		public void setSelectedExamId(Long examId) { iSelectedExamId = examId; }
		
		public String getExamName() { return iExamName; }
		public void setExamName(String examName) { iExamName = examName; }
		public String getExamType() { return iExamType; }
		public void setExamType(String examType) { iExamType = examType; }
	    
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

		public boolean hasDistributionConflicts() { return iDistributionConflicts != null; }
		public TableInterface getDistributionConflicts() { return iDistributionConflicts; }
		public void setDistributionConflicts(TableInterface distrubutionConflicts) { iDistributionConflicts = distrubutionConflicts; }

		public boolean hasStudentConflicts() { return iStudentConflicts != null; }
		public TableInterface getStudentConflicts() { return iStudentConflicts; }
		public void setStudentConflicts(TableInterface studentConflicts) { iStudentConflicts = studentConflicts; }

		public boolean hasInstructorConflicts() { return iInstructorConflicts != null; }
		public TableInterface getInstructorConflicts() { return iInstructorConflicts; }
		public void setInstructorConflicts(TableInterface instructorConflicts) { iInstructorConflicts = instructorConflicts; }
		
		public boolean hasPeriods() { return iPeriods != null && iPeriods.hasLines(); }
		public TableInterface getPeriods() { return iPeriods; }
		public void setPeriods(TableInterface periods) { iPeriods = periods; }

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
		
		public boolean hasPeriodsErrorMessage() { return iPeriodsErrorMessage != null && !iPeriodsErrorMessage.isEmpty(); }
		public void setPeriodsErrorMessage(String message) { iPeriodsErrorMessage = message; }
		public void addPeriodsErrorMessage(String message) {
			if (iPeriodsErrorMessage == null || iPeriodsErrorMessage.isEmpty())
				iPeriodsErrorMessage = message;
			else
				iPeriodsErrorMessage += "\n" + message;
		}
		public String getPeriodsErrorMessage() { return iPeriodsErrorMessage; }
		
		public boolean hasRoomsErrorMessage() { return iRoomsErrorMessage != null && !iRoomsErrorMessage.isEmpty(); }
		public void setRoomsErrorMessage(String message) { iRoomsErrorMessage = message; }
		public void addRoomsErrorMessage(String message) {
			if (iRoomsErrorMessage == null || iRoomsErrorMessage.isEmpty())
				iRoomsErrorMessage = message;
			else
				iRoomsErrorMessage += "\n" + message;
		}
		public String getRoomsErrorMessage() { return iRoomsErrorMessage; }
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Integer getMinRoomCapacity() { return iMinRoomCapacity; }
		public void setMinRoomCapacity(Integer cap) { iMinRoomCapacity = cap; }
		public Integer getMaxRooms() { return iMaxRooms; }
		public void setMaxRooms(Integer maxRooms) { iMaxRooms = maxRooms; }
		public boolean isCanAssign() { return iCanAssign; }
		public void setCanAssign(boolean canAssign) { iCanAssign = canAssign; }
		
		public boolean hasAssignConfirmation() { return iAssignConfirmation != null && !iAssignConfirmation.isEmpty(); }
		public String getAssignConfirmation() { return iAssignConfirmation; }
		public void setAssignConfirmation(String assignConfirmation) { iAssignConfirmation = assignConfirmation; }
		
		public boolean isCanShowSuggestions() { return iCanShowSuggestions; }
		public void setCanShowSuggestions(boolean canShowSuggestions) { iCanShowSuggestions = canShowSuggestions; }
		
		public boolean hasSuggestions() { return iSuggestions != null && iSuggestions.hasLines(); }
		public TableInterface getSuggestions() { return iSuggestions; }
		public void setSuggestions(TableInterface suggestions) { iSuggestions = suggestions; }
		public boolean hasSuggestionsMessage() { return iSuggestionsMessage != null && !iSuggestionsMessage.isEmpty(); }
		public void setSuggestionsMessage(String message) { iSuggestionsMessage = message; }
		public String getSuggestionsMessage() { return iSuggestionsMessage; }
		public boolean isSuggestionsTimeoutReached() { return iSuggestionsTimeoutReached != null && iSuggestionsTimeoutReached.booleanValue(); }
		public void setSuggestionsTimeoutReached(boolean timeoutReached) { iSuggestionsTimeoutReached = timeoutReached; }
		
		public SuggestionProperties getSuggestionProperties() { return iSuggestionProperties; }
		public void setSuggestionProperties(SuggestionProperties properties) { iSuggestionProperties = properties; }
		public boolean hasPageMessages() { return iPageMessages != null && !iPageMessages.isEmpty(); }
		public List<PageMessage> getPageMessages() { return iPageMessages; }
		public void addPageMessage(PageMessage message) {
			if (iPageMessages == null) iPageMessages = new ArrayList<PageMessage>();
			iPageMessages.add(message);
		}
	}
	
	public static class ChangeInterface implements IsSerializable {
		private Long iExamId;
		private String iPeriod;
		private String iRoom;
		
		public ChangeInterface() {}
		public ChangeInterface(Long examId, String period, String room) {
			iExamId = examId;
			iPeriod = period;
			iRoom = room;
		}
		
		public Long getExamId() { return iExamId; }
		public void setExamId(Long examId) { iExamId = examId; }
		
		public String getPeriod() { return iPeriod; }
		public void setPeriod(String period) { iPeriod = period; }
		public boolean hasPeriod() { return iPeriod != null && !iPeriod.isEmpty(); }
		
		public String getRoom() { return iRoom; }
		public boolean hasRoom() { return iRoom != null && !iRoom.isEmpty(); }
		public void setRoom(String room) { iRoom = room; }
		
		@Override
		public int hashCode() { return getExamId().hashCode(); }
		
		@Override
		public String toString() { return iExamId + "/{period=" + iPeriod + ", room=" + iRoom + "}"; }
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
