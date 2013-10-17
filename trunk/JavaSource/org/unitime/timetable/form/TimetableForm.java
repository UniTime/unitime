/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.form;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.webutil.timegrid.TimetableGridModel;
import org.unitime.timetable.webutil.timegrid.TimetableGridTable;


/** 
 * @author Tomas Muller
 */
public class TimetableForm extends ActionForm {
	private static final long serialVersionUID = -783119205646238864L;
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
	private boolean iShowInstructors = false;
	private boolean iShowEvents = false;
	private boolean iShowComments = false;

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
		iShowInstructors = false;
		iShowEvents = false;
		iShowComments = false;
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
	
	public boolean getShowInstructors() { return iShowInstructors; }
	public void setShowInstructors(boolean showInstructors) { iShowInstructors = showInstructors; }

	public boolean getShowComments() { return iShowComments; }
	public void setShowComments(boolean showComments) { iShowComments = showComments; }

	public boolean getShowEvents() { return iShowEvents; }
	public void setShowEvents(boolean showEvents) { iShowEvents = showEvents; }

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
	public void setWeeks(Vector weeks) { iWeeks = weeks; }
}

