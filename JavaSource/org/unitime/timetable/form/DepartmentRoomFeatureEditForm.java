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

/**
 *
 * XDoclet definition:
 * @struts:form name="roomFeatureEditForm"
 *
 * @author Tomas Muller
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
