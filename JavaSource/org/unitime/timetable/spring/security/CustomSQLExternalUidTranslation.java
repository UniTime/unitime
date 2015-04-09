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
package org.unitime.timetable.spring.security;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.model.dao.UserDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class CustomSQLExternalUidTranslation implements ExternalUidTranslation {

	public String translate(String uid, Source source, Source target) {
        if (uid==null || source.equals(target)) return uid;
        if (source.equals(Source.LDAP)) return uid2ext(uid);
        if (target.equals(Source.LDAP)) return ext2uid(uid);
        return uid;
    }
    
    public String uid2ext(String username) {
		org.hibernate.Session hibSession = UserDAO.getInstance().createNewSession();
		try {
			String sql = ApplicationProperty.CustomSQLUidToExternalTranslation.value();
			if (sql.indexOf("%SCHEMA%") >= 0)
				sql = sql.replace("%SCHEMA%", _RootDAO.getConfiguration().getProperty("default_schema"));
			Object ret = hibSession.createSQLQuery(sql).setParameter(0, username).setMaxResults(1).uniqueResult();
			return (ret == null ? username : ret.toString());
		} finally {
			hibSession.close();
		}
    }
    
    public String ext2uid(String externalUniqueId) {
		org.hibernate.Session hibSession = UserDAO.getInstance().createNewSession();
		try {
			String sql = ApplicationProperty.CustomSQLExternalToUidTranslation.value();
			if (sql.indexOf("%SCHEMA%") >= 0)
				sql = sql.replace("%SCHEMA%", _RootDAO.getConfiguration().getProperty("default_schema"));
			Object ret = hibSession.createSQLQuery(sql).setParameter(0, externalUniqueId).setMaxResults(1).uniqueResult();
			return (ret == null ? externalUniqueId : ret.toString());
		} finally {
			hibSession.close();
		}
    }
}
