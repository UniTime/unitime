/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.webutil.timegrid;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.SolverPredefinedSetting.IdValue;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.ui.StudentGroupInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class TimetableGridTable {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
	protected static Formats.Format<Number> sUtF = Formats.getNumberFormat("0.00");
	public static final int sDaysAll = 0;
	public static final int sDaysAllExceptSat = 1;
	public static final int sDaysMon = 2;
	public static final int sDaysTue = 3;
	public static final int sDaysWed = 4;
	public static final int sDaysThu = 5;
	public static final int sDaysFri = 6;
	public static final int sDaysSat = 7;
	public static final int sDaysSun = 8;
	public static final int sDaysMonThu = 9;
	public static final int sDaysFriSat = 10;
	public static final int sDaysMWF = 11;
	public static final int sDaysTTh = 12;
	public static String[] sDays = new String[] {
			"All",
			"All except Weekend",
			"Monday",
			"Tuesday",
			"Wednesday",
			"Thursday",
			"Friday",
			"Saturday",
			"Sunday",
			"Monday - Thursday",
			"Friday & Saturday",
			"Monday, Wednesday, Friday",
			"Tuesday, Thursday",
	};
	public static final int sDispModeInRow   = 0;
	public static final int sDispModePerWeekHorizontal = 1;
	public static final int sDispModePerWeekVertical = 2;
	public static final int sDispModeWeekByWeekHorizontal = 3;
	public static String[] sDispModes = new String[] {
		"In Row [horizontal]",
		"Per Week [horizontal]",
		"Per Week [vertical]",
		"Per Date [horizontal]"
	};
	public static final int sOrderByNameAsc = 0;
	public static final int sOrderByNameDesc = 1;
	public static final int sOrderBySizeAsc = 2;
	public static final int sOrderBySizeDesc = 3;
    public static final int sOrderByTypeAsc = 4;
    public static final int sOrderByTypeDesc = 5;
    public static final int sOrderByUtilizationAsc = 6;
    public static final int sOrderByUtilizationDesc = 7;
	public static String[] sOrderBy = new String[] { 
		"Name [asc]", 
		"Name [desc]", 
		"Size [asc]", 
		"Size [desc]",
        "Type [asc]",
        "Type [desc]",
        "Utilization [asc]",
        "Utilization [desc]",
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
	private boolean iShowInstructors = false;
	private boolean iShowEvents = false;
	private boolean iShowComments = false;
	private boolean iShowTimes = false;
	private org.unitime.timetable.gwt.server.Query iQuery = null;
	private int iNrSlotsPerPeriod = -1;
	
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
	public boolean isDepartmentalResourceType() {
		return iResourceType == TimetableGridModel.sResourceTypeDepartment || iResourceType == TimetableGridModel.sResourceTypeSubjectArea;
	}
	public void setResourceType(int resourceType) { iResourceType = resourceType; }
	public String getFindString() { return iFindStr; }
	public void setFindString(String findSrt) {
		iFindStr = findSrt;
		iQuery = (findSrt == null ? null : new org.unitime.timetable.gwt.server.Query(findSrt));
	}
	public int getOrderBy() { return iOrderBy; }
	public void setOrderBy(int orderBy) { iOrderBy = orderBy; }
	public int getWeek() { return iWeek; }
	public void setWeek(int week) { iWeek = week; }
	public boolean getShowUselessTimes() { return iShowUselessTimes; }
	public void setShowUselessTimes(boolean showUselessTimes) { iShowUselessTimes = showUselessTimes; }
	public boolean getShowInstructors() { return iShowInstructors; }
	public void setShowInstructors(boolean showInstructors) { iShowInstructors = showInstructors; }
	public boolean getShowTimes() { return iShowTimes; }
	public void setShowTimes(boolean showTimes) { iShowTimes = showTimes; }
	public boolean getShowComments() { return iShowComments; }
	public void setShowComments(boolean showComments) { iShowComments = showComments; }
	public boolean getShowEvents() { return iShowEvents; }
	public void setShowEvents(boolean showEvents) { iShowEvents = showEvents; }
	protected Date iFirstDate = null;
	protected int iFirstDay = 0;
	public Vector getWeeks(SessionContext context) throws Exception {
		Vector weeks = new Vector();
		weeks.addElement(new IdValue(new Long(-100),"All weeks"));
        Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		int startWeek = DateUtils.getWeek(session.getSessionBeginDateTime()) - ApplicationProperty.SessionNrExcessDays.intValue()/7;
		Calendar endCal = Calendar.getInstance(Locale.US);
		endCal.setTime(session.getSessionEndDateTime());
		endCal.add(Calendar.DAY_OF_YEAR, ApplicationProperty.SessionNrExcessDays.intValue());
		int week = startWeek;
		iFirstDate = DateUtils.getStartDate(session.getSessionStartYear(), startWeek);
		iFirstDay = DateUtils.getFirstDayOfWeek(session.getSessionStartYear(),startWeek) - session.getDayOfYear(1,session.getPatternStartMonth())-1;
		while (DateUtils.getStartDate(session.getSessionStartYear(),week).compareTo(endCal.getTime()) <= 0) {
			weeks.addElement(new IdValue(new Long(week), sDF.format(DateUtils.getStartDate(session.getSessionStartYear(), week))+" - "+sDF.format(DateUtils.getEndDate(session.getSessionStartYear(), week))));
			week++;
		}
		if (iWeek<startWeek || iWeek>=week) iWeek = -100;
		return weeks;
	}
	
	public TimetableGridTable() {
	}
	
	public void load(UserContext user) {
		setDays(Integer.parseInt(user.getProperty("TimetableGridTable.days",String.valueOf(getDays()))));
		setDayMode(Integer.parseInt(user.getProperty("TimetableGridTable.dayMode",String.valueOf(getDayMode()))));
		setBgMode(Integer.parseInt(user.getProperty("TimetableGridTable.bgMode",String.valueOf(getBgMode()))));
		setFindString(user.getProperty("TimetableGridTable.findString",getFindString()));
		setOrderBy(Integer.parseInt(user.getProperty("TimetableGridTable.orderBy",String.valueOf(getOrderBy()))));
		setDispMode(Integer.parseInt(user.getProperty("TimetableGridTable.dispMode",String.valueOf(getDispMode()))));
		setResourceType(Integer.parseInt(user.getProperty("TimetableGridTable.resourceType",String.valueOf(getResourceType()))));
		setShowUselessTimes("1".equals(user.getProperty("TimetableGridTable.showUselessTimes",getShowUselessTimes() ? "1" : "0")));
		setWeek(Integer.parseInt(user.getProperty("TimetableGridTable.week",String.valueOf(getWeek()))));
		setShowInstructors("1".equals(user.getProperty("TimetableGridTable.showInstructors",getShowInstructors() ? "1" : "0")));
		setShowComments("1".equals(user.getProperty("TimetableGridTable.showComments",getShowComments() ? "1" : "0")));
		setShowEvents("1".equals(user.getProperty("TimetableGridTable.showEvets",getShowEvents() ? "1" : "0")));
		setShowTimes("1".equals(user.getProperty("TimetableGridTable.showTimes",getShowTimes() ? "1" : "0")));
	}
	
	public void save(UserContext user) {
		user.setProperty("TimetableGridTable.days",String.valueOf(getDays()));
		user.setProperty("TimetableGridTable.dayMode",String.valueOf(getDayMode()));
		user.setProperty("TimetableGridTable.bgMode",String.valueOf(getBgMode()));
		user.setProperty("TimetableGridTable.findString",getFindString());
		user.setProperty("TimetableGridTable.orderBy",String.valueOf(getOrderBy()));
		user.setProperty("TimetableGridTable.dispMode",String.valueOf(getDispMode()));
		user.setProperty("TimetableGridTable.resourceType",String.valueOf(getResourceType()));
		user.setProperty("TimetableGridTable.showUselessTimes",getShowUselessTimes() ? "1" : "0");
		user.setProperty("TimetableGridTable.week",String.valueOf(getWeek()));
		user.setProperty("TimetableGridTable.showInstructors",getShowInstructors() ? "1" : "0");
		user.setProperty("TimetableGridTable.showComments",getShowComments() ? "1" : "0");
		user.setProperty("TimetableGridTable.showEvets",getShowEvents() ? "1" : "0");
		user.setProperty("TimetableGridTable.showTimes",getShowTimes() ? "1" : "0");
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
		return isDispModePerWeekHorizontal() || isDispModePerWeekVertical() || isDispModeWeekByWeekHorizontal();
	}

	public boolean isDispModeInRow() {
		return iDispMode == sDispModeInRow;
	}
	
	public boolean isDispModeWeekByWeekHorizontal() {
		return iDispMode == sDispModeWeekByWeekHorizontal;
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
			case sDaysMonThu: return 0;
			case sDaysFriSat : return 4;
			case sDaysMWF: return 0;
			case sDaysTTh: return 1;
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
			case sDaysMonThu: return 3;
			case sDaysFriSat : return 5;
			case sDaysMWF: return 4;
			case sDaysTTh: return 3;
			default : return 4;
		}
	}
	
	public boolean skipDay(int day) {
		switch (iDays) {
		case sDaysMWF:
			return day == 1 || day == 3;
		case sDaysTTh:
			return day == 2;
		default:
			return false;
		}
	}
	
	public int nrDays() {
		switch (iDays) {
		case sDaysMWF:
			return 3;
		case sDaysTTh:
			return 2;
		default:
			return endDay() - startDay() + 1;
		} 
	}
	
	/*
	public int nrSlotsPerDay() {
		return lastSlot() - firstSlot() + 1; 
	}
	*/
	
	public int nrSlotsPerPeriod() {
		if (iNrSlotsPerPeriod < 0) {
			iNrSlotsPerPeriod = ApplicationProperty.TimetableGridSlotsPerPeriod.intValue();;
		}
		return iNrSlotsPerPeriod;
	}
	
	public int firstSlot() {
		switch (iDayMode) {
			case sDayModeDay : 
			case sDayModeDayEvening :
				return ApplicationProperty.TimetableGridFirstDaySlot.intValue();
			case sDayModeEvening : 
				return ApplicationProperty.TimetableGridLastDaySlot.intValue() + 1;
			case sDayModeAll :
			default :
				return 0;
			
		}
	}

	public int lastSlot() {
		switch (iDayMode) {
			case sDayModeDay : 
				return ApplicationProperty.TimetableGridLastDaySlot.intValue();
			case sDayModeDayEvening :
			case sDayModeEvening : 
				return ApplicationProperty.TimetableGridLastEveningSlot.intValue();
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
		out.println("<th class='Timetable"+(rowNumber==0?"Head":"")+"Cell"+sfx2+"' "+
				(getResourceType() == TimetableGridModel.sResourceTypeRoom ? "onmouseover=\"showGwtRoomHint(this, '" + model.getResourceId() + "');\" onmouseout=\"hideGwtRoomHint();\"" : "") +
				">");
		if (isDispModePerWeek()) {
			out.println(model.getName());
			if (getResourceType() == TimetableGridModel.sResourceTypeRoom)
				out.println("<div style='white-space:nowrap;' title='capacity " + model.getSize() + " seats, utilization " + 
						sUtF.format(model.getUtilization() / nrSlotsPerPeriod()) + " periods'  >(" +
						model.getSize() + ", " + sUtF.format(model.getUtilization() / nrSlotsPerPeriod()) + ")</div>");
			else if (getResourceType() == TimetableGridModel.sResourceTypeCurriculum)
				out.println("<span style='white-space:nowrap;' title='" + model.getSize() + " students'  >(" + model.getSize() + ")</span>");
			else if (model.getResourceType() == TimetableGridModel.sResourceTypeStudentGroup)
				out.println("<div style='white-space:nowrap;' title='capacity " + model.getSize() + " seats, same class " + 
						sUtF.format(100.0 * model.getUtilization()) + "%'  >(" +
						model.getSize() + ", " + sUtF.format(100.0 * model.getUtilization()) + "%)</div>");
			else if (model.getSize() > 0)
				out.println("<span style='white-space:nowrap;' title='" + model.getSize() + " classes'  >(" + model.getSize() + ")</span>");
		}
		out.println("</th>");
		if (isDispModePerWeekVertical()) {
			for (int day=startDay(); day<=endDay(); day++) {
				if (skipDay(day)) continue;
				boolean eol = (day==endDay());
				out.println("<th colspan='"+(1+model.getMaxIdxForDay(day,firstSlot(),lastSlot()))+"'class='TimetableHeadCellVertical"+(eol?"EOL":"")+"'>");
				out.println(CONSTANTS.days()[day]);
				out.println("</th>");
			}
		} else { //isDispModeInRow() || isDispModePerWeekVertical()
			for (int day=startDay();(isDispModeInRow() && day<=endDay()) || (isDispModePerWeek() && day==startDay());day++) {
				if (skipDay(day)) continue;
				for (int slot=firstSlot();slot<=lastSlot();slot+=nrSlotsPerPeriod()) {
					int time = slot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
					boolean eod = (slot+nrSlotsPerPeriod()-1==lastSlot());
					boolean eol = (eod && (isDispModePerWeek() ||  day==endDay()));
					out.println("<th colspan='"+nrSlotsPerPeriod()+"' class='Timetable" + (rowNumber==0?"Head":"") + "Cell" + (eol?"EOL":eod?"EOD":"") + "'>");
					if (isDispModeInRow())
						out.println(CONSTANTS.days()[day]+"<br>");
					out.println(Constants.toTime(time));
					out.println("</th>");
				}
			}
        }
		out.println("</tr>");
	}
	
    private void getMouseOverAndMouseOut(StringBuffer onMouseOver, StringBuffer onMouseOut, StringBuffer onClick, TimetableGridCell cell, String bgColor) {
    	if (cell==null) return;
    	onMouseOver.append(" onmouseover=\"");
    	onMouseOut.append(" onmouseout=\"");
    	if (cell.getAssignmentId() >= 0) {
    		onMouseOver.append("mOvr(" + cell.getAssignmentId() + ");");
    		onMouseOut.append("mOut(" + cell.getAssignmentId() + ",'" + (bgColor == null ? "transparent" : bgColor) + "');");
    	}
        if (cell.getOnClick() != null && !cell.getOnClick().isEmpty()) {
        	onMouseOver.append("mHnd(this);");
        	onClick.append(" onclick=\"");
        	if (cell.getTitle() != null && !cell.getTitle().isEmpty())
        		onClick.append("hideGwtHint();");
        	if (cell.getAssignmentId() >= 0)
        		onClick.append("mOut(" + cell.getAssignmentId() + ",'" + (bgColor == null ? "transparent" : bgColor) + "');");
            onClick.append(cell.getOnClick() + "\"");
        }
        if (cell.getTitle() != null && !cell.getTitle().isEmpty()) {
            onMouseOver.append("showGwtHint(this,'" + cell.getTitle() + "');");
            onMouseOut.append("hideGwtHint();");
        }
        onMouseOver.append("\" ");
        onMouseOut.append("\" ");
    }
	
	
	public void printToHtml(PrintWriter out, TimetableGridModel model, int rowNumber) {
		int maxCellWidth = ApplicationProperty.TimetableGridMaxCellWidth.intValue();
		int maxCellWidthVertical = ApplicationProperty.TimetableGridMaxCellWidthVertical.intValue();
		model.clearRendered();
		if (isDispModePerWeek() || rowNumber%10==0)
			printHeader(out, model, rowNumber);
		out.println("<tr valign='top'>");
		if (isDispModeInRow()) {
			int maxIdx = model.getMaxIdx(startDay(),endDay(),firstSlot(),lastSlot());
			out.println("<th rowspan='"+(1+maxIdx)+"' class='Timetable" + (rowNumber%10==0?"Head":"") + "Cell' " +
			(getResourceType() == TimetableGridModel.sResourceTypeRoom ? "onmouseover=\"showGwtRoomHint(this, '" + model.getResourceId() + "');\" onmouseout=\"hideGwtRoomHint();\"" : "") +
			">");
			out.println(model.getName());
			if (getResourceType() == TimetableGridModel.sResourceTypeRoom)
				out.println("<div style='white-space:nowrap;' title='capacity " + model.getSize() + " seats, utilization " + 
						sUtF.format(model.getUtilization() / nrSlotsPerPeriod()) + " periods'  >(" +
						model.getSize() + ", " + sUtF.format(model.getUtilization() / nrSlotsPerPeriod()) + ")</div>");
			else if (getResourceType() == TimetableGridModel.sResourceTypeCurriculum)
				out.println("<span style='white-space:nowrap;' title='" + model.getSize() + " students'  >(" + model.getSize() + ")</span>");
			if (getResourceType() == TimetableGridModel.sResourceTypeStudentGroup)
				out.println("<div style='white-space:nowrap;' title='capacity " + model.getSize() + " seats, same class " + 
						sUtF.format(100.0 * model.getUtilization()) + "%'  >(" +
						model.getSize() + ", " + sUtF.format(100.0 * model.getUtilization()) + "%)</div>");
			else if (model.getSize() > 0)
				out.println("<span style='white-space:nowrap;' title='" + model.getSize() + " classes'  >(" + model.getSize() + ")</span>");
			out.println("</th>");
			for (int idx=0;idx<=maxIdx;idx++) {
				if (idx>0)
					out.println("</tr><tr valign='top'>");
				for (int day=startDay();day<=endDay();day++) {
					if (skipDay(day)) continue;
					for (int slot=firstSlot();slot<=lastSlot();slot++) {
						int slotsToEnd = lastSlot()-slot+1;
						TimetableGridCell cell = model.getCell(day,slot,idx);
						if (cell==null && model.isRendered(day,slot,idx)) continue;
                        int length = (cell==null?1:cell.getLength()+cell.getSlot()-slot);
						int colSpan = (cell==null?1:Math.min(length,slotsToEnd));
						int rowSpan = (isDepartmentalResourceType() && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,colSpan));
						model.setRendered(day,slot,idx,rowSpan,colSpan);
						if (cell==null) {
							String bgColor = model.getBackground(day,slot);
							if (bgColor==null && !model.isAvailable(day,slot))
								bgColor=TimetableGridCell.sBgColorNotAvailable;
							boolean eod = (slot == lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							boolean first = (slot == firstSlot() || model.getCell(day,slot-1,idx)!=null);
							boolean in = !first && !eod && !eol && (model.getCell(day,slot+1,idx)==null || model.getCell(day,slot-1,idx)==null) && (((slot - firstSlot())%nrSlotsPerPeriod()) !=0);
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
							StringBuffer onClick = new StringBuffer();
							getMouseOverAndMouseOut(onMouseOver, onMouseOut, onClick, cell, bgColor);
							out.println("<td nowrap style='"+(bgColor==null?"":"background-color:"+bgColor)+";max-width:"+(maxCellWidth*colSpan)+"px;' "+
									" class='TimetableCell"+(eol?"EOL":eod?"EOD":"")+"' "+
									"align='center' "+
									"colspan='"+colSpan+"' rowSpan='"+rowSpan+"' "+
									(cell.getAssignmentId()>=0?"name='c"+cell.getAssignmentId()+"' ":"")+
									onClick + 
									onMouseOver + 
									onMouseOut +
									//(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
	                    			">");
							out.print(cell.getName());
							if (iShowTimes)
								out.print("<BR>"+cell.getTime());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom && cell.getRoomName() != null)
								out.print("<BR>"+cell.getRoomName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeInstructor && iShowInstructors && !cell.getInstructor().isEmpty())
								out.print("<BR>"+cell.getInstructor());
							if (iShowComments)
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
				if (skipDay(day)) continue;
				if (day>startDay())
					out.println("</tr><tr valign='top'>");
				int maxIdx = model.getMaxIdxForDay(day,firstSlot(),lastSlot());
				out.println("<th rowspan='"+(1+maxIdx)+"' class='TimetableCell'>"+CONSTANTS.days()[day]+"</th>");
				for (int idx=0;idx<=maxIdx;idx++) {
					if (idx>0)
						out.println("</tr><tr valign='top'>");
					for (int slot=firstSlot();slot<=lastSlot();slot++) {
						int slotsToEnd = lastSlot()-slot+1;
						TimetableGridCell cell = model.getCell(day,slot,idx);
						if (cell==null && model.isRendered(day,slot,idx)) continue;
                		int length = (cell==null?1:cell.getLength()+cell.getSlot()-slot);
						int colSpan = (cell==null?1:Math.min(length,slotsToEnd));
						int rowSpan = (isDepartmentalResourceType() && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,colSpan));
						model.setRendered(day,slot,idx,rowSpan,colSpan);
						if (cell==null) {
							String bgColor = model.getBackground(day,slot);
							if (bgColor==null && !model.isAvailable(day,slot))
								bgColor=TimetableGridCell.sBgColorNotAvailable;
							boolean eod = (slot == lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							boolean first = (slot == firstSlot() || model.getCell(day,slot-1,idx)!=null);
							boolean in = !first && !eod && !eol && (model.getCell(day,slot+1,idx)==null || model.getCell(day,slot-1,idx)==null) && (((slot - firstSlot())%nrSlotsPerPeriod()) !=0); 
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
							StringBuffer onClick = new StringBuffer();
							getMouseOverAndMouseOut(onMouseOver, onMouseOut, onClick, cell, bgColor);
							out.println("<td nowrap style='"+(bgColor==null?"":"background-color:"+bgColor)+";max-width:"+(maxCellWidth*colSpan)+"px;' "+
									" class='TimetableCell"+(eol?"EOL":eod?"EOD":"")+"' "+
									"align='center' "+
									"colspan='"+colSpan+"' rowSpan='"+rowSpan+"' "+
									(cell.getAssignmentId()>=0?"name='c"+cell.getAssignmentId()+"' ":"")+
									onClick +
									onMouseOver + 
									onMouseOut +
									//(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
	                    			">");
							out.print(cell.getName());
							if (iShowTimes)
								out.print("<BR>"+cell.getTime());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom && cell.getRoomName() != null)
								out.print("<BR>"+cell.getRoomName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeInstructor && iShowInstructors && !cell.getInstructor().isEmpty())
								out.print("<BR>"+cell.getInstructor());
							if (iShowComments)
								out.print(cell.getShortComment()==null?"":"<BR>"+cell.getShortComment());
							if (iWeek==-100 && cell.hasDays() && !cell.getDays().equals(iDefaultDatePatternName))
								out.print("<BR>"+cell.getDays());
							out.println("</td>");
							slot+=length-1;
						}
					}
				}
			}
		} else if (isDispModeWeekByWeekHorizontal()) {
			boolean firstDay = true;
			Calendar c = Calendar.getInstance(Locale.US);
			c.setTime(iFirstDate);
			for (int d = 0; d < 365; d++ ) {
				if (d > 0)
					c.add(Calendar.DAY_OF_YEAR, 1);
				int date = d + iFirstDay;
				if (model.getFirstDay() >= 0 && (date < model.getFirstDay() || date > model.getFirstDay() + 6)) continue;
				int day = d % 7;
				if (day < startDay() || day > endDay() || skipDay(day)) continue;
				boolean hasClasses = false;
				for (int slot=firstSlot();slot<=lastSlot();slot++) {
					if (model.getCell(day, slot, 0, date) != null) {
						hasClasses = true; break;
					}
				}
				if (!hasClasses) continue;
				if (firstDay) {
					firstDay = false;
				} else {
					out.println("</tr><tr valign='top'>");
				}
				int maxIdx = model.getMaxIdxForDay(day,firstSlot(),lastSlot(),date);
				out.println("<th rowspan='"+(1+maxIdx)+"' class='TimetableCell'>"+CONSTANTS.days()[day]+ " " + sDF.format(c.getTime()) + "</th>");
				for (int idx=0;idx<=maxIdx;idx++) {
					if (idx>0)
						out.println("</tr><tr valign='top'>");
					for (int slot=firstSlot();slot<=lastSlot();slot++) {
						int slotsToEnd = lastSlot()-slot+1;
						TimetableGridCell cell = model.getCell(day,slot,idx,date);
						if (cell==null && model.isRendered(day,slot,idx,date)) continue;
                		int length = (cell==null?1:cell.getLength()+cell.getSlot()-slot);
						int colSpan = (cell==null?1:Math.min(length,slotsToEnd));
						int rowSpan = (isDepartmentalResourceType() && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,colSpan,date));
						model.setRendered(day,slot,idx,rowSpan,colSpan,date);
						if (cell==null) {
							String bgColor = model.getBackground(day,slot);
							if (bgColor==null && !model.isAvailable(day,slot))
								bgColor=TimetableGridCell.sBgColorNotAvailable;
							boolean eod = (slot == lastSlot());
							boolean eol = (eod && (isDispModePerWeek() || day==endDay()));
							boolean first = (slot == firstSlot() || model.getCell(day,slot-1,idx,date)!=null);
							boolean in = !first && !eod && !eol && (model.getCell(day,slot+1,idx,date)==null || model.getCell(day,slot-1,idx,date)==null) && (((slot - firstSlot())%nrSlotsPerPeriod()) !=0); 
							boolean inEod = eod && model.getCell(day,slot-1,idx,date)==null;
							boolean inEol = eol && model.getCell(day,slot-1,idx,date)==null;
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
							StringBuffer onClick = new StringBuffer();
							getMouseOverAndMouseOut(onMouseOver, onMouseOut, onClick, cell, bgColor);
							out.println("<td nowrap style='"+(bgColor==null?"":"background-color:"+bgColor)+";max-width:"+(maxCellWidth*colSpan)+"px;' "+
									" class='TimetableCell"+(eol?"EOL":eod?"EOD":"")+"' "+
									"align='center' "+
									"colspan='"+colSpan+"' rowSpan='"+rowSpan+"' "+
									(cell.getAssignmentId()>=0?"name='c"+cell.getAssignmentId()+"' ":"")+
									onClick +
									onMouseOver + 
									onMouseOut +
									//(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
	                    			">");
							out.print(cell.getName());
							if (iShowTimes)
								out.print("<BR>"+cell.getTime());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom)
								out.print("<BR>"+cell.getRoomName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeInstructor && iShowInstructors && !cell.getInstructor().isEmpty())
								out.print("<BR>"+cell.getInstructor());
							if (iShowComments)
								out.print(cell.getShortComment()==null?"":"<BR>"+cell.getShortComment());
							// if (iWeek==-100 && cell.hasDays() && !cell.getDays().equals(iDefaultDatePatternName))
							//	out.print("<BR>"+cell.getDays());
							out.println("</td>");
							slot+=length-1;
						}
					}
				}
			}
		} else { //isDispModePerWeekVertical
			//FIXME time goes for half-hours (only every nrSlotsPerPeriod()-th slot is checked)
			int step = nrSlotsPerPeriod();
			for (int slot=firstSlot();slot<=lastSlot();slot+=step) {
				int time = slot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
                int slotsToEnd = lastSlot()-slot+1;
                if (((slot-firstSlot())%nrSlotsPerPeriod()) == 0) {
                	out.println("<th class='TimetableHeadCell"+(slot==firstSlot()?"":"In")+"Vertical'>" + Constants.toTime(time) + "</th>");
                } else {
                	out.println("<th class='TimetableHeadCellInVertical'>&nbsp;</th>");
                }
                for (int day=startDay();day<=endDay();day++) {
                	if (skipDay(day)) continue;
                	int maxIdx = model.getMaxIdxForDay(day,firstSlot(),lastSlot());
                	for (int idx=0;idx<=maxIdx;idx++) {
                    	TimetableGridCell cell = model.getCell(day,slot, idx);
                    	if (model.isRendered(day,slot,idx)) continue;
						int rowSpan = (cell==null?1:Math.min(cell.getLength()+cell.getSlot()-slot,slotsToEnd));
						int colSpan = (isDepartmentalResourceType() && cell!=null?1:model.getDepth(day,slot,idx,maxIdx,rowSpan)); 
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
							StringBuffer onClick = new StringBuffer();
                    		getMouseOverAndMouseOut(onMouseOver, onMouseOut, onClick, cell, bgColor);
                    		boolean eol = (day==endDay());
                    		out.println("<td nowrap style='"+(bgColor==null?"":"background-color:"+bgColor)+";max-width:"+(maxCellWidthVertical*colSpan)+"px;' "+
                    				"class='TimetableCell"+(slot==firstSlot()?"":"In")+"Vertical" + (eol?"EOL":"")+ "' align='center' "+
                    				"colspan='"+colSpan+"' rowSpan='"+rowSpanDivStep+"' "+
                    				(cell.getAssignmentId()>=0?"name='c"+cell.getAssignmentId()+"' ":"")+
                    				onClick + onMouseOver + onMouseOut +
                    				//(cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
                            		">");
							out.print(cell.getName());
							if (iShowTimes)
								out.print("<BR>"+cell.getTime());
							if (getResourceType()!=TimetableGridModel.sResourceTypeRoom)
								out.print("<BR>"+cell.getRoomName());
							if (getResourceType()!=TimetableGridModel.sResourceTypeInstructor && iShowInstructors && !cell.getInstructor().isEmpty())
								out.print("<BR>"+cell.getInstructor());
							if (iShowComments)
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
	
	private boolean match(final String name) {
		return iQuery == null || iQuery.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					term: for (StringTokenizer s = new StringTokenizer(term, " ,"); s.hasMoreTokens(); ) {
						String termToken = s.nextToken();
						for (StringTokenizer t = new StringTokenizer(name, " ,"); t.hasMoreTokens(); ) {
							String token = t.nextToken();
							if (token.toLowerCase().startsWith(termToken.toLowerCase())) continue term;
						}
						return false;
					}
					return true;
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return name.matches(term);
				} else if ("find".equals(attr)) {
					return name.toLowerCase().indexOf(term.toLowerCase()) >= 0;
				}
				return false;
			}
		});
		/*
		if (getFindString()==null || getFindString().trim().length()==0) return true;
		StringTokenizer stk = new StringTokenizer(getFindString().toUpperCase()," ,");
		String n = name.toUpperCase();
		while (stk.hasMoreTokens()) {
			String token = stk.nextToken().trim();
			if (token.length()==0) continue;
			if (n.indexOf(token)<0) return false;
		}
		return true;*/
	}
	
    private static enum Size {
		eq, lt, gt, le, ge
	};
    
    private boolean match(final Location location) {
    	return iQuery == null || iQuery.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					for (StringTokenizer s = new StringTokenizer(location.getLabel(), " ,"); s.hasMoreTokens(); ) {
						String token = s.nextToken();
						if (term.equalsIgnoreCase(token)) return true;
					}
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return location.getLabel().matches(term);
				} else if ("find".equals(attr)) {
					return location.getLabel().toLowerCase().indexOf(term.toLowerCase()) >= 0;
				} else if ("type".equals(attr)) {
					return term.equalsIgnoreCase(location.getRoomType().getReference()) || term.equalsIgnoreCase(location.getRoomType().getLabel());
				} else if ("size".equals(attr)) {
					int min = 0, max = Integer.MAX_VALUE;
					Size prefix = Size.eq;
					String number = term;
					if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
					else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
					else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
					else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
					else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
					try {
						int a = Integer.parseInt(number);
						switch (prefix) {
							case eq: min = max = a; break; // = a
							case le: max = a; break; // <= a
							case ge: min = a; break; // >= a
							case lt: max = a - 1; break; // < a
							case gt: min = a + 1; break; // > a
						}
					} catch (NumberFormatException e) {}
					if (term.contains("..")) {
						try {
							String a = term.substring(0, term.indexOf('.'));
							String b = term.substring(term.indexOf("..") + 2);
							min = Integer.parseInt(a); max = Integer.parseInt(b);
						} catch (NumberFormatException e) {}
					}
					return min <= location.getCapacity() && location.getCapacity() <= max;
				}
				return false;
			}
		});
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
	
	public boolean reload(HttpServletRequest request, SessionContext context, SolverProxy solver) throws Exception {
		if (iModels!=null) iModels.clear();
		Session acadSession = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		DatePattern defaultDatePattern = acadSession.getDefaultDatePatternNotNull();
    	iDefaultDatePatternName = (defaultDatePattern==null?null:defaultDatePattern.getName());
    	TimetableGridContext cx = new TimetableGridContext(this, acadSession);
		if (solver!=null) {
			iModels = solver.getTimetableGridTables(cx);
			Collections.sort(iModels,new TimetableGridModelComparator());
			showUselessTimesIfDesired();
			return true;
		}
		String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
		if (solutionIdsStr==null || solutionIdsStr.length()==0) return false;
		Transaction tx = null;
		try {
			SolutionDAO dao = new SolutionDAO();
			org.hibernate.Session hibSession = dao.getSession();
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
				tx = hibSession.beginTransaction();
			
			if (getResourceType()==TimetableGridModel.sResourceTypeRoom) {
				if (RoomAvailability.getInstance() != null) {
			        Calendar startDateCal = Calendar.getInstance(Locale.US);
			        startDateCal.setTime(DateUtils.getDate(1, acadSession.getStartMonth(), acadSession.getSessionStartYear()));
			        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
			        startDateCal.set(Calendar.MINUTE, 0);
			        startDateCal.set(Calendar.SECOND, 0);
			        Calendar endDateCal = Calendar.getInstance(Locale.US);
			        endDateCal.setTime(DateUtils.getDate(0, acadSession.getEndMonth() + 1, acadSession.getSessionStartYear()));
			        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
			        endDateCal.set(Calendar.MINUTE, 59);
			        endDateCal.set(Calendar.SECOND, 59);
			        RoomAvailability.getInstance().activate(acadSession, startDateCal.getTime(), endDateCal.getTime(), RoomAvailabilityInterface.sClassType, false);
		            RoomAvailability.setAvailabilityWarning(request, acadSession, true, true);
				}
				
				Query q = hibSession.createQuery(
						"select distinct r from "+
						"Location as r inner join r.assignments as a where "+
						"a.solution.uniqueId in ("+solutionIdsStr+")");
				q.setCacheable(true);
				for (Iterator i=q.list().iterator();i.hasNext();) {
					Location room = (Location)i.next();
					if (!match(room)) continue;
					iModels.add(new SolutionGridModel(solutionIdsStr, room, hibSession, cx));
				}
			} else if (getResourceType()==TimetableGridModel.sResourceTypeInstructor) {
				if (RoomAvailability.getInstance() != null && getShowEvents()) {
			        Calendar startDateCal = Calendar.getInstance(Locale.US);
			        startDateCal.setTime(DateUtils.getDate(1, acadSession.getStartMonth(), acadSession.getSessionStartYear()));
			        startDateCal.set(Calendar.HOUR_OF_DAY, 0);
			        startDateCal.set(Calendar.MINUTE, 0);
			        startDateCal.set(Calendar.SECOND, 0);
			        Calendar endDateCal = Calendar.getInstance(Locale.US);
			        endDateCal.setTime(DateUtils.getDate(0, acadSession.getEndMonth() + 1, acadSession.getSessionStartYear()));
			        endDateCal.set(Calendar.HOUR_OF_DAY, 23);
			        endDateCal.set(Calendar.MINUTE, 59);
			        endDateCal.set(Calendar.SECOND, 59);
			        RoomAvailability.getInstance().activate(acadSession, startDateCal.getTime(), endDateCal.getTime(), RoomAvailabilityInterface.sClassType, false);
		            RoomAvailability.setAvailabilityWarning(request, acadSession, true, true);
				}
                String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
				Query q = null;
				if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
					q = hibSession.createQuery(
							"select distinct i.instructor from "+
							"ClassInstructor as i inner join i.classInstructing.assignments as a where "+
							"a.solution.uniqueId in ("+solutionIdsStr+")");
				} else {
					q = hibSession.createQuery(
							"select distinct i from "+
							"DepartmentalInstructor as i inner join i.assignments as a where "+
							"a.solution.uniqueId in ("+solutionIdsStr+")");
				}
				q.setCacheable(true);
				HashSet puids = new HashSet();
				for (Iterator i=q.list().iterator();i.hasNext();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
					String name = (instructor.getLastName()+", "+instructor.getFirstName()+" "+instructor.getMiddleName()).trim();
					if (!match(name)) continue;
					if (instructor.getExternalUniqueId()==null || instructor.getExternalUniqueId().length()<=0 || puids.add(instructor.getExternalUniqueId())) {
                        SolutionGridModel m = new SolutionGridModel(solutionIdsStr, instructor, hibSession, cx);
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
					iModels.add(new SolutionGridModel(solutionIdsStr, dept, hibSession, cx));
				}
			} else if (getResourceType()==TimetableGridModel.sResourceTypeSubjectArea) {
				Query q = hibSession.createQuery(
						"select distinct sa from "+
						"Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o inner join o.subjectArea as sa where "+
						"a.solution.uniqueId in ("+solutionIdsStr+") and o.isControl=true");
				q.setCacheable(true);
				for (Iterator i=q.list().iterator();i.hasNext();) {
					SubjectArea sa = (SubjectArea)i.next();
					String name = sa.getSubjectAreaAbbreviation();
					if (!match(name)) continue;
					iModels.add(new SolutionGridModel(solutionIdsStr, sa, hibSession, cx));
				}
			} else if (getResourceType()==TimetableGridModel.sResourceTypeCurriculum) {
				Query q = hibSession.createQuery(
						"select distinct cc.classification from "+
						"CurriculumCourse cc, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co where "+
						"a.solution.uniqueId in ("+solutionIdsStr+") and co = cc.course");
				q.setCacheable(true);
				for (Iterator i=q.list().iterator();i.hasNext();) {
					CurriculumClassification cc = (CurriculumClassification)i.next();
					String name = cc.getCurriculum().getAbbv() + " " + cc.getName();
					if (!match(name)) continue;
					iModels.add(new SolutionGridModel(solutionIdsStr, cc, hibSession, cx));
				}
			} else if (getResourceType()==TimetableGridModel.sResourceTypeStudentGroup) {
				Query q = hibSession.createQuery(
						"select distinct c from ConstraintInfo c inner join c.assignments a where a.solution.uniqueId in ("+solutionIdsStr+") and c.definition.name = 'GroupInfo'");
				q.setCacheable(true);
				for (Iterator i=q.list().iterator();i.hasNext();) {
					ConstraintInfo g = (ConstraintInfo)i.next();
					if (!match(g.getOpt()) && !match(g.getOpt())) continue;
					TimetableInfo info = g.getInfo();
					if (info != null && info instanceof StudentGroupInfo)
						iModels.add(new SolutionGridModel(solutionIdsStr, (StudentGroupInfo)info, hibSession, cx));
				}
				if (iModels.isEmpty()) {
					q = hibSession.createQuery(
							"select distinct r.group from StudentGroupReservation r, Assignment a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering as io where "+
							"a.solution.uniqueId in ("+solutionIdsStr+") and io = r.instructionalOffering");
					q.setCacheable(true);
					for (Iterator i=q.list().iterator();i.hasNext();) {
						StudentGroup g = (StudentGroup)i.next();
						if (!match(g.getGroupName()) && !match(g.getGroupAbbreviation())) continue;
						iModels.add(new SolutionGridModel(solutionIdsStr, g, hibSession, cx));
					}					
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
		
	public class TimetableGridModelComparator implements Comparator<TimetableGridModel> {
		public int compareModels(TimetableGridModel m1, TimetableGridModel m2) {
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
                case sOrderByUtilizationAsc :
                	return Double.compare(m1.getUtilization(), m2.getUtilization());
                case sOrderByUtilizationDesc :
                	return Double.compare(m2.getUtilization(), m1.getUtilization());
			}
			return 0;
		}
		
		public int compare(TimetableGridModel m1, TimetableGridModel m2) {
			int cmp = compareModels(m1, m2);
			if (cmp != 0) return cmp;
			return m1.getName().compareTo(m2.getName());
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
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required time</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred time</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred time</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No time preference</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged time</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged time</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited time</td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeRoomPref) {
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No room preference</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited room</td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeStudentConf) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = TimetableGridCell.conflicts2color(nrConflicts);
                out.println("<tr><td width='40' style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==15?"or more ":"")+"student conflicts</td><td></td></tr>");
            }
        } else if (iBgMode==TimetableGridModel.sBgModeInstructorBtbPref) {
            out.println("<tr><td idth=40 style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No instructor back-to-back preference <i>(distance=0)</i></td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged back-to-back <i>(0&lt;distance&lt;=5)</i></td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged back-to-back <i>(5&lt;distance&lt;=20)</i></td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited back-to-back <i>(20&lt;distance)</i></td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeDistributionConstPref) {
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No violated constraint<i>(distance=0)</i></td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged/preferred constraint violated</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged/preferred constraint violated</i></td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required/prohibited constraint violated</i></td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModePerturbations) {
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No change</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No initial assignment</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Room changed</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Time changed</i></td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Both time and room changed</i></td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModePerturbationPenalty) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = TimetableGridCell.conflicts2color(nrConflicts);
                out.println("<tr><td width='40' style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+(nrConflicts==0?"Zero perturbation penalty":nrConflicts==15?"Perturbation penalty above 15":"Perturbation penalty below or equal to "+nrConflicts)+"</td><td></td></tr>");
            }
        } else if (iBgMode==TimetableGridModel.sBgModeHardConflicts) {
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required time and room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in room with no hard conflict</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in room (but there is a hard conflict), can be moved in time with no conflict</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in room (but there is a hard conflict)</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in time with no hard conflict, cannot be moved in room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Can be moved in time (but there is a hard conflict), cannot be moved in room</td><td></td></tr>");
        } else if (iBgMode==TimetableGridModel.sBgModeDepartmentalBalancing) {
            for (int nrConflicts=0;nrConflicts<=3;nrConflicts++) {
                String color = TimetableGridCell.conflicts2colorFast(nrConflicts);
                out.println("<tr><td width='40' style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+(nrConflicts==0?"Zero penalty":nrConflicts==3?"Penalty equal or above 3":"Penalty equal to "+nrConflicts)+"</td><td></td></tr>");
            }
        } else if (iBgMode==TimetableGridModel.sBgModeTooBigRooms) {
        	out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is smaller than room limit of a class</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is not more than 25% bigger than the smallest avaialable room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is not more than 50% bigger than the smallest avaialable room</td><td></td></tr>");
            out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Assigned room is more than 50% bigger than the smallest avaialable room</td><td></td></tr>");
        } 
        out.println("<tr><td colspan='2'>Free times:</td></tr>");
        out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.sBgColorNotAvailable+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Time not available</td><td></td></tr>");
        out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No preference</td><td></td></tr>");
        if (iShowUselessTimes) {
        	out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Standard (MWF or TTh) time pattern is broken (time cannot be used for MW, WF, MF or TTh class)</td><td></td></tr>");
        	out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Useless half-hour</td><td></td></tr>");
        	out.println("<tr><td width='40' style='background-color:"+TimetableGridCell.pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Useless half-hour and broken standard time pattern</td><td></td></tr>");
        }
    }
}
