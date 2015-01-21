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

import java.util.Hashtable;
import java.util.List;

import org.unitime.timetable.model.base.BaseCurriculumProjectionRule;
import org.unitime.timetable.model.dao.CurriculumProjectionRuleDAO;



/**
 * @author Tomas Muller
 */
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