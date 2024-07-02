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
package org.unitime.timetable.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomUrlProvider;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;

/**
 * @author Tomas Muller
 */
public class DefaultRoomUrlProvider implements RoomUrlProvider {
	private static final long serialVersionUID = 8009417407983850965L;
	private transient ExternalTermProvider iExternalTermProvider = null;
	
	protected String replaceExternal(String url, Session session) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, UnsupportedEncodingException {
		if (url == null) return null;
		if (url.contains(":xterm") || url.contains(":xcampus")) {
			if (iExternalTermProvider == null) {
				String clazz = ApplicationProperty.CustomizationExternalTerm.value();
				if (clazz == null || clazz.isEmpty()) return url;
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
			}
			AcademicSessionInfo info = new AcademicSessionInfo(session);
			return replace(url,
					":xterm", iExternalTermProvider.getExternalTerm(info),
					":xcampus", iExternalTermProvider.getExternalCampus(info)
					);
		} else {
			return url;
		}
	}
	
	protected String replaceParameter(String url, String parameter, String value) throws UnsupportedEncodingException {
		if (url == null) return null;
		if (url.contains(parameter)) {
			if (value == null) return null;
			return url.replace(parameter, URLEncoder.encode(value, "utf-8"));
		}
		return url;
	}
	
	protected String replace(String url, String... params) throws UnsupportedEncodingException {
		for (int i = 0; i < params.length && url != null; i+= 2) {
			url = replaceParameter(url, params[i], params[i+1]);
		}
		return url;
	}

	@Override
	public String getRoomUrl(Location location) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException, UnsupportedEncodingException {
		if (location instanceof Room) {
			String url = ApplicationProperty.DefaultRoomUrlRoom.value();
			if (url == null || url.isEmpty()) return null;
			Room room = (Room)location;
			Session session = room.getSession();
			return replaceExternal(replace(url,
					":year", session.getAcademicYear(),
					":term", session.getAcademicTerm(),
					":campus", session.getAcademicInitiative(),
					":building", room.getBuildingAbbv(),
					":roomNbr", room.getRoomNumber(),
					":name", room.getLabel(),
					":roomId", room.getExternalUniqueId(),
					":id", room.getExternalUniqueId(),
					":buildingId", room.getBuilding().getExternalUniqueId()), session);
		} else if (location instanceof NonUniversityLocation) {
			String url = ApplicationProperty.DefaultRoomUrlLoncation.value();
			if (url == null || url.isEmpty()) return null;
			NonUniversityLocation room = (NonUniversityLocation)location;
			Session session = room.getSession();
			return replaceExternal(replace(url,
					":year", session.getAcademicYear(),
					":term", session.getAcademicTerm(),
					":campus", session.getAcademicInitiative(),
					":name", room.getLabel(),
					":id", room.getExternalUniqueId()), session);
		} else {
			return null;
		}
	}

}
