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
package org.unitime.timetable.events;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox.FilterRpcRequest;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox.FilterRpcResponse;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.FinalExamEvent;
import org.unitime.timetable.model.MidtermExamEvent;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;

public class EventFilterBackend extends FilterBoxBackend {
	private static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public void load(FilterRpcRequest request, FilterRpcResponse response) {
		String timeFilter = "";
		Integer after = null;
		if (request.hasOption("after")) {
			after = TimeSelector.TimeUtils.parseTime(request.getOption("after"), null);
			timeFilter += " and m.stopPeriod > " + after;
		}
		if (request.hasOption("before")) {
			timeFilter += " and m.startPeriod > " + TimeSelector.TimeUtils.parseTime(request.getOption("before"), after);
		}
		if (request.hasOption("from")) {
			try {
				timeFilter += " and m.meetingDate >= " + HibernateUtil.date(new SimpleDateFormat(CONSTANTS.eventDateFormat()).parse(request.getOption("from")));
			} catch (ParseException e) {}
		}
		if (request.hasOption("to")) {
			try {
				timeFilter += " and m.meetingDate <= " + HibernateUtil.date(new SimpleDateFormat(CONSTANTS.eventDateFormat()).parse(request.getOption("to")));
			} catch (ParseException e) {}
		}
		
		String dayFilter = "";
		if (request.hasOptions("day")) {
			String dow = "";
			for (String day: request.getOptions("day")) {
				if (!dow.isEmpty()) dow += ",";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_MON].equals(day))
					dow += "2";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_TUE].equals(day))
					dow += "3";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_WED].equals(day))
					dow += "4";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_THU].equals(day))
					dow += "5";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_FRI].equals(day))
					dow += "6";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_SAT].equals(day))
					dow += "7";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_SUN].equals(day))
					dow += "1";
			}
        	if (dow.indexOf(',') >= 0)
        		dayFilter += " and " + HibernateUtil.dayOfWeek("m.meetingDate") + " in (" + dow + ")";
        	else
        		dayFilter += " and " + HibernateUtil.dayOfWeek("m.meetingDate") + " = " + dow;
		}

		String typeFilter = "";
		if (request.hasOptions("type")) {
			String type = "";
			for (String t: request.getOptions("type")) {
				if (!type.isEmpty()) type += ",";
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeClass]))
					type += ClassEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeCourse]))
					type += CourseEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeSpecial]))
					type += SpecialEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeFinalExam]))
					type += FinalExamEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeMidtermExam]))
					type += MidtermExamEvent.class.getName();
			}
			typeFilter = " and e.class in (" + type + ")";
		}
		
		String modeFilter = "";
		String modeWhere = "";
		if (request.hasOption("mode")) {
			String mode = request.getOption("mode");
			if ("My Events".equals(mode)) {
				modeFilter = " and e.mainContact.externalUniqueId = '" + request.getOption("user") + "'";
			} else if ("Approved Events".equals(mode)) {
				modeFilter = " and e not in (select distinct x.event from Meeting x where x.approvedDate is null)";
			} else if ("Awaiting Events".equals(mode)) {
				modeFilter = " and m.approvedDate is null";
			} else if ("Awaiting My Approval".equals(mode)) {
				modeWhere = ", Location l inner join l.roomDepts rd inner join rd.department.timetableManagers g";
				modeFilter = " and m.approvedDate is null" +
						" and l.session.uniqueId = :sessionId and l.permanentId = m.locationPermanentId and rd.control=true and g.externalUniqueId = '" + request.getOption("user") + "'";
			} else if ("Conflicting Events".equals(mode)) {
				modeWhere = ", Meeting mx";
				modeFilter = " and mx.uniqueId!=m.uniqueId and m.meetingDate=mx.meetingDate and m.startPeriod < mx.stopPeriod and m.stopPeriod > mx.startPeriod and m.locationPermanentId = mx.locationPermanentId";
			}
		}
		
		String requestedFilter = "";
		if (request.hasOption("requested")) {
			for (StringTokenizer s=new StringTokenizer(request.getOption("requested").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                requestedFilter += " and (upper(e.mainContact.firstName) like '%"+token+"%' or upper(e.mainContact.middleName) like '%"+token+"%' or upper(e.mainContact.lastName) like '%"+token+"%')";
            }
		}
		
		String sponsorFilter = "";
		if (request.hasOptions("sponsor")) {
			String sponsor = "";
			for (String s: request.getOptions("sponsor"))
				sponsor += (sponsor.isEmpty() ? "" : ",") + "'" + s.replace("'", "\\'") + "'";
			sponsorFilter = " and e.sponsoringOrganization.name in (" + sponsor + ")";
		}
		
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		int total = 0;
		for (Object[] o: (List<Object[]>)hibSession.createQuery(
				"select e.class, count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s " + modeWhere +
				" where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
				timeFilter + dayFilter + modeFilter + requestedFilter + sponsorFilter +
				" group by e.class order by e.class")
				.setLong("sessionId", request.getSessionId())
				.setCacheable(true)
				.list()) {
			int type = ((Number)o[0]).intValue();
			int count = ((Number)o[1]).intValue();
			Entity e = new Entity(new Long(type), Event.sEventTypesAbbv[type], Event.sEventTypesAbbv[type]);
			e.setCount(count);
			response.add("type", e);
			total += count;
		}
		
		for (Object[] o: (List<Object[]>)hibSession.createQuery(
				"select " + HibernateUtil.dayOfWeek("m.meetingDate") + ", count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s " + modeWhere +
				" where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
				timeFilter + modeFilter + requestedFilter + sponsorFilter + typeFilter + 
				" group by " + HibernateUtil.dayOfWeek("m.meetingDate") + " order by " + HibernateUtil.dayOfWeek("m.meetingDate"))
				.setLong("sessionId", request.getSessionId())
				.setCacheable(true)
				.list()) {
			int type = Integer.parseInt(o[0].toString());
			String day = null;
			switch (type) {
			case 1: day = Constants.DAY_NAMES_FULL[Constants.DAY_SUN]; break;
			case 2: day = Constants.DAY_NAMES_FULL[Constants.DAY_MON]; break;
			case 3: day = Constants.DAY_NAMES_FULL[Constants.DAY_TUE]; break;
			case 4: day = Constants.DAY_NAMES_FULL[Constants.DAY_WED]; break;
			case 5: day = Constants.DAY_NAMES_FULL[Constants.DAY_THU]; break;
			case 6: day = Constants.DAY_NAMES_FULL[Constants.DAY_FRI]; break;
			case 7: day = Constants.DAY_NAMES_FULL[Constants.DAY_SAT]; break;
			}
			int count = ((Number)o[1]).intValue();
			Entity e = new Entity(new Long(type), day, day);
			e.setCount(count);
			response.add("day", e);
			total += count;
		}
		
		Entity all = new Entity(0l, "All", "All Events");
		all.setCount(((Number)hibSession.createQuery(
				"select count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s " +
				"where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
				timeFilter + dayFilter + typeFilter + sponsorFilter +requestedFilter).setLong("sessionId", request.getSessionId()).setCacheable(true).uniqueResult()).intValue());
		response.add("mode", all);
		if (request.hasOption("user")) {
			int myCnt = ((Number)hibSession.createQuery(
					"select count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s " +
					"where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
					"and e.mainContact.externalUniqueId = '" + request.getOption("user") + "'" +
					timeFilter + dayFilter + typeFilter + sponsorFilter +requestedFilter).setLong("sessionId", request.getSessionId()).setCacheable(true).uniqueResult()).intValue();
			if (myCnt > 0) {
				Entity my = new Entity(1l, "My", "My Events"); my.setCount(myCnt);
				response.add("mode", my);
			}
			String role = request.getOption("role");
			if (role != null) {
				int approvedCnt = ((Number)hibSession.createQuery(
						"select count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s " +
						"where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
						"and e not in (select distinct x.event from Meeting x where x.approvedDate is null) " +
						timeFilter + dayFilter + typeFilter + sponsorFilter +requestedFilter).setLong("sessionId", request.getSessionId()).setCacheable(true).uniqueResult()).intValue();
				if (approvedCnt > 0) {
					Entity approved = new Entity(2l, "Approved", "Approved Events"); approved.setCount(approvedCnt);
					response.add("mode", approved);
				}
				
				int awaitingCnt = ((Number)hibSession.createQuery(
						"select count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s " +
						"where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
						"and m.approvedDate is null " +
						timeFilter + dayFilter + typeFilter + sponsorFilter +requestedFilter).setLong("sessionId", request.getSessionId()).setCacheable(true).uniqueResult()).intValue();
				if (awaitingCnt > 0) {
					Entity awaiting = new Entity(2l, "Awaiting", "Awaiting Events"); awaiting.setCount(awaitingCnt);
					response.add("mode", awaiting);
				}
				
				int conflictingCnt = ((Number)hibSession.createQuery(
						"select count(distinct e) from Event e inner join e.meetings m, Meeting mx, Location l inner join l.session s " +
						"where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
						"and mx.uniqueId!=m.uniqueId and m.meetingDate=mx.meetingDate and m.startPeriod < mx.stopPeriod and m.stopPeriod > mx.startPeriod and " +
						"m.locationPermanentId = mx.locationPermanentId " +
						timeFilter + dayFilter + typeFilter + sponsorFilter +requestedFilter).setLong("sessionId", request.getSessionId()).setCacheable(true).uniqueResult()).intValue();
				if (conflictingCnt > 0) {
					Entity conflicting = new Entity(2l, "Conflicting", "Conflicting Events"); conflicting.setCount(conflictingCnt);
					response.add("mode", conflicting);
				}
				
				if (Roles.EVENT_MGR_ROLE.equals(role)) {
					int myApprovalCnt = ((Number)hibSession.createQuery(
							"select count(distinct e) from Event e inner join e.meetings m, Location l inner join l.session s inner join l.roomDepts rd inner join rd.department.timetableManagers g " +
							"where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
							"and m.approvedDate is null " +
							"and rd.control=true and g.externalUniqueId = '" + request.getOption("user") + "' " +
							timeFilter + dayFilter + typeFilter + sponsorFilter +requestedFilter).setLong("sessionId", request.getSessionId()).setCacheable(true).uniqueResult()).intValue();
					if (myApprovalCnt > 0) {
						Entity awaiting = new Entity(2l, "My Awaiting", "Awaiting My Approval"); awaiting.setCount(myApprovalCnt);
						response.add("mode", awaiting);
					}					
				}
			}
		}

		for (Object[] org: (List<Object[]>)hibSession.createQuery(
				"select o.uniqueId, o.name, count(distinct e) from Event e inner join e.sponsoringOrganization o inner join e.meetings m, Location l inner join l.session s " + modeWhere +
				" where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
				timeFilter + dayFilter + typeFilter + requestedFilter + modeFilter +
				" group by o.uniqueId, o.name order by o.name")
				.setLong("sessionId", request.getSessionId())
				.setCacheable(true).list()) {
			Long id = (Long)org[0];
			String name = (String)org[1];
			int count = ((Number)org[2]).intValue();
			Entity sponsor = new Entity(id, name, name); sponsor.setCount(count);
			response.add("sponsor", sponsor);
		}
		
		response.add("other", new Entity(0l, "Conflicts", "Display Conflicts"));
		
		/*
		for (int i = 0; i < Constants.DAY_NAMES_FULL.length; i++)
			response.add("day", new Entity((long)i, Constants.DAY_NAMES_FULL[i], Constants.DAY_NAMES_FULL[i]));
			*/
	}

	@Override
	public void suggestions(FilterRpcRequest request, FilterRpcResponse response) {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		
		String restrictions = "";
		String restrictionsWhere = "";
		if (request.hasOptions("type")) {
			String type = "";
			for (String t: request.getOptions("type")) {
				if (!type.isEmpty()) type += ",";
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeClass]))
					type += ClassEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeCourse]))
					type += CourseEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeSpecial]))
					type += SpecialEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeFinalExam]))
					type += FinalExamEvent.class.getName();
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeMidtermExam]))
					type += MidtermExamEvent.class.getName();
			}
			
			restrictions += " and e.class in (" + type + ")";
		}
		
		Integer after = null;
		if (request.hasOption("after")) {
			after = TimeSelector.TimeUtils.parseTime(request.getOption("after"), null);
			restrictions += " and m.stopPeriod > " + after;
		}
		if (request.hasOption("before")) {
			restrictions += " and m.startPeriod > " + TimeSelector.TimeUtils.parseTime(request.getOption("before"), after);
		}
		if (request.hasOptions("day")) {
			String dow = "";
			for (String day: request.getOptions("day")) {
				if (!dow.isEmpty()) dow += ",";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_MON].equals(day))
					dow += "2";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_TUE].equals(day))
					dow += "3";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_WED].equals(day))
					dow += "4";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_THU].equals(day))
					dow += "5";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_FRI].equals(day))
					dow += "6";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_SAT].equals(day))
					dow += "7";
				if (Constants.DAY_NAMES_FULL[Constants.DAY_SUN].equals(day))
					dow += "1";
			}
        	if (dow.indexOf(',') >= 0)
        		restrictions += " and " + HibernateUtil.dayOfWeek("m.meetingDate") + " in (" + dow + ")";
        	else
        		restrictions += " and " + HibernateUtil.dayOfWeek("m.meetingDate") + " = " + dow;
		}
		if (request.hasOption("from")) {
			try {
				restrictions += " and m.meetingDate >= " + HibernateUtil.date(new SimpleDateFormat(CONSTANTS.eventDateFormat()).parse(request.getOption("from")));
			} catch (ParseException e) {}
		}
		if (request.hasOption("to")) {
			try {
				restrictions += " and m.meetingDate <= " + HibernateUtil.date(new SimpleDateFormat(CONSTANTS.eventDateFormat()).parse(request.getOption("to")));
			} catch (ParseException e) {}
		}
		
		if (request.hasOptions("sponsor")) {
			String sponsor = "";
			for (String s: request.getOptions("sponsor"))
				sponsor += (sponsor.isEmpty() ? "" : ",") + "'" + s.replace("'", "\\'") + "'";
			restrictions += " and e.sponsoringOrganization.name in (" + sponsor + ")";
		}
		
		if (request.hasOption("mode")) {
			String mode = request.getOption("mode");
			if ("My Events".equals(mode)) {
				restrictions += " and e.mainContact.externalUniqueId = '" + request.getOption("user") + "'";
			} else if ("Approved Events".equals(mode)) {
				restrictions += " and e not in (select distinct x.event from Meeting x where x.approvedDate is null)";;
			} else if ("Awaiting Events".equals(mode)) {
				restrictions += " and m.approvedDate is null";
			} else if ("Awaiting My Approval".equals(mode)) {
				restrictionsWhere += ", Location l inner join l.roomDepts rd inner join rd.department.timetableManagers g";
				restrictions = " and m.approvedDate is null" +
						" and l.session.uniqueId = :sessionId and l.permanentId = m.locationPermanentId and rd.control=true and g.externalUniqueId = '" + request.getOption("user") + "'";
			} else if ("Conflicting Events".equals(mode)) {
				restrictionsWhere += ", Meeting mx";
				restrictions += " and mx.uniqueId!=m.uniqueId and m.meetingDate=mx.meetingDate and m.startPeriod < mx.stopPeriod and m.stopPeriod > mx.startPeriod and m.locationPermanentId = mx.locationPermanentId";
			}
		}
		
		String requested = "";
		if (request.hasOption("requested")) {
			for (StringTokenizer s=new StringTokenizer(request.getOption("requested").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                requested += " and (upper(e.mainContact.firstName) like '%"+token+"%' or upper(e.mainContact.middleName) like '%"+token+"%' or upper(e.mainContact.lastName) like '%"+token+"%')";
            }
		}
		
		List<Event> events = (List<Event>)hibSession.createQuery(
				"select distinct e from Event e inner join e.meetings m, Location l inner join l.session s " + restrictionsWhere +
				" where lower(e.eventName) like lower(:query) || '%' " +
				"and s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
				restrictions + requested +
				" order by e.eventName")
				.setLong("sessionId", request.getSessionId())
				.setString("query", request.getText())
				.setCacheable(true)
				.setMaxResults(20)
				.list();
		
		for (Event event: events)
			response.addSuggestion(event.getEventName(), event.getEventName(), event.getEventTypeLabel());
		
		if (!request.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			String contactFilter = "";
			for (StringTokenizer s=new StringTokenizer(request.getText().trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                contactFilter += " and (upper(c.firstName) like '%"+token+"%' or upper(c.middleName) like '%"+token+"%' or upper(c.lastName) like '%"+token+"%')";
            }
			List<EventContact> contacts = (List<EventContact>)hibSession.createQuery(
					"select distinct c from Event e inner join e.meetings m inner join e.mainContact c, Location l inner join l.session s " + restrictionsWhere +
					" where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate and m.locationPermanentId = l.permanentId " +
					restrictions + contactFilter +
					" order by c.lastName, c.firstName, c.middleName")
					.setLong("sessionId", request.getSessionId())
					.setCacheable(true)
					.setMaxResults(20)
					.list();
			for (EventContact contact: contacts)
				response.addSuggestion(contact.getName(), contact.getName(), "Requested By");

		}
	}

	@Override
	public void enumarate(FilterRpcRequest request, FilterRpcResponse response) {
		
	}

}
