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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MenuInterface implements IsSerializable {
	private String iName = null;
	private String iTitle = null;
	private String iPage = null;
	private String iTarget = null;
	private boolean iGWT = false;
	private List<MenuInterface> iSubMenus = null;

	public MenuInterface() {}
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }
	public String getPage() { return iPage; }
	public void setPage(String page) { iPage = page; }
	public String getTarget() { return iTarget; }
	public void setTarget(String target) { iTarget = target; }
	public void setGWT(boolean gwt) { iGWT = gwt; }
	public boolean isGWT() { return iGWT; }
	public boolean isSeparator() { return getName() == null; }
	
	public boolean hasSubMenus() { return iSubMenus != null && !iSubMenus.isEmpty(); }
	public List<MenuInterface> getSubMenus() { return iSubMenus; }
	public void addSubMenu(MenuInterface menu) {
		if (iSubMenus == null) iSubMenus = new ArrayList<MenuInterface>();
		iSubMenus.add(menu);
	}
}
