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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.Transaction;
import org.unitime.timetable.defaults.ApplicationProperty;
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
import org.unitime.timetable.model.dao.ItypeDescDAO;

/**
 * 
 * @author Stephanie Schluttenhofer, Tomas Muller
 *
 */
public class InstructionalOfferingRollForward extends SessionRollForward {
	
	InstructionalOfferingRollForward(Log log) {
		super(log);
	}
	
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
	}
	public void rollForwardExpiredInstructionalOfferingsForASubjectArea(String subjectAreaAbbreviation, Session fromSession, Session toSession){
		CourseOfferingDAO coDao = new CourseOfferingDAO();
		String query = "select co from CourseOffering co"
				+ " where co.subjectArea.subjectAreaAbbreviation = '" + subjectAreaAbbreviation + "'"
				+ "  and co.subjectArea.session.uniqueId = " + fromSession.getUniqueId().longValue()
				+ "  and co.isControl = 1"
				+ "  and co.instructionalOffering.notOffered = false"
				+ "  and 0 = (select count(cc) from CourseCatalog cc"
				+ " where cc.session.uniqueId = " + toSession.getUniqueId().longValue()
                + "  and cc.subject = co.subjectArea.subjectAreaAbbreviation"
                + "  and (cc.courseNumber = co.courseNbr or cc.previousCourseNumber= co.courseNbr))"
                + " and 0 = (select count(co2) from CourseOffering co2"
                + "  where co2.subjectArea.session.uniqueId = " + toSession.getUniqueId().longValue()
                + "   and co2.subjectArea.subjectAreaAbbreviation = co.subjectArea.subjectAreaAbbreviation"
                + "   and co2.courseNbr = co.courseNbr)";
		List l = coDao.getQuery(query).list();
		if (l != null){
			CourseOffering co = null;
			for (Iterator it = l.iterator(); it.hasNext();){
				co = (CourseOffering) it.next();
				if ((co.getSubjectArea().getSubjectAreaAbbreviation().equals("ECE")&& co.getCourseNbr().equals("495E"))
					|| (co.getSubjectArea().getSubjectAreaAbbreviation().equals("AMST")&& co.getCourseNbr().equals("650A"))
					|| (co.getSubjectArea().getSubjectAreaAbbreviation().equals("FLL")&& co.getCourseNbr().equals("650T")))
					continue;
				rollForwardInstructionalOffering(co.getInstructionalOffering(), fromSession, toSession);
			}
		}
		coDao.getSession().clear();
	}
	
	public void rollForwardInstructionalOfferingForACourseOffering(CourseOffering co, Session fromSession, Session toSession){
		CourseOfferingDAO coDao = new CourseOfferingDAO();
		
		if (co != null){
				rollForwardInstructionalOffering(co.getInstructionalOffering(), fromSession, toSession);
		}
		coDao.getSession().clear();
	}

	
	public void addNewInstructionalOfferingsForASubjectArea(
			String subjectAreaAbbreviation, Session toSession) {
		CourseOfferingDAO coDao = new CourseOfferingDAO();
		if (sessionHasCourseCatalog(toSession)){
		    String query = "select cc2 from CourseCatalog cc2";
			query += " where cc2.session.uniqueId=:sessionId";
			query += "  and cc2.subject = :subjectAbbv";
			query += "  and cc2.uniqueId not in ";
			query += " (select distinct cc.uniqueId from CourseCatalog cc, CourseOffering co";
			query += "  where co.subjectArea.session.uniqueId=:sessionId";
			query += "  and co.subjectArea.subjectAreaAbbreviation=:subjectAbbv";
			query += "  and cc.session.uniqueId=:sessionId";
			query += "  and cc.subject=:subjectAbbv";
			query += "  and cc.courseNumber = co.courseNbr)";
		    List l = coDao.getQuery(query)
				.setString("subjectAbbv", subjectAreaAbbreviation)
				.setLong("sessionId", toSession.getUniqueId())
				.list();
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
		iLog.info("Creating " + courseCatalogEntry.getSubject() + " " + courseCatalogEntry.getCourseNumber());
		InstructionalOffering instructionalOffering = createToInstructionalOfferingFromCourseCatalog(courseCatalogEntry, toSession);
		if (instructionalOffering != null){
			CourseOffering courseOffering = createToCourseOfferingFromCourseCatalog(courseCatalogEntry, toSession);
			courseOffering.setInstructionalOffering(instructionalOffering);
			instructionalOffering.setNotOffered(new Boolean(true));
			instructionalOffering.addTocourseOfferings(courseOffering);
			InstrOfferingConfig instrOffrConfig = createToInstrOfferingConfigFromCourseCatalog(courseCatalogEntry, toSession);
			if (instrOffrConfig != null){
				instructionalOffering.addToinstrOfferingConfigs(instrOffrConfig);
				instrOffrConfig.setInstructionalOffering(instructionalOffering);
			}
			if (instructionalOffering.getInstrOfferingPermId() == null){
				instructionalOffering.generateInstrOfferingPermId();
			}
			InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
			ioDao.saveOrUpdate(instructionalOffering);
			ioDao.getSession().flush();
			ioDao.getSession().evict(instructionalOffering);
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
		iLog.info("Rolling " + fromInstructionalOffering.getCourseNameWithTitle());
		Transaction trns = null;
		try {
			trns = hibSession.beginTransaction();
			InstructionalOffering toInstructionalOffering = findToInstructionalOffering(fromInstructionalOffering, toSession, hibSession);
			if (toInstructionalOffering == null){
				return;
			}
			if (toInstructionalOffering.getInstrOfferingConfigs() != null && toInstructionalOffering.getInstrOfferingConfigs().size() > 0){
				toInstructionalOffering.getInstrOfferingConfigs().clear();
			}
			toInstructionalOffering.setNotOffered(fromInstructionalOffering.isNotOffered());
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
					hibSession.update(toInstructionalOffering);
					rollForwardSchedSubpartsForAConfig(fromInstrOffrConfig, toInstrOffrConfig, hibSession, toSession);
					hibSession.update(toInstructionalOffering);
				}
			}
			if (trns.isActive()) {
				trns.commit();
			}
			hibSession.flush();
			hibSession.evict(toInstructionalOffering);
			hibSession.evict(fromInstructionalOffering);
		} catch (Exception e){
			iLog.error("Failed to roll " + fromInstructionalOffering.getCourseName(), e);
			if (trns != null){
				if (trns.isActive()){
					trns.rollback();
				}
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
	
	private Class_ rollForwardClass(Class_ fromClass,SchedulingSubpart toSubpart, Session toSession, org.hibernate.Session hibSession) throws Exception{
		Class_ toClass = new Class_();
		toClass.setEnabledForStudentScheduling(fromClass.isEnabledForStudentScheduling());
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
			DatePattern toDp = DatePattern.findByName(toSession, fromClass.getDatePattern().getName());
			if (toDp == null){
				toDp = fromClass.getDatePattern().findCloseMatchDatePatternInSession(toSession);
			}
			toClass.setDatePattern(toDp);
		}
		if (isClassRollForward()) {
			rollForwardTimePrefs(fromClass, toClass, toSession);
			rollForwardBuildingPrefs(fromClass, toClass, toSession);
			rollForwardRoomPrefs(fromClass, toClass, toSession);
			rollForwardRoomGroupPrefs(fromClass, toClass, toSession);
			rollForwardRoomFeaturePrefs(fromClass, toClass, toSession);
			rollForwardDistributionPrefs(fromClass, toClass, toSession, hibSession);
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
			DatePattern toDp = null;
			toDp = DatePattern.findByName(toSession, fromSubpart.getDatePattern().getName());
			if (toDp == null){
				toDp = fromSubpart.getDatePattern().findCloseMatchDatePatternInSession(toSession);
			}
			toSubpart.setDatePattern(toDp);
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
			Class_ fromClass = null;
			Class_ toClass = null;
			for (Iterator it = fromSubpart.getClasses().iterator(); it.hasNext();){
				fromClass = (Class_) it.next();
				toClass = rollForwardClass(fromClass, toSubpart, toSession, hibSession);
				RollForwardClass rfc = new RollForwardClass();
				rfc.setToClass(toClass);
				rfc.setFromClass(fromClass);
				rfc.setFromParentClass(fromClass.getParentClass());
				rfc.setParentSubpart(rfSs);
				rfSs.addToRollForwardClasses(rfc);
				if (fromClass.getChildClasses() != null && fromClass.getChildClasses().size() > 0){
					for (Iterator ccIt = fromClass.getChildClasses().iterator(); ccIt.hasNext();){
						rfc.addToLastLikeChildClasses(ccIt.next());
					}
				}
				if (parentSubpart != null){
					Class_ parentClass = parentSubpart.findParentClassMatchingFromParentClass(fromClass.getParentClass());
					toClass.setParentClass(parentClass);
					parentClass.addTochildClasses(toClass);
				}
			}
		}
		hibSession.update(toInstrOffrConfig);
		rollForwardTimePrefs(fromSubpart, toSubpart, toSession);
		rollForwardBuildingPrefs(fromSubpart, toSubpart, toSession);
		rollForwardRoomPrefs(fromSubpart, toSubpart, toSession);
		rollForwardRoomGroupPrefs(fromSubpart, toSubpart, toSession);
		rollForwardRoomFeaturePrefs(fromSubpart, toSubpart, toSession);
		rollForwardDistributionPrefs(fromSubpart, toSubpart, toSession, hibSession);
		if (fromSubpart.getChildSubparts() != null && fromSubpart.getChildSubparts().size() > 0){
			SchedulingSubpart childSubpart = null;
			for(Iterator it = fromSubpart.getChildSubparts().iterator(); it.hasNext();){
				childSubpart = (SchedulingSubpart) it.next();
				rollForwardSchedulingSubpart(toInstrOffrConfig, childSubpart, rfSs, hibSession,toSession);
			}
		}
		hibSession.update(toInstrOffrConfig);
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
		
	private InstructionalOffering createToInstructionalOfferingFromFromInstructionalOffering(InstructionalOffering fromInstructionalOffering, Session toSession, org.hibernate.Session hibSession){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		InstructionalOffering toInstructionalOffering = new InstructionalOffering();
		toInstructionalOffering.setNotOffered(fromInstructionalOffering.isNotOffered());
		toInstructionalOffering.setSession(toSession);
		toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
		toInstructionalOffering.setInstrOfferingPermId(fromInstructionalOffering.getInstrOfferingPermId());
		toInstructionalOffering.setByReservationOnly(fromInstructionalOffering.isByReservationOnly());
		toInstructionalOffering.setLastWeekToEnroll(fromInstructionalOffering.getLastWeekToEnroll());
		toInstructionalOffering.setLastWeekToChange(fromInstructionalOffering.getLastWeekToChange());
		toInstructionalOffering.setLastWeekToDrop(toInstructionalOffering.getLastWeekToDrop());
		CourseOffering fromCourseOffering = null;
		CourseOffering toCourseOffering = null;
		for(Iterator coIt = fromInstructionalOffering.getCourseOfferings().iterator(); coIt.hasNext();){
			fromCourseOffering = (CourseOffering) coIt.next();
			toCourseOffering = new CourseOffering();
			toCourseOffering.setSubjectArea(fromCourseOffering.getSubjectArea().findSameSubjectAreaInSession(toSession));
			toCourseOffering.setCourseNbr(fromCourseOffering.getCourseNbr());
			if (fromInstructionalOffering.getCourseOfferings().size() == 1){
				toCourseOffering.setIsControl(new Boolean(true));
			} else {
				toCourseOffering.setIsControl(fromCourseOffering.isIsControl());
			}
			toCourseOffering.setExternalUniqueId(fromCourseOffering.getExternalUniqueId());
			toCourseOffering.setNbrExpectedStudents(fromCourseOffering.getNbrExpectedStudents());
			toCourseOffering.setDemand(fromCourseOffering.getDemand());
			toCourseOffering.setPermId(fromCourseOffering.getPermId());
			toCourseOffering.setScheduleBookNote(fromCourseOffering.getScheduleBookNote());
//			toCourseOffering.setScheduleBookNote("***Course not in Registrar's Course Catalog*** " + (toCourseOffering.getScheduleBookNote()==null?"":toCourseOffering.getScheduleBookNote()));
			toCourseOffering.setTitle(fromCourseOffering.getTitle());
			toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
			toCourseOffering.setInstructionalOffering(toInstructionalOffering);
			toCourseOffering.setReservation(fromCourseOffering.getReservation());
			toCourseOffering.setConsentType(fromCourseOffering.getConsentType());
			toCourseOffering.setCourseType(fromCourseOffering.getCourseType());
			toInstructionalOffering.addTocourseOfferings(toCourseOffering);
			if(fromCourseOffering.getCreditConfigs() != null && !fromCourseOffering.getCreditConfigs().isEmpty()){
				CourseCreditUnitConfig ccuc = null;
				for(Iterator ccIt = fromCourseOffering.getCreditConfigs().iterator(); ccIt.hasNext();){
					ccuc = (CourseCreditUnitConfig) ccIt.next();
					if (ccuc instanceof ArrangeCreditUnitConfig) {
						ArrangeCreditUnitConfig fromAcuc = (ArrangeCreditUnitConfig) ccuc;
						ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)fromAcuc.clone();
						toAcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toAcuc);
					} else if (ccuc instanceof FixedCreditUnitConfig) {
						FixedCreditUnitConfig fromFcuc = (FixedCreditUnitConfig) ccuc;
						FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) fromFcuc.clone();
						toFcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toFcuc);
					} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
						VariableRangeCreditUnitConfig fromVrcuc = (VariableRangeCreditUnitConfig) ccuc;
						VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) fromVrcuc.clone();
						toVrcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toVrcuc);
					} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
						VariableFixedCreditUnitConfig fromVfcuc = (VariableFixedCreditUnitConfig) ccuc;
						VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) fromVfcuc.clone();
						toVfcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toVfcuc);
					}
				}
			}
		}
		if (toInstructionalOffering.getInstrOfferingPermId() == null){
			toInstructionalOffering.generateInstrOfferingPermId();
		}
		hibSession.saveOrUpdate(toInstructionalOffering); 
		return(toInstructionalOffering);
		
	}
	
	private InstructionalOffering createToInstructionalOfferingFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session){
		if (courseCatalogEntry == null || session == null){
			return(null);
		}
		InstructionalOffering toInstructionalOffering = new InstructionalOffering();
		toInstructionalOffering.setNotOffered(new Boolean(false));
		toInstructionalOffering.setSession(session);
		toInstructionalOffering.setByReservationOnly(false);
		return(toInstructionalOffering);
	}
	
	private InstrOfferingConfig createToInstrOfferingConfigFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session){
		if (courseCatalogEntry == null || session == null){
			return(null);
		}
		InstrOfferingConfig toInstrOfferingConfig = new InstrOfferingConfig();
		toInstrOfferingConfig.setName("1");
		toInstrOfferingConfig.setLimit(new Integer(0));
		toInstrOfferingConfig.setUnlimitedEnrollment(new Boolean(false));
		if(courseCatalogEntry.getSubparts() != null  && !courseCatalogEntry.getSubparts().isEmpty()){
			CourseSubpartCredit courseSubpartCredit = null;
			SchedulingSubpart schedSupart = null;
			ItypeDescDAO itDao = new ItypeDescDAO();
			for (Iterator cscIt = courseCatalogEntry.getSubparts().iterator(); cscIt.hasNext();){
				courseSubpartCredit = (CourseSubpartCredit) cscIt.next();
				schedSupart = new SchedulingSubpart();
				schedSupart.setInstrOfferingConfig(toInstrOfferingConfig);
				toInstrOfferingConfig.addToschedulingSubparts(schedSupart);
				schedSupart.setMinutesPerWk(new Integer(0));
				schedSupart.setAutoSpreadInTime(ApplicationProperty.SchedulingSubpartAutoSpreadInTimeDefault.isTrue());
				schedSupart.setStudentAllowOverlap(ApplicationProperty.SchedulingSubpartStudentOverlapsDefault.isTrue());
				schedSupart.setItype(itDao.get(new Integer(Integer.parseInt(courseSubpartCredit.getSubpartId()))));
				CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseSubpartCredit.getCreditFormat(), courseSubpartCredit.getCreditType(), courseSubpartCredit.getCreditUnitType(), courseSubpartCredit.getFixedMinimumCredit(), courseSubpartCredit.getMaximumCredit(), courseSubpartCredit.isFractionalCreditAllowed(), new Boolean(false));
				if (ccuc instanceof ArrangeCreditUnitConfig) {					
					ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)ccuc;
					toAcuc.setOwner(schedSupart);
					schedSupart.addTocreditConfigs(toAcuc);
				} else if (ccuc instanceof FixedCreditUnitConfig) {
					FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) ccuc;
					toFcuc.setOwner(schedSupart);
					schedSupart.addTocreditConfigs(toFcuc);
				} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
					VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) ccuc;
					toVrcuc.setOwner(schedSupart);
					schedSupart.addTocreditConfigs(toVrcuc);
				} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
					VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) ccuc;
					toVfcuc.setOwner(schedSupart);
					schedSupart.addTocreditConfigs(toVfcuc);
				}
			}
		}
		return(toInstrOfferingConfig);
	}
	private CourseOffering createToCourseOfferingFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session){
		if (courseCatalogEntry == null || session == null){
			return(null);
		}
		CourseOffering toCourseOffering = new CourseOffering();
		toCourseOffering.setSubjectArea(SubjectArea.findByAbbv(session.getUniqueId(), courseCatalogEntry.getSubject()));
		toCourseOffering.setCourseNbr(courseCatalogEntry.getCourseNumber());
		toCourseOffering.setIsControl(new Boolean(true));
		toCourseOffering.setExternalUniqueId(courseCatalogEntry.getExternalUniqueId());
		toCourseOffering.setPermId(courseCatalogEntry.getPermanentId());
		toCourseOffering.setTitle(courseCatalogEntry.getTitle());
		toCourseOffering.setNbrExpectedStudents(new Integer(0));
		toCourseOffering.setDemand(new Integer(0));
		toCourseOffering.setConsentType(OfferingConsentType.getOfferingConsentTypeForReference(courseCatalogEntry.getApprovalType()));
		return(toCourseOffering);
	}
	
	private InstructionalOffering createToInstructionalOfferingBasedOnCourseCatalog(InstructionalOffering fromInstructionalOffering, Session toSession, org.hibernate.Session hibSession){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		
		CourseCatalog controllingCourseCatalogEntry = CourseCatalog.findCourseFromPreviousSessionInCatalogForSession(fromInstructionalOffering.getControllingCourseOffering(), toSession);

		if (controllingCourseCatalogEntry == null){
			return(null);
		}
		InstructionalOffering toInstructionalOffering = createToInstructionalOfferingFromCourseCatalog(controllingCourseCatalogEntry, toSession);
		toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
		toInstructionalOffering.setInstrOfferingPermId(fromInstructionalOffering.getInstrOfferingPermId());
		CourseOffering fromCourseOffering = null;
		CourseOffering toCourseOffering = null;
		CourseCatalog courseCatalogEntry = null;
		for(Iterator coIt = fromInstructionalOffering.getCourseOfferings().iterator(); coIt.hasNext();){
			fromCourseOffering = (CourseOffering) coIt.next();
			courseCatalogEntry = CourseCatalog.findCourseFromPreviousSessionInCatalogForSession(fromCourseOffering, toSession);
			if (courseCatalogEntry != null){
				toCourseOffering = createToCourseOfferingFromCourseCatalog(courseCatalogEntry, toSession);
				toCourseOffering.setIsControl(fromCourseOffering.isIsControl());
				toCourseOffering.setScheduleBookNote(fromCourseOffering.getScheduleBookNote());
				toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
				toCourseOffering.setNbrExpectedStudents(fromCourseOffering.getNbrExpectedStudents());
				toCourseOffering.setDemand(fromCourseOffering.getDemand());
				toCourseOffering.setInstructionalOffering(toInstructionalOffering);
				toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
				toInstructionalOffering.addTocourseOfferings(toCourseOffering);
				if(courseCatalogEntry.getCreditType() != null){
					CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseCatalogEntry.getCreditFormat(), courseCatalogEntry.getCreditType(), courseCatalogEntry.getCreditUnitType(), courseCatalogEntry.getFixedMinimumCredit(), courseCatalogEntry.getMaximumCredit(), courseCatalogEntry.isFractionalCreditAllowed(), new Boolean(true));
					if (ccuc instanceof ArrangeCreditUnitConfig) {					
						ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)ccuc;
						toAcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toAcuc);
					} else if (ccuc instanceof FixedCreditUnitConfig) {
						FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) ccuc;
						toFcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toFcuc);
					} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
						VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) ccuc;
						toVrcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toVrcuc);
					} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
						VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) ccuc;
						toVfcuc.setOwner(toCourseOffering);
						toCourseOffering.addTocreditConfigs(toVfcuc);
					}
				}				
			}
		}
		if (toInstructionalOffering.getCourseOfferings().size() == 1){
			toCourseOffering.setIsControl(new Boolean(true));
		}

		if (toInstructionalOffering.getInstrOfferingPermId() == null){
			toInstructionalOffering.generateInstrOfferingPermId();
		}
		hibSession.saveOrUpdate(toInstructionalOffering); 
		return(toInstructionalOffering);		
	}

	
	private InstructionalOffering findToInstructionalOffering(InstructionalOffering fromInstructionalOffering, Session toSession, org.hibernate.Session hibSession){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		CourseOffering co = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromInstructionalOffering.getControllingCourseOffering().getUniqueId());
		if (co == null && ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {
			co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(toSession.getUniqueId(), fromInstructionalOffering.getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation(), fromInstructionalOffering.getControllingCourseOffering().getCourseNbr());
		}
		
		if (co != null){
			InstructionalOffering toInstructionalOffering = co.getInstructionalOffering();
			if (toInstructionalOffering != null){
				toInstructionalOffering.deleteAllClasses(hibSession);
				toInstructionalOffering.deleteAllDistributionPreferences(hibSession);
				toInstructionalOffering.getInstrOfferingConfigs().clear();
				return(toInstructionalOffering);
			}
		}
		if (sessionHasCourseCatalog(toSession)){
			return(createToInstructionalOfferingBasedOnCourseCatalog(fromInstructionalOffering, toSession, hibSession));
		} else {
			return(createToInstructionalOfferingFromFromInstructionalOffering(fromInstructionalOffering, toSession, hibSession));
		}
		
	}
	
	@SuppressWarnings("unused")
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
	
	@SuppressWarnings("unused")
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
