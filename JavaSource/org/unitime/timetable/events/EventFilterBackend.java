/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.FinalExamEvent;
import org.unitime.timetable.model.MidtermExamEvent;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.UnavailableEvent;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EventFilterRpcRequest.class)
public class EventFilterBackend extends FilterBoxBackend<EventFilterRpcRequest> {
	public static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public void load(EventFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		EventQuery query = getQuery(request, context);
		
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();

		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		Map<Integer, Integer> type2count = new HashMap<Integer, Integer>();
		for (Object[] o: (List<Object[]>)query.select("e.class, count(distinct e)").group("e.class").order("e.class").exclude("query").exclude("type").query(hibSession).list()) {
			int type = ((Number)o[0]).intValue();
			int count = ((Number)o[1]).intValue();
			type2count.put(type, count);
		}
		for (int i = 0; i < Event.sEventTypesAbbv.length; i++) {
			Entity e = new Entity(new Long(i), Event.sEventTypesAbbv[i], Event.sEventTypesAbbv[i]);
			Integer count = type2count.get(i);
			e.setCount(count == null ? 0 : count);
			response.add("type", e);
		}
		
		Map<Integer, Integer> day2count = new HashMap<Integer, Integer>();
		for (Object[] o: (List<Object[]>)query.select(HibernateUtil.dayOfWeek("m.meetingDate") + ", count(distinct e)")
				.order(HibernateUtil.dayOfWeek("m.meetingDate")).group(HibernateUtil.dayOfWeek("m.meetingDate"))
				.exclude("query").exclude("day").query(hibSession).list()) {
			int type = Integer.parseInt(o[0].toString());
			int count = ((Number)o[1]).intValue();
			day2count.put(type, count);
		}
		for (int i = 0; i < Constants.DAY_NAMES_FULL.length; i++) {
			String day = Constants.DAY_NAMES_FULL[i];
			int type = 0;
			switch (i) {
			case Constants.DAY_SUN: type = 1; break;
			case Constants.DAY_MON: type = 2; break;
			case Constants.DAY_TUE: type = 3; break;
			case Constants.DAY_WED: type = 4; break;
			case Constants.DAY_THU: type = 5; break;
			case Constants.DAY_FRI: type = 6; break;
			case Constants.DAY_SAT: type = 7; break;
			}
			Integer count = day2count.get(type);
			Entity e = new Entity(new Long(type), day, day);
			e.setCount(count == null ? 0 : count);
			response.add("day", e);
		}
		
		Entity all = new Entity(0l, "All", "All Events");
		all.setCount(((Number)query.select("count(distinct e)").exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue());
		response.add("mode", all);
		if (context.isAuthenticated() && context.getUser().getCurrentAuthority() != null) {
			int myCnt = ((Number)query.select("count(distinct e)").where("e.mainContact.externalUniqueId = :user and e.class not in (ClassEvent, FinalExamEvent, MidtermExamEvent)").set("user", context.getUser().getExternalUserId())
					.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
			Entity my = new Entity(1l, "My", "My Events"); my.setCount(myCnt);
			response.add("mode", my);
			
			if (context.hasPermission(Right.HasRole)) {
				int approvedCnt = ((Number)query.select("count(distinct e)").where("m.approvalStatus = 1")
						.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue(); 
				Entity approved = new Entity(2l, "Approved", "Approved Events"); approved.setCount(approvedCnt);
				response.add("mode", approved);
				
				int notApprovedCnt = ((Number)query.select("count(distinct e)").where("m.approvalStatus = 0")
						.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
				Entity notApproved = new Entity(3l, "Unapproved", "Not Approved Events"); notApproved.setCount(notApprovedCnt);
				response.add("mode", notApproved);

				int conflictingCnt = ((Number)query.select("count(distinct e)").from("Meeting mx")
						.where("mx.uniqueId!=m.uniqueId and m.meetingDate=mx.meetingDate and m.startPeriod < mx.stopPeriod and m.stopPeriod > mx.startPeriod and m.locationPermanentId = mx.locationPermanentId and m.approvalStatus <= 1 and mx.approvalStatus <= 1")
						.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
				Entity conflicting = new Entity(5l, "Conflicting", "Conflicting Events"); conflicting.setCount(conflictingCnt);
				response.add("mode", conflicting);
				
				if (context.getUser().getCurrentAuthority().hasRight(Right.EventMeetingApprove)) {
					int awaitingCnt = ((Number)query.select("count(distinct e)").where("m.approvalStatus = 0 and m.meetingDate >= :today").set("today", today)
							.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
					Entity awaiting = new Entity(4l, "Awaiting", "Awaiting Events"); awaiting.setCount(awaitingCnt);
					response.add("mode", awaiting);

					if (context.getUser().getCurrentAuthority().hasRight(Right.EventMeetingApprove)) {
						int myApprovalCnt = ((Number)query.select("count(distinct e)").joinWithLocation().from("inner join l.eventDepartment.timetableManagers g")
								.where("m.approvalStatus = 0 and g.externalUniqueId = :user and m.meetingDate >= :today").set("user", context.getUser().getExternalUserId())
								.set("today", today).exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
						Entity myAwaiting = new Entity(6l, "My Awaiting", "Awaiting My Approval"); myAwaiting.setCount(myApprovalCnt);
						response.add("mode", myAwaiting);
					}
					
					int rejectedCnt = ((Number)query.select("count(distinct e)").where("m.approvalStatus >= 2")
							.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
					Entity rejected = new Entity(7l, "Cancelled", "Cancelled / Rejected"); rejected.setCount(rejectedCnt);
					response.add("mode", rejected);
				}
				
				if (context.getUser().getCurrentAuthority().hasRight(Right.EventSetExpiration)) {
					int expiringCnt = ((Number)query.select("count(distinct e)").where("m.approvalStatus = 0 and e.expirationDate is not null")
							.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
					Entity expiring = new Entity(8l, "Expiring", "Expiring Events"); expiring.setCount(expiringCnt);
					response.add("mode", expiring);
				}
			}
		}

		for (Object[] org: (List<Object[]>)query.select("o.uniqueId, o.name, count(distinct e)").from("inner join e.sponsoringOrganization o")
				.group("o.uniqueId, o.name").order("o.name").exclude("query").exclude("sponsor").query(hibSession).list()) {
			Long id = (Long)org[0];
			String name = (String)org[1];
			int count = ((Number)org[2]).intValue();
			Entity sponsor = new Entity(id, name, name); sponsor.setCount(count);
			response.add("sponsor", sponsor);
		}
	}
	
	public static EventQuery getQuery(FilterRpcRequest request, EventContext context) {
		EventQuery query = new EventQuery(request.getSessionId());
		
		if (request.hasOptions("flag")) {
			if (request.getOption("flag").contains("All Sessions"))
				query.checkSession(false);
		}
		
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		
		if (!context.hasPermission(Right.HasRole)) {
			Session session = SessionDAO.getInstance().get(request.getSessionId());
			String prohibitedTypes = null;
			if (!session.getStatusType().canNoRoleReportClass())
				prohibitedTypes = "ClassEvent";
			if (!session.getStatusType().canNoRoleReportExamFinal())
				prohibitedTypes = (prohibitedTypes == null ? "FinalExamEvent": prohibitedTypes + ",FinalExamEvent");
			if (!session.getStatusType().canNoRoleReportExamMidterm())
				prohibitedTypes = (prohibitedTypes == null ? "MidtermExamEvent": prohibitedTypes + ",MidtermExamEvent");
			if (prohibitedTypes != null)
				query.addWhere("xtype", "e.class not in (" + prohibitedTypes + ")");						
		}
		
		if (request.getText() != null && !request.getText().isEmpty()) {
			query.addWhere("query", "lower(e.eventName) like lower(:Xquery) || '%'" + (request.getText().length() >= 2 ? " or lower(e.eventName) like '% ' || lower(:Xquery) || '%'" : ""));
			query.addParameter("query", "Xquery", request.getText());
		}
		
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
				if (t.equals(Event.sEventTypesAbbv[Event.sEventTypeUnavailable]))
					type += UnavailableEvent.class.getName();
			}
			query.addWhere("type", "e.class in (" + type + ")");
		}
		
		Integer after = null;
		if (request.hasOption("after")) {
			after = TimeSelector.TimeUtils.parseTime(CONSTANTS, request.getOption("after"), null);
			query.addWhere("after", "m.stopPeriod > :Xafter");
			query.addParameter("after", "Xafter", after);
		}
		if (request.hasOption("before")) {
			query.addWhere("before", "m.startPeriod < :Xbefore");
			query.addParameter("before", "Xbefore", TimeSelector.TimeUtils.parseTime(CONSTANTS, request.getOption("before"), after));
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
        		query.addWhere("day", HibernateUtil.dayOfWeek("m.meetingDate") + " in (" + dow + ")");
        	else
        		query.addWhere("day", HibernateUtil.dayOfWeek("m.meetingDate") + " = " + dow);
		}
		if (request.hasOption("from")) {
			Date date = null;
			try {
				int dayOfYear = Integer.parseInt(request.getOption("from"));
				date = DateUtils.getDate(SessionDAO.getInstance().get(request.getSessionId()).getSessionStartYear(), dayOfYear);
			} catch (NumberFormatException f) {
				try {
					date = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).parse(request.getOption("from"));
				} catch (ParseException p) {}
			}
			if (date != null) {
				query.addParameter("from", "Xfrom", date);
				query.addWhere("from", "m.meetingDate >= :Xfrom");
			}
		}
		if (request.hasOption("to")) {
			Date last = null;
			try {
				int dayOfYear = Integer.parseInt(request.getOption("to"));
				last = DateUtils.getDate(SessionDAO.getInstance().get(request.getSessionId()).getSessionStartYear(), dayOfYear);
			} catch (NumberFormatException f) {
				try {
					last = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).parse(request.getOption("to"));
				} catch (ParseException p) {}
				
			}
			if (last != null) {
				query.addParameter("to", "Xto", last);
				query.addWhere("to", "m.meetingDate <= :Xto");
			}
		}
		if (request.hasOptions("dates")) {
			List<Date> dates = new ArrayList<Date>();
			String ids = "";
			for (String d: request.getOptions("dates")) {
				Date date = null;
				try {
					int dayOfYear = Integer.parseInt(d);
					date = DateUtils.getDate(SessionDAO.getInstance().get(request.getSessionId()).getSessionStartYear(), dayOfYear);
				} catch (NumberFormatException f) {
					try {
						date = Formats.getDateFormat(Formats.Pattern.DATE_EVENT).parse(d);
					} catch (ParseException p) {}
				}
				if (date != null) {
					ids += (ids.isEmpty() ? "" : ",") + ":Xdate" + dates.size();
					dates.add(date);
				}
			}
			if (!dates.isEmpty()) {
				query.addWhere("dates", "m.meetingDate in (" + ids + ")");
				for (int i = 0; i < dates.size(); i++)
					query.addParameter("dates", "Xdate" + i, dates.get(i));
			}
		}
		if (request.hasOptions("room")) {
			String ids = "";
			for (String id: request.getOptions("room")) {
				ids += (ids.isEmpty() ? "" : ",") + id;
			}
			query.addWhere("room", "l.uniqueId in (" + ids + ")");
		}
		
		if (request.hasOptions("sponsor")) {
			String sponsor = "";
			int id = 0;
			for (String s: request.getOptions("sponsor")) {
				sponsor += (sponsor.isEmpty() ? "" : ",") + ":Xsp" + id;
				query.addParameter("sponsor", "Xsp" + id, s);
				id++;
			}
			query.addWhere("sponsor", "e.sponsoringOrganization.name in (" + sponsor + ")");
		}
		
		if (request.hasOption("mode")) {
			String mode = request.getOption("mode");
			if ("My Events".equals(mode) && context.isAuthenticated()) {
				query.addWhere("mode", "e.mainContact.externalUniqueId = :Xowner and e.class not in (ClassEvent, FinalExamEvent, MidtermExamEvent)");
				query.addParameter("mode", "Xowner", context.getUser().getExternalUserId());
			} else if ("Approved Events".equals(mode)) {
				query.addWhere("mode", "m.approvalStatus = 1");
			} else if ("Not Approved Events".equals(mode)) {
				query.addWhere("mode", "m.approvalStatus = 0");
			} else if ("Awaiting Events".equals(mode)) {
				query.addWhere("mode", "m.approvalStatus = 0 and m.meetingDate >= :Xtoday");
				query.addParameter("mode", "Xtoday", today);
			} else if ("Awaiting My Approval".equals(mode) && context.isAuthenticated()) {
				query.addFrom("mode", "Location Xl inner join Xl.eventDepartment.timetableManagers Xg");
				query.addWhere("mode", "m.approvalStatus = 0 and Xl.session.uniqueId = :sessionId and Xl.permanentId = m.locationPermanentId and Xg.externalUniqueId = :Xuser and m.meetingDate >= :Xtoday");
				query.addParameter("mode", "Xuser", context.getUser().getExternalUserId());
				query.addParameter("mode", "Xtoday", today);
			} else if ("Conflicting Events".equals(mode)) {
				query.addFrom("mode", "Meeting Xm");
				query.addWhere("mode", "Xm.uniqueId != m.uniqueId and m.meetingDate = Xm.meetingDate and m.startPeriod < Xm.stopPeriod and m.stopPeriod > Xm.startPeriod and m.locationPermanentId = Xm.locationPermanentId and m.approvalStatus <= 1 and Xm.approvalStatus <= 1");
			} else if ("Cancelled / Rejected".equals(mode)) {
				query.addWhere("mode", "m.approvalStatus >= 2");
			} else if ("Expiring Events".equals(mode)) {
				query.addWhere("mode", "m.approvalStatus = 0 and e.expirationDate is not null");
			} else {
				query.addWhere("mode", "m.approvalStatus <= 1");
			}
		} else {
			if (!request.hasOption("requested"))
				query.addWhere("mode", "m.approvalStatus <= 1");
		}
		
		if (request.hasOption("requested") ) {
			String requested = "";
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getOption("requested").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                requested += (requested.isEmpty() ? "" : " and ") + "(upper(e.mainContact.firstName) like :Xreq" + id + " || '%' or " +
                		"upper(e.mainContact.middleName) like :Xreq" + id + " || '%' or upper(e.mainContact.lastName) like :Xreq" + id + " || '%' or upper(e.mainContact.emailAddress) like :Xreq" + id + " || '%')";
                query.addParameter("requested", "Xreq" + id, token);
                id++;
            }
			if (id > 0)
				query.addWhere("requested", requested);
		}
		
		return query;
	}

	@Override
	public void suggestions(EventFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		
		EventQuery query = getQuery(request, context);
		
		for (Event event: (List<Event>)query.select("distinct e").limit(20).order("e.eventName").query(hibSession).list())
			response.addSuggestion(event.getEventName(), event.getEventName(), event.getEventTypeLabel());
		
		if ((context.hasPermission(Right.EventLookupContact) || context.hasPermission(Right.EventLookupSchedule)) && (!request.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20))) {
			EventQuery.EventInstance instance = query.select("distinct c").from("inner join e.mainContact c").exclude("sponsor").exclude("requested").exclude("query");
			
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getText().trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                instance.where("upper(c.firstName) like :cn" + id + " || '%' or upper(c.middleName) like :cn" + id + " || '%' or upper(c.lastName) like :cn" + id + " || '%' or upper(c.emailAddress) like :cn" + id + " || '%'").set("cn" + id, token);
                id++;
            }
			
			if (id > 0)
				for (EventContact contact: (List<EventContact>)instance.limit(20).query(hibSession).list())
					response.addSuggestion(contact.getName(), contact.getName(), "Requested By", "requested");

		}
	}

	@Override
	public void enumarate(EventFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		
		EventQuery query = getQuery(request, context);
		
		for (Event event: (List<Event>)query.select("distinct e").query(hibSession).list()) {
			Entity entity = new Entity(event.getUniqueId(), event.getEventTypeAbbv(), event.getEventName());
			response.addResult(entity);
		}
	}
	
	public static class EventQuery {
		private Long iSessionId;
		private boolean iCheckSession = true;
		private Map<String, String> iFrom = new HashMap<String, String>();
		private Map<String, String> iWhere = new HashMap<String, String>();
		private Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public EventQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public void checkSession(boolean check) { iCheckSession = check; }
		
		public void addFrom(String option, String from) { iFrom.put(option, from); }
		public void addWhere(String option, String where) { iWhere.put(option, where); }

		private void addParameter(String option, String name, Object value) {
			Map<String, Object> params = iParams.get(option);
			if (params == null) { params = new HashMap<String, Object>(); iParams.put(option, params); }
			params.put(name, value);
		}
		
		public String getFrom(Collection<String> excludeOption) {
			String from = "";
			for (Map.Entry<String, String> entry: iFrom.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				from += ", " + entry.getValue();
			}
			return from;
		}
		
		public String getWhere(Collection<String> excludeOption) {
			String where = "";
			for (Map.Entry<String, String> entry: iWhere.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				where += " and (" + entry.getValue() + ")";
			}
			return where;
		}
		
		public org.hibernate.Query setParams(org.hibernate.Query query, Collection<String> excludeOption) {
			for (Map.Entry<String, Map<String, Object>> entry: iParams.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				for (Map.Entry<String, Object> param: entry.getValue().entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setInteger(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setLong(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setString(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setBoolean(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setDate(param.getKey(), (Date)param.getValue());
					} else {
						query.setString(param.getKey(), param.getValue().toString());
					}
				}
			}
			return query;
		}
		
		public EventInstance select(String select) {
			return new EventInstance(select, iWhere.containsKey("room"));
		}
		
		
		public class EventInstance {
			private String iSelect = null, iFrom = null, iWhere = null, iOrderBy = null, iGroupBy = null, iType = "Event";
			private Integer iLimit = null;
			private boolean iJoinWithLocation = false;
			private Set<String> iExclude = new HashSet<String>();
			private Map<String, Object> iParams = new HashMap<String, Object>();
			
			private EventInstance(String select, boolean joinWithLocation) {
				iSelect = select;
				iJoinWithLocation = joinWithLocation;
			}
			
			public EventInstance from(String from) { iFrom = from; return this; }
			public EventInstance where(String where) { 
				if (iWhere == null)
					iWhere = "(" + where + ")";
				else
					iWhere += " and (" + where + ")";
				return this;
			}
			public EventInstance type(String type) { iType = type; return this; }
			public EventInstance order(String orderBy) { iOrderBy = orderBy; return this; }
			public EventInstance group(String groupBy) { iGroupBy = groupBy; return this; }
			public EventInstance exclude(String excludeOption) { iExclude.add(excludeOption); return this; }
			public EventInstance set(String param, Object value) { iParams.put(param, value); return this; }
			public EventInstance limit(Integer limit) { iLimit = (limit == null || limit <= 0 ? null : limit); return this; }
			public EventInstance joinWithLocation() { iJoinWithLocation = true; return this; }
			
			public String query() {
				return
					"select " + (iSelect == null ? "distinct e" : iSelect) +
					" from " + iType + " e inner join e.meetings m" + (iJoinWithLocation ? ", Location l inner join l.session s" : ", Session s" ) + 
					(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					(iCheckSession ? "" : ", Session z") +
					" where " +
					(iCheckSession ?
						"s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate" : 
						"z.uniqueId = :sessionId and s.academicInitiative = z.academicInitiative" ) +
					(iJoinWithLocation ? " and m.locationPermanentId = l.permanentId" : "") +
					getWhere(iExclude) + (iWhere == null ? "" : " and (" + iWhere + ")") +
					(iGroupBy == null ? "" : " group by " + iGroupBy) +
					(iOrderBy == null ? "" : " order by " + iOrderBy);
			}
			
			public org.hibernate.Query query(org.hibernate.Session hibSession) {
				org.hibernate.Query query = setParams(hibSession.createQuery(query()), iExclude).setLong("sessionId", iSessionId).setCacheable(true);
				for (Map.Entry<String, Object> param: iParams.entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setInteger(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setLong(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setString(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setBoolean(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setDate(param.getKey(), (Date)param.getValue());
					} else {
						query.setString(param.getKey(), param.getValue().toString());
					}
				}
				if (iLimit != null)
					query.setMaxResults(iLimit);
				return query;
			}
		}
	}

}