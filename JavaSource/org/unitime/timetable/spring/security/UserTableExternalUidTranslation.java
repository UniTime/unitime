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

import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.model.User;
import org.unitime.timetable.model.dao.UserDAO;

/**
 * @author Tomas Muller
 */
public class UserTableExternalUidTranslation implements ExternalUidTranslation {

	public String translate(String uid, Source source, Source target) {
        if (uid==null || source.equals(target)) return uid;
        if (source.equals(Source.LDAP)) return uid2ext(uid);
        if (target.equals(Source.LDAP)) return ext2uid(uid);
        return uid;
    }
    
    public String uid2ext(String username) {
		org.hibernate.Session hibSession = UserDAO.getInstance().createNewSession();
		try {
			User user = (User) hibSession.createQuery("from User where username=:username").setString("username", username).setMaxResults(1).uniqueResult();
			return (user == null ? username : user.getExternalUniqueId());
		} finally {
			hibSession.close();
		}
    }
    
    public String ext2uid(String externalUniqueId) {
		org.hibernate.Session hibSession = UserDAO.getInstance().createNewSession();
		try {
			User user = (User) hibSession.createQuery("from User where externalUniqueId=:externalUniqueId").setString("externalUniqueId", externalUniqueId).setMaxResults(1).uniqueResult();
			return (user == null ? externalUniqueId : user.getUsername());
		} finally {
			hibSession.close();
		}
    }
}
