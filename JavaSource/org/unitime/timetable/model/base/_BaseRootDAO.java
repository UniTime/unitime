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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;

import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.commons.hibernate.util.HibernateUtil;



public abstract class _BaseRootDAO {

	protected static Map sessionFactoryMap;
	protected static SessionFactory sessionFactory;
	protected static ThreadLocal mappedSessions;
	protected static ThreadLocal sessions;
	protected static Configuration configuration;
	

	/**
	 * Configure the session factory by reading hibernate config file
	 */
	public static void initialize () {
		org.unitime.timetable.model.dao._RootDAO.initialize(
			(String) null);
	}
	
	/**
	 * Configure the session factory by reading hibernate config file
	 * @param configFileName the name of the configuration file
	 */
	public static void initialize (String configFileName) {
		org.unitime.timetable.model.dao._RootDAO.initialize(
			configFileName,
			org.unitime.timetable.model.dao._RootDAO.getNewConfiguration(
				null));
	}

	public static void initialize (String configFileName, Configuration configuration) {
		if (configFileName==null && sessionFactory!=null) return;
        if (sessionFactoryMap!=null && sessionFactoryMap.get(configFileName)!=null) return;
        HibernateUtil.configureHibernateFromRootDAO(configFileName, configuration);
        org.unitime.timetable.model.dao._RootDAO.setSessionFactory(configuration.buildSessionFactory());
        org.unitime.timetable.model.dao._RootDAO.configuration = configuration;
        DatabaseUpdate.update();
	}

	/**
	 * Set the session factory
	 */
	protected static void setSessionFactory (SessionFactory sessionFactory) {
		setSessionFactory(
			(String) null,
			sessionFactory);
	}

	/**
	 * Set the session factory
	 */
	protected static void setSessionFactory (String configFileName, SessionFactory sf) {
		if (null == configFileName) {
			sessionFactory = sf;
		}
		else {
			if (null == sessionFactoryMap) sessionFactoryMap = new HashMap();
			sessionFactoryMap.put(
				configFileName,
				sessionFactory);
		}
	}

	/**
	 * Return the SessionFactory that is to be used by these DAOs.  Change this
	 * and implement your own strategy if you, for example, want to pull the SessionFactory
	 * from the JNDI tree.
	 */
	protected SessionFactory getSessionFactory() {
		return getSessionFactory(
		getConfigurationFileName());
	}

	protected SessionFactory getSessionFactory(String configFile) {
		if (null == configFile) {
			if (null == sessionFactory)
				throw new RuntimeException("The session factory has not been initialized (or an error occured during initialization)");
			else
				return sessionFactory;
		}
		else {
			if (null == sessionFactoryMap)
				throw new RuntimeException("The session factory for '" + configFile + "' has not been initialized (or an error occured during initialization)");
			else {
				SessionFactory sf = (SessionFactory) sessionFactoryMap.get(configFile);
				if (null == sf)
					throw new RuntimeException("The session factory for '" + configFile + "' has not been initialized (or an error occured during initialization)");
				else
					return sf;
			}
		}
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public Session getSession() {
		return getSession(
			getConfigurationFileName(),
			false);
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public Session createNewSession() {
		return getSession(
			getConfigurationFileName(),
			true);
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @param configFile the config file must match the meta attribute "config-file" in the hibernate mapping file
	 * @return the active Session
	 */
	private Session getSession(String configFile, boolean createNew) {
		if (createNew) {
			return getSessionFactory(configFile).openSession();
		}
		else {
			if (null == configFile) {
				if (null == sessions) sessions = new ThreadLocal();
				Session session = (Session) sessions.get();
				if (null == session || !session.isOpen()) {
					session = getSessionFactory(null).openSession();
					sessions.set(session);
				}
				return session;
			}
			else {
				if (null == mappedSessions) mappedSessions = new ThreadLocal();
				java.util.HashMap map = (java.util.HashMap) mappedSessions.get();
				if (null == map) {
					map = new HashMap(1);
					mappedSessions.set(map);
				}
				Session session = (Session) map.get(configFile);
				if (null == session || !session.isOpen()) {
					session = getSessionFactory(configFile).openSession();
					map.put(configFile, session);
				}
				return session;
			}
		}
	}
	
	/**
	 * Get current thread opened session, if there is any
	 */
	public Session getCurrentThreadSession() {
		return getCurrentThreadSession(getConfigurationFileName());
	}
	
	/**
	 * Get current thread opened session, if there is any
	 */
	private Session getCurrentThreadSession(String configFile) {
		if (configFile == null) {
			if (sessions != null) {
				Session session = (Session) sessions.get();
				if (session != null) return session;
			}
		} else {
			if (mappedSessions != null) {
				java.util.HashMap map = (java.util.HashMap) mappedSessions.get();
				if (map != null) {
					Session session = (Session) map.get(configFile);
					return session;
				}
			}
		}
		return null;
	}

	/**
	 * Close all sessions for the current thread
	 */
	public static void closeCurrentThreadSessions () {
		if (null != sessions) {
			Session session = (Session) sessions.get();
			if (null != session && session.isOpen()) {
				session.close();
			}
		}
		if (null != mappedSessions) {
			java.util.HashMap map = (java.util.HashMap) mappedSessions.get();
			if (null != map) {
				HibernateException thrownException = null;
				Session session = null;
				for (Iterator i=map.values().iterator(); i.hasNext(); ) {
					session = (Session) i.next();
					try {
						if (null != session && session.isOpen()) {
							session.close();
						}
					}
					catch (HibernateException e) {
						thrownException = e;
					}
				}
				map.clear();
				if (null != thrownException) throw thrownException;
			}
		}
	}

	/**
	 * Close the session
	 */
	public void closeSession (Session session) {
		// TODO - further research required; assumes servlet filter does closing
		// if (null != session) session.close();
	}

	/**
	 * Begin the transaction related to the session
	 */
	public Transaction beginTransaction(Session s) {
		return s.beginTransaction();
	}

	/**
	 * Commit the given transaction
	 */
	public void commitTransaction(Transaction t) {
		t.commit();
	}

	/**
	 * Return a new Configuration to use
	 */
	 public static Configuration getNewConfiguration (String configFileName) {
	 	return new Configuration();
	 }
	
	/**
	 * @return Returns the configuration.
	 */
	public static Configuration getConfiguration() {
		return configuration;
	}	 	 
	
	 
	
	/**
	 * Return the name of the configuration file to be used with this DAO or null if default
	 */
	public String getConfigurationFileName () {
		return null;
	}

	/**
	 * Return the specific Object class that will be used for class-specific
	 * implementation of this DAO.
	 * @return the reference Class
	 */
	protected abstract Class getReferenceClass();

	/**
	 * Used by the base DAO classes but here for your modification
	 * Get object matching the given key and return it.
	 */
	protected Object get(Class refClass, Serializable key) {
		Session s = null;
		try {
			s = getSession();
			return get(refClass, key, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Get object matching the given key and return it.
	 */
	protected Object get(Class refClass, Serializable key, Session s) {
		return s.get(refClass, key);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Load object matching the given key and return it.
	 */
	protected Object load(Class refClass, Serializable key) {
		Session s = null;
		try {
			s = getSession();
			return load(refClass, key, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Load object matching the given key and return it.
	 */
	protected Object load(Class refClass, Serializable key, Session s) {
		return s.load(refClass, key);
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 */
	public java.util.List findAll () {
		Session s = null;
		try {
			s = getSession();
    		return findAll(s);
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 * Use the session given.
	 * @param s the Session
	 */
	public java.util.List findAll (Session s) {
   		return findAll(s, getDefaultOrder());
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 */
	public java.util.List findAll (Order defaultOrder) {
		Session s = null;
		try {
			s = getSession();
			return findAll(s, defaultOrder);
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 * Use the session given.
	 * @param s the Session
	 */
	public java.util.List findAll (Session s, Order defaultOrder) {
		Criteria crit = s.createCriteria(getReferenceClass());
		if (null != defaultOrder) crit.addOrder(defaultOrder);
		return crit.list();
	}

	/**
	 * Return all objects related to the implementation of this DAO with a filter.
	 * Use the session given.
	 * @param propName the name of the property to use for filtering
	 * @param filter the value of the filter
	 */
	protected java.util.List findFiltered (String propName, Object filter) {
		return findFiltered(propName, filter, getDefaultOrder());
	}

	/**
	 * Return all objects related to the implementation of this DAO with a filter.
	 * Use the session given.
	 * @param propName the name of the property to use for filtering
	 * @param filter the value of the filter
	 * @param orderProperty the name of the property used for ordering
	 */
	protected java.util.List findFiltered (String propName, Object filter, Order order) {
		Session s = null;
		try {
			s = getSession();
			return findFiltered(s, propName, filter, getDefaultOrder());
		}
		finally {
			closeSession(s);
		}
	}
	
	/**
	 * Return all objects related to the implementation of this DAO with a filter.
	 * Use the session given.
	 * @param s the Session
	 * @param propName the name of the property to use for filtering
	 * @param filter the value of the filter
	 * @param orderProperty the name of the property used for ordering
	 */
	protected java.util.List findFiltered (Session s, String propName, Object filter, Order order) {
		Criteria crit = s.createCriteria(getReferenceClass());
		crit.add(Restrictions.eq(propName, filter));
		if (null != order) crit.addOrder(order);
		return crit.list();
	}
	
	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * @param name the name of a query defined externally 
	 * @return Query
	 */
	protected Query getNamedQuery(String name) {
		Session s = null;
		try {
			s = getSession();
			return getNamedQuery(name, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the session given.
	 * @param name the name of a query defined externally 
	 * @param s the Session
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Session s) {
		Query q = s.getNamedQuery(name);
		return q;
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * @param name the name of a query defined externally 
	 * @param param the first parameter to set
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Serializable param) {
		Session s = null;
		try {
			s = getSession();
			return getNamedQuery(name, param, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the session given.
	 * @param name the name of a query defined externally 
	 * @param param the first parameter to set
	 * @param s the Session
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Serializable param, Session s) {
		Query q = s.getNamedQuery(name);
		q.setParameter(0, param);
		return q;
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the parameters given.
	 * @param name the name of a query defined externally 
	 * @param params the parameter array
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Serializable[] params) {
		Session s = null;
		try {
			s = getSession();
			return getNamedQuery(name, params, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the parameters given and the Session given.
	 * @param name the name of a query defined externally 
	 * @param params the parameter array
	 * @s the Session
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Serializable[] params, Session s) {
		Query q = s.getNamedQuery(name);
		if (null != params) {
			for (int i = 0; i < params.length; i++) {
				q.setParameter(i, params[i]);
			}
		}
		return q;
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the parameters given.
	 * @param name the name of a query defined externally 
	 * @param params the parameter Map
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Map params) {
		Session s = null;
		try {
			s = getSession();
			return getNamedQuery(name, params, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the parameters given and the Session given.
	 * @param name the name of a query defined externally 
	 * @param params the parameter Map
	 * @s the Session
	 * @return Query
	 */
	protected Query getNamedQuery(String name, Map params, Session s) {
		Query q = s.getNamedQuery(name);
		if (null != params) {
			for (Iterator i=params.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry entry = (Map.Entry) i.next();
				q.setParameter((String) entry.getKey(), entry.getValue());
			}
		}
		return q;
	}

	/**
	 * Execute a query. 
	 * @param queryStr a query expressed in Hibernate's query language
	 * @return a distinct list of instances (or arrays of instances)
	 */
	public Query getQuery(String queryStr) {
		Session s = null;
		try {
			s = getSession();
			return getQuery(queryStr, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Execute a query but use the session given instead of creating a new one.
	 * @param queryStr a query expressed in Hibernate's query language
	 * @s the Session to use
	 */
	public Query getQuery(String queryStr, Session s) {
		return s.createQuery(queryStr);
	}



	/**
	 * Execute a query. 
	 * @param query a query expressed in Hibernate's query language
	 * @param queryStr the name of a query defined externally 
	 * @param param the first parameter to set
	 * @return Query
	 */
	protected Query getQuery(String queryStr, Serializable param) {
		Session s = null;
		try {
			s = getSession();
			return getQuery(queryStr, param, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Execute a query but use the session given instead of creating a new one.
	 * @param queryStr a query expressed in Hibernate's query language
	 * @param param the first parameter to set
	 * @s the Session to use
	 * @return Query
	 */
	protected Query getQuery(String queryStr, Serializable param, Session s) {
		Query q = getQuery(queryStr, s);
		q.setParameter(0, param);
		return q;
	}

	/**
	 * Execute a query. 
	 * @param queryStr a query expressed in Hibernate's query language
	 * @param params the parameter array
	 * @return Query
	 */
	protected Query getQuery(String queryStr, Serializable[] params) {
		Session s = null;
		try {
			s = getSession();
			return getQuery(queryStr, params, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Execute a query but use the session given instead of creating a new one.
	 * @param queryStr a query expressed in Hibernate's query language
	 * @param params the parameter array
	 * @s the Session
	 * @return Query
	 */
	protected Query getQuery(String queryStr, Serializable[] params, Session s) {
		Query q = getQuery(queryStr, s);
		if (null != params) {
			for (int i = 0; i < params.length; i++) {
				q.setParameter(i, params[i]);
			}
		}
		return q;
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the parameters given.
	 * @param queryStr a query expressed in Hibernate's query language
	 * @param params the parameter Map
	 * @return Query
	 */
	protected Query getQuery(String queryStr, Map params) {
		Session s = null;
		try {
			s = getSession();
			return getQuery(queryStr, params, s);
		} finally {
			closeSession(s);
		}
	}

	/**
	 * Obtain an instance of Query for a named query string defined in the mapping file.
	 * Use the parameters given and the Session given.
	 * @param queryStr a query expressed in Hibernate's query language
	 * @param params the parameter Map
	 * @s the Session
	 * @return Query
	 */
	protected Query getQuery(String queryStr, Map params, Session s) {
		Query q = getQuery(queryStr, s);
		if (null != params) {
			for (Iterator i=params.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry entry = (Map.Entry) i.next();
				q.setParameter((String) entry.getKey(), entry.getValue());
			}
		}
		return q;
	}

	protected Order getDefaultOrder () {
		return null;
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Persist the given transient instance, first assigning a generated identifier. 
	 * (Or using the current value of the identifier property if the assigned generator is used.) 
	 */
	protected Serializable save(Object obj) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			Serializable rtn = save(obj, s);
			commitTransaction(t);
			return rtn;
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Persist the given transient instance, first assigning a generated identifier. 
	 * (Or using the current value of the identifier property if the assigned generator is used.) 
	 */
	protected Serializable save(Object obj, Session s) {
		return s.save(obj);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Either save() or update() the given instance, depending upon the value of its
	 * identifier property.
	 */
	protected void saveOrUpdate(Object obj) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			saveOrUpdate(obj, s);
			commitTransaction(t);
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Either save() or update() the given instance, depending upon the value of its
	 * identifier property.
	 */
	protected void saveOrUpdate(Object obj, Session s) {
		s.saveOrUpdate(obj);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param obj a transient instance containing updated state
	 */
	protected void update(Object obj) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			update(obj, s);
			commitTransaction(t);
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param obj a transient instance containing updated state
	 * @param s the Session
	 */
	protected void update(Object obj, Session s) {
		s.update(obj);
	}

	/**
	 * Delete all objects returned by the query. Return the number of objects deleted.
	 */
	protected int delete(String delStmt) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			int rtn = delete(delStmt, s);
			commitTransaction(t);
			return rtn;
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Delete all objects returned by the query. Return the number of objects deleted.
	 */
	protected int  delete(String delStmt, Session s) {
		return delete (delStmt, null, s);
	}

	/**
	 * Delete all objects returned by the query.
	 */
	protected int delete(String delStmt, Object parameterValue) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			int rtn = delete (delStmt, null, s);
			commitTransaction(t);
			return rtn;
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Delete all objects returned by the query.
	 */
	protected int delete(String delStmt, Object value, Session s) {
		Query query = s.createQuery (delStmt);
		if (null != value)
			query.setParameter(0, value);
		return delete (query, s);
	}

	/**
	 * Delete all objects returned by the query
	 */
	protected int delete (Query query) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			int rtn = delete (query, s);
			commitTransaction(t);
			return rtn;
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Delete all objects returned by the query
	 */
	protected int delete (Query query, Session s) {
		List list = query.list();
		for (Iterator i=list.iterator(); i.hasNext(); ) {
			delete(i.next(), s);
		}
		return list.size();
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 */
	protected void delete(Object obj) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			delete(obj, s);
			commitTransaction(t);
		}
		catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
		finally {
			closeSession(s);
		}
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 */
	protected void delete(Object obj, Session s) {
		s.delete(obj);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
	 * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
	 */
	protected void refresh(Object obj, Session s) {
		s.refresh(obj);
	}


}
