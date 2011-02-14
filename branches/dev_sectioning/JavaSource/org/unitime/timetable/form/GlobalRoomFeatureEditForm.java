/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Collection;
import java.util.Set;

import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;


/** 
 * 
 * XDoclet definition:
 * @struts:form name="roomFeatureEditForm"
 */
public class GlobalRoomFeatureEditForm extends RoomFeatureEditForm {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3258125843296629559L;
	// --------------------------------------------------------- Instance Variables
	GlobalRoomFeature roomFeature = new GlobalRoomFeature();
	// --------------------------------------------------------- Methods

	/**
	 * @return
	 */
	public String getLabel() {
		return roomFeature.getLabel();
	}
	/**
	 * @return
	 */
	public Collection getRooms() {
		return roomFeature.getRooms();
	}
	/**
	 * @return
	 */
	public Long getUniqueId() {
		return roomFeature.getUniqueId();
	}
	/**
	 * @param label
	 */
	public void setLabel(String label) {
		roomFeature.setLabel(label);
	}
	/**
	 * @param rooms
	 */
	public void setRooms(Set rooms) {
		roomFeature.setRooms(rooms);
	}
	/**
	 * @param uniqueId
	 */
	public void setUniqueId(Long uniqueId) {
		roomFeature.setUniqueId(uniqueId);
	}
	/**
	 * @return
	 */
	public String getSisReference() {
		return roomFeature.getSisReference();
	}
	/**
	 * @return
	 */
	public String getSisValue() {
		return roomFeature.getSisValue();
	}
	/**
	 * @param sisReference
	 */
	public void setSisReference(String sisReference) {
		roomFeature.setSisReference(sisReference);
	}
	/**
	 * @param sisValue
	 */
	public void setSisValue(String sisValue) {
		roomFeature.setSisValue(sisValue);
	}
	/**
	 * @return Returns the roomFeature.
	 */
	public RoomFeature getRoomFeature() {
		return (RoomFeature) roomFeature;
	}
	/**
	 * @param roomFeature The roomFeature to set.
	 */
	public void setRoomFeature(RoomFeature roomFeature) {
		this.roomFeature = (GlobalRoomFeature) roomFeature;
	}
}
