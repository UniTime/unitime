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
package org.unitime.timetable.model.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.unitime.commons.hibernate.util.HibernateUtil;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;


/**
 * @author Tomas Muller
 */
public class _RootDAO<T, K extends Serializable> {
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
		CriteriaBuilder cb = s.getCriteriaBuilder();
		CriteriaQuery<T> cr = cb.createQuery(getReferenceClass());
		Root<T> root = cr.from(getReferenceClass());
		cr.select(root);
		Query<T> query = s.createQuery(cr);
		query.setCacheable(true);
		return query.getResultList();
	}

	/**
	 * Return the specific Object class that will be used for class-specific
	 * implementation of this DAO.
	 * @return the reference Class
	 */
	public Class<T> getReferenceClass () {
		return null;
	}
}
