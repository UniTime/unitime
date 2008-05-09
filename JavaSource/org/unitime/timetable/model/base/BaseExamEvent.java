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

public abstract class BaseExamEvent extends org.unitime.timetable.model.Event  implements Serializable {

	public static String REF = "ExamEvent";


	// constructors
	public BaseExamEvent () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamEvent (
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
	private org.unitime.timetable.model.Exam exam;






	/**
	 * Return the value associated with the column: EXAM_ID
	 */
	public org.unitime.timetable.model.Exam getExam () {
		return exam;
	}

	/**
	 * Set the value related to the column: EXAM_ID
	 * @param exam the EXAM_ID value
	 */
	public void setExam (org.unitime.timetable.model.Exam exam) {
		this.exam = exam;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExamEvent)) return false;
		else {
			org.unitime.timetable.model.ExamEvent examEvent = (org.unitime.timetable.model.ExamEvent) obj;
			if (null == this.getUniqueId() || null == examEvent.getUniqueId()) return false;
			else return (this.getUniqueId().equals(examEvent.getUniqueId()));
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