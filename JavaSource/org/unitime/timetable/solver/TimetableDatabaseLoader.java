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
package org.unitime.timetable.solver;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.TimetableLoader;
import org.cpsolver.coursett.constraint.ClassLimitConstraint;
import org.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import org.cpsolver.coursett.constraint.DiscouragedRoomConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.constraint.MinimizeNumberOfUsedGroupsOfTime;
import org.cpsolver.coursett.constraint.MinimizeNumberOfUsedRoomsConstraint;
import org.cpsolver.coursett.constraint.RoomConstraint;
import org.cpsolver.coursett.constraint.SpreadConstraint;
import org.cpsolver.coursett.constraint.FlexibleConstraint.FlexibleConstraintType;
import org.cpsolver.coursett.model.Configuration;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.StudentGroup;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.cpsolver.ifs.model.BinaryConstraint;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.IdGenerator;
import org.cpsolver.ifs.util.Progress;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.DistributionPref.Structure;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.AssignmentDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.course.weights.ClassWeightProvider;
import org.unitime.timetable.solver.course.weights.DefaultClassWeights;
import org.unitime.timetable.solver.curricula.LastLikeStudentCourseDemands;
import org.unitime.timetable.solver.curricula.StudentCourseDemands;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.AreaClasfMajor;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.Group;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.WeightedCourseOffering;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.WeightedStudentId;
import org.unitime.timetable.solver.curricula.StudentGroupCourseDemands;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.util.duration.DurationModel;

/**
 * @author Tomas Muller
 */
public class TimetableDatabaseLoader extends TimetableLoader {
	private static Log sLog = LogFactory.getLog(TimetableDatabaseLoader.class);
	protected static CPSolverMessages MSG = Localization.create(CPSolverMessages.class);
	private Session iSession;
	private Long iSessionId;
	private Long[] iSolverGroupId;
	private String iSolverGroupIds;
	private String iDepartmentIds = null;
	private SolverGroup[] iSolverGroup;
	private Long[] iSolutionId;
	private long iFakeLectureId = 0;
	
	private Hashtable<Long, RoomConstraint> iRooms = new Hashtable<Long, RoomConstraint>();
	private Hashtable<Object, InstructorConstraint> iInstructors = new Hashtable<Object, InstructorConstraint>();
	private Hashtable<InstructorConstraint, List<DistributionType>> iInstructorDistributions = new Hashtable<InstructorConstraint, List<DistributionType>>();
	private Hashtable<Long, Lecture> iLectures = new Hashtable<Long, Lecture>();
	private Hashtable<Long, SchedulingSubpart> iSubparts = new Hashtable<Long, SchedulingSubpart>();
	private Hashtable<Long, Student> iStudents = new Hashtable<Long, Student>();
	private Hashtable<Long, WeightedStudentId> iWeightedStudents = new Hashtable<Long, WeightedStudentId>();
	private Hashtable<Long, String> iDeptNames = new Hashtable<Long, String>();
	private Hashtable<Long, Class_> iClasses = new Hashtable<Long, Class_>();
	private Set<DatePattern> iAllUsedDatePatterns = new HashSet<DatePattern>();
	private Hashtable<Long, StudentGroup> iGroups = new Hashtable<Long, StudentGroup>();
	private Set<Class_> iAllClasses = null;
	private Hashtable<InstructionalOffering, List<Configuration>> iAltConfigurations = new Hashtable<InstructionalOffering, List<Configuration>>();
	private Hashtable<InstructionalOffering, Hashtable<InstrOfferingConfig, Set<SchedulingSubpart>>> iOfferings = new Hashtable<InstructionalOffering, Hashtable<InstrOfferingConfig,Set<SchedulingSubpart>>>();
    private Hashtable<CourseOffering, Set<Student>> iCourse2students = new Hashtable<CourseOffering, Set<Student>>();

    private boolean iDeptBalancing = true;
    private boolean iSubjectBalancing = false;
    private boolean iMppAssignment = true;
    private boolean iInteractiveMode = false;
    private boolean iSpread = true;
    private boolean iAutoSameStudents = true;
    private String iAutoPrecedence = null;
    private boolean iLoadCommittedAssignments = false;

    private double iFewerSeatsDisouraged = 0.01;
    private double iFewerSeatsStronglyDisouraged = 0.02;
    
    private double iNormalizedPrefDecreaseFactor = TimePatternModel.sDefaultDecreaseFactor;
    
    private double iAlterTimePatternWeight = 0.0;
    private double iAlterDatePatternWeight = 1.0;
    private TimePatternModel iAlterTimePatternModel = (TimePatternModel)TimePattern.getDefaultRequiredTimeTable().getModel(); 
    private boolean iWeakenTimePreferences = false;
    
    private Progress iProgress = null;
    
    private boolean iLoadStudentEnrlsFromSolution = false;
    private boolean iFixMinPerWeek = false;
    private boolean iAssignSingleton = true;
    private boolean iIgnoreRoomSharing = false;
    private boolean iLoadStudentInstructorConflicts = false;
    private int iMaxRoomCombinations = 100;
    
    private String iAutoSameStudentsConstraint = "SAME_STUDENTS";
    private String iInstructorFormat = null;
    
    private boolean iRoomAvailabilityTimeStampIsSet = false;
    
    private CommittedStudentConflictsMode iCommittedStudentConflictsMode = CommittedStudentConflictsMode.Load;
    
    private StudentCourseDemands iStudentCourseDemands = null;
    private StudentGroupCourseDemands iStudentGroupCourseDemands = null;
    
    private ClassWeightProvider iClassWeightProvider = null;
    private boolean iUseAmPm = true;
    private boolean iShowClassSuffix = false, iShowConfigName = false;
    private boolean iLoadCommittedReservations = false;
    private boolean iInstructorDistributionsAcrossDepartments = false;

    public static enum CommittedStudentConflictsMode {
    		Ignore,
    		Load,
    		Compute};
    		
    public TimetableDatabaseLoader(TimetableModel model, org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment) {
        super(model, assignment);
        Progress.sTraceEnabled=false;
        iProgress = Progress.getInstance(model);
        
        iSessionId = model.getProperties().getPropertyLong("General.SessionId",(Long)null);
        iSolverGroupId = model.getProperties().getPropertyLongArry("General.SolverGroupId",null);
        iSolutionId = model.getProperties().getPropertyLongArry("General.SolutionId",null);
        iIgnoreRoomSharing = model.getProperties().getPropertyBoolean("General.IgnoreRoomSharing",iIgnoreRoomSharing);
        
        iSolverGroupIds = "";
        if (iSolverGroupId!=null) {
        	for (int i=0;i<iSolverGroupId.length;i++) {
        		if (i>0) iSolverGroupIds+=",";
        		iSolverGroupIds+=iSolverGroupId[i].toString();
        	}
        }

        iDeptBalancing = getModel().getProperties().getPropertyBoolean("General.DeptBalancing",iDeptBalancing);
        iSubjectBalancing = getModel().getProperties().getPropertyBoolean("General.SubjectBalancing",iSubjectBalancing);
        iSpread = getModel().getProperties().getPropertyBoolean("General.Spread",iSpread);
        iAutoSameStudents = getModel().getProperties().getPropertyBoolean("General.AutoSameStudents",iAutoSameStudents);
        iAutoPrecedence = getModel().getProperties().getProperty("General.AutoPrecedence");
        iMppAssignment = getModel().getProperties().getPropertyBoolean("General.MPP",iMppAssignment);
        iInteractiveMode = getModel().getProperties().getPropertyBoolean("General.InteractiveMode", iInteractiveMode);
        iAssignSingleton = getModel().getProperties().getPropertyBoolean("General.AssignSingleton", iAssignSingleton);
        iMaxRoomCombinations = getModel().getProperties().getPropertyInt("General.MaxRoomCombinations", iMaxRoomCombinations);
        
        iFewerSeatsDisouraged = getModel().getProperties().getPropertyDouble("Global.FewerSeatsDisouraged", iFewerSeatsDisouraged);
        iFewerSeatsStronglyDisouraged = getModel().getProperties().getPropertyDouble("Global.FewerSeatsStronglyDisouraged", iFewerSeatsStronglyDisouraged);
        
        iNormalizedPrefDecreaseFactor = getModel().getProperties().getPropertyDouble("General.NormalizedPrefDecreaseFactor", iNormalizedPrefDecreaseFactor);
        
        iLoadStudentEnrlsFromSolution = getModel().getProperties().getPropertyBoolean("Global.LoadStudentEnrlsFromSolution", iLoadStudentEnrlsFromSolution);
        iLoadStudentInstructorConflicts = getModel().getProperties().getPropertyBoolean("Global.LoadStudentInstructorConflicts", iLoadStudentInstructorConflicts);
        
        iFixMinPerWeek = getModel().getProperties().getPropertyBoolean("Global.FixMinPerWeek", iFixMinPerWeek);
        
        iAlterTimePatternWeight = getModel().getProperties().getPropertyDouble("TimePreferences.Weight", iAlterTimePatternWeight);
        iAlterDatePatternWeight = getModel().getProperties().getPropertyDouble("General.AlternativeDatePatternWeight", iAlterDatePatternWeight);
        iAlterTimePatternModel.setPreferences(getModel().getProperties().getProperty("TimePreferences.Pref", null));
        
        iWeakenTimePreferences = getModel().getProperties().getPropertyBoolean("TimePreferences.Weaken", iWeakenTimePreferences);
        
        iAutoSameStudentsConstraint = getModel().getProperties().getProperty("General.AutoSameStudentsConstraint",iAutoSameStudentsConstraint);
        
        iInstructorFormat = getModel().getProperties().getProperty("General.InstructorFormat", DepartmentalInstructor.sNameFormatLastFist);
        iInstructorDistributionsAcrossDepartments = getModel().getProperties().getPropertyBoolean("General.ApplyInstructorDistributionsAcrossAllDepartments", iInstructorDistributionsAcrossDepartments);
                
        try {
        	String studentCourseDemandsClassName = getModel().getProperties().getProperty("Curriculum.StudentCourseDemadsClass", LastLikeStudentCourseDemands.class.getName());
        	if (studentCourseDemandsClassName.indexOf(' ') >= 0) studentCourseDemandsClassName = studentCourseDemandsClassName.replace(" ", "");
        	if (studentCourseDemandsClassName.indexOf('.') < 0) studentCourseDemandsClassName = "org.unitime.timetable.solver.curricula." + studentCourseDemandsClassName;
            Class studentCourseDemandsClass = Class.forName(studentCourseDemandsClassName);
            iStudentCourseDemands = (StudentCourseDemands)studentCourseDemandsClass.getConstructor(DataProperties.class).newInstance(getModel().getProperties());
        } catch (Exception e) {
        	iProgress.message(msglevel("badStudentCourseDemands", Progress.MSGLEVEL_WARN), MSG.warnFailedLoadCustomStudentDemands(),e);
        	iStudentCourseDemands = new LastLikeStudentCourseDemands(getModel().getProperties());
        }
        IdGenerator studentIdGenerator = new IdGenerator();
        if (iStudentCourseDemands instanceof StudentCourseDemands.NeedsStudentIdGenerator)
        	((StudentCourseDemands.NeedsStudentIdGenerator)iStudentCourseDemands).setStudentIdGenerator(studentIdGenerator);
        getModel().getProperties().setProperty("General.SaveStudentEnrollments", iStudentCourseDemands.isMakingUpStudents() ? "false" : "true");
        getModel().getProperties().setProperty("General.WeightStudents", iStudentCourseDemands.isWeightStudentsToFillUpOffering() ? "true" : "false");
        if (getModel().getProperties().getPropertyBoolean("General.StudentGroupCourseDemands", false)) {
        	iStudentGroupCourseDemands = new StudentGroupCourseDemands(getModel().getProperties());
        	if (iStudentGroupCourseDemands instanceof StudentCourseDemands.NeedsStudentIdGenerator)
            	((StudentCourseDemands.NeedsStudentIdGenerator)iStudentGroupCourseDemands).setStudentIdGenerator(studentIdGenerator);
        }
        
        iCommittedStudentConflictsMode = CommittedStudentConflictsMode.valueOf(getModel().getProperties().getProperty("General.CommittedStudentConflicts",
        		iCommittedStudentConflictsMode.name()));
        iLoadCommittedAssignments = getModel().getProperties().getPropertyBoolean("General.LoadCommittedAssignments", iLoadCommittedAssignments);
        if (iCommittedStudentConflictsMode == CommittedStudentConflictsMode.Load && iStudentCourseDemands.isMakingUpStudents()) {
        	iCommittedStudentConflictsMode = CommittedStudentConflictsMode.Compute;
        	getModel().getProperties().setProperty("General.CommittedStudentConflicts", iCommittedStudentConflictsMode.name());
        }
        if (iLoadStudentEnrlsFromSolution && iStudentCourseDemands.isMakingUpStudents()) {
        	iLoadStudentEnrlsFromSolution = false;
        	getModel().getProperties().setProperty("Global.LoadStudentEnrlsFromSolution", "false");
        }
        if (iLoadStudentInstructorConflicts && iStudentCourseDemands.isMakingUpStudents()) {
        	iLoadStudentInstructorConflicts = false;
        	getModel().getProperties().setProperty("Global.LoadStudentInstructorConflicts", "false");
        }
        
        try {
        	String classWeightProviderClassName = getModel().getProperties().getProperty("ClassWeightProvider.Class", DefaultClassWeights.class.getName());
        	if (classWeightProviderClassName.indexOf(' ') >= 0) classWeightProviderClassName = classWeightProviderClassName.replace(" ", "");
        	if (classWeightProviderClassName.indexOf('.') < 0) classWeightProviderClassName = "org.unitime.timetable.solver.course.weights." + classWeightProviderClassName;
            Class classWeightProviderClass = Class.forName(classWeightProviderClassName);
            iClassWeightProvider = (ClassWeightProvider)classWeightProviderClass.getConstructor(DataProperties.class).newInstance(getModel().getProperties());
        } catch (Exception e) {
        	iProgress.message(msglevel("badClassWeightProvider", Progress.MSGLEVEL_WARN), MSG.warnFauledLoadCustomClassWeights(),e);
        	iClassWeightProvider = new DefaultClassWeights(getModel().getProperties());
        }
        iLoadCommittedReservations = getModel().getProperties().getPropertyBoolean("General.LoadCommittedReservations", iLoadCommittedReservations);
        
        iUseAmPm = getModel().getProperties().getPropertyBoolean("General.UseAmPm", iUseAmPm);
        iShowClassSuffix = ApplicationProperty.SolverShowClassSufix.isTrue();
        iShowConfigName = ApplicationProperty.SolverShowConfiguratioName.isTrue();
    }
    
    public int msglevel(String type, int defaultLevel) {
    	String level = ApplicationProperty.SolverLogLevel.value(type);
    	if (level == null) return defaultLevel;
    	if ("warn".equalsIgnoreCase(level)) return Progress.MSGLEVEL_WARN;
    	if ("error".equalsIgnoreCase(level)) return Progress.MSGLEVEL_ERROR;
    	if ("fatal".equalsIgnoreCase(level)) return Progress.MSGLEVEL_FATAL;
    	if ("info".equalsIgnoreCase(level)) return Progress.MSGLEVEL_INFO;
    	if ("debug".equalsIgnoreCase(level)) return Progress.MSGLEVEL_DEBUG;
    	if ("trace".equalsIgnoreCase(level)) return Progress.MSGLEVEL_TRACE;
    	return defaultLevel;
    }
    
    private String getClassLabel(Class_ clazz) {
    	return "<A href='classDetail.do?cid="+clazz.getUniqueId()+"'>"+clazz.getClassLabel(iShowClassSuffix, iShowConfigName)+"</A>";
    }
    
    private String getClassLabel(Lecture lecture) {
    	return "<A href='classDetail.do?cid="+lecture.getClassId()+"'>"+lecture.getName()+"</A>";
    }
    
    private String getOfferingLabel(InstructionalOffering offering) {
    	return "<A href='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"'>"+offering.getCourseName()+"</A>";
    }
    private String getOfferingLabel(CourseOffering offering) {
    	return "<A href='instructionalOfferingDetail.do?io="+offering.getInstructionalOffering().getUniqueId()+"'>"+offering.getCourseName()+"</A>";
    }
    private String getSubpartLabel(SchedulingSubpart subpart) {
    	String suffix = subpart.getSchedulingSubpartSuffix();
    	return "<A href='schedulingSubpartDetail.do?ssuid="+subpart.getUniqueId()+"'>"+subpart.getCourseName()+" "+subpart.getItypeDesc().trim()+(suffix==null || suffix.length()==0?"":" ("+suffix+")")+"</A>";
    }
    
    private Hashtable iRoomPreferences = null;
    
    private PreferenceLevel getRoomPreference(Long deptId, Long locationId) {
    	if (iRoomPreferences == null) {
    		iRoomPreferences = new Hashtable();
    		for (int i=0;i<iSolverGroup.length;i++) {
        		for (Iterator j=iSolverGroup[i].getDepartments().iterator();j.hasNext();) {
        			Department department = (Department)j.next();
        			Hashtable roomPreferencesThisDept = new Hashtable();
        			iRoomPreferences.put(department.getUniqueId(),roomPreferencesThisDept);
            		for (Iterator k=department.getPreferences(RoomPref.class).iterator();k.hasNext();) {
            			RoomPref pref = (RoomPref)k.next();
            			roomPreferencesThisDept.put(pref.getRoom().getUniqueId(),pref.getPrefLevel());
            		}
        		}
    		}
    	}
    	Hashtable roomPreferencesThisDept = (Hashtable)iRoomPreferences.get(deptId);
    	if (roomPreferencesThisDept==null) return null;
    	return (PreferenceLevel)roomPreferencesThisDept.get(locationId);
    }
    
    public static List<RoomLocation> computeRoomLocations(Class_ clazz) {
    	return computeRoomLocations(clazz, false, 0.01, 0.02);
    }
    
    public static List<RoomLocation> computeRoomLocations(Class_ clazz, boolean interactiveMode, double fewerSeatsDisouraged, double fewerSeatsStronglyDisouraged) {
    	int minClassLimit = clazz.getExpectedCapacity().intValue();
    	int maxClassLimit = clazz.getMaxExpectedCapacity().intValue();
    	if (maxClassLimit<minClassLimit) maxClassLimit = minClassLimit;
    	float room2limitRatio = clazz.getRoomRatio().floatValue();
    	int roomCapacity = Math.round(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
        int discouragedCapacity = (int)Math.round((1.0-fewerSeatsStronglyDisouraged) * roomCapacity);
        int stronglyDiscouragedCapacity = (int)Math.round((1.0-fewerSeatsStronglyDisouraged) * roomCapacity);

        List<RoomLocation> roomLocations = new ArrayList<RoomLocation>();
        boolean reqRoom = false;
        boolean reqBldg = false;
        boolean reqGroup = false;

		Set allRooms = clazz.getAvailableRooms();
		Set groupPrefs = clazz.effectivePreferences(RoomGroupPref.class);
		Set roomPrefs = clazz.effectivePreferences(RoomPref.class);
		Set bldgPrefs = clazz.effectivePreferences(BuildingPref.class);
		Set featurePrefs = clazz.effectivePreferences(RoomFeaturePref.class);
			
    	for (Iterator i1=allRooms.iterator();i1.hasNext();) {
    		Location room = (Location)i1.next();
    	    	boolean add = true;

    		PreferenceCombination pref = new SumPreferenceCombination();
    		
    		// --- group preference ----------
    		PreferenceCombination groupPref = PreferenceCombination.getDefault();
    		for (Iterator i2=groupPrefs.iterator();i2.hasNext();) {
    			RoomGroupPref p = (RoomGroupPref)i2.next();
    			if (p.getRoomGroup().getRooms().contains(room))
    				groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
    		}
    		
    		if (groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited)) {
            	if (interactiveMode)
                    pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
    		}
    		
            if (reqGroup && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired)) {
                if (interactiveMode)
                	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
            }
            
            if (!reqGroup && (groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))) {
            	reqGroup=true; 
                if (interactiveMode) {
                    for (RoomLocation r: roomLocations) {
                        r.setPreference(r.getPreference()+100);
                    }
                } else roomLocations.clear();
            }

            if (!groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited) && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))
            	pref.addPreferenceProlog(groupPref.getPreferenceProlog());
    		
            
            // --- room preference ------------
    		String roomPref = null;

    		PreferenceLevel roomPreference = null;
    		for (Iterator k=clazz.getManagingDept().getPreferences(RoomPref.class).iterator();k.hasNext();) {
    			RoomPref x = (RoomPref)k.next();
    			if (room.equals(x.getRoom())) roomPreference = x.getPrefLevel();
    		}
    		
    		if (roomPreference!=null) {
    			roomPref = roomPreference.getPrefProlog();

    			if (PreferenceLevel.sProhibited.equals(roomPref)) { 
    				add=false;
    			}
    			
    			if (PreferenceLevel.sStronglyDiscouraged.equals(roomPref)) {
    				roomPref = PreferenceLevel.sProhibited;
    			}
    		}

    		for (Iterator i2=roomPrefs.iterator();i2.hasNext();) {
    			RoomPref p = (RoomPref)i2.next();
    			if (room.equals(p.getRoom())) {
    				roomPref = p.getPrefLevel().getPrefProlog();
    				break;
    			}
    		}
    		
            if (roomPref!=null && roomPref.equals(PreferenceLevel.sProhibited)) {
            	if (interactiveMode)
                    pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
            }
            
            if (reqRoom && (roomPref==null || !roomPref.equals(PreferenceLevel.sRequired))) {
                if (interactiveMode)
                	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
            }
            
            if (!reqRoom && (roomPref!=null && roomPref.equals(PreferenceLevel.sRequired))) {
                reqRoom=true; 
                if (interactiveMode) {
                    for (RoomLocation r: roomLocations) {
                        r.setPreference(r.getPreference()+100);
                    }
                } else roomLocations.clear();
            }
            
            if (roomPref!=null && !roomPref.equals(PreferenceLevel.sProhibited) && !roomPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(roomPref);

            // --- building preference ------------
    		Building bldg = (room instanceof Room ? ((Room)room).getBuilding() : null);

    		String bldgPref = null;
    		for (Iterator i2=bldgPrefs.iterator();i2.hasNext();) {
    			BuildingPref p = (BuildingPref)i2.next();
    			if (bldg!=null && bldg.equals(p.getBuilding())) {
    				bldgPref = p.getPrefLevel().getPrefProlog();
    				break;
    			}
    		}
    		
            if (bldgPref!=null && bldgPref.equals(PreferenceLevel.sProhibited)) {
                if (interactiveMode)
                	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
            }
            
            if (reqBldg && (bldgPref==null || !bldgPref.equals(PreferenceLevel.sRequired))) {
                if (interactiveMode)
                	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
            }
            
            if (!reqBldg && (bldgPref!=null && bldgPref.equals(PreferenceLevel.sRequired))) {
                reqBldg = true;
                if (interactiveMode) {
                    for (RoomLocation r: roomLocations) {
                        r.setPreference(r.getPreference()+100);
                    }
                } else roomLocations.clear();
            }

            if (bldgPref!=null && !bldgPref.equals(PreferenceLevel.sProhibited) && !bldgPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(bldgPref);
            
            // --- room features preference --------  
            boolean acceptableFeatures = true;
            PreferenceCombination featurePref = new MinMaxPreferenceCombination();
            for (Iterator i2=featurePrefs.iterator();i2.hasNext();) {
            	RoomFeaturePref roomFeaturePref = (RoomFeaturePref)i2.next();
            	RoomFeature feature = roomFeaturePref.getRoomFeature();
            	String p = roomFeaturePref.getPrefLevel().getPrefProlog();
            	
            	boolean hasFeature = feature.getRooms().contains(room);
                if (p.equals(PreferenceLevel.sProhibited) && hasFeature) {
                    acceptableFeatures=false;
                }
                if (p.equals(PreferenceLevel.sRequired) && !hasFeature) {
                    acceptableFeatures=false;
                }
                if (p!=null && hasFeature && !p.equals(PreferenceLevel.sProhibited) && !p.equals(PreferenceLevel.sRequired)) 
                	featurePref.addPreferenceProlog(p);
            }
            pref.addPreferenceInt(featurePref.getPreferenceInt());
            
            if (!acceptableFeatures) {
                if (interactiveMode)
                	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                else
                    add=false;
            }
            
            
    		// --- room size ----------------- 
            if (room.getCapacity().intValue()<stronglyDiscouragedCapacity) {
            	if (interactiveMode)
            		pref.addPreferenceInt(1000);
            	else add=false;
            }
            else if (room.getCapacity().intValue()<discouragedCapacity) {
                pref.addPreferenceProlog(PreferenceLevel.sStronglyDiscouraged);
            }
            else if (room.getCapacity().intValue()<roomCapacity) {
            	pref.addPreferenceProlog(PreferenceLevel.sDiscouraged);
            }

            int prefInt = pref.getPreferenceInt();
            
            if (!add) continue;
            
            roomLocations.add(
            		new RoomLocation(
            				room.getUniqueId(),
            				room.getLabel(),
            				(bldg==null?null:bldg.getUniqueId()),
            				prefInt,
            				room.getCapacity().intValue(),
            				room.getCoordinateX(),
            				room.getCoordinateY(),
            				(room.isIgnoreTooFar()==null?false:room.isIgnoreTooFar().booleanValue()),
            				null));
        }
    	return roomLocations;
    }
    
    private Lecture loadClass(Class_ clazz, org.hibernate.Session hibSession) {
    	if (clazz.isCancelled()) {
    		iProgress.message(msglevel("cancelledClass", Progress.MSGLEVEL_WARN), MSG.warnCancelledClass(getClassLabel(clazz)));
    		return null;
    	}
    	
    	List<TimeLocation> timeLocations = new ArrayList<TimeLocation>();
    	List<RoomLocation> roomLocations = new ArrayList<RoomLocation>();
    	
    	iProgress.message(msglevel("loadingClass", Progress.MSGLEVEL_DEBUG), MSG.debugLoadingClass(getClassLabel(clazz)));
    	
    	Department dept = clazz.getControllingDept();
    	iDeptNames.put(dept.getUniqueId(),dept.getShortLabel());
    	iProgress.trace("department: "+dept.getName()+" (id:"+dept.getUniqueId()+")");
    	
    	int minClassLimit = clazz.getExpectedCapacity().intValue();
    	int maxClassLimit = clazz.getMaxExpectedCapacity().intValue();
    	if (maxClassLimit<minClassLimit) maxClassLimit = minClassLimit;
    	if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment())
    		minClassLimit = maxClassLimit = Integer.MAX_VALUE;
    	float room2limitRatio = clazz.getRoomRatio().floatValue();
    	int roomCapacity = Math.round(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
    	
    	iProgress.trace("class limit: ["+minClassLimit+","+maxClassLimit+"]");
    	iProgress.trace("room2limitRatio: "+room2limitRatio);
    	
    	iProgress.trace("room capacity: "+roomCapacity);
        int discouragedCapacity = (int)Math.round((1.0-iFewerSeatsDisouraged) * roomCapacity);
        iProgress.trace("discouraged capacity: "+discouragedCapacity);
        int stronglyDiscouragedCapacity = (int)Math.round((1.0-iFewerSeatsStronglyDisouraged) * roomCapacity);
        iProgress.trace("strongly discouraged capacity: "+stronglyDiscouragedCapacity);
        
        Set timePrefs = clazz.effectivePreferences(TimePref.class);
        
        if (timePrefs.isEmpty()) {
            if (clazz.getSchedulingSubpart().getMinutesPerWk().intValue()!=0) {
            	DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
                iProgress.message(msglevel("noTimePattern", Progress.MSGLEVEL_WARN), MSG.warnNoTimePattern(getClassLabel(clazz), dm.getArrangedHours(clazz.getSchedulingSubpart().getMinutesPerWk(), clazz.effectiveDatePattern())));
            }
            return null;
        }
        
        Set patterns = new HashSet();
        
        DatePattern datePattern = clazz.effectiveDatePattern(); 
        if (datePattern==null) {
            iProgress.message(msglevel("noDatePattern", Progress.MSGLEVEL_WARN), MSG.warnNoDatePattern(getClassLabel(clazz)));
            return null;
        }
        
        iAllUsedDatePatterns.add(datePattern);


        int nrRooms = (clazz.getNbrRooms()==null?1:clazz.getNbrRooms().intValue());

        Set groupPrefs = clazz.effectivePreferences(RoomGroupPref.class);
        Set roomPrefs = clazz.effectivePreferences(RoomPref.class);
        Set bldgPrefs = clazz.effectivePreferences(BuildingPref.class);
        Set featurePrefs = clazz.effectivePreferences(RoomFeaturePref.class);
        
        if (nrRooms>0) {
            boolean reqRoom = false;
            boolean reqBldg = false;
            boolean reqGroup = false;

 			Set allRooms = clazz.getAvailableRooms();
 			
        	for (Iterator i1=allRooms.iterator();i1.hasNext();) {
        		Location room = (Location)i1.next();
        		iProgress.trace("checking room "+room.getLabel()+" ...");
        		boolean add=true;

        		PreferenceCombination pref = new SumPreferenceCombination();
        		
        		// --- group preference ----------
        		PreferenceCombination groupPref = PreferenceCombination.getDefault();
        		for (Iterator i2=groupPrefs.iterator();i2.hasNext();) {
        			RoomGroupPref p = (RoomGroupPref)i2.next();
        			if (p.getRoomGroup().getRooms().contains(room))
        				groupPref.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
        		}
        		
        		if (groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited)) {
                	iProgress.trace("group is prohibited :-(");
                	if (iInteractiveMode)
                        pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
        		}
        		
                if (reqGroup && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired)) {
                	iProgress.trace("building is not required :-(");
                    if (iInteractiveMode)
                    	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
                }
                
                if (!reqGroup && (groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))) {
                	iProgress.trace("group is required, removing all previous rooms (they are not required)");
                	reqGroup=true; 
                    if (iInteractiveMode) {
                        for (RoomLocation r: roomLocations) {
                            r.setPreference(r.getPreference()+100);
                        }
                    } else roomLocations.clear();
                }

                if (!groupPref.getPreferenceProlog().equals(PreferenceLevel.sProhibited) && !groupPref.getPreferenceProlog().equals(PreferenceLevel.sRequired))
                	pref.addPreferenceProlog(groupPref.getPreferenceProlog());
        		
                
                // --- room preference ------------
        		String roomPref = null;
        		
        		PreferenceLevel roomPreference = getRoomPreference(clazz.getManagingDept().getUniqueId(),room.getUniqueId());
        		if (roomPreference!=null) {
        			roomPref = roomPreference.getPrefProlog();

        			if (!iInteractiveMode && PreferenceLevel.sProhibited.equals(roomPref)) {
        				iProgress.trace("room is prohibited (on room level) :-(");
        				add=false;
        			}
        			
        			if (PreferenceLevel.sStronglyDiscouraged.equals(roomPref)) {
        				roomPref = PreferenceLevel.sProhibited;
        			}
    			}
    			
    			for (Iterator i2=roomPrefs.iterator();i2.hasNext();) {
        			RoomPref p = (RoomPref)i2.next();
        			if (room.equals(p.getRoom())) {
        				roomPref = p.getPrefLevel().getPrefProlog();
        				iProgress.trace("room preference is "+p.getPrefLevel().getPrefName());
        				break;
        			}
        		}
        		
                if (roomPref!=null && roomPref.equals(PreferenceLevel.sProhibited)) {
                	iProgress.trace("room is prohibited :-(");
                	if (iInteractiveMode)
                        pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
                }
                
                if (reqRoom && (roomPref==null || !roomPref.equals(PreferenceLevel.sRequired))) {
                	iProgress.trace("room is not required :-(");
                    if (iInteractiveMode)
                    	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
                }
                
                if (!reqRoom && (roomPref!=null && roomPref.equals(PreferenceLevel.sRequired))) {
                	iProgress.trace("room is required, removing all previous rooms (they are not required)");
                    reqRoom=true; 
                    if (iInteractiveMode) {
                        for (RoomLocation r: roomLocations) {
                            r.setPreference(r.getPreference()+100);
                        }
                    } else roomLocations.clear();
                }
                
                if (roomPref!=null && !roomPref.equals(PreferenceLevel.sProhibited) && !roomPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(roomPref);

                // --- building preference ------------
        		Building bldg = (room instanceof Room ? ((Room)room).getBuilding() : null);

        		String bldgPref = null;
        		for (Iterator i2=bldgPrefs.iterator();i2.hasNext();) {
        			BuildingPref p = (BuildingPref)i2.next();
        			if (bldg!=null && bldg.equals(p.getBuilding())) {
        				bldgPref = p.getPrefLevel().getPrefProlog();
        				iProgress.trace("building preference is "+p.getPrefLevel().getPrefName());
        				break;
        			}
        		}
        		
                if (bldgPref!=null && bldgPref.equals(PreferenceLevel.sProhibited)) {
                	iProgress.trace("building is prohibited :-(");
                    if (iInteractiveMode)
                    	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
                }
                
                if (reqBldg && (bldgPref==null || !bldgPref.equals(PreferenceLevel.sRequired))) {
                	iProgress.trace("building is not required :-(");
                    if (iInteractiveMode)
                    	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
                }
                
                if (!reqBldg && (bldgPref!=null && bldgPref.equals(PreferenceLevel.sRequired))) {
                	iProgress.trace("building is required, removing all previous rooms (they are not required)");
                    reqBldg = true;
                    if (iInteractiveMode) {
                        for (RoomLocation r: roomLocations) {
                            r.setPreference(r.getPreference()+100);
                        }
                    } else roomLocations.clear();
                }

                if (bldgPref!=null && !bldgPref.equals(PreferenceLevel.sProhibited) && !bldgPref.equals(PreferenceLevel.sRequired)) pref.addPreferenceProlog(bldgPref);
                
                // --- room features preference --------  
                boolean acceptableFeatures = true;
                PreferenceCombination featurePref = new MinMaxPreferenceCombination();
                for (Iterator i2=featurePrefs.iterator();i2.hasNext();) {
                	RoomFeaturePref roomFeaturePref = (RoomFeaturePref)i2.next();
                	RoomFeature feature = roomFeaturePref.getRoomFeature();
                	String p = roomFeaturePref.getPrefLevel().getPrefProlog();
                	
                	boolean hasFeature = feature.getRooms().contains(room);
                    if (p.equals(PreferenceLevel.sProhibited) && hasFeature) {
                    	iProgress.trace("present feature "+feature.getLabel()+" is prohibited :-(");
                        acceptableFeatures=false;
                    }
                    if (p.equals(PreferenceLevel.sRequired) && !hasFeature) {
                    	iProgress.trace("not present feature "+feature.getLabel()+" is required :-(");
                        acceptableFeatures=false;
                    }
                    if (p!=null && hasFeature && !p.equals(PreferenceLevel.sProhibited) && !p.equals(PreferenceLevel.sRequired)) 
                    	featurePref.addPreferenceProlog(p);
                }
                pref.addPreferenceInt(featurePref.getPreferenceInt());
                
                if (!acceptableFeatures) {
                    if (iInteractiveMode)
                    	pref.addPreferenceProlog(PreferenceLevel.sProhibited);
                    else
                        add=false;
                }
                
                
        		// --- room size ----------------- 
                if (room.getCapacity().intValue()<stronglyDiscouragedCapacity) {
                	iProgress.trace("too small :-(");
                	if (iInteractiveMode)
                		pref.addPreferenceInt(1000);
                	else add=false;
                }
                else if (room.getCapacity().intValue()<discouragedCapacity) {
                	iProgress.trace("room of strongly discouraged size");
                    pref.addPreferenceProlog(PreferenceLevel.sStronglyDiscouraged);
                }
                else if (room.getCapacity().intValue()<roomCapacity) {
                	iProgress.trace("room of discouraged size");
                	pref.addPreferenceProlog(PreferenceLevel.sDiscouraged);
                }
                
                int prefInt = pref.getPreferenceInt();
                iProgress.trace("room preference is "+prefInt);
                
                if (!add) continue;
                
                roomLocations.add(
                		new RoomLocation(
                				room.getUniqueId(),
                				room.getLabel(),
                				(bldg==null?null:bldg.getUniqueId()),
                				prefInt,
                				room.getCapacity().intValue(),
                				room.getCoordinateX(),
                				room.getCoordinateY(),
                				(room.isIgnoreTooFar()==null?false:room.isIgnoreTooFar().booleanValue()),
                				getRoomConstraint(clazz.getManagingDept(),room,hibSession)));
            }
        	
            if (roomLocations.isEmpty() || roomLocations.size()<(clazz.getNbrRooms()==null?1:clazz.getNbrRooms().intValue())) {
            	iProgress.message(msglevel("noRoom", Progress.MSGLEVEL_WARN), clazz.getNbrRooms().intValue() > 1 ? MSG.warnNoRooms(getClassLabel(clazz)) : MSG.warnNoRoom(getClassLabel(clazz)));
            	return null;
            }
        } else {
            if (!groupPrefs.isEmpty() || !roomPrefs.isEmpty() || !bldgPrefs.isEmpty() || !featurePrefs.isEmpty()) 
                iProgress.message(msglevel("zeroRoomsButPref", Progress.MSGLEVEL_WARN), MSG.warnZeroRoomsButPref(getClassLabel(clazz)));
        }
        
        int minPerWeek = clazz.getSchedulingSubpart().getMinutesPerWk().intValue();
        
        boolean onlyReq = false;
        for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
        	TimePref timePref = (TimePref)i1.next();
        	TimePatternModel pattern = timePref.getTimePatternModel();
        	if (pattern.isExactTime() || pattern.countPreferences(PreferenceLevel.sRequired)>0)
        		onlyReq = true;
        }
        if (onlyReq) {
        	iProgress.trace("time pattern has requred times");
        }
        
        ClassDurationType dtype = clazz.getSchedulingSubpart().getInstrOfferingConfig().getEffectiveDurationType();
        DurationModel dm = clazz.getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
        for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
        	TimePref timePref = (TimePref)i1.next();
        	TimePatternModel pattern = timePref.getTimePatternModel();
        	if (pattern.isExactTime()) {
                if (datePattern.getType() == DatePattern.sTypePatternSet) {
                	Set<DatePatternPref> datePatternPrefs = (Set<DatePatternPref>)clazz.effectivePreferences(DatePatternPref.class);
                	boolean hasReq = false;
                	for (DatePatternPref p: datePatternPrefs) {
                		if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) { hasReq = true; break; }
                	}
                	for (DatePattern child: datePattern.findChildren()) {
                		int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(clazz.getSchedulingSubpart().getMinutesPerWk(), child, pattern.getExactDays());
                		int length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
                		int breakTime = ExactTimeMins.getBreakTime(minsPerMeeting);
                		String pr = PreferenceLevel.sNeutral;
                		for (DatePatternPref p: datePatternPrefs) {
                			if (p.getDatePattern().equals(child)) pr = p.getPrefLevel().getPrefProlog();
                		}
                		int prVal = 0;
                		if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
                			prVal = PreferenceLevel.prolog2int(pr);
                		}
                		if (iInteractiveMode) {
                			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) prVal += 100;
                			if (PreferenceLevel.sProhibited.equals(pr)) prVal += 100;
                			
                		} else {
                			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) continue;
                			if (PreferenceLevel.sProhibited.equals(pr)) continue;
                		}
                        TimeLocation  loc = new TimeLocation(
                        		pattern.getExactDays(), pattern.getExactStartSlot(), length,
                        		PreferenceLevel.sIntLevelNeutral, 0, PreferenceLevel.prolog2int(pr), 
                        		child.getUniqueId(), child.getName(), child.getPatternBitSet(),
                        		breakTime);
                        loc.setTimePatternId(pattern.getTimePattern().getUniqueId());
                        if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
                        	loc.setNormalizedPreference(iAlterDatePatternWeight * prVal);
                        }
                        if (loc.getStartSlot() + loc.getLength() > Constants.SLOTS_PER_DAY) {
                        	iProgress.message(msglevel("timeOverMidnight", Progress.MSGLEVEL_ERROR), MSG.warnExactTimeOverMidnight(getClassLabel(clazz), loc.getName(iUseAmPm)));
                        	continue;
                        }
                        timeLocations.add(loc);
                	}
                } else {
            		int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, pattern.getExactDays());
            		int length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
            		int breakTime = ExactTimeMins.getBreakTime(minsPerMeeting);
                    TimeLocation  loc = new TimeLocation(pattern.getExactDays(),pattern.getExactStartSlot(),length,PreferenceLevel.sIntLevelNeutral,0,datePattern.getUniqueId(),datePattern.getName(),datePattern.getPatternBitSet(),breakTime);
                    loc.setTimePatternId(pattern.getTimePattern().getUniqueId());
                    if (loc.getStartSlot() + loc.getLength() > Constants.SLOTS_PER_DAY) {
                    	iProgress.message(msglevel("timeOverMidnight", Progress.MSGLEVEL_ERROR), MSG.warnExactTimeOverMidnight(getClassLabel(clazz), loc.getName(iUseAmPm)));
                    	continue;
                    }
                    timeLocations.add(loc);
                }
        		
                continue;
        	}
        	
        	patterns.add(pattern.getTimePattern());
            
            if (iWeakenTimePreferences) {
                pattern.weakenHardPreferences();
                onlyReq = false;
            }
        	
            if (!dm.isValidCombination(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, timePref.getTimePattern())) {
        		iProgress.message(msglevel("noTimePattern", Progress.MSGLEVEL_WARN), MSG.warnWrongTimePattern(getClassLabel(clazz), clazz.getSchedulingSubpart().getMinutesPerWk(), (dtype == null ? MSG.defaultDurationTypeName() : dtype.getLabel()), pattern.getName()));
        		minPerWeek = pattern.getMinPerMtg()*pattern.getNrMeetings();
        		if (iFixMinPerWeek)
        			clazz.getSchedulingSubpart().setMinutesPerWk(new Integer(minPerWeek));
        	}
            
            for (int time=0;time<pattern.getNrTimes(); time++) {
            	if (pattern.getStartSlot(time) + pattern.getSlotsPerMtg() > Constants.SLOTS_PER_DAY) {
            		iProgress.message(msglevel("timeOverMidnight", Progress.MSGLEVEL_WARN), MSG.warnTimeOverMidnight(pattern.getName(), getClassLabel(clazz), pattern.getStartTime(time)));
            		continue;
            	}
                for (int day=0;day<pattern.getNrDays(); day++) {
                    String pref = pattern.getPreference(day,time);
                	iProgress.trace("checking time "+pattern.getDayHeader(day)+" "+pattern.getTimeHeaderShort(time)+" ("+pref+")");
                	if (!dm.isValidSelection(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, timePref.getTimePattern(), pattern.getDayCode(day))) {
                		iProgress.trace("time is not valid :-(");
                		continue;
                	}
                    if (!iInteractiveMode && pref.equals(PreferenceLevel.sProhibited)) {
                    	iProgress.trace("time is prohibited :-(");
                    	continue;
                    }
                    if (!iInteractiveMode && onlyReq && !pref.equals(PreferenceLevel.sRequired)) {
                    	iProgress.trace("time is not required :-(");
                    	continue;
                    }
                    if (datePattern.getType() == DatePattern.sTypePatternSet) {
                    	Set<DatePatternPref> datePatternPrefs = (Set<DatePatternPref>)clazz.effectivePreferences(DatePatternPref.class);
                    	boolean hasReq = false;
                    	for (DatePatternPref p: datePatternPrefs) {
                    		if (PreferenceLevel.sRequired.equals(p.getPrefLevel().getPrefProlog())) { hasReq = true; break; }
                    	}
                    	for (DatePattern child: datePattern.findChildren()) {
                    		if (!dm.isValidSelection(clazz.getSchedulingSubpart().getMinutesPerWk(), child, timePref.getTimePattern(), pattern.getDayCode(day)))
                    			continue;
                    		String pr = PreferenceLevel.sNeutral;
                    		for (DatePatternPref p: datePatternPrefs) {
                    			if (p.getDatePattern().equals(child)) pr = p.getPrefLevel().getPrefProlog();
                    		}
                    		int prVal = 0;
                    		if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
                    			prVal = PreferenceLevel.prolog2int(pr);
                    		}
                    		if (iInteractiveMode) {
                    			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) prVal += 100;
                    			if (PreferenceLevel.sProhibited.equals(pr)) prVal += 100;
                    			
                    		} else {
                    			if (hasReq && !PreferenceLevel.sRequired.equals(pr)) continue;
                    			if (PreferenceLevel.sProhibited.equals(pr)) continue;
                    		}
                    		TimeLocation  loc = new TimeLocation(
                                    pattern.getDayCode(day),
                                    pattern.getStartSlot(time),
                                    pattern.getSlotsPerMtg(),
                                    PreferenceLevel.prolog2int(pattern.getPreference(day, time)),
                                    pattern.getNormalizedPreference(day,time,iNormalizedPrefDecreaseFactor),
                                    PreferenceLevel.prolog2int(pr),
                                    child.getUniqueId(),
                                    child.getName(),
                                    child.getPatternBitSet(),
                                    pattern.getBreakTime());
                            loc.setTimePatternId(pattern.getTimePattern().getUniqueId());
                        	if (iAlterTimePatternWeight!=0.0) {
                        		String altPref = iAlterTimePatternModel.getCombinedPreference(loc.getDayCode(), loc.getStartSlot(), loc.getLength(), TimePatternModel.sMixAlgMinMax);
                        		if (!altPref.equals(PreferenceLevel.sNeutral)) {
                        			loc.setNormalizedPreference(loc.getNormalizedPreference()+iAlterTimePatternWeight*PreferenceLevel.prolog2int(altPref));
                        		}
                        	}
                            if (iInteractiveMode && onlyReq && !pref.equals(PreferenceLevel.sRequired)) {
                                loc.setPreference(PreferenceLevel.sIntLevelProhibited);
                                loc.setNormalizedPreference(PreferenceLevel.sIntLevelProhibited);
                            }
                            if (!PreferenceLevel.sNeutral.equals(pr) && !PreferenceLevel.sRequired.equals(pr)) {
                            	loc.setNormalizedPreference(loc.getNormalizedPreference() + iAlterDatePatternWeight * prVal);
                            }
                            timeLocations.add(loc);
                    	}
                    } else {
                        TimeLocation  loc = new TimeLocation(
                                pattern.getDayCode(day),
                                pattern.getStartSlot(time),
                                pattern.getSlotsPerMtg(),
                                PreferenceLevel.prolog2int(pattern.getPreference(day, time)),
                                pattern.getNormalizedPreference(day,time,iNormalizedPrefDecreaseFactor),
                                datePattern.getUniqueId(),
                                datePattern.getName(),
                                datePattern.getPatternBitSet(),
                                pattern.getBreakTime());
                        loc.setTimePatternId(pattern.getTimePattern().getUniqueId());
                    	if (iAlterTimePatternWeight!=0.0) {
                    		String altPref = iAlterTimePatternModel.getCombinedPreference(loc.getDayCode(), loc.getStartSlot(), loc.getLength(), TimePatternModel.sMixAlgMinMax);
                    		if (!altPref.equals(PreferenceLevel.sNeutral)) {
                    			loc.setNormalizedPreference(loc.getNormalizedPreference()+iAlterTimePatternWeight*PreferenceLevel.prolog2int(altPref));
                    		}
                    	}
                        if (iInteractiveMode && onlyReq && !pref.equals(PreferenceLevel.sRequired)) {
                            loc.setPreference(PreferenceLevel.sIntLevelProhibited);
                            loc.setNormalizedPreference(PreferenceLevel.sIntLevelProhibited);
                        }
                        timeLocations.add(loc);                    	
                    }
                }
            }
        }
        if (iInteractiveMode) {
        	for (TimePattern pattern: TimePattern.findApplicable(iSession, false, false, false, minPerWeek, datePattern, dm, clazz.getManagingDept())) {
        		if (patterns.contains(pattern)) continue;
        		TimePatternModel model = pattern.getTimePatternModel();
        		iProgress.trace("adding prohibited pattern "+model.getName());
                for (int time=0;time<model.getNrTimes(); time++) {
                    for (int day=0;day<model.getNrDays(); day++) {
                    	if (datePattern.getType() == DatePattern.sTypePatternSet) {
                        	Set<DatePatternPref> datePatternPrefs = (Set<DatePatternPref>)clazz.effectivePreferences(DatePatternPref.class);
                        	for (DatePattern child: datePattern.findChildren()) {
                        		if (!dm.isValidSelection(clazz.getSchedulingSubpart().getMinutesPerWk(), child, pattern, model.getDayCode(day))) continue;
                        		String pr = PreferenceLevel.sNeutral;
                        		for (DatePatternPref p: datePatternPrefs)
                        			if (p.getDatePattern().equals(child)) pr = p.getPrefLevel().getPrefProlog();
                        		TimeLocation  loc = new TimeLocation(
                                        model.getDayCode(day),
                                        model.getStartSlot(time),
                                        model.getSlotsPerMtg(),
                                        PreferenceLevel.prolog2int(model.getPreference(day, time)),
                                        model.getNormalizedPreference(day,time,iNormalizedPrefDecreaseFactor),
                                        PreferenceLevel.prolog2int(pr),
                                        child.getUniqueId(),
                                        child.getName(),
                                        child.getPatternBitSet(),
                                        model.getBreakTime());
                                loc.setTimePatternId(model.getTimePattern().getUniqueId());
                                loc.setPreference(1000);
                                loc.setNormalizedPreference(1000.0);
                                timeLocations.add(loc);
                    		}
                    	} else {
                    		if (!dm.isValidSelection(clazz.getSchedulingSubpart().getMinutesPerWk(), datePattern, pattern, model.getDayCode(day))) continue;
                            TimeLocation  loc = new TimeLocation(
                                    model.getDayCode(day),
                                    model.getStartSlot(time),
                                    model.getSlotsPerMtg(),
                                    PreferenceLevel.prolog2int(model.getPreference(day, time)),
                                    model.getNormalizedPreference(day,time,iNormalizedPrefDecreaseFactor),
                                    datePattern.getUniqueId(),
                                    datePattern.getName(),
                                    datePattern.getPatternBitSet(),
                                    model.getBreakTime()); 
                            loc.setTimePatternId(model.getTimePattern().getUniqueId());
                            loc.setPreference(1000);
                            loc.setNormalizedPreference(1000.0);
                            timeLocations.add(loc);
                    	}
                    }
                }
        	}
        }
        
        if (timeLocations.isEmpty()) {
        	iProgress.message(msglevel("noTime", Progress.MSGLEVEL_WARN), MSG.warnNoTime(getClassLabel(clazz)));
            return null;
        }
    	
    	List<DepartmentalInstructor> instructors = clazz.getLeadInstructors();
    	
    	String className = clazz.getClassLabel(iShowClassSuffix, iShowConfigName);

    	Lecture lecture = new Lecture(
    			clazz.getUniqueId(),
    			clazz.getManagingDept().getSolverGroup().getUniqueId(),
    			clazz.getSchedulingSubpart().getUniqueId(),
    			className,
    			timeLocations,
    			roomLocations,
    			nrRooms,
    			null,
    			minClassLimit, maxClassLimit,
    			room2limitRatio);
    	lecture.setNote(clazz.getNotes());
    	if (clazz.getManagingDept()!=null)
    		lecture.setScheduler(clazz.getManagingDept().getUniqueId());
    	lecture.setDepartment(dept.getUniqueId());
    	for (DepartmentalInstructor instructor: instructors) {
    		getInstructorConstraint(instructor,hibSession).addVariable(lecture);
    	}
    	if (nrRooms > 1)
    		lecture.setMaxRoomCombinations(iMaxRoomCombinations);

    	long estNrValues = lecture.nrValues();
    	if (estNrValues>1000000) {
    	    iProgress.message(msglevel("hugeDomain", Progress.MSGLEVEL_WARN), MSG.warnHugeDomain(getClassLabel(lecture), estNrValues));
            for (DepartmentalInstructor instructor: instructors) {
                getInstructorConstraint(instructor,hibSession).removeVariable(lecture);
            }
    	    return null;
    	} else if (estNrValues>10000) {
            iProgress.message(msglevel("bigDomain", Progress.MSGLEVEL_WARN), MSG.warnBigDomain(getClassLabel(lecture), estNrValues));
    	}
    	
        if (lecture.values(getAssignment()).isEmpty()) {
        	if (!iInteractiveMode) {
        		iProgress.message(msglevel("noPlacement", Progress.MSGLEVEL_WARN), MSG.warnNoPlacement(getClassLabel(lecture)));
                for (DepartmentalInstructor instructor: instructors) {
            		getInstructorConstraint(instructor,hibSession).removeVariable(lecture);
            	}
        		return null;
        	} else
        		iProgress.message(msglevel("noPlacement", Progress.MSGLEVEL_WARN), MSG.warnNoPlacementInteractive(getClassLabel(lecture)));
        }
        if (iClassWeightProvider != null)
        	lecture.setWeight(iClassWeightProvider.getWeight(lecture));
    	iLectures.put(clazz.getUniqueId(),lecture);
        getModel().addVariable(lecture);

        for (RoomLocation r: roomLocations) {
            r.getRoomConstraint().addVariable(lecture);
        }
        
        return lecture;
    }
    
    private void assignCommited() {
    	if (!getModel().hasConstantVariables()) return;
    	setPhase(MSG.phaseAssignCommitted(), getModel().constantVariables().size());
    	for (Lecture lecture: getModel().constantVariables()) {
    		incProgress();
    		if (getAssignment().getValue(lecture)!=null) continue;
    		Placement placement = (Placement)lecture.getInitialAssignment();
    		getModel().weaken(getAssignment(), placement);
    		Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = getModel().conflictConstraints(getAssignment(), placement);
            if (conflictConstraints.isEmpty()) {
                getAssignment().assign(0,placement);
            } else {
                String warn = MSG.warnCannotAssignCommitted(getClassLabel(lecture), placement.getLongName(iUseAmPm));
            	warn += MSG.warnReasonFirstLine();
                for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
                	Set<Placement> vals = conflictConstraints.get(c);
                    for (Placement v: vals) {
                        warn += MSG.warnReasonConflict(getClassLabel(v.variable()), v.getLongName(iUseAmPm));
                    }
                    warn += MSG.warnReasonConstraint(TimetableSolver.getConstraintName(c));
                    iProgress.message(msglevel("cannotAssignCommitted", Progress.MSGLEVEL_WARN), warn);
                }
            }
    	}
    }
    
    private void purgeInvalidValues() {
    	setPhase(MSG.phasePurgeInvalidValues(), getModel().variables().size());
    	for (Lecture lecture: new ArrayList<Lecture>(getModel().variables())) {
    		List<Placement> oldValues = new ArrayList<Placement>(lecture.values(getAssignment()));
    		lecture.purgeInvalidValues(iInteractiveMode);
    		if (lecture.values(getAssignment()).isEmpty()) {
	            String warn = (iInteractiveMode ? MSG.warnNoPlacementAfterCommitInteractive(getClassLabel(lecture)) : MSG.warnNoPlacementAfterCommit(getClassLabel(lecture))); 
    			for (Placement p: oldValues) {
                    warn += MSG.warnReasonNotValid(TimetableSolver.getNotValidReason(p, getAssignment(), iUseAmPm));
    			}
                iProgress.message(msglevel("noPlacementAfterCommit", Progress.MSGLEVEL_WARN), warn);
    			if (!iInteractiveMode) {
    				getModel().removeVariable(lecture);
    				for (Constraint c: new ArrayList<Constraint>(lecture.constraints())) {
    					c.removeVariable(lecture);
    					if (c.variables().isEmpty() || c instanceof BinaryConstraint)
    						getModel().removeConstraint(c);
    				}
                    for (Iterator i=lecture.students().iterator();i.hasNext();) {
                        Student s = (Student)i.next();
                        s.getLectures().remove(lecture);
                    }
    			}
    		}
    		incProgress();
    	}
    }
    
    private void loadAssignment(Assignment assignment) {
    	Lecture lecture = (Lecture)iLectures.get(assignment.getClazz().getUniqueId());
    	int dayCode = assignment.getDays().intValue();
    	int startSlot = assignment.getStartSlot().intValue();
    	Long patternId = assignment.getTimePattern().getUniqueId();
    	DatePattern datePattern = assignment.getDatePattern();
    	Set<Location> rooms = assignment.getRooms();

    	if (lecture==null) return;
    	Placement initialPlacement = null;
    	for (Iterator i2=lecture.values(getAssignment()).iterator();i2.hasNext();) {
    		Placement placement = (Placement)i2.next();
    		if (placement.getTimeLocation().getDayCode()!=dayCode) continue;
    		if (placement.getTimeLocation().getStartSlot()!=startSlot) continue;
    		if (!placement.getTimeLocation().getTimePatternId().equals(patternId)) continue;
    		if (datePattern != null && !placement.getTimeLocation().getDatePatternId().equals(datePattern.getUniqueId())) continue;
    		if (rooms.size() != placement.getNrRooms()) continue;
    		boolean sameRooms = true;
    		for (Iterator i=rooms.iterator();sameRooms && i.hasNext();) {
    			Location r = (Location)i.next();
    			if (!placement.hasRoomLocation(r.getUniqueId()))
    				sameRooms = false;
    		}
    		if (!sameRooms) continue;
    		initialPlacement = placement; break;
    	}
    	if (initialPlacement==null) {
    		TimeLocation timeLocation = null;
        	for (TimeLocation t: lecture.timeLocations()) {
        		if (t.getDayCode()!=dayCode) continue;
        		if (t.getStartSlot()!=startSlot) continue;
        		if (!t.getTimePatternId().equals(patternId)) continue;
        		if (datePattern != null && !t.getDatePatternId().equals(datePattern.getUniqueId())) continue;
        		timeLocation = t; break;
        	}
        	List<RoomLocation> roomLocations = new ArrayList<RoomLocation>();
    		for (Location room: rooms) {
            	for (RoomLocation r: lecture.roomLocations()) {
            		if (r.getId().equals(room.getUniqueId()))
            			roomLocations.add(r);
            	}
    		}
    		if (timeLocation!=null && roomLocations.size()==lecture.getNrRooms()) {
    			initialPlacement = new Placement(lecture,timeLocation,roomLocations);
        	}
    	}
    	if (initialPlacement==null) {
    		StringBuffer sb = new StringBuffer(assignment.getTimeLocation().getLongName(iUseAmPm)+" ");
    		for (Iterator<Location> i=rooms.iterator();i.hasNext();) {
    			sb.append(i.next().getLabel());
    			if (i.hasNext()) sb.append(", ");
    		}
    		if (!assignment.getInstructors().isEmpty()) {
    			sb.append(" ");
    			for (Iterator i=assignment.getInstructors().iterator();i.hasNext();) {
        			sb.append(((DepartmentalInstructor)i.next()).getName(iInstructorFormat));
        			if (i.hasNext()) sb.append(", ");
    			}
    		}
    		iProgress.message(msglevel("placementNotValid", Progress.MSGLEVEL_WARN), MSG.warnPlacementNotValid(getClassLabel(lecture), sb.toString()));
    		return;
    	}
        if (!initialPlacement.isValid()) {
			String reason = "";
           	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
    			if (!ic.isAvailable(lecture, initialPlacement))
    				reason += MSG.warnReasonInstructorNotAvailable(ic.getName());
           	}
	    	if (lecture.getNrRooms()>0) {
	    		if (initialPlacement.isMultiRoom()) {
	    			for (RoomLocation roomLocation: initialPlacement.getRoomLocations()) {
	    				if (!roomLocation.getRoomConstraint().isAvailable(lecture,initialPlacement.getTimeLocation(),lecture.getScheduler()))
	    					reason += MSG.warnReasonRoomNotAvailable(roomLocation.getName());
	    			}
	    		} else {
					if (!initialPlacement.getRoomLocation().getRoomConstraint().isAvailable(lecture,initialPlacement.getTimeLocation(),lecture.getScheduler()))
						reason += MSG.warnReasonRoomNotAvailable(initialPlacement.getRoomLocation().getName());
	    		}
	    	}
	    	Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = getModel().conflictConstraints(getAssignment(), initialPlacement);
            if (!conflictConstraints.isEmpty()) {
                for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
                	Set<Placement> vals = conflictConstraints.get(c);
                    for (Placement p: vals) {
                        Lecture l = p.variable();
                        if (l.isCommitted())
                        	reason += MSG.warnReasonConstraintCommitedAssignment(getClassLabel(l), p.getLongName(iUseAmPm), TimetableSolver.getConstraintName(c));
                        if (p.equals(initialPlacement))
                        	reason += MSG.warnReasonConstraint(TimetableSolver.getConstraintName(c));
                    }
                }
            }
            if (reason.isEmpty())
            	iProgress.message(msglevel("cannotAssign", Progress.MSGLEVEL_WARN), MSG.warnCannotAssignClass(getClassLabel(lecture), initialPlacement.getLongName(iUseAmPm)));
            else
            	iProgress.message(msglevel("cannotAssign", Progress.MSGLEVEL_WARN), MSG.warnCannotAssignClassWithReason(getClassLabel(lecture), initialPlacement.getLongName(iUseAmPm), reason));
		} else {
			if (iMppAssignment) lecture.setInitialAssignment(initialPlacement);
			getModel().weaken(getAssignment(), initialPlacement);
			Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = getModel().conflictConstraints(getAssignment(), initialPlacement);
        	if (conflictConstraints.isEmpty()) {
        		getAssignment().assign(0,initialPlacement);
	        } else {
                String warn = MSG.warnCannotAssignClass(getClassLabel(lecture), initialPlacement.getLongName(iUseAmPm));
                warn += MSG.warnReasonFirstLine();
                for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
                	Set<Placement> vals = conflictConstraints.get(c);
                	for (Placement v: vals) {
                        warn += MSG.warnReasonConflict(getClassLabel(v.variable()), v.getLongName(iUseAmPm));
            	    }
                	warn += MSG.warnReasonConstraint(TimetableSolver.getConstraintName(c));
                    iProgress.message(msglevel("cannotAssign", Progress.MSGLEVEL_WARN), warn);
    	        }
	        }
		}
    }
    
    private RoomConstraint getRoomConstraint(Department dept, Location location, org.hibernate.Session hibSession) {
    	RoomConstraint rc = (RoomConstraint)iRooms.get(location.getUniqueId());
    	if (rc==null) {
    		PreferenceLevel roomPreference = getRoomPreference(dept.getUniqueId(), location.getUniqueId());
    		boolean discouraged = (!iInteractiveMode && roomPreference!=null && (
    				roomPreference.getPrefProlog().equals(PreferenceLevel.sProhibited) || 
    				roomPreference.getPrefProlog().equals(PreferenceLevel.sStronglyDiscouraged) ||
    				roomPreference.getPrefProlog().equals(PreferenceLevel.sDiscouraged)));
    		RoomSharingModel sharingModel = location.getRoomSharingModel();
            if (sharingModel!=null && iIgnoreRoomSharing) {
                for (int d=0;d<sharingModel.getNrDays();d++)
                    for (int t=0;t<sharingModel.getNrTimes();t++)
                        if (!String.valueOf(RoomSharingModel.sNotAvailablePref).equals(sharingModel.getPreference(d, t)))
                            sharingModel.setPreference(d, t, String.valueOf(RoomSharingModel.sFreeForAllPref));
            }
    		if (sharingModel!=null && sharingModel.allAvailable(null))
    			sharingModel=null;
    		Long buildingId = null;
    		if (location instanceof Room) {
    			buildingId = ((Room)location).getBuilding().getUniqueId();
    		}
    		rc = (discouraged?
    				new DiscouragedRoomConstraint(
    						getModel().getProperties(),
    						location.getUniqueId(),
    						location.getLabel(),
    						buildingId,
    						location.getCapacity().intValue(), 
    						sharingModel, 
    						location.getCoordinateX(),
    						location.getCoordinateY(),
    						(location.isIgnoreTooFar()==null?false:location.isIgnoreTooFar().booleanValue()),
    						(location.isIgnoreRoomCheck()==null?true:!location.isIgnoreRoomCheck().booleanValue())
    				)
    			:
    				new RoomConstraint(
    						location.getUniqueId(),
    						location.getLabel(),
    						buildingId,
    						location.getCapacity().intValue(), 
    						sharingModel, 
    						location.getCoordinateX(),
    						location.getCoordinateY(),
    						(location.isIgnoreTooFar()==null?false:location.isIgnoreTooFar().booleanValue()),
    						(location.isIgnoreRoomCheck()==null?true:!location.isIgnoreRoomCheck().booleanValue())
    				)
    			);
            
            rc.setType(location instanceof Room ? ((Room)location).getRoomType().getUniqueId() : null);
            
    		//loadRoomAvailability(location, rc, hibSession);

    		getModel().addConstraint(rc);
    		iRooms.put(location.getUniqueId(),rc);
    	}
    	return rc;
    }
    
    private InstructorConstraint getInstructorConstraint(DepartmentalInstructor instructor, org.hibernate.Session hibSession) {
    	if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().length()>0) {
    		InstructorConstraint ic = (InstructorConstraint)iInstructors.get(instructor.getExternalUniqueId());
    		if (ic!=null) return ic;
    	}
    	InstructorConstraint ic = (InstructorConstraint)iInstructors.get(instructor.getUniqueId());
    	if (ic==null) {
            boolean ignDist = (instructor.isIgnoreToFar()!=null && instructor.isIgnoreToFar().booleanValue());
    		ic = new InstructorConstraint(instructor.getUniqueId(),instructor.getExternalUniqueId(),instructor.getName(iInstructorFormat),ignDist);
            ic.setType(instructor.getPositionType()==null?new Long(Long.MAX_VALUE):new Long(instructor.getPositionType().getSortOrder()));
    		//loadInstructorAvailability(instructor, ic, hibSession);
			getModel().addConstraint(ic);
    		iInstructors.put(instructor.getUniqueId(),ic);
    		if (instructor.getExternalUniqueId()!=null && instructor.getExternalUniqueId().length()>0)
    			iInstructors.put(instructor.getExternalUniqueId(),ic);
    	}
    	return ic;
    }
    
    private void loadInstructorAvailabilities(org.hibernate.Session hibSession, String puids) {
    	Query q = hibSession.createQuery("select distinct i.externalUniqueId, a from ClassInstructor ci inner join ci.instructor i inner join ci.classInstructing.assignments a " +
    			"where ci.lead = true and i.externalUniqueId in ("+puids+") and a.solution.owner.session.uniqueId=:sessionId and a.solution.commited=true and a.solution.owner.uniqueId not in ("+iSolverGroupIds+")");
    	q.setLong("sessionId",iSessionId.longValue());
		for (Iterator i=q.iterate();i.hasNext();) {
			Object[] x = (Object[])i.next();
			String puid = (String)x[0];
			Assignment a = (Assignment)x[1];
			InstructorConstraint ic = (InstructorConstraint)iInstructors.get(puid);
			Placement p = a.getPlacement();
			ic.setNotAvailable(p);
			if (!iLectures.containsKey(a.getClassId())) {
				iLectures.put(a.getClassId(), p.variable());
				getModel().addVariable(p.variable());
			}
			
		}
    }
    
    private void loadInstructorAvailabilities(org.hibernate.Session hibSession) {
    	setPhase(MSG.phaseLoadInstructorAvailabilities(), 1);
    	StringBuffer puids = new StringBuffer();
    	int idx = 0;
    	for (Enumeration e=iInstructors.elements();e.hasMoreElements();) {
    		InstructorConstraint ic = (InstructorConstraint)e.nextElement();
    		if (ic.getPuid()==null) continue;
    		if (puids.length()>0) puids.append(",");
    		puids.append("'"+ic.getPuid()+"'"); idx++;
    		if (idx==100) {
    			loadInstructorAvailabilities(hibSession, puids.toString());
    			puids = new StringBuffer();
				idx = 0;
    		}
    	}
    	if (puids.length()>0) loadInstructorAvailabilities(hibSession, puids.toString());
    	incProgress();
    }
    
    private void loadInstructorStudentConflicts(org.hibernate.Session hibSession, String puids) {
    	for (Object[] x: (List<Object[]>)hibSession.createQuery("select s.uniqueId, s.externalUniqueId from Student s " +
    			"where s.session.uniqueId = :sessionId and s.externalUniqueId in (" + puids + ")")
    			.setLong("sessionId",iSessionId.longValue()).list()) {
    		Long studentId = (Long)x[0];
    		String puid = (String)x[1];
			InstructorConstraint ic = iInstructors.get(puid);
			Student s = iStudents.get(studentId);
			if (s != null && ic != null) {
				iProgress.debug(MSG.debugStudentInstructorPair(puid, s.getId()));
				s.setInstructor(ic);
				for (Lecture lecture: ic.variables()) {
					s.addLecture(lecture);
					lecture.addStudent(getAssignment(), s);
				}
			}
		}
    }
    
    private void loadInstructorStudentConflicts(org.hibernate.Session hibSession) {
    	setPhase(MSG.phaseLoadInstructorStudentConflicts(), 1);
    	StringBuffer puids = new StringBuffer();
    	int idx = 0;
    	for (InstructorConstraint ic: iInstructors.values()) {
    		if (ic.getPuid() == null) continue;
    		if (puids.length() > 0) puids.append(",");
    		puids.append("'"+ic.getPuid()+"'"); idx++;
    		if (idx==100) {
    			loadInstructorStudentConflicts(hibSession, puids.toString());
    			puids = new StringBuffer();
				idx = 0;
    		}
    	}
    	if (puids.length()>0) loadInstructorStudentConflicts(hibSession, puids.toString());
    	incProgress();
    }
    
    private void loadRoomAvailabilities(org.hibernate.Session hibSession, String roomids) {
		Query q = hibSession.createQuery("select distinct r.uniqueId, a from Location r inner join r.assignments as a "+
				"where r.uniqueId in ("+roomids+") and a.solution.owner.session.uniqueId=:sessionId and a.solution.commited=true and a.solution.owner.uniqueId not in ("+iSolverGroupIds+") and r.ignoreRoomCheck = false");
		q.setLong("sessionId",iSessionId.longValue());
		for (Iterator i=q.iterate();i.hasNext();) {
			Object[] x = (Object[])i.next();
			Long roomId = (Long)x[0];
			Assignment a = (Assignment)x[1];
			Placement p = a.getPlacement();
			RoomConstraint rc = (RoomConstraint)iRooms.get(roomId);
			rc.setNotAvailable(p);
			if (!iLectures.containsKey(a.getClassId())) {
				iLectures.put(a.getClassId(), p.variable());
				getModel().addVariable(p.variable());
			}
		}
    }
    
    private void loadRoomAvailabilities(org.hibernate.Session hibSession) {
    	setPhase(MSG.phaseLoadRoomAvailabilities(), 1);
    	StringBuffer roomids = new StringBuffer();
    	int idx = 0;
    	for (Enumeration e=iRooms.elements();e.hasMoreElements();) {
    		RoomConstraint rc = (RoomConstraint)e.nextElement();
    		if (roomids.length()>0) roomids.append(",");
    		roomids.append(rc.getResourceId()); idx++;
    		if (idx==100) {
    			loadRoomAvailabilities(hibSession, roomids.toString());
    			roomids = new StringBuffer();
    			idx = 0;
    		}
    	}
    	if (roomids.length()>0) loadRoomAvailabilities(hibSession, roomids.toString());
    	incProgress();
    }
    
    private Constraint createGroupConstraint(DistributionPref pref) {
    	return createGroupConstraint(pref.getUniqueId(), pref.getDistributionType(), pref.getPrefLevel(), pref.getOwner());
    }
    
    private Constraint createGroupConstraint(Long id, DistributionType type, PreferenceLevel pref, Object owner) {
    	Constraint gc = null;
    	if (type.getReference().matches("_(.+)_")){
    		for (FlexibleConstraintType fcType: FlexibleConstraintType.values()) {
    			if (type.getReference().matches(fcType.getPattern())) {
    				try {
    					gc = fcType.create(id, owner.toString(), pref.getPrefProlog(), type.getReference());
    				} catch (IllegalArgumentException e) {
    					iProgress.warn(MSG.warnFlexibleConstraintNotLoaded(type.getReference()), e);
    				}
    			}
    		}
    		if (gc == null) {
    			iProgress.warn(MSG.warnDistributionConstraintNotKnown(type.getReference()));
	        	return null;
    		}
    	} else if ("SAME_INSTR".equals(type.getReference())) {
    		if (PreferenceLevel.sRequired.equals(pref.getPrefProlog()))
    			gc = new InstructorConstraint(new Long(-id),null, type.getLabel(),false);
    	} else if ("SPREAD".equals(type.getReference())) {
    		gc = new SpreadConstraint(getModel().getProperties(), MSG.nameSpreadConstraint());
    	} else if ("MIN_ROOM_USE".equals(type.getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedRoomsConstraint(getModel().getProperties());
    		else
    			iProgress.message(msglevel("constraintNotUsed", Progress.MSGLEVEL_INFO), MSG.warnMinRoomUseInteractive());
    	} else if ("MIN_GRUSE(10x1h)".equals(type.getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"10x1h",MinimizeNumberOfUsedGroupsOfTime.sGroups10of1h);
    		else
    			iProgress.message(msglevel("constraintNotUsed", Progress.MSGLEVEL_INFO), MSG.warnMinGroupUseInteractive());
    	} else if ("MIN_GRUSE(5x2h)".equals(type.getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"5x2h",MinimizeNumberOfUsedGroupsOfTime.sGroups5of2h);
    		else
    			iProgress.message(msglevel("constraintNotUsed", Progress.MSGLEVEL_INFO), MSG.warnMinGroupUseInteractive());
    	} else if ("MIN_GRUSE(3x3h)".equals(type.getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"3x3h",MinimizeNumberOfUsedGroupsOfTime.sGroups3of3h);
    		else
    			iProgress.message(msglevel("constraintNotUsed", Progress.MSGLEVEL_INFO), MSG.warnMinGroupUseInteractive());
    	} else if ("MIN_GRUSE(2x5h)".equals(type.getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"2x5h",MinimizeNumberOfUsedGroupsOfTime.sGroups2of5h);
    		else
    			iProgress.message(msglevel("constraintNotUsed", Progress.MSGLEVEL_INFO), MSG.warnMinGroupUseInteractive());
    	} else if (IgnoreStudentConflictsConstraint.REFERENCE.equals(type.getReference())) {
    		if (PreferenceLevel.sRequired.equals(pref.getPrefProlog()))
    			gc = new IgnoreStudentConflictsConstraint();
    	} else {
    		GroupConstraint.ConstraintType gcType = GroupConstraint.ConstraintType.get(type.getReference());
    		if (gcType == null) {
    			iProgress.error(MSG.warnDistributionConstraintNotImplemented(type.getReference()));
    			return null;
    		}
    		gc = new GroupConstraint(id, gcType, pref.getPrefProlog());
    	}
    	return gc;
    }
    
    private void errorAddGroupConstraintNotFound(DistributionPref pref, Class_ clazz) {
        if (pref.getOwner()!=null && pref.getOwner() instanceof DepartmentalInstructor) 
            iProgress.message(msglevel("notLoadedInInstrPref", Progress.MSGLEVEL_INFO), MSG.warnClassNotLoadedButUsedInDistPref(getClassLabel(clazz), pref.getLabel()));
        else
            iProgress.message(msglevel("notLoadedInDistPref", Progress.MSGLEVEL_WARN), MSG.warnClassNotLoadedButUsedInDistPref(getClassLabel(clazz), pref.getLabel()));
    }
    
    private Lecture getLecture(Class_ clazz) {
    	Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    	if (lecture!=null) return lecture;
    	if (iAllClasses.contains(clazz)) return null;
    	try {
    		Assignment assignment = clazz.getCommittedAssignment();
        	if (assignment!=null) {
        		Placement placement = assignment.getPlacement();
        		lecture = (Lecture)placement.variable();
        		getModel().addVariable(lecture);
        		iLectures.put(clazz.getUniqueId(), lecture);
        	}
    	} catch (LazyInitializationException e) {
    		Assignment assignment = (new AssignmentDAO()).get(clazz.getCommittedAssignment().getUniqueId());
        	if (assignment!=null) {
        		Placement placement = assignment.getPlacement();
        		lecture = (Lecture)placement.variable();
        		getModel().addVariable(lecture);
        		iLectures.put(clazz.getUniqueId(), lecture);
        	}
    	}
    	return lecture;
    }
    
    private void addGroupConstraint(Constraint<Lecture, Placement> gc) {
    	if (gc.variables().isEmpty()) return;
    	boolean allVariablesAreCommitted = true;
    	for (Lecture lecture: gc.variables()) {
    		if (!lecture.isCommitted()) {
    			allVariablesAreCommitted = false;
    			break;
    		}
    	}
    	if (allVariablesAreCommitted) {
    		String vars = "";
    		for (Lecture l: gc.variables())
    			vars += (vars.isEmpty() ? "" : ", ") + getClassLabel(l);
    		iProgress.debug(MSG.debugDistributionAllCommitted(gc.getName(), vars));
			for (Lecture lecture: new ArrayList<Lecture>(gc.variables())) {
				gc.removeVariable(lecture);
			}
    		return;
    	}
		getModel().addConstraint(gc);
		iProgress.trace("Added constraint "+gc.getName()+" between "+gc.variables());
    }
    
    private void loadGroupConstraint(DistributionPref pref) {
    	Structure structure = pref.getStructure();
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
    		Constraint gc[] = new Constraint[maxSize];
    		Set<Class_> gcClasses[] = new Set[maxSize];
    		for (int i=0;i<gc.length;i++) {
    			gc[i]=createGroupConstraint(pref);
    			if (gc[i]==null) return;
    			gcClasses[i]=new HashSet<Class_>();
    		}
    		List<Lecture> allLectureOfCorrectOrder = new ArrayList<Lecture>();
    		for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
    			DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			Lecture lecture = (Lecture)getLecture(clazz);
        			if (lecture!=null)  allLectureOfCorrectOrder.add(lecture);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Class_ clazz: classes) {
            			Lecture lecture = getLecture(clazz);
            			if (lecture!=null)  allLectureOfCorrectOrder.add(lecture);
        	    	}
        		}
        	}
    		List<DistributionObject> distributionObjects = new ArrayList<DistributionObject>(pref.getDistributionObjects());
    		Collections.sort(distributionObjects, new ChildrenFirstDistributionObjectComparator());
        	for (DistributionObject distributionObject: distributionObjects) {
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			Lecture lecture = (Lecture)getLecture(clazz);
        			if (lecture==null) {
        				errorAddGroupConstraintNotFound(pref, clazz); continue;
        			}
        			for (int j=0;j<gc.length;j++) {
        				gc[j].addVariable(lecture);
        				gcClasses[j].add(clazz);
        			}
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	if (classes.isEmpty()) {
        	    		iProgress.message(msglevel("badDistributionObj", Progress.MSGLEVEL_WARN), MSG.warnBadDistributionObject(pref.getLabel(), getSubpartLabel(subpart)));
        	    		continue;
        	    	}
        	    	for (int j=0;j<gc.length;j++) {
        	    		Class_ clazz = null;
        	    		for (Iterator<Class_> k=gcClasses[j].iterator();k.hasNext() && clazz==null;) {
        	    			clazz = getParentClass(k.next(), subpart);
        	    		}
        	    		if (clazz == null) {
        	    			// no children in the list, filter out classes with wrong parent (there is a subpart parent but no class parent)
        	    			List<Class_> adepts = new ArrayList<Class_>();
        	    			adepts: for (Class_ adept: classes) {
        	    		    	for (Class_ other: gcClasses[j])
        	    		    		if (shareParent(other.getSchedulingSubpart(), adept.getSchedulingSubpart()) && !shareParent(other, adept))
        	    		    			continue adepts;
        	    		    	adepts.add(adept);
        	    			}
        	    			if (!adepts.isEmpty() && adepts.size() < classes.size()) {
            	    			// there are adepts, but some classes have been filtered out -> we may need to skip some previous choices in the adept index
        	    				int k = 0;
        	    				for (int i = 0; i < j; i++) {
        	    					adepts: for (Class_ adept: adepts) {
        	    						for (Class_ other: gcClasses[i])
        	    							if (shareParent(other.getSchedulingSubpart(), adept.getSchedulingSubpart()) && !shareParent(other, adept))
        	    								continue adepts;
        	    						// adept found for group i -> include it in the adept index
        	    						k++; break;
        	    					}
        	    				}
        	    				clazz = adepts.get(k % adepts.size());
        	    			}
        	    		}
        	    		if (clazz == null) {
        	    			// fallback -> take j-th class
        	    			clazz = (Class_)classes.get(j%classes.size());
        	    		}
        	    		
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			
            			gc[j].addVariable(lecture);
            			gcClasses[j].add(clazz);
        	    	}
        		} else {
        			iProgress.message(msglevel("badDistributionObj", Progress.MSGLEVEL_WARN), MSG.warnBadDistributionObjectNotSupported(pref.getLabel(), distributionObject.getPrefGroup().toString()));
        		}
        	}
    		for (int i=0;i<gc.length;i++) {
    			Comparator cmp = new ObjectsByGivenOrderComparator(allLectureOfCorrectOrder);
    			if (!gc[i].variables().isEmpty()) {
    				Collections.sort(gc[i].variables(), cmp);
    				addGroupConstraint(gc[i]);
    			}
    		}
    	} else if (structure == DistributionPref.Structure.Pairwise) {
    		List<Lecture> lectures = new ArrayList<Lecture>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			Lecture lecture = getLecture(clazz);
        			if (lecture==null) {
        				errorAddGroupConstraintNotFound(pref, clazz); continue;
        			}
        			lectures.add(lecture);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Class_ clazz: classes) {
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			lectures.add(lecture);
        	    	}
        		} else {
        			iProgress.message(msglevel("badDistributionObj", Progress.MSGLEVEL_WARN), MSG.warnBadDistributionObjectNotSupported(pref.getLabel(), distributionObject.getPrefGroup().toString()));
        		}
        	}
        	if (lectures.size()<2) {
        		iProgress.message(msglevel("distrPrefIncomplete", Progress.MSGLEVEL_WARN), MSG.warnBadDistributionIncomplete(pref.getLabel()));
        	} else {
        		for (int idx1=0;idx1<lectures.size()-1;idx1++) {
        			Lecture l1 = lectures.get(idx1);
        			for (int idx2=idx1+1;idx2<lectures.size();idx2++) {
        				Lecture l2 = lectures.get(idx2);
        				Constraint gc = createGroupConstraint(pref);
        	    		if (gc==null) return;
        	    		gc.addVariable(l1);
        	    		gc.addVariable(l2);
        	    		addGroupConstraint(gc); 
        			}
        		}
        	}
    	} else if (structure == DistributionPref.Structure.OneOfEach) {
    		List<Lecture> lectures = new ArrayList<Lecture>();
    		List<Integer> counts = new ArrayList<Integer>();
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		int count = 0;
    			if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			Lecture lecture = getLecture(clazz);
        			if (lecture==null) {
        				errorAddGroupConstraintNotFound(pref, clazz); continue;
        			}
        			lectures.add(lecture); count++;
    			} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Class_ clazz: classes) {
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			lectures.add(lecture); count++;
        	    	}
        		} else {
        			iProgress.message(msglevel("badDistributionObj", Progress.MSGLEVEL_WARN), MSG.warnBadDistributionObjectNotSupported(pref.getLabel(), distributionObject.getPrefGroup().toString()));
        		}
    			if (count > 0) counts.add(count);
        	}
        	if (counts.size() > 1) {
        		for (Enumeration<List<Lecture>> e = DistributionPref.permutations(lectures, counts); e.hasMoreElements(); ) {
        			Collection<Lecture> perm = e.nextElement();
    				Constraint gc = createGroupConstraint(pref);
    	    		if (gc==null) return;
        			for (Lecture lecture: perm) gc.addVariable(lecture);
        			addGroupConstraint(gc);
        			iProgress.debug("Posted " + gc.getName() + " between " + gc.variables());
        		}
        	}
    	} else {
	    	Integer grouping = null;
	    	switch (structure) {
	    	case GroupsOfTwo: grouping = 2; break;
	    	case GroupsOfThree: grouping = 3; break;
	    	case GroupsOfFour: grouping = 4; break;
	    	case GroupsOfFive: grouping = 5; break;
	    	}
    		Constraint gc = createGroupConstraint(pref);
    		if (gc==null) return;
        	for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			Lecture lecture = getLecture(clazz);
        			if (lecture==null) {
        				errorAddGroupConstraintNotFound(pref, clazz); continue;
        			}
        			gc.addVariable(lecture);
        			if (grouping != null && gc.variables().size() == grouping) {
            			addGroupConstraint(gc);
            			gc=createGroupConstraint(pref);
            			if (gc==null) return;
            		}
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	List<Class_> classes = new ArrayList<Class_>(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Class_ clazz: classes) {
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			gc.addVariable(lecture);
                		if (grouping != null && gc.variables().size() == grouping) {
               				addGroupConstraint(gc); 
                			gc=createGroupConstraint(pref);
                			if (gc==null) return;
                		}
        	    	}
        		} else {
        			iProgress.message(msglevel("badDistributionObj", Progress.MSGLEVEL_WARN), MSG.warnBadDistributionObjectNotSupported(pref.getLabel(), distributionObject.getPrefGroup().toString()));
        		}
        	}
       		addGroupConstraint(gc);
    	}
    }
    
    private void loadInstructorGroupConstraint(DepartmentalInstructor instructor, DistributionPref pref) {
		Constraint gc = createGroupConstraint(pref);
		if (gc==null) return;
		boolean loadConstraint = false;
    	for (Iterator i=instructor.getClasses().iterator();i.hasNext();) {
    		ClassInstructor classInstructor = (ClassInstructor)i.next();
   			Class_ clazz = (Class_)classInstructor.getClassInstructing();
   			if (classInstructor.isLead() && clazz.getManagingDept().isInheritInstructorPreferences()) {
   				loadConstraint = true; break;
   			}
    	}
    	if (!loadConstraint) return;
    	InstructorConstraint ic = null;
    	if (instructor.getExternalUniqueId() != null && instructor.getExternalUniqueId().length() > 0) {
    		ic = iInstructors.get(instructor.getExternalUniqueId());
    	} else {
    		ic = iInstructors.get(instructor.getUniqueId());
    	}
    	if (ic != null && ic.variables().size() > 1 && iInstructorDistributionsAcrossDepartments) {
    		for (Lecture lecutre: ic.variables())
    			gc.addVariable(lecutre);
    	} else {
        	for (Iterator i=instructor.getClasses().iterator();i.hasNext();) {
        		ClassInstructor classInstructor = (ClassInstructor)i.next();
        		if (!classInstructor.isLead()) continue;
       			Class_ clazz = (Class_)classInstructor.getClassInstructing();
       			Lecture lecture = getLecture(clazz);
       			if (lecture==null) {
       				errorAddGroupConstraintNotFound(pref, clazz); continue;
       			}
       			gc.addVariable(lecture);
        	}
    	}
   		addGroupConstraint(gc);
    	if (ic != null) {
    		List<DistributionType> distributions = iInstructorDistributions.get(ic);
    		if (distributions == null) {
    			distributions = new ArrayList<DistributionType>();
    			iInstructorDistributions.put(ic, distributions);
    		}
    		distributions.add(pref.getDistributionType());
    	}
    }
    
    private void loadInstructorGroupConstraints(DepartmentalInstructor instructor, Set<Long> checkedDistPrefIds) {
    	Set prefs = instructor.getPreferences(DistributionPref.class);
    	if (prefs==null || prefs.isEmpty()) return;
    	for (Iterator i=prefs.iterator();i.hasNext();) {
    		DistributionPref pref = (DistributionPref)i.next();
    		if (checkedDistPrefIds.add(pref.getUniqueId()))
    			loadInstructorGroupConstraint(instructor, pref);
    	}
    }

    private void loadInstructorGroupConstraints(Department department, Set<Long> checkedDistPrefIds, org.hibernate.Session hibSession) {
    	if (!department.isInheritInstructorPreferences()) return;
    	List instructors = null;
    	if (department.isExternalManager()) {
    		instructors = hibSession.createQuery("select distinct i.instructor from Class_ as c inner join c.classInstructors i " +
    				"where i.lead = true and (c.managingDept=:deptId or (c.managingDept is null and c.controllingDept=:deptId))").
    				setLong("deptId", department.getUniqueId()).list();    		
    	} else {
    		instructors = hibSession.createQuery(
    				"select distinct di from DepartmentalInstructor di inner join di.department d where d.uniqueId=:deptId"
    				).setLong("deptId",department.getUniqueId()).list();
    	}
    	if (instructors==null || instructors.isEmpty()) return;
    	setPhase(MSG.phaseLoadInstructorGroupConstraints(department.getShortLabel()), instructors.size());
    	for (Iterator i=instructors.iterator();i.hasNext();) {
    		DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
    		loadInstructorGroupConstraints(instructor, checkedDistPrefIds);
    		incProgress();
    	}
    }
    

    private static Class_ getParentClass(Class_ clazz, SchedulingSubpart parentSubpart) {
		if (clazz==null) return null;
		if (parentSubpart.getClasses().contains(clazz)) return clazz;
		return getParentClass(clazz.getParentClass(), parentSubpart);
	}
    
    private static boolean shareParent(Class_ c1, Class_ c2) {
    	if (c1.getParentClass() == null) return false;
    	for (Class_ p = c2.getParentClass(); p != null; p = p.getParentClass())
    		if (c1.getParentClass().equals(p)) return true;
    	return shareParent(c1.getParentClass(), c2);
    }
    
    private static boolean shareParent(SchedulingSubpart s1, SchedulingSubpart s2) {
    	if (s1.getParentSubpart() == null) return false;
    	for (SchedulingSubpart p = s2.getParentSubpart(); p != null; p = p.getParentSubpart())
    		if (s1.getParentSubpart().equals(p)) return true;
    	return shareParent(s1.getParentSubpart(), s2);
    }
    
    public static class ChildrenFirstDistributionObjectComparator implements Comparator<DistributionObject> {
    	
    	public int compare(DistributionObject d1, DistributionObject d2) {
    		if (d1.getPrefGroup() instanceof Class_) {
    			if (d2.getPrefGroup() instanceof Class_)
    				return d1.compareTo(d2);
    			else
    				return 1; //classes last
    		} else if (d2.getPrefGroup() instanceof Class_)
    			return -1;
    		
    		if (!(d1.getPrefGroup() instanceof SchedulingSubpart) || !(d2.getPrefGroup() instanceof SchedulingSubpart))
    			return d1.compareTo(d2); //should not happen
    		
    		SchedulingSubpart s1 = (SchedulingSubpart)d1.getPrefGroup();
    		SchedulingSubpart s2 = (SchedulingSubpart)d2.getPrefGroup();
    		if (s1.getClasses().size()<=1) {
    			if (s2.getClasses().size()<=1)
    				return d1.compareTo(d2);
    			else
    				return 1; //singleton last
    		} else if (s2.getClasses().size()<=1)
    			return -1;
    		
    		if (getParentClass((Class_)s1.getClasses().iterator().next(),s2)!=null)
    			return -1; //c1 is child, c2 is parent

    		if (getParentClass((Class_)s2.getClasses().iterator().next(),s1)!=null)
    			return 1; //c2 is child, c1 is parent
    		
    		// distribution objects share a parent, return the one with more classes first
    		if (shareParent(s1, s2)) {
    			if (s1.getClasses().size() < s2.getClasses().size())
    				return 1;
    			else if (s1.getClasses().size() > s2.getClasses().size())
    				return -1;
    		}

    		return d1.compareTo(d2);
    	}
    }
    
    private String getClassLimitConstraitName(SchedulingSubpart subpart) {
    	if (subpart==null) return MSG.nameClassLimitConstraint();
    	String name = subpart.getCourseName()+" "+subpart.getItypeDesc().trim();
    	String sufix = subpart.getSchedulingSubpartSuffix();
    	if (sufix!=null && sufix.length()>0)
    		name += " ("+sufix+")";
    	return name;
    }
    
    private String getClassLimitConstraitName(Lecture lecture) {
    	SchedulingSubpart subpart = iSubparts.get(lecture.getSchedulingSubpartId());
    	if (subpart == null) subpart = SchedulingSubpartDAO.getInstance().get(lecture.getSchedulingSubpartId());
    	return getClassLimitConstraitName(subpart);
    }
    
    
    private void createChildrenClassLimitConstraits(Lecture parentLecture) {
    	if (!parentLecture.hasAnyChildren()) return;
    	
		for (Long subpartId: parentLecture.getChildrenSubpartIds()) {
        	List<Lecture> children = parentLecture.getChildren(subpartId);

        	ClassLimitConstraint clc = new ClassLimitConstraint(parentLecture, getClassLimitConstraitName(parentLecture));

        	boolean isMakingSense = false;
        	for (Lecture lecture: children) {
        		createChildrenClassLimitConstraits(lecture);
        		if (!lecture.isCommitted() && lecture.minClassLimit() != lecture.maxClassLimit())
        			isMakingSense=true;
        	} 
        	
        	if (!isMakingSense) continue;
        	
        	for (Lecture lecture: children) {
        		if (!lecture.isCommitted())
        			clc.addVariable(lecture);
        		else
        			clc.setClassLimitDelta(clc.getClassLimitDelta() - iClasses.get(lecture.getClassId()).getClassLimit());
        	}
        	
        	if (clc.variables().isEmpty()) continue;

        	SchedulingSubpart subpart = iSubparts.get(subpartId);
        	if (subpart == null) subpart = SchedulingSubpartDAO.getInstance().get(subpartId);
    		for (Class_ clazz: subpart.getClasses()) {
    			if (iLectures.get(clazz.getUniqueId()) == null)
    				clc.setClassLimitDelta(clc.getClassLimitDelta() - clazz.getClassLimit());
    		}

    		iProgress.trace("Added constraint "+clc.getName()+" between "+clc.variables());
       		getModel().addConstraint(clc);
        }
    }
    
    public void load() {
    	ApplicationProperties.setSessionId(iSessionId);
    	org.hibernate.Session hibSession = null;
    	Transaction tx = null;
    	try {
    		hibSession = TimetableManagerDAO.getInstance().getSession();
    		hibSession.setCacheMode(CacheMode.IGNORE);
    		hibSession.setFlushMode(FlushMode.COMMIT);
    		
    		tx = hibSession.beginTransaction(); 
    		
    		load(hibSession);
    		
    		tx.commit();
    	} catch (Exception e) {
    		iProgress.message(msglevel("loadFailed", Progress.MSGLEVEL_FATAL), MSG.fatalLoadFailed(e.getMessage()), e);
    		tx.rollback();
    	} finally {
    		// here we need to close the session since this code may run in a separate thread
    		if (hibSession!=null && hibSession.isOpen()) hibSession.close();
    	}
    }
    
    private boolean postSameStudentConstraint(Class_ clazz, String type) {
    	boolean posted = false;
    	if (!clazz.getChildClasses().isEmpty()) {
    		for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
    			Class_ c = (Class_)i.next();
    			if (postSameStudentConstraint(c, type))
    				posted = true;
    		}
    	}
    	
    	if (posted) return true;
    	
    	Lecture lecture = getLecture(clazz);
		if (lecture==null) return false;
		
		List<Lecture> variables = new ArrayList<Lecture>();
		variables.add(lecture);
		
		Class_ parent = clazz;
		while ((parent=parent.getParentClass())!=null) {
			Lecture parentLecture = getLecture(parent);
			if (parentLecture!=null)
				variables.add(parentLecture);
		}

    	for (Iterator i=clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator();i.hasNext();) {
    		SchedulingSubpart subpart = (SchedulingSubpart)i.next();
    		if (subpart.getParentSubpart()!=null || subpart.getClasses().size()!=1) continue;
    		Class_ singleClazz = (Class_)subpart.getClasses().iterator().next();
    		Lecture singleLecture = getLecture(singleClazz);
			if (singleLecture!=null && !variables.contains(singleLecture))
				variables.add(singleLecture);
    	}

    	if (variables.size()==1) return false;
    	
    	GroupConstraint gc = new GroupConstraint(null, GroupConstraint.ConstraintType.get(type), PreferenceLevel.sRequired);
		for (Lecture var: variables)
			gc.addVariable(var);
    	
    	addGroupConstraint(gc);
		return true;
    }
    
    private boolean postPrecedenceConstraint(Class_ clazz, String preference) {
    	boolean posted = false;
    	if (!clazz.getChildClasses().isEmpty()) {
    		for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
    			Class_ c = (Class_)i.next();
    			if (postPrecedenceConstraint(c, preference))
    				posted = true;
    		}
    	}
    	
    	if (posted) return true;
    	
    	Lecture lecture = getLecture(clazz);
		if (lecture==null) return false;
		
		List<Lecture> variables = new ArrayList<Lecture>();
		variables.add(lecture);
		Set<Integer> itypes = new HashSet<Integer>();
		itypes.add(clazz.getSchedulingSubpart().getItype().getItype());
		
		Class_ parent = clazz;
		while ((parent=parent.getParentClass())!=null) {
			Lecture parentLecture = getLecture(parent);
			if (parentLecture!=null) {
				variables.add(0, parentLecture);
				itypes.add(parent.getSchedulingSubpart().getItype().getItype());
			}
		}

    	if (variables.size() <= 1 || itypes.size() <= 1) return false;
    	
    	GroupConstraint gc = new GroupConstraint(null, GroupConstraint.ConstraintType.PRECEDENCE, preference);
    	String info = "";
		for (Lecture var: variables) {
			gc.addVariable(var);
			if (!info.isEmpty()) info += ", ";
			info += getClassLabel(var);
		}
		iProgress.info(MSG.infoAutomaticPrecedence(info, PreferenceLevel.prolog2string(preference)));
    	
    	addGroupConstraint(gc);
		return true;
    }
    
    private void propagateCommittedAssignment(HashSet students, Assignment assignment) {
    	Class_ clazz = assignment.getClazz();
    	Lecture parentLecture = null; Class_ c = clazz;
    	while ((parentLecture==null || parentLecture.isCommitted()) && c.getParentClass()!=null) {
    		c = c.getParentClass();
    		parentLecture = (Lecture)iLectures.get(c.getUniqueId());
    	}
    	if (parentLecture!=null && !parentLecture.isCommitted()) {
    		for (Lecture lecture: parentLecture.sameSubpartLectures()) {
    			if (!lecture.equals(parentLecture) && !lecture.isCommitted()) {
    				//iProgress.debug("[A] Students "+students+" cannot enroll "+lecture.getName()+" due to the enrollment of "+clazz.getClassLabel(iShowClassSuffix, iShowConfigName));
    				for (Iterator i=students.iterator();i.hasNext();) {
    					Student student = (Student)i.next();
    					student.addCanNotEnroll(lecture);
    				}
    			}
    		}
    	}
    	
    	if (!clazz.getSchedulingSubpart().getChildSubparts().isEmpty()) {
    		for (Iterator i=clazz.getSchedulingSubpart().getChildSubparts().iterator();i.hasNext();) {
    			SchedulingSubpart subpart = (SchedulingSubpart)i.next();
    			for (Iterator j=subpart.getClasses().iterator();j.hasNext();) {
    				Class_ child = (Class_)j.next();
    				if (!clazz.equals(child.getParentClass()))
    					propagateCommittedAssignment(students, clazz, child);
    			}
    		}
    		
    	}
    }
    
    private void propagateCommittedAssignment(HashSet students, Class_ parent, Class_ clazz) {
    	Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    	if (lecture!=null && !lecture.isCommitted()) {
    		//iProgress.debug("[B] Students "+students+" cannot enroll "+lecture.getName()+" due to the enrollment of "+parent.getClassLabel(iShowClassSuffix, iShowConfigName));
			for (Iterator i=students.iterator();i.hasNext();) {
				Student student = (Student)i.next();
				student.addCanNotEnroll(lecture);
			}
    	} else {
       		for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
       			Class_ child = (Class_)i.next();
       			propagateCommittedAssignment(students, parent, child);
        	}
    	}
    }
    
    private void loadCommittedStudentConflicts(org.hibernate.Session hibSession, Set<Long> offeringsToAvoid) {
        //Load all committed assignment - student relations that may be relevant
		List<Object[]> assignmentEnrollments = (List<Object[]>)hibSession.createQuery(
    			"select distinct a, e.studentId, io.uniqueId from "+
    			"Solution s inner join s.assignments a inner join s.studentEnrollments e inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering io "+
    			"where "+
    			"s.commited=true and s.owner.session.uniqueId=:sessionId and s.owner not in ("+iSolverGroupIds+") and "+
    			"a.clazz=e.clazz").setLong("sessionId", iSessionId.longValue()).list();

    	
		// Filter out relevant relations (relations that are for loaded students)
		Hashtable<Assignment, Set<Student>> assignments = new Hashtable<Assignment, Set<Student>>();
		for (Object[] result: assignmentEnrollments) {
    		Assignment assignment = (Assignment)result[0];
			Long studentId = (Long)result[1];
			Long offeringId = (Long)result[2];
			if (offeringsToAvoid.contains(offeringId)) continue;
    		Student student = (Student)iStudents.get(studentId);
    		if (student!=null) {
    			Set<Student> students = assignments.get(assignment);
    			if (students==null) {
    				students = new HashSet<Student>();
    				assignments.put(assignment, students);
    			}
    			students.add(student);
    		}
		}
		
		// Ensure no assignment-class relation is got from the cache
		for (Iterator i1=assignmentEnrollments.iterator(); i1.hasNext();) {
			Object[] result = (Object[])i1.next();
    		Assignment assignment = (Assignment)result[0];
    		if (!assignments.containsKey(assignment)) hibSession.evict(assignment);
		}
		
		// Make up the appropriate committed placements and propagate those through the course structure
        setPhase(MSG.phaseLoadCommittedStudentConflicts(), assignments.size());
		for (Iterator i1=assignments.entrySet().iterator(); i1.hasNext();) {
			Map.Entry entry = (Map.Entry)i1.next();
			Assignment assignment = (Assignment)entry.getKey();
			HashSet students = (HashSet)entry.getValue();
   			Placement committedPlacement = assignment.getPlacement();
   			for (Iterator i2=students.iterator();i2.hasNext();) {
   				Student student = (Student)i2.next();
    			student.addCommitedPlacement(committedPlacement);
   			}
   			if (!iLectures.containsKey(assignment.getClassId())) {
   				iLectures.put(assignment.getClassId(), committedPlacement.variable());
    			getModel().addVariable(committedPlacement.variable());
    		}
    		propagateCommittedAssignment(students, assignment);
    		incProgress();
        }
    }
    
    private boolean somehowEnroll(Student student, CourseOffering course, float weight, Double priority) {
    	if (course.getInstructionalOffering().isNotOffered()) return false;
    	boolean hasSomethingCommitted = false;
    	config: for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
    		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
    			for (Class_ clazz: subpart.getClasses()) {
    				if (clazz.getCommittedAssignment() != null) {
    					hasSomethingCommitted = true;
    					break config;
    				}
    			}
    		}
    	}
    	if (!hasSomethingCommitted) return false;
    	if (!iOfferings.containsKey(course.getInstructionalOffering()))
			iOfferings.put(course.getInstructionalOffering(), loadOffering(course.getInstructionalOffering(), true));
    	
    	if (iLoadCommittedReservations) {
        	InstructionalOffering offering = course.getInstructionalOffering();
        	Set<Long> reservedClasses = new HashSet<Long>();
    		for (Reservation reservation: offering.getReservations()) {
    			if (reservation.getClasses().isEmpty() && reservation.getConfigurations().isEmpty()) continue;
    			if (reservation instanceof CourseReservation) {
    				CourseReservation cr = (CourseReservation)reservation;
    				if (!course.equals(cr.getCourse())) continue;
    			} else if (reservation instanceof CurriculumReservation) {
    				WeightedStudentId studentId = iWeightedStudents.get(student.getId());
    				if (studentId == null) continue;
    				CurriculumReservation cr = (CurriculumReservation)reservation;
    				boolean match = false;
    				for (AreaClasfMajor acm: studentId.getMajors()) {
    					if (cr.hasArea(acm.getArea()) && cr.hasClassification(acm.getClasf()) && cr.hasMajor(acm.getMajor())) {
    						match = true;
    						break;
    					}
    				}
    				if (!match) continue;
    			} else if (reservation instanceof StudentGroupReservation) {
    				WeightedStudentId studentId = iWeightedStudents.get(student.getId());
    				if (studentId == null) continue;
    				StudentGroupReservation gr = (StudentGroupReservation)reservation;
    				if (gr.getGroup() == null) continue;
    				Group g = studentId.getGroup(gr.getGroup().getGroupAbbreviation());
    				if (g == null || g.getId() < 0) continue;
    			} else if (reservation instanceof IndividualReservation) {
    				IndividualReservation ir = (IndividualReservation)reservation;
    				WeightedStudentId studentId = iWeightedStudents.get(student.getId());
    				if (studentId == null) continue;
    				boolean match = false;
    				for (org.unitime.timetable.model.Student s: ir.getStudents())
    					if (studentId.getStudentId() == s.getUniqueId()) match = true;
    				if (!match) continue;
    			} else continue;
    			for (Class_ clazz: reservation.getClasses()) {
    				propagateReservedClasses(clazz, reservedClasses);
    				Class_ parent = clazz.getParentClass();
    				while (parent != null) {
    					reservedClasses.add(parent.getUniqueId());
    					parent = parent.getParentClass();
    				}
    			}
    			for (InstrOfferingConfig config: reservation.getConfigurations()) {
    				for (SchedulingSubpart subpart: config.getSchedulingSubparts())
    					for (Class_ clazz: subpart.getClasses())
        					reservedClasses.add(clazz.getUniqueId());
    			}
    		}
    		
    		if (!reservedClasses.isEmpty()) {
    			iProgress.debug(course.getCourseName() + ": Student " + student.getId() + " has reserved classes " + reservedClasses);
    			Set<Lecture> prohibited = new HashSet<Lecture>();
                for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
            		boolean hasConfigReservation = false;
                	subparts: for (SchedulingSubpart subpart: config.getSchedulingSubparts())
                		for (Class_ clazz: subpart.getClasses())
                			if (reservedClasses.contains(clazz.getUniqueId())) {
                				hasConfigReservation = true; break subparts;
                			}
            		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
                		boolean hasSubpartReservation = false;
                		for (Class_ clazz: subpart.getClasses())
                			if (reservedClasses.contains(clazz.getUniqueId())) {
                    			hasSubpartReservation = true; break;
                			}
                		// !hasConfigReservation >> all lectures are cannot attend (there is a reservation on a different config)
                		// otherwise if !hasSubpartReservation >> there is reservation on some other subpart --> can attend any of the classes of this subpart
                		if (!hasConfigReservation || hasSubpartReservation)
                    		for (Class_ clazz: subpart.getClasses()) {
                    			if (reservedClasses.contains(clazz.getUniqueId())) continue;
                    			Lecture lecture = iLectures.get(clazz.getUniqueId());
                    			if (lecture != null)
                    				prohibited.add(lecture);
                    		}
                	}
                }
    			iProgress.debug(course.getCourseName() + ": Student " + student.getId() + " cannot attend classes " + prohibited);
    			student.addCanNotEnroll(offering.getUniqueId(), prohibited);
    		}
    	}
    	
    	student.addOffering(course.getInstructionalOffering().getUniqueId(), weight, priority);
        Set<Student> students = iCourse2students.get(course);
        if (students==null) {
            students = new HashSet<Student>();
            iCourse2students.put(course,students);
        }
        students.add(student);
        return true;
    }
    
    private void makeupCommittedStudentConflicts(Set<Long> offeringsToAvoid) {
        setPhase(MSG.phaseMakeupCommittedStudentConflicts(), iStudents.size());
    	for (Student student: iStudents.values()) {
    		Set<WeightedCourseOffering> courses = iStudentCourseDemands.getCourses(student.getId());
    		if (iStudentGroupCourseDemands != null) {
    			Set<WeightedCourseOffering> other = iStudentGroupCourseDemands.getCourses(student.getId());
    			if (other != null && !other.isEmpty()) {
    				if (courses == null) courses = other; else courses.addAll(other);
    			}
    		}
    		incProgress();
    		if (courses == null) continue;
    		for (WeightedCourseOffering course: courses) {
    			if (offeringsToAvoid.contains(course.getCourseOffering().getInstructionalOffering().getUniqueId())) continue;
    			if (!somehowEnroll(student, course.getCourseOffering(), course.getWeight(), iStudentCourseDemands.getEnrollmentPriority(student.getId(), course.getCourseOfferingId())))
    				offeringsToAvoid.add(course.getCourseOffering().getInstructionalOffering().getUniqueId());
    		}
    	}
    }
    
    private void propagateReservedClasses(Class_ clazz, Set<Long> reservedClasses) {
    	reservedClasses.add(clazz.getUniqueId());
    	for (Class_ child: clazz.getChildClasses())
    		propagateReservedClasses(child, reservedClasses);
    }
    
    private boolean canAttend(Set<Lecture> cannotAttendLectures, Collection<Lecture> lectures) {
    	for (Iterator e=lectures.iterator();e.hasNext();) {
    		Lecture lecture = (Lecture)e.next();
    		if (cannotAttendLectures.contains(lecture)) continue;
    		boolean canAttend = true;
    		if (lecture.hasAnyChildren()) {
    			for (Long subpartId: lecture.getChildrenSubpartIds()) {
    				if (!canAttend(cannotAttendLectures, lecture.getChildren(subpartId))) {
    					canAttend = false; break;
    				}
    			}
    		}
    		if (canAttend) return true;
    	}
    	return false;
    }
    
    private boolean canAttendConfigurations(Set<Lecture> cannotAttendLectures, List<Configuration> configurations) {
    	for (Configuration cfg: configurations) {
    		boolean canAttend = true;
    		for (Long subpartId: cfg.getTopSubpartIds()) {
    			if (!canAttend(cannotAttendLectures, cfg.getTopLectures(subpartId))) {
    				canAttend = false; break;
    			}
    		}
    		if (canAttend) return true;
    	}
    	return false;
    }
    
    private void checkReservation(CourseOffering course, Set<Lecture> cannotAttendLectures, List<Configuration> configurations) {
    	if (canAttendConfigurations(cannotAttendLectures, configurations)) return;
    	iProgress.message(msglevel("badCourseReservation", Progress.MSGLEVEL_WARN), MSG.warnBadCourseReservations(getOfferingLabel(course)));
    }
    
    private Collection<InstrOfferingConfig> sortedConfigs(InstructionalOffering offering) {
    	if (offering.getInstrOfferingConfigs().size() <= 1)
    		return offering.getInstrOfferingConfigs();
    	TreeSet<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(offering.getControllingCourseOffering().getSubjectArea().getUniqueId()));
		configs.addAll(offering.getInstrOfferingConfigs());
		return configs;
    }

    private Hashtable<InstrOfferingConfig, Set<SchedulingSubpart>> loadOffering(InstructionalOffering offering, boolean assignCommitted) {
    	// solver group ids for fast check
    	HashSet<Long> solverGroupIds = new HashSet<Long>();
    	for (Long solverGroupId: iSolverGroupId)
    		solverGroupIds.add(solverGroupId);
    	
    	Hashtable<InstrOfferingConfig, Set<SchedulingSubpart>> cfg2topSubparts = new Hashtable<InstrOfferingConfig, Set<SchedulingSubpart>>();
    	
    	// alternative configurations
    	List<Configuration> altCfgs = new ArrayList<Configuration>();
		iAltConfigurations.put(offering, altCfgs);
    	
    	for (InstrOfferingConfig config: sortedConfigs(offering)) {
    		
    		// create a configuration, set alternative configurations
    		Configuration cfg = new Configuration(offering.getUniqueId(), config.getUniqueId(), config.getLimit().intValue());
			Set<SchedulingSubpart> topSubparts = new HashSet<SchedulingSubpart>();
    		
    		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
    			
    			for (Class_ clazz: subpart.getClasses()) {
    				Lecture lecture = iLectures.get(clazz.getUniqueId());
    				if (lecture == null) {
    					if (clazz.getManagingDept().getSolverGroup() == null) {
    						iProgress.message(msglevel("noSolverGroup", Progress.MSGLEVEL_WARN), MSG.warnNoSolverGroup(getClassLabel(clazz), clazz.getManagingDept().getManagingDeptAbbv()));
    						continue;
    					}
        				if (solverGroupIds.contains(clazz.getManagingDept().getSolverGroup().getUniqueId())) continue; // only classes of other problems
        				if (clazz.getCommittedAssignment() == null) continue; // only committed classes
        	   			if (iLectures.containsKey(clazz.getUniqueId())) continue; // already loaded

        	   			Placement committedPlacement = clazz.getCommittedAssignment().getPlacement();
        	   			lecture = committedPlacement.variable();
           				iLectures.put(clazz.getUniqueId(), lecture);
           				iClasses.put(lecture.getClassId(), clazz);
           				iSubparts.put(subpart.getUniqueId(), subpart);
    	    			getModel().addVariable(lecture);
    	    			
    	    			if (assignCommitted) {
    	    				getModel().weaken(getAssignment(), committedPlacement);
        	        		Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = getModel().conflictConstraints(getAssignment(), committedPlacement);
        	                if (conflictConstraints.isEmpty()) {
        	                	getAssignment().assign(0,committedPlacement);
        	                } else {
        	                    String warn = MSG.warnCannotAssignCommitted(getClassLabel(lecture), committedPlacement.getLongName(iUseAmPm));
        	                	warn += MSG.warnReasonFirstLine();
        	                    for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
        	                    	Set<Placement> vals = conflictConstraints.get(c);
        	                        for (Placement v: vals) {
        	                            warn += MSG.warnReasonConflict(getClassLabel(v.variable()), v.getLongName(iUseAmPm));
        	                        }
        	                        warn += MSG.warnReasonConstraint(TimetableSolver.getConstraintName(c));
        	                        iProgress.message(msglevel("cannotAssignCommitted", Progress.MSGLEVEL_WARN), warn);
        	                    }
        	                }
    	    			}
    				}
    			}
    		}
    		
    		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
				List<Lecture> sameSubpart = new ArrayList<Lecture>();

    			for (Class_ clazz: subpart.getClasses()) {
    				Lecture lecture = iLectures.get(clazz.getUniqueId());
    				if (lecture == null) continue;
    				
    				// set parent lecture
    				Class_ parentClazz = clazz.getParentClass();
    				if (parentClazz!=null) {
    					Lecture parentLecture = null; Class_ c = clazz;
    					while (parentLecture == null && c.getParentClass() != null) {
    						c = c.getParentClass();
    						parentLecture = iLectures.get(c.getUniqueId());
    					}
    					if (parentLecture != null)
    						lecture.setParent(parentLecture);
    				}
    				
    				// set same subpart lectures
    				sameSubpart.add(lecture);
    				lecture.setSameSubpartLectures(sameSubpart);
    				
    				if (lecture.getParent() == null) {
        				// set configuration
        				lecture.setConfiguration(cfg);

    					// top subparts
    					topSubparts.add(subpart);
    				}
    			}
    		}
    		
    		if (!cfg.getTopLectures().isEmpty()) { // skip empty configurations
        		altCfgs.add(cfg);
    			cfg.setAltConfigurations(altCfgs);
    			cfg2topSubparts.put(config, topSubparts);
    		}
    	}
    	
    	return cfg2topSubparts;
    }
    
    private void load(org.hibernate.Session hibSession) throws Exception {
		iProgress.setStatus(MSG.statusLoadingInputData());

		TravelTime.populateTravelTimes(getModel().getDistanceMetric(), iSessionId, hibSession);

		iSolverGroup = null;
		iSession = null;
		
        if (iSolverGroup==null) {
        	iSolverGroup = new SolverGroup[iSolverGroupId.length];
        	for (int i=0;i<iSolverGroupId.length;i++) {
        		iSolverGroup[i] = SolverGroupDAO.getInstance().get(iSolverGroupId[i], hibSession);
        		if (iSolverGroup[i]==null) {
        			iProgress.message(msglevel("loadFailed", Progress.MSGLEVEL_FATAL), MSG.fatalUnableToLoadSolverGroup(iSolverGroupId[i]));
        			return;
        		}
        		iProgress.debug("solver group["+(i+1)+"]: "+iSolverGroup[i].getName());
        	}
        }
		if (iSolverGroup==null || iSolverGroup.length==0) {
			iProgress.message(msglevel("loadFailed", Progress.MSGLEVEL_FATAL), MSG.fatalNoSolverGroupLoaded());
			return;
		}
		
    	iDepartmentIds = "";
    	for (int j=0;j<iSolverGroup.length;j++) {
    	    for (Iterator i=iSolverGroup[j].getDepartments().iterator();i.hasNext();) {
    	        Department d = (Department)i.next();
    	        if (iDepartmentIds.length()>0) iDepartmentIds += ",";
    	        iDepartmentIds += d.getUniqueId().toString();
    	    }
    	}
    	getModel().getProperties().setProperty("General.DepartmentIds",iDepartmentIds);
		
        Hashtable<Long, Solution> solutions = null;
        if (iSolutionId!=null && iSolutionId.length>0) {
        	solutions = new Hashtable<Long, Solution>();
        	String note="";
        	for (int i=0;i<iSolutionId.length;i++) {
        		Solution solution = (new SolutionDAO()).get(iSolutionId[i], hibSession);
        		if (solution==null) {
        			iProgress.message(msglevel("loadFailed", Progress.MSGLEVEL_FATAL), MSG.fatalUnableToLoadSolution(iSolutionId[i]));
        			return;
        		}
       			iProgress.debug("solution["+(i+1)+"] version: "+solution.getUniqueId()+" (created "+solution.getCreated()+", solver group "+solution.getOwner().getName()+")");
       			if (solution.getNote() != null && !note.contains(solution.getNote())) {
        			if (note.length()>0) note += "\n";
        			note += solution.getNote();
        		}
        		solutions.put(solution.getOwner().getUniqueId(),solution);
        		if (note.length() > 1000) note = note.substring(0,1000);
        	}
        	getModel().getProperties().setProperty("General.Note",note);
            String solutionIdStr = "";
        	for (int i=0;i<iSolverGroupId.length;i++) {
        		Solution solution = solutions.get(iSolverGroupId[i]);
        		if (solution!=null) {
        			if (solutionIdStr.length()>0) solutionIdStr += ",";
        			solutionIdStr += solution.getUniqueId().toString();
        		}
        	}
        	getModel().getProperties().setProperty("General.SolutionId",solutionIdStr);
        }
        
		if (iSession==null)
			iSession = (new SessionDAO()).get(iSessionId, hibSession);
		if (iSession==null) {
			iProgress.message(msglevel("loadFailed", Progress.MSGLEVEL_FATAL), MSG.fatalNoSessionLoaded());
			return;
		}
		iProgress.debug("session: "+iSession.getLabel());

		getModel().getProperties().setProperty("Data.Term",iSession.getAcademicYearTerm());
		getModel().getProperties().setProperty("Data.Initiative",iSession.getAcademicInitiative());
		getModel().setYear(iSession.getSessionStartYear());
		getModel().getProperties().setProperty("DatePattern.DayOfWeekOffset", String.valueOf(
				Constants.getDayOfWeek(DateUtils.getDate(1, iSession.getPatternStartMonth(), iSession.getSessionStartYear()))));
		if (iSession.getDefaultDatePattern() != null) {
			BitSet pattern = iSession.getDefaultDatePattern().getPatternBitSet();
			String patternStr = "";
			for (int i = 0; i < pattern.length(); i++)
				patternStr += (pattern.get(i) ? "1" : "0");
			getModel().getProperties().setProperty("DatePattern.Default", patternStr);
		}
		
		iAllClasses = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
		for (int i=0;i<iSolverGroup.length;i++) {
			for (Iterator j=iSolverGroup[i].getDepartments().iterator();j.hasNext();) {
	    		Department d = (Department)j.next();
	    		iAllClasses.addAll(d.getClassesFetchWithStructure());
	    	}
		}
		if (iAllClasses==null || iAllClasses.isEmpty()) {
			iProgress.message(msglevel("noClasses", Progress.MSGLEVEL_FATAL), MSG.fatalNoClassesToLoad());
			return;
		}
		iProgress.debug("classes to load: "+iAllClasses.size());
		
		setPhase(MSG.phaseLoadingClasses(),iAllClasses.size());
		int ord = 0;
		HashSet<SchedulingSubpart> subparts = new HashSet<SchedulingSubpart>();
		for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
			Class_ clazz = (Class_)i1.next();
			Lecture lecture = loadClass(clazz,hibSession);
			subparts.add(clazz.getSchedulingSubpart());
			if (lecture!=null) 
				lecture.setOrd(ord++);
			iClasses.put(clazz.getUniqueId(),clazz);
			incProgress();
		}
		
		loadInstructorAvailabilities(hibSession);
		
		loadRoomAvailabilities(hibSession);
		
		setPhase(MSG.phaseLoadingOfferings(), iAllClasses.size());
    	Set<Long> loadedOfferings = new HashSet<Long>();
		for (Class_ clazz: iAllClasses) {
			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
			incProgress();
			
			if (lecture==null) continue; //skip classes that were not loaded
			
			InstructionalOffering offering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
			if (!loadedOfferings.add(offering.getUniqueId())) continue; // already loaded
			
			iOfferings.put(offering, loadOffering(offering, false));
		}
		
		List<DistributionPref> distPrefs = new ArrayList<DistributionPref>();
		for (int i=0;i<iSolverGroup.length;i++) {
			distPrefs.addAll(iSolverGroup[i].getDistributionPreferences());
		}
		setPhase(MSG.phaseLoadingDistributions(),distPrefs.size());
		for (Iterator i=distPrefs.iterator();i.hasNext();) {
			DistributionPref distributionPref = (DistributionPref)i.next();
			if (!PreferenceLevel.sNeutral.equals(distributionPref.getPrefLevel().getPrefProlog()))
				loadGroupConstraint(distributionPref);
			incProgress();
		}
		
		Set<Long> checkedDistPrefIds = new HashSet<Long>();
		for (int i=0;i<iSolverGroup.length;i++) {
			for (Iterator j=iSolverGroup[i].getDepartments().iterator();j.hasNext();) {
				loadInstructorGroupConstraints((Department)j.next(), checkedDistPrefIds, hibSession);
			}
		}
		
		if (iAutoSameStudents) {
			setPhase(MSG.phasePostingSameStudents(),iAllClasses.size());
    		for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
    			Class_ clazz = (Class_)i1.next();
    			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    			if (lecture==null) continue;
    			
    			if (!lecture.hasAnyChildren())
    				postSameStudentConstraint(clazz, iAutoSameStudentsConstraint);
    			
    			incProgress();
    		}
		}
		
		if (iAutoPrecedence != null) {
			PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(iAutoPrecedence);
			if (pref == null) { // Lookup preference if needed
				for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList())
					if (iAutoPrecedence.equalsIgnoreCase(p.getPrefProlog()) || iAutoPrecedence.equalsIgnoreCase(p.getPrefName()) || iAutoPrecedence.equals(p.getAbbreviation())) {
						pref = p; break;
					}
			}
			if (pref == null) {
				iProgress.message(msglevel("autoPrecedence", Progress.MSGLEVEL_WARN), MSG.warnPrecedenceNotRecognized(iAutoPrecedence));
			} else if (!PreferenceLevel.sNeutral.equals(pref.getPrefProlog())) {
				setPhase(MSG.phasePostingAutomaticPrecedences(),iAllClasses.size());
	    		for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
	    			Class_ clazz = (Class_)i1.next();
	    			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
	    			if (lecture==null) continue;
	    			
	    			if (!lecture.hasAnyChildren())
	    				postPrecedenceConstraint(clazz, pref.getPrefProlog());
	    			
	    			incProgress();
	    		}				
			}
		}
		
		postAutomaticHierarchicalConstraints(hibSession);
		
		postAutomaticInstructorConstraints(hibSession);
		
		assignCommited();
		
    	setPhase(MSG.phasePostingClassLimits(), iOfferings.size());
    	for (Map.Entry<InstructionalOffering, Hashtable<InstrOfferingConfig, Set<SchedulingSubpart>>> entry: iOfferings.entrySet()) {
    		Hashtable<InstrOfferingConfig, Set<SchedulingSubpart>> topSubparts = entry.getValue();
    		for (Map.Entry<InstrOfferingConfig, Set<SchedulingSubpart>> subpartEntry: topSubparts.entrySet()) {
    			InstrOfferingConfig config = subpartEntry.getKey();
    			Set<SchedulingSubpart> topSubpartsThisConfig = subpartEntry.getValue();
    			for (SchedulingSubpart subpart: topSubpartsThisConfig) {

    				boolean isMakingSense = false;
    				for (Class_ clazz: subpart.getClasses()) {
    					Lecture lecture = iLectures.get(clazz.getUniqueId());
    					if (lecture == null) continue;
    					createChildrenClassLimitConstraits(lecture);
    					if (!lecture.isCommitted() && lecture.minClassLimit() != lecture.maxClassLimit())
        					isMakingSense=true;
        			}
    				
    				if (!isMakingSense) continue;
    				
        			if (subpart.getParentSubpart()==null) {
        				
            			ClassLimitConstraint clc = new ClassLimitConstraint(config.getLimit(), getClassLimitConstraitName(subpart));
            			
            			for (Class_ clazz: subpart.getClasses()) {
            				Lecture lecture = iLectures.get(clazz.getUniqueId());
            				if (lecture == null || lecture.isCommitted()) {
            					clc.setClassLimitDelta(clc.getClassLimitDelta() - clazz.getClassLimit());
            					continue;
            				}
            				clc.addVariable(lecture);
            			}
            			
            			if (clc.variables().isEmpty()) continue;
            			
           	    		iProgress.trace("Added constraint "+clc.getName()+" between "+clc.variables());
           	    		getModel().addConstraint(clc);
        			
        			} else {
        			
        				Hashtable<Long, ClassLimitConstraint> clcs = new Hashtable<Long, ClassLimitConstraint>();
        				
            			for (Class_ clazz: subpart.getClasses()) {
            				Lecture lecture = iLectures.get(clazz.getUniqueId());

            				Class_ parentClazz = clazz.getParentClass();
            				
            				ClassLimitConstraint clc = clcs.get(parentClazz.getUniqueId());
            				if (clc == null) {
            					clc = new ClassLimitConstraint(parentClazz.getClassLimit(), parentClazz.getClassLabel(iShowClassSuffix, iShowConfigName));
            					clcs.put(parentClazz.getUniqueId(), clc);
            				}
            				
            				if (lecture == null || lecture.isCommitted()) {
        						clc.setClassLimitDelta(clc.getClassLimitDelta()-clazz.getClassLimit());
            				} else {
                				clc.addVariable(lecture);
            				}
            			}
            			for (ClassLimitConstraint clc: clcs.values()) {
            				if (!clc.variables().isEmpty()) {
                				iProgress.trace("Added constraint "+clc.getName()+" between "+clc.variables());
                				getModel().addConstraint(clc);
            				}
            			}
        			}
        			
    			}
    		}
    		incProgress();
    	}
		
		iStudentCourseDemands.init(hibSession, iProgress, iSession, iOfferings.keySet());
		if (iStudentGroupCourseDemands != null)
			iStudentGroupCourseDemands.init(hibSession, iProgress, iSession, iOfferings.keySet());

    	setPhase(MSG.phaseLoadingStudents(),iOfferings.size());
    	for (InstructionalOffering offering: iOfferings.keySet()) {
    		
    		boolean unlimitedOffering = false;
    		int offeringLimit = 0;
    		for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
    			if (config.isUnlimitedEnrollment())
    				unlimitedOffering = true;
    			else
    				offeringLimit += config.getLimit();
    		
    		
    		Double factor = null;
    		if (!unlimitedOffering) {
        		int totalCourseLimit = 0;
        		
        		for (CourseOffering course: offering.getCourseOfferings()) {
            		int courseLimit = -1;
            		if (course.getReservation() != null)
            			courseLimit = course.getReservation();
            		if (courseLimit < 0) {
            			if (offering.getCourseOfferings().size() == 1)
            				courseLimit = offeringLimit;
            			else {
            				iProgress.message(msglevel("crossListWithoutReservation", Progress.MSGLEVEL_INFO), MSG.infoCrosslistNoCourseReservations(getOfferingLabel(course)));
            				if (course.getProjectedDemand() != null && offering.getProjectedDemand() > 0)
            					courseLimit = course.getProjectedDemand();
            				else if (course.getDemand() != null && offering.getDemand() > 0)
            					courseLimit = course.getDemand();
            				else
            					courseLimit = offeringLimit / offering.getCourseOfferings().size();
            			}
            		}
            		
            		totalCourseLimit += courseLimit;
        		}
        		
        		if (totalCourseLimit < offeringLimit)
        			iProgress.message(msglevel("courseReservationsBelowLimit", totalCourseLimit == 0 ? Progress.MSGLEVEL_INFO : Progress.MSGLEVEL_WARN), MSG.warnReservationBelowLimit(getOfferingLabel(offering), totalCourseLimit, offeringLimit));

        		if (totalCourseLimit > offeringLimit)
        			iProgress.message(msglevel("courseReservationsOverLimit", Progress.MSGLEVEL_INFO), MSG.warnReservationsOverLimit(getOfferingLabel(offering), totalCourseLimit, offeringLimit));
        		
        		if (totalCourseLimit == 0) continue;
        		
        		if (totalCourseLimit != offeringLimit)
        			factor = new Double(((double)offeringLimit) / totalCourseLimit);    			
    		}
    		
    		for (CourseOffering course: offering.getCourseOfferings()) {
        		Set<WeightedStudentId> studentIds = iStudentCourseDemands.getDemands(course);
        		
        		if (iStudentGroupCourseDemands != null) {
        			Set<WeightedStudentId> other = iStudentGroupCourseDemands.getDemands(course);
        			if (other != null && !other.isEmpty()) {
        				if (studentIds == null) studentIds = other; else studentIds.addAll(other);
        			}
        		}
        		
				float studentWeight = 0.0f;
				if (studentIds != null)
					for (WeightedStudentId studentId: studentIds)
						studentWeight += studentId.getWeight();

        		int courseLimit = -1;
        		if (course.getReservation() != null)
        			courseLimit = course.getReservation();
        		if (courseLimit < 0) {
        			if (offering.getCourseOfferings().size() == 1 && !unlimitedOffering)
        				courseLimit = offeringLimit;
        			else {
        				courseLimit = Math.round(studentWeight);
        			}
        		}
        		
        		if (factor!=null)
        			courseLimit = (int)Math.round(courseLimit * factor);
        		
        		if (studentIds == null || studentIds.isEmpty()) {
        			iProgress.message(msglevel("offeringWithoutDemand", Progress.MSGLEVEL_INFO), MSG.infoNoStudentInCourse(getOfferingLabel(course)));
        			continue;
        		}
        		
        		if (courseLimit == 0 && offering.getCourseOfferings().size() > 1) {
        			iProgress.message(msglevel("noCourseReservation", Progress.MSGLEVEL_WARN), MSG.warnNoReservedSpaceForCourse(getOfferingLabel(course)));
        		}
        		
        		double weight = (iStudentCourseDemands.isWeightStudentsToFillUpOffering() && courseLimit != 0 ? (double)courseLimit / studentWeight : 1.0);
        		
        		Set<Lecture> cannotAttendLectures = null;
        		
        		if (offering.getCourseOfferings().size() > 1) {
        			
        			Set<Long> reservedClasses = new HashSet<Long>();
        			int limit = 0;
        			boolean unlimited = false;

        			for (Reservation r: offering.getReservations()) {
        				if (r instanceof CourseReservation && course.equals(((CourseReservation)r).getCourse())) {
            				for (Class_ clazz: r.getClasses()) {
            					limit += clazz.getMaxExpectedCapacity();
        						propagateReservedClasses(clazz, reservedClasses);
        						Class_ parent = clazz.getParentClass();
            					while (parent != null) {
            						reservedClasses.add(parent.getUniqueId());
            						parent = parent.getParentClass();
            					}
            				}
            				for (InstrOfferingConfig config: r.getConfigurations()) {
            					if (config.isUnlimitedEnrollment())
            						unlimited = true;
            					else
            						limit += config.getLimit();
            					for (SchedulingSubpart subpart: config.getSchedulingSubparts())
            						for (Class_ clazz: subpart.getClasses())
                    					reservedClasses.add(clazz.getUniqueId());
            				}
        				}
        			}
        			
            		if (!reservedClasses.isEmpty()) {
        				iProgress.debug("Course requests for course "+getOfferingLabel(course)+" are "+reservedClasses);
                        if (!unlimited && courseLimit > limit)
                            iProgress.message(msglevel("insufficientCourseReservation", Progress.MSGLEVEL_WARN), MSG.warnTooLittleSpaceInCourse(getOfferingLabel(course), limit, courseLimit));
                        cannotAttendLectures = new HashSet<Lecture>();
                        for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
                    		boolean hasConfigReservation = false;
                        	subparts: for (SchedulingSubpart subpart: config.getSchedulingSubparts())
                        		for (Class_ clazz: subpart.getClasses())
                        			if (reservedClasses.contains(clazz.getUniqueId())) {
                        				hasConfigReservation = true; break subparts;
                        			}
                    		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
                        		boolean hasSubpartReservation = false;
                        		for (Class_ clazz: subpart.getClasses())
                        			if (reservedClasses.contains(clazz.getUniqueId())) {
                            			hasSubpartReservation = true; break;
                        			}
                        		// !hasConfigReservation >> all lectures are cannot attend (there is a reservation on a different config)
                        		// otherwise if !hasSubpartReservation >> there is reservation on some other subpart --> can attend any of the classes of this subpart
                        		if (!hasConfigReservation || hasSubpartReservation)
                            		for (Class_ clazz: subpart.getClasses()) {
                            			if (reservedClasses.contains(clazz.getUniqueId())) continue;
                            			Lecture lecture = iLectures.get(clazz.getUniqueId());
                            			if (lecture != null && (iLoadCommittedReservations || !lecture.isCommitted()))
                            				cannotAttendLectures.add(lecture);
                            		}
                        	}
                        }
                        if (!cannotAttendLectures.isEmpty()) {
                        	iProgress.debug("Prohibited lectures for course " + getOfferingLabel(course)+" are " + cannotAttendLectures);
                        	checkReservation(course, cannotAttendLectures, iAltConfigurations.get(offering));
                        }
        			}
        		}

    			for (WeightedStudentId studentId: studentIds) {
        			Student student = iStudents.get(studentId.getStudentId());
        			if (student==null) {
        				student = new Student(studentId.getStudentId());
        				student.setAcademicArea(studentId.getArea());
        				student.setAcademicClassification(studentId.getClasf());
        				student.setMajor(studentId.getMajor());
        				student.setCurriculum(studentId.getCurriculum());
        				getModel().addStudent(student);
        				iStudents.put(studentId.getStudentId(), student);
        				iWeightedStudents.put(studentId.getStudentId(), studentId);
        				for (Group g: studentId.getGroups()) {
        					StudentGroup group = iGroups.get(g.getId());
        					if (group == null) {
        						group = new StudentGroup(g.getId(), g.getWeight(), g.getName());
        						iGroups.put(g.getId(), group);
        						getModel().addStudentGroup(group);
        					}
        					group.addStudent(student);
        					student.addGroup(group);
        				}
        			}
        			student.addOffering(offering.getUniqueId(), weight * studentId.getWeight(), iStudentCourseDemands.getEnrollmentPriority(studentId.getStudentId(), course.getUniqueId()));
        			
                    Set<Student> students = iCourse2students.get(course);
                    if (students==null) {
                        students = new HashSet<Student>();
                        iCourse2students.put(course,students);
                    }
                    students.add(student);

        			student.addCanNotEnroll(offering.getUniqueId(), cannotAttendLectures);
        			
					Set<Long> reservedClasses = new HashSet<Long>();
        			for (Reservation reservation: offering.getReservations()) {
        				if (reservation.getClasses().isEmpty() && reservation.getConfigurations().isEmpty()) continue;
        				if (reservation instanceof CourseReservation) continue;
        				if (reservation instanceof CurriculumReservation) {
        					CurriculumReservation cr = (CurriculumReservation)reservation;
        					boolean match = false;
        					for (AreaClasfMajor acm: studentId.getMajors()) {
        						if (cr.hasArea(acm.getArea()) && cr.hasClassification(acm.getClasf()) && cr.hasMajor(acm.getMajor())) {
        							match = true;
        							break;
        						}
        					}
        					if (!match) continue;
        				} else if (reservation instanceof StudentGroupReservation) {
        					StudentGroupReservation gr = (StudentGroupReservation)reservation;
        					if (gr.getGroup() == null) continue;
        					Group g = studentId.getGroup(gr.getGroup().getGroupAbbreviation());
        					if (g == null || g.getId() < 0) continue;
        				} else if (reservation instanceof IndividualReservation) {
            				IndividualReservation ir = (IndividualReservation)reservation;
            				boolean match = false;
            				for (org.unitime.timetable.model.Student s: ir.getStudents())
            					if (studentId.getStudentId() == s.getUniqueId()) match = true;
            				if (!match) continue;
            			} else continue;
        				for (Class_ clazz: reservation.getClasses()) {
    						propagateReservedClasses(clazz, reservedClasses);
    						Class_ parent = clazz.getParentClass();
        					while (parent != null) {
        						reservedClasses.add(parent.getUniqueId());
        						parent = parent.getParentClass();
        					}
        				}
        				for (InstrOfferingConfig config: reservation.getConfigurations()) {
        					for (SchedulingSubpart subpart: config.getSchedulingSubparts())
        						for (Class_ clazz: subpart.getClasses())
                					reservedClasses.add(clazz.getUniqueId());
        				}
        			}
        			
        			if (!reservedClasses.isEmpty()) {
        				iProgress.debug(course.getCourseName() + ": Student " + student.getId() + " has reserved classes " + reservedClasses);
        				Set<Lecture> prohibited = new HashSet<Lecture>();
                        for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
                    		boolean hasConfigReservation = false;
                        	subparts: for (SchedulingSubpart subpart: config.getSchedulingSubparts())
                        		for (Class_ clazz: subpart.getClasses())
                        			if (reservedClasses.contains(clazz.getUniqueId())) {
                        				hasConfigReservation = true; break subparts;
                        			}
                    		for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
                        		boolean hasSubpartReservation = false;
                        		for (Class_ clazz: subpart.getClasses())
                        			if (reservedClasses.contains(clazz.getUniqueId())) {
                            			hasSubpartReservation = true; break;
                        			}
                        		// !hasConfigReservation >> all lectures are cannot attend (there is a reservation on a different config)
                        		// otherwise if !hasSubpartReservation >> there is reservation on some other subpart --> can attend any of the classes of this subpart
                        		if (!hasConfigReservation || hasSubpartReservation)
                            		for (Class_ clazz: subpart.getClasses()) {
                            			if (reservedClasses.contains(clazz.getUniqueId())) continue;
                            			Lecture lecture = iLectures.get(clazz.getUniqueId());
                            			if (lecture != null && (iLoadCommittedReservations || !lecture.isCommitted()))
                            				prohibited.add(lecture);
                            		}
                        	}
                        }
        				iProgress.debug(course.getCourseName() + ": Student " + student.getId() + " cannot attend classes " + prohibited);
        				student.addCanNotEnroll(offering.getUniqueId(), prohibited);
        			}
        		}
        	}
    		
        	incProgress();
    	}
    	iProgress.debug(iStudents.size()+" students loaded.");
    	
    	if (!hibSession.isOpen())
    		iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());

    	if (iCommittedStudentConflictsMode == CommittedStudentConflictsMode.Load && !iStudentCourseDemands.isMakingUpStudents())
    		loadCommittedStudentConflicts(hibSession, loadedOfferings);
    	else if (iCommittedStudentConflictsMode != CommittedStudentConflictsMode.Ignore)
    		makeupCommittedStudentConflicts(loadedOfferings);
    	
    	if (!hibSession.isOpen())
    		iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());

    	Hashtable<Student, Set<Lecture>> iPreEnrollments = new Hashtable<Student, Set<Lecture>>();
    	if (iLoadStudentEnrlsFromSolution) {
    		if (iStudentCourseDemands.canUseStudentClassEnrollmentsAsSolution()) {
    			// Load real student enrollments (not saved last-like)
    			List<Object[]> enrollments = (List<Object[]>)hibSession.createQuery(
    					"select distinct e.student.uniqueId, e.clazz.uniqueId from " +
    					"StudentClassEnrollment e, Class_ c where " + 
    					"e.courseOffering.instructionalOffering = c.schedulingSubpart.instrOfferingConfig.instructionalOffering and " +
    					"c.managingDept.solverGroup.uniqueId in (" + iSolverGroupIds + ")").list();
    			setPhase(MSG.phaseLoadingStudentEnrollemnts(), enrollments.size());
    			int totalEnrollments = 0;
    			for (Object[] o: enrollments) {
            		Long studentId = (Long)o[0];
            		Long clazzId = (Long)o[1];
            		
                    Student student = (Student)iStudents.get(studentId);
                    if (student==null) continue;
                    
                    Lecture lecture = (Lecture)iLectures.get(clazzId);
                    if (lecture!=null) {

                		Set<Lecture> preEnrollments = iPreEnrollments.get(student);
                		if (preEnrollments == null) {
                			preEnrollments = new HashSet<Lecture>();
                			iPreEnrollments.put(student, preEnrollments);
                		}
                		preEnrollments.add(lecture);
                		
                		if (student.hasOffering(lecture.getConfiguration().getOfferingId()) && student.canEnroll(lecture)) {
                    		student.addLecture(lecture);
                    		lecture.addStudent(getAssignment(), student);
                    		totalEnrollments ++;
                    	}
                    }

    				incProgress();
    			}
    			iProgress.message(msglevel("enrollmentsLoaded", Progress.MSGLEVEL_INFO), MSG.infoEnrollmentsLoaded(totalEnrollments, iPreEnrollments.size()));
    		} else {
    			// Load enrollments from selected / committed solutions
            	for (int idx=0;idx<iSolverGroupId.length;idx++) {
            		Solution solution = (solutions == null ? null : solutions.get(iSolverGroupId[idx]));
            		List studentEnrls = null;
            		if (solution != null) {
            			studentEnrls = hibSession
            				.createQuery("select distinct e.studentId, e.clazz.uniqueId from StudentEnrollment e where e.solution.uniqueId=:solutionId")
            				.setLong("solutionId", solution.getUniqueId())
            				.list();
            		} else {
            			studentEnrls = hibSession
        				.createQuery("select distinct e.studentId, e.clazz.uniqueId from StudentEnrollment e where e.solution.owner.uniqueId=:sovlerGroupId and e.solution.commited = true")
        				.setLong("sovlerGroupId", iSolverGroupId[idx])
        				.list();
            		}
            		setPhase(MSG.phaseLoadingStudentEnrollemntsPhase(idx+1),studentEnrls.size());
                	for (Iterator i1=studentEnrls.iterator();i1.hasNext();) {
                		Object o[] = (Object[])i1.next();
                		Long studentId = (Long)o[0];
                		Long clazzId = (Long)o[1];
                		
                        Student student = (Student)iStudents.get(studentId);
                        if (student==null) continue;
                        
                        Lecture lecture = (Lecture)iLectures.get(clazzId);
                        if (lecture != null && lecture.getConfiguration() != null) {
                    		Set<Lecture> preEnrollments = iPreEnrollments.get(student);
                    		if (preEnrollments == null) {
                    			preEnrollments = new HashSet<Lecture>();
                    			iPreEnrollments.put(student, preEnrollments);
                    		}
                    		preEnrollments.add(lecture);

                        	if (student.hasOffering(lecture.getConfiguration().getOfferingId()) && student.canEnroll(lecture)) {
                        		student.addLecture(lecture);
                        		lecture.addStudent(getAssignment(), student);
                        	}
                        }
                        
                        incProgress();
                	}
            	}
            	
            	if (getModel().getProperties().getPropertyBoolean("Global.LoadOtherCommittedStudentEnrls", true)) {
                	// Other committed enrollments
        			List<Object[]> enrollments = (List<Object[]>)hibSession.createQuery(
        					"select distinct e.studentId, e.clazz.uniqueId from " +
        					"StudentEnrollment e, Class_ c where " + 
        					"e.solution.commited = true and e.solution.owner.uniqueId not in (" + iSolverGroupIds + ") and " +
        					"e.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering = c.schedulingSubpart.instrOfferingConfig.instructionalOffering and " +
        					"c.managingDept.solverGroup.uniqueId in (" + iSolverGroupIds + ")").list();
        			setPhase(MSG.phaseLoadingOtherStudentEnrollments(), enrollments.size());

        			for (Object[] o: enrollments) {
                		Long studentId = (Long)o[0];
                		Long clazzId = (Long)o[1];
                		
                        Student student = (Student)iStudents.get(studentId);
                        if (student==null) continue;
                        
                        Lecture lecture = (Lecture)iLectures.get(clazzId);
                        if (lecture != null && lecture.getConfiguration() != null) {

                    		Set<Lecture> preEnrollments = iPreEnrollments.get(student);
                    		if (preEnrollments == null) {
                    			preEnrollments = new HashSet<Lecture>();
                    			iPreEnrollments.put(student, preEnrollments);
                    		}
                    		preEnrollments.add(lecture);
                    		                    		
                    		if (student.hasOffering(lecture.getConfiguration().getOfferingId()) && student.canEnroll(lecture)) {
                        		student.addLecture(lecture);
                        		lecture.addStudent(getAssignment(), student);
                        	}
                        }

        				incProgress();
        			}
        		}
    		}
    	}
    	
    	if (!hibSession.isOpen())
    		iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());
    	
    	RoomAvailabilityInterface availability = null;
    	if (SolverServerImplementation.getInstance() != null)
    		availability = SolverServerImplementation.getInstance().getRoomAvailability();
    	else
    		availability = RoomAvailability.getInstance();
        if (availability != null) {
        	Date[] startEnd = initializeRoomAvailability(availability);
        	if (startEnd != null) {
        		loadRoomAvailability(availability, startEnd);
        		loadInstructorAvailability(availability, startEnd);
        	}
        }

        if (!hibSession.isOpen())
            iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());

    	setPhase(MSG.phaseInitialSectioning(), iOfferings.size());
        for (InstructionalOffering offering: iOfferings.keySet()) {
    		Set<Student> students = new HashSet<Student>();
			for (CourseOffering course: offering.getCourseOfferings()) {
				Set<Student> courseStudents = iCourse2students.get(course);
				if (courseStudents != null)
					students.addAll(courseStudents);
			}
    		if (students.isEmpty()) continue;
    		
    		getModel().getStudentSectioning().initialSectioning(getAssignment(), offering.getUniqueId(), offering.getCourseName(), students, iAltConfigurations.get(offering));
    		
    		incProgress();
    	}
        
    	for (Enumeration e=iStudents.elements();e.hasMoreElements();) {
    		((Student)e.nextElement()).clearDistanceCache();
    	}
    	
    	if (!iPreEnrollments.isEmpty()) {
        	setPhase(MSG.phaseCheckingLoadedEnrollments(), iPreEnrollments.size());
        	for (Map.Entry<Student, Set<Lecture>> entry: iPreEnrollments.entrySet()) {
        		incProgress();
        		Student student = entry.getKey();
        		Set<Lecture> lectures = entry.getValue();
        		for (Lecture lecture: lectures) {
        			if (!lecture.students().contains(student)) {
        				iProgress.message(msglevel("studentNotEnrolled", Progress.MSGLEVEL_WARN), MSG.warnStudentShouldBeInClass(student.getId(), getClassLabel(lecture)));
        			}
        		}
        		for (Lecture lecture: student.getLectures()) {
        			if (!lectures.contains(lecture)) {
        				Lecture instead = null;
        				if (lecture.sameStudentsLectures() != null) {
            				for (Lecture other: lecture.sameStudentsLectures()) {
            					if (lectures.contains(other)) instead = other;
            				}
        				}
        				if (instead != null)
            				iProgress.message(msglevel("studentEnrolled", Progress.MSGLEVEL_WARN), MSG.warnStudentShouldNotBeInClassShouldBeInOther(student.getId(), getClassLabel(lecture), getClassLabel(instead)));
        				else
            				iProgress.message(msglevel("studentEnrolled", Progress.MSGLEVEL_INFO), MSG.warnStudentShouldNotBeInClass(student.getId(), getClassLabel(lecture)));
        			}
        		}
        	}
    	}
        
    	if (!hibSession.isOpen())
    		iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());

        if (iLoadStudentInstructorConflicts)
        	loadInstructorStudentConflicts(hibSession);

        setPhase(MSG.phaseComputingJenrl(),iStudents.size());
        Hashtable jenrls = new Hashtable();
        for (Iterator i1=iStudents.values().iterator();i1.hasNext();) {
            Student st = (Student)i1.next();
            for (Iterator i2=st.getLectures().iterator();i2.hasNext();) {
                Lecture l1 = (Lecture)i2.next();
                for (Iterator i3=st.getLectures().iterator();i3.hasNext();) {
                    Lecture l2 = (Lecture)i3.next();
                    if (l1.getId()>=l2.getId()) continue;
                    Hashtable x = (Hashtable)jenrls.get(l1);
                    if (x==null) { x = new Hashtable(); jenrls.put(l1, x); }
                    JenrlConstraint jenrl = (JenrlConstraint)x.get(l2);
                    if (jenrl==null) {
                        jenrl = new JenrlConstraint();
                        getModel().addConstraint(jenrl);
                        jenrl.addVariable(l1);
                        jenrl.addVariable(l2);
                        x.put(l2, jenrl);
                    }
                    jenrl.incJenrl(getAssignment(), st);
                }
            }
            incProgress();
        }
        
    	if (!hibSession.isOpen())
    		iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());
    	
    	if (!getModel().getStudentSectioning().hasFinalSectioning())
    		postAutomaticStudentConstraints(hibSession);

    	if (solutions!=null) {
        	for (int idx=0;idx<iSolverGroupId.length;idx++) {
        		Solution solution = (Solution)solutions.get(iSolverGroupId[idx]);
        		if (solution==null) continue;
            	setPhase(MSG.phaseCreatingInitialAssignmentPhase(idx+1),solution.getAssignments().size());
            	for (Iterator i1=solution.getAssignments().iterator();i1.hasNext();) {
            		Assignment assignment = (Assignment)i1.next();
            		loadAssignment(assignment);
            		incProgress();
            	}
        	}
        } else if (iLoadCommittedAssignments) {
        	setPhase(MSG.phaseCreatingCommittedAssignment(), getModel().variables().size());
        	for (Lecture lecture: getModel().variables()) {
        		if (lecture.isCommitted()) continue;
        		Class_ clazz = iClasses.get(lecture.getClassId());
        		if (clazz != null && clazz.getCommittedAssignment() != null)
        			loadAssignment(clazz.getCommittedAssignment());
        		incProgress();
        	}
        }
        
    	if (!hibSession.isOpen())
    		iProgress.message(msglevel("hibernateFailure", Progress.MSGLEVEL_FATAL), MSG.fatalHibernateSessionClosed());

    	if (iSpread) {
    		setPhase(MSG.phasePostingAutoSpreads(), subparts.size());
    		for (SchedulingSubpart subpart: subparts) {
    			if (subpart.getClasses().size()<=1) {
    				incProgress();
    				continue;
    			}
    			if (!subpart.isAutoSpreadInTime().booleanValue()) {
    				iProgress.debug("Automatic spread constraint disabled for "+getSubpartLabel(subpart));
    				incProgress();
    				continue;
    			}
				SpreadConstraint spread = new SpreadConstraint(getModel().getProperties(),subpart.getCourseName()+" "+subpart.getItypeDesc().trim());
				for (Iterator i2=subpart.getClasses().iterator();i2.hasNext();) {
					Class_ clazz = (Class_)i2.next();
					Lecture lecture = (Lecture)getLecture(clazz);
					if (lecture==null) continue;
					spread.addVariable(lecture);
				}
				if (spread.variables().isEmpty())
					iProgress.message(msglevel("courseWithNoClasses", Progress.MSGLEVEL_WARN), MSG.warnCourseWithNoClasses(getSubpartLabel(subpart)));
				else
					getModel().addConstraint(spread);
    			incProgress();
    		}
		}
		
		if (iDeptBalancing) {
        	setPhase(MSG.phasePostingDeptSpreads(),getModel().variables().size());
            Hashtable<Long, DepartmentSpreadConstraint> depSpreadConstraints = new Hashtable<Long, DepartmentSpreadConstraint>();
            for (Lecture lecture: getModel().variables()) {
                if (lecture.getDepartment()==null) continue;
                DepartmentSpreadConstraint deptConstr = (DepartmentSpreadConstraint)depSpreadConstraints.get(lecture.getDepartment());
                if (deptConstr==null) {
                    deptConstr = new DepartmentSpreadConstraint(getModel().getProperties(),lecture.getDepartment(),(String)iDeptNames.get(lecture.getDepartment()));
                    depSpreadConstraints.put(lecture.getDepartment(),deptConstr);
                    getModel().addConstraint(deptConstr);
                }
                deptConstr.addVariable(lecture);
                incProgress();
            }
        }
		
		if (iSubjectBalancing) {
        	setPhase(MSG.phasePostingSubjectSpreads(),getModel().variables().size());
            Hashtable<Long, SpreadConstraint> subjectSpreadConstraints = new Hashtable<Long, SpreadConstraint>();
            for (Lecture lecture: getModel().variables()) {
            	Class_ clazz = iClasses.get(lecture.getClassId());
            	if (clazz == null) continue;
            	for (CourseOffering co: clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
            		Long subject = co.getSubjectArea().getUniqueId();
            		SpreadConstraint subjectSpreadConstr = subjectSpreadConstraints.get(subject);
                    if (subjectSpreadConstr==null) {
                        subjectSpreadConstr = new SpreadConstraint(getModel().getProperties(), co.getSubjectArea().getSubjectAreaAbbreviation());
                        subjectSpreadConstraints.put(subject, subjectSpreadConstr);
                        getModel().addConstraint(subjectSpreadConstr);
                    }
                    subjectSpreadConstr.addVariable(lecture);
            	}
                incProgress();
            }
        }

		if (getModel().getProperties().getPropertyBoolean("General.PurgeInvalidPlacements", true))
			purgeInvalidValues();
		
		/*
		for (Constraint c: getModel().constraints()) {
			if (c instanceof SpreadConstraint)
				((SpreadConstraint)c).init();
			if (c instanceof DiscouragedRoomConstraint)
				((DiscouragedRoomConstraint)c).setEnabled(true);
			if (c instanceof MinimizeNumberOfUsedRoomsConstraint)
				((MinimizeNumberOfUsedRoomsConstraint)c).setEnabled(true);
			if (c instanceof MinimizeNumberOfUsedGroupsOfTime)
				((MinimizeNumberOfUsedGroupsOfTime)c).setEnabled(true);
		}
		*/
		
		setPhase(MSG.phaseCheckingForInconsistencies(), getModel().variables().size());
		for (Lecture lecture: getModel().variables()) {
			
			incProgress();
    		for (Iterator i=lecture.students().iterator();i.hasNext();) {
    			Student s = (Student)i.next();
    			if (!s.canEnroll(lecture))
    				iProgress.message(msglevel("badStudentEnrollment", Progress.MSGLEVEL_INFO), MSG.warnBadStudentEnrollment(s.getId(), getClassLabel(lecture)));
    		}
    		
    		//check same instructor constraint
    		if (!lecture.values(getAssignment()).isEmpty() && lecture.timeLocations().size()==1 && !lecture.getInstructorConstraints().isEmpty()) {
        		for (Lecture other: getModel().variables()) {
        			if (other.values(getAssignment()).isEmpty() || other.timeLocations().size()!=1 || lecture.getClassId().compareTo(other.getClassId())<=0) continue;
        			Placement p1 = lecture.values(getAssignment()).get(0);
        			Placement p2 = other.values(getAssignment()).get(0);
        			if (!other.getInstructorConstraints().isEmpty()) {
        	           	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
        	           		if (!other.getInstructorConstraints().contains(ic)) continue;
        	           		if (p1.canShareRooms(p2) && p1.sameRooms(p2)) continue;
        	           		if (p1.getTimeLocation().hasIntersection(p2.getTimeLocation())) {
        	           			iProgress.message(msglevel("reqInstructorOverlap", Progress.MSGLEVEL_WARN), MSG.warnSameInstructorTimeConflict(
        	           					getClassLabel(lecture), p1.getLongName(iUseAmPm), getClassLabel(other), p2.getLongName(iUseAmPm)));
        	           		} else if (ic.getDistancePreference(p1,p2)==PreferenceLevel.sIntLevelProhibited && lecture.roomLocations().size()==1 && other.roomLocations().size()==1) {
        	           			iProgress.message(msglevel("reqInstructorBackToBack", Progress.MSGLEVEL_WARN), MSG.warnSameInstructorBackToBack(
        	           					Math.round(10.0*Placement.getDistanceInMeters(getModel().getDistanceMetric(),p1,p2)), getClassLabel(lecture), p1.getLongName(iUseAmPm), getClassLabel(other), p2.getLongName(iUseAmPm)));
        	           		}
        				}
        			}
        		}    			
    		}
    		
			if (!lecture.isSingleton()) continue;
    		for (Lecture other: getModel().variables()) {
    			if (!other.isSingleton() || lecture.getClassId().compareTo(other.getClassId())<=0) continue;
    			Placement p1 = new Placement(lecture, lecture.timeLocations().get(0), lecture.roomLocations());
    			Placement p2 = new Placement(other, other.timeLocations().get(0), other.roomLocations());
    			if (p1.shareRooms(p2) && p1.getTimeLocation().hasIntersection(p2.getTimeLocation()) && !p1.canShareRooms(p2)) {
    				iProgress.message(msglevel("reqRoomOverlap", Progress.MSGLEVEL_WARN), MSG.warnSameRoomTimeConflict(getClassLabel(lecture), p1.getLongName(iUseAmPm), getClassLabel(other), p2.getLongName(iUseAmPm)));
    			}
    		}
    		if (getAssignment().getValue(lecture)==null) {
    			Placement placement = new Placement(lecture, lecture.timeLocations().get(0), lecture.roomLocations());
    			if (!placement.isValid()) {
    				String reason = "";
    	           	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
    	           		if (!ic.isAvailable(lecture, placement))
    	           			reason += MSG.warnReasonInstructorNotAvailable(ic.getName());
    	           	}
    		    	if (lecture.getNrRooms()>0) {
    		    		if (placement.isMultiRoom()) {
    		    			for (RoomLocation roomLocation: placement.getRoomLocations()) {
    		    				if (!roomLocation.getRoomConstraint().isAvailable(lecture,placement.getTimeLocation(),lecture.getScheduler()))
    		    					reason += MSG.warnReasonRoomNotAvailable(roomLocation.getName());
    		    			}
    		    		} else {
    						if (!placement.getRoomLocation().getRoomConstraint().isAvailable(lecture,placement.getTimeLocation(),lecture.getScheduler()))
    							reason += MSG.warnReasonRoomNotAvailable(placement.getRoomLocation().getName());
    		    		}
    		    	}
    		    	Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = getModel().conflictConstraints(getAssignment(), placement);
    	            if (!conflictConstraints.isEmpty()) {
    	                for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
    	                	Set<Placement> vals = conflictConstraints.get(c);
    	                    for (Placement p: vals) {
    	                        Lecture l = p.variable();
    	                        if (l.isCommitted())
    	                        	reason += MSG.warnReasonConstraintCommitedAssignment(getClassLabel(l), p.getLongName(iUseAmPm), TimetableSolver.getConstraintName(c));
    	                        if (p.equals(placement))
    	                        	reason += MSG.warnReasonConstraint(TimetableSolver.getConstraintName(c));
    	                    }
    	                }
    	            }
    		    	iProgress.message(msglevel("reqInvalidPlacement", Progress.MSGLEVEL_WARN),
    		    			reason.isEmpty() ? MSG.warnRequiresInvalidPlacement(getClassLabel(lecture), placement.getLongName(iUseAmPm)) :
    		    			MSG.warnRequiresInvalidPlacementWithReason(getClassLabel(lecture), placement.getLongName(iUseAmPm), reason));
    			} else if (iAssignSingleton && getModel().conflictValues(getAssignment(), placement).isEmpty())
    				getAssignment().assign(0, placement);
    		}
		}
		
		getModel().createAssignmentContexts(getAssignment(), true);

		if (getModel().getProperties().getPropertyBoolean("General.EnrollmentCheck", true))
			new EnrollmentCheck(getModel(), getAssignment(), msglevel("enrollmentCheck", Progress.MSGLEVEL_WARN)).checkStudentEnrollments(iProgress);
		
		if (getModel().getProperties().getPropertyBoolean("General.SwitchStudents",true) && getAssignment().nrAssignedVariables() != 0 && !iLoadStudentEnrlsFromSolution)
			getModel().switchStudents(getAssignment(), getTerminationCondition());
		
 		setPhase(MSG.phaseDone(),1);incProgress();            
		iProgress.message(msglevel("allDone", Progress.MSGLEVEL_INFO), MSG.infoModelLoaded());
    }
    
    public static class ObjectsByGivenOrderComparator implements Comparator {
    	List<?> iOrderedSet = null;
    	public ObjectsByGivenOrderComparator(List<?> orderedSetOfLectures) {
    		iOrderedSet = orderedSetOfLectures;
    	}
    	public int compare(Object o1, Object o2) {
    		int idx1 = iOrderedSet.indexOf(o1);
    		int idx2 = iOrderedSet.indexOf(o2);
    		int cmp = Double.compare(idx1, idx2);
    		if (cmp!=0) return cmp;
    		return ((Comparable)o1).compareTo(o2);
    	}
    }
    
    public void roomAvailabilityActivate(RoomAvailabilityInterface availability, Date startTime, Date endTime) {
        try {
        	availability.activate(new SessionDAO().get(iSessionId), startTime, endTime, RoomAvailabilityInterface.sClassType, ApplicationProperty.RoomAvailabilitySolverWaitForSync.isTrue());
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.message(msglevel("roomAvailabilityFailure", Progress.MSGLEVEL_WARN), MSG.warnRoomAvailableServiceFailed(e.getMessage()));
        } 
    }
    
    public Date[] initializeRoomAvailability(RoomAvailabilityInterface availability) {
    	Date startDate = null, endDate = null;
        for (Iterator i=iAllUsedDatePatterns.iterator();i.hasNext();) {
            DatePattern dp = (DatePattern)i.next();
            if (startDate == null || startDate.compareTo(dp.getStartDate())>0)
                startDate = dp.getStartDate();
            if (endDate == null || endDate.compareTo(dp.getEndDate())<0)
                endDate = dp.getEndDate();
        }
        if (startDate == null || endDate == null) {
        	iProgress.message(msglevel("roomAvailabilityFailure", Progress.MSGLEVEL_WARN), MSG.warnRoomAvailableServiceNoDates());
        	return null;
        }
        Calendar startDateCal = Calendar.getInstance(Locale.US);
        startDateCal.setTime(startDate);
        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
        startDateCal.set(Calendar.MINUTE, 0);
        startDateCal.set(Calendar.SECOND, 0);
        Calendar endDateCal = Calendar.getInstance(Locale.US);
        endDateCal.setTime(endDate);
        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
        endDateCal.set(Calendar.MINUTE, 59);
        endDateCal.set(Calendar.SECOND, 59);
        roomAvailabilityActivate(availability, startDateCal.getTime(),endDateCal.getTime());
        return new Date[] {startDateCal.getTime(), endDateCal.getTime()};
    }
    
    public void loadRoomAvailability(RoomAvailabilityInterface availability, Date[] startEnd) {
        setPhase(MSG.phaseLoadingRoomAvailability(), iRooms.size());
        int firstDOY = iSession.getDayOfYear(1,iSession.getPatternStartMonth());
        int lastDOY = iSession.getDayOfYear(0,iSession.getPatternEndMonth()+1);
        int size = lastDOY - firstDOY;
        Calendar c = Calendar.getInstance(Locale.US);
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
        int sessionYear = iSession.getSessionStartYear();
        for (Enumeration e=iRooms.elements();e.hasMoreElements();) {
            RoomConstraint room = (RoomConstraint)e.nextElement();
            incProgress();
            if (!room.getConstraint()) continue;
            Collection<TimeBlock> times = getRoomAvailability(availability, room, startEnd[0], startEnd[1]);
            if (times==null) continue;
            for (TimeBlock time : times) {
                iProgress.debug(room.getName()+" not available due to "+time);
                int dayCode = 0;
                c.setTime(time.getStartTime());
                int m = c.get(Calendar.MONTH);
                int d = c.get(Calendar.DAY_OF_MONTH);
                if (c.get(Calendar.YEAR)<sessionYear) m-=(12 * (sessionYear - c.get(Calendar.YEAR)));
                if (c.get(Calendar.YEAR)>sessionYear) m+=(12 * (c.get(Calendar.YEAR) - sessionYear));
                BitSet weekCode = new BitSet(size);
                int offset = iSession.getDayOfYear(d,m) - firstDOY;
                if (offset < 0 || offset >= size) continue;
                weekCode.set(offset);
                switch (c.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_MON]; break;
                    case Calendar.TUESDAY   : dayCode = Constants.DAY_CODES[Constants.DAY_TUE]; break;
                    case Calendar.WEDNESDAY : dayCode = Constants.DAY_CODES[Constants.DAY_WED]; break;
                    case Calendar.THURSDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_THU]; break;
                    case Calendar.FRIDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_FRI]; break;
                    case Calendar.SATURDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_SAT]; break;
                    case Calendar.SUNDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_SUN]; break;
                }
                int startSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                c.setTime(time.getEndTime());
                int endSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                if (endSlot == 0 && c.get(Calendar.DAY_OF_MONTH) != d) endSlot = 288; // next day midnight
                int length = endSlot - startSlot;
                if (length<=0) continue;
                TimeLocation timeLocation = new TimeLocation(dayCode, startSlot, length, 0, 0, null, df.format(time.getStartTime()), weekCode, 0);
                List<TimeLocation> timeLocations = new ArrayList<TimeLocation>(1); timeLocations.add(timeLocation);
                RoomLocation roomLocation = new RoomLocation(room.getResourceId(), room.getName(), room.getBuildingId(), 0, room.getCapacity(), room.getPosX(), room.getPosY(),
                        room.getIgnoreTooFar(), room);
                List<RoomLocation> roomLocations = new ArrayList<RoomLocation>(1); roomLocations.add(roomLocation);
                Lecture lecture = new Lecture(
                        new Long(--iFakeLectureId), null, null, time.getEventName(), 
                        timeLocations, roomLocations, 1, 
                        new Placement(null,timeLocation,roomLocations), 0, 0, 1.0);
                lecture.setNote(time.getEventType());
                Placement p = (Placement)lecture.getInitialAssignment();
                lecture.setBestAssignment(p, 0);
                lecture.setCommitted(true);
                room.setNotAvailable(p);
                getModel().addVariable(p.variable());
            }
        }
    }
    
    public Collection<TimeBlock> getRoomAvailability(RoomAvailabilityInterface availability, RoomConstraint room, Date startTime, Date endTime) {
        Collection<TimeBlock> ret = null;
        String ts = null;
        try {
            ret = availability.getRoomAvailability(room.getResourceId(), startTime, endTime, RoomAvailabilityInterface.sClassType);
            if (!iRoomAvailabilityTimeStampIsSet) ts = availability.getTimeStamp(startTime, endTime, RoomAvailabilityInterface.sClassType);
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.message(msglevel("roomAvailabilityFailure", Progress.MSGLEVEL_WARN), MSG.warnRoomAvailableServiceFailed(e.getMessage()));
        } 
        if (!iRoomAvailabilityTimeStampIsSet) {
            iRoomAvailabilityTimeStampIsSet = true;
            if (ts!=null) {
                getModel().getProperties().setProperty("RoomAvailability.TimeStamp", ts);
                iProgress.message(msglevel("roomAvailabilityUpdated", Progress.MSGLEVEL_INFO), MSG.infoUsingRoomAvailability(ts));
            } else {
                iProgress.message(msglevel("roomAvailabilityFailure", Progress.MSGLEVEL_ERROR), MSG.warnRoomAvailableServiceNotAvailable());
            }
        }
        return ret;
    }
    
    public void loadInstructorAvailability(RoomAvailabilityInterface availability, Date[] startEnd) {
        setPhase(MSG.phaseLoadingInstructorAvailability(), getModel().getInstructorConstraints().size());
        int firstDOY = iSession.getDayOfYear(1,iSession.getPatternStartMonth());
        int lastDOY = iSession.getDayOfYear(0,iSession.getPatternEndMonth()+1);
        int size = lastDOY - firstDOY;
        Calendar c = Calendar.getInstance(Locale.US);
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
        int sessionYear = iSession.getSessionStartYear();
        for (InstructorConstraint instructor: getModel().getInstructorConstraints()) {
            incProgress();
            Collection<TimeBlock> times = getInstructorAvailability(availability, instructor, startEnd[0], startEnd[1]);
            if (times==null) continue;
            for (TimeBlock time : times) {
                iProgress.debug(instructor.getName() + " not available due to " + time);
                int dayCode = 0;
                c.setTime(time.getStartTime());
                int m = c.get(Calendar.MONTH);
                int d = c.get(Calendar.DAY_OF_MONTH);
                if (c.get(Calendar.YEAR)<sessionYear) m-=(12 * (sessionYear - c.get(Calendar.YEAR)));
                if (c.get(Calendar.YEAR)>sessionYear) m+=(12 * (c.get(Calendar.YEAR) - sessionYear));
                BitSet weekCode = new BitSet(size);
                int offset = iSession.getDayOfYear(d,m) - firstDOY;
                if (offset < 0 || offset >= size) continue;
                weekCode.set(offset);
                switch (c.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.MONDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_MON]; break;
                    case Calendar.TUESDAY   : dayCode = Constants.DAY_CODES[Constants.DAY_TUE]; break;
                    case Calendar.WEDNESDAY : dayCode = Constants.DAY_CODES[Constants.DAY_WED]; break;
                    case Calendar.THURSDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_THU]; break;
                    case Calendar.FRIDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_FRI]; break;
                    case Calendar.SATURDAY  : dayCode = Constants.DAY_CODES[Constants.DAY_SAT]; break;
                    case Calendar.SUNDAY    : dayCode = Constants.DAY_CODES[Constants.DAY_SUN]; break;
                }
                int startSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                c.setTime(time.getEndTime());
                int endSlot = (c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE) - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
                if (endSlot == 0 && c.get(Calendar.DAY_OF_MONTH) != d) endSlot = 288; // next day midnight
                int length = endSlot - startSlot;
                if (length<=0) continue;
                TimeLocation timeLocation = new TimeLocation(dayCode, startSlot, length, 0, 0, null, df.format(time.getStartTime()), weekCode, 0);
                List<TimeLocation> timeLocations = new ArrayList<TimeLocation>(1); timeLocations.add(timeLocation);
                Lecture lecture = new Lecture(
                        new Long(--iFakeLectureId), null, null, time.getEventName(), 
                        timeLocations, new ArrayList<RoomLocation>(), 0, 
                        new Placement(null,timeLocation,(RoomLocation)null), 0, 0, 1.0);
                lecture.setNote(time.getEventType());
                Placement p = (Placement)lecture.getInitialAssignment();
                lecture.setBestAssignment(p, 0);
                lecture.setCommitted(true);
                instructor.setNotAvailable(p);
                getModel().addVariable(p.variable());
            }
        }
    }
    
    public Collection<TimeBlock> getInstructorAvailability(RoomAvailabilityInterface availability, InstructorConstraint instructor, Date startTime, Date endTime) {
        Collection<TimeBlock> ret = null;
        String ts = null;
        try {
            ret = availability.getInstructorAvailability(instructor.getResourceId(), startTime, endTime, RoomAvailabilityInterface.sClassType);
            if (!iRoomAvailabilityTimeStampIsSet) ts = availability.getTimeStamp(startTime, endTime, RoomAvailabilityInterface.sClassType);
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.message(msglevel("roomAvailabilityFailure", Progress.MSGLEVEL_WARN), MSG.warnRoomAvailableServiceFailed(e.getMessage()));
        } 
        if (!iRoomAvailabilityTimeStampIsSet) {
            iRoomAvailabilityTimeStampIsSet = true;
            if (ts!=null) {
                getModel().getProperties().setProperty("RoomAvailability.TimeStamp", ts);
                iProgress.message(msglevel("roomAvailabilityUpdated", Progress.MSGLEVEL_INFO), MSG.infoUsingRoomAvailability(ts));
            } else {
                iProgress.message(msglevel("roomAvailabilityFailure", Progress.MSGLEVEL_ERROR), MSG.warnRoomAvailableServiceNotAvailable());
            }
        }
        return ret;
    }
    
    protected void postAutomaticHierarchicalConstraints(org.hibernate.Session hibSession) {
		String constraints = getModel().getProperties().getProperty("General.AutomaticHierarchicalConstraints");
		if (constraints == null || constraints.isEmpty()) return;
		List<DistributionType> types = (List<DistributionType>)hibSession.createQuery("from DistributionType where examPref = false").list();
		List<DatePattern> patterns = (List<DatePattern>)hibSession.createQuery("from DatePattern where session.uniqueId = :sessionId").setLong("sessionId", iSessionId).list();
		for (String term: constraints.split("[,;][ ]?(?=([^\"]*\"[^\"]*\")*[^\"]*$)")) {
			String constraint = term.trim().toLowerCase();
			if (constraint.isEmpty()) continue;
			PreferenceLevel pref = null;
			for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
				if (constraint.startsWith(p.getPrefName().toLowerCase() + " ") || constraint.startsWith(p.getPrefName().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefName().length() + 1).trim();
					break;
				} else if (constraint.startsWith(p.getPrefProlog().toLowerCase() + " ") || constraint.startsWith(p.getPrefProlog().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefProlog().length() + 1).trim();
					break;
				} else if (p.getPrefAbbv() != null && (constraint.startsWith(p.getPrefAbbv().toLowerCase() + " ") || constraint.startsWith(p.getPrefAbbv().toLowerCase() + ":"))) {
					pref = p;
					constraint = constraint.substring(p.getPrefAbbv().length() + 1).trim();
					break;
				} else if (PreferenceLevel.sRequired.equals(p.getPrefProlog()) && (constraint.startsWith("required ") || constraint.startsWith("required:"))) {
					pref = p; constraint = constraint.substring("required ".length()).trim(); break;
				} else if (PreferenceLevel.sStronglyPreferred.equals(p.getPrefProlog()) && (constraint.startsWith("strongly preferred ") || constraint.startsWith("strongly preferred:"))) {
					pref = p; constraint = constraint.substring("strongly preferred ".length()).trim(); break;
				} else if (PreferenceLevel.sPreferred.equals(p.getPrefProlog()) && (constraint.startsWith("preferred ") || constraint.startsWith("preferred:"))) {
					pref = p; constraint = constraint.substring("preferred ".length()).trim(); break;
				} else if (PreferenceLevel.sNeutral.equals(p.getPrefProlog()) && (constraint.startsWith("neutral ") || constraint.startsWith("neutral:"))) {
					pref = p; constraint = constraint.substring("neutral ".length()).trim(); break;
				} else if (PreferenceLevel.sDiscouraged.equals(p.getPrefProlog()) && (constraint.startsWith("discouraged ") || constraint.startsWith("discouraged:"))) {
					pref = p; constraint = constraint.substring("discouraged ".length()).trim(); break;
				} else if (PreferenceLevel.sStronglyDiscouraged.equals(p.getPrefProlog()) && (constraint.startsWith("strongly discouraged ") || constraint.startsWith("strongly discouraged:"))) {
					pref = p; constraint = constraint.substring("strongly discouraged ".length()).trim(); break;
				} else if (PreferenceLevel.sProhibited.equals(p.getPrefProlog()) && (constraint.startsWith("prohibited ") || constraint.startsWith("prohibited:"))) {
					pref = p; constraint = constraint.substring("prohibited ".length()).trim(); break;
				}
			}
			if (pref == null) {
				iProgress.message(msglevel("automaticHierarchicalConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticHierarchicalConstraint(term));
				continue;
			}
			DistributionType type = null;
			for (DistributionType t: types) {
				if (constraint.equalsIgnoreCase(t.getReference()) || constraint.equalsIgnoreCase(t.getAbbreviation()) || constraint.equalsIgnoreCase(t.getLabel())) {
					type = t; constraint = ""; break;
				} else if (constraint.startsWith(t.getReference().toLowerCase() + " ") || constraint.startsWith(t.getReference().toLowerCase() + ":")) {
					type = t; constraint = constraint.substring(t.getReference().length() + 1).trim(); break;
				} else if (constraint.startsWith(t.getAbbreviation().toLowerCase() + " ") || constraint.startsWith(t.getAbbreviation().toLowerCase() + ":")) {
					type = t; constraint = constraint.substring(t.getAbbreviation().length() + 1).trim(); break;
				} else if (constraint.startsWith(t.getLabel().toLowerCase() + " ") || constraint.startsWith(t.getLabel().toLowerCase() + ":")) {
					type = t; constraint = constraint.substring(t.getLabel().length() + 1).trim(); break;
				}
			}
			if (type == null) {
				for (GroupConstraint.ConstraintType t: GroupConstraint.ConstraintType.values()) {
					if (constraint.equalsIgnoreCase(t.reference()) || constraint.equalsIgnoreCase(t.getName())) {
						type = new DistributionType(); type.setReference(t.reference()); type.setLabel(t.getName()); type.setAbbreviation(t.getName());
						constraint = ""; break;
					} else if (constraint.startsWith(t.reference().toLowerCase() + " ") || constraint.startsWith(t.reference().toLowerCase() + ":")) {
						type = new DistributionType(); type.setReference(t.reference()); type.setLabel(t.getName()); type.setAbbreviation(t.getName());
						constraint = constraint.substring(t.reference().length() + 1).trim(); break;
					} else if (constraint.startsWith(t.getName().toLowerCase() + " ") || constraint.startsWith(t.getName().toLowerCase() + ":")) {
						type = new DistributionType(); type.setReference(t.reference()); type.setLabel(t.getName()); type.setAbbreviation(t.getName());
						constraint = constraint.substring(t.getName().length() + 1).trim(); break;
					}
				}
			}
			if (type == null) {
				iProgress.message(msglevel("automaticHierarchicalConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticHierarchicalConstraint(term));
				continue;
			}
			DatePattern pattern = null;
			if (!constraint.isEmpty()) {
				for (DatePattern p: patterns) {
					if (constraint.equalsIgnoreCase(p.getName()) || constraint.equalsIgnoreCase("\"" + p.getName() + "\"")) {
						pattern = p; break;
					}
				}
				if (pattern == null) {
					iProgress.message(msglevel("automaticHierarchicalConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticHierarchicalConstraintBadDatePattern(constraint));
					continue;
				}
			}
			setPhase(pattern == null ? MSG.phasePostingAutomaticConstraint(pref.getPrefName(), type.getLabel()) : MSG.phasePostingAutomaticConstraintDatePattern(pref.getPrefName(), type.getLabel(), pattern.getName()), iAllClasses.size());
			for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
    			Class_ clazz = (Class_)i1.next();
    			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    			if (lecture==null) continue;
    			
    			if (!lecture.hasAnyChildren())
    				postAutomaticHierarchicalConstraint(clazz, type, pref, pattern);
    			
    			incProgress();
    		}			
		}
    }
    
    protected boolean postAutomaticHierarchicalConstraint(Class_ clazz, DistributionType type, PreferenceLevel preference, DatePattern pattern) {
    	boolean posted = false;
    	if (!clazz.getChildClasses().isEmpty()) {
    		for (Iterator i=clazz.getChildClasses().iterator();i.hasNext();) {
    			Class_ c = (Class_)i.next();
    			if (postAutomaticHierarchicalConstraint(c, type, preference, pattern))
    				posted = true;
    		}
    	}
    	
    	if (posted) return true;
    	
		if (getLecture(clazz) == null) return false;
		if (pattern != null && !pattern.equals(clazz.effectiveDatePattern())) return false;
		
		List<Lecture> variables = new ArrayList<Lecture>();

		Class_ parent = clazz;
		while (parent != null) {
			if (pattern == null || pattern.equals(parent.effectiveDatePattern())) {
				Lecture lecture = getLecture(parent);
				if (lecture != null)
					variables.add(0, lecture);
			}
			parent = parent.getParentClass();
		}

    	if (variables.size() <= 1) return false;
    	Constraint gc = createGroupConstraint(clazz.getUniqueId(), type, preference, clazz);
    	if (gc == null) return false;
    	String info = "";
		for (Lecture var: variables) {
			gc.addVariable(var);
			if (!info.isEmpty()) info += ", ";
			info += getClassLabel(var);
		}
		iProgress.info(MSG.infoPostedConstraint(type.getLabel(), info, preference.getPrefName()));
    	addGroupConstraint(gc);
    	
		return true;
    }
    
    protected void postAutomaticStudentConstraints(org.hibernate.Session hibSession) {
		String constraints = getModel().getProperties().getProperty("General.AutomaticStudentConstraints");
		if (constraints == null || constraints.isEmpty()) return;
		Map<String, Integer> classes2counts = new HashMap<String, Integer>();
		Map<String, Student> firstStudent = new HashMap<String, Student>();
		for (Iterator<Student> i1 = getModel().getAllStudents().iterator(); i1.hasNext(); ) {
			Student student = i1.next();
			Set<Long> idSet = new TreeSet<Long>();
			for (Lecture lecture: student.getLectures())
				idSet.add(lecture.getClassId());
			String ids = "";
			for (Long id: idSet)
				ids += (ids.isEmpty() ? "" : ",") + id;
			Integer count = classes2counts.get(ids);
			classes2counts.put(ids, 1 + (count == null ? 0 : count.intValue()));
			if (count == null)
				firstStudent.put(ids, student);
		}
		int limit = getModel().getProperties().getPropertyInt("General.AutomaticStudentConstraints.StudentLimit", 5);

		List<DistributionType> types = (List<DistributionType>)hibSession.createQuery("from DistributionType where examPref = false").list();
		for (String term: constraints.split("[,;][ ]?(?=([^\"]*\"[^\"]*\")*[^\"]*$)")) {
			String constraint = term.trim().toLowerCase();
			if (constraint.isEmpty()) continue;
			PreferenceLevel pref = null;
			for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
				if (constraint.startsWith(p.getPrefName().toLowerCase() + " ") || constraint.startsWith(p.getPrefName().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefName().length() + 1).trim();
					break;
				} else if (constraint.startsWith(p.getPrefProlog().toLowerCase() + " ") || constraint.startsWith(p.getPrefProlog().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefProlog().length() + 1).trim();
					break;
				} else if (p.getPrefAbbv() != null && constraint.startsWith(p.getPrefAbbv().toLowerCase() + " ") || constraint.startsWith(p.getPrefAbbv().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefAbbv().length() + 1).trim();
					break;
				} else if (PreferenceLevel.sRequired.equals(p.getPrefProlog()) && (constraint.startsWith("required ") || constraint.startsWith("required:"))) {
					pref = p; constraint = constraint.substring("required ".length()).trim(); break;
				} else if (PreferenceLevel.sStronglyPreferred.equals(p.getPrefProlog()) && (constraint.startsWith("strongly preferred ") || constraint.startsWith("strongly preferred:"))) {
					pref = p; constraint = constraint.substring("strongly preferred ".length()).trim(); break;
				} else if (PreferenceLevel.sPreferred.equals(p.getPrefProlog()) && (constraint.startsWith("preferred ") || constraint.startsWith("preferred:"))) {
					pref = p; constraint = constraint.substring("preferred ".length()).trim(); break;
				} else if (PreferenceLevel.sNeutral.equals(p.getPrefProlog()) && (constraint.startsWith("neutral ") || constraint.startsWith("neutral:"))) {
					pref = p; constraint = constraint.substring("neutral ".length()).trim(); break;
				} else if (PreferenceLevel.sDiscouraged.equals(p.getPrefProlog()) && (constraint.startsWith("discouraged ") || constraint.startsWith("discouraged:"))) {
					pref = p; constraint = constraint.substring("discouraged ".length()).trim(); break;
				} else if (PreferenceLevel.sStronglyDiscouraged.equals(p.getPrefProlog()) && (constraint.startsWith("strongly discouraged ") || constraint.startsWith("strongly discouraged:"))) {
					pref = p; constraint = constraint.substring("strongly discouraged ".length()).trim(); break;
				} else if (PreferenceLevel.sProhibited.equals(p.getPrefProlog()) && (constraint.startsWith("prohibited ") || constraint.startsWith("prohibited:"))) {
					pref = p; constraint = constraint.substring("prohibited ".length()).trim(); break;
				}
			}
			if (pref == null) {
				iProgress.message(msglevel("automaticStudentConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticStudentConstraint(term));
				continue;
			}
			DistributionType type = null;
			for (DistributionType t: types) {
				if (constraint.equalsIgnoreCase(t.getReference()) || constraint.equalsIgnoreCase(t.getAbbreviation()) || constraint.equalsIgnoreCase(t.getLabel())) {
					type = t; constraint = ""; break;
				} else if (constraint.startsWith(t.getReference().toLowerCase() + " ") || constraint.startsWith(t.getReference().toLowerCase() + ":")) {
					type = t; constraint = constraint.substring(t.getReference().length() + 1).trim(); break;
				} else if (constraint.startsWith(t.getAbbreviation().toLowerCase() + " ") || constraint.startsWith(t.getAbbreviation().toLowerCase() + ":")) {
					type = t; constraint = constraint.substring(t.getAbbreviation().length() + 1).trim(); break;
				} else if (constraint.startsWith(t.getLabel().toLowerCase() + " ") || constraint.startsWith(t.getLabel().toLowerCase() + ":")) {
					type = t; constraint = constraint.substring(t.getLabel().length() + 1).trim(); break;
				}
			}
			if (type == null) {
				for (GroupConstraint.ConstraintType t: GroupConstraint.ConstraintType.values()) {
					if (constraint.equalsIgnoreCase(t.reference()) || constraint.equalsIgnoreCase(t.getName())) {
						type = new DistributionType(); type.setReference(t.reference()); type.setLabel(t.getName()); type.setAbbreviation(t.getName());
					}
				}
			}
			if (type == null) {
				iProgress.message(msglevel("automaticStudentConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticStudentConstraint(term));
				continue;
			}
			setPhase(MSG.phasePostingAutomaticStudentConstraints(pref.getPrefName(), type.getLabel()), classes2counts.size());
			for (Map.Entry<String, Integer> entry: classes2counts.entrySet()) {
				incProgress();
				if (entry.getValue() >= limit) {
					List<Lecture> variables = new ArrayList<Lecture>();
					for (String id: entry.getKey().split(",")) {
						Lecture lecture = (Lecture)iLectures.get(Long.valueOf(id));
						if (lecture != null) variables.add(lecture);
					}
					Student student = firstStudent.get(entry.getKey());
					if (variables.size() > 1) {
				    	Constraint gc = createGroupConstraint(student.getId(), type, pref, student);
				    	if (gc == null) continue;
				    	String info = "";
						for (Lecture var: variables) {
							gc.addVariable(var);
							if (!info.isEmpty()) info += ", ";
							info += getClassLabel(var);
						}
						iProgress.info(MSG.infoPostedConstraint(type.getLabel(), info, pref.getPrefName()));
				    	addGroupConstraint(gc);
					}
				}
			}
		}
    }
    
    protected void checkTermination() {
    	if (getTerminationCondition() != null && !getTerminationCondition().canContinue(new org.cpsolver.ifs.solution.Solution(getModel(), getAssignment())))
    		throw new RuntimeException(MSG.fatalLoadInterrupted());
    }
    
    protected void setPhase(String phase, long progressMax) {
    	checkTermination();
    	iProgress.setPhase(phase, progressMax);
    }
    
    protected void incProgress() {
    	checkTermination();
    	iProgress.incProgress();
    }
    
    protected boolean isSameType(DistributionType t1, DistributionType t2) {
    	if (t1.getReference().equals(t2.getReference())) return true;
    	if (t1.getReference().matches("_(.+)_")) {
    		for (FlexibleConstraintType fcType: FlexibleConstraintType.values()) {
    			if (t1.getReference().matches(fcType.getPattern()) && t2.getReference().matches(fcType.getPattern())) return true;
    		}
    		return false;
    	}
    	return t1.getReference().replaceFirst("\\([a-z0-9\\.]+\\)","").equals(t2.getReference().replaceFirst("\\([a-z0-9\\.]+\\)",""));
    }
    
    protected void postAutomaticInstructorConstraints(org.hibernate.Session hibSession) {
		String constraints = getModel().getProperties().getProperty("General.AutomaticInstructorConstraints");
		if (constraints == null || constraints.isEmpty()) return;
		List<DistributionType> types = (List<DistributionType>)hibSession.createQuery("from DistributionType where examPref = false").list();
		for (String term: constraints.split("[,;][ ]?(?=([^\"]*\"[^\"]*\")*[^\"]*$)")) {
			String constraint = term.trim().toLowerCase();
			if (constraint.isEmpty()) continue;
			PreferenceLevel pref = null;
			for (PreferenceLevel p: PreferenceLevel.getPreferenceLevelList()) {
				if (constraint.startsWith(p.getPrefName().toLowerCase() + " ") || constraint.startsWith(p.getPrefName().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefName().length() + 1).trim();
					break;
				} else if (constraint.startsWith(p.getPrefProlog().toLowerCase() + " ") || constraint.startsWith(p.getPrefProlog().toLowerCase() + ":")) {
					pref = p;
					constraint = constraint.substring(p.getPrefProlog().length() + 1).trim();
					break;
				} else if (p.getPrefAbbv() != null && (constraint.startsWith(p.getPrefAbbv().toLowerCase() + " ") || constraint.startsWith(p.getPrefAbbv().toLowerCase() + ":"))) {
					pref = p;
					constraint = constraint.substring(p.getPrefAbbv().length() + 1).trim();
					break;
				} else if (PreferenceLevel.sRequired.equals(p.getPrefProlog()) && (constraint.startsWith("required ") || constraint.startsWith("required:"))) {
					pref = p; constraint = constraint.substring("required ".length()).trim(); break;
				} else if (PreferenceLevel.sStronglyPreferred.equals(p.getPrefProlog()) && (constraint.startsWith("strongly preferred ") || constraint.startsWith("strongly preferred:"))) {
					pref = p; constraint = constraint.substring("strongly preferred ".length()).trim(); break;
				} else if (PreferenceLevel.sPreferred.equals(p.getPrefProlog()) && (constraint.startsWith("preferred ") || constraint.startsWith("preferred:"))) {
					pref = p; constraint = constraint.substring("preferred ".length()).trim(); break;
				} else if (PreferenceLevel.sNeutral.equals(p.getPrefProlog()) && (constraint.startsWith("neutral ") || constraint.startsWith("neutral:"))) {
					pref = p; constraint = constraint.substring("neutral ".length()).trim(); break;
				} else if (PreferenceLevel.sDiscouraged.equals(p.getPrefProlog()) && (constraint.startsWith("discouraged ") || constraint.startsWith("discouraged:"))) {
					pref = p; constraint = constraint.substring("discouraged ".length()).trim(); break;
				} else if (PreferenceLevel.sStronglyDiscouraged.equals(p.getPrefProlog()) && (constraint.startsWith("strongly discouraged ") || constraint.startsWith("strongly discouraged:"))) {
					pref = p; constraint = constraint.substring("strongly discouraged ".length()).trim(); break;
				} else if (PreferenceLevel.sProhibited.equals(p.getPrefProlog()) && (constraint.startsWith("prohibited ") || constraint.startsWith("prohibited:"))) {
					pref = p; constraint = constraint.substring("prohibited ".length()).trim(); break;
				}
			}
			if (pref == null) {
				iProgress.message(msglevel("automaticHierarchicalConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticInstructorConstraint(term));
				continue;
			}
			DistributionType type = null;
			for (DistributionType t: types) {
				if (constraint.equalsIgnoreCase(t.getReference()) || constraint.equalsIgnoreCase(t.getAbbreviation()) || constraint.equalsIgnoreCase(t.getLabel())) {
					type = t; break;
				}
			}
			if (type == null) {
				for (GroupConstraint.ConstraintType t: GroupConstraint.ConstraintType.values()) {
					if (constraint.equalsIgnoreCase(t.reference()) || constraint.equalsIgnoreCase(t.getName())) {
						type = new DistributionType(); type.setReference(t.reference()); type.setLabel(t.getName()); type.setAbbreviation(t.getName());
						break;
					}
				}
			}
			if (type == null) {
				iProgress.message(msglevel("automaticHierarchicalConstraints", Progress.MSGLEVEL_WARN), MSG.warnFailedToParseAutomaticInstructorConstraint(term));
				continue;
			}
			setPhase(MSG.phasePostingAutomaticConstraint(pref.getPrefName(), type.getLabel()), iInstructors.size());
			ic: for (InstructorConstraint ic: getModel().getInstructorConstraints()) {
		    	incProgress();
				List<Lecture> variables = ic.variables();
				if (variables.size() <= 1) continue;
				List<DistributionType> distributions = iInstructorDistributions.get(ic);
				if (distributions != null) {
					for (DistributionType other: distributions)
						if (isSameType(type, other)) continue ic;
				}
		    	Constraint gc = createGroupConstraint(ic.getId(), type, pref, ic);
		    	if (gc == null) continue;
		    	String info = "";
				for (Lecture var: variables) {
					gc.addVariable(var);
					if (!info.isEmpty()) info += ", ";
					info += getClassLabel(var);
				}
				iProgress.info(MSG.infoPostedConstraint(type.getLabel(), info, pref.getPrefName()));
		    	addGroupConstraint(gc);
    		}
		}
    }
}
