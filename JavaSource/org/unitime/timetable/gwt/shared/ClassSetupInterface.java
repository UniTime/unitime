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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class ClassSetupInterface implements IsSerializable, Serializable, GwtRpcResponse, GwtRpcRequest<ClassSetupInterface>{
	private static final long serialVersionUID = 5836074172101388912L;
	private List<ClassLine> iClassLines = new ArrayList<ClassLine>();
	private List<Reference> iDepartments;
	private List<Reference> iInstructionalMethods;
	private List<Reference> iLMSs;
	private List<Reference> iDatePatterns;
	private List<Reference> iSubparts;
	
	private Long iConfigId;
	private Integer iLimit;
	private Boolean iUnlimited;
	private Long iOfferingId;
	private Boolean iDisplayOptionForMaxLimit;
	private Boolean iDisplayMaxLimit;
	private Boolean iDisplayInstructors;
	private Boolean iDisplayEnabledForStudentScheduling;
	private Boolean iDisplayExternalId;
	private Boolean iEditExternalId;
	private Boolean iEditSnapshotLimits;
	private Long iInstructionalMethodId;
	private Boolean iInstructionalMethodEditable;
	private Boolean iDisplayLms;
	private String iName;
	private Boolean iDisplayEnrollments;
	private Boolean iEditUnlimited;
	private Boolean iDisplaySnapshotLimit;
	private Boolean iValidateLimits;
	private Operation iOperation;
	private long iLastGeneratedId = 0;
	
	public static enum Operation implements IsSerializable, Serializable {
		LOAD,
		SAVE,
	}
	
	public ClassSetupInterface() {}
	public ClassSetupInterface(Operation op, Long configId) {
		iOperation = op;
		iConfigId = configId;
	}
	
	public Long addClassLine(ClassLine classLine) {
		if (classLine.getClassId() == null)
			classLine.setClassId(--iLastGeneratedId);
		iClassLines.add(classLine);
		return classLine.getClassId();
	}
	public ClassLine copy(ClassLine classLine) {
		ClassLine copy = new ClassLine(classLine);
		copy.setClassId(--iLastGeneratedId);
		copy.setLabel(copy.getSubpartLabel() + " New" + iLastGeneratedId);
		Reference d = getDepartment(copy.getDepartmentId());
		if (d == null || !d.isSelectable()) copy.setDepartmentId(null);
		Reference l = getLMS(copy.getLMS());
		if (l == null || !l.isSelectable()) copy.setLMS(-1l);
		Reference p = getDatePattern(copy.getDatePatternId());
		if (p == null || !p.isSelectable()) copy.setDatePatternId(null);
		return copy;
	}
	public List<ClassLine> getClassLines() { return iClassLines; }
	
	public void addDepartment(Long id, String ref, String label, boolean selectable) {
		if (iDepartments == null) iDepartments = new ArrayList<Reference>();
		iDepartments.add(new Reference(id, ref, label, selectable));
	}
	public void addDepartment(Long id, String ref, String label) {
		addDepartment(id, ref, label, true);
	}
	public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
	public List<Reference> getDepartments() { return iDepartments; }
	public Reference getDepartment(Long id) {
		if (iDepartments == null || id == null) return null;
		for (Reference ref: iDepartments)
			if (ref.getId().equals(id)) return ref;
		return null;
	}

	public void addInstructionalMethod(Long id, String ref, String label) {
		if (iInstructionalMethods == null) iInstructionalMethods = new ArrayList<Reference>();
		iInstructionalMethods.add(new Reference(id, ref, label, true));
	}
	public boolean hasInstructionalMethods() { return iInstructionalMethods != null && !iInstructionalMethods.isEmpty(); }
	public List<Reference> getInstructionalMethods() { return iInstructionalMethods; }
	public Reference getInstructionalMethod(Long id) {
		if (iInstructionalMethods == null || id == null) return null;
		for (Reference ref: iInstructionalMethods)
			if (ref.getId().equals(id)) return ref;
		return null;
	}

	public void addLMS(Long id, String ref, String label) {
		if (iLMSs == null) iLMSs = new ArrayList<Reference>();
		iLMSs.add(new Reference(id, ref, label, true));
	}
	public boolean hasLMSs() { return iLMSs != null && !iLMSs.isEmpty(); }
	public List<Reference> getLMSs() { return iLMSs; }
	public Reference getLMS(Long id) {
		if (iLMSs == null || id == null) return null;
		for (Reference ref: iLMSs)
			if (ref.getId().equals(id)) return ref;
		return null;
	}
	
	public void addDatePattern(Long id, String ref, String label) {
		if (iDatePatterns == null) iDatePatterns = new ArrayList<Reference>();
		iDatePatterns.add(new Reference(id, ref, label, true));
	}
	public boolean hasDatePatterns() { return iDatePatterns != null && !iDatePatterns.isEmpty(); }
	public List<Reference> getDatePatterns() { return iDatePatterns; }
	public Reference getDatePattern(Long id) {
		if (iDatePatterns == null || id == null) return null;
		for (Reference ref: iDatePatterns)
			if (ref.getId().equals(id)) return ref;
		return null;
	}
	
	public void addSubpart(Long id, String ref, String label) {
		if (iSubparts == null) iSubparts = new ArrayList<Reference>();
		iSubparts.add(new Reference(id, ref, label, true));
	}
	public boolean hasSubparts() { return iSubparts != null && !iSubparts.isEmpty(); }
	public List<Reference> getSubparts() { return iSubparts; }
	public Reference getSubpart(Long id) {
		if (iSubparts == null || id == null) return null;
		for (Reference ref: iSubparts)
			if (ref.getId().equals(id)) return ref;
		return null;
	}
	public int getSubpartIndex(Long id) {
		if (iSubparts == null || id == null) return -1;
		for (int i = 0; i < iSubparts.size(); i++)
			if (iSubparts.get(i).getId().equals(id)) return i;
		return -1;
	}

	public Operation getOperation() { return iOperation; }
	public void setOperation(Operation operation) { iOperation = operation; }
	public void setConfigId(Long configId) { iConfigId = configId; }
	public Long getConfigId() { return iConfigId; }
	public void setLimit(Integer limit) { iLimit = limit; }
	public Integer getLimit() { return iLimit; }
	public void setUnlimited(Boolean unlimited) { iUnlimited = unlimited; }
	public Boolean isUnlimited() { return iUnlimited; }
	public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
	public Long getOfferingId() { return iOfferingId; }
	public void setName(String name) { iName = name; }
	public String getName() { return iName; }

	public void setDisplayOptionForMaxLimit(boolean displayOptionForMaxLimit) { iDisplayOptionForMaxLimit = displayOptionForMaxLimit; }
	public boolean isDisplayOptionForMaxLimit() { return Boolean.TRUE.equals(iDisplayOptionForMaxLimit); }
	public void setDisplayMaxLimit(boolean displayMaxLimit) { iDisplayMaxLimit = displayMaxLimit; }
	public boolean isDisplayMaxLimit() { return Boolean.TRUE.equals(iDisplayMaxLimit); }
	public void setDisplayInstructors(boolean displayInstructors) { iDisplayInstructors = displayInstructors; }
	public boolean isDisplayInstructors() { return Boolean.TRUE.equals(iDisplayInstructors); }
	public void setDisplayEnabledForStudentScheduling(boolean displayEnabledForStudentScheduling) { iDisplayEnabledForStudentScheduling = displayEnabledForStudentScheduling; }
	public boolean isDisplayEnabledForStudentScheduling() { return Boolean.TRUE.equals(iDisplayEnabledForStudentScheduling); }
	public void setDisplayExternalId(boolean displayExternalId) { iDisplayExternalId = displayExternalId; }
	public boolean isDisplayExternalId() { return Boolean.TRUE.equals(iDisplayExternalId); }
	public void setEditExternalId(boolean editExternalId) { iEditExternalId = editExternalId; }
	public boolean isEditExternalId() { return Boolean.TRUE.equals(iEditExternalId); }
	public void setEditSnapshotLimits(boolean editSnapshotLimits) { iEditSnapshotLimits = editSnapshotLimits; }
	public boolean isEditSnapshotLimits() { return Boolean.TRUE.equals(iEditSnapshotLimits); }
	public void setInstructionalMethodEditable(boolean instructionalMethodEditable) { iInstructionalMethodEditable = instructionalMethodEditable; }
	public boolean isInstructionalMethodEditable() { return Boolean.TRUE.equals(iInstructionalMethodEditable); }
	public void setDisplayLms(boolean displayLms) { iDisplayLms = displayLms; }
	public boolean isDisplayLms() { return iDisplayLms; }
	public void setDisplayEnrollments(boolean displayEnrollments) { iDisplayEnrollments = displayEnrollments; }
	public boolean isDisplayEnrollments() { return Boolean.TRUE.equals(iDisplayEnrollments); }
	public void setEditUnlimited(boolean editUnlimited) { iEditUnlimited = editUnlimited; }
	public boolean isEditUnlimited() { return Boolean.TRUE.equals(iEditUnlimited); }
	public void setDisplaySnapshotLimit(boolean displaySnapshotLimit) { iDisplaySnapshotLimit = displaySnapshotLimit; }
	public boolean isDisplaySnapshotLimit() { return Boolean.TRUE.equals(iDisplaySnapshotLimit); }
	public void setValidateLimits(boolean validateLimits) { iValidateLimits = validateLimits; }
	public boolean isValidateLimits() { return Boolean.TRUE.equals(iValidateLimits); }
	
	public void setInstructionalMethodId(Long instructionalMethodId) { iInstructionalMethodId = instructionalMethodId; }
	public Long getInstructionalMethodId() { return iInstructionalMethodId; }
	
	protected boolean canCancelChildren(ClassLine line) {
		if (line == null || !line.isEditable() || !line.isCanCancel()) return false;
		boolean cancel = !line.getCancelled();
		for (ClassLine child: iClassLines) {
			if (line.getClassId().equals(child.getParentId()) && cancel != child.getCancelled() && !canCancelChildren(child))
				return false;
		}
		return true;
	}
	
	public ClassLine getClassLine(Long id) {
		if (id == null) return null;
		for (ClassLine line: iClassLines)
			if (id.equals(line.getClassId()))
				return line;
		return null;
	}
	
	public List<ClassLine> getLines(ClassLine l) {
		if (l == null) return null;
		Set<Long> ids = new HashSet<>();
		List<ClassLine> ret = new ArrayList<>();
		for (ClassLine line: iClassLines) {
			if (l.getClassId().equals(line.getClassId())) {
				ids.add(line.getClassId());
				ret.add(line);
			} if (line.getParentId() != null && ids.contains(line.getParentId())) {
				ids.add(line.getClassId());
				ret.add(line);
			}
		}
		return ret;
	}
	
	public ClassLine getLastLine(ClassLine l) {
		if (l == null) return null;
		Set<Long> ids = new HashSet<>();
		ClassLine last = null;
		for (ClassLine line: iClassLines) {
			if (l.getClassId().equals(line.getClassId())) {
				ids.add(line.getClassId());
				last = line;
			} if (line.getParentId() != null && ids.contains(line.getParentId())) {
				ids.add(line.getClassId());
				last = line;
			}
		}
		return last;
	}
	
	public boolean canCancel(ClassLine line) {
		if (line == null || !line.isCanCancel()) return false;
		boolean cancel = !line.getCancelled();
		ClassLine parent = getClassLine(line.getParentId());
		if (parent != null && cancel && !parent.getCancelled() && countNotCancelledChildren(parent.getClassId(), line.getSubpartId()) <= 1)
			return false; // cannot cancel last child
		if (parent != null) {
			// cannot open a note of a cancelled parent
			if (!cancel && parent.getCancelled()) return false;
			// cannot cancel last child
			if (cancel && !parent.getCancelled() && countNotCancelledChildren(parent.getClassId(), line.getSubpartId()) == 1) return false;
		}
		for (ClassLine l: getLines(line))
			if (cancel != l.getCancelled() && !l.isCanCancel()) return false;
		return true;
	}
	
	public boolean canDelete(ClassLine line) {
		if (line == null || !line.isCanDelete()) return false;
		if (isLastOfSubpart(line) || isLastChild(line)) return false;
		return true;
	}
	
	public boolean canMoveAway(ClassLine line) {
		if (line == null || !line.isEditable()) return false;
		if (isLastChild(line)) return false;
		return true;
	}
	
	public Long getPreviousParentId(ClassLine line) {
		if (line == null || line.getParentId() == null) return null;
		ClassLine parent = getClassLine(line.getParentId());
		if (parent == null) return null;
		Long prevId = null;
		for (ClassLine l: iClassLines) {
			if (l.getClassId().equals(parent.getClassId())) return prevId;
			if (l.getSubpartId().equals(parent.getSubpartId()) && (line.getCancelled() || !l.getCancelled()))
				prevId = l.getClassId();
		}
		return prevId;
	}
	
	public Long getNextParentId(ClassLine line) {
		if (line == null || line.getParentId() == null) return null;
		ClassLine parent = getClassLine(line.getParentId());
		if (parent == null) return null;
		boolean match = false;
		for (ClassLine l: iClassLines) {
			if (l.getClassId().equals(parent.getClassId()))
				match = true;
			else if (match && l.getSubpartId().equals(parent.getSubpartId()) && (line.getCancelled() || !l.getCancelled()))
				return l.getClassId();
		}
		return null;
	}
	
	public List<ClassLine> getChildren(Long id) {
		if (id == null) return null;
		List<ClassLine> ret = new ArrayList<>();
		for (ClassLine line: iClassLines) {
			if (id.equals(line.getParentId()))
				ret.add(line);
		}
		return ret;
	}
	
	public boolean hasChildren(Long id) {
		if (id == null) return false;
		for (ClassLine line: iClassLines)
			if (id.equals(line.getParentId())) return true;
		return false;
	}
	
	public int countNotCancelledChildren(Long id, Long subpartId) {
		if (id == null) return 0;
		int count = 0;
		for (ClassLine line: iClassLines)
			if (id.equals(line.getParentId()) && subpartId.equals(line.getSubpartId()) && !line.getCancelled())
				count ++;
		return count;
	}
	
	public boolean isLastOfSubpart(ClassLine child) {
		if (child == null) return false;
		Long subpartId = child.getSubpartId();
		if (subpartId == null) return false;
		int opened = 0, count = 0;
		for (ClassLine line: iClassLines)
			if (subpartId.equals(line.getSubpartId())) {
				count ++;
				if (!line.getCancelled()) opened ++;
			}
		if (child.getCancelled())
			return count == 1;
		else
			return opened == 1;
	}
	
	public boolean isLastChild(ClassLine child) {
		if (child == null) return false;
		Long subpartId = child.getSubpartId();
		Long parentId = child.getParentId();
		if (parentId == null || subpartId == null) return false;
		int opened = 0, count = 0;
		for (ClassLine line: iClassLines)
			if (subpartId.equals(line.getSubpartId()) && parentId.equals(line.getParentId())) {
				count ++;
				if (!line.getCancelled()) opened ++;
			}
		if (child.getCancelled())
			return count == 1;
		else
			return opened == 1;
	}

	public static class ClassLine implements IsSerializable, Serializable {
		private static final long serialVersionUID = 8206025602292396756L;
		private Long iClassId;
		private Long iSubpartId;
		private Integer iIType;
		private boolean iEditable = true;
		private boolean iEditableDatePattern = true;
		private boolean iCanCancel = true;
		private boolean iCanDelete = true;
		private Integer iEnrollment;
		private Integer iSnapshotLimit;
		private Integer iMinClassLimit;
		private Integer iMaxClassLimit;
		private Integer iNumberOfRooms;
		private Boolean iDisplayInstructors;
		private Boolean iEnabledForStudentScheduling;
		private Float iRoomRatio;
		private Long iParentId;
		private Long iDepartmentId;
		private Long iDatePatternId;
		private Boolean iCancelled;
		private Long iLMS;
		private Boolean iSplitAttendance;
		private String iTime;
		private String iRoom;
		private String iInstructor;
		private String iExternalId;
		private Integer iIndent;
		private String iLabel;
		private String iSubpartLabel;
		private String iError;

		public ClassLine() {}
		
		public ClassLine(ClassLine line) {
			iSubpartId = line.getSubpartId();
			iIType = line.getIType();
			iEditable = true;
			iEditableDatePattern = true;
			iCanCancel = false;
			iCanDelete = true;
			iEnrollment = (line.getEnrollment() == null ? null : Integer.valueOf(0));
			iMinClassLimit = line.getMinClassLimit();
			iMaxClassLimit = line.getMaxClassLimit();
			iSnapshotLimit = null;
			iNumberOfRooms = line.getNumberOfRooms();
			iDisplayInstructors = line.getDisplayInstructors();
			iEnabledForStudentScheduling = line.getEnabledForStudentScheduling();
			iRoomRatio = line.getRoomRatio();
			iParentId = line.getParentId();
			iDepartmentId = line.getDepartmentId();
			iDatePatternId = line.getDatePatternId();
			iCancelled = false;
			iLMS = line.getLMS();
			iSplitAttendance = line.getSplitAttendance();
			iIndent = line.getIndent();
			iSubpartLabel = line.getSubpartLabel();
		}
		
		public boolean hasClassId() { return iClassId != null; }
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
		
		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		
		public Integer getIType() { return iIType; }
		public void setIType(Integer itype) { iIType = itype; }
		public boolean isEditable() { return iEditable; }
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditableDatePattern() { return iEditableDatePattern; }
		public void setEditableDatePattern(boolean editableDatePattern) { iEditableDatePattern = editableDatePattern; }
		public boolean isCanCancel() { return iCanCancel; }
		public void setCanCancel(boolean canCancel) { iCanCancel = canCancel; }
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		
		public Integer getEnrollment() { return iEnrollment; }
		public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }
		public Integer getSnapshotLimit() { return iSnapshotLimit; }
		public void setSnapshotLimit(Integer snapshotLimit) { iSnapshotLimit = snapshotLimit; }
		public Integer getMinClassLimit() { return iMinClassLimit; }
		public void setMinClassLimit(Integer minClassLimit) { iMinClassLimit = minClassLimit; }
		public Integer getMaxClassLimit() { return iMaxClassLimit; }
		public void setMaxClassLimit(Integer maxClassLimit) { iMaxClassLimit = maxClassLimit; }
		public Integer getNumberOfRooms() { return iNumberOfRooms; }
		public void setNumberOfRooms(Integer numberOfRooms) { iNumberOfRooms = numberOfRooms; }
		public Boolean getDisplayInstructors() { return iDisplayInstructors; }
		public void setDisplayInstructors(Boolean displayInstructors) { iDisplayInstructors = displayInstructors; }
		public Boolean getEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
		public void setEnabledForStudentScheduling(Boolean enabledForStudentScheduling) { iEnabledForStudentScheduling = enabledForStudentScheduling; }
		public Float getRoomRatio() { return iRoomRatio; }
		public void setRoomRatio(Float roomRatio) { iRoomRatio = roomRatio; }
		public Long getParentId() { return iParentId; }
		public void setParentId(Long parentId) { iParentId = parentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDatePatternId() { return iDatePatternId; }
		public void setDatePatternId(Long datePatternId) { iDatePatternId = datePatternId; }
		public Boolean getCancelled() { return iCancelled; }
		public void setCancelled(Boolean cancelled) { iCancelled = cancelled; }
		public Long getLMS() { return iLMS; }
		public void setLMS(Long lms) { iLMS = lms; }
		public Boolean getSplitAttendance() { return iSplitAttendance; }
		public void setSplitAttendance(Boolean splitAttendance) { iSplitAttendance = splitAttendance; }
		public String getTime() { return iTime; }
		public void setTime(String time) { iTime = time; }
		public String getRoom() { return iRoom; }
		public void setRoom(String room) { iRoom = room; }
		public String getInstructor() { return iInstructor; }
		public void setInstructor(String instructor) { iInstructor = instructor; }
		public String getExternalId() { return iExternalId; }
		public void setExternalId(String externalId) { iExternalId = externalId; }
		public Integer getIndent() { return iIndent; }
		public void setIndent(Integer indent) { iIndent = indent; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public String getSubpartLabel() { return iSubpartLabel; }
		public void setSubpartLabel(String label) { iSubpartLabel = label; }
		
		public void setError(String error) { iError = error; }
		public void addError(String error) {
			if (iError == null || iError.isEmpty()) iError = error;
			else iError += "\n" + error;
		}
		public boolean hasError() { return iError != null && !iError.isEmpty(); }
		public String getError() { return iError; }
		
		@Override
		public int hashCode() {
			return getClassId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ClassLine)) return false;
			return getClassId().equals(((ClassLine)o).getClassId());
		}
	}
	
	public static class Reference implements IsSerializable, Serializable {
		private static final long serialVersionUID = -7003516910478521868L;
		private Long iId;
		private String iReference;
		private String iLabel;
		private boolean iSelectable = true;
		
		public Reference() {}
		public Reference(Long id, String ref, String label, boolean selectable) {
			iId = id; iReference = ref; iLabel = label; iSelectable = selectable;
		}
		
		public Long getId() { return iId; }
		public String getReference() { return iReference; }
		public String getLabel() { return iLabel; }
		
		public boolean isSelectable() { return iSelectable; }
		public void setSelectable(boolean selectable) { iSelectable = selectable; }
		
		@Override
		public String toString() {
			return getReference() + " - " + getLabel();
		}
	}
	
	public static enum ClassSetupColumn {
		ERROR,
		CLASS_NAME,
		EXTERNAL_ID,
		BUTTONS,
		ENROLLMENT,
		LIMIT,
		CANCELLED,
		SNAPSHOT,
		ROOM_RATIO,
		NBR_ROOMS,
		SPLIT_ATTENDANCE,
		DEPARTMENT,
		DATE_PATTERN,
		LMS,
		DISPLAY_INSTRUCTOR,
		STUDENT_SCHEDULING,
		TIME,
		ROOM,
		INSTRUCTOR,
	}
}
