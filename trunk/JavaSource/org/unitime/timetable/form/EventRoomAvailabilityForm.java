/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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
package org.unitime.timetable.form;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Query;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;

public class EventRoomAvailabilityForm extends ActionForm {

	private Long iSessionId;
	private String iLocationType;
	private List iLocations;
	private int iStartTime;
	private int iStopTime;
	private TreeSet<Date> iMeetingDates = new TreeSet();;
	private Date iSomeDate = new Date();
	private Long iBuildingId;
	private String iRoomNumber;
	private String iMinCapacity;
	private String iMaxCapacity;
	private String iOp;
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iSessionId = null;
		iLocations = null;
		iSomeDate = null;
		iOp = null;
		iStartTime = 30;
		iStopTime = 70;
		iLocationType = null;
		iMeetingDates.clear();
		iMinCapacity = null;
		iMaxCapacity = null;
		iLocations = null;
	}

	
	public void load (HttpSession session) {
		iMeetingDates = (TreeSet<Date>) session.getAttribute("Event.MeetingDates");
		iSessionId = (Long) session.getAttribute("Event.SessionId");
		iSomeDate = iMeetingDates.first();
		iStartTime = (Integer) session.getAttribute("Event.StartTime");
		iStopTime = (Integer) session.getAttribute("Event.StopTime");
		iLocationType = (String) session.getAttribute("Event.LocationType");
		iMeetingDates = (TreeSet<Date>) session.getAttribute("Event.MeetingDates");
		iMinCapacity = (String) session.getAttribute("Event.MinCapacity");
		iMaxCapacity = (String) session.getAttribute("Event.MaxCapacity");
		iBuildingId = (Long) session.getAttribute("Event.BuildingId");
		iRoomNumber = (String) session.getAttribute("Event.RoomNumber");
		iLocations = getPossibleLocations();
	}
	
	public void save (HttpSession session) {
		
	}
	
	public List getPossibleLocations() {
		Query hibQuery = new LocationDAO().getSession().createQuery("select r from Room r where r.building.uniqueId = :buildingId");
		hibQuery.setLong("buildingId", iBuildingId);
		System.out.println(iBuildingId);
		return hibQuery.setCacheable(true).list();
	}

	public TreeSet getOverlappingMeetings(Date meetingDate, int startTime, int stopTime, Location location) {
		Calendar start = Calendar.getInstance();
		start.setTime(meetingDate);
		start.set(Calendar.HOUR, startTime/4);
		start.set(Calendar.MINUTE, (startTime%4)*15);
		start.set(Calendar.SECOND,0);
		Calendar stop = Calendar.getInstance();
		stop.setTime(meetingDate);
		stop.set(Calendar.HOUR, stopTime/4);
		stop.set(Calendar.MINUTE, (stopTime%4)*15);
		stop.set(Calendar.SECOND,0);
		Query hibQuery = new MeetingDAO().getSession().createQuery("Select m from Meeting m where m.startTime <=:stopTime and m.stopTime>=:startTime and m.location.uniqueId=:locId");
			hibQuery.setDate("startTime",start.getTime());
			hibQuery.setDate("stopTime", stop.getTime());
			hibQuery.setLong("locId",223206l);
//		System.out.print(hibQuery.setCacheable(true).list());
		return (TreeSet) hibQuery.setCacheable(true).list();	
	}

	public List getMeetings () {
/*		List overlappingMeetings = null;
		for (Iterator i=iLocations.iterator(); i.hasNext();){
			Room location = (Room) i.next(); 
			Query q = new MeetingDAO().getSession().createQuery("Select m from Meeting m where m.location.uniqueId = :locationId");
			q.setLong("locationId", location.getUniqueId());
			overlappingMeetings.addAll(q.setCacheable(true).list());
		}
		return (TreeSet) overlappingMeetings;
	//	getOverlappingMeetings(iSomeDate, iStartTime, iStopTime);
*/
		Meeting m;
		//m.g
		Query q = new MeetingDAO().getSession().createQuery("Select m from Meeting m where m.startPeriod<=:stopTime and m.stopPeriod>=:startTime");
			q.setInteger("startTime", iStartTime);
			q.setInteger("stopTime", iStopTime);
		return q.setCacheable(true).list();
		
	}

	public List getLocations() {return iLocations;}
	public void setLocations(List locations) {iLocations=locations;}
	
	public int getStartTime() {return iStartTime;}
	public void setStartTime(int startTime) {iStartTime = startTime;}
	
	public int getStopTime() {return iStopTime;}
	public void setStopTime(int stopTime) {iStopTime = stopTime;}
	
	
    // Query hibQuery = new EventDAO().getSession().createQuery(query);
    // List events = hibQuery.setCacheable(true).list();

	
}
