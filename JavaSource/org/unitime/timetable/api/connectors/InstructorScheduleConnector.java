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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.cpsolver.coursett.model.TimeLocation;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

/**
 * @author Tomas Muller
 */
@Service("/api/instructor-schedule")
public class InstructorScheduleConnector extends ApiConnector{

	@Override
	public void doGet(ApiHelper helper) throws IOException {
		String externalId = helper.getParameter("id");
		if (externalId == null)
			throw new IllegalArgumentException("Parameter ID not provided");
		if (ApplicationProperty.ApiTrimLeadingZerosFromUserExternalIds.isTrue())
			while (externalId.startsWith("0")) externalId = externalId.substring(1);

		boolean includePrefs = "1".equalsIgnoreCase(helper.getParameter("pref")) || "true".equalsIgnoreCase(helper.getParameter("pref"));
		boolean allTerms = "1".equalsIgnoreCase(helper.getParameter("all")) || "true".equalsIgnoreCase(helper.getParameter("all"));

		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null || allTerms) {
			Map<Long, InstructorScheduleInfo> responses = new HashMap<Long, InstructorScheduleInfo>();
			for (DepartmentalInstructor di: (List<DepartmentalInstructor>)helper.getHibSession().createQuery(
					"from DepartmentalInstructor d where d.externalUniqueId = :externalId")
					.setString("externalId", externalId).setCacheable(true).list()) {
				Long sid = di.getDepartment().getSessionId();
				InstructorScheduleInfo response = responses.get(sid);
				if (response == null) {
					if (!helper.getSessionContext().hasPermissionAnyAuthority(sid, "Session", Right.ApiRetrieveInstructorSchedule)) continue;
					response = new InstructorScheduleInfo(di);
					responses.put(sid, response);
				}
				response.add(di, helper.getSessionContext().hasPermissionAnyAuthority(sid, "Session", Right.HasRole), includePrefs);
			}

			helper.setResponse(responses.values());
		} else {
			helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveInstructorSchedule);
			
			boolean checkStatus = !helper.getSessionContext().hasPermission(Right.HasRole);
			InstructorScheduleInfo response = null;
			for (DepartmentalInstructor di: (List<DepartmentalInstructor>)helper.getHibSession().createQuery(
					"from DepartmentalInstructor d where d.externalUniqueId = :externalId and department.session.uniqueId = :sessionId")
					.setString("externalId", externalId)
					.setLong("sessionId", sessionId).setCacheable(true).list()) {
				if (response == null) response = new InstructorScheduleInfo(di);
				response.add(di, checkStatus, includePrefs);
			}

			helper.setResponse(response);
		}
	}
	
	class InstructorScheduleInfo {
		String iExternalId;
		SessionInfo iSession;
		List<InstructorInfo> iInstructors = new ArrayList<InstructorInfo>();
		List<ClassInfo> iClasses = new ArrayList<ClassInfo>();
		List<CourseInfo> iCourses = new ArrayList<CourseInfo>();
		List<ExamInfo> iExams = new ArrayList<ExamInfo>();
		
		InstructorScheduleInfo(DepartmentalInstructor instructor) {
			iExternalId = instructor.getExternalUniqueId();
			iSession = new SessionInfo(instructor.getDepartment().getSession());
		}
		
		void add(DepartmentalInstructor instructor, boolean statusCheck, boolean includePrefs) {
			iInstructors.add(new InstructorInfo(instructor, includePrefs));
			if (!statusCheck || instructor.getDepartment().getSession().canNoRoleReportClass()) {
				for (ClassInstructor ci: instructor.getClasses())
					if (!ci.getClassInstructing().isCancelled())
						iClasses.add(new ClassAssignmentInfo(ci));
				for (OfferingCoordinator oc: instructor.getOfferingCoordinators())
					for (CourseOffering co: oc.getOffering().getCourseOfferings())
						if (!co.getInstructionalOffering().isNotOffered())
							iCourses.add(new CourseInfo(co, null, oc.getResponsibility()));
			}
			for (Exam exam: instructor.getExams()) {
				if (!statusCheck || exam.canView())
					iExams.add(new ExamInfo(exam));
			}
		}
	}
	
	class SessionInfo {
		Long iSessionId;
		String iReference;
		String iYear, iTerm, iCampus;
		
		SessionInfo(Session session) {
			iSessionId = session.getUniqueId();
			iReference = session.getReference();
			iYear = session.getAcademicYear();
			iTerm = session.getAcademicTerm();
			iCampus = session.getAcademicInitiative();
		}
	}
	
	class InstructorInfo {
		Long iInstructorId;
		String iExternalId;
		String iFirstName;
		String iMiddleName;
		String iLastName;
		String iTitle;
		PositionInfo iPosition;
		String iEmail;
		DepartmentInfo iDepartment;
		String iAcademicTitle;
		String iNote;
		List<PreferenceInfo> iPreferences;
		List<TimePrefInfo> iAvailabilities;
		String iTeachingPreference;
		Float iMaxLoad;
		Boolean iIgnoreDistanceConflicts;
		
		InstructorInfo(DepartmentalInstructor instructor, boolean includePrefs) {
			iInstructorId = instructor.getUniqueId();
			iExternalId = instructor.getExternalUniqueId();
			iFirstName = instructor.getFirstName();
			iMiddleName = instructor.getMiddleName();
			iLastName = instructor.getLastName();
			iTitle = instructor.getAcademicTitle();
			if (instructor.getPositionType() != null)
				iPosition = new PositionInfo(instructor.getPositionType());
			iEmail = instructor.getEmail();
			iDepartment = new DepartmentInfo(instructor.getDepartment());
			iAcademicTitle = instructor.getAcademicTitle();
			if (includePrefs) {
				iNote = instructor.getNote();
				if (instructor.getTeachingPreference() != null)
					iTeachingPreference = instructor.getTeachingPreference().getPrefProlog();
				iIgnoreDistanceConflicts = instructor.isIgnoreToFar();
				iMaxLoad = instructor.getMaxLoad();
				iPreferences = new ArrayList<PreferenceInfo>();
				iAvailabilities = new ArrayList<TimePrefInfo>();
				for (Preference preference: instructor.getPreferences()) {
					if (preference instanceof TimePref) {
						TimePatternModel model = ((TimePref)preference).getTimePatternModel();
						for (int d = 0; d < model.getNrDays(); d++) {
							int start = -1; String pref = null;
							for (int t = 0; t < model.getNrTimes(); t++) {
								String p = model.getPreference(d, t);
								if (pref == null || !pref.equals(p)) {
									if (pref != null && !pref.equals(PreferenceLevel.sNeutral)) {
										int end = model.getStartSlot(t - 1);
										iAvailabilities.add(new TimePrefInfo(d, start, 1 + end, pref));
									}
									start = model.getStartSlot(t); pref = p;
								}
							}
							if (pref != null && !pref.equals(PreferenceLevel.sNeutral)) {
								int end = model.getStartSlot(model.getNrTimes() - 1);
								iAvailabilities.add(new TimePrefInfo(d, start, 1 + end, pref));
							}
						}
					} else if (preference instanceof RoomPref) {
						iPreferences.add(new PreferenceInfo((RoomPref)preference));
					} else if (preference instanceof BuildingPref) {
						iPreferences.add(new PreferenceInfo((BuildingPref)preference));
					} else if (preference instanceof RoomGroupPref) {
						iPreferences.add(new PreferenceInfo((RoomGroupPref)preference));
					} else if (preference instanceof RoomFeaturePref) {
						iPreferences.add(new PreferenceInfo((RoomFeaturePref)preference));
					} else if (preference instanceof DistributionPref) {
						iPreferences.add(new PreferenceInfo((DistributionPref)preference));
					} else if (preference instanceof InstructorAttributePref) {
						iPreferences.add(new PreferenceInfo((InstructorAttributePref)preference));
					} else if (preference instanceof InstructorCoursePref) {
						iPreferences.add(new PreferenceInfo((InstructorCoursePref)preference));
					}
				}
			}
		}

	}
	
	class DepartmentInfo {
		String iCode;
		String iName;
		DepartmentInfo(Department department) {
			iCode = department.getDeptCode();
			iName = department.getName();
		}
	}
	
	class PositionInfo {
		String iReference;
		String iLabel;
		
		PositionInfo(PositionType type) {
			iReference = type.getReference();
			iLabel = type.getLabel();
		}
	}
	
	class ClassAssignmentInfo extends ClassInfo {
		Boolean iLead;
		Integer iPercentShare;
		String iResponsibility;
		
		ClassAssignmentInfo(ClassInstructor ci) {
			super(ci.getClassInstructing());
			iLead = ci.getLead();
			iPercentShare = ci.getPercentShare();
			if (ci.getResponsibility() != null)
				iResponsibility = ci.getResponsibility().getReference();
		}
	}
	
	class CourseInfo {
		Long iCourseId;
		String iSubjectArea;
		String iCourseNumber;
		String iCourseTitle;
		boolean iControl;
		String iType;
		String iClassSuffix;
		String iClassExternalId;
		String iCredit;
		String iNote;
		String iResponsibility;
		
		CourseInfo(CourseOffering co, Class_ clazz, TeachingResponsibility responsibility) {
			iCourseId = co.getUniqueId();
			iSubjectArea = co.getSubjectAreaAbbv();
			iCourseNumber = co.getCourseNbr();
			iCourseTitle = co.getTitle();
			iControl = co.isIsControl();
			if (co.getCourseType() != null)
				iType = co.getCourseType().getReference();
			if (clazz != null) {
				iClassSuffix = clazz.getClassSuffix(co);
				iClassExternalId = clazz.getExternalId(co);
			}
			if (co.getCredit() != null)
				iCredit = co.getCredit().creditAbbv();
			iNote = co.getScheduleBookNote();
			if (responsibility != null)
				iResponsibility = responsibility.getReference();
		}
	}
	
	class ClassInfo {
		List<CourseInfo> iCourse = new ArrayList<CourseInfo>();
		Long iClassId;
		String iSubpart;
		String iSectionNumber;
		String iNote;
		Integer iLimit;
		List<AssignmentInfo> iMeetings;
		
		ClassInfo(Class_ clazz) {
			iClassId = clazz.getUniqueId();
			iSectionNumber = clazz.getSectionNumberString();
			iSubpart = clazz.getSchedulingSubpart().getItypeDesc().trim();
			iNote = clazz.getSchedulePrintNote();
			for (CourseOffering course: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings())
				iCourse.add(new CourseInfo(course, clazz, null));
			Assignment assignment = clazz.getCommittedAssignment();
			int minLimit = clazz.getExpectedCapacity();
        	int maxLimit = clazz.getMaxExpectedCapacity();
        	int limit = maxLimit;
            if (assignment != null) {
            	iMeetings = new ArrayList<AssignmentInfo>();
            	for (int i = 0; i < Constants.DAY_CODES.length; i++) {
					if ((assignment.getDays() & Constants.DAY_CODES[i]) != 0) {
						if (assignment.getRooms().isEmpty()) {
							iMeetings.add(new AssignmentInfo(assignment.getTimeLocation(), assignment.getDatePattern(), i, null));
						} else {
							for (Location location: assignment.getRooms())
								iMeetings.add(new AssignmentInfo(assignment.getTimeLocation(), assignment.getDatePattern(), i, location));
						}
					}
            	}
            	if (minLimit < maxLimit) {
	        		int roomLimit = (int) Math.floor(assignment.getPlacement().getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
	        		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
	        	}
			}
            if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
            if (limit >= 0) iLimit = limit;
		}
	}
	
	protected class AssignmentInfo {
		String iDayOfWeek;
		String iStartTime;
		String iEndTime;
		String iDatePattern;
		String iFirstDate;
		String iLastDate;
		String iBuilding;
		String iRoomNumber;
		String iLocation;
		String iRoomType;
		Integer iRoomCapacity;
		
		AssignmentInfo(TimeLocation time, DatePattern dp, int day, Location room) {
			if (time != null) {
				iStartTime = time.getStartTimeHeader(false);
				iEndTime = time.getEndTimeHeader(false);
				iDayOfWeek = Constants.DAY_NAMES_FULL[day];
				iDatePattern = time.getDatePatternName();
				Date firstDate = firstDate(dp, Constants.DAY_CODES[day]);
				if (firstDate != null)
					iFirstDate = new SimpleDateFormat("yyyy-MM-dd").format(firstDate);
				Date lastDate = lastDate(dp, Constants.DAY_CODES[day]);
				if (lastDate != null)
					iLastDate = new SimpleDateFormat("yyyy-MM-dd").format(lastDate);
			}
			if (room != null) {
				if (room instanceof Room) {
					iBuilding = ((Room)room).getBuildingAbbv();
					iRoomNumber = ((Room)room).getRoomNumber();
				} else {
					iLocation = room.getLabel();
				}
				iRoomType = room.getRoomTypeLabel();
				iRoomCapacity = room.getCapacity();
			}
		}
	}
	
	protected class ExamInfo {
		Long iExamId;
		String iName;
		String iType;
		Integer iSize;
		Integer iLength;
		Integer iPrintOffset;
		List<ExamOwnerInfo> iOwners = new ArrayList<ExamOwnerInfo>();
		PeriodInfo iPeriod;
		List<RoomInfo> iRoom;
		
		ExamInfo(Exam exam) {
			iExamId = exam.getUniqueId();
			iName = exam.getName();
			iType = exam.getExamType().getReference();
			for (ExamOwner owner: exam.getOwners())
				iOwners.add(new ExamOwnerInfo(owner));
			iSize = exam.getSize();
			iLength = exam.getLength();
			iPrintOffset = exam.getPrintOffset();
			if (exam.getAssignedPeriod() != null)
				iPeriod = new PeriodInfo(exam.getAssignedPeriod());
			for (Location room: exam.getAssignedRooms()) {
				if (iRoom == null)
					iRoom = new ArrayList<RoomInfo>();
				iRoom.add(new RoomInfo(room));
			}
		}
	}
	
	protected class ExamOwnerInfo {
		String iType;
		Long iCourseId;
		Long iOfferingId;
		Long iConfigurationId;
		Long iClassId;
		String iSubjectArea;
		String iCourseNumber;
		String iCourseTitle;
		String iClassSuffix;
		String iClassExternalId;
		String iSubpart;
		String iSectionNumber;	
		String iConfiguration;
		
		public ExamOwnerInfo(ExamOwner owner) {
			switch (owner.getOwnerType()) {
			case ExamOwner.sOwnerTypeClass: iType = "Class"; iClassId = owner.getOwnerId(); break;
			case ExamOwner.sOwnerTypeConfig: iType = "Config"; iConfigurationId = owner.getOwnerId(); break;
			case ExamOwner.sOwnerTypeCourse: iType = "Course"; iCourseId = owner.getOwnerId(); break;
			case ExamOwner.sOwnerTypeOffering: iType = "Offering"; iOfferingId = owner.getOwnerId(); break;
			}
			iSubjectArea = owner.getCourse().getSubjectAreaAbbv();
			iCourseNumber = owner.getCourse().getCourseNbr();
			iCourseTitle = owner.getCourse().getTitle();
			if (owner.getOwnerObject() instanceof Class_) {
				Class_ clazz = (Class_)owner.getOwnerObject();
				iClassId = clazz.getUniqueId();
				iClassSuffix = clazz.getClassSuffix(owner.getCourse());
				iClassExternalId = clazz.getExternalId(owner.getCourse());
				iSubpart = clazz.getSchedulingSubpart().getItypeDesc().trim();
				iSectionNumber = clazz.getSectionNumberString();
			} else if (owner.getOwnerObject() instanceof InstrOfferingConfig) {
				iConfiguration = ((InstrOfferingConfig)owner.getOwnerObject()).getName();
				iConfigurationId = owner.getOwnerId();
			}
		}
	}
	
	protected class PeriodInfo {
		String iDate;
		String iStartTime;
		String iEndTime;
		
		PeriodInfo(ExamPeriod period) {
			iDate = new SimpleDateFormat("yyyy-MM-dd").format(period.getStartDate());
			int start = period.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
			int sh = start / 60;
			int sm = start % 60;
			iStartTime = sh + ":" + (sm < 10 ? "0" : "") + sm;
			int end = start + (Constants.SLOT_LENGTH_MIN * period.getLength());
			int eh = end / 60;
			int em = end % 60;
			iEndTime = eh + ":" + (em < 10 ? "0" : "") + em;
		}
	}
	
	protected class RoomInfo {
		String iBuilding;
		String iRoomNumber;
		String iLocation;
		String iRoomType;
		Integer iRoomCapacity;
		
		RoomInfo(Location room) {
			if (room instanceof Room) {
				iBuilding = ((Room)room).getBuildingAbbv();
				iRoomNumber = ((Room)room).getRoomNumber();
			} else {
				iLocation = room.getLabel();
			}
			iRoomType = room.getRoomTypeLabel();
			iRoomCapacity = room.getCapacity();
		}
	}
	
	class PreferenceInfo {
		String iType;
		String iPreference;
		String iPreferenceText;
		String iReference;
		String iValue;
		String iExternalId;
		
		PreferenceInfo(RoomPref preference) {
			iType = "room";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iReference = preference.getRoom().getLabel();
			iValue = preference.getRoom().getDisplayName();
			if (iValue == null) iValue = preference.getRoom().getLabel();
			iExternalId = preference.getRoom().getExternalUniqueId();
		}
		PreferenceInfo(BuildingPref preference) {
			iType = "building";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iValue = preference.getBuilding().getName();
			iReference = preference.getBuilding().getAbbreviation();
			iExternalId = preference.getBuilding().getExternalUniqueId();
		}
		PreferenceInfo(RoomFeaturePref preference) {
			iType = "room-feature";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iValue = preference.getRoomFeature().getLabel();
			iReference = preference.getRoomFeature().getAbbv();
		}
		PreferenceInfo(RoomGroupPref preference) {
			iType = "room-group";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iValue = preference.getRoomGroup().getName();
			iReference = preference.getRoomGroup().getAbbv();
		}
		PreferenceInfo(DistributionPref preference) {
			iType = "distribution";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iValue = preference.getDistributionType().getLabel();
			iReference = preference.getDistributionType().getReference();
		}
		PreferenceInfo(InstructorAttributePref preference) {
			iType = "attribute";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iValue = preference.getAttribute().getName();
			iReference = preference.getAttribute().getCode();
		}
		PreferenceInfo(InstructorCoursePref preference) {
			iType = "course";
			iPreference = preference.getPrefLevel().getPrefProlog();
			iPreferenceText = preference.getPrefLevel().getPrefName();
			iValue = preference.getCourse().getTitle();
			iReference = preference.getCourse().getCourseName();
			iExternalId = preference.getCourse().getExternalUniqueId();
		}
	}
	
	protected class TimePrefInfo {
		String iDayOfWeek;
		String iStartTime;
		String iEndTime;
		String iPreference;
		String iPreferenceText;
		
		private String slot2time(int slot) {
			int minutesSinceMidnight = slot*Constants.SLOT_LENGTH_MIN+Constants.FIRST_SLOT_TIME_MIN;
			int hour = minutesSinceMidnight/60;
		    int min = minutesSinceMidnight%60;
			return hour + ":" + (min < 10 ? "0" : "") + min;
		}
		
		TimePrefInfo(int day, int start, int end, String pref) {
			iDayOfWeek = Constants.DAY_NAMES_FULL[day];
			iStartTime = slot2time(start);
			iEndTime = slot2time(end);
			iPreference = pref;
			iPreferenceText = PreferenceLevel.prolog2string(pref);
		}
	}
	
	protected static Date firstDate(DatePattern dp, int dayCode) {
    	if (dp == null) return null;
    	BitSet weekCode = dp.getPatternBitSet();
    	if (weekCode.isEmpty()) return null;
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	Date dpFirstDate = DateUtils.getDate(1, dp.getSession().getPatternStartMonth(), dp.getSession().getSessionStartYear());
    	cal.setTime(dpFirstDate);
    	int idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	while (idx < weekCode.size()) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((dayCode & DayCode.MON.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((dayCode & DayCode.TUE.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((dayCode & DayCode.WED.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((dayCode & DayCode.THU.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((dayCode & DayCode.FRI.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((dayCode & DayCode.SAT.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((dayCode & DayCode.SUN.getCode()) != 0) return cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	return null;
	}
	
	protected static Date lastDate(DatePattern dp, int dayCode) {
		if (dp == null) return null;
		BitSet weekCode = dp.getPatternBitSet();
    	if (weekCode.isEmpty()) return null;
		Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
		Date dpFirstDate = DateUtils.getDate(1, dp.getSession().getPatternStartMonth(), dp.getSession().getSessionStartYear());
    	cal.setTime(dpFirstDate);
    	int idx = weekCode.length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((dayCode & DayCode.MON.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((dayCode & DayCode.TUE.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((dayCode & DayCode.WED.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((dayCode & DayCode.THU.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((dayCode & DayCode.FRI.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((dayCode & DayCode.SAT.getCode()) != 0) return cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((dayCode & DayCode.SUN.getCode()) != 0) return cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	return null;
    }
	
	@Override
	protected String getName() {
		return "instructor-schedule";
	}
}
