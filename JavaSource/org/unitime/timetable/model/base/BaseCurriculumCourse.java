package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the curricula_course table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="curricula_course"
 */

public abstract class BaseCurriculumCourse  implements Serializable {

	public static String REF = "CurriculumCourse";
	public static String PROP_PERC_SHARE = "percShare";
	public static String PROP_LL_SHARE = "llShare";
	public static String PROP_ORD = "ord";


	// constructors
	public BaseCurriculumCourse () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCurriculumCourse (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCurriculumCourse (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CurriculumClassification classification,
		org.unitime.timetable.model.CourseOffering course,
		java.lang.Float percShare,
		java.lang.Integer ord) {

		this.setUniqueId(uniqueId);
		this.setClassification(classification);
		this.setCourse(course);
		this.setPercShare(percShare);
		this.setOrd(ord);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Float percShare;
	private java.lang.Float llShare;
	private java.lang.Integer ord;

	// many to one
	private org.unitime.timetable.model.CurriculumClassification classification;
	private org.unitime.timetable.model.CourseOffering course;

	// collections
	private java.util.Set groups;


	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="org.unitime.commons.hibernate.id.UniqueIdGenerator"
     *  column="uniqueid"
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
	 * Return the value associated with the column: pr_share
	 */
	public java.lang.Float getPercShare () {
		return percShare;
	}

	/**
	 * Set the value related to the column: pr_share
	 * @param percShare the pr_share value
	 */
	public void setPercShare (java.lang.Float percShare) {
		this.percShare = percShare;
	}



	/**
	 * Return the value associated with the column: ll_share
	 */
	public java.lang.Float getLlShare () {
		return llShare;
	}

	/**
	 * Set the value related to the column: ll_share
	 * @param llShare the ll_share value
	 */
	public void setLlShare (java.lang.Float llShare) {
		this.llShare = llShare;
	}



	/**
	 * Return the value associated with the column: ord
	 */
	public java.lang.Integer getOrd () {
		return ord;
	}

	/**
	 * Set the value related to the column: ord
	 * @param ord the ord value
	 */
	public void setOrd (java.lang.Integer ord) {
		this.ord = ord;
	}



	/**
	 * Return the value associated with the column: cur_clasf_id
	 */
	public org.unitime.timetable.model.CurriculumClassification getClassification () {
		return classification;
	}

	/**
	 * Set the value related to the column: cur_clasf_id
	 * @param classification the cur_clasf_id value
	 */
	public void setClassification (org.unitime.timetable.model.CurriculumClassification classification) {
		this.classification = classification;
	}



	/**
	 * Return the value associated with the column: course_id
	 */
	public org.unitime.timetable.model.CourseOffering getCourse () {
		return course;
	}

	/**
	 * Set the value related to the column: course_id
	 * @param course the course_id value
	 */
	public void setCourse (org.unitime.timetable.model.CourseOffering course) {
		this.course = course;
	}



	/**
	 * Return the value associated with the column: groups
	 */
	public java.util.Set getGroups () {
		return groups;
	}

	/**
	 * Set the value related to the column: instructors
	 * @param courses the courses value
	 */
	public void setGroups (java.util.Set groups) {
		this.groups = groups;
	}

	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CurriculumCourse)) return false;
		else {
			org.unitime.timetable.model.CurriculumCourse curriculumCourse = (org.unitime.timetable.model.CurriculumCourse) obj;
			if (null == this.getUniqueId() || null == curriculumCourse.getUniqueId()) return false;
			else return (this.getUniqueId().equals(curriculumCourse.getUniqueId()));
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