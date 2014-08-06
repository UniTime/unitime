/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SessionConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSessionConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Session iSession;
	private String iKey;
	private String iValue;
	private String iDescription;


	public static String PROP_VALUE = "value";
	public static String PROP_DESCRIPTION = "description";

	public BaseSessionConfig() {
		initialize();
	}

	protected void initialize() {}

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SessionConfig)) return false;
		SessionConfig sessionConfig = (SessionConfig)o;
		if (getSession() == null || sessionConfig.getSession() == null || !getSession().equals(sessionConfig.getSession())) return false;
		if (getKey() == null || sessionConfig.getKey() == null || !getKey().equals(sessionConfig.getKey())) return false;
		return true;
	}

	public int hashCode() {
		if (getSession() == null || getKey() == null) return super.hashCode();
		return getSession().hashCode() ^ getKey().hashCode();
	}

	public String toString() {
		return "SessionConfig[" + getSession() + ", " + getKey() + "]";
	}

	public String toDebugString() {
		return "SessionConfig[" +
			"\n	Description: " + getDescription() +
			"\n	Key: " + getKey() +
			"\n	Session: " + getSession() +
			"\n	Value: " + getValue() +
			"]";
	}
}
