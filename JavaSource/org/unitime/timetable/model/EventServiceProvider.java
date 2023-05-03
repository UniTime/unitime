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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.base.BaseEventServiceProvider;
import org.unitime.timetable.model.dao.EventServiceProviderDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.rights.Right;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "service_provider")
public class EventServiceProvider extends BaseEventServiceProvider implements Comparable<EventServiceProvider> {
	private static final long serialVersionUID = 1L;
	

	public EventServiceProvider() {
		super();
	}
	
	public static EventServiceProvider getEventServiceProvider(String reference, org.hibernate.Session hibSession) {
		if (reference == null || reference.isEmpty()) return null;
		return hibSession.createQuery(
				"from EventServiceProvider where reference = :reference", EventServiceProvider.class)
				.setParameter("reference", reference).setMaxResults(1).setCacheable(true).uniqueResult();
	}

	@Transient
	public boolean isUsed() {
		if ((EventServiceProviderDAO.getInstance().getSession().createQuery("select count(e) from Event e inner join e.requestedServices p where p.uniqueId = :providerId", Number.class)
			.setParameter("providerId", getUniqueId()).uniqueResult()).intValue() > 0) return true;
		return false;
	}

	@Override
	public int compareTo(EventServiceProvider p) {
		int cmp = getLabel().compareToIgnoreCase(p.getLabel());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(p.getUniqueId() == null ? -1 : p.getUniqueId());
	}
	
	public static TreeSet<EventServiceProvider> getServiceProviders(UserContext user) {
		TreeSet<EventServiceProvider> providers = new TreeSet<EventServiceProvider>();
		providers.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
				"from EventServiceProvider where visible = true and session is null", EventServiceProvider.class
				).setCacheable(true).list());
		if (user == null || user.getCurrentAuthority() == null) return providers;
		if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
			providers.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
					"from EventServiceProvider where visible = true and session.uniqueId = :sessionId", EventServiceProvider.class
					).setParameter("sessionId", user.getCurrentAcademicSessionId()).setCacheable(true).list());
		} else {
			providers.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
					"from EventServiceProvider where visible = true and session.uniqueId = :sessionId and department is null", EventServiceProvider.class
					).setParameter("sessionId", user.getCurrentAcademicSessionId()).setCacheable(true).list());
			for (UserQualifier q: user.getCurrentAuthority().getQualifiers("Department"))
				providers.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
						"from EventServiceProvider where visible = true and department.uniqueId = :departmentId", EventServiceProvider.class
						).setParameter("departmentId", (Long)q.getQualifierId()).setCacheable(true).list());
		}
		return providers;
	}
	
	public EventServiceProvider findInSession(org.hibernate.Session hibSession, Long sessionId) {
		if (hibSession == null)
			hibSession = EventServiceProviderDAO.getInstance().getSession();
		if (getSession() == null) {
			return this;
		} else if (getDepartment() == null) {
			return hibSession.createQuery(
					"from EventServiceProvider where session.uniqueId = :sessionId and department is null and reference = :reference", EventServiceProvider.class)
					.setParameter("sessionId", sessionId).setParameter("reference", getReference()).setMaxResults(1).setCacheable(true).uniqueResult();
		} else {
			return hibSession.createQuery(
					"from EventServiceProvider where session.uniqueId = :sessionId and department.deptCode = :deptCode and reference = :reference", EventServiceProvider.class)
					.setParameter("sessionId", sessionId).setParameter("deptCode", getDepartment().getDeptCode()).setParameter("reference", getReference()).setMaxResults(1).setCacheable(true).uniqueResult();
		}
	}
	
	public EventServiceProvider findInSession(Long sessionId) {
		return findInSession(null, sessionId);
	}
	
	public static List<EventServiceProvider> findAll(Long sessionId) {
		return EventServiceProviderDAO.getInstance().getSession().createQuery(
				"from EventServiceProvider where visible = true and (session is null or session.uniqueId = :sessionId)", EventServiceProvider.class)
				.setParameter("sessionId", sessionId).setCacheable(true).list();
	}
	
	public Set<Long> getLocationIds(Long sessionId) {
		if (getSession() == null) {
			if (isAllRooms()) return null;
			HashSet<Long> ids = new HashSet<Long>(EventServiceProviderDAO.getInstance().getSession().createQuery(
					"select l.uniqueId from Room l inner join l.allowedServices s where s.uniqueId = :serviceId and l.session.uniqueId = :sessionId", Long.class)
					.setParameter("sessionId", sessionId).setParameter("serviceId", getUniqueId()).setCacheable(true).list());
			ids.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
					"select l.uniqueId from NonUniversityLocation l inner join l.allowedServices s where s.uniqueId = :serviceId and l.session.uniqueId = :sessionId", Long.class)
					.setParameter("sessionId", sessionId).setParameter("serviceId", getUniqueId()).setCacheable(true).list());
			return ids;
		} else if (getDepartment() == null) {
			if (isAllRooms()) return null;
			HashSet<Long> ids = new HashSet<Long>(EventServiceProviderDAO.getInstance().getSession().createQuery(
					"select l.uniqueId from Room l inner join l.allowedServices s where s.uniqueId = :serviceId and l.session.uniqueId = :sessionId", Long.class)
					.setParameter("sessionId", getSession().getUniqueId()).setParameter("serviceId", getUniqueId()).setCacheable(true).list());
			ids.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
					"select l.uniqueId from NonUniversityLocation l inner join l.allowedServices s where s.uniqueId = :serviceId and l.session.uniqueId = :sessionId", Long.class)
					.setParameter("sessionId", getSession().getUniqueId()).setParameter("serviceId", getUniqueId()).setCacheable(true).list());
			return ids;
		} else {
			if (isAllRooms()) {
				return new HashSet<Long>(
						EventServiceProviderDAO.getInstance().getSession().createQuery(
						"select l.uniqueId from Location l where l.eventdepartment.uniqueId = :departmentId", Long.class)
						.setParameter("departmentId", getDepartment().getUniqueId()).setCacheable(true).list());
			} else {
				HashSet<Long> ids = new HashSet<Long>(EventServiceProviderDAO.getInstance().getSession().createQuery(
						"select l.uniqueId from Room l inner join l.allowedServices s where s.uniqueId = :serviceId and l.eventDepartment = s.department", Long.class)
						.setParameter("serviceId", getUniqueId()).setCacheable(true).list());
				ids.addAll(EventServiceProviderDAO.getInstance().getSession().createQuery(
						"select l.uniqueId from NonUniversityLocation l inner join l.allowedServices s where s.uniqueId = :serviceId and l.eventDepartment = s.department", Long.class)
						.setParameter("serviceId", getUniqueId()).setCacheable(true).list());
				return ids;	
			}
		}
	}
	
}
