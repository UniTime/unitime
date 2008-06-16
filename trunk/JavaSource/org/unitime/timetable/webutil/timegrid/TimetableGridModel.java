/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
public abstract class TimetableGridModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private TimetableGridCell[][][] iData;
	private boolean[][] iAvailable;
	private String[][] iBackground = null;
	private String iName;
	private int iSize = 0;
    private Long iType = null;
	private transient boolean[][][] iRendered = null;

	public static final int sResourceTypeRoom = 0;
	public static final int sResourceTypeInstructor = 1;
	public static final int sResourceTypeDepartment = 2;
	
	public static final int sBgModeNotAvailable = -1;
	public static final int sBgModeNone = 0;
	public static final int sBgModeTimePref = 1;
	public static final int sBgModeRoomPref = 2;
	public static final int sBgModeStudentConf = 3;
	public static final int sBgModeInstructorBtbPref = 4;
	public static final int sBgModeDistributionConstPref = 5;
	public static final int sBgModePerturbations = 6;
	public static final int sBgModePerturbationPenalty = 7;
	public static final int sBgModeHardConflicts = 8;
	public static final int sBgModeDepartmentalBalancing = 9;
	public static final int sBgModeTooBigRooms = 10;
	
	public static String[] sBgModes = new String[] {
		"None",
		"Time Preferences",
		"Room Preferences",
		"Student Conflicts",
		"Instructor Back-to-Back Preferences",
		"Distribution Preferences",
		"Perturbations",
		"Perturbation Penalty",
		"Hard Conflicts",
		"Departmental Balancing Penalty",
		"Too Big Rooms"
	};
	public static String[] sResourceTypes = new String[] {
		"Room", "Instructor", "Department"
	};
	
	
	private int iResourceType;
	private int iBgMode;
	private long iResourceId;
	
	public TimetableGridModel() {
		iData = new TimetableGridCell[Constants.DAY_CODES.length][Constants.SLOTS_PER_DAY][];
		for (int i=0;i<Constants.DAY_CODES.length;i++)
			for (int j=0;j<Constants.SLOTS_PER_DAY;j++)
				iData[i][j]=null;
		iAvailable = new boolean[Constants.DAY_CODES.length][Constants.SLOTS_PER_DAY];
		for (int i=0;i<Constants.DAY_CODES.length;i++)
			for (int j=0;j<Constants.SLOTS_PER_DAY;j++)
				iAvailable[i][j]=true;
	}
	
	public TimetableGridModel(int resourceType, long resourceId) {
		this();
		iResourceType = resourceType; iResourceId = resourceId;
	}
	
	public int getResourceType() { return iResourceType; }
	public long getResourceId() { return iResourceId; }
	
	public static String[] getBackgroundModes() { return sBgModes; }
	
	public void setName(String name) { iName = name;}
	public void setSize(int size) { iSize = size; }
    public void setType(Long type) { iType = type; }
	public void addCell(int slot, TimetableGridCell cell) {
		addCell(slot/Constants.SLOTS_PER_DAY,slot%Constants.SLOTS_PER_DAY,cell);
	}
	public void addCell(int day, int slot, TimetableGridCell cell) {
		int idx = 0;
		for (int i=0;i<cell.getLength();i++)
			idx = Math.max(getIndex(day,slot+i,cell), idx);
		for (int i=0;i<cell.getLength();i++) {
			shift(day,slot+i,idx);
			setCell(day,slot+i,idx,cell);
		}
	}
	public void setAvailable(int slot, boolean available) {
		setAvailable(slot/Constants.SLOTS_PER_DAY,slot%Constants.SLOTS_PER_DAY,available);
	}
	public void setAvailable(int day, int slot, boolean available) {
		iAvailable[day][slot] = available;
	}
	public void setBackground(int day, int slot, String background) {
		if (iBackground==null) {
			iBackground = new String[Constants.DAY_CODES.length][Constants.SLOTS_PER_DAY];
			for (int i=0;i<Constants.DAY_CODES.length;i++)
				for (int j=0;j<Constants.SLOTS_PER_DAY;j++)
					iBackground[i][j]=null;
		}
		iBackground[day][slot] = background;
	}
	public String getBackground(int day, int slot) {
		if (iBackground==null) return null;
		return iBackground[day][slot];
	}
	
	public String getName() { return iName; }
	
	public int getSize() { return iSize; }
    
    public Long getType() { return iType; }
	
	public void shift(int day, int slot, int idx) {
		TimetableGridCell cell = getCell(day,slot,idx);
		if (cell==null) return;
		for (int s=cell.getSlot();s<cell.getSlot()+cell.getLength();s++) {
			shift(day,s,idx+1);
			setCell(day,s,idx,null);
			setCell(day,s,idx+1,cell);
		}
	}
	
	public void setCell(int day, int slot, int idx, TimetableGridCell cell) {
		if (iData[day][slot]==null) {
			iData[day][slot] = new TimetableGridCell[idx+1];
			for (int i=0;i<idx;i++)
				iData[day][slot][i]=null;
			iData[day][slot][idx]=cell;
		} else if (iData[day][slot].length<=idx) {
			TimetableGridCell[] old = iData[day][slot];
			iData[day][slot] = new TimetableGridCell[idx+1];
			for (int i=0;i<idx;i++)
				iData[day][slot][i]=(i<old.length?old[i]:null);
			iData[day][slot][idx]=cell;
		} else {
			if (iData[day][slot][idx]!=null && cell!=null) {
				System.out.println("WARN: ("+day+","+slot+","+idx+") already full with "+iData[day][slot][idx].getName());
			}
			iData[day][slot][idx]=cell;
		}
	}
	
	public int getIndex(int day, int slot, TimetableGridCell cell) {
		if (iData[day][slot]==null) {
			return 0;
		} else {
			int idx = 0;
			for (int i=0;i<iData[day][slot].length;i++) {
				if (iData[day][slot][i]!=null && iData[day][slot][i].compareTo(cell)<=0)
					idx = i+1;
			}
			return idx;
		}
	}
	
	public int nrCells(int day, int slot) {
		if (iData[day][slot]==null) return 0;
		int ret = 0;
		for (int i=0;i<iData[day][slot].length;i++)
			if (iData[day][slot][i]!=null)
				ret++;
		return ret;
	}
	
	public TimetableGridCell getCell(int day, int slot, int idx) {
		if (iData[day][slot]==null)
			return null;
		if (iData[day][slot].length<=idx)
			return null;
		return iData[day][slot][idx];
	}
	
	public int getMaxIdx(int startDay, int endDay, int firstSlot, int lastSlot) {
		int max = 0;
		for (int day=startDay;day<=endDay;day++)
			for (int slot=firstSlot;slot<=lastSlot;slot++)
				if (iData[day][slot]!=null)
					max = Math.max(max,iData[day][slot].length-1);
		return max;
	}
	public int getMaxIdxForDay(int day, int firstSlot, int lastSlot) {
		int max = 0;
		for (int slot=firstSlot;slot<=lastSlot;slot++)
			if (iData[day][slot]!=null)
				max = Math.max(max,iData[day][slot].length-1);
		return max;
	}
	public boolean isAvailable(int day, int slot) {
		return iAvailable[day][slot];
	}
	
	public void clearRendered() {
		iRendered = null;
	}
	public boolean isRendered(int day, int slot, int idx) {
		if (iRendered==null || iRendered[day][slot]==null)
			return false;
		if (iRendered[day][slot].length<=idx)
			return false;
		return iRendered[day][slot][idx];
	}
	
	public void setRendered(int day, int slot, int idx, int rowSpan, int colSpan) {
		if (iRendered==null) {
			iRendered = new boolean[Constants.DAY_CODES.length][Constants.SLOTS_PER_DAY][];
			for (int i=0;i<Constants.DAY_CODES.length;i++)
				for (int j=0;j<Constants.SLOTS_PER_DAY;j++)
					iRendered[i][j]=null;
		}
		for (int row=0;row<rowSpan;row++) {
			for (int col=0;col<colSpan;col++) {
				if (iRendered[day][slot+col]==null) {
					iRendered[day][slot+col] = new boolean[idx+rowSpan];
					for (int i=0;i<idx+rowSpan;i++)
						iRendered[day][slot+col][i]=false;
					iRendered[day][slot+col][idx+row]=true;
				} else if (iRendered[day][slot+col].length<=idx+row) {
					boolean[] old = iRendered[day][slot+col];
					iRendered[day][slot+col] = new boolean[idx+rowSpan];
					for (int i=0;i<idx+rowSpan;i++)
						iRendered[day][slot+col][i]=(i<old.length?old[i]:false);
					iRendered[day][slot+col][idx+row]=true;
				} else {
					if (iRendered[day][slot+col][idx+row])
						System.out.println("WARN: ("+day+","+(slot+col)+","+(idx+row)+") already rendered");
					iRendered[day][slot+col][idx+row]=true;
				}
			}
		}
	}
	
	public int getDepth(int day, int slot, int idx, int maxIdx) {
		int depth = 1;
		for (int i=idx+1;i<=maxIdx;i++) {
			if (getCell(day,slot,i)!=null || isRendered(day,slot,i)) break;
			depth++;
		}
		return depth;
	}
	
	public int getDepth(int day, int slot, int idx, int maxIdx, int colSpan) {
		int depth = Integer.MAX_VALUE;
		for (int col=0;col<colSpan;col++)
			depth = Math.min(depth,getDepth(day,slot+col,idx,maxIdx));
		return depth;
	}
	
	protected boolean isUselessFirst(int d, int s) {
    	if (s-1<0 || s+6>=Constants.SLOTS_PER_DAY) return false;
    	return (nrCells(d,s-1)!=0 &&
           		nrCells(d,s+0)==0 &&
           		nrCells(d,s+1)==0 &&
           		nrCells(d,s+2)==0 &&
           		nrCells(d,s+3)==0 &&
           		nrCells(d,s+4)==0 &&
           		nrCells(d,s+5)==0 &&
           		nrCells(d,s+6)!=0);
	}
	
	protected boolean isUseless(int d, int s) {
		return isUselessFirst(d,s) || 
			isUselessFirst(d,s-1) || 
			isUselessFirst(d,s-2) || 
			isUselessFirst(d,s-3) || 
			isUselessFirst(d,s-4) || 
			isUselessFirst(d,s-5);
	}
	
	protected void initBgModeUselessSlots() {
        for (int d=0;d<Constants.DAY_CODES.length;d++) {
            for (int s=0;s<Constants.SLOTS_PER_DAY;s++) {
            	if (!isAvailable(d,s)) continue;
                int pref = 0;
                if (nrCells(d,s)==0) {
                	if (isUseless(d,s)) pref=4;
                	switch (d) {
                		case 0 :
                			if (nrCells(2,s)!=0 && nrCells(4,s)!=0) pref++; 
                			break;
                		case 1 :
                			if (nrCells(3,s)!=0) pref++;
                			break;
                		case 2 :
                			if (nrCells(0,s)!=0 && nrCells(4,s)!=0) pref++;
                			break;
                		case 3 :
                			if (nrCells(1,s)!=0) pref++;
                			break;
                		case 4 :
                			if (nrCells(0,s)!=0 && nrCells(2,s)!=0) pref++; 
                			break;
                	}
                }
                String background = TimetableGridCell.pref2color(PreferenceLevel.sNeutral); 
                if (pref>4)
                	background = TimetableGridCell.pref2color(PreferenceLevel.sProhibited);
                else if (pref==4)
                	background = TimetableGridCell.pref2color(PreferenceLevel.sStronglyDiscouraged);
                else if (pref>0)
                	background = TimetableGridCell.pref2color(PreferenceLevel.sDiscouraged);
                setBackground(d, s, background);
            }
        }
	}	
}