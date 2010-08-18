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

import java.io.Serializable;
import java.util.BitSet;

import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.model.PreferenceLevel;


/**
 * @author Tomas Muller
 */
public class TimetableGridCell implements Serializable, Comparable {
	private static final long serialVersionUID = 2L;
	private String iName;
	private String iShortComment;
	private String iShortCommentNoColors;
	private String iInstructor;
	private String iOnClick;
	private String iTitle;
	private String iBackground;
	private int iLength;
	private int iNrMeetings;
	private int iMeetingNumber;
	private long iAssignmentId;
	private long iRoomId;
	private String iRoomName;
	private BitSet iWeekCode;
	private String iDatePatternName;
	private int iDay;
	private int iSlot;
	
	public static String sBgColorEmpty = "rgb(255,255,255)";
	public static String sBgColorRequired = "rgb(80,80,200)";
	public static String sBgColorStronglyPreferred = "rgb(40,180,60)"; 
	public static String sBgColorPreferred = "rgb(170,240,60)";
	public static String sBgColorNeutral = "rgb(240,240,240)";
	public static String sBgColorDiscouraged = "rgb(240,210,60)";
	public static String sBgColorStronglyDiscouraged = "rgb(240,120,60)";
	public static String sBgColorProhibited = "rgb(220,50,40)";
	public static String sBgColorNotAvailable = "rgb(200,200,200)";
	public static String sBgColorNotAvailableButAssigned = sBgColorProhibited;
	
	public TimetableGridCell(int day, int slot, long assignmentId, long roomId, String roomName, String name, String shortComment, String shortCommentNoColors, String onClick, String title, String background, int length, int meetingNumber, int nrMeetings, String datePatternName, BitSet weekCode, String instructor) {
		iDay = day;
		iSlot = slot;
		iAssignmentId = assignmentId;
		iName = name;
		iShortComment = shortComment;
		iShortCommentNoColors = shortCommentNoColors;
		iOnClick = onClick;
		iTitle = title;
		iBackground = background;
		iLength = length;
		iMeetingNumber = meetingNumber;
		iNrMeetings = nrMeetings;
		iRoomName = roomName;
		iRoomId = roomId;
		iWeekCode = weekCode;
		iDatePatternName = datePatternName;
		iInstructor = instructor;
	}
	
	public TimetableGridCell copyCell(int day, int mtgNumber) {
		return new TimetableGridCell(
				day,
				iSlot,
				iAssignmentId,
				iRoomId,
				iRoomName,
				iName,
				iShortComment,
				iShortCommentNoColors,
				iOnClick,
				iTitle,
				iBackground,
				iLength,
				mtgNumber,
				iNrMeetings,
				iDatePatternName,
				iWeekCode,
				iInstructor);
	}
	
	public String getName() { return iName; }
	public String getShortComment() { return iShortComment; }
	public String getShortCommentNoColors() { return iShortCommentNoColors; }
	public String getOnClick() { return iOnClick; }
	public String getTitle() { return iTitle; }
	public String getBackground() {
		return (iBackground==null?sBgColorEmpty:iBackground); 
	}
	public int getLength() { return iLength; }
	public int getNrMeetings() { return iNrMeetings; }
	public int getMeetingNumber() { return iMeetingNumber; }
	public long getAssignmentId() { return iAssignmentId; }
	public long getRoomId() { return iRoomId; }
	public String getRoomName() { return iRoomName; }
	public String getInstructor() { return (iInstructor==null?"":iInstructor); }
	
    public static String pref2color(String pref) {
    	return PreferenceLevel.prolog2bgColor(pref);
    }
    
    public static String pref2color(int pref) {
    	return PreferenceLevel.prolog2bgColor(PreferenceLevel.int2prolog(pref));
    }

    public static String conflicts2color(int nrConflicts) {
        if (nrConflicts>15) nrConflicts = 15;
        String color = null;
        if (nrConflicts==0) {
            color = "rgb(240,240,240)";
        } else if (nrConflicts<5) {
            color = "rgb(240,"+(240-(30*nrConflicts/5))+","+(240-(180*nrConflicts/5))+")";
        } else if (nrConflicts<10) {
            color = "rgb(240,"+(210-(90*(nrConflicts-5)/5))+",60)";
        } else {
            color = "rgb("+(240-(20*(nrConflicts-10)/5))+","+(120-(70*(nrConflicts-10)/5))+","+(60-(20*(nrConflicts-10)/5))+")";
        }
        return color;
    }
    
    public static String conflicts2colorFast(int nrConflicts) {
        if (nrConflicts==0) return "rgb(240,240,240)";
        if (nrConflicts==1) return "rgb(240,210,60)";
        if (nrConflicts==2) return "rgb(240,120,60)";
        return "rgb(220,50,40)";
    }    
    
    public BitSet getWeekCode() { return iWeekCode; }
    
    public int compareTo(Object o) {
    	if (o==null || !(o instanceof TimetableGridCell)) return -1;
    	TimetableGridCell c = (TimetableGridCell)o;
    	int cmp = (iWeekCode==null || c.iWeekCode==null?0:Double.compare(iWeekCode.nextSetBit(0),c.iWeekCode.nextSetBit(0)));
    	if (cmp!=0) return cmp;
    	cmp = (iWeekCode==null || c.iWeekCode==null?0:Double.compare(iWeekCode.length(),c.iWeekCode.length()));
    	if (cmp!=0) return cmp;
    	return new NaturalOrderComparator().compare(iName,c.iName);
    }
    
    public boolean hasDays() {
    	return iWeekCode!=null;
    }
    
    public String getDays() {
    	return iDatePatternName;
    }
    
    public int getDay() { return iDay; }
    public int getSlot() { return iSlot; }
}
