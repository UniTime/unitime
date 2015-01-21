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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.base.BaseEventDateMapping;
import org.unitime.timetable.model.dao.EventDateMappingDAO;

/**
 * @author Tomas Muller
 */
public class EventDateMapping extends BaseEventDateMapping implements Comparable<EventDateMapping> {
	private static final long serialVersionUID = 1L;

	public EventDateMapping() {
		super();
	}
	
	public Date getClassDate() {
	    Calendar c = Calendar.getInstance(Locale.US);
	    c.setTime(getSession().getSessionBeginDateTime());
	    c.add(Calendar.DAY_OF_YEAR, getClassDateOffset());
	    return c.getTime();
	}

	public void setClassDate(Date classDate) {
		long diff = classDate.getTime() - getSession().getSessionBeginDateTime().getTime();
		setClassDateOffset((int)Math.round(diff/(1000.0 * 60 * 60 * 24)));
	}
	
	public Date getEventDate() {
		Calendar c = Calendar.getInstance(Locale.US);
	    c.setTime(getSession().getSessionBeginDateTime());
	    c.add(Calendar.DAY_OF_YEAR, getEventDateOffset());
	    return c.getTime();
	}
	
	public void setEventDate(Date eventDate) {
		long diff = eventDate.getTime() - getSession().getSessionBeginDateTime().getTime();
		setEventDateOffset((int)Math.round(diff/(1000.0 * 60 * 60 * 24)));
	}
	
	public static List<EventDateMapping> findAll(Long sessionId) {
		return (List<EventDateMapping>)EventDateMappingDAO.getInstance().getSession().createQuery(
				"from EventDateMapping where session.uniqueId = :sessionId order by classDateOffset")
				.setLong("sessionId", sessionId).setCacheable(true).list();
	}
	
	public static boolean hasMapping(Long sessionId) {
		return ((Number)EventDateMappingDAO.getInstance().getSession().createQuery(
				"select count(m) from EventDateMapping m where m.session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).intValue() > 0;
	}
	
	public static Class2EventDateMap getMapping(Long sessionId) {
		return sessionId == null ? null : new Class2EventDateMap(findAll(sessionId));
	}

	@Override
	public int compareTo(EventDateMapping m) {
		int cmp = getClassDateOffset().compareTo(m.getClassDateOffset());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(m.getUniqueId() == null ? -1 : m.getUniqueId());
	}

	public static class Class2EventDateMap {
		private SimpleDateFormat iDateFormat = new SimpleDateFormat("yyMMdd");
		private Map<String, Date> iClass2EventDates = new HashMap<String, Date>();
		private Map<String, Date> iEvent2ClassDates = new HashMap<String, Date>();
		
		public Class2EventDateMap() {
		}
		
		public Class2EventDateMap(List<EventDateMapping> mappings) {
			for (EventDateMapping mapping: mappings)
				addMapping(mapping.getClassDate(), mapping.getEventDate());
		}
		
		public void addMapping(Date classDate, Date eventDate) {
			iClass2EventDates.put(iDateFormat.format(classDate), eventDate);
			iEvent2ClassDates.put(iDateFormat.format(eventDate), classDate);
		}
		
		public boolean hasEventDate(Date eventDate) {
			return (eventDate == null ? false : iEvent2ClassDates.containsKey(iDateFormat.format(eventDate)));
		}
		
		public Date getClassDate(Date eventDate) {
			Date classDate = (eventDate == null ? null : iEvent2ClassDates.get(iDateFormat.format(eventDate)));
			if (classDate == null && hasClassDate(eventDate)) return null;
			return (classDate == null ? eventDate : classDate);
		}
		
		public boolean hasClassDate(Date classDate) {
			return (classDate == null ? false : iClass2EventDates.containsKey(iDateFormat.format(classDate)));
		}
		
		public Date getEventDate(Date classDate) {
			Date eventDate = (classDate == null ? null : iClass2EventDates.get(iDateFormat.format(classDate)));
			return (eventDate == null ? classDate : eventDate);
		}

		public String toString() {
			return ToolBox.dict2string(iClass2EventDates, 2);
		}
	}
}
