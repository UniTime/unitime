/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */
@RemoteServiceRelativePath("menu.gwt")
public interface MenuService extends RemoteService {
	public List<MenuInterface> getMenu() throws MenuException;
	public HashMap<String, String> getUserInfo() throws MenuException;
	public HashMap<String, String> getSessionInfo() throws MenuException;
	public String getVersion() throws MenuException;
	public HashMap<String, String> getSolverInfo(boolean includeSolutionInfo) throws MenuException;
	
	public String[] getHelpPageAndLocalizedTitle(String title) throws MenuException;
	
	public String getUserData(String property) throws MenuException;
	public Boolean setUserData(String property, String value) throws MenuException;
	public HashMap<String, String> getUserData(Collection<String> property) throws MenuException;
	public Boolean setUserData(List<String[]> property2value) throws MenuException;
}
