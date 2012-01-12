/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.guice.modules;

import javax.servlet.http.HttpSession;

import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.gwt.server.CalendarServlet;
import org.unitime.timetable.gwt.server.CurriculaServlet;
import org.unitime.timetable.gwt.server.EventServlet;
import org.unitime.timetable.gwt.server.GuiceRemoteServiceServlet;
import org.unitime.timetable.gwt.server.LookupServlet;
import org.unitime.timetable.gwt.server.MenuServlet;
import org.unitime.timetable.gwt.server.PatternServlet;
import org.unitime.timetable.gwt.server.ReservationServlet;
import org.unitime.timetable.gwt.server.SavedHQLExportCSVServlet;
import org.unitime.timetable.gwt.server.SavedHQLServlet;
import org.unitime.timetable.gwt.server.SectioningServlet;
import org.unitime.timetable.gwt.server.SimpleEditServlet;
import org.unitime.timetable.gwt.services.CurriculaService;
import org.unitime.timetable.gwt.services.EventService;
import org.unitime.timetable.gwt.services.LookupService;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.ReservationService;
import org.unitime.timetable.gwt.services.SavedHQLService;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SimpleEditService;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;

public class GwtModule extends ServletModule {
	@Override
    protected void configureServlets() {
		// Serve all GWT services via GuiceRemoteServiceServlet
		serve("*.gwt").with(GuiceRemoteServiceServlet.class);
		
		// Additional servlets
		serve("/calendar").with(CalendarServlet.class);
		serve("/pattern").with(PatternServlet.class);
		serve("/unitime/hql.csv").with(SavedHQLExportCSVServlet.class);
		
		// Bind GWT services
		bind(CurriculaService.class).to(CurriculaServlet.class);
		bind(EventService.class).to(EventServlet.class);
		bind(LookupService.class).to(LookupServlet.class);
		bind(MenuService.class).to(MenuServlet.class);
		bind(ReservationService.class).to(ReservationServlet.class);
		bind(SavedHQLService.class).to(SavedHQLServlet.class);
		bind(SectioningService.class).to(SectioningServlet.class);
		bind(SimpleEditService.class).to(SimpleEditServlet.class);
	}
	
	@Provides @RequestScoped User provideUser(HttpSession session) {
		return Web.getUser(session);
	}
	
	@Provides @RequestScoped StudentSolverProxy provideStudentSolver(HttpSession session) {
		return WebSolver.getStudentSolver(session);
	}
	
	@Provides @RequestScoped SolverProxy provideCourseSolver(HttpSession session) {
		return WebSolver.getSolver(session);
	}
	
	@Provides @RequestScoped ExamSolverProxy provideExamSolver(HttpSession session) {
		return WebSolver.getExamSolver(session);
	}
}