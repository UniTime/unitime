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


import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.List;

import org.unitime.timetable.model.base.BaseLearningManagementSystemInfo;
import org.unitime.timetable.model.dao.LearningManagementSystemInfoDAO;
import org.unitime.timetable.security.UserContext;

@Entity
@Table(name = "learn_mgmt_sys_info")
public class LearningManagementSystemInfo extends BaseLearningManagementSystemInfo {

    /**
	 * 
	 */
	private static final long serialVersionUID = 45964274048126169L;
	public static String LEARNING_MANAGEMENT_SYSTEM_LIST_ATTR = "lmsList";

	public LearningManagementSystemInfo() {
		super();
	}

    public boolean isUsed(org.hibernate.Session hibSession) {
    	    if (this.isDefaultLms()) {
    	    		return(true);
    	    }
    	    return ((hibSession == null ? LearningManagementSystemInfoDAO.getInstance().getSession() : hibSession).createQuery(
    			"select count(c) from Class_ c where c.lmsInfo.uniqueId = :lmsId", Number.class)
    			.setParameter("lmsId", getUniqueId(), org.hibernate.type.LongType.INSTANCE).setCacheable(true).uniqueResult()).intValue() > 0;
    }

	public static List<LearningManagementSystemInfo> findAll(UserContext user) {
	   	return(findAll(user.getCurrentAcademicSessionId()));
	}
	
	public static List<LearningManagementSystemInfo> findAll(Long sessionId) {
	   	@SuppressWarnings("unchecked")
		List<LearningManagementSystemInfo> list = LearningManagementSystemInfoDAO.getInstance().getSession().createQuery(
    			"select distinct lms from LearningManagementSystemInfo as lms where lms.session.uniqueId=:sessionId", LearningManagementSystemInfo.class)
    			.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE)
    			.setCacheable(true).list();
	   	return(list);
	}

	public static LearningManagementSystemInfo findBySessionIdAndReference(Long sessionId, String reference) {
	   	@SuppressWarnings("unchecked")
		LearningManagementSystemInfo lms = LearningManagementSystemInfoDAO.getInstance().getSession().createQuery(
    			"select distinct lms from LearningManagementSystemInfo as lms where lms.session.uniqueId=:sessionId and lms.reference = :ref", LearningManagementSystemInfo.class)
    			.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE).setParameter("ref", reference, org.hibernate.type.StringType.INSTANCE)
    			.setCacheable(true).uniqueResult();

	   	return(lms);
	}

	public static LearningManagementSystemInfo getDefaultIfExists(Long sessionId) {
		return(LearningManagementSystemInfoDAO.getInstance().getSession().createQuery(
    			"select distinct lms from LearningManagementSystemInfo as lms where lms.session.uniqueId=:sessionId and lms.defaultLms = true", LearningManagementSystemInfo.class)
    			.setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE)
    			.setCacheable(true).uniqueResult());
	}
	
    public static boolean isLmsInfoDefinedForSession(Long sessionId) {
    	LearningManagementSystemInfoDAO lmsdao = LearningManagementSystemInfoDAO.getInstance();
      return(lmsdao.findBySession(lmsdao.getSession(), sessionId).size() > 0);
    }

    public static boolean isLmsInfoDefinedForSession(Session hibSession, Long sessionId) {
      return(isLmsInfoDefinedForSession(hibSession, sessionId));
    }
    
	public Object clone(){
		LearningManagementSystemInfo newLms = new LearningManagementSystemInfo();
		newLms.setReference(getReference());
		newLms.setLabel(getLabel());
		newLms.setExternalUniqueId(getExternalUniqueId());
		newLms.setDefaultLms(getDefaultLms());
		newLms.setSession(getSession());
		return(newLms);
	}


}
