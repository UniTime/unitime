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
package org.unitime.timetable.gwt.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.unitime.timetable.gwt.shared.MenuException;
import org.unitime.timetable.gwt.shared.MenuInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MenuServiceAsync {
	void getMenu(AsyncCallback<List<MenuInterface>> callback) throws MenuException;
	
	public void getUserInfo(AsyncCallback<HashMap<String, String>> callback) throws MenuException;
	public void getSessionInfo(AsyncCallback<HashMap<String, String>> callback) throws MenuException;
	public void getVersion(AsyncCallback<String> callback) throws MenuException;
	public void getSolverInfo(AsyncCallback<HashMap<String, String>> callback) throws MenuException;

	public void getHelpPage(String title, AsyncCallback<String> callback) throws MenuException;
	
	public void getUserData(String property, AsyncCallback<String> callback) throws MenuException;
	public void setUserData(String property, String value, AsyncCallback<Boolean> callback) throws MenuException;
	public void getUserData(Collection<String> property, AsyncCallback<HashMap<String, String>> callback) throws MenuException;
	public void setUserData(List<String[]> property2value, AsyncCallback<Boolean> callback) throws MenuException;
}
