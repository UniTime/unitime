package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.unitime.timetable.model.dao.FinalExamEventDAO;
import org.hibernate.criterion.Order;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseFinalExamEventDAO extends org.unitime.timetable.model.dao._RootDAO {

	// query name references


	public static FinalExamEventDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static FinalExamEventDAO getInstance () {
		if (null == instance) instance = new FinalExamEventDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.timetable.model.FinalExamEvent.class;
	}

    public Order getDefaultOrder () {
		return null;
    }

	/**
	 * Cast the object as a org.unitime.timetable.model.FinalExamEvent
	 */
	public org.unitime.timetable.model.FinalExamEvent cast (Object object) {
		return (org.unitime.timetable.model.FinalExamEvent) object;
	}

	public org.unitime.timetable.model.FinalExamEvent get(java.lang.Long key)
	{
		return (org.unitime.timetable.model.FinalExamEvent) get(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.FinalExamEvent get(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.FinalExamEvent) get(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.FinalExamEvent load(java.lang.Long key)
	{
		return (org.unitime.timetable.model.FinalExamEvent) load(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.FinalExamEvent load(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.FinalExamEvent) load(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.FinalExamEvent loadInitialize(java.lang.Long key, Session s) 
	{ 
		org.unitime.timetable.model.FinalExamEvent obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param finalExamEvent a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.FinalExamEvent finalExamEvent)
	{
		return (java.lang.Long) super.save(finalExamEvent);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param finalExamEvent a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.FinalExamEvent finalExamEvent, Session s)
	{
		return (java.lang.Long) save((Object) finalExamEvent, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param finalExamEvent a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.timetable.model.FinalExamEvent finalExamEvent)
	{
		saveOrUpdate((Object) finalExamEvent);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param finalExamEvent a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.timetable.model.FinalExamEvent finalExamEvent, Session s)
	{
		saveOrUpdate((Object) finalExamEvent, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param finalExamEvent a transient instance containing updated state
	 */
	public void update(org.unitime.timetable.model.FinalExamEvent finalExamEvent) 
	{
		update((Object) finalExamEvent);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param finalExamEvent a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.timetable.model.FinalExamEvent finalExamEvent, Session s)
	{
		update((Object) finalExamEvent, s);
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
	 * @param finalExamEvent the instance to be removed
	 */
	public void delete(org.unitime.timetable.model.FinalExamEvent finalExamEvent)
	{
		delete((Object) finalExamEvent);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param finalExamEvent the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.timetable.model.FinalExamEvent finalExamEvent, Session s)
	{
		delete((Object) finalExamEvent, s);
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
	public void refresh (org.unitime.timetable.model.FinalExamEvent finalExamEvent, Session s)
	{
		refresh((Object) finalExamEvent, s);
	}


}