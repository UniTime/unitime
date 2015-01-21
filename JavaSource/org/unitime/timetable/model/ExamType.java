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

import java.util.List;
import java.util.TreeSet;

import org.hibernate.SessionFactory;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.model.base.BaseExamType;
import org.unitime.timetable.model.dao.ExamTypeDAO;

/**
 * @author Tomas Muller
 */
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
		return (ExamType)ExamTypeDAO.getInstance().getSession().createQuery(
				"from ExamType where reference = :ref")
				.setString("ref", ref).setCacheable(true).setMaxResults(1).uniqueResult();
	}
	
	public static List<ExamType> findAllOfType(int type) {
		return (List<ExamType>)ExamTypeDAO.getInstance().getSession().createQuery(
				"from ExamType where type = :type order by type, label")
				.setInteger("type", type).setCacheable(true).list();
	}

	public static TreeSet<ExamType> findAllUsed(Long sessionId) {
		return new TreeSet<ExamType>(ExamTypeDAO.getInstance().getSession().createQuery(
				"select distinct p.examType from ExamPeriod p where p.session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).setCacheable(true).list());
	}

	public static List<ExamType> findAll() {
		return (List<ExamType>)ExamTypeDAO.getInstance().getSession().createQuery(
				"from ExamType order by type, label")
				.setCacheable(true).list();
	}
	
	public boolean isUsed(Long sessionId) {
		if (sessionId == null) {
			return ((Number)ExamTypeDAO.getInstance().getSession().createQuery(
					"select count(p) from ExamPeriod p where p.examType.uniqueId = :typeId")
					.setLong("typeId", getUniqueId()).setCacheable(true).uniqueResult()).longValue() > 0;
		} else {
			return ((Number)ExamTypeDAO.getInstance().getSession().createQuery(
					"select count(p) from ExamPeriod p where p.examType.uniqueId = :typeId and p.session.uniqueId = :sessionId")
					.setLong("typeId", getUniqueId()).setLong("sessionId", sessionId).setCacheable(true).uniqueResult()).longValue() > 0;
		}
	}
	
	public static void refreshSolution(Long sessionId, Long examTypeId) {
        org.hibernate.Session hibSession = ExamTypeDAO.getInstance().getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory(); 
        for (Long examId: (List<Long>)hibSession.createQuery(
        		"select x.uniqueId from Exam x where x.session.uniqueId = :sessionId and x.examType.uniqueId = :examTypeId")
        		.setLong("sessionId", sessionId)
        		.setLong("examTypeId", examTypeId)
        		.setCacheable(true).list()) {
            hibSessionFactory.getCache().evictEntity(Exam.class, examId);
            hibSessionFactory.getCache().evictCollection(Exam.class.getName()+".assignedRooms", examId);
            hibSessionFactory.getCache().evictCollection(Exam.class.getName()+".conflicts", examId);
        }
        for (Long eventId: (List<Long>)hibSession.createQuery(
        		"select e.uniqueId from ExamEvent e inner join e.exam x where x.session.uniqueId = :sessionId and x.examType.uniqueId = :examTypeId")
        		.setLong("sessionId", sessionId)
        		.setLong("examTypeId", examTypeId)
        		.setCacheable(true).list()) {
            hibSessionFactory.getCache().evictEntity(Event.class, eventId);
            hibSessionFactory.getCache().evictCollection(Event.class.getName()+".meetings", eventId);
        }
        hibSessionFactory.getCache().evictDefaultQueryRegion();
   }
}
