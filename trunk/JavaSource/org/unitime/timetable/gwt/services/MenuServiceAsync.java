/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.unitime.timetable.gwt.shared.MenuException;
import org.unitime.timetable.gwt.shared.MenuInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface MenuServiceAsync {
	void getMenu(AsyncCallback<List<MenuInterface>> callback) throws MenuException;
	
	public void getUserInfo(AsyncCallback<HashMap<String, String>> callback) throws MenuException;
	public void getSessionInfo(AsyncCallback<HashMap<String, String>> callback) throws MenuException;
	public void getVersion(AsyncCallback<String> callback) throws MenuException;
	public void getSolverInfo(boolean includeSolutionInfo, AsyncCallback<HashMap<String, String>> callback) throws MenuException;

	public void getHelpPageAndLocalizedTitle(String title, AsyncCallback<String[]> callback) throws MenuException;
	
	public void getUserData(String property, AsyncCallback<String> callback) throws MenuException;
	public void setUserData(String property, String value, AsyncCallback<Boolean> callback) throws MenuException;
	public void getUserData(Collection<String> property, AsyncCallback<HashMap<String, String>> callback) throws MenuException;
	public void setUserData(List<String[]> property2value, AsyncCallback<Boolean> callback) throws MenuException;
}
