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
package org.unitime.timetable.dataexchange;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class AcademicSessionSetupImport extends BaseImport {
	protected static Formats.Format<Number> sFloatFormat = Formats.getNumberFormat("0.000");

    public void loadXml(Element root) throws Exception{
        if (!root.getName().equalsIgnoreCase("sessionSetup")) {
            throw new Exception("Given XML file is not academic session setup file.");
        }
        try {
            beginTransaction();
            
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            Formats.Format<Date> dateFormat = Formats.getDateFormat(root.attributeValue("dateFormat", "yyyy/M/d"));

            Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            session = createOrUpdateSession(session, root, dateFormat);
            if (session == null)
                throw new Exception("No session found for the given campus, year, and term.");
            
            Element departmentsEl = root.element("departments");
            if (departmentsEl != null)
            	importDepartments(departmentsEl, session);
            
            Element subjectAreasEl = root.element("subjectAreas");
            if (subjectAreasEl != null)
            	importSubjectAreas(subjectAreasEl, session);
            
            Element managersEl = root.element("managers");
            if (managersEl != null)
            	importManagers(managersEl, session);
            
            Element solverGroupsEl = root.element("solverGroups");
            if (solverGroupsEl != null)
            	importSolverGroups(solverGroupsEl, session);
            
            Element datePatternsEl = root.element("datePatterns");
            if (datePatternsEl != null)
            	importDatePatterns(datePatternsEl, session, dateFormat);
            
            Element timePatternsEl = root.element("timePatterns");
            if (timePatternsEl != null)
            	importTimePatterns(timePatternsEl, session);

            Element examinationPeriodsEl = root.element("examinationPeriods");
            if (examinationPeriodsEl != null) {
            	for (Iterator i = examinationPeriodsEl.elementIterator("periods"); i.hasNext(); ) {
            		Element periodsEl = (Element)i.next();
            		importExaminationPeriods(periodsEl, session, dateFormat);
            	}
            }

            Element academicAreasEl = root.element("academicAreas");
            if (academicAreasEl != null)
            	importAcademicAreas(academicAreasEl, session);
            
            Element academicClassificationsEl = root.element("academicClassifications");
            if (academicClassificationsEl != null)
            	importAcademicClassifications(academicClassificationsEl, session);

            Element posMajorsEl = root.element("posMajors");
            if (posMajorsEl != null)
            	importMajors(posMajorsEl, session);

            Element posMinorsEl = root.element("posMinors");
            if (posMinorsEl != null)
            	importMinors(posMinorsEl, session);

            Element studentGroupsEl = root.element("studentGroups");
            if (studentGroupsEl != null)
            	importStudentGroups(studentGroupsEl, session);

            Element studentAccomodationsEl = root.element("studentAccomodations");
            if (studentAccomodationsEl != null)
            	importStudentAccomodations(studentAccomodationsEl, session);
            
            commitTransaction();
            
            HibernateUtil.clearCache();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
    
    public Session createOrUpdateSession(Session session, Element root, Formats.Format<Date> dateFormat) throws ParseException {
		info("Importing academic session...");
		
    	Element sessionEl = root.element("session");
    	if (sessionEl == null) return session;
    	if (session == null) {
    		session = new Session();
    		session.setAcademicInitiative(root.attributeValue("campus"));
    		session.setAcademicTerm(root.attributeValue("term"));
    		session.setAcademicYear(root.attributeValue("year"));
    		session.setStatusType(DepartmentStatusType.findByRef("initial"));
    		session.setBuildings(new HashSet<Building>());
    		session.setDepartments(new HashSet<Department>());
    		session.setDistributionObjects(new HashSet<DistributionObject>());
    		session.setInstructionalOfferings(new HashSet<InstructionalOffering>());
    		session.setPreferences(new HashSet<Preference>());
    		session.setRooms(new HashSet<Location>());
    		session.setSubjectAreas(new HashSet<SubjectArea>());
    		session.setLastWeekToEnroll(1);
    		session.setLastWeekToChange(1);
    		session.setLastWeekToDrop(4);
    	}
    	session.setSessionBeginDateTime(dateFormat.parse(sessionEl.attributeValue("startDate")));
    	session.setSessionEndDateTime(dateFormat.parse(sessionEl.attributeValue("endDate")));
    	session.setClassesEndDateTime(dateFormat.parse(sessionEl.attributeValue("classEndDate")));
    	session.setExamBeginDate(dateFormat.parse(sessionEl.attributeValue("examStartDate")));
    	session.setEventBeginDate(dateFormat.parse(sessionEl.attributeValue("eventStartDate", sessionEl.attributeValue("startDate"))));
    	session.setEventEndDate(dateFormat.parse(sessionEl.attributeValue("eventEndDate", sessionEl.attributeValue("endDate"))));
    	
    	Element deadlinesEl = sessionEl.element("deadlines");
    	if (deadlinesEl != null) {
    		session.setLastWeekToEnroll(Integer.valueOf(deadlinesEl.attributeValue("lastWeekToEnroll", "1")));
            session.setLastWeekToChange(Integer.valueOf(deadlinesEl.attributeValue("lastWeekToChange", "1")));
            session.setLastWeekToDrop(Integer.valueOf(deadlinesEl.attributeValue("lastWeekToDrop", "4")));
    	}
    	
    	Element holidayEl = sessionEl.element("holidays");
    	if (holidayEl != null) {
    		StringBuffer holiday = new StringBuffer();
    		int acadYear = session.getSessionStartYear(); 
            int startMonth = DateUtils.getStartMonth(session.getEventBeginDate() != null && session.getEventBeginDate().before(session.getSessionBeginDateTime()) ? session.getEventBeginDate() : session.getSessionBeginDateTime(), acadYear, ApplicationProperty.SessionNrExcessDays.intValue());
    		int endMonth = DateUtils.getEndMonth(session.getEventEndDate() != null && session.getEventEndDate().after(session.getSessionEndDateTime()) ? session.getEventEndDate() : session.getSessionEndDateTime(), acadYear, ApplicationProperty.SessionNrExcessDays.intValue());
    		Map<Date, Integer> date2index = new HashMap<Date, Integer>();
    		int idx = 0;
    		for (int m = startMonth; m <= endMonth; m++) {
    			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, acadYear);
    			for (int d = 1; d <= daysOfMonth; d++) {
    				date2index.put(DateUtils.getDate(d, m, acadYear), idx);
    				holiday.append('0');
    				idx++;
    			}
    		}
    		for (Iterator i = holidayEl.elementIterator("holiday"); i.hasNext(); ) {
    			Element e = (Element)i.next();
    			Date d = dateFormat.parse(e.attributeValue("date"));
    			Integer index = date2index.get(d);
    			if (index != null) holiday.setCharAt(index, '1');
    		}
    		for (Iterator i = holidayEl.elementIterator("break"); i.hasNext(); ) {
    			Element e = (Element)i.next();
    			Date sd = dateFormat.parse(e.attributeValue("startDate", e.attributeValue("date")));
    			Date ed = dateFormat.parse(e.attributeValue("endDate", e.attributeValue("date")));
    			Integer i1 = date2index.get(sd);
    			Integer i2 = date2index.get(ed);
    			if (i1 != null && i2 != null)
    				for (int j = i1; j <= i2; j++)
    					holiday.setCharAt(j, '2');
    		}
    		session.setHolidays(holiday.toString());
    	}
    	getHibSession().saveOrUpdate(session);
    	
    	flush(true);
    	
    	return session;
    }
    
    public void importDepartments(Element root, Session session) {
    	info("Importing departments...");
    	
    	Map<String, Department> id2dept = new Hashtable<String, Department>();
        Map<String, Department> code2dept = new Hashtable<String, Department>();
        for (Department dept: (List<Department>)getHibSession().createQuery(
        		"from Department where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (dept.getExternalUniqueId() != null)
        		id2dept.put(dept.getExternalUniqueId(), dept);
        	code2dept.put(dept.getDeptCode(), dept);
        }

        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            String code = element.attributeValue("code");
            
            Department dept = null;
            if (externalId != null)
            	dept = id2dept.remove(externalId);
            if (dept == null)
            	dept = code2dept.get(code);
            
            if (dept == null) {
            	dept = new Department();
            	dept.setSession(session);
            	dept.setExternalManager(false);
            	dept.setAllowEvents(false);
            	dept.setAllowStudentScheduling(true);
            	dept.setAllowReqTime(false);
            	dept.setAllowReqRoom(false);
            	dept.setAllowReqDistribution(false);
            	dept.setInheritInstructorPreferences(false);
            	dept.setDistributionPrefPriority(0);
            	dept.setSubjectAreas(new HashSet<SubjectArea>());
            	debug("Department " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Department " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            dept.setExternalUniqueId(externalId);
            dept.setDeptCode(code);
            dept.setAbbreviation(element.attributeValue("abbreviation"));
            dept.setName(element.attributeValue("name"));
            
            Element externalEl = element.element("externalManager");
            if (externalEl != null) {
            	dept.setExternalManager("true".equalsIgnoreCase(externalEl.attributeValue("enabled", "true")));
            	dept.setExternalMgrAbbv(externalEl.attributeValue("abbreviation"));
            	dept.setExternalMgrLabel(externalEl.attributeValue("label"));
            	if (dept.getUniqueId() == null)
            		dept.setInheritInstructorPreferences(!dept.getExternalManager());
            }
            
            Element eventsEl = element.element("eventManagement");
            if (eventsEl != null) {
            	dept.setAllowEvents("true".equalsIgnoreCase(eventsEl.attributeValue("enabled", "true")));
            }
            
            Element studentsEl = element.element("studentScheduling");
            if (studentsEl != null) {
            	dept.setAllowStudentScheduling("true".equalsIgnoreCase(studentsEl.attributeValue("enabled", "true")));
            }
            
            Element requiredEl = element.element("required");
            if (requiredEl != null) {
            	dept.setAllowReqTime("true".equalsIgnoreCase(requiredEl.attributeValue("time", "true")));
            	dept.setAllowReqTime("true".equalsIgnoreCase(requiredEl.attributeValue("room", "true")));
            	dept.setAllowReqTime("true".equalsIgnoreCase(requiredEl.attributeValue("distribution", "true")));
            }
            
            Element instructorEl = element.element("instructorPreferences");
            if (instructorEl != null) {
            	dept.setInheritInstructorPreferences("true".equalsIgnoreCase(instructorEl.attributeValue("inherit", "true")));
            }
            
            Element distEl = element.element("distributionPreferences");
            if (distEl != null) {
            	dept.setDistributionPrefPriority(Integer.parseInt(distEl.attributeValue("priority", "0")));
            }

            getHibSession().saveOrUpdate(dept);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (Department dept: id2dept.values()) {
            	debug("Department " + dept.getDeptCode() + " (" + dept.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(dept);
            }
        }
        
        flush(true);
    }
    
    public void importSubjectAreas(Element root, Session session) {
    	info("Importing subject ares...");
    	
    	Map<String, SubjectArea> id2subject = new Hashtable<String, SubjectArea>();
        Map<String, SubjectArea> abbv2subject = new Hashtable<String, SubjectArea>();
        for (SubjectArea subject: (List<SubjectArea>)getHibSession().createQuery(
        		"from SubjectArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (subject.getExternalUniqueId() != null)
        		id2subject.put(subject.getExternalUniqueId(), subject);
        	abbv2subject.put(subject.getSubjectAreaAbbreviation(), subject);
        }

        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            String abbv = element.attributeValue("abbreviation");
            
            SubjectArea subject = null;
            if (externalId != null)
            	subject = id2subject.remove(externalId);
            if (subject == null)
            	subject = abbv2subject.get(abbv);
            
            if (subject == null) {
            	subject = new SubjectArea();
            	subject.setSession(session);
            	
            	debug("Subject area " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Subject area " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            subject.setExternalUniqueId(externalId);
            subject.setSubjectAreaAbbreviation(abbv);
            subject.setTitle(element.attributeValue("title"));
            subject.setDepartment(Department.findByDeptCode(element.attributeValue("department"), session.getUniqueId()));

            getHibSession().saveOrUpdate(subject);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (SubjectArea subject: id2subject.values()) {
            	debug("Subject area " + subject.getSubjectAreaAbbreviation() + " (" + subject.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(subject);
            }
        }
        
        flush(true);
    }
    
    public void importManagers(Element root, Session session) {
    	info("Importing managers...");
    	
        Map<String, TimetableManager> id2manager = new Hashtable<String, TimetableManager>();
        for (TimetableManager m: new TreeSet<TimetableManager>((List<TimetableManager>)getHibSession().createQuery(
				"select distinct m from TimetableManager m inner join m.departments d where d.session.uniqueId = :sessionId"
				).setLong("sessionId", session.getUniqueId()).list())) {
        	id2manager.put(m.getExternalUniqueId(), m);
        }
        Map<String, TimetableManager> allManagers = new Hashtable<String, TimetableManager>();
        for (TimetableManager m: new TreeSet<TimetableManager>((List<TimetableManager>)getHibSession().createQuery(
				"from TimetableManager m").list())) {
        	allManagers.put(m.getExternalUniqueId(), m);
        }
        
        Map<String, Department> code2dept = new Hashtable<String, Department>();
        for (Department dept: (List<Department>)getHibSession().createQuery(
        		"from Department where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	code2dept.put(dept.getDeptCode(), dept);
        }
        
        Map<String, Roles> ref2role = new Hashtable<String, Roles>();
        for (Roles role: Roles.findAll(true, getHibSession())) {
        	ref2role.put(role.getReference(), role);
        }

        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            TimetableManager manager = id2manager.get(externalId);
            if (manager == null)
            	manager = allManagers.get(externalId);
            
            if (manager == null) {
            	manager = new TimetableManager();
            	manager.setSolverGroups(new HashSet<SolverGroup>());
            	manager.setManagerRoles(new HashSet<ManagerRole>());
            	manager.setDepartments(new HashSet<Department>());
            	
            	debug("Manager " + externalId + " created.");
            } else {
            	debug("Manager " + externalId + " updated.");
            }
        	manager.setExternalUniqueId(externalId);
        	manager.setFirstName(element.attributeValue("firstName"));
        	manager.setMiddleName(element.attributeValue("middleName"));
        	manager.setLastName(element.attributeValue("lastName"));
        	manager.setEmailAddress(element.attributeValue("email"));
        	manager.setAcademicTitle(element.attributeValue("acadTitle"));
        	
        	Set<Department> departments = new HashSet<Department>(manager.getDepartments());
        	for (Iterator mIt = element.elementIterator("department"); mIt.hasNext(); ) {
        		Element mEl = (Element)mIt.next();
        		Department department = code2dept.get(mEl.attributeValue("code"));
        		if (department == null || departments.remove(department)) continue;
        		manager.getDepartments().add(department);
        		department.getTimetableManagers().add(manager);
        	}
        	for (Department department: departments) {
        		if (session.equals(department.getSession())) {
        			manager.getDepartments().remove(department);
        			department.getTimetableManagers().remove(manager);
        		}
        	}
        	
        	Map<String, ManagerRole> roles = new HashMap<String, ManagerRole>();
        	for (ManagerRole mr: manager.getManagerRoles())
        		roles.put(mr.getRole().getReference(), mr);
        	
        	int idx = 0;
        	for (Iterator rIt = element.elementIterator("role"); rIt.hasNext(); ) {
        		Element rEl = (Element)rIt.next();
        		ManagerRole role = roles.remove(rEl.attributeValue("reference"));
        		if (role == null) {
        			role = new ManagerRole();
        			role.setRole(ref2role.get(rEl.attributeValue("reference")));
        			role.setTimetableManager(manager);
        			manager.getManagerRoles().add(role);
        		}
        		role.setPrimary("true".equalsIgnoreCase(rEl.attributeValue("primary", idx == 0 ? "true" : "false")));
        		role.setReceiveEmails("true".equalsIgnoreCase(rEl.attributeValue("email", "true")));
        		idx ++;
        	}
        	
        	for (ManagerRole mr: roles.values()) {
        		getHibSession().delete(mr);
        		manager.getManagerRoles().remove(mr);
        	}
        	
        	getHibSession().saveOrUpdate(manager);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (TimetableManager manager: id2manager.values()) {
            	debug("Manager " + manager.getExternalUniqueId() + " removed.");
            	for (Iterator<Department> i = manager.getDepartments().iterator(); i.hasNext(); ) {
            		Department d = i.next();
            		if (session.equals(d.getSession())) i.remove();
            	}
            	if (manager.getDepartments().isEmpty()) {
            		getHibSession().delete(manager);
            	} else {
            		getHibSession().update(manager);
            	}
            }
        }
        
        flush(true);
    }
    
    public void importSolverGroups(Element root, Session session) {
    	info("Importing solver groups...");
    	
        Map<String, SolverGroup> abbv2group = new Hashtable<String, SolverGroup>();
        for (SolverGroup g: (List<SolverGroup>)getHibSession().createQuery(
				"from SolverGroup where session.uniqueId = :sessionId"
				).setLong("sessionId", session.getUniqueId()).list()) {
        	abbv2group.put(g.getAbbv(), g);
        }
        Map<String, TimetableManager> id2manager = new Hashtable<String, TimetableManager>();
        for (TimetableManager m: new TreeSet<TimetableManager>((List<TimetableManager>)getHibSession().createQuery(
				"from TimetableManager m").list())) {
        	id2manager.put(m.getExternalUniqueId(), m);
        }
        
        Map<String, Department> code2dept = new Hashtable<String, Department>();
        for (Department dept: (List<Department>)getHibSession().createQuery(
        		"from Department where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	code2dept.put(dept.getDeptCode(), dept);
        }
        
        Set<Department> updatedDepartments = new HashSet<Department>();
        Set<TimetableManager> updatedManagers = new HashSet<TimetableManager>();
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String abbv = element.attributeValue("abbreviation");
            SolverGroup group = abbv2group.remove(abbv);
            if (group == null) {
            	group = new SolverGroup();
            	group.setDepartments(new HashSet<Department>());
            	group.setSession(session);
            	group.setSolutions(new HashSet<Solution>());
            	group.setTimetableManagers(new HashSet<TimetableManager>());
            	debug("Solver group " + abbv + " created.");
            } else {
            	debug("Solver group " + abbv + " updated.");
            }
            group.setAbbv(abbv);
            group.setName(element.attributeValue("name"));
            
            Set<Department> departments = new HashSet<Department>(group.getDepartments());
            for (Iterator dIt = element.elementIterator("department"); dIt.hasNext(); ) {
            	Element dEl = (Element)dIt.next();
            	Department department = code2dept.get(dEl.attributeValue("code"));
            	if (department == null || departments.remove(department)) continue;
            	if (department.getSolverGroup() != null) {
            		department.getSolverGroup().getDepartments().remove(department);
            	}
            	department.setSolverGroup(group);
            	group.getDepartments().add(department);
        		updatedDepartments.add(department);
            }
            
            for (Department department: departments) {
            	group.getDepartments().remove(department);
            	department.setSolverGroup(null);
            	updatedDepartments.add(department);
            }
            
            Set<TimetableManager> managers = new HashSet<TimetableManager>(group.getTimetableManagers());
            for (Iterator mIt = element.elementIterator("manager"); mIt.hasNext(); ) {
            	Element mEl = (Element)mIt.next();
            	TimetableManager manager = id2manager.get(mEl.attributeValue("externalId"));
            	if (manager == null || managers.remove(manager)) continue;
            	manager.getSolverGroups().add(group);
            	group.getTimetableManagers().add(manager);
            	updatedManagers.add(manager);
            }
            
            for (TimetableManager manager: managers) {
            	group.getTimetableManagers().remove(manager);
            	manager.getSolverGroups().remove(group);
            	updatedManagers.add(manager);
            }
            
        	getHibSession().saveOrUpdate(group);
        }
        
        boolean incremental = "true".equalsIgnoreCase(root.attributeValue("incremental"));
        
        for (SolverGroup group: abbv2group.values()) {
        	if (incremental && !group.getDepartments().isEmpty()) {
        		getHibSession().update(group);
        		continue;
        	}
        	debug("Solver group " + group.getAbbv() + " removed.");
        	for (Iterator<Department> i = group.getDepartments().iterator(); i.hasNext(); ) {
        		Department d = i.next();
        		d.setSolverGroup(null);
        		i.remove();
        		updatedDepartments.add(d);
        	}
        	for (Iterator<TimetableManager> i = group.getTimetableManagers().iterator(); i.hasNext(); ) {
        		TimetableManager m = i.next();
        		m.getSolverGroups().remove(group);
        		updatedManagers.add(m);
        	}
        	getHibSession().delete(group);
        }
        
        for (Department d: updatedDepartments)
        	getHibSession().saveOrUpdate(d);
        
        for (TimetableManager m: updatedManagers)
        	getHibSession().saveOrUpdate(m);
        
        flush(true);
    }
    
    public void importAcademicAreas(Element root, Session session) {
    	info("Importing academic areas...");
    	
        Map<String, AcademicArea> id2area = new Hashtable<String, AcademicArea>();
        Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
        for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
        		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (area.getExternalUniqueId() != null)
        		id2area.put(area.getExternalUniqueId(), area);
        	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
        }

        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            String abbv = element.attributeValue("abbreviation");
            
            AcademicArea area = null;
            if (externalId != null)
            	area = id2area.remove(externalId);
            if (area == null)
            	area = abbv2area.get(abbv);
            
            if (area == null) {
            	area = new AcademicArea();
            	area.setSession(session);
            	debug("Academic area " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Academic area " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            area.setExternalUniqueId(externalId);
            area.setAcademicAreaAbbreviation(abbv);
            area.setTitle(element.attributeValue("title", element.attributeValue("longTitle")));

            getHibSession().saveOrUpdate(area);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (AcademicArea area: id2area.values()) {
            	debug("Academic area " + area.getAcademicAreaAbbreviation() + " (" + area.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(area);
            }
        }
        
        flush(true);
    }
    
    public void importAcademicClassifications(Element root, Session session) {
    	info("Importing academic classifications...");
    	
        Map<String, AcademicClassification> id2clasf = new Hashtable<String, AcademicClassification>();
        Map<String, AcademicClassification> code2clasf = new Hashtable<String, AcademicClassification>();
        for (AcademicClassification clasf: (List<AcademicClassification>)getHibSession().createQuery(
        		"from AcademicClassification where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (clasf.getExternalUniqueId() != null)
        		id2clasf.put(clasf.getExternalUniqueId(), clasf);
        	code2clasf.put(clasf.getCode(), clasf);
        }
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            String code = element.attributeValue("code");
            
            AcademicClassification clasf = null;
            if (externalId != null)
            	clasf = id2clasf.remove(externalId);
            if (clasf == null)
            	clasf = code2clasf.get(code);
            
            if (clasf == null) {
            	clasf = new AcademicClassification();
            	clasf.setSession(session);
            	debug("Academic classification " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Academic classification " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            
            clasf.setExternalUniqueId(externalId);
            clasf.setCode(code);
            clasf.setName(element.attributeValue("name"));
            
            getHibSession().saveOrUpdate(clasf);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (AcademicClassification clasf: id2clasf.values()) {
            	debug("Academic classification " + clasf.getCode() + " (" + clasf.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(clasf);
            }
        }
        
        flush(true);
    }
    
    public void importMajors(Element root, Session session) {
    	info("Importing majors...");
    	
        Map<String, PosMajor> id2major = new Hashtable<String, PosMajor>();
        Map<String, PosMajor> code2major = new Hashtable<String, PosMajor>();
        for (PosMajor major: (List<PosMajor>)getHibSession().createQuery(
        		"from PosMajor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (major.getExternalUniqueId() != null)
        		id2major.put(major.getExternalUniqueId(), major);
        	for (AcademicArea area: major.getAcademicAreas())
        		code2major.put(area.getAcademicAreaAbbreviation() + ":" + major.getCode(), major);
        }
        
        Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
        for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
        		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
        }
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            String code = trim(element.attributeValue("code"), "code", 10);
            AcademicArea area = abbv2area.get(element.attributeValue("academicArea"));
            
            if (area == null) {
            	warn("Unknown academic area " + element.attributeValue("academicArea"));
            	continue;
            }
            
            PosMajor major = null;
            if (externalId != null)
            	major = id2major.remove(externalId);
            if (major == null)
            	major = code2major.get(area.getAcademicAreaAbbreviation() + ":" + code);
            
            if (major == null) {
            	major = new PosMajor();
            	major.setSession(session);
            	major.setAcademicAreas(new HashSet<AcademicArea>());
            	debug("Major " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Major " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            
            major.setExternalUniqueId(externalId);
            major.setCode(code);
            major.setName(trim(element.attributeValue("name"), "name", 50));
            
            major.getAcademicAreas().clear();
            major.getAcademicAreas().add(area);
            area.getPosMajors().add(major);
            
            getHibSession().saveOrUpdate(major);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (PosMajor major: id2major.values()) {
            	String abbv = null;
            	for (AcademicArea area: major.getAcademicAreas()) {
            		area.getPosMajors().remove(major);
            		abbv = area.getAcademicAreaAbbreviation();
            	}
            	debug("Major " + abbv + " " + major.getCode() + " (" + major.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(major);
            }    	
        }
        
        flush(true);
    }
    
    public void importMinors(Element root, Session session) {
    	info("Importing minors...");
    	
        Map<String, PosMinor> id2minor = new Hashtable<String, PosMinor>();
        Map<String, PosMinor> code2minor = new Hashtable<String, PosMinor>();
        for (PosMinor minor: (List<PosMinor>)getHibSession().createQuery(
        		"from PosMinor where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (minor.getExternalUniqueId() != null)
        		id2minor.put(minor.getExternalUniqueId(), minor);
        	for (AcademicArea area: minor.getAcademicAreas())
        		code2minor.put(area.getAcademicAreaAbbreviation() + ":" + minor.getCode(), minor);
        }
        
        Map<String, AcademicArea> abbv2area = new Hashtable<String, AcademicArea>();
        for (AcademicArea area: (List<AcademicArea>)getHibSession().createQuery(
        		"from AcademicArea where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	abbv2area.put(area.getAcademicAreaAbbreviation(), area);
        }
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String externalId = element.attributeValue("externalId");
            String code = element.attributeValue("code");
            AcademicArea area = abbv2area.get(element.attributeValue("academicArea"));
            
            if (area == null) {
            	warn("Unknown academic area " + element.attributeValue("academicArea"));
            	continue;
            }
            
            PosMinor minor = null;
            if (externalId != null)
            	minor = id2minor.remove(externalId);
            if (minor == null)
            	minor = code2minor.get(area.getAcademicAreaAbbreviation() + ":" + code);
            
            if (minor == null) {
            	minor = new PosMinor();
            	minor.setSession(session);
            	minor.setAcademicAreas(new HashSet<AcademicArea>());
            	debug("Minor " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Minor " + area.getAcademicAreaAbbreviation() + " " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            
            minor.setExternalUniqueId(externalId);
            minor.setCode(code);
            minor.setName(element.attributeValue("name"));
            
            minor.getAcademicAreas().clear();
            minor.getAcademicAreas().add(area);
            area.getPosMinors().add(minor);
            
            getHibSession().saveOrUpdate(minor);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (PosMinor minor: id2minor.values()) {
            	String abbv = null;
            	for (AcademicArea area: minor.getAcademicAreas()) {
            		area.getPosMinors().remove(minor);
            		abbv = area.getAcademicAreaAbbreviation();
            	}
            	debug("Minor " + abbv + " " + minor.getCode() + " (" + minor.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(minor);
            }
        }
        
        flush(true);
    }
    
    public void importStudentGroups(Element root, Session session) {
    	info("Importing student groups...");
    	
        Map<String, StudentGroup> id2group = new Hashtable<String, StudentGroup>();
        Map<String, StudentGroup> code2group = new Hashtable<String, StudentGroup>();
        for (StudentGroup group: (List<StudentGroup>)getHibSession().createQuery(
        		"from StudentGroup where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (group.getExternalUniqueId() != null)
        		id2group.put(group.getExternalUniqueId(), group);
        	code2group.put(group.getGroupAbbreviation(), group);
        }
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element)it.next();
            
            String externalId = element.attributeValue("externalId");
            String code = element.attributeValue("code");
            String name = element.attributeValue("name");
            String size = element.attributeValue("size");
            
            StudentGroup group = null;
            if (externalId != null)
            	group = id2group.remove(externalId);
            if (group == null)
            	group = code2group.get(code);
            
            if (group == null) {
            	group = new StudentGroup();
            	group.setSession(session);
            	debug("Group " + code + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Group " + code + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            
            group.setExternalUniqueId(externalId);
            group.setGroupAbbreviation(code);
            group.setGroupName(name);
            try {
    			group.setExpectedSize(size == null || size.isEmpty() ? null : Integer.valueOf(size));
    		} catch (NumberFormatException e) {
    			group.setExpectedSize(null);
    		}
            
            getHibSession().saveOrUpdate(group);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (StudentGroup group: id2group.values()) {
            	debug("Group " + group.getGroupAbbreviation() + " (" + group.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(group);
            }
        }
        
        flush(true);
    }
    
    public void importStudentAccomodations(Element root, Session session) {
    	info("Importing student accomodations...");
    	
        Map<String, StudentAccomodation> id2accomodation = new Hashtable<String, StudentAccomodation>();
        Map<String, StudentAccomodation> code2accomodation = new Hashtable<String, StudentAccomodation>();
        for (StudentAccomodation accomodation: (List<StudentAccomodation>)getHibSession().createQuery(
        		"from StudentAccomodation where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	if (accomodation.getExternalUniqueId() != null)
        		id2accomodation.put(accomodation.getExternalUniqueId(), accomodation);
        	code2accomodation.put(accomodation.getAbbreviation(), accomodation);
        }
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element)it.next();
            
            String externalId = element.attributeValue("externalId");
            String abbv = element.attributeValue("code");
            String name = element.attributeValue("name");
            
            StudentAccomodation accomodation = null;
            if (externalId != null)
            	accomodation = id2accomodation.remove(externalId);
            if (accomodation == null)
            	accomodation = code2accomodation.get(abbv);
            
            if (accomodation == null) {
            	accomodation = new StudentAccomodation();
            	accomodation.setSession(session);
            	debug("Accomodation " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " created.");
            } else {
            	debug("Accomodation " + abbv + (externalId == null ? "" : " (" + externalId + ")") + " updated.");
            }
            
            accomodation.setExternalUniqueId(externalId);
            accomodation.setAbbreviation(abbv);
            accomodation.setName(name);
            
            getHibSession().saveOrUpdate(accomodation);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (StudentAccomodation accomodation: id2accomodation.values()) {
            	debug("Accomodation " + accomodation.getAbbreviation() + " (" + accomodation.getExternalUniqueId() + ") deleted.");
            	getHibSession().delete(accomodation);
            }
        }
        
        flush(true);
    }
    
    public void importTimePatterns(Element root, Session session) {
    	info("Importing time patterns...");
    	
        Map<String, TimePattern> name2pattern = new Hashtable<String, TimePattern>();
        for (TimePattern p: (List<TimePattern>)getHibSession().createQuery(
				"from TimePattern where session.uniqueId = :sessionId"
				).setLong("sessionId", session.getUniqueId()).list()) {
        	name2pattern.put(p.getName(), p);
        }
        
        Map<String, Department> code2dept = new Hashtable<String, Department>();
        for (Department dept: (List<Department>)getHibSession().createQuery(
        		"from Department where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	code2dept.put(dept.getDeptCode(), dept);
        }
        
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String name = element.attributeValue("name");
            TimePattern pattern = name2pattern.get(name);
            if (pattern == null) {
            	pattern = new TimePattern();
            	pattern.setTimes(new HashSet<TimePatternTime>());
            	pattern.setDays(new HashSet<TimePatternDays>());
            	pattern.setDepartments(new HashSet<Department>());
            	pattern.setSession(session);
            	debug("Time pattern " + name + " created.");
            } else {
            	debug("Time pattern " + name + " updated.");
            }
            
        	pattern.setName(name);
        	pattern.setNrMeetings(Integer.parseInt(element.attributeValue("nbrMeetings", "1")));
        	pattern.setMinPerMtg(Integer.parseInt(element.attributeValue("minsPerMeeting", "50")));
        	pattern.setVisible("true".equalsIgnoreCase(element.attributeValue("visible", "true")));
        	pattern.setSlotsPerMtg(Integer.parseInt(element.attributeValue("nbrSlotsPerMeeting", "12")));
        	pattern.setBreakTime(Integer.parseInt(element.attributeValue("breakTime", "0")));
        	String type = element.attributeValue("type", TimePattern.sTypes[0]);
        	for (int tId = 0; tId < TimePattern.sTypes.length; tId++) {
        		if (TimePattern.sTypes[tId].equalsIgnoreCase(type)) pattern.setType(tId);
        	}
        	
        	Set<Department> departments = new HashSet<Department>(pattern.getDepartments());
        	for (Iterator dIt = element.elementIterator("department"); dIt.hasNext(); ) {
            	Element dEl = (Element)dIt.next();
            	Department department = code2dept.get(dEl.attributeValue("code"));
            	if (department == null || departments.remove(department)) continue;
            	pattern.getDepartments().add(department);
            }
            for (Department department: departments) {
            	pattern.getDepartments().remove(department);
            }
            
            Map<Integer, TimePatternTime> times = new HashMap<Integer, TimePatternTime>();
            for (TimePatternTime time: pattern.getTimes())
            	times.put(time.getStartSlot(), time);
        	for (Iterator tIt = element.elementIterator("time"); tIt.hasNext(); ) {
            	Element tEl = (Element)tIt.next();
            	int slot = time2slot(Integer.parseInt(tEl.attributeValue("start")));
            	if (times.remove(slot) == null) {
            		TimePatternTime time = new TimePatternTime();
            		time.setStartSlot(slot);
            		pattern.getTimes().add(time);
            	}
            }
            for (TimePatternTime time: times.values()) {
            	pattern.getTimes().remove(time);
            }
            
            Map<Integer, TimePatternDays> days = new HashMap<Integer, TimePatternDays>();
            for (TimePatternDays d: pattern.getDays())
            	days.put(d.getDayCode(), d);
        	for (Iterator tIt = element.elementIterator("days"); tIt.hasNext(); ) {
            	Element tEl = (Element)tIt.next();
            	int code = days2code(tEl.attributeValue("code"));
            	if (days.remove(code) == null) {
            		TimePatternDays d = new TimePatternDays();
            		d.setDayCode(code);
            		pattern.getDays().add(d);
            	}
            }
            for (TimePatternDays d: days.values()) {
            	pattern.getDays().remove(d);
            }
            
        	getHibSession().saveOrUpdate(pattern);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (TimePattern tp: name2pattern.values()) {
            	debug("Time pattern " + tp.getName() + " removed.");
            	getHibSession().delete(tp);
            }
        }
        
        flush(true);
    }
    
    public void importDatePatterns(Element root, Session session, Formats.Format<Date> dateFormat) throws ParseException {
    	info("Importing date patterns...");
    	
        Map<String, DatePattern> name2pattern = new Hashtable<String, DatePattern>();
        for (DatePattern p: (List<DatePattern>)getHibSession().createQuery(
				"from DatePattern where session.uniqueId = :sessionId"
				).setLong("sessionId", session.getUniqueId()).list()) {
        	name2pattern.put(p.getName(), p);
        }
        
        Map<String, Department> code2dept = new Hashtable<String, Department>();
        for (Department dept: (List<Department>)getHibSession().createQuery(
        		"from Department where session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).list()) {
        	code2dept.put(dept.getDeptCode(), dept);
        }
        
        Map<String, DatePattern> updatedPatterns = new Hashtable<String, DatePattern>();
        Map<String, List<String>> parents = new HashMap<String, List<String>>();
        DatePattern defaultDatePattern = null;
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            String name = element.attributeValue("name");
            DatePattern pattern = name2pattern.get(name);
            if (pattern == null) {
            	pattern = new DatePattern();
            	pattern.setDepartments(new HashSet<Department>());
            	pattern.setParents(new HashSet<DatePattern>());
            	pattern.setSession(session);
            	pattern.setType(0);
            	pattern.setOffset(0);
            	pattern.setPattern("0");
            	debug("Date pattern " + name + " created.");
            } else {
            	debug("Date pattern " + name + " updated.");
            }
            
        	pattern.setName(name);
        	pattern.setVisible("true".equalsIgnoreCase(element.attributeValue("visible", "true")));
        	String type = element.attributeValue("type", DatePattern.sTypes[0]);
        	for (int tId = 0; tId < DatePattern.sTypes.length; tId++) {
        		if (DatePattern.sTypes[tId].equalsIgnoreCase(type)) pattern.setType(tId);
        	}
        	if ("true".equalsIgnoreCase(element.attributeValue("default", "false")))
        		defaultDatePattern = pattern;
        	String nbrWeeks = element.attributeValue("nbrWeeks");
        	pattern.setNumberOfWeeks(nbrWeeks == null || nbrWeeks.isEmpty() ? null : sFloatFormat.parse(nbrWeeks).floatValue());
        	
        	Set<Department> departments = new HashSet<Department>(pattern.getDepartments());
        	for (Iterator dIt = element.elementIterator("department"); dIt.hasNext(); ) {
            	Element dEl = (Element)dIt.next();
            	Department department = code2dept.get(dEl.attributeValue("code"));
            	if (department == null || departments.remove(department)) continue;
            	pattern.getDepartments().add(department);
            }
            for (Department department: departments) {
            	pattern.getDepartments().remove(department);
            }
            
            if (pattern.getType() == DatePattern.sTypePatternSet) {
            	for (Iterator dIt = element.elementIterator("datePattern"); dIt.hasNext(); ) {
                	Element dEl = (Element)dIt.next();
                	String child = dEl.attributeValue("name");
                	List<String> p = parents.get(child);
                	if (p == null) { p = new ArrayList<String>(); parents.put(child, p); }
                	p.add(name);
            	}
            }
            
            BitSet weekCode = new BitSet();
            int startMonth = session.getPatternStartMonth();
			int endMonth = session.getPatternEndMonth();
			int year = session.getSessionStartYear();
    		Map<Date, Integer> date2index = new HashMap<Date, Integer>();
    		int idx = 0;
    		for (int m = startMonth; m <= endMonth; m++) {
    			int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
    			for (int d = 1; d <= daysOfMonth; d++) {
    				date2index.put(DateUtils.getDate(d, m, year), idx);
    				idx++;
    			}
    		}
    		
    		for (Iterator dIt = element.elementIterator("dates"); dIt.hasNext(); ) {
    			Element dEl = (Element)dIt.next();
    			Date sd = dateFormat.parse(dEl.attributeValue("fromDate", dEl.attributeValue("date")));
    			Date ed = dateFormat.parse(dEl.attributeValue("toDate", dEl.attributeValue("date")));
    			Integer i1 = date2index.get(sd);
    			Integer i2 = date2index.get(ed);
    			if (i1 != null && i2 != null)
    				for (int j = i1; j <= i2; j++)
    					weekCode.set(j, true);
    		}
    		if (pattern.getType() != DatePattern.sTypePatternSet)
    			pattern.setPatternBitSet(weekCode);
    		
    		updatedPatterns.put(pattern.getName(), pattern);
        }
        
        for (DatePattern pattern: updatedPatterns.values()) {
        	List<String> p = parents.get(pattern.getName());
        	
        	Set<DatePattern> patterns = new HashSet<DatePattern>(pattern.getParents());
        	if (p != null)
        		for (String name: p) {
        			DatePattern dp = updatedPatterns.get(name);
        			if (dp == null || patterns.remove(dp)) continue;
        			pattern.getParents().add(dp);
        		}
        	pattern.getParents().removeAll(patterns);
        }
        
        for (DatePattern pattern: updatedPatterns.values())
        	getHibSession().saveOrUpdate(pattern);
        
        if (defaultDatePattern != null) {
        	session.setDefaultDatePattern(defaultDatePattern);
        	getHibSession().saveOrUpdate(session);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (DatePattern dp: name2pattern.values()) {
            	debug("Time pattern " + dp.getName() + " removed.");
            	getHibSession().delete(dp);
            }
        }
        
        flush(true);
    }
    
    public int time2slot(int time) {
		int hour = time/100;
		int min = time%100;
		if ((min % Constants.SLOT_LENGTH_MIN) != 0){
			min = min - (min % Constants.SLOT_LENGTH_MIN);
		}
		return (hour * 60 + min - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;
	}
    
    public int days2code(String daysOfWeek) {
    	int code = 0;
		String tmpDays = daysOfWeek;
		if(tmpDays.contains("Th")){
			code += Constants.DAY_CODES[Constants.DAY_THU];
			tmpDays = tmpDays.replace("Th", "..");
		}
		if(tmpDays.contains("R")){
			code += Constants.DAY_CODES[Constants.DAY_THU];
			tmpDays = tmpDays.replace("R", "..");
		}
		if (tmpDays.contains("Su")){
			code += Constants.DAY_CODES[Constants.DAY_SUN];
			tmpDays = tmpDays.replace("Su", "..");
		}
		if (tmpDays.contains("U")){
			code += Constants.DAY_CODES[Constants.DAY_SUN];
			tmpDays = tmpDays.replace("U", "..");
		}
		if (tmpDays.contains("M")){
			code += Constants.DAY_CODES[Constants.DAY_MON];
			tmpDays = tmpDays.replace("M", ".");
		}
		if (tmpDays.contains("T")){
			code += Constants.DAY_CODES[Constants.DAY_TUE];
			tmpDays = tmpDays.replace("T", ".");
		}
		if (tmpDays.contains("W")){
			code += Constants.DAY_CODES[Constants.DAY_WED];
			tmpDays = tmpDays.replace("W", ".");
		}
		if (tmpDays.contains("F")){
			code += Constants.DAY_CODES[Constants.DAY_FRI];
			tmpDays = tmpDays.replace("F", ".");
		}
		if (tmpDays.contains("S")){
			code += Constants.DAY_CODES[Constants.DAY_SAT];
			tmpDays = tmpDays.replace("S", ".");
		}
		return code;
    }
    
    public void importExaminationPeriods(Element root, Session session, Formats.Format<Date> dateFormat) throws ParseException {
    	info("Importing " + root.attributeValue("type") + " examination periods...");
    	ExamType type = ExamType.findByReference(root.attributeValue("type"));
    	if (type == null) {
    		error("Examination type " + root.attributeValue("type") + " does not exist.");
    		return;
    	}
    	
    	Map<String, ExamPeriod> periods = new HashMap<String, ExamPeriod>();
    	for (ExamPeriod period: (List<ExamPeriod>)getHibSession().createQuery(
    			"from ExamPeriod p where p.session.uniqueId = :sessionId and p.examType.uniqueId = :typeId")
    			.setLong("sessionId", session.getUniqueId()).setLong("typeId", type.getUniqueId()).list()) {
    		periods.put(period.getDateOffset() + ":" + period.getStartSlot(), period);
    	}
    	
        for (Iterator it = root.elementIterator(); it.hasNext(); ) {
            Element element = (Element) it.next();
            
            Date date = dateFormat.parse(element.attributeValue("date"));
            long diff = date.getTime() - session.getExamBeginDate().getTime();
            int offset = (int)Math.round(diff/(1000.0 * 60 * 60 * 24));
            int startSlot = time2slot(Integer.valueOf(element.attributeValue("startTime")));
            
            ExamPeriod period = periods.remove(offset + ":" + startSlot);
            if (period == null) {
            	period = new ExamPeriod();
            	period.setSession(session);
            	period.setExamType(type);
            	period.setDateOffset(offset);
            	period.setStartSlot(startSlot);
            }
            
            period.setLength(Integer.valueOf(element.attributeValue("length", "60"))/5);
            period.setEventStartOffset(Integer.valueOf(element.attributeValue("eventStartOffset", "0")));
            period.setEventStopOffset(Integer.valueOf(element.attributeValue("eventStopOffset", "0")));

            period.setPrefLevel(PreferenceLevel.getPreferenceLevel(element.attributeValue("preference", "0")));
            
            if (period.getUniqueId() == null)
            	debug("Examination period " + period.getName() + " created.");
            else
            	debug("Examination period " + period.getName() + " updated.");
            
            getHibSession().saveOrUpdate(period);
        }
        
        if (!"true".equalsIgnoreCase(root.attributeValue("incremental"))) {
            for (ExamPeriod period: periods.values()) {
            	debug("Examination period " + period.getName() + " removed.");
            	getHibSession().delete(period);
            }
        }
        
        flush(true);
    }
}
