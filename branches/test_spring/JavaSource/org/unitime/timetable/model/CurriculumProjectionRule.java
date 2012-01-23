/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Hashtable;
import java.util.List;

import org.unitime.timetable.model.base.BaseCurriculumProjectionRule;
import org.unitime.timetable.model.dao.CurriculumProjectionRuleDAO;



public class CurriculumProjectionRule extends BaseCurriculumProjectionRule {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CurriculumProjectionRule () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CurriculumProjectionRule (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static List<CurriculumProjectionRule> findAll(Long sessionId) {
	    return CurriculumProjectionRuleDAO.getInstance().getSession()
	        .createQuery("select r from CurriculumProjectionRule r where r.academicArea.session.uniqueId=:sessionId")
	        .setLong("sessionId", sessionId)
	        .setCacheable(true).list();
	}

    public static List<CurriculumProjectionRule> findByAcademicArea(Long acadAreaId) {
        return CurriculumProjectionRuleDAO.getInstance().getSession()
            .createQuery("select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId")
            .setLong("acadAreaId", acadAreaId)
            .setCacheable(true).list();
    }
    
    public static Hashtable<String, Float> getProjections(Long acadAreaId, Long acadClasfId) {
    	Hashtable<String, Float> ret = new Hashtable<String, Float>();
    	for (CurriculumProjectionRule r: (List<CurriculumProjectionRule>)CurriculumProjectionRuleDAO.getInstance().getSession()
    			.createQuery("select r from CurriculumProjectionRule r where r.academicArea.uniqueId=:acadAreaId and r.academicClassification.uniqueId=:acadClasfId")
    			.setLong("acadAreaId", acadAreaId)
    			.setLong("acadClasfId", acadClasfId)
    			.setCacheable(true).list()) {
    		ret.put(r.getMajor() == null ? "" : r.getMajor().getCode(), r.getProjection());
    	}
    	return ret;
    }

}