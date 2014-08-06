/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
