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

public abstract class BaseFinalExamEvent extends org.unitime.timetable.model.ExamEvent  implements Serializable {

	public static String REF = "FinalExamEvent";


	// constructors
	public BaseFinalExamEvent () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseFinalExamEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseFinalExamEvent (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}



	private int hashCode = Integer.MIN_VALUE;









	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.FinalExamEvent)) return false;
		else {
			org.unitime.timetable.model.FinalExamEvent finalExamEvent = (org.unitime.timetable.model.FinalExamEvent) obj;
			if (null == this.getUniqueId() || null == finalExamEvent.getUniqueId()) return false;
			else return (this.getUniqueId().equals(finalExamEvent.getUniqueId()));
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