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
package org.unitime.timetable.util;

/**
 *
 * @author Tomas Muller
 *
 */
public class IdValue {
	private Long iId;
	private String iValue;
	private String iType;
	private boolean iEnabled;
	public IdValue(Long id, String value) {
		this(id,value,null,true);
	}
	public IdValue(Long id, String value, String type) {
		this(id,value,type,true);
	}
	public IdValue(Long id, String value, String type, boolean enabled) {
		iId = id; iValue = value; iType = type; iEnabled = enabled;
	}
	public Long getId() { return iId; }
	public String getValue() { return iValue; }
	public String getType() { return iType;}
	public boolean getEnabled() { return iEnabled; }
	public boolean getDisabled() { return !iEnabled; }
}	
