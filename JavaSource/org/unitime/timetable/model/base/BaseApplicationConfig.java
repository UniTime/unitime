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

import org.unitime.timetable.model.ApplicationConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseApplicationConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iKey;
	private String iValue;
	private String iDescription;


	public static String PROP_NAME = "key";
	public static String PROP_VALUE = "value";
	public static String PROP_DESCRIPTION = "description";

	public BaseApplicationConfig() {
		initialize();
	}

	public BaseApplicationConfig(String key) {
		setKey(key);
		initialize();
	}

	protected void initialize() {}

	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }

	public String getValue() { return iValue; }
	public void setValue(String value) { iValue = value; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ApplicationConfig)) return false;
		if (getKey() == null || ((ApplicationConfig)o).getKey() == null) return false;
		return getKey().equals(((ApplicationConfig)o).getKey());
	}

	public int hashCode() {
		if (getKey() == null) return super.hashCode();
		return getKey().hashCode();
	}

	public String toString() {
		return "ApplicationConfig["+getKey()+"]";
	}

	public String toDebugString() {
		return "ApplicationConfig[" +
			"\n	Description: " + getDescription() +
			"\n	Key: " + getKey() +
			"\n	Value: " + getValue() +
			"]";
	}
}
