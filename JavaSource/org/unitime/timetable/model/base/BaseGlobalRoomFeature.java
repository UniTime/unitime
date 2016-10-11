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

import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseGlobalRoomFeature extends RoomFeature implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iSisReference;
	private String iSisValue;

	private Session iSession;

	public static String PROP_SIS_REFERENCE = "sisReference";
	public static String PROP_SIS_VALUE = "sisValue";

	public BaseGlobalRoomFeature() {
		initialize();
	}

	public BaseGlobalRoomFeature(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getSisReference() { return iSisReference; }
	public void setSisReference(String sisReference) { iSisReference = sisReference; }

	public String getSisValue() { return iSisValue; }
	public void setSisValue(String sisValue) { iSisValue = sisValue; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof GlobalRoomFeature)) return false;
		if (getUniqueId() == null || ((GlobalRoomFeature)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((GlobalRoomFeature)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "GlobalRoomFeature["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "GlobalRoomFeature[" +
			"\n	Abbv: " + getAbbv() +
			"\n	Description: " + getDescription() +
			"\n	FeatureType: " + getFeatureType() +
			"\n	Label: " + getLabel() +
			"\n	Session: " + getSession() +
			"\n	SisReference: " + getSisReference() +
			"\n	SisValue: " + getSisValue() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
