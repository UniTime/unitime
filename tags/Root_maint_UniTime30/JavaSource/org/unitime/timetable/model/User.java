/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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

	/**
	 * Constructor for required fields
	 */
	public User (
		java.lang.String username,
		java.lang.String password) {

		super (
			username,
			password);
	}

/*[CONSTRUCTOR MARKER END]*/
    
    public static User findByExternalId(String externalId) {
        return (User)
            new UserDAO().
            getSession().
            createQuery("select u from User u where u.externalUniqueId=:externalId").
            setString("externalId", externalId).
            uniqueResult();
    }

    public static User findByUserName(String userName) {
        return (User)
            new UserDAO().
            getSession().
            createQuery("select u from User u where u.username=:userName").
            setString("userName", userName).
            uniqueResult();
    }
    
}