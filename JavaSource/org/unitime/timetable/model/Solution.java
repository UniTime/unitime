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
package org.unitime.timetable.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.text.DecimalFormat;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.hibernate.query.Query;
import org.hibernate.SessionFactory;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.sectioning.PublishedSectioningSolutionsTable.InfoComparator;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.base.BaseSolution;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.DivSecAssignmentComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolutionInfoDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "solution")
public class Solution extends BaseSolution implements ClassAssignmentProxy {
	private static CourseMessages MSG = Localization.create(CourseMessages.class);
	private static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	private static final long serialVersionUID = 1L;
	private static Log sLog = LogFactory.getLog(Solution.class);
	public static DecimalFormat sSufixFormat = new DecimalFormat("000");
	public static boolean DEBUG = false;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Solution () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Solution (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public SolutionInfo getSolutionInfo(String name) {
		if ("GlobalInfo".equals(name)) return getGlobalInfo();
		return SolutionInfoDAO.getInstance().getSession().
			createQuery("select si from SolutionInfo si where si.definition.name=:name and si.solution.uniqueId=:solutionId", SolutionInfo.class).
			setParameter("name", name).
			setParameter("solutionId", getUniqueId()).
			uniqueResult();
	}

	public TimetableInfo getInfo(String name) {
		SolutionInfo sinfo = getSolutionInfo(name);
		if (sinfo==null) return null;
		return sinfo.getInfo();
	}
	
	
	public void uncommitSolution(org.hibernate.Session hibSession) {
		uncommitSolution(hibSession, null);
	}
	
	public void uncommitSolution(org.hibernate.Session hibSession, String sendNotificationPuid) {
		if (DEBUG) sLog.debug("uncommit["+getUniqueId()+","+getOwner().getName()+"] -------------------------------------------------------");
		setCommitDate(null);
		setCommited(Boolean.FALSE);

		hibSession.merge(this);
		
	    if (ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue()) {
			deleteObjects(hibSession,
		              "ClassEvent",
		              "select e.uniqueId from Solution s inner join s.assignments a, ClassEvent e where e.clazz=a.clazz and s.uniqueId=:solutionId");
	    } else {
			EventContact contact = (sendNotificationPuid == null ? null : EventContact.findByExternalUniqueId(sendNotificationPuid));
		    if (contact == null && sendNotificationPuid != null) {
		        TimetableManager manager = TimetableManager.findByExternalId(sendNotificationPuid);
		        if (manager != null) {
			        contact = new EventContact();
			        contact.setFirstName(manager.getFirstName());
		            contact.setMiddleName(manager.getMiddleName());
			        contact.setLastName(manager.getLastName());
			        contact.setExternalUniqueId(manager.getExternalUniqueId());
			        contact.setEmailAddress(manager.getEmailAddress());
			        hibSession.persist(contact);
		        }
		    }
		    
			Calendar cal = Calendar.getInstance(Locale.US);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date today = cal.getTime();
			
			List<ClassEvent> events = hibSession.createQuery(
					"select e from Solution s inner join s.assignments a, ClassEvent e where e.clazz=a.clazz and s.uniqueId=:solutionId", ClassEvent.class)
					.setParameter("solutionId", getUniqueId()).list();
			for (ClassEvent event: events) {
	        	for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); )
	        		if (!i.next().getMeetingDate().before(today)) i.remove();
	        	if (event.getMeetings().isEmpty()) {
	        		hibSession.remove(event);
	        	} else {
	    			if (event.getNotes() == null)
	    				event.setNotes(new HashSet<EventNote>());
					EventNote note = new EventNote();
					note.setEvent(event);
					note.setNoteType(EventNote.sEventNoteTypeDeletion);
					note.setTimeStamp(new Date());
					note.setUser(contact == null ? "System" : contact.getName());
					note.setUserId(sendNotificationPuid);
					note.setTextNote(MSG.classNoteUncommitted(getOwner().getName()));
					note.setMeetings(MSG.classMeetingsNotApplicable());
					event.getNotes().add(note);
	        		hibSession.merge(event);
	        	}
			}
	    }

//		removeDivSecNumbers(hibSession);
		
		if (sendNotificationPuid!=null) sendNotification(this, null, sendNotificationPuid, true, null);

		// Manually fix the Clazz_.committedAssignment cache.
		for (Assignment a: getAssignments())
			a.getClazz().setCommittedAssignment(null);
	}
	
	public boolean commitSolution(Vector messages, org.hibernate.Session hibSession) {
		return commitSolution(messages, hibSession, null);
	}

    public static boolean shareRooms(Assignment a1, Assignment a2) {
        if (!a1.getPlacement().sameRooms(a2.getPlacement())) return false;
        for (Iterator i=a1.getClazz().getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject distObj = (DistributionObject)i.next();
            DistributionPref dp = distObj.getDistributionPref();
            String ref = dp.getDistributionType().getReference();
            if (!"MEET_WITH".equals(ref) && !"MEET_WITH_WEEKS".equals(ref) && !"CAN_SHARE_ROOM".equals(ref)) continue;
            if (!PreferenceLevel.sRequired.equals(dp.getPrefLevel().getPrefProlog())) continue;
            for (Iterator j=dp.getDistributionObjects().iterator();j.hasNext();) {
                DistributionObject distObj2 = (DistributionObject)j.next();
                if (distObj2.getPrefGroup().equals(a2.getClazz()) || distObj2.getPrefGroup().equals(a2.getClazz().getSchedulingSubpart()))
                    return true;
            }
        }
        for (Iterator i=a1.getClazz().getSchedulingSubpart().getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject distObj = (DistributionObject)i.next();
            DistributionPref dp = distObj.getDistributionPref();
            String ref = dp.getDistributionType().getReference();
            if (!"MEET_WITH".equals(ref) && !"MEET_WITH_WEEKS".equals(ref) && !"CAN_SHARE_ROOM".equals(ref)) continue;
            if (!PreferenceLevel.sRequired.equals(dp.getPrefLevel().getPrefProlog())) continue;
            for (Iterator j=dp.getDistributionObjects().iterator();j.hasNext();) {
                DistributionObject distObj2 = (DistributionObject)j.next();
                if (distObj2.getPrefGroup().equals(a2.getClazz()) || distObj2.getPrefGroup().equals(a2.getClazz().getSchedulingSubpart()))
                    return true;
            }
        }
        for (Iterator i=a1.getInstructors().iterator();i.hasNext();) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            for (Iterator j=instr.getDistributionPreferences().iterator();j.hasNext();) {
                DistributionPref dp = (DistributionPref)j.next();
                String ref = dp.getDistributionType().getReference();
                if (!"MEET_WITH".equals(ref) && !"MEET_WITH_WEEKS".equals(ref) && !"CAN_SHARE_ROOM".equals(ref)) continue;
                if (!PreferenceLevel.sRequired.equals(dp.getPrefLevel().getPrefProlog())) continue;
                for (Iterator k=DepartmentalInstructor.getAllForInstructor(instr, instr.getDepartment().getSession().getUniqueId()).iterator();k.hasNext();) {
                    DepartmentalInstructor inst2 = (DepartmentalInstructor)k.next();
                    for (Iterator l=inst2.getClasses().iterator();l.hasNext();) {
                        ClassInstructor ci = (ClassInstructor)l.next();
                        if (ci.getClassInstructing().equals(a2.getClazz())) return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean commitSolution(List<String> messages, org.hibernate.Session hibSession, String sendNotificationPuid) {
    	return commitSolution(messages, hibSession, sendNotificationPuid, null);
    }

	public boolean commitSolution(List<String> messages, org.hibernate.Session hibSession, String sendNotificationPuid, Long[] ownerIds) {
		List<Solution> solutions = hibSession.createQuery("from Solution where owner.uniqueId = :ownerId", Solution.class).setParameter("ownerId", getOwner().getUniqueId()).list();
		Solution uncommittedSolution = null;
		for (Iterator i=solutions.iterator();i.hasNext();) {
			Solution s = (Solution)i.next();
			if (s.equals(this)) continue;
			if (s.isCommited().booleanValue()) {
				uncommittedSolution = s;
				s.uncommitSolution(hibSession, null);
			}
		}
		if (DEBUG) sLog.debug("commit["+getUniqueId()+","+getOwner().getName()+"] -------------------------------------------------------");
			
		boolean isOK = true;
		if (ApplicationProperty.CourseTimetablingCommitSkipChecking.isTrue()) {
			// skip conflict checking
		} else if (ownerIds != null && ownerIds.length > 1 && ownerIds.length <= 1000) {
			for (Object[] o: hibSession.createQuery(
					"select r, a1, a2 from Location r inner join r.assignments a1 inner join r.assignments a2 "+
					"where a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId not in :ownerIds and " +
					"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
					"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
					.setParameterList("ownerIds", ownerIds, Long.class)
					.setParameter("solutionId", getUniqueId())
					.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
					.list()) {
				Location room = (Location)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (!room.isIgnoreRoomCheck() && a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a, b)) {
					messages.add(MESSAGES.failedCommitRoomConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), room.getLabel()));
					isOK=false;
				}
			}
			for (Object[] o: hibSession.createQuery(
					"select r, a1, a2 from Room r inner join r.assignments a1 inner join r.parentRoom.assignments a2 "+
					"where a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId not in :ownerIds and " +
					"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
					"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
					.setParameterList("ownerIds", ownerIds, Long.class)
					.setParameter("solutionId", getUniqueId())
					.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
					.list()) {
				Location room = (Location)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (!room.isIgnoreRoomCheck() && a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a, b)) {
					messages.add(MESSAGES.failedCommitRoomConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), room.getLabel()));
					isOK=false;
				}
			}
			for (Object[] o: hibSession.createQuery(
					"select r, a1, a2 from Room r inner join r.parentRoom.assignments a1 inner join r.assignments a2 "+
					"where a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId not in :ownerIds and " +
					"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
					"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
					.setParameterList("ownerIds", ownerIds, Long.class)
					.setParameter("solutionId", getUniqueId())
					.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
					.list()) {
				Location room = (Location)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (!room.isIgnoreRoomCheck() && a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a, b)) {
					messages.add(MESSAGES.failedCommitRoomConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), room.getLabel()));
					isOK=false;
				}
			}
			
			for (Object[] o: hibSession.createQuery(
					"select i1, a1, a2 from ClassInstructor c1 inner join c1.instructor i1 inner join c1.classInstructing.assignments a1, " +
							"ClassInstructor c2 inner join c2.instructor i2 inner join c2.classInstructing.assignments a2 where c1.lead = true and c2.lead = true and " +
							"i1.department.solverGroup != i2.department.solverGroup and i2.department.session.uniqueId = :sessionId and " +
							"i1.externalUniqueId is not null and i1.externalUniqueId = i2.externalUniqueId and " + 
							"a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId not in :ownerIds and " +
							"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
							"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
							.setParameterList("ownerIds", ownerIds, Long.class)
							.setParameter("solutionId", getUniqueId())
							.setParameter("sessionId", getOwner().getSession().getUniqueId())
							.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
							.list()) {
				DepartmentalInstructor instructor = (DepartmentalInstructor)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a,b)) {
					messages.add(MESSAGES.failedCommitInstructorConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), instructor.nameLastNameFirst()));
					isOK=false;
				}
			}
		} else {
			for (Object[] o: hibSession.createQuery(
					"select r, a1, a2 from Location r inner join r.assignments a1 inner join r.assignments a2 "+
					"where a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId != :ownerId and " +
					"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
					"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
					.setParameter("ownerId", getOwner().getUniqueId())
					.setParameter("solutionId", getUniqueId())
					.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
					.list()) {
				Location room = (Location)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (!room.isIgnoreRoomCheck() && a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a, b)) {
					messages.add(MESSAGES.failedCommitRoomConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), room.getLabel()));
					isOK=false;
				}
			}
			for (Object[] o: hibSession.createQuery(
					"select r, a1, a2 from Room r inner join r.assignments a1 inner join r.parentRoom.assignments a2 "+
					"where a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId != :ownerId and " +
					"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
					"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
					.setParameter("ownerId", getOwner().getUniqueId())
					.setParameter("solutionId", getUniqueId())
					.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
					.list()) {
				Location room = (Location)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (!room.isIgnoreRoomCheck() && a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a, b)) {
					messages.add(MESSAGES.failedCommitRoomConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), room.getLabel()));
					isOK=false;
				}
			}
			for (Object[] o: hibSession.createQuery(
					"select r, a1, a2 from Room r inner join r.parentRoom.assignments a1 inner join r.assignments a2 "+
					"where a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId != :ownerId and " +
					"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
					"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
					.setParameter("ownerId", getOwner().getUniqueId())
					.setParameter("solutionId", getUniqueId())
					.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
					.list()) {
				Location room = (Location)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (!room.isIgnoreRoomCheck() && a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a, b)) {
					messages.add(MESSAGES.failedCommitRoomConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), room.getLabel()));
					isOK=false;
				}
			}
			
			for (Object[] o: hibSession.createQuery(
					"select i1, a1, a2 from ClassInstructor c1 inner join c1.instructor i1 inner join c1.classInstructing.assignments a1, " +
							"ClassInstructor c2 inner join c2.instructor i2 inner join c2.classInstructing.assignments a2 where c1.lead = true and c2.lead = true and " +
							"i1.department.solverGroup.uniqueId = :ownerId and i2.department.solverGroup.uniqueId != :ownerId and i2.department.session.uniqueId = :sessionId and " +
							"i1.externalUniqueId is not null and i1.externalUniqueId = i2.externalUniqueId and " + 
							"a1.solution.uniqueId = :solutionId and a2.solution.commited = true and a2.solution.owner.uniqueId != :ownerId and " +
							"bitand(a1.days, a2.days) > 0 and (a1.timePattern.type = :exactType or a2.timePattern.type = :exactType or " +
							"(a1.startSlot < a2.startSlot + a2.timePattern.slotsPerMtg and a2.startSlot < a1.startSlot + a1.timePattern.slotsPerMtg))", Object[].class)
							.setParameter("ownerId", getOwner().getUniqueId())
							.setParameter("solutionId", getUniqueId())
							.setParameter("sessionId", getOwner().getSession().getUniqueId())
							.setParameter("exactType", TimePattern.TimePatternType.ExactTime.ordinal())
							.list()) {
				DepartmentalInstructor instructor = (DepartmentalInstructor)o[0];
				Assignment a = (Assignment)o[1];
				Assignment b = (Assignment)o[2];
				if (a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a,b)) {
					messages.add(MESSAGES.failedCommitInstructorConflict(a.getClassName(), a.getTimeLocation().getName(CONSTANTS.useAmPm()), b.getClassName(), b.getTimeLocation().getName(CONSTANTS.useAmPm()), instructor.nameLastNameFirst()));
					isOK=false;
				}
			}
		}
		
		if (!isOK) {
			if (sendNotificationPuid!=null) sendNotification(uncommittedSolution, this, sendNotificationPuid, false, messages);
			return false;
		}
		
		// NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		SolverInfoDef defJenrlInfo = SolverInfoDef.findByName(hibSession,"JenrlInfo");
		Hashtable solverInfos = new Hashtable();
		AssignmentDAO adao = AssignmentDAO.getInstance(); 
		q = hibSession.createQuery(
				"select a.uniqueId, oa.uniqueId, count(*) from "+
				"Solution s inner join s.assignments a inner join s.studentEnrollments as e, "+
				"Solution os inner join os.assignments oa inner join os.studentEnrollments as oe "+
				"where "+
				"s.uniqueId=:solutionId and os.owner.session.uniqueId=:sessionId and os.owner.uniqueId!=:ownerId and "+
				"a.clazz=e.clazz and oa.clazz=oe.clazz and a.clazz.schedulingSubpart!=oa.clazz.schedulingSubpart and e.studentId=oe.studentId "+
				"group by a.uniqueId, oa.uniqueId");
		q.setParameter("ownerId", getOwner().getUniqueId());
		q.setParameter("solutionId", getUniqueId());
		q.setParameter("sessionId", getOwner().getSession().getUniqueId());
		Iterator otherAssignments = q.iterate();
		while (otherAssignments.hasNext()) {
			Object[] result = (Object[])otherAssignments.next();
			Assignment assignment = adao.get((Integer)result[0],hibSession);
			Assignment otherAssignment = adao.get((Integer)result[1],hibSession);
			int jenrl = ((Number)result[2]).intValue();
			
			if (assignment==null || otherAssignment==null || jenrl==0 || !assignment.isInConflict(otherAssignment)) continue;
			addCommitJenrl(hibSession, assignment, otherAssignment, jenrl, defJenrlInfo, solverInfos);
        }	
		
		for (Iterator i=solverInfos.values().iterator();i.hasNext();) {
			SolverInfo sInfo = (SolverInfo)i.next();
			hibSession.saveOrUpdate(sInfo);
		}
		*/
		
		setCommitDate(new Date());
		setCommited(Boolean.TRUE);
		
//		createDivSecNumbers(hibSession, messages);
		
		EventContact contact = null;
		if (sendNotificationPuid!=null) {
		    contact = EventContact.findByExternalUniqueId(sendNotificationPuid);
		    if (contact==null) {
		        TimetableManager manager = TimetableManager.findByExternalId(sendNotificationPuid);
		        if (manager != null) {
		        	contact = new EventContact();
		        	contact.setFirstName(manager.getFirstName());
		        	contact.setMiddleName(manager.getMiddleName());
		        	contact.setLastName(manager.getLastName());
		        	contact.setExternalUniqueId(manager.getExternalUniqueId());
		        	contact.setEmailAddress(manager.getEmailAddress());
		        	hibSession.persist(contact);
		        }
		    }
		}
        Hashtable<Long,ClassEvent> classEvents = new Hashtable();
        for (ClassEvent e: hibSession.createQuery(
                "select e from Solution s inner join s.assignments a, ClassEvent e where e.clazz=a.clazz and s.uniqueId=:solutionId", ClassEvent.class)
                .setParameter("solutionId", getUniqueId())
                .list()) {
            classEvents.put(e.getClazz().getUniqueId(),e);
        }
        EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(getSession().getUniqueId());
		for (Iterator i=getAssignments().iterator();i.hasNext();) {
		    Assignment a = (Assignment)i.next();
		    ClassEvent event = a.generateCommittedEvent(classEvents.get(a.getClassId()), true, class2eventDates);
		    classEvents.remove(a.getClassId());
		    if (event != null && !event.getMeetings().isEmpty()) {
		        event.setMainContact(contact);
    			if (event.getNotes() == null)
    				event.setNotes(new HashSet<EventNote>());
				EventNote note = new EventNote();
				note.setEvent(event);
				note.setNoteType(event.getUniqueId() == null ? EventNote.sEventNoteTypeCreateEvent : EventNote.sEventNoteTypeEditEvent);
				note.setTimeStamp(new Date());
				note.setUser(contact == null ? "System" : contact.getName());
				note.setUserId(sendNotificationPuid);
				note.setTextNote(MSG.classNoteCommitted(getOwner().getName()));
				note.setMeetings(a.getPlacement().getLongName(CONSTANTS.useAmPm()));
				event.getNotes().add(note);
				if (event.getUniqueId() == null)
					hibSession.persist(event);
				else
					hibSession.merge(event);
		    }
		    if (event != null && event.getMeetings().isEmpty() && event.getUniqueId() != null)
		    	hibSession.remove(event);
		}
		
		if (ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue()) {
			for (Enumeration<ClassEvent> e=classEvents.elements();e.hasMoreElements();) {
			    ClassEvent event = e.nextElement();
			    hibSession.remove(event);
			}
		} else {
			Calendar cal = Calendar.getInstance(Locale.US);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date today = cal.getTime();

			for (Enumeration e=classEvents.elements();e.hasMoreElements();) {
			    ClassEvent event = (ClassEvent)e.nextElement();
	        	for (Iterator<Meeting> i = event.getMeetings().iterator(); i.hasNext(); )
	        		if (!i.next().getMeetingDate().before(today)) i.remove();
	        	if (event.getMeetings().isEmpty()) {
	        		hibSession.remove(event);
	        	} else {
	    			if (event.getNotes() == null)
	    				event.setNotes(new HashSet<EventNote>());
					EventNote note = new EventNote();
					note.setEvent(event);
					note.setNoteType(EventNote.sEventNoteTypeDeletion);
					note.setTimeStamp(new Date());
					note.setUser(contact == null ? "System" : contact.getName());
					note.setUserId(sendNotificationPuid);
					note.setTextNote(MSG.classNoteCommittedClassRemoved(getOwner().getName()));
					note.setMeetings(MSG.classMeetingsNotApplicable());
					event.getNotes().add(note);
	        		hibSession.merge(event);
	        	}
			}
		}
		
		if (sendNotificationPuid!=null) sendNotification(uncommittedSolution, this, sendNotificationPuid, true, messages);

		// Manually fix the Clazz_.committedAssignment cache.
		for (Assignment a: getAssignments())
			a.getClazz().setCommittedAssignment(a);

		return true;
	}
	
	public static void sendNotification(Solution uncommittedSolution, Solution committedSolution, String puid, boolean success, List<String> messages) {
		try {
			if (ApplicationProperty.EmailNotificationSolutionCommits.isFalse()) 
				return; //email notification disabled

			Formats.Format<Date> sdf = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
			SolverGroup owner = (uncommittedSolution==null?committedSolution:uncommittedSolution).getOwner();
			
			String subject = "Solution "+(committedSolution!=null?uncommittedSolution!=null?"recommitted":"committed":"uncommitted")+" for "+owner.getName();
			
			if (!success) {
				subject = "Failed to "+(committedSolution!=null?uncommittedSolution!=null?"recommit":"commit":"uncommit")+" a solution for "+owner.getName();
			}
			
        	String mail = subject;
        	mail += "\r\n";
        	mail += "\r\n";
        	if (messages!=null && !messages.isEmpty()) {
        		mail += "Message(s): ----------------- \r\n";
        		for (String m: messages) {
        			mail += m + "\r\n";
        		}
            	mail += "\r\n";
            	mail += "\r\n";
        	}
        	if (committedSolution!=null) {
        		mail += "Committed solution info: -------------- \r\n";
        		mail += "Created: "+sdf.format(committedSolution.getCreated())+"\r\n";
        		mail += "Owner: "+committedSolution.getOwner().getName()+"\r\n";
                if (committedSolution.getCommitDate()!=null)
                    mail += "Commited: "+sdf.format(committedSolution.getCommitDate())+"\r\n";
        		if (committedSolution.getNote()!=null && committedSolution.getNote().trim().length()>0) 
        			mail+= "Note: "+committedSolution.getNote()+"\r\n";
        		PropertiesInfo globalInfo = (PropertiesInfo)committedSolution.getInfo("GlobalInfo");
    			Vector infos = new Vector(globalInfo.keySet());
    			Collections.sort(infos, new InfoComparator());
        		for (Enumeration e=infos.elements();e.hasMoreElements();) {
        			String key = (String)e.nextElement();
        			String value = (String)globalInfo.getProperty(key);
        			mail += key+": "+value+"\r\n";
        		}
            	mail += "\r\n";
            	mail += "\r\n";
        	}
        	if (uncommittedSolution!=null) {
        		mail += "Uncommitted solution info: -------------- \r\n";
        		mail += "Created: "+sdf.format(uncommittedSolution.getCreated())+"\r\n";
        		mail += "Owner: "+uncommittedSolution.getOwner().getName()+"\r\n";
        		if (uncommittedSolution.getNote()!=null && uncommittedSolution.getNote().trim().length()>0) 
        			mail+= "Note: "+uncommittedSolution.getNote()+"\r\n";
        		PropertiesInfo globalInfo = (PropertiesInfo)uncommittedSolution.getInfo("GlobalInfo");
    			Vector infos = new Vector(globalInfo.keySet());
    			Collections.sort(infos, new InfoComparator());
        		for (Enumeration e=infos.elements();e.hasMoreElements();) {
        			String key = (String)e.nextElement();
        			String value = (String)globalInfo.getProperty(key);
        			mail += key+": "+value+"\r\n";
        		}
            	mail += "\r\n";
            	mail += "\r\n";
        	}
        	
        	TimetableManager mgr = TimetableManager.findByExternalId(puid);
        	
        	mail += "Manager info -------------- \r\n";
        	mail += "Name: "+mgr.getName()+"\r\n";
        	//mail += "PUID: "+mgr.getPuid()+"\r\n";
        	mail += "Email: "+mgr.getEmailAddress()+"\r\n";
        	mail += "\r\n";
        	mail += "Session info -------------- \r\n";
        	mail += "Session Term: "+owner.getSession().getAcademicYearTerm()+"\r\n";
        	mail += "Session Initiative: "+owner.getSession().getAcademicInitiative()+"\r\n";
        	mail += "Departments (from solver group): \r\n";
        	for (Iterator i=owner.getDepartments().iterator();i.hasNext();) {
        		Department d = (Department)i.next();
        		mail += "  "+d.getLabel()+"\r\n";
        	}
        	mail += "\r\n";
        	mail += "Application info -------------- \r\n";
        	mail += "Version: "+Constants.getVersion()+" ("+Constants.getReleaseDate()+")\r\n";
        	mail += "TimeStamp: "+(new Date());
        	
        	Email email = Email.createEmail();
        	email.addRecipient(mgr.getEmailAddress(), mgr.getName());
        	email.addNotifyCC();
        	email.setSubject("UniTime (Solution Commit): "+subject);
        	email.setText(mail);
        	email.send();
		} catch (Exception e) {
			sLog.error("Unable to send solution commit/uncommit notification, reason: "+e.getMessage(),e);
		}
	}
	
	public void export(CSVFile file, String instructorFormat) {
		file.setSeparator(",");
		file.setQuotationMark("\"");
		if (isCommited().booleanValue()) {
			file.setHeader(new CSVField[] {
					new CSVField("COURSE"),
					new CSVField("ITYPE"),
					new CSVField("SECTION"),
					new CSVField("SUFFIX"),
					new CSVField("EXTERNAL_ID"),
					new CSVField("DATE_PATTERN"),
					new CSVField("DAY"),
					new CSVField("START_TIME"),
					new CSVField("END_TIME"),
					new CSVField("ROOM"),
					new CSVField("INSTRUCTOR"),
					new CSVField("SCHEDULE_NOTE")
				});
		} else {
			file.setHeader(new CSVField[] {
				new CSVField("COURSE"),
				new CSVField("ITYPE"),
				new CSVField("SECTION"),
				new CSVField("SUFFIX"),
				new CSVField("DATE_PATTERN"),
				new CSVField("DAY"),
				new CSVField("START_TIME"),
				new CSVField("END_TIME"),
				new CSVField("ROOM"),
				new CSVField("INSTRUCTOR"),
				new CSVField("SCHEDULE_NOTE")
			});
		}
		
		Vector assignments = new Vector(getAssignments());
		
		assignments.addAll(getOwner().getNotAssignedClasses(this));
		
		Collections.sort(assignments, new ClassOrAssignmentComparator());
		for (Iterator i=assignments.iterator();i.hasNext();) {
			Object o = i.next();
			if (o instanceof Assignment) {
				Assignment assignment = (Assignment)o;
				Class_ clazz = assignment.getClazz();
				List<DepartmentalInstructor> leads = clazz.getLeadInstructors();
				StringBuffer leadsSb = new StringBuffer();
				for (Iterator<DepartmentalInstructor> e=leads.iterator();e.hasNext();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)e.next();
					leadsSb.append(instructor.getName(instructorFormat));
					if (e.hasNext()) leadsSb.append(";");
				}
				Placement placement = assignment.getPlacement();
				if (isCommited().booleanValue()) {
					file.addLine(new CSVField[] {
							new CSVField(clazz.getCourseName()),
							new CSVField(clazz.getItypeDesc()),
							new CSVField(clazz.getSectionNumber()),
							new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
							new CSVField(clazz.getDivSecNumber()),
							new CSVField(clazz.effectiveDatePattern().getName()),
							new CSVField(placement.getTimeLocation().getDayHeader()),
							new CSVField(placement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm())),
							new CSVField(placement.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm())),
							new CSVField(placement.getRoomName(",")),
							new CSVField(leadsSb),
							new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
					});
				} else {
					file.addLine(new CSVField[] {
							new CSVField(clazz.getCourseName()),
							new CSVField(clazz.getItypeDesc()),
							new CSVField(clazz.getSectionNumber()),
							new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
							new CSVField(clazz.effectiveDatePattern().getName()),
							new CSVField(placement.getTimeLocation().getDayHeader()),
							new CSVField(placement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm())),
							new CSVField(placement.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm())),
							new CSVField(placement.getRoomName(",")),
							new CSVField(leadsSb),
							new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
					});
				}
			} else {
				Class_ clazz = (Class_)o;
				List<DepartmentalInstructor> leads = clazz.getLeadInstructors();
				StringBuffer leadsSb = new StringBuffer();
				for (Iterator<DepartmentalInstructor> e=leads.iterator();e.hasNext();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)e.next();
					leadsSb.append(instructor.getName(instructorFormat));
					if (e.hasNext()) leadsSb.append(";");
				}
				if (isCommited().booleanValue()) {
					file.addLine(new CSVField[] {
							new CSVField(clazz.getCourseName()),
							new CSVField(clazz.getItypeDesc()),
							new CSVField(clazz.getSectionNumber()),
							new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
							new CSVField(clazz.getDivSecNumber()),
							new CSVField(clazz.effectiveDatePattern().getName()),
							new CSVField(""),
							new CSVField(""),
							new CSVField(""),
							new CSVField(""),
							new CSVField(leadsSb),
							new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
					});
				} else {
					file.addLine(new CSVField[] {
							new CSVField(clazz.getCourseName()),
							new CSVField(clazz.getItypeDesc()),
							new CSVField(clazz.getSectionNumber()),
							new CSVField(clazz.getSchedulingSubpart().getSchedulingSubpartSuffix()),
							new CSVField(clazz.effectiveDatePattern().getName()),
							new CSVField(""),
							new CSVField(""),
							new CSVField(""),
							new CSVField(""),
							new CSVField(leadsSb),
							new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
					});
				}
			}
		}
	}
	
	private static class ClassOrAssignmentComparator implements Comparator {
		ClassComparator cc = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY);
		public int compare(Object o1, Object o2) {
			if (o1==null || o2==null) return 0;
			Class_ c1 = (o1 instanceof Class_ ? (Class_)o1 : ((Assignment)o1).getClazz());
			Class_ c2 = (o2 instanceof Class_ ? (Class_)o2 : ((Assignment)o2).getClazz());
			return cc.compare(c1,c2);
		}
	}
	
	private void deleteObjects(org.hibernate.Session hibSession, String objectName, String idQuery) {
		Iterator<Long> idIterator = hibSession.createQuery(idQuery, Long.class).setParameter("solutionId", getUniqueId()).list().iterator();
		StringBuffer ids = new StringBuffer();
		int idx = 0;
		while (idIterator.hasNext()) {
			ids.append(idIterator.next()); idx++;
			if (idx==100) {
				hibSession.createMutationQuery("delete "+objectName+" as x where x.uniqueId in ("+ids+")").executeUpdate();
				ids = new StringBuffer();
				idx = 0;
			} else if (idIterator.hasNext()) {
				ids.append(",");
			}
		}
		if (idx>0)
			hibSession.createMutationQuery("delete "+objectName+" as x where x.uniqueId in ("+ids+")").executeUpdate();
	}
	
	public void delete(org.hibernate.Session hibSession) {
		// NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		Iterator i = hibSession.createQuery(
				"select distinct c, oa from "+
				"ConstraintInfo c inner join c.assignments a, Assignment oa "+
				"where "+
				"a.solution.uniqueId=:solutionId and oa.solution.uniqueId!=:solutionId and oa in elements ( c.assignments) ")
				.setParameter("solutionId", getUniqueId())
				.iterate();
		while (i.hasNext()) {
			Object[] next = (Object[])i.next();
			ConstraintInfo c = (ConstraintInfo)next[0];
			Assignment oa = (Assignment)next[1];
			oa.getConstraintInfo().remove(c);
			hibSession.saveOrUpdate(oa);
		}
		*/
		
		try {
			SolutionInfo solutionInfo = getSolutionInfo("CBSInfo");
			if (solutionInfo!=null)
				solutionInfo.delete(hibSession);
		} catch (Exception e) {
			Debug.error(e);
		}
		
        hibSession.createMutationQuery(
				"delete StudentEnrollment x where x.solution.uniqueId=:solutionId ")
				.setParameter("solutionId", getUniqueId())
				.executeUpdate();
		
		hibSession.createMutationQuery(
				"delete JointEnrollment x where x.solution.uniqueId=:solutionId ")
				.setParameter("solutionId", getUniqueId())
				.executeUpdate();

		deleteObjects(
				hibSession,
				"SolverInfo",
				"select a.uniqueId from AssignmentInfo a where a.assignment.solution.uniqueId=:solutionId"
				);
		
		deleteObjects(
				hibSession,
				"SolverInfo",
				"select s.uniqueId from SolutionInfo s where s.solution.uniqueId=:solutionId"
				);

		deleteObjects(
				hibSession,
				"SolverInfo",
				"select c.uniqueId from ConstraintInfo c inner join c.assignments a where a.solution.uniqueId=:solutionId"
				);
		
		hibSession.createMutationQuery(
				"delete Assignment x where x.solution.uniqueId=:solutionId ")
				.setParameter("solutionId", getUniqueId())
				.executeUpdate();
		
		deleteObjects(
				hibSession,
				"SolverParameter",
				"select p.uniqueId from Solution s inner join s.parameters p where s.uniqueId=:solutionId"
				);
		
		getOwner().getSolutions().remove(this);
		
		hibSession.remove(this);
	}
	
	public void empty(org.hibernate.Session hibSession, TimetableInfoFileProxy proxy) {
		// NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		Iterator i = hibSession.createQuery(
				"select distinct c, oa from "+
				"ConstraintInfo c inner join c.assignments a, Assignment oa "+
				"where "+
				"a.solution.uniqueId=:solutionId and oa.solution.uniqueId!=:solutionId and oa in elements ( c.assignments) ")
				.setParameter("solutionId", getUniqueId())
				.iterate();
		while (i.hasNext()) {
			Object[] next = (Object[])i.next();
			ConstraintInfo c = (ConstraintInfo)next[0];
			Assignment oa = (Assignment)next[1];
			oa.getConstraintInfo().remove(c);
			hibSession.saveOrUpdate(oa);
		}
		*/
		
		try {
			SolutionInfo solutionInfo = getSolutionInfo("CBSInfo");
			if (solutionInfo!=null)
				solutionInfo.delete(hibSession, proxy);
		} catch (Exception e) {
			Debug.error(e);
		}
		
		hibSession.flush(); 

		hibSession.createMutationQuery(
				"delete StudentEnrollment x where x.solution.uniqueId=:solutionId")
				.setParameter("solutionId", getUniqueId())
				.executeUpdate();
		
		hibSession.createMutationQuery(
				"delete JointEnrollment x where x.solution.uniqueId=:solutionId")
				.setParameter("solutionId", getUniqueId())
				.executeUpdate();

		deleteObjects(
				hibSession,
				"SolverInfo",
				"select a.uniqueId from AssignmentInfo a where a.assignment.solution.uniqueId=:solutionId"
				);
		
		deleteObjects(
				hibSession,
				"SolverInfo",
				"select s.uniqueId from SolutionInfo s where s.solution.uniqueId=:solutionId"
				);

		deleteObjects(
				hibSession,
				"SolverInfo",
				"select c.uniqueId from ConstraintInfo c inner join c.assignments a where a.solution.uniqueId=:solutionId"
				);
		
		hibSession.createMutationQuery(
				"delete Assignment x where x.solution.uniqueId=:solutionId")
				.setParameter("solutionId", getUniqueId())
				.executeUpdate();
		
		deleteObjects(
				hibSession,
				"SolverParameter",
				"select p.uniqueId from Solution s inner join s.parameters p where s.uniqueId=:solutionId"
				);
		
		setAssignments(null);
		setSolutionInfo(null);
		setJointEnrollments(null);
		setStudentEnrollments(null);
		setParameters(null);
		
		hibSession.merge(this);

		hibSession.flush();
	}
	
	public void updateCommittedStudentEnrollmentInfos(org.hibernate.Session hibSession) throws Exception {
		SolutionInfo sInfo = getSolutionInfo("GlobalInfo");
		if (sInfo!=null) {
			PropertiesInfo propInfo = (PropertiesInfo)sInfo.getInfo();
			String conf = propInfo.getProperty("Student conflicts");
			int studentConf = Integer.parseInt(conf.substring(0,conf.indexOf(' ')));
			if (conf.indexOf("committed:")>=0) {
				int commitedStart = conf.indexOf("committed:")+"committed:".length();
				int commitedEnd = conf.indexOf(',',commitedStart);
				int commitedConf = Integer.parseInt(conf.substring(commitedStart,commitedEnd));
			    //String newConf = (studentConf-commitedConf)+" [committed:"+0+conf.substring(commitedEnd);
				String newConf = (studentConf-commitedConf)+" ["+conf.substring(commitedEnd+2);
				propInfo.setProperty("Student conflicts", newConf);
			}
			sInfo.setInfo(propInfo);
			hibSession.merge(sInfo);
		}
		
		//NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		if (getAssignments()!=null) {
			for (Iterator i=getAssignments().iterator();i.hasNext();) {
				Assignment a = (Assignment)i.next();
				for (Iterator j=a.getAssignmentInfo().iterator();j.hasNext();) {
					AssignmentInfo aInfo = (AssignmentInfo)j.next();
					if (!"AssignmentInfo".equals(aInfo.getDefinition().getName())) continue;
					AssignmentPreferenceInfo assignmentInfo = (AssignmentPreferenceInfo)aInfo.getInfo();
					assignmentInfo.setNrStudentConflicts(assignmentInfo.getNrStudentConflicts()-assignmentInfo.getNrCommitedStudentConflicts());
					assignmentInfo.setNrCommitedStudentConflicts(0);
					aInfo.setInfo(assignmentInfo);
					hibSession.saveOrUpdate(aInfo);
				}
				for (Iterator j=a.getConstraintInfos("JenrlInfo").iterator();j.hasNext();) {
					ConstraintInfo c = (ConstraintInfo)j.next();
					boolean isCommittedInfo = false;
					for (Iterator k=c.getAssignments().iterator();k.hasNext();) {
						Assignment oa = (Assignment)k.next();
						if (!oa.getSolution().equals(this)) {
							oa.getConstraintInfo().remove(c);
							hibSession.saveOrUpdate(oa);
							isCommittedInfo = true;
						}
					}
					if (isCommittedInfo) {
						a.getConstraintInfo().remove(c);
						hibSession.saveOrUpdate(a);
						hibSession.remove(c);
					}
				}
			}
		}
		
		SolverInfoDef defJenrlInfo = SolverInfoDef.findByName(hibSession,"JenrlInfo");
		Hashtable solverInfos = new Hashtable();
		AssignmentDAO adao = AssignmentDAO.getInstance(); 
		Query q = hibSession.createQuery(
				"select a.uniqueId, oa.uniqueId, count(*) from "+
				"Solution s inner join s.assignments a inner join s.studentEnrollments as e, "+
				"Solution os inner join os.assignments oa inner join os.studentEnrollments as oe "+
				"where "+
				"s.uniqueId=:solutionId and os.owner.session.uniqueId=:sessionId and os.owner.uniqueId!=:ownerId and os.commited=true and "+
				"a.clazz=e.clazz and oa.clazz=oe.clazz and a.clazz.schedulingSubpart!=oa.clazz.schedulingSubpart and e.studentId=oe.studentId "+
				"group by a.uniqueId, oa.uniqueId");
		q.setParameter("ownerId", getOwner().getUniqueId());
		q.setParameter("solutionId", getUniqueId());
		q.setParameter("sessionId", getOwner().getSession().getUniqueId());
		Iterator otherAssignments = q.iterate();
		while (otherAssignments.hasNext()) {
			Object[] result = (Object[])otherAssignments.next();
			Assignment assignment = adao.get((Integer)result[0],hibSession);
			Assignment otherAssignment = adao.get((Integer)result[1],hibSession);
			int jenrl = ((Number)result[2]).intValue();
			
			if (assignment==null || otherAssignment==null || jenrl==0 || !assignment.isInConflict(otherAssignment)) continue;
			addCommitJenrl(hibSession, otherAssignment, assignment, jenrl, defJenrlInfo, solverInfos);
        }
		
		for (Iterator i=solverInfos.values().iterator();i.hasNext();) {
			SolverInfo solverInfo = (SolverInfo)i.next();
			hibSession.saveOrUpdate(solverInfo);
		}
		*/
	}
	
	@Transient
	public Session getSession() {
		return getOwner().getSession();
	}
	
    public static List<Solution> findBySessionId(Long sessionId) {
    	return (SolutionDAO.getInstance()).
    			getSession().
    			createQuery("select s from Solution s where s.owner.session.uniqueId=:sessionId", Solution.class).
    			setParameter("sessionId", sessionId.longValue()).
    			//setCacheable(true).
    			list();
    }

    private HashSet takenDivisionNumbers(SchedulingSubpart subpart) {
    	HashSet divNums = new HashSet();

    	InstructionalOffering offering = subpart.getInstrOfferingConfig().getInstructionalOffering();
    	ItypeDesc itype = subpart.getItype();
   		for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
   			InstrOfferingConfig cfg = (InstrOfferingConfig)i.next();
   			for (Iterator j=cfg.getSchedulingSubparts().iterator();j.hasNext();) {
   				SchedulingSubpart s = (SchedulingSubpart)j.next();
   				if (!s.getItype().equals(itype)) continue;
   		    	for (Iterator k=s.getClasses().iterator();k.hasNext();) {
   		    		Class_ clazz = (Class_)k.next();
   		    		if (clazz.getClassSuffix()==null) continue;
   		    		Integer divNum = Integer.valueOf(clazz.getClassSuffix().substring(0,3));
   		    		divNums.add(divNum);
   		    	}
   			}
   		}

    	return divNums;
    }
    
    public void createDivSecNumbers(org.hibernate.Session hibSession, Vector messages) {
    	Vector assignments = new Vector(getAssignments());
        assignments.addAll(SolutionDAO.getInstance().getSession().createQuery(
                "select distinct c from Class_ c, Solution s inner join s.owner.departments d "+
                "where s.uniqueId = :solutionId and c.managingDept=d and "+
                "c.uniqueId not in (select a.clazz.uniqueId from s.assignments a)", Class_.class).
                setParameter("solutionId", getUniqueId()).
                list());
        HashSet relatedOfferings = new HashSet();
        for (Enumeration e=assignments.elements();e.hasMoreElements();) {
            Object o = e.nextElement();
            Assignment assignment = (o instanceof Assignment?(Assignment)o:null);
            Class_ clazz = (assignment==null?(Class_)o:assignment.getClazz());
            relatedOfferings.add(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());
        }
        for (Iterator i=relatedOfferings.iterator();i.hasNext();) {
            InstructionalOffering io = (InstructionalOffering)i.next();
            for (Iterator j=io.getInstrOfferingConfigs().iterator();j.hasNext();) {
                InstrOfferingConfig ioc = (InstrOfferingConfig)j.next();
                for (Iterator k=ioc.getSchedulingSubparts().iterator();k.hasNext();) {
                    SchedulingSubpart subpart = (SchedulingSubpart)k.next();
                    for (Iterator l=subpart.getClasses().iterator();l.hasNext();) {
                        Class_ clazz = (Class_)l.next();
                        if (clazz.getClassSuffix()!=null && !getOwner().getDepartments().contains(clazz.getManagingDept())) {
                            Assignment assignment = clazz.getCommittedAssignment();
                            assignments.add(assignment==null?(Object)clazz:(Object)assignment);
                            clazz.setClassSuffix(null);
                        }
                    }
                }
            }
        }
    	DivSecAssignmentComparator cmp = new DivSecAssignmentComparator(this, true, false);
    	Collections.sort(assignments, cmp);
    	
    	Assignment lastAssignment = null;
    	SchedulingSubpart lastSubpart = null;
    	Class_ lastClazz = null;
    	int divNum = 1, secNum = 0;
    	HashSet takenDivNums = null;
    	
    	HashSet recompute = new HashSet();
    	
    	for (Enumeration e=assignments.elements();e.hasMoreElements();) {
            Object o = e.nextElement();
    		Assignment assignment = (o instanceof Assignment?(Assignment)o:null);
    		Class_ clazz = (assignment==null?(Class_)o:assignment.getClazz());
    		
    		if (clazz.getParentClass()!=null && clazz.getSchedulingSubpart().getItype().equals(clazz.getParentClass().getSchedulingSubpart().getItype()))
    			continue;
    		
    		if (lastSubpart==null || !lastSubpart.equals(clazz.getSchedulingSubpart())) {
    			takenDivNums = takenDivisionNumbers(clazz.getSchedulingSubpart());
    			lastAssignment = null; lastSubpart = null; lastClazz = null;
    		}
    		
    		int nrClasses = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getNrClasses(clazz.getSchedulingSubpart().getItype());
    		
    		if (lastAssignment!=null && assignment!=null) {
    			if (nrClasses>=100 && cmp.compareTimeLocations(lastAssignment.getClazz(), assignment.getClazz(), lastAssignment.getTimeLocation(),assignment.getTimeLocation())==0) {
        			if (lastClazz!=null && 
        					clazz.getParentClass()!=null && 
        					!clazz.getParentClass().equals(lastClazz.getParentClass()) && 
        					clazz.getParentClass().getDivSecNumber()!=null && 
        					lastClazz.getParentClass().getDivSecNumber()!=null) {
        				if (cmp.compareTimeLocations(lastAssignment.getClazz(), assignment.getClazz(), lastAssignment.getTimeLocation(),assignment.getTimeLocation())==0 && 
        						clazz.getParentClass().getDivSecNumber().substring(0,3).equals(lastClazz.getParentClass().getDivSecNumber().substring(0,3))) {
        					secNum++;
        				} else {
       						divNum++; secNum = 1;
       						while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
        				}
        			} else {
        				secNum++;
        			}
    			} else {
    				divNum++; secNum = 1;
    				while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
    			}
            } else if (lastClazz!=null) {
                divNum++; secNum = 1;
                while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
    		} else {
    			divNum = 1; secNum = 1;
    			while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
    		}
    		
    		if (divNum==100 && secNum==1) {
    			sLog.warn("Division number exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
    	    	for (Iterator i=clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
    	    		InstrOfferingConfig cfg = (InstrOfferingConfig)i.next();
    	    		for (Iterator j=cfg.getSchedulingSubparts().iterator();j.hasNext();) {
    	    			SchedulingSubpart subpart = (SchedulingSubpart)j.next();
    	    			if (subpart.getItype().equals(clazz.getSchedulingSubpart().getItype()))
    	    				recompute.add(subpart);
    	    		}
    	    	}
    		}
    		
    		clazz.setClassSuffix(sSufixFormat.format(divNum)+sSufixFormat.format(secNum));
    		hibSession.merge(clazz);
    		
    		lastAssignment = assignment;
    		lastSubpart = clazz.getSchedulingSubpart();
    		lastClazz = clazz;
    	}
    	
    	if (!recompute.isEmpty()) {
    		HashSet recompute2 = new HashSet();
        	for (Iterator i=assignments.iterator();i.hasNext();) {
                Object o = i.next();
                Assignment assignment = (o instanceof Assignment?(Assignment)o:null);
                Class_ clazz = (assignment==null?(Class_)o:assignment.getClazz());
        		if (recompute.contains(clazz.getSchedulingSubpart())) {
        			clazz.setClassSuffix(null);
        			hibSession.merge(clazz);
        		} else {
        			i.remove();
        		}
        	}
    		cmp = new DivSecAssignmentComparator(this, false, false);
        	Collections.sort(assignments, cmp);
    		lastAssignment = null; lastSubpart = null; lastClazz = null;
        	for (Enumeration e=assignments.elements();e.hasMoreElements();) {
                Object o = e.nextElement();
                Assignment assignment = (o instanceof Assignment?(Assignment)o:null);
                Class_ clazz = (assignment==null?(Class_)o:assignment.getClazz());

        		if (lastSubpart==null || !lastSubpart.equals(clazz.getSchedulingSubpart())) {
        			takenDivNums = takenDivisionNumbers(clazz.getSchedulingSubpart());
        			lastAssignment = null; lastSubpart = null; lastClazz = null;
        		}

        		if (lastAssignment!=null && assignment!=null) {
        			if (cmp.compareTimeLocations(lastAssignment.getClazz(), assignment.getClazz(), lastAssignment.getTimeLocation(),assignment.getTimeLocation())==0) {
        				secNum++;
        			} else {
        				divNum++; secNum = 1;
           				while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
        			}
                } else if (lastClazz!=null) {
                    divNum++; secNum = 1;
                    while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
                } else {
        			divNum = 1; secNum = 1;
        			while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
        		}

        		if (divNum==100 && secNum==1) {
        			sLog.warn("Division number still (fallback) exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
        	    	for (Iterator i=clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().iterator();i.hasNext();) {
        	    		InstrOfferingConfig cfg = (InstrOfferingConfig)i.next();
        	    		for (Iterator j=cfg.getSchedulingSubparts().iterator();j.hasNext();) {
        	    			SchedulingSubpart subpart = (SchedulingSubpart)j.next();
        	    			if (subpart.getItype().equals(clazz.getSchedulingSubpart().getItype()))
        	    				recompute2.add(subpart);
        	    		}
        	    	}
        		}

        		clazz.setClassSuffix(sSufixFormat.format(divNum)+sSufixFormat.format(secNum));
        		hibSession.merge(clazz);
        		
        		lastAssignment = assignment;
        		lastSubpart = clazz.getSchedulingSubpart();
        		lastClazz = clazz;
        	}
        	
        	if (!recompute2.isEmpty()) {
            	for (Iterator i=assignments.iterator();i.hasNext();) {
                    Object o = i.next();
                    Assignment assignment = (o instanceof Assignment?(Assignment)o:null);
                    Class_ clazz = (assignment==null?(Class_)o:assignment.getClazz());
            		if (recompute2.contains(clazz.getSchedulingSubpart())) {
            			clazz.setClassSuffix(null);
            			hibSession.merge(clazz);
            		} else {
            			i.remove();
            		}
            	}
        		cmp = new DivSecAssignmentComparator(this, false, true);
            	Collections.sort(assignments, cmp);
        		lastAssignment = null; lastSubpart = null; lastClazz = null;
            	for (Enumeration e=assignments.elements();e.hasMoreElements();) {
                    Object o = e.nextElement();
                    Assignment assignment = (o instanceof Assignment?(Assignment)o:null);
                    Class_ clazz = (assignment==null?(Class_)o:assignment.getClazz());

            		if (lastSubpart==null || cmp.compareSchedulingSubparts(lastSubpart, clazz.getSchedulingSubpart())!=0) {
            			takenDivNums = takenDivisionNumbers(clazz.getSchedulingSubpart());
            			lastAssignment = null; lastSubpart = null; lastClazz = null;
            		}

            		if (lastAssignment!=null && assignment!=null) {
            			if (cmp.compareTimeLocations(lastAssignment.getClazz(), assignment.getClazz(), lastAssignment.getTimeLocation(),assignment.getTimeLocation())==0) {
            				secNum++;
            			} else {
            				divNum++; secNum = 1;
               				while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
            			}
                    } else if (lastClazz!=null) {
                        divNum++; secNum = 1;
                        while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
            		} else {
            			divNum = 1; secNum = 1;
            			while (takenDivNums.contains(Integer.valueOf(divNum))) divNum++;
            		}

            		if (divNum==100 && secNum==1) {
            			messages.add("Division number exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
            			sLog.warn("Division number still (fallback2) exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
            		}

            		clazz.setClassSuffix(sSufixFormat.format(divNum)+sSufixFormat.format(secNum));
            		hibSession.merge(clazz);
            		
            		lastAssignment = assignment;
            		lastSubpart = clazz.getSchedulingSubpart();
            		lastClazz = clazz;
            	}
        	}
    	}
        
        /*
        lastSubpart = null;
        TreeSet otherClasses = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        otherClasses.addAll(SolutionDAO.getInstance().getSession().createQuery(
                "select distinct c from Class_ c, Solution s inner join s.owner.departments d "+
                "where s.uniqueId = :solutionId and c.managingDept=d and "+
                "c.uniqueId not in (select a.clazz.uniqueId from s.assignments a) order by c.schedulingSubpart.uniqueId, c.sectionNumberCache").
                setParameter("solutionId", getUniqueId()).
                list());
        
        for (Iterator i=otherClasses.iterator();i.hasNext();) {
            Class_ clazz = (Class_)i.next();
            
            if (clazz.getParentClass()!=null && clazz.getSchedulingSubpart().getItype().equals(clazz.getParentClass().getSchedulingSubpart().getItype()))
                continue;

            if (clazz.getClassSuffix()!=null) {
                sLog.warn("This is odd, class "+clazz.getClassLabel()+" already has a div-sec number "+clazz.getClassSuffix()+".");
                continue;
            }
            
            if (lastSubpart==null || !lastSubpart.equals(clazz.getSchedulingSubpart())) {
                takenDivNums = takenDivisionNumbers(clazz.getSchedulingSubpart());
            }
            divNum = 1;
            while (takenDivNums.contains(Integer.valueOf(divNum)))
                divNum++;
            if (divNum==100) {
                messages.add("Division number exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
            }
            clazz.setClassSuffix(sSufixFormat.format(divNum)+sSufixFormat.format(1));
            takenDivNums.add(Integer.valueOf(divNum));
            lastSubpart = clazz.getSchedulingSubpart();
            hibSession.merge(clazz);
        }
        */
    }
    
    public void removeDivSecNumbers(org.hibernate.Session hibSession) {
    	HashSet classes = new HashSet();
    	for (Iterator i=getAssignments().iterator();i.hasNext();) {
    		Assignment assignment = (Assignment)i.next();
    		Class_ clazz = assignment.getClazz();
    		if (clazz==null || clazz.getClassSuffix()==null) continue;
    		classes.add(clazz);
    	}
        
        HashSet subparts2fix = new HashSet();
    	
    	for (Iterator i=classes.iterator();i.hasNext();) {
    		Class_ clazz = (Class_)i.next();
    		
    		clazz.setClassSuffix(null);
            
            subparts2fix.add(clazz.getSchedulingSubpart());
            
            hibSession.merge(clazz);
    	}
    	
        List<Class_> otherClasses = SolutionDAO.getInstance().getSession().createQuery(
                "select distinct c from Class_ c, Solution s inner join s.owner.departments d "+
                "where s.uniqueId = :solutionId and c.managingDept=d and "+
                "c.uniqueId not in (select a.clazz.uniqueId from s.assignments a)", Class_.class).
                setParameter("solutionId", getUniqueId()).
                list();
        for (Class_ clazz: otherClasses) {
            if (clazz.getClassSuffix()==null) continue;
            clazz.setClassSuffix(null);
            
            subparts2fix.add(clazz.getSchedulingSubpart());
            
            hibSession.merge(clazz);
        }
        
        for (Iterator i=subparts2fix.iterator();i.hasNext();) {
            SchedulingSubpart subpart = (SchedulingSubpart)i.next();
            
            TreeSet takenDivNums = new TreeSet(takenDivisionNumbers(subpart));
            int dec = 0, lastDiv = 0;
            
            for (Iterator j=takenDivNums.iterator();j.hasNext();) {
                int div = ((Integer)j.next()).intValue();
                dec += (div-lastDiv-1); 
                lastDiv = div;
                if (dec>0) {
                    sLog.debug(subpart.getSchedulingSubpartLabel()+": "+div+"->"+(div-dec));
                    InstructionalOffering offering = subpart.getInstrOfferingConfig().getInstructionalOffering();
                    ItypeDesc itype = subpart.getItype();
                    for (Iterator i1=offering.getInstrOfferingConfigs().iterator();i1.hasNext();) {
                        InstrOfferingConfig cfg = (InstrOfferingConfig)i1.next();
                        for (Iterator i2=cfg.getSchedulingSubparts().iterator();i2.hasNext();) {
                            SchedulingSubpart s = (SchedulingSubpart)i2.next();
                            if (!s.getItype().equals(itype)) continue;
                            for (Iterator i3=s.getClasses().iterator();i3.hasNext();) {
                                Class_ clazz = (Class_)i3.next();
                                if (clazz.getClassSuffix()==null || clazz.getClassSuffix().length() != 6) continue;
                                int clazzDivNum = Integer.parseInt(clazz.getClassSuffix().substring(0,3));
                                int clazzSecNum = Integer.parseInt(clazz.getClassSuffix().substring(3,6));
                                if (clazzDivNum==div) {
                                    clazz.setClassSuffix(sSufixFormat.format(clazzDivNum-dec)+sSufixFormat.format(clazzSecNum));
                                    hibSession.merge(clazz);
                                }
                            }
                        }
                    }

                }
            }
        }
    }    
    
    private transient Hashtable iAssignmentTable = null; 
    
	public Assignment getAssignment(Long classId) {
		if (iAssignmentTable==null) {
			iAssignmentTable = new Hashtable();
			for (Iterator i=getAssignments().iterator();i.hasNext();) {
				Assignment a = (Assignment)i.next();
				iAssignmentTable.put(a.getClassId(),a);
			}
		}
		return (Assignment)iAssignmentTable.get(classId);
	}
	
	public AssignmentInfo getAssignment(Class_ clazz) {
        if (!getOwner().getDepartments().contains(clazz.getManagingDept()))
            return clazz.getCommittedAssignment();
		return getAssignment(clazz.getUniqueId());
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) {
		return getAssignmentInfo(clazz.getUniqueId());
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId) {
		Assignment a = getAssignment(classId);
		return (a==null?null:(AssignmentPreferenceInfo)a.getAssignmentInfo("AssignmentInfo"));
	}
	
	public Map<Long, AssignmentInfo> getAssignmentTable(Collection classesOrClassIds) {
		HashMap<Long, AssignmentInfo> assignments = new HashMap<Long, AssignmentInfo> ();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			AssignmentInfo assignment = (classOrClassId instanceof Class_ ? getAssignment((Class_)classOrClassId) : getAssignment((Long)classOrClassId));
			if (assignment!=null)
				assignments.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, assignment);
		}
		return assignments;
	}
	
	public Map<Long, AssignmentPreferenceInfo> getAssignmentInfoTable(Collection classesOrClassIds) {
		HashMap<Long, AssignmentPreferenceInfo> infos = new HashMap<Long, AssignmentPreferenceInfo> ();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			AssignmentPreferenceInfo info = (classOrClassId instanceof Class_ ? getAssignmentInfo((Class_)classOrClassId) : getAssignmentInfo((Long)classOrClassId));
			if (info!=null)
				infos.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, info);
		}
		return infos;
	}

	private transient DataProperties iPropertiesCache = null;
	@Transient
	public synchronized DataProperties getProperties() {
		if (iPropertiesCache==null) {
			iPropertiesCache = new DataProperties();
			for (Iterator i=getParameters().iterator();i.hasNext();) {
				SolverParameter p = (SolverParameter)i.next();
				iPropertiesCache.setProperty(p.getDefinition().getName(),p.getValue());
			}
		}
		return iPropertiesCache;
	}

    
    public static void refreshSolution(Long solutionId) {
        SolutionDAO dao = SolutionDAO.getInstance();
        org.hibernate.Session hibSession = dao.getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory(); 
        hibSessionFactory.getCache().evictEntityData(Solution.class, solutionId);
        hibSessionFactory.getCache().evictCollectionData(Solution.class.getName()+".parameters", solutionId);
        hibSessionFactory.getCache().evictCollectionData(Solution.class.getName()+".assignments", solutionId);
        for (Long classId: hibSession.createQuery("select c.uniqueId from "+
                    "Class_ c, Solution s inner join s.owner.departments d where s.uniqueId=:solutionId and "+
                    "c.managingDept = d", Long.class).
                    setParameter("solutionId", solutionId.longValue()).list()) {
            hibSessionFactory.getCache().evictEntityData(Class_.class, classId);
            hibSessionFactory.getCache().evictCollectionData(Class_.class.getName()+".assignments", classId);
        }
        hibSessionFactory.getCache().evictCollectionData(SolverGroup.class.getName()+".solutions", hibSession.createQuery("select owner.uniqueId from Solution s where s.uniqueId=:solutionId", Long.class).setParameter("solutionId", solutionId).uniqueResult());
   }
    
    public static boolean hasTimetable(Long sessionId) {
        return SolutionDAO.getInstance().getSession().
                createQuery("select count(s) from Solution s " +
                        "where s.owner.session.uniqueId=:sessionId and " +
                        "s.commited = true", Number.class).
                setParameter("sessionId", sessionId).setCacheable(true).uniqueResult().longValue()>0;
    }
    
	@Override
	public boolean hasConflicts(Long offeringId) {
		InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId);
		if (offering == null || offering.isNotOffered()) return false;
		
		for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
			for (SchedulingSubpart subpart: config.getSchedulingSubparts())
				for (Class_ clazz: subpart.getClasses()) {
					if (clazz.isCancelled()) continue;
					AssignmentInfo assignment = getAssignment(clazz);
					if (assignment == null) continue;
					if (assignment.getRooms() != null)
						for (Location room : assignment.getRooms()) {
							if (!room.isIgnoreRoomCheck()) {
								for (Assignment a : room.getCommitedAssignments())
									if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
										return true;
			            	}
							if (room instanceof Room) {
								Room r = (Room)room;
								if (r.getParentRoom() != null && !r.getParentRoom().isIgnoreRoomCheck()) {
									for (Assignment a : r.getParentRoom().getCommitedAssignments())
										if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
											return true;
								}
								for (Room p: r.getPartitions()) {
									if (!p.isIgnoreRoomCheck())
										for (Assignment a : p.getCommitedAssignments())
											if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
												return true;
								}
							}
			            }
					
					if (clazz.getClassInstructors() != null)
						for (ClassInstructor instructor: clazz.getClassInstructors()) {
							if (!instructor.isLead()) continue;
							for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor())) {
								for (ClassInstructor ci : di.getClasses()) {
									if (ci.equals(instructor) || ci.getClassInstructing().equals(clazz) || !ci.isLead()) continue;
				            		AssignmentInfo a = getAssignment(ci.getClassInstructing());
				            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareInstructor(a.getClazz()))
				            			return true;
				            	}
			            	}
							if (instructor.getInstructor().getExternalUniqueId() != null) {
								for (Class_ c: Class_DAO.getInstance().getSession().createQuery(
									"select e.clazz from StudentClassEnrollment e where e.student.externalUniqueId = :externalId and e.student.session.uniqueId = :sessionId", Class_.class)
									.setParameter("sessionId", instructor.getInstructor().getDepartment().getSessionId())
									.setParameter("externalId", instructor.getInstructor().getExternalUniqueId())
									.setCacheable(true).list()) {
									AssignmentInfo a = getAssignment(c);
				            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a)) return true;
								}
							}
						}
					
			        Class_ parent = clazz.getParentClass();
			        while (parent!=null) {
			        	AssignmentInfo a = getAssignment(parent);
			        	if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
			        		return true;
			        	parent = parent.getParentClass();
			        }
			        
			        for (Iterator<SchedulingSubpart> i = clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator(); i.hasNext();) {
			        	SchedulingSubpart ss = i.next();
			        	if (ss.getClasses().size() == 1) {
			        		Class_ child = ss.getClasses().iterator().next();
			        		if (clazz.equals(child)) continue;
			        		AssignmentInfo a = getAssignment(child);
			        		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
			        			return true;
			        	}
			        }
				}
		
		return false;
	}
    
	@Override
	public Set<AssignmentInfo> getConflicts(Long classId) {
		if (classId == null) return null;
		Class_ clazz = Class_DAO.getInstance().get(classId);
		if (clazz == null || clazz.isCancelled()) return null;
		AssignmentInfo assignment = getAssignment(clazz);
		if (assignment == null) return null;
		Set<AssignmentInfo> conflicts = new HashSet<AssignmentInfo>();
		if (assignment.getRooms() != null)
			for (Location room : assignment.getRooms()) {
				if (!room.isIgnoreRoomCheck()) {
					for (Assignment a : room.getAssignments(this))
						if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
							conflicts.add(a);
            	}
				if (room instanceof Room) {
					Room r = (Room)room;
					if (r.getParentRoom() != null && !r.getParentRoom().isIgnoreRoomCheck()) {
						for (Assignment a : r.getParentRoom().getCommitedAssignments())
							if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
								conflicts.add(a);
					}
					for (Room p: r.getPartitions()) {
						if (!p.isIgnoreRoomCheck())
							for (Assignment a : p.getCommitedAssignments())
								if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
									conflicts.add(a);
					}
				}
            }
		
		if (clazz.getClassInstructors() != null)
			for (ClassInstructor instructor: clazz.getClassInstructors()) {
				if (!instructor.isLead()) continue;
				for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor())) {
					for (ClassInstructor ci : di.getClasses()) {
						if (ci.equals(instructor) || ci.getClassInstructing().equals(clazz) || !ci.isLead()) continue;
						AssignmentInfo a = getAssignment(ci.getClassInstructing());
	            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareInstructor(a.getClazz()))
	            			conflicts.add(a);
	            	}
            	}
				if (instructor.getInstructor().getExternalUniqueId() != null) {
					for (Class_ c: Class_DAO.getInstance().getSession().createQuery(
						"select e.clazz from StudentClassEnrollment e where e.student.externalUniqueId = :externalId and e.student.session.uniqueId = :sessionId", Class_.class)
						.setParameter("sessionId", instructor.getInstructor().getDepartment().getSessionId())
						.setParameter("externalId", instructor.getInstructor().getExternalUniqueId())
						.setCacheable(true).list()) {
						AssignmentInfo a = getAssignment(c);
	            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
	            			conflicts.add(a);
					}
				}
			}
		
        Class_ parent = clazz.getParentClass();
        while (parent!=null) {
        	AssignmentInfo a = getAssignment(parent);
        	if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
    			conflicts.add(a);
        	parent = parent.getParentClass();
        }
        
        Queue<Class_> children = new LinkedList(clazz.getChildClasses());
        Class_ child = null;
        while ((child=children.poll())!=null) {
        	AssignmentInfo a = getAssignment(child);
        	if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
    			conflicts.add(a);
        	if (!child.getChildClasses().isEmpty())
        		children.addAll(child.getChildClasses());
        }
        
        for (Iterator<SchedulingSubpart> i = clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator(); i.hasNext();) {
        	SchedulingSubpart ss = i.next();
        	if (ss.getClasses().size() == 1) {
        		child = ss.getClasses().iterator().next();
        		if (clazz.equals(child)) continue;
        		AssignmentInfo a = getAssignment(child);
        		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
        			conflicts.add(a);
        	}
        }
        
        return conflicts;
	}
	
	@Override
	public Set<TimeBlock> getConflictingTimeBlocks(Long classId) {
		return null;
	}
	
	public static Hashtable<Long,Set<Long>> findConflictingStudents(Long classId, int startSlot, int length, List<Date> dates) {
    	Hashtable<Long,Set<Long>> table = new Hashtable();
    	if (dates.isEmpty()) return table;
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}
    	Query<Object[]> q = LocationDAO.getInstance().getSession()
    	    .createQuery("select distinct e.clazz.uniqueId, e.studentId "+
    	        	"from StudentEnrollment e, ClassEvent c inner join c.meetings m, StudentEnrollment x "+
    	        	"where x.clazz.uniqueId=:classId and x.studentId=e.studentId and " + // only look among students of the given class 
    	        	"e.clazz=c.clazz and " + // link ClassEvent c with StudentClassEnrollment e
            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
    	        	"e.solution.commited=true and x.solution.commited = true and " +
            		"e.solution.owner.session = x.solution.owner.session and " +
            		"m.meetingDate in ("+datesStr+") and m.approvalStatus = 1", Object[].class)
            .setParameter("classId", classId)
            .setParameter("startSlot", startSlot)
            .setParameter("endSlot", startSlot + length);
    	for (int i=0; i<dates.size(); i++) {
    		q.setParameter("date"+i, dates.get(i));
    	}
        for (Object[] o: q.setCacheable(true).list()) {
            Set<Long> set = table.get((Long)o[0]);
            if (set==null) {
            	set = new HashSet<Long>();
            	table.put((Long)o[0], set);
            }
            set.add((Long)o[1]);
        }
        return table;
    }
}
