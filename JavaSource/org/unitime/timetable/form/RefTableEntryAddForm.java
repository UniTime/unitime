/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
