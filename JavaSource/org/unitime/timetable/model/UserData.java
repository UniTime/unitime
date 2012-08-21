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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseUserData;
import org.unitime.timetable.model.dao.UserDataDAO;




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
