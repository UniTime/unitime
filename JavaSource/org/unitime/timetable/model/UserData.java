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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseUserData;
import org.unitime.timetable.model.dao.UserDataDAO;




/**
 * @author Tomas Muller
 */
public class UserData extends BaseUserData {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UserData () {
		super();
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public UserData(String externalUniqueId, String name) {
		setExternalUniqueId(externalUniqueId);
		setName(name);
		initialize();
	}

	public static void setProperty(String externalUniqueId, String name, String value) {
		try {
			UserDataDAO dao = new UserDataDAO();
			UserData userData = dao.get(new UserData(externalUniqueId, name));
			if (value!=null && value.length()==0) value=null;
			if (userData==null && value==null) return;
			if (userData!=null && value!=null && value.equals(userData.getValue())) return;
			if (userData==null) {
				userData = new UserData(externalUniqueId, name);
			}
			userData.setValue(value);
			if (value==null)
				dao.delete(userData);
			else
				dao.saveOrUpdate(userData);
		} catch (Exception e) {
			Debug.warning("Failed to set user property " + name + ":=" + value + " (" + e.getMessage() + ")");
		}
	}
	
	public static String getProperty(String externalUniqueId, String name) {
		UserDataDAO dao = new UserDataDAO();
		UserData userData = dao.get(new UserData(externalUniqueId, name));
		return (userData==null?null:userData.getValue());
	}
	
	public static String getProperty(String externalUniqueId, String name, String defaultValue) {
		String value = getProperty(externalUniqueId, name);
		return (value!=null?value:defaultValue);
	}

	public static void removeProperty(String externalUniqueId, String name) {
		setProperty(externalUniqueId, name, null);
	}
	
	public static HashMap<String,String> getProperties(String externalUniqueId, Collection<String> names) {
		String q = "select u from UserData u where u.externalUniqueId = :externalUniqueId and u.name in (";
		for (Iterator<String> i = names.iterator(); i.hasNext(); ) {
			q += "'" + i.next() + "'";
			if (i.hasNext()) q += ",";
		}
		q += ")";
		HashMap<String,String> ret = new HashMap<String, String>();
		for (UserData u: (List<UserData>)UserDataDAO.getInstance().getSession().createQuery(q).setString("externalUniqueId", externalUniqueId).setCacheable(true).list()) {
			ret.put(u.getName(), u.getValue());
		}
		return ret;
	}
	
	public static HashMap<String,String> getProperties(String externalUniqueId) {
		String q = "select u from UserData u where u.externalUniqueId = :externalUniqueId";
		HashMap<String,String> ret = new HashMap<String, String>();
		for (UserData u: (List<UserData>)UserDataDAO.getInstance().getSession().createQuery(q).setString("externalUniqueId", externalUniqueId).setCacheable(true).list()) {
			ret.put(u.getName(), u.getValue());
		}
		return ret;
	}

	public static boolean getPropertyBoolean(String externalUniqueId, String name, boolean defaultValue) {
		String value = getProperty(externalUniqueId, name);
		return (value!=null?"1".equals(value):defaultValue);
	}
}
