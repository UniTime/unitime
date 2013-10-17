/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.cpsolver.ifs.util.ToolBox;

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
