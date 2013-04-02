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
package org.unitime.timetable.server;

import java.util.List;

import org.hibernate.Query;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.LastChangesInterface.ChangeLogInterface;
import org.unitime.timetable.gwt.shared.LastChangesInterface.LastChangesRequest;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao.ChangeLogDAO;
import org.unitime.timetable.security.SessionContext;

@GwtRpcImplements(LastChangesRequest.class)
public class LastChangesBackend implements GwtRpcImplementation<LastChangesRequest, GwtRpcResponseList<ChangeLogInterface>> {

	@Override
	public GwtRpcResponseList<ChangeLogInterface> execute(LastChangesRequest request, SessionContext context) {
		GwtRpcResponseList<ChangeLogInterface> response = new GwtRpcResponseList<ChangeLogInterface>();
		
		for (ChangeLog cl: findChangeLog(request)) {
			ChangeLogInterface log = new ChangeLogInterface();
			if (cl.getDepartment() != null) {
				log.setDepartment(cl.getDepartment().getDeptCode());
				log.setDepartmentId(cl.getDepartment().getUniqueId());
			}
			if (cl.getSubjectArea() != null) {
				log.setSubject(cl.getSubjectArea().getSubjectAreaAbbreviation());
				log.setSubjectId(cl.getSubjectArea().getUniqueId());
			}
			if (cl.getSession() != null) {
				log.setSession(cl.getSession().getLabel());
				log.setSessionId(cl.getSession().getUniqueId());
				log.setSessionDate(cl.getSession().getSessionBeginDateTime());
				log.setSessionInitiative(cl.getSession().getAcademicInitiative());
			}
			if (cl.getManager() != null) {
				log.setManager(cl.getManager().getName());
			}
			if (cl.getTimeStamp() != null) {
				log.setDate(cl.getTimeStamp());
			}
			if (cl.getOperation() != null) {
				log.setOperation(cl.getOperation().getTitle());
			}
			if (cl.getObjectTitle() != null) {
				log.setObject(cl.getObjectTitle());
			}
			if (cl.getSource() != null) {
				log.setPage(cl.getSource().getTitle());
			}
			log.setId(cl.getUniqueId());
			response.add(log);
		}
		
		return response;
	}
	
	private List<ChangeLog> findChangeLog(LastChangesRequest request) {
		String from = "ChangeLog l";
		
		String where = "l.objectType = :type and l.objectUniqueId = :id";
		
		String groupBy = null;
		
		String orderBy = "l.timeStamp desc";
		
		if (Location.class.getName().equals(request.getObjectType())) {
			if ("true".equalsIgnoreCase(request.getOption("multi-session"))) {
				from = "ChangeLog l, Location r1, Location r2";
				where = "l.objectType in (:type, :roomType, :locType) and r1.uniqueId = :id and r2.permanentId = r1.permanentId and r2.uniqueId = l.objectUniqueId";
			} else {
				where = "l.objectType in (:type, :roomType, :locType) and l.objectUniqueId = :id";
			}
		}

		if (request.hasOption("operation")) {
			where += " and l.operationString = :operation";
		}
		
		if (request.hasOption("page")) {
			where += " and l.sourceString = :source";
		}
		
		String query = "select l from " + from + " where " + where + (groupBy == null ? "" : " group by " + groupBy ) + " order by " + orderBy;
		
		Query q = ChangeLogDAO.getInstance().getSession().createQuery(query);
		
		if (request.hasOption("limit"))
			q.setMaxResults(Integer.valueOf(request.getOption("limit")));
		else
			q.setMaxResults(Integer.valueOf(ApplicationProperties.getProperty("unitime.changelog.limit", "1000")));
		
		if (request.hasOption("operation")) {
			q.setString("operation", request.getOption("operation").toUpperCase());
		}
		
		if (request.hasOption("page")) {
			q.setString("source", request.getOption("page").replace(' ', '_').toUpperCase());
		}
		
		if (Location.class.getName().equals(request.getObjectType())) {
			q.setString("roomType", Room.class.getName());
			q.setString("locType", NonUniversityLocation.class.getName());
		}

		return q.setString("type", request.getObjectType()).setLong("id", request.getObjectId()).setCacheable(true).list();
	}

}
