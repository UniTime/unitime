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
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class InstructorInterface implements IsSerializable, Comparable<InstructorInterface> {
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

	public static class AttributeTypeInterface implements GwtRpcResponse {
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
	}
	
	public static class PositionInterface implements GwtRpcResponse {
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
	}
	
	public static class DepartmentInterface implements IsSerializable {
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
	}
	
	public static class PreferenceInterface implements IsSerializable {
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
	}
	
	public static class AttributeInterface implements GwtRpcResponse {
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
	
	public static class InstructorAttributePropertiesInterface implements GwtRpcResponse {
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
	
	public static class InstructorAttributePropertiesRequest implements GwtRpcRequest<InstructorAttributePropertiesInterface> {
		public InstructorAttributePropertiesRequest() {}
	}
	
	public static class GetInstructorAttributesRequest implements GwtRpcRequest<GwtRpcResponseList<AttributeInterface>> {
		public Long iDepartmentId;
		
		public GetInstructorAttributesRequest() {}
		
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		
		@Override
		public String toString() { return iDepartmentId == null ? "" : iDepartmentId.toString(); }
	}
	
	public static class GetInstructorsRequest implements GwtRpcRequest<GwtRpcResponseList<InstructorInterface>> {
		public Long iDepartmentId;
		
		public GetInstructorsRequest() {}
		
		public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
		public Long getDepartmentId() { return iDepartmentId; }
		
		@Override
		public String toString() { return iDepartmentId == null ? "" : iDepartmentId.toString(); }
	}

	public static class UpdateInstructorAttributeRequest implements GwtRpcRequest<AttributeInterface> {
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
	
	public static class GetInstructorAttributeParentsRequest implements GwtRpcRequest<GwtRpcResponseList<AttributeInterface>> {
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
}
