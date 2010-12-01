package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.hibernate.criterion.Order;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseCourseEventDAO extends org.unitime.timetable.model.dao._RootDAO {

	// query name references


	public static CourseEventDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static CourseEventDAO getInstance () {
		if (null == instance) instance = new CourseEventDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.timetable.model.CourseEvent.class;
	}

    public Order getDefaultOrder () {
		return null;
    }

	/**
	 * Cast the object as a org.unitime.timetable.model.CourseEvent
	 */
	public org.unitime.timetable.model.CourseEvent cast (Object object) {
		return (org.unitime.timetable.model.CourseEvent) object;
	}

	public org.unitime.timetable.model.CourseEvent get(java.lang.Long key)
	{
		return (org.unitime.timetable.model.CourseEvent) get(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.CourseEvent get(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.CourseEvent) get(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.CourseEvent load(java.lang.Long key)
	{
		return (org.unitime.timetable.model.CourseEvent) load(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.CourseEvent load(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.CourseEvent) load(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.CourseEvent loadInitialize(java.lang.Long key, Session s) 
	{ 
		org.unitime.timetable.model.CourseEvent obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param courseEvent a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.CourseEvent courseEvent)
	{
		return (java.lang.Long) super.save(courseEvent);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param courseEvent a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.CourseEvent courseEvent, Session s)
	{
		return (java.lang.Long) save((Object) courseEvent, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param courseEvent a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.timetable.model.CourseEvent courseEvent)
	{
		saveOrUpdate((Object) courseEvent);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param courseEvent a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.timetable.model.CourseEvent courseEvent, Session s)
	{
		saveOrUpdate((Object) courseEvent, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param courseEvent a transient instance containing updated state
	 */
	public void update(org.unitime.timetable.model.CourseEvent courseEvent) 
	{
		update((Object) courseEvent);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param courseEvent a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.timetable.model.CourseEvent courseEvent, Session s)
	{
		update((Object) courseEvent, s);
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
	 * @param courseEvent the instance to be removed
	 */
	public void delete(org.unitime.timetable.model.CourseEvent courseEvent)
	{
		delete((Object) courseEvent);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param courseEvent the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.timetable.model.CourseEvent courseEvent, Session s)
	{
		delete((Object) courseEvent, s);
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
	public void refresh (org.unitime.timetable.model.CourseEvent courseEvent, Session s)
	{
		refresh((Object) courseEvent, s);
	}


}