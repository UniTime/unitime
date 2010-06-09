package org.unitime.timetable.model.base;

import java.io.Serializable;

public abstract class BaseCurriculumCourseGroup implements Serializable {
	
	public static String REF = "BaseCurriculumCourseGroup";

	public static String PROP_NAME = "name";
	public static String PROP_COLOR = "color";
	public static String PROP_TYPE = "type";


	// constructors
	public BaseCurriculumCourseGroup () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCurriculumCourseGroup (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCurriculumCourseGroup (
		java.lang.Long uniqueId,
		java.lang.String name,
		java.lang.Integer type, 
		org.unitime.timetable.model.Curriculum curriculum) {

		this.setUniqueId(uniqueId);
		this.setName(name);
		this.setType(type);
		this.setCurriculum(curriculum);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String color;
	private java.lang.Integer type;

	// many to one
	private org.unitime.timetable.model.Curriculum curriculum;

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
	 * Return the value associated with the column: name
	 */
	public java.lang.String getName () {
		return name;
	}

	/**
	 * Set the value related to the column: name
	 * @param name the name value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
	}



	/**
	 * Return the value associated with the column: color
	 */
	public java.lang.String getColor () {
		return color;
	}

	/**
	 * Set the value related to the column: color
	 * @param color the color value
	 */
	public void setColor (java.lang.String color) {
		this.color = color;
	}



	/**
	 * Return the value associated with the column: type
	 */
	public java.lang.Integer getType () {
		return type;
	}

	/**
	 * Set the value related to the column: type
	 * @param type the type value
	 */
	public void setType (java.lang.Integer type) {
		this.type = type;
	}

	/**
	 * Return the value associated with the column: curricula_id
	 */
	public org.unitime.timetable.model.Curriculum getCurriculum () {
		return curriculum;
	}

	/**
	 * Set the value related to the column: curricula_id
	 * @param curriculum the curricula_id value
	 */
	public void setCurriculum (org.unitime.timetable.model.Curriculum curriculum) {
		this.curriculum = curriculum;
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
