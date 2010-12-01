package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the EXAM_PERIOD table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXAM_PERIOD"
 */

public abstract class BaseExamPeriod  implements Serializable {

	public static String REF = "ExamPeriod";
	public static String PROP_DATE_OFFSET = "dateOffset";
	public static String PROP_START_SLOT = "startSlot";
	public static String PROP_LENGTH = "length";
	public static String PROP_EXAM_TYPE = "examType";
	public static String PROP_EVENT_START_OFFSET = "eventStartOffset";
	public static String PROP_EVENT_STOP_OFFSET = "eventStopOffset";


	// constructors
	public BaseExamPeriod () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamPeriod (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamPeriod (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.PreferenceLevel prefLevel,
		java.lang.Integer dateOffset,
		java.lang.Integer startSlot,
		java.lang.Integer length,
		java.lang.Integer examType,
		java.lang.Integer eventStartOffset,
		java.lang.Integer eventStopOffset) {

		this.setUniqueId(uniqueId);
		this.setPrefLevel(prefLevel);
		this.setDateOffset(dateOffset);
		this.setStartSlot(startSlot);
		this.setLength(length);
		this.setExamType(examType);
		this.setEventStartOffset(eventStartOffset);
		this.setEventStopOffset(eventStopOffset);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer dateOffset;
	private java.lang.Integer startSlot;
	private java.lang.Integer length;
	private java.lang.Integer examType;
	private java.lang.Integer eventStartOffset;
	private java.lang.Integer eventStopOffset;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.PreferenceLevel prefLevel;



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
	 * Return the value associated with the column: DATE_OFS
	 */
	public java.lang.Integer getDateOffset () {
		return dateOffset;
	}

	/**
	 * Set the value related to the column: DATE_OFS
	 * @param dateOffset the DATE_OFS value
	 */
	public void setDateOffset (java.lang.Integer dateOffset) {
		this.dateOffset = dateOffset;
	}



	/**
	 * Return the value associated with the column: START_SLOT
	 */
	public java.lang.Integer getStartSlot () {
		return startSlot;
	}

	/**
	 * Set the value related to the column: START_SLOT
	 * @param startSlot the START_SLOT value
	 */
	public void setStartSlot (java.lang.Integer startSlot) {
		this.startSlot = startSlot;
	}



	/**
	 * Return the value associated with the column: LENGTH
	 */
	public java.lang.Integer getLength () {
		return length;
	}

	/**
	 * Set the value related to the column: LENGTH
	 * @param length the LENGTH value
	 */
	public void setLength (java.lang.Integer length) {
		this.length = length;
	}



	/**
	 * Return the value associated with the column: EXAM_TYPE
	 */
	public java.lang.Integer getExamType () {
		return examType;
	}

	/**
	 * Set the value related to the column: EXAM_TYPE
	 * @param examType the EXAM_TYPE value
	 */
	public void setExamType (java.lang.Integer examType) {
		this.examType = examType;
	}



	/**
	 * Return the value associated with the column: event_start_offset
	 */
	public java.lang.Integer getEventStartOffset () {
		return eventStartOffset;
	}

	/**
	 * Set the value related to the column: event_start_offset
	 * @param eventStartOffset the event_start_offset value
	 */
	public void setEventStartOffset (java.lang.Integer eventStartOffset) {
		this.eventStartOffset = eventStartOffset;
	}



	/**
	 * Return the value associated with the column: event_stop_offset
	 */
	public java.lang.Integer getEventStopOffset () {
		return eventStopOffset;
	}

	/**
	 * Set the value related to the column: event_stop_offset
	 * @param eventStopOffset the event_stop_offset value
	 */
	public void setEventStopOffset (java.lang.Integer eventStopOffset) {
		this.eventStopOffset = eventStopOffset;
	}



	/**
	 * Return the value associated with the column: SESSION_ID
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param session the SESSION_ID value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: PREF_LEVEL_ID
	 */
	public org.unitime.timetable.model.PreferenceLevel getPrefLevel () {
		return prefLevel;
	}

	/**
	 * Set the value related to the column: PREF_LEVEL_ID
	 * @param prefLevel the PREF_LEVEL_ID value
	 */
	public void setPrefLevel (org.unitime.timetable.model.PreferenceLevel prefLevel) {
		this.prefLevel = prefLevel;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExamPeriod)) return false;
		else {
			org.unitime.timetable.model.ExamPeriod examPeriod = (org.unitime.timetable.model.ExamPeriod) obj;
			if (null == this.getUniqueId() || null == examPeriod.getUniqueId()) return false;
			else return (this.getUniqueId().equals(examPeriod.getUniqueId()));
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