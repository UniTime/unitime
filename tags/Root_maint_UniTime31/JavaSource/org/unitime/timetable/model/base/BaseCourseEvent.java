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

public abstract class BaseCourseEvent extends org.unitime.timetable.model.Event  implements Serializable {

	public static String REF = "CourseEvent";
	public static String PROP_REQ_ATTENDANCE = "reqAttendance";


	// constructors
	public BaseCourseEvent () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseEvent (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Boolean reqAttendance;

	// collections
	private java.util.Set relatedCourses;






	/**
	 * Return the value associated with the column: REQ_ATTD
	 */
	public java.lang.Boolean isReqAttendance () {
		return reqAttendance;
	}

	/**
	 * Set the value related to the column: REQ_ATTD
	 * @param reqAttendance the REQ_ATTD value
	 */
	public void setReqAttendance (java.lang.Boolean reqAttendance) {
		this.reqAttendance = reqAttendance;
	}



	/**
	 * Return the value associated with the column: relatedCourses
	 */
	public java.util.Set getRelatedCourses () {
		return relatedCourses;
	}

	/**
	 * Set the value related to the column: relatedCourses
	 * @param relatedCourses the relatedCourses value
	 */
	public void setRelatedCourses (java.util.Set relatedCourses) {
		this.relatedCourses = relatedCourses;
	}

	public void addTorelatedCourses (org.unitime.timetable.model.RelatedCourseInfo relatedCourseInfo) {
		if (null == getRelatedCourses()) setRelatedCourses(new java.util.HashSet());
		getRelatedCourses().add(relatedCourseInfo);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseEvent)) return false;
		else {
			org.unitime.timetable.model.CourseEvent courseEvent = (org.unitime.timetable.model.CourseEvent) obj;
			if (null == this.getUniqueId() || null == courseEvent.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseEvent.getUniqueId()));
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