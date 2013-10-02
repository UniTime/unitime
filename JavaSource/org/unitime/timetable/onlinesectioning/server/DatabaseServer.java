/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.IgnoreStudentConflictsConstraint;

import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XDistribution;
import org.unitime.timetable.onlinesectioning.model.XDistributionType;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;

public class DatabaseServer extends AbstractServer {

	public DatabaseServer(Long sessionId, boolean waitTillStarted) throws SectioningException {
		super(sessionId, waitTillStarted);
	}

	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
		Collection<XCourseId> ret = new ArrayList<XCourseId>();
		for (CourseOffering c: (List<CourseOffering>)getCurrentHelper().getHibSession().createQuery(
				"select c from CourseOffering c where " +
				"c.subjectArea.session.uniqueId = :sessionId and c.instructionalOffering.notOffered = false and (" +
				"lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' " +
				(query.length() > 2 ? "or lower(c.title) like '%' || :q || '%'" : "") + ") " +
				"order by case " +
				"when lower(c.subjectArea.subjectAreaAbbreviation || ' ' || c.courseNbr) like :q || '%' then 0 else 1 end," + // matches on course name first
				"c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
				.setString("q", query.toLowerCase())
				.setLong("sessionId", getAcademicSession().getUniqueId())
				.setCacheable(true).list()) {
			XCourse course = new XCourse(c);
			if (matcher == null || matcher.match(course))
				ret.add(course);
			if (limit != null && ret.size() >= limit) break;
		}
		return ret;
	}

	@Override
	public Collection<XCourseId> findCourses(CourseMatcher matcher) {
		Collection<XCourseId> ret = new ArrayList<XCourseId>();
		for (CourseOffering c: (List<CourseOffering>)getCurrentHelper().getHibSession().createQuery(
				"select c from CourseOffering c where " +
				"c.subjectArea.session.uniqueId = :sessionId and c.instructionalOffering.notOffered = false " +
				"order by c.subjectArea.subjectAreaAbbreviation, c.courseNbr")
				.setLong("sessionId", getAcademicSession().getUniqueId())
				.setCacheable(true).list()) {
			XCourse course = new XCourse(c);
			if (matcher == null || matcher.match(course))
				ret.add(course);
		}
		return ret;
	}

	@Override
	public Collection<XStudent> findStudents(StudentMatcher matcher) {
		Collection<XStudent> ret = new ArrayList<XStudent>();
		for (Student s: (List<Student>)getCurrentHelper().getHibSession().createQuery(
				"select distinct s from Student s " +
						"left join fetch s.courseDemands as cd " +
	                    "left join fetch cd.courseRequests as cr " +
	                    "left join fetch cr.classWaitLists as cwl " + 
	                    "left join fetch s.classEnrollments as e " +
	                    "left join fetch s.academicAreaClassifications as a " +
	                    "left join fetch s.posMajors as mj " +
	                    "left join fetch s.waitlists as w " +
	                    "left join fetch s.groups as g " +
				"where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getAcademicSession().getUniqueId())
				.setCacheable(true).list()) {
			XStudent student = new XStudent(s, getCurrentHelper(), getAcademicSession().getFreeTimePattern());
			if (matcher == null || matcher.match(student))
				ret.add(student);
		}
		return ret;
	}

	@Override
	public XCourse getCourse(Long courseId) {
		CourseOffering c = CourseOfferingDAO.getInstance().get(courseId, getCurrentHelper().getHibSession());
		return c == null || c.getInstructionalOffering().isNotOffered() ? null : new XCourse(c);
	}

	@Override
	public XCourseId getCourse(String course) {
		CourseOffering c = (CourseOffering)getCurrentHelper().getHibSession().createQuery(
				"from CourseOffering where subjectAreaAbbv || ' ' || courseNbr = :name and subjectArea.session.uniqueId = :sessionId and instructionalOffering.notOffered = false")
				.setLong("sessionId", getAcademicSession().getUniqueId())
				.setString("name", course)
				.setCacheable(true).setMaxResults(1).uniqueResult();
		return c == null ? null : new XCourseId(c);
	}

	@Override
	public XStudent getStudent(Long studentId) {
		Student s = StudentDAO.getInstance().get(studentId, getCurrentHelper().getHibSession());
		return s == null ? null : new XStudent(s, getCurrentHelper(), getAcademicSession().getFreeTimePattern());
	}

	@Override
	public XOffering getOffering(Long offeringId) {
		InstructionalOffering o = InstructionalOfferingDAO.getInstance().get(offeringId, getCurrentHelper().getHibSession());
		return o == null ? null : new XOffering(o, getCurrentHelper());
	}

	@Override
	public Collection<XCourseRequest> getRequests(Long offeringId) {
		Collection<XCourseRequest> ret = new ArrayList<XCourseRequest>();
		for (CourseDemand d: (List<CourseDemand>)getCurrentHelper().getHibSession().createQuery(
				"select distinct d from CourseRequest r inner join r.courseDemand d where r.courseOffering.instructionalOffering = :offeringId")
				.setLong("offeringId", offeringId).setCacheable(true).list()) {
			ret.add(new XCourseRequest(d, getCurrentHelper()));
		}
		return ret;
	}

	@Override
	public XExpectations getExpectations(Long offeringId) {
		Map<Long, Double> expectations = new HashMap<Long, Double>();
		for (Object[] info: (List<Object[]>)getCurrentHelper().getHibSession().createQuery(
    			"select i.clazz.uniqueId, i.nbrExpectedStudents from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering = :offeringId").
    			setLong("offeringId", offeringId).
    			setCacheable(true).list()) {
			expectations.put((Long)info[0], (Double)info[1]);
		}
		return new XExpectations(offeringId, expectations);
	}

	@Override
	public void update(XExpectations expectations) {
	}

	@Override
	public void remove(XStudent student) {
	}

	@Override
	public void update(XStudent student, boolean updateRequests) {
	}

	@Override
	public void remove(XOffering offering) {
	}

	@Override
	public void update(XOffering offering) {
	}

	@Override
	public void clearAll() {
	}

	@Override
	public void clearAllStudents() {
	}

	@Override
	public XCourseRequest assign(XCourseRequest request, XEnrollment enrollment) {
		request.setEnrollment(enrollment);
		return request;
	}

	@Override
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist) {
		request.setWaitlist(waitlist);
		return request;
	}

	@Override
	public void addDistribution(XDistribution distribution) {
		
	}

	@Override
	public Collection<XDistribution> getDistributions(Long offeringId) {
		Collection<XDistribution> distributions = new ArrayList<XDistribution>();
		List<DistributionPref> distPrefs = getCurrentHelper().getHibSession().createQuery(
    		"select distinct p from DistributionPref p inner join p.distributionObjects o, Department d, " +
    		"Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io " +
    		"where p.distributionType.reference in (:ref1, :ref2) and d.session.uniqueId = :sessionId " +
    		"and io.uniqueId = :offeringId and (o.prefGroup = c or o.prefGroup = c.schedulingSubpart) " +
    		"and p.owner = d and p.prefLevel.prefProlog = :pref")
    		.setString("ref1", GroupConstraint.ConstraintType.LINKED_SECTIONS.reference())
    		.setString("ref2", IgnoreStudentConflictsConstraint.REFERENCE)
    		.setString("pref", PreferenceLevel.sRequired)
    		.setLong("sessionId", getAcademicSession().getUniqueId())
    		.setLong("offeringId", offeringId)
    		.list();
        if (!distPrefs.isEmpty()) {
        	for (DistributionPref pref: distPrefs) {
        		int variant = 0;
        		for (Collection<Class_> sections: ReloadAllData.getSections(pref)) {
        			XDistributionType type = XDistributionType.IngoreConflicts;
        			if (GroupConstraint.ConstraintType.LINKED_SECTIONS.reference().equals(pref.getDistributionType().getReference()))
        				type = XDistributionType.LinkedSections;
        			distributions.add(new XDistribution(type, pref.getUniqueId(), variant++, sections));
        		}
        	}
        }
        return distributions;
	}

}
