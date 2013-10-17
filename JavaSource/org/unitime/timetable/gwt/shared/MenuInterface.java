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
package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class MenuInterface implements IsSerializable {
	private String iName = null;
	private String iTitle = null;
	private String iPage = null;
	private String iHash = null;
	private Map<String, List<String>> iParameters = null;
	private String iTarget = null;
	private boolean iGWT = false;
	private List<MenuInterface> iSubMenus = null;

	public MenuInterface() {}
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }
	
	public boolean hasPage() { return iPage != null && !iPage.isEmpty(); }
	public String getPage() { return iPage; }
	public void setPage(String page) { iPage = page; }
	
	public boolean hasTarget() { return iTarget != null && !iTarget.isEmpty(); }
	public String getTarget() { return iTarget; }
	public void setTarget(String target) { iTarget = target; }
	
	public void setGWT(boolean gwt) { iGWT = gwt; }
	public boolean isGWT() { return iGWT; }
	public boolean isSeparator() { return getName() == null; }
	
	public String getHash() { return iHash; }
	public void setHash(String hash) { iHash = hash; }
	public boolean hasHash() { return iHash != null && !iHash.isEmpty(); }
	
	public boolean hasParameters() { return iParameters != null && !iParameters.isEmpty(); }
	public void addParameter(String name, String value) {
		if (iParameters == null) iParameters = new HashMap<String, List<String>>();
		List<String> values = iParameters.get(name);
		if (values == null) {
			values = new ArrayList<String>();
			iParameters.put(name, values);
		}
		values.add(value);
	}
	public String getParameters(ValueEncoder encoder) {
		String ret = "";
		if (iParameters != null)
			for (Map.Entry<String, List<String>> values: iParameters.entrySet()) {
				for (String value: values.getValue()) {
					if (!ret.isEmpty()) ret += "&";
					ret += values.getKey() + "=" + (encoder == null ? value : encoder.encode(value));
				}
			}
		return ret;
	}
	
	public boolean hasSubMenus() { return iSubMenus != null && !iSubMenus.isEmpty(); }
	public List<MenuInterface> getSubMenus() { return iSubMenus; }
	public void addSubMenu(MenuInterface menu) {
		if (iSubMenus == null) iSubMenus = new ArrayList<MenuInterface>();
		iSubMenus.add(menu);
	}
	
	public String getURL(ValueEncoder encoder) {
		if (isGWT())
			return "gwt.jsp?page=" + getPage() + (hasParameters() ? "&" + getParameters(encoder) : "") + (hasHash() ? "#" + getHash() : "");
		else
			return getPage() + (hasParameters() ? "?" + getParameters(encoder) : "") + (hasHash() ? "#" + getHash() : "");
	}
	
	public static interface ValueEncoder {
		public String encode(String value);
	}
}
