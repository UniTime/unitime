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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.Settings;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSettings implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iKey;
	private String iDefaultValue;
	private String iAllowedValues;
	private String iDescription;

	private Set<ManagerSettings> iManagerSettings;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "key";
	public static String PROP_DEFAULT_VALUE = "defaultValue";
	public static String PROP_ALLOWED_VALUES = "allowedValues";
	public static String PROP_DESCRIPTION = "description";

	public BaseSettings() {
		initialize();
	}

	public BaseSettings(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getKey() { return iKey; }
	public void setKey(String key) { iKey = key; }

	public String getDefaultValue() { return iDefaultValue; }
	public void setDefaultValue(String defaultValue) { iDefaultValue = defaultValue; }

	public String getAllowedValues() { return iAllowedValues; }
	public void setAllowedValues(String allowedValues) { iAllowedValues = allowedValues; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public Set<ManagerSettings> getManagerSettings() { return iManagerSettings; }
	public void setManagerSettings(Set<ManagerSettings> managerSettings) { iManagerSettings = managerSettings; }
	public void addTomanagerSettings(ManagerSettings managerSettings) {
		if (iManagerSettings == null) iManagerSettings = new HashSet<ManagerSettings>();
		iManagerSettings.add(managerSettings);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Settings)) return false;
		if (getUniqueId() == null || ((Settings)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Settings)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Settings["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Settings[" +
			"\n	AllowedValues: " + getAllowedValues() +
			"\n	DefaultValue: " + getDefaultValue() +
			"\n	Description: " + getDescription() +
			"\n	Key: " + getKey() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
