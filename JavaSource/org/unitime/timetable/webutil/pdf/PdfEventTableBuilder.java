/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.webutil.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.jsp.JspWriter;

import org.unitime.commons.Debug;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EventListForm;
import org.unitime.timetable.form.MeetingListForm;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.PdfEventHandler;
import org.unitime.timetable.util.PdfFont;
import org.unitime.timetable.webutil.WebEventTableBuilder;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;


public class PdfEventTableBuilder extends WebEventTableBuilder {
    protected PdfWriter iWriter = null;
    protected Document iDocument = null;
    protected PdfPTable iPdfTable = null;

    protected Color iBgColor = Color.WHITE;
    protected boolean iUnderline = false;
    protected boolean iOverline = false;

    protected Color iTextColor = Color.BLACK;
    protected boolean iTextItalic = false;

    public PdfEventTableBuilder() {
    	super();
    }
    
    public int getMaxResults() {
    	return 1500;
    }
    
    public PdfPCell createCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(Color.BLACK);
        cell.setPadding(3);
        cell.setBorderWidth(0);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(iBgColor);
        if (iUnderline) cell.setBorderWidthBottom(1);
        if (iOverline) cell.setBorderWidthTop(0.5f);
        return cell;
    }
    
    public void addText(PdfPCell cell, String text) {
        addText(cell, text, false, iTextItalic, Element.ALIGN_LEFT, iTextColor, true);
    }
    
    public void addText(PdfPCell cell, String text, boolean bold, int orientation) {
        addText(cell, text, bold, iTextItalic, orientation, iTextColor, true);
    }

    public void addText(PdfPCell cell, String text, int orientation) {
        addText(cell, text, false, iTextItalic, orientation, iTextColor, true);
    }
    
    public void addText(PdfPCell cell, String text, boolean bold, boolean italic,  int orientation, Color color, boolean newLine) {
        if (text==null) return;
        if (cell.getPhrase()==null) {
            Chunk ch = new Chunk(text, PdfFont.getSmallFont(bold, italic, color));
            cell.setPhrase(new Paragraph(ch));
            cell.setVerticalAlignment(Element.ALIGN_TOP);
            cell.setHorizontalAlignment(orientation);
        } else {
            cell.getPhrase().add(new Chunk((newLine?"\n":"")+text,PdfFont.getSmallFont(bold, italic, color)));
        }
    }
    
    public float[] getWidths(boolean events, boolean mainContact) {
        if (events) {
            if (mainContact) return new float[] {200, 90, 80, 100, 100, 120};
            else return new float[] {200, 90, 80, 100, 100};
        } else {
            if (mainContact) return new float[] {150, 85, 50, 50, 100, 110, 90, 80, 120, 75};
            return new float[] {200, 85, 75, 50, 150, 100, 100, 100};
        }
    }
    
    protected void pdfBuildTableHeader(boolean events, boolean mainContact, int numEventsOrMeetings) {
        if (events) {
        	PdfPCell c;
        	if (numEventsOrMeetings > getMaxResults()) {
            	c = createCell();
                addText(c, "**Warning: More than " + getMaxResults() + " events match your search criteria. Only the first " + getMaxResults() + " events are displayed. Please, redefine the search criteria in your filter.", true, Element.ALIGN_LEFT);
                c.setColspan(mainContact?6:5);
                iPdfTable.addCell(c);
            }
            iBgColor = new Color(224,224,224);
            //first line
            c = createCell();
            addText(c, LABEL, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, ENROLLMENT, true, Element.ALIGN_RIGHT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, EVENT_CAPACITY, true, Element.ALIGN_RIGHT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, SPONSORING_ORG, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, EVENT_TYPE, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            if (mainContact) {
                c = createCell();
                addText(c, MAIN_CONTACT, true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
            }
            //second line
            iBgColor = new Color(244,244,244);
            iUnderline = true;
            c = createCell();
            iPdfTable.addCell(c);
            c = createCell();
            c.setColspan(2);
            addText(c, MEETING_DATE, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, MEETING_TIME, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, MEETING_LOCATION, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            if (mainContact) {
                c = createCell();
                addText(c, APPROVED_DATE, true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
            }
            if (numEventsOrMeetings <= getMaxResults()){
            	iPdfTable.setHeaderRows(2);
            } else {
                iPdfTable.setHeaderRows(3);
            }
        } else {
            PdfPCell c;
            if (numEventsOrMeetings > getMaxResults()){
            	c = createCell();
                addText(c, "**Warning: More than " + getMaxResults() + " meetings match your search criteria. Only the first " + getMaxResults() + " meetings are displayed. Please, redefine the search criteria in your filter.", true, Element.ALIGN_LEFT);
                c.setColspan(mainContact?10:9);
                iPdfTable.addCell(c);
            }
            iBgColor = new Color(224,224,224);
            //first line
            iUnderline = true;
            c = createCell();
            addText(c, LABEL, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, EVENT_TYPE, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, (mainContact?"Enrl.":ENROLLMENT), true, Element.ALIGN_RIGHT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, (mainContact?"Cap.":EVENT_CAPACITY), true, Element.ALIGN_RIGHT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, SPONSORING_ORG, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, MEETING_DATE, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, MEETING_TIME, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            c = createCell();
            addText(c, MEETING_LOCATION, true, Element.ALIGN_LEFT);
            iPdfTable.addCell(c);
            if (mainContact) {
                c = createCell();
                addText(c, MAIN_CONTACT, true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
                c = createCell();
                addText(c, APPROVED_DATE, true, Element.ALIGN_LEFT);
                iPdfTable.addCell(c);
            }
            if (numEventsOrMeetings <= getMaxResults()){
            	iPdfTable.setHeaderRows(1);
            } else {
             	iPdfTable.setHeaderRows(2);
            }

            
        }
        iBgColor = Color.WHITE; iUnderline = false;
   }
    
    private PdfPCell pdfBuildEventName(Event e) {
        PdfPCell cell = createCell();
        addText(cell, e.getEventName()==null?"":e.getEventName(), true, Element.ALIGN_LEFT); 
        return cell;
    }

    private PdfPCell pdfBuildEventCapacity(Event e) {
        PdfPCell cell = createCell();
    	int minCap = (e.getMinCapacity()==null?-1:e.getMinCapacity());
    	int maxCap = (e.getMaxCapacity()==null?-1:e.getMaxCapacity());
    	if (minCap==-1){
    		
    	} else {
    		if (maxCap!=-1) {
    			if (maxCap!=minCap) {
    			    addText(cell, minCap+"-"+maxCap, Element.ALIGN_RIGHT);    				
    			} else {
    			    addText(cell, String.valueOf(minCap), Element.ALIGN_RIGHT);
    			}
    		}
    	}
    	return cell;
    }
 
    private PdfPCell pdfBuildEventEnrollment(Event e) {
    	PdfPCell cell = createCell();
    	if (Event.sEventTypeClass == e.getEventType()) {
    		ClassEvent ce = new ClassEventDAO().get(Long.valueOf(e.getUniqueId()));
			if (ce.getClazz().getEnrollment() != null){
				addText(cell, ce.getClazz().getEnrollment().toString(), Element.ALIGN_RIGHT);
			} else {
				addText(cell, "0", Element.ALIGN_RIGHT);
			}
		} else if (Event.sEventTypeFinalExam == e.getEventType() || Event.sEventTypeMidtermExam == e.getEventType()) {
			ExamEvent ee = new ExamEventDAO().get(e.getUniqueId());
			addText(cell, String.valueOf(ee.getExam().countStudents()), Element.ALIGN_RIGHT);
		} else if (Event.sEventTypeCourse == e.getEventType()) {
			CourseEvent ce = new CourseEventDAO().get(e.getUniqueId());
			int enrl = 0;
			for (RelatedCourseInfo rci: ce.getRelatedCourses())
				enrl += rci.countStudents();
			addText(cell, String.valueOf(enrl), Element.ALIGN_RIGHT);
    	} 
    	return (cell);
    }
    
    private PdfPCell pdfBuildSponsoringOrg(Event e) {
        PdfPCell cell = createCell();
        addText(cell, e.getSponsoringOrganization()==null?"":e.getSponsoringOrganization().getName());
    	return cell;
    }
    
    private PdfPCell pdfBuildEventTypeAbbv(Event e) {
        PdfPCell cell = createCell();
        addText(cell, e.getEventTypeAbbv());
        return cell;
    }
    
    private PdfPCell pdfBuildMainContactName(Event e) {
        PdfPCell cell = createCell();
    	if (e.getMainContact()!=null)
    	    addText(cell,
    	                (e.getMainContact().getLastName()==null?"":(e.getMainContact().getLastName()+", "))+
    	    			(e.getMainContact().getFirstName()==null?"":e.getMainContact().getFirstName()));
    	return cell;
    }
    
    private PdfPCell pdfBuildEmptyMeetingInfo() {
        PdfPCell cell = createCell();
    	return cell;
    }
 
    private PdfPCell pdfBuildDate(Meeting m) {
        PdfPCell cell = createCell();
        addText(cell, sDateFormat.format(m.getMeetingDate()));
    	return cell;
    }
    
    private PdfPCell pdfBuildDate (MultiMeeting m) {
        PdfPCell cell = createCell();
        Calendar c = Calendar.getInstance();
        c.setTime(m.getMeetings().first().getMeetingDate());
        int y1 = c.get(Calendar.YEAR);
        c.setTime(m.getMeetings().last().getMeetingDate());
        int y2 = c.get(Calendar.YEAR);
        addText(cell,
                m.getDays()+" "+
                (y1==y2?sDateFormatM1:sDateFormatM2).format(m.getMeetings().first().getMeetingDate())
                +" - "+
                sDateFormatM2.format(m.getMeetings().last().getMeetingDate())
                );
        return cell;
    }

    private PdfPCell pdfBuildTime (Meeting m) {
        PdfPCell cell = createCell();
		addText(cell,
                m.isAllDay()?"All Day":
                Constants.toTime(Constants.SLOT_LENGTH_MIN*m.getStartPeriod()+Constants.FIRST_SLOT_TIME_MIN+(m.getStartOffset()==null?0:m.getStartOffset()))+" - "+
                Constants.toTime(Constants.SLOT_LENGTH_MIN*m.getStopPeriod()+Constants.FIRST_SLOT_TIME_MIN+(m.getStopOffset()==null?0:m.getStopOffset())));
		return cell;
    }
    
    private PdfPCell pdfBuildLocation (Meeting m) {
        PdfPCell cell = createCell();
    	addText(cell, m.getLocation()==null?"":m.getLocation().getLabel());
    	return cell;
    }
    
    private PdfPCell pdfBuildApproved (Meeting m) {
        PdfPCell cell = createCell();
    	SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
    	addText(cell, m.getApprovedDate()==null?"":df.format(m.getApprovedDate()));
    	return cell;
    }
    
    private PdfPCell pdfBuildApproved (MultiMeeting mm) {
        PdfPCell cell = createCell();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Date approvalDate = null; //latest approval date
        for (Meeting m : mm.getMeetings())
            if (approvalDate==null || approvalDate.compareTo(m.getApprovedDate())<0) approvalDate = m.getApprovedDate();
        addText(cell, approvalDate==null?"":df.format(approvalDate));
        return cell;
    }

    private void pdfAddEventsRowsToTable(Event e, boolean mainContact, TreeSet<MultiMeeting> meetings) {
        boolean allPast = true;
        for (MultiMeeting meeting : meetings) {
            if (!meeting.isPast()) { allPast = false; break; }
        }
        if (allPast) {
            iTextColor = Color.GRAY; iTextItalic = true;
        }
        iBgColor = new Color(223, 231, 242);
        iPdfTable.addCell(pdfBuildEventName(e));
        iPdfTable.addCell(pdfBuildEventEnrollment(e));
        iPdfTable.addCell(pdfBuildEventCapacity(e));
        iPdfTable.addCell(pdfBuildSponsoringOrg(e));
        iPdfTable.addCell(pdfBuildEventTypeAbbv(e));
        if (mainContact)
            iPdfTable.addCell(pdfBuildMainContactName(e));
        iTextColor = Color.BLACK; iTextItalic = false; iBgColor = Color.WHITE;
    }
    
    private void pdfAddMeetingRowsToTable (MultiMeeting mm, boolean mainContact, boolean printOverlaps) {
        Meeting m = mm.getMeetings().first();
        if (mm.isPast()) {
            iTextColor = Color.GRAY; iTextItalic = true;
        } else {
            if (m.isApproved()) {
                //bgColor = "#DDFFDD";
            } else {
                iBgColor = new Color(255,255,221);
            }
        }
        iPdfTable.addCell(pdfBuildEmptyMeetingInfo());
        PdfPCell cell = mm.getMeetings().size()==1?pdfBuildDate(m):pdfBuildDate(mm);
        cell.setColspan(2);
        iPdfTable.addCell(cell);
        iPdfTable.addCell(pdfBuildTime(m));
        iPdfTable.addCell(pdfBuildLocation(m));
        if (mainContact)
            iPdfTable.addCell(mm.getMeetings().size()==1?pdfBuildApproved(m):pdfBuildApproved(mm));
        iTextColor = Color.BLACK; iTextItalic = false; iBgColor = Color.WHITE;
        if (printOverlaps) {
            TreeSet<Meeting> overlaps = new TreeSet();
            for (Meeting mx: mm.getMeetings()) {
                overlaps.addAll(mx.getTimeRoomOverlaps());
            }
            if (!overlaps.isEmpty()) {
                for (MultiMeeting o: Event.getMultiMeetings(overlaps))
                    pdfAddOverlappingMeetingToTable(o, mainContact);
            }
        }
    }
    
    private void pdfAddOverlappingMeetingToTable(MultiMeeting mm, boolean mainContact) {
        Meeting m = mm.getMeetings().first();
        if (mm.isPast()) {
            iTextColor = Color.GRAY; iTextItalic = true;
        }        
        iBgColor = new Color(255,215,215);
        PdfPCell cell = createCell();
        addText(cell,"   Conf/w "+m.getEvent().getEventName()+" ("+m.getEvent().getEventTypeAbbv()+")");
        iPdfTable.addCell(cell);
        cell = (mm.getMeetings().size()==1?pdfBuildDate(m):pdfBuildDate(mm));
        cell.setColspan(2);
        iPdfTable.addCell(cell);
        iPdfTable.addCell(pdfBuildTime(m));
        iPdfTable.addCell(pdfBuildLocation(m));
        if (mainContact)
            iPdfTable.addCell(mm.getMeetings().size()==1?pdfBuildApproved(m):pdfBuildApproved(mm));
        iTextColor = Color.BLACK; iTextItalic = false; iBgColor = Color.WHITE;
    }
    
    private void pdfAddMeetingRowsToTable (Meeting m, boolean mainContact, Event lastEvent, Date now, boolean line, boolean printOverlaps) {
        if (m.getStartTime().before(now)) {
            iTextColor = Color.GRAY; iTextItalic = true;
        } else {
            if (m.isApproved()) {
                //bgColor = "#DDFFDD";
            } else {
                iBgColor = new Color(255,255,221);
            }
        }
        if (line && lastEvent!=null && !lastEvent.getUniqueId().equals(m.getEvent().getUniqueId())) {
            iOverline = true;
        }
        if (lastEvent!=null && lastEvent.getUniqueId().equals(m.getEvent().getUniqueId())) {
            PdfPCell cell = createCell();
            iPdfTable.addCell(cell);
            iPdfTable.addCell(cell);
            iPdfTable.addCell(cell);
            iPdfTable.addCell(cell);
            iPdfTable.addCell(cell);
        } else {
            iPdfTable.addCell(pdfBuildEventName(m.getEvent()));
            iPdfTable.addCell(pdfBuildEventTypeAbbv(m.getEvent()));
            iPdfTable.addCell(pdfBuildEventEnrollment(m.getEvent()));
            iPdfTable.addCell(pdfBuildEventCapacity(m.getEvent()));
            iPdfTable.addCell(pdfBuildSponsoringOrg(m.getEvent()));
        }
        iPdfTable.addCell(pdfBuildDate(m));
        iPdfTable.addCell(pdfBuildTime(m));
        iPdfTable.addCell(pdfBuildLocation(m));
        if (mainContact) {
            if (lastEvent!=null && lastEvent.getUniqueId().equals(m.getEvent().getUniqueId())) {
                PdfPCell cell = createCell();
                iPdfTable.addCell(cell);
            } else {
                iPdfTable.addCell(pdfBuildMainContactName(m.getEvent()));
            }
            iPdfTable.addCell(pdfBuildApproved(m));
        }
        iTextColor = Color.BLACK; iTextItalic = false; iBgColor = Color.WHITE; iOverline = false;
        if (printOverlaps) {
            TreeSet<Meeting> overlaps = new TreeSet(m.getTimeRoomOverlaps());
            if (!overlaps.isEmpty()) {
                for (Meeting o: overlaps)
                    pdfAddOverlappingMeetingToTable(o, mainContact, now);
            }
        }
    }
    
    private void pdfAddOverlappingMeetingToTable(Meeting m, boolean mainContact, Date now) {
        if (m.getStartTime().before(now)) {
            iTextColor = Color.GRAY; iTextItalic = true;
        }
        iBgColor = new Color(255,215,215);
        PdfPCell cell = createCell();
        addText(cell, "   Conf/w "+(m.getEvent().getEventName()==null?"":m.getEvent().getEventName())+" ("+m.getEvent().getEventTypeAbbv()+")");
        cell.setColspan(2);
        iPdfTable.addCell(cell);
        iPdfTable.addCell(pdfBuildEventEnrollment(m.getEvent()));
        iPdfTable.addCell(pdfBuildEventCapacity(m.getEvent()));
        iPdfTable.addCell(pdfBuildSponsoringOrg(m.getEvent()));
        iPdfTable.addCell(pdfBuildDate(m));
        iPdfTable.addCell(pdfBuildTime(m));
        iPdfTable.addCell(pdfBuildLocation(m));
        if (mainContact) {
            iPdfTable.addCell(pdfBuildMainContactName(m.getEvent()));
            iPdfTable.addCell(pdfBuildApproved(m));
        }
        iTextColor = Color.BLACK; iTextItalic = false; iBgColor = Color.WHITE;
    }    
    
    public File pdfTableForEvents (EventListForm form){
        List events = loadEvents(form);
        if (events.isEmpty()) return null;
        
        boolean mainContact = form.isAdmin() || form.isEventManager();
        
        FileOutputStream out = null;
        try {
            File file = ApplicationProperties.getTempFile("events", "pdf");
            
            iDocument = new Document(PageSize.LETTER, 15, 15, 15, 30); 

            out = new FileOutputStream(file);
            iWriter = PdfEventHandler.initFooter(iDocument, out);
            iDocument.open();

            iPdfTable = new PdfPTable(getWidths(true, mainContact));
            iPdfTable.setWidthPercentage(100);
            iPdfTable.getDefaultCell().setPadding(3);
            iPdfTable.getDefaultCell().setBorderWidth(0);
            iPdfTable.setSplitRows(false);
            
            pdfBuildTableHeader(true, mainContact, events.size());

            for (Iterator it = events.iterator();it.hasNext();){
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
                }
                TreeSet<MultiMeeting> meetings = event.getMultiMeetings();
                pdfAddEventsRowsToTable(event, mainContact, meetings);
                for (MultiMeeting meeting : meetings) 
                    pdfAddMeetingRowsToTable(meeting, mainContact, form.getDispConflicts());
            }

            iDocument.add(iPdfTable);
            iDocument.close();

            return file;
        } catch (Exception e) {
            Debug.error(e);
        } finally {
            try {
                if (out!=null) out.close();
            } catch (IOException e) {}
        }
        return null;
    }
    
    public File pdfTableForMeetings(MeetingListForm form) {
        List meetings = loadMeetings(form);
        
        if (meetings.isEmpty()) return null;
        
        boolean mainContact = form.isAdmin() || form.isEventManager();
        
        FileOutputStream out = null;
        try {
            File file = ApplicationProperties.getTempFile("meetings", "pdf");
            
            float[] widths = getWidths(false, mainContact);
            
            iDocument = new Document(PageSize.LETTER.rotate(), 15, 15, 15, 30); 

            out = new FileOutputStream(file);
            iWriter = PdfEventHandler.initFooter(iDocument, out);
            iDocument.open();

            iPdfTable = new PdfPTable(widths);
            iPdfTable.setWidthPercentage(100);
            iPdfTable.getDefaultCell().setPadding(3);
            iPdfTable.getDefaultCell().setBorderWidth(0);
            iPdfTable.setSplitRows(false);
            
            pdfBuildTableHeader(false, mainContact, meetings.size());

            Event lastEvent = null;
            Date now = new Date();
            boolean line = MeetingListForm.sOrderByName.equals(form.getOrderBy());
            
            for (Iterator it = meetings.iterator();it.hasNext();){
                Meeting meeting = (Meeting) it.next();
                pdfAddMeetingRowsToTable(meeting, mainContact, lastEvent, now, line, form.getDispConflicts());
                lastEvent = meeting.getEvent();
            }

            iDocument.add(iPdfTable);
            iDocument.close();

            return file;
        } catch (Exception e) {
            Debug.error(e);
        } finally {
            try {
                if (out!=null) out.close();
            } catch (IOException e) {}
        }
        return null;
    }
        
    protected TableStream initTable(JspWriter outputStream){
    	TableStream table = new TableStream(outputStream);
        table.setWidth("100%");
        table.setBorder(0);
        table.setCellSpacing(0);
        table.setCellPadding(3);
        table.tableDefComplete();
        return(table);
    }            
   
}
