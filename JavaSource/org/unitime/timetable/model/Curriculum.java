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

import org.unitime.timetable.model.base.BaseCurriculum;
import org.unitime.timetable.model.dao.CurriculumDAO;

/**
 * @author Tomas Muller
 */
public class Curriculum extends BaseCurriculum implements Comparable<Curriculum> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Curriculum () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Curriculum (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static List<Curriculum> findAll(Long sessionId) {
	    return CurriculumDAO.getInstance().getSession()
	        .createQuery("select c from Curriculum c where c.department.session.uniqueId=:sessionId")
	        .setLong("sessionId", sessionId)
	        .setCacheable(true).list();
	}

    public static List<Curriculum> findByDepartment(Long deptId) {
        return CurriculumDAO.getInstance().getSession()
            .createQuery("select c from Curriculum c where c.department.uniqueId=:deptId")
            .setLong("deptId", deptId)
            .setCacheable(true).list();
    }
    
    public int compareTo(Curriculum c) {
    	int cmp = getAbbv().compareToIgnoreCase(c.getAbbv());
    	if (cmp != 0) return cmp;
    	return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(c.getUniqueId() == null ? -1 : c.getUniqueId());
    }
    
    public boolean isTemplateFor(Curriculum curriculum) {
    	if (!curriculum.isMultipleMajors()) return false;
    	if (!curriculum.getAcademicArea().equals(getAcademicArea())) return false;
    	if (curriculum.getMajors().size() <= getMajors().size() || getMajors().size() > 1) return false;
    	return getMajors().isEmpty() || curriculum.getMajors().containsAll(getMajors());
    }
}