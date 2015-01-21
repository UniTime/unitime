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

/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:form name="refTableEntryForm"
 */

/**
 * @author Tomas Muller
 */
public abstract class RefTableEntryAddForm extends ActionForm {

	private static final long serialVersionUID = -6654932547373433298L;

	// --------------------------------------------------------- Instance Variables

	/** reference property */
	private String reference;

	/** label property */
	private String label;


	// --------------------------------------------------------- Methods

	/** 
	 * Returns the reference.
	 * @return String
	 */
	public String getReference() {
		return reference;
	}

	/** 
	 * Set the reference.
	 * @param reference The reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/** 
	 * Returns the label.
	 * @return String
	 */
	public String getLabel() {
		return label;
	}

	/** 
	 * Set the label.
	 * @param label The label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}
