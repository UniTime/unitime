/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.util.Constants;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CurriculaServlet extends RemoteServiceServlet implements CurriculaService {
	private static Logger sLog = Logger.getLogger(CurriculaServlet.class);

	public List<String[]> findCurricula(String filter) throws CurriculaException {
		try {
			List<String[]> results = new ArrayList<String[]>();
			Query q = new Query(filter);
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSession().getUniqueId();
				
				
				List<Curriculum> curricula = hibSession.createQuery(
						"select distinct c from Curriculum c where c.department.session.uniqueId = :sessionId order by c.abbv")
						.setLong("sessionId", sessionId)
						.setCacheable(true).list();
				for (Curriculum c: curricula) {
					if (q.match(new CurriculaMatcher(c))) {
						String majors = "";
						String majorIds = "";
						String majorCodes = "";
						String majorNames = "";
						for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
							PosMajor major = i.next();
							if (!majors.isEmpty()) { majors += "<br>"; majorIds += ","; majorCodes += ","; majorNames += ", "; }
							majors += Constants.toInitialCase(major.getName());
							majorIds += major.getUniqueId();
							majorCodes += major.getCode();
							majorNames += major.getName();
						}
						int students = 0, lastLike = 0, enrollment = 0;
						for (Iterator<CurriculumClassification> i = c.getClassifications().iterator(); i.hasNext(); ) {
							CurriculumClassification clasf = i.next();
							students += clasf.getNrStudents();
							lastLike += clasf.getLlStudents();
						}
						/*
						c.setAbbv(c.getAcademicArea().getAcademicAreaAbbreviation() + (majorCodes.isEmpty() ? "" : "/" + majorCodes));
						c.setName((c.getAcademicArea().getLongTitle() == null ? c.getAcademicArea().getShortTitle() : c.getAcademicArea().getLongTitle()) + 
								(majorNames.isEmpty() ? "" : " / " + majorNames));
						if (c.getName().length() > 60) c.setName(c.getName().substring(0, 60));
						hibSession.save(c);
						*/
						results.add(new String[] {
							String.valueOf(c.getUniqueId()),
							c.getAbbv(),
							Constants.toInitialCase(c.getAcademicArea().getLongTitle() == null ? c.getAcademicArea().getShortTitle() : c.getAcademicArea().getLongTitle()),
							majors,
							c.getDepartment().getLabel(),
							String.valueOf(students),
							"",
							String.valueOf(lastLike),
						});
					}
				}
			} finally {
				hibSession.close();
			}
			sLog.info("Found " + results.size() + " curricula.");
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}
	
	public List<String[]> getEnrollment(List<String> curriculumId) throws CurriculaException {
		try {
			if (curriculumId == null || curriculumId.isEmpty()) return new ArrayList<String[]>();
			List<String[]> results = new ArrayList<String[]>();
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				Long sessionId = getAcademicSession().getUniqueId();
				for (String cid: curriculumId) {
					Curriculum c = CurriculumDAO.getInstance().get(Long.parseLong(cid), hibSession);
					if (c == null) continue;
					String majorIds = "";
					for (Iterator<PosMajor> i = c.getMajors().iterator(); i.hasNext(); ) {
						PosMajor major = i.next();
						if (!majorIds.isEmpty()) { majorIds += ","; }
						majorIds += major.getUniqueId();
					}
					results.add(new String[] {
							cid.toString(),
							((Number)hibSession.createQuery(
							"select count(distinct s) from Student s inner join s.academicAreaClassifications a " + 
							(majorIds.isEmpty() ? "" : " inner join s.posMajors m ") + "where " +
							"s.session.uniqueId = :sessionId and "+
							"a.academicArea.uniqueId = :areaId " + 
							(majorIds.isEmpty() ? "" : "and m.uniqueId in (" + majorIds + ")"))
							.setLong("sessionId", sessionId)
							.setLong("areaId", c.getAcademicArea().getUniqueId())
							.setCacheable(true).uniqueResult()).toString()});
				}
			} finally {
				hibSession.close();
			}
			return results;
		} catch (Exception e) {
			if (e instanceof CurriculaException) throw (CurriculaException)e;
			sLog.error(e.getMessage(), e);
			throw new CurriculaException(e.getMessage());
		}
	}

	private TimetableManager getManager() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new CurriculaException("not authenticated");
		TimetableManager manager = TimetableManager.getManager(user);
		if (manager == null) throw new CurriculaException("access denied");
		return manager;
	}
	
	private Session getAcademicSession() {
		User user = Web.getUser(getThreadLocalRequest().getSession());
		if (user == null) throw new CurriculaException("not authenticated");
		Session session = Session.getCurrentAcadSession(user);
		if (session == null) throw new CurriculaException("academic session not selected");
		return session;
	}
	
	private class CurriculaMatcher implements Query.TermMatcher {
		private Curriculum iCurriculum;
		
		private CurriculaMatcher(Curriculum c) {
			iCurriculum = c;
		}
		
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || "dept".equals(attr)) {
				if (eq(iCurriculum.getDepartment().getDeptCode(), term) ||
					eq(iCurriculum.getDepartment().getAbbreviation(), term) ||
					has(iCurriculum.getDepartment().getName(), term)) return true;
			}
			if (attr == null || "abbv".equals(attr) || "curricula".equals(attr)) {
				if (eq(iCurriculum.getAbbv(), term)) return true;
			}
			if (attr == null || "name".equals(attr) || "curricula".equals(attr)) {
				if (has(iCurriculum.getName(), term)) return true;
			}
			if (attr == null || "area".equals(attr)) {
				if (eq(iCurriculum.getAcademicArea().getAcademicAreaAbbreviation(), term) ||
					has(iCurriculum.getAcademicArea().getShortTitle(), term) ||
					has(iCurriculum.getAcademicArea().getLongTitle(), term)) return true;
			}
			if (attr == null || "major".equals(attr)) {
				for (Iterator<PosMajor> i = iCurriculum.getMajors().iterator(); i.hasNext(); ) {
					PosMajor m = i.next();
					if (eq(m.getCode(), term) || has(m.getName(), term)) return true;
				}
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			for (String t: name.split(" "))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
	
	}

}
