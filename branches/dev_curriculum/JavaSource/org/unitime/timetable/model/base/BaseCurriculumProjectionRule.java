package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the curriculum_rule table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="curriculum_rule"
 */

public abstract class BaseCurriculumProjectionRule  implements Serializable {

	public static String REF = "Curriculum";
	public static String PROP_PROJECTION = "projection";


	// constructors
	public BaseCurriculumProjectionRule () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCurriculumProjectionRule (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCurriculumProjectionRule (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.AcademicArea academicArea,
		org.unitime.timetable.model.AcademicClassification academicClassification,
		java.lang.Float projection) {

		this.setUniqueId(uniqueId);
		this.setAcademicArea(academicArea);
		this.setAcademicClassification(academicClassification);
		this.setProjection(projection);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Float projection;

	// many to one
	private org.unitime.timetable.model.AcademicArea academicArea;
	private org.unitime.timetable.model.AcademicClassification academicClassification;
	private org.unitime.timetable.model.PosMajor major;



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
	 * Return the value associated with the column: projection
	 */
	public java.lang.Float getProjection () {
		return projection;
	}

	/**
	 * Set the value related to the column: projection
	 * @param projection the projection value
	 */
	public void setProjection (java.lang.Float projection) {
		this.projection = projection;
	}



	/**
	 * Return the value associated with the column: acad_area_id
	 */
	public org.unitime.timetable.model.AcademicArea getAcademicArea () {
		return academicArea;
	}

	/**
	 * Set the value related to the column: acad_area_id
	 * @param academicArea the acad_area_id value
	 */
	public void setAcademicArea (org.unitime.timetable.model.AcademicArea academicArea) {
		this.academicArea = academicArea;
	}



	/**
	 * Return the value associated with the column: acad_clasf_id
	 */
	public org.unitime.timetable.model.AcademicClassification getAcademicClassification () {
		return academicClassification;
	}

	/**
	 * Set the value related to the column: acad_clasf_id
	 * @param academicClassification the acad_clasf_id value
	 */
	public void setAcademicClassification (org.unitime.timetable.model.AcademicClassification academicClassification) {
		this.academicClassification = academicClassification;
	}
	
	
	/**
	 * Return the value associated with the column: major_id
	 */
	public org.unitime.timetable.model.PosMajor getMajor () {
		return major;
	}

	/**
	 * Set the value related to the column: major_id
	 * @param major the major_id value
	 */
	public void setMajor (org.unitime.timetable.model.PosMajor major) {
		this.major = major;
	}



	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Curriculum)) return false;
		else {
			org.unitime.timetable.model.Curriculum curriculum = (org.unitime.timetable.model.Curriculum) obj;
			if (null == this.getUniqueId() || null == curriculum.getUniqueId()) return false;
			else return (this.getUniqueId().equals(curriculum.getUniqueId()));
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