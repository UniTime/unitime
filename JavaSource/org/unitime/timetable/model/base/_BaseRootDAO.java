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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;

import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Order;
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.commons.hibernate.util.HibernateUtil;

public abstract class _BaseRootDAO<T, K extends Serializable> {

	protected static Map<String, SessionFactory> sSessionFactoryMap;
	protected static SessionFactory sSessionFactory;
	protected static ThreadLocal<HashMap<String,Session>> sMappedSessions;
	protected static ThreadLocal<Session> sSessions;
	protected static Configuration sConfiguration;
	

	/**
	 * Configure the session factory by reading hibernate config file
	 */
	public static void initialize () {
		initialize(null);
	}
	
	/**
	 * Configure the session factory by reading hibernate config file
	 * @param configFileName the name of the configuration file
	 */
	public static void initialize (String configFileName) {
		initialize(configFileName, getNewConfiguration(null));
	}

	public static void initialize (String configFileName, Configuration configuration) {
		if (configFileName == null && sSessionFactory != null) return;
        if (sSessionFactoryMap != null && sSessionFactoryMap.get(configFileName) != null) return;
        HibernateUtil.configureHibernateFromRootDAO(configFileName, configuration);
        setSessionFactory(configuration.buildSessionFactory());
        sConfiguration = configuration;
        HibernateUtil.addBitwiseOperationsToDialect();
        DatabaseUpdate.update();
	}

	/**
	 * Set the session factory
	 */
	protected static void setSessionFactory (SessionFactory sessionFactory) {
		setSessionFactory(null, sessionFactory);
	}

	/**
	 * Set the session factory
	 */
	protected static void setSessionFactory (String configFileName, SessionFactory sessionFactory) {
		if (configFileName == null) {
			sSessionFactory = sessionFactory;
		} else {
			if (sSessionFactoryMap == null)
				sSessionFactoryMap = new HashMap<String, SessionFactory>();
			sSessionFactoryMap.put(configFileName, sessionFactory);
		}
	}

	/**
	 * Return the SessionFactory that is to be used by these DAOs.  Change this
	 * and implement your own strategy if you, for example, want to pull the SessionFactory
	 * from the JNDI tree.
	 */
	protected SessionFactory getSessionFactory() {
		return getSessionFactory(getConfigurationFileName());
	}

	protected SessionFactory getSessionFactory(String configFile) {
		if (configFile == null) {
			if (sSessionFactory == null)
				throw new RuntimeException("The session factory has not been initialized (or an error occured during initialization)");
			else
				return sSessionFactory;
		}
		else {
			if (sSessionFactoryMap == null)
				throw new RuntimeException("The session factory for '" + configFile + "' has not been initialized (or an error occured during initialization)");
			else {
				SessionFactory sessionFactory = (SessionFactory) sSessionFactoryMap.get(configFile);
				if (sessionFactory == null)
					throw new RuntimeException("The session factory for '" + configFile + "' has not been initialized (or an error occured during initialization)");
				else
					return sessionFactory;
			}
		}
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public Session getSession() {
		return getSession(getConfigurationFileName(), false);
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public Session createNewSession() {
		return getSession(getConfigurationFileName(), true);
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @param configFile the config file must match the meta attribute "config-file" in the hibernate mapping file
	 * @return the active Session
	 */
	private Session getSession(String configFile, boolean createNew) {
		if (createNew) {
			return getSessionFactory(configFile).openSession();
		} else {
			if (configFile == null) {
				if (sSessions == null)
					sSessions = new ThreadLocal<Session>();
				Session session = sSessions.get();
				if (session == null || !session.isOpen()) {
					session = getSessionFactory(null).openSession();
					sSessions.set(session);
				}
				return session;
			}
			else {
				if (sMappedSessions == null) sMappedSessions = new ThreadLocal<HashMap<String,Session>>();
				HashMap<String,Session> map = sMappedSessions.get();
				if (map == null) {
					map = new HashMap<String,Session>();
					sMappedSessions.set(map);
				}
				Session session = map.get(configFile);
				if (session == null || !session.isOpen()) {
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
			if (sSessions != null) {
				Session session = sSessions.get();
				if (session != null) return session;
			}
		} else {
			if (sMappedSessions != null) {
				HashMap<String, Session> map = sMappedSessions.get();
				if (map != null) return map.get(configFile);
			}
		}
		return null;
	}

	/**
	 * Close all sessions for the current thread
	 */
	public static boolean closeCurrentThreadSessions() {
		boolean ret = false;
		if (sSessions != null) {
			Session session = sSessions.get();
			if (session != null && session.isOpen()) {
				session.close();
				ret = true;
			}
			sSessions.remove();
		}
		if (sMappedSessions != null) {
			HashMap<String,Session> map = sMappedSessions.get();
			if (map != null) {
				HibernateException thrownException = null;
				for (Session session: map.values()) {
					try {
						if (null != session && session.isOpen()) {
							session.close();
							ret = true;
						}
					} catch (HibernateException e) {
						thrownException = e;
					}
				}
				map.clear();
				if (null != thrownException) throw thrownException;
			}
			sMappedSessions.remove();
		}
		return ret;
	}


	/**
	 * Begin the transaction related to the session
	 */
	public Transaction beginTransaction(Session s) {
		// already in a transaction, do not create a new one
		if (s.getTransaction() != null && s.getTransaction().isActive()) return null;
		
		return s.beginTransaction();
	}

	/**
	 * Commit the given transaction
	 */
	public void commitTransaction(Transaction t) {
		if (t != null) t.commit();
	}

	/**
	 * Return a new Configuration to use
	 */
	 public static Configuration getNewConfiguration(String configFileName) {
	 	return new Configuration();
	 }
	
	/**
	 * @return Returns the configuration.
	 */
	public static Configuration getConfiguration() {
		return sConfiguration;
	}	 	 
	 
	
	/**
	 * Return the name of the configuration file to be used with this DAO or null if default
	 */
	public String getConfigurationFileName() {
		return null;
	}

	/**
	 * Return the specific Object class that will be used for class-specific
	 * implementation of this DAO.
	 * @return the reference Class
	 */
	protected abstract Class<T> getReferenceClass();

	/**
	 * Used by the base DAO classes but here for your modification
	 * Get object matching the given key and return it.
	 */
	protected T get(Class<T> refClass, K key) {
		return get(refClass, key, getSession());
	}
	
	/**
	 * Get object matching the given key and return it.
	 */
	public T get(K key) {
		return get(getReferenceClass(), key);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Get object matching the given key and return it.
	 */
	@SuppressWarnings("unchecked")
	protected T get(Class<T> refClass, K key, Session s) {
		return (T)s.get(refClass, key);
	}
	
	/**
	 * Get object matching the given key and return it.
	 */
	public T get(K key, Session s) {
		return get(getReferenceClass(), key, s);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Load object matching the given key and return it.
	 */
	protected T load(Class<T> refClass, K key) {
		return load(refClass, key, getSession());
	}
	
	/**
	 * Load object matching the given key and return it.
	 */
	public T load(K key) {
		return load(getReferenceClass(), key);
	}

	/**
	 * Used by the base DAO classes but here for your modification
	 * Load object matching the given key and return it.
	 */
	@SuppressWarnings("unchecked")
	protected T load(Class<T> refClass, K key, Session s) {
		return (T)s.load(refClass, key);
	}
	
	/**
	 * Load object matching the given key and return it.
	 */
	public T load(K key, Session s) {
		return load(getReferenceClass(), key, s);
	}
	
	/**
	 * Load and initialize object matching the given key and return it.
	 */
	public T loadInitialize(K key, Session s) {
		T obj = load(key, s);
		if (!Hibernate.isInitialized(obj)) Hibernate.initialize(obj);
		return obj;
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 */
	public List<T> findAll () {
		return findAll(getSession());
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 * Use the session given.
	 * @param s the Session
	 */
	public List<T> findAll (Session s) {
   		return findAll(s, getDefaultOrder());
	}

	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 * The results are ordered by the order specified
	 * Use the session given.
	 */
	public List<T> findAll (Order... orders) {
		return findAll(getSession(), orders);
	}


	/**
	 * Return all objects related to the implementation of this DAO with no filter.
	 * Use the session given.
	 */
	@SuppressWarnings("unchecked")
	public List<T> findAll (Session s, Order... orders) {
		Criteria crit = s.createCriteria(getReferenceClass());
		if (orders != null) {
			for (Order order: orders) {
				if (order != null)
					crit.addOrder(order);
			}
		}
		crit.setCacheable(true);
		return (List<T>)crit.list();
	}

	/**
	 * Execute a query. 
	 * @param queryStr a query expressed in Hibernate's query language
	 * @return a distinct list of instances (or arrays of instances)
	 */
	public Query getQuery(String queryStr) {
		return getQuery(queryStr, getSession());
	}

	/**
	 * Execute a query but use the session given instead of creating a new one.
	 * @param queryStr a query expressed in Hibernate's query language
	 * @param s the Session to use
	 */
	public Query getQuery(String queryStr, Session s) {
		return s.createQuery(queryStr);
	}

	protected Order getDefaultOrder () {
		return null;
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. 
	 * (Or using the current value of the identifier property if the assigned generator is used.) 
	 */
	public K save(T obj) {
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			K rtn = save(obj, s);
			commitTransaction(t);
			return rtn;
		} catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. 
	 * (Or using the current value of the identifier property if the assigned generator is used.) 
	 */
	@SuppressWarnings("unchecked")
	public K save(T obj, Session s) {
		return (K)s.save(obj);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its
	 * identifier property.
	 */
	public void saveOrUpdate(T obj) {
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
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its
	 * identifier property.
	 */
	public void saveOrUpdate(T obj, Session s) {
		s.saveOrUpdate(obj);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param obj a transient instance containing updated state
	 */
	public void update(T obj) {
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
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param obj a transient instance containing updated state
	 * @param s the Session
	 */
	public void update(T obj, Session s) {
		s.update(obj);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 */
	public void delete(T obj) {
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
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 */
	public void delete(T obj, Session s) {
		s.delete(obj);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 */
	public void delete(K key) {
		delete(load(key));
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 */
	public void delete(K key, Session s) {
		s.delete(load(key, s));
	}

	/**
	 * Re-read the state of the given instance from the underlying database. It is inadvisable to use this to implement
	 * long-running sessions that span many business tasks. This method is, however, useful in certain special circumstances.
	 */
	public void refresh(T obj, Session s) {
		s.refresh(obj);
	}
}
