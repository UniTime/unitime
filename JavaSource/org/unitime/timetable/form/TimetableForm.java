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
package org.unitime.timetable.form;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.webutil.timegrid.TimetableGridModel;
import org.unitime.timetable.webutil.timegrid.TimetableGridTable;


/** 
 * @author Tomas Muller
 */
public class TimetableForm extends ActionForm {
	private String iOp = null;
	private String iResource = null;
	private String iDay = null;
	private String iDayMode = null;
	private String iFind = null;
	private String iOrderBy = null;
	private String iDispMode = null;
	private String iBgColor = null;
	private boolean iLoaded = false;
	private Integer iWeek = null;
	private Vector iWeeks = new Vector();
	private boolean iShowUselessTimes = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iResource = TimetableGridModel.sResourceTypes[0];
		iDay = TimetableGridTable.sDays[0];
		iDayMode = TimetableGridTable.sDayMode[0];
		iShowUselessTimes = false;
		iFind = null;
		iOrderBy = TimetableGridTable.sOrderBy[0];
		iDispMode = TimetableGridTable.sDispModes[0];
		iBgColor = TimetableGridModel.sBgModes[0];
		iLoaded = false;
		iWeek = null;
		iWeeks = new Vector();
	}
	
	public void load(HttpSession session) throws Exception {
		TimetableGridTable table = (TimetableGridTable)session.getAttribute("Timetable.table");
		if (table==null) {
			table = new TimetableGridTable();
			table.load(session);
			session.setAttribute("Timetable.table",table);
		}
		iResource = TimetableGridModel.sResourceTypes[table.getResourceType()];
		iDay = TimetableGridTable.sDays[table.getDays()];
		iDayMode = TimetableGridTable.sDayMode[table.getDayMode()];
		iFind = table.getFindString();
		iOrderBy = TimetableGridTable.sOrderBy[table.getOrderBy()];
		iDispMode = TimetableGridTable.sDispModes[table.getDispMode()];
		iBgColor = TimetableGridModel.sBgModes[table.getBgMode()];
		iWeeks = table.getWeeks(session);
		iWeek = new Integer(table.getWeek());
		iShowUselessTimes = table.getShowUselessTimes();
	}
	
	public void save(HttpSession session) throws Exception {
		TimetableGridTable table = (TimetableGridTable)session.getAttribute("Timetable.table");
		if (table==null) {
			table = new TimetableGridTable();
			session.setAttribute("Timetable.table",table);
		}
		table.setResourceType(getResourceInt());
		table.setDays(getDayInt());
		table.setDayMode(getDayModeInt());
		table.setFindString(getFind());
		table.setOrderBy(getOrderByInt());
		table.setDispMode(getDispModeInt());
		table.setBgMode(getBgColorInt());
		table.setShowUselessTimes(iShowUselessTimes);
		if (iWeek!=null)
			table.setWeek(iWeek.intValue());
		table.save(session);
	}
	
	public String getResource() { return iResource; }
	public void setResource(String resource) { iResource = resource; }
	public String[] getResources() { return TimetableGridModel.sResourceTypes; }
	public int getResourceInt() {
		for (int i=0;i<getResources().length;i++)
			if (getResources()[i].equals(iResource)) return i;
		return -1;
	}
	
	public String getDay() { return iDay; }
	public void setDay(String day) { iDay = day; }
	public String[] getDays() { return TimetableGridTable.sDays; }
	public int getDayInt() {
		for (int i=0;i<getDays().length;i++)
			if (getDays()[i].equals(iDay)) return i;
		return -1;
	}
	
	public String getDayMode() { return iDayMode; }
	public void setDayMode(String dayMode) { iDayMode = dayMode; }
	public String[] getDayModes() { return TimetableGridTable.sDayMode; }
	public int getDayModeInt() {
		for (int i=0;i<getDayModes().length;i++)
			if (getDayModes()[i].equals(iDayMode)) return i;
		return -1;
	}
	
	public boolean getShowUselessTimes() { return iShowUselessTimes; }
	public void setShowUselessTimes(boolean showUselessTimes) { iShowUselessTimes = showUselessTimes; }
	
	public String getFind() { return iFind; }
	public void setFind(String find) { iFind = find; }
	
	public String getOrderBy() { return iOrderBy; }
	public void setOrderBy(String orderBy) { iOrderBy = orderBy; }
	public String[] getOrderBys() { return TimetableGridTable.sOrderBy; }
	public int getOrderByInt() { 
		for (int i=0;i<getOrderBys().length;i++)
			if (getOrderBys()[i].equals(iOrderBy)) return i;
		return -1;
	}
	
	public String getDispMode() { return iDispMode; }
	public void setDispMode(String dispMode) { iDispMode = dispMode; }
	public String[] getDispModes() { return TimetableGridTable.sDispModes; }
	public int getDispModeInt() {
		for (int i=0;i<getDispModes().length;i++)
			if (getDispModes()[i].equals(iDispMode)) return i;
		return -1;
	}
	
	public String getBgColor() { return iBgColor; }
	public void setBgColor(String bgColor) { iBgColor = bgColor; }
	public String[] getBgColors() { return TimetableGridModel.sBgModes; }
	public int getBgColorInt() {
		for (int i=0;i<getBgColors().length;i++)
			if (getBgColors()[i].equals(iBgColor)) return i;
		return -1;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	
	public boolean getLoaded() { return iLoaded; }
	public void setLoaded(boolean loaded) { iLoaded = loaded; }
	
	public Integer getWeek() { return iWeek; }
	public void setWeek(Integer week) { iWeek = week; }
	public Vector getWeeks() { return iWeeks; }
}

