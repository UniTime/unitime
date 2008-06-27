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
package org.unitime.timetable.model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.commons.User;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ListSolutionsForm.InfoComparator;
import org.unitime.timetable.model.base.BaseSolution;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.DivSecAssignmentComparator;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolutionInfoDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.ifs.util.CSVFile;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.CSVFile.CSVField;


public class Solution extends BaseSolution implements ClassAssignmentProxy {
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

	/**
	 * Constructor for required fields
	 */
	public Solution (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SolverGroup owner,
		java.util.Date created,
		java.lang.Boolean valid,
		java.lang.Boolean commited) {

		super (
			uniqueId,
			owner,
			created,
			valid,
			commited);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public SolutionInfo getSolutionInfo(String name) throws Exception {
		if ("GlobalInfo".equals(name)) return getGlobalInfo();
		return (SolutionInfo)
			(new SolutionInfoDAO()).
			getSession().
			createQuery("select si from SolutionInfo si where si.definition.name=:name and si.solution.uniqueId=:solutionId").
			setString("name",name).
			setInteger("solutionId",getUniqueId().intValue()).
			uniqueResult();
		/*
		org.hibernate.Session session = (new SolutionInfoDAO()).getSession();
		SolverInfoDef def = SolverInfoDef.findByName(session,name);
		if (def==null) return null;
		return (SolutionInfo)session.createCriteria(SolutionInfo.class).add(Restrictions.eq("definition",def)).add(Restrictions.eq("solution",this)).setCacheable(true).uniqueResult();
		*/
	}

	public TimetableInfo getInfo(String name) throws Exception {
		SolutionInfo sinfo = getSolutionInfo(name);
		if (sinfo==null) return null;
		return sinfo.getInfo();
	}
	
	//NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
	/*
	private void removeCommitJenrl(org.hibernate.Session hibSession, Assignment assignment, ConstraintInfo cinfo, JenrlInfo jenrl, Hashtable solverInfos) throws Exception {
		Assignment otherAssignment = null;
		for (Iterator i=cinfo.getAssignments().iterator();i.hasNext();) {
			Assignment a = (Assignment)i.next();
			if (!assignment.equals(a)) {
				otherAssignment = a; break;
			}
		}
		if (otherAssignment==null) return;
		if (DEBUG) {
			sLog.debug(
					assignment.getSolution().getUniqueId()+","+assignment.getClassName()+","+assignment.getPlacement().getName()+","+
					otherAssignment.getSolution().getUniqueId()+","+otherAssignment.getClassName()+","+otherAssignment.getPlacement().getName()+","+
					(-jenrl.getJenrl()));
		}
		for (Iterator j=otherAssignment.getAssignmentInfo().iterator();j.hasNext();) {
			AssignmentInfo aInfo = (AssignmentInfo)j.next();
			if (!"AssignmentInfo".equals(aInfo.getDefinition().getName())) continue;
			AssignmentPreferenceInfo assignmentInfo = (AssignmentPreferenceInfo)aInfo.getInfo();
			assignmentInfo.setNrStudentConflicts(assignmentInfo.getNrStudentConflicts()-(int)jenrl.getJenrl());
			aInfo.setInfo(assignmentInfo);
			hibSession.saveOrUpdate(aInfo);
			if (DEBUG) sLog.debug("   aInfo["+otherAssignment.getClassName()+"]-="+((int)jenrl.getJenrl())+" (total:"+assignmentInfo.getNrStudentConflicts()+")");
		}
		
		SolutionInfo sInfo = (SolutionInfo)solverInfos.get(otherAssignment.getSolution().getUniqueId());
		if (sInfo == null) {
			sInfo = otherAssignment.getSolution().getSolutionInfo("GlobalInfo");
			if (sInfo!=null)
				solverInfos.put(otherAssignment.getSolution().getUniqueId(), sInfo);
		}
		
		if (sInfo!=null) {
			PropertiesInfo propInfo = (PropertiesInfo)sInfo.getInfo();
			String conf = propInfo.getProperty("Student conflicts");
			int studentConf = Integer.parseInt(conf.substring(0,conf.indexOf(' ')));
			int commitedStart = conf.indexOf("committed:")+"committed:".length();
			int commitedEnd = conf.indexOf(',',commitedStart);
			int commitedConf = Integer.parseInt(conf.substring(commitedStart,commitedEnd));
			String newConf = (studentConf-(int)jenrl.getJenrl())+" [committed:"+(commitedConf-(int)jenrl.getJenrl())+conf.substring(commitedEnd);
			propInfo.setProperty("Student conflicts", newConf);
			sInfo.setInfo(propInfo);
			if (DEBUG) sLog.debug("   sInfo["+otherAssignment.getSolution().getOwner().getName()+"]-="+((int)jenrl.getJenrl())+" (total:"+(commitedConf-(int)jenrl.getJenrl())+")");
		}
			
		if (!otherAssignment.getSolution().isCommited().booleanValue()) {
			if (DEBUG) sLog.debug("   -jInfo["+assignment.getClassName()+","+otherAssignment.getClassName()+"]="+((JenrlInfo)cinfo.getInfo()).getJenrl());
			for (Iterator i=cinfo.getAssignments().iterator();i.hasNext();) {
				Assignment a = (Assignment)i.next();
				a.getConstraintInfo().remove(cinfo);
				hibSession.saveOrUpdate(a);
			}
			hibSession.delete(cinfo);
		}
	}
	
	private void addCommitJenrl(org.hibernate.Session hibSession, Assignment assignment, Assignment otherAssignment, int jenrl, SolverInfoDef defJenrlInfo, Hashtable solverInfos) throws Exception {
		if (DEBUG) {
			sLog.debug(
					assignment.getSolution().getUniqueId()+","+assignment.getClassName()+","+assignment.getPlacement().getName()+","+
					otherAssignment.getSolution().getUniqueId()+","+otherAssignment.getClassName()+","+otherAssignment.getPlacement().getName()+","+
					jenrl);
		}
		if (!otherAssignment.getSolution().isCommited().booleanValue()) {
			JenrlInfo jInfo = new JenrlInfo();
			jInfo.setJenrl(jenrl);
			jInfo.setIsCommited(true);
			jInfo.setIsDistance(!assignment.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation()));
			ConstraintInfo constraintInfo = new ConstraintInfo();
			constraintInfo.setInfo(jInfo);
			constraintInfo.setDefinition(defJenrlInfo);
			constraintInfo.setOpt("C"+(jInfo.isSatisfied()?"S":"")+(jInfo.isHard()?"H":"")+(jInfo.isDistance()?"D":"")+(jInfo.isFixed()?"F":""));
			HashSet jAssignments = new HashSet();
			jAssignments.add(assignment);
			jAssignments.add(otherAssignment);
			constraintInfo.setAssignments(jAssignments);
			hibSession.saveOrUpdate(constraintInfo);
			assignment.getConstraintInfo().add(constraintInfo);
			hibSession.saveOrUpdate(assignment);
			otherAssignment.getConstraintInfo().add(constraintInfo);
			hibSession.saveOrUpdate(otherAssignment);
			if (DEBUG) sLog.debug("   +jInfo["+assignment.getClassName()+","+otherAssignment.getClassName()+"]="+jInfo.getJenrl());
		}
		
		for (Iterator j=otherAssignment.getAssignmentInfo().iterator();j.hasNext();) {
			AssignmentInfo aInfo = (AssignmentInfo)j.next();
			if (!"AssignmentInfo".equals(aInfo.getDefinition().getName())) continue;
			AssignmentPreferenceInfo assignmentInfo = (AssignmentPreferenceInfo)aInfo.getInfo();
			assignmentInfo.setNrStudentConflicts(assignmentInfo.getNrStudentConflicts()+jenrl);
			aInfo.setInfo(assignmentInfo);
			hibSession.saveOrUpdate(aInfo);
			if (DEBUG) sLog.debug("   aInfo["+otherAssignment.getClassName()+"]+="+jenrl+" (total:"+assignmentInfo.getNrStudentConflicts()+")");
		}
		
		SolutionInfo sInfo = (SolutionInfo)solverInfos.get(otherAssignment.getSolution().getUniqueId());
		if (sInfo == null) {
			sInfo = otherAssignment.getSolution().getSolutionInfo("GlobalInfo");
			if (sInfo!=null)
				solverInfos.put(otherAssignment.getSolution().getUniqueId(), sInfo);
		}

		if (sInfo!=null) {
			PropertiesInfo propInfo = (PropertiesInfo)sInfo.getInfo();
			String conf = propInfo.getProperty("Student conflicts");
			String newConf = conf;
			
			int studentConf = Integer.parseInt(conf.substring(0,conf.indexOf(' ')));
			int commitedStart = conf.indexOf("committed:");
			int commitedEnd = conf.indexOf(',',commitedStart);
			int commitedConf = 0;
			if (commitedStart>=0) {
				commitedConf = Integer.parseInt(conf.substring(commitedStart+"committed:".length(),commitedEnd));
				newConf = (studentConf+jenrl)+" [committed:"+(commitedConf+jenrl)+conf.substring(commitedEnd);
			} else {
				commitedEnd = conf.indexOf('[')+1;
				newConf = (studentConf+jenrl)+" [committed:"+(commitedConf+jenrl)+", "+conf.substring(commitedEnd);
			}
			if (DEBUG) sLog.debug("   sInfo["+otherAssignment.getSolution().getOwner().getName()+"]+="+jenrl+" (total:"+(commitedConf+jenrl)+")");
			propInfo.setProperty("Student conflicts", newConf);
			sInfo.setInfo(propInfo);
		}
	}
	*/
	
	public void uncommitSolution(org.hibernate.Session hibSession) throws Exception {
		uncommitSolution(hibSession, null);
	}
	
	public void uncommitSolution(org.hibernate.Session hibSession, String sendNotificationPuid) throws Exception {
		if (DEBUG) sLog.debug("uncommit["+getUniqueId()+","+getOwner().getName()+"] -------------------------------------------------------");
		setCommitDate(null);
		setCommited(Boolean.FALSE);
		
		//NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		Hashtable solverInfos = new Hashtable();
		Iterator jenrlInfos = 
			hibSession.createQuery(
				"select distinct a, c from " +
				"ConstraintInfo c inner join c.assignments a where " +
				"c.definition.name='JenrlInfo' and c.opt like '%C%' and a.solution=:solutionId")
				.setInteger("solutionId",getUniqueId().intValue())
				.iterate();
		while (jenrlInfos.hasNext()) {
			Object[] next = (Object[])jenrlInfos.next(); 
			Assignment assignment = (Assignment)next[0];
			ConstraintInfo cinfo = (ConstraintInfo)next[1];
			JenrlInfo jenrl = (JenrlInfo)cinfo.getInfo();
			removeCommitJenrl(hibSession,assignment,cinfo,jenrl,solverInfos);
		}
		
		for (Iterator i=solverInfos.values().iterator();i.hasNext();) {
			SolverInfo sInfo = (SolverInfo)i.next();
			hibSession.saveOrUpdate(sInfo);
		}
		*/

		hibSession.update(this);
		
		deleteObjects(hibSession,
	              "ClassEvent",
	              "select e.uniqueId from Solution s inner join s.assignments a, ClassEvent e where e.clazz=a.clazz and s.uniqueId=:solutionId");

		removeDivSecNumbers(hibSession);
		
		if (sendNotificationPuid!=null) sendNotification(this, null, sendNotificationPuid, true, null);
	}
	
	public boolean commitSolution(Vector messages, org.hibernate.Session hibSession) throws Exception {
		return commitSolution(messages, hibSession, null);
	}

    public static boolean shareRooms(Assignment a1, Assignment a2) {
        if (!a1.getPlacement().sameRooms(a2.getPlacement())) return false;
        for (Iterator i=a1.getClazz().getDistributionObjects().iterator();i.hasNext();) {
            DistributionObject distObj = (DistributionObject)i.next();
            DistributionPref dp = distObj.getDistributionPref();
            String ref = dp.getDistributionType().getReference();
            if (!"MEET_WITH".equals(ref) && !"CAN_SHARE_ROOM".equals(ref)) continue;
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
            if (!"MEET_WITH".equals(ref) && !"CAN_SHARE_ROOM".equals(ref)) continue;
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
                if (!"MEET_WITH".equals(ref) && !"CAN_SHARE_ROOM".equals(ref)) continue;
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

	public boolean commitSolution(Vector messages, org.hibernate.Session hibSession, String sendNotificationPuid) throws Exception {
		List solutions = hibSession.createCriteria(Solution.class).add(Restrictions.eq("owner",getOwner())).list();
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
		
		Query q = hibSession.createQuery(
				"select distinct r from Assignment a, Assignment as xa, Location as r "+
				"where a.solution.uniqueId=:solutionId and r in elements(a.rooms) "+
				"and xa.solution.commited=true and xa.solution.owner.session.uniqueId=:sessionId and xa.solution.owner.uniqueId!=:ownerId and r in elements(xa.rooms)"
				);
		q.setLong("ownerId",getOwner().getUniqueId().longValue());
		q.setInteger("solutionId",getUniqueId().intValue());
		q.setLong("sessionId",getOwner().getSession().getUniqueId().longValue());
		Iterator rooms = q.iterate();
		
		boolean isOK = true;
		while (rooms.hasNext()) {
			Location room = (Location)rooms.next();
			if (room.isIgnoreRoomCheck()!=null && room.isIgnoreRoomCheck().booleanValue()) continue;
			q = hibSession.createQuery("select distinct a from Location r, Assignment as a "+
			"where r.uniqueId=:roomId and a in elements(r.assignments) and a.solution.commited=true and a.solution.owner.uniqueId!=:ownerId");
			q.setInteger("roomId",room.getUniqueId().intValue());
			q.setLong("ownerId",getOwner().getUniqueId().longValue());
			List commitedAssignments = q.list();
			if (commitedAssignments.isEmpty()) continue;
			q = hibSession.createQuery("select distinct a from Location r, Assignment as a "+
					"where r.uniqueId=:roomId and a in elements(r.assignments) and a.solution.uniqueId=:solutionId");
			q.setInteger("roomId",room.getUniqueId().intValue());
			q.setInteger("solutionId",getUniqueId().intValue());
			Iterator assignments = q.iterate();
			
			while (assignments.hasNext()) {
				Assignment a = (Assignment)assignments.next();
				for (Iterator k=commitedAssignments.iterator();k.hasNext();) {
					Assignment b=(Assignment)k.next();
					if (a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a,b)) {
						messages.addElement("Class "+a.getClassName()+" "+a.getTimeLocation().getName()+" overlaps with "+b.getClassName()+" "+b.getTimeLocation().getName()+" (room "+room.getLabel()+")");
						isOK=false;
					}
				}
			}
		}
		
		q = hibSession.createQuery(
				"select distinct i from Assignment a, Assignment as xa, DepartmentalInstructor as i "+
				"where a.solution.uniqueId=:solutionId and i in elements(a.instructors) "+
				"and xa.solution.commited=true and xa.solution.owner.session.uniqueId=:sessionId and xa.solution.owner.uniqueId!=:ownerId and i in elements(xa.instructors)"
				);
		q.setLong("ownerId",getOwner().getUniqueId().longValue());
		q.setInteger("solutionId",getUniqueId().intValue());
		q.setLong("sessionId",getOwner().getSession().getUniqueId().longValue());
		Iterator instructors = q.iterate();
		
		while (instructors.hasNext()) {
			DepartmentalInstructor instructor = (DepartmentalInstructor)instructors.next();
			q = hibSession.createQuery("select distinct a from DepartmentalInstructor i, Assignment as a "+
			"where i.uniqueId=:instructorId and a in elements(i.assignments) and a.solution.commited=true and a.solution.owner.uniqueId!=:ownerId");
			q.setInteger("instructorId",instructor.getUniqueId().intValue());
			q.setLong("ownerId",getOwner().getUniqueId().longValue());
			List commitedAssignments = q.list();
			if (commitedAssignments.isEmpty()) continue;
			q = hibSession.createQuery("select distinct a from DepartmentalInstructor i, Assignment as a "+
					"where i.uniqueId=:instructorId and a in elements(i.assignments) and a.solution.uniqueId=:solutionId");
			q.setInteger("instructorId",instructor.getUniqueId().intValue());
			q.setInteger("solutionId",getUniqueId().intValue());
			Iterator assignments = q.iterate();
			
			while (assignments.hasNext()) {
				Assignment a = (Assignment)assignments.next();
				for (Iterator k=commitedAssignments.iterator();k.hasNext();) {
					Assignment b=(Assignment)k.next();
					if (a.getTimeLocation().hasIntersection(b.getTimeLocation()) && !shareRooms(a,b)) {
						messages.addElement("Class "+a.getClassName()+" "+a.getTimeLocation().getName()+" overlaps with "+b.getClassName()+" "+b.getTimeLocation().getName()+" (instructor "+instructor.nameLastNameFirst()+")");
						isOK=false;
					}
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
		AssignmentDAO adao = new AssignmentDAO(); 
		q = hibSession.createQuery(
				"select a.uniqueId, oa.uniqueId, count(*) from "+
				"Solution s inner join s.assignments a inner join s.studentEnrollments as e, "+
				"Solution os inner join os.assignments oa inner join os.studentEnrollments as oe "+
				"where "+
				"s.uniqueId=:solutionId and os.owner.session.uniqueId=:sessionId and os.owner.uniqueId!=:ownerId and "+
				"a.clazz=e.clazz and oa.clazz=oe.clazz and a.clazz.schedulingSubpart!=oa.clazz.schedulingSubpart and e.studentId=oe.studentId "+
				"group by a.uniqueId, oa.uniqueId");
		q.setLong("ownerId",getOwner().getUniqueId().longValue());
		q.setInteger("solutionId",getUniqueId().intValue());
		q.setLong("sessionId",getOwner().getSession().getUniqueId().longValue());
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
		
		createDivSecNumbers(hibSession, messages);
		
		EventContact contact = null;
		if (sendNotificationPuid!=null) {
		    contact = EventContact.findByExternalUniqueId(sendNotificationPuid);
		    if (contact==null) {
		        TimetableManager manager = TimetableManager.findByExternalId(sendNotificationPuid);
		        contact = new EventContact();
		        contact.setFirstName(manager.getFirstName());
                contact.setMiddleName(manager.getMiddleName());
		        contact.setLastName(manager.getLastName());
		        contact.setExternalUniqueId(manager.getExternalUniqueId());
		        contact.setEmailAddress(manager.getEmailAddress());
		        contact.setPhone("unknown");
		        hibSession.save(contact);
		    }
		}
        Hashtable<Long,ClassEvent> classEvents = new Hashtable();
        for (Iterator i=hibSession.createQuery(
                "select e from Solution s inner join s.assignments a, ClassEvent e where e.clazz=a.clazz and s.uniqueId=:solutionId")
                .setLong("solutionId",getUniqueId())
                .iterate(); i.hasNext();) {
            ClassEvent e = (ClassEvent)i.next();
            classEvents.put(e.getClazz().getUniqueId(),e);
        }
		for (Iterator i=getAssignments().iterator();i.hasNext();) {
		    Assignment a = (Assignment)i.next();
		    ClassEvent event = a.generateCommittedEvent(classEvents.get(a.getClassId()),true);
		    classEvents.remove(a.getClassId());
		    if (event!=null) {
		        event.setMainContact(contact);
		        hibSession.saveOrUpdate(event);
		    }
		}
		for (Enumeration e=classEvents.elements();e.hasMoreElements();) {
		    ClassEvent event = (ClassEvent)e.nextElement();
		    hibSession.delete(event);
		}
		
		if (sendNotificationPuid!=null) sendNotification(uncommittedSolution, this, sendNotificationPuid, true, messages);
		
		return true;
	}
	
	public static void sendNotification(Solution uncommittedSolution, Solution committedSolution, String puid, boolean success, Vector messages) {
		try {
			if (!"true".equalsIgnoreCase((String)ApplicationProperties.getProperty("tmtbl.notif.commit.enabled", "true")))
				return; //email notification disabled

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mmaa");
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
        		for (Enumeration e=messages.elements();e.hasMoreElements();) {
        			String m = (String)e.nextElement();
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
        	Session session = owner.getSession();
        	
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
        	mail += "Version: "+Constants.VERSION+"."+Constants.BLD_NUMBER+" ("+Constants.REL_DATE+")\r\n";
        	mail += "TimeStamp: "+(new Date());
        	
        	Email email = new Email();
        	
        	email.sendMail(
        			(String)ApplicationProperties.getProperty("tmtbl.smtp.host", "smtp.purdue.edu"), 
        			(String)ApplicationProperties.getProperty("tmtbl.smtp.domain", "smtp.purdue.edu"), 
        			(String)ApplicationProperties.getProperty("tmtbl.inquiry.sender", "smasops@purdue.edu"), 
        			mgr.getEmailAddress(), 
        			(String)ApplicationProperties.getProperty("tmtbl.inquiry.email","smasops@purdue.edu"), 
        			"Timetabling (Solution commit): "+subject, 
        			mail, 
        			new Vector());
        	
		} catch (Exception e) {
			sLog.error("Unable to send solution commit/uncommit notification, reason: "+e.getMessage(),e);
		}
	}
	
	public void export(CSVFile file, User user) {
		String instructorFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
		file.setSeparator(",");
		file.setQuotationMark("\"");
		if (isCommited().booleanValue()) {
			file.setHeader(new CSVField[] {
					new CSVField("COURSE"),
					new CSVField("ITYPE"),
					new CSVField("SECTION"),
					new CSVField("SUFFIX"),
					new CSVField("DIV-SEC"),
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
				Vector leads = clazz.getLeadInstructors();
				StringBuffer leadsSb = new StringBuffer();
				for (Enumeration e=leads.elements();e.hasMoreElements();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
					leadsSb.append(instructor.getName(instructorFormat));
					if (e.hasMoreElements()) leadsSb.append(";");
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
							new CSVField(placement.getTimeLocation().getStartTimeHeader()),
							new CSVField(placement.getTimeLocation().getEndTimeHeader()),
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
							new CSVField(placement.getTimeLocation().getStartTimeHeader()),
							new CSVField(placement.getTimeLocation().getEndTimeHeader()),
							new CSVField(placement.getRoomName(",")),
							new CSVField(leadsSb),
							new CSVField(clazz.getSchedulePrintNote()==null?"":clazz.getSchedulePrintNote())
					});
				}
			} else {
				Class_ clazz = (Class_)o;
				Vector leads = clazz.getLeadInstructors();
				StringBuffer leadsSb = new StringBuffer();
				for (Enumeration e=leads.elements();e.hasMoreElements();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)e.nextElement();
					leadsSb.append(instructor.getName(instructorFormat));
					if (e.hasMoreElements()) leadsSb.append(";");
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
		Iterator idIterator = hibSession.createQuery(idQuery).setInteger("solutionId",getUniqueId().intValue()).iterate();
		StringBuffer ids = new StringBuffer();
		int idx = 0;
		while (idIterator.hasNext()) {
			ids.append(idIterator.next()); idx++;
			if (idx==100) {
				hibSession.createQuery("delete "+objectName+" as x where x.uniqueId in ("+ids+")").executeUpdate();
				ids = new StringBuffer();
				idx = 0;
			} else if (idIterator.hasNext()) {
				ids.append(",");
			}
		}
		if (idx>0)
			hibSession.createQuery("delete "+objectName+" as x where x.uniqueId in ("+ids+")").executeUpdate();
	}
	
	public void delete(org.hibernate.Session hibSession) {
		// NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		Iterator i = hibSession.createQuery(
				"select distinct c, oa from "+
				"ConstraintInfo c inner join c.assignments a, Assignment oa "+
				"where "+
				"a.solution.uniqueId=:solutionId and oa.solution.uniqueId!=:solutionId and oa in elements ( c.assignments) ")
				.setInteger("solutionId",getUniqueId().intValue())
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
		
        hibSession.createQuery(
				"delete StudentEnrollment x where x.solution.uniqueId=:solutionId ")
				.setLong("solutionId", getUniqueId().longValue())
				.executeUpdate();
		
		hibSession.createQuery(
				"delete JointEnrollment x where x.solution.uniqueId=:solutionId ")
				.setLong("solutionId", getUniqueId().longValue())
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
		
		hibSession.createQuery(
				"delete Assignment x where x.solution.uniqueId=:solutionId ")
				.setInteger("solutionId", getUniqueId().intValue())
				.executeUpdate();
		
		deleteObjects(
				hibSession,
				"SolverParameter",
				"select p.uniqueId from Solution s inner join s.parameters p where s.uniqueId=:solutionId"
				);

		hibSession.delete(this);
	}
	
	public void empty(org.hibernate.Session hibSession, TimetableInfoFileProxy proxy) {
		// NOTE: In order to decrease the amount of interaction between solutions persistance of committed student conflicts was disabled
		/*
		Iterator i = hibSession.createQuery(
				"select distinct c, oa from "+
				"ConstraintInfo c inner join c.assignments a, Assignment oa "+
				"where "+
				"a.solution.uniqueId=:solutionId and oa.solution.uniqueId!=:solutionId and oa in elements ( c.assignments) ")
				.setInteger("solutionId",getUniqueId().intValue())
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

		hibSession.createQuery(
				"delete StudentEnrollment x where x in "+
				" ( select s from StudentEnrollment s where s.solution.uniqueId=:solutionId ) ")
				.setInteger("solutionId", getUniqueId().intValue())
				.executeUpdate();
		
		hibSession.createQuery(
				"delete JointEnrollment x where x in "+
				" ( select j from JointEnrollment j where j.solution.uniqueId=:solutionId ) ")
				.setInteger("solutionId", getUniqueId().intValue())
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
		
		hibSession.createQuery(
				"delete Assignment x where x in "+
				" ( select a from Assignment a where a.solution.uniqueId=:solutionId ) ")
				.setInteger("solutionId", getUniqueId().intValue())
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
		
		hibSession.saveOrUpdate(this);

		hibSession.flush(); hibSession.clear();
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
			hibSession.saveOrUpdate(sInfo);
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
						hibSession.delete(c);
					}
				}
			}
		}
		
		SolverInfoDef defJenrlInfo = SolverInfoDef.findByName(hibSession,"JenrlInfo");
		Hashtable solverInfos = new Hashtable();
		AssignmentDAO adao = new AssignmentDAO(); 
		Query q = hibSession.createQuery(
				"select a.uniqueId, oa.uniqueId, count(*) from "+
				"Solution s inner join s.assignments a inner join s.studentEnrollments as e, "+
				"Solution os inner join os.assignments oa inner join os.studentEnrollments as oe "+
				"where "+
				"s.uniqueId=:solutionId and os.owner.session.uniqueId=:sessionId and os.owner.uniqueId!=:ownerId and os.commited=true and "+
				"a.clazz=e.clazz and oa.clazz=oe.clazz and a.clazz.schedulingSubpart!=oa.clazz.schedulingSubpart and e.studentId=oe.studentId "+
				"group by a.uniqueId, oa.uniqueId");
		q.setLong("ownerId",getOwner().getUniqueId().longValue());
		q.setInteger("solutionId",getUniqueId().intValue());
		q.setLong("sessionId",getOwner().getSession().getUniqueId().longValue());
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
	
	public Session getSession() {
		return getOwner().getSession();
	}
	
    public static Collection findBySessionId(Long sessionId) {
    	return (new SolutionDAO()).
    			getSession().
    			createQuery("select s from Solution s where s.owner.session.uniqueId=:sessionId").
    			setLong("sessionId", sessionId.longValue()).
    			//setCacheable(true).
    			list();
    }

    public static Collection findBySessionIdAndManagerId(Long sessionId, Long managerId) {
    	return (new SolutionDAO()).
    			getSession().
    			createQuery("select s from Solution s inner join s.owner sg inner join sg.timetableManagers m where sg.session.uniqueId=:sessionId and m.uniqueId=:managerId").
    			setLong("sessionId", sessionId.longValue()).
    			setLong("managerId", managerId.longValue()).
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
        assignments.addAll(new SolutionDAO().getSession().createQuery(
                "select distinct c from Class_ c, Solution s inner join s.owner.departments d "+
                "where s.uniqueId = :solutionId and c.managingDept=d and "+
                "c.uniqueId not in (select a.clazz.uniqueId from s.assignments a)").
                setLong("solutionId", getUniqueId().longValue()).
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
       						while (takenDivNums.contains(new Integer(divNum))) divNum++;
        				}
        			} else {
        				secNum++;
        			}
    			} else {
    				divNum++; secNum = 1;
    				while (takenDivNums.contains(new Integer(divNum))) divNum++;
    			}
            } else if (lastClazz!=null) {
                divNum++; secNum = 1;
                while (takenDivNums.contains(new Integer(divNum))) divNum++;
    		} else {
    			divNum = 1; secNum = 1;
    			while (takenDivNums.contains(new Integer(divNum))) divNum++;
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
    		hibSession.update(clazz);
    		
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
        			hibSession.update(clazz);
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
           				while (takenDivNums.contains(new Integer(divNum))) divNum++;
        			}
                } else if (lastClazz!=null) {
                    divNum++; secNum = 1;
                    while (takenDivNums.contains(new Integer(divNum))) divNum++;
                } else {
        			divNum = 1; secNum = 1;
        			while (takenDivNums.contains(new Integer(divNum))) divNum++;
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
        		hibSession.update(clazz);
        		
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
            			hibSession.update(clazz);
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
               				while (takenDivNums.contains(new Integer(divNum))) divNum++;
            			}
                    } else if (lastClazz!=null) {
                        divNum++; secNum = 1;
                        while (takenDivNums.contains(new Integer(divNum))) divNum++;
            		} else {
            			divNum = 1; secNum = 1;
            			while (takenDivNums.contains(new Integer(divNum))) divNum++;
            		}

            		if (divNum==100 && secNum==1) {
            			messages.add("Division number exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
            			sLog.warn("Division number still (fallback2) exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
            		}

            		clazz.setClassSuffix(sSufixFormat.format(divNum)+sSufixFormat.format(secNum));
            		hibSession.update(clazz);
            		
            		lastAssignment = assignment;
            		lastSubpart = clazz.getSchedulingSubpart();
            		lastClazz = clazz;
            	}
        	}
    	}
        
        /*
        lastSubpart = null;
        TreeSet otherClasses = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
        otherClasses.addAll(new SolutionDAO().getSession().createQuery(
                "select distinct c from Class_ c, Solution s inner join s.owner.departments d "+
                "where s.uniqueId = :solutionId and c.managingDept=d and "+
                "c.uniqueId not in (select a.clazz.uniqueId from s.assignments a) order by c.schedulingSubpart.uniqueId, c.sectionNumberCache").
                setLong("solutionId", getUniqueId().longValue()).
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
            while (takenDivNums.contains(new Integer(divNum)))
                divNum++;
            if (divNum==100) {
                messages.add("Division number exceeded 99 for scheduling subpart "+clazz.getSchedulingSubpart().getSchedulingSubpartLabel()+".");
            }
            clazz.setClassSuffix(sSufixFormat.format(divNum)+sSufixFormat.format(1));
            takenDivNums.add(new Integer(divNum));
            lastSubpart = clazz.getSchedulingSubpart();
            hibSession.update(clazz);
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
    	
    	HashSet classes2update = new HashSet();
    	for (Iterator i=classes.iterator();i.hasNext();) {
    		Class_ clazz = (Class_)i.next();
    		
    		int divNum = Integer.parseInt(clazz.getClassSuffix().substring(0,3));
    		clazz.setClassSuffix(null);
            
            subparts2fix.add(clazz.getSchedulingSubpart());
            
            hibSession.update(clazz);
    	}
    	
        List otherClasses = new SolutionDAO().getSession().createQuery(
                "select distinct c from Class_ c, Solution s inner join s.owner.departments d "+
                "where s.uniqueId = :solutionId and c.managingDept=d and "+
                "c.uniqueId not in (select a.clazz.uniqueId from s.assignments a)").
                setLong("solutionId", getUniqueId().longValue()).
                list();
        for (Iterator i=otherClasses.iterator();i.hasNext();) {
            Class_ clazz = (Class_)i.next();
            if (clazz.getClassSuffix()==null) continue;
            clazz.setClassSuffix(null);
            
            subparts2fix.add(clazz.getSchedulingSubpart());
            
            hibSession.update(clazz);
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
                                if (clazz.getClassSuffix()==null) continue;
                                int clazzDivNum = Integer.parseInt(clazz.getClassSuffix().substring(0,3));
                                int clazzSecNum = Integer.parseInt(clazz.getClassSuffix().substring(3,6));
                                if (clazzDivNum==div) {
                                    clazz.setClassSuffix(sSufixFormat.format(clazzDivNum-dec)+sSufixFormat.format(clazzSecNum));
                                    hibSession.update(clazz);
                                }
                            }
                        }
                    }

                }
            }
        }
    }    
    
    private transient Hashtable iAssignmentTable = null; 
    
	public Assignment getAssignment(Long classId) throws Exception {
		if (iAssignmentTable==null) {
			iAssignmentTable = new Hashtable();
			for (Iterator i=getAssignments().iterator();i.hasNext();) {
				Assignment a = (Assignment)i.next();
				iAssignmentTable.put(a.getClassId(),a);
			}
		}
		return (Assignment)iAssignmentTable.get(classId);
	}
	
	public Assignment getAssignment(Class_ clazz) throws Exception {
        if (!getOwner().getDepartments().contains(clazz.getManagingDept()))
            return clazz.getCommittedAssignment();
		return getAssignment(clazz.getUniqueId());
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) throws Exception {
		return getAssignmentInfo(clazz.getUniqueId());
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId) throws Exception {
		Assignment a = getAssignment(classId);
    	return (a==null?null:(AssignmentPreferenceInfo)a.getAssignmentInfo("AssignmentInfo"));
	}
	
	public Hashtable getAssignmentTable(Collection classesOrClassIds) throws Exception {
		Hashtable assignments = new Hashtable();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			Assignment assignment = (classOrClassId instanceof Class_ ? getAssignment((Class_)classOrClassId) : getAssignment((Long)classOrClassId));
			if (assignment!=null)
				assignments.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, assignment);
		}
		return assignments;
	}
	
	public Hashtable getAssignmentInfoTable(Collection classesOrClassIds) throws Exception {
		Hashtable infos = new Hashtable();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			AssignmentPreferenceInfo info = (classOrClassId instanceof Class_ ? getAssignmentInfo((Class_)classOrClassId) : getAssignmentInfo((Long)classOrClassId));
			if (info!=null)
				infos.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, info);
		}
		return infos;
	}

	private transient DataProperties iPropertiesCache = null;
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
        SolutionDAO dao = new SolutionDAO();
        org.hibernate.Session hibSession = dao.getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory(); 
        hibSessionFactory.evict(Solution.class, solutionId);
        hibSessionFactory.evictCollection(Solution.class.getName()+".parameters", solutionId);
        hibSessionFactory.evictCollection(Solution.class.getName()+".assignments", solutionId);
        for (Iterator i=hibSession.createQuery("select c.uniqueId from "+
                    "Class_ c, Solution s where s.uniqueId=:solutionId and "+
                    "c.managingDept.uniqueId in (s.owner.departments.uniqueId)").
                    setLong("solutionId", solutionId.longValue()).iterate(); i.hasNext();) {
            Number classId = (Number)i.next();
            hibSession.getSessionFactory().evict(Class_.class, classId);
            hibSessionFactory.evictCollection(Class_.class.getName()+".assignments", classId);
        }
   }
    
    public static boolean hasTimetable(Long sessionId) {
        return ((Number)new SolutionDAO().getSession().
                createQuery("select count(s) from Solution s " +
                        "where s.owner.session.uniqueId=:sessionId and " +
                        "s.commited = true").
                setLong("sessionId",sessionId).uniqueResult()).longValue()>0;
    }
}
