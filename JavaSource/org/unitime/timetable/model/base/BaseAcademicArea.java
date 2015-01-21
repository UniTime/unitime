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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAcademicArea implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iAcademicAreaAbbreviation;
	private String iTitle;

	private Session iSession;
	private Set<PosMajor> iPosMajors;
	private Set<PosMinor> iPosMinors;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_ACADEMIC_AREA_ABBREVIATION = "academicAreaAbbreviation";
	public static String PROP_LONG_TITLE = "title";

	public BaseAcademicArea() {
		initialize();
	}

	public BaseAcademicArea(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getAcademicAreaAbbreviation() { return iAcademicAreaAbbreviation; }
	public void setAcademicAreaAbbreviation(String academicAreaAbbreviation) { iAcademicAreaAbbreviation = academicAreaAbbreviation; }

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<PosMajor> getPosMajors() { return iPosMajors; }
	public void setPosMajors(Set<PosMajor> posMajors) { iPosMajors = posMajors; }
	public void addToposMajors(PosMajor posMajor) {
		if (iPosMajors == null) iPosMajors = new HashSet<PosMajor>();
		iPosMajors.add(posMajor);
	}

	public Set<PosMinor> getPosMinors() { return iPosMinors; }
	public void setPosMinors(Set<PosMinor> posMinors) { iPosMinors = posMinors; }
	public void addToposMinors(PosMinor posMinor) {
		if (iPosMinors == null) iPosMinors = new HashSet<PosMinor>();
		iPosMinors.add(posMinor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof AcademicArea)) return false;
		if (getUniqueId() == null || ((AcademicArea)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AcademicArea)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "AcademicArea["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AcademicArea[" +
			"\n	AcademicAreaAbbreviation: " + getAcademicAreaAbbreviation() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Session: " + getSession() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
