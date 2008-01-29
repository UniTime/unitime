/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.interfaces;

import java.util.Map;

/**
 * Interface to lookup external ids for manager
 * 
 * @author Heston Fernandes
 * 
 */
public interface ExternalUidLookup {
	
	public final String SEARCH_ID = "searchId";
	public final String EXTERNAL_ID = "externalId";
	public final String USERNAME = "userName";
	public final String FIRST_NAME = "firstName";
	public final String MIDDLE_NAME = "middleName";
	public final String LAST_NAME = "lastName";
	public final String EMAIL = "email";
	
	public Map doLookup(Map attributes) throws Exception;

	public String getErrorMessage();
	
}
