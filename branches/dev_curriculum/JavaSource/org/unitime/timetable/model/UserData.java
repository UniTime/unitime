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
package org.unitime.timetable.model;

import javax.servlet.http.HttpSession;

import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.base.BaseUserData;
import org.unitime.timetable.model.dao.UserDataDAO;




public class UserData extends BaseUserData {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public UserData () {
		super();
	}

	/**
	 * Constructor for primary name
	 */
	public UserData (
		java.lang.String externalUniqueId,
		java.lang.String name) {

		super (
			externalUniqueId,
			name);
	}

	/**
	 * Constructor for required fields
	 */
	public UserData (
		java.lang.String externalUniqueId,
		java.lang.String name,
		java.lang.String value) {

		super (
			externalUniqueId,
			name,
			value);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static void setProperty(String externalUniqueId, String name, String value) {
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
	
	public static void setProperty(HttpSession session, String name, String value) {
		User user = Web.getUser(session);
		if (user==null || user.getId()==null) return;
		setProperty(user.getId(),name,value);
	}

	public static String getProperty(HttpSession session, String name) {
		User user = Web.getUser(session);
		if (user==null || user.getId()==null) return null;
		return getProperty(user.getId(),name);
	}
	
	public static String getProperty(HttpSession session, String name, String defaultValue) {
		String value = getProperty(session, name);
		return (value!=null?value:defaultValue);
	}

	public static void removeProperty(HttpSession session, String name) {
		setProperty(session, name, null);
	}
	
	public static int getPropertyInt(HttpSession session, String name, int defaultValue) {
		String value = getProperty(session, name);
		return (value!=null?Integer.parseInt(value):defaultValue);
	}
	
	public static void setPropertyInt(HttpSession session, String name, int value) {
		setProperty(session, name, String.valueOf(value));
	}

	public static double getPropertyDouble(HttpSession session, String name, double defaultValue) {
		String value = getProperty(session, name);
		return (value!=null?Double.parseDouble(value):defaultValue);
	}
	
	public static void setPropertyDouble(HttpSession session, String name, double value) {
		setProperty(session, name, String.valueOf(value));
	}

	public static boolean getPropertyBoolean(HttpSession session, String name, boolean defaultValue) {
		String value = getProperty(session, name);
		return (value!=null?"1".equals(value):defaultValue);
	}
	
	public static boolean getPropertyBoolean(String externalUniqueId, String name, boolean defaultValue) {
		String value = getProperty(externalUniqueId, name);
		return (value!=null?"1".equals(value):defaultValue);
	}

	public static void setPropertyBoolean(HttpSession session, String name, boolean value) {
		setProperty(session, name, (value?"1":"0"));
	}
}
