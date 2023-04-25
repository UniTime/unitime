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


import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.AcademicSessionLookup;
import org.unitime.timetable.model.base.BaseSubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller, Heston Fernandes
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "subject_area")
public class SubjectArea extends BaseSubjectArea implements Comparable<SubjectArea> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3256439188198207794L;
		
    /** Request attribute name for available subject areas **/
    public static String SUBJ_AREA_ATTR_NAME = "subjectAreaList";
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public SubjectArea () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SubjectArea (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	/**
	 * Retrieves all subject areas academic session
	 * @param sessionId academic session
	 * @return List of SubjectArea objects
	 */
	public static List<SubjectArea> getSubjectAreaList(Long sessionId) {
		return SubjectAreaDAO.getInstance().getSession()
				.createQuery("from SubjectArea where session.uniqueId = :sessionId", SubjectArea.class)
				.setParameter("sessionId", sessionId)
				.list();
	}
	
	/**
	 * Retrieve a subject area for a given abbreviation and academic session
	 * @param sessionId
	 * @param subjectAreaAbbr
	 * @return null if no matches found
	 */
	public static SubjectArea findByAbbv (Long sessionId, String subjectAreaAbbr) {
	    return findByAbbv(SubjectAreaDAO.getInstance().getSession(), sessionId, subjectAreaAbbr);
	}
	
	public static SubjectArea findByAbbv(org.hibernate.Session hibSession, Long sessionId, String subjectAreaAbbr) {
		return (hibSession == null ? SubjectAreaDAO.getInstance().getSession() : hibSession).createQuery(
				"from SubjectArea where session.uniqueId = :sessionId and subjectAreaAbbreviation = :subjectAreaAbbr", SubjectArea.class
				).setParameter("sessionId", sessionId).setParameter("subjectAreaAbbr", subjectAreaAbbr).setMaxResults(1).uniqueResult();
	}
	
	public static SubjectArea findUsingInitiativeYearTermSubjectAbbreviation(String academicInitiative, String academicYear, String term, String subjectAreaAbbreviation,
	org.hibernate.Session hibSession) {
		
		return  hibSession.createQuery("from SubjectArea sa where sa.session.academicInitiative = :campus and sa.session.academicYear = :year and sa.session.academicTerm = :term and sa.subjectAreaAbbreviation = :subj", SubjectArea.class)
         .setParameter("campus", academicInitiative)
         .setParameter("year", academicYear)
         .setParameter("term", term)
         .setParameter("subj", subjectAreaAbbreviation)
         .setCacheable(true)
         .setMaxResults(1)
         .uniqueResult();	
		
	}
	
	public static SubjectArea findUsingCampusYearTermExternalSubjectAbbreviation(
			String campus, String year, String term, String externalSubjectAreaAbbreviation, org.hibernate.Session hibSession) {
		String className = ApplicationProperty.AcademicSessionLookupImplementation.value();
		AcademicSessionLookup academicSessionLookup = null;
		try {
			academicSessionLookup = (AcademicSessionLookup) Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			return findByAbbv(hibSession, org.unitime.timetable.model.Session.getSessionUsingInitiativeYearTerm(campus, year, term, hibSession).getUniqueId(), externalSubjectAreaAbbreviation);
		}
		return academicSessionLookup.findSubjectAreaForCampusYearTerm(campus, year, term, externalSubjectAreaAbbreviation, hibSession);
	}
	
	@Transient
	public ArrayList<TimetableManager> getManagers() {
		if (getDepartment() != null){
			ArrayList<TimetableManager> al = new ArrayList<TimetableManager>();
			al.addAll(getDepartment().getTimetableManagers());
			return(al);
		} else {
			return(new ArrayList<TimetableManager>());
		}
	}

	public int compareTo(SubjectArea s) {
		int cmp = new NaturalOrderComparator().compare(
				getSubjectAreaAbbreviation() == null ? "" : getSubjectAreaAbbreviation(),
				s.getSubjectAreaAbbreviation() == null ? "" : s.getSubjectAreaAbbreviation());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(s.getUniqueId() == null ? -1 : s.getUniqueId());
	}

	public String toString() {
	    return this.getSubjectAreaAbbreviation() + " - " + this.getTitle();
	}
	
	@Transient
	public String getLabel() {
		return this.getSubjectAreaAbbreviation() + " - " + this.getTitle();
	}
	
	@Transient
	public Long getSessionId(){
		if (this.getSession() != null) {
			return(this.getSession().getUniqueId());
		} else {
			return(null);
		}
	}
	
	public Object clone(){
		SubjectArea newSubjectArea = new SubjectArea();
		newSubjectArea.setDepartment(getDepartment());
		newSubjectArea.setExternalUniqueId(getExternalUniqueId());
		newSubjectArea.setTitle(getTitle());
		newSubjectArea.setSession(getSession());
		newSubjectArea.setSubjectAreaAbbreviation(getSubjectAreaAbbreviation());
		return(newSubjectArea);
	}
	
	public SubjectArea findSameSubjectAreaInSession(org.unitime.timetable.model.Session session){
		return SubjectArea.findByAbbv(session.getUniqueId(), this.getSubjectAreaAbbreviation());
	}

	/**
	 * Check if a subject area has offered classes
	 * @return
	 */
	public boolean hasOfferedCourses() {
		for (CourseOffering co: getCourseOfferings()) {
			if ( !co.getInstructionalOffering().isNotOffered())
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public static TreeSet<SubjectArea> getAllSubjectAreas(Long sessionId) {
		return new TreeSet<SubjectArea>(
				SubjectAreaDAO.getInstance().getSession().createQuery("from SubjectArea where session.uniqueId = :sessionId", SubjectArea.class)
				.setParameter("sessionId", sessionId).setCacheable(true).list());
	}
	
	public static TreeSet<SubjectArea> getUserSubjectAreas(UserContext user) {
		return getUserSubjectAreas(user, true);
	}
	
	public static TreeSet<SubjectArea> getUserSubjectAreas(UserContext user, boolean allSubjectsIfExternalManager) {
		TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
		if (user == null || user.getCurrentAuthority() == null) return subjectAreas;
		for (Department department: Department.getUserDepartments(user)) {
			if (department.isExternalManager() && allSubjectsIfExternalManager) {
				subjectAreas.addAll(department.getSession().getSubjectAreas());
				break;
			} else {
				subjectAreas.addAll(department.getSubjectAreas());
			}
		}
		return subjectAreas;
	}
	
	@Transient
	public Department getEffectiveFundingDept() {
		if (getFundingDept() == null) {
			return getDepartment();
		} else {
			return getFundingDept();
		}
	}

}
