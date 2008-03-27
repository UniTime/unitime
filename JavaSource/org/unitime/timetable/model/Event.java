/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org
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
 
package org.unitime.timetable.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.base.BaseEvent;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;



public class Event extends BaseEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Event () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Event (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Event (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.EventType eventType,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			eventType,
			minCapacity,
			maxCapacity);
	}

/*[CONSTRUCTOR MARKER END]*/

	
	
    public static List findEventsOfSubjectArea(org.hibernate.Session hibSession, Long subjectAreaId, Integer eventType) {
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.course.subjectArea.uniqueId=:subjectAreaId and e.eventType.uniqueId=:eventType")
                .setLong("subjectAreaId", subjectAreaId)
                .setInteger("examType", eventType)
                .setCacheable(true)
                .list();
    }
    
    public static List findEventsOfSubjectArea(Long subjectAreaId, Integer eventType) {
        return (findEventsOfSubjectArea((new EventDAO().getSession()), subjectAreaId, eventType));
    }
     
    public static List findExamsOfCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId, Integer eventType) {
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.course.uniqueId=:courseOfferingId and e.eventType.uniqueId=:eventType")
                .setLong("courseOfferingId", courseOfferingId)
                .setInteger("eventType", eventType)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourseOffering(Long courseOfferingId, Integer eventType) {
        return (findExamsOfCourseOffering((new EventDAO().getSession()),courseOfferingId, eventType));
    }
 
    public static List findEventsOfCourse(org.hibernate.Session hibSession, Long subjectAreaId, String courseNbr, Integer eventType) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findEventsOfSubjectArea(subjectAreaId, eventType);
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.course.subjectArea.uniqueId=:subjectAreaId and e.eventType.uniqueId=:eventType and "+
                (courseNbr.indexOf('*')>=0?"r.course.courseNbr like :courseNbr":"r.course.courseNbr=:courseNbr"))
                .setLong("subjectAreaId", subjectAreaId)
                .setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"))
                .setInteger("eventType", eventType)
                .setCacheable(true)
                .list();
    }

    
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, Long ownerId, Integer ownerType) {
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.ownerId=:ownerId and r.ownerType=:ownerType and e.eventType.uniqueId=:eventType")
                .setLong("ownerId", ownerId)
                .setInteger("ownerType", ownerType)
                .setLong("eventType", eventType)
                .setCacheable(true)
                .list();
    }
    
    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, Long ownerId, Integer ownerType) {
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, ownerId, ownerType));
    }
    
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, CourseOffering courseOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, courseOffering.getUniqueId(), ExamOwner.sOwnerTypeCourse));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, CourseOffering courseOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, courseOffering));
    }
    
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, InstructionalOffering instructionalOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, instructionalOffering.getUniqueId(), ExamOwner.sOwnerTypeOffering));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, InstructionalOffering instructionalOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, instructionalOffering));
    }
  
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, InstrOfferingConfig instrOffrConfig){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, instrOffrConfig.getUniqueId(), ExamOwner.sOwnerTypeConfig));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, InstrOfferingConfig instrOffrConfig){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, instrOffrConfig));
    }
  
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, Class_ clazz){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, clazz.getUniqueId(), ExamOwner.sOwnerTypeClass));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, Class_ clazz){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, clazz));
    }
  
    
    
    public static List findEventsOfCourse(Long subjectAreaId, String courseNbr, Integer eventType) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findEventsOfSubjectArea(subjectAreaId, eventType);
        return (findEventsOfCourse((new EventDAO().getSession()), subjectAreaId, courseNbr, eventType));
    }
    
    public Set<Object> getStudents() {
        HashSet<Object> students = new HashSet<Object>();
        for (Iterator<?> i=getRelatedCourses().iterator();i.hasNext();)
            students.addAll(((RelatedCourseInfo)i.next()).getStudents());
        return students;
  
    }
    
    public int countStudents() {
        int nrStudents = 0;
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            nrStudents += ((RelatedCourseInfo)i.next()).countStudents();
        return nrStudents;
       
    }
    
    public static void deleteFromEvents(org.hibernate.Session hibSession, Integer ownerType, Long ownerId) {
        for (Iterator i=hibSession.createQuery("select r from Event e inner join e.relatedCourses r where "+
                "r.ownerType=:ownerType and r.ownerId=:ownerId")
                .setInteger("ownerType", ownerType)
                .setLong("ownerId", ownerId).iterate();i.hasNext();) {
            RelatedCourseInfo relatedCourse = (RelatedCourseInfo)i.next();
            Event event = relatedCourse.getEvent();
            event.getRelatedCourses().remove(relatedCourse);
            relatedCourse.setOwnerId(null);
            relatedCourse.setCourse(null);
            hibSession.delete(relatedCourse);
            if (event.getRelatedCourses().isEmpty()) {
                hibSession.delete(event);
            } else {
                hibSession.saveOrUpdate(event);
            }
        }
    }
    
    public static void deleteFromEvents(org.hibernate.Session hibSession, Class_ clazz) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeClass, clazz.getUniqueId());
    }
    public static void deleteFromEvents(org.hibernate.Session hibSession, InstrOfferingConfig config) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeConfig, config.getUniqueId());
    }
    public static void deleteFromEvents(org.hibernate.Session hibSession, InstructionalOffering offering) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeOffering, offering.getUniqueId());
    }
    public static void deleteFromEvents(org.hibernate.Session hibSession, CourseOffering course) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeCourse, course.getUniqueId());
    }
    
	public String toString() {
		return (this.getEventName());
	}
	
}