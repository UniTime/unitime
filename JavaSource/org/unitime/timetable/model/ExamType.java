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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.hibernate.SessionFactory;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.model.base.BaseExamType;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.SimpleExaminationPermission;

/**
 * @author Tomas Muller
 */
@Entity
@Table(name = "exam_type")
public class ExamType extends BaseExamType implements Comparable<ExamType> {
	private static final long serialVersionUID = 1L;
	protected static ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	public static final int sExamTypeFinal = 0;
	public static final int sExamTypeMidterm = 1;
	
	public ExamType() {
		super();
	}

	@Override
	public int compareTo(ExamType o) {
		return (getType().equals(o.getType()) ? getLabel().compareToIgnoreCase(o.getLabel()) : getType().compareTo(o.getType()));
	}
	
	public static ExamType findByReference(String ref) {
		return ExamTypeDAO.getInstance().getSession().createQuery(
				"from ExamType where reference = :ref", ExamType.class)
				.setParameter("ref", ref, String.class).setCacheable(true).setMaxResults(1).uniqueResult();
	}
	
	public static List<ExamType> findAllOfType(int type) {
		return ExamTypeDAO.getInstance().getSession().createQuery(
				"from ExamType where type = :type order by type, label", ExamType.class)
				.setParameter("type", type, Integer.class).setCacheable(true).list();
	}

	public static TreeSet<ExamType> findAllUsed(Long sessionId) {
		return new TreeSet<ExamType>(ExamTypeDAO.getInstance().getSession().createQuery(
				"select distinct p.examType from ExamPeriod p where p.session.uniqueId = :sessionId", ExamType.class)
				.setParameter("sessionId", sessionId, Long.class).setCacheable(true).list());
	}

	public static List<ExamType> findAllApplicable(UserContext user, DepartmentStatusType.Status... status) {
		List<ExamType> types = new ArrayList<ExamType>();
		UserAuthority authority = (user == null ? null : user.getCurrentAuthority());
		if (authority == null) return types;
		SimpleExaminationPermission p = new SimpleExaminationPermission();
		
		for (ExamType type: findAll()) {
			ExamStatus examStatus = ExamStatus.findStatus(user.getCurrentAcademicSessionId(), type.getUniqueId());
			if (examStatus != null) {
				if (p.checkManager(authority, examStatus, status) && p.checkStatus(authority, examStatus.effectiveStatus(), status))
					types.add(type);
			} else {
				types.add(type);
			}
		}
		
		return types;
	}
	
	public static List<ExamType> findAllUsedApplicable(UserContext user, DepartmentStatusType.Status... status) {
		List<ExamType> types = new ArrayList<ExamType>();
		UserAuthority authority = (user == null ? null : user.getCurrentAuthority());
		if (authority == null) return types;
		SimpleExaminationPermission p = new SimpleExaminationPermission();
		
		for (ExamType type: findAllUsed(user.getCurrentAcademicSessionId())) {
			ExamStatus examStatus = ExamStatus.findStatus(user.getCurrentAcademicSessionId(), type.getUniqueId());
			if (examStatus != null) {
				if (p.checkManager(authority, examStatus, status) && p.checkStatus(authority, examStatus.effectiveStatus(), status))
					types.add(type);
			} else {
				types.add(type);
			}
		}
		
		return types;
	}
	
	public static List<ExamType> findAll() {
		return findAll(null);
	}

	public static List<ExamType> findAll(org.hibernate.Session hibSession) {
		return (hibSession != null ? hibSession : ExamTypeDAO.getInstance().getSession()).createQuery(
				"from ExamType order by type, label", ExamType.class)
				.setCacheable(true).list();
	}
	
	public boolean isUsed(Long sessionId) {
		if (sessionId == null) {
			return (ExamTypeDAO.getInstance().getSession().createQuery(
					"select count(p) from ExamPeriod p where p.examType.uniqueId = :typeId", Number.class)
					.setParameter("typeId", getUniqueId(), Long.class).setCacheable(true).uniqueResult()).longValue() > 0;
		} else {
			return (ExamTypeDAO.getInstance().getSession().createQuery(
					"select count(p) from ExamPeriod p where p.examType.uniqueId = :typeId and p.session.uniqueId = :sessionId", Number.class)
					.setParameter("typeId", getUniqueId(), Long.class).setParameter("sessionId", sessionId, Long.class).setCacheable(true).uniqueResult()).longValue() > 0;
		}
	}
	
	public static void refreshSolution(Long sessionId, Long examTypeId) {
        org.hibernate.Session hibSession = ExamTypeDAO.getInstance().getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory(); 
        for (Long examId: hibSession.createQuery(
        		"select x.uniqueId from Exam x where x.session.uniqueId = :sessionId and x.examType.uniqueId = :examTypeId", Long.class)
        		.setParameter("sessionId", sessionId, Long.class)
        		.setParameter("examTypeId", examTypeId, Long.class)
        		.setCacheable(true).list()) {
            hibSessionFactory.getCache().evictEntityData(Exam.class, examId);
            hibSessionFactory.getCache().evictCollectionData(Exam.class.getName()+".assignedRooms", examId);
            hibSessionFactory.getCache().evictCollectionData(Exam.class.getName()+".conflicts", examId);
        }
        for (Long eventId: hibSession.createQuery(
        		"select e.uniqueId from ExamEvent e inner join e.exam x where x.session.uniqueId = :sessionId and x.examType.uniqueId = :examTypeId", Long.class)
        		.setParameter("sessionId", sessionId, Long.class)
        		.setParameter("examTypeId", examTypeId, Long.class)
        		.setCacheable(true).list()) {
            hibSessionFactory.getCache().evictEntityData(Event.class, eventId);
            hibSessionFactory.getCache().evictCollectionData(Event.class.getName()+".meetings", eventId);
        }
        hibSessionFactory.getCache().evictDefaultQueryRegion();
   }
}
