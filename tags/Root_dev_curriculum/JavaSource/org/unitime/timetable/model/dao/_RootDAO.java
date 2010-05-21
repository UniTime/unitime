/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.dao;

import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;


public class _RootDAO extends org.unitime.timetable.model.base._BaseRootDAO {

/*
	If you are using lazy loading, uncomment this
	Somewhere, you should call RootDAO.closeCurrentThreadSessions();
*/
	public void closeSession (Session session) {
		// do nothing here because the session will be closed later
	}

/*	
	If you are pulling the SessionFactory from a JNDI tree, uncomment this
	protected SessionFactory getSessionFactory(String configFile) {
		// If you have a single session factory, ignore the configFile parameter
		// Otherwise, you can set a meta attribute under the class node called "config-file" which
		// will be passed in here so you can tell what session factory an individual mapping file
		// belongs to
		return (SessionFactory) new InitialContext().lookup("java:/{SessionFactoryName}");
	}
*/

	public Class getReferenceClass () {
		return null;
	}
	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 * The results are ordered by the order specified in orderList
	 * Use the session given.
	 * @param orderList Collection of Order objects 
	 */
	public java.util.List findAll (java.util.Collection orderList) {
		Session s = null;
		try {
			s = getSession();
			Criteria crit = s.createCriteria(getReferenceClass());
			if (null != orderList) {
			    Iterator iter = orderList.iterator();
			    while (iter.hasNext())
			        crit.addOrder( (Order) iter.next() );			    
			}
			return crit.list();
		}
		finally {
			closeSession(s);
		}
	}


}
