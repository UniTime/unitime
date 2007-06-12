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
package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseApplicationConfigDAO extends org.unitime.timetable.model.dao._RootDAO {

	// query name references


	public static ApplicationConfigDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static ApplicationConfigDAO getInstance () {
		if (null == instance) instance = new ApplicationConfigDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.timetable.model.ApplicationConfig.class;
	}

    public Order getDefaultOrder () {
		return null;
    }

	/**
	 * Cast the object as a org.unitime.timetable.model.ApplicationConfig
	 */
	public org.unitime.timetable.model.ApplicationConfig cast (Object object) {
		return (org.unitime.timetable.model.ApplicationConfig) object;
	}

	public org.unitime.timetable.model.ApplicationConfig get(java.lang.String key)
	{
		return (org.unitime.timetable.model.ApplicationConfig) get(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.ApplicationConfig get(java.lang.String key, Session s)
	{
		return (org.unitime.timetable.model.ApplicationConfig) get(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.ApplicationConfig load(java.lang.String key)
	{
		return (org.unitime.timetable.model.ApplicationConfig) load(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.ApplicationConfig load(java.lang.String key, Session s)
	{
		return (org.unitime.timetable.model.ApplicationConfig) load(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.ApplicationConfig loadInitialize(java.lang.String key, Session s) 
	{ 
		org.unitime.timetable.model.ApplicationConfig obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param applicationConfig a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.String save(org.unitime.timetable.model.ApplicationConfig applicationConfig)
	{
		return (java.lang.String) super.save(applicationConfig);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param applicationConfig a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public java.lang.String save(org.unitime.timetable.model.ApplicationConfig applicationConfig, Session s)
	{
		return (java.lang.String) save((Object) applicationConfig, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param applicationConfig a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.timetable.model.ApplicationConfig applicationConfig)
	{
		saveOrUpdate((Object) applicationConfig);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param applicationConfig a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.timetable.model.ApplicationConfig applicationConfig, Session s)
	{
		saveOrUpdate((Object) applicationConfig, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param applicationConfig a transient instance containing updated state
	 */
	public void update(org.unitime.timetable.model.ApplicationConfig applicationConfig) 
	{
		update((Object) applicationConfig);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param applicationConfig a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.timetable.model.ApplicationConfig applicationConfig, Session s)
	{
		update((Object) applicationConfig, s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param id the instance ID to be removed
	 */
	public void delete(java.lang.Object id)
	{
        if (id instanceof String)
            delete((Object) load((String)id));
        else
            super.delete(id);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param id the instance ID to be removed
	 * @param s the Session
	 */
	public void delete(java.lang.Object id, Session s)
	{
        if (id instanceof String)
            delete((Object) load((String)id, s), s);
        else
            super.delete(id, s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param applicationConfig the instance to be removed
	 */
	public void delete(org.unitime.timetable.model.ApplicationConfig applicationConfig)
	{
		delete((Object) applicationConfig);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param applicationConfig the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.timetable.model.ApplicationConfig applicationConfig, Session s)
	{
		delete((Object) applicationConfig, s);
	}
	
	/**
	 * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
	 * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
	 * For example 
	 * <ul> 
	 * <li>where a database trigger alters the object state upon insert or update</li>
	 * <li>after executing direct SQL (eg. a mass update) in the same session</li>
	 * <li>after inserting a Blob or Clob</li>
	 * </ul>
	 */
	public void refresh (org.unitime.timetable.model.ApplicationConfig applicationConfig, Session s)
	{
		refresh((Object) applicationConfig, s);
	}


}