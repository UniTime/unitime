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
 
package org.unitime.timetable.model;

import java.util.Collection;

import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.base.BaseEventNote;
import org.unitime.timetable.util.Formats;



/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public class EventNote extends BaseEventNote implements Comparable<EventNote> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public EventNote () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public EventNote (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/
	
	public static final int sEventNoteTypeCreateEvent = 0;
    public static final int sEventNoteTypeAddMeetings = 1;
	public static final int sEventNoteTypeApproval = 2;
	public static final int sEventNoteTypeRejection = 3;
	public static final int sEventNoteTypeDeletion = 4;
	public static final int sEventNoteTypeEditEvent = 5;
	public static final int sEventNoteTypeInquire = 6;
	public static final int sEventNoteTypeCancel = 7;
	public static final int sEventNoteTypeEmail = 8;
	
	public static final String[] sEventNoteTypeBgColor = new String[] {
	    "transparent", "transparent", "#D7FFD7", "#FFD7D7", "transparent", "transparent", "#FFFFD7", "transparent"  
	};
	public static final String[] sEventNoteTypeName = new String[] {
	    "Create", "Update", "Approve", "Reject", "Delete", "Edit", "Inquire", "Cancel", "Email"
	};

	public int compareTo(EventNote n) {
	    int cmp = getTimeStamp().compareTo(n.getTimeStamp());
	    if (cmp!=0) return cmp;
	    return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(n.getUniqueId() == null ? -1 : n.getUniqueId());
	}
	
	public void setMeetingCollection(Collection meetings) {
        String meetingStr = "";
        for (MultiMeeting m : Event.getMultiMeetings(meetings)) {
            if (meetingStr.length()>0) meetingStr += "\n";
            meetingStr += m.toShortString();
        }
        setMeetings(meetingStr);
	}
	
	public String getMeetingsHtml() {
	    if (getMeetings()==null || getMeetings().length()==0) return "<i>N/A</i>";
	    return getMeetings().replaceAll("\n", "<br>");
	}
	
	public String toHtmlString(boolean includeUser) {
	    return "<tr style=\"background-color:"+sEventNoteTypeBgColor[getNoteType()]+";\" valign='top' " +
	            "onMouseOver=\"this.style.backgroundColor='rgb(223,231,242)';\" " +
	            "onMouseOut=\"this.style.backgroundColor='"+sEventNoteTypeBgColor[getNoteType()]+"';\">" +
	    		"<td>"+Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP).format(getTimeStamp())+"</td>" +
                (includeUser?"<td>"+(getUser()==null || getUser().length()==0?"<i>N/A</i>":getUser())+"</td>":"") +
	    		"<td>"+sEventNoteTypeName[getNoteType()]+"</td>" +
	    		"<td>"+getMeetingsHtml()+"</td>"+
	    		"<td>"+(getTextNote()==null?"":getTextNote().replaceAll("\n", "<br>"))+"</td>"+
	    		"</tr>";
	}

}
