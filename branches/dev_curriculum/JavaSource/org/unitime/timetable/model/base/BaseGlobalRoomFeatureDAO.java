/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;

public abstract class BaseGlobalRoomFeatureDAO extends _RootDAO<GlobalRoomFeature,Long> {

	private static GlobalRoomFeatureDAO sInstance;

	public static GlobalRoomFeatureDAO getInstance() {
		if (sInstance == null) sInstance = new GlobalRoomFeatureDAO();
		return sInstance;
	}

	public Class<GlobalRoomFeature> getReferenceClass() {
		return GlobalRoomFeature.class;
	}
}
