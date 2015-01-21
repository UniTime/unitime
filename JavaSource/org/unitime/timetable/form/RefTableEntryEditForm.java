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
import org.unitime.timetable.model.RefTableEntry;


/** 
* MyEclipse Struts
* Creation date: 02-18-2005
* 
* XDoclet definition:
* @struts:form name="refTableEntryEditForm"
*/
/**
 * @author Tomas Muller
 */
public abstract class RefTableEntryEditForm extends ActionForm {

	private static final long serialVersionUID = 3604015148650913385L;

	// --------------------------------------------------------- Instance Variables
	RefTableEntry refTableEntry;
	// --------------------------------------------------------- Methods

	/**
	 * @return Returns the refTableEntry.
	 */
	public RefTableEntry getRefTableEntry() {
		return refTableEntry;
	}
	/**
	 * @param refTableEntry The refTableEntry to set.
	 */
	public void setRefTableEntry(RefTableEntry refTableEntry) {
		this.refTableEntry = refTableEntry;
	}
	
	public boolean equals(Object arg0) {
		return refTableEntry.equals(arg0);
	}
	
	public String getReference() {
		return refTableEntry.getReference();
	}
	
	public String getLabel() {
		return refTableEntry.getLabel();
	}
	
	public int hashCode() {
		return refTableEntry.hashCode();
	}
	
	public void setReference(String reference) {
		refTableEntry.setReference(reference);
	}
	public void setLabel(String label) {
		refTableEntry.setLabel(label);
	}
	public String toString() {
		return refTableEntry.toString();
	}
	
	
	/**
	 * @return
	 */
	public Long getUniqueId() {
		return refTableEntry.getUniqueId();
	}
	/**
	 * @param refTableEntryId
	 */
	public void setUniqueId(Long uniqueId) {
		refTableEntry.setUniqueId(uniqueId);
	}
}
