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
