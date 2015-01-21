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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.unitime.timetable.model.base.BaseStudentSectioningQueue;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.security.UserContext;

/**
 * @author Tomas Muller
 */
public class StudentSectioningQueue extends BaseStudentSectioningQueue implements Comparable<StudentSectioningQueue> {
	private static final long serialVersionUID = 8492171207847794888L;

	public StudentSectioningQueue() {
		super();
	}
	
	public static enum Type {
		STUDENT_ENROLLMENT_CHANGE,
		CLASS_ASSIGNMENT_CHANGE,
		SESSION_STATUS_CHANGE,
		SESSION_RELOAD,
		OFFERING_CHANGE
	}
	
	public static TreeSet<StudentSectioningQueue> getItems(org.hibernate.Session hibSession, Long sessionId, Date lastTimeStamp) {
		if (sessionId != null) {
			if (lastTimeStamp == null) {
				return new TreeSet<StudentSectioningQueue>(
						hibSession.createQuery("select q from StudentSectioningQueue q where q.sessionId = :sessionId")
						.setLong("sessionId", sessionId).list());
			} else {
				return new TreeSet<StudentSectioningQueue>(
						hibSession.createQuery("select q from StudentSectioningQueue q where q.sessionId = :sessionId and q.timeStamp > :timeStamp")
						.setLong("sessionId", sessionId).setTimestamp("timeStamp", lastTimeStamp).list());
			}
		} else {
			if (lastTimeStamp == null) {
				return new TreeSet<StudentSectioningQueue>(
						hibSession.createQuery("select q from StudentSectioningQueue q").list());
			} else {
				return new TreeSet<StudentSectioningQueue>(
						hibSession.createQuery("select q from StudentSectioningQueue q where q.timeStamp > :timeStamp")
						.setTimestamp("timeStamp", lastTimeStamp).list());
			}
		}
	}
	
	public static Date getLastTimeStamp(org.hibernate.Session hibSession, Long sessionId) {
		if (sessionId != null)
			return (Date) hibSession.createQuery(
						"select max(q.timeStamp) from StudentSectioningQueue q where q.sessionId = :sessionId"
					).setLong("sessionId", sessionId).uniqueResult();
		else
			return (Date) hibSession.createQuery("select max(q.timeStamp) from StudentSectioningQueue q").uniqueResult();
			
	}

	@Override
	public int compareTo(StudentSectioningQueue q) {
		int cmp = getTimeStamp().compareTo(q.getTimeStamp());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(q.getUniqueId() == null ? -1 : q.getUniqueId());
	}
	
	protected static void addItem(org.hibernate.Session hibSession, UserContext user, Long sessionId, Type type, Collection<Long> ids) {
		StudentSectioningQueue q = new StudentSectioningQueue();
		q.setTimeStamp(new Date());
		q.setType(type.ordinal());
		q.setSessionId(sessionId);
		Document d = DocumentHelper.createDocument();
		Element root = d.addElement("generic");
		if (user != null) {
			Element e = root.addElement("user");
			e.addAttribute("id", user.getExternalUserId()).setText(user.getName());
		}
		if (ids != null && !ids.isEmpty()) {
			for (Long id: ids)
				root.addElement("id").setText(id.toString());
		}
		q.setMessage(d);
		hibSession.save(q);
	}
	
	protected static void addItem(org.hibernate.Session hibSession, UserContext user, Long sessionId, Type type, Long... ids) {
		StudentSectioningQueue q = new StudentSectioningQueue();
		q.setTimeStamp(new Date());
		q.setType(type.ordinal());
		q.setSessionId(sessionId);
		Document d = DocumentHelper.createDocument();
		Element root = d.addElement("generic");
		if (user != null) {
			Element e = root.addElement("user");
			e.addAttribute("id", user.getExternalUserId()).setText(user.getName());
		}
		if (ids != null && ids.length > 0) {
			for (Long id: ids)
				root.addElement("id").setText(id.toString());
		}
		q.setMessage(d);
		hibSession.save(q);
	}
	
	public List<Long> getIds() {
		if (getMessage() == null) return null;
		Element root = getMessage().getRootElement();
		if (!"generic".equals(root.getName())) return null;
		List<Long> ids = new ArrayList<Long>();
		for (Iterator<Element> i = root.elementIterator("id"); i.hasNext(); )
			ids.add(Long.valueOf(i.next().getText()));
		return ids;
	}
	
	public OnlineSectioningLog.Entity getUser() {
		if (getMessage() == null) return null;
		Element root = getMessage().getRootElement();
		if (!"generic".equals(root.getName())) return null;
		Element user = root.element("user");
		if (user == null)
			return OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
				.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
				.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
		else
			return OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(user.attributeValue("id"))
				.setName(user.getText())
				.setType(OnlineSectioningLog.Entity.EntityType.MANAGER).build();
	}
	
	public static void sessionStatusChanged(org.hibernate.Session hibSession, UserContext user, Long sessionId, boolean reload) {
		addItem(hibSession, user, sessionId, (reload ? Type.SESSION_RELOAD : Type.SESSION_STATUS_CHANGE));
	}
	
	public static void allStudentsChanged(org.hibernate.Session hibSession, UserContext user, Long sessionId) {
		addItem(hibSession, user, sessionId, Type.STUDENT_ENROLLMENT_CHANGE);
	}

	public static void studentChanged(org.hibernate.Session hibSession, UserContext user, Long sessionId, Collection<Long> studentIds) {
		addItem(hibSession, user, sessionId, Type.STUDENT_ENROLLMENT_CHANGE, studentIds);
	}
	
	public static void classAssignmentChanged(org.hibernate.Session hibSession, UserContext user, Long sessionId, Collection<Long> classIds) {
		addItem(hibSession, user, sessionId, Type.CLASS_ASSIGNMENT_CHANGE, classIds);
	}
	
	public static void offeringChanged(org.hibernate.Session hibSession, UserContext user, Long sessionId, Collection<Long> offeringId) {
		addItem(hibSession, user, sessionId, Type.OFFERING_CHANGE, offeringId);
	}

	public static void offeringChanged(org.hibernate.Session hibSession, UserContext user, Long sessionId, Long... offeringId) {
		addItem(hibSession, user, sessionId, Type.OFFERING_CHANGE, offeringId);
	}
}
