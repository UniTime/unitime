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
package org.unitime.timetable.form;

import org.apache.struts.action.ActionForm;
import org.unitime.timetable.model.SolverParameterDef;


/** 
 * @author Tomas Muller
 */
public class SolverParameterDefEditForm extends ActionForm {

	private static final long serialVersionUID = 7546894309851294183L;

	// --------------------------------------------------------- Instance Variables

	/** solverParameterDef property */
	private SolverParameterDef solverParameterDef = new SolverParameterDef();;

	// --------------------------------------------------------- Methods

	/** 
	 * Returns the solverParameterDef.
	 * @return SolverParameterDef
	 */
	public SolverParameterDef getSolverParameterDef() {
		return solverParameterDef;
	}

	/** 
	 * Set the solverParameterDef.
	 * @param solverParameterDef The solverParameterDef to set
	 */
	public void setSolverParameterDef(SolverParameterDef solverParameterDef) {
		this.solverParameterDef = solverParameterDef;
	}
	
	/**
	 * 
	 * @return
	 */
	public Long getUniqueId() {
		return solverParameterDef.getUniqueId();
	}

	/**
	 * 
	 * @param uniqueId
	 */
	public void setUniqueId(Long uniqueId) {
		solverParameterDef.setUniqueId(uniqueId);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getKey() {
		return solverParameterDef.getName();
	}

	/**
	 * 
	 * @param key
	 */
	public void setKey(String key) {
		solverParameterDef.setName(key);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDesc() {
		return solverParameterDef.getDescription();
	}

	/**
	 * 
	 * @param desc
	 */
	public void setDesc(String desc) {
		solverParameterDef.setDescription(desc);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getDefaultValue() {
		return solverParameterDef.getDefault();
	}

	/**
	 * 
	 * @param DefaultValue
	 */
	public void setDefaultValue(String DefaultValue) {
		solverParameterDef.setDefault(DefaultValue);
	}

}

