package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.unitime.timetable.model.dao.CurriculaDAO;
import org.hibernate.criterion.Order;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseCurriculaDAO extends org.unitime.timetable.model.dao._RootDAO {

	// query name references


	public static CurriculaDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static CurriculaDAO getInstance () {
		if (null == instance) instance = new CurriculaDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.timetable.model.Curricula.class;
	}

    public Order getDefaultOrder () {
		return Order.asc("name");
    }

	/**
	 * Cast the object as a org.unitime.timetable.model.Curricula
	 */
	public org.unitime.timetable.model.Curricula cast (Object object) {
		return (org.unitime.timetable.model.Curricula) object;
	}

	public org.unitime.timetable.model.Curricula get(java.lang.Long key)
	{
		return (org.unitime.timetable.model.Curricula) get(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.Curricula get(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.Curricula) get(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.Curricula load(java.lang.Long key)
	{
		return (org.unitime.timetable.model.Curricula) load(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.Curricula load(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.Curricula) load(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.Curricula loadInitialize(java.lang.Long key, Session s) 
	{ 
		org.unitime.timetable.model.Curricula obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param curricula a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.Curricula curricula)
	{
		return (java.lang.Long) super.save(curricula);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param curricula a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.Curricula curricula, Session s)
	{
		return (java.lang.Long) save((Object) curricula, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param curricula a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.timetable.model.Curricula curricula)
	{
		saveOrUpdate((Object) curricula);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param curricula a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.timetable.model.Curricula curricula, Session s)
	{
		saveOrUpdate((Object) curricula, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param curricula a transient instance containing updated state
	 */
	public void update(org.unitime.timetable.model.Curricula curricula) 
	{
		update((Object) curricula);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param curricula a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.timetable.model.Curricula curricula, Session s)
	{
		update((Object) curricula, s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param id the instance ID to be removed
	 */
	public void delete(java.lang.Long id)
	{
		delete((Object) load(id));
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param id the instance ID to be removed
	 * @param s the Session
	 */
	public void delete(java.lang.Long id, Session s)
	{
		delete((Object) load(id, s), s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param curricula the instance to be removed
	 */
	public void delete(org.unitime.timetable.model.Curricula curricula)
	{
		delete((Object) curricula);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param curricula the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.timetable.model.Curricula curricula, Session s)
	{
		delete((Object) curricula, s);
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
	public void refresh (org.unitime.timetable.model.Curricula curricula, Session s)
	{
		refresh((Object) curricula, s);
	}


}