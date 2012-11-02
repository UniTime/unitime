/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.model.base.BaseExamType;
import org.unitime.timetable.model.dao.ExamTypeDAO;

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

	public static List<ExamType> findAllUsed(Long sessionId) {
		return (List<ExamType>)ExamTypeDAO.getInstance().getSession().createQuery(
				"select distinct p.examType from ExamPeriod p where p.session.uniqueId = :sessionId order by p.examType.type, p.examType.label")
				.setLong("sessionId", sessionId).setCacheable(true).list();
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
					.setLong("typeId", getUniqueId()).uniqueResult()).longValue() > 0;
		} else {
			return ((Number)ExamTypeDAO.getInstance().getSession().createQuery(
					"select count(p) from ExamPeriod p where p.examType.uniqueId = :typeId and p.session.uniqueId = :sessionId")
					.setLong("typeId", getUniqueId()).setLong("sessionId", sessionId).uniqueResult()).longValue() > 0;
		}
	}
}
