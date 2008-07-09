package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the SPONSORING_ORGANIZATION table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SPONSORING_ORGANIZATION"
 */

public abstract class BaseSponsoringOrganization  implements Serializable {

	public static String REF = "SponsoringOrganization";
	public static String PROP_NAME = "name";
	public static String PROP_EMAIL = "email";


	// constructors
	public BaseSponsoringOrganization () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSponsoringOrganization (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSponsoringOrganization (
		java.lang.Long uniqueId,
		java.lang.String name) {

		this.setUniqueId(uniqueId);
		this.setName(name);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String email;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="org.unitime.commons.hibernate.id.UniqueIdGenerator"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: NAME
	 */
	public java.lang.String getName () {
		return name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param name the NAME value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
	}



	/**
	 * Return the value associated with the column: EMAIL
	 */
	public java.lang.String getEmail () {
		return email;
	}

	/**
	 * Set the value related to the column: EMAIL
	 * @param email the EMAIL value
	 */
	public void setEmail (java.lang.String email) {
		this.email = email;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SponsoringOrganization)) return false;
		else {
			org.unitime.timetable.model.SponsoringOrganization sponsoringOrganization = (org.unitime.timetable.model.SponsoringOrganization) obj;
			if (null == this.getUniqueId() || null == sponsoringOrganization.getUniqueId()) return false;
			else return (this.getUniqueId().equals(sponsoringOrganization.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}