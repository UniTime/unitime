package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.unitime.timetable.model.dao.RoomTypeOptionDAO;
import org.hibernate.criterion.Order;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseRoomTypeOptionDAO extends org.unitime.timetable.model.dao._RootDAO {

	// query name references


	public static RoomTypeOptionDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static RoomTypeOptionDAO getInstance () {
		if (null == instance) instance = new RoomTypeOptionDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.timetable.model.RoomTypeOption.class;
	}

    public Order getDefaultOrder () {
		return null;
    }

	/**
	 * Cast the object as a org.unitime.timetable.model.RoomTypeOption
	 */
	public org.unitime.timetable.model.RoomTypeOption cast (Object object) {
		return (org.unitime.timetable.model.RoomTypeOption) object;
	}

	public org.unitime.timetable.model.RoomTypeOption get(org.unitime.timetable.model.RoomTypeOption key)
	{
		return (org.unitime.timetable.model.RoomTypeOption) get(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.RoomTypeOption get(org.unitime.timetable.model.RoomTypeOption key, Session s)
	{
		return (org.unitime.timetable.model.RoomTypeOption) get(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.RoomTypeOption load(org.unitime.timetable.model.RoomTypeOption key)
	{
		return (org.unitime.timetable.model.RoomTypeOption) load(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.RoomTypeOption load(org.unitime.timetable.model.RoomTypeOption key, Session s)
	{
		return (org.unitime.timetable.model.RoomTypeOption) load(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.RoomTypeOption loadInitialize(org.unitime.timetable.model.RoomTypeOption key, Session s) 
	{ 
		org.unitime.timetable.model.RoomTypeOption obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param roomTypeOption a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public org.unitime.timetable.model.RoomTypeOption save(org.unitime.timetable.model.RoomTypeOption roomTypeOption)
	{
		return (org.unitime.timetable.model.RoomTypeOption) super.save(roomTypeOption);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param roomTypeOption a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public org.unitime.timetable.model.RoomTypeOption save(org.unitime.timetable.model.RoomTypeOption roomTypeOption, Session s)
	{
		return (org.unitime.timetable.model.RoomTypeOption) save((Object) roomTypeOption, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param roomTypeOption a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.timetable.model.RoomTypeOption roomTypeOption)
	{
		saveOrUpdate((Object) roomTypeOption);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param roomTypeOption a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.timetable.model.RoomTypeOption roomTypeOption, Session s)
	{
		saveOrUpdate((Object) roomTypeOption, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param roomTypeOption a transient instance containing updated state
	 */
	public void update(org.unitime.timetable.model.RoomTypeOption roomTypeOption) 
	{
		update((Object) roomTypeOption);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param roomTypeOption a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.timetable.model.RoomTypeOption roomTypeOption, Session s)
	{
		update((Object) roomTypeOption, s);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param roomTypeOption the instance to be removed
	 */
	public void delete(org.unitime.timetable.model.RoomTypeOption roomTypeOption)
	{
		delete((Object) roomTypeOption);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param roomTypeOption the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.timetable.model.RoomTypeOption roomTypeOption, Session s)
	{
		delete((Object) roomTypeOption, s);
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
	public void refresh (org.unitime.timetable.model.RoomTypeOption roomTypeOption, Session s)
	{
		refresh((Object) roomTypeOption, s);
	}


}