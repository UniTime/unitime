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

import java.util.Collection;
import java.util.Set;

/**
 *
 * XDoclet definition:
 * @struts:form name="roomFeatureEditForm"
 */
public class DepartmentRoomFeatureEditForm extends RoomFeatureEditForm {
	String deptCode = null;
	String label = null;
	Integer uniqueId = null;
	Collection rooms = null;

	/**
	 * @return
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return
	 */
	public String getDeptCode() {
		return deptCode;
	}
	/**
	 * @return
	 */
	public Collection getRooms() {
		return rooms;
	}
	/**
	 * @return
	 */
	public Integer getUniqueId() {
		return uniqueId;
	}
	/**
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @param owner
	 */
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	/**
	 * @param rooms
	 */
	public void setRooms(Set rooms) {
		this.rooms = rooms;
	}
	/**
	 * @param uniqueId
	 */
	public void setUniqueId(Integer uniqueId) {
		this.uniqueId = uniqueId;
	}
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3689069555917796146L;
}
