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
package org.unitime.timetable.model.dao;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
import java.util.List;
import org.unitime.timetable.model.RoomDept;

public class RoomDeptDAO extends _RootDAO<RoomDept,Long> {
	private static RoomDeptDAO sInstance;

	public RoomDeptDAO() {}

	public static RoomDeptDAO getInstance() {
		if (sInstance == null) sInstance = new RoomDeptDAO();
		return sInstance;
	}

	public Class<RoomDept> getReferenceClass() {
		return RoomDept.class;
	}

	@SuppressWarnings("unchecked")
	public List<RoomDept> findByRoom(org.hibernate.Session hibSession, Long roomId) {
		return hibSession.createQuery("from RoomDept x where x.room.uniqueId = :roomId", RoomDept.class).setParameter("roomId", roomId).list();
	}

	@SuppressWarnings("unchecked")
	public List<RoomDept> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from RoomDept x where x.department.uniqueId = :departmentId", RoomDept.class).setParameter("departmentId", departmentId).list();
	}
}
