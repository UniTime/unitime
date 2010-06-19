package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the curricula_clasf table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="curricula_clasf"
 */

public abstract class BaseCurriculumClassification  implements Serializable {

	public static String REF = "CurriculumClassification";
	public static String PROP_NAME = "name";
	public static String PROP_NR_STUDENTS = "nrStudents";
	public static String PROP_ORD = "ord";


	// constructors
	public BaseCurriculumClassification () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCurriculumClassification (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCurriculumClassification (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Curriculum curriculum,
		org.unitime.timetable.model.AcademicClassification academicClassification,
		java.lang.String name,
		java.lang.Integer nrStudents,
		java.lang.Integer ord) {

		this.setUniqueId(uniqueId);
		this.setCurriculum(curriculum);
		this.setAcademicClassification(academicClassification);
		this.setName(name);
		this.setNrStudents(nrStudents);
		this.setOrd(ord);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.Integer nrStudents;
	private java.lang.Integer ord;

	// many to one
	private org.unitime.timetable.model.Curriculum curriculum;
	private org.unitime.timetable.model.AcademicClassification academicClassification;

	// collections
	private java.util.Set courses;



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
	 * Return the value associated with the column: nr_students
	 */
	public java.lang.Integer getNrStudents () {
		return nrStudents;
	}

	/**
	 * Set the value related to the column: nr_students
	 * @param nrStudents the nr_students value
	 */
	public void setNrStudents (java.lang.Integer nrStudents) {
		this.nrStudents = nrStudents;
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
	 * Return the value associated with the column: courses
	 */
	public java.util.Set getCourses () {
		return courses;
	}

	/**
	 * Set the value related to the column: courses
	 * @param courses the courses value
	 */
	public void setCourses (java.util.Set courses) {
		this.courses = courses;
	}

	public void addTocourses (org.unitime.timetable.model.CurriculumCourse curriculumCourse) {
		if (null == getCourses()) setCourses(new java.util.HashSet());
		getCourses().add(curriculumCourse);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CurriculumClassification)) return false;
		else {
			org.unitime.timetable.model.CurriculumClassification curriculumClassification = (org.unitime.timetable.model.CurriculumClassification) obj;
			if (null == this.getUniqueId() || null == curriculumClassification.getUniqueId()) return false;
			else return (this.getUniqueId().equals(curriculumClassification.getUniqueId()));
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