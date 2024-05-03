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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class InstrOfferingConfigInterface implements IsSerializable, Serializable, GwtRpcResponse, GwtRpcRequest<InstrOfferingConfigInterface>{
	private static final long serialVersionUID = 6991472992890559070L;
	private List<SubpartLine> iSubpartLines = new ArrayList<SubpartLine>();
	private List<Reference> iDepartments;
	private List<Reference> iInstructionalMethods;
	private List<Reference> iDurationTypes;
	private List<Reference> iInstructionalTypes;
	private List<Reference> iConfigs;

	private Long iOfferingId;
	private Long iCourseId;
	private Long iConfigId;
	private Operation iOperation;
	private Integer iLimit;
	private Boolean iUnlimited;
	private Boolean iDisplayOptionForMaxLimit;
	private Boolean iDisplayMaxLimit;
	private Boolean iCanDelete;
	private Long iInstructionalMethodId;
	private Boolean iInstructionalMethodEditable;
	private Long iDurationTypeId;
	private Boolean iDurationTypeEditable;
	private String iCourseName;
	private String iConfigName;
	private long iLastGeneratedId = 0;
	private Boolean iDisplayCourseLink;
	private Boolean iCheckLimits;
	private Integer iMaxNumberOfClasses;
	private String iOp;

	public static enum Operation implements IsSerializable, Serializable {
		LOAD,
		SAVE,
		DELETE,
	}
	
	public InstrOfferingConfigInterface() {}
	public InstrOfferingConfigInterface(Operation op, Long offeringId, Long configId) {
		iOperation = op;
		iOfferingId = offeringId;
		iConfigId = configId;
	}
	
	public Long addSubpartLine(SubpartLine subpartLine) {
		if (subpartLine.getSubpartId() == null)
			subpartLine.setSubpartId(--iLastGeneratedId);
		iSubpartLines.add(subpartLine);
		return subpartLine.getSubpartId();
	}

	public SubpartLine addSubpartLine(Reference instructionalType) {
		SubpartLine subpartLine = new SubpartLine();
		subpartLine.setSubpartId(--iLastGeneratedId);
		subpartLine.setLabel(instructionalType.getLabel());
		subpartLine.setIType(instructionalType.getId().intValue());
		subpartLine.setRoomRatio(1f);
		subpartLine.setNumberOfRooms(1);
		subpartLine.setSplitAttendance(false);
		subpartLine.setIndent(0);
		iSubpartLines.add(subpartLine);
		return subpartLine;
	}
	
	public SubpartLine copy(SubpartLine classLine) {
		SubpartLine copy = new SubpartLine(classLine);
		copy.setSubpartId(--iLastGeneratedId);
		copy.setLabel(copy.getLabel() + iLastGeneratedId);
		Reference d = getDepartment(copy.getDepartmentId());
		if (d == null || !d.isSelectable()) copy.setDepartmentId(null);
		return copy;
	}
	public List<SubpartLine> getSubpartLines() { return iSubpartLines; }

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
	public void setCourseId(Long courseId) { iCourseId = courseId; }
	public Long getCourseId() { return iCourseId; }
	public void setCourseName(String name) { iCourseName = name; }
	public String getCourseName() { return iCourseName; }
	public void setConfigName(String name) { iConfigName = name; }
	public String getConfigName() { return iConfigName; }
	public void setInstructionalMethodId(Long instructionalMethodId) { iInstructionalMethodId = instructionalMethodId; }
	public Long getInstructionalMethodId() { return iInstructionalMethodId; }
	public void setDurationTypeId(Long durationTypeId) { iDurationTypeId = durationTypeId; }
	public Long getDurationTypeId() { return iDurationTypeId; }

	public void setDisplayOptionForMaxLimit(boolean displayOptionForMaxLimit) { iDisplayOptionForMaxLimit = displayOptionForMaxLimit; }
	public boolean isDisplayOptionForMaxLimit() { return Boolean.TRUE.equals(iDisplayOptionForMaxLimit); }
	public void setDisplayMaxLimit(boolean displayMaxLimit) { iDisplayMaxLimit = displayMaxLimit; }
	public boolean isDisplayMaxLimit() { return Boolean.TRUE.equals(iDisplayMaxLimit); }
	public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	public boolean isCanDelete() { return Boolean.TRUE.equals(iCanDelete); }
	public void setInstructionalMethodEditable(boolean instructionalMethodEditable) { iInstructionalMethodEditable = instructionalMethodEditable; }
	public boolean isInstructionalMethodEditable() { return Boolean.TRUE.equals(iInstructionalMethodEditable); }
	public void setDurationTypeEditable(boolean durationTypeEditable) { iDurationTypeEditable = durationTypeEditable; }
	public boolean isDurationTypeEditable() { return Boolean.TRUE.equals(iDurationTypeEditable); }
	public void setDisplayCourseLink(boolean displayCourseLink) { iDisplayCourseLink = displayCourseLink; }
	public boolean isDisplayCourseLink() { return Boolean.TRUE.equals(iDisplayCourseLink); }
	public void setCheckLimits(boolean checkLimits) { iCheckLimits = checkLimits; }
	public boolean isCheckLimits() { return Boolean.TRUE.equals(iCheckLimits); }
	public void setMaxNumberOfClasses(Integer maxNumberOfClasses) { iMaxNumberOfClasses = maxNumberOfClasses; }
	public Integer getMaxNumberOfClasses() { return iMaxNumberOfClasses; }
	public void setOp(String op) { iOp = op; }
	public String getOp() { return iOp; }
	
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
	
	public void addDurationType(Long id, String ref, String label) {
		if (iDurationTypes == null) iDurationTypes = new ArrayList<Reference>();
		iDurationTypes.add(new Reference(id, ref, label, true));
	}
	public boolean hasDurationTypes() { return iDurationTypes != null && !iDurationTypes.isEmpty(); }
	public List<Reference> getDurationTypes() { return iDurationTypes; }
	public Reference getDurationType(Long id) {
		if (iDurationTypes == null || id == null) return null;
		for (Reference ref: iDurationTypes)
			if (ref.getId().equals(id)) return ref;
		return null;
	}
	
	
	public void addConfig(Long id, String label) {
		if (iConfigs == null) iConfigs = new ArrayList<Reference>();
		iConfigs.add(new Reference(id, label, label, true));
	}
	public boolean hasConfigs() { return iConfigs != null && !iConfigs.isEmpty(); }
	public List<Reference> getConfigs() { return iConfigs; }
	
	public List<SubpartLine> getParents(SubpartLine line) {
		ArrayList<SubpartLine> parents = new ArrayList<SubpartLine>();
		SubpartLine parent = getSubpartLine(line.getParentId());
		while (parent != null) {
			parents.add(parent);
			parent = getSubpartLine(parent.getParentId());
		}
		Collections.reverse(parents);
		return parents;
	}
	public List<SubpartLine> getParentsWithMe(SubpartLine line) {
		ArrayList<SubpartLine> parents = new ArrayList<SubpartLine>();
		SubpartLine parent = getSubpartLine(line.getParentId());
		while (parent != null) {
			parents.add(parent);
			parent = getSubpartLine(parent.getParentId());
		}
		Collections.reverse(parents);
		parents.add(line);
		return parents;
	}
	public List<SubpartLine> getSameParent(SubpartLine line) {
		ArrayList<SubpartLine> ret = new ArrayList<SubpartLine>();
		for (SubpartLine l: iSubpartLines) {
			if ((line.getParentId() == null && l.getParentId() == null) || (line.getParentId() != null && line.getParentId().equals(l.getParentId())))
				ret.add(l);
		}
		return ret;
	}
	
	public void addInstructionalType(Long id, String ref, String label, boolean basic) {
		if (iInstructionalTypes == null) iInstructionalTypes = new ArrayList<Reference>();
		iInstructionalTypes.add(new Reference(id, ref, label, basic));
	}
	public boolean hasInstructionalTypes() { return iInstructionalTypes != null && !iInstructionalTypes.isEmpty(); }
	public List<Reference> getInstructionalTypes() { return iInstructionalTypes; }
	public Reference getInstructionalType(Long id) {
		if (iInstructionalTypes == null || id == null) return null;
		for (Reference ref: iInstructionalTypes)
			if (ref.getId().equals(id)) return ref;
		return null;
	}
	
	public SubpartLine getSubpartLine(Long id) {
		if (id == null) return null;
		for (SubpartLine line: iSubpartLines)
			if (id.equals(line.getSubpartId()))
				return line;
		return null;
	}
	
	public List<SubpartLine> getLines(SubpartLine l) {
		if (l == null) return null;
		Set<Long> ids = new HashSet<>();
		List<SubpartLine> ret = new ArrayList<>();
		for (SubpartLine line: iSubpartLines) {
			if (l.getSubpartId().equals(line.getSubpartId())) {
				ids.add(line.getSubpartId());
				ret.add(line);
			} if (line.getParentId() != null && ids.contains(line.getParentId())) {
				ids.add(line.getSubpartId());
				ret.add(line);
			}
		}
		return ret;
	}

	public static class SubpartLine implements IsSerializable, Serializable {
		private static final long serialVersionUID = 7969397309023277169L;
		private Long iSubpartId;
		private Integer iIType;
		private boolean iEditable = true;
		private boolean iCanDelete = true;
		private boolean iLocked = false;
		private Integer iMinClassLimit;
		private Integer iMaxClassLimit;
		private Integer iNumberOfClasses;
		private Integer iNumberOfRooms;
		private Integer iMinutesPerWeek;
		private Float iRoomRatio;
		private Long iParentId;
		private Long iDepartmentId;
		private Boolean iSplitAttendance;
		private Integer iIndent;
		private String iLabel;
		private String iError;
		
		public SubpartLine() {
			iEditable = true;
			iCanDelete = true;
			iLocked = false;
		}
		
		public SubpartLine(SubpartLine line) {
			iSubpartId = line.getSubpartId();
			iIType = line.getIType();
			iEditable = true;
			iCanDelete = true;
			iLocked = false;
			iMinClassLimit = line.getMinClassLimit();
			iMaxClassLimit = line.getMaxClassLimit();
			iNumberOfRooms = line.getNumberOfRooms();
			iMinutesPerWeek = line.getMinutesPerWeek();
			iRoomRatio = line.getRoomRatio();
			iParentId = line.getParentId();
			iDepartmentId = line.getDepartmentId();
			iSplitAttendance = line.getSplitAttendance();
			iIndent = line.getIndent();
			iNumberOfClasses = line.getNumberOfClasses();
		}
		
		public boolean hasSubpartId() { return iSubpartId != null; }
		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }

		public Integer getIType() { return iIType; }
		public void setIType(Integer itype) { iIType = itype; }
		public boolean isEditable() { return iEditable; }
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		public boolean isLocked() { return iLocked; }
		public void setLocked(boolean locked) { iLocked = locked; }
		
		public Integer getMinClassLimit() { return iMinClassLimit; }
		public void setMinClassLimit(Integer minClassLimit) { iMinClassLimit = minClassLimit; }
		public Integer getMaxClassLimit() { return iMaxClassLimit; }
		public void setMaxClassLimit(Integer maxClassLimit) { iMaxClassLimit = maxClassLimit; }
		public Integer getNumberOfRooms() { return iNumberOfRooms; }
		public void setNumberOfRooms(Integer numberOfRooms) { iNumberOfRooms = numberOfRooms; }
		public Float getRoomRatio() { return iRoomRatio; }
		public void setRoomRatio(Float roomRatio) { iRoomRatio = roomRatio; }
		public Long getParentId() { return iParentId; }
		public void setParentId(Long parentId) { iParentId = parentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Boolean getSplitAttendance() { return iSplitAttendance; }
		public void setSplitAttendance(Boolean splitAttendance) { iSplitAttendance = splitAttendance; }
		public Integer getIndent() { return iIndent; }
		public void setIndent(Integer indent) { iIndent = indent; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public Integer getNumberOfClasses() { return iNumberOfClasses; }
		public void setNumberOfClasses(Integer numberOfClasses) { iNumberOfClasses = numberOfClasses; }
		public Integer getMinutesPerWeek() { return iMinutesPerWeek; }
		public void setMinutesPerWeek(Integer minutesPerWeek) { iMinutesPerWeek = minutesPerWeek; }
		
		public void setError(String error) { iError = error; }
		public void addError(String error) {
			if (iError == null || iError.isEmpty()) iError = error;
			else iError += "\n" + error;
		}
		public boolean hasError() { return iError != null && !iError.isEmpty(); }
		public String getError() { return iError; }
		
		@Override
		public int hashCode() {
			return getSubpartId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof SubpartLine)) return false;
			return getSubpartId().equals(((SubpartLine)o).getSubpartId());
		}
	}

	public static class Reference implements IsSerializable, Serializable {
		private static final long serialVersionUID = -6683008505387009447L;
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
	
	public static enum InstrOfferingConfigColumn {
		ERROR,
		LABEL,
		BUTTONS,
		LIMIT,
		NBR_CLASSES,
		MINS_PER_WK,
		NBR_ROOMS,
		SPLIT_ATTENDANCE,
		ROOM_RATIO,
		DEPARTMENT,
	}
}
