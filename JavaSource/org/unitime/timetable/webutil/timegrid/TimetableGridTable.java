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
package org.unitime.timetable.webutil.timegrid;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.SolverPredefinedSetting.IdValue;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

/**
 * @author Tomas Muller
 */
public class TimetableGridTable {
	private static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy");
	public static final int sDaysAll = 0;
	public static final int sDaysAllExceptSat = 1;
	public static final int sDaysMon = 2;
	public static final int sDaysTue = 3;
	public static final int sDaysWed = 4;
	public static final int sDaysThu = 5;
	public static final int sDaysFri = 6;
	public static final int sDaysSat = 7;
	public static final int sDaysSun = 8;
	public static String[] sDays = new String[] {
			"All",
			"All except Weekend",
			"Monday",
			"Tuesday",
			"Wednesday",
			"Thursday",
			"Friday",
			"Saturday",
			"Sunday"
	};
	public static final int sDispModeInRow   = 0;
	public static final int sDispModePerWeekHorizontal = 1;
	public static final int sDispModePerWeekVertical = 2;
	public static String[] sDispModes = new String[] {
		"In Row [horizontal]",
		"Per Week [horizontal]",
		"Per Week [vertical]",
	};
	public static final int sOrderByNameAsc = 0;
	public static final int sOrderByNameDesc = 1;
	public static final int sOrderBySizeAsc = 2;
	public static final int sOrderBySizeDesc = 3;
    public static final int sOrderByTypeAsc = 4;
    public static final int sOrderByTypeDesc = 5;
	public static String[] sOrderBy = new String[] { 
		"Name [asc]", 
		"Name [desc]", 
		"Size [asc]", 
		"Size [desc]",
        "Type [asc]",
        "Type [desc]"
	};

	
	private int iDays = sDaysAllExceptSat;
	
	public static final int sDayModeDay = 0;
	public static final int sDayModeEvening = 1;
	public static final int sDayModeDayEvening = 2;
	public static final int sDayModeAll = 3;
	public static String[] sDayMode = new String[] { 
		"Daytime", 
		"Evening", 
		"Daytime & Evening", 
		"All" 
	};
	
	private int iDayMode = sDayModeDay;
	private int iDispMode = sDispModePerWeekHorizontal;
	private int iBgMode = TimetableGridModel.sBgModeNone;
	Vector iModels = new Vector();
	private int iResourceType = TimetableGridModel.sResourceTypeRoom;
	private String iFindStr = null;
	private int iOrderBy = sOrderByNameAsc;
	private int iWeek = -100;
	private boolean iShowUselessTimes = false;
	
	private String iDefaultDatePatternName = null;
	
	public Vector models() { return iModels; }
	public void setDays(int days) { iDays = days;}
	public int getDays() { return iDays; }
	public void setDayMode(int dayMode) { iDayMode = dayMode; }
	public int getDayMode() { return iDayMode;}
	public void setDispMode(int dispMode) { iDispMode = dispMode; }
	public int getDispMode() { return iDispMode; }
	public void setBgMode(int bgMode) { iBgMode = bgMode; }
	public int getBgMode() { return iBgMode; }
	public int getResourceType() { return iResourceType; }
	public void setResourceType(int resourceType) { iResourceType = resourceType; }
	public String getFindString() { return iFindStr; }
	public void setFindString(String findSrt) { iFindStr = findSrt;}
	public int getOrderBy() { return iOrderBy; }
	public void setOrderBy(int orderBy) { iOrderBy = orderBy; }
	public int getWeek() { return iWeek; }
	public void setWeek(int week) { iWeek = week; }
	public boolean getShowUselessTimes() { return iShowUselessTimes; }
	public void setShowUselessTimes(boolean showUselessTimes) { iShowUselessTimes = showUselessTimes; }
	public Vector getWeeks(HttpSession httpSession) throws Exception { 
		Vector weeks = new Vector();
		weeks.addElement(new IdValue(new Long(-100),"All weeks"));
        Session session = Session.getCurrentAcadSession(Web.getUser(httpSession));
		int startWeek = DateUtils.getWeek(session.getSessionBeginDateTime())-(Session.sNrExcessDays/7);
		int endWeek = DateUtils.getWeek(session.getSessionEndDateTime())+(Session.sNrExcessDays/7);
		for (int i=startWeek;i<=endWeek;i++) {
			weeks.addElement(new IdValue(new Long(i),sDF.format(DateUtils.getStartDate(session.getYear(),i))+" - "+sDF.format(DateUtils.getEndDate(session.getYear(),i))));
		}
		if (iWeek<startWeek || iWeek>endWeek) iWeek = -100;
		return weeks;
	}
	
	public TimetableGridTable() {
	}
	
	public void load(HttpSession httpSession) {
		setDays(UserData.getPropertyInt(httpSession,"TimetableGridTable.days",getDays()));
		setDayMode(UserData.getPropertyInt(httpSession,"TimetableGridTable.dayMode",getDayMode()));
		setBgMode(UserData.getPropertyInt(httpSession,"TimetableGridTable.bgMode",getBgMode()));
		setFindString(UserData.getProperty(httpSession,"TimetableGridTable.findString",getFindString()));
		setOrderBy(UserData.getPropertyInt(httpSession,"TimetableGridTable.orderBy",getOrderBy()));
		setDispMode(UserData.getPropertyInt(httpSession,"TimetableGridTable.dispMode",getDispMode()));
		setResourceType(UserData.getPropertyInt(httpSession,"TimetableGridTable.resourceType",getResourceType()));
		setShowUselessTimes(UserData.getPropertyBoolean(httpSession,"TimetableGridTable.showUselessTimes",getShowUselessTimes()));
		setWeek(UserData.getPropertyInt(httpSession,"TimetableGridTable.week",getWeek()));
	}
	
	public void save(HttpSession httpSession) {
		UserData.setPropertyInt(httpSession,"TimetableGridTable.days",getDays());
		UserData.setPropertyInt(httpSession,"TimetableGridTable.dayMode",getDayMode());
		UserData.setPropertyInt(httpSession,"TimetableGridTable.bgMode",getBgMode());
		UserData.setProperty(httpSession,"TimetableGridTable.findString",getFindString());
		UserData.setPropertyInt(httpSession,"TimetableGridTable.orderBy",getOrderBy());
		UserData.setPropertyInt(httpSession,"TimetableGridTable.dispMode",getDispMode());
		UserData.setPropertyInt(httpSession,"TimetableGridTable.resourceType",getResourceType());
		UserData.setPropertyBoolean(httpSession,"TimetableGridTable.showUselessTimes",getShowUselessTimes());
		UserData.setPropertyInt(httpSession,"TimetableGridTable.week",getWeek());
		
	}

	public void printToHtml(JspWriter jsp) {
		PrintWriter out = new PrintWriter(jsp);
		printToHtml(out);
		out.flush();
	}
	
	public void printToHtml(PrintWriter out) {
        out.println("<table border='0' cellpadding='2' cellspacing='0'>");
        int rowNumber=0; 
        for (Enumeration e = models().elements(); e.hasMoreElements(); rowNumber++) {
        	printToHtml(out,(TimetableGridModel)e.nextElement(),rowNumber);
        }
        out.println("</table>");		
	}
	
	public boolean isDispModePerWeekVertical() {
		return iDispMode == sDispModePerWeekVertical;
	}
	
	public boolean isDispModePerWeekHorizontal() {
		return iDispMode == sDispModePerWeekHorizontal;
	}

	public boolean isDispModePerWeek() {
		return isDispModePerWeekHorizontal() || isDispModePerWeekVertical();
	}

	public boolean isDispModeInRow() {
		return iDispMode == sDispModeInRow;
	}
	
	public int startDay() {
		switch (iDays) {
			case sDaysAll : return 0;
			case sDaysAllExceptSat : return 0;
			case sDaysMon : return 0;
			case sDaysTue : return 1;
			case sDaysWed : return 2;
			case sDaysThu : return 3;
			case sDaysFri : return 4;
			case sDaysSat : return 5;
			case sDaysSun : return 6;
			default : return 0;
		}			
	}
	
	public int endDay() {
		switch (iDays) {
			case sDaysAll : return 6;
			case sDaysAllExceptSat : return 4;
			case sDaysMon : return 0;
			case sDaysTue : return 1;
			case sDaysWed : return 2;
			case sDaysThu : return 3;
			case sDaysFri : return 4;
			case sDaysSat : return 5;
			case sDaysSun : return 6;
			default : return 4;
		}			
	}
	
	/*
	public int nrSlotsPerDay() {
		return lastSlot() - firstSlot() + 1; 
	}
	*/
	
	public int firstSlot() {
		switch (iDayMode) {
			case sDayModeDay : 
			case sDayModeDayEvening :
				return Constants.DAY_SLOTS_FIRST;
			case sDayModeEvening : 
				return Constants.EVENING_SLOTS_FIRST;
			case sDayModeAll :
			default :
				return 0;
			
		}
	}

	public int lastSlot() {
		switch (iDayMode) {
			case sDayModeDay : 
				return Constants.DAY_SLOTS_LAST;
			case sDayModeDayEvening :
			case sDayModeEvening : 
				return Constants.EVENING_SLOTS_LAST;
			case sDayModeAll :
			default :
				return Constants.SLOTS_PER_DAY-1;
			
		}
	}

	public void printHeader(PrintWriter out, TimetableGridModel model, int rowNumber) {
		String sfx2 = "";
		if (isDispModePerWeekVertical())
			sfx2 += "Vertical";
		out.println("<tr valign='top'>");
		out.println("<th class='Timetable"+(rowNumber==0?"Head":"")+"Cell"+sfx2+"'>");
		if (isDispModePerWeek())
			out.println(model.getName()+(model.getSize()>0?" ("+model.getSize()+")":""));
		out.println("</th>");
		if (isDispModePerWeekVertical()) {
			for (int day=startDay(); day<=endDay(); day++) {
				boolean eol = (day==endDay());
				out.println("<th width='40' height='40' colspan='"+(1+model.getMaxIdxForDay(day,firstSlot(),lastSlot()))+"'class='TimetableHeadCellVertical"+(eol?"EOL":"")+"'>");
				out.println(Constants.DAY_NAME[day]);
				out.println("</th>");
			}
		} else { //isDispModeInRow() || isDispModePerWeekVertical()
			for (int day=startDay();(isDispModeInRow() && day<=endDay()) || (isDispModePerWeek() && day==startDay());day++) {
				for (int slot=firstSlot();slot<=lastSlot();slot+=6) {
					int time = slot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
					int hour = time/60;
					int min = time%60;
					boolean eod = (slot+5==lastSlot());
					boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
					out.println("<th width='40' height='40' colspan='6' class='Timetable" + (rowNumber==0?"Head":"") + "Cell" + (eol?"EOL":eod?"EOD":"") + "'>");
					if (isDispModeInRow())
						out.println(Constants.DAY_NAME[day]+"<br>");
					out.println((hour>12?hour-12:hour)+":"+(min<10?"0":"")+min+(hour>=12?"p":"a"));
					out.println("</th>");
				}
			}
        }
		out.println("</tr>");
	}
	
    private void getMouseOverAndMouseOut(StringBuffer onMouseOver, StringBuffer onMouseOut, TimetableGridCell cell, String bgColor, boolean changeMouse) {
    	if (cell==null) return;
    	onMouseOver.append(" onmouseover=\"");
        onMouseOut.append(" onmouseout=\"");
        if (isDispModePerWeek()) {
        	for (int i=0;i<cell.getNrMeetings();i++) {
        		onMouseOver.append("if (document.getElementById('"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+i+"')!=null) document.getElementById('"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+i+"').style.backgroundColor='rgb(223,231,242)';");
        		onMouseOut.append("if (document.getElementById('"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+i+"')!=null) document.getElementById('"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+i+"').style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';");
            }
        }
        if (changeMouse)
        	onMouseOver.append("this.style.cursor='hand';this.style.cursor='pointer';");
        onMouseOver.append("\" ");
        onMouseOut.append("\" ");
    }
	
	
	public void printToHtml(PrintWriter out, TimetableGridModel model, int rowNumber) {
		model.clearRendered();
		if (isDispModePerWeek() || rowNumber%10==0)
			printHeader(out, model, rowNumber);
		out.println("<tr valign='top'>");
		if (isDispModeInRow()) {
			int maxIdx = model.getMaxIdx(startDay(),endDay(),firstSlot(),lastSlot());
			out.println("<th width='40' height='40' rowspan='"+(1+maxIdx)+"' class='Timetable" + (rowNumber%10==0?"Head":"") + "Cell'>");
			out.println(model.getName()+(model.getSize()>0?" ("+model.getSize()+")":""));
			out.println("</th>");
			for (int idx=0;idx<=maxIdx;idx++) {
				if (idx>0)
					out.println("</tr><tr valign='top'>");
				for (int day=startDay();day<=endDay();day++) {
					for (int slot=firstSlot();slot<=lastSlot();slot++) {
						int slotsToEnd = lastSlot()-slot+1;
						TimetableGridCell cell = model.getCell(day,slot,idx);
						if (cell==null && model.isRendered(day,slot,idx)) continue;
                        int length = (cell==null?1:cell.getLength()+cell.getSlot()-slot);
						int colSpan = (cell==null?1:Math.min(length,slotsToEnd));
						int rowSpan = (getResourceType()==TimetableGridModel.sResourceTypeDepartment && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,colSpan));
						model.setRendered(day,slot,idx,rowSpan,colSpan);
						if (cell==null) {
							String bgColor = model.getBackground(day,slot);
							if (bgColor==null && !model.isAvailable(day,slot))
								bgColor=TimetableGridCell.sBgColorNotAvailable;
							boolean eod = (slot == lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							boolean first = (slot == firstSlot() || model.getCell(day,slot-1,idx)!=null);
							boolean in = !first && !eod && !eol && (model.getCell(day,slot+1,idx)==null || model.getCell(day,slot-1,idx)==null) && ((slot%6) !=0);
							boolean inEod = eod && model.getCell(day,slot-1,idx)==null;
							boolean inEol = eol && model.getCell(day,slot-1,idx)==null;
							out.println("<td class='TimetableCell" + (first?"First":in?"In":inEol?"InEOL":inEod?"InEOD":eol?"EOL":eod?"EOD":"") + "' rowSpan='"+rowSpan+"' colSpan='"+colSpan+"' "+(bgColor==null?"":"style='background-color:"+bgColor+"'")+">&nbsp;</td>");
						} else {
							String bgColor = cell.getBackground();
	                		if (getBgMode()==TimetableGridModel.sBgModeNone && !TimetableGridCell.sBgColorNotAvailable.equals(bgColor)) {
	                    		for (int i=0;i<length;i++)
	                    			if (!model.isAvailable(day,slot+i)) {
	                    				bgColor = TimetableGridCell.sBgColorNotAvailableButAssigned;
	                    				break;
	                    			}
	                		}
							boolean eod = (slot+length > lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							StringBuffer onMouseOver = new StringBuffer();
							StringBuffer onMouseOut = new StringBuffer();
							getMouseOverAndMouseOut(onMouseOver, onMouseOut, cell, bgColor, cell.getOnClick()!=null);
							out.println("<td nowrap "+(bgColor==null?"":"style='background-color:"+bgColor+"' ")+
									" class='TimetableCell"+(eol?"EOL":eod?"EOD":"")+"' "+
									"align='center' "+
									"colspan='"+colSpan+"' rowSpan='"+rowSpan+"' "+
									(cell.getOnClick()==null?"":"onclick=\""+cell.getOnClick()+"\" ")+
									(cell.getAssignmentId()>=0?"id='"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+cell.getMeetingNumber()+"' ":"")+
									onMouseOver + 
									onMouseOut +
									(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
	                    			">");
							out.print(cell.getName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom)
								out.print("<BR>"+cell.getRoomName());
							else
								out.print(cell.getShortComment()==null?"":"<BR>"+cell.getShortComment());
							if (iWeek==-100 && cell.hasDays() && !cell.getDays().equals(iDefaultDatePatternName))
								out.print("<BR>"+cell.getDays());
							out.println("</td>");
							slot+=length-1;
						}
					}
				}
			}			
		} else  if (isDispModePerWeekHorizontal()) {
			for (int day=startDay();day<=endDay();day++) {
				if (day>startDay())
					out.println("</tr><tr valign='top'>");
				int maxIdx = model.getMaxIdxForDay(day,firstSlot(),lastSlot());
				out.println("<th width='40' height='40' rowspan='"+(1+maxIdx)+"' class='TimetableCell'>"+Constants.DAY_NAME[day]+"</th>");
				for (int idx=0;idx<=maxIdx;idx++) {
					if (idx>0)
						out.println("</tr><tr valign='top'>");
					for (int slot=firstSlot();slot<=lastSlot();slot++) {
						int slotsToEnd = lastSlot()-slot+1;
						TimetableGridCell cell = model.getCell(day,slot,idx);
						if (cell==null && model.isRendered(day,slot,idx)) continue;
                		int length = (cell==null?1:cell.getLength()+cell.getSlot()-slot);
						int colSpan = (cell==null?1:Math.min(length,slotsToEnd));
						int rowSpan = (getResourceType()==TimetableGridModel.sResourceTypeDepartment && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,colSpan));
						model.setRendered(day,slot,idx,rowSpan,colSpan);
						if (cell==null) {
							String bgColor = model.getBackground(day,slot);
							if (bgColor==null && !model.isAvailable(day,slot))
								bgColor=TimetableGridCell.sBgColorNotAvailable;
							boolean eod = (slot == lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							boolean first = (slot == firstSlot() || model.getCell(day,slot-1,idx)!=null);
							boolean in = !first && !eod && !eol && (model.getCell(day,slot+1,idx)==null || model.getCell(day,slot-1,idx)==null) && ((slot%6) !=0); 
							boolean inEod = eod && model.getCell(day,slot-1,idx)==null;
							boolean inEol = eol && model.getCell(day,slot-1,idx)==null;
							//boolean last = !eod && !eol && model.getCell(day,slot+1,idx)!=null;
							out.println("<td class='TimetableCell" + (first?"First":in?"In":inEol?"InEOL":inEod?"InEOD":eol?"EOL":eod?"EOD":"") + "' rowSpan='"+rowSpan+"' colSpan='"+colSpan+"' "+(bgColor==null?"":"style='background-color:"+bgColor+"'")+">&nbsp;</td>");
						} else {
							String bgColor = cell.getBackground();
	                		if (getBgMode()==TimetableGridModel.sBgModeNone && !TimetableGridCell.sBgColorNotAvailable.equals(bgColor)) {
	                    		for (int i=0;i<length;i++)
	                    			if (!model.isAvailable(day,slot+i)) {
	                    				bgColor = TimetableGridCell.sBgColorNotAvailableButAssigned;
	                    				break;
	                    			}
	                		}
							boolean eod = (slot+length > lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							StringBuffer onMouseOver = new StringBuffer();
							StringBuffer onMouseOut = new StringBuffer();
							getMouseOverAndMouseOut(onMouseOver, onMouseOut, cell, bgColor, cell.getOnClick()!=null);
							out.println("<td nowrap "+(bgColor==null?"":"style='background-color:"+bgColor+"' ")+
									" class='TimetableCell"+(eol?"EOL":eod?"EOD":"")+"' "+
									"align='center' "+
									"colspan='"+colSpan+"' rowSpan='"+rowSpan+"' "+
									(cell.getOnClick()==null?"":"onclick=\""+cell.getOnClick()+"\" ")+
									(cell.getAssignmentId()>=0?"id='"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+cell.getMeetingNumber()+"' ":"")+
									onMouseOver + 
									onMouseOut +
									(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
	                    			">");
							out.print(cell.getName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom)
								out.print("<BR>"+cell.getRoomName());
							else
								out.print(cell.getShortComment()==null?"":"<BR>"+cell.getShortComment());
							if (iWeek==-100 && cell.hasDays() && !cell.getDays().equals(iDefaultDatePatternName))
								out.print("<BR>"+cell.getDays());
							out.println("</td>");
							slot+=length-1;
						}
					}
				}
			}
		} else { //isDispModePerWeekVertical
			//FIXME time goes for half-hours (only every 6th slot is checked)
			int step = 6;
			for (int slot=firstSlot();slot<=lastSlot();slot+=step) {
				int time = slot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
				int hour = time/60;
				int min = time%60;
                int slotsToEnd = lastSlot()-slot+1;
                if ((slot%6) == 0) {
                	out.println("<th width='40' height='40' class='TimetableHeadCell"+(slot==firstSlot()?"":"In")+"Vertical'>" + (hour>12?hour-12:hour) + ":" + (min<10?"0":"") + min + (hour>=12?"p":"a") + "</th>");
                } else {
                	out.println("<th width='40' height='40' class='TimetableHeadCellInVertical'>&nbsp;</th>");
                }
                for (int day=startDay();day<=endDay();day++) {
                	int maxIdx = model.getMaxIdxForDay(day,firstSlot(),lastSlot());
                	for (int idx=0;idx<=maxIdx;idx++) {
                    	TimetableGridCell cell = model.getCell(day,slot, idx);
                    	if (model.isRendered(day,slot,idx)) continue;
						int rowSpan = (cell==null?1:Math.min(cell.getLength()+cell.getSlot()-slot,slotsToEnd));
						int colSpan = (getResourceType()==TimetableGridModel.sResourceTypeDepartment && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,rowSpan)); 
						model.setRendered(day,slot,idx,colSpan,rowSpan);
						int rowSpanDivStep = (int)Math.ceil(((double)rowSpan)/step);
                    	
                    	if (cell==null) {
							String bgColor = model.getBackground(day,slot);
							if (bgColor==null && !model.isAvailable(day,slot))
								bgColor=TimetableGridCell.sBgColorNotAvailable;
                            boolean eol = (day==endDay() && (idx+colSpan-1)==maxIdx);
                           	out.println("<td class='TimetableCell"+(slot==firstSlot()?"":"In")+"Vertical"+(eol?"EOL":"")+ "' rowSpan='"+rowSpanDivStep+"' colSpan='"+colSpan+"' "+(bgColor==null?"":"style='background-color:"+bgColor+"'")+">&nbsp;</td>");
                    	} else {
                    		String bgColor = cell.getBackground();
                    		if (getBgMode()==TimetableGridModel.sBgModeNone && !TimetableGridCell.sBgColorNotAvailable.equals(bgColor)) {
                        		for (int i=0;i<cell.getLength()+cell.getSlot()-slot;i++)
                        			if (!model.isAvailable(day,slot+i)) {
                        				bgColor = TimetableGridCell.sBgColorNotAvailableButAssigned;
                        				break;
                        			}
                    		}
                    		StringBuffer onMouseOver = new StringBuffer();
                    		StringBuffer onMouseOut = new StringBuffer();
                    		getMouseOverAndMouseOut(onMouseOver, onMouseOut, cell, bgColor, cell.getOnClick()!=null);
                    		boolean eol = (day==endDay());
                    		out.println("<td nowrap "+
                    				(bgColor==null?"":"style='background-color:"+bgColor+"' ")+
                    				"class='TimetableCell"+(slot==firstSlot()?"":"In")+"Vertical" + (eol?"EOL":"")+ "' align='center' "+
                    				"colspan='"+colSpan+"' rowSpan='"+rowSpanDivStep+"' "+
                    				(cell.getOnClick()==null?"":"onclick=\""+cell.getOnClick()+"\" ")+
                    				(cell.getAssignmentId()>=0?"id='"+cell.getAssignmentId()+"."+cell.getRoomId()+"."+cell.getMeetingNumber()+"' ":"")+
                    				onMouseOver + onMouseOut +
                    				(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
                            		">");
							out.print(cell.getName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom)
								out.print("<BR>"+cell.getRoomName());
							else
								out.print(cell.getShortComment()==null?"":"<BR>"+cell.getShortComment());
							if (iWeek==-100 && cell.hasDays() && !cell.getDays().equals(iDefaultDatePatternName))
								out.print("<BR>"+cell.getDays());
                    		out.println("</td>");
                    	}
                    }
                }
                out.println("</tr><tr valign='top'>");
                if (slot==lastSlot())
                	out.println("<td>&nbsp;</td>");
			}
		}
	}
	
	private boolean match(String name) {
		if (getFindString()==null || getFindString().trim().length()==0) return true;
		StringTokenizer stk = new StringTokenizer(getFindString().toUpperCase()," ,");
		String n = name.toUpperCase();
		while (stk.hasMoreTokens()) {
			String token = stk.nextToken().trim();
			if (token.length()==0) continue;
			if (n.indexOf(token)<0) return false;
		}
		return true;
	}
	
	private void showUselessTimesIfDesired() {
		if (iShowUselessTimes && iModels!=null) {
			for (Enumeration e=iModels.elements();e.hasMoreElements();)
				((TimetableGridModel)e.nextElement()).initBgModeUselessSlots();
		}
	}
	
	public String getDefaultDatePatternName() {
		return iDefaultDatePatternName;
	}
	
	public boolean reload(HttpSession session) throws Exception {
		if (iModels!=null) iModels.clear();
		Session acadSession = Session.getCurrentAcadSession(Web.getUser(session));
		DatePattern defaultDatePattern = acadSession.getDefaultDatePatternNotNull();
    	iDefaultDatePatternName = (defaultDatePattern==null?null:defaultDatePattern.getName());
		SolverProxy solver = WebSolver.getSolver(session);
		int startDay = (getWeek()==-100?-1:DateUtils.getFirstDayOfWeek(acadSession.getYear(),getWeek())-acadSession.getDayOfYear(1,acadSession.getStartMonth())-1);
		if (solver!=null) {
			iModels = solver.getTimetableGridTables(getFindString(), getResourceType(), startDay, getBgMode());
			Collections.sort(iModels,new TimetableGridModelComparator());
			showUselessTimesIfDesired();
			return true;
		}
		String solutionIdsStr = (String)session.getAttribute("Solver.selectedSolutionId");
		if (solutionIdsStr==null || solutionIdsStr.length()==0) return false;
		Transaction tx = null;
		try {
			SolutionDAO dao = new SolutionDAO();
			org.hibernate.Session hibSession = dao.getSession();
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
				tx = hibSession.beginTransaction();
			
			if (getResourceType()==TimetableGridModel.sResourceTypeRoom) {
				Query q = hibSession.createQuery(
						"select distinct r from "+
						"Location as r inner join r.assignments as a where "+
						"a.solution.uniqueId in ("+solutionIdsStr+")");
				q.setCacheable(true);
				for (Iterator i=q.list().iterator();i.hasNext();) {
					Location room = (Location)i.next();
					if (!match(room.getLabel())) continue;
					iModels.add(new SolutionGridModel(solutionIdsStr, room, hibSession,startDay,getBgMode()));
				}
			} else if (getResourceType()==TimetableGridModel.sResourceTypeInstructor) {
                String instructorNameFormat = Settings.getSettingValue(Web.getUser(session), Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
				Query q = hibSession.createQuery(
						"select distinct i from "+
						"DepartmentalInstructor as i inner join i.assignments as a where "+
						"a.solution.uniqueId in ("+solutionIdsStr+")");
				q.setCacheable(true);
				HashSet puids = new HashSet();
				for (Iterator i=q.list().iterator();i.hasNext();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
					String name = (instructor.getLastName()+", "+instructor.getFirstName()+" "+instructor.getMiddleName()).trim();
					if (!match(name)) continue;
					if (instructor.getExternalUniqueId()==null || instructor.getExternalUniqueId().length()<=0 || puids.add(instructor.getExternalUniqueId())) {
                        SolutionGridModel m = new SolutionGridModel(solutionIdsStr, instructor, hibSession,startDay,getBgMode());
                        m.setName(instructor.getName(instructorNameFormat));
						iModels.add(m);
                    }
				}
			} else if (getResourceType()==TimetableGridModel.sResourceTypeDepartment) {
				Query q = hibSession.createQuery(
						"select distinct d from "+
						"Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea.department as d where "+
						"a.solution.uniqueId in ("+solutionIdsStr+") and o.isControl=true");
				q.setCacheable(true);
				for (Iterator i=q.list().iterator();i.hasNext();) {
					Department dept = (Department)i.next();
					String name = dept.getAbbreviation();
					if (!match(name)) continue;
					iModels.add(new SolutionGridModel(solutionIdsStr, dept, hibSession,startDay,getBgMode()));
				}
			}
			if (tx!=null) tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			Debug.error(e);
			throw e;
		} 
		Collections.sort(iModels,new TimetableGridModelComparator());
		showUselessTimesIfDesired();
		return true;
	}
		
	public class TimetableGridModelComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			TimetableGridModel m1 = (TimetableGridModel)o1;
			TimetableGridModel m2 = (TimetableGridModel)o2;
			switch (getOrderBy()) {
				case sOrderByNameAsc :
					return m1.getName().compareTo(m2.getName());
				case sOrderByNameDesc :
					return m2.getName().compareTo(m1.getName());
				case sOrderBySizeAsc :
					return Double.compare(m1.getSize(),m2.getSize());
				case sOrderBySizeDesc :
					return Double.compare(m2.getSize(),m1.getSize());
                case sOrderByTypeAsc :
                    if (m1.getType()!=null && m2.getType()!=null) {
                        int cmp = m1.getType().compareTo(m2.getType());
                        if (cmp!=0) return cmp;
                    }
                    return m1.getName().compareTo(m2.getName());
                case sOrderByTypeDesc :
                    if (m1.getType()!=null && m2.getType()!=null) {
                        int cmp = m2.getType().compareTo(m1.getType());
                        if (cmp!=0) return cmp;
                    }
                    return m2.getName().compareTo(m1.getName());
			}
			return 0;
		}
	}
	
	public void printLegend(JspWriter jsp) {
		PrintWriter out = new PrintWriter(jsp);
		printLegend(out);
		out.flush();
	}

	public void printLegend(PrintWriter out) {
		if (iBgMode!=TimetableGridModel.sBgModeNone) {
			out.println("<tr><td colspan='2'>Assigned classes:</td></tr>");
		}
        if (iBgMode==TimetableGridModel.sBgModeTimePref) {
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required time</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred time</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred time</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No time preference</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged time</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged time</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited time</td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeRoomPref) {
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No room preference</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited room</td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeStudentConf) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = TimetableGridCell.conflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==15?"or more ":"")+"student conflicts</td><td></td></tr>");
            }
        } else if (iBgMode==TimetableGridModel.sBgModeInstructorBtbPref) {
            out.println("<tr><td idth=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No instructor back-to-back preference <i>(distance=0)</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged back-to-back <i>(0&lt;distance&lt;=5)</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged back-to-back <i>(5&lt;distance&lt;=20)</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited back-to-back <i>(20&lt;distance)</i></td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeDistributionConstPref) {
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No violated constraint<i>(distance=0)</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged/preferred constraint violated</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged/preferred constraint violated</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required/prohibited constraint violated</i></td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModePerturbations) {
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No change</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No initial assignment</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Room changed</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Time changed</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Both time and room changed</i></td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModePerturbationPenalty) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = TimetableGridCell.conflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+(nrConflicts==0?"Zero perturbation penalty":nrConflicts==15?"Perturbation penalty above 15":"Perturbation penalty below or equal to "+nrConflicts)+"</td><td></td></tr>");
            }
        } else if (iBgMode==TimetableGridModel.sBgModeHardConflicts) {
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required time and room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in room with no hard conflict</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in room (but there is a hard conflict), can be moved in time with no conflict</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in room (but there is a hard conflict)</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in time with no hard conflict, cannot be moved in room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in time (but there is a hard conflict), cannot be moved in room</td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeDepartmentalBalancing) {
            for (int nrConflicts=0;nrConflicts<=3;nrConflicts++) {
                String color = TimetableGridCell.conflicts2colorFast(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+(nrConflicts==0?"Zero penalty":nrConflicts==3?"Penalty equal or above 3":"Penalty equal to "+nrConflicts)+"</td><td></td></tr>");
            }
        } else if (iBgMode==TimetableGridModel.sBgModeTooBigRooms) {
        	out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is smaller than room limit of a class</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is not more than 25% bigger than the smallest avaialable room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is not more than 50% bigger than the smallest avaialable room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is more than 50% bigger than the smallest avaialable room</td><td></td></tr>");
        } 
        out.println("<tr><td colspan='2'>Free times:</td></tr>");
        out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.sBgColorNotAvailable+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Time not available</td><td></td></tr>");
        out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No preference</td><td></td></tr>");
        if (iShowUselessTimes) {
        	out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Standard (MWF or TTh) time pattern is broken (time cannot be used for MW, WF, MF or TTh class)</td><td></td></tr>");
        	out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Useless half-hour</td><td></td></tr>");
        	out.println("<tr><td width=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Useless half-hour and broken standard time pattern</td><td></td></tr>");
        }
    }
}
