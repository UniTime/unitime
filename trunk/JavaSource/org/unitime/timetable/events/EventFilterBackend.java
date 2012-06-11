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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.FinalExamEvent;
import org.unitime.timetable.model.MidtermExamEvent;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

public class EventFilterBackend extends FilterBoxBackend {
	
	@Override
	public void load(FilterRpcRequest request, FilterRpcResponse response, EventRights rights) {
		EventQuery query = getQuery(request);

		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		for (Object[] o: (List<Object[]>)query.select("e.class, count(distinct e)").group("e.class").order("e.class").exclude("query").exclude("type").query(hibSession).list()) {
			int type = ((Number)o[0]).intValue();
			int count = ((Number)o[1]).intValue();
			Entity e = new Entity(new Long(type), Event.sEventTypesAbbv[type], Event.sEventTypesAbbv[type]);
			e.setCount(count);
			response.add("type", e);
		}
		
		for (Object[] o: (List<Object[]>)query.select(HibernateUtil.dayOfWeek("m.meetingDate") + ", count(distinct e)")
				.order(HibernateUtil.dayOfWeek("m.meetingDate")).group(HibernateUtil.dayOfWeek("m.meetingDate"))
				.exclude("query").exclude("day").query(hibSession).list()) {
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
		}
		
		Entity all = new Entity(0l, "All", "All Events");
		all.setCount(((Number)query.select("count(distinct e)").exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue());
		response.add("mode", all);
		if (request.hasOption("user")) {
			int myCnt = ((Number)query.select("count(distinct e)").where("e.mainContact.externalUniqueId = :user").set("user", request.getOption("user"))
					.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
			if (myCnt > 0) {
				Entity my = new Entity(1l, "My", "My Events"); my.setCount(myCnt);
				response.add("mode", my);
			}
			String role = request.getOption("role");
			if (role != null) {
				int approvedCnt = ((Number)query.select("count(distinct e)").where("m.approvedDate is not null")
						.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue(); 
				if (approvedCnt > 0) {
					Entity approved = new Entity(2l, "Approved", "Approved Events"); approved.setCount(approvedCnt);
					response.add("mode", approved);
				}
				
				int awaitingCnt = ((Number)query.select("count(distinct e)").where("m.approvedDate is null")
						.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
				if (awaitingCnt > 0) {
					Entity awaiting = new Entity(2l, "Awaiting", "Awaiting Events"); awaiting.setCount(awaitingCnt);
					response.add("mode", awaiting);
				}
				
				int conflictingCnt = ((Number)query.select("count(distinct e)").from("Meeting mx")
						.where("mx.uniqueId!=m.uniqueId and m.meetingDate=mx.meetingDate and m.startPeriod < mx.stopPeriod and m.stopPeriod > mx.startPeriod and m.locationPermanentId = mx.locationPermanentId")
						.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
				if (conflictingCnt > 0) {
					Entity conflicting = new Entity(2l, "Conflicting", "Conflicting Events"); conflicting.setCount(conflictingCnt);
					response.add("mode", conflicting);
				}
				
				if (Roles.EVENT_MGR_ROLE.equals(role)) {
					int myApprovalCnt = ((Number)query.select("count(distinct e)").joinWithLocation().from("inner join l.roomDepts rd inner join rd.department.timetableManagers g")
							.where("m.approvedDate is null and rd.control=true and g.externalUniqueId = :user").set("user", request.getOption("user"))
							.exclude("query").exclude("mode").query(hibSession).uniqueResult()).intValue();
					if (myApprovalCnt > 0) {
						Entity awaiting = new Entity(2l, "My Awaiting", "Awaiting My Approval"); awaiting.setCount(myApprovalCnt);
						response.add("mode", awaiting);
					}					
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
		
		response.add("other", new Entity(0l, "Conflicts", "Display Conflicts"));
	}
	
	public static EventQuery getQuery(FilterRpcRequest request) {
		EventQuery query = new EventQuery(request.getSessionId());
		
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
			}
			query.addWhere("type", "e.class in (" + type + ")");
		}
		
		Integer after = null;
		if (request.hasOption("after")) {
			after = TimeSelector.TimeUtils.parseTime(request.getOption("after"), null);
			query.addWhere("after", "m.stopPeriod > :Xafter");
			query.addParameter("after", "Xafter", after);
		}
		if (request.hasOption("before")) {
			query.addWhere("before", "m.startPeriod < :Xbefore");
			query.addParameter("before", "Xbefore", TimeSelector.TimeUtils.parseTime(request.getOption("before"), after));
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
					date = new SimpleDateFormat(CONSTANTS.eventDateFormat()).parse(request.getOption("from"));
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
					last = new SimpleDateFormat(CONSTANTS.eventDateFormat()).parse(request.getOption("to"));
				} catch (ParseException p) {}
				
			}
			if (last != null) {
				query.addParameter("to", "Xto", last);
				query.addWhere("to", "m.meetingDate <= :Xto");
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
			if ("My Events".equals(mode)) {
				query.addWhere("mode", "e.mainContact.externalUniqueId = '" + request.getOption("user") + "'");
			} else if ("Approved Events".equals(mode)) {
				query.addWhere("mode", "m.approvedDate is not null");
			} else if ("Awaiting Events".equals(mode)) {
				query.addWhere("mode", "m.approvedDate is null");
			} else if ("Awaiting My Approval".equals(mode)) {
				query.addFrom("mode", "Location Xl inner join Xl.roomDepts Xrd inner join Xrd.department.timetableManagers Xg");
				query.addWhere("mode", "m.approvedDate is null and Xl.session.uniqueId = :sessionId and Xl.permanentId = m.locationPermanentId and Xrd.control=true and Xg.externalUniqueId = :Xuser");
				query.addParameter("mode", "Xuser", request.getOption("user"));
			} else if ("Conflicting Events".equals(mode)) {
				query.addFrom("mode", "Meeting Xm");
				query.addWhere("mode", "Xm.uniqueId != m.uniqueId and m.meetingDate = Xm.meetingDate and m.startPeriod < Xm.stopPeriod and m.stopPeriod > Xm.startPeriod and m.locationPermanentId = Xm.locationPermanentId");
			}
		}
		
		if (request.hasOption("requested") ) {
			String requested = "";
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getOption("requested").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                requested += (requested.isEmpty() ? "" : " and ") + "(upper(e.mainContact.firstName) like '%' || :Xreq" + id + " || '%' or " +
                		"upper(e.mainContact.middleName) like '%' || :Xreq" + id + " || '%' or upper(e.mainContact.lastName) like '%' || :Xreq" + id + " || '%')";
                query.addParameter("requested", "Xreq" + id, token);
                id++;
            }
			query.addWhere("requested", requested);
		}
		
		return query;
	}

	@Override
	public void suggestions(FilterRpcRequest request, FilterRpcResponse response, EventRights rights) {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		
		EventQuery query = getQuery(request);
		
		for (Event event: (List<Event>)query.select("distinct e").limit(20).order("e.eventName").query(hibSession).list())
			response.addSuggestion(event.getEventName(), event.getEventName(), event.getEventTypeLabel());
		
		if (rights.canLookupContacts() && (!request.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20))) {
			EventQuery.EventInstance instance = query.select("distinct c").from("inner join e.mainContact c").exclude("sponsor").exclude("query");
			
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getText().trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                instance.where("upper(c.firstName) like '%' || :cn" + id + " || '%' or upper(c.middleName) like '%' || :cn" + id + " || '%' or upper(c.lastName) like '%' || :cn" + id + " || '%'").set("cn" + id, token);
            }
			
			for (EventContact contact: (List<EventContact>)instance.limit(20).query(hibSession).list())
				response.addSuggestion(contact.getName(), contact.getName(), "Requested By");

		}
	}

	@Override
	public void enumarate(FilterRpcRequest request, FilterRpcResponse response, EventRights rights) {
		org.hibernate.Session hibSession = EventDAO.getInstance().getSession();
		
		EventQuery query = getQuery(request);
		
		for (Event event: (List<Event>)query.select("distinct e").query(hibSession).list()) {
			Entity entity = new Entity(event.getUniqueId(), event.getEventTypeAbbv(), event.getEventName());
			response.addResult(entity);
		}
	}
	
	public static class EventQuery {
		private Long iSessionId;
		private Map<String, String> iFrom = new HashMap<String, String>();
		private Map<String, String> iWhere = new HashMap<String, String>();
		private Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public EventQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
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
					" from " + iType + " e inner join e.meetings m" + (iJoinWithLocation ? ", Location l inner join l.session s" : ", Session s") + 
					(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					" where s.uniqueId = :sessionId and m.meetingDate >= s.eventBeginDate and m.meetingDate <= s.eventEndDate" +
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