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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.query.Query;
import org.unitime.commons.hibernate.util.HibernateUtil;

/**
 * @author Tomas Muller
 */
public abstract class _BaseRootDAO<T, K extends Serializable> {
	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public Session getSession() {
		return HibernateUtil.getSession();
	}

	/**
	 * Return a new Session object that must be closed when the work has been completed.
	 * @return the active Session
	 */
	public Session createNewSession() {
		return HibernateUtil.createNewSession();
	}
	
	/**
	 * Begin the transaction related to the session
	 */
	protected Transaction beginTransaction(Session s) {
		// already in a transaction, do not create a new one
		if (s.getTransaction() != null && s.getTransaction().isActive()) return null;
		
		return s.beginTransaction();
	}

	/**
	 * Commit the given transaction
	 */
	protected void commitTransaction(Transaction t) {
		if (t != null) t.commit();
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
		Transaction t = null;
		Session s = null;
		try {
			s = getSession();
			t = beginTransaction(s);
			List<T> rtn = findAll(s);
			commitTransaction(t);
			return rtn;
		} catch (HibernateException e) {
			if (null != t) t.rollback();
            throw e;
		}
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
		CriteriaBuilder cb = s.getCriteriaBuilder();
		CriteriaQuery<T> cr = cb.createQuery(getReferenceClass());
		Root<T> root = cr.from(getReferenceClass());
		cr.select(root);
		if (orders != null && orders.length > 0) {
			List<javax.persistence.criteria.Order> x = new ArrayList<javax.persistence.criteria.Order>();
			for (Order o: orders)
				if (o != null)
					x.add(o.isAscending() ? cb.asc(root.get(o.getPropertyName())) : cb.desc(root.get(o.getPropertyName())));
			if (!x.isEmpty())
				cr.orderBy(x);
		}
		Query<T> query = s.createQuery(cr);
		return query.getResultList();
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
}