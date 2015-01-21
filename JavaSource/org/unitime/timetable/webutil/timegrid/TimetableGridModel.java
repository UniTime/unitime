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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

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
	private transient HashSet<Integer>[][][] iRenderedDate = null;
	private transient Hashtable<Integer,Hashtable<Long,Integer>> iIndex = null;
	private int iFirstDay = -1;

	public static final int sResourceTypeRoom = 0;
	public static final int sResourceTypeInstructor = 1;
	public static final int sResourceTypeDepartment = 2;
	public static final int sResourceTypeCurriculum = 3;
	
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
		"Room", "Instructor", "Department", "Curriculum"
	};
	
	
	private int iResourceType;
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
	public int getFirstDay() { return iFirstDay; }
	public void setFirstDay(int firstDay) { iFirstDay = firstDay; }
	
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
	
	private Hashtable<Long, Integer> index(int date) {
		if (iIndex == null)
			iIndex = new Hashtable<Integer, Hashtable<Long,Integer>>();
		Hashtable<Long, Integer> index = iIndex.get(date);
		if (index == null) {
			index = new Hashtable<Long, Integer>();
			iIndex.put(date, index);
			for (int day = 0; day < iData.length; day++) {
				for (int slot = 0; slot < iData[day].length; slot++) {
					if (iData[day][slot] == null) continue;
					Set<Integer> used = new HashSet<Integer>();
					List<TimetableGridCell> cells = new ArrayList<TimetableGridCell>();
					for (TimetableGridCell cell: iData[day][slot])
						if (cell != null && cell.getWeekCode().get(date)) {
							Integer i = index.get(cell.getAssignmentId());
							if (i == null) {
								cells.add(cell);
							} else {
								used.add(i);
							}
						}
					int i = 0;
					for (TimetableGridCell cell: cells) {
						while (used.contains(i)) i++;
						index.put(cell.getAssignmentId(), i++);
					}
				}
			}
		}
		return index;
	}
	
	public TimetableGridCell getCell(int day, int slot, int idx, int date) {
		if (iData[day][slot]==null)
			return null;
		Hashtable<Long, Integer> index = index(date);
		for (TimetableGridCell cell: iData[day][slot])
			if (cell != null && cell.getWeekCode().get(date) && idx == index.get(cell.getAssignmentId()))
				return cell;
		return null;
	}
	
	public int getMaxIdx(int startDay, int endDay, int firstSlot, int lastSlot) {
		int max = 0;
		for (int day=startDay;day<=endDay;day++)
			for (int slot=firstSlot;slot<=lastSlot;slot++)
				if (iData[day][slot]!=null)
					max = Math.max(max,iData[day][slot].length-1);
		return max;
	}
	
	public int getMaxIdx(int startDay, int endDay, int firstSlot, int lastSlot, int date) {
		int max = 0;
		for (int day=startDay;day<=endDay;day++)
			for (int slot=firstSlot;slot<=lastSlot;slot++) {
				int idx = 0;
				if (iData[day][slot] != null) {
					Hashtable<Long, Integer> index = index(date);
					for (TimetableGridCell cell: iData[day][slot])
						if (cell != null && cell.getWeekCode().get(date))
							idx = Math.max(idx, index.get(cell.getAssignmentId()));
				}
				max = Math.max(max, idx);
			}
		return max;
	}

	public int getMaxIdxForDay(int day, int firstSlot, int lastSlot) {
		int max = 0;
		for (int slot=firstSlot;slot<=lastSlot;slot++)
			if (iData[day][slot]!=null)
				max = Math.max(max,iData[day][slot].length-1);
		return max;
	}
	
	public int getMaxIdxForDay(int day, int firstSlot, int lastSlot, int date) {
		return getMaxIdx(day, day, firstSlot, lastSlot, date);
	}
	
	public boolean isAvailable(int day, int slot) {
		return iAvailable[day][slot];
	}
	
	public void clearRendered() {
		iRendered = null; iRenderedDate = null;
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
	
	public boolean isRendered(int day, int slot, int idx, int date) {
		if (iRenderedDate==null || iRenderedDate[day][slot]==null)
			return false;
		if (iRenderedDate[day][slot].length<=idx)
			return false;
		return iRenderedDate[day][slot][idx].contains(date);
	}
	
	public void setRendered(int day, int slot, int idx, int rowSpan, int colSpan, int date) {
		if (iRenderedDate==null) {
			iRenderedDate = new HashSet[Constants.DAY_CODES.length][Constants.SLOTS_PER_DAY][];
			for (int i=0;i<Constants.DAY_CODES.length;i++)
				for (int j=0;j<Constants.SLOTS_PER_DAY;j++)
					iRenderedDate[i][j]=null;
		}
		for (int row=0;row<rowSpan;row++) {
			for (int col=0;col<colSpan;col++) {
				if (iRenderedDate[day][slot+col]==null) {
					iRenderedDate[day][slot+col] = new HashSet[idx+rowSpan];
					for (int i=0;i<idx+rowSpan;i++)
						iRenderedDate[day][slot+col][i]=new HashSet<Integer>();
					iRenderedDate[day][slot+col][idx+row].add(date);
				} else if (iRenderedDate[day][slot+col].length<=idx+row) {
					HashSet<Integer>[] old = iRenderedDate[day][slot+col];
					iRenderedDate[day][slot+col] = new HashSet[idx+rowSpan];
					for (int i=0;i<idx+rowSpan;i++)
						iRenderedDate[day][slot+col][i]=(i<old.length?old[i]:new HashSet<Integer>());
					iRenderedDate[day][slot+col][idx+row].add(date);
				} else {
					if (iRenderedDate[day][slot+col][idx+row].contains(date))
						System.out.println("WARN: ("+day+","+(slot+col)+","+(idx+row)+"," + date + ") already rendered");
					iRenderedDate[day][slot+col][idx+row].add(date);
				}
			}
		}
	}
	
	public int getDepth(int day, int slot, int idx, int maxIdx, int colSpan) {
		int depth = Integer.MAX_VALUE;
		for (int col=0;col<colSpan;col++) {
			int d = 1;
			for (int i=idx+1;i<=maxIdx;i++) {
				if (getCell(day,slot+col,i)!=null || isRendered(day,slot+col,i)) break;
				d++;
			}
			depth = Math.min(depth,d);
		}
		return depth;
	}
	
	public int getDepth(int day, int slot, int idx, int maxIdx, int colSpan, int date) {
		int depth = Integer.MAX_VALUE;
		for (int col=0;col<colSpan;col++) {
			int d = 1;
			for (int i=idx+1;i<=maxIdx;i++) {
				if (getCell(day,slot+col,i, date)!=null || isRendered(day,slot+col,i, date)) break;
				d++;
			}
			depth = Math.min(depth,d);
		}
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
