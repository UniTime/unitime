package org.unitime.timetable.webutil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.hibernate.Query;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.form.EventListForm;
import org.unitime.timetable.form.MeetingListForm;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;


public class WebEventTableBuilder {

	public static SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US);
	//public static SimpleDateFormat sDateFormatDay = new SimpleDateFormat("EEE", Locale.US);	
	public static SimpleDateFormat sDateFormatM1 = new SimpleDateFormat("MM/dd", Locale.US);
	public static SimpleDateFormat sDateFormatM2 = new SimpleDateFormat("MM/dd, yyyy", Locale.US);
	
	//Colors
    protected static String indent = "&nbsp;&nbsp;&nbsp;&nbsp;";
    protected static String oddRowBGColor = "#DFE7F2";
    protected static String oddRowBGColorChild = "#EFEFEF";
    protected static String oddRowMouseOverBGColor = "#8EACD0";
    protected static String evenRowMouseOverBGColor = "#8EACD0";
    protected static String headerBGColor1 = "#E0E0E0";
    protected static String headerBGColor2 = "#F4F4F4";
    protected String disabledColor = "gray";

    protected static String formName = "eventListForm";
    
    protected static String LABEL = "Name";
    protected static String EMPTY = "&nbsp;";
    public static final String EVENT_CAPACITY = "Capacity";
    public static final String EVENT_TYPE = "Type";
    public static final String MAIN_CONTACT = "Main Contact";
    public static final String SPONSORING_ORG = "Sponsoring Org";
    public static final String MEETING_DATE = "Date";
    public static final String MEETING_TIME = "Time";
    public static final String MEETING_LOCATION = "Location";
    public static final String APPROVED_DATE = "Approved";
    
    public WebEventTableBuilder() {
    	super();
    }
    
    protected String getRowMouseOver(boolean isHeaderRow, boolean isControl){
        return ("this.style.backgroundColor='" 
                + (isHeaderRow ?oddRowMouseOverBGColor:evenRowMouseOverBGColor) 
                + "';this.style.cursor='"
                + (isControl ? "hand" : "default")
                + "';this.style.cursor='"
                + (isControl ? "pointer" : "default")+ "';");
   	
    }
    
    protected String getRowMouseOut(String color){
        return ("this.style.backgroundColor='"+ (color==null?"transparent":color) +"';");   	
    }

    protected TableRow initRow(boolean isHeaderRow){
        TableRow row = new TableRow();
        if (isHeaderRow){
        	row.setBgColor(oddRowBGColor);
        }
        return (row);
    }
    
    protected TableHeaderCell headerCell(String content, int rowSpan, int colSpan){
    	TableHeaderCell cell = new TableHeaderCell();
    	cell.setRowSpan(rowSpan);
    	cell.setColSpan(colSpan);
    	cell.setAlign("left");
    	cell.setValign("bottom");
    	cell.addContent("<font size=\"-1\">");
    	cell.addContent(content);
    	cell.addContent("</font>");
    	return(cell);
     }
    
    private TableCell initCell(boolean isEditable, String onClick){
        return (initCell(isEditable, onClick, 1, false));
    }

    private TableCell initCell(boolean isEditable, String onClick, int cols){
        return (initCell(isEditable, onClick, cols, false));
    }

    private TableCell initCell(boolean isEditable, String onClick, int cols, boolean nowrap){
        TableCell cell = new TableCell();
        cell.setValign("top");
        if (cols > 1){
            cell.setColSpan(cols);
        }
        if (nowrap){
            cell.setNoWrap(true);
        }
        if (onClick != null && onClick.length() > 0){
        	cell.setOnClick(onClick);
        }
        if (!isEditable){
        	cell.addContent("<font color=" + disabledColor + ">");
        }
        return (cell);
    }

    private void endCell(TableCell cell, boolean isEditable){
        if (!isEditable){
            cell.addContent("</font>");
        }   
    }
   
    protected TableCell initNormalCell(String text, boolean isEditable){
        return (initColSpanCell(text, isEditable, 1));
    }
    
    private TableCell initColSpanCell(String text, boolean isEditable, int cols){
        TableCell cell = initCell(isEditable, null, cols);
        cell.addContent(text);
        endCell(cell, isEditable);
        return (cell);
        
    }
    
    protected void buildTableHeader(TableStream table, boolean mainContact){  
    	TableRow row = new TableRow();
    	row.setBgColor(headerBGColor1);
    	TableRow row2 = new TableRow();
    	row2.setBgColor(headerBGColor2);
     	TableHeaderCell cell = null;
        cell = this.headerCell(LABEL, 1, 1);
        row.addContent(cell);
        cell = this.headerCell(EMPTY, 1, 1);            
        cell.setStyle("border-bottom: gray 1px solid");
        row2.addContent(cell);
        cell = this.headerCell(EVENT_CAPACITY, 1, 1);
        row.addContent(cell);
        cell = this.headerCell(MEETING_DATE, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row2.addContent(cell);
        cell = this.headerCell(SPONSORING_ORG, 1, 1);
        row.addContent(cell);
        cell = this.headerCell(MEETING_TIME, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row2.addContent(cell);
        cell = this.headerCell(EVENT_TYPE, 1, 1);
        row.addContent(cell);
        cell = this.headerCell(MEETING_LOCATION, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row2.addContent(cell);
    	if (mainContact) {
    		cell = this.headerCell(MAIN_CONTACT, 1, 1);
    		row.addContent(cell);
    		cell = this.headerCell(APPROVED_DATE, 1, 1);
        	cell.setStyle("border-bottom: gray 1px solid");
    		row2.addContent(cell);
    	}
    	table.addContent(row);
    	table.addContent(row2);
   }

    protected void buildMeetingTableHeader(TableStream table, boolean mainContact){  
        TableRow row = new TableRow();
        row.setBgColor(headerBGColor1);
        TableHeaderCell cell = null;
        cell = this.headerCell(LABEL, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        cell = this.headerCell(EVENT_TYPE, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        cell = this.headerCell(EVENT_CAPACITY, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        cell = this.headerCell(SPONSORING_ORG, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        cell = this.headerCell(MEETING_DATE, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        cell = this.headerCell(MEETING_TIME, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        cell = this.headerCell(MEETING_LOCATION, 1, 1);
        cell.setStyle("border-bottom: gray 1px solid");
        row.addContent(cell);
        if (mainContact) {
            cell = this.headerCell(MAIN_CONTACT, 1, 1);
            cell.setStyle("border-bottom: gray 1px solid");
            row.addContent(cell);
            cell = this.headerCell(APPROVED_DATE, 1, 1);
            cell.setStyle("border-bottom: gray 1px solid");
            row.addContent(cell);
        }
        table.addContent(row);
   }

    private String subjectOnClickAction(Long eventId){
        return("document.location='eventDetail.do?op=view&id=" + eventId + "';");
    }    
    
    private TableCell buildEventName(Event e) {
        TableCell cell = this.initCell(true, null, 1, true);    	
        cell.addContent("<a name='A"+e.getUniqueId()+"'>"+(e.getEventName()==null?"":"<b>"+e.getEventName()+"</b>")+"</a>");
        this.endCell(cell, true);
        return (cell);
    }

    private TableCell buildEventCapacity(Event e) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	int minCap = (e.getMinCapacity()==null?-1:e.getMinCapacity());
    	int maxCap = (e.getMaxCapacity()==null?-1:e.getMaxCapacity());
    	if (minCap==-1){
    		cell.addContent("&nbsp;");
    	} else {
    		if (maxCap!=-1) {
    			if (maxCap!=minCap) {
        			cell.addContent(minCap+"-"+maxCap);    				
    			} else {cell.addContent(minCap);}
    		}
    	}
    	this.endCell(cell, true);
    	return (cell);
    }
    
    private TableCell buildSponsoringOrg(Event e) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent(e.getSponsoringOrganization()==null?"&nbsp;":e.getSponsoringOrganization().getName());
    	return(cell);
    }

    private TableCell buildEventType(Event e) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent(e.getEventTypeLabel());
    	return(cell);
    }
    
    private TableCell buildEventTypeAbbv(Event e) {
        TableCell cell = this.initCell(true, null, 1, true);
        cell.addContent(e.getEventTypeAbbv());
        return(cell);
    }
    
    private TableCell buildMainContactName(Event e) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	if (e.getMainContact()!=null)
    	    cell.addContent((e.getMainContact().getLastName()==null?"":(e.getMainContact().getLastName()+", "))+
    	    			(e.getMainContact().getFirstName()==null?"":e.getMainContact().getFirstName()));
    	return(cell);
    }
    
    private TableCell buildEmptyEventInfo() {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent("");
    	return(cell);
    }
    
    private TableCell buildEmptyMeetingInfo() {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent("");
    	return(cell);
    }
 
    private TableCell buildDate (Meeting m) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent(sDateFormat.format(m.getMeetingDate()));
    	/*
    			+" &nbsp;&nbsp;<font color='gray'><i>("
    			+sDateFormatDay.format(m.getMeetingDate())
    			+")</i></font>"); //date cannot be null*/
    	return(cell);
    }
    
    private TableCell buildDate (MultiMeeting m) {
        TableCell cell = this.initCell(true, null, 1, true);
        Calendar c = Calendar.getInstance();
        c.setTime(m.getMeetings().first().getMeetingDate());
        int y1 = c.get(Calendar.YEAR);
        c.setTime(m.getMeetings().last().getMeetingDate());
        int y2 = c.get(Calendar.YEAR);
        cell.addContent(
                m.getDays()+" "+
                (y1==y2?sDateFormatM1:sDateFormatM2).format(m.getMeetings().first().getMeetingDate())
                +" - "+
                sDateFormatM2.format(m.getMeetings().last().getMeetingDate())
                );
        return(cell);
    }

    private TableCell buildTime (Meeting m) {
    	TableCell cell = this.initCell(true, null, 1, true);
		int start = Constants.SLOT_LENGTH_MIN*m.getStartPeriod()+
			Constants.FIRST_SLOT_TIME_MIN+
			(m.getStartOffset()==null?0:m.getStartOffset());
		int startHour = start/60;
		int startMin = start%60;
		int end = Constants.SLOT_LENGTH_MIN*m.getStopPeriod()+
			Constants.FIRST_SLOT_TIME_MIN+
			(m.getStopOffset()==null?0:m.getStopOffset());
		int endHour = end/60;
		int endMin = end%60;
		cell.addContent((startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a")+ "-" + 
						(endHour>12?endHour-12:endHour)+":"+(endMin<10?"0":"")+endMin+(endHour>=12?"p":"a")); //time cannot be null
		return(cell);
    }
    
    private TableCell buildLocation (Meeting m) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent(m.getLocation()==null?"":m.getLocation().getLabel());
    	return(cell);
    }
    
    private TableCell buildApproved (Meeting m) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
    	cell.addContent(m.getApprovedDate()==null?"&nbsp;":df.format(m.getApprovedDate()));
    	return(cell);
    }
    
    private TableCell buildApproved (MultiMeeting mm) {
        TableCell cell = this.initCell(true, null, 1, true);
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Date approvalDate = null; //latest approval date
        for (Meeting m : mm.getMeetings())
            if (approvalDate==null || approvalDate.compareTo(m.getApprovedDate())<0) approvalDate = m.getApprovedDate();
        cell.addContent(approvalDate==null?"&nbsp;":df.format(approvalDate));
        return(cell);
    }

    private void addEventsRowsToTable(TableStream table, Event e, boolean mainContact, TreeSet<MultiMeeting> meetings) {
        TableRow row = (this.initRow(true));
        row.setOnMouseOver(this.getRowMouseOver(true, true));
        row.setOnMouseOut(this.getRowMouseOut(oddRowBGColor));
        row.setOnClick(subjectOnClickAction(e.getUniqueId()));
        
        TableCell cell = null;
        row.addContent(buildEventName(e));
        row.addContent(buildEventCapacity(e));
        row.addContent(buildSponsoringOrg(e));
        row.addContent(buildEventType(e));
        if (mainContact)
            row.addContent(buildMainContactName(e));
        boolean allPast = true;
        for (MultiMeeting meeting : meetings) {
            if (!meeting.isPast()) { allPast = false; break; }
        }
        if (allPast) row.setStyle("font-style:italic;color:gray;");
        table.addContent(row);
    }
    
    private void addMeetingRowsToTable (TableStream table, MultiMeeting mm, boolean mainContact) {
        Meeting m = mm.getMeetings().first();
        TableRow row = (this.initRow(false));
        row.setOnMouseOver(this.getRowMouseOver(false, true));
        row.setOnClick(subjectOnClickAction(m.getEvent().getUniqueId()));
        row.addContent(buildEmptyMeetingInfo());
        row.addContent(mm.getMeetings().size()==1?buildDate(m):buildDate(mm));
        row.addContent(buildTime(m));
        row.addContent(buildLocation(m));
        String bgColor = null;
        if (mainContact)
            row.addContent(mm.getMeetings().size()==1?buildApproved(m):buildApproved(mm));
        if (mm.isPast()) {
            row.setStyle("font-style:italic;color:gray;");
        } else {
            if (m.isApproved()) {
                //bgColor = "#DDFFDD";
            } else {
                bgColor = "#FFFFDD";
            }
        }
        row.setBgColor(bgColor);
        row.setOnMouseOut(getRowMouseOut(bgColor));
        table.addContent(row);
    }
    
    private void addMeetingRowsToTable (TableStream table, Meeting m, boolean mainContact, Event lastEvent, Date now, boolean line) {
        TableRow row = (this.initRow(false));
        row.setOnMouseOver(this.getRowMouseOver(false, true));
        row.setOnClick(subjectOnClickAction(m.getEvent().getUniqueId()));
        if (lastEvent!=null && lastEvent.getUniqueId().equals(m.getEvent().getUniqueId())) {
            TableCell cell = this.initCell(true, null, 1, true);
            cell.addContent("&nbsp;");
            row.addContent(cell);
            row.addContent(cell);
            row.addContent(cell);
            row.addContent(cell);
        } else {
            row.addContent(buildEventName(m.getEvent()));
            row.addContent(buildEventTypeAbbv(m.getEvent()));
            row.addContent(buildEventCapacity(m.getEvent()));
            row.addContent(buildSponsoringOrg(m.getEvent()));
        }
        row.addContent(buildDate(m));
        row.addContent(buildTime(m));
        row.addContent(buildLocation(m));
        String bgColor = null;
        if (mainContact) {
            if (lastEvent!=null && lastEvent.getUniqueId().equals(m.getEvent().getUniqueId())) {
                TableCell cell = this.initCell(true, null, 1, true);
                cell.addContent("&nbsp;");
                row.addContent(cell);
            } else {
                row.addContent(buildMainContactName(m.getEvent()));
            }
            row.addContent(buildApproved(m));
        }
        if (m.getStartTime().before(now)) {
            row.setStyle("font-style:italic;color:gray;");
        } else {
            if (m.isApproved()) {
                //bgColor = "#DDFFDD";
            } else {
                bgColor = "#FFFFDD";
            }
        }
        row.setBgColor(bgColor);
        row.setOnMouseOut(getRowMouseOut(bgColor));
        if (line && lastEvent!=null && !lastEvent.getUniqueId().equals(m.getEvent().getUniqueId())) {
            for (Iterator i=row.getContents().iterator();i.hasNext();)
                ((TableCell)i.next()).setStyle("border-top: gray 1px solid;");
        }
        table.addContent(row);
    }
    
    public void htmlTableForEvents (HttpSession httpSession, EventListForm form, JspWriter outputStream){

        ArrayList eventIds = new ArrayList();
        
        String query = "select distinct e from Event e inner join e.meetings m where e.class in (";
        for (int i=0;i<form.getEventTypes().length;i++) {
        	if (i>0) query+=",";
        	switch (form.getEventTypes()[i].intValue()) {
        	case Event.sEventTypeClass : query += "ClassEvent"; break;
        	case Event.sEventTypeFinalExam : query += "FinalExamEvent"; break;
        	case Event.sEventTypeMidtermExam : query += "MidtermExamEvent"; break;
        	case Event.sEventTypeCourse : query += "CourseEvent"; break;
        	case Event.sEventTypeSpecial : query += "SpecialEvent"; break;
        	}
        	//query += form.getEventTypes()[i];
        }
        query += ")";
        
        if (form.getEventNameSubstring()!=null && form.getEventNameSubstring().trim().length()>0) {
        	query += " and upper(e.eventName) like :eventNameSubstring";
        }
        
        if (form.getEventDateFrom()!=null && form.getEventDateFrom().trim().length()>0) {
        	query += " and m.meetingDate>=:eventDateFrom";
        }
        
        if (form.getEventDateTo()!=null && form.getEventDateTo().trim().length()>0) {
        	query += " and m.meetingDate<=:eventDateTo";
        }
                
        if (form.getEventMainContactSubstring()!=null && form.getEventMainContactSubstring().trim().length()>0) {
        	for (StringTokenizer s=new StringTokenizer(form.getEventMainContactSubstring().trim(),", ");s.hasMoreTokens();) {
        		String token = s.nextToken();
        		query += " and (e.mainContact.firstName like '%"+token+"%' or e.mainContact.middleName like '%"+token+"%' or e.mainContact.lastName like '%"+token+"%')";
        	}
        }
        
        switch (form.getMode()) {
            case EventListForm.sModeMyEvents :
                query += " and e.mainContact.externalUniqueId = :userId";
                break;
            case EventListForm.sModeAllApprovedEvents :
                query += " and m.approvedDate is not null";
                break;
            case EventListForm.sModeAllEventsWaitingApproval :
                query += " and m.approvedDate is null";
                break;
            case EventListForm.sModeEvents4Approval :
                query += " and m.approvedDate is null";
                break;
            case EventListForm.sModeAllEvents : 
                break;
        }
        
        if (form.getSponsoringOrganization()!=null && form.getSponsoringOrganization()>=0) {
            query += " and e.sponsoringOrganization.uniqueId=:sponsorOrgId";
        }

        query += " order by e.eventName, e.uniqueId";
        
        Query hibQuery = new EventDAO().getSession().createQuery(query);
        
        if (form.getEventNameSubstring()!=null && form.getEventNameSubstring().trim().length()>0) {
        	hibQuery.setString("eventNameSubstring", "%"+form.getEventNameSubstring().toUpperCase().trim()+"%");
        }
        
        if (form.getEventDateFrom()!=null && form.getEventDateFrom().trim().length()>0) {
        	try {
        		hibQuery.setDate("eventDateFrom", new SimpleDateFormat("MM/dd/yyyy").parse(form.getEventDateFrom()));
        	} catch (ParseException ex) {
        		hibQuery.setDate("eventDateFrom", new Date());
        	}
        }
        
        if (form.getEventDateTo()!=null && form.getEventDateTo().trim().length()>0) {
        	try {
        		hibQuery.setDate("eventDateTo", new SimpleDateFormat("MM/dd/yyyy").parse(form.getEventDateTo()));
        	} catch (ParseException ex) {
        		hibQuery.setDate("eventDateTo", new Date());
        	}
        }
        
        if (form.getSponsoringOrganization()!=null && form.getSponsoringOrganization()>=0) {
            hibQuery.setLong("sponsorOrgId", form.getSponsoringOrganization());
        }
        
        switch (form.getMode()) {
            case EventListForm.sModeMyEvents :
                hibQuery.setString("userId", form.getUserId());
                break;
            case EventListForm.sModeAllApprovedEvents :
            case EventListForm.sModeAllEventsWaitingApproval :
            case EventListForm.sModeEvents4Approval :
                break;
        }
        
        List events = hibQuery.setCacheable(true).list();
        int numberOfEvents = events.size();
        
        TableStream eventsTable = this.initTable(outputStream);
        if (numberOfEvents>100 && form.getMode()!=EventListForm.sModeEvents4Approval) {
        	TableRow row = new TableRow();
        	TableCell cell = initCell(true, null, 5, false);
        	cell.addContent("Warning: There are more than 100 events matching your search criteria. Only the first 100 events are displayed. Please, redefine the search criteria in your filter.");
        	cell.setStyle("padding-bottom:10px;color:red;font-weight:bold;");
        	row.addContent(cell);
        	eventsTable.addContent(row);
        }
        if (numberOfEvents==0) {
        	TableRow row = new TableRow();
        	TableCell cell = initCell(true, null, 5, false);
        	cell.addContent("No events matching the search criteria were found.");
        	cell.setStyle("padding-bottom:10px;color:red;font-weight:bold;");
        	row.addContent(cell);
        	eventsTable.addContent(row);
        } else buildTableHeader(eventsTable, form.isAdmin() || form.isEventManager());

        int idx = 0;
        for (Iterator it = events.iterator();it.hasNext();idx++){
            Event event = (Event) it.next();
            if (form.getMode()==EventListForm.sModeEvents4Approval) {
                boolean myApproval = false;
                for (Iterator j=event.getMeetings().iterator();j.hasNext();) {
                    Meeting m = (Meeting)j.next();
                    if (m.getApprovedDate()==null && m.getLocation()!=null && form.getManagingDepartments().contains(m.getLocation().getControllingDepartment())) {
                        myApproval = true; break;
                    }
                }
                if (!myApproval) continue;
            } else  if (idx==100) break;
            eventIds.add(event.getUniqueId());
            TreeSet<MultiMeeting> meetings = event.getMultiMeetings();
            addEventsRowsToTable(eventsTable, event, form.isAdmin() || form.isEventManager(), meetings);
            for (MultiMeeting meeting : meetings) 
                addMeetingRowsToTable(eventsTable, meeting, form.isAdmin() || form.isEventManager());
        }

        eventsTable.tableComplete();
        Navigation.set(httpSession, Navigation.sInstructionalOfferingLevel, eventIds);
    }
    
    public boolean match(String filter, String name) {
        if (filter==null || filter.trim().length()==0) return true;
        String n = (name==null?"":name).toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(filter.toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*")+"\\s";
                        if (!Pattern.compile(tokenRegExp).matcher(" "+n+" ").find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;
    }

    public void htmlTableForMeetings(HttpSession httpSession, MeetingListForm form, JspWriter outputStream){

        ArrayList eventIds = new ArrayList();
        
        String query = "select m from Event e inner join e.meetings m where e.class in (";
        for (int i=0;i<form.getEventTypes().length;i++) {
            if (i>0) query+=",";
            switch (form.getEventTypes()[i].intValue()) {
            case Event.sEventTypeClass : query += "ClassEvent"; break;
            case Event.sEventTypeFinalExam : query += "FinalExamEvent"; break;
            case Event.sEventTypeMidtermExam : query += "MidtermExamEvent"; break;
            case Event.sEventTypeCourse : query += "CourseEvent"; break;
            case Event.sEventTypeSpecial : query += "SpecialEvent"; break;
            }
            //query += form.getEventTypes()[i];
        }
        query += ")";
        
        if (form.getEventNameSubstring()!=null && form.getEventNameSubstring().trim().length()>0) {
            query += " and upper(e.eventName) like :eventNameSubstring";
        }
        
        if (form.getEventDateFrom()!=null && form.getEventDateFrom().trim().length()>0) {
            query += " and m.meetingDate>=:eventDateFrom";
        }
        
        if (form.getEventDateTo()!=null && form.getEventDateTo().trim().length()>0) {
            query += " and m.meetingDate<=:eventDateTo";
        }
                
        if (form.getEventMainContactSubstring()!=null && form.getEventMainContactSubstring().trim().length()>0) {
            for (StringTokenizer s=new StringTokenizer(form.getEventMainContactSubstring().trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken();
                query += " and (e.mainContact.firstName like '%"+token+"%' or e.mainContact.middleName like '%"+token+"%' or e.mainContact.lastName like '%"+token+"%')";
            }
        }
        
        switch (form.getMode()) {
            case EventListForm.sModeMyEvents :
                query += " and e.mainContact.externalUniqueId = :userId";
                break;
            case EventListForm.sModeAllApprovedEvents :
                query += " and m.approvedDate is not null";
                break;
            case EventListForm.sModeAllEventsWaitingApproval :
                query += " and m.approvedDate is null";
                break;
            case EventListForm.sModeEvents4Approval :
                query += " and m.approvedDate is null";
                break;
            case EventListForm.sModeAllEvents : 
                break;
        }
        
        if (form.getSponsoringOrganization()!=null && form.getSponsoringOrganization()>=0) {
            query += " and e.sponsoringOrganization.uniqueId=:sponsorOrgId";
        }

        Query hibQuery = new EventDAO().getSession().createQuery(query);
        
        if (form.getEventNameSubstring()!=null && form.getEventNameSubstring().trim().length()>0) {
            hibQuery.setString("eventNameSubstring", "%"+form.getEventNameSubstring().toUpperCase().trim()+"%");
        }
        
        if (form.getEventDateFrom()!=null && form.getEventDateFrom().trim().length()>0) {
            try {
                hibQuery.setDate("eventDateFrom", new SimpleDateFormat("MM/dd/yyyy").parse(form.getEventDateFrom()));
            } catch (ParseException ex) {
                hibQuery.setDate("eventDateFrom", new Date());
            }
        }
        
        if (form.getEventDateTo()!=null && form.getEventDateTo().trim().length()>0) {
            try {
                hibQuery.setDate("eventDateTo", new SimpleDateFormat("MM/dd/yyyy").parse(form.getEventDateTo()));
            } catch (ParseException ex) {
                hibQuery.setDate("eventDateTo", new Date());
            }
        }
        
        if (form.getSponsoringOrganization()!=null && form.getSponsoringOrganization()>=0) {
            hibQuery.setLong("sponsorOrgId", form.getSponsoringOrganization());
        }
        
        switch (form.getMode()) {
            case EventListForm.sModeMyEvents :
                hibQuery.setString("userId", form.getUserId());
                break;
            case EventListForm.sModeAllApprovedEvents :
            case EventListForm.sModeAllEventsWaitingApproval :
            case EventListForm.sModeEvents4Approval :
                break;
        }
        
        List meetings = hibQuery.setCacheable(true).list();
        if (form.getMode()==EventListForm.sModeEvents4Approval || (form.getLocation()!=null && form.getLocation().trim().length()>0)) {
            for (Iterator it = meetings.iterator();it.hasNext();){
                Meeting meeting = (Meeting) it.next();
                if (form.getMode()==EventListForm.sModeEvents4Approval) {
                    if (meeting.getApprovedDate()!=null || meeting.getLocation()==null || 
                            !form.getManagingDepartments().contains(meeting.getLocation().getControllingDepartment())) {
                        it.remove(); continue;
                    }
                }
                if (meeting.getLocation()==null || !match(form.getLocation(), meeting.getLocation().getLabel())) {
                    it.remove(); continue;
                }
            }
        }
        int numberOfMeetings = meetings.size();
        Comparator<Meeting> cmp = null;
        if (MeetingListForm.sOrderByName.equals(form.getOrderBy())) {
            cmp = new Comparator<Meeting>() {
                public int compare(Meeting m1, Meeting m2) {
                    int cmp = m1.getEvent().getEventName().compareToIgnoreCase(m2.getEvent().getEventName());
                    if (cmp!=0) return cmp;
                    cmp = m1.getEvent().getUniqueId().compareTo(m2.getEvent().getUniqueId());
                    if (cmp!=0) return cmp;
                    return m1.compareTo(m2);
                }
            };
        } else if (MeetingListForm.sOrderByLocation.equals(form.getOrderBy())) {
            cmp = new Comparator<Meeting>() {
                public int compare(Meeting m1, Meeting m2) {
                    String l1 = (m1.getLocation()==null?"":m1.getLocation().getLabel());
                    String l2 = (m2.getLocation()==null?"":m2.getLocation().getLabel());
                    int cmp = l1.compareToIgnoreCase(l2);
                    if (cmp!=0) return cmp;
                    return m1.compareTo(m2);
                }
            };
        } else if (MeetingListForm.sOrderByLocation.equals(form.getOrderBy())) {
            cmp = new Comparator<Meeting>() {
                public int compare(Meeting m1, Meeting m2) {
                    return m1.compareTo(m2);
                }
            };
        }

        if (cmp!=null)
            Collections.sort(meetings,cmp); 
        
        TableStream eventsTable = this.initTable(outputStream);
        if (numberOfMeetings>100 && form.getMode()!=EventListForm.sModeEvents4Approval) {
            TableRow row = new TableRow();
            TableCell cell = initCell(true, null, 5, false);
            cell.addContent("Warning: There are more than 500 meetings matching your search criteria. Only the first 500 meetings are displayed. Please, redefine the search criteria in your filter.");
            cell.setStyle("padding-bottom:10px;color:red;font-weight:bold;");
            row.addContent(cell);
            eventsTable.addContent(row);
        }
        if (numberOfMeetings==0) {
            TableRow row = new TableRow();
            TableCell cell = initCell(true, null, 5, false);
            cell.addContent("No meetings matching the search criteria were found.");
            cell.setStyle("padding-bottom:10px;color:red;font-weight:bold;");
            row.addContent(cell);
            eventsTable.addContent(row);
        } else buildMeetingTableHeader(eventsTable, form.isAdmin() || form.isEventManager());

        int idx = 0;
        HashSet<Long> eventIdsHash = new HashSet();
        Event lastEvent = null;
        Date now = new Date();
        boolean line = MeetingListForm.sOrderByName.equals(form.getOrderBy());
        for (Iterator it = meetings.iterator();it.hasNext();idx++){
            Meeting meeting = (Meeting) it.next();
            if (idx==500) break;
            if (eventIdsHash.add(meeting.getEvent().getUniqueId())) eventIds.add(meeting.getEvent().getUniqueId());
            addMeetingRowsToTable(eventsTable, meeting, form.isAdmin() || form.isEventManager(), lastEvent, now, line);
            lastEvent = meeting.getEvent();
        }

        eventsTable.tableComplete();
        Navigation.set(httpSession, Navigation.sInstructionalOfferingLevel, eventIds);
    }
        
    protected TableStream initTable(JspWriter outputStream){
    	TableStream table = new TableStream(outputStream);
        table.setWidth("90%");
        table.setBorder(0);
        table.setCellSpacing(0);
        table.setCellPadding(3);
        table.tableDefComplete();
        return(table);
    }            
   
}
