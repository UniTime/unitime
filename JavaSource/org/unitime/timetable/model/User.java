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

import org.unitime.timetable.model.base.BaseUser;
import org.unitime.timetable.model.dao.UserDAO;

public class User extends BaseUser {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public User () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public User (java.lang.String username) {
		super(username);
	}

/*[CONSTRUCTOR MARKER END]*/
    
    public static User findByExternalId(String externalId) {
        return (User)
            new UserDAO().
            getSession().
            createQuery("select u from User u where u.externalUniqueId=:externalId").
            setString("externalId", externalId).
            setCacheable(true).
            setMaxResults(1).
            uniqueResult();
    }

    public static User findByUserName(String userName) {
        return (User)
            new UserDAO().
            getSession().
            createQuery("select u from User u where u.username=:userName").
            setString("userName", userName).
            setCacheable(true).
            setMaxResults(1).
            uniqueResult();
    }
    
}
