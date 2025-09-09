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

public class CourseOfferingInterface implements IsSerializable, Serializable, GwtRpcResponse  {
	private static final long serialVersionUID = 1L;
	private Long iId;
	private String iAbbreviation;
	private String iName;
	private String iExternalId;
	private Boolean iCanEdit;

	private Long iUniqueId;
	private Boolean iIsControl;
	private String iPermId;
	private Integer iProjectedDemand;
	private Integer iNbrExpectedStudents;
	private Integer iDemand;
	private Integer iEnrollment;
	private Integer iReservation;
	private Boolean iByReservationOnly;
	private String iSubjectAreaAbbv;
	private String iCourseNbr;
	private String iTitle;
	private String iScheduleBookNote;
	private String iExternalUniqueId;
	private String iLabel;
	private Integer iLastWeekToEnroll;
	private Integer iLastWeekToChange;
	private Integer iLastWeekToDrop;
	private String iNotes;
	private Long iConsent;
	private String iConsentText;
	private Long iDemandOfferingId;
	private String iDemandOfferingText;
	private Long iAlternativeCourseOfferingId;
	private Long iParentCourseOfferingId;
	private Long iFundingDepartmentId;
	private Long iEffectiveFundingDepartmentId;
	private Long iCourseTypeId;
	private Integer iWaitList;
	private String iCreditFormat;
	private String iCreditText;
	private Long iCreditType;
	private Long iCreditUnitType;
	private Float iUnits;
	private Float iMaxUnits;
	private Boolean iFractionalIncrementsAllowed;
	private Boolean iIoNotOffered;
	private List<CoordinatorInterface> iCoordinators = new ArrayList<CoordinatorInterface>();
	private Set<String> iOverrides;
	private String iCatalogLinkLocation;
	private String iCatalogLinkLabel;
	private String iErrorMessage;

	private List<CoordinatorInterface> iSendCoordinators = new ArrayList<CoordinatorInterface>();
	public void addSendCoordinator(CoordinatorInterface coordinator) { iSendCoordinators.add(coordinator); }
	public List<CoordinatorInterface> getSendCoordinators() { return iSendCoordinators; }
	public void clearInstructors() { iSendCoordinators = new ArrayList<CoordinatorInterface>(); }
	
	public void addCoordinator(CoordinatorInterface coordinator) { iCoordinators.add(coordinator); }
	public List<CoordinatorInterface> getCoordinators() { return iCoordinators; }

	public void addCourseOverride(String override) { iOverrides.add(override); }
    public Set<String> getCourseOverrides() { return iOverrides; }
    public void clearCourseOverrides() { iOverrides = new HashSet<String>(); }

	public String getErrorMessage() { return iErrorMessage; }
	public void setErrorMessage(String errorMessage) { iErrorMessage = errorMessage; }
	
    public String getCatalogLinkLabel() {
		return iCatalogLinkLabel;
	}

	public void setCatalogLinkLabel(String catalogLinkLabel) {
		iCatalogLinkLabel = catalogLinkLabel;
	}

	public String getCatalogLinkLocation() {
		return iCatalogLinkLocation;
	}

	public void setCatalogLinkLocation(String catalogLinkLocation) {
		iCatalogLinkLocation = catalogLinkLocation;
	}
	
	public String getCreditText() {
		return iCreditText;
	}
	public void setCreditText(String creditText) {
		iCreditText = creditText;
	}
	public String getCreditFormat() {
		return iCreditFormat;
	}
	public void setCreditFormat(String creditFormat) {
		iCreditFormat = creditFormat;
	}
	public Long getCreditType() {
		return iCreditType;
	}
	public void setCreditType(Long creditType) {
		iCreditType = creditType;
	}
	public Long getCreditUnitType() {
		return iCreditUnitType;
	}
	public void setCreditUnitType(Long creditUnitType) {
		iCreditUnitType = creditUnitType;
	}
	public Float getUnits() {
		return iUnits;
	}
	public void setUnits(Float units) {
		iUnits = units;
	}
	public Float getMaxUnits() {
		return iMaxUnits;
	}
	public void setMaxUnits(Float maxUnits) {
		iMaxUnits = maxUnits;
	}
	public Boolean getFractionalIncrementsAllowed() {
		return iFractionalIncrementsAllowed;
	}
	public void setFractionalIncrementsAllowed(Boolean fractionalIncrementsAllowed) {
		iFractionalIncrementsAllowed = fractionalIncrementsAllowed;
	}
	
	public String getLabel() { return iLabel; }
	public void setLabel(String label) { iLabel = label; }
	
	public Long getConsent() { return iConsent; }
	public void setConsent(Long consent) { iConsent = consent; }
	
	public String getConsentText() { return iConsentText; }
	public void setConsentText(String consent) { iConsentText = consent; }
	
	public Long getDemandOfferingId() { return iDemandOfferingId; }
	public void setDemandOfferingId(Long demandOfferingId) { iDemandOfferingId = demandOfferingId; }
	
	public String getDemandOfferingText() { return iDemandOfferingText; }
	public void setDemandOfferingText(String demandOfferingText) { iDemandOfferingText = demandOfferingText; }
	
	public Long getAlternativeCourseOfferingId() { return iAlternativeCourseOfferingId; }
	public void setAlternativeCourseOfferingId(Long alternativeCourseOfferingId) { iAlternativeCourseOfferingId = alternativeCourseOfferingId; }
	
	public Long getParentCourseOfferingId() { return iParentCourseOfferingId; }
	public void setParentCourseOfferingId(Long parentCourseOfferingId) { iParentCourseOfferingId = parentCourseOfferingId; }
	
	public Long getFundingDepartmentId() { return iFundingDepartmentId; }
	public void setFundingDepartmentId(Long fundingDepartmentId) { iFundingDepartmentId = fundingDepartmentId; }
	
	public Long getEffectiveFundingDepartmentId() { return iEffectiveFundingDepartmentId; }
	public void setEffectiveFundingDepartmentId(Long effectiveFundingDepartmentId) { iEffectiveFundingDepartmentId = effectiveFundingDepartmentId; }
	
	public Long getCourseTypeId() { return iCourseTypeId; }
	public void setCourseTypeId(Long courseTypeId) { iCourseTypeId = courseTypeId; }
	
	public Integer getWaitList() { return iWaitList; }
	public void setWaitList(Integer waitList) { iWaitList = waitList; }

	private Long iSubjectAreaId;
	public void setSubjectAreaId(Long iSubjectAreaId) {
		this.iSubjectAreaId = iSubjectAreaId;
	}

	public Long getSubjectAreaId() {
		return iSubjectAreaId;
	}
	
	private Long iInstrOfferingId;
	public void setInstrOfferingId(Long iInstrOfferingId) {
		this.iInstrOfferingId = iInstrOfferingId;
	}

	public Long getInstrOfferingId() {
		return iInstrOfferingId;
	}
	
	public void setLastWeekToEnroll(Integer iLastWeekToEnroll) {
		this.iLastWeekToEnroll = iLastWeekToEnroll;
	}

	public Integer getLastWeekToEnroll() {
		return iLastWeekToEnroll;
	}
	
	public void setLastWeekToChange(Integer iLastWeekToChange) {
		this.iLastWeekToChange = iLastWeekToChange;
	}

	public Integer getLastWeekToChange() {
		return iLastWeekToChange;
	}
	
	public void setLastWeekToDrop(Integer iLastWeekToDrop) {
		this.iLastWeekToDrop = iLastWeekToDrop;
	}

	public Integer getLastWeekToDrop() {
		return iLastWeekToDrop;
	}
	
	public void setNotes(String iNotes) {
		this.iNotes = iNotes;
	}

	public String getNotes() {
		return iNotes;
	}

	public Long getUniqueId() {
		return iUniqueId;
	}

	public void setUniqueId(Long iUniqueId) {
		this.iUniqueId = iUniqueId;
	}

	public Boolean getIsControl() {
		return iIsControl;
	}

	public void setIsControl(Boolean iIsControl) {
		this.iIsControl = iIsControl;
	}

	public String getPermId() {
		return iPermId;
	}

	public void setPermId(String iPermId) {
		this.iPermId = iPermId;
	}

	public Integer getProjectedDemand() {
		return iProjectedDemand;
	}

	public void setProjectedDemand(Integer iProjectedDemand) {
		this.iProjectedDemand = iProjectedDemand;
	}

	public Integer getNbrExpectedStudents() {
		return iNbrExpectedStudents;
	}

	public void setNbrExpectedStudents(Integer iNbrExpectedStudents) {
		this.iNbrExpectedStudents = iNbrExpectedStudents;
	}

	public Integer getDemand() {
		return iDemand;
	}

	public void setDemand(Integer iDemand) {
		this.iDemand = iDemand;
	}

	public Integer getEnrollment() {
		return iEnrollment;
	}

	public void setEnrollment(Integer iEnrollment) {
		this.iEnrollment = iEnrollment;
	}

	public Integer getReservation() {
		return iReservation;
	}

	public void setReservation(Integer iReservation) {
		this.iReservation = iReservation;
	}
	
	public Boolean getByReservationOnly() {
		return iByReservationOnly;
	}

	public void setByReservationOnly(Boolean iByReservationOnly) {
		this.iByReservationOnly = iByReservationOnly;
	}

	public String getSubjectAreaAbbv() {
		return iSubjectAreaAbbv;
	}

	public void setSubjectAreaAbbv(String iSubjectAreaAbbv) {
		this.iSubjectAreaAbbv = iSubjectAreaAbbv;
	}

	public String getCourseNbr() {
		return iCourseNbr;
	}

	public void setCourseNbr(String courseNbr) {
		iCourseNbr = courseNbr;
	}

	public String getTitle() {
		return iTitle;
	}

	public void setTitle(String title) {
		iTitle = title;
	}

	public String getScheduleBookNote() {
		return iScheduleBookNote;
	}

	public void setScheduleBookNote(String iScheduleBookNote) {
		this.iScheduleBookNote = iScheduleBookNote;
	}

	public String getExternalUniqueId() {
		return iExternalUniqueId;
	}

	public void setExternalUniqueId(String iExternalUniqueId) {
		this.iExternalUniqueId = iExternalUniqueId;
	}

	public CourseOfferingInterface() {}

	public Boolean getIoNotOffered() {
		return iIoNotOffered;
	}
	public void setIoNotOffered(Boolean ioNotOffered) {
		iIoNotOffered = ioNotOffered;
	}
	
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }
	
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }
	
	public String getCourseName() { return iName; }
	public void setCourseName(String name) { iName = name; }
	
	public boolean hasExternalId() { return iExternalId != null && !iExternalId.isEmpty(); }
	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { iExternalId = externalId; }
	
	public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
	public boolean isCanEdit() { return iCanEdit != null && iCanEdit.booleanValue(); }

	public String toString() { return ((iAbbreviation == null || iAbbreviation.isEmpty() ? "" : iAbbreviation) + (iName == null || iName.isEmpty() ? "" : " " + iName)).trim(); }

	public static class CoordinatorInterface  implements IsSerializable {
		private String iInstructorId;
		private String iResponsibilityId;
		private String iPercShare;
		
		public CoordinatorInterface() {
		}
		
		public String getInstructorId() { return iInstructorId; }
		public void setInstructorId(String instructorId) { iInstructorId = instructorId; }
		
		public String getResponsibilityId() { return iResponsibilityId; }
		public void setResponsibilityId(String responsibilityId) { iResponsibilityId = responsibilityId; }
		
		public String getPercShare() { return iPercShare; }
		public void setPercShare(String percShare) { iPercShare = percShare; }
	}
	
	public static class GetCourseOfferingRequest implements GwtRpcRequest<GetCourseOfferingResponse> {
		private Long iCourseOfferingId = null;
		
		public GetCourseOfferingRequest() {}
		
		public GetCourseOfferingRequest(Long courseOfferingId) {
			iCourseOfferingId = courseOfferingId;
		}
		
		public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }
		public Long getCourseOfferingId() { return iCourseOfferingId; }
	}
	
	public static class GetCourseOfferingResponse implements GwtRpcResponse {

		public GetCourseOfferingResponse() {}

		private CourseOfferingInterface iCourseOffering;
		private Integer wkEnrollDefault;
		private Integer wkChangeDefault;
		private Integer wkDropDefault;
		private String weekStartDayOfWeek;
		
		public void setCourseOffering(CourseOfferingInterface courseOffering) { iCourseOffering = courseOffering; }
		public CourseOfferingInterface getCourseOffering() { return iCourseOffering; }
		
		public Integer getWkEnrollDefault() { return wkEnrollDefault; }
	    public void setWkEnrollDefault(Integer wkEnrollDefault) { this.wkEnrollDefault = wkEnrollDefault; }

	    public Integer getWkChangeDefault() { return wkChangeDefault; }
	    public void setWkChangeDefault(Integer wkChangeDefault) { this.wkChangeDefault = wkChangeDefault; }

	    public Integer getWkDropDefault() { return wkDropDefault; }
	    public void setWkDropDefault(Integer wkDropDefault) { this.wkDropDefault = wkDropDefault; }
	    
	    public String getWeekStartDayOfWeek() { return weekStartDayOfWeek; }
	    public void setWeekStartDayOfWeek(String weekStartDayOfWeek) { this.weekStartDayOfWeek = weekStartDayOfWeek; }
		
	}
	
	public static class CourseOfferingPropertiesRequest implements GwtRpcRequest<CourseOfferingPropertiesInterface> {
		private Long iSessionId = null;
		private Boolean iIsEdit = null;
		private Long iSubjAreaId = null;
		private Long iCourseOfferingId = null;
		private String iCourseNumber = null;
		
		public CourseOfferingPropertiesRequest() {}
		
		public CourseOfferingPropertiesRequest(Boolean isEdit, Long subjAreaId) {
			iIsEdit = isEdit;
			iSubjAreaId = subjAreaId;
		}
		
		public CourseOfferingPropertiesRequest(Boolean isEdit, Long subjAreaId, String courseOfferingId) {
			if (!isEdit) {
				iCourseNumber = courseOfferingId;
			} else {
				iCourseOfferingId = Long.valueOf(courseOfferingId);
			}
			iIsEdit = isEdit;
			iSubjAreaId = subjAreaId;
		}
		
		public boolean hasSessionId() { return iSessionId != null; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public Long getSubjAreaId() { return iSubjAreaId; }
		public void setSubjAreaId(Long subjAreaId) { iSubjAreaId = subjAreaId; }
		
		public Long getCourseOfferingId() { return iCourseOfferingId; }
		public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }
		
		public String getCourseNumber() { return iCourseNumber; }
		public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }
		
		public Boolean getIsEdit() { return iIsEdit; }
		public void setIsEdit(Boolean isEdit) { iIsEdit = isEdit; }
		
		@Override
		public String toString() { return (hasSessionId() ? getSessionId().toString() : ""); }
	}
	
	public static class CourseOfferingPropertiesInterface implements GwtRpcResponse {
		private AcademicSessionInterface iSession = null;
		private List<SubjectAreaInterface> iSubjectAreas = new ArrayList<SubjectAreaInterface>();
		private List<CourseCreditFormatInterface> iCourseCreditFormats = new ArrayList<CourseCreditFormatInterface>();
		private List<CourseCreditTypeInterface> iCourseCreditTypes = new ArrayList<CourseCreditTypeInterface>();
		private List<CourseCreditUnitTypeInterface> iCourseCreditUnitTypes = new ArrayList<CourseCreditUnitTypeInterface>();
		private List<CourseOfferingInterface> iCourseDemands = new ArrayList<CourseOfferingInterface>();
		private List<CourseOfferingInterface> iAltCourseOfferings = new ArrayList<CourseOfferingInterface>();
		private List<CourseTypeInterface> iCourseTypes = new ArrayList<CourseTypeInterface>();
		private List<OverrideTypeInterface> iOverrideTypes = new ArrayList<OverrideTypeInterface>();
		private List<WaitListInterface> iWaitLists = new ArrayList<WaitListInterface>();
		private List<OfferingConsentTypeInterface> iOfferingConsentTypes = new ArrayList<OfferingConsentTypeInterface>();
		private List<ResponsibilityInterface> iResponsibilities = new ArrayList<ResponsibilityInterface>();
		private List<DepartmentInterface> iFundingDepartments = new ArrayList<DepartmentInterface>();
		private List<CourseOfferingInterface> iParentCourseOfferings = new ArrayList<CourseOfferingInterface>();
		private String iCourseNbrRegex;
		private String iCourseNbrInfo;
		private Boolean iCourseOfferingMustBeUnique;
		private Boolean iCourseOfferingNumberUpperCase;
		private Boolean iAllowAlternativeCourseOfferings;
		private Boolean iCoursesFundingDepartmentsEnabled;
		private Boolean iCanEditExternalIds;
		private Boolean iCanShowExternalIds;
		private Integer iWaitListDefault;
		private Integer wkEnrollDefault;
		private Integer wkChangeDefault;
		private Integer wkDropDefault;
		private String weekStartDayOfWeek;
		private Integer prefRowsAdded;
		private String iCourseUrlProvider;
		private String iInstructionalOfferingId;
		private String iDefaultTeachingResponsibilityId;
		private Long iSubjectAreaEffectiveFundingDept;
		private Boolean iAllowParentCourseOfferings;

		private List<InstructorInterface> iInstructors = new ArrayList<InstructorInterface>();
		
		public void addInstructor(InstructorInterface instructor) { iInstructors.add(instructor); }
		public List<InstructorInterface> getInstructors() { return iInstructors; }

		public void addFundingDepartment(DepartmentInterface fundingDepartment) { iFundingDepartments.add(fundingDepartment); }
		public List<DepartmentInterface> getFundingDepartments() { return iFundingDepartments; }
		public void setFundingDepartments(List<DepartmentInterface> fundingDepartments) { iFundingDepartments = fundingDepartments; }

		public void addSubjectArea(SubjectAreaInterface subjectArea) { iSubjectAreas.add(subjectArea); }
		public List<SubjectAreaInterface> getSubjectAreas() { return iSubjectAreas; }
		public void setSubjectAreas(List<SubjectAreaInterface> subjectAreas) { iSubjectAreas = subjectAreas; }
		
		public void setCoursesFundingDepartmentsEnabled(Boolean coursesFundingDepartmentsEnabled) { iCoursesFundingDepartmentsEnabled = coursesFundingDepartmentsEnabled; }
		public Boolean getCoursesFundingDepartmentsEnabled() { return iCoursesFundingDepartmentsEnabled; }
		
		public void addCourseCreditFormat(CourseCreditFormatInterface courseCreditFormat) { iCourseCreditFormats.add(courseCreditFormat); }
		public List<CourseCreditFormatInterface> getCourseCreditFormats() { return iCourseCreditFormats; }
		
		public void addCourseCreditType(CourseCreditTypeInterface courseCreditType) { iCourseCreditTypes.add(courseCreditType); }
		public List<CourseCreditTypeInterface> getCourseCreditTypes() { return iCourseCreditTypes; }
		
		public void addCourseCreditUnitType(CourseCreditUnitTypeInterface courseCreditUnitType) { iCourseCreditUnitTypes.add(courseCreditUnitType); }
		public List<CourseCreditUnitTypeInterface> getCourseCreditUnitTypes() { return iCourseCreditUnitTypes; }
		
		public void addOfferingConsentType(OfferingConsentTypeInterface offeringConsentType) { iOfferingConsentTypes.add(offeringConsentType); }
		public List<OfferingConsentTypeInterface> getOfferingConsentTypes() { return iOfferingConsentTypes; }
		
		public void addResponsibility(ResponsibilityInterface responsibility) { iResponsibilities.add(responsibility); }
		public List<ResponsibilityInterface> getResponsibilities() { return iResponsibilities; }
		
		public void addCourseDemands(CourseOfferingInterface courseDemands) { iCourseDemands.add(courseDemands); }
		public List<CourseOfferingInterface> getCourseDemands() { return iCourseDemands; }
		
		public void addAltCourseOffering(CourseOfferingInterface altCourseOffering) { iAltCourseOfferings.add(altCourseOffering); }
		public List<CourseOfferingInterface> getAltCourseOfferings() { return iAltCourseOfferings; }
		
		public void addCourseType(CourseTypeInterface courseType) { iCourseTypes.add(courseType); }
		public List<CourseTypeInterface> getCourseTypes() { return iCourseTypes; }
		
		public void addOverrideType(OverrideTypeInterface overrideType) { iOverrideTypes.add(overrideType); }
		public List<OverrideTypeInterface> getOverrideTypes() { return iOverrideTypes; }
		
		public void addWaitList(WaitListInterface waitListItem) { iWaitLists.add(waitListItem); }
		public List<WaitListInterface> getWaitLists() { return iWaitLists; }

		public AcademicSessionInterface getAcademicSession() { return iSession; }
		public void setAcademicSession(AcademicSessionInterface session) { iSession = session; }
		public Long getAcademicSessionId() { return (iSession == null ? null : iSession.getId()); }
		public String getAcademicSessionName() { return (iSession == null ? null : iSession.getLabel()); }
		
		public void setCourseNbrRegex(String courseNbrRegex) { iCourseNbrRegex = courseNbrRegex; }
		public String getCourseNbrRegex() { return iCourseNbrRegex; }
		
		public void setCourseNbrInfo(String courseNbrInfo) { iCourseNbrInfo = courseNbrInfo; }
		public String getCourseNbrInfo() { return iCourseNbrInfo; }
		
		public void setCourseOfferingMustBeUnique(Boolean courseOfferingMustBeUnique) { iCourseOfferingMustBeUnique = courseOfferingMustBeUnique; }
		public Boolean getCourseOfferingMustBeUnique() { return iCourseOfferingMustBeUnique; }

		public void setCourseOfferingNumberUpperCase(Boolean courseOfferingNumberUpperCase) { iCourseOfferingNumberUpperCase = courseOfferingNumberUpperCase; }
		public Boolean getCourseOfferingNumberUpperCase() { return iCourseOfferingNumberUpperCase; }
		
		public void setAllowAlternativeCourseOfferings(Boolean allowAlternativeCourseOfferings) { iAllowAlternativeCourseOfferings = allowAlternativeCourseOfferings; }
		public Boolean getAllowAlternativeCourseOfferings() { return iAllowAlternativeCourseOfferings; }
		
		public void setCourseUrlProvider(String courseUrlProvider) { iCourseUrlProvider = courseUrlProvider; }
		public String getCourseUrlProvider() { return iCourseUrlProvider; }
		
		public void setCanEditExternalIds(Boolean canEditExternalIds) { iCanEditExternalIds = canEditExternalIds; }
		public Boolean getCanEditExternalIds() { return iCanEditExternalIds; }
		
		public void setWaitListDefault(Integer waitListDefault) { iWaitListDefault = waitListDefault; }
		public Integer getWaitListDefault() { return iWaitListDefault; }

		public void setCanShowExternalIds(Boolean canShowExternalIds) { iCanShowExternalIds = canShowExternalIds; }
		public Boolean getCanShowExternalIds() { return iCanShowExternalIds; }

	    public Integer getWkEnrollDefault() { return wkEnrollDefault; }
	    public void setWkEnrollDefault(Integer wkEnrollDefault) { this.wkEnrollDefault = wkEnrollDefault; }

	    public Integer getWkChangeDefault() { return wkChangeDefault; }
	    public void setWkChangeDefault(Integer wkChangeDefault) { this.wkChangeDefault = wkChangeDefault; }

	    public Integer getWkDropDefault() { return wkDropDefault; }
	    public void setWkDropDefault(Integer wkDropDefault) { this.wkDropDefault = wkDropDefault; }
	    
	    public String getWeekStartDayOfWeek() { return weekStartDayOfWeek; }
	    public void setWeekStartDayOfWeek(String weekStartDayOfWeek) { this.weekStartDayOfWeek = weekStartDayOfWeek; }
	    
	    public Integer getPrefRowsAdded() { return prefRowsAdded; }
	    public void setPrefRowsAdded(Integer prefRowsAdded) { this.prefRowsAdded = prefRowsAdded; }
	    
	    public String getInstructionalOfferingId() { return iInstructionalOfferingId; }
	    public void setInstructionalOfferingId(String instructionalOfferingId) { iInstructionalOfferingId = instructionalOfferingId; }
	    
	    public String getDefaultTeachingResponsibilityId() { return iDefaultTeachingResponsibilityId; }
	    public void setDefaultTeachingResponsibilityId(String defaultTeachingResponsibilityId) { iDefaultTeachingResponsibilityId = defaultTeachingResponsibilityId; }
	    
	    public Long getSubjectAreaEffectiveFundingDept() { return iSubjectAreaEffectiveFundingDept; }
	    public void setSubjectAreaEffectiveFundingDept(Long subjectAreaEffectiveFundingDept) { iSubjectAreaEffectiveFundingDept = subjectAreaEffectiveFundingDept; }
	    
		public void setAllowParentCourseOfferings(Boolean allowParentCourseOfferings) { iAllowParentCourseOfferings = allowParentCourseOfferings; }
		public Boolean getAllowParentCourseOfferings() { return iAllowParentCourseOfferings; }
		
		public void addParentCourseOffering(CourseOfferingInterface altCourseOffering) { iParentCourseOfferings.add(altCourseOffering); }
		public List<CourseOfferingInterface> getParenCourseOfferings() { return iParentCourseOfferings; }
	}
	
	public static class SubjectAreaInterface implements GwtRpcResponse {
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
			if (o == null || !(o instanceof SubjectAreaInterface)) return false;
			return getId().equals(((SubjectAreaInterface)o).getId());
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

	public static class DepartmentInterface implements GwtRpcResponse {
		private Long iId;
		private String iAbbv;
		private String iLabel;
		
		public DepartmentInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getAbbreviation() { return iAbbv; }
		public void setAbbreviation(String abbv) { iAbbv = abbv; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof SubjectAreaInterface)) return false;
			return getId().equals(((SubjectAreaInterface)o).getId());
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

	public static class CourseCreditFormatInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		private String iReference;

		public CourseCreditFormatInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		public String getReference() {
			return iReference;
		}

		public void setReference(String reference) {
			iReference = reference;
		}
	}
	
	public static class CourseCreditTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public CourseCreditTypeInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	
	public static class InstructorInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public InstructorInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	
	public static class CourseTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public CourseTypeInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	
	public static class OverrideTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iReference;
		private String iName;
		
		public OverrideTypeInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
	}
	
	public static class WaitListInterface implements GwtRpcResponse {
		private Long iId;
		private String iValue;
		private String iLabel;
		
		public WaitListInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	
	public static class CourseCreditUnitTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public CourseCreditUnitTypeInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	
	public static class OfferingConsentTypeInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public OfferingConsentTypeInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	
	public static class ResponsibilityInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public ResponsibilityInterface() {
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
	}
	

	public static enum UpdateCourseOfferingAction implements IsSerializable {
		CREATE, UPDATE;
	}
	
	public static class UpdateCourseOfferingRequest implements GwtRpcRequest<CourseOfferingInterface> {
		private UpdateCourseOfferingAction iAction;
		private CourseOfferingInterface iCourseOffering;
		
		public UpdateCourseOfferingRequest() {}
		
		public UpdateCourseOfferingAction getAction() { return iAction; }
		public void setAction(UpdateCourseOfferingAction action) { iAction = action; }

		public CourseOfferingInterface getCourseOffering() { return iCourseOffering; }
		public void setCourseOffering(CourseOfferingInterface courseOffering) { iCourseOffering = courseOffering; }
	}
	
	public static class AcademicSessionInterface implements GwtRpcResponse {
		private Long iId;
		private String iLabel;
		
		public AcademicSessionInterface() {}
		
		public AcademicSessionInterface(Long id, String label) {
			iId = id; iLabel = label;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }

		@Override
		public boolean equals(Object object) {
			if (object == null || !(object instanceof AcademicSessionInterface)) return false;
			return getId().equals(((AcademicSessionInterface)object).getId());
		}
	}

	public static class CourseOfferingPermissionsInterface implements GwtRpcResponse {
		private Boolean iCanAddCourseOffering;
		private Boolean iCanEditCourseOffering;
		private Boolean iCanEditCourseOfferingNote;
		private Boolean iCanEditCourseOfferingCoordinators;
		
		public CourseOfferingPermissionsInterface() {
		}
		
		public Boolean getCanAddCourseOffering() { return iCanAddCourseOffering; }
		public void setCanAddCourseOffering(Boolean canAddCourseOffering) { iCanAddCourseOffering = canAddCourseOffering; }
		
		public Boolean getCanEditCourseOffering() { return iCanEditCourseOffering; }
		public void setCanEditCourseOffering(Boolean canEditCourseOffering) { iCanEditCourseOffering = canEditCourseOffering; }
		
		public Boolean getCanEditCourseOfferingNote() { return iCanEditCourseOfferingNote; }
		public void setCanEditCourseOfferingNote(Boolean canEditCourseOfferingNote) { iCanEditCourseOfferingNote = canEditCourseOfferingNote; }
		
		public Boolean getCanEditCourseOfferingCoordinators() { return iCanEditCourseOfferingCoordinators; }
		public void setCanEditCourseOfferingCoordinators(Boolean canEditCourseOfferingCoordinators) { iCanEditCourseOfferingCoordinators = canEditCourseOfferingCoordinators; }
	}

	public static class CourseOfferingCheckPermissions implements GwtRpcRequest<CourseOfferingPermissionsInterface> {
		private Long iCourseOfferingId;
		private Long iSubjAreaId;
		
		public CourseOfferingCheckPermissions() {}
		public CourseOfferingCheckPermissions(Long courseOfferingId, Long subjectAreaId) { 
			iCourseOfferingId = courseOfferingId;
			iSubjAreaId = subjectAreaId;
		}
		
		public Long getCourseOfferingId() { return iCourseOfferingId; }
		public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }
		
		public Long getSubjectAreaId() { return iSubjAreaId; }
		public void setSubjectAreaId(Long subjectAreaId) { iSubjAreaId = subjectAreaId; }
	}
	
	public static class CourseOfferingCheckExists implements GwtRpcRequest<CourseOfferingCheckExistsInterface> {
		private Long iSubjectAreaId;
		private String iCourseNumber;
		private Boolean iIsEdit;
		private Long iCourseOfferingId;
		
		public CourseOfferingCheckExists() {}
		
		public Long getSubjectAreaId() { return iSubjectAreaId; }
		public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }
		
		public String getCourseNumber() { return iCourseNumber; }
		public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }
		
		public Boolean getIsEdit() { return iIsEdit; }
		public void setIsEdit(Boolean isEdit) { iIsEdit = isEdit; }
		
		public Long getCourseOfferingId() { return iCourseOfferingId; }
		public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }
	}
	
	public static class CourseOfferingCheckExistsInterface implements GwtRpcResponse {
		private String iResponseText;
		
		public CourseOfferingCheckExistsInterface() {
		}
		
		public String getResponseText() { return iResponseText; }
		public void setResponseText(String responseText) { iResponseText = responseText; }		
	}
}
