/*
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
package org.unitime.timetable.solver.studentsct;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.IdGenerator;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.studentsct.StudentSectioningLoader;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.constraint.DependentCourses;
import org.cpsolver.studentsct.constraint.FixedAssignments;
import org.cpsolver.studentsct.constraint.LinkedSections;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.RequestGroup;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.cpsolver.studentsct.model.Student.StudentPriority;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumOverride;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.IndividualRestriction;
import org.cpsolver.studentsct.reservation.Reservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.cpsolver.studentsct.reservation.Restriction;
import org.cpsolver.studentsct.reservation.UniversalOverride;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpart;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CurriculumOverrideReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.IndividualOverrideReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.StudentGroupType.AllowDisabledSection;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.UniversalOverrideReservation;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.ResultType;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.custom.CourseRequestsValidationProvider;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider.CriticalCourses;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.StudentHoldsCheckProvider;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbStudentMatcher;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.solver.curricula.LastLikeStudentCourseDemands;
import org.unitime.timetable.solver.curricula.ProjectedStudentCourseDemands;
import org.unitime.timetable.solver.curricula.StudentCourseDemands;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.AreaClasfMajor;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.Group;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.WeightedStudentId;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.DefaultExternalClassNameHelper;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.duration.DurationModel;


/**
 * @author Tomas Muller
 */
public class StudentSectioningDatabaseLoader extends StudentSectioningLoader {
    private static Log sLog = LogFactory.getLog(StudentSectioningDatabaseLoader.class);
    private boolean iIncludeCourseDemands = true;
    private boolean iIncludeUseCommittedAssignments = false;
    private boolean iMakeupAssignmentsFromRequiredPrefs = false;
    private boolean iLoadStudentInfo = true;
    private String iInitiative = null;
    private String iTerm = null;
    private String iYear = null;
    private String iOwnerId = null;
    private Long iSessionId = null;
    private long iMakeupAssignmentId = 0;
	private BitSet iFreeTimePattern = null;
	private Date iDatePatternFirstDate = null;
	private boolean iTweakLimits = false;
	private boolean iAllowToKeepCurrentEnrollment = false;
	private long iMakeupReservationId = 0;
	private boolean iLoadSectioningInfos = false;
	private boolean iProjections = false;
	private boolean iFixWeights = true;
	private boolean iCheckForNoBatchStatus = true;
	private boolean iCheckEnabledForScheduling = true;
	private boolean iLoadRequestGroups = false;
	private String iRequestGroupRegExp = null;
	private Query iStudentQuery = null;
	private boolean iNoUnlimitedGroupReservations = false;
	private boolean iLinkedClassesMustBeUsed = false;
	private boolean iAllowDefaultCourseAlternatives = false;
	private boolean iIncludeUnavailabilities = true;
	private boolean iIncludeUnavailabilitiesFromOtherSessions = true;
	private boolean iDecreaseCreditLimitsByOtherSessionEnrollments = false;
	private String iShortDistanceAccomodationReference = null;
	private boolean iCheckOverrideStatus = false, iValidateOverrides = false;
	private CourseRequestsValidationProvider iValidationProvider = null;
	private List<Long> iUpdatedStudents = new ArrayList<Long>();
	private NameFormat iStudentNameFormat = null, iInstructorNameFormat = null;
	private StudentSolver iValidator = null;
	private boolean iCheckRequestStatusSkipCancelled = false, iCheckRequestStatusSkipPending = false;
	private int iNrValidationThreads = 1;
	private boolean iCanContinue = true;
	private boolean iCheckCriticalCourses = false;
	private CriticalCoursesProvider iCriticalCoursesProvider = null;
	private int iNrCheckCriticalThreads = 1;
	private boolean iMoveCriticalCoursesUp = false;
	private boolean iMoveFreeTimesDown = false;
	private boolean iCorrectConfigLimit = false;
	private boolean iUseSnapShotLimits = false;
	private boolean iMoveParentCoursesUp = false;
	private Map<StudentPriority, String> iPriorityStudentGroupReference = new HashMap<StudentPriority, String>();
	private Map<StudentPriority, Query> iPriorityStudentQuery = new HashMap<StudentPriority, Query>();
	private Query iProjectedStudentQuery = null;
	private RequestPriority iLCRequestPriority = null;
	private Map<Long, Set<Long>> iLCDemands = new HashMap<Long, Set<Long>>();
	private org.cpsolver.ifs.util.Query iVisitingStudentsQuery = null;
	private boolean iVisitingFaceToFaceCheckFirstChoiceOnly = false;
    
    private Progress iProgress = null;
    
    private StudentCourseDemands iStudentCourseDemands = null;
    private boolean iUseAmPm = true;
    private String iDatePatternFormat = null;
    private boolean iShowClassSuffix = false, iShowConfigName = false;
    private boolean iMaxCreditChecking = false;
    private float iMaxDefaultCredit = -1f;
    private float iMinDefaultCredit = -1f;
    private Date iClassesFixedDate = null;
    private int iClassesFixedDateIndex = 0;
    private Date iFirstDay = null;
    private int iDayOfWeekOffset = 0;
    private Query iOnlineOnlyStudentQuery = null;
    private String iOnlineOnlyCourseNameRegExp;
    private String iOnlineOnlyInstructionalModeRegExp;
    private String iResidentialInstructionalModeRegExp;
    private String iMPPCoursesRegExp = null;
    private boolean iOnlineOnlyExclusiveCourses = false;
    private static enum IgnoreNotAssigned { all, other, none }
    private IgnoreNotAssigned iIgnoreNotAssigned = IgnoreNotAssigned.other;
    private boolean iFixAssignedEnrollments = false;
    private boolean iSkipStudentsWithHold = false;
    private StudentHoldsCheckProvider iStudentHoldsCheckProvider = null;
    private InMemoryReport iStudentHoldsCSV;
    private boolean iUseAdvisorWaitLists = false, iUseAdvisorNoSubs = false;
    private Date iClassesPastDate = null;
    private int iClassesPastDateIndex = 0;
    private boolean iLoadArrangedHoursPlacements = true;
    private boolean iReplaceRejectedWithAlternative = false;
    private boolean iReplacePendingWithAlternative = false;
    private boolean iReplaceCancelledWitAlternative = false;
    private boolean iReplaceNotOfferedWithAlternative = false;
    
    public StudentSectioningDatabaseLoader(StudentSolver solver, StudentSectioningModel model, org.cpsolver.ifs.assignment.Assignment<Request, Enrollment> assignment) {
        super(model, assignment);
        iIncludeCourseDemands = model.getProperties().getPropertyBoolean("Load.IncludeCourseDemands", iIncludeCourseDemands);
        iIncludeUseCommittedAssignments = model.getProperties().getPropertyBoolean("Load.IncludeUseCommittedAssignments", iIncludeUseCommittedAssignments);
        iLoadStudentInfo = model.getProperties().getPropertyBoolean("Load.LoadStudentInfo", iLoadStudentInfo);
        iMakeupAssignmentsFromRequiredPrefs = model.getProperties().getPropertyBoolean("Load.MakeupAssignmentsFromRequiredPrefs", iMakeupAssignmentsFromRequiredPrefs);
        iInitiative = model.getProperties().getProperty("Data.Initiative");
        iYear = model.getProperties().getProperty("Data.Year");
        iTerm = model.getProperties().getProperty("Data.Term");
        iOwnerId = model.getProperties().getProperty("General.OwnerPuid");
        iSessionId = model.getProperties().getPropertyLong("General.SessionId", null);
        iTweakLimits = model.getProperties().getPropertyBoolean("Load.TweakLimits", iTweakLimits);
        iAllowToKeepCurrentEnrollment = model.getProperties().getPropertyBoolean("Load.AllowToKeepCurrentEnrollment", iAllowToKeepCurrentEnrollment);
        iLoadSectioningInfos = model.getProperties().getPropertyBoolean("Load.LoadSectioningInfos",iLoadSectioningInfos);
        iProgress = Progress.getInstance(getModel());
        iFixWeights = model.getProperties().getPropertyBoolean("Load.FixWeights", iFixWeights);
        iCheckForNoBatchStatus = model.getProperties().getPropertyBoolean("Load.CheckForNoBatchStatus", iCheckForNoBatchStatus);
        iCheckEnabledForScheduling = model.getProperties().getPropertyBoolean("Load.CheckEnabledForScheduling", iCheckEnabledForScheduling);
        iLoadRequestGroups = model.getProperties().getPropertyBoolean("Load.RequestGroups", iLoadRequestGroups);
        iRequestGroupRegExp = model.getProperties().getProperty("Load.RequestGroupRegExp");
        iDatePatternFormat = ApplicationProperty.DatePatternFormatUseDates.value();
        iNoUnlimitedGroupReservations = model.getProperties().getPropertyBoolean("Load.NoUnlimitedGroupReservations", iNoUnlimitedGroupReservations);
        iUseSnapShotLimits = model.getProperties().getPropertyBoolean("Load.UseSnapShotLimits", iUseSnapShotLimits);
        iCorrectConfigLimit = model.getProperties().getPropertyBoolean("Load.CorrectConfigLimit", iCorrectConfigLimit);
        iLinkedClassesMustBeUsed = model.getProperties().getPropertyBoolean("LinkedClasses.mustBeUsed", false);
        iAllowDefaultCourseAlternatives = ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue();
        iIncludeUnavailabilities = model.getProperties().getPropertyBoolean("Load.IncludeUnavailabilities", iIncludeUnavailabilities);
        iIncludeUnavailabilitiesFromOtherSessions = model.getProperties().getPropertyBoolean("Load.IncludeUnavailabilitiesFromOtherSessions", iIncludeUnavailabilitiesFromOtherSessions);
        iDecreaseCreditLimitsByOtherSessionEnrollments = model.getProperties().getPropertyBoolean("Load.DecreaseCreditLimitsByOtherSessionEnrollments", iDecreaseCreditLimitsByOtherSessionEnrollments);
        iShortDistanceAccomodationReference = model.getProperties().getProperty("Distances.ShortDistanceAccommodationReference", "SD");
        for (StudentPriority priority: StudentPriority.values()) {
        	if (priority == StudentPriority.Normal) break;
            String priorityStudentFilter = model.getProperties().getProperty("Load." + priority.name() + "StudentFilter", null);
            if (priorityStudentFilter != null && !priorityStudentFilter.isEmpty()) {
            	Query q = new Query(priorityStudentFilter);
            	iPriorityStudentQuery.put(priority, q);
            	iProgress.info(priority.name() + " student filter: " + q);
            }
            String groupRef = model.getProperties().getProperty("Load." + priority.name() + "StudentGroupReference", null);
            if (groupRef != null && !groupRef.isEmpty()) {
            	iPriorityStudentGroupReference.put(priority, groupRef);
            	iProgress.info(priority.name() + " student group: " + groupRef);
            }
        }
        
        String visitingStudentsQuery = model.getProperties().getProperty("Load.VisitingStudentFilter", null);
        if (visitingStudentsQuery != null && !visitingStudentsQuery.isEmpty()) {
        	iVisitingStudentsQuery = new org.cpsolver.ifs.util.Query(visitingStudentsQuery);
        }
        iVisitingFaceToFaceCheckFirstChoiceOnly = model.getProperties().getPropertyBoolean("Load.VisitingFaceToFaceCheckFirstChoiceOnly", false);
        		
        iCheckOverrideStatus = model.getProperties().getPropertyBoolean("Load.CheckOverrideStatus", iCheckOverrideStatus);
        iValidateOverrides = model.getProperties().getPropertyBoolean("Load.ValidateOverrides", iValidateOverrides);
        if ((iValidateOverrides || iCheckOverrideStatus) && ApplicationProperty.CustomizationCourseRequestsValidation.value() != null) {
        	try {
        		iValidationProvider = ((CourseRequestsValidationProvider)Class.forName(ApplicationProperty.CustomizationCourseRequestsValidation.value()).getDeclaredConstructor().newInstance());
        	} catch (Exception e) {
        		iProgress.error("Failed to create course request validation provider: " + e.getMessage());
        	}
        	iNrValidationThreads = model.getProperties().getPropertyInt("CourseRequestsValidation.NrThreads", 10);
        }
        iCheckCriticalCourses = model.getProperties().getPropertyBoolean("Load.CheckCriticalCourses", iCheckCriticalCourses);
        if (iCheckCriticalCourses && ApplicationProperty.CustomizationCriticalCourses.value() != null) {
        	try {
        		iCriticalCoursesProvider = ((CriticalCoursesProvider)Class.forName(ApplicationProperty.CustomizationCriticalCourses.value()).getDeclaredConstructor().newInstance());
        	} catch (Exception e) {
        		iProgress.error("Failed to create critical courses provider: " + e.getMessage());
        	}
        	iNrCheckCriticalThreads = model.getProperties().getPropertyInt("CheckCriticalCourses.NrThreads", 10);
        }
        try {
        	String studentCourseDemandsClassName = getModel().getProperties().getProperty("StudentSct.ProjectedCourseDemadsClass", LastLikeStudentCourseDemands.class.getName());
        	if (studentCourseDemandsClassName.indexOf(' ') >= 0) studentCourseDemandsClassName = studentCourseDemandsClassName.replace(" ", "");
        	if (studentCourseDemandsClassName.indexOf('.') < 0) studentCourseDemandsClassName = "org.unitime.timetable.solver.curricula." + studentCourseDemandsClassName;
            Class studentCourseDemandsClass = Class.forName(studentCourseDemandsClassName);
            iStudentCourseDemands = (StudentCourseDemands)studentCourseDemandsClass.getConstructor(DataProperties.class).newInstance(getModel().getProperties());
            iProgress.info("Projected demands: " + getModel().getProperties().getProperty("StudentSct.ProjectedCourseDemadsClass", LastLikeStudentCourseDemands.class.getName()));
        } catch (Exception e) {
        	if (model.getProperties().getPropertyBoolean("Load.IncludeLastLikeStudents", false)) {
        		iStudentCourseDemands = new ProjectedStudentCourseDemands(model.getProperties());
            	iProgress.info("Projected demands: Projected Student Course Demands");
        	} else {
        		iProgress.info("Projected demands: None");
        	}
        }
        if (iStudentCourseDemands != null && iStudentCourseDemands instanceof StudentCourseDemands.NeedsStudentIdGenerator) {
        	((StudentCourseDemands.NeedsStudentIdGenerator)iStudentCourseDemands).setStudentIdGenerator(new IdGenerator());
        }
        
        String query = model.getProperties().getProperty("Load.StudentQuery", null);
        if (query != null && !query.isEmpty()) {
        	iStudentQuery = new Query(query);
        	iProgress.info("Student filter: " + iStudentQuery); 
        }

        iProjections = "Projection".equals(model.getProperties().getProperty("StudentSctBasic.Mode", "Initial"));
        iUseAmPm = model.getProperties().getPropertyBoolean("General.UseAmPm", iUseAmPm);
        iShowClassSuffix = ApplicationProperty.SolverShowClassSufix.isTrue();
        iShowConfigName = ApplicationProperty.SolverShowConfiguratioName.isTrue();
        iStudentNameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingStudentNameFormat.value());
        iInstructorNameFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
        iCheckRequestStatusSkipCancelled = model.getProperties().getPropertyBoolean("Load.CheckRequestStatusSkipCancelled", iCheckRequestStatusSkipCancelled);
        iCheckRequestStatusSkipPending = model.getProperties().getPropertyBoolean("Load.CheckRequestStatusSkipPending", iCheckRequestStatusSkipPending);
        iMaxCreditChecking = model.getProperties().getPropertyBoolean("Load.MaxCreditChecking", iMaxCreditChecking);
        iMaxDefaultCredit = model.getProperties().getPropertyFloat("Load.DefaultMaxCredit", iMaxDefaultCredit);
        iMinDefaultCredit = model.getProperties().getPropertyFloat("Load.DefaultMinCredit", iMinDefaultCredit);
        iMoveCriticalCoursesUp = model.getProperties().getPropertyBoolean("Load.MoveCriticalCoursesUp", iMoveCriticalCoursesUp);
        iMoveFreeTimesDown = model.getProperties().getPropertyBoolean("Load.MoveFreeTimesDown", iMoveFreeTimesDown);
        iMoveParentCoursesUp = model.getProperties().getPropertyBoolean("Load.MoveParentCoursesUp", iMoveCriticalCoursesUp || iMoveFreeTimesDown);

        String onlineOnlyStudentFilter = model.getProperties().getProperty("Load.OnlineOnlyStudentFilter", null);
        iOnlineOnlyInstructionalModeRegExp = model.getProperties().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
        iResidentialInstructionalModeRegExp = model.getProperties().getProperty("Load.ResidentialInstructionalModeRegExp");
        iOnlineOnlyCourseNameRegExp = model.getProperties().getProperty("Load.OnlineOnlyCourseNameRegExp");
        iOnlineOnlyExclusiveCourses = model.getProperties().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false);
        if (onlineOnlyStudentFilter != null && !onlineOnlyStudentFilter.isEmpty()) {
        	iOnlineOnlyStudentQuery = new Query(onlineOnlyStudentFilter);
        	iProgress.info("Online-only student filter: " + iOnlineOnlyStudentQuery); 
        }
        
        String classesFixedDate = getModel().getProperties().getProperty("General.ClassesFixedDate", "");
        if (!classesFixedDate.isEmpty()) {
        	try {
        		iClassesFixedDate = new SimpleDateFormat("yyyy-MM-dd").parse(classesFixedDate);
        	} catch (Exception e) {
        		iProgress.warn("Failed to parse classes fixed date " + classesFixedDate + ". The date must be in the yyyy-mm-dd format.");
        	}
        }
        
        String classesPastDate = getModel().getProperties().getProperty("General.ClassesPastDate", "");
        if (!classesPastDate.isEmpty()) {
        	if ("today".equalsIgnoreCase(classesPastDate)) {
        		iClassesPastDate = new Date();
        	} else {
            	try {
            		iClassesPastDate = new SimpleDateFormat("yyyy-MM-dd").parse(classesPastDate);
            	} catch (Exception e) {
            		iProgress.warn("Failed to parse classes past date " + classesPastDate + ". The date must be in the yyyy-mm-dd format.");
            	}
        	}
        }
        
        String projQuery = model.getProperties().getProperty("Load.ProjectedStudentQuery", null);
        if (projQuery != null && !projQuery.isEmpty()) {
        	iProjectedStudentQuery = new Query(projQuery);
        	iProgress.info("Projected students filter: " + iProjectedStudentQuery); 
        }
        
        iMPPCoursesRegExp = model.getProperties().getProperty("Load.MPPCoursesRegExp", null);
        iIgnoreNotAssigned = IgnoreNotAssigned.valueOf(model.getProperties().getProperty("Load.IgnoreNotAssigned", iIgnoreNotAssigned.name()).toLowerCase());
        if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) {
        	iProgress.info("MPP courses: " + iMPPCoursesRegExp + " (ignoring " + iIgnoreNotAssigned + " not assigned)");
        }
        iFixAssignedEnrollments = model.getProperties().getPropertyBoolean("Load.FixAssignedEnrollments", iFixAssignedEnrollments);
        
        iSkipStudentsWithHold = model.getProperties().getPropertyBoolean("Load.SkipStudentsWithHold", iSkipStudentsWithHold);
        if (iSkipStudentsWithHold && Customization.StudentHoldsCheckProvider.hasProvider())
        	iStudentHoldsCheckProvider = Customization.StudentHoldsCheckProvider.getProvider();
        if (iStudentHoldsCheckProvider != null && iSkipStudentsWithHold) {
        	iStudentHoldsCSV = new InMemoryReport("HOLDS", "Student Holds (" + Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT).format(new Date()) + ")");
        	iStudentHoldsCSV.setHeader(new CSVField[] {
    				new CSVField("Student Id"),
    				new CSVField("Name"),
    				new CSVField("Error")
    		});
        	if (solver != null) solver.setReport(iStudentHoldsCSV);
        }
        String lcPriority = model.getProperties().getProperty("Load.LCRequestPriority");
        if (lcPriority != null && !lcPriority.isEmpty()) {
        	try {
        		iLCRequestPriority = RequestPriority.valueOf(lcPriority);
        	} catch (Exception e) {
        		iProgress.warn("Failed to parse LC request priority " + lcPriority + ".");
        	}
        }
        
        iUseAdvisorWaitLists = model.getProperties().getPropertyBoolean("Load.UseAdvisorWaitLists", iUseAdvisorWaitLists);
        iUseAdvisorNoSubs = model.getProperties().getPropertyBoolean("Load.UseAdvisorNoSubs", iUseAdvisorNoSubs);
        iLoadArrangedHoursPlacements = model.getProperties().getPropertyBoolean("Load.ArrangedHoursPlacements", iLoadArrangedHoursPlacements);
        
        iReplaceRejectedWithAlternative = model.getProperties().getPropertyBoolean("Load.ReplaceRejectedWithSubstitute", iReplaceRejectedWithAlternative);
        iReplacePendingWithAlternative = model.getProperties().getPropertyBoolean("Load.ReplacePendingWithSubstitute", iReplacePendingWithAlternative);
        iReplaceCancelledWitAlternative = model.getProperties().getPropertyBoolean("Load.ReplaceCancelledWithSubstitute", iReplaceCancelledWitAlternative);
        iReplaceNotOfferedWithAlternative = model.getProperties().getPropertyBoolean("Load.ReplaceNotOfferedWithSubstitute", iReplaceNotOfferedWithAlternative);
    }
    
    public void load() {
        iProgress.setStatus("Loading input data ...");
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        try {
            hibSession = SessionDAO.getInstance().getSession();
            hibSession.setCacheMode(CacheMode.IGNORE);
            hibSession.setHibernateFlushMode(FlushMode.MANUAL);
            
            tx = hibSession.beginTransaction(); 

            Session session = null;
            if (iSessionId!=null) {
                session = SessionDAO.getInstance().get(iSessionId);
                if (session!=null) {
                    iYear = session.getAcademicYear();
                    iTerm = session.getAcademicTerm();
                    iInitiative = session.getAcademicInitiative();
                    getModel().getProperties().setProperty("Data.Year", iYear);
                    getModel().getProperties().setProperty("Data.Term", iTerm);
                    getModel().getProperties().setProperty("Data.Initiative", iInitiative);
                }
            } else {
                session = Session.getSessionUsingInitiativeYearTerm(iInitiative, iYear, iTerm);
                if (session!=null) {
                    iSessionId = session.getUniqueId();
                    getModel().getProperties().setProperty("General.SessionId", String.valueOf(iSessionId));
                }
            }
            
            if (session==null) throw new Exception("Session "+iInitiative+" "+iTerm+iYear+" not found!");
        	ApplicationProperties.setSessionId(session.getUniqueId());
        	
        	iFirstDay = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear());
    		iDayOfWeekOffset = Constants.getDayOfWeek(iFirstDay);
    		getModel().setDayOfWeekOffset(iDayOfWeekOffset);
    				
        	if (iClassesFixedDate != null) {
        		iClassesFixedDateIndex = Days.daysBetween(new LocalDate(iFirstDay), new LocalDate(iClassesFixedDate)).getDays();
        		iProgress.info("Classes Fixed Date: " + iClassesFixedDate + " (date pattern index: " + iClassesFixedDateIndex + ")");
        	}
        	if (iClassesPastDate != null) {
        		iClassesPastDateIndex = Days.daysBetween(new LocalDate(iFirstDay), new LocalDate(iClassesPastDate)).getDays();
        		iProgress.info("Classes Past Date: " + iClassesPastDate + " (date pattern index: " + iClassesPastDateIndex + ")");
        	}

        	iProgress.info("Loading data for "+iInitiative+" "+iTerm+iYear+"...");
            
            if (getModel().getDistanceConflict() != null)
            	TravelTime.populateTravelTimes(getModel().getDistanceConflict().getDistanceMetric(), iSessionId, hibSession);
            
            load(session, hibSession);
            
            if (!iUpdatedStudents.isEmpty()) {
            	StudentSectioningQueue.studentChanged(hibSession, null, iSessionId, iUpdatedStudents);
            	hibSession.flush();
            }
            
            tx.commit();
        } catch (Exception e) {
            iProgress.fatal("Unable to load sectioning problem, reason: "+e.getMessage(),e);
            sLog.error(e.getMessage(),e);
            if (tx != null) tx.rollback();
        } finally {
            // here we need to close the session since this code may run in a separate thread
            if (hibSession!=null && hibSession.isOpen()) hibSession.close();
            if (iValidationProvider != null) iValidationProvider.dispose();
            if (iCriticalCoursesProvider != null) iCriticalCoursesProvider.dispose();
        }
    }
    
    private List<Instructor> getInstructors(Class_ clazz) {
        if (!clazz.isDisplayInstructor().booleanValue()) return null;
        List<Instructor> ret = new ArrayList<Instructor>();
        TreeSet ts = new TreeSet(clazz.getClassInstructors());
        for (Iterator i=ts.iterator();i.hasNext();) {
            ClassInstructor ci = (ClassInstructor)i.next();
            if (!ci.isLead().booleanValue()) continue;
            if (ci.getResponsibility() != null && ci.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary)) continue;
            ret.add(new Instructor(ci.getInstructor().getUniqueId(), ci.getInstructor().getExternalUniqueId(), iInstructorNameFormat.format(ci.getInstructor()), ci.getInstructor().getEmail()));
        }
        return ret;
    }
    
    public TimeLocation makeupTime(Class_ c, int shiftDays) {
        DatePattern datePattern = c.effectiveDatePattern(); 
        if (datePattern==null) {
            iProgress.warn("        -- makup time for "+c.getClassLabel(iShowClassSuffix, iShowConfigName)+": no date pattern set");
            return null;
        }        
        for (Iterator i=c.getEffectiveTimePreferences().iterator();i.hasNext();) {
            TimePref tp = (TimePref)i.next();
            TimePatternModel pattern = tp.getTimePatternModel();
            if (pattern.isExactTime()) {
    			DurationModel dm = c.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
    			int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(c.getSchedulingSubpart().getMinutesPerWk(), c.effectiveDatePattern(), pattern.getExactDays());
                int length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
                int breakTime = ExactTimeMins.getBreakTime(minsPerMeeting); 
                return new TimeLocation(pattern.getExactDays(),pattern.getExactStartSlot(),length,PreferenceLevel.sIntLevelNeutral,0,datePattern.getUniqueId(),datePattern.getName(),datePattern.getPatternBitSet(),breakTime);
            } else {
                for (int time=0;time<pattern.getNrTimes(); time++) {
                    for (int day=0;day<pattern.getNrDays(); day++) {
                        String pref = pattern.getPreference(day,time);
                        if (pref.equals(PreferenceLevel.sRequired)) {
                        return new TimeLocation(
                                pattern.getDayCode(day),
                                pattern.getStartSlot(time),
                                pattern.getSlotsPerMtg(),
                                PreferenceLevel.prolog2int(pattern.getPreference(day, time)),
                                pattern.getNormalizedPreference(day,time,0.77),
                                datePattern.getUniqueId(),
                                datePattern.getName(),
                                (shiftDays == 0 ? datePattern.getPatternBitSet() : Assignment.shift(datePattern.getPatternBitSet(), shiftDays)),
                                pattern.getBreakTime());
                        }
                    }
                }
            }
        }
        if (c.getEffectiveTimePreferences().isEmpty())
            iProgress.warn("        -- makup time for "+c.getClassLabel(iShowClassSuffix, iShowConfigName)+": no time preference set");
        else
            iProgress.warn("        -- makup time for "+c.getClassLabel(iShowClassSuffix, iShowConfigName)+": no required time set");
        return null;
    }
    
    public Vector makeupRooms(Class_ c) {
        Vector rooms = new Vector();
        for (Iterator i=c.getEffectiveRoomPreferences().iterator();i.hasNext();) {
            RoomPref rp = (RoomPref)i.next();
            if (!PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) {
                iProgress.warn("        -- makup room for "+c.getClassLabel(iShowClassSuffix, iShowConfigName)+": preference for "+rp.getRoom().getLabel()+" is not required");
                continue;
            }
            Location room = (Location)rp.getRoom();
            RoomLocation roomLocation = new RoomLocation(
                    room.getUniqueId(),
                    room.getLabel(),
                    (room instanceof Room? ((Room)room).getBuilding().getUniqueId() : null),
                    0,
                    room.getCapacity().intValue(),
                    room.getCoordinateX(),
                    room.getCoordinateY(),
                    room.isIgnoreTooFar().booleanValue(),
                    null);
            rooms.addElement(roomLocation);
        }        
        return rooms;
    }
    
    public Placement makeupPlacement(Class_ c, int shiftDays) {
        TimeLocation time = makeupTime(c, shiftDays);
        if (time==null) return null;
        Vector rooms = makeupRooms(c);
        Vector times = new Vector(1); times.addElement(time);
        Lecture lecture = new Lecture(c.getUniqueId(), null, c.getSchedulingSubpart().getUniqueId(), c.getClassLabel(iShowClassSuffix, iShowConfigName), times, rooms, rooms.size(), new Placement(null,time,rooms), 0, 0, 1.0);
        lecture.setNote(c.getNotes());
        Placement p = (Placement)lecture.getInitialAssignment();
        p.setAssignmentId(Long.valueOf(iMakeupAssignmentId++));
        lecture.setBestAssignment(p, 0l);
        iProgress.trace("makup placement for "+c.getClassLabel(iShowClassSuffix, iShowConfigName)+": "+p.getLongName(iUseAmPm));
        return p;
    }
    
    private int getCourseLimit(CourseOffering co) {
    	if (!iCorrectConfigLimit) {
            int limit = 0;
            for (Iterator<InstrOfferingConfig> j = co.getInstructionalOffering().getInstrOfferingConfigs().iterator(); j.hasNext(); ) {
            	InstrOfferingConfig ioc = j.next();
                if (ioc.isUnlimitedEnrollment()) return -1;
                int configLimit = ioc.getLimit();
                if (iUseSnapShotLimits) {
            		Integer snapShotLimit = ioc.getSnapShotLimit();
                	if (snapShotLimit != null && snapShotLimit > configLimit) configLimit = snapShotLimit;	
                }
                limit += configLimit;
            }
            if (co.getReservation() != null)
            	limit = co.getReservation();
            if (limit >= 9999) return -1;
            return limit;
    	}
        int reservedDisabledSpace = 0;
    	for (org.unitime.timetable.model.Reservation r: co.getInstructionalOffering().getReservations()) {
    		if (r instanceof LearningCommunityReservation && !((LearningCommunityReservation)r).getCourse().equals(co)) continue;
    		if (r instanceof StudentGroupReservation) {
    			if (!r.getClasses().isEmpty()) {
    				boolean needDisabled = false;
    				for (Class_ c: r.getClasses()) {
    					if (!c.isEnabledForStudentScheduling()) needDisabled = true;
    				}
    				if (!needDisabled) continue;
    			}
    			StudentGroup gr = ((StudentGroupReservation)r).getGroup();
    			if (gr.getType() != null && gr.getType().getAllowDisabledSection() == AllowDisabledSection.WithGroupReservation) {
    				int reservationLimit = (r.getLimit() == null ? iNoUnlimitedGroupReservations ? gr.getStudents().size() : -1 : r.getLimit());
    				if (reservationLimit >= 0) {
    					reservedDisabledSpace += reservationLimit;
    				} else {
    					reservedDisabledSpace = -1; break;
    				}
    			}
    		}
    	}
    	
    	int limit = 0, limitDisabled = 0;
    	boolean updated = false;
        for (Iterator<InstrOfferingConfig> j = co.getInstructionalOffering().getInstrOfferingConfigs().iterator(); j.hasNext(); ) {
        	InstrOfferingConfig ioc = j.next();
            if (ioc.isUnlimitedEnrollment()) return -1;
            int configLimit = ioc.getLimit();
            if (iUseSnapShotLimits) {
        		Integer snapShotLimit = ioc.getSnapShotLimit();
            	if (snapShotLimit != null && snapShotLimit > configLimit) {
            		configLimit = snapShotLimit;
            	}
            }
            int configEnabled = configLimit;
            
            for (SchedulingSubpart subpart: ioc.getSchedulingSubparts()) {
            	int subpartEnabled = 0;
            	int subpartDisabled = 0;
            	for (Class_ c: subpart.getClasses()) {
            		int classLimit = getSectionLimit(c, false);
            		if (c.isCancelled()) {
            			classLimit = 0;
            			for (StudentClassEnrollment e: c.getStudentEnrollments()) {
                			if ((iCheckForNoBatchStatus && e.getStudent().hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) ||
                				(iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(e.getStudent())))) {
                				classLimit ++;
                			}
                		}
            		}
                	if (c.isEnabledForStudentScheduling())
                		subpartEnabled += classLimit;
                	else {
                		subpartDisabled += classLimit;
                		for (StudentClassEnrollment e: c.getStudentEnrollments()) {
                			if ((iCheckForNoBatchStatus && e.getStudent().hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) ||
                				(iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(e.getStudent())))) {
                				subpartEnabled ++; subpartDisabled --;
                			}
                		}
                	}
            	}
            	int subpartLimit = subpartEnabled + subpartDisabled;
            	if (subpartLimit < configLimit) {
            		configLimit = subpartLimit; updated = true; }
            	if (subpartEnabled < configEnabled) {
            		configEnabled = subpartEnabled; updated = true;
            	}
            }
            
            limit += configEnabled;
            limitDisabled += (configLimit - configEnabled);
        }
        if (limitDisabled > 0)
        	if (reservedDisabledSpace < 0)
        		limit += limitDisabled;
        	else
        		limit += Math.min(limitDisabled, reservedDisabledSpace);
        
        if (updated && co.getReservation() == null) {
        	iProgress.debug("Course limit of " + co.getCourseName() + " decreased to " + limit + " (disabled: " + limitDisabled + ", reserved: " + reservedDisabledSpace + ").");
        }
        
        if (co.getReservation() != null)
        	limit = co.getReservation();
        if (limit >= 9999) return -1;
        return limit;
    }
    
    private int getConfigLimit(InstrOfferingConfig ioc) {
    	if (ioc.isUnlimitedEnrollment()) return -1;
    	int configLimit = ioc.getLimit();
    	if (configLimit >= 9999) return -1;
    	if (iUseSnapShotLimits) {
    		Integer snapShotLimit = ioc.getSnapShotLimit();
        	if (snapShotLimit != null && snapShotLimit > configLimit) {
        		iProgress.debug("Using snapshot limit for " + ioc.getCourseName() + " [" + ioc.getName() + "] (limit: " + configLimit + ", snapshot: " + snapShotLimit + ")");
        		configLimit = snapShotLimit;
        	}
    	}
    	if (!iCorrectConfigLimit) return configLimit;
    	int reservedDisabledSpace = 0;
    	for (org.unitime.timetable.model.Reservation r: ioc.getInstructionalOffering().getReservations()) {
    		if (r instanceof StudentGroupReservation) {
    			if (!r.getConfigurations().isEmpty() && !r.getConfigurations().contains(ioc)) continue;
    			if (!r.getClasses().isEmpty()) {
    				boolean thisConfig = false;
    				boolean needDisabled = false;
    				for (Class_ c: r.getClasses()) {
    					if (c.getSchedulingSubpart().getInstrOfferingConfig().equals(ioc)) thisConfig = true;
    					if (!c.isEnabledForStudentScheduling()) needDisabled = true;
    				}
    				if (!thisConfig || !needDisabled) continue;
    			}
    			StudentGroup gr = ((StudentGroupReservation)r).getGroup();
    			if (gr.getType() != null && gr.getType().getAllowDisabledSection() == AllowDisabledSection.WithGroupReservation) {
    				int reservationLimit = (r.getLimit() == null ? iNoUnlimitedGroupReservations ? gr.getStudents().size() : -1 : r.getLimit());
    				if (reservationLimit >= 0) {
    					reservedDisabledSpace += reservationLimit;
    				} else {
    					reservedDisabledSpace = -1; break;
    				}
    			}
    		}
    	}
    	for (SchedulingSubpart subpart: ioc.getSchedulingSubparts()) {
        	int subpartEnabled = 0;
        	int subpartDisabled = 0;
        	for (Class_ c: subpart.getClasses()) {
            	int classLimit = getSectionLimit(c, false);
        		if (c.isCancelled()) {
        			classLimit = 0;
        			for (StudentClassEnrollment e: c.getStudentEnrollments()) {
            			if ((iCheckForNoBatchStatus && e.getStudent().hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) ||
            				(iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(e.getStudent())))) {
            				classLimit ++;
            			}
            		}
        		}
            	if (c.isEnabledForStudentScheduling()) {
            		subpartEnabled += classLimit;
            	} else {
            		subpartDisabled += classLimit;
            		for (StudentClassEnrollment e: c.getStudentEnrollments()) {
            			if ((iCheckForNoBatchStatus && e.getStudent().hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) ||
            				(iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(e.getStudent())))) {
            				subpartEnabled ++; subpartDisabled --;
            			}
            		}
            	}
        	}
        	int subpartLimit = subpartEnabled + (reservedDisabledSpace < 0 ? subpartDisabled : Math.min(subpartDisabled, reservedDisabledSpace));
        	if (subpartLimit < configLimit) {
        		configLimit = subpartLimit;
        		iProgress.debug("Configuration limit of " + ioc.getCourseName() + " [" + ioc.getName() + "] decreased to " + configLimit +
        				" (" + subpart.getItypeDesc().trim() + (subpart.getSchedulingSubpartSuffix() == null ? "" : " " + subpart.getSchedulingSubpartSuffix()) + 
        				" enabled: " + subpartEnabled + ", disabled: " + subpartDisabled + ", reserved: " + reservedDisabledSpace + ").");
        	}
    	}
    	return configLimit;
    }
    
    public int getSectionLimit(Class_ clazz, boolean infinityCheck) {
        int minLimit = clazz.getExpectedCapacity();
    	int maxLimit = clazz.getMaxExpectedCapacity();
    	if (iUseSnapShotLimits && clazz.getSnapshotLimit() != null) {
    		if (minLimit < clazz.getSnapshotLimit()) {
    			if (infinityCheck)
    				iProgress.debug("Using snapshot limit for " + clazz.getClassLabel(iShowClassSuffix, iShowConfigName) + " (limit: " + clazz.getExpectedCapacity() + ", snapshot: " + clazz.getSnapshotLimit() + ")");
    			minLimit = clazz.getSnapshotLimit();
    		}
    		if (maxLimit < clazz.getSnapshotLimit()) {
    			maxLimit = clazz.getSnapshotLimit();
    		}
    	}
    	int classLimit = maxLimit;
    	if (minLimit < maxLimit) {
    		Assignment a = clazz.getCommittedAssignment();
            Placement p = null;
            if (iMakeupAssignmentsFromRequiredPrefs) {
                p = makeupPlacement(clazz, 0);
            } else if (a != null) {
                p = a.getPlacement();
            }
            if (p != null) {
        		int roomLimit = (int) Math.floor(p.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
        		classLimit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
        	}
    	}
    	if (infinityCheck && (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || classLimit >= 9999)) return -1;
        return classLimit;
    }
    
    private Offering loadOffering(InstructionalOffering io, Hashtable<Long, Course> courseTable, Hashtable<Long, Section> classTable, int shiftDays) {
    	if (io.getInstrOfferingConfigs().isEmpty()) {
    		return null;
    	}
    	String courseName = io.getCourseName();
        Offering offering = new Offering(io.getUniqueId().longValue(), courseName);
        for (Iterator<CourseOffering> i = io.getCourseOfferings().iterator(); i.hasNext(); ) {
        	CourseOffering co = i.next();
        	if (!co.isAllowStudentScheduling()) continue;
            int projected = (co.getProjectedDemand()==null ? 0 : co.getProjectedDemand().intValue());
            Course course = new Course(co.getUniqueId(), co.getSubjectArea().getSubjectAreaAbbreviation(), co.getCourseNbr(), offering, getCourseLimit(co), projected);
            if (co.getCredit() != null)
            	course.setCredit(co.getCredit().creditAbbv() + "|" + co.getCredit().creditText());
            course.setTitle(co.getTitle());
            course.setType(co.getCourseType() == null ? null : co.getCourseType().getReference());
            course.setNote(co.getScheduleBookNote());
            courseTable.put(co.getUniqueId(), course);
        }
        Hashtable<Long,Section> class2section = new Hashtable<Long,Section>();
        Hashtable<Long,Subpart> ss2subpart = new Hashtable<Long, Subpart>();
        DecimalFormat df = new DecimalFormat("000");
        for (Iterator<InstrOfferingConfig> i = io.getInstrOfferingConfigs().iterator(); i.hasNext(); ) {
        	InstrOfferingConfig ioc = i.next();
            Config config = new Config(ioc.getUniqueId(), getConfigLimit(ioc), courseName + " [" + ioc.getName() + "]", offering);
            InstructionalMethod im = ioc.getEffectiveInstructionalMethod();
            if (im != null) {
            	config.setInstructionalMethodId(im.getUniqueId());
            	config.setInstructionalMethodName(im.getLabel());
            	config.setInstructionalMethodReference(im.getReference());
            }
            TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(new SchedulingSubpartComparator());
            subparts.addAll(ioc.getSchedulingSubparts());
            for (SchedulingSubpart ss: subparts) {
                String sufix = ss.getSchedulingSubpartSuffix();
                Subpart parentSubpart = (ss.getParentSubpart() == null ? null : (Subpart)ss2subpart.get(ss.getParentSubpart().getUniqueId()));
                if (ss.getParentSubpart() != null && parentSubpart == null) {
                    iProgress.error("Subpart " + ss.getSchedulingSubpartLabel() + " has parent " + 
                    		ss.getSchedulingSubpartLabel() +", but the appropriate parent subpart is not loaded.");
                }
                Subpart subpart = new Subpart(ss.getUniqueId().longValue(), df.format(ss.getItype().getItype()) + sufix,
                		ss.getItype().getAbbv().trim() + (ioc.getInstructionalMethod() == null ? "" : " (" + ioc.getInstructionalMethod().getLabel() + ")"), config, parentSubpart);
                subpart.setAllowOverlap(ss.isStudentAllowOverlap());
                if (ss.getCredit() != null)
                	subpart.setCredit(ss.getCredit().creditAbbv() + "|" + ss.getCredit().creditText());
                ss2subpart.put(ss.getUniqueId(), subpart);
                for (Iterator<Class_> j = ss.getClasses().iterator(); j.hasNext(); ) {
                	Class_ c = j.next();
                    Section parentSection = (c.getParentClass() == null ? null : (Section)class2section.get(c.getParentClass().getUniqueId()));
                    if (c.getParentClass()!=null && parentSection==null) {
                        iProgress.error("Class " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + " has parent " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + ", but the appropriate parent section is not loaded.");
                    }
                    Assignment a = c.getCommittedAssignment();
                    Placement p = null;
                    if (iMakeupAssignmentsFromRequiredPrefs) {
                        p = makeupPlacement(c, shiftDays);
                    } else if (a != null) {
                        p = a.getPlacement(shiftDays);
                    }
                    if (p != null && p.getTimeLocation() != null) {
                    	p.getTimeLocation().setDatePattern(
                    			p.getTimeLocation().getDatePatternId(),
                    			(a == null ? datePatternName(c.effectiveDatePattern(), p.getTimeLocation()) : datePatternName(a.getDatePattern(), p.getTimeLocation())),
                    			p.getTimeLocation().getWeekCode());
                    }
                    if (p == null && iLoadArrangedHoursPlacements) {
                    	DatePattern dp = c.effectiveDatePattern();
                    	if (dp != null) {
                    		TimeLocation tl = new TimeLocation(0, 0, 0, 0, 0.0, dp.getUniqueId(), datePatternName(dp, null), dp.getPatternBitSet(), 0);
                    		List<RoomLocation> rooms = new ArrayList<RoomLocation>();
                    		for (Iterator<?> it = c.effectivePreferences(RoomPref.class).iterator(); it.hasNext(); ) {
                    			RoomPref rp = (RoomPref)it.next();
                    			if (PreferenceLevel.sRequired.equals(rp.getPrefLevel().getPrefProlog())) {
                    				Location room = rp.getRoom();
                    				RoomLocation rl = new RoomLocation(
                    						room.getUniqueId(),
                    						room.getLabel(),
                    						(room instanceof Room? ((Room)room).getBuilding().getUniqueId() : null),
                    						0,
                    						room.getCapacity().intValue(),
                    						room.getCoordinateX(),
                    						room.getCoordinateY(),
                    						room.isIgnoreTooFar().booleanValue(),
                    						null);
                    				rooms.add(rl);
                    			}
                    		}
                    		p = new Placement(null, tl, rooms);
                    	}
                    }
                    Section section = new Section(c.getUniqueId().longValue(), getSectionLimit(c, true), (c.getClassSuffix() == null ? c.getSectionNumberString() : c.getClassSuffix()), subpart, p,
                    		getInstructors(c), parentSection);
                    if (iCheckEnabledForScheduling && !c.isEnabledForStudentScheduling())
                    	section.setEnabled(false);
                    if (iClassesFixedDateIndex > 0 && p != null && p.getTimeLocation() != null && p.getTimeLocation().getDayCode() != 0 && p.getTimeLocation().getFirstMeeting(iDayOfWeekOffset) < iClassesFixedDateIndex) {
                    	iProgress.info("Class " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + " " + p.getLongName(iUseAmPm) + " starts before the fixed date, it is marked as disabled for student scheduling.");
                    	section.setEnabled(false);
                    }
                    if (iClassesFixedDateIndex > 0 && a == null && c.effectiveDatePattern() != null) {
                    	int firstMeeting = c.effectiveDatePattern().getPatternBitSet().nextSetBit(0);
                		if (firstMeeting >= 0 && firstMeeting < iClassesFixedDateIndex) {
                			iProgress.info("Class " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + " Arranged Hours " + c.effectiveDatePattern().getName() + " starts before the fixed date, it is marked as disabled for student scheduling.");
                        	section.setEnabled(false);
                		}
                    }
                    if (iClassesPastDateIndex > 0 && p != null && p.getTimeLocation() != null && p.getTimeLocation().getDayCode() != 0 && p.getTimeLocation().getFirstMeeting(iDayOfWeekOffset) < iClassesPastDateIndex) {
                    	iProgress.info("Class " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + " " + p.getLongName(iUseAmPm) + " starts before the past date, it should be avoided.");
                    	section.setPast(true);
                    }
                    if (iClassesPastDateIndex > 0 && a == null && c.effectiveDatePattern() != null) {
                    	int firstMeeting = c.effectiveDatePattern().getPatternBitSet().nextSetBit(0);
                		if (firstMeeting >= 0 && firstMeeting < iClassesPastDateIndex) {
                			iProgress.info("Class " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + " Arranged Hours " + c.effectiveDatePattern().getName() + " starts before the past date, it should be avoided.");
                			section.setPast(true);
                		}
                    }
                    if (a == null) {
                    	section.setOnline(true);
                    } else if (a != null) {
                    	boolean hasRoom = false;
                    	for (Location loc: a.getRooms())
                    		if (!loc.isIgnoreRoomCheck()) hasRoom = true;
                    	section.setOnline(!hasRoom);
                    }
                    for (CourseOffering course: io.getCourseOfferings()) {
                    	String suffix = c.getClassSuffix(course);
                    	if (suffix != null)
                    		section.setName(course.getUniqueId(), suffix);
                    }
                    section.setCancelled(c.isCancelled());
                    if (section.isCancelled() && iAllowToKeepCurrentEnrollment && iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && !courseName.matches(iMPPCoursesRegExp) && !c.getStudentEnrollments().isEmpty()) {
                    	iProgress.info("Class " + c.getClassLabel(iShowClassSuffix, iShowConfigName) + " is cancelled but has enrollments, the class will be treated as disabled.");
                    	section.setCancelled(false); section.setEnabled(false);
                    }
                    class2section.put(c.getUniqueId(), section);
                    classTable.put(c.getUniqueId(), section);
                }
            }
        }
        for (org.unitime.timetable.model.Reservation reservation: io.getReservations()) {
        	Reservation r = null;
        	if (reservation instanceof OverrideReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((org.unitime.timetable.model.IndividualReservation)reservation).getStudents())
        			studentIds.add(s.getUniqueId());
        		if (reservation.isAlwaysExpired())
        			r = new ReservationOverride(reservation.getUniqueId(), offering, studentIds);
        		else
        			r = new IndividualReservation(reservation.getUniqueId(), offering, studentIds);
        		OverrideType type = ((OverrideReservation)reservation).getOverrideType();
        		r.setPriority(ApplicationProperty.ReservationPriorityOverride.intValue());
        		r.setMustBeUsed(type.isMustBeUsed());
        		r.setAllowOverlap(type.isAllowTimeConflict());
        		r.setCanAssignOverLimit(type.isAllowOverLimit());
        		r.setBreakLinkedSections(type.isBreakLinkedSections());
        	} else if (reservation instanceof IndividualOverrideReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((org.unitime.timetable.model.IndividualReservation)reservation).getStudents())
        			studentIds.add(s.getUniqueId());
        		if (reservation.isAlwaysExpired())
        			r = new ReservationOverride(reservation.getUniqueId(), offering, studentIds);
        		else
        			r = new IndividualReservation(reservation.getUniqueId(), offering, studentIds);
        		r.setPriority(reservation.getPriority());
        		r.setMustBeUsed(reservation.isMustBeUsed());
        		r.setAllowOverlap(reservation.isAllowOverlap());
        		r.setCanAssignOverLimit(reservation.isCanAssignOverLimit());
        	} else if (reservation instanceof org.unitime.timetable.model.IndividualReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((org.unitime.timetable.model.IndividualReservation)reservation).getStudents())
        			studentIds.add(s.getUniqueId());
        		r = new IndividualReservation(reservation.getUniqueId(), offering, studentIds);
        		r.setPriority(ApplicationProperty.ReservationPriorityIndividual.intValue());
        		r.setAllowOverlap(ApplicationProperty.ReservationAllowOverlapIndividual.isTrue());
        		r.setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitIndividual.isTrue());
        		r.setMustBeUsed(ApplicationProperty.ReservationMustBeUsedIndividual.isTrue());
        	} else if (reservation instanceof GroupOverrideReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((StudentGroupReservation)reservation).getGroup().getStudents())
        			studentIds.add(s.getUniqueId());
        		if (((StudentGroupReservation)reservation).isAlwaysExpired())
        			r = new ReservationOverride(reservation.getUniqueId(), offering, studentIds);
        		else
        			r = new GroupReservation(reservation.getUniqueId(),
        					(reservation.getLimit() == null ? iNoUnlimitedGroupReservations ? studentIds.size() : -1.0 : reservation.getLimit()),
        					offering, studentIds);
        		r.setPriority(reservation.getPriority());
        		r.setMustBeUsed(reservation.isMustBeUsed());
        		r.setAllowOverlap(reservation.isAllowOverlap());
        		r.setCanAssignOverLimit(reservation.isCanAssignOverLimit());
        		StudentGroupType type = ((StudentGroupReservation)reservation).getGroup().getType();
        		if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.WithGroupReservation) r.setAllowDisabled(true);
        	} else if (reservation instanceof LearningCommunityReservation) {
        		CourseOffering co = ((LearningCommunityReservation)reservation).getCourse();
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((LearningCommunityReservation)reservation).getGroup().getStudents())
        			studentIds.add(s.getUniqueId());
        		for (Course course: offering.getCourses()) {
        			if (co.getUniqueId().equals(course.getId()))
                		r = new org.cpsolver.studentsct.reservation.LearningCommunityReservation(reservation.getUniqueId(),
                				(reservation.getLimit() == null ? iNoUnlimitedGroupReservations ? studentIds.size() : -1.0 : reservation.getLimit()),
                				course, studentIds);
        		}
        		if (r != null) {
            		r.setPriority(ApplicationProperty.ReservationPriorityLearningCommunity.intValue());
            		r.setAllowOverlap(ApplicationProperty.ReservationAllowOverlapLearningCommunity.isTrue());
            		r.setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitLearningCommunity.isTrue());
            		r.setMustBeUsed(ApplicationProperty.ReservationMustBeUsedLearningCommunity.isTrue());
            		StudentGroupType type = ((StudentGroupReservation)reservation).getGroup().getType();
            		if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.WithGroupReservation) r.setAllowDisabled(true);
            		if (iLCRequestPriority != null) {
                		Set<Long> courseStudentIds = iLCDemands.get(co.getUniqueId());
                		if (courseStudentIds == null) {
                			courseStudentIds = new HashSet<Long>();
                			iLCDemands.put(co.getUniqueId(), courseStudentIds);
                		}
                		for (org.unitime.timetable.model.Student s: ((LearningCommunityReservation)reservation).getGroup().getStudents())
                			courseStudentIds.add(s.getUniqueId());
            		}
        		}
        	} else if (reservation instanceof StudentGroupReservation) {
        		List<Long> studentIds = new ArrayList<Long>();
        		for (org.unitime.timetable.model.Student s: ((StudentGroupReservation)reservation).getGroup().getStudents())
        			studentIds.add(s.getUniqueId());
        		r = new GroupReservation(reservation.getUniqueId(),
        				(reservation.getLimit() == null ? iNoUnlimitedGroupReservations ? studentIds.size() : -1.0 : reservation.getLimit()),
        				offering, studentIds);
        		r.setPriority(ApplicationProperty.ReservationPriorityGroup.intValue());
        		r.setAllowOverlap(ApplicationProperty.ReservationAllowOverlapGroup.isTrue());
        		r.setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitGroup.isTrue());
        		r.setMustBeUsed(ApplicationProperty.ReservationMustBeUsedGroup.isTrue());
        		StudentGroupType type = ((StudentGroupReservation)reservation).getGroup().getType();
        		if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.WithGroupReservation) r.setAllowDisabled(true);
        	} else if (reservation instanceof CurriculumOverrideReservation) {
        		org.unitime.timetable.model.CurriculumReservation cr = (org.unitime.timetable.model.CurriculumReservation)reservation;
        		List<String> areas = new ArrayList<String>();
        		for (AcademicArea area: cr.getAreas())
        			areas.add(area.getAcademicAreaAbbreviation());
        		List<String> classifications = new ArrayList<String>();
        		for (AcademicClassification clasf: cr.getClassifications())
        			classifications.add(clasf.getCode());
        		List<String> majors = new ArrayList<String>();
        		for (PosMajor major: cr.getMajors())
        			majors.add(major.getCode());
        		List<String> minors = new ArrayList<String>();
        		for (PosMinor minor: cr.getMinors())
        			minors.add(minor.getCode());
        		if (((CurriculumOverrideReservation)reservation).isAlwaysExpired())
        			r = new CurriculumOverride(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
            				offering, areas, classifications, majors, minors);
        		else
        			r = new CurriculumReservation(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
            				offering, areas, classifications, majors, minors);
        		r.setPriority(reservation.getPriority());
        		r.setMustBeUsed(reservation.isMustBeUsed());
        		r.setAllowOverlap(reservation.isAllowOverlap());
        		r.setCanAssignOverLimit(reservation.isCanAssignOverLimit());
        		for (PosMajorConcentration conc: cr.getConcentrations())
        			((CurriculumReservation)r).addConcentration(conc.getMajor().getCode(), conc.getCode());
        	} else if (reservation instanceof org.unitime.timetable.model.CurriculumReservation) {
        		org.unitime.timetable.model.CurriculumReservation cr = (org.unitime.timetable.model.CurriculumReservation)reservation;
        		List<String> areas = new ArrayList<String>();
        		for (AcademicArea area: cr.getAreas())
        			areas.add(area.getAcademicAreaAbbreviation());
        		List<String> classifications = new ArrayList<String>();
        		for (AcademicClassification clasf: cr.getClassifications())
        			classifications.add(clasf.getCode());
        		List<String> majors = new ArrayList<String>();
        		for (PosMajor major: cr.getMajors())
        			majors.add(major.getCode());
        		List<String> minors = new ArrayList<String>();
        		for (PosMinor minor: cr.getMinors())
        			minors.add(minor.getCode());
        		r = new CurriculumReservation(reservation.getUniqueId(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()),
        				offering, areas, classifications, majors, minors);
        		r.setPriority(ApplicationProperty.ReservationPriorityCurriculum.intValue());
        		r.setAllowOverlap(ApplicationProperty.ReservationAllowOverlapCurriculum.isTrue());
        		r.setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitCurriculum.isTrue());
        		r.setMustBeUsed(ApplicationProperty.ReservationMustBeUsedCurriculum.isTrue());
        		for (PosMajorConcentration conc: cr.getConcentrations())
        			((CurriculumReservation)r).addConcentration(conc.getMajor().getCode(), conc.getCode());
        	} else if (reservation instanceof org.unitime.timetable.model.CourseReservation) {
        		CourseOffering co = ((org.unitime.timetable.model.CourseReservation)reservation).getCourse();
        		for (Course course: offering.getCourses()) {
        			if (co.getUniqueId().equals(course.getId()))
        				r = new CourseReservation(reservation.getUniqueId(), course);
        		}
        		if (r != null) {
        			r.setPriority(ApplicationProperty.ReservationPriorityCourse.intValue());
            		r.setAllowOverlap(ApplicationProperty.ReservationAllowOverlapCourse.isTrue());
            		r.setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitCourse.isTrue());
            		r.setMustBeUsed(ApplicationProperty.ReservationMustBeUsedCourse.isTrue());
        		}
        	} else if (reservation instanceof UniversalOverrideReservation) {
        		r = new UniversalOverride(reservation.getUniqueId(), reservation.isAlwaysExpired(), (reservation.getLimit() == null ? -1.0 : reservation.getLimit()), offering, ((UniversalOverrideReservation)reservation).getFilter());
        		r.setPriority(reservation.getPriority());
        		r.setMustBeUsed(reservation.isMustBeUsed());
        		r.setAllowOverlap(reservation.isAllowOverlap());
        		r.setCanAssignOverLimit(reservation.isCanAssignOverLimit());
        		
        	}
        	if (r == null) {
        		iProgress.warn("Failed to load reservation " + reservation.getUniqueId() + "."); continue;
        	}
        	r.setExpired(reservation.isExpired());
        	configs: for (InstrOfferingConfig ioc: reservation.getConfigurations()) {
        		for (Config config: offering.getConfigs()) {
        			if (ioc.getUniqueId().equals(config.getId())) {
        				r.addConfig(config);
        				continue configs;
        			}
        		}
        	}
        	classes: for (Class_ c: reservation.getClasses()) {
        		for (Config config: offering.getConfigs()) {
        			for (Subpart subpart: config.getSubparts()) {
        				for (Section section: subpart.getSections()) {
        					if (c.getUniqueId().equals(section.getId())) {
        						r.addSection(section, reservation.isReservationInclusive());
        						continue classes;
        					}
        				}
        			}
        		}
        	}
        }
        if (io.isByReservationOnly())
        	new DummyReservation(offering);
        
        if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) {
        	boolean match = false;
        	for (Course course: offering.getCourses())
        		if (course.getName().matches(iMPPCoursesRegExp)) { match = true; break; }
        	if (!match)
        		for (Reservation r: offering.getReservations())
        			r.setExpired(true);
        }
        
        return offering;
    }
    
    public void skipStudent(org.unitime.timetable.model.Student s, Hashtable<Long,Course> courseTable, Hashtable<Long,Section> classTable) {
    	iProgress.debug("Skipping student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+NameFormat.defaultFormat().format(s)+")");
    	
    	// If the student is enrolled in some classes, decrease the space in these classes accordingly
    	Map<Course, List<Section>> assignment = new HashMap<Course, List<Section>>();
    	for (StudentClassEnrollment enrollment: s.getClassEnrollments()) {
    		Section section = classTable.get(enrollment.getClazz().getUniqueId());
    		Course course = courseTable.get(enrollment.getCourseOffering().getUniqueId());
    		if (section == null || course == null) continue;
    		
    		List<Section> sections = assignment.get(course);
    		if (sections == null) {
    			sections = new ArrayList<Section>();
    			assignment.put(course, sections);
    			
				// If there is space in the course, decrease it by one
				if (course.getLimit() > 0)
					course.setLimit(course.getLimit() - 1);

				// If there is space in the configuration, decrease it by one
				Config config = section.getSubpart().getConfig();
				if (config.getLimit() > 0) {
					config.setLimit(config.getLimit() - 1);
				}
    		}
    		
    		// If there is space in the section, decrease it by one
    		if (section.getLimit() > 0) {
    			section.setLimit(section.getLimit() - 1);
    		}
    		sections.add(section);
    	}
    	
    	// For each offering the student is enrolled in
    	for (Map.Entry<Course, List<Section>> entry: assignment.entrySet()) {
    		Course course = entry.getKey();
    		List<Section> sections = entry.getValue();
    		
    		// Look for a matching reservation
    		Reservation reservation = null;
    		for (Reservation r: course.getOffering().getReservations()) {
    			// Skip reservations with no space that can be skipped
    			if (r.getReservationLimit() >= 0.0 && r.getReservationLimit() < 1.0 && !r.mustBeUsed()) continue;
    			
    			// Check applicability
    			boolean applicable = false;
    			if (r instanceof org.cpsolver.studentsct.reservation.LearningCommunityReservation) {
    				applicable = ((org.cpsolver.studentsct.reservation.LearningCommunityReservation)r).getStudentIds().contains(s.getUniqueId())
    						&& course.equals(((org.cpsolver.studentsct.reservation.LearningCommunityReservation)r).getCourse());
    			} else if (r instanceof GroupReservation) {
    				applicable = ((GroupReservation)r).getStudentIds().contains(s.getUniqueId());
    			} else if (r instanceof IndividualReservation) {
    				applicable = ((IndividualReservation)r).getStudentIds().contains(s.getUniqueId());
    			}  else if (r instanceof CourseReservation) {
    				applicable = course.equals(((CourseReservation)r).getCourse());
    			} else if (r instanceof CurriculumReservation) {
    				CurriculumReservation c = (CurriculumReservation)r;
    				if (!c.getMajors().isEmpty() || c.getMinors().isEmpty())
    					for (StudentAreaClassificationMajor aac: s.getAreaClasfMajors()) {
        					if (c.getAcademicAreas().contains(aac.getAcademicArea().getAcademicAreaAbbreviation()) &&
        						(c.getClassifications().isEmpty() || c.getClassifications().contains(aac.getAcademicClassification().getCode())) &&
        						(c.getMajors().isEmpty() || c.getMajors().contains(aac.getMajor().getCode()))) {
        						Set<String> conc = c.getConcentrations(aac.getMajor().getCode());
        	                    if (conc != null && !conc.isEmpty()) {
        	                        if (aac.getConcentration() != null && conc.contains(aac.getConcentration().getCode())) {
        	                        	applicable = true; break;
        	                        }
        	                    } else {
        							applicable = true; break;
        	                    }
        					}
        				}
    				if (!c.getMinors().isEmpty())
        				for (StudentAreaClassificationMinor aac: s.getAreaClasfMinors()) {
        					if (c.getAcademicAreas().contains(aac.getAcademicArea().getAcademicAreaAbbreviation()) &&
        						(c.getClassifications().isEmpty() || c.getClassifications().contains(aac.getAcademicClassification().getCode())) &&
        						(c.getMinors().contains(aac.getMinor().getCode()))) {
        							applicable = true; break;
        					}
        				}
    			} else if (reservation instanceof UniversalOverride) {
    				UniversalOverride u = (UniversalOverride)reservation;
    				applicable = (u.getFilter() != null && !u.getFilter().isEmpty() && new Query(u.getFilter()).match(new DbStudentMatcher(s)));
    			}
    			if (!applicable) continue;
    			
    			// If it does not need to be used, check if actually used
				if (!r.mustBeUsed()) {
					boolean included = true;
					if (r.areRestrictionsInclusive()) {
						for (Section section: sections) {
	    					if (!r.getConfigs().isEmpty() && !r.getConfigs().contains(section.getSubpart().getConfig())) {
	    						included = false; break;
	    					}
	    					Set<Section> sectionsThisSubpart = r.getSections(section.getSubpart());
	    					if (sectionsThisSubpart != null && !sectionsThisSubpart.contains(section)) {
	    						included = false; break;
	    					}
	    				}	
					} else {
						included = false;
						if (r.getConfigs().isEmpty() && r.getSections().isEmpty()) {
							included = true;
						} else {
							for (Section section: sections) {
								if (r.getConfigs().contains(section.getSubpart().getConfig())) {
									included = true; break;		
								}
								Set<Section> sectionsThisSubpart = r.getSections(section.getSubpart());
				                if (sectionsThisSubpart != null && sectionsThisSubpart.contains(section)) {
				                	included =  true; break;
				                }
							}
						}
					}
    				if (!included) continue;
				}
				
				if (reservation == null || r.compareTo(getAssignment(), reservation) < 0)
					reservation = r;
    		}
    		
			// Update reservation
    		if (reservation != null) {
    			if (reservation instanceof org.cpsolver.studentsct.reservation.LearningCommunityReservation) {
    				org.cpsolver.studentsct.reservation.LearningCommunityReservation g = (org.cpsolver.studentsct.reservation.LearningCommunityReservation)reservation;
    				g.getStudentIds().remove(s.getUniqueId());
    				if (g.getReservationLimit() >= 1.0 && g.getReservationLimit() != g.getCourse().getLimit())
    					g.setReservationLimit(g.getReservationLimit() - 1.0);
    			} else if (reservation instanceof GroupReservation) {
					GroupReservation g = (GroupReservation)reservation;
					g.getStudentIds().remove(s.getUniqueId());
					if (g.getReservationLimit() >= 1.0)
						g.setReservationLimit(g.getReservationLimit() - 1.0);
				} else if (reservation instanceof IndividualReservation) {
					IndividualReservation i = (IndividualReservation)reservation;
					i.getStudentIds().remove(s.getUniqueId());
				} else if (reservation instanceof CourseReservation) {
					// nothing to do here
				} else if (reservation instanceof CurriculumReservation) {
					CurriculumReservation c = (CurriculumReservation)reservation;
					if (c.getReservationLimit() >= 1.0)
						c.setReservationLimit(c.getReservationLimit() - 1.0);
				} else if (reservation instanceof UniversalOverride) {
					UniversalOverride u = (UniversalOverride)reservation;
					if (u.getReservationLimit() >= 1.0)
						u.setReservationLimit(u.getReservationLimit() - 1.0);
				}
    		}
    	}
    	
    	// Update curriculum counts
    	updateCurriculumCounts(s);    	
    }
    
    protected void checkOverrideStatuses(org.hibernate.Session hibSession, List<org.unitime.timetable.model.Student> students) {
    	List<org.unitime.timetable.model.Student> filteredStudents = new ArrayList<org.unitime.timetable.model.Student>();
		for (org.unitime.timetable.model.Student s: students) {
			if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
    		if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) continue;
            if (iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(s))) continue;
            filteredStudents.add(s);
		}
		setPhase("Checking override statuses...", filteredStudents.size());
    	OnlineSectioningLog.Entity user = Entity.newBuilder().setExternalId(iOwnerId).setType(Entity.EntityType.MANAGER).build();
    	Collection<Long> updatedStudentIds = iValidationProvider.updateStudents(null, new OnlineSectioningHelper(hibSession, user), filteredStudents);
    	if (updatedStudentIds != null) iUpdatedStudents.addAll(updatedStudentIds);
    }
    
    protected void validateOverrides(org.hibernate.Session hibSession, List<org.unitime.timetable.model.Student> students) {
    	if (iNrValidationThreads <= 1) {
    		setPhase("Validate overrides...", students.size());
    		for (org.unitime.timetable.model.Student s: students) {
        		incProgress();
        		if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
        		if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) continue;
                if (iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(s))) continue;
        		validateOverrides(hibSession, s);
        	}
		} else {
			List<org.unitime.timetable.model.Student> filteredStudents = new ArrayList<org.unitime.timetable.model.Student>();
			for (org.unitime.timetable.model.Student s: students) {
				if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
        		if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) continue;
                if (iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(s))) continue;
                filteredStudents.add(s);
			}
			setPhase("Validate overrides...", filteredStudents.size());
			List<Worker> workers = new ArrayList<Worker>();
			Iterator<org.unitime.timetable.model.Student> iterator = filteredStudents.iterator();
			for (int i = 0; i < iNrValidationThreads; i++)
				workers.add(new Worker(hibSession, i, iterator));
			for (Worker worker: workers) worker.start();
			for (Worker worker: workers) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					iCanContinue = false;
					try { worker.join(); } catch (InterruptedException x) {}
				}
			}
			if (!iCanContinue)
				throw new RuntimeException("The validate was interrupted.");
		}
    }
    
    protected void validateOverrides(org.hibernate.Session hibSession, org.unitime.timetable.model.Student s) {
    	if (iValidator == null) {
    		iValidator = new StudentSolver(getModel().getProperties(), null);
    		iValidator.setInitalSolution(new Solution(getModel(), getAssignment()));
    	}
    	OnlineSectioningLog.Action.Builder action = OnlineSectioningLog.Action.newBuilder();
    	action.setOperation("validate-overrides");
		action.setSession(OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(iSessionId)
    			.setName(iTerm + iYear + iInitiative)
    			);
    	action.setStartTime(System.currentTimeMillis());
    	OnlineSectioningLog.Entity user = Entity.newBuilder().setExternalId(iOwnerId).setType(Entity.EntityType.MANAGER).build(); 
    	action.setUser(user);
    	action.setStudent(OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(s.getUniqueId())
				.setExternalId(s.getExternalUniqueId())
				.setName(iStudentNameFormat.format(s))
				.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
		long c0 = OnlineSectioningHelper.getCpuTime();
		try {
        	if (iValidationProvider.revalidateStudent(iValidator, new OnlineSectioningHelper(hibSession, user), s, action)) {
        		iUpdatedStudents.add(s.getUniqueId());
        		action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
        	} else {
        		action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
        	}
		} catch (Exception e) {
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			if (e.getCause() != null) {
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
			} else {
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
		} finally {
			action.setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
			action.setEndTime(System.currentTimeMillis());
			OnlineSectioningLogger.getInstance().record(OnlineSectioningLog.Log.newBuilder().addAction(action).build());
		}
    }
    
    protected boolean isNoSub(CourseDemand cd) {
    	if (iUseAdvisorWaitLists) {
    		for (AdvisorCourseRequest acr: cd.getStudent().getAdvisorCourseRequests()) {
        		if (acr.getWaitlist() != null && acr.getWaitlist().booleanValue() && acr.getCourseOffering() != null) {
        			for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests())
        				if (acr.getCourseOffering().equals(cr.getCourseOffering())) return true;
        		}
        	}
    	} else if (iUseAdvisorNoSubs) {
    		for (AdvisorCourseRequest acr: cd.getStudent().getAdvisorCourseRequests()) {
        		if (acr.getNoSub() != null && acr.getNoSub().booleanValue() && acr.getCourseOffering() != null) {
        			for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests())
        				if (acr.getCourseOffering().equals(cr.getCourseOffering())) {
        					return true;
        				}
        		}
    		}
    	}
    	return cd.effectiveWaitList() || cd.effectiveNoSub();
    }
    
    public Student loadStudent(org.hibernate.Session hibSession, org.unitime.timetable.model.Student s, Hashtable<Long,Course> courseTable, Hashtable<Long,Section> classTable) {
    	// Check for nobatch sectioning status
        if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) {
        	skipStudent(s, courseTable, classTable);
        	return null;
        }
        
        // Check student query, if present
        if (iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(s))) {
        	skipStudent(s, courseTable, classTable);
        	return null;
        }
        
        if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) {
        	boolean match = false;
        	for (CourseDemand cd: s.getCourseDemands())
        		for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests())
        			if (cr.getCourseOffering().getCourseName().matches(iMPPCoursesRegExp)) {
        				if (iIgnoreNotAssigned == IgnoreNotAssigned.all && cr.getClassEnrollments().isEmpty()) continue;
        				match = true;
        			}
        	if (!match) {
        		skipStudent(s, courseTable, classTable);
            	return null;
        	}
        }
        
        if (iStudentHoldsCheckProvider != null && iSkipStudentsWithHold) {
        	String error = getStudentHoldError(hibSession, s);
        	if (error != null) {
        		iProgress.info(iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): " + error);
        		iStudentHoldsCSV.addLine(new CSVField[] {
        				new CSVField(s.getExternalUniqueId()),
        				new CSVField(iStudentNameFormat.format(s)),
        				new CSVField(error)
        		});
        		skipStudent(s, courseTable, classTable);
        		return null;
        	}
        }
        
        iProgress.debug("Loading student "+s.getUniqueId()+" (id="+s.getExternalUniqueId()+", name="+iStudentNameFormat.format(s)+")");
        Student student = new Student(s.getUniqueId().longValue());
        student.setExternalId(s.getExternalUniqueId());
        student.setName(iStudentNameFormat.format(s));
        student.setStatus(s.getSectioningStatus() == null ? null : s.getSectioningStatus().getReference());
        priorities: for (StudentPriority priority: StudentPriority.values()) {
        	if (priority == StudentPriority.Normal) break;
        	Query query = iPriorityStudentQuery.get(priority);
        	String groupRef = iPriorityStudentGroupReference.get(priority);
        	if (query != null && query.match(new DbStudentMatcher(s))) {
            	student.setPriority(priority);
            	break priorities;
        	} else if (groupRef != null) {
        		for (StudentGroup g: s.getGroups()) {
            		if (groupRef.equals(g.getGroupAbbreviation())) {
            			student.setPriority(priority);
            			break priorities;
            		}
            	}
        	}
        }
        if (s.getClassStartDate() != null)
        	student.setClassFirstDate(Days.daysBetween(new LocalDate(iFirstDay), new LocalDate(s.getClassStartDate())).getDays());
        if (s.getClassEndDate() != null)
        	student.setClassLastDate(Days.daysBetween(new LocalDate(iFirstDay), new LocalDate(s.getClassEndDate())).getDays());
        student.setBackToBackPreference(s.getBackToBackPreference());
        student.setModalityPreference(s.getModalityPreference());
        if (iLoadStudentInfo) loadStudentInfo(student,s);
        if (iShortDistanceAccomodationReference != null)
        	for (StudentAccomodation ac: s.getAccomodations())
        		if (iShortDistanceAccomodationReference.equals(ac.getAbbreviation()))
        			student.setNeedShortDistances(true);
        for (StudentGroup g: s.getGroups()) {
        	StudentGroupType type = g.getType();
        	if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.AlwaysAllowed) {
        		student.setAllowDisabled(true);
        		break;
        	}
        }
        float maxCredit = iMaxDefaultCredit;
        if (s.getMaxCredit() != null)
        	maxCredit = s.getMaxCredit();
        if (s.getOverrideMaxCredit() != null) {
        	if (s.isRequestCancelled() && !iCheckRequestStatusSkipCancelled)
        		maxCredit = s.getOverrideMaxCredit();
        	else if (s.isRequestPending() && !iCheckRequestStatusSkipPending)
        		maxCredit = s.getOverrideMaxCredit();
        }
        if (maxCredit > 0f)
        	student.setMaxCredit(maxCredit);
        float minCredit = iMinDefaultCredit;
        if (s.getMinCredit() != null)
        	minCredit = s.getMinCredit();
        if (minCredit >= 0 && minCredit <= maxCredit)
        	student.setMinCredit(minCredit);
        
		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
			public int compare(CourseDemand d1, CourseDemand d2) {
				if (d1.isAlternative() && !d2.isAlternative()) return 1;
				if (!d1.isAlternative() && d2.isAlternative()) return -1;
				int cmp = d1.getPriority().compareTo(d2.getPriority());
				if (cmp != 0) return cmp;
				return d1.getUniqueId().compareTo(d2.getUniqueId());
			}
		});
		Set<CourseOffering> alternatives = new HashSet<CourseOffering>();
		demands.addAll(s.getCourseDemands());
		float credit = 0f, assignedCredit = 0f;
		int skippedNoAltCourseDemands = 0;
        for (CourseDemand cd: demands) {
            if (cd.getFreeTime()!=null) {
            	TimeLocation ft = new TimeLocation(
                        cd.getFreeTime().getDayCode(),
                        cd.getFreeTime().getStartSlot(),
                        cd.getFreeTime().getLength(),
                        0, 0, -1l, "", iFreeTimePattern, 0);
                new FreeTimeRequest(
                        cd.getUniqueId(),
                        cd.getPriority(),
                        cd.isAlternative(),
                        student, ft);
            } else if (!cd.getCourseRequests().isEmpty()) {
                Vector<Course> courses = new Vector<Course>();
                HashSet<Choice> selChoices = new HashSet<Choice>();
                HashSet<Choice> reqChoices = new HashSet<Choice>();
                HashSet<Choice> wlChoices = new HashSet<Choice>();
                HashSet<Section> assignedSections = new HashSet<Section>();
                Config assignedConfig = null;
                TreeSet<org.unitime.timetable.model.CourseRequest> crs = new TreeSet<org.unitime.timetable.model.CourseRequest>(new Comparator<org.unitime.timetable.model.CourseRequest>() {
                	public int compare(org.unitime.timetable.model.CourseRequest r1, org.unitime.timetable.model.CourseRequest r2) {
                		return r1.getOrder().compareTo(r2.getOrder());
                	}
				});
                crs.addAll(cd.getCourseRequests());
                float creditThisRequest = 0;
                int skippedCourseRequests = 0;
                for (org.unitime.timetable.model.CourseRequest cr: crs) {
                	if (cr.isRequestRejected() && cr.getClassEnrollments().isEmpty()) {
                		iProgress.info("Requested course " + cr.getCourseOffering().getCourseName() + " has rejected override for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                		if (iReplaceRejectedWithAlternative) skippedCourseRequests ++;
                		continue;
                	}
                	if (iCheckRequestStatusSkipCancelled && cr.isRequestCancelled() && cr.getClassEnrollments().isEmpty()) {
                		iProgress.info("Requested course " + cr.getCourseOffering().getCourseName() + " has cancelled override for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                		if (iReplaceCancelledWitAlternative) skippedCourseRequests ++;
                		continue;
                	}
                	if (iCheckRequestStatusSkipPending && (cr.isRequestPending() || cr.isRequestNeeded()) && cr.getClassEnrollments().isEmpty()) {
                		iProgress.info("Requested course " + cr.getCourseOffering().getCourseName() + " has pending override for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                		if (iReplacePendingWithAlternative) skippedCourseRequests ++;
                		continue;
                	}
                    Course course = courseTable.get(cr.getCourseOffering().getUniqueId());
                    if (course==null) {
                        iProgress.warn("Student " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ") requests course " + cr.getCourseOffering().getCourseName() + " that is not loaded.");
                        if (iReplaceNotOfferedWithAlternative) skippedCourseRequests ++;
                        continue;
                    }
                    if (iIgnoreNotAssigned == IgnoreNotAssigned.all && cr.getClassEnrollments().isEmpty()) {
                    	iProgress.info("Requested course " + cr.getCourseOffering().getCourseName() + " is not assigned for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                		continue;
                    }
                    if (iIgnoreNotAssigned == IgnoreNotAssigned.other && cr.getClassEnrollments().isEmpty() && iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && !course.getName().matches(iMPPCoursesRegExp)) {
                    	iProgress.info("Requested course " + cr.getCourseOffering().getCourseName() + " is not assigned for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                    	continue;
                    }
                    CourseCreditUnitConfig creditCfg = cr.getCourseOffering().getCredit();
                    if (creditCfg != null && creditThisRequest < creditCfg.getMinCredit()) creditThisRequest = creditCfg.getMinCredit();
                    for (Iterator<ClassWaitList> k=cr.getClassWaitLists().iterator();k.hasNext();) {
                        ClassWaitList cwl = k.next();
                        Section section = course.getOffering().getSection(cwl.getClazz().getUniqueId().longValue());
                        if (section != null && cwl.getType().equals(ClassWaitList.Type.LOCKED.ordinal()))
                        	wlChoices.add(section.getChoice());
                    }
                    if (cr.getPreferences() != null) {
                    	for (StudentSectioningPref p: cr.getPreferences()) {
                    		boolean required = p.isRequired();
                    		if (p instanceof StudentClassPref) {
                    			StudentClassPref scp = (StudentClassPref)p;
    							Section section = course.getOffering().getSection(scp.getClazz().getUniqueId());
    							if (section != null)
    								(required ? reqChoices : selChoices).add(section.getChoice());
                    		} else if (p instanceof StudentInstrMthPref) {
                    			StudentInstrMthPref imp = (StudentInstrMthPref)p;
                    			for (Config config: course.getOffering().getConfigs())
                    				if (config.getInstructionalMethodId() != null && config.getInstructionalMethodId().equals(imp.getInstructionalMethod().getUniqueId()))
                    					(required ? reqChoices : selChoices).add(new Choice(config));
                    		}
                    	}
                    }
                    if (assignedConfig==null) {
                        HashSet<Long> subparts = new HashSet<Long>();
                        for (Iterator<StudentClassEnrollment> i = cr.getClassEnrollments().iterator(); i.hasNext(); ) {
                        	StudentClassEnrollment enrl = i.next();
                        	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                            if (section!=null) {
                            	if (getModel().isMPP()) {
                            		selChoices.add(section.getChoice());
                            		if (iAllowToKeepCurrentEnrollment && !reqChoices.isEmpty()) {
                            			reqChoices.add(new Choice(section));
                            			reqChoices.add(new Choice(section.getSubpart().getConfig()));
                            		}
                            	}
                                assignedSections.add(section);
                                if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                                	iProgress.error("There is a problem assigning " + course.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                                }
                                assignedConfig = section.getSubpart().getConfig();
                                if (!subparts.add(section.getSubpart().getId())) {
                                	iProgress.error("There is a problem assigning " + course.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                                }
                            } else {
                            	iProgress.error("There is a problem assigning " + course.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel(iShowClassSuffix, iShowConfigName) + " not known.");
                            }
                        }
                    }
                    courses.addElement(course);
                }
                if (iAllowDefaultCourseAlternatives && crs.size() == 1 && !cd.isAlternative()) {
                	CourseOffering co = crs.first().getCourseOffering();
                	CourseOffering alt = co.getAlternativeOffering();
                	if (alt != null) {
                		// there is an alternative, but it is already requested -> do nothing
                    	demands: for (CourseDemand d: demands)
                    		for (org.unitime.timetable.model.CourseRequest r: d.getCourseRequests())
                    			if (alt.equals(r.getCourseOffering())) { alt = null; break demands; } 
                	}
                	if (alt != null && alternatives.add(alt)) {
                		// there is an alternative, not requested -> add the alternative
                		Course course = courseTable.get(alt.getUniqueId());
                        if (course == null) {
                            iProgress.warn("Course " + co.getCourseName() + "has an alternative course " + alt.getCourseName() + " that is not loaded (" + s.getExternalUniqueId() + ").");
                        } else {
                            CourseCreditUnitConfig creditCfg = alt.getCredit();
                            if (creditCfg != null && creditThisRequest < creditCfg.getMinCredit()) creditThisRequest = creditCfg.getMinCredit();
                        	if (assignedConfig==null) {
                                HashSet<Long> subparts = new HashSet<Long>();
                                for (StudentClassEnrollment enrl: alt.getClassEnrollments(s)) {
                                	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                                    if (section != null) {
                                    	if (getModel().isMPP()) {
                                    		selChoices.add(section.getChoice());
                                    		if (iAllowToKeepCurrentEnrollment && !reqChoices.isEmpty()) {
                                    			reqChoices.add(new Choice(section));
                                    			reqChoices.add(new Choice(section.getSubpart().getConfig()));
                                    		}
                                    	}
                                        assignedSections.add(section);
                                        if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                                        	iProgress.error("There is a problem assigning " + course.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                                        }
                                        assignedConfig = section.getSubpart().getConfig();
                                        if (!subparts.add(section.getSubpart().getId())) {
                                        	iProgress.error("There is a problem assigning " + course.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                                        }
                                    } else {
                                    	iProgress.error("There is a problem assigning " + course.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel(iShowClassSuffix, iShowConfigName) + " not known.");
                                    }
                                }
                            }
                        	courses.addElement(course);
                        }
                	}
                }
                if (courses.isEmpty()) {
                	if (skippedCourseRequests > 0 && !cd.isAlternative() && !isNoSub(cd))
                		skippedNoAltCourseDemands ++;
                	continue;
                }
                credit += creditThisRequest;
                boolean alternative = cd.isAlternative() || (iMaxCreditChecking && maxCredit > 0 && credit > maxCredit);
                if (alternative && iIgnoreNotAssigned == IgnoreNotAssigned.all)
                	alternative = false;
                if (alternative && iIgnoreNotAssigned == IgnoreNotAssigned.other && iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && !courses.get(0).getName().matches(iMPPCoursesRegExp))
                	alternative = false;
                if (alternative && skippedNoAltCourseDemands > 0) {
                	iProgress.info("Substitude course " + courses.get(0).getName() + " treated as primary due to a skipped course for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                	alternative = false;
                	skippedNoAltCourseDemands --;
                }
                CourseRequest request = new CourseRequest(
                        cd.getUniqueId(),
                        cd.getPriority(),
                        alternative,
                        student,
                        courses,
                        cd.effectiveWaitList() || cd.effectiveNoSub(), 
                        cd.getEffectiveCritical().toRequestPriority(),
                        cd.getTimestamp().getTime());
                if (iLCRequestPriority != null && !alternative) {
                	Set<Long> studentIds = iLCDemands.get(courses.get(0).getId());
                	if (studentIds != null && studentIds.contains(student.getId()) && iLCRequestPriority.isHigher(request)) {
                		iProgress.debug("Student " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ") request " + request + " changed to " + iLCRequestPriority + " due to an LC reservation.");
                		request.setRequestPriority(iLCRequestPriority);
                	}
                }
                request.getSelectedChoices().addAll(selChoices);
                request.getRequiredChoices().addAll(reqChoices);
                request.getWaitlistedChoices().addAll(wlChoices);
                Course assignedCourse = null;
                if (assignedConfig != null)
                	for (Course c: assignedConfig.getOffering().getCourses()) {
                		if (request.getCourses().contains(c)) { assignedCourse = c; break; }
                	}
                if (assignedConfig!=null &&
                		(assignedSections.size() == assignedConfig.getSubparts().size() ||
                		(getModel().isMPP() && getModel().getKeepInitialAssignments()) ||
                		(iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && assignedCourse != null && !assignedCourse.getName().matches(iMPPCoursesRegExp)))) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections, getAssignment());
                    request.setInitialAssignment(enrollment);
                    assignedCredit += enrollment.getCredit();
                    if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && assignedCourse != null && !assignedCourse.getName().matches(iMPPCoursesRegExp)) {
                    	if (iAllowToKeepCurrentEnrollment)
                    		request.setFixedValue(enrollment);
                    	else {
                    		boolean cancelled = false;
                        	for (Section section: enrollment.getSections())
                        		if (section.isCancelled()) { cancelled = true; break; }
                        	if (!cancelled) request.setFixedValue(enrollment);
                    	}
                    } else if (iFixAssignedEnrollments) {
                    	boolean cancelled = false;
                    	for (Section section: enrollment.getSections())
                    		if (section.isCancelled()) { cancelled = true; break; }
                    	if (!cancelled) request.setFixedValue(enrollment);
                    }
                }
                if (!cd.isAlternative() && maxCredit > 0 && credit > maxCredit) {
                	if (iMaxCreditChecking)
                		iProgress.info("Request " + request + " is treated as alternative (" + credit + " > " + maxCredit + ") for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                	else
                		iProgress.info("Request " + request + " is over the max credit limit for " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ")");
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	iProgress.error("There is a problem assigning " + request.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ") wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                }
            }
        }

        if (!s.getClassEnrollments().isEmpty()) {
        	TreeSet<Course> courses = new TreeSet<Course>(new Comparator<Course>() {
        		public int compare(Course c1, Course c2) {
        			return (c1.getSubjectArea() + " " + c1.getCourseNumber()).compareTo(c2.getSubjectArea() + " " + c2.getCourseNumber());
        		}
        	});
        	Map<Long, Long> timeStamp = new Hashtable<Long, Long>();
        	for (StudentClassEnrollment enrl: s.getClassEnrollments()) {
        		if (enrl.getCourseRequest() != null || alternatives.contains(enrl.getCourseOffering())) continue; // already loaded
        		Course course = courseTable.get(enrl.getCourseOffering().getUniqueId());
                if (course==null) {
                    iProgress.warn("Student " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ") requests course " + enrl.getCourseOffering().getCourseName()+" that is not loaded.");
                    continue;
                }
                if (enrl.getTimestamp() != null) timeStamp.put(enrl.getCourseOffering().getUniqueId(), enrl.getTimestamp().getTime());
                courses.add(course);
        	}
        	int priority = 0;
        	courses: for (Course course: courses) {
        		Vector<Course> cx = new Vector<Course>(); cx.add(course);
        		CourseRequest request = null;
        		for (Request r: student.getRequests()) {
        			if (r instanceof CourseRequest && getAssignment().getValue(r) == null && ((CourseRequest)r).getCourses().contains(course)) {
        				request = (CourseRequest)r;
        				break;
        			}
        		}
        		if (request == null) {
        			request = new CourseRequest(
                        course.getId(),
                        priority++,
                        false,
                        student,
                        cx,
                        true,
                        timeStamp.get(course.getId()));
        		}
                HashSet<Section> assignedSections = new HashSet<Section>();
                Config assignedConfig = null;
                HashSet<Long> subparts = new HashSet<Long>();
                for (Iterator<StudentClassEnrollment> i = s.getClassEnrollments().iterator(); i.hasNext(); ) {
                	StudentClassEnrollment enrl = i.next();
                	if (course.getId() != enrl.getCourseOffering().getUniqueId()) continue;
                	Section section = course.getOffering().getSection(enrl.getClazz().getUniqueId());
                    if (section!=null) {
                        assignedSections.add(section);
                        if (assignedConfig != null && assignedConfig.getId() != section.getSubpart().getConfig().getId()) {
                        	iProgress.error("There is a problem assigning " + request.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): classes from different configurations.");
                        	continue courses;
                        }
                        assignedConfig = section.getSubpart().getConfig();
                        if (!subparts.add(section.getSubpart().getId())) {
                        	iProgress.error("There is a problem assigning " + request.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): two or more classes of the same subpart.");
                        	continue courses;
                        }
                    } else {
                    	iProgress.error("There is a problem assigning " + request.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): class " + enrl.getClazz().getClassLabel(iShowClassSuffix, iShowConfigName) + " not known.");
                    	Section x = classTable.get(enrl.getClazz().getUniqueId());
                    	if (x != null) {
                    		iProgress.info("  but a class with the same id is loaded, but under offering " + x.getSubpart().getConfig().getOffering().getName() + " (id is " + x.getSubpart().getConfig().getOffering().getId() + 
                    				", expected " +course.getOffering().getId() + ")");
                    	}
                    	continue courses;
                    }
                }
                Course assignedCourse = null;
                if (assignedConfig != null)
                	for (Course c: assignedConfig.getOffering().getCourses()) {
                		if (request.getCourses().contains(c)) { assignedCourse = c; break; }
                	}
                if (assignedConfig!=null &&
                		(assignedSections.size() == assignedConfig.getSubparts().size() ||
                		(getModel().isMPP() && getModel().getKeepInitialAssignments()) ||
                		(iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && assignedCourse != null && !assignedCourse.getName().matches(iMPPCoursesRegExp)))) {
                    Enrollment enrollment = new Enrollment(request, 0, assignedConfig, assignedSections, getAssignment());
                    request.setInitialAssignment(enrollment);
                    assignedCredit += enrollment.getCredit();
                    if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && assignedCourse != null && !assignedCourse.getName().matches(iMPPCoursesRegExp))
                    	if (iAllowToKeepCurrentEnrollment)
                    		request.setFixedValue(enrollment);
                    	else {
                    		boolean cancelled = false;
                        	for (Section section: enrollment.getSections())
                        		if (section.isCancelled()) { cancelled = true; break; }
                        	if (!cancelled) request.setFixedValue(enrollment);
                    	}
                    else if (iFixAssignedEnrollments) {
                    	boolean cancelled = false;
                    	for (Section section: enrollment.getSections())
                    		if (section.isCancelled()) { cancelled = true; break; }
                    	if (!cancelled) request.setFixedValue(enrollment);
                    }
                }
                if (assignedConfig!=null && assignedSections.size() != assignedConfig.getSubparts().size()) {
                	iProgress.error("There is a problem assigning " + request.getName() + " to " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): wrong number of classes (" +
                			"has " + assignedSections.size() + ", expected " + assignedConfig.getSubparts().size() + ").");
                }
        	}
        }
        
        if (iAllowToKeepCurrentEnrollment && assignedCredit > student.getMaxCredit()) {
        	iProgress.warn("Student " + iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + ") has " + assignedCredit + " credits assigned but his/her maximum is " + student.getMaxCredit());
        	student.setMaxCredit(assignedCredit);
        }
        
        return student;
    }
    
    public void assignStudent(Student student) {
		for (Request r: student.getRequests()) {
			if (r.getInitialAssignment() != null && student.isAvailable(r.getInitialAssignment()) && r.getModel().conflictValues(getAssignment(), r.getInitialAssignment()).isEmpty()) {
				if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && r instanceof CourseRequest && r.getInitialAssignment().getCourse().getName().matches(iMPPCoursesRegExp)) continue;
				getAssignment().assign(0, r.getInitialAssignment());
			}
		}
    }
    
    public void checkForConflicts(Student student) {
		float credit = 0f;
    	for (Request r: student.getRequests()) {
    		if (getAssignment().getValue(r) != null || r.getInitialAssignment() == null || !(r instanceof CourseRequest)) continue;
    		if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && r.getInitialAssignment().getCourse().getName().matches(iMPPCoursesRegExp)) continue;
    		if (student.isAvailable(r.getInitialAssignment()) && r.getModel().conflictValues(getAssignment(), r.getInitialAssignment()).isEmpty()) {
    			getAssignment().assign(0, r.getInitialAssignment());
    			continue;
    		}
           	CourseRequest cr = (CourseRequest)r;
           	Enrollment enrl = (Enrollment)r.getInitialAssignment();
           	if (enrl == null) continue;
           	credit += enrl.getCredit();
    		if ((iAllowToKeepCurrentEnrollment || iTweakLimits) && student.getId() >= 0) {
    			iProgress.info("There was a problem assigning " + cr.getName() + " to " + student.getName() + " (" + student.getExternalId() + ") ");
    			boolean hasMustUse = false;
				for (Reservation reservation: enrl.getOffering().getReservations()) {
					if (reservation.isApplicable(student) && reservation.mustBeUsed())
						hasMustUse = true;
				}
				boolean hasLimit = false, hasOverlap = false, hasDisabled = false, hasLinked = false;
				if (!student.isAvailable(enrl)) {
					hasOverlap = true;
				}
	           	for (Iterator<Section> i = enrl.getSections().iterator(); i.hasNext();) {
	           		Section section = i.next();
	           		if (section.getTime() != null) {
	               		for (Request q: student.getRequests()) {
	               			Enrollment enrlx = getAssignment().getValue(q);
	               			if (enrlx == null || !(q instanceof CourseRequest)) continue;
	               			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
	               				Section sectionx = j.next();
	               				if (sectionx.getTime() == null) continue;
	               				if (sectionx.isOverlapping(section)) {
	               					hasOverlap = true; break;
	               				}
	               			}
	               		}
	           		}
	           		if (section.getLimit() >= 0 && section.getLimit() < 1 + section.getEnrollments(getAssignment()).size()) {
    					if (iTweakLimits) {
    						section.setLimit(section.getEnrollments(getAssignment()).size() + 1);
    						section.clearReservationCache();
    						iProgress.info("Limit of " + section.getSubpart().getConfig().getOffering().getName() + " section " + section.getSubpart().getName() + " " + section.getName() + " increased to "+section.getLimit());
    					} else {
    						hasLimit = true;
    					}
	           		}
	           		if (!section.isEnabled(student)) hasDisabled = true;
	           	}
	           	if (enrl.getConfig().getLimit() >= 0 && enrl.getConfig().getLimit() < 1 + enrl.getConfig().getEnrollments(getAssignment()).size()) {
           			if (iTweakLimits) {
    					enrl.getConfig().setLimit(enrl.getConfig().getEnrollments(getAssignment()).size() + 1);
    					enrl.getConfig().clearReservationCache();
    					iProgress.info("Limit of " + enrl.getConfig().getOffering().getName() + " configuration " + enrl.getConfig().getName() + " increased to "+enrl.getConfig().getLimit());
           			} else {
           				hasLimit = true;
           			}
           		}
           		if (enrl.getCourse() != null && enrl.getCourse().getLimit() >= 0 && enrl.getCourse().getLimit() < 1 + enrl.getCourse().getEnrollments(getAssignment()).size()) {
           			if (iTweakLimits) {
    					enrl.getCourse().setLimit(enrl.getCourse().getEnrollments(getAssignment()).size() + 1);
    					iProgress.info("Limit of " + enrl.getConfig().getOffering().getName() + " course increased to " + enrl.getCourse().getLimit());
    				} else {
    					hasLimit = true;
    				}
           		}
       			if (iAllowToKeepCurrentEnrollment && student.hasMaxCredit() && credit > student.getMaxCredit()) {
       				student.setMaxCredit(credit);
       				iProgress.info("Max credit increased to " + credit + " for " + student.getName() + " (" + student.getExternalId() + ") ");
       			}
       			if (cr.isNotAllowed(enrl)) {
       				iProgress.info("Created an override restriction for " + cr.getName() + " of " + student.getName() + " (" + student.getExternalId() + ") ");
       				IndividualRestriction restriction = new IndividualRestriction(--iMakeupReservationId, enrl.getOffering(), student.getId());
       				for (Section section: enrl.getSections())
       					restriction.addSection(section);
       			}
       			if (enrl.getReservation() != null && enrl.getReservation().canBreakLinkedSections()) {
       				hasLinked = true;
       			} else {
       				for (LinkedSections ls: enrl.getStudent().getLinkedSections()) {
           				if (ls.inConflict(getAssignment(), enrl) != null)
           					hasLinked = true;
           			}
       			}
           		if (iAllowToKeepCurrentEnrollment) {
               		Reservation reservation = new ReservationOverride(--iMakeupReservationId, enrl.getOffering(), student.getId());
               		reservation.setPriority(0); // top priority -- use this reservation to get in!
               		if (hasLimit) reservation.setCanAssignOverLimit(true);
               		if (hasOverlap) reservation.setAllowOverlap(true);
    				if (hasDisabled) reservation.setAllowDisabled(hasDisabled);
    				if (hasMustUse) { reservation.setMustBeUsed(true); reservation.setExpired(false); }
    				else { reservation.setExpired(true); }
    				if (hasLinked) { reservation.setBreakLinkedSections(true); }
    				Set<String> props = new TreeSet<String>();
    				if (reservation.mustBeUsed()) props.add("mustBeUsed");
    				if (reservation.isAllowOverlap()) props.add("allowOverlap");
    				if (reservation.canAssignOverLimit()) props.add("allowOverLimit");
    				if (reservation.isAllowDisabled()) props.add("allowDisabled");
    				if (reservation.isExpired()) props.add("expired");
    				if (reservation.canBreakLinkedSections()) props.add("canBreakLinkedSections");
    				iProgress.info("Created an override reservation for " + cr.getName() + " of " + student.getName() + " (" + student.getExternalId() + ") " + props);
    				for (Section section: enrl.getSections())
    					reservation.addSection(section);
           		}
				enrl.guessReservation(getAssignment(), true);
				if (r.getModel().conflictValues(getAssignment(), enrl).isEmpty()) {
	    			getAssignment().assign(0, enrl);
	    			continue;
	    		}
    		}
           	iProgress.error("There is a problem assigning " + cr.getName() + " to " + student.getName() + " (" + student.getExternalId() + ")" );
           	boolean hasLimit = false, hasOverlap = false;
           	for (Iterator<Section> i = enrl.getSections().iterator(); i.hasNext();) {
           		Section section = i.next();
           		iProgress.info("  " + section.getSubpart().getName() + " " + section.getName() + (section.getTime() == null ? "" : " " + section.getTime().getLongName(iUseAmPm)));
           		if (section.getTime() != null) {
           			if (!student.isAvailable(section)) {
           				for (Unavailability unavailability: student.getUnavailabilities())
           		            if (unavailability.isOverlapping(section)) {
           		            	Section sectionx = unavailability.getSection();
           		            	iProgress.info("    student is not available due to " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
               							sectionx.getName() + " " + sectionx.getTime().getLongName(iUseAmPm));
           		            }
           				hasOverlap = true;
           			}
               		for (Request q: student.getRequests()) {
               			Enrollment enrlx = getAssignment().getValue(q);
               			if (enrlx == null || !(q instanceof CourseRequest)) continue;
               			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
               				Section sectionx = j.next();
               				if (sectionx.getTime() == null) continue;
               				if (sectionx.isOverlapping(section)) {
               					iProgress.info("    overlaps with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
               							sectionx.getName() + " " + sectionx.getTime().getLongName(iUseAmPm));
               					hasOverlap = true;
               				}
               			}
               		}
           		}
           		if (section.getLimit() >= 0 && section.getLimit() < 1 + section.getEnrollments(getAssignment()).size()) {
           			iProgress.info("    has no space available (limit is "+ section.getLimit() + ")");
           			hasLimit = true;
           		}
           	}
           	if (enrl.getConfig().getLimit() >= 0 && enrl.getConfig().getLimit() < 1 + enrl.getConfig().getEnrollments(getAssignment()).size()) {
           		iProgress.info("  config " + enrl.getConfig().getName() + " has no space available (limit is "+ enrl.getConfig().getLimit() + ")");
       			hasLimit = true;
           	}
           	if (enrl.getCourse() != null && enrl.getCourse().getLimit() >= 0 && enrl.getCourse().getLimit() < 1 + enrl.getCourse().getEnrollments(getAssignment()).size()) {
           		iProgress.info("  course " + enrl.getCourse().getName() + " has no space available (limit is "+ enrl.getCourse().getLimit() + ")");
       			hasLimit = true;
           	}
           	if (!hasLimit && !hasOverlap) {
           		for (Map.Entry<Constraint<Request, Enrollment>, Set<Enrollment>> e: r.getModel().conflictConstraints(getAssignment(), r.getInitialAssignment()).entrySet()) {
           			for (Iterator<Enrollment> i = e.getValue().iterator(); i.hasNext();) {
               			Enrollment enrlx = i.next();
               			for (Iterator<Section> j = enrlx.getSections().iterator(); j.hasNext();) {
               				Section sectionx = j.next();
               				iProgress.info("    conflicts with " + sectionx.getSubpart().getConfig().getOffering().getName() + " " + sectionx.getSubpart().getName() + " " +
           							sectionx.getName() + (sectionx.getTime() == null ? "" : " " + sectionx.getTime().getLongName(iUseAmPm)) +
           							" due to " + e.getKey().getClass().getSimpleName());
               			}
           				if (enrlx.getRequest().getStudent().getId() != student.getId())
           					iProgress.info("    of a different student (" + enrlx.getRequest().getStudent().getExternalId() + ")");
               		}
           		}
           	}
    	}
    }
    
    private boolean isParent(Request r1, Request r2) {
		if (r1 instanceof CourseRequest && r2 instanceof CourseRequest && !r1.equals(r2)) {
			CourseRequest cr1 = (CourseRequest)r1;
			CourseRequest cr2 = (CourseRequest)r2;
			for (Course course: cr2.getCourses())
				if (course.hasParent() && cr1.hasCourse(course.getParent())) return true;
		}
		return false;
    }
    
    public void reorderStudentRequests(Student student) {
    	int assigned = 0, critical = 0, freetime = 0, dependents = 0;
    	for (Request r: student.getRequests()) {
    		if (r instanceof CourseRequest) {
    			if (r.getInitialAssignment() != null && getAssignment().getValue(r) != null)
    				assigned ++;
    			if (r.isCritical() && !r.isAlternative())
    				critical ++;
    			for (Course c: ((CourseRequest)r).getCourses())
    				if (c.hasParent()) {
    					dependents ++; break;
    				}
    		} else if (r instanceof FreeTimeRequest) {
    			freetime ++;
    		}
    	}
    	if (iMoveParentCoursesUp && dependents > 0) {
    		for (Request r1: student.getRequests()) {
        		if (r1 instanceof CourseRequest && r1.hasChildren()) {
        			for (Request r2: student.getRequests()) {
        				if (isParent(r1, r2) && !r1.getRequestPriority().isSame(r2)) {
        					CourseRequest cr1 = (CourseRequest)r1;
        					CourseRequest cr2 = (CourseRequest)r2;
        					RequestPriority rp = (cr1.getRequestPriority().isHigher(r2) ? cr1.getRequestPriority() : cr2.getRequestPriority());
        					cr1.setRequestPriority(rp);
        					cr2.setRequestPriority(rp);
        				}
        			}
        		}
    		}
    	}
    	if ((getModel().isMPP() && getModel().getKeepInitialAssignments() && assigned > 0) || (iMoveCriticalCoursesUp && critical > 0) || (iMoveFreeTimesDown && freetime > 0) || (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) || (iMoveParentCoursesUp && dependents > 0)) {
			Collections.sort(student.getRequests(), new Comparator<Request>() {
				@Override
				public int compare(Request r1, Request r2) {
					if (r1.isAlternative() != r2.isAlternative()) return r1.isAlternative() ? 1 : -1;
					if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) {
						boolean a1 = (r1 instanceof CourseRequest && r1.getInitialAssignment() != null && getAssignment().getValue(r1) != null && !r1.getInitialAssignment().getCourse().getName().matches(iMPPCoursesRegExp));
						boolean a2 = (r2 instanceof CourseRequest && r2.getInitialAssignment() != null && getAssignment().getValue(r2) != null && !r2.getInitialAssignment().getCourse().getName().matches(iMPPCoursesRegExp));
						if (a1 != a2) {
							return a1 ? -1 : 1;
						}
					} else if (getModel().isMPP() && getModel().getKeepInitialAssignments()) {
						boolean a1 = (r1 instanceof CourseRequest && r1.getInitialAssignment() != null && getAssignment().getValue(r1) != null);
						boolean a2 = (r2 instanceof CourseRequest && r2.getInitialAssignment() != null && getAssignment().getValue(r2) != null);
						if (a1 != a2) return a1 ? -1 : 1;
					}
					if (iMoveParentCoursesUp) {
						boolean p1 = isParent(r1, r2);
						boolean p2 = isParent(r2, r1);
						if (p1 && !p2) return -1;
						if (p2 && !p1) return 1;
					}
					if (iMoveCriticalCoursesUp) {
						RequestPriority p1 = r1.getRequestPriority();
						RequestPriority p2 = r2.getRequestPriority();
						if (p1 != p2) {
							return (p1 == null ? RequestPriority.Normal : p1).compareCriticalsTo(p2 == null ? RequestPriority.Normal : p2);
						}
					}
					if (iMoveFreeTimesDown) {
						boolean f1 = (r1 instanceof FreeTimeRequest);
						boolean f2 = (r2 instanceof FreeTimeRequest);
						if (f1 != f2) return f1 ? 1 : -1;
					}
					return r1.getPriority() < r2.getPriority() ? -1 : 1;
				}
			});
			int p = 0;
			for (Request r: student.getRequests())
				r.setPriority(p++);
		}
    }
    
    private String curriculum(Student student) {
    	if (!student.getAreaClassificationMajors().isEmpty()) {
    		AreaClassificationMajor acm = student.getAreaClassificationMajors().get(0);
    		return acm.getArea() + ":" + acm.getClassification() + ":" + acm.getMajor();
    	}
    	return "";
    }
    
    private String curriculum(org.unitime.timetable.model.Student student) {
    	String curriculum = "";
    	for (StudentAreaClassificationMajor aac: student.getAreaClasfMajors()) {
    		return aac.getAcademicArea().getAcademicAreaAbbreviation() + ":" + aac.getAcademicClassification().getCode() + ":" + aac.getMajor().getCode();
    	}
    	return curriculum;
    }
    
    Map<Long, Map<String, Integer>> iCourse2Curricula2Weight = new Hashtable<Long, Map<String, Integer>>();
    private void updateCurriculumCounts(Student student) {
    	String curriculum = curriculum(student);
    	for (Request request: student.getRequests()) {
    		if (request instanceof CourseRequest) {
    			Course course = (request.getInitialAssignment() != null ? request.getInitialAssignment().getCourse() : ((CourseRequest)request).getCourses().get(0));
    			Map<String, Integer> c2w = iCourse2Curricula2Weight.get(course.getId());
    			if (c2w == null) {
    				c2w = new Hashtable<String, Integer>();
    				iCourse2Curricula2Weight.put(course.getId(), c2w);
    			}
    			Integer cx = c2w.get(curriculum);
    			c2w.put(curriculum, 1 + (cx == null ? 0 : cx));
    		}
    	}
    }
    
    private void updateCurriculumCounts(org.unitime.timetable.model.Student student) {
    	String curriculum = curriculum(student);
    	Set<Long> courses = new HashSet<Long>();
    	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
    		Long courseId = enrollment.getCourseOffering().getUniqueId();
    		if (courses.add(courseId)) {
    			Map<String, Integer> c2w = iCourse2Curricula2Weight.get(courseId);
    			if (c2w == null) {
    				c2w = new Hashtable<String, Integer>();
    				iCourse2Curricula2Weight.put(courseId, c2w);
    			}
    			Integer cx = c2w.get(curriculum);
    			c2w.put(curriculum, 1 + (cx == null ? 0 : cx));
    		}
    	}
    	demands: for (CourseDemand demand: student.getCourseDemands()) {
    		org.unitime.timetable.model.CourseRequest request = null;
    		for (org.unitime.timetable.model.CourseRequest r: demand.getCourseRequests()) {
    			if (courses.contains(r.getCourseOffering().getUniqueId())) continue demands;
    			if (request == null || r.getOrder() < request.getOrder())
    				request = r;
    		}
    		if (request != null) {
    			Long courseId = request.getCourseOffering().getUniqueId();
        		courses.add(courseId);
    			Map<String, Integer> c2w = iCourse2Curricula2Weight.get(courseId);
    			if (c2w == null) {
    				c2w = new Hashtable<String, Integer>();
    				iCourse2Curricula2Weight.put(courseId, c2w);
    			}
    			Integer cx = c2w.get(curriculum);
    			c2w.put(curriculum, 1 + (cx == null ? 0 : cx));
    		}
    	}
    }
    
    private void fixWeights(org.hibernate.Session hibSession, Collection<Course> courses) {
    	setPhase("Computing projected request weights...", courses.size());
    	for (Course course: courses) {
    		incProgress();
    		
    		Map<String, Integer> cur2real = iCourse2Curricula2Weight.get(course.getId());
    		if (cur2real != null) {
        		Map<String, Double> cur2proj = new Hashtable<String, Double>();
        		for (CourseRequest request: course.getRequests()) {
        			if (!request.getCourses().get(0).equals(course) || !request.getStudent().isDummy()) continue;
    				Double proj = cur2proj.get(curriculum(request.getStudent()));
    				cur2proj.put(curriculum(request.getStudent()), request.getWeight() + (proj == null ? 0.0 : proj));
        		}
        		
        		for (String cur: cur2proj.keySet()) {
        			double proj = cur2proj.get(cur);
        			Integer real = cur2real.get(cur);
        			if (real == null) continue;
        			iProgress.debug("Projected demands for course " + course.getName() + ": " + cur.replace(':', ' ') + " multiplies by " + (real >= proj ? 0.0 : (proj - real) / proj) + " (projected=" + proj + ", real=" + real + ")");
        		}
        		
        		for (CourseRequest request: course.getRequests()) {
        			if (!request.getCourses().get(0).equals(course) || !request.getStudent().isDummy()) continue;
        			double proj = cur2proj.get(curriculum(request.getStudent()));
        			Integer real = cur2real.get(curriculum(request.getStudent()));
        			if (real == null) continue;
        			request.setWeight(request.getWeight() * (real >= proj ? 0.0 : (proj - real) / proj));
        		}
    		}

    		double nrStudents = 0.0;
    		double nrLastLike = 0.0;
    		int lastLikeCount = 0;
    		for (CourseRequest request: course.getRequests()) {
    			if (!request.getCourses().get(0).equals(course)) continue;
    			nrStudents += request.getWeight();
    			if (request.getStudent().isDummy()) {
    				nrLastLike += request.getWeight();
    				lastLikeCount ++;
    			}
    		}
    		
	        double projected = course.getProjected();
	        double limit = course.getLimit();
	        if (limit >= 9999) limit = -1;
	        
	        int configLimit = 0;
	        for (Config config: course.getOffering().getConfigs()) {
	        	if (config.getLimit() < 0 || config.getLimit() >= 9999) { configLimit = -1; break; }
	        	int cLimit = config.getLimit();
	        	for (Subpart subpart: config.getSubparts()) {
	        		int subpartLimit = 0;
	        		for (Section section: subpart.getSections()) {
	        			if (section.getLimit() < 0 || section.getLimit() >= 9999) { subpartLimit = -1; break; }
	        			subpartLimit += section.getLimit();
	        		}
	        		if (subpartLimit >= 0 && subpartLimit < cLimit) { cLimit = subpartLimit; }
	        	}
	        	configLimit += cLimit;
	        }
	        
	        if (course.getOffering().getCourses().size() > 1) {
		        int offeringLimit = 0;
		        for (Course c: course.getOffering().getCourses()) {
		        	if (c.getLimit() < 0 || c.getLimit() >= 9999) { offeringLimit = -1; break; }
	        		offeringLimit += c.getLimit();
		        }
		        if (configLimit >= 0 && configLimit < offeringLimit)
		        	limit = (limit * configLimit) / offeringLimit;
		        else if (configLimit >= 0 && offeringLimit < 0)
		        	limit = configLimit / course.getOffering().getCourses().size();
	        } else {
	        	if (configLimit >= 0 && (configLimit < limit || limit < 0))
	        		limit = configLimit;
	        }
	        
	        if (limit < 0) {
	            iProgress.debug("Course " + course.getName() + " is unlimited.");
	            continue;
	        }
	        if (projected <= 0) {
	        	iProgress.info("No projected demand for course " + course.getName() + ", using course limit (" + Math.round(limit) + ")");
	            projected = limit;
	        } else if (limit < projected) {
	        	if (!iProjections)
	        		iProgress.info("Projected number of students is over course limit for course " + course.getName() + " (" + Math.round(projected) + ">" + Math.round(limit) + ")");
	        	projected = limit;
	        }
    		if (lastLikeCount <= 0) {
				iProgress.info("No projected course demands for course " + course.getName());
    			continue;
    		}
	        double weight = (nrLastLike <= 0 ? 0.0 : Math.max(0.0, projected - (nrStudents - nrLastLike)) / nrLastLike);
	        iProgress.debug("Projected student weight for " + course.getName() + " is " + weight + " (projected=" + nrLastLike + ", real=" + (nrStudents - nrLastLike) + ", limit=" + projected + ")");
	        int left = 0;
	        for (CourseRequest request: new ArrayList<CourseRequest>(course.getRequests())) {
	        	if (request.getStudent().isDummy()) {
	        		request.setWeight(weight * request.getWeight());
	        		if (request.getWeight() <= 0.0) {
	                    Student student = request.getStudent();
	                    getModel().removeVariable(request);
	                    student.getRequests().remove(request);
	                    for (Course c: request.getCourses())
	                    	c.getRequests().remove(request);
	                    if (student.getRequests().isEmpty())
	                    	getModel().removeStudent(student);
	        		} else {
	        			left++;
	        		}
	        	}
	        }
	        if (left <= 0)
	        	iProgress.info("No projected course demands needed for course " + course.getName());
    	}
    	getModel().requestWeightsChanged(getAssignment());
    }
    
    
    public void loadStudentInfo(Student student, org.unitime.timetable.model.Student s) {
        for (StudentAreaClassificationMajor acm: s.getAreaClasfMajors()) {
            // student.getAcademicAreaClasiffications().add(new AcademicAreaCode(acm.getAcademicArea().getAcademicAreaAbbreviation(),acm.getAcademicClassification().getCode()));
            // student.getMajors().add(new AcademicAreaCode(acm.getAcademicArea().getAcademicAreaAbbreviation(),acm.getMajor().getCode()));
        	student.getAreaClassificationMajors().add(new AreaClassificationMajor(
        			acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle(),
        			acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName(),
        			acm.getMajor().getCode(), acm.getMajor().getName(),
        			(acm.getConcentration() == null ? null : acm.getConcentration().getCode()), (acm.getConcentration() == null ? null : acm.getConcentration().getName()),
        			(acm.getDegree() == null ? null : acm.getDegree().getReference()), (acm.getDegree() == null ? null : acm.getDegree().getLabel()),
        			(acm.getProgram() == null ? null : acm.getProgram().getReference()), (acm.getProgram() == null ? null : acm.getProgram().getLabel()),
        			(acm.getCampus() == null ? null : acm.getCampus().getReference()), (acm.getCampus() == null ? null : acm.getCampus().getLabel()),
        			acm.getWeight()));
        }
        for (StudentAreaClassificationMinor acm: s.getAreaClasfMinors()) {
        	student.getAreaClassificationMinors().add(new AreaClassificationMajor(
        			acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle(),
        			acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName(),
        			acm.getMinor().getCode(), acm.getMinor().getName()));
        }
        for (StudentGroup g: s.getGroups()) {
        	student.getGroups().add(new org.cpsolver.studentsct.model.StudentGroup(g.getType() == null ? null : g.getType().getReference(), g.getGroupAbbreviation(), g.getGroupName()));
        }
        for (StudentAccomodation a: s.getAccomodations())
        	student.getAccommodations().add(a.getAbbreviation());
        for (Advisor a: s.getAdvisors())
        	student.getAdvisors().add(new Instructor(0, a.getExternalUniqueId(), a.getLastName() == null ? null : iInstructorNameFormat.format(a), a.getEmail()));
    }
    
    public void loadAdvisorWaitLists(Student student, org.unitime.timetable.model.Student s) {
    	for (AdvisorCourseRequest acr: s.getAdvisorCourseRequests()) {
    		if (acr.getWaitlist() != null && acr.getWaitlist().booleanValue() && acr.getCourseOffering() != null) {
    			for (Request r: student.getRequests()) {
    				if (r.isAlternative() || !(r instanceof CourseRequest)) continue;
    				CourseRequest cr = (CourseRequest)r;
    				if (cr.getCourse(acr.getCourseOffering().getUniqueId()) != null) {
    					cr.setWaitlist(true);
    					iProgress.debug(iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): " + cr.getName() + " marked as wait-listed.");
    				}
    			}
    		}
    	}
    }
    
    public void loadAdvisorNoSubs(Student student, org.unitime.timetable.model.Student s) {
    	for (AdvisorCourseRequest acr: s.getAdvisorCourseRequests()) {
    		if (acr.getNoSub() != null && acr.getNoSub().booleanValue() && acr.getCourseOffering() != null) {
    			for (Request r: student.getRequests()) {
    				if (r.isAlternative() || !(r instanceof CourseRequest)) continue;
    				CourseRequest cr = (CourseRequest)r;
    				if (cr.getCourse(acr.getCourseOffering().getUniqueId()) != null) {
    					cr.setWaitlist(true);
    					iProgress.debug(iStudentNameFormat.format(s) + " (" + s.getExternalUniqueId() + "): " + cr.getName() + " marked as wait-listed.");
    				}
    			}
    		}
    	}
    }
    
    public void loadRequestGroups(Student student, org.unitime.timetable.model.Student s) {
        for (StudentGroup g: s.getGroups()) {
        	if (iRequestGroupRegExp != null && !iRequestGroupRegExp.isEmpty() && !g.getGroupName().matches(iRequestGroupRegExp)) continue;
        	if (g.getType() != null && !g.getType().isKeepTogether()) continue;
        	for (Request r: student.getRequests()) {
        		if (r instanceof CourseRequest) {
        			CourseRequest cr = (CourseRequest)r;
        			Course course = cr.getCourses().get(0);
        			RequestGroup group = null;
        			for (RequestGroup rg: course.getRequestGroups()) {
        				if (rg.getId() == g.getUniqueId()) { group = rg; break; }
        			}
        			if (group == null)
        				group = new RequestGroup(g.getUniqueId(), g.getGroupName(), course);
        			cr.addRequestGroup(group);
        		}
        	}
        }
    }
    
	public static BitSet getFreeTimeBitSet(Session session) {
		int startMonth = session.getPatternStartMonth();
		int endMonth = session.getPatternEndMonth();
		int size = DateUtils.getDayOfYear(0, endMonth + 1, session.getSessionStartYear()) - DateUtils.getDayOfYear(1, startMonth, session.getSessionStartYear());
		BitSet ret = new BitSet(size);
		for (int i = 0; i < size; i++)
			ret.set(i);
		return ret;
	}
	
    private String datePatternName(DatePattern dp, TimeLocation time) {
    	if ("never".equals(iDatePatternFormat)) return dp.getName();
    	if ("extended".equals(iDatePatternFormat) && !dp.isExtended()) return dp.getName();
    	if ("alternate".equals(iDatePatternFormat) && dp.isAlternate()) return dp.getName();
    	if (time == null) {
    		Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
    		Date first = dp.getStartDate();
    		Date last = dp.getEndDate();
    		return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    	}
    	if (time.getWeekCode().isEmpty()) return time.getDatePatternName();
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(iDatePatternFirstDate);
    	int idx = time.getWeekCode().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date first = null;
    	while (idx < time.getWeekCode().size() && first == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return time.getDatePatternName();
    	cal.setTime(iDatePatternFirstDate);
    	idx = time.getWeekCode().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return time.getDatePatternName();
        Formats.Format<Date> dpf = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
    	return dpf.format(first) + (first.equals(last) ? "" : " - " + dpf.format(last));
    }

	public static Date getDatePatternFirstDay(Session s) {
		return DateUtils.getDate(1, s.getPatternStartMonth(), s.getSessionStartYear());
	}
	
	public static interface SectionProvider {
		public Section get(Long classId);
	}
	
	public static List<Collection<Section>> getSections(DistributionPref pref, SectionProvider classTable) {
		List<Collection<Section>> ret = new ArrayList<Collection<Section>>();
		DistributionPref.Structure structure = pref.getStructure();
		if (structure == null) structure = DistributionPref.Structure.AllClasses;
    	if (structure == DistributionPref.Structure.Progressive) {
    		int maxSize = 0;
    		for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_)
        			maxSize = Math.max(maxSize, 1);
        		else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart)
        			maxSize = Math.max(maxSize, ((SchedulingSubpart)distributionObject.getPrefGroup()).getClasses().size());
    		}
    		Set<Section> sections[] = new Set[maxSize];
    		for (int i=0;i<sections.length;i++)
    			sections[i] = new HashSet<Section>();

    		List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>(pref.getDistributionObjects());
    		Collections.sort(distributionObjects, new TimetableDatabaseLoader.ChildrenFirstDistributionObjectComparator());
    		for (DistributionObject distributionObject: distributionObjects) {
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Section section = classTable.get(distributionObject.getPrefGroup().getUniqueId());
        			if (section!=null)
        				for (int j = 0; j < sections.length; j++)
        					sections[j].add(section);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (int j = 0; j < sections.length; j++) {
        	    		Section section = null;
        	    		sections: for (Section s: sections[j]) {
        	    			Section p = s.getParent();
        	    			while (p != null) {
        	    				if (p.getSubpart().getId() == subpart.getUniqueId()) {
        	    					section = s;
        	    					break sections;
        	    				}
        	    				p  = p.getParent();
        	    			}
        	    		}
        	    		if (section == null)
        	    			section = classTable.get(classes.get(j%classes.size()).getUniqueId());
        	    		if (section!=null)
        	    			sections[j].add(section);
        	    	}
        		}
    		}
    		for (Set<Section> s: sections)
    			ret.add(s);
    	} else if (structure == DistributionPref.Structure.OneOfEach) {
    		List<Section> sections = new ArrayList<Section>();
    		List<Integer> counts = new ArrayList<Integer>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
    			int count = 0;
    			if (distributionObject.getPrefGroup() instanceof Class_) {
        			Section section = classTable.get(distributionObject.getPrefGroup().getUniqueId());
        			if (section != null) {
        				sections.add(section); count ++;
        			}
    			} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        			List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        			for (Class_ clazz: classes) {
        	    		Section section = classTable.get(clazz.getUniqueId());
	        			if (section != null) {
	        				sections.add(section); count ++;
	        			}
        	    	}
        		}
    			if (count > 0) counts.add(count);
        	}
        	if (counts.size() > 1) {
        		for (Enumeration<List<Section>> e = DistributionPref.permutations(sections, counts); e.hasMoreElements(); )
        			ret.add(e.nextElement());
        	}
    	} else {
    		List<Section> sections = new ArrayList<Section>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Section section = classTable.get(distributionObject.getPrefGroup().getUniqueId());
        			if (section != null)
        				sections.add(section);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Class_ clazz: classes) {
        	    		Section section = classTable.get(clazz.getUniqueId());
	        			if (section != null)
	        				sections.add(section);
        	    	}        	        	    		
        		}
        	}
        	if (structure == DistributionPref.Structure.Pairwise) {
	        	if (sections.size() >= 2) {
	        		for (int idx1 = 0; idx1 < sections.size() - 1; idx1++) {
	        			Section s1 = sections.get(idx1);
	        			for (int idx2 = idx1 + 1; idx2 < sections.size(); idx2++) {
	        				Section s2 = sections.get(idx2);
	        				Set<Section> s = new HashSet<Section>();
	        				s.add(s1); s.add(s2);
	        				ret.add(s);
	        			}
	        		}
	        	}
			} else if (structure == DistributionPref.Structure.AllClasses) {
				ret.add(sections);
			} else {
		    	int grouping = 2;
		    	switch (structure) {
		    	case GroupsOfTwo: grouping = 2; break;
		    	case GroupsOfThree: grouping = 3; break;
		    	case GroupsOfFour: grouping = 4; break;
		    	case GroupsOfFive: grouping = 5; break;
		    	}
				List<Section> s = new ArrayList<Section>();
				for (Section section: sections) {
					s.add(section);
					if (s.size() == grouping) {
						ret.add(s); s = new ArrayList<Section>();
					}
				}
				if (s.size() >= 2)
					ret.add(new HashSet<Section>(s));
			}
	    }		
		return ret;
	}
	
	protected boolean isFaceToFace(CourseRequest r) {
		if (iVisitingFaceToFaceCheckFirstChoiceOnly) {
			Course firstChoice = r.getCourses().get(0);
			for (Enrollment e: r.values(getAssignment()))
				for (Section s: e.getSections())
					if (!s.isOnline() && e.getCourse().equals(firstChoice)) return true;
		} else {
			for (Enrollment e: r.values(getAssignment()))
				for (Section s: e.getSections())
					if (!s.isOnline()) return true;
		}
		return false;
	}
	
    public void load(Session session, org.hibernate.Session hibSession) {
    	iFreeTimePattern = getFreeTimeBitSet(session);
		iDatePatternFirstDate = getDatePatternFirstDay(session);
    	
        Hashtable<Long, Course> courseTable = new Hashtable<Long, Course>();
        final Hashtable<Long, Section> classTable = new Hashtable<Long, Section>();
        List<InstructionalOffering> offerings = hibSession.createQuery(
                "select distinct io from InstructionalOffering io " +
                "left join io.courseOfferings as co "+
                "left join fetch io.instrOfferingConfigs as ioc "+
                "left join fetch ioc.schedulingSubparts as ss "+
                "left join fetch ss.classes as c "+
                "left join fetch io.reservations as r "+
                "where " +
                "io.session.uniqueId = :sessionId and io.notOffered = false and co.subjectArea.department.allowStudentScheduling = true", InstructionalOffering.class).
                setParameter("sessionId", session.getUniqueId().longValue()).
                setFetchSize(1000).list();
        setPhase("Loading course offerings...", offerings.size());
        for (InstructionalOffering io: offerings) {
        	incProgress();
            Offering offering = loadOffering(io, courseTable, classTable, 0);
            if (offering!=null) getModel().addOffering(offering);
        }
        
        setPhase("Loading parent coursess...", offerings.size());
        for (InstructionalOffering io: offerings) {
        	incProgress();
        	boolean hasParent = false;
        	for (CourseOffering co: io.getCourseOfferings()) {
        		if (co.getParentOffering() != null) {
        			Course course = courseTable.get(co.getUniqueId());
        			if (course == null) continue;
        			Course parent = courseTable.get(co.getParentOffering().getUniqueId());
        			if (parent == null) {
        				iProgress.warn("Prerequisite course " + co.getParentOffering().getCourseName() + " for " + co.getCourseName() + " did not get loaded.");
        			} else {
        				course.setParent(parent);
        				hasParent = true;
        			}
        		}
        	}
        	if (hasParent && getModel().getProperties().getPropertyBoolean("Sectioning.DependentCourses", true)) 
        		getModel().addGlobalConstraint(new DependentCourses());
        }
        
        List<DistributionPref> distPrefs = hibSession.createQuery(
        		"select p from DistributionPref p, Department d where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId" +
        		" and p.owner = d and p.prefLevel.prefProlog = :pref", DistributionPref.class)
        		.setParameter("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference())
        		.setParameter("ref2", IgnoreStudentConflictsConstraint.REFERENCE)
        		.setParameter("pref", PreferenceLevel.sRequired)
        		.setParameter("sessionId", iSessionId)
        		.list();
        if (!distPrefs.isEmpty()) {
        	setPhase("Loading distribution preferences...", distPrefs.size());
        	SectionProvider p = new SectionProvider() {
				@Override
				public Section get(Long classId) {
					return classTable.get(classId);
				}
			};
        	for (DistributionPref pref: distPrefs) {
        		incProgress();
        		for (Collection<Section> sections: getSections(pref, p)) {
        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference())) {
        				getModel().addLinkedSections(iLinkedClassesMustBeUsed, sections);
        			} else {
        				for (Section s1: sections)
                			for (Section s2: sections)
                				if (!s1.equals(s2)) s1.addIgnoreConflictWith(s2.getId());
        			}
        		}
        	}
        }
        
        Map<String, Student> ext2student = new HashMap<String, Student>();
        Set<Student> onlineOnlyStudents = new HashSet<Student>();
        List<StudentSchedulingRule> rules = new ArrayList<StudentSchedulingRule>();
        for (StudentSchedulingRule rule: hibSession.createQuery("from StudentSchedulingRule order by ord", StudentSchedulingRule.class).list()) {
        	// ignore rules that do not apply to batch
			if (!rule.isAppliesToBatch()) continue;
			// check academic session
			if (!rule.matchSession(iTerm, iYear, iInitiative)) continue;
			rules.add(rule);
        }
        Map<StudentSchedulingRule, Set<Student>> rule2students = new HashMap<StudentSchedulingRule, Set<Student>>();
        Map<Student, StudentSchedulingRule> student2rule = new HashMap<Student, StudentSchedulingRule>();
        if (iIncludeCourseDemands || iProjections) {
            List<org.unitime.timetable.model.Student> students = hibSession.createQuery(
                    "select distinct s from Student s " +
/*                    "left join fetch s.courseDemands as cd "+
                    "left join fetch cd.courseRequests as cr "+
                    "left join fetch cr.classWaitLists as cw " +
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch s.waitlists as w " +
                    (iLoadStudentInfo ? "left join fetch s.areaClasfMajors as a left join fetch s.groups as g " : "") +*/
                    "where s.session.uniqueId=:sessionId", org.unitime.timetable.model.Student.class).
                    setParameter("sessionId", session.getUniqueId().longValue()).
                    setFetchSize(1000).list();
            if (iValidateOverrides && iValidationProvider != null) {
            	validateOverrides(hibSession, students);
            } else if (iCheckOverrideStatus && iValidationProvider != null) {
            	checkOverrideStatuses(hibSession, students);
            }
            if (iCheckCriticalCourses)
            	checkCriticalCourses(hibSession, students);
            
            setPhase("Loading student requests...", students.size());
            for (Iterator i=students.iterator();i.hasNext();) {
                org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)i.next(); incProgress();
                if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
                Student student = loadStudent(hibSession, s, courseTable, classTable);
                if (student == null) continue;
                if (iUseAdvisorWaitLists)
                	loadAdvisorWaitLists(student, s);
                else if (iUseAdvisorNoSubs)
                	loadAdvisorNoSubs(student, s);
                boolean hasRule = false;
                for (StudentSchedulingRule rule: rules) {
                	if (rule.getStudentFilter() != null && !rule.getStudentFilter().isEmpty() && !new Query(rule.getStudentFilter()).match(new DbStudentMatcher(s))) continue;
                	Set<Student> ruleStudents = rule2students.get(rule);
                	if (ruleStudents == null) {
                		ruleStudents = new HashSet<Student>();
                		rule2students.put(rule, ruleStudents);
                	}
                	ruleStudents.add(student);
            		student2rule.put(student, rule);
            		hasRule = true;
                	break;
                }
                if (!hasRule && iOnlineOnlyStudentQuery != null && iOnlineOnlyStudentQuery.match(new DbStudentMatcher(s)))
                	onlineOnlyStudents.add(student);
                updateCurriculumCounts(student);
                if (iProjections) {
                	// Decrease the limits accordingly
                	for (Request request: student.getRequests()) {
                		if (request.getInitialAssignment() != null && request.getInitialAssignment().isCourseRequest()) {
                			Enrollment enrollment = request.getInitialAssignment();
                			if (enrollment.getConfig().getLimit() > 0)
                				enrollment.getConfig().setLimit(enrollment.getConfig().getLimit() - 1);
                			for (Section section: enrollment.getSections())
                				if (section.getLimit() > 0)
                					section.setLimit(section.getLimit() - 1);
                			if (enrollment.getCourse() != null && enrollment.getCourse().getLimit() > 0)
                				enrollment.getCourse().setLimit(enrollment.getCourse().getLimit() - 1);
                			if (enrollment.getReservation() != null) {
                				if (enrollment.getReservation() instanceof GroupReservation && enrollment.getReservation().getReservationLimit() >= 1.0) {
                					((GroupReservation)enrollment.getReservation()).getStudentIds().remove(student.getId());
                					((GroupReservation)enrollment.getReservation()).setReservationLimit(((GroupReservation)enrollment.getReservation()).getReservationLimit() - 1.0);
                				} else if (enrollment.getReservation() instanceof IndividualReservation) {
                					((IndividualReservation)enrollment.getReservation()).getStudentIds().remove(student.getId());
                				} else if (enrollment.getReservation() instanceof CurriculumReservation && enrollment.getReservation().getReservationLimit() >= 1.0) {
                					((CurriculumReservation)enrollment.getReservation()).setReservationLimit(enrollment.getReservation().getReservationLimit() - 1.0);
                				} else if (enrollment.getReservation() instanceof UniversalOverride && enrollment.getReservation().getReservationLimit() >= 1.0) {
                					((UniversalOverride)enrollment.getReservation()).setReservationLimit(enrollment.getReservation().getReservationLimit() - 1.0);
                				}
                			}
                		}
                		if (request instanceof CourseRequest) {
                			for (Course course: ((CourseRequest)request).getCourses()) {
                				course.getRequests().remove(request);
                			}
                		}
                	}
                } else {
                	if (iLoadRequestGroups) loadRequestGroups(student, s);
                	if (student.getExternalId() != null && !student.getExternalId().isEmpty())
                		ext2student.put(student.getExternalId(), student);
                    getModel().addStudent(student);
                    // assignStudent(student);
                }
            }
        }
        
        if (iIncludeUnavailabilities) {
        	setPhase("Loading unavailabilities...", offerings.size());
        	for (InstructionalOffering offering: offerings) {
        		incProgress();
        		for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
        			for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
        				for (Class_ clazz: subpart.getClasses()) {
    						Section section = classTable.get(clazz.getUniqueId());
    						if (section == null || section.isCancelled() || section.getTime() == null) continue;
        					for (ClassInstructor ci: clazz.getClassInstructors()) {
        						if (ci.getInstructor().getExternalUniqueId() == null || ci.getInstructor().getExternalUniqueId().isEmpty()) continue;
        						Student student = ext2student.get(ci.getInstructor().getExternalUniqueId());
        						if (student != null) {
        							boolean canOverlap = !ci.isLead();
        							if (ci.getTeachingRequest() != null)
        								for (TeachingClassRequest tcr: ci.getTeachingRequest().getClassRequests())
        									if (tcr.getTeachingClass().equals(ci.getClassInstructing())) {
        										canOverlap = tcr.isCanOverlap(); break;
        									}
        							Unavailability ua = new Unavailability(student, section, canOverlap);
        							ua.setTeachingAssignment(true);
        						}
        			        }
        			        for (TeachingClassRequest tcr: clazz.getTeachingRequests()) {
        			        	if (!tcr.isAssignInstructor() && tcr.getTeachingRequest().isCommitted()) {
        			            	for (DepartmentalInstructor di: tcr.getTeachingRequest().getAssignedInstructors()) {
        			            		if (di.getExternalUniqueId() == null || di.getExternalUniqueId().isEmpty()) continue;
        			            		Student student = ext2student.get(di.getExternalUniqueId());
                						if (student != null) {
                							Unavailability ua = new Unavailability(student, section, tcr.isCanOverlap());
                							ua.setTeachingAssignment(true);
                						}
        			            	}
        			        	}
        			        }
        				}
        			}
        		}
        		
        	}
        }
        
        if (iIncludeUnavailabilitiesFromOtherSessions) {
            List<StudentClassEnrollment> enrollments = new ArrayList<StudentClassEnrollment>(hibSession.createQuery(
    				"select e2 " +
    				"from Student s1 inner join s1.session z1, StudentClassEnrollment e2 inner join e2.student s2 inner join s2.session z2 " +
    				"where s1.externalUniqueId is not null and z1.uniqueId = :sessionId and s1.externalUniqueId = s2.externalUniqueId and " +
    				"e2.clazz.cancelled = false and e2.clazz.committedAssignment is not null and " +
    				"z1 != z2 and z1.sessionBeginDateTime <= z2.classesEndDateTime and z2.sessionBeginDateTime <= z1.classesEndDateTime",
    				StudentClassEnrollment.class).setParameter("sessionId", session.getUniqueId()).list());
        	setPhase("Loading unavailabilities from other academic sessions...", enrollments.size());
        	Collections.sort(enrollments, new Comparator<StudentClassEnrollment>() {
        		ClassCourseComparator ccc = new ClassCourseComparator();
				@Override
				public int compare(StudentClassEnrollment e1, StudentClassEnrollment e2) {
					int cmp = e1.getStudent().compareTo(e2.getStudent());
					if (cmp != 0) return cmp;
					return ccc.compareClasses(e1.getCourseOffering(), e2.getCourseOffering(), e1.getClazz(), e2.getClazz());
				}
			});
        	for (StudentClassEnrollment enrollment: enrollments) {
        		Student student = ext2student.get(enrollment.getStudent().getExternalUniqueId());
        		if (student != null) {
        			Section section = classTable.get(enrollment.getClazz().getUniqueId());
        			if (section == null && !enrollment.getClazz().isCancelled() && enrollment.getClazz().getCommittedAssignment() != null) {
    					int shiftDays = DateUtils.daysBetween(
    							AcademicSessionInfo.getDatePatternFirstDay(session),
    							AcademicSessionInfo.getDatePatternFirstDay(enrollment.getCourseOffering().getInstructionalOffering().getSession()));
    					Offering offering = loadOffering(enrollment.getCourseOffering().getInstructionalOffering(), courseTable, classTable, shiftDays);
    		            if (offering != null) {
    		            	offering.setDummy(true);
    		            	getModel().addOffering(offering);
        					section = classTable.get(enrollment.getClazz().getUniqueId());
        					if (section != null && !section.isCancelled() && section.getTime() != null) {
        						Unavailability ua = new Unavailability(student, section, section.isAllowOverlap());
        						ua.setTeachingAssignment(false);
        						ua.setCourseId(enrollment.getCourseOffering().getUniqueId());
        					}
    		            }
        			} else if (section != null && !section.isCancelled() && section.getTime() != null) {
        				Unavailability ua = new Unavailability(student, section, section.isAllowOverlap());
        				ua.setTeachingAssignment(false);
						ua.setCourseId(enrollment.getCourseOffering().getUniqueId());
        			}
        		}
        	}
        }
        
        if (iDecreaseCreditLimitsByOtherSessionEnrollments) {
        	List<StudentClassEnrollment> enrollments = new ArrayList<StudentClassEnrollment>(hibSession.createQuery(
    				"select e2 " +
    				"from Student s1 inner join s1.session z1, StudentClassEnrollment e2 inner join e2.student s2 inner join s2.session z2 " +
    				"where s1.externalUniqueId is not null and z1.uniqueId = :sessionId and s1.externalUniqueId = s2.externalUniqueId and " +
    				"e2.clazz.cancelled = false and " +
    				"z1.academicTerm = z2.academicTerm and z1.academicYear = z2.academicYear and z2.academicInitiative != z1.academicInitiative",
    				StudentClassEnrollment.class).setParameter("sessionId", session.getUniqueId()).list());
        	setPhase("Loading credits from other academic sessions...", enrollments.size());
        	Collections.sort(enrollments, new Comparator<StudentClassEnrollment>() {
        		ClassCourseComparator ccc = new ClassCourseComparator();
				@Override
				public int compare(StudentClassEnrollment e1, StudentClassEnrollment e2) {
					int cmp = e1.getStudent().compareTo(e2.getStudent());
					if (cmp != 0) return cmp;
					return ccc.compareClasses(e1.getCourseOffering(), e2.getCourseOffering(), e1.getClazz(), e2.getClazz());
				}
			});
			HasGradableSubpart gs = null;
			if (Class_.getExternalClassNameHelper() != null && Class_.getExternalClassNameHelper() instanceof HasGradableSubpart)
				gs = (HasGradableSubpart) Class_.getExternalClassNameHelper();
			else
				gs = new DefaultExternalClassNameHelper();
        	for (StudentClassEnrollment enrollment: enrollments) {
        		Student student = ext2student.get(enrollment.getStudent().getExternalUniqueId());
        		if (student != null && (student.hasMaxCredit() || student.hasMinCredit())) {
        			Float creditOverride = enrollment.getClazz().getCredit(enrollment.getCourseOffering());
        			if (creditOverride != null) {
        				if (student.hasMaxCredit())
        					student.setMaxCredit(Math.max(0, student.getMaxCredit() - creditOverride));
        				if (student.hasMinCredit())
        					student.setMinCredit(Math.max(0, student.getMinCredit() - creditOverride));
        				iProgress.debug("Decreasing credits by " + creditOverride + " for " + student.getName() + " (" + student.getExternalId() + ") for " + enrollment.getClazz().getClassLabel(enrollment.getCourseOffering()));
        			} else {
        				CourseCreditUnitConfig credit = enrollment.getClazz().getSchedulingSubpart().getCredit(); 
        				if (credit == null && gs.isGradableSubpart(enrollment.getClazz().getSchedulingSubpart(), enrollment.getCourseOffering(), hibSession)) {
        					credit = enrollment.getCourseOffering().getCredit();
        				}
        				if (credit != null) {
        					if (student.hasMaxCredit())
        						student.setMaxCredit(Math.max(0, student.getMaxCredit() - credit.getMinCredit()));
        					if (student.hasMinCredit())
        						student.setMinCredit(Math.max(0, student.getMinCredit() - credit.getMinCredit()));
            				iProgress.debug("Decreasing credits by " + credit.getMinCredit() + " for " + student.getName() + " (" + student.getExternalId() + ") for " + enrollment.getClazz().getClassLabel(enrollment.getCourseOffering()));
        				}
        			}
        		}
        	}
        }
        
        if (!rule2students.isEmpty()) {
        	for (Map.Entry<StudentSchedulingRule, Set<Student>> entry: rule2students.entrySet()) {
        		StudentSchedulingRule rule = entry.getKey();
        		Map<Course, Set<Long>> course2students = new HashMap<Course, Set<Long>>();
        		for (Student s: entry.getValue()) {
        			for (Request r: s.getRequests())
        				if (r instanceof CourseRequest)
        					for (Course c: ((CourseRequest)r).getCourses()) {
        						Set<Long> set = course2students.get(c);
        						if (set == null) {
        							set = new HashSet<Long>();
        							course2students.put(c, set);
        						}
        						set.add(s.getId());
        					}
        		}
        		setPhase("Creating " + rule.getRuleName() + " restrictions...", course2students.size());
            	for (Map.Entry<Course, Set<Long>> e: course2students.entrySet()) {
             		incProgress();
             		Course course = e.getKey();
             		if (rule.isDisjunctive()) {
             			if (rule.hasCourseName() && rule.matchesCourseName(course.getName())) {
    						// no restriction needed
    					} else if (rule.hasCourseType() && rule.matchesCourseType(course.getType())) {
    						// no restriction needed
    					} else if (rule.hasInstructionalMethod()) {
    						Offering offering = course.getOffering();
                    		List<Config> configs = new ArrayList<Config>();
                    		for (Config config: offering.getConfigs())
                    			if (rule.matchesInstructionalMethod(config.getInstructionalMethodReference()))
                    				configs.add(config);	
                    		if (configs.size() == offering.getConfigs().size()) {
                    			// student can take any configuration -> no need for an override
                    			continue;
                    		}
                    		Restriction r = new IndividualRestriction(--iMakeupReservationId, course.getOffering(), e.getValue());
                    		for (Config config: configs)
                    			r.addConfig(config);
    					} else {
    						// no match >> cannot take the course
    						new IndividualRestriction(--iMakeupReservationId, course.getOffering(), e.getValue());
    					}
             		} else {
             			if (!rule.matchesCourseName(course.getName()) || !rule.matchesCourseType(course.getType())) {
                 			new IndividualRestriction(--iMakeupReservationId, course.getOffering(), e.getValue()); 
                 		} else if (rule.hasInstructionalMethod()) {
                    		Offering offering = course.getOffering();
                    		List<Config> configs = new ArrayList<Config>();
                    		for (Config config: offering.getConfigs())
                    			if (rule.matchesInstructionalMethod(config.getInstructionalMethodReference()))
                    				configs.add(config);	
                    		if (configs.size() == offering.getConfigs().size()) {
                    			// student can take any configuration -> no need for an override
                    			continue;
                    		}
                    		Restriction r = new IndividualRestriction(--iMakeupReservationId, course.getOffering(), e.getValue());
                    		for (Config config: configs)
                    			r.addConfig(config);
                 		}	
             		}
            	}
        	}
        }
        if (!onlineOnlyStudents.isEmpty()) {
        	Map<Course, Set<Long>> course2students = new HashMap<Course, Set<Long>>();
    		for (Student s: onlineOnlyStudents) {
    			for (Request r: s.getRequests())
    				if (r instanceof CourseRequest)
    					for (Course c: ((CourseRequest)r).getCourses()) {
    						Set<Long> set = course2students.get(c);
    						if (set == null) {
    							set = new HashSet<Long>();
    							course2students.put(c, set);
    						}
    						set.add(s.getId());
    					}
    		}
    		setPhase("Creating online-only restrictions...", course2students.size());
        	for (Map.Entry<Course, Set<Long>> e: course2students.entrySet()) {
         		incProgress();
         		Course course = e.getKey();
         		if (iOnlineOnlyCourseNameRegExp != null && !iOnlineOnlyCourseNameRegExp.isEmpty() && !course.getName().matches(iOnlineOnlyCourseNameRegExp)) {
         			new IndividualRestriction(--iMakeupReservationId, course.getOffering(), e.getValue()); 
         		} else if (iOnlineOnlyInstructionalModeRegExp != null) {
            		Offering offering = course.getOffering();
            		List<Config> configs = new ArrayList<Config>();
            		for (Config config: offering.getConfigs()) {
            			if (iOnlineOnlyInstructionalModeRegExp.isEmpty()) {
            				if (config.getInstructionalMethodReference() == null || config.getInstructionalMethodReference().isEmpty())
            					configs.add(config);	
            			} else {
            				if (config.getInstructionalMethodReference() != null && config.getInstructionalMethodReference().matches(iOnlineOnlyInstructionalModeRegExp)) {
            					configs.add(config);
            				}
            			}
            		}
            		if (configs.size() == offering.getConfigs().size()) {
            			// student can take any configuration -> no need for an override
            			continue;
            		}
            		Restriction r = new IndividualRestriction(--iMakeupReservationId, course.getOffering(), e.getValue());
            		for (Config config: configs)
            			r.addConfig(config);
         		}
        	}
        }
        if (iOnlineOnlyStudentQuery != null && iOnlineOnlyExclusiveCourses && iOnlineOnlyCourseNameRegExp != null && !iOnlineOnlyCourseNameRegExp.isEmpty()) {
        	setPhase("Creating inverse online-only restrictions...", getModel().getOfferings().size());
        	for (Offering offering: getModel().getOfferings()) {
        		incProgress();
        		for (Course course: offering.getCourses()) {
        			if (course.getName().matches(iOnlineOnlyCourseNameRegExp)) {
            			Set<Long> studentIds = new HashSet<Long>();
            			for (CourseRequest cr: course.getRequests()) {
            				if (!onlineOnlyStudents.contains(cr.getStudent()) && !student2rule.containsKey(cr.getStudent()))
            					studentIds.add(cr.getStudent().getId());
            			}
            			if (!studentIds.isEmpty()) {
            				new IndividualRestriction(--iMakeupReservationId, course.getOffering(), studentIds);
            			}
        			}
        		}
        	}
        }
        if (iOnlineOnlyStudentQuery != null && iOnlineOnlyExclusiveCourses && iResidentialInstructionalModeRegExp != null) {
        	setPhase("Creating residential restrictions...", getModel().getOfferings().size());
        	for (Offering offering: getModel().getOfferings()) {
        		incProgress();
        		List<Config> configs = new ArrayList<Config>();
        		for (Config config: offering.getConfigs()) {
        			if (iResidentialInstructionalModeRegExp.isEmpty()) {
        				if (config.getInstructionalMethodReference() == null || config.getInstructionalMethodReference().isEmpty())
        					configs.add(config);	
        			} else {
        				if (config.getInstructionalMethodReference() != null && config.getInstructionalMethodReference().matches(iResidentialInstructionalModeRegExp)) {
        					configs.add(config);
        				}
        			}
        		}
        		if (configs.size() == offering.getConfigs().size()) {
        			// student can take any configuration -> no need for an override
        			continue;
        		}
        		Set<Long> studentIds = new HashSet<Long>();
        		for (Course course: offering.getCourses()) {
        			for (CourseRequest cr: course.getRequests()) {
        				if (!onlineOnlyStudents.contains(cr.getStudent()) && !student2rule.containsKey(cr.getStudent()))
        					studentIds.add(cr.getStudent().getId());
        			}
        		}
    			if (!studentIds.isEmpty()) {
    				Restriction r = new IndividualRestriction(--iMakeupReservationId, offering, studentIds);
    				for (Config config: configs)
    					r.addConfig(config);
    			}
        	}
        }
        
        for (LinkedSections c: getModel().getLinkedSections())
        	c.createConstraints();
        
        if (iVisitingStudentsQuery != null) {
        	setPhase("Checking for face-to-face courses for visiting students...", getModel().getStudents().size());
        	for (Student student: getModel().getStudents()) {
            	incProgress();
            	boolean visiting = iVisitingStudentsQuery != null && iVisitingStudentsQuery.match(new UniversalOverride.StudentMatcher(student));
            	if (visiting) {
            		for (Request r: student.getRequests()) {
            			if (r instanceof CourseRequest && r.getRequestPriority() == RequestPriority.Normal && isFaceToFace((CourseRequest)r))
            				getModel().setCourseRequestPriority((CourseRequest)r, RequestPriority.VisitingF2F);
            		}
            		if (student.getModalityPreference() == null || student.getModalityPreference() == ModalityPreference.NO_PREFERENCE)
            			student.setModalityPreference(ModalityPreference.ONLINE_PREFERRED);
            	}
        	}
        }
        
        setPhase("Assigning students...", getModel().getStudents().size());
        for (Student student: getModel().getStudents()) {
        	incProgress();
        	assignStudent(student);
        }
        
        setPhase("Checking for student conflicts...", getModel().getStudents().size());
        for (Student student: getModel().getStudents()) {
        	incProgress();
        	checkForConflicts(student);
        }
        
        if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty() && getModel().getKeepInitialAssignments()) {
        	setPhase("Removing initial assignment for matching course(s) ...", getModel().variables().size());
        	for (Request request: getModel().variables()) {
        		incProgress();
        		if (request instanceof CourseRequest && request.getInitialAssignment() != null && request.getInitialAssignment().getCourse().getName().matches(iMPPCoursesRegExp)) {
        			request.setInitialAssignment(null);
        		}
        	}
        }
        
        if (getModel().isMPP() && getModel().getKeepInitialAssignments()) {
        	setPhase("Moving assigned requests first...", getModel().getStudents().size());
            for (Student student: getModel().getStudents()) {
            	incProgress();
            	reorderStudentRequests(student);
            }
        } else if (iMoveCriticalCoursesUp) {
        	setPhase("Moving critical requests first...", getModel().getStudents().size());
            for (Student student: getModel().getStudents()) {
            	incProgress();
            	reorderStudentRequests(student);
            }
        } else if (iMoveFreeTimesDown) {
        	setPhase("Moving free times last...", getModel().getStudents().size());
            for (Student student: getModel().getStudents()) {
            	incProgress();
            	reorderStudentRequests(student);
            }
        } else if (iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) {
        	setPhase("Moving assigned requests not matching the course(s) first...", getModel().getStudents().size());
            for (Student student: getModel().getStudents()) {
            	incProgress();
            	reorderStudentRequests(student);
            }
        } else if (iMoveParentCoursesUp && getModel().getDependentCoursesConstraint() != null) {
        	setPhase("Moving prerequisite course up...", getModel().getStudents().size());
            for (Student student: getModel().getStudents()) {
            	incProgress();
            	reorderStudentRequests(student);
            }
        } 
                
        if (iStudentCourseDemands != null) {
        	iStudentCourseDemands.init(hibSession, iProgress, SessionDAO.getInstance().get(iSessionId, hibSession), offerings);
    		Hashtable<Long, Student> students = new Hashtable<Long, Student>();
    		
            Hashtable<Long, Set<Long>> classAssignments = null;
            if (iIncludeUseCommittedAssignments && !iStudentCourseDemands.isMakingUpStudents()) {
                classAssignments = new Hashtable();
                List<Object[]> enrollments = hibSession.createQuery("select distinct se.studentId, se.clazz.uniqueId from StudentEnrollment se where "+
                    "se.solution.commited=true and se.solution.owner.session.uniqueId=:sessionId", Object[].class).
                    setParameter("sessionId", session.getUniqueId().longValue()).setFetchSize(1000).list();
                setPhase("Loading projected class assignments...", enrollments.size());
                for (Iterator<Object[]> i=enrollments.iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next(); incProgress();
                    Long studentId = (Long)o[0];
                    Long classId = (Long)o[1];
                    Set<Long> classIds = classAssignments.get(studentId);
                    if (classIds==null) {
                        classIds = new HashSet<Long>();
                        classAssignments.put(studentId, classIds);
                    }
                    classIds.add(classId);
                }
            }
    		
            setPhase("Loading projected course requests...", offerings.size());
        	long requestId = -1;
            for (InstructionalOffering io: offerings) {
            	incProgress();
            	for (CourseOffering co: io.getCourseOfferings()) {
        	        Course course = courseTable.get(co.getUniqueId());
        	        if (course == null) continue;
            		Set<WeightedStudentId> demands = iStudentCourseDemands.getDemands(co);
            		if (demands == null) continue;
            		for (WeightedStudentId demand: demands) {
            			if (iProjectedStudentQuery != null && (
            				(demand.getStudent() != null && !iProjectedStudentQuery.match(new DbStudentMatcher(demand.getStudent()))) ||
            				(demand.getStudent() == null && !iProjectedStudentQuery.match(new ProjectedStudentMatcher(demand)))
            				)) continue;
            	        Student student = (Student)students.get(demand.getStudentId());
            	        if (student == null) {
            	        	student = new Student(demand.getStudentId(), true);
            	            for (AreaClasfMajor acm: demand.getMajors())
            	            	student.getAreaClassificationMajors().add(new AreaClassificationMajor(acm.getArea(), acm.getClasf(), acm.getMajor(), acm.getConcentration()));
            	            for (AreaClasfMajor acm: demand.getMinors())
            	            	student.getAreaClassificationMinors().add(new AreaClassificationMajor(acm.getArea(), acm.getClasf(), acm.getMajor()));
            	            students.put(demand.getStudentId(), student);
            	        }
            	        List<Course> courses = new ArrayList<Course>();
            	        courses.add(course);
            	        CourseRequest request = new CourseRequest(
            	        		requestId--,
            	                0,
            	                false,
            	                student,
            	                courses,
            	                false,
            	                null);
            	        request.setWeight(demand.getWeight());
            	        if (classAssignments != null && !classAssignments.isEmpty()) {
            	        	Set<Long> classIds = classAssignments.get(demand.getStudentId());
            	        	if (classIds != null) {
            	                enrollments: for (Enrollment enrollment: request.values(getAssignment())) {
            	                	for (Section section: enrollment.getSections())
            	                		if (!classIds.contains(section.getId())) continue enrollments;
            	                	request.setInitialAssignment(enrollment);
            	                	break;
            	                }
            	        	}
            	        }
            		}
            	}
            }
            
            for (Student student: students.values()) {
                getModel().addStudent(student);
                assignStudent(student);
            }
            
            for (Student student: students.values()) {
            	checkForConflicts(student);
            }
            
            if (iFixWeights)
            	fixWeights(hibSession, courseTable.values());
            
            if ((iMPPCoursesRegExp != null && !iMPPCoursesRegExp.isEmpty()) || iFixAssignedEnrollments) {
            	boolean hasFixed = false;
                for (Request r: getModel().variables()) {
                    if (r instanceof CourseRequest && ((CourseRequest)r).isFixed()) {
                        hasFixed = true; break;
                    }
                }
                if (hasFixed)
                    getModel().addGlobalConstraint(new FixedAssignments());
            }
            
            getModel().createAssignmentContexts(getAssignment(), true);
        }
        
        /*
        if (iIncludeLastLikeStudents) {
            Hashtable<Long, Set<Long>> classAssignments = null;
            if (iIncludeUseCommittedAssignments) {
                classAssignments = new Hashtable();
                List enrollments = hibSession.createQuery("select distinct se.studentId, se.clazz.uniqueId from StudentEnrollment se where "+
                    "se.solution.commited=true and se.solution.owner.session.uniqueId=:sessionId").
                    setParameter("sessionId", session.getUniqueId().longValue()).setFetchSize(1000).list();
                setPhase("Loading last-like class assignments...", enrollments.size());
                for (Iterator i=enrollments.iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next(); incProgress();
                    Long studentId = (Long)o[0];
                    Long classId = (Long)o[1];
                    Set<Long> classIds = classAssignments.get(studentId);
                    if (classIds==null) {
                        classIds = new HashSet<Long>();
                        classAssignments.put(studentId, classIds);
                    }
                    classIds.add(classId);
                }
            }
        
            Hashtable<Long, org.unitime.timetable.model.Student> students = new Hashtable<Long, org.unitime.timetable.model.Student>();
            List enrollments = hibSession.createQuery(
                    "select d, c.uniqueId from LastLikeCourseDemand d left join fetch d.student s, CourseOffering c left join c.demandOffering cx " +
                    "where d.subjectArea.session.uniqueId=:sessionId and c.subjectArea.session.uniqueId=:sessionId and " +
                    "((c.permId=null and d.subjectArea=c.subjectArea and d.courseNbr=c.courseNbr ) or "+
                    " (c.permId!=null and c.permId=d.coursePermId) or "+
                    " (cx.permId=null and d.subjectArea=cx.subjectArea and d.courseNbr=cx.courseNbr) or "+
                    " (cx.permId!=null and cx.permId=d.coursePermId)) "+
                    "order by s.uniqueId, d.priority, d.uniqueId").
                    setParameter("sessionId", session.getUniqueId().longValue()).setFetchSize(1000).list();
            setPhase("Loading last-like course requests...", enrollments.size());
            Hashtable lastLikeStudentTable = new Hashtable();
            for (Iterator i=enrollments.iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();incProgress();
                LastLikeCourseDemand d = (LastLikeCourseDemand)o[0];
                org.unitime.timetable.model.Student s = (org.unitime.timetable.model.Student)d.getStudent();
                Long courseOfferingId = (Long)o[1];
                if (s.getExternalUniqueId()!=null && loadedStudentIds.contains(s.getExternalUniqueId())) continue;
                loadLastLikeStudent(hibSession, d, s, courseOfferingId, lastLikeStudentTable, courseTable, classTable, classAssignments);
                students.put(s.getUniqueId(), s);
            }
            for (Enumeration e=lastLikeStudentTable.elements();e.hasMoreElements();) {
                Student student = (Student)e.nextElement();
                getModel().addStudent(student);
            	assignStudent(student, students.get(student.getId()));
            }
        }
		*/
        
        if (iLoadSectioningInfos) {
        	List<SectioningInfo> infos = hibSession.createQuery(
				"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :sessionId", SectioningInfo.class)
				.setParameter("sessionId", iSessionId)
				.list();
        	setPhase("Loading sectioning infos...", infos.size());
        	for (SectioningInfo info : infos) {
        		incProgress();
        		Section section = classTable.get(info.getClazz().getUniqueId());
        		if (section != null) {
        			section.setSpaceExpected(info.getNbrExpectedStudents());
        			section.setSpaceHeld(info.getNbrHoldingStudents());
        			if (section.getLimit() >= 0 && (section.getLimit() - section.getEnrollments(getAssignment()).size()) <= section.getSpaceExpected())
        				iProgress.info("Section " + section.getSubpart().getConfig().getOffering().getName() + " " + section.getSubpart().getName() + " " +
        						section.getName() + " has high demand (limit: " + section.getLimit() + ", enrollment: " + section.getEnrollments(getAssignment()).size() +
        						", expected: " + section.getSpaceExpected() + ")");
        		}
        	}	
        }
        
        setPhase("Done",1);incProgress();
    }
    
    protected void checkTermination() {
    	if (getTerminationCondition() != null && !getTerminationCondition().canContinue(new Solution<Request, Enrollment>(getModel(), getAssignment())))
    		throw new RuntimeException("The load was interrupted.");
    }
    
    protected void setPhase(String phase, long progressMax) {
    	checkTermination();
    	iProgress.setPhase(phase, progressMax);
    }
    
    protected void incProgress() {
    	checkTermination();
    	iProgress.incProgress();
    }
    
    protected class Worker extends Thread {
    	private org.hibernate.Session iHibSession;
		private Iterator<org.unitime.timetable.model.Student> iStudents;
		
		public Worker(org.hibernate.Session hibSession, int index, Iterator<org.unitime.timetable.model.Student> students) {
			setName("OverrideValidator-" + (1 + index));
			iStudents = students;
			iHibSession = hibSession;
		}
		
		@Override
	    public void run() {
			iProgress.debug(getName() + " has started.");
			org.hibernate.Session hibSession = null;
			try {
				ApplicationProperties.setSessionId(iSessionId);
				hibSession = StudentDAO.getInstance().createNewSession();
				while (true) {
					org.unitime.timetable.model.Student student = null;
					synchronized (iStudents) {
						if (!iCanContinue) {
							iProgress.debug(getName() + " has stopped.");
							return;
						}
						if (!iStudents.hasNext()) break;
						student = iStudents.next();
						iProgress.incProgress();
					}
					org.unitime.timetable.model.Student newStudent = StudentDAO.getInstance().get(student.getUniqueId(), hibSession);
		    		validateOverrides(hibSession, newStudent);
		    		synchronized (iStudents) {
		    			iHibSession.merge(newStudent);
					}
				}
			} finally {
				ApplicationProperties.setSessionId(null);
				if (hibSession != null) hibSession.close();
			}
			iProgress.debug(getName() + " has finished.");
		}
	}
    
    protected void checkCriticalCourses(org.hibernate.Session hibSession, List<org.unitime.timetable.model.Student> students) {
    	if (iNrCheckCriticalThreads <= 1) {
    		setPhase("Checking critical courses...", students.size());
    		for (org.unitime.timetable.model.Student s: students) {
        		incProgress();
        		if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
        		if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) continue;
                if (iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(s))) continue;
                checkCriticalCourses(hibSession, s);
        	}
		} else {
			List<org.unitime.timetable.model.Student> filteredStudents = new ArrayList<org.unitime.timetable.model.Student>();
			for (org.unitime.timetable.model.Student s: students) {
				if (s.getCourseDemands().isEmpty() && s.getClassEnrollments().isEmpty()) continue;
        		if (iCheckForNoBatchStatus && s.hasSectioningStatusOption(StudentSectioningStatus.Option.nobatch)) continue;
                if (iStudentQuery != null && !iStudentQuery.match(new DbStudentMatcher(s))) continue;
                filteredStudents.add(s);
			}
			setPhase("Checking critical courses...", filteredStudents.size());
			List<CriticalCoursesWorker> workers = new ArrayList<CriticalCoursesWorker>();
			Iterator<org.unitime.timetable.model.Student> iterator = filteredStudents.iterator();
			for (int i = 0; i < iNrCheckCriticalThreads; i++)
				workers.add(new CriticalCoursesWorker(hibSession, i, iterator));
			for (CriticalCoursesWorker worker: workers) worker.start();
			for (CriticalCoursesWorker worker: workers) {
				try {
					worker.join();
				} catch (InterruptedException e) {
					iCanContinue = false;
					try { worker.join(); } catch (InterruptedException x) {}
				}
			}
			if (!iCanContinue)
				throw new RuntimeException("The critical course check was interrupted.");
		}
    }
    
    protected int isCritical(CourseDemand cd, CriticalCourses critical) {
		if (critical == null || cd.isAlternative()) return 0;
		for (org.unitime.timetable.model.CourseRequest cr: cd.getCourseRequests()) {
			if (cr.getOrder() == 0 && critical.isCritical(cr.getCourseOffering()) > 0) return critical.isCritical(cr.getCourseOffering());
		}
		return 0;
	}
    
    protected String getStudentHoldError(org.hibernate.Session hibSession, org.unitime.timetable.model.Student s) {
    	if (iValidator == null) {
    		iValidator = new StudentSolver(getModel().getProperties(), null);
    		iValidator.setInitalSolution(new Solution(getModel(), getAssignment()));
    	}
    	OnlineSectioningLog.Entity user = Entity.newBuilder().setExternalId(iOwnerId).setType(Entity.EntityType.MANAGER).build(); 
    	OnlineSectioningHelper helper = new OnlineSectioningHelper(hibSession, user);
    	OnlineSectioningLog.Action.Builder action = helper.getAction();
    	action.setOperation("check-hold");
		action.setSession(OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(iSessionId)
    			.setName(iTerm + iYear + iInitiative)
    			);
    	action.setStartTime(System.currentTimeMillis());
    	action.setUser(user);
    	action.setStudent(OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(s.getUniqueId())
				.setExternalId(s.getExternalUniqueId())
				.setName(iStudentNameFormat.format(s))
				.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
		long c0 = OnlineSectioningHelper.getCpuTime();
		String error = null;
		try {
			error = iStudentHoldsCheckProvider.getStudentHoldError(iValidator, helper, new XStudentId(s, helper));
		} catch (Exception e) {
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			if (e.getCause() != null) {
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
			} else {
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
		} finally {
			action.setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
			action.setEndTime(System.currentTimeMillis());
			action.setResult(error == null ? ResultType.TRUE : ResultType.FALSE);
			if (error != null)
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.INFO)
						.setText(error));
			OnlineSectioningLogger.getInstance().record(OnlineSectioningLog.Log.newBuilder().addAction(action).build());
		}
		return error;
    }
    
    protected void checkCriticalCourses(org.hibernate.Session hibSession, org.unitime.timetable.model.Student s) {
    	if (iValidator == null) {
    		iValidator = new StudentSolver(getModel().getProperties(), null);
    		iValidator.setInitalSolution(new Solution(getModel(), getAssignment()));
    	}
    	OnlineSectioningLog.Entity user = Entity.newBuilder().setExternalId(iOwnerId).setType(Entity.EntityType.MANAGER).build(); 
    	OnlineSectioningHelper helper = new OnlineSectioningHelper(hibSession, user);
    	OnlineSectioningLog.Action.Builder action = helper.getAction();
    	action.setOperation("critical-courses");
		action.setSession(OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(iSessionId)
    			.setName(iTerm + iYear + iInitiative)
    			);
    	action.setStartTime(System.currentTimeMillis());
    	action.setUser(user);
    	action.setStudent(OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(s.getUniqueId())
				.setExternalId(s.getExternalUniqueId())
				.setName(iStudentNameFormat.format(s))
				.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
		long c0 = OnlineSectioningHelper.getCpuTime();
		try {
			CriticalCourses critical = iCriticalCoursesProvider.getCriticalCourses(iValidator, helper, new XStudent(s, helper, iFreeTimePattern, iDatePatternFirstDate));
			boolean changed = false;
			for (CourseDemand cd: s.getCourseDemands()) {
				int crit = isCritical(cd, critical);
				if (cd.getCritical() == null || cd.getCritical().intValue() != crit) {
					cd.setCritical(crit); hibSession.merge(cd); changed = true;
				}
			}
			for (AdvisorCourseRequest acr: s.getAdvisorCourseRequests()) {
				int crit = acr.isCritical(critical);
				if (acr.getCritical() == null || acr.getCritical().intValue() != crit) {
					acr.setCritical(crit); hibSession.merge(acr);
				}
			}
			if (changed) {
        		iUpdatedStudents.add(s.getUniqueId());
        		action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
        	} else {
        		action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
        	}
		} catch (Exception e) {
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			if (e.getCause() != null) {
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
			} else {
				action.addMessage(OnlineSectioningLog.Message.newBuilder()
						.setLevel(OnlineSectioningLog.Message.Level.FATAL)
						.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
		} finally {
			action.setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
			action.setEndTime(System.currentTimeMillis());
			OnlineSectioningLogger.getInstance().record(OnlineSectioningLog.Log.newBuilder().addAction(action).build());
		}
    }
    
    protected class CriticalCoursesWorker extends Thread {
    	private org.hibernate.Session iHibSession;
		private Iterator<org.unitime.timetable.model.Student> iStudents;
		
		public CriticalCoursesWorker(org.hibernate.Session hibSession, int index, Iterator<org.unitime.timetable.model.Student> students) {
			setName("CriticalCourses-" + (1 + index));
			iStudents = students;
			iHibSession = hibSession;
		}
		
		@Override
	    public void run() {
			iProgress.debug(getName() + " has started.");
			org.hibernate.Session hibSession = null;
			try {
				ApplicationProperties.setSessionId(iSessionId);
				hibSession = StudentDAO.getInstance().createNewSession();
				while (true) {
					org.unitime.timetable.model.Student student = null;
					synchronized (iStudents) {
						if (!iCanContinue) {
							iProgress.debug(getName() + " has stopped.");
							return;
						}
						if (!iStudents.hasNext()) break;
						student = iStudents.next();
						iProgress.incProgress();
					}
					org.unitime.timetable.model.Student newStudent = StudentDAO.getInstance().get(student.getUniqueId(), hibSession);
		    		checkCriticalCourses(hibSession, newStudent);
		    		synchronized (iStudents) {
		    			iHibSession.merge(newStudent);
					}
				}
			} finally {
				ApplicationProperties.setSessionId(null);
				if (hibSession != null) hibSession.close();
			}
			iProgress.debug(getName() + " has finished.");
		}
	}
    
    public static class ProjectedStudentMatcher implements TermMatcher {
		private WeightedStudentId iStudent;
		
		public ProjectedStudentMatcher(WeightedStudentId student) {
			iStudent = student;
		}
		
		public WeightedStudentId student() { return iStudent; }
		
		@Override
		public boolean match(String attr, String term) {
			if (attr == null && term.isEmpty()) return true;
			if ("area".equals(attr)) {
				for (AreaClasfMajor acm: student().getMajors())
					if (like(acm.getArea(), term)) return true;
				for (AreaClasfMajor acm: student().getMinors())
					if (like(acm.getArea(), term)) return true;
			} else if ("clasf".equals(attr) || "classification".equals(attr)) {
				for (AreaClasfMajor acm: student().getMajors())
					if (like(acm.getClasf(), term)) return true;
			} else if ("major".equals(attr)) {
				for (AreaClasfMajor acm: student().getMajors())
					if (like(acm.getMajor(), term)) return true;
			} else if ("concentration".equals(attr)) {
				for (AreaClasfMajor acm: student().getMajors())
					if (acm.getConcentration() != null && like(acm.getConcentration(), term)) return true;
			} else if ("degree".equals(attr)) {
				for (AreaClasfMajor acm: student().getMajors())
					if (acm.getDegree() != null && like(acm.getDegree(), term)) return true;
			} else if ("program".equals(attr)) {
				for (AreaClasfMajor acm: student().getMajors())
					if (acm.getProgram() != null && like(acm.getProgram(), term)) return true;
			} else if ("primary-area".equals(attr)) {
				AreaClasfMajor acm = student().getPrimaryMajor();
				if (acm != null && like(acm.getArea(), term)) return true;
			} else if ("primary-clasf".equals(attr) || "primary-classification".equals(attr)) {
				AreaClasfMajor acm = student().getPrimaryMajor();
				if (acm != null && like(acm.getClasf(), term)) return true;
			} else if ("primary-major".equals(attr)) {
				AreaClasfMajor acm = student().getPrimaryMajor();
				if (acm != null && like(acm.getMajor(), term)) return true;
			} else if ("primary-concentration".equals(attr)) {
				AreaClasfMajor acm = student().getPrimaryMajor();
				if (acm != null && acm.getConcentration() != null && like(acm.getConcentration(), term)) return true;
			} else if ("primary-degree".equals(attr)) {
				AreaClasfMajor acm = student().getPrimaryMajor();
				if (acm != null && acm.getDegree() != null && like(acm.getDegree(), term)) return true;
			} else if ("primary-program".equals(attr)) {
				AreaClasfMajor acm = student().getPrimaryMajor();
				if (acm != null && acm.getProgram() != null && like(acm.getProgram(), term)) return true;
			} else if ("minor".equals(attr)) {
				for (AreaClasfMajor acm: student().getMinors())
					if (like(acm.getMajor(), term)) return true;
			} else if ("group".equals(attr)) {
				for (Group group: student().getGroups())
					if (like(group.getName(), term)) return true;
			}
			return false;
		}
		
		private boolean like(String name, String term) {
			if (name == null) return false;
			if (term.indexOf('%') >= 0) {
				return name.matches("(?i)" + term.replaceAll("%", ".*"));
			} else {
				return name.equalsIgnoreCase(term);
			}
		}
	}
}
