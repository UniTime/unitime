/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
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
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.AssignmentDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.remote.core.RemoteSolverServer;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.RoomAvailability;

import net.sf.cpsolver.coursett.TimetableLoader;
import net.sf.cpsolver.coursett.constraint.ClassLimitConstraint;
import net.sf.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import net.sf.cpsolver.coursett.constraint.DiscouragedRoomConstraint;
import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.InstructorConstraint;
import net.sf.cpsolver.coursett.constraint.JenrlConstraint;
import net.sf.cpsolver.coursett.constraint.MinimizeNumberOfUsedGroupsOfTime;
import net.sf.cpsolver.coursett.constraint.MinimizeNumberOfUsedRoomsConstraint;
import net.sf.cpsolver.coursett.constraint.RoomConstraint;
import net.sf.cpsolver.coursett.constraint.SpreadConstraint;
import net.sf.cpsolver.coursett.model.Configuration;
import net.sf.cpsolver.coursett.model.InitialSectioning;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.Student;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import net.sf.cpsolver.coursett.preference.PreferenceCombination;
import net.sf.cpsolver.coursett.preference.SumPreferenceCombination;
import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.util.FastVector;
import net.sf.cpsolver.ifs.util.Progress;

/**
 * @author Tomas Muller
 */
public class TimetableDatabaseLoader extends TimetableLoader {
	private static Log sLog = LogFactory.getLog(TimetableDatabaseLoader.class);
	private Session iSession;
	private Long iSessionId;
	private Long[] iSolverGroupId;
	private String iSolverGroupIds;
	private String iDepartmentIds = null;
	private SolverGroup[] iSolverGroup;
	private Long[] iSolutionId;
	
	private Hashtable iRooms = new Hashtable();
	private Hashtable iInstructors = new Hashtable();
	private Hashtable iLectures = new Hashtable();
	private Hashtable iSubparts = new Hashtable();
	private Hashtable iStudents = new Hashtable();
	private Hashtable iDeptNames = new Hashtable();
	private Hashtable iPatterns = new Hashtable();
	private Hashtable iClasses = new Hashtable();
	private Set iAllUsedDatePatterns = new HashSet();
	private Set iAllClasses = null;
	
    private boolean iDeptBalancing = true;
    private boolean iMppAssignment = true;
    private boolean iInteractiveMode = false;
    private boolean iSpread = true;
    private boolean iAutoSameStudents = true;

    private double iFewerSeatsDisouraged = 0.01;
    private double iFewerSeatsStronglyDisouraged = 0.02;
    
    private double iNormalizedPrefDecreaseFactor = TimePatternModel.sDefaultDecreaseFactor;
    
    private double iAlterTimePatternWeight = 0.0;
    private TimePatternModel iAlterTimePatternModel = (TimePatternModel)TimePattern.getDefaultRequiredTimeTable().getModel(); 
    private boolean iWeakenTimePreferences = false;
    
    private Progress iProgress = null;
    
    private int iStartDay = 0;
    private int iEndDay = 0;
    
    //private Set iAvailableRooms = new HashSet();
    private boolean iLoadStudentEnrlsFromSolution = false;
    private boolean iFixMinPerWeek = false;
    private boolean iAssignSingleton = true;
    private boolean iIgnoreRoomSharing = false;
    
    private String iAutoSameStudentsConstraint = "SAME_STUDENTS";
    private String iInstructorFormat = null;
    
    private boolean iRoomAvailabilityTimeStampIsSet = false;
    private boolean iWeighStudents = true;
    
    public TimetableDatabaseLoader(TimetableModel model) {
        super(model);
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
        iSpread = getModel().getProperties().getPropertyBoolean("General.Spread",iSpread);
        iAutoSameStudents = getModel().getProperties().getPropertyBoolean("General.AutoSameStudents",iAutoSameStudents);
        iMppAssignment = getModel().getProperties().getPropertyBoolean("General.MPP",iMppAssignment);
        iInteractiveMode = getModel().getProperties().getPropertyBoolean("General.InteractiveMode", iInteractiveMode);
        iAssignSingleton = getModel().getProperties().getPropertyBoolean("General.AssignSingleton", iAssignSingleton);
        
        iFewerSeatsDisouraged = getModel().getProperties().getPropertyDouble("Global.FewerSeatsDisouraged", iFewerSeatsDisouraged);
        iFewerSeatsStronglyDisouraged = getModel().getProperties().getPropertyDouble("Global.FewerSeatsStronglyDisouraged", iFewerSeatsStronglyDisouraged);
        
        iNormalizedPrefDecreaseFactor = getModel().getProperties().getPropertyDouble("General.NormalizedPrefDecreaseFactor", iNormalizedPrefDecreaseFactor);
        
        iLoadStudentEnrlsFromSolution = getModel().getProperties().getPropertyBoolean("Global.LoadStudentEnrlsFromSolution", iLoadStudentEnrlsFromSolution);
        
        iFixMinPerWeek = getModel().getProperties().getPropertyBoolean("Global.FixMinPerWeek", iFixMinPerWeek);
        
        iAlterTimePatternWeight = getModel().getProperties().getPropertyDouble("TimePreferences.Weight", iAlterTimePatternWeight);
        iAlterTimePatternModel.setPreferences(getModel().getProperties().getProperty("TimePreferences.Pref", null));
        
        iWeakenTimePreferences = getModel().getProperties().getPropertyBoolean("TimePreferences.Weaken", iWeakenTimePreferences);
        
        iAutoSameStudentsConstraint = getModel().getProperties().getProperty("General.AutoSameStudentsConstraint",iAutoSameStudentsConstraint);
        
        iInstructorFormat = getModel().getProperties().getProperty("General.InstructorFormat", DepartmentalInstructor.sNameFormatLastFist);
        
        iWeighStudents = getModel().getProperties().getPropertyBoolean("General.WeightStudents", iWeighStudents);
    }
    
    private String getClassLabel(Class_ clazz) {
    	return "<A href='classDetail.do?cid="+clazz.getUniqueId()+"'>"+clazz.getClassLabel()+"</A>";
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
    	/**FOR TESTING  
    	if (locationId.intValue()==3798) return PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged); //BRNG B222
    	if (locationId.intValue()==3852) return PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged); //ME 118
    	if (locationId.intValue()==3713) return PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged); //CIVL 2108
    	*/
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
    
    public static Vector computeRoomLocations(Class_ clazz) {
    	return computeRoomLocations(clazz, false, 0.01, 0.02);
    }
    
    public static Vector computeRoomLocations(Class_ clazz, boolean interactiveMode, double fewerSeatsDisouraged, double fewerSeatsStronglyDisouraged) {
    	int minClassLimit = clazz.getExpectedCapacity().intValue();
    	int maxClassLimit = clazz.getMaxExpectedCapacity().intValue();
    	if (maxClassLimit<minClassLimit) maxClassLimit = minClassLimit;
    	float room2limitRatio = clazz.getRoomRatio().floatValue();
    	int roomCapacity = (int)Math.ceil(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
        int discouragedCapacity = (int)Math.round((1.0-fewerSeatsStronglyDisouraged) * roomCapacity);
        int stronglyDiscouragedCapacity = (int)Math.round((1.0-fewerSeatsStronglyDisouraged) * roomCapacity);

        Vector roomLocations = new Vector();
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
                    for (Enumeration e=roomLocations.elements();e.hasMoreElements();) {
                        RoomLocation r = (RoomLocation)e.nextElement();
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
                    for (Enumeration e=roomLocations.elements();e.hasMoreElements();) {
                        RoomLocation r = (RoomLocation)e.nextElement();
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
                    for (Enumeration e=roomLocations.elements();e.hasMoreElements();) {
                        RoomLocation r = (RoomLocation)e.nextElement();
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
            
            roomLocations.addElement(
            		new RoomLocation(
            				room.getUniqueId(),
            				room.getLabel(),
            				(bldg==null?null:bldg.getUniqueId()),
            				prefInt,
            				room.getCapacity().intValue(),
            				(room.getCoordinateX()==null?-1:room.getCoordinateX().intValue()),
            				(room.getCoordinateY()==null?-1:room.getCoordinateY().intValue()),
            				(room.isIgnoreTooFar()==null?false:room.isIgnoreTooFar().booleanValue()),
            				null));
        }
    	return roomLocations;
    }
    
    private Lecture loadClass(Class_ clazz, org.hibernate.Session hibSession) {
    	Vector timeLocations = new Vector();
    	Vector roomLocations = new Vector();
    	
    	iProgress.debug("loading class "+getClassLabel(clazz));
    	
    	Department dept = clazz.getControllingDept();
    	iDeptNames.put(dept.getUniqueId(),dept.getShortLabel());
    	iProgress.trace("department: "+dept.getName()+" (id:"+dept.getUniqueId()+")");
    	
    	int minClassLimit = clazz.getExpectedCapacity().intValue();
    	int maxClassLimit = clazz.getMaxExpectedCapacity().intValue();
    	if (maxClassLimit<minClassLimit) maxClassLimit = minClassLimit;
    	float room2limitRatio = clazz.getRoomRatio().floatValue();
    	int roomCapacity = (int)Math.ceil(minClassLimit<=0?room2limitRatio:room2limitRatio*minClassLimit);
    	
    	/**FOR TESTING
    	if (clazz.getCourseName().equals("A&AE 001")) {
    		minClassLimit = clazz.getRoomCapacity().intValue();
    		maxClassLimit = clazz.getExpectedCapacity().intValue();
    		room2limitRatio = 1.0;
    		roomCapacity = (int)Math.ceil(minClassLimit*room2limitRatio);
    		iProgress.debug("Tweaking "+getClassLabel(clazz)+": classLimit="+minClassLimit+".."+maxClassLimit+", room2limitRatio="+room2limitRatio);
    	}
    	*/
    	
    	iProgress.trace("class limit: ["+minClassLimit+","+maxClassLimit+"]");
    	iProgress.trace("room2limitRatio: "+room2limitRatio);
    	
    	iProgress.trace("room capacity: "+roomCapacity);
        int discouragedCapacity = (int)Math.round((1.0-iFewerSeatsDisouraged) * roomCapacity);
        iProgress.trace("discouraged capacity: "+discouragedCapacity);
        int stronglyDiscouragedCapacity = (int)Math.round((1.0-iFewerSeatsStronglyDisouraged) * roomCapacity);
        iProgress.trace("strongly discouraged capacity: "+stronglyDiscouragedCapacity);
        
        Set timePrefs = clazz.effectivePreferences(TimePref.class);
        
        if (timePrefs.isEmpty()) {
            if (clazz.getSchedulingSubpart().getMinutesPerWk().intValue()!=0)
                iProgress.warn("Class "+getClassLabel(clazz)+" has no time pattern selected (class not loaded). <i>If not changed, this class will be treated as Arrange "+Math.round(clazz.getSchedulingSubpart().getMinutesPerWk().intValue()/50.0)+" Hours in MSF.</i>");
            return null;
        }
        
        Set patterns = new HashSet();
        
        DatePattern datePattern = clazz.effectiveDatePattern(); 
        if (datePattern==null) {
            iProgress.warn("Class "+getClassLabel(clazz)+" has no date pattern selected (class not loaded).");
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
                        for (Enumeration e=roomLocations.elements();e.hasMoreElements();) {
                            RoomLocation r = (RoomLocation)e.nextElement();
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
                        for (Enumeration e=roomLocations.elements();e.hasMoreElements();) {
                            RoomLocation r = (RoomLocation)e.nextElement();
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
                        for (Enumeration e=roomLocations.elements();e.hasMoreElements();) {
                            RoomLocation r = (RoomLocation)e.nextElement();
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
                
                roomLocations.addElement(
                		new RoomLocation(
                				room.getUniqueId(),
                				room.getLabel(),
                				(bldg==null?null:bldg.getUniqueId()),
                				prefInt,
                				room.getCapacity().intValue(),
                				(room.getCoordinateX()==null?-1:room.getCoordinateX().intValue()),
                				(room.getCoordinateY()==null?-1:room.getCoordinateY().intValue()),
                				(room.isIgnoreTooFar()==null?false:room.isIgnoreTooFar().booleanValue()),
                				getRoomConstraint(clazz.getManagingDept(),room,hibSession)));
            }
        	
            if (roomLocations.isEmpty() || roomLocations.size()<(clazz.getNbrRooms()==null?1:clazz.getNbrRooms().intValue())) {
            	iProgress.warn("Class "+getClassLabel(clazz)+" has no available room"+(clazz.getNbrRooms()!=null && clazz.getNbrRooms().intValue()>1?"s":"")+" (class not loaded).");
            	return null;
            }
        } else {
            if (!groupPrefs.isEmpty() || !roomPrefs.isEmpty() || !bldgPrefs.isEmpty() || !featurePrefs.isEmpty()) 
                iProgress.warn("Class "+getClassLabel(clazz)+" requires no room (number of rooms is set to zero), but it contains some room preferences.");
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
        
        for (Iterator i1=timePrefs.iterator();i1.hasNext();) {
        	TimePref timePref = (TimePref)i1.next();
        	TimePatternModel pattern = timePref.getTimePatternModel();
        	if (pattern.isExactTime()) {
        		int length = ExactTimeMins.getNrSlotsPerMtg(pattern.getExactDays(),clazz.getSchedulingSubpart().getMinutesPerWk().intValue());
        		int breakTime = ExactTimeMins.getBreakTime(pattern.getExactDays(),clazz.getSchedulingSubpart().getMinutesPerWk().intValue()); 
                TimeLocation  loc = new TimeLocation(pattern.getExactDays(),pattern.getExactStartSlot(),length,PreferenceLevel.sIntLevelNeutral,0,datePattern.getUniqueId(),datePattern.getName(),datePattern.getPatternBitSet(),breakTime);
                loc.setTimePatternId(pattern.getTimePattern().getUniqueId());
                timeLocations.addElement(loc);
                continue;
        	}
        	
        	patterns.add(pattern.getTimePattern());
            
            if (iWeakenTimePreferences) {
                pattern.weakenHardPreferences();
                onlyReq = false;
            }
        	
        	if (clazz.getSchedulingSubpart().getMinutesPerWk().intValue()!=pattern.getMinPerMtg()*pattern.getNrMeetings()) {
        		iProgress.warn("Class "+getClassLabel(clazz)+" has "+clazz.getSchedulingSubpart().getMinutesPerWk()+" minutes per week, but "+pattern.getName()+" time pattern selected.");
        		minPerWeek = pattern.getMinPerMtg()*pattern.getNrMeetings();
        		if (iFixMinPerWeek)
        			clazz.getSchedulingSubpart().setMinutesPerWk(new Integer(minPerWeek));
        	}
            
            for (int time=0;time<pattern.getNrTimes(); time++) {
                for (int day=0;day<pattern.getNrDays(); day++) {
                    String pref = pattern.getPreference(day,time);
                	iProgress.trace("checking time "+pattern.getDayHeader(day)+" "+pattern.getTimeHeaderShort(time)+" ("+pref+")");
                    if (!iInteractiveMode && pref.equals(PreferenceLevel.sProhibited)) {
                    	iProgress.trace("time is prohibited :-(");
                    	continue;
                    }
                    if (!iInteractiveMode && onlyReq && !pref.equals(PreferenceLevel.sRequired)) {
                    	iProgress.trace("time is not required :-(");
                    	continue;
                    }
                    Iterator startSlotsIterator = pattern.getStartSlots(day,time).iterator();
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
                    timeLocations.addElement(loc);
                }
            }
        }
        if (iInteractiveMode) {
        	Vector allPatterns = (Vector)iPatterns.get(new Integer(minPerWeek));
        	if (allPatterns==null) {
        		allPatterns = TimePattern.findByMinPerWeek(iSession,false,false,false,minPerWeek,clazz.getManagingDept());
        		iPatterns.put(new Integer(minPerWeek),allPatterns);
        	}
        	for (Enumeration e1=allPatterns.elements();e1.hasMoreElements();) {
        		TimePattern pattern = (TimePattern)e1.nextElement();
        		if (patterns.contains(pattern)) continue;
        		TimePatternModel model = pattern.getTimePatternModel();
        		iProgress.trace("adding prohibited pattern "+model.getName());
                for (int time=0;time<model.getNrTimes(); time++) {
                    for (int day=0;day<model.getNrDays(); day++) {
                        PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited);
                        Iterator startSlotsIterator = model.getStartSlots(day,time).iterator();
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
                        timeLocations.addElement(loc);
                    }
                }
        	}
        }
        
        if (timeLocations.isEmpty()) {
        	iProgress.warn("Class "+getClassLabel(clazz)+" has no available time (class not loaded).");
            return null;
        }
    	
    	Vector instructors = clazz.getLeadInstructors();
    	
    	String className = clazz.getClassLabel();

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
    	for (Enumeration e=instructors.elements();e.hasMoreElements();) {
    		DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
    		getInstructorConstraint(instructor,hibSession).addVariable(lecture);
    	}

    	long estNrValues = lecture.nrTimeLocations();
    	for (int i=0;i<lecture.getNrRooms();i++) {
    	    estNrValues *= (lecture.nrRoomLocations()-i)/(lecture.getNrRooms()-i);
    	}
    	if (estNrValues>1000000) {
    	    iProgress.error("Class "+getClassLabel(lecture)+" has too many possible placements ("+estNrValues+"). " +
    	    		"The class was not loaded in order to prevent out of memory exception. " +
    	    		"Please restrict the number of available rooms and/or times for this class.");
            for (Enumeration e=instructors.elements();e.hasMoreElements();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
                getInstructorConstraint(instructor,hibSession).removeVariable(lecture);
            }
    	    return null;
    	} else if (estNrValues>10000) {
            iProgress.warn("Class "+getClassLabel(lecture)+" has quite a lot of possible placements ("+estNrValues+"). " +
                    "Solver may run too slow. " +
                    "If possible, please restrict the number of available rooms and/or times for this class.");
    	}
    	
        if (lecture.values().isEmpty()) {
        	if (!iInteractiveMode) {
        		iProgress.warn("Class "+getClassLabel(lecture)+" has no available placement (class not loaded).");
            	for (Enumeration e=instructors.elements();e.hasMoreElements();) {
            		DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
            		getInstructorConstraint(instructor,hibSession).removeVariable(lecture);
            	}
        		return null;
        	} else
        		iProgress.warn("Class "+getClassLabel(lecture)+" has no available placement.");
        }
    	iLectures.put(clazz.getUniqueId(),lecture);
        getModel().addVariable(lecture);

        for (Enumeration e1=roomLocations.elements(); e1.hasMoreElements();) {
        	RoomLocation r = (RoomLocation)e1.nextElement();
            r.getRoomConstraint().addVariable(lecture);
        }
        
        /*
        for (Iterator i1=clazz.effectivePreferences(DistributionPref.class, false).iterator();i1.hasNext();) {
        	DistributionPref pref = (DistributionPref)i1.next();
        	iProgress.trace("Dist. constraint "+pref.getPrefLevel().getPrefName()+" "+pref.getDistributionType().getLabel()+" added for class "+lecture.getName()); 
        	Constraint constraint = getGroupConstraint(pref);
        	if (constraint!=null)
        		constraint.addVariable(lecture);
        }
        */
        
        return lecture;
    }
    
    private void assignCommited() {
    	if (!getModel().hasConstantVariables()) return;
    	iProgress.setPhase("Assigning committed classes ...", getModel().constantVariables().size());
    	for (Enumeration e=getModel().constantVariables().elements();e.hasMoreElements();) {
    		Lecture lecture = (Lecture)e.nextElement();
    		iProgress.incProgress();
    		if (lecture.getAssignment()!=null) continue;
    		Placement placement = (Placement)lecture.getInitialAssignment();
            Hashtable conflictConstraints = getModel().conflictConstraints(placement);
            if (conflictConstraints.isEmpty()) {
                lecture.assign(0,placement);
            } else {
                String warn = "Unable to assign committed class "+getClassLabel(lecture)+" &larr; "+placement.getName();
            	warn+="<br>&nbsp;&nbsp;Reason:";
                for (Enumeration ex=conflictConstraints.keys();ex.hasMoreElements();) {
                    Constraint c = (Constraint)ex.nextElement();
                    Collection vals = (Collection)conflictConstraints.get(c);
                    for (Iterator i=vals.iterator();i.hasNext();) {
                        Value v = (Value) i.next();
                        warn+="<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel((Lecture)v.variable())+" = "+((Placement)v).getLongName();
                    }
                    warn+="<br>&nbsp;&nbsp;&nbsp;&nbsp;    in constraint "+c;
                    iProgress.warn(warn);
                }
            }
    	}
    }
    
    private void purgeInvalidValues() {
    	iProgress.setPhase("Purging invalid placements ...", getModel().variables().size());
    	HashSet alreadyEmpty = new HashSet();
    	for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
    		Lecture lecture = (Lecture)e.nextElement();
    		if (lecture.values().isEmpty()) 
    			alreadyEmpty.add(lecture);
    	}
    	for (Enumeration e=(new Vector(getModel().variables())).elements();e.hasMoreElements();) {
    		Lecture lecture = (Lecture)e.nextElement();
    		Vector oldValues = new Vector(lecture.values());
    		lecture.purgeInvalidValues(iInteractiveMode);
    		if (lecture.values().isEmpty()) {
    			if (!alreadyEmpty.contains(lecture)) {
                    String warn = "Class "+getClassLabel(lecture)+" has no available placement (after enforcing consistency between the problem and committed solutions"+(iInteractiveMode?"":", class not loaded")+")."; 
	    			for (Enumeration f=oldValues.elements();f.hasMoreElements();) {
	    				Placement p = (Placement)f.nextElement();
                        warn += "<br>&nbsp;&nbsp;&nbsp;&nbsp;"+p.getNotValidReason();
	    			}
                    iProgress.warn(warn);
    			}
    			if (!iInteractiveMode) {
    				getModel().removeVariable(lecture);
    				for (Enumeration f=(new Vector(lecture.constraints())).elements();f.hasMoreElements();) {
    					Constraint c = (Constraint)f.nextElement();
    					c.removeVariable(lecture);
    					if (c.variables().isEmpty())
    						getModel().removeConstraint(c);
    				}
                    for (Iterator i=lecture.students().iterator();i.hasNext();) {
                        Student s = (Student)i.next();
                        s.getLectures().remove(lecture);
                    }
    			}
    		}
    		iProgress.incProgress();
    	}
    }
    
    private void loadAssignment(Assignment assignment) {
    	Lecture lecture = (Lecture)iLectures.get(assignment.getClazz().getUniqueId());
    	int dayCode = assignment.getDays().intValue();
    	int startSlot = assignment.getStartSlot().intValue();
    	Long patternId = assignment.getTimePattern().getUniqueId();
    	Set rooms = assignment.getRooms();

    	if (lecture==null) return;
    	Placement initialPlacement = null;
    	for (Iterator i2=lecture.values().iterator();i2.hasNext();) {
    		Placement placement = (Placement)i2.next();
    		if (placement.getTimeLocation().getDayCode()!=dayCode) continue;
    		if (placement.getTimeLocation().getStartSlot()!=startSlot) continue;
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
        	for (Enumeration e2=lecture.timeLocations().elements();e2.hasMoreElements();) {
        		TimeLocation t = (TimeLocation)e2.nextElement();
        		if (t.getDayCode()!=dayCode) continue;
        		if (t.getStartSlot()!=startSlot) continue;
        		if (!t.getTimePatternId().equals(patternId)) continue;
        		timeLocation = t; break;
        	}
        	Vector roomLocations = new Vector();
    		for (Iterator i=rooms.iterator();i.hasNext();) {
    			Location room = (Location)i.next();
            	for (Enumeration e2=lecture.roomLocations().elements();e2.hasMoreElements();) {
            		RoomLocation r = (RoomLocation)e2.nextElement();
            		if (r.getId().equals(room.getUniqueId()))
            			roomLocations.add(r);
            	}
    		}
    		if (timeLocation!=null && roomLocations.size()==lecture.getNrRooms()) {
    			initialPlacement = new Placement(lecture,timeLocation,roomLocations);
        	}
    	}
    	if (initialPlacement==null) {
    		StringBuffer sb = new StringBuffer(assignment.getTimeLocation().getName()+" ");
    		for (Iterator i=rooms.iterator();i.hasNext();) {
    			sb.append(((Location)i.next()).getLabel());
    			if (i.hasNext()) sb.append(", ");
    		}
    		if (!assignment.getInstructors().isEmpty()) {
    			sb.append(" ");
    			for (Iterator i=assignment.getInstructors().iterator();i.hasNext();) {
        			sb.append(((DepartmentalInstructor)i.next()).getName(iInstructorFormat));
        			if (i.hasNext()) sb.append(", ");
    			}
    		}
    		iProgress.warn("Unable to assign "+getClassLabel(lecture)+" &larr; "+sb+" (placement not valid)");
    		return;
    	}
        if (!initialPlacement.isValid()) {
			String reason = "";
           	for (Enumeration e=lecture.getInstructorConstraints().elements();e.hasMoreElements();) {
           		InstructorConstraint ic = (InstructorConstraint)e.nextElement();
    			if (!ic.isAvailable(lecture, initialPlacement))
    				reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;instructor "+ic.getName()+" not available";
           	}
	    	if (lecture.getNrRooms()>0) {
	    		if (initialPlacement.isMultiRoom()) {
	    			for (Enumeration f=initialPlacement.getRoomLocations().elements();f.hasMoreElements();) {
	    				RoomLocation roomLocation = (RoomLocation)f.nextElement();
	    				if (!roomLocation.getRoomConstraint().isAvailable(lecture,initialPlacement.getTimeLocation(),lecture.getScheduler()))
	    					reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;room "+roomLocation.getName()+" not available";
	    			}
	    		} else {
					if (!initialPlacement.getRoomLocation().getRoomConstraint().isAvailable(lecture,initialPlacement.getTimeLocation(),lecture.getScheduler()))
						reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;room "+initialPlacement.getRoomLocation().getName()+" not available";
	    		}
	    	}
            Hashtable conflictConstraints = getModel().conflictConstraints(initialPlacement);
            if (!conflictConstraints.isEmpty()) {
                for (Enumeration ex=conflictConstraints.keys();ex.hasMoreElements();) {
                    Constraint c = (Constraint)ex.nextElement();
                    Collection vals = (Collection)conflictConstraints.get(c);
                    for (Iterator i=vals.iterator();i.hasNext();) {
                        Placement p = (Placement) i.next();
                        Lecture l = (Lecture)p.variable();
                        if (l.isCommitted())
                        	reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;conflict with committed assignment "+getClassLabel(l)+" = "+p.getLongName()+" (in constraint "+c+")";
                        if (p.equals(initialPlacement))
                        	reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;constraint "+c;
                    }
                }
            }
	    	iProgress.warn("Unable to assign "+getClassLabel(lecture)+" &larr; "+initialPlacement.getLongName()+(reason.length()==0?".":":"+reason));
		} else {
			if (iMppAssignment) lecture.setInitialAssignment(initialPlacement);
			Hashtable conflictConstraints = getModel().conflictConstraints(initialPlacement);
        	if (conflictConstraints.isEmpty()) {
    	        lecture.assign(0,initialPlacement);
	        } else {
                String warn = "Unable to assign "+getClassLabel(lecture)+" &larr; "+initialPlacement.getName();
                warn += "<br>&nbsp;&nbsp;Reason:";
	            for (Enumeration ex=conflictConstraints.keys();ex.hasMoreElements();) {
            	    Constraint c = (Constraint)ex.nextElement();
        	        Collection vals = (Collection)conflictConstraints.get(c);
    	            for (Iterator i=vals.iterator();i.hasNext();) {
	                    Value v = (Value) i.next();
                        warn += "<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel((Lecture)v.variable())+" = "+((Placement)v).getLongName();
            	    }
                    warn += "<br>&nbsp;&nbsp;&nbsp;&nbsp;    in constraint "+c;
                    iProgress.warn(warn);
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
    						(location.getCoordinateX()==null?-1:location.getCoordinateX().intValue()),
    						(location.getCoordinateY()==null?-1:location.getCoordinateY().intValue()),
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
    						(location.getCoordinateX()==null?-1:location.getCoordinateX().intValue()),
    						(location.getCoordinateY()==null?-1:location.getCoordinateY().intValue()),
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
    		/*
    		if (ic!=null && !ic.getResourceId().equals(instructor.getUniqueId()))
    			iProgress.debug("Instructor "+instructor.getName(iInstructorFormat)+" (puid:"+instructor.getExternalUniqueId()+") is interesting :)");
    		*/
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
    
    private void loadInstructorAvailability(DepartmentalInstructor instructor, InstructorConstraint ic, org.hibernate.Session hibSession) {
    	if (instructor.getExternalUniqueId()==null || instructor.getExternalUniqueId().length()==0) return;
    	Query q = hibSession.createQuery("select distinct a from DepartmentalInstructor i inner join i.assignments as a " +
    			"where i.externalUniqueId=:puid and a.solution.owner.session.uniqueId=:sessionId and a.solution.commited=true and a.solution.owner.uniqueId not in ("+iSolverGroupIds+")");
    	q.setString("puid", instructor.getExternalUniqueId());
		q.setLong("sessionId",iSessionId.longValue());
		for (Iterator i=q.iterate();i.hasNext();) {
			Assignment a = (Assignment)i.next();
			Placement p = a.getPlacement();
			ic.setNotAvailable(p);
			if (!iLectures.containsKey(a.getClassId())) {
				iLectures.put(a.getClassId(), p.variable());
				getModel().addVariable(p.variable());
			}
		}
    }
    
    private void loadInstructorAvailabilities(org.hibernate.Session hibSession, String puids) {
    	Query q = hibSession.createQuery("select distinct i.externalUniqueId, a from DepartmentalInstructor i inner join i.assignments as a " +
    			"where i.externalUniqueId in ("+puids+") and a.solution.owner.session.uniqueId=:sessionId and a.solution.commited=true and a.solution.owner.uniqueId not in ("+iSolverGroupIds+")");
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
    	iProgress.setPhase("Loading instructor availabilities ...", 1);
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
    	iProgress.incProgress();
    }
    
    private void loadRoomAvailability(Location location, RoomConstraint rc, org.hibernate.Session hibSession) {
		Query q = hibSession.createQuery("select distinct a from Location r inner join r.assignments as a "+
			"where r.uniqueId=:roomId and a.solution.owner.session.uniqueId=:sessionId and a.solution.commited=true and a.solution.owner.uniqueId not in ("+iSolverGroupIds+")");
		q.setInteger("roomId",location.getUniqueId().intValue());
		q.setLong("sessionId",iSessionId.longValue());
		for (Iterator i=q.iterate();i.hasNext();) {
			Assignment a = (Assignment)i.next();
			Placement p = a.getPlacement();
			rc.setNotAvailable(p);
			if (!iLectures.containsKey(a.getClassId())) {
				iLectures.put(a.getClassId(), p.variable());
				getModel().addVariable(p.variable());
			}
		}
    }
    
    private void loadRoomAvailabilities(org.hibernate.Session hibSession, String roomids) {
		Query q = hibSession.createQuery("select distinct r.uniqueId, a from Location r inner join r.assignments as a "+
				"where r.uniqueId in ("+roomids+") and a.solution.owner.session.uniqueId=:sessionId and a.solution.commited=true and a.solution.owner.uniqueId not in ("+iSolverGroupIds+")");
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
    	iProgress.setPhase("Loading room availabilities ...", 1);
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
    	iProgress.incProgress();
    }
    
    private Constraint createGroupConstraint(DistributionPref pref) {
    	Constraint gc = null;
    	if ("SAME_INSTR".equals(pref.getDistributionType().getReference()) && PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())) {
    		gc = new InstructorConstraint(new Long(-(int)pref.getUniqueId().longValue()),null, pref.getDistributionType().getLabel(),false);
    	} else if ("SPREAD".equals(pref.getDistributionType().getReference())) {
    		gc = new SpreadConstraint(getModel().getProperties(), "spread");
    	} else if ("MIN_ROOM_USE".equals(pref.getDistributionType().getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedRoomsConstraint(getModel().getProperties());
    		else
    			iProgress.info("Minimize number of used rooms constraint not loaded due to the interactive mode of the solver.");
    	} else if ("MIN_GRUSE(10x1h)".equals(pref.getDistributionType().getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"10x1h",MinimizeNumberOfUsedGroupsOfTime.sGroups10of1h);
    		else
    			iProgress.info("Minimize number of used groups of time constraint not loaded due to the interactive mode of the solver.");
    	} else if ("MIN_GRUSE(5x2h)".equals(pref.getDistributionType().getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"5x2h",MinimizeNumberOfUsedGroupsOfTime.sGroups5of2h);
    		else
    			iProgress.info("Minimize number of used groups of time constraint not loaded due to the interactive mode of the solver.");
    	} else if ("MIN_GRUSE(3x3h)".equals(pref.getDistributionType().getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"3x3h",MinimizeNumberOfUsedGroupsOfTime.sGroups3of3h);
    		else
    			iProgress.info("Minimize number of used groups of time constraint not loaded due to the interactive mode of the solver.");
    	} else if ("MIN_GRUSE(2x5h)".equals(pref.getDistributionType().getReference())) {
    		if (!iInteractiveMode)
    			gc = new MinimizeNumberOfUsedGroupsOfTime(getModel().getProperties(),"2x5h",MinimizeNumberOfUsedGroupsOfTime.sGroups2of5h);
    		else
    			iProgress.info("Minimize number of used groups of time constraint not loaded due to the interactive mode of the solver.");
    	} else {
    		gc = new GroupConstraint(pref.getUniqueId(),pref.getDistributionType().getReference(),pref.getPrefLevel().getPrefProlog());
    	}
    	return gc;
    }
    
    private void errorAddGroupConstraintNotFound(DistributionPref pref, Class_ clazz) {
        if (pref.getOwner()!=null && pref.getOwner() instanceof DepartmentalInstructor) 
            iProgress.info("Lecture "+getClassLabel(clazz)+" not found/loaded, but used in distribution preference "+pref.getDistributionType().getLabel()+pref.getGroupingSufix());
        else
            iProgress.warn("Lecture "+getClassLabel(clazz)+" not found/loaded, but used in distribution preference "+pref.getDistributionType().getLabel()+pref.getGroupingSufix());
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
    
    private void addGroupConstraint(Constraint gc) {
    	if (gc.variables().isEmpty()) return;
    	boolean allVariablesAreCommitted = true;
    	for (Enumeration e=gc.variables().elements();e.hasMoreElements();) {
    		Lecture lecture = (Lecture)e.nextElement();
    		if (!lecture.isCommitted()) {
    			allVariablesAreCommitted = false;
    			break;
    		}
    	}
    	if (allVariablesAreCommitted) {
    		iProgress.debug("Not created constraint "+gc.getName()+" between "+gc.variables()+" (all variables are committed)");
			for (Enumeration e=(new Vector(gc.variables())).elements();e.hasMoreElements();) {
				Lecture lecture = (Lecture)e.nextElement();
				gc.removeVariable(lecture);
			}
    		return;
    	}
		getModel().addConstraint(gc);
		iProgress.trace("Added constraint "+gc.getName()+" between "+gc.variables());
    }
    
    private void loadGroupConstraint(DistributionPref pref) {
    	int groupingType = (pref.getGrouping()==null?DistributionPref.sGroupingNone:pref.getGrouping().intValue());
    	if (groupingType==DistributionPref.sGroupingProgressive) {
    		int maxSize = 0;
    		for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
        		DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_)
        			maxSize = Math.max(maxSize, 1);
        		else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart)
        			maxSize = Math.max(maxSize, ((SchedulingSubpart)distributionObject.getPrefGroup()).getClasses().size());
    		}
    		Constraint gc[] = new Constraint[maxSize];
    		Set gcClasses[] = new Set[maxSize];
    		for (int i=0;i<gc.length;i++) {
    			gc[i]=createGroupConstraint(pref);
    			if (gc[i]==null) return;
    			gcClasses[i]=new HashSet();
    		}
    		Vector allLectureOfCorrectOrder = new Vector();
    		for (Iterator i=pref.getOrderedSetOfDistributionObjects().iterator();i.hasNext();) {
    			DistributionObject distributionObject = (DistributionObject)i.next();
        		if (distributionObject.getPrefGroup() instanceof Class_) {
        			Class_ clazz = (Class_)distributionObject.getPrefGroup();
        			Lecture lecture = (Lecture)getLecture(clazz);
        			if (lecture!=null)  allLectureOfCorrectOrder.add(lecture);
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	Vector classes = new Vector(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Enumeration e=classes.elements();e.hasMoreElements();) {
        	    		Class_ clazz = (Class_)e.nextElement();
            			Lecture lecture = getLecture(clazz);
            			if (lecture!=null)  allLectureOfCorrectOrder.add(lecture);
        	    	}
        		}
        	}
    		Vector distributionObjects = new Vector(pref.getDistributionObjects());
    		Collections.sort(distributionObjects, new ChildrenFirstDistributionObjectComparator());
        	for (Enumeration e=distributionObjects.elements();e.hasMoreElements();) {
        		DistributionObject distributionObject = (DistributionObject)e.nextElement();
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
        	    	Vector classes = new Vector(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (int j=0;j<gc.length;j++) {
        	    		Class_ clazz = null;
        	    		for (Iterator k=gcClasses[j].iterator();k.hasNext() && clazz==null;) {
        	    			clazz = getParentClass((Class_)k.next(),subpart);
        	    		}
        	    		if (clazz==null) clazz = (Class_)classes.elementAt(j%classes.size());
        	    		
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			
            			gc[j].addVariable(lecture);
            			gcClasses[j].add(clazz);
        	    	}
        		} else {
        			iProgress.warn("Distribution preference "+pref.getDistributionType().getLabel()+pref.getGroupingSufix()+" refers to unsupported object "+distributionObject.getPrefGroup());
        		}
        	}
    		for (int i=0;i<gc.length;i++) {
    			Comparator cmp = new ObjectsByGivenOrderComparator(allLectureOfCorrectOrder);
    			if (!gc[i].variables().isEmpty()) {
    				Collections.sort(gc[i].variables(), cmp);
    				addGroupConstraint(gc[i]);
    			}
    		}
    	} else if (groupingType==DistributionPref.sGroupingPairWise) {
    		Vector lectures = new Vector();
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
        	    	Vector classes = new Vector(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Enumeration e=classes.elements();e.hasMoreElements();) {
        	    		Class_ clazz = (Class_)e.nextElement();
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			lectures.add(lecture);
        	    	}
        		} else {
        			iProgress.warn("Distribution preference "+pref.getDistributionType().getLabel()+pref.getGroupingSufix()+" refers to unsupported object "+distributionObject.getPrefGroup());
        		}
        	}
        	if (lectures.size()<2) {
        		iProgress.warn("Distribution preference "+pref.getDistributionType().getLabel()+pref.getGroupingSufix()+" refers to less than two classes");
        	} else {
        		for (int idx1=0;idx1<lectures.size()-1;idx1++) {
        			Lecture l1 = (Lecture)lectures.elementAt(idx1);
        			for (int idx2=idx1+1;idx2<lectures.size();idx2++) {
        				Lecture l2 = (Lecture)lectures.elementAt(idx2);
        				Constraint gc = createGroupConstraint(pref);
        	    		if (gc==null) return;
        	    		gc.addVariable(l1);
        	    		gc.addVariable(l2);
        	    		addGroupConstraint(gc); 
        			}
        		}
        	}
    	} else {
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
            		if (groupingType>=DistributionPref.sGroupingByTwo && gc.variables().size()==groupingType) {
            			addGroupConstraint(gc);
            			gc=createGroupConstraint(pref);
            			if (gc==null) return;
            		}
        		} else if (distributionObject.getPrefGroup() instanceof SchedulingSubpart) {
        			SchedulingSubpart subpart = (SchedulingSubpart)distributionObject.getPrefGroup();
        	    	Vector classes = new Vector(subpart.getClasses());
        	    	Collections.sort(classes,new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        	    	for (Enumeration e=classes.elements();e.hasMoreElements();) {
        	    		Class_ clazz = (Class_)e.nextElement();
            			Lecture lecture = getLecture(clazz);
            			if (lecture==null) {
            				errorAddGroupConstraintNotFound(pref, clazz); continue;
            			}
            			gc.addVariable(lecture);
                		if (groupingType>=DistributionPref.sGroupingByTwo && gc.variables().size()==groupingType) {
               				addGroupConstraint(gc); 
                			gc=createGroupConstraint(pref);
                			if (gc==null) return;
                		}
        	    	}
        		} else {
        			iProgress.warn("Distribution preference "+pref.getDistributionType().getLabel()+pref.getGroupingSufix()+" refers to unsupported object "+distributionObject.getPrefGroup());
        		}
        	}
       		addGroupConstraint(gc);
    	}
    }
    
    private void loadInstructorGroupConstraint(DepartmentalInstructor instructor, DistributionPref pref) {
		Constraint gc = createGroupConstraint(pref);
		if (gc==null) return;
		boolean allExternallyManaged = true;
    	for (Iterator i=instructor.getClasses().iterator();i.hasNext();) {
    		ClassInstructor classInstructor = (ClassInstructor)i.next();
   			Class_ clazz = (Class_)classInstructor.getClassInstructing();
   			if (!clazz.getManagingDept().isExternalManager().booleanValue()) {
   				allExternallyManaged = false; break;
   			}
    	}
    	if (allExternallyManaged) return;
    	for (Iterator i=instructor.getClasses().iterator();i.hasNext();) {
    		ClassInstructor classInstructor = (ClassInstructor)i.next();
   			Class_ clazz = (Class_)classInstructor.getClassInstructing();
   			Lecture lecture = getLecture(clazz);
   			if (lecture==null) {
   				errorAddGroupConstraintNotFound(pref, clazz); continue;
   			}
   			gc.addVariable(lecture);
    	}
   		addGroupConstraint(gc);
    }
    
    private void loadInstructorGroupConstraints(DepartmentalInstructor instructor) {
    	Set prefs = instructor.getPreferences(DistributionPref.class);
    	if (prefs==null || prefs.isEmpty()) return;
    	for (Iterator i=prefs.iterator();i.hasNext();) {
    		DistributionPref pref = (DistributionPref)i.next();
    		loadInstructorGroupConstraint(instructor, pref);
    	}
    }

    private void loadInstructorGroupConstraints(Department department, org.hibernate.Session hibSession) {
    	List instructors = hibSession.createQuery(
    			"select distinct di from DepartmentalInstructor di inner join di.department d where d.uniqueId=:deptId"
    			).setLong("deptId",department.getUniqueId().longValue()).list();
    	if (instructors==null || instructors.isEmpty()) return;
    	iProgress.setPhase("Loading instructor distr. constraints for "+department.getShortLabel()+" ...", instructors.size());
    	for (Iterator i=instructors.iterator();i.hasNext();) {
    		DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
    		loadInstructorGroupConstraints(instructor);
    		iProgress.incProgress();
    	}
    }
    

    private static Class_ getParentClass(Class_ clazz, SchedulingSubpart parentSubpart) {
		if (clazz==null) return null;
		if (parentSubpart.getClasses().contains(clazz)) return clazz;
		return getParentClass(clazz.getParentClass(), parentSubpart);
	}
    
    private static class ChildrenFirstDistributionObjectComparator implements Comparator {
    	
    	public int compare(Object o1, Object o2) {
    		DistributionObject d1 = (DistributionObject)o1;
    		DistributionObject d2 = (DistributionObject)o2;
    		
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

    		return d1.compareTo(d2);
    	}
    }
    
    private String getClassLimitConstraitName(SchedulingSubpart subpart) {
    	if (subpart==null) return "class-limit";
    	String name = subpart.getCourseName()+" "+subpart.getItypeDesc().trim();
    	String sufix = subpart.getSchedulingSubpartSuffix();
    	if (sufix!=null && sufix.length()>0)
    		name += " ("+sufix+")";
    	return name;
    }
    
    private String getClassLimitConstraitName(Lecture lecture) {
    	return getClassLimitConstraitName((SchedulingSubpart)iSubparts.get(lecture.getSchedulingSubpartId()));
    }
    
    
    private void createChildrenClassLimitConstraits(Lecture parentLecture) {
    	if (!parentLecture.hasAnyChildren()) return;
		for (Enumeration e1=parentLecture.getChildrenSubpartIds();e1.hasMoreElements();) {
			Long subpartId = (Long) e1.nextElement();
        	Vector children = parentLecture.getChildren(subpartId);

        	ClassLimitConstraint clc = new ClassLimitConstraint(parentLecture, getClassLimitConstraitName(parentLecture));

        	boolean isMakingSense = false;
        	for (Enumeration e=children.elements();e.hasMoreElements();) {
        		Lecture lecture = (Lecture)e.nextElement();
        		if (lecture.minClassLimit()!=lecture.maxClassLimit()) {
        			isMakingSense=true; break;
        		}
        	} 
        	
        	if (!isMakingSense) continue;

        	for (Enumeration e=children.elements();e.hasMoreElements();) {
        		Lecture lecture = (Lecture)e.nextElement();
        		clc.addVariable(lecture);
        		createChildrenClassLimitConstraits(lecture);
        	}

    		for (Iterator i=((SchedulingSubpart)iSubparts.get(subpartId)).getClasses().iterator();i.hasNext();) {
    			Class_ clazz = (Class_)i.next();
    			if (iLectures.get(clazz.getUniqueId())==null)
    				clc.setClassLimitDelta(clc.getClassLimitDelta()-clazz.getClassLimit());
    		}

    		iProgress.trace("Added constraint "+clc.getName()+" between "+clc.variables());
       		getModel().addConstraint(clc);
        }
    }
    
    private Lecture getEnrollment(Student student, Vector lectures) {
		for (Enumeration e=lectures.elements();e.hasMoreElements();) {
			Lecture lecture = (Lecture)e.nextElement();
			if (student.getLectures().contains(lecture)) return lecture;
		}
    	return null;
    }
    
    public void load() {
    	org.hibernate.Session hibSession = null;
    	Transaction tx = null;
    	try {
    		TimetableManagerDAO dao = new TimetableManagerDAO();
    		hibSession = dao.getSession();
    		tx = hibSession.beginTransaction(); 
    		
    		load(hibSession);
    		
    		tx.commit();
    	} catch (Exception e) {
    		iProgress.fatal("Unable to load input data, reason:"+e.getMessage(),e);
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
		
		Vector variables = new Vector();
		variables.addElement(lecture);
		
		Class_ parent = clazz;
		while ((parent=parent.getParentClass())!=null) {
			Lecture parentLecture = getLecture(parent);
			if (parentLecture!=null)
				variables.addElement(parentLecture);
		}

    	for (Iterator i=clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator();i.hasNext();) {
    		SchedulingSubpart subpart = (SchedulingSubpart)i.next();
    		if (subpart.getParentSubpart()!=null || subpart.getClasses().size()!=1) continue;
    		Class_ singleClazz = (Class_)subpart.getClasses().iterator().next();
    		Lecture singleLecture = getLecture(singleClazz);
			if (singleLecture!=null && !variables.contains(singleLecture))
				variables.addElement(singleLecture);
    	}

    	if (variables.size()==1) return false;
    	
    	GroupConstraint gc = new GroupConstraint(null,type,PreferenceLevel.sRequired);
		for (Enumeration e=variables.elements();e.hasMoreElements();)
			gc.addVariable((Lecture)e.nextElement());
    	
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
    		for (Enumeration e=parentLecture.sameSubpartLectures().elements();e.hasMoreElements();) {
    			Lecture lecture = (Lecture)e.nextElement();
    			if (!lecture.equals(parentLecture) && !lecture.isCommitted()) {
    				//iProgress.debug("[A] Students "+students+" cannot enroll "+lecture.getName()+" due to the enrollment of "+clazz.getClassLabel());
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
    		//iProgress.debug("[B] Students "+students+" cannot enroll "+lecture.getName()+" due to the enrollment of "+parent.getClassLabel());
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
    
    private void loadCommittedStudentConflicts(org.hibernate.Session hibSession) {
        Query q = null;
        if (iSolverGroup.length>1 || iSolverGroup[0].isExternalManager()) {
        	q = hibSession.createQuery(
        			"select distinct a, e.studentId from "+
        			"Solution s inner join s.assignments a inner join s.studentEnrollments e "+
        			"where "+
        			"s.commited=true and s.owner.session.uniqueId=:sessionId and s.owner not in ("+iSolverGroupIds+") and "+
        			"a.clazz=e.clazz");
        } else {
        	q = hibSession.createQuery(
        			"select distinct a, e.studentId from "+
        			"Solution s inner join s.assignments a inner join s.studentEnrollments e, "+
        			"LastLikeCourseDemand d inner join d.subjectArea sa "+
        			"where "+
        			"s.commited=true and s.owner.session.uniqueId=:sessionId and s.owner.uniqueId!=:ownerId and "+
        			"a.clazz=e.clazz and e.studentId=d.student.uniqueId and sa.department.uniqueId in ("+iDepartmentIds+")");
        	q.setLong("ownerId",iSolverGroup[0].getUniqueId().longValue());
        }
		q.setLong("sessionId", iSessionId.longValue());
		List assignmentEnrollments = q.list();

		Hashtable assignments = new Hashtable();
		for (Iterator i1=assignmentEnrollments.iterator(); i1.hasNext();) {
			Object[] result = (Object[])i1.next();
    		Assignment assignment = (Assignment)result[0];
			Long studentId = (Long)result[1];
    		Student student = (Student)iStudents.get(studentId);
    		if (student!=null) {
    			HashSet students = (HashSet)assignments.get(assignment);
    			if (students==null) {
    				students = new HashSet();
    				assignments.put(assignment, students);
    			}
    			students.add(student);
    		}
		}
		for (Iterator i1=assignmentEnrollments.iterator(); i1.hasNext();) {
			Object[] result = (Object[])i1.next();
    		Assignment assignment = (Assignment)result[0];
    		if (!assignments.containsKey(assignment)) hibSession.evict(assignment);
		}
		for (Enumeration e1=assignments.keys(); e1.hasMoreElements();) {
			Assignment assignment = (Assignment)e1.nextElement();
			Hibernate.initialize(assignment.getClazz());
		}
		for (Enumeration e1=assignments.keys(); e1.hasMoreElements();) {
			Assignment assignment = (Assignment)e1.nextElement();
			Hibernate.initialize(assignment.getClazz().getChildClasses());
			Hibernate.initialize(assignment.getClazz().getSchedulingSubpart());
		}
		for (Enumeration e1=assignments.keys(); e1.hasMoreElements();) {
			Assignment assignment = (Assignment)e1.nextElement();
			Hibernate.initialize(assignment.getClazz().getSchedulingSubpart().getChildSubparts());
			Hibernate.initialize(assignment.getClazz().getSchedulingSubpart().getClasses());
		}
		
        iProgress.setPhase("Loading student conflicts with commited solutions ...", assignments.size());
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
    		iProgress.incProgress();
        }
    }
    
    private boolean canAttend(Set cannotAttendLectures, Collection lectures) {
    	for (Iterator e=lectures.iterator();e.hasNext();) {
    		Lecture lecture = (Lecture)e.next();
    		if (cannotAttendLectures.contains(lecture)) continue;
    		boolean canAttend = true;
    		if (lecture.hasAnyChildren()) {
    			for (Enumeration f=lecture.getChildrenSubpartIds();f.hasMoreElements();) {
    				Long subpartId = (Long)f.nextElement();
    				if (!canAttend(cannotAttendLectures, lecture.getChildren(subpartId))) {
    					canAttend = false; break;
    				}
    			}
    		}
    		if (canAttend) return true;
    	}
    	return false;
    }
    
    private boolean canAttendConfigurations(Set cannotAttendLectures, Vector configurations) {
    	for (Enumeration e=configurations.elements();e.hasMoreElements();) {
    		Configuration cfg = (Configuration)e.nextElement();
    		boolean canAttend = true;
    		for (Enumeration f=cfg.getTopSubpartIds();f.hasMoreElements();) {
    			Long subpartId = (Long)f.nextElement();
    			if (!canAttend(cannotAttendLectures, cfg.getTopLectures(subpartId))) {
    				canAttend = false; break;
    			}
    		}
    		if (canAttend) return true;
    	}
    	return false;
    }
    
    private void checkReservation(CourseOffering course, Set cannotAttendLectures, Vector configurations) {
    	if (canAttendConfigurations(cannotAttendLectures, configurations)) return;
    	iProgress.warn("Inconsistent course reservations for course "+getOfferingLabel(course));
    }
    
    private void propagateCannotAttend(Class_ clazz, HashSet cannotAttendLectures) {
    	for (Iterator i=clazz.getChildClasses().iterator(); i.hasNext();) {
    		Class_ child = (Class_)i.next();
			Lecture lecture = (Lecture)iLectures.get(child.getUniqueId());
			if (lecture!=null)
				cannotAttendLectures.add(lecture);
			propagateCannotAttend(child, cannotAttendLectures);
    	}
    }
    
    private void propagateUpCannotAttend(Set classes, HashSet cannotAttendLectures) {
    	if (classes.isEmpty()) return;
    	HashSet parentClasses = new HashSet();
    	HashSet subparts = new HashSet();
    	for (Iterator x=classes.iterator();x.hasNext();) {
    		Class_ cx = (Class_)x.next();;
    		SchedulingSubpart subpart = cx.getSchedulingSubpart();
    		if (subparts.add(subpart)) {
    	    	for (Iterator i=subpart.getClasses().iterator();i.hasNext();) {
    	    		Class_ clazz = (Class_)i.next();
    	    		if (classes.contains(clazz)) {
    	    			if (clazz.getParentClass()!=null) parentClasses.add(clazz.getParentClass());
    	    		} else {
    	    			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    	    			if (lecture!=null && !lecture.isCommitted())
    	    				cannotAttendLectures.add(lecture);
    	    		}
    	    	}
    		}
    	}
    	propagateUpCannotAttend(parentClasses, cannotAttendLectures);
    }
    
    private Set computeCannotAttendLectures(Set reservedClassIds) {
    	HashSet cannotAttendLectures = new HashSet();
    	HashSet parentClasses = new HashSet();
    	for (Iterator i=reservedClassIds.iterator();i.hasNext();) {
    		Long classId = (Long)i.next();
    		Class_ cx = (Class_)iClasses.get(classId);
    		if (cx==null) cx = (new Class_DAO()).get(classId);
    		if (cx==null) continue;
    		if (cx.getParentClass()!=null)
    			parentClasses.add(cx.getParentClass());
    		for (Iterator j=cx.getSchedulingSubpart().getClasses().iterator();j.hasNext();) {
    			Class_ clazz = (Class_)j.next();
    			if (reservedClassIds.contains(clazz.getUniqueId())) continue;
    			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    			if (lecture!=null && !lecture.isCommitted())
    				cannotAttendLectures.add(lecture);
    			propagateCannotAttend(clazz, cannotAttendLectures);
    		}
    	}
    	propagateUpCannotAttend(parentClasses, cannotAttendLectures);
    	return cannotAttendLectures;
    }
    
    private void load(org.hibernate.Session hibSession) throws Exception {
		iProgress.setStatus("Loading input data ...");
		SolverGroupDAO dao = new SolverGroupDAO();
		hibSession = dao.getSession();
		
		hibSession.setFlushMode(FlushMode.COMMIT);

		iSolverGroup = null;
		iSession = null;
		
        if (iSolverGroup==null) {
        	iSolverGroup = new SolverGroup[iSolverGroupId.length];
        	for (int i=0;i<iSolverGroupId.length;i++) {
        		iSolverGroup[i] = dao.get(iSolverGroupId[i], hibSession);
        		if (iSolverGroup[i]==null) {
        			iProgress.fatal("Unable to load solver group "+iSolverGroupId[i]+".");
        			return;
        		}
        		iProgress.debug("solver group["+(i+1)+"]: "+iSolverGroup[i].getName());
        	}
        }
		if (iSolverGroup==null || iSolverGroup.length==0) {
			iProgress.fatal("No solver group loaded.");
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
		
        Hashtable solutions = null;
        if (iSolutionId!=null && iSolutionId.length>0) {
        	solutions = new Hashtable();
        	//iLoadStudentEnrlsFromSolution = true;
        	String note="";
        	for (int i=0;i<iSolutionId.length;i++) {
        		Solution solution = (new SolutionDAO()).get(iSolutionId[i], hibSession);
        		if (solution==null) {
        			iProgress.fatal("Unable to load solution "+iSolutionId[i]+".");
        			return;
        		}
       			iProgress.debug("solution["+(i+1)+"] version: "+solution.getUniqueId()+" (created "+solution.getCreated()+", solver group "+solution.getOwner().getName()+")");
       			if (solution.getNote()!=null) {
        			if (note.length()>0) note += "\n";
        			note += solution.getNote();
        		}
        		solutions.put(solution.getOwner().getUniqueId(),solution);
        		if (!solution.isCommited().booleanValue()) iLoadStudentEnrlsFromSolution = false;
        	}
        	for (int i=0;iLoadStudentEnrlsFromSolution && i<iSolverGroupId.length;i++)
        		if (!solutions.containsKey(iSolverGroupId[i]))
        			iLoadStudentEnrlsFromSolution = false;
        	getModel().getProperties().setProperty("General.Note",note);
            String solutionIdStr = "";
        	for (int i=0;i<iSolverGroupId.length;i++) {
        		Solution solution = (Solution)solutions.get(iSolverGroupId[i]);
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
			iProgress.fatal("No session loaded.");
			return;
		}
		iProgress.debug("session: "+iSession.getLabel());

		getModel().getProperties().setProperty("Data.Term",iSession.getAcademicYearTerm());
		getModel().getProperties().setProperty("Data.Initiative",iSession.getAcademicInitiative());
		iStartDay = DateUtils.getDayOfYear(iSession.getSessionBeginDateTime());
		iEndDay = DateUtils.getDayOfYear(iSession.getSessionEndDateTime());
		getModel().setYear(iSession.getYear());
		
		iAllClasses = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
		for (int i=0;i<iSolverGroup.length;i++) {
			for (Iterator j=iSolverGroup[i].getDepartments().iterator();j.hasNext();) {
	    		Department d = (Department)j.next();
	    		iAllClasses.addAll(d.getClassesFetchWithStructure());
	    	}
		}
		if (iAllClasses==null || iAllClasses.isEmpty()) {
			iProgress.fatal("No classes to load.");
			return;
		}
		iProgress.debug("classes to load: "+iAllClasses.size());
		
		for (Iterator i=iAllClasses.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			Hibernate.initialize(c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs());
		}
		for (Iterator i=iAllClasses.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			Hibernate.initialize(c.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts());
		}
		for (Iterator i=iAllClasses.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			Hibernate.initialize(c.getSchedulingSubpart().getClasses());
		}
		for (Iterator i=iAllClasses.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			Hibernate.initialize(c.getClassInstructors());
		}
		for (Iterator i=iAllClasses.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
				ClassInstructor ci = (ClassInstructor)j.next();
				Hibernate.initialize(ci.getInstructor());
			}
		}
		for (Iterator i=iAllClasses.iterator();i.hasNext();) {
			Class_ c = (Class_)i.next();
			Hibernate.initialize(c.getPreferences());
			Hibernate.initialize(c.getSchedulingSubpart().getPreferences());
			for (Iterator j=c.getClassInstructors().iterator();j.hasNext();) {
				ClassInstructor ci = (ClassInstructor)j.next();
				Hibernate.initialize(ci.getInstructor().getPreferences());
			}
			c.getControllingDept().getPreferences();
			c.getManagingDept().getPreferences();
		}
		
		iProgress.setPhase("Loading classes ...",iAllClasses.size());
		Hashtable subparts = new Hashtable();
		Hashtable offerings = new Hashtable();
		Hashtable configurations = new Hashtable();
		Hashtable altConfigurations = new Hashtable();
		Hashtable io2lectures = new Hashtable();
		int ord = 0;
		for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
			Class_ clazz = (Class_)i1.next();
			Lecture lecture = loadClass(clazz,hibSession);
			if (lecture!=null) 
				lecture.setOrd(ord++);
			iClasses.put(clazz.getUniqueId(),clazz);
			iProgress.incProgress();
		}
		
		loadInstructorAvailabilities(hibSession);
		
		loadRoomAvailabilities(hibSession);
		
		iProgress.setPhase("Setting parent classes ...",iLectures.size());
		for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
			Class_ clazz = (Class_)i1.next();
			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
			if (lecture==null) continue;

			Class_ parentClazz = clazz.getParentClass();
			if (parentClazz!=null) {
				Lecture parentLecture = null; Class_ c = clazz;
				while ((parentLecture==null || parentLecture.isCommitted()) && c.getParentClass()!=null) {
					c = c.getParentClass();
					parentLecture = (Lecture)iLectures.get(c.getUniqueId());
				}
				if (parentLecture!=null && !parentLecture.isCommitted()) lecture.setParent(parentLecture);
			}
			
			SchedulingSubpart subpart = clazz.getSchedulingSubpart();
			InstrOfferingConfig config = subpart.getInstrOfferingConfig();
			InstructionalOffering offering = config.getInstructionalOffering();
			
			HashSet lectures = (HashSet)io2lectures.get(offering.getUniqueId());
			if (lectures==null) {
				lectures = new HashSet();
				io2lectures.put(offering.getUniqueId(), lectures);
			}
			lectures.add(lecture);
			
			iSubparts.put(subpart.getUniqueId(),subpart);

			if (lecture.getParent()==null) {
				Configuration cfg = (Configuration)configurations.get(config);
				if (cfg==null) {
					cfg = new Configuration(offering.getUniqueId(), config.getUniqueId(), config.getLimit().intValue());
					configurations.put(config, cfg);
					Vector altCfgs = (Vector)altConfigurations.get(offering);
					if (altCfgs==null) {
						altCfgs = new FastVector();
						altConfigurations.put(offering, altCfgs);
					}
					altCfgs.addElement(cfg);
					cfg.setAltConfigurations(altCfgs);
				}

				lecture.setConfiguration(cfg);
			
				Hashtable topSubparts = (Hashtable)offerings.get(offering);
				
				if (topSubparts==null) {
					topSubparts = new Hashtable();
					offerings.put(offering,topSubparts);
				}
				HashSet topSubpartsThisConfig = (HashSet)topSubparts.get(config);
				if (topSubpartsThisConfig==null) {
					topSubpartsThisConfig = new HashSet();
					topSubparts.put(config, topSubpartsThisConfig);
				}

				topSubpartsThisConfig.add(clazz.getSchedulingSubpart());
			}
			
			Vector sameSubpart = (Vector)subparts.get(clazz.getSchedulingSubpart());
			if (sameSubpart==null) {
				sameSubpart = new FastVector();
				subparts.put(clazz.getSchedulingSubpart(), sameSubpart);
			}
			sameSubpart.addElement(lecture);
			
			lecture.setSameSubpartLectures(sameSubpart);
			
			iProgress.incProgress();
		}

		Vector distPrefs = new Vector();
		for (int i=0;i<iSolverGroup.length;i++) {
			distPrefs.addAll(iSolverGroup[i].getDistributionPreferences());
		}
		iProgress.setPhase("Loading distribution preferences ...",distPrefs.size());
		for (Iterator i=distPrefs.iterator();i.hasNext();) {
			DistributionPref distributionPref = (DistributionPref)i.next();
			Hibernate.initialize(distributionPref.getDistributionObjects());
		}
		for (Iterator i=distPrefs.iterator();i.hasNext();) {
			DistributionPref distributionPref = (DistributionPref)i.next();
			if (!PreferenceLevel.sNeutral.equals(distributionPref.getPrefLevel().getPrefProlog()))
				loadGroupConstraint(distributionPref);
			iProgress.incProgress();
		}
		
		for (int i=0;i<iSolverGroup.length;i++) {
			for (Iterator j=iSolverGroup[i].getDepartments().iterator();j.hasNext();) {
				loadInstructorGroupConstraints((Department)j.next(), hibSession);
			}
		}
		
		if (iAutoSameStudents) {
			iProgress.setPhase("Posting automatic same_students constraints ...",iAllClasses.size());
    		for (Iterator i1=iAllClasses.iterator();i1.hasNext();) {
    			Class_ clazz = (Class_)i1.next();
    			Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    			if (lecture==null) continue;
    			
    			if (!lecture.hasAnyChildren())
    				postSameStudentConstraint(clazz, iAutoSameStudentsConstraint);
    			
    			iProgress.incProgress();
    		}
		}
		
		assignCommited();
		
    	iProgress.setPhase("Posting class limit constraints ...", offerings.size());
    	for (Iterator i1=offerings.entrySet().iterator();i1.hasNext();) {
    		Map.Entry entry = (Map.Entry)i1.next();
    		InstructionalOffering offering = (InstructionalOffering)entry.getKey();
    		Hashtable topSubparts = (Hashtable)entry.getValue();
    		for (Iterator i2=topSubparts.entrySet().iterator();i2.hasNext();) {
    			Map.Entry subpartEntry = (Map.Entry)i2.next();
    			InstrOfferingConfig config = (InstrOfferingConfig)subpartEntry.getKey();
    			Set topSubpartsThisConfig = (Set)subpartEntry.getValue();
    			for (Iterator i3=topSubpartsThisConfig.iterator();i3.hasNext();) {
        			SchedulingSubpart subpart = (SchedulingSubpart)i3.next();
        			Vector lectures = (Vector)subparts.get(subpart);
    			
        			boolean isMakingSense = false;
        			for (Enumeration e4=lectures.elements();e4.hasMoreElements();) {
        				Lecture lecture = (Lecture)e4.nextElement();
        				if (lecture.minClassLimit()!=lecture.maxClassLimit()) {
        					isMakingSense=true; break;
        				}
        			}
        			
        			if (!isMakingSense) continue;
        			
        			if (subpart.getParentSubpart()==null) {
            			ClassLimitConstraint clc = new ClassLimitConstraint(config.getLimit().intValue(), getClassLimitConstraitName(subpart));
            			
            			for (Enumeration e4=lectures.elements();e4.hasMoreElements();) {
            				Lecture lecture = (Lecture)e4.nextElement();
            				clc.addVariable(lecture);
            				createChildrenClassLimitConstraits(lecture);
            			}
            			
            			for (Iterator i4=subpart.getClasses().iterator();i4.hasNext();) {
            				Class_ clazz = (Class_)i4.next();
            				if (iLectures.get(clazz.getUniqueId())==null)
            					clc.setClassLimitDelta(clc.getClassLimitDelta()-clazz.getClassLimit());
            			}
            			
           	    		iProgress.trace("Added constraint "+clc.getName()+" between "+clc.variables());
           	    		getModel().addConstraint(clc);
        			} else {
        				Hashtable clcs = new Hashtable();
        				
            			for (Enumeration e4=lectures.elements();e4.hasMoreElements();) {
            				Lecture lecture = (Lecture)e4.nextElement();
            				Class_ clazz = (Class_)iClasses.get(lecture.getClassId());
            				Class_ parentClazz = clazz.getParentClass();
            				
            				ClassLimitConstraint clc = (ClassLimitConstraint)clcs.get(parentClazz.getUniqueId());
            				if (clc==null) {
            					clc = new ClassLimitConstraint(parentClazz.getClassLimit(), parentClazz.getClassLabel());
            					clcs.put(parentClazz.getUniqueId(), clc);
            				}
            				
            				clc.addVariable(lecture);
            				createChildrenClassLimitConstraits(lecture);
            			}
            			
            			for (Iterator i4=subpart.getClasses().iterator();i4.hasNext();) {
            				Class_ clazz = (Class_)i4.next();
            				
            				if (iLectures.get(clazz.getUniqueId())==null) {
            					Class_ parentClazz = clazz.getParentClass();
            					ClassLimitConstraint clc = (ClassLimitConstraint)clcs.get(parentClazz.getUniqueId());
            					if (clc!=null)
            						clc.setClassLimitDelta(clc.getClassLimitDelta()-clazz.getClassLimit());
            				}
            			}
            			
            			for (Iterator i4=clcs.values().iterator();i4.hasNext();) {
            				ClassLimitConstraint clc = (ClassLimitConstraint)i4.next();
            				iProgress.trace("Added constraint "+clc.getName()+" between "+clc.variables());
            				getModel().addConstraint(clc);
            			}
        			}
        			
    			}
    		}
    		iProgress.incProgress();
    	}
		

		Hashtable demands = new Hashtable();
		Hashtable demandsPermId = new Hashtable();
		StringBuffer subjectAreas = new StringBuffer();
		for (int i=0;i<iSolverGroup.length;i++) {
			for (Iterator j=iSolverGroup[i].getDepartments().iterator();j.hasNext();) {
				Department dept = (Department)j.next();
				for (Iterator k=dept.getSubjectAreas().iterator();k.hasNext();) {
					SubjectArea sa = (SubjectArea)k.next();
					if (subjectAreas.length()>0)
						subjectAreas.append(",");
					subjectAreas.append(sa.getUniqueId());
					demands.put(sa.getUniqueId(),new Hashtable());
				}
			}
		}
			
		if (subjectAreas.length()>0) {
			Query q = hibSession.createQuery("select d.subjectArea.uniqueId, d.courseNbr, d.student.uniqueId, d.coursePermId "+
					"from LastLikeCourseDemand d where d.subjectArea.uniqueId in ("+subjectAreas+")");
			for (Iterator i=q.iterate();i.hasNext();) {
				Object[] d = (Object[])i.next();
				Long subjectAreaId = (Long)d[0];
				String courseNbr = (String)d[1];
				Long studentId = (Long)d[2];
				String coursePermId = (String)d[3];
				
				Hashtable demandsThisSubjArea = (Hashtable)demands.get(subjectAreaId);
				HashSet studentIds = (HashSet)demandsThisSubjArea.get(courseNbr);
				if (studentIds==null) {
					studentIds = new HashSet();
					demandsThisSubjArea.put(courseNbr, studentIds);
				}
				studentIds.add(studentId);
				
				if (coursePermId!=null) {
				    studentIds = (HashSet)demandsPermId.get(coursePermId);
	                if (studentIds==null) {
	                    studentIds = new HashSet();
	                    demandsPermId.put(coursePermId, studentIds);
	                }
	                studentIds.add(studentId);
				}
			}
		}			
			
    	iProgress.setPhase("Loading students ...",offerings.size());
    	Hashtable offering2students = new Hashtable();
    	for (Enumeration e1=offerings.keys();e1.hasMoreElements();) {
    		InstructionalOffering offering = (InstructionalOffering)e1.nextElement();
    		
    		int totalCourseLimit = 0;
    		
    		for (Iterator i2=offering.getCourseOfferings().iterator();i2.hasNext();) {
        		CourseOffering course = (CourseOffering)i2.next();

        		int courseLimit = -1;
        		for (Iterator i3=offering.getCourseReservations().iterator();i3.hasNext();) {
        			CourseOfferingReservation r = (CourseOfferingReservation)i3.next();
        			if (r.getCourseOffering().equals(course))
        				courseLimit = r.getReserved().intValue();
        		}
        		if (courseLimit<0) {
        			if (offering.getCourseOfferings().size()==1)
        				courseLimit = offering.getLimit().intValue();
        			else {
        				iProgress.warn("Cross-listed course "+getOfferingLabel(course)+" does not have any course reservation.");
        				courseLimit = course.getDemand().intValue() + (course.getDemandOffering()==null?0:course.getDemandOffering().getDemand().intValue());
        			}
        		}
        		
        		totalCourseLimit += courseLimit;
    		}
    		
    		Double factor = null;
    		
    		if (totalCourseLimit<offering.getLimit().intValue())
    			iProgress.warn("Total number of course reservations is below the offering limit for instructional offering "+getOfferingLabel(offering)+" ("+totalCourseLimit+"<"+offering.getLimit().intValue()+").");

    		if (totalCourseLimit>offering.getLimit().intValue())
    			iProgress.warn("Total number of course reservations exceeds the offering limit for instructional offering "+getOfferingLabel(offering)+" ("+totalCourseLimit+">"+offering.getLimit().intValue()+").");
    		
    		if (totalCourseLimit==0) continue;
    		
    		if (totalCourseLimit!=offering.getLimit().intValue())
    			factor = new Double(((double)offering.getLimit().intValue())/totalCourseLimit);
    		
            Hashtable computedCourseReservations = new Hashtable();
            Hashtable course2students = new Hashtable();
    		
        	for (Iterator i2=offering.getCourseOfferings().iterator();i2.hasNext();) {
        		CourseOffering course = (CourseOffering)i2.next();

        		Set studentIds = null;
                Hashtable demandsThisSubjArea = (Hashtable)demands.get(course.getSubjectArea().getUniqueId());
                if (demandsThisSubjArea!=null) {
                    if (course.getPermId()!=null && demandsPermId.containsKey(course.getPermId()))
                        studentIds = (Set)demandsPermId.get(course.getPermId());
                    else
                        studentIds = (Set)demandsThisSubjArea.get(course.getCourseNbr());
                } else {
                    List courseDemands = course.getCourseOfferingDemands();
                    if (!courseDemands.isEmpty()) {
                        studentIds = new HashSet(courseDemands.size());
                        for (Iterator i3=courseDemands.iterator();i3.hasNext();) {
                            LastLikeCourseDemand demand = (LastLikeCourseDemand)i3.next();
                            studentIds.add(demand.getStudent().getUniqueId());
                        }
                    }
                }
        		if (course.getDemandOffering()!=null) {
                    demandsThisSubjArea = (Hashtable)demands.get(course.getDemandOffering().getSubjectArea().getUniqueId());
                    if (demandsThisSubjArea!=null) {
                        if (studentIds==null) studentIds = new HashSet();
                        if (course.getDemandOffering().getPermId()!=null && demandsPermId.containsKey(course.getDemandOffering().getPermId())) {
                            studentIds.addAll((Set)demandsPermId.get(course.getDemandOffering().getPermId()));
                        } else {
                            studentIds.addAll((Set)demandsThisSubjArea.get(course.getDemandOffering().getCourseNbr()));
                        }
                    } else {
                        List courseDemands = course.getDemandOffering().getCourseOfferingDemands();
                        if (!courseDemands.isEmpty()) {
                            if (studentIds==null)
                                studentIds = new HashSet(courseDemands.size());
                            for (Iterator i3=courseDemands.iterator();i3.hasNext();) {
                                LastLikeCourseDemand demand = (LastLikeCourseDemand)i3.next();
                                studentIds.add(demand.getStudent().getUniqueId());
                            }
                        }
                    }
        		}
        		
        		int courseLimit = -1;
        		for (Iterator i3=offering.getCourseReservations().iterator();i3.hasNext();) {
        			CourseOfferingReservation r = (CourseOfferingReservation)i3.next();
        			if (r.getCourseOffering().equals(course))
        				courseLimit = r.getReserved().intValue();
        		}
        		if (courseLimit<0) {
        			if (offering.getCourseOfferings().size()==1)
        				courseLimit = offering.getLimit().intValue();
        			else {
        				courseLimit = studentIds.size();
        			}
        		}
        		
        		if (factor!=null)
        			courseLimit = (int)Math.round(courseLimit*factor.doubleValue());
        		
        		if (studentIds==null || studentIds.isEmpty()) {
        			iProgress.info("No student enrollments for offering "+getOfferingLabel(course)+".");
        			continue;
        		}
        		
        		if (courseLimit==0) {
        			iProgress.warn("No reserved space for students of offering "+getOfferingLabel(course)+".");
        		}
        		
        		double weight = (!iWeighStudents || courseLimit==0?1.0:(double)courseLimit / studentIds.size());
        		
        		Set cannotAttendLectures = null;
        		
        		if (offering.getCourseOfferings().size()>1) {
            		HashSet reservedClasses = new HashSet(
        				hibSession.
        				createQuery("select distinct r.owner from CourseOfferingReservation r "+
        						"where r.courseOffering.uniqueId=:courseId and r.ownerClassId='C'").
        				setLong("courseId", course.getUniqueId().longValue()).
        				list());

            		if (!reservedClasses.isEmpty()) {
        				iProgress.debug("Course requests for course "+getOfferingLabel(course)+" are "+reservedClasses);
        				cannotAttendLectures = computeCannotAttendLectures(reservedClasses);
        				iProgress.debug("Prohibited lectures for course "+getOfferingLabel(course)+" are "+cannotAttendLectures);
        				checkReservation(course, cannotAttendLectures, (Vector)altConfigurations.get(offering));
        			}
                    
                    Hashtable totalClassLimit = new Hashtable();
                    for (Iterator i=reservedClasses.iterator();i.hasNext();) {
                        Long classId = (Long)i.next();
                        Class_ clazz = (Class_)iClasses.get(classId);
                        if (clazz==null) clazz = (new Class_DAO()).get(classId);
                        if (clazz==null) continue;
                        Integer tcl = (Integer)totalClassLimit.get(clazz.getSchedulingSubpart());
                        totalClassLimit.put(clazz.getSchedulingSubpart(), new Integer((tcl==null?0:tcl.intValue())+clazz.getMaxExpectedCapacity().intValue()));
        		}
                    for (Iterator i=totalClassLimit.entrySet().iterator();i.hasNext();) {
                        Map.Entry entry = (Map.Entry)i.next();
                        SchedulingSubpart subpart = (SchedulingSubpart)entry.getKey();
                        int limit = ((Integer)entry.getValue()).intValue();
                        if (courseLimit>=limit) {
                            if (courseLimit>limit)
                                iProgress.warn("Too little space reserved in "+getSubpartLabel(subpart)+" for course "+getOfferingLabel(course)+" ("+limit+"<"+courseLimit+").");
                            for (Iterator j=offering.getCourseOfferings().iterator();j.hasNext();) {
                                CourseOffering co = (CourseOffering)j.next();
                                if (co.equals(course)) continue;
                                Hashtable cannotAttendClasses = (Hashtable)computedCourseReservations.get(co);
                                if (cannotAttendClasses==null) {
                                    cannotAttendClasses = new Hashtable();
                                    computedCourseReservations.put(co, cannotAttendClasses);
                                }
                                HashSet cannotAttendClassesThisSubpart = (HashSet)cannotAttendClasses.get(subpart);
                                if (cannotAttendClassesThisSubpart==null) {
                                    cannotAttendClassesThisSubpart = new HashSet();
                                    cannotAttendClasses.put(subpart, cannotAttendClassesThisSubpart);
                                }
                                for (Iterator k=reservedClasses.iterator();k.hasNext();) {
                                    Long classId = (Long)k.next();
                                    Class_ clazz = (Class_)iClasses.get(classId);
                                    if (clazz==null) clazz = (new Class_DAO()).get(classId);
                                    if (clazz==null) continue;
                                    if (clazz.getSchedulingSubpart().equals(subpart))
                                        cannotAttendClassesThisSubpart.add(clazz);
                                }
                            }
                        }
                    }
        		}

    			for (Iterator i3=studentIds.iterator();i3.hasNext();) {
    				Long studentId = (Long)i3.next();
        			Student student = (Student)iStudents.get(studentId);
        			if (student==null) {
        				student = new Student(studentId);
        				getModel().addStudent(student);
        				iStudents.put(studentId, student);
        			}
        			student.addOffering(offering.getUniqueId(), weight);
        			Set students = (Set)offering2students.get(offering);
        			if (students==null) {
        				students = new HashSet();
        				offering2students.put(offering,students);
        			}
        			students.add(student);
        			
                    Set cstudents = (Set)course2students.get(course);
                    if (cstudents==null) {
                        cstudents = new HashSet();
                        course2students.put(course,cstudents);
                    }
                    cstudents.add(student);

        			student.addCanNotEnroll(offering.getUniqueId(), cannotAttendLectures);
        		}
        	}
    		
            for (Iterator i1=computedCourseReservations.entrySet().iterator();i1.hasNext();) {
                Map.Entry entry1 = (Map.Entry)i1.next();
                CourseOffering course = (CourseOffering)entry1.getKey();
                Hashtable cannotAttendClasses = (Hashtable)entry1.getValue();
                HashSet reservedClasses = new HashSet();
                for (Iterator i2=cannotAttendClasses.entrySet().iterator();i2.hasNext();) {
                    Map.Entry entry2 = (Map.Entry)i2.next();
                    SchedulingSubpart subpart = (SchedulingSubpart)entry2.getKey();
                    HashSet cannotAttendClassesThisSubpart = (HashSet)entry2.getValue();
                    for (Iterator i3=subpart.getClasses().iterator();i3.hasNext();) {
                        Class_ clazz = (Class_) i3.next();
                        if (cannotAttendClassesThisSubpart.contains(clazz)) continue;
                        reservedClasses.add(clazz.getUniqueId());
                    }
                }
                if (!reservedClasses.isEmpty()) {
                    Set students = (Set)course2students.get(course);
                    if (students==null || students.isEmpty()) continue;
                    iProgress.debug("Course requests for course "+getOfferingLabel(course)+" are "+reservedClasses);
                    Set cannotAttendLectures = computeCannotAttendLectures(reservedClasses);
                    if (cannotAttendLectures.isEmpty()) continue;
                    boolean first = true;
                    for (Iterator i2=students.iterator();i2.hasNext();) {
                        Student student = (Student)i2.next();
                        student.addCanNotEnroll(offering.getUniqueId(), cannotAttendLectures);
                        if (first) {
                            Set allCannotAttendLectures = (Set)student.canNotEnrollSections().get(offering.getUniqueId());
                            iProgress.debug("Prohibited lectures for course "+getOfferingLabel(course)+" are "+allCannotAttendLectures);
                            checkReservation(course, allCannotAttendLectures, (Vector)altConfigurations.get(offering));
                        }
                        first = false;
                    }
                }
            }
            
    		
        	iProgress.incProgress();
    	}
    	iProgress.debug(iStudents.size()+" students loaded.");
    	
    	if (!hibSession.isOpen())
    		iProgress.fatal("Hibernate session not open.");

    	loadCommittedStudentConflicts(hibSession);
    	
    	if (!hibSession.isOpen())
    		iProgress.fatal("Hibernate session not open.");

    	if (solutions!=null && iLoadStudentEnrlsFromSolution) {
        	for (int idx=0;idx<iSolverGroupId.length;idx++) {
        		Solution solution = (Solution)solutions.get(iSolverGroupId[idx]);
        		List studentEnrls = hibSession
        			.createQuery("select distinct e.studentId, e.clazz.uniqueId from StudentEnrollment e where e.solution.uniqueId=:solutionId")
        			.setInteger("solutionId", solution.getUniqueId().intValue())
        			.list();
        		iProgress.setPhase("Loading student enrolments ["+(idx+1)+"] ...",studentEnrls.size());
        		Hashtable subpart2students = new Hashtable();
            	for (Iterator i1=studentEnrls.iterator();i1.hasNext();) {
            		Object o[] = (Object[])i1.next();
            		Long studentId = (Long)o[0];
            		Long clazzId = (Long)o[1];
            		
                    Student student = (Student)iStudents.get(studentId);
                    if (student==null) continue;
                    
                    Lecture lecture = (Lecture)iLectures.get(clazzId);
                    if (lecture!=null) {
                    	if (student.hasOffering(lecture.getConfiguration().getOfferingId()) && student.canEnroll(lecture)) {
                    		student.addLecture(lecture);
                    		lecture.addStudent(student);
                    	}
                    }
                    
                    iProgress.incProgress();
            	}
        	}
    	}
    	
    	if (!hibSession.isOpen())
    		iProgress.fatal("Hibernate session not open.");
    	
        if (hasRoomAvailability()) loadRoomAvailability(RoomAvailability.getInstance());

        if (!hibSession.isOpen())
            iProgress.fatal("Hibernate session not open.");

    	iProgress.setPhase("Initial sectioning ...", offerings.size());
        for (Enumeration e1=offerings.keys();e1.hasMoreElements();) {
    		InstructionalOffering offering = (InstructionalOffering)e1.nextElement();
    		Set students = (Set)offering2students.get(offering);
    		if (students==null) continue;
    		
    		InitialSectioning.initialSectioningCfg(iProgress, offering.getUniqueId(), offering.getCourseName(), students, (Vector)altConfigurations.get(offering));
    		
    		/*
    		Hashtable topSubparts = (Hashtable)entry.getValue();
    		for (Iterator i2=topSubparts.entrySet().iterator();i2.hasNext();) {
    			Map.Entry subpartEntry = (Map.Entry)i2.next();
    			InstrOfferingConfig config = (InstrOfferingConfig)subpartEntry.getKey();
    			Set topSubpartsThisConfig = (Set)subpartEntry.getValue();
    			for (Iterator i3=topSubpartsThisConfig.iterator();i3.hasNext();) {
        			SchedulingSubpart subpart = (SchedulingSubpart)i3.next();
        			Vector lectures = (Vector)subparts.get(subpart);
        			initialSectioning(offering.getUniqueId(), offering.getCourseName(),students,lectures);
    			}
    		}
    		*/
    		
    		iProgress.incProgress();
    	}
    	for (Enumeration e=iStudents.elements();e.hasMoreElements();) {
    		((Student)e.nextElement()).clearDistanceCache();
    	}
        
    	if (!hibSession.isOpen())
    		iProgress.fatal("Hibernate session not open.");

    	iProgress.setPhase("Computing jenrl ...",iStudents.size());
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
                        jenrl.addVariable(l1);
                        jenrl.addVariable(l2);
                        getModel().addConstraint(jenrl);
                        x.put(l2, jenrl);
                    }
                    jenrl.incJenrl(st);
                }
            }
            iProgress.incProgress();
        }
        
    	if (!hibSession.isOpen())
    		iProgress.fatal("Hibernate session not open.");

    	if (solutions!=null) {
        	for (int idx=0;idx<iSolverGroupId.length;idx++) {
        		Solution solution = (Solution)solutions.get(iSolverGroupId[idx]);
        		if (solution==null) continue;
            	iProgress.setPhase("Creating initial assignment ["+(idx+1)+"] ...",solution.getAssignments().size());
            	for (Iterator i1=solution.getAssignments().iterator();i1.hasNext();) {
            		Assignment assignment = (Assignment)i1.next();
            		loadAssignment(assignment);
            		iProgress.incProgress();
            	}
        	}
        }
        
    	if (!hibSession.isOpen())
    		iProgress.fatal("Hibernate session not open.");

    	if (iSpread) {
    		iProgress.setPhase("Posting automatic spread constraints ...",subparts.size());
    		for (Enumeration e1=subparts.keys();e1.hasMoreElements();) {
    			SchedulingSubpart subpart = (SchedulingSubpart)e1.nextElement();
    			if (subpart.getClasses().size()<=1) {
    				iProgress.incProgress();
    				continue;
    			}
    			if (!subpart.isAutoSpreadInTime().booleanValue()) {
    				iProgress.debug("Automatic spread constraint disabled for "+getSubpartLabel(subpart));
    				iProgress.incProgress();
    				continue;
    			}
    			/*
    			if (subpart.getClasses().size()<=5) {
    				GroupConstraint gc = new GroupConstraint(new Long(-1),GroupConstraint.TYPE_DIFF_TIME,PreferenceLevel.sStronglyPreferred);
    				for (Iterator i2=subpart.getClasses().iterator();i2.hasNext();) {
    					Class_ clazz = (Class_)i2.next();
    					Lecture lecture = (Lecture)iLectures.get(clazz.getUniqueId());
    					if (lecture==null) continue;
    					gc.addVariable(lecture);
    				}
    				getModel().addConstraint(gc);
    			} else {
    			*/
    				SpreadConstraint spread = new SpreadConstraint(getModel().getProperties(),subpart.getCourseName()+" "+subpart.getItypeDesc().trim());
    				for (Iterator i2=subpart.getClasses().iterator();i2.hasNext();) {
    					Class_ clazz = (Class_)i2.next();
    					Lecture lecture = (Lecture)getLecture(clazz);
    					if (lecture==null) continue;
    					spread.addVariable(lecture);
    				}
    				if (spread.variables().isEmpty())
    					iProgress.warn("No class for course "+getSubpartLabel(subpart));
    				else
    					getModel().addConstraint(spread);
    			/*
    			}
    			*/
    			//iProgress.trace("Constraint "+spread+" added.");
    			iProgress.incProgress();
    		}
		}
		
		if (iDeptBalancing) {
        	iProgress.setPhase("Creating dept. spread constraints ...",getModel().variables().size());
            Hashtable depSpreadConstraints = new Hashtable();
            for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
                Lecture lecture = (Lecture)e.nextElement();
                if (lecture.getDepartment()==null) continue;
                DepartmentSpreadConstraint deptConstr = (DepartmentSpreadConstraint)depSpreadConstraints.get(lecture.getDepartment());
                if (deptConstr==null) {
                    deptConstr = new DepartmentSpreadConstraint(getModel().getProperties(),lecture.getDepartment(),(String)iDeptNames.get(lecture.getDepartment()));
                    depSpreadConstraints.put(lecture.getDepartment(),deptConstr);
                    getModel().addConstraint(deptConstr);
                }
                deptConstr.addVariable(lecture);
                iProgress.incProgress();
            }
        }
		
		purgeInvalidValues();
		
		for (Enumeration e=getModel().constraints().elements();e.hasMoreElements();) {
			Constraint c = (Constraint)e.nextElement();
			if (c instanceof SpreadConstraint)
				((SpreadConstraint)c).init();
			if (c instanceof DiscouragedRoomConstraint)
				((DiscouragedRoomConstraint)c).setEnabled(true);
			if (c instanceof MinimizeNumberOfUsedRoomsConstraint)
				((MinimizeNumberOfUsedRoomsConstraint)c).setEnabled(true);
			if (c instanceof MinimizeNumberOfUsedGroupsOfTime)
				((MinimizeNumberOfUsedGroupsOfTime)c).setEnabled(true);
		}
		
		iProgress.setPhase("Checking for inconsistencies...", getModel().variables().size());
		for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
			Lecture lecture = (Lecture)e.nextElement();
			
			iProgress.incProgress();
    		for (Iterator i=lecture.students().iterator();i.hasNext();) {
    			Student s = (Student)i.next();
    			if (!s.canEnroll(lecture))
    				iProgress.info("Invalid student enrollment of student "+s.getId()+" in class "+getClassLabel(lecture)+" found.");
    		}
    		
    		//check same instructor constraint
    		if (!lecture.values().isEmpty() && lecture.timeLocations().size()==1 && !lecture.getInstructorConstraints().isEmpty()) {
        		for (Enumeration f=getModel().variables().elements();f.hasMoreElements();) {
        			Lecture other = (Lecture)f.nextElement();
        			if (other.values().isEmpty() || other.timeLocations().size()!=1 || lecture.getClassId().compareTo(other.getClassId())<=0) continue;
        			Placement p1 = (Placement)lecture.values().firstElement();
        			Placement p2 = (Placement)other.values().firstElement();
        			if (!other.getInstructorConstraints().isEmpty()) {
        	           	for (Enumeration g=lecture.getInstructorConstraints().elements();g.hasMoreElements();) {
        	           		InstructorConstraint ic = (InstructorConstraint)g.nextElement();
        	           		if (!other.getInstructorConstraints().contains(ic)) continue;
        	           		if (p1.canShareRooms(p2) && p1.sameRooms(p2)) continue;
        	           		if (p1.getTimeLocation().hasIntersection(p2.getTimeLocation())) {
        	           			iProgress.warn("Same instructor and overlapping time required:"+
        	           					"<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(lecture)+" &larr; "+p1.getLongName()+
        	           					"<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(other)+" &larr; "+p2.getLongName());
        	           		} else if (ic.getDistancePreference(p1,p2)==PreferenceLevel.sIntLevelProhibited && lecture.roomLocations().size()==1 && other.roomLocations().size()==1) {
        	           			iProgress.warn("Same instructor, back-to-back time and rooms too far (distance="+Math.round(10.0*Placement.getDistance(p1,p2))+"m) required:"+
        	           					"<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(lecture)+" &larr; "+p1.getLongName()+
        	           					"<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(other)+" &larr; "+p2.getLongName());
        	           		}
        				}
        			}
        		}    			
    		}
    		
			if (!lecture.isSingleton()) continue;
    		for (Enumeration f=getModel().variables().elements();f.hasMoreElements();) {
    			Lecture other = (Lecture)f.nextElement();
    			if (!other.isSingleton() || lecture.getClassId().compareTo(other.getClassId())<=0) continue;
    			Placement p1 = (Placement)lecture.values().firstElement();
    			Placement p2 = (Placement)other.values().firstElement();
    			if (p1.shareRooms(p2) && p1.getTimeLocation().hasIntersection(p2.getTimeLocation()) && !p1.canShareRooms(p2)) {
    				iProgress.warn("Same room and overlapping time required:"+
    						"<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(lecture)+" &larr; "+p1.getLongName()+
    						"<br>&nbsp;&nbsp;&nbsp;&nbsp;"+getClassLabel(other)+" &larr; "+p2.getLongName());
    			}
    		}
    		if (lecture.getAssignment()==null) {
    			Placement placement = (Placement)lecture.values().firstElement();
    			if (!placement.isValid()) {
    				String reason = "";
    	           	for (Enumeration g=lecture.getInstructorConstraints().elements();g.hasMoreElements();) {
    	           		InstructorConstraint ic = (InstructorConstraint)g.nextElement();
    	           		if (!ic.isAvailable(lecture, placement))
    	           			reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;instructor "+ic.getName()+" not available";
    	           	}
    		    	if (lecture.getNrRooms()>0) {
    		    		if (placement.isMultiRoom()) {
    		    			for (Enumeration f=placement.getRoomLocations().elements();f.hasMoreElements();) {
    		    				RoomLocation roomLocation = (RoomLocation)f.nextElement();
    		    				if (!roomLocation.getRoomConstraint().isAvailable(lecture,placement.getTimeLocation(),lecture.getScheduler()))
    		    					reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;room "+roomLocation.getName()+" not available";
    		    			}
    		    		} else {
    						if (!placement.getRoomLocation().getRoomConstraint().isAvailable(lecture,placement.getTimeLocation(),lecture.getScheduler()))
    							reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;room "+placement.getRoomLocation().getName()+" not available";
    		    		}
    		    	}
    	            Hashtable conflictConstraints = getModel().conflictConstraints(placement);
    	            if (!conflictConstraints.isEmpty()) {
    	                for (Enumeration ex=conflictConstraints.keys();ex.hasMoreElements();) {
    	                    Constraint c = (Constraint)ex.nextElement();
    	                    Collection vals = (Collection)conflictConstraints.get(c);
    	                    for (Iterator i=vals.iterator();i.hasNext();) {
    	                        Placement p = (Placement) i.next();
    	                        Lecture l = (Lecture)p.variable();
    	                        if (l.isCommitted())
    	                        	reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;conflict with committed assignment "+getClassLabel(l)+" = "+p.getLongName()+" (in constraint "+c+")";
    	                        if (p.equals(placement))
    	                        	reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;constraint "+c;
    	                    }
    	                }
    	            }
    		    	iProgress.warn("Class "+getClassLabel(lecture)+" requires an invalid placement "+placement.getLongName()+(reason.length()==0?".":":"+reason));
    			} else if (iAssignSingleton && getModel().conflictValues(placement).isEmpty())
    				lecture.assign(0, placement);
    		}
		}

		new EnrollmentCheck(getModel()).checkStudentEnrollments(iProgress);
		
		if (!getModel().assignedVariables().isEmpty() && !iLoadStudentEnrlsFromSolution)
			getModel().switchStudents();
		
 		iProgress.setPhase("Done",1);iProgress.incProgress();            
		iProgress.info("Model successfully loaded.");
    }
    
    public static class ObjectsByGivenOrderComparator implements Comparator {
    	Vector iOrderedSet = null;
    	public ObjectsByGivenOrderComparator(Vector orderedSetOfLectures) {
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
    
    public boolean isRemote() { return RemoteSolverServer.getServerThread()!=null; }
    
    public boolean hasRoomAvailability() {
        try {
            if (isRemote()) {
                return (Boolean)RemoteSolverServer.query(new Object[]{"hasRoomAvailability"});
            } else return RoomAvailability.getInstance()!=null;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.warn("Unable to access room availability service, reason:"+e.getMessage());
            return false;
        }
    }
    
    public void roomAvailabilityActivate(Date startTime, Date endTime) {
        try {
            if (isRemote()) {
                RemoteSolverServer.query(new Object[]{"activateRoomAvailability",iSessionId,startTime,endTime});
            } else {
                RoomAvailability.getInstance().activate(new SessionDAO().get(iSessionId), startTime, endTime, 
                        "true".equals(ApplicationProperties.getProperty("tmtbl.room.availability.solver.waitForSync","true")));
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.warn("Unable to access room availability service, reason:"+e.getMessage());
        } 
    }
    
    public void loadRoomAvailability(RoomAvailabilityInterface ra) {
        Date startDate = null, endDate = null;
        for (Iterator i=iAllUsedDatePatterns.iterator();i.hasNext();) {
            DatePattern dp = (DatePattern)i.next();
            if (startDate == null || startDate.compareTo(dp.getStartDate())>0)
                startDate = dp.getStartDate();
            if (endDate == null || endDate.compareTo(dp.getEndDate())<0)
                endDate = dp.getEndDate();
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
        roomAvailabilityActivate(startDateCal.getTime(),endDateCal.getTime());
        iProgress.setPhase("Loading room availability...", iRooms.size());
        int firstDOY = iSession.getDayOfYear(1,iSession.getStartMonth());
        int lastDOY = iSession.getDayOfYear(0,iSession.getEndMonth()+1);
        int size = lastDOY - firstDOY;
        Calendar c = Calendar.getInstance(Locale.US);
        SimpleDateFormat df = new SimpleDateFormat("MM/dd");
        long id = 0;
        int sessionYear = iSession.getYear();
        for (Enumeration e=iRooms.elements();e.hasMoreElements();) {
            RoomConstraint room = (RoomConstraint)e.nextElement();
            iProgress.incProgress();
            Collection<TimeBlock> times = getRoomAvailability(room, startDateCal.getTime(),endDateCal.getTime());
            if (times==null) continue;
            for (TimeBlock time : times) {
                iProgress.debug(room.getName()+" not available due to "+time);
                int dayCode = 0;
                c.setTime(time.getStartTime());
                int m = c.get(Calendar.MONTH);
                int d = c.get(Calendar.DAY_OF_MONTH);
                if (c.get(Calendar.YEAR)<sessionYear) m-=12;
                if (c.get(Calendar.YEAR)>sessionYear) m+=12;
                BitSet weekCode = new BitSet(size);
                int offset = iSession.getDayOfYear(d,m) - firstDOY;
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
                int length = endSlot - startSlot;
                if (length<=0) continue;
                TimeLocation timeLocation = new TimeLocation(dayCode, startSlot, length, 0, 0, null, df.format(time.getStartTime()), weekCode, 0);
                Vector timeLocations = new Vector(1); timeLocations.addElement(timeLocation);
                RoomLocation roomLocation = new RoomLocation(room.getResourceId(), room.getName(), room.getBuildingId(), 0, room.getCapacity(), room.getPosX(), room.getPosY(),
                        room.getIgnoreTooFar(), room);
                Vector roomLocations = new Vector(1); roomLocations.add(roomLocation);
                Lecture lecture = new Lecture(
                        new Long(--id), null, null, time.getEventName(), 
                        timeLocations, roomLocations, 1, 
                        new Placement(null,timeLocation,roomLocations), 0, 0, 1.0);
                lecture.setNote(time.getEventType());
                Placement p = (Placement)lecture.getInitialAssignment();
                lecture.setBestAssignment(p);
                lecture.setCommitted(true);
                room.setNotAvailable(p);
                getModel().addVariable(p.variable());
            }
        }
    }
    
    public Collection<TimeBlock> getRoomAvailability(RoomConstraint room, Date startTime, Date endTime) {
        Collection<TimeBlock> ret = null;
        String ts = null;
        try {
            if (isRemote()) {
                ret = (Collection<TimeBlock>)RemoteSolverServer.query(new Object[]{"getClassRoomAvailability",room.getResourceId(),startTime,endTime});
                if (!iRoomAvailabilityTimeStampIsSet) ts = (String)RemoteSolverServer.query(new Object[]{"getRoomAvailabilityTimeStamp",startTime, endTime});
            } else {
                ret = RoomAvailability.getInstance().getRoomAvailability(
                        LocationDAO.getInstance().get(room.getResourceId()), startTime, endTime,
                        new String[] {RoomAvailabilityInterface.sClassType});
                if (!iRoomAvailabilityTimeStampIsSet) ts = RoomAvailability.getInstance().getTimeStamp(startTime, endTime);
            }
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            iProgress.warn("Unable to access room availability service, reason:"+e.getMessage());
        } 
        if (!iRoomAvailabilityTimeStampIsSet) {
            iRoomAvailabilityTimeStampIsSet = true;
            if (ts!=null) {
                getModel().getProperties().setProperty("RoomAvailability.TimeStamp", ts);
                iProgress.info("Using room availability that was updated on "+ts+".");
            } else {
                iProgress.error("Room availability is not available.");
            }
        }
        return ret;
    }
}
