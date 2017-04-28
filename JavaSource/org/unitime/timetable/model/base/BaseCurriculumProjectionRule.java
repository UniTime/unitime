/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.PosMajor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculumProjectionRule implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Float iProjection;
	private Float iSnapshotProjection;
	private Date iSnapshotProjectedDate;

	private AcademicArea iAcademicArea;
	private PosMajor iMajor;
	private AcademicClassification iAcademicClassification;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PROJECTION = "projection";
	public static String PROP_SNAPSHOT_PROJ = "snapshotProjection";
	public static String PROP_SNAPSHOT_PROJ_DATE = "snapshotProjectedDate";

	public BaseCurriculumProjectionRule() {
		initialize();
	}

	public BaseCurriculumProjectionRule(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Float getProjection() { return iProjection; }
	public void setProjection(Float projection) { iProjection = projection; }

	public Float getSnapshotProjection() { return iSnapshotProjection; }
	public void setSnapshotProjection(Float snapshotProjection) { iSnapshotProjection = snapshotProjection; }

	public Date getSnapshotProjectedDate() { return iSnapshotProjectedDate; }
	public void setSnapshotProjectedDate(Date snapshotProjectedDate) { iSnapshotProjectedDate = snapshotProjectedDate; }

	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	public PosMajor getMajor() { return iMajor; }
	public void setMajor(PosMajor major) { iMajor = major; }

	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumProjectionRule)) return false;
		if (getUniqueId() == null || ((CurriculumProjectionRule)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumProjectionRule)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CurriculumProjectionRule["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CurriculumProjectionRule[" +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Major: " + getMajor() +
			"\n	Projection: " + getProjection() +
			"\n	SnapshotProjectedDate: " + getSnapshotProjectedDate() +
			"\n	SnapshotProjection: " + getSnapshotProjection() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
