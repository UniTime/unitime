/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.custom.SectionLimitProvider;
import org.unitime.timetable.onlinesectioning.custom.SectionUrlProvider;
import org.unitime.timetable.solver.jgroups.SolverContainer;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningService {
	private static Log sLog = LogFactory.getLog(OnlineSectioningService.class);
	
	private static SolverContainer<OnlineSectioningServer> sOnlineSectioningServerContainer;

    public static SectionLimitProvider sSectionLimitProvider = null;
    public static SectionUrlProvider sSectionUrlProvider = null;
    public static boolean sUpdateLimitsUsingSectionLimitProvider = false;
    
	public static void startService(SolverContainer<OnlineSectioningServer> container) {
		sOnlineSectioningServerContainer = container;
		if (ApplicationProperties.getProperty("unitime.custom.SectionLimitProvider") != null) {
        	try {
        		sSectionLimitProvider = (SectionLimitProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.SectionLimitProvider")).newInstance();
        	} catch (Exception e) {
        		sLog.fatal("Unable to initialize section limit provider, reason: "+e.getMessage(), e);
        	}
        }
        if (ApplicationProperties.getProperty("unitime.custom.SectionUrlProvider") != null) {
        	try {
        		sSectionUrlProvider = (SectionUrlProvider)Class.forName(ApplicationProperties.getProperty("unitime.custom.SectionUrlProvider")).newInstance();
        	} catch (Exception e) {
        		sLog.fatal("Unable to initialize section URL provider, reason: "+e.getMessage(), e);
        	}
        }
        sUpdateLimitsUsingSectionLimitProvider = "true".equalsIgnoreCase(ApplicationProperties.getProperty("unitime.custom.SectionLimitProvider.updateLimits", "false"));
	}

	public static boolean isEnabled() {
		// if autostart is enabled, just check whether there are some instances already loaded in
		if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.autostart", "false")))
			return !sOnlineSectioningServerContainer.getSolvers().isEmpty();
		
		// quick check for existing instances
		if (!sOnlineSectioningServerContainer.getSolvers().isEmpty()) return true;
		
		// otherwise, look for a session that has sectioning enabled
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		String campus = ApplicationProperties.getProperty("unitime.enrollment.campus");
		for (Iterator<Session> i = SessionDAO.getInstance().findAll().iterator(); i.hasNext(); ) {
			final Session session = i.next();
			
			if (year != null && !year.equals(session.getAcademicYear())) continue;
			if (term != null && !term.equals(session.getAcademicTerm())) continue;
			if (campus != null && !campus.equals(session.getAcademicInitiative())) continue;
			if (session.getStatusType().isTestSession()) continue;

			if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;

			return true;
		}
		return false;
	}
	
	public static boolean isRegistrationEnabled() {
		for (Session session: SessionDAO.getInstance().findAll()) {
			if (session.getStatusType().isTestSession()) continue;
			if (!session.getStatusType().canOnlineSectionStudents() && !session.getStatusType().canSectionAssistStudents() && session.getStatusType().canPreRegisterStudents()) return true;
		}
		return false;
	}

	public static void createInstance(Long academicSessionId) {
		sOnlineSectioningServerContainer.createSolver(academicSessionId.toString(), null);
	}
	
	public static OnlineSectioningServer getInstance(final Long academicSessionId) throws SectioningException {
		return sOnlineSectioningServerContainer.getSolver(academicSessionId.toString());
	}
	
	public static TreeSet<AcademicSessionInfo> getAcademicSessions() {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			TreeSet<AcademicSessionInfo> ret = new TreeSet<AcademicSessionInfo>();
			for (String sessionId: sOnlineSectioningServerContainer.getSolvers()) {
				ret.add(new AcademicSessionInfo(SessionDAO.getInstance().get(Long.valueOf(sessionId), hibSession)));
			}
			return ret;
		} finally {
			hibSession.close();
		}
	}
	
	public static void unload(Long academicSessionId) {
		sOnlineSectioningServerContainer.unloadSolver(academicSessionId.toString());
	}
	
	public static void stopService() {
	}
}
