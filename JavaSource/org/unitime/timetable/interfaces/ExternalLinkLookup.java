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
package org.unitime.timetable.interfaces;

import java.util.Map;

/**
 * Interface to generate external links
 * 
 * @author Heston Fernandes
 */
public interface ExternalLinkLookup {
	
	/** Attribute for the link label */
	public final String LINK_LABEL = "label";

	/** Attribute for the link location */
	public final String LINK_LOCATION = "href";
	
	/**
	 * Generate the link based on the attributes of the object
	 * @param obj object whose attributes may be used in constructing the link
	 * @return Map object containing two elements LINK_LABEL and LINK LOCATION
	 */
	public Map getLink(Object obj) throws Exception;
	
	/**
	 * Sets the error message (if any)
	 * @return
	 */
	public String getErrorMessage();

}
