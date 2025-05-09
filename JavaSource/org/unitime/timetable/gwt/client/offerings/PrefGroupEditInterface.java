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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PrefGroupEditInterface {
	
	public static enum Operation {
		GET, UPDATE, NEXT, PREVIOUS, CLEAR_CLASS_PREFS,
		DATE_PATTERN, INSTRUCTORS,
		;
	}
	public static enum InheritInstructorPrefs {
		NEVER, ALWAYS, ASK,
		;
	}

	public static abstract class PrefGroupEditRequest<T> implements IsSerializable {
		private Long iId;
		private Operation iOperation;
		private T iPayLoad;
		
		PrefGroupEditRequest() {}
		PrefGroupEditRequest(Long id) { iId = id; }
		PrefGroupEditRequest(Long id, Operation op, T payLoad) { iId = id; iOperation = op; iPayLoad = payLoad; }
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation op) { iOperation = op; }
		
		public T getPayLoad() { return iPayLoad; }
		public void setPayLoad(T payLoad) { iPayLoad = payLoad; }
	}
	
	public static abstract class PrefGroupEditResponse implements GwtRpcResponse {
		private Long iId, iPreviousId, iNextId;
		private String iName;
		private Preferences iDatePrefs;
		private TimePreferences iTimePrefs;
		private List<PrefLevel> iPrefLevels;
		private List<Preferences> iRoomPrefs;
		private Preferences iDistributionPrefs;
		private Preferences iCoursePrefs;
		private Integer iNbrRooms;
		private String iUrl;
		private String iInstructorUnavailability = null;
		private String iInstructorTimePreferences = null;
		private Boolean iCanClearPrefs;
		
		public PrefGroupEditResponse() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public boolean hasRoomPreferences() { return iRoomPrefs != null && !iRoomPrefs.isEmpty(); }
		public List<Preferences> getRoomPreferences() { return iRoomPrefs; }
		public void addRoomPreference(Preferences pref) {
			if (iRoomPrefs == null) iRoomPrefs = new ArrayList<Preferences>();
			iRoomPrefs.add(pref);
		}
		public Preferences getRoomPreference(PreferenceType type) {
			if (iRoomPrefs == null) return null;
			for (Preferences p: iRoomPrefs)
				if (type == p.getType()) return p;
			return null;
		}
		
		public boolean hasTimePreferences() { return iTimePrefs != null && iTimePrefs.hasItems(); }
		public TimePreferences getTimePreferences() { return iTimePrefs; }
		public void setTimePreferences(TimePreferences datePrefs) { iTimePrefs = datePrefs; }
		
		public boolean hasDatePreferences() { return iDatePrefs != null && iDatePrefs.hasItems(); }
		public Preferences getDatePreferences() { return iDatePrefs; }
		public void setDatePreferences(Preferences datePrefs) { iDatePrefs = datePrefs; }
		
		public boolean hasDistributionPreferences() { return iDistributionPrefs != null && iDistributionPrefs.hasItems(); }
		public Preferences getDistributionPreferences() { return iDistributionPrefs; }
		public void setDistributionPreferences(Preferences distributionPrefs) { iDistributionPrefs = distributionPrefs; }

		public boolean hasCoursePreferences() { return iCoursePrefs != null && iCoursePrefs.hasItems(); }
		public Preferences getCoursePreferences() { return iCoursePrefs; }
		public void setCoursePreferences(Preferences coursePrefs) { iCoursePrefs = coursePrefs; }

		public void addPrefLevel(PrefLevel prefLevel) {
			if (iPrefLevels == null) iPrefLevels = new ArrayList<PrefLevel>();
			iPrefLevels.add(prefLevel);
		}
		public List<PrefLevel> getPrefLevels() { return iPrefLevels; }
		public PrefLevel getPrefLevel(Long id) {
			if (iPrefLevels == null) return null;
			for (PrefLevel level: iPrefLevels)
				if (level.getId().equals(id)) return level;
			return null;
		}
		public PrefLevel getPrefLevel(String code) {
			if (iPrefLevels == null) return null;
			for (PrefLevel level: iPrefLevels)
				if (level.getCode().equals(code)) return level;
			return null;
		}
		
		public void setNbrRooms(int nbrRooms) { iNbrRooms = nbrRooms; }
		public Integer getNbrRooms() { return iNbrRooms; }
		public boolean hasRooms() { return iNbrRooms != null && iNbrRooms > 0; }
		public boolean hasMultipleRooms() { return iNbrRooms != null && iNbrRooms > 1; }
		
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }
		
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		
		public boolean hasInstructorUnavailability() { return iInstructorUnavailability != null && !iInstructorUnavailability.isEmpty(); }
		public String getInstructorUnavailability() { return iInstructorUnavailability; }
		public void setInstructorUnavailability(String unavailability) { iInstructorUnavailability = unavailability; }
		public boolean hasInstructorTimePrefereneces() { return iInstructorTimePreferences != null && !iInstructorTimePreferences.isEmpty(); }
		public String getInstructorTimePrefereneces() { return iInstructorTimePreferences; }
		public void setInstructorTimePrefereneces(String preferences) { iInstructorTimePreferences = preferences; }
		
		public boolean canClearPrefs() { return iCanClearPrefs != null && iCanClearPrefs.booleanValue(); }
		public void setCanClearPrefs(boolean canClearPrefs) { iCanClearPrefs = canClearPrefs; }
	}
	
	public static class ClassEditRequest extends PrefGroupEditRequest<ClassEditResponse> implements GwtRpcRequest<ClassEditResponse> {
		
	}
	
	public static class ClassEditResponse extends PrefGroupEditResponse {
		private TableInterface iProperties;
		private TableInterface iTimetable;
		
		private Boolean iSearchableDatePattern;
		private Long iDatePatternId;
		private List<IdLabel> iDatePatterns;
		private Boolean iDisplayInstructors;
		private Boolean iStudentScheduling;
		private String iScheduleNote, iRequestNote;
		private List<IdLabel> iResponsibilities;
		private List<IdLabel> iInstructors;
		private List<ClassInstr> iClassInstructors;
		private InheritInstructorPrefs iInheritInstructorPrefs;
		private Long iDefaultResponsibilityId;
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public TableInterface getProperties() { return iProperties; }
		public void setProperties(TableInterface properties) { iProperties = properties; }
		public CellInterface addProperty(String text) {
			if (iProperties == null) iProperties = new TableInterface();
			return iProperties.addProperty(text);
		}

		public InheritInstructorPrefs getInheritInstructorPrefs() {
			return (iInheritInstructorPrefs == null ? InheritInstructorPrefs.NEVER: iInheritInstructorPrefs);
		}
		public void setInheritInstructorPrefs(InheritInstructorPrefs prefs) {
			iInheritInstructorPrefs = prefs;
		}
		
		public boolean hasTimetable() { return iTimetable != null; }
		public TableInterface getTimetable() { return iTimetable; }
		public void setTimetable(TableInterface timetable) { iTimetable = timetable; }
		
		public boolean isDisplayInstructors() { return iDisplayInstructors == null || iDisplayInstructors.booleanValue(); }
		public void setDisplayInstructors(Boolean display) { iDisplayInstructors = display; }
		public boolean isStudentScheduling() { return iStudentScheduling == null || iStudentScheduling.booleanValue(); }
		public void setStudentScheduling(Boolean scheduling) { iStudentScheduling = scheduling; }
		
		public String getScheduleNote() { return iScheduleNote; }
		public boolean hasScheduleNote() { return iScheduleNote != null && !iScheduleNote.isEmpty(); }
		public void setScheduleNote(String note) { iScheduleNote = note; }

		public String getRequestNote() { return iRequestNote; }
		public boolean hasRequestNote() { return iRequestNote != null && !iRequestNote.isEmpty(); }
		public void setRequestNote(String note) { iRequestNote = note; }

		public boolean isSearchableDatePattern() { return iSearchableDatePattern != null && iSearchableDatePattern.booleanValue(); }
		public void setSearchableDatePattern(boolean searchableDatePattern) { iSearchableDatePattern = searchableDatePattern; }
		public Long getDatePatternId() { return iDatePatternId; }
		public void setDatePatternId(Long datePatternId) { iDatePatternId = datePatternId; }
		public boolean hasDatePatterms() { return iDatePatterns != null && !iDatePatterns.isEmpty(); }
		public List<IdLabel> getDatePatterns() { return iDatePatterns; }
		public IdLabel getDatePattern(Long id) {
			if (iDatePatterns == null) return null;
			for (IdLabel item: iDatePatterns)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addDatePattern(Long id, String label, String pattern) {
			if (iDatePatterns == null) iDatePatterns = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, pattern);
			iDatePatterns.add(item);
			return item;
		}
		public IdLabel removeDatePattern(Long id) {
			if (iDatePatterns == null) return null;
			for (Iterator<IdLabel> i = iDatePatterns.iterator(); i.hasNext(); ) {
				IdLabel item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return item;
				}
			}
			return null;
		}
		
		public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }
		public List<IdLabel> getInstructors() { return iInstructors; }
		public IdLabel getInstructor(Long id) {
			if (iInstructors == null) return null;
			for (IdLabel item: iInstructors)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addInstructor(Long id, String label, String description) {
			if (iInstructors == null) iInstructors = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, description);
			iInstructors.add(item);
			return item;
		}
		public IdLabel removeInstructor(Long id) {
			if (iInstructors == null) return null;
			for (Iterator<IdLabel> i = iInstructors.iterator(); i.hasNext(); ) {
				IdLabel item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return item;
				}
			}
			return null;
		}
		
		public boolean hasResponsibilities() { return iResponsibilities != null && !iResponsibilities.isEmpty(); }
		public List<IdLabel> getResponsibilities() { return iResponsibilities; }
		public IdLabel getResponsibility(Long id) {
			if (iResponsibilities == null) return null;
			for (IdLabel item: iResponsibilities)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addResponsibility(Long id, String label, String description) {
			if (iResponsibilities == null) iResponsibilities = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, description);
			iResponsibilities.add(item);
			return item;
		}
		public IdLabel removeResponsibility(Long id) {
			if (iResponsibilities == null) return null;
			for (Iterator<IdLabel> i = iResponsibilities.iterator(); i.hasNext(); ) {
				IdLabel item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return item;
				}
			}
			return null;
		}
		public Long getDefaultResponsibilityId() { return iDefaultResponsibilityId; }
		public void setDefaultResponsibilityId(Long defaultResponsibilityId) { iDefaultResponsibilityId = defaultResponsibilityId; }
		
		public boolean hasClassInstructors() { return iClassInstructors != null && !iClassInstructors.isEmpty(); }
		public List<ClassInstr> getClassInstructors() { return iClassInstructors; }
		public void setClassInstructors(List<ClassInstr> classInstructors) { iClassInstructors = classInstructors; }
		public ClassInstr getClassInstructor(Long id) {
			if (iClassInstructors == null) return null;
			for (ClassInstr item: iClassInstructors)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public void addClassInstructor(ClassInstr instr) {
			if (iClassInstructors == null) iClassInstructors = new ArrayList<ClassInstr>();
			iClassInstructors.add(instr);
		}
		public ClassInstr removeClassInstructor(Long instructorId, Long responsibilityId) {
			if (iClassInstructors == null) return null;
			ClassInstr ci = new ClassInstr(instructorId, responsibilityId);
			for (Iterator<ClassInstr> i = iClassInstructors.iterator(); i.hasNext(); ) {
				ClassInstr item = i.next();
				if (ci.equals(item)) {
					i.remove();
					return item;
				}
			}
			return null;
		}
	}
	
	public static class SubpartEditRequest extends PrefGroupEditRequest<SubpartEditResponse> implements GwtRpcRequest<SubpartEditResponse> {
		
	}
	
	public static class SubpartEditResponse extends PrefGroupEditResponse {
		private TableInterface iProperties;
		
		private Boolean iSearchableDatePattern;
		private Long iDatePatternId;
		private List<IdLabel> iDatePatterns;
		
		private List<IdLabel> iInstructionalTypes;
		private List<IdLabel> iExtInstructionalTypes;
		
		private Boolean iAutoSpreadInTime;
		private Boolean iStudentsCanOverlap;

		private Boolean iCreditFractionsAllowed;
		private Float iCreditUnits;
		private Float iCreditMaxUnits;
		private Long iCreditFormatId;
		private List<IdLabel> iCreditFormats;
		private Long iCreditTypeId;
		private List<IdLabel> iCreditTypes;
		private Long iCreditUnitTypeId;
		private List<IdLabel> iCreditUnitTypes;
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public TableInterface getProperties() { return iProperties; }
		public void setProperties(TableInterface properties) { iProperties = properties; }
		public CellInterface addProperty(String text) {
			if (iProperties == null) iProperties = new TableInterface();
			return iProperties.addProperty(text);
		}
		
		public boolean isAutoSpreadInTime() { return iAutoSpreadInTime == null || iAutoSpreadInTime.booleanValue(); }
		public void setAutoSpreadInTime(Boolean spread) { iAutoSpreadInTime = spread; }
		public boolean isStudentsCanOverlap() { return iStudentsCanOverlap == null || iStudentsCanOverlap.booleanValue(); }
		public void setStudentsCanOverlap(Boolean canOverlap) { iStudentsCanOverlap = canOverlap; }
		
		public boolean isSearchableDatePattern() { return iSearchableDatePattern != null && iSearchableDatePattern.booleanValue(); }
		public void setSearchableDatePattern(boolean searchableDatePattern) { iSearchableDatePattern = searchableDatePattern; }
		public Long getDatePatternId() { return iDatePatternId; }
		public void setDatePatternId(Long datePatternId) { iDatePatternId = datePatternId; }
		public boolean hasDatePatterms() { return iDatePatterns != null && !iDatePatterns.isEmpty(); }
		public List<IdLabel> getDatePatterns() { return iDatePatterns; }
		public IdLabel getDatePattern(Long id) {
			if (iDatePatterns == null) return null;
			for (IdLabel item: iDatePatterns)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addDatePattern(Long id, String label, String pattern) {
			if (iDatePatterns == null) iDatePatterns = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, pattern);
			iDatePatterns.add(item);
			return item;
		}
		public IdLabel removeDatePattern(Long id) {
			if (iDatePatterns == null) return null;
			for (Iterator<IdLabel> i = iDatePatterns.iterator(); i.hasNext(); ) {
				IdLabel item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return item;
				}
			}
			return null;
		}
		
		public boolean hasInstructionalTypes() { return iInstructionalTypes != null && !iInstructionalTypes.isEmpty(); }
		public List<IdLabel> getInstructionalTypes() { return iInstructionalTypes; }
		public IdLabel getInstructionalType(Long id) {
			if (iInstructionalTypes != null)
				for (IdLabel item: iInstructionalTypes)
					if (id.equals(item.getId())) return item;
			if (iExtInstructionalTypes != null)
				for (IdLabel item: iExtInstructionalTypes)
					if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addInstructionalType(Long id, String label, String description) {
			if (iInstructionalTypes == null) iInstructionalTypes = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, description);
			iInstructionalTypes.add(item);
			return item;
		}
		public boolean hasExtInstructionalTypes() { return iExtInstructionalTypes != null && !iExtInstructionalTypes.isEmpty(); }
		public List<IdLabel> getExtInstructionalTypes() { return iExtInstructionalTypes; }
		public IdLabel addExtInstructionalType(Long id, String label, String description) {
			if (iExtInstructionalTypes == null) iExtInstructionalTypes = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, description);
			iExtInstructionalTypes.add(item);
			return item;
		}

		public boolean isCreditFractionsAllowed() { return iCreditFractionsAllowed != null && iCreditFractionsAllowed.booleanValue(); }
		public void setCreditFractionsAllowed(boolean frectionsAllowed) { iCreditFractionsAllowed = frectionsAllowed; }
		public boolean hasCreditUnits() { return iCreditUnits != null; }
		public Float getCreditUnits() { return iCreditUnits; }
		public void setCreditUnits(Float credits) { iCreditUnits = credits; }
		public boolean hasCreditMaxUnits() { return iCreditMaxUnits != null; }
		public Float getCreditMaxUnits() { return iCreditMaxUnits; }
		public void setCreditMaxUnits(Float credits) { iCreditMaxUnits = credits; }
		public boolean hasCreditFormats() { return iCreditFormats != null && !iCreditFormats.isEmpty(); }
		public List<IdLabel> getCreditFormats() { return iCreditFormats; }
		public IdLabel getCreditFormat(Long id) {
			if (iCreditFormats == null) return null;
			for (IdLabel item: iCreditFormats)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel getCreditFormat(String reference) {
			if (iCreditFormats == null) return null;
			for (IdLabel item: iCreditFormats)
				if (reference.equals(item.getDescription())) return item;
			return null;
		}
		public IdLabel addCreditFormat(Long id, String label, String reference) {
			if (iCreditFormats == null) iCreditFormats = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, reference);
			iCreditFormats.add(item);
			return item;
		}
		public boolean hasCreditTypes() { return iCreditTypes != null && !iCreditTypes.isEmpty(); }
		public List<IdLabel> getCreditTypes() { return iCreditTypes; }
		public IdLabel getCreditType(Long id) {
			if (iCreditTypes == null) return null;
			for (IdLabel item: iCreditTypes)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addCreditType(Long id, String label, String reference) {
			if (iCreditTypes == null) iCreditTypes = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, reference);
			iCreditTypes.add(item);
			return item;
		}
		public boolean hasCreditUnitTypes() { return iCreditUnitTypes != null && !iCreditUnitTypes.isEmpty(); }
		public List<IdLabel> getCreditUnitTypes() { return iCreditUnitTypes; }
		public IdLabel getCreditUnitType(Long id) {
			if (iCreditUnitTypes == null) return null;
			for (IdLabel item: iCreditUnitTypes)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addCreditUnitType(Long id, String label, String reference) {
			if (iCreditUnitTypes == null) iCreditUnitTypes = new ArrayList<IdLabel>();
			IdLabel item = new IdLabel(id, label, reference);
			iCreditUnitTypes.add(item);
			return item;
		}
		public String getCreditFormat() {
			if (iCreditFormatId == null) return null;
			IdLabel format = getCreditFormat(iCreditFormatId);
			return (format == null ? null : format.getDescription());
		}
		public Long getCreditFormatId() { return iCreditFormatId; }
		public void setCreditFormatId(Long id) { iCreditFormatId = id; }
		public Long getCreditTypeId() { return iCreditTypeId; }
		public void setCreditTypeId(Long id) { iCreditTypeId = id; }
		public Long getCreditUnitTypeId() { return iCreditUnitTypeId; }
		public void setCreditUnitTypeId(Long id) { iCreditUnitTypeId = id; }
	}
	
	public static class InstructorPreferencesEditRequest extends PrefGroupEditRequest<InstructorPreferencesEditResponse> implements GwtRpcRequest<InstructorPreferencesEditResponse> {
		
	}
	
	public static class InstructorPreferencesEditResponse extends PrefGroupEditResponse {

	}
	
	public static class InstructorAssignmentPreferencesEditRequest extends PrefGroupEditRequest<InstructorAssignmentPreferencesEditResponse> implements GwtRpcRequest<InstructorAssignmentPreferencesEditResponse> {
	}
	
	public static class InstructorAssignmentPreferencesEditResponse extends PrefGroupEditResponse {
		private Long iTeachingPrefId;
		private Float iMaxTeachingLoad;
		private Set<Long> iAttributeIds;
		private List<AttributeInterface> iAttributes;
		
		public Long getTeachingPrefId() { return iTeachingPrefId; }
		public void setTeachingPrefId(Long prefId) { iTeachingPrefId = prefId; }
		public Float getMaxTeachingLoad() { return iMaxTeachingLoad; }
		public void setMaxTeachingLoad(Float maxLoad) { iMaxTeachingLoad = maxLoad; }

		public void addInstructorAttribute(AttributeInterface attribute) {
			addInstructorAttribute(attribute.getId());
		}
		public void addInstructorAttribute(Long attributeId) {
			if (iAttributeIds == null) iAttributeIds = new HashSet<Long>();
			iAttributeIds.add(attributeId);
		}
		public void removeInstructorAttribute(Long attributeId) {
			if (iAttributeIds == null) iAttributeIds = new HashSet<Long>();
			iAttributeIds.remove(attributeId);
		}
		public boolean hasInstructorAttribute(AttributeInterface attribute) {
			return hasInstructorAttribute(attribute.getId());
		}
		public boolean hasInstructorAttribute(Long attributeId) {
			return iAttributeIds != null && iAttributeIds.contains(attributeId);
		}
		public Set<Long> getInstructorAttributeIds() { return iAttributeIds; }
		public boolean hasInstructorAttributeIds() { return iAttributeIds != null && !iAttributeIds.isEmpty(); }

		public List<AttributeInterface> getAttributes() {
			return iAttributes;
		}
		public void addAttribute(AttributeInterface attribute) {
			if (iAttributes == null) iAttributes = new ArrayList<AttributeInterface>();
			iAttributes.add(attribute);
		}
		public Set<AttributeTypeInterface> getAttributeTypes() {
			Set<AttributeTypeInterface> types = new TreeSet<AttributeTypeInterface>();
			if (iAttributes != null)
				for (AttributeInterface attribute: iAttributes)
					if (attribute.getType() != null) types.add(attribute.getType());
			return types;
		}
		public List<AttributeInterface> getAttributesOfType(AttributeTypeInterface type) {
			List<AttributeInterface> ret = new ArrayList<AttributeInterface>();
			if (iAttributes != null)
				for (AttributeInterface attribute: iAttributes) {
					if (type == null && attribute.getType() == null) ret.add(attribute);
					if (type != null && type.equals(attribute.getType())) ret.add(attribute);
				}
			return ret;
		}
		public boolean hasAttributesOfType(AttributeTypeInterface type) {
			if (iAttributes != null)
				for (AttributeInterface attribute: iAttributes) {
					if (type == null && attribute.getType() == null) return true;
					if (type != null && type.equals(attribute.getType())) return true;
				}
			return false;
		}
	}

	public static class ClassInstr implements IsSerializable {
		private Long iId;
		private Long iInstructorId;
		private Long iResponsibilityId;
		private boolean iCheckConflicts = true;
		private int iPercent = 0;
		
		public ClassInstr() {}
		public ClassInstr(Long instructorId, Long responsibilityId) {
			iInstructorId = instructorId; iResponsibilityId = responsibilityId;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
		public Long getResponsibilityId() { return iResponsibilityId; }
		public void setResponsibilityId(Long responsibilityId) { iResponsibilityId = responsibilityId; }
		public boolean isCheckConflicts() { return iCheckConflicts; }
		public void setCheckConflicts(boolean checkConflicts) { iCheckConflicts = checkConflicts; }
		public int getPercentShare() { return iPercent; }
		public void setPercentShare(int percent) { iPercent = percent; }
		
		@Override
		public int hashCode() {
			if (getResponsibilityId() == null)
				return getInstructorId().hashCode();
			else
				return getInstructorId().hashCode() ^ getResponsibilityId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ClassInstr)) return false;
			ClassInstr ci = (ClassInstr)o;
			if (!ci.getInstructorId().equals(getInstructorId())) return false;
			if (getResponsibilityId() == null)
				return (ci.getResponsibilityId() == null);
			else
				return getResponsibilityId().equals(ci.getResponsibilityId());
		}
	}
	
	public static class PrefLevel implements IsSerializable {
		private Long iId;
		private String iLabel;
		private String iTitle;
		private String iColor;
		private String iCode;
		private Character iTpCode;
		
		public PrefLevel() {}
		public PrefLevel(Long id, String code, String label, String title, String color, char tpCode) {
			iId = id; iCode = code; iLabel = label; iTitle = title; iColor = color; iTpCode = tpCode;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getCode() { return iCode; }
		public void setCode(String code) { iCode = code;} 
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public String getColor() { return iColor; }
		public void setColor(String color) { iColor = color; }
		public boolean isHard() { return "R".equals(iCode) || "P".equals(iCode); }
		public void setTpCode(Character tpCode) { iTpCode = tpCode; }
		public Character getTpCode() { return iTpCode; }
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + "}";
		}
	}
	
	public static class IdLabel implements IsSerializable, Comparable<IdLabel> {
		private Long iId;
		private String iLabel;
		private String iDescription;
		private String iAllowedPrefs;

		public IdLabel() {}
		public IdLabel(Long id, String label, String description) {
			iId = id; iLabel = label; iDescription = description;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdValue)) return false;
			return getId().equals(((IdValue)o).getId());
		}
		@Override
		public int compareTo(IdLabel other) {
			return NaturalOrderComparator.compare(getLabel(), other.getLabel());
		}
		
		public boolean hasDescription() { return iDescription != null && !iDescription.isEmpty(); }
		public String getDescription() { return iDescription; }
		public void setDescription(String description) { iDescription = description; }
		
		public boolean hasAllowedPrefs() { return iAllowedPrefs != null && !iAllowedPrefs.isEmpty(); }
		public String getAllowedPrefs() { return iAllowedPrefs; }
		public void setAllowedPrefs(String prefs) { iAllowedPrefs = prefs; }
		public boolean isAllowed(PrefLevel pref) {
			if (iAllowedPrefs == null || iAllowedPrefs.isEmpty()) return true;
			return iAllowedPrefs.indexOf(pref.getTpCode()) >= 0;
		}
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + "}";
		}
	}
	
	public static enum DayCode {
		MON(64),
		TUE(32),
		WED(16),
		THU(8),
		FRI(4),
		SAT(2),
		SUN(1),
		;
		DayCode(int code) { iCode = code; }
		private int iCode;
		public int getCode() { return iCode; }
	}
	
	public static class TimePatternModel implements IsSerializable {
		private boolean iHardAllowed = true;
		private List<Integer> iTimes;
		private List<Integer> iDays;
		private char[][] iPreference;
		private int iLength = 0;
		private int iDayOffset = 0;
		private boolean iExactTime = false;
		private Long iTimePatternId;
		private String iTimePatternName;
		private boolean iValid = true;
		private List<PrefLevel> iPrefLevels;
		private Boolean iHorizontal;
		private int iAssignedDay, iAssignedTime;
		
		public TimePatternModel() {}
		public TimePatternModel(TimePatternModel tp) {
			if (tp.iTimes != null) iTimes = new ArrayList<Integer>(tp.iTimes);
			if (tp.iDays != null) iDays = new ArrayList<Integer>(tp.iDays);
			if (tp.iPreference != null) {
				iPreference = new char[iDays.size()][iTimes.size()];
				for (int d = 0; d < iDays.size(); d++)
					for (int t = 0; t < iTimes.size(); t++)
						iPreference[d][t] = tp.iPreference[d][t]; 
			}
			iLength = tp.iLength;
			iDayOffset = tp.iDayOffset;
			iExactTime = tp.iExactTime;
			iValid = tp.iValid;
			iTimePatternId = tp.iTimePatternId;
			iTimePatternName = tp.iTimePatternName;
		}
		
		public Long getId() { return iTimePatternId; }
		public void setId(Long id) { iTimePatternId = id; }
		public String getName() { return iTimePatternName; }
		public void setName(String name) { iTimePatternName = name; }
		public boolean isExactTime() { return iExactTime; }
		public void setExactTime(boolean exactTime) { iExactTime = exactTime; }
		public boolean isValid() { return iValid; }
		public void setValid(boolean valid) { iValid = valid; }
		
		public boolean hasAllowedPrefs() { return !iHardAllowed; }
		public void setAllowHard(boolean allowHard) { iHardAllowed = allowHard; }
		public boolean isAllowedPref(Character code) {
			if (iHardAllowed) return true;
			return code != 'R' && code != 'P';
		}
		
		public boolean hasRequired() {
			return !isExactTime() && getPreference().indexOf('R') >= 0;
		}
		public boolean hasPreference() {
			return !isExactTime() && (
					getPreference().indexOf('P') >= 0 ||
					getPreference().indexOf('0') >= 0 ||
					getPreference().indexOf('1') >= 0 ||
					getPreference().indexOf('2') >= 0 ||
					getPreference().indexOf('3') >= 0);
		}
		
		public void addTime(int time) {
			if (iTimes == null) iTimes = new ArrayList<Integer>();
			iTimes.add(time);
		}
		public List<Integer> getTimes() { return iTimes; }
		public void addDays(int days) {
			if (iDays == null) iDays = new ArrayList<Integer>();
			iDays.add(days);
		}
		public List<Integer> getDays() { return iDays; }
		public void setLength(int length) { iLength = length; }
		public int getLength(Integer length) { return iLength; }
		public void setDayOffset(int offset) { iDayOffset = offset; }
		public int getDayOffset() { return iDayOffset; }
		public void setPreference(String preference) {
			if (isExactTime()) {
				if (iDays != null) iDays.clear();
				if (iTimes != null) iTimes.clear();
				if (preference != null) {
					for (String p: preference.split(";")) {
						if (p.indexOf(',') >= 0) {
							addDays(Integer.valueOf(p.substring(0, p.indexOf(','))));
							addTime(Integer.valueOf(p.substring(1+ p.indexOf(','))));
						}
					}
				}
			} else {
				iPreference = new char[iDays.size()][iTimes.size()];
				int idx = 0;
				for (int d = 0; d < iDays.size(); d++)
					for (int t = 0; t < iTimes.size(); t++)  {
						iPreference[d][t] = (preference == null || preference.length() <= idx ? '2' : preference.charAt(idx));
						idx++;
					}
			}
		}
		public String getPreference() {
			if (isExactTime()) {
				if (iDays != null && iDays.size() > 0 && iTimes != null && iTimes.size() > 0) {
					String ret = null;
					for (int i = 0; i < Math.min(iDays.size(), iTimes.size()); i++) {
						if (i == 0)
							ret = iDays.get(i) + "," + iTimes.get(i);
						else
							ret += ";" + iDays.get(i) + "," + iTimes.get(i);
					}
					return ret;
				}
				return null;
			} else {
				String ret = "";
				for (int d = 0; d < iDays.size(); d++)
					for (int t = 0; t < iTimes.size(); t++)
						ret += iPreference[d][t];
				return ret;
			}
		}
		
		public char getPreference(int days, int time) { return iPreference[days][time]; }
		public void setPreference(int days, int time, char code) { iPreference[days][time] = code; }
		public Integer getExactTime() {
			if (iTimes != null && iTimes.size() == 1)
				return iTimes.get(0);
			return null;
		}
		public Integer getExactDays() {
			if (iDays != null && iDays.size() == 1)
				return iDays.get(0);
			return null;
		}
		public void setExactTime(Integer time) {
			if (iTimes != null) iTimes.clear();
			if (time != null) addTime(time);
		}
		public void setExactDays(Integer days) {
			if (iDays != null) iDays.clear();
			if (days != null) addTime(days);
		}
		public int getNrTimes() { return iTimes == null ? 0 : iTimes.size(); }
		public int getNrDays() { return iDays == null ? 0 : iDays.size(); }
		public String getDaysLabel(int index, GwtConstants gwtC) {
			String shortDays = "", longDays = ""; int nrDays = 0;
			int dayCode = iDays.get(index);
			for (int i = 0; i < DayCode.values().length; i++) {
				DayCode dc = DayCode.values()[(i + iDayOffset) % DayCode.values().length];
				if ((dc.getCode() & dayCode) != 0) {
					nrDays ++;
					shortDays += gwtC.shortDays()[dc.ordinal()];
					longDays += gwtC.days()[dc.ordinal()];
				}
			}
			return (nrDays > 1 ? shortDays : longDays);
		}
		public String getStartTime(int index, GwtConstants gwtC) {
			int startSlot = iTimes.get(index);
			int min = startSlot * 5;
	        int h = min / 60;
	        int m = min % 60;
	        if (gwtC.useAmPm())
	            return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? gwtC.timeShortPm() : gwtC.timeShortAm());
	        else
	            return h + ":" + (m < 10 ? "0" : "") + m;
		}
		public String getEndTime(int index, GwtConstants gwtC) {
			int startSlot = iTimes.get(index);
			int min = startSlot * 5 + iLength;
	        int h = min / 60;
	        int m = min % 60;
	        if (gwtC.useAmPm())
	            return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? gwtC.timeShortPm() : gwtC.timeShortAm());
	        else
	            return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public void addPrefLevel(PrefLevel prefLevel) {
			if (iPrefLevels == null) iPrefLevels = new ArrayList<PrefLevel>();
			iPrefLevels.add(prefLevel);
		}
		public List<PrefLevel> getPrefLevels() { return iPrefLevels; }
		public PrefLevel getPrefLevel(Long id) {
			if (iPrefLevels == null) return null;
			for (PrefLevel level: iPrefLevels)
				if (level.getId().equals(id)) return level;
			return null;
		}
		public PrefLevel getPrefLevel(String code) {
			if (iPrefLevels == null) return null;
			for (PrefLevel level: iPrefLevels)
				if (level.getCode().equals(code)) return level;
			return null;
		}
		public void setPrefLevels(List<PrefLevel> prefLevels) { iPrefLevels = prefLevels; }
		public boolean isHorizontal() { return iHorizontal != null && iHorizontal.booleanValue(); }
		public void setHorizontal(boolean horizontal) { iHorizontal = horizontal; }
		public void setAssignment(int dayCode, int startSlot) { iAssignedDay = dayCode; iAssignedTime = startSlot; }
		public boolean isAssigned(int d, int t) {
			return iAssignedDay == iDays.get(d) && iAssignedTime == iTimes.get(t);
		}
	}
	
	public static class Selection implements IsSerializable {
		private Long iItem;
		private Long iLevel;
		private Integer iRoomIndex;
		
		public Selection() {}
		public Selection(Long item, Long level) {
			iItem = item; iLevel = level;
		}
		
		public Long getItem() { return iItem; }
		public void setItem(Long item) { iItem = item; }
		public Long getLevel() { return iLevel; }
		public void setLevel(Long level) { iLevel = level; }
		public Integer getRoomIndex() { return iRoomIndex; }
		public void setRoomIndex(Integer roomIndex) { iRoomIndex = roomIndex; }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Selection)) return false;
			Selection s = (Selection)o;
			return  PrefGroupEditInterface.equals(iItem, s.iItem) &&
					PrefGroupEditInterface.equals(iLevel, s.iLevel) &&
					PrefGroupEditInterface.equals(getRoomIndex(), s.getRoomIndex());
		}
		
		@Override
		public String toString() {
			return "{ item : " + iItem + ", level : " + iLevel + (iRoomIndex == null ? "" : ", room: " + (iRoomIndex + 1)) + "}";
		}
	}
	
	public static class TimeSelection extends Selection {
		private String iPreference;
		
		public TimeSelection() { super(); }
		public TimeSelection(Long item, Long level, String preference) {
			super(item, level);
			iPreference = preference;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof TimeSelection)) return false;
			TimeSelection s = (TimeSelection)o;
			return  PrefGroupEditInterface.equals(getItem(), s.getItem()) &&
					PrefGroupEditInterface.equals(getLevel(), s.getLevel()) &&
					PrefGroupEditInterface.equals(getPreference(), s.getPreference());
		}
		
		public String getPreference() { return iPreference; }
		public void setPreference(String preference) { iPreference = preference; }
		
		@Override
		public String toString() {
			return "{ item : " + getItem() + ", preference : " + getPreference() + (getRoomIndex() == null ? "" : ", room : " + (1 + getRoomIndex())) + "}";
		}
	}
	
	public static class TimePreferences implements IsSerializable {
		private Long iId;
		private String iType;
		private List<TimePatternModel> iItems;
		private List<TimeSelection> iSelections;
		private boolean iHorizontal = true;
		
		public TimePreferences() {}
		public TimePreferences(Long id, String type) {
			iId = id; iType = type;
		}

		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		public boolean isHorizontal() { return iHorizontal; }
		public void setHorizontal(boolean horizontal) { iHorizontal = horizontal; }
		
		public boolean hasItems() { return iItems != null && !iItems.isEmpty(); }
		public List<TimePatternModel> getItems() { return iItems; }
		public TimePatternModel getItem(Long id) {
			if (iItems == null) return null;
			for (TimePatternModel item: iItems)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public void addItem(TimePatternModel tp) {
			if (iItems == null) iItems = new ArrayList<TimePatternModel>();
			iItems.add(tp);
		}
		public TimePatternModel removeItem(Long id) {
			if (iItems == null) return null;
			for (Iterator<TimePatternModel> i = iItems.iterator(); i.hasNext(); ) {
				TimePatternModel item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return item;
				}
			}
			return null;
		}
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		public boolean hasSelections() { return iSelections != null && !iSelections.isEmpty(); }
		public void clearSelections() {
			if (iSelections != null) iSelections.clear();
		}
		public boolean isEmpty() {
			if (iSelections != null)
				for (Selection s: iSelections) {
					if (s.getLevel() != null) return false;
				}
			return true;
		}
		public List<TimeSelection> getSelections() { return iSelections; }
		public void addSelection(TimeSelection selection) {
			if (iSelections == null) iSelections = new ArrayList<TimeSelection>();
			if (getItem(selection.getItem()) != null)
				iSelections.add(selection);
		}
		public TimeSelection getSelection(Long item) {
			if (iSelections == null) return null;
			for (TimeSelection selection: iSelections)
				if (item.equals(selection.getItem())) return selection;
			return null;
		}
		@Override
		public String toString() {
			return "{ id : " + iId + ", type : " + (iType == null ? "null" : "'" + iType + "'") + ", selections: " + iSelections + "}";
		}
	}
	
	public static enum PreferenceType {
		TIME, DATE,
		ROOM, ROOM_GROUP, ROOM_FEATURE, BUILDING,
		DISTRIBUTION,
		COURSE,
	}
	
	public static class Preferences implements IsSerializable, Comparable<Preferences> {
		private PreferenceType iType;
		private TreeSet<IdLabel> iItems;
		private List<Selection> iSelections;
		private boolean iAllowHard = true;
		
		public Preferences() {}
		public Preferences(PreferenceType type) {
			iType = type;
		}
		public Preferences(Preferences p) {
			iType = p.iType;
			if (p.iItems != null)
				iItems = new TreeSet<IdLabel>(p.iItems);
			if (p.iSelections != null)
				iSelections = new ArrayList<Selection>(p.iSelections);
		}
		
		public PreferenceType getType() { return iType; }
		public void setType(PreferenceType type) { iType = type; }
		public boolean isAllowHard() { return iAllowHard; }
		public void setAllowHard(boolean allowHard) { iAllowHard = allowHard; }
		
		public boolean hasItems() { return iItems != null && !iItems.isEmpty(); }
		public Set<IdLabel> getItems() { return iItems; }
		public IdLabel getItem(Long id) {
			if (iItems == null) return null;
			for (IdLabel item: iItems)
				if (id.equals(item.getId())) return item;
			return null;
		}
		public IdLabel addItem(Long id, String label, String description) {
			if (iItems == null) iItems = new TreeSet<IdLabel>();
			IdLabel item = new IdLabel(id, label, description);
			if (!iItems.contains(item)) {
				iItems.add(item);
				return item;
			}
			return null;
		}
		public IdLabel removeItem(Long id) {
			if (iItems == null) return null;
			for (Iterator<IdLabel> i = iItems.iterator(); i.hasNext(); ) {
				IdLabel item = i.next();
				if (id.equals(item.getId())) {
					i.remove();
					return item;
				}
			}
			return null;
		}
		
		@Override
		public int hashCode() { return getType().hashCode(); }
		@Override
		public int compareTo(Preferences other) {
			return getType().compareTo(other.getType());
		}
		
		public boolean hasSelections() { return iSelections != null && !iSelections.isEmpty(); }
		public void clearSelections() {
			if (iSelections != null) iSelections.clear();
		}
		public boolean isEmpty() {
			if (iSelections != null)
				for (Selection s: iSelections) {
					if (s.getLevel() != null) return false;
				}
			return true;
		}
		public List<Selection> getSelections() { return iSelections; }
		public void addSelection(Selection selection) {
			if (iSelections == null) iSelections = new ArrayList<Selection>();
			if (getItem(selection.getItem()) != null)
				iSelections.add(selection);
		}
		public Selection getSelection(Long item) {
			if (iSelections == null) return null;
			for (Selection selection: iSelections)
				if (item.equals(selection.getItem())) return selection;
			return null;
		}
		
		public Selection hasNoPrefSelection() {
			if (!hasSelections()) return null;
			for (int i = 0; i < getSelections().size() - 1; i ++) {
				Selection a = getSelections().get(i);
				if (a.getItem() == null) continue;
				if (a.getLevel() == null) return a;
			}
			return null;
		}
		
		public Selection hasDuplicateSelection() {
			if (!hasSelections()) return null;
			for (int i = 0; i < getSelections().size() - 1; i ++) {
				Selection a = getSelections().get(i);
				if (a.getItem() == null) continue;
				for (int j = i + 1; j < getSelections().size(); j++) {
					Selection b = getSelections().get(j);
					if (a.getItem().equals(b.getItem()) && PrefGroupEditInterface.equals(a.getRoomIndex(), b.getRoomIndex()))
						return a;
				}
			}
			return null;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Preferences)) return false;
			Preferences original = (Preferences)obj;
			if (!PrefGroupEditInterface.equals(iType, original.iType)) return false;
			int selections = (iSelections == null ? 0 : iSelections.size());
			int originalSelections = (original.iSelections == null ? 0 : original.iSelections.size());
			if (selections != originalSelections) return false; // different number of selections
			if (selections > 0) {
				s: for (Selection s: iSelections) {
					for (Selection o: original.iSelections)
						if (s.equals(o)) continue s;
					return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "{ type : " + (iType == null ? "-" : iType ) + ", selections: " + iSelections + "}";
		}
	}
	
	public static boolean equalsString(String o1, String o2) {
		return (o1 == null ? "" : o1).equals(o2 == null ? "" : o2);
	}
	
	public static boolean equals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
	
	public static class DistributionEditRequest implements GwtRpcRequest<DistributionEditResponse> {
		private Long iPreferenceId, iSubpartId, iClassId;
		private Operation iOperation;
		private DistributionEditResponse iData;
		
		public static enum Operation {
			GET, SAVE, DELETE,
		}
		
		public DistributionEditRequest() {}

		public Long getPreferenceId() { return iPreferenceId; }
		public void setPreferenceId(Long preferenceId) { iPreferenceId = preferenceId; }
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		public DistributionEditResponse getData() { return iData; }
		public void setData(DistributionEditResponse data) { iData = data; }		
	}
	
	public static class DistributionEditResponse implements GwtRpcResponse {
		private Long iPreferenceId;
		private Long iPrefLevelId, iDistTypeId, iStructureId;
		private List<IdLabel> iDistTypes, iStructures, iPrefLevels, iSubjects;
		private List<DistributionObjectInterface> iDistributionObjects;
		private boolean iCanDelete;
		private String iBackUrl, iBackTitle;
		private boolean iConfirms;
		
		public Long getPreferenceId() { return iPreferenceId; }
		public void setPreferenceId(Long preferenceId) { iPreferenceId = preferenceId; }

		public Long getPrefLevelId() { return iPrefLevelId; }
		public void setPrefLevelId(Long id) { iPrefLevelId = id; }
		public void addPrefLevel(Long id, String label, char pref) {
			if (iPrefLevels == null) iPrefLevels = new ArrayList<IdLabel>();
			iPrefLevels.add(new IdLabel(id, label, ""+pref));
		}
		public List<IdLabel> getPrefLevels() { return iPrefLevels; }
		public boolean hasPrefLevels() { return iPrefLevels != null && !iPrefLevels.isEmpty(); }
		public IdLabel getPrefLevel(Long id) {
			if (iPrefLevels == null) return null;
			for (IdLabel item: iPrefLevels)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public Long getDistTypeId() { return iDistTypeId; }
		public void setDistTypeId(Long id) { iDistTypeId = id; }
		public void addDistType(Long id, String label, String description, String allowedPrefs) {
			if (iDistTypes == null) iDistTypes = new ArrayList<IdLabel>();
			IdLabel dt = new IdLabel(id, label, description);
			dt.setAllowedPrefs(allowedPrefs);
			iDistTypes.add(dt);
		}
		public List<IdLabel> getDistTypes() { return iDistTypes; }
		public boolean hasDistTypes() { return iDistTypes != null && !iDistTypes.isEmpty(); }
		public IdLabel getDistType(Long id) {
			if (iDistTypes == null) return null;
			for (IdLabel item: iDistTypes)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public Long getStructureId() { return iStructureId; }
		public void setStructureId(Long id) { iStructureId = id; }
		public void addStructure(int id, String label, String description) {
			if (iStructures == null) iStructures = new ArrayList<IdLabel>();
			iStructures.add(new IdLabel(Long.valueOf(id), label, description));
		}
		public List<IdLabel> getStructures() { return iStructures; }
		public boolean hasStructures() { return iStructures != null && !iStructures.isEmpty(); }
		public IdLabel getStructure(Long id) {
			if (iStructures == null) return null;
			for (IdLabel item: iStructures)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public void addSubject(Long id, String label, String description) {
			if (iSubjects == null) iSubjects = new ArrayList<IdLabel>();
			iSubjects.add(new IdLabel(id, label, description));
		}
		public List<IdLabel> getSubjects() { return iSubjects; }
		public boolean hasSubjects() { return iSubjects != null && !iSubjects.isEmpty(); }
		public IdLabel getSubject(Long id) {
			if (iSubjects == null) return null;
			for (IdLabel item: iSubjects)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public void addDistributionObject(DistributionObjectInterface dist) {
			if (iDistributionObjects == null) iDistributionObjects = new ArrayList<DistributionObjectInterface>();
			iDistributionObjects.add(dist);
		}
		public List<DistributionObjectInterface> getDistributionObjects() { return iDistributionObjects; }
		public boolean hasDistributionObjects() { return iDistributionObjects != null && !iDistributionObjects.isEmpty(); }
		public void setDistributionObjects(List<DistributionObjectInterface> objects) {
			iDistributionObjects = objects;
		}
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
		
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }
		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
	}
	
	public static class DistributionObjectInterface implements IsSerializable {
		private Long iSubjectId, iCourseId, iSubpartId, iClassId;
		private String iSubject, iCourse, iSubpart, iClazz;
		
		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
		
		public String getSubject() { return iSubject ; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getCourse() { return iCourse; }
		public void setCourse(String course) { iCourse = course; }
		public String getSubpart() { return iSubpart; }
		public void setSubpart(String subpart) { iSubpart = subpart; }
		public String getClazz() { return iClazz; }
		public void setClazz(String clazz) { iClazz = clazz; }
		
		public boolean isValid() {
			if (iClassId == null) return false;
			if (iClassId < 0) return iSubpartId != null;
			return true; 
		}
		public String getId() { return iCourseId + ":" + iSubpartId + ":" + iClassId; }
	}
	
	public static class DistributionsLookupCourses implements GwtRpcRequest<GwtRpcResponseList<IdLabel>> {
		private Long iSubjectId;
		public Long getSubjectId() { return iSubjectId; }
		public void setSubjectId(Long subjectId) { iSubjectId = subjectId; }
	}
	
	public static class DistributionsLookupSubparts implements GwtRpcRequest<GwtRpcResponseList<IdLabel>> {
		private Long iCourseId;
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
	}
	
	public static class DistributionsLookupClasses implements GwtRpcRequest<GwtRpcResponseList<IdLabel>> {
		private Long iSubpartId;
		public Long getSubpartId() { return iSubpartId; }
		public void setSubpartId(Long subpartId) { iSubpartId = subpartId; }
	}
}
