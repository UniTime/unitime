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

import java.util.HashSet;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseStudentSectioningPref;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;

public abstract class StudentSectioningPref extends BaseStudentSectioningPref {
	private static final long serialVersionUID = 1L;

	public StudentSectioningPref() {
		super();
	}
	
	public static void updateStudentSectioningPreferences() {
		org.hibernate.Session hibSession = new _RootDAO().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			boolean first = true;
			for (CourseRequestOption option: (List<CourseRequestOption>)hibSession.createQuery("from CourseRequestOption where optionType = :type"
					).setInteger("type", OnlineSectioningLog.CourseRequestOption.OptionType.REQUEST_PREFERENCE.getNumber()).list()) {
				if (first) {
					Debug.info(" - Updating student scheduling preferences ...");
					first = false;
				}
				CourseRequest cr = option.getCourseRequest();
				hibSession.delete(option);
				cr.getCourseRequestOptions().remove(option);
				if (cr.getPreferences() == null) cr.setPreferences(new HashSet<StudentSectioningPref>());
				try {
					OnlineSectioningLog.CourseRequestOption pref = option.getOption();
					if (pref != null) {
    					if (pref.getInstructionalMethodCount() > 0) {
    						for (OnlineSectioningLog.Entity e: pref.getInstructionalMethodList()) {
    							boolean required = false;
    							if (e.getParameterCount() > 0)
    								for (OnlineSectioningLog.Property p: e.getParameterList())
    									if ("required".equals(p.getKey()))
    										required = "true".equals(p.getValue());
    							InstructionalMethod im = InstructionalMethodDAO.getInstance().get(e.getUniqueId());
    							if (im == null)
    								im = InstructionalMethod.findByReference(e.getName(), hibSession);
    							if (im != null) {
    								StudentInstrMthPref imp = new StudentInstrMthPref();
    								imp.setCourseRequest(cr);
    								imp.setRequired(required);
    								imp.setInstructionalMethod(im);
    								imp.setLabel(im.getReference());
    								cr.getPreferences().add(imp);
    							}
    						}
    					}
    					if (pref.getSectionCount() > 0) {
    						for (OnlineSectioningLog.Section x: pref.getSectionList()) {
    							boolean required = (x.hasPreference() && x.getPreference() == OnlineSectioningLog.Section.Preference.REQUIRED);
    							Class_ clazz = Class_DAO.getInstance().get(x.getClazz().getUniqueId(), hibSession);
    							if (clazz != null) {
    								StudentClassPref scp = new StudentClassPref();
    								scp.setCourseRequest(cr);
    								scp.setRequired(required);
    								scp.setClazz(clazz);
    								scp.setLabel(clazz.getClassPrefLabel(cr.getCourseOffering()));
    								cr.getPreferences().add(scp);
    							}
    						}
    					}
                    }
				} catch (Exception e) {}
				hibSession.update(cr);
			}
			for (StudentInstrMthPref p: (List<StudentInstrMthPref>)hibSession.createQuery("from StudentInstrMthPref where label is null and instructionalMethod.label is not null").list()) {
				p.setLabel(p.getInstructionalMethod().getLabel());
				hibSession.update(p);
			}
			for (StudentClassPref p: (List<StudentClassPref>)hibSession.createQuery("from StudentClassPref where label is null").list()) {
				p.setLabel(p.getClazz().getClassPrefLabel(p.getCourseRequest().getCourseOffering()));
				hibSession.update(p);
			}
			tx.commit();
		} catch (Exception e) {
			Debug.error("Failed to update student sectioning preferences: " + e.getMessage(), e);
			tx.rollback();
		} finally {
			hibSession.getSessionFactory().getCache().evictCollectionRegion(CourseRequest.class.getName() + ".courseRequestOptions");
			hibSession.close();
		}
	}
}
