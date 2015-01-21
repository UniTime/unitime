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

import java.util.Collection;
import java.util.Set;

import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.RoomFeature;


/** 
 * 
 * XDoclet definition:
 * @struts:form name="roomFeatureEditForm"
 *
 * @author Tomas Muller
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
