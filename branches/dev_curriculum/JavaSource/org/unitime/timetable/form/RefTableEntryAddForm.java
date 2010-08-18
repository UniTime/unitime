/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
