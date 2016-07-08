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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class InstructorInterface implements IsSerializable, Comparable<InstructorInterface>, Serializable {
	private static final long serialVersionUID = 1L;
	private Long iId;
	private String iExternalId;
	private String iFirstName, iMiddleName, iLastName, iFormattedName, iOrderName;
	private DepartmentInterface iDepartment = null;
	private PositionInterface iPosition = null;
	private PreferenceInterface iTeachingPreference = null;
	private Float iMaxLoad = null;
	private List<AttributeInterface> iAttributes;
	
	public InstructorInterface() {}
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	
    public String getExternalId() { return iExternalId; }
    public void setExternalId(String externalId) { iExternalId = externalId; }
    
    public String getFirstName() { return iFirstName; }
    public void setFirstName(String fname) { iFirstName = fname; }
    
    public String getMiddleName() { return iMiddleName; }
    public void setMiddleName(String mname) { iMiddleName = mname; }
    
    public String getLastName() { return iLastName; }
    public void setLastName(String lname) { iLastName = lname; }
    
    public String getFormattedName() { return iFormattedName; }
    public void setFormattedName(String name) { iFormattedName = name; }    
	
    public String getOrderName() { return (iOrderName != null ? iOrderName : iFormattedName); }
    public void setOrderName(String name) { iOrderName = name; }    

    public DepartmentInterface getDepartment() { return iDepartment; }
	public void setDepartment(DepartmentInterface department) { iDepartment = department; }
	
	public PositionInterface getPosition() { return iPosition; }
	public void setPosition(PositionInterface position) { iPosition = position; }
	
	public boolean hasTeachingPreference() { return iTeachingPreference != null; }
	public PreferenceInterface getTeachingPreference() { return iTeachingPreference; }
	public void setTeachingPreference(PreferenceInterface teachingPreference) { iTeachingPreference = teachingPreference; }
	
	public boolean hasMaxLoad() { return iTeachingPreference != null && !"P".equals(iTeachingPreference.getCode()) && iMaxLoad != null && iMaxLoad > 0f; }
	public Float getMaxLoad() { return iMaxLoad; }
	public void setMaxLoad(Float maxLoad) { iMaxLoad = maxLoad; }
	
	public boolean hasAttributes() { return iAttributes != null && !iAttributes.isEmpty(); }
	public List<AttributeInterface> getAttributes() { return iAttributes; }
	public List<AttributeInterface> getAttributes(AttributeTypeInterface type) {
		List<AttributeInterface> ret = new ArrayList<AttributeInterface>();
		if (hasAttributes())
			for (AttributeInterface a: getAttributes())
				if (type == null || type.equals(a.getType())) ret.add(a);
		return ret;
	}
	public void addAttribute(AttributeInterface attribute) {
		if (iAttributes == null) iAttributes = new ArrayList<AttributeInterface>();
		iAttributes.add(attribute);
	}
	public boolean hasAttribute(Long attributeId) {
		if (iAttributes == null || attributeId == null) return false;
		for (AttributeInterface a: iAttributes)
			if (a.getId().equals(attributeId)) return true;
		return false;
	}
	
	@Override
	public int compareTo(InstructorInterface instructor) {
		int cmp = getOrderName().compareToIgnoreCase(instructor.getOrderName());
		if (cmp != 0) return cmp;
		return (getId() == null ? new Long(-1l) : getId()).compareTo(instructor.getId() == null ? -1l : instructor.getId());
	}

	@Override
	public int hashCode() { return getId().hashCode(); }
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof InstructorInterface)) return false;
		return getId().equals(((InstructorInterface)object).getId());
	}
	
	@Override
	public String toString() {
		return (getFormattedName() == null ? getLastName() + ", " + getFirstName() : getFormattedName()) + (getExternalId() == null ? "" : " (" + getExternalId() + ")");
	}

	public static class AttributeTypeInterface implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iAbbv;
		private String iLabel;
		private boolean iConjunctive = false;
		private boolean iRequired = false;
		
		public AttributeTypeInterface() {}
		
		public AttributeTypeInterface(Long id, String abbv, String label, boolean conjunctive, boolean required) {
			iId = id; iAbbv = abbv; iLabel = label; iConjunctive = conjunctive; iRequired = required;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }

		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public boolean isConjunctive() { return iConjunctive; }
		public void setConjunctive(boolean conjunctive) { iConjunctive = conjunctive; }
		
		public boolean isRequired() { return iRequired; }
		public void setRequired(boolean required) { iRequired = required; }

		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof AttributeTypeInterface)) return false;
			return getId().equals(((AttributeTypeInterface)object).getId());
		}
		
		@Override
		public String toString() {
			return getLabel();
		}
	}
	
	public static class PositionInterface implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iAbbv;
		private String iLabel;
		private Integer iSortOrder;
		
		public PositionInterface() {}
		
		public PositionInterface(Long id, String abbv, String label) {
			iId = id; iAbbv = abbv; iLabel = label;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }

		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public Integer getSortOrder() { return iSortOrder; }
		public void setSortOrder(Integer sortOrder) { iSortOrder = sortOrder; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof PositionInterface)) return false;
			return getId().equals(((PositionInterface)object).getId());
		}
		
		@Override
		public String toString() {
			return getLabel();
		}
	}
	
	public static class DepartmentInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iAbbv;
		private String iCode;
		private String iLabel;
		private String iTitle;
		private boolean iCanAddAttribute, iCanSeeAttributes;
		
		public DepartmentInterface() {
			super();
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }

		public String getDeptCode() { return iCode; }
		public void setDeptCode(String code) { iCode = code; }
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		
		public String getAbbreviationOrCode() { return getAbbreviation() == null || getAbbreviation().isEmpty() ? getDeptCode() : getAbbreviation(); }
		
		public boolean isCanSeeAttributes() { return iCanSeeAttributes; }
		public void setCanSeeAttributes(boolean canSeeAttributes) { iCanSeeAttributes = canSeeAttributes; }

		public boolean isCanAddAttribute() { return iCanAddAttribute; }
		public void setCanAddAttribute(boolean canAddAttribute) { iCanAddAttribute = canAddAttribute; }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof DepartmentInterface)) return false;
			return getId().equals(((DepartmentInterface)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
		
		@Override
		public String toString() {
			return getDeptCode() + " - " + getLabel();
		}
	}
	
	public static class PreferenceInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iCode, iName, iAbbv;
		private String iColor;
		private Long iId;
		private boolean iEditable;
		
		public PreferenceInterface() {}
		public PreferenceInterface(Long id, String color, String code, String name, String abbv, boolean editable) {
			iId = id; iColor = color; iCode = code; iName = name; iAbbv = abbv; iEditable = editable;
		}
		
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public String getAbbv() { return iAbbv; }
		public void setAbbv(String abbv) { iAbbv = abbv; }

		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean isEditable() { return iEditable; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof PreferenceInterface)) return false;
			return getId().equals(((PreferenceInterface)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
		
		@Override
		public String toString() {
			return getName();
		}
	}
	
	public static class AttributeInterface implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId, iParentId;
		private String iCode;
		private String iName;
		private String iParentName;
		private DepartmentInterface iDepartment = null;
		private AttributeTypeInterface iType;
		private TreeSet<InstructorInterface> iInstructors = null;
		private Boolean iCanEdit = null, iCanDelete = null, iCanAssign = null, iCanChangeType = null;
		
		public AttributeInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public Long getParentId() { return iParentId; }
		public void setParentId(Long id) { iParentId = id; }

		public String getParentName() { return iParentName; }
		public void setParentName(String name) { iParentName = name; }

		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }

		
		public AttributeInterface(AttributeInterface attribute) {
			iId = attribute.iId; iCode = attribute.iCode; iName = attribute.iName;
			iParentId = attribute.iParentId; iParentName = attribute.iParentName;
			iDepartment = attribute.iDepartment;
			iInstructors = (attribute.iInstructors == null ? null : new TreeSet<InstructorInterface>(attribute.iInstructors));
			iCanEdit = attribute.iCanEdit;
			iCanDelete = attribute.iCanDelete;
			iCanAssign = attribute.iCanAssign;
			iCanChangeType = attribute.iCanChangeType;
		}
		
		public boolean isDepartmental() { return iDepartment != null; }
		public DepartmentInterface getDepartment() { return iDepartment; }
		public void setDepartment(DepartmentInterface department) { iDepartment = department; }
		
		public boolean hasType() { return iType != null; }
		public AttributeTypeInterface getType() { return iType; }
		public void setType(AttributeTypeInterface type) { iType = type; }
		
		public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }
		public void addInstructor(InstructorInterface instructor) {
			if (iInstructors == null) iInstructors = new TreeSet<InstructorInterface>();
			iInstructors.add(instructor);
		}
		public TreeSet<InstructorInterface> getInstructors() { return iInstructors; }
		public InstructorInterface getInstructor(Long id) {
			if (iInstructors == null || id == null) return null;
			for (InstructorInterface instructor: iInstructors)
				if (id.equals(instructor.getId())) return instructor;
			return null;
		}
		public boolean hasInstructor(Long id) {
			if (iInstructors == null || id == null) return false;
			for (InstructorInterface instructor: iInstructors)
				if (id.equals(instructor.getId())) return true;
			return false;
		}
		
		public boolean canEdit() { return iCanEdit != null && iCanEdit; }
		public void setCanEdit(Boolean canEdit) { iCanEdit = canEdit; }
		
		public boolean canDelete() { return iCanDelete != null && iCanDelete; }
		public void setCanDelete(Boolean canDelete) { iCanDelete = canDelete; }

		public boolean canAssign() { return iCanAssign != null && iCanAssign; }
		public void setCanAssign(Boolean canAssign) { iCanAssign = canAssign; }

		public boolean canChangeType() { return iCanChangeType != null && iCanChangeType; }
		public void setCanChangeType(Boolean canChangeType) { iCanChangeType = canChangeType; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AttributeInterface)) return false;
			return getId().equals(((AttributeInterface)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
		
		@Override
		public String toString() {
			return getName() + (getType() == null ? "" : " (" + getType() + ")");
		}
	}
	
	public static enum InstructorsColumn {
		SELECTION,
		ID,
		NAME,
		POSITION,
		TEACHING_PREF,
		MAX_LOAD,
		ATTRIBUTES,
		;
	}
	
	public static enum AttributesColumn {
		CODE,
		NAME,
		TYPE,
		PARENT,
		INSTRUCTORS,
		;
	}
	
	public static class InstructorAttributePropertiesInterface implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private List<DepartmentInterface> iDepartments = new ArrayList<DepartmentInterface>();
		private List<AttributeTypeInterface> iAttributeTypes = new ArrayList<AttributeTypeInterface>();
		private boolean iCanAddGlobalAttribute = false;
		private Long iLastDepartmentId = null;
		
		public InstructorAttributePropertiesInterface() {}
		
		public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
		public void addDepartment(DepartmentInterface department) { iDepartments.add(department); }
		public List<DepartmentInterface> getDepartments() { return iDepartments; }
		public DepartmentInterface getDepartment(Long departmentId) {
			for (DepartmentInterface department: iDepartments)
				if (department.getId().equals(departmentId)) return department;
			return null;
		}

		public boolean hasAttributeTypes() { return iAttributeTypes != null && !iAttributeTypes.isEmpty(); }
		public void addAttributeType(AttributeTypeInterface type) { iAttributeTypes.add(type); }
		public List<AttributeTypeInterface> getAttributeTypes() { return iAttributeTypes; }
		public AttributeTypeInterface getAttributeType(Long typeId) {
			for (AttributeTypeInterface type: iAttributeTypes)
				if (type.getId().equals(typeId)) return type;
			return null;
		}

		public boolean isCanAddGlobalAttribute() { return iCanAddGlobalAttribute; }
		public void setCanAddGlobalAttribute(boolean canAddGlobalAttribute) { iCanAddGlobalAttribute = canAddGlobalAttribute; }
		
		public Long getLastDepartmentId() { return iLastDepartmentId; }
		public void setLastDepartmentId(Long departmentId) { iLastDepartmentId = departmentId; }
	}
	
	public static class InstructorAttributePropertiesRequest implements GwtRpcRequest<InstructorAttributePropertiesInterface>, Serializable {
		private static final long serialVersionUID = 1L;
		public InstructorAttributePropertiesRequest() {}
	}
	
	public static class GetInstructorAttributesRequest implements GwtRpcRequest<GwtRpcResponseList<AttributeInterface>>, Serializable {
		private static final long serialVersionUID = 1L;
		public Long iDepartmentId;
		
		public GetInstructorAttributesRequest() {}
		
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		
		@Override
		public String toString() { return iDepartmentId == null ? "" : iDepartmentId.toString(); }
	}
	
	public static class GetInstructorsRequest implements GwtRpcRequest<GwtRpcResponseList<InstructorInterface>>, Serializable {
		private static final long serialVersionUID = 1L;
		public Long iDepartmentId;
		
		public GetInstructorsRequest() {}
		
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		
		@Override
		public String toString() { return iDepartmentId == null ? "" : iDepartmentId.toString(); }
	}

	public static class UpdateInstructorAttributeRequest implements GwtRpcRequest<AttributeInterface>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iDeleteAttributeId = null;
		private AttributeInterface iAttribute = null;
		private List<Long> iAddInstructors = new ArrayList<Long>();
		private List<Long> iDropInstructors = new ArrayList<Long>();
		
		public UpdateInstructorAttributeRequest() {}
		
		public void setDeleteAttributeId(Long attributeId) { iDeleteAttributeId = attributeId; }
		public boolean hasDeleteAttributeId() { return iDeleteAttributeId != null; }
		public Long getDeleteAttributeId() { return iDeleteAttributeId; }
		
		public boolean hasAttribute() { return iAttribute != null; }
		public void setAttribute(AttributeInterface attribute) { iAttribute = attribute; }
		public AttributeInterface getAttribute() { return iAttribute; }
		
		public void addInstructor(Long instructorId) { iAddInstructors.add(instructorId); }
		public List<Long> getAddInstructors() { return iAddInstructors; }
		public boolean hasAddInstructors() { return !iAddInstructors.isEmpty(); }
		
		public void dropInstructor(Long instructorId) { iDropInstructors.add(instructorId); }
		public List<Long> getDropInstructors() { return iDropInstructors; }
		public boolean hasDropInstructors() { return !iDropInstructors.isEmpty(); }
		
		@Override
		public String toString() {
			if (hasAttribute())
				return getAttribute().getName() + (getAttribute().getType() == null ? "" : " (" + getAttribute().getType().getAbbreviation() + ")") +
						(hasAddInstructors() ? " ADD" + getAddInstructors() : "") +
						(hasDropInstructors() ? " DROP" + getDropInstructors() : "");
			else
				return "DELETE " + getDeleteAttributeId();
		}		
	}
	
	public static class GetInstructorAttributeParentsRequest implements GwtRpcRequest<GwtRpcResponseList<AttributeInterface>>, Serializable {
		private static final long serialVersionUID = 1L;
		public Long iDepartmentId, iTypeId, iAttributeId;
		
		public GetInstructorAttributeParentsRequest() {}
		
		public GetInstructorAttributeParentsRequest(Long departmentId, Long typeId, Long attributeId) {
			iDepartmentId = departmentId;
			iTypeId = typeId;
			iAttributeId = attributeId;
		}
		
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		
		public void setTypeId(Long typeId) { iTypeId = typeId; }
		public Long getTypeId() { return iTypeId; }

		public void setAttributeId(Long attributeId) { iAttributeId = attributeId; }
		public Long getAttributeId() { return iAttributeId; }

		@Override
		public String toString() {
			return (iDepartmentId == null ? "" : iDepartmentId.toString()) + "," +
					(iTypeId == null ? "" : iTypeId.toString()) + "," +
					(iAttributeId == null ? "" : iAttributeId.toString());
		}
	}
	
	public static class SetLastDepartmentRequest implements GwtRpcRequest<GwtRpcResponseNull>, Serializable {
		private static final long serialVersionUID = 1L;
		public Long iDepartmentId;
		
		public SetLastDepartmentRequest() {}
		public SetLastDepartmentRequest(Long departmentId) {
			setDepartmentId(departmentId);
		}
		
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		
		@Override
		public String toString() { return iDepartmentId == null ? "" : iDepartmentId.toString(); }
	}
	
	public static class CourseInfo implements Comparable<CourseInfo>, IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iCourseId;
		private String iCourseName;
		
		public CourseInfo() {}

		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public Long getCourseId() { return iCourseId; }
		
		public void setCourseName(String courseName) { iCourseName = courseName; }
		public String getCourseName() { return iCourseName; }
		
		public int hashCode() { return getCourseId().hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CourseInfo)) return false;
			return getCourseId().equals(((CourseInfo)o).getCourseId());
		}
		public int compareTo(CourseInfo course) {
			return getCourseName().compareTo(course.getCourseName());
		}
		
		@Override
		public String toString() {
			return getCourseName();
		}
	}
	
	public static class SectionInfo implements Comparable<SectionInfo>, IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSectionId;
		private String iSectionName;
		private String iExternalId;
		private String iType;
		private boolean iCommon;
		private String iTime;
		private String iDate;
		private String iRoom;
		
		public SectionInfo() {}
		
		public void setSectionId(Long sectionId) { iSectionId = sectionId; }
		public Long getSectionId() { return iSectionId; }
		
		public void setSectionName(String sectionName) { iSectionName = sectionName; }
		public String getSectionName() { return iSectionName; }
		
		public void setExternalId(String externalId) { iExternalId = externalId; }
		public String getExternalId() { return iExternalId; }
		
		public void setSectionType(String type) { iType = type; }
		public String getSectionType() { return iType; }
		
		public void setCommon(boolean common) { iCommon = common; }
		public boolean isCommon() { return iCommon; }
		
		public void setTime(String time) { iTime = time; }
		public String getTime() { return iTime; }
		
		public void setDate(String date) { iDate = date; }
		public String getDate() { return iDate; }

		public void setRoom(String room) { iRoom = room; }
		public String getRoom() { return iRoom; }
		
		public int hashCode() { return getSectionId().hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof SectionInfo)) return false;
			return getSectionId().equals(((SectionInfo)o).getSectionId());
		}
		public int compareTo(SectionInfo section) {
			int cmp = (getExternalId() == null ? "" : getExternalId()).compareTo(section.getExternalId() == null ? "" : section.getExternalId());
			if (cmp != 0) return cmp;
			return getSectionName().compareTo(section.getSectionName());
		}
		
		@Override
		public String toString() {
			return (getExternalId() == null ? getSectionName() : getSectionType() + " " + getExternalId());
		}
	}
	
	public static class PreferenceInfo implements Comparable<PreferenceInfo>, IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iOwnerId;
		private String iName;
		private String iPreference;
		private String iComparable;
		
		public PreferenceInfo() {}
		public PreferenceInfo(Long id, String name, String pref) {
			iOwnerId = id; iName = name; iPreference = pref;
		}
		
		public void setOwnerId(Long ownerId) { iOwnerId = ownerId; }
	    public Long getOwnerId() { return iOwnerId; }

	    public void setOwnerName(String name) { iName = name; }
	    public String getOwnerName() { return iName; }
	    
	    public void setPreference(String preference) { iPreference = preference; }
	    public String getPreference() { return iPreference; }
	    
	    public void setComparable(String comparable) { iComparable = comparable; }
	    public String getComparable() { return iComparable == null ? iName : iComparable; }
	    
	    public int hashCode() { return getOwnerId().hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof PreferenceInfo)) return false;
			return getOwnerId().equals(((PreferenceInfo)o).getOwnerId());
		}
		public int compareTo(PreferenceInfo pref) {
			int cmp = getComparable().compareTo(pref.getComparable());
			if (cmp != 0) return cmp;
			return getPreference().compareTo(pref.getPreference());
		}
		
		@Override
		public String toString() {
			return getPreference() + " " + getOwnerName();
		}
	}
	
	public static class InstructorInfo implements Comparable<InstructorInfo>, GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
	    private Long iInstructorId;
	    private String iExternalId;
	    private String iName;
	    private float iAssignedLoad, iMaxLoad;
	    private String iTeachingPreference;
	    private List<PreferenceInfo> iTimePreferences = new ArrayList<PreferenceInfo>();
	    private List<PreferenceInfo> iCoursePreferences = new ArrayList<PreferenceInfo>();
	    private List<PreferenceInfo> iDistributionPreferences = new ArrayList<PreferenceInfo>();
	    private List<AttributeInterface> iAttributes = new ArrayList<AttributeInterface>();
	    private Map<String,Double> iValues = new HashMap<String, Double>();
	    private String iAvailability;
	    private List<TeachingRequestInfo> iAssignedRequests = new ArrayList<TeachingRequestInfo>();
	    private List<ClassInfo> iEnrollments = new ArrayList<ClassInfo>();
	    private int iAssignmentIndex = -1;

		public InstructorInfo() {}
		
		public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
	    public Long getInstructorId() { return iInstructorId; }
	    
	    public void setExternalId(String externalId) { iExternalId = externalId; }
	    public String getExternalId() { return iExternalId; }
	    public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
	    
	    public void setInstructorName(String name) { iName = name; }
	    public String getInstructorName() { return iName; }
	    
	    public void setAssignedLoad(float assignedLoad) { iAssignedLoad = assignedLoad; }
	    public float getAssignedLoad() { return iAssignedLoad; }
	    
	    public void setMaxLoad(float maxLoad) { iMaxLoad = maxLoad; }
	    public float getMaxLoad() { return iMaxLoad; }
	    
	    public void setTeachingPreference(String teachinPreference) { iTeachingPreference = teachinPreference; }
	    public String getTeachingPreference() { return iTeachingPreference; }
	    
	    public void addTimePreference(PreferenceInfo preference) { iTimePreferences.add(preference); }
	    public List<PreferenceInfo> getTimePreferences() { return iTimePreferences; }
	    
	    public void addCoursePreference(PreferenceInfo preference) { iCoursePreferences.add(preference); }
	    public List<PreferenceInfo> getCoursePreferences() { return iCoursePreferences; }

	    public void addDistributionPreference(PreferenceInfo preference) { iDistributionPreferences.add(preference); }
	    public List<PreferenceInfo> getDistributionPreferences() { return iDistributionPreferences; }
	    
	    public boolean hasAssignmentIndex() { return iAssignmentIndex >= 0; }
	    public int getAssignmentIndex() { return iAssignmentIndex; }
	    public void setAssignmentIndex(int index) { iAssignmentIndex = index; }
	    
	    public void addAttribute(AttributeInterface attribute) { iAttributes.add(attribute); }
	    public List<AttributeInterface> getAttributes() { return iAttributes; }
	    public boolean hasAttribute(String attribute) { 
	    	for (AttributeInterface a: iAttributes)
	    		if (attribute.equals(a.getName())) return true;
	    	return false;
	    }
	    
	    public void setValue(String criterion, double value) {
	    	iValues.put(criterion, value);
	    }
	    public void addValue(String criterion, double value) {
	    	Double old = iValues.get(criterion);
	    	iValues.put(criterion, value + (old == null ? 0.0 : old.doubleValue()));
	    }
	    public Map<String,Double> getValues() { return iValues; }
	    public Double getValue(String criterion) { return iValues.get(criterion); }
	    
	    public void setAvailability(String availability) { iAvailability = availability; }
	    public String getAvailability() { return iAvailability; }
	    public boolean hetAvailability() { return iAvailability != null && !iAvailability.isEmpty(); }
	    
	    public List<TeachingRequestInfo> getAssignedRequests() { return iAssignedRequests; }
	    public void addAssignedRequest(TeachingRequestInfo request) { iAssignedRequests.add(request); }
	    
	    public List<ClassInfo> getEnrollments() { return iEnrollments; }
	    public void addEnrollment(ClassInfo enrollment) { iEnrollments.add(enrollment); }
	    
	    public int hashCode() { return getInstructorId().hashCode(); }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof InstructorInfo)) return false;
			return getInstructorId().equals(((InstructorInfo)o).getInstructorId());
		}
		public int compareTo(InstructorInfo course) {
			return getInstructorName().compareTo(course.getInstructorName());
		}
		
		@Override
		public String toString() {
			return getInstructorName() + (getExternalId() == null ? "" : " (" + getExternalId() + ")");
		}
	}
	
	public static class TeachingRequestInfo implements Comparable<TeachingRequestInfo>, GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private CourseInfo iCourse;
		private Long iRequestId;
		private float iLoad;
		private List<SectionInfo> iSections = new ArrayList<SectionInfo>();
		private List<InstructorInfo> iInstructors;
	    private List<PreferenceInfo> iInstructorPreferences = new ArrayList<PreferenceInfo>();
	    private List<PreferenceInfo> iAttributePreferences = new ArrayList<PreferenceInfo>();
	    private Map<String,Double> iValues = new HashMap<String, Double>();
	    private int iNrInstructors = 0;
		private String iConflict;
		
		public TeachingRequestInfo() {}
		
		public void setCourse(CourseInfo course) { iCourse = course; }
		public CourseInfo getCourse() { return iCourse; }
		
		public void setRequestId(Long requestId) { iRequestId = requestId; }
		public Long getRequestId() { return iRequestId; }
		
		public void setLoad(float load) { iLoad = load; }
		public float getLoad() { return iLoad; }

		public void addSection(SectionInfo section) { iSections.add(section); }
		public List<SectionInfo> getSections() { return iSections; }
		
		public void addInstructor(InstructorInfo instructor) {
			if (iInstructors == null) iInstructors = new ArrayList<InstructorInfo>();
			iInstructors.add(instructor);
		}
		public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }
		public InstructorInfo getInstructor(int index) {
			if (iInstructors == null || iInstructors.isEmpty()) return null;
			if (index < 0) return null;
			for (int i = 0; i < iInstructors.size(); i++) {
				InstructorInfo instructor = iInstructors.get(i);
				if (instructor.hasAssignmentIndex() && index == instructor.getAssignmentIndex()) return instructor;
				if (!instructor.hasAssignmentIndex() && index == i) return instructor;
			}
			return null;
		}
		public int getNrAssignedInstructors() { return iInstructors == null ? 0 : iInstructors.size(); }
		public List<InstructorInfo> getInstructors() { return iInstructors; }
		public InstructorInfo getInstructor(Long instructorId) {
			if (iInstructors == null) return null;
			for (InstructorInfo instructor: iInstructors)
				if (instructor.getInstructorId().equals(instructorId)) return instructor;
			return null;
		}
		
	    public void addInstructorPreference(PreferenceInfo preference) { iInstructorPreferences.add(preference); }
	    public List<PreferenceInfo> getInstructorPreferences() { return iInstructorPreferences; }

	    public void addAttributePreference(PreferenceInfo preference) { iAttributePreferences.add(preference); }
	    public List<PreferenceInfo> getAttributePreferences() { return iAttributePreferences; }
	    
	    public void setValue(String criterion, double value) {
	    	iValues.put(criterion, value);
	    }
	    public Map<String,Double> getValues() { return iValues; }
	    public Double getValue(String criterion) { return iValues.get(criterion); }
	    
	    public int getNrInstructors() { return iNrInstructors; }
	    public void setNrInstructors(int nrInstructors) { iNrInstructors = nrInstructors; }
	    
		public String getConflict() { return iConflict; }
		public boolean hasConflict() { return iConflict != null && !iConflict.isEmpty(); }
		public void setConflict(String conflict) { iConflict = conflict; }
		
	    @Override
	    public int hashCode() {
	        return iRequestId.hashCode();
	    }
	    
	    @Override
	    public boolean equals(Object o) {
	        if (o == null || !(o instanceof TeachingRequestInfo)) return false;
	        TeachingRequestInfo tr = (TeachingRequestInfo)o;
	        return getRequestId().equals(tr.getRequestId());
	    }

		@Override
		public int compareTo(TeachingRequestInfo r) {
			int cmp = getCourse().compareTo(r.getCourse());
			if (cmp != 0) return cmp;
			Iterator<SectionInfo> i1 = getSections().iterator();
			Iterator<SectionInfo> i2 = r.getSections().iterator();
			while (i1.hasNext() && i2.hasNext()) {
				cmp = i1.next().compareTo(i2.next());
				if (cmp != 0) return cmp;
			}
			if (i2.hasNext()) return -1;
			if (i1.hasNext()) return 1;
			return getRequestId().compareTo(r.getRequestId());
		}
		
		@Override
		public String toString() {
			return getCourse() + " " + getSections();
		}
	}
	
	public static class ClassInfo implements Comparable<ClassInfo>, IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
	    private Long iCourseId;
	    private Long iClassId;
	    private String iCourse;
	    private String iSection;
	    private String iType;
	    private String iExternalId;
	    private String iRoom;
	    private boolean iInstructor;
	    private String iTime;
	    private String iDate;
	    
	    public ClassInfo() {}

	    public Long getCourseId() { return iCourseId; }
	    public void setCourseId(Long courseId) { iCourseId = courseId; }
	    public Long getClassId() { return iClassId; }
	    public void setClassId(Long classId) { iClassId = classId; }
	    
	    public String getCourse() { return iCourse; }
	    public void setCourse(String course) { iCourse = course; }
	    public String getSection() { return iSection; }
	    public void setSection(String section) { iSection = section; }
	    public String getType() { return iType; }
	    public void setType(String type) { iType = type; }
	    public String getExternalId() { return iExternalId; }
	    public void setExternalId(String externalId) { iExternalId = externalId; }
	    public String getRoom() { return iRoom; }
	    public void setRoom(String room) { iRoom = room; }
	    public String getTime() { return iTime; }
	    public void setTime(String time) { iTime = time; }
	    public String getDate() { return iDate; }
	    public void setDate(String date) { iDate = date; }
	    public boolean isInstructor() { return iInstructor; }
	    public void setInstructor(boolean instructor) { iInstructor = instructor; }
	    
	    @Override
	    public int hashCode() { return getClassId().hashCode(); }
	    
	    @Override
	    public boolean equals(Object o) {
	    	if (o == null || !(o instanceof ClassInfo)) return false;
	    	return getClassId().equals(((ClassInfo)o).getClassId());
	    }

		@Override
		public int compareTo(ClassInfo o) {
			int cmp = getCourse().compareTo(o.getCourse());
			if (cmp != 0) return cmp;
			cmp = getSection().compareTo(o.getSection());
			if (cmp != 0) return cmp;
			return getClassId().compareTo(o.getClassId());
		}
	    
		@Override
		public String toString() {
			return getCourse() + " " + getType() + " " + getExternalId();
		}
	}
	
	public static class TeachingRequestsPageRequest implements GwtRpcRequest<GwtRpcResponseList<TeachingRequestInfo>>, Serializable {
		private static final long serialVersionUID = 1L;
		private boolean iAssigned = true;
		private Long iSubjectAreaId = null;
		
		public TeachingRequestsPageRequest() {}
		
		public TeachingRequestsPageRequest(Long subjectAreaId, boolean assigned) {
			iAssigned = assigned;
			iSubjectAreaId = subjectAreaId;
		}
		
		public boolean isAssigned() { return iAssigned; }
		public Long getSubjectAreaId() { return iSubjectAreaId; }
		
		@Override
		public String toString() {
			return (isAssigned() ? "ASSIGNED" : "UNASSIGNED") + (getSubjectAreaId() == null ? "(all)" : "(" + getSubjectAreaId() + ")");
		}
	}
	
	public static class TeachingAssignmentsPageRequest implements GwtRpcRequest<GwtRpcResponseList<InstructorInfo>>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iDepartmentId = null;
		
		public TeachingAssignmentsPageRequest() {}
		
		public TeachingAssignmentsPageRequest(Long departmentId) {
			iDepartmentId = departmentId;
		}
		
		public Long getDepartmentId() { return iDepartmentId; }
		
		@Override
		public String toString() {
			return (getDepartmentId() == null ? "(all)" : "(" + getDepartmentId() + ")");
		}
	}
	
	public static class SubjectAreaInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iAbbv;
		private String iLabel;
		
		public SubjectAreaInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof DepartmentInterface)) return false;
			return getId().equals(((DepartmentInterface)o).getId());
		}
		
		@Override
		public int hashCode() {
			return getId().hashCode();
		}
		
		@Override
		public String toString() {
			return getAbbreviation();
		}
	}
	
	public static class TeachingRequestsPagePropertiesRequest implements GwtRpcRequest<TeachingRequestsPagePropertiesResponse>, Serializable {
		private static final long serialVersionUID = 1L;
		public TeachingRequestsPagePropertiesRequest() {}
	}
	
	public static class TeachingRequestsPagePropertiesResponse implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private List<SubjectAreaInterface> iSubjecAreas = new ArrayList<SubjectAreaInterface>();
		private List<DepartmentInterface> iDepartments = new ArrayList<DepartmentInterface>();
		private List<PreferenceInterface> iPreferences = new ArrayList<PreferenceInterface>();
		private Long iLastSubjectAreaId = null, iLastDepartmentId = null;
		private List<AttributeTypeInterface> iAttributeTypes = new ArrayList<AttributeTypeInterface>();
		private List<RoomSharingDisplayMode> iModes;
		private boolean iHasSolver;
		private InstructorAvailabilityModel iAvailabilityModel;
		
		public TeachingRequestsPagePropertiesResponse() {}
		
		public void addSubjectArea(SubjectAreaInterface subjectArea) { iSubjecAreas.add(subjectArea); }
		public List<SubjectAreaInterface> getSubjectAreas() { return iSubjecAreas; }
		
		public void addDepartment(DepartmentInterface department) { iDepartments.add(department); }
		public List<DepartmentInterface> getDepartments() { return iDepartments; }

		public void addPreference(PreferenceInterface preference) { iPreferences.add(preference); }
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
		public PreferenceInterface getPreference(String p) {
			for (PreferenceInterface preference: iPreferences)
				if (p.equals(preference.getCode())) return preference;
			return null;
		}
		
		public void setLastSubjectAreaId(Long lastSubjectAreaId) { iLastSubjectAreaId = lastSubjectAreaId; }
		public Long getLastSubjectAreaId() { return iLastSubjectAreaId; }
		
		public void setLastDepartmentId(Long lastDepartmentId) { iLastDepartmentId = lastDepartmentId; }
		public Long getLastDepartmentId() { return iLastDepartmentId; }

		public boolean hasAttributeTypes() { return iAttributeTypes != null && !iAttributeTypes.isEmpty(); }
		public void addAttributeType(AttributeTypeInterface type) { iAttributeTypes.add(type); }
		public List<AttributeTypeInterface> getAttributeTypes() { return iAttributeTypes; }
		public AttributeTypeInterface getAttributeType(Long typeId) {
			for (AttributeTypeInterface type: iAttributeTypes)
				if (type.getId().equals(typeId)) return type;
			return null;
		}
		
		public void addMode(RoomSharingDisplayMode mode) {
			if (iModes == null) iModes = new ArrayList<RoomSharingDisplayMode>();
			iModes.add(mode);
		}
		
		public List<RoomSharingDisplayMode> getModes() {
			return iModes;
		}
		
		public boolean hasModes() { return iModes != null && !iModes.isEmpty(); }
		
		public boolean isHasSolver() { return iHasSolver; }
		public void setHasSolver(boolean hasSolver) { iHasSolver = hasSolver; }
		
		public InstructorAvailabilityModel getInstructorAvailabilityModel() { return iAvailabilityModel; }
		public void setInstructorAvailabilityModel(InstructorAvailabilityModel model) { iAvailabilityModel = model; }
	}
	
	public static class TeachingRequestDetailRequest implements GwtRpcRequest<TeachingRequestInfo>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iRequestId;
		
		public TeachingRequestDetailRequest() {}
		public TeachingRequestDetailRequest(Long requestId) { iRequestId = requestId; }
		
		public Long getRequestId() { return iRequestId; }
		public void setRequestId(Long requestId) { iRequestId = requestId; }
		
		@Override
		public String toString() {
			return getRequestId().toString();
		}
	}
	
	public static class TeachingAssignmentsDetailRequest implements GwtRpcRequest<InstructorInfo>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iInstructorId;
		
		public TeachingAssignmentsDetailRequest() {}
		public TeachingAssignmentsDetailRequest(Long instructorId) { iInstructorId = instructorId; }
		
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
		
		@Override
		public String toString() {
			return getInstructorId().toString();
		}
	}
	
	public static class AssignmentInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private TeachingRequestInfo iRequest;
		private int iIndex;
		private InstructorInfo iInstructor;
		private List<String> iConflicts;
		
		public AssignmentInfo() {}
		
		public TeachingRequestInfo getRequest() { return iRequest; }
		public void setRequest(TeachingRequestInfo request) { iRequest = request; }
		
		public int getIndex() { return iIndex; }
		public void setIndex(int index) { iIndex = index; }
		
		public InstructorInfo getInstructor() { return iInstructor; }
		public void setInstructor(InstructorInfo instructor) { iInstructor = instructor; }
		
		public boolean hasConflicts() { return iConflicts != null && !iConflicts.isEmpty(); }
		public List<String> getConflicts() { return iConflicts; }
		public String getConflicts(String separator) {
			if (iConflicts == null || iConflicts.isEmpty()) return null;
			String ret = "";
			for (String conflict: iConflicts)
				ret += (ret.isEmpty() ? "" : separator) + conflict;
			return ret;
		}
		public void addConflict(String conflict) {
			if (iConflicts == null) iConflicts = new ArrayList<String>();
			iConflicts.add(conflict);
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (SectionInfo section: getRequest().getSections()) {
				if (section.isCommon()) continue;
				if (sb.length() > 0) sb.append(", ");
				sb.append(section.toString());
			}
			return getRequest().getCourse() + " " + sb + (getRequest().getNrInstructors() > 1 ? "[" + getIndex() + "]" : "") + ": " + (getInstructor() == null ? "NULL" : getInstructor());
		}
		
		@Override
		public int hashCode() { return getRequest().hashCode(); }
		
		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof AssignmentInfo)) return false;
			AssignmentInfo assignment = (AssignmentInfo)object;
			return getRequest().equals(assignment.getRequest()) && getIndex() == assignment.getIndex();
		}
	}
	
	public static class SuggestionInfo implements IsSerializable, Serializable, Comparable<SuggestionInfo> {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private List<AssignmentInfo> iAssignments = new ArrayList<AssignmentInfo>();
		private double iValue;
		private Map<String,Double> iValues = new HashMap<String, Double>();
		private int iNrConflicts = 0;
		
		public SuggestionInfo() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public void addAssignment(AssignmentInfo assignment) {
			if (assignment.getInstructor() == null) iNrConflicts ++;
			iAssignments.add(assignment);
		}
		public List<AssignmentInfo> getAssignments() { return iAssignments; }

	    public void setValue(String criterion, double value) {
	    	iValues.put(criterion, value);
	    }
	    public void addValue(String criterion, double value) {
	    	Double old = iValues.get(criterion);
	    	iValues.put(criterion, value + (old == null ? 0.0 : old.doubleValue()));
	    }
	    public Map<String,Double> getValues() { return iValues; }
	    public Double getValue(String criterion) { return iValues.get(criterion); }

	    public double getValue() { return iValue; }
	    public void setValue(double value) { iValue = value; }
	    
	    public int nrConflicts() { return iNrConflicts; }

		@Override
		public int compareTo(SuggestionInfo s) {
			int conf = nrConflicts() - s.nrConflicts();
			if (conf != 0) return conf;
			if (getValue() < s.getValue()) return -1;
			if (getValue() > s.getValue()) return 1;
			int size = getAssignments().size() - s.getAssignments().size();
			if (size != 0) return size;
			return getId().compareTo(s.getId());
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (AssignmentInfo a: getAssignments()) {
				if (sb.length() > 0) sb.append("\n\t");
				sb.append(a);
			}
			return "[" + sb + "]";
		}
	}
	
	public static class SuggestionsResponse implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private SuggestionInfo iAssignment = null;
		private List<SuggestionInfo> iSuggestions = null;
		private boolean iTimeoutReached = false;
		private int iNrCombinationsConsidered = 0, iNrSolutions = 0;
	    private List<SuggestionInfo> iDomain = null;
	    
		public SuggestionsResponse() {}
		
		public boolean hasSuggestions() { return iSuggestions != null && !iSuggestions.isEmpty(); }
		public void addSuggestion(SuggestionInfo suggestion) {
			if (iSuggestions == null) iSuggestions = new ArrayList<SuggestionInfo>();
			iSuggestions.add(suggestion);
		}
		public List<SuggestionInfo> getSuggestions() { return iSuggestions; }
		
		public boolean isTimeoutReached() { return iTimeoutReached; }
		public void setTimeoutReached(boolean timeoutReached) { iTimeoutReached = timeoutReached; }
		
		public int getNrCombinationsConsidered() { return iNrCombinationsConsidered; }
		public void setNrCombinationsConsidered(int nrCombinationsConsidered) { iNrCombinationsConsidered = nrCombinationsConsidered; }

		public int getNrSolutions() { return iNrSolutions; }
		public void setNrSolutions(int nrSolutions) { iNrSolutions = nrSolutions; }
		
	    public void addDomainValue(SuggestionInfo suggestion) {
	    	if (iDomain == null) iDomain = new ArrayList<SuggestionInfo>();
	    	iDomain.add(suggestion);
	    }
	    public boolean hasDomainValues() { return iDomain != null && !iDomain.isEmpty(); }
	    public List<SuggestionInfo> getDomainValues() { return iDomain; }
	    
	    public SuggestionInfo getCurrentAssignment() { return iAssignment; }
	    public void setCurrentAssignment(SuggestionInfo assignment) { iAssignment = assignment; }
	}
	
	public static class ComputeSuggestionsRequest implements GwtRpcRequest<SuggestionsResponse>, Serializable {
		private static final long serialVersionUID = 1L;
		private List<AssignmentInfo> iAssignments = new ArrayList<AssignmentInfo>();
		private Long iSelectedRequestId;
		private int iSelectedIndex;
		private Long iSelectedInstructorId;
		private int iMaxDepth = 2;
		private int iTimeout = 5000;
		private int iMaxResults = 20;
		
		public ComputeSuggestionsRequest() {}
		
		public void addAssignment(AssignmentInfo assignment) { iAssignments.add(assignment); }
		public List<AssignmentInfo> getAssignments() { return iAssignments; }
		
		public void setMaxDept(int maxDept) { iMaxDepth = maxDept; }
		public int getMaxDept() { return iMaxDepth; }
		
		public void setTimeout(int timeout) { iTimeout = timeout; }
		public int getTimeout() { return iTimeout; }
		
		public void setMaxResults(int maxResults) { iMaxResults = maxResults; }
		public int getMaxResults() { return iMaxResults; }
		
		public Long getSelectedRequestId() { return iSelectedRequestId; }
		public void setSelectedRequestId(Long requestId) { iSelectedRequestId = requestId; }
		
		public Long getSelectedInstructorId() { return iSelectedInstructorId; }
		public void setSelectedInstructorId(Long instructorId) { iSelectedInstructorId = instructorId; }

		public int getSelectedIndex() { return iSelectedIndex; }
		public void setSelectedIndex(int idx) { iSelectedIndex = idx; }
		
		@Override
		public String toString() {
			return getMaxDept() + "," + getTimeout() + "," + getMaxResults();
		}
	}
	
	public static class InstructorAssignmentRequest implements GwtRpcRequest<GwtRpcResponseNull>, Serializable {
		private static final long serialVersionUID = 1L;
		private List<AssignmentInfo> iAssignments = new ArrayList<AssignmentInfo>();
		
		public InstructorAssignmentRequest() {}
		
		public void addAssignment(AssignmentInfo assignment) { iAssignments.add(assignment); }
		public List<AssignmentInfo> getAssignments() { return iAssignments; }
		
		@Override
		public String toString() {
			return (getAssignments() == null ? "" : getAssignments().toString());
		}
	}
	
	public static enum ChangesType {
		INITIAL, BEST, SAVED;
	}
	
	public static class AssignmentChangesRequest implements GwtRpcRequest<AssignmentChangesResponse>, Serializable {
		private static final long serialVersionUID = 1L;
		private ChangesType iType = ChangesType.INITIAL;
		
		public AssignmentChangesRequest() {}
		public AssignmentChangesRequest(ChangesType type) {
			iType = type;
		}
		
		public void setType(ChangesType type) { iType = type; }
		public ChangesType getType() { return iType; }
	}
	
	public static class AssignmentChangesResponse implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private List<AssignmentInfo> iChanges = new ArrayList<AssignmentInfo>();
		
		public AssignmentChangesResponse() {}
		
		public void addChange(AssignmentInfo change) { iChanges.add(change); }
		public List<AssignmentInfo> getChanges() { return iChanges; }
	}

}
