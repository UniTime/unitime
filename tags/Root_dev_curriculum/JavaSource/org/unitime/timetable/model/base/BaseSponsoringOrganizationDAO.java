package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.hibernate.criterion.Order;

/**
 * This is an automatically generated DAO class which should not be edited.
 */
public abstract class BaseSponsoringOrganizationDAO extends org.unitime.timetable.model.dao._RootDAO {

	// query name references


	public static SponsoringOrganizationDAO instance;

	/**
	 * Return a singleton of the DAO
	 */
	public static SponsoringOrganizationDAO getInstance () {
		if (null == instance) instance = new SponsoringOrganizationDAO();
		return instance;
	}

	public Class getReferenceClass () {
		return org.unitime.timetable.model.SponsoringOrganization.class;
	}

    public Order getDefaultOrder () {
		return Order.asc("name");
    }

	/**
	 * Cast the object as a org.unitime.timetable.model.SponsoringOrganization
	 */
	public org.unitime.timetable.model.SponsoringOrganization cast (Object object) {
		return (org.unitime.timetable.model.SponsoringOrganization) object;
	}

	public org.unitime.timetable.model.SponsoringOrganization get(java.lang.Long key)
	{
		return (org.unitime.timetable.model.SponsoringOrganization) get(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.SponsoringOrganization get(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.SponsoringOrganization) get(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.SponsoringOrganization load(java.lang.Long key)
	{
		return (org.unitime.timetable.model.SponsoringOrganization) load(getReferenceClass(), key);
	}

	public org.unitime.timetable.model.SponsoringOrganization load(java.lang.Long key, Session s)
	{
		return (org.unitime.timetable.model.SponsoringOrganization) load(getReferenceClass(), key, s);
	}

	public org.unitime.timetable.model.SponsoringOrganization loadInitialize(java.lang.Long key, Session s) 
	{ 
		org.unitime.timetable.model.SponsoringOrganization obj = load(key, s); 
		if (!Hibernate.isInitialized(obj)) {
			Hibernate.initialize(obj);
		} 
		return obj; 
	}


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param sponsoringOrganization a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization)
	{
		return (java.lang.Long) super.save(sponsoringOrganization);
	}

	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * Use the Session given.
	 * @param sponsoringOrganization a transient instance of a persistent class
	 * @param s the Session
	 * @return the class identifier
	 */
	public java.lang.Long save(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization, Session s)
	{
		return (java.lang.Long) save((Object) sponsoringOrganization, s);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param sponsoringOrganization a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization)
	{
		saveOrUpdate((Object) sponsoringOrganization);
	}

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default the
	 * instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the identifier
	 * property mapping. 
	 * Use the Session given.
	 * @param sponsoringOrganization a transient instance containing new or updated state.
	 * @param s the Session.
	 */
	public void saveOrUpdate(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization, Session s)
	{
		saveOrUpdate((Object) sponsoringOrganization, s);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param sponsoringOrganization a transient instance containing updated state
	 */
	public void update(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization) 
	{
		update((Object) sponsoringOrganization);
	}

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * Use the Session given.
	 * @param sponsoringOrganization a transient instance containing updated state
	 * @param the Session
	 */
	public void update(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization, Session s)
	{
		update((Object) sponsoringOrganization, s);
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
	 * @param sponsoringOrganization the instance to be removed
	 */
	public void delete(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization)
	{
		delete((Object) sponsoringOrganization);
	}

	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * Use the Session given.
	 * @param sponsoringOrganization the instance to be removed
	 * @param s the Session
	 */
	public void delete(org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization, Session s)
	{
		delete((Object) sponsoringOrganization, s);
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
	public void refresh (org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization, Session s)
	{
		refresh((Object) sponsoringOrganization, s);
	}


}