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

import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverPredefinedSetting;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSolverPredefinedSetting implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iDescription;
	private Integer iAppearance;

	private Set<SolverParameter> iParameters;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_APPEARANCE = "appearance";

	public BaseSolverPredefinedSetting() {
		initialize();
	}

	public BaseSolverPredefinedSetting(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	public Integer getAppearance() { return iAppearance; }
	public void setAppearance(Integer appearance) { iAppearance = appearance; }

	public Set<SolverParameter> getParameters() { return iParameters; }
	public void setParameters(Set<SolverParameter> parameters) { iParameters = parameters; }
	public void addToparameters(SolverParameter solverParameter) {
		if (iParameters == null) iParameters = new HashSet<SolverParameter>();
		iParameters.add(solverParameter);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverPredefinedSetting)) return false;
		if (getUniqueId() == null || ((SolverPredefinedSetting)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverPredefinedSetting)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SolverPredefinedSetting["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverPredefinedSetting[" +
			"\n	Appearance: " + getAppearance() +
			"\n	Description: " + getDescription() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
