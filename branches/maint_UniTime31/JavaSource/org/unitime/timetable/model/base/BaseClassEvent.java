package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the EVENT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EVENT"
 */

public abstract class BaseClassEvent extends org.unitime.timetable.model.Event  implements Serializable {

	public static String REF = "ClassEvent";


	// constructors
	public BaseClassEvent () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseClassEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseClassEvent (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}



	private int hashCode = Integer.MIN_VALUE;


	// many to one
	private org.unitime.timetable.model.Class_ clazz;






	/**
	 * Return the value associated with the column: CLASS_ID
	 */
	public org.unitime.timetable.model.Class_ getClazz () {
		return clazz;
	}

	/**
	 * Set the value related to the column: CLASS_ID
	 * @param clazz the CLASS_ID value
	 */
	public void setClazz (org.unitime.timetable.model.Class_ clazz) {
		this.clazz = clazz;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ClassEvent)) return false;
		else {
			org.unitime.timetable.model.ClassEvent classEvent = (org.unitime.timetable.model.ClassEvent) obj;
			if (null == this.getUniqueId() || null == classEvent.getUniqueId()) return false;
			else return (this.getUniqueId().equals(classEvent.getUniqueId()));
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