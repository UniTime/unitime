package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the curricula table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="curricula"
 */

public abstract class BaseCurricula  implements Serializable {

	public static String REF = "Curricula";
	public static String PROP_ABBV = "abbv";
	public static String PROP_NAME = "name";


	// constructors
	public BaseCurricula () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCurricula (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCurricula (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Department department,
		java.lang.String abbv,
		java.lang.String name) {

		this.setUniqueId(uniqueId);
		this.setDepartment(department);
		this.setAbbv(abbv);
		this.setName(name);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String abbv;
	private java.lang.String name;

	// many to one
	private org.unitime.timetable.model.AcademicArea academicArea;
	private org.unitime.timetable.model.Department department;

	// collections
	private java.util.Set classifications;



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
	 * Return the value associated with the column: abbv
	 */
	public java.lang.String getAbbv () {
		return abbv;
	}

	/**
	 * Set the value related to the column: abbv
	 * @param abbv the abbv value
	 */
	public void setAbbv (java.lang.String abbv) {
		this.abbv = abbv;
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
	 * Return the value associated with the column: dept_id
	 */
	public org.unitime.timetable.model.Department getDepartment () {
		return department;
	}

	/**
	 * Set the value related to the column: dept_id
	 * @param department the dept_id value
	 */
	public void setDepartment (org.unitime.timetable.model.Department department) {
		this.department = department;
	}



	/**
	 * Return the value associated with the column: classifications
	 */
	public java.util.Set getClassifications () {
		return classifications;
	}

	/**
	 * Set the value related to the column: classifications
	 * @param classifications the classifications value
	 */
	public void setClassifications (java.util.Set classifications) {
		this.classifications = classifications;
	}

	public void addToclassifications (org.unitime.timetable.model.CurriculaClassification curriculaClassification) {
		if (null == getClassifications()) setClassifications(new java.util.HashSet());
		getClassifications().add(curriculaClassification);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Curricula)) return false;
		else {
			org.unitime.timetable.model.Curricula curricula = (org.unitime.timetable.model.Curricula) obj;
			if (null == this.getUniqueId() || null == curricula.getUniqueId()) return false;
			else return (this.getUniqueId().equals(curricula.getUniqueId()));
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