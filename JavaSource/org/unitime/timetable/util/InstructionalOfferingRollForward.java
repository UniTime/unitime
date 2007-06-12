/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.ArrangeCreditUnitConfig;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;

/**
 * 
 * @author Stephanie Schluttenhofer
 *
 */
public class InstructionalOfferingRollForward extends SessionRollForward {
	
	
	public void rollForwardInstructionalOfferingsForASubjectArea(String subjectAreaAbbreviation, Session fromSession, Session toSession){
		CourseOfferingDAO coDao = new CourseOfferingDAO();
		String query = "from CourseOffering as co where co.subjectArea.subjectAreaAbbreviation = '" + subjectAreaAbbreviation
			+ "' and co.isControl = 1"
			+ " and co.subjectArea.session.uniqueId = " + fromSession.getUniqueId();
		List l = coDao.getQuery(query).list();
		if (l != null){
			CourseOffering co = null;
			for (Iterator it = l.iterator(); it.hasNext();){
				co = (CourseOffering) it.next();
				rollForwardInstructionalOffering(co.getInstructionalOffering(), fromSession, toSession);
			}
		}
		if (sessionHasCourseCatalog(toSession)){
			query = "select cc2.* from CourseCatalog cc2";
			query += " where cc2.session.uniqueId=:sessionId";
			query += "  and cc2.uniqueId not in ";
			query += " (select distinct cc.* from CourseCatalog cc, CourseOffering co";
			query += "  where co.subjectArea.session.uniqueId=:sessionId";
			query += "  and co.subjectArea.subjectAreaAbbreviation=:subjectAbbv";
			query += "  and cc.session.uniqueId=:sessionId";
			query += "  and cc.subject=:subjectAbbv";
			query += "  and cc.courseNumber = co.courseNumber)";
			l = coDao.getQuery(query).list();
			if (l != null){
				CourseCatalog cc = null;
				for (Iterator ccIt = l.iterator(); ccIt.hasNext();){
					cc = (CourseCatalog) ccIt.next();
					addInstructionalOffering(cc, toSession);
				}
			}
		}
		coDao.getSession().clear();
	}
	
	private void addInstructionalOffering(CourseCatalog courseCatalogEntry, Session toSession) {
		InstructionalOffering instructionalOffering = createToInstructionalOfferingFromCourseCatalog(courseCatalogEntry, toSession);
		if (instructionalOffering != null){
			CourseOffering courseOffering = createToCourseOfferingFromCourseCatalog(courseCatalogEntry, toSession);
			courseOffering.setInstructionalOffering(instructionalOffering);
			instructionalOffering.addTocourseOfferings(courseOffering);
			(new InstructionalOfferingDAO()).save(instructionalOffering); 
		}
	}

	public void rollForwardInstructionalOfferingsForACourseOffering(String subjectAreaAbbreviation, String courseNumber, Session fromSession, Session toSession){
		CourseOfferingDAO coDao = new CourseOfferingDAO();
		String query = "from CourseOffering as co where co.subjectArea.subjectAreaAbbreviation = '" + subjectAreaAbbreviation
		+ "' and co.getCourseNbr = '" + courseNumber
		+ "' and co.isControl = 1"
		+ " and co.subjectArea.session.uniqueId = " + fromSession.getUniqueId();
		List l = coDao.getQuery(query).list();
		if (l != null && l.size() > 0){
			CourseOffering co = null;
			for (Iterator it = l.iterator(); it.hasNext();){
				co = (CourseOffering) it.next();
				rollForwardInstructionalOffering(co.getInstructionalOffering(), fromSession, toSession);
			}
		}
	}

	public void rollForwardInstructionalOffering(InstructionalOffering fromInstructionalOffering, Session fromSession, Session toSession){
		InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
		org.hibernate.Session hibSession = ioDao.getSession();
		Transaction trns = null;
		try {
			trns = hibSession.beginTransaction();
			InstructionalOffering toInstructionalOffering = findToInstructionalOffering(fromInstructionalOffering, toSession);
			if (toInstructionalOffering == null){
				trns.commit();
				return;
			}
			if (toInstructionalOffering.getInstrOfferingConfigs() != null && toInstructionalOffering.getInstrOfferingConfigs().size() > 0){
				toInstructionalOffering.getInstrOfferingConfigs().clear();
			}
			toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
			InstrOfferingConfig fromInstrOffrConfig = null;
			InstrOfferingConfig toInstrOffrConfig = null;
			if (fromInstructionalOffering.getInstrOfferingConfigs() != null && fromInstructionalOffering.getInstrOfferingConfigs().size() > 0){
				for (Iterator it = fromInstructionalOffering.getInstrOfferingConfigs().iterator(); it.hasNext();){
					fromInstrOffrConfig = (InstrOfferingConfig) it.next();
					toInstrOffrConfig = new InstrOfferingConfig();
					toInstrOffrConfig.setLimit(fromInstrOffrConfig.getLimit());
					toInstrOffrConfig.setInstructionalOffering(toInstructionalOffering);
					toInstrOffrConfig.setName(fromInstrOffrConfig.getName());
					toInstrOffrConfig.setUnlimitedEnrollment(fromInstrOffrConfig.isUnlimitedEnrollment());
					toInstrOffrConfig.setUniqueIdRolledForwardFrom(fromInstrOffrConfig.getUniqueId());
					toInstructionalOffering.addToinstrOfferingConfigs(toInstrOffrConfig);
					hibSession.saveOrUpdate(toInstrOffrConfig);
					hibSession.saveOrUpdate(toInstructionalOffering);
					rollForwardSchedSubpartsForAConfig(fromInstrOffrConfig, toInstrOffrConfig, hibSession, toSession);
				}
			}
			trns.commit();
		} catch (Exception e){
			Debug.info(e.getMessage());
			e.printStackTrace();
			if (trns != null){
				trns.rollback();
			}
		}		
	}
	
	private void rollForwardCourseCreditUnitConfigForSchedSubpart(SchedulingSubpart fromSubpart, SchedulingSubpart toSubpart){
		if (sessionHasCourseCatalog(toSubpart.getSession())){
			if (fromSubpart.getParentSubpart() != null && !fromSubpart.getParentSubpart().getItype().getItype().equals(fromSubpart.getItype().getItype())){
				CourseCatalog courseCatalogEntry = CourseCatalog.findCourseInCatalogForSession(toSubpart.getControllingCourseOffering(), toSubpart.getSession());
				if (courseCatalogEntry != null && courseCatalogEntry.getSubparts() != null){
					CourseSubpartCredit csc = null;
					boolean found = false;
					for (Iterator cscIt = courseCatalogEntry.getSubparts().iterator(); (cscIt.hasNext() && !found);){
						csc = (CourseSubpartCredit) cscIt.next();
						if (csc.getSubpartId().equals(toSubpart.getItype().getItype().toString())){
							found = true;
						}
					}
					if (found){
						CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(csc.getCreditFormat(), csc.getCreditType(), csc.getCreditUnitType(), csc.getFixedMinimumCredit(), csc.getMaximumCredit(), csc.isFractionalCreditAllowed(), new Boolean(true));
						ccuc.setOwner(toSubpart);
						toSubpart.setCredit(ccuc);
					}
				}
			}	
		} else if (fromSubpart.getCredit() != null){
			Float units = null;
			Float maxUnits = null;
			Boolean fractionalIncrementsAllowed = null;
			if (fromSubpart.getCredit() instanceof FixedCreditUnitConfig) {
				FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) fromSubpart.getCredit();
				units = fcuc.getFixedUnits();
			} else if (fromSubpart.getCredit() instanceof VariableFixedCreditUnitConfig) {
				VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) fromSubpart.getCredit();
				units = vfcuc.getMinUnits();
				maxUnits = vfcuc.getMaxUnits();
				
			} else if (fromSubpart.getCredit() instanceof VariableRangeCreditUnitConfig) {
				VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) fromSubpart.getCredit();
				units = vrcuc.getMinUnits();
				maxUnits = vrcuc.getMaxUnits();
				fractionalIncrementsAllowed = vrcuc.isFractionalIncrementsAllowed();
			}
			CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(fromSubpart.getCredit().getCreditFormat(), fromSubpart.getCredit().getCreditType(), fromSubpart.getCredit().getCreditUnitType(), units, maxUnits, fractionalIncrementsAllowed, new Boolean(false));
			ccuc.setOwner(toSubpart);
			toSubpart.setCredit(ccuc);
		}	
	}
	
	private Class_ rollForwardClass(Class_ fromClass,SchedulingSubpart toSubpart, Session toSession){
		Class_ toClass = new Class_();
		toClass.setDisplayInScheduleBook(fromClass.isDisplayInScheduleBook());
		toClass.setDisplayInstructor(fromClass.isDisplayInstructor());
		toClass.setExpectedCapacity(fromClass.getExpectedCapacity());
		toClass.setMaxExpectedCapacity(fromClass.getMaxExpectedCapacity());
		toClass.setNbrRooms(fromClass.getNbrRooms());
		toClass.setNotes(fromClass.getNotes());
		toClass.setRoomRatio(fromClass.getRoomRatio());
		toClass.setSchedulePrintNote(fromClass.getSchedulePrintNote());
		toClass.setSchedulingSubpart(toSubpart);
		toClass.setUniqueIdRolledForwardFrom(fromClass.getUniqueId());
		toSubpart.addToclasses(toClass);
		if (fromClass.getManagingDept() != null && !fromClass.getManagingDept().equals(fromClass.getControllingDept())){
			toClass.setManagingDept(Department.findByDeptCode(fromClass.getManagingDept().getDeptCode(), toSession.getUniqueId()));
		}
		if (fromClass.getDatePattern() != null){
			toClass.setDatePattern(DatePattern.findByName(toSession, fromClass.getDatePattern().getName()));
		}
		return(toClass);
	}
	
	private void rollForwardSchedulingSubpart(InstrOfferingConfig toInstrOffrConfig, SchedulingSubpart fromSubpart, RollForwardSchedSubpart parentSubpart, org.hibernate.Session hibSession, Session toSession) throws Exception{
		SchedulingSubpart toSubpart = new SchedulingSubpart();
		toSubpart.setAutoSpreadInTime(fromSubpart.isAutoSpreadInTime());
		toSubpart.setStudentAllowOverlap(fromSubpart.isStudentAllowOverlap());
		toSubpart.setInstrOfferingConfig(toInstrOffrConfig);
		toInstrOffrConfig.addToschedulingSubparts(toSubpart);
		toSubpart.setMinutesPerWk(fromSubpart.getMinutesPerWk());
		toSubpart.setItype(fromSubpart.getItype());
		toSubpart.setUniqueIdRolledForwardFrom(fromSubpart.getUniqueId());
		
		rollForwardCourseCreditUnitConfigForSchedSubpart(fromSubpart, toSubpart);
		if (fromSubpart.getDatePattern() != null){
			toSubpart.setDatePattern(DatePattern.findByName(toSession, fromSubpart.getDatePattern().getName()));
		}
		
		RollForwardSchedSubpart rfSs = new RollForwardSchedSubpart();
		rfSs.setFromSubpart(fromSubpart);
		rfSs.setToSubpart(toSubpart);

		if (parentSubpart != null){
			rfSs.setFromParentSubpart(parentSubpart.getFromSubpart());
			parentSubpart.addToFromChildSubparts(fromSubpart);
			rfSs.setToParentSubpart(parentSubpart.getToSubpart());
			parentSubpart.addToToChildSubparts(toSubpart);			
			toSubpart.setParentSubpart(parentSubpart.getToSubpart());
			parentSubpart.getToSubpart().addTochildSubparts(toSubpart);
		}
				
		hibSession.update(toInstrOffrConfig);
		if (fromSubpart.getClasses() != null && fromSubpart.getClasses().size() > 0){
			Class_ c = null;
			Class_ newC = null;
			for (Iterator it = fromSubpart.getClasses().iterator(); it.hasNext();){
				c = (Class_) it.next();
				newC = rollForwardClass(c, toSubpart, toSession);
				RollForwardClass rfc = new RollForwardClass();
				rfc.setToClass(newC);
				rfc.setFromClass(c);
				rfc.setFromParentClass(c.getParentClass());
				rfc.setParentSubpart(rfSs);
				rfSs.addToRollForwardClasses(rfc);
				if (c.getChildClasses() != null && c.getChildClasses().size() > 0){
					for (Iterator ccIt = c.getChildClasses().iterator(); ccIt.hasNext();){
						rfc.addToLastLikeChildClasses(ccIt.next());
					}
				}
				if (parentSubpart != null){
					Class_ parentClass = parentSubpart.findParentClassMatchingFromParentClass(c.getParentClass());
					newC.setParentClass(parentClass);
					parentClass.addTochildClasses(newC);
				}
			}
		}
		hibSession.update(toInstrOffrConfig);
		rollForwardTimePrefs(fromSubpart, toSubpart);
		rollForwardBuildingPrefs(fromSubpart, toSubpart, toSession);
		rollForwardRoomPrefs(fromSubpart, toSubpart, toSession);
		rollForwardRoomGroupPrefs(fromSubpart, toSubpart);
		rollForwardRoomFeaturePrefs(fromSubpart, toSubpart);
		if (fromSubpart.getChildSubparts() != null && fromSubpart.getChildSubparts().size() > 0){
			SchedulingSubpart childSubpart = null;
			for(Iterator it = fromSubpart.getChildSubparts().iterator(); it.hasNext();){
				childSubpart = (SchedulingSubpart) it.next();
				rollForwardSchedulingSubpart(toInstrOffrConfig, childSubpart, rfSs, hibSession,toSession);
			}
		}
	}
	
	private void rollForwardSchedSubpartsForAConfig(InstrOfferingConfig ioc, InstrOfferingConfig newIoc, org.hibernate.Session hibSession, Session toSession) throws Exception{
		if (ioc.getSchedulingSubparts() != null && ioc.getSchedulingSubparts().size() > 0){
			SchedulingSubpart ss = null;
			for(Iterator it = ioc.getSchedulingSubparts().iterator(); it.hasNext();){
				ss = (SchedulingSubpart) it.next();
				if (ss.getParentSubpart() == null){
					rollForwardSchedulingSubpart(newIoc, ss, null, hibSession,toSession);
				}
			}
		}
	}
		
	private InstructionalOffering createToInstructionalOfferingFromFromInstructionalOffering(InstructionalOffering fromInstructionalOffering, Session toSession){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		InstructionalOffering toInstructionalOffering = new InstructionalOffering();
		toInstructionalOffering.setConsentType(fromInstructionalOffering.getConsentType());
		toInstructionalOffering.setDesignatorRequired(fromInstructionalOffering.isDesignatorRequired());
		toInstructionalOffering.setNotOffered(fromInstructionalOffering.isNotOffered());
		toInstructionalOffering.setSession(toSession);
		toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
		if(fromInstructionalOffering.getCreditConfigs() != null && !fromInstructionalOffering.getCreditConfigs().isEmpty()){
			CourseCreditUnitConfig ccuc = null;
			for(Iterator ccIt = fromInstructionalOffering.getCreditConfigs().iterator(); ccIt.hasNext();){
				ccuc = (CourseCreditUnitConfig) ccIt.next();
				if (ccuc instanceof ArrangeCreditUnitConfig) {
					ArrangeCreditUnitConfig fromAcuc = (ArrangeCreditUnitConfig) ccuc;
					ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)fromAcuc.clone();
					toAcuc.setOwner(toInstructionalOffering);
					toInstructionalOffering.addTocreditConfigs(toAcuc);
				} else if (ccuc instanceof FixedCreditUnitConfig) {
					FixedCreditUnitConfig fromFcuc = (FixedCreditUnitConfig) ccuc;
					FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) fromFcuc.clone();
					toFcuc.setOwner(toInstructionalOffering);
					toInstructionalOffering.addTocreditConfigs(toFcuc);
				} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
					VariableRangeCreditUnitConfig fromVrcuc = (VariableRangeCreditUnitConfig) ccuc;
					VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) fromVrcuc.clone();
					toVrcuc.setOwner(toInstructionalOffering);
					toInstructionalOffering.addTocreditConfigs(toVrcuc);
				} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
					VariableFixedCreditUnitConfig fromVfcuc = (VariableFixedCreditUnitConfig) ccuc;
					VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) fromVfcuc.clone();
					toVfcuc.setOwner(toInstructionalOffering);
					toInstructionalOffering.addTocreditConfigs(toVfcuc);
				}
			}
		}
		CourseOffering fromCourseOffering = null;
		CourseOffering toCourseOffering = null;
		for(Iterator coIt = fromInstructionalOffering.getCourseOfferings().iterator(); coIt.hasNext();){
			fromCourseOffering = (CourseOffering) coIt.next();
			toCourseOffering = new CourseOffering();
			toCourseOffering.setSubjectArea(fromCourseOffering.getSubjectArea().findSameSubjectAreaInSession(toSession));
			toCourseOffering.setCourseNbr(fromCourseOffering.getCourseNbr());
			toCourseOffering.setIsControl(fromCourseOffering.isIsControl());
			toCourseOffering.setExternalUniqueId(fromCourseOffering.getExternalUniqueId());
			toCourseOffering.setNbrExpectedStudents(fromCourseOffering.getNbrExpectedStudents());
			toCourseOffering.setPermId(fromCourseOffering.getPermId());
			toCourseOffering.setScheduleBookNote(fromCourseOffering.getScheduleBookNote());
			toCourseOffering.setTitle(fromCourseOffering.getTitle());
			toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
			toCourseOffering.setInstructionalOffering(toInstructionalOffering);
			toInstructionalOffering.addTocourseOfferings(toCourseOffering);
		}
		(new InstructionalOfferingDAO()).save(toInstructionalOffering); 
		return(toInstructionalOffering);
		
	}
	
	private InstructionalOffering createToInstructionalOfferingFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session){
		if (courseCatalogEntry == null || session == null){
			return(null);
		}
		InstructionalOffering toInstructionalOffering = new InstructionalOffering();
		toInstructionalOffering.setConsentType(OfferingConsentType.getOfferingConsentTypeForReference(courseCatalogEntry.getApprovalType()));
		toInstructionalOffering.setDesignatorRequired(courseCatalogEntry.isDesignatorRequired());
		toInstructionalOffering.setNotOffered(new Boolean(false));
		toInstructionalOffering.setSession(session);
		if(courseCatalogEntry.getCreditType() != null){
			CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseCatalogEntry.getCreditFormat(), courseCatalogEntry.getCreditType(), courseCatalogEntry.getCreditUnitType(), courseCatalogEntry.getFixedMinimumCredit(), courseCatalogEntry.getMaximumCredit(), courseCatalogEntry.isFractionalCreditAllowed(), new Boolean(true));
			if (ccuc instanceof ArrangeCreditUnitConfig) {					
				ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)ccuc;
				toAcuc.setOwner(toInstructionalOffering);
				toInstructionalOffering.addTocreditConfigs(toAcuc);
			} else if (ccuc instanceof FixedCreditUnitConfig) {
				FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) ccuc;
				toFcuc.setOwner(toInstructionalOffering);
				toInstructionalOffering.addTocreditConfigs(toFcuc);
			} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
				VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) ccuc;
				toVrcuc.setOwner(toInstructionalOffering);
				toInstructionalOffering.addTocreditConfigs(toVrcuc);
			} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
				VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) ccuc;
				toVfcuc.setOwner(toInstructionalOffering);
				toInstructionalOffering.addTocreditConfigs(toVfcuc);
			}
		}
		return(toInstructionalOffering);
	}
	private CourseOffering createToCourseOfferingFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session){
		if (courseCatalogEntry == null || session == null){
			return(null);
		}
		CourseOffering toCourseOffering = new CourseOffering();
		toCourseOffering = new CourseOffering();
		toCourseOffering.setSubjectArea(SubjectArea.findByAbbv(session.getUniqueId(), courseCatalogEntry.getSubject()));
		toCourseOffering.setCourseNbr(courseCatalogEntry.getCourseNumber());
		toCourseOffering.setIsControl(new Boolean(true));
		toCourseOffering.setExternalUniqueId(courseCatalogEntry.getExternalUniqueId());
		toCourseOffering.setPermId(courseCatalogEntry.getPermanentId());
		toCourseOffering.setTitle(courseCatalogEntry.getTitle());
		toCourseOffering.setNbrExpectedStudents(new Integer(0));
		return(toCourseOffering);
	}
	
	private InstructionalOffering createToInstructionalOfferingBasedOnCourseCatalog(InstructionalOffering fromInstructionalOffering, Session toSession){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		
		CourseCatalog controllingCourseCatalogEntry = CourseCatalog.findCourseFromPreviousSessionInCatalogForSession(fromInstructionalOffering.getControllingCourseOffering(), toSession);
		InstructionalOffering toInstructionalOffering = createToInstructionalOfferingFromCourseCatalog(controllingCourseCatalogEntry, toSession);
	
		SubjectArea sa = SubjectArea.findByAbbv(toSession.getUniqueId(), controllingCourseCatalogEntry.getSubject());
		InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
		CourseOffering fromCourseOffering = null;
		CourseOffering toCourseOffering = null;
		CourseCatalog courseCatalogEntry = null;
		for(Iterator coIt = fromInstructionalOffering.getCourseOfferings().iterator(); coIt.hasNext();){
			fromCourseOffering = (CourseOffering) coIt.next();
			courseCatalogEntry = CourseCatalog.findCourseFromPreviousSessionInCatalogForSession(fromInstructionalOffering.getControllingCourseOffering(), toSession);
			if (courseCatalogEntry != null){
				toCourseOffering = createToCourseOfferingFromCourseCatalog(courseCatalogEntry, toSession);
				toCourseOffering.setIsControl(fromCourseOffering.isIsControl());
				toCourseOffering.setScheduleBookNote(fromCourseOffering.getScheduleBookNote());
				toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
				toCourseOffering.setNbrExpectedStudents(fromCourseOffering.getNbrExpectedStudents());
				toCourseOffering.setInstructionalOffering(toInstructionalOffering);
				toInstructionalOffering.addTocourseOfferings(toCourseOffering);
			}
		}
		(new InstructionalOfferingDAO()).save(toInstructionalOffering); 
		return(toInstructionalOffering);		
	}

	
	private InstructionalOffering findToInstructionalOffering(InstructionalOffering fromInstructionalOffering, Session toSession){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		if (sessionHasCourseCatalog(toSession)){
			return(createToInstructionalOfferingBasedOnCourseCatalog(fromInstructionalOffering, toSession));
		} else {
			return(createToInstructionalOfferingFromFromInstructionalOffering(fromInstructionalOffering, toSession));
		}
		
	}
	
	private class RollForwardSchedSubpart{
		private SchedulingSubpart fromParentSubpart;
		private SchedulingSubpart toParentSubpart;
		private SchedulingSubpart fromSubpart;
		private SchedulingSubpart toSubpart;
		private List fromChildSubparts;
		private List toChildSubparts;
		private List rollForwardClasses;
		
		public List getFromChildSubparts() {
			return fromChildSubparts;
		}
		public Class_ findParentClassMatchingFromParentClass(Class_ fromParentClass) {
			if (getRollForwardClasses() != null && getRollForwardClasses().size() > 0){
				RollForwardClass rfc = null;
				for(Iterator it = getRollForwardClasses().iterator(); it.hasNext();){
					rfc = (RollForwardClass) it.next();
					if (rfc.getFromClass().equals(fromParentClass)){
						return(rfc.getToClass());
					}
				}
			}
			return null;
		}
		public void setFromChildSubparts(List fromChildSubparts) {
			this.fromChildSubparts = fromChildSubparts;
		}
		public void addToFromChildSubparts(SchedulingSubpart fromChildSubpart){
			if (fromChildSubparts == null){
				fromChildSubparts = new ArrayList();
			}
			fromChildSubparts.add(fromChildSubpart);
		}
		public SchedulingSubpart getFromParentSubpart() {
			return fromParentSubpart;
		}
		public void setFromParentSubpart(SchedulingSubpart fromParentSubpart) {
			this.fromParentSubpart = fromParentSubpart;
		}
		public SchedulingSubpart getFromSubpart() {
			return fromSubpart;
		}
		public void setFromSubpart(SchedulingSubpart fromSubpart) {
			this.fromSubpart = fromSubpart;
		}
		public List getToChildSubparts() {
			return toChildSubparts;
		}
		public void setToChildSubparts(List toChildSubparts) {
			this.toChildSubparts = toChildSubparts;
		}
		public void addToToChildSubparts(SchedulingSubpart toChildSubpart){
			if (toChildSubparts == null){
				toChildSubparts = new ArrayList();
			}
			toChildSubparts.add(toChildSubpart);
		}
		public SchedulingSubpart getToParentSubpart() {
			return toParentSubpart;
		}
		public void setToParentSubpart(SchedulingSubpart toParentSubpart) {
			this.toParentSubpart = toParentSubpart;
		}
		public SchedulingSubpart getToSubpart() {
			return toSubpart;
		}
		public void setToSubpart(SchedulingSubpart toSubpart) {
			this.toSubpart = toSubpart;
		}
		public List getRollForwardClasses() {
			return rollForwardClasses;
		}
		public void setRollForwardClasses(List rollForwardClasses) {
			this.rollForwardClasses = rollForwardClasses;
		}
		public void addToRollForwardClasses(RollForwardClass rollForwardClass){
			if (rollForwardClasses == null){
				rollForwardClasses = new ArrayList();
			}
			rollForwardClasses.add(rollForwardClass);
		}
	}
	
	private class RollForwardClass {
		private Class_ fromParentClass;
		private Class_ toParentClass;
		private Class_ fromClass;
		private Class_ toClass;
		private List fromChildClasses;
		private List toChildClasses;
		private RollForwardSchedSubpart parentSubpart;

		public List getFromChildClasses() {
			return fromChildClasses;
		}
		public void setFromChildClasses(List lastLikeChildClasses) {
			this.fromChildClasses = lastLikeChildClasses;
		}
		public void addToLastLikeChildClasses(Object fromChildClass){
			if (fromChildClasses == null){
				fromChildClasses = new ArrayList();
			}
			fromChildClasses.add(fromChildClass);
		}
		public Class_ getFromClass() {
			return fromClass;
		}
		public void setFromClass(Class_ fromClass) {
			this.fromClass = fromClass;
		}
		public Class_ getFromParentClass() {
			return fromParentClass;
		}
		public void setFromParentClass(Class_ fromParentClass) {
			this.fromParentClass = fromParentClass;
		}
		public List getToChildClasses() {
			return toChildClasses;
		}
		public void setToChildClasses(List newChildClasses) {
			this.toChildClasses = newChildClasses;
		}
		public void addToNewChildClasses(Class_ toChildClass){
			if (toChildClasses == null){
				toChildClasses = new ArrayList();
			}
			toChildClasses.add(toChildClass);
		}
		public Class_ getToClass() {
			return toClass;
		}
		public void setToClass(Class_ toClass) {
			this.toClass = toClass;
		}
		public Class_ getToParentClass() {
			return toParentClass;
		}
		public void setToParentClass(Class_ toParentClass) {
			this.toParentClass = toParentClass;
		}
		public RollForwardSchedSubpart getParentSubpart() {
			return parentSubpart;
		}
		public void setParentSubpart(RollForwardSchedSubpart parentSubpart) {
			this.parentSubpart = parentSubpart;
		}
		
	}
	
}
