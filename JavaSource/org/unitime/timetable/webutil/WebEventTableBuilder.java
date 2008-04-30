package org.unitime.timetable.webutil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.hibernate.Query;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.form.EventListForm;
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
    
    protected String getRowMouseOut(boolean isHeaderRow){
        return ("this.style.backgroundColor='"  + (isHeaderRow ?oddRowBGColor:"transparent") + "';");   	
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
    
    protected void buildTableHeader(TableStream table){  
    	TableRow row = new TableRow();
    	row.setBgColor(headerBGColor1);
    	TableRow row2 = new TableRow();
    	row2.setBgColor(headerBGColor2);
     	TableHeaderCell cell = null;
//    	if (isShowLabel()){
    		cell = this.headerCell(LABEL, 1, 1);
    		row.addContent(cell);
    		cell = this.headerCell(EMPTY, 1, 1);    		
        	cell.setStyle("border-bottom: gray 1px solid");
    		row2.addContent(cell);
//    	}
//    	if (isShowDivSec()){
    		cell = this.headerCell(EVENT_CAPACITY, 1, 1);
    		row.addContent(cell);
    		cell = this.headerCell(MEETING_DATE, 1, 1);
        	cell.setStyle("border-bottom: gray 1px solid");
    		row2.addContent(cell);
//    	}   	
//    	if (isShowDemand()){
    		cell = this.headerCell(SPONSORING_ORG, 1, 1);
    		row.addContent(cell);
    		cell = this.headerCell(MEETING_TIME, 1, 1);
        	cell.setStyle("border-bottom: gray 1px solid");
    		row2.addContent(cell);
//    	}
//    	if (isShowProjectedDemand()){
    		cell = this.headerCell(EVENT_TYPE, 1, 1);
    		row.addContent(cell);
    		cell = this.headerCell(MEETING_LOCATION, 1, 1);
        	cell.setStyle("border-bottom: gray 1px solid");
    		row2.addContent(cell);
//    	if (isShowManager()){
    		cell = this.headerCell(MAIN_CONTACT, 1, 1);
    		row.addContent(cell);
    		cell = this.headerCell(APPROVED_DATE, 1, 1);
        	cell.setStyle("border-bottom: gray 1px solid");
    		row2.addContent(cell);
//    	}
    	table.addContent(row);
    	table.addContent(row2);
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
    		cell.addContent("");
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
    	cell.addContent("");
    	return(cell);
    }

    private TableCell buildEventType(Event e) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	cell.addContent(e.getEventType().getLabel());
    	return(cell);
    }
    
    private TableCell buildMainContactName(Event e) {
    	TableCell cell = this.initCell(true, null, 1, true);
    	if (e.getMainContact()!=null)
    	    cell.addContent(e.getMainContact().getLastName()+", "+e.getMainContact().getFirstName());
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
    	cell.addContent(m.getApprovedDate()==null?"":df.format(m.getApprovedDate()));
    	return(cell);
    }
    
    private TableCell buildApproved (MultiMeeting mm) {
        TableCell cell = this.initCell(true, null, 1, true);
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Date approvalDate = null; //latest approval date
        for (Meeting m : mm.getMeetings())
            if (approvalDate==null || approvalDate.compareTo(m.getApprovedDate())<0) approvalDate = m.getApprovedDate();
        cell.addContent(approvalDate==null?"":df.format(approvalDate));
        return(cell);
    }

    private void addEventsRowsToTable(TableStream table, Event e) {
        TableRow row = (this.initRow(true));
        row.setOnMouseOver(this.getRowMouseOver(true, true));
        row.setOnMouseOut(this.getRowMouseOut(true));
        row.setOnClick(subjectOnClickAction(e.getUniqueId()));
        
        TableCell cell = null;
        row.addContent(buildEventName(e));
        row.addContent(buildEventCapacity(e));
        row.addContent(buildSponsoringOrg(e));
        row.addContent(buildEventType(e));
        row.addContent(buildMainContactName(e));
        table.addContent(row);
    }
    
    private void addMeetingRowsToTable (TableStream table, Meeting m) {
        TableRow row = (this.initRow(false));
        row.setOnMouseOver(this.getRowMouseOver(false, true));
        row.setOnMouseOut(this.getRowMouseOut(false));
        row.setOnClick(subjectOnClickAction(m.getEvent().getUniqueId()));
        
        TableCell cell = null;
        row.addContent(buildEmptyMeetingInfo());
        row.addContent(buildDate(m));
        row.addContent(buildTime(m));
        row.addContent(buildLocation(m));
        row.addContent(buildApproved(m));
        table.addContent(row);
    }
    
    private void addMeetingRowsToTable (TableStream table, MultiMeeting mm) {
        Meeting m = mm.getMeetings().first();
        TableRow row = (this.initRow(false));
        row.setOnMouseOver(this.getRowMouseOver(false, true));
        row.setOnMouseOut(this.getRowMouseOut(false));
        row.setOnClick(subjectOnClickAction(m.getEvent().getUniqueId()));
        
        TableCell cell = null;
        row.addContent(buildEmptyMeetingInfo());
        row.addContent(mm.getMeetings().size()==1?buildDate(m):buildDate(mm));
        row.addContent(buildTime(m));
        row.addContent(buildLocation(m));
        row.addContent(mm.getMeetings().size()==1?buildApproved(m):buildApproved(mm));
        table.addContent(row);
    }
    
    public void htmlTableForEvents (HttpSession httpSession, EventListForm form, JspWriter outputStream){

        ArrayList eventIds = new ArrayList();
        
        String query = "select distinct e from Event e inner join e.meetings m where e.eventType.reference in (";
        for (int i=0;i<form.getEventTypes().length;i++) {
        	if (i>0) query+=",";
        	query += "'"+form.getEventTypes()[i]+"'";
        }
        query += ")";
        
        if (form.getEventNameSubstring()!=null && form.getEventNameSubstring().trim().length()>0) {
        	query += " and e.eventName like :eventNameSubstring";
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

        query += " order by e.eventName, e.uniqueId";
        
        Query hibQuery = new EventDAO().getSession().createQuery(query);
        
        if (form.getEventNameSubstring()!=null && form.getEventNameSubstring().trim().length()>0) {
        	hibQuery.setString("eventNameSubstring", "%"+form.getEventNameSubstring().trim()+"%");
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
        
        List events = hibQuery.setCacheable(true).list();
        int numberOfEvents = events.size();
        
        /*
        TreeSet events = new TreeSet(new Comparator<Event>(){
        	public int compare(Event e1, Event e2) {
        		int cmp = e1.getEventName().compareTo(e2.getEventName());
        		if (cmp!=0) return cmp;
        		return e1.getUniqueId().compareTo(e2.getUniqueId());
        	}
        });
        events.addAll(Event.findAll());*/

        /*
        List events = Event.findAll();
        Collections.sort(events, new Comparator<Event>(){
        	public int compare(Event e1, Event e2) {
        		int cmp = e1.getEventName().compareTo(e2.getEventName());
        		if (cmp!=0) return cmp;
        		return e1.getUniqueId().compareTo(e2.getUniqueId());
        	}
        });
        */
        
        TableStream eventsTable = this.initTable(outputStream);
        if (numberOfEvents>100) {
        	TableRow row = new TableRow();
        	TableCell cell = initCell(true, null, 5, false);
        	cell.addContent("Warning: There are more than 100 events matching your search criteria. Only the first 100 events are displayed. Please, redefine the search criteria in your filter.");
        	cell.setStyle("padding-bottom:10px;color:red;font-weight:bold;");
        	row.addContent(cell);
        	eventsTable.addContent(row);
        }
        buildTableHeader(eventsTable);

        int idx = 0;
        for (Iterator it = events.iterator();it.hasNext();idx++){
        	if (idx==100) break;
            Event event = (Event) it.next();
            eventIds.add(event.getUniqueId());
            addEventsRowsToTable(eventsTable, event);
            for (MultiMeeting meeting : event.getMultiMeetings()) 
                addMeetingRowsToTable(eventsTable, meeting);
            /*
			for (Iterator i=new TreeSet(event.getMeetings()).iterator();i.hasNext();) {
				Meeting meeting = (Meeting)i.next();
				addMeetingRowsToTable(eventsTable, meeting);
			}
			*/
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
