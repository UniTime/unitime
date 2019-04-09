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
package org.unitime.timetable.server.solver;

import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridBackground;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;

/**
 * @author Tomas Muller
 */
public class TimetableGridHelper {
	protected static GwtMessages MSG = Localization.create(GwtMessages.class);
	protected static GwtConstants CONST = Localization.create(GwtConstants.class);
	
	public static enum ResourceType {
		ROOM,
		INSTRUCTOR,
		DEPARTMENT,
		CURRICULUM,
		SUBJECT_AREA,
		STUDENT_GROUP,
	}
	
	public static enum BgMode {
		None,
		TimePref,
		RoomPref,
		StudentConf,
		InstructorBtbPref,
		DistributionConstPref,
		Perturbations,
		PerturbationPenalty,
		HardConflicts,
		DepartmentalBalancing,
		TooBigRooms,
		StudentGroups,
	}
	
	public static enum OrderBy {
		NameAsc,
		NameDesc,
		SizeAsc,
		SizeDesc,
	    TypeAsc,
	    TypeDesc,
	    UtilizationAsc,
	    UtilizationDesc,
	}
	
	public static enum DisplayMode {
		InRow,
		PerWeekHorizontal,
		PerWeekVertical,
		WeekByWeekHorizontal,
	}
	
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
    
	public static String hardConflicts2pref(AssignmentPreferenceInfo assignmentInfo) {
		if (assignmentInfo==null) return PreferenceLevel.sNeutral;
		String pref = PreferenceLevel.sNeutral;
		if (assignmentInfo.getNrRoomLocations()==1 && assignmentInfo.getNrTimeLocations()==1) pref = PreferenceLevel.sRequired;
		else if (assignmentInfo.getNrSameTimePlacementsNoConf()>0) pref=PreferenceLevel.sStronglyPreferred;
		else if (assignmentInfo.getNrTimeLocations()>1 && assignmentInfo.getNrSameRoomPlacementsNoConf()>0) pref=PreferenceLevel.sProhibited;
		else if (assignmentInfo.getNrTimeLocations()>1) pref=PreferenceLevel.sNeutral;
		else if (assignmentInfo.getNrSameRoomPlacementsNoConf()>0) pref=PreferenceLevel.sDiscouraged;
		else if (assignmentInfo.getNrRoomLocations()>1) pref=PreferenceLevel.sStronglyDiscouraged;
		else pref=PreferenceLevel.sRequired;
		return pref;
	}
	
	private static int gradient(int min, int v1, int max, int v2, int value) {
    	return (value <= min ? v1 : value >= max ? v2 : v1 + (v2 - v1) * (value - min) / (max - min));
    }
    
    public static String percentage2color(int p) {
    	int[] points = new int[] {   0,  20,  40,  60,  80, 100 };
    	int[] r = new int[] {      220, 240, 240, 240,  70,  30 };
    	int[] g = new int[] {       50, 120, 210, 240, 230, 160 };
    	int[] b = new int[] {       40,  60,  60, 240,  30,  60 };
    	for (int i = 1; i < points.length; i++) {
    		if (p <= points[i])
    			return
    				"rgb(" + gradient(points[i-1], r[i-1], points[i], r[i], p) + "," +
    				gradient(points[i-1], g[i-1], points[i], g[i], p) + "," +
    				gradient(points[i-1], b[i-1], points[i], b[i], p) + ")";
    	}
    	return "rgb(" + r[points.length - 1] + "," + g[points.length - 1] + "," + b[points.length - 1] + ")"; 
    }
    
    public static String pattern2string(BitSet pattern) {
    	StringBuffer ret = new StringBuffer();
    	for (int i = 0; i < pattern.length(); i++)
    		ret.append(pattern.get(i) ? '1' : '0');
    	return ret.toString();
    }
    
    public static class Combine<T> implements Iterable<T> {
    	private Iterable<T>[] iItems;
    	
    	public Combine(Iterable<T>... items) {
    		iItems = items;
    	}

		@Override
		public Iterator<T> iterator() {
			return new CombinedIterator<T>(iItems);
		}
    }
    
    public static class CombinedIterator<T> implements Iterator<T> {
    	private Iterable<T>[] iItems;
    	private Iterator<T> iIterator;
    	private int iIdx;
    	
    	public CombinedIterator(Iterable<T>... items) {
    		iItems = items;
    		iIdx = -1;
    		iIterator = null; 
    	}

		@Override
		public boolean hasNext() {
			if (iIterator != null && iIterator.hasNext()) return true;
			while (true) {
				iIdx++;
				if (iIdx >= iItems.length) return false;
				iIterator = iItems[iIdx].iterator();
				if (iIterator.hasNext()) return true;
			}
		}

		@Override
		public T next() {
			return iIterator.next();
		}

		@Override
		public void remove() {
			iIterator.remove();
		}
    }

	private static void setCell(TimetableGridCell[][][] data, int day, int slot, int idx, TimetableGridCell cell) {
		if (data[day][slot]==null) {
			data[day][slot] = new TimetableGridCell[idx+1];
			for (int i = 0; i < idx; i++)
				data[day][slot][i] = null;
			data[day][slot][idx] = cell;
		} else if (data[day][slot].length <= idx) {
			TimetableGridCell[] old = data[day][slot];
			data[day][slot] = new TimetableGridCell[idx+1];
			for (int i = 0; i < idx; i++)
				data[day][slot][i] = (i < old.length ? old[i] : null);
			data[day][slot][idx] = cell;
		} else {
			data[day][slot][idx] = cell;
		}
	}
	
	private static TimetableGridCell getCell(TimetableGridCell[][][] data, int day, int slot, int idx) {
		if (data[day][slot] == null) return null;
		if (data[day][slot].length <= idx) return null;
		return data[day][slot][idx];
	}
	
	private static int getIndex(TimetableGridCell[][][] data, int day, int slot, int length) {
		idx: for (int idx = 0;; idx++) {
			for (int i = 0; i < length; i++)
				if (getCell(data, day, slot + i, idx) != null) continue idx;
			return idx;
		}
	}
	
	private static void addCell(TimetableGridCell[][][] data, int day, int slot, int length, TimetableGridCell cell) {
		int idx = getIndex(data, day, slot, length);
		for (int i = 0; i < length; i++)
			setCell(data, day, slot + i, idx, cell);
	}
	
	private static int nrEmptyAbove(TimetableGridCell[][][] data, int day, int slot, int index, int length, TimetableGridCell cell) {
		for (int idx = index + 1; idx < length; idx++) {
			for (int i = 0; i < cell.getLength(); i++)
				if (getCell(data, day, slot + i, idx) != null) return idx - index - 1;
		}
		return length - index - 1;
	}
	
	private static int nrCells(TimetableGridCell[][][] data, int day, int slot) {
		if (data[day][slot] == null) return 0;
		int ret = 0;
		for (int i = 0; i < data[day][slot].length; i++)
			if (data[day][slot][i] != null)
				ret++;
		return ret;
	}
	
	private static boolean isUselessFirst(TimetableGridCell[][][] data, int d, int s) {
    	if (s - 1 < 0 || s + 6 >= 288) return false;
    	return (nrCells(data, d, s - 1) != 0 &&
           		nrCells(data, d, s + 0) == 0 &&
           		nrCells(data, d, s + 1) == 0 &&
           		nrCells(data, d, s + 2) == 0 &&
           		nrCells(data, d, s + 3) == 0 &&
           		nrCells(data, d, s + 4) == 0 &&
           		nrCells(data, d, s + 5) == 0 &&
           		nrCells(data, d, s + 6) != 0);
	}
	
	private static boolean isUseless(TimetableGridCell[][][] data, int d, int s) {
		return isUselessFirst(data, d, s) || 
			isUselessFirst(data, d, s - 1) || 
			isUselessFirst(data, d, s - 2) || 
			isUselessFirst(data, d, s - 3) || 
			isUselessFirst(data, d, s - 4) || 
			isUselessFirst(data, d, s - 5);
	}
	
	public static void initBgModeUselessSlots(TimetableGridModel model, TimetableGridCell[][][] data) {
        for (int d = 0; d < data.length; d++) {
        	int start = 0; Integer lastPref = null;
            for (int s = 0; s < 288; s++) {
            	Integer pref = null;
            	if (model.isAvailable(d, s)) {
            		pref = 0;
                    if (nrCells(data, d, s)==0) {
                    	if (isUseless(data, d, s)) pref=4;
                    	switch (d) {
                    		case 0 :
                    			if (nrCells(data, 2, s)!=0 && nrCells(data, 4, s)!=0) pref++; 
                    			break;
                    		case 1 :
                    			if (nrCells(data, 3, s)!=0) pref++;
                    			break;
                    		case 2 :
                    			if (nrCells(data, 0, s)!=0 && nrCells(data, 4, s)!=0) pref++;
                    			break;
                    		case 3 :
                    			if (nrCells(data, 1, s)!=0) pref++;
                    			break;
                    		case 4 :
                    			if (nrCells(data, 0, s)!=0 && nrCells(data, 2, s)!=0) pref++; 
                    			break;
                    	}
                    }
            	}
            	if (lastPref != null && !lastPref.equals(pref)) {
            		TimetableGridBackground bg = new TimetableGridBackground();
            		bg.setDay(d);
            		bg.setSlot(start);
            		bg.setLength(s - start);
            		bg.setAvailable(true);
            		bg.setBackground(pref2color(PreferenceLevel.sNeutral)); 
                    if (lastPref > 4)
                    	bg.setBackground(pref2color(PreferenceLevel.sProhibited));
                    else if (lastPref == 4)
                    	bg.setBackground(pref2color(PreferenceLevel.sStronglyDiscouraged));
                    else if (lastPref > 0)
                    	bg.setBackground(pref2color(PreferenceLevel.sDiscouraged));
                    model.addBackground(bg);
            		lastPref = pref; start = s;
            	} else if (lastPref == null && pref != null) {
            		lastPref = pref; start = s;
            	}
            }
            if (lastPref != null) {
            	TimetableGridBackground bg = new TimetableGridBackground();
        		bg.setDay(d);
        		bg.setSlot(start);
        		bg.setLength(288 - start);
        		bg.setAvailable(true);
        		bg.setBackground(pref2color(PreferenceLevel.sNeutral)); 
                if (lastPref > 4)
                	bg.setBackground(pref2color(PreferenceLevel.sProhibited));
                else if (lastPref == 4)
                	bg.setBackground(pref2color(PreferenceLevel.sStronglyDiscouraged));
                else if (lastPref > 0)
                	bg.setBackground(pref2color(PreferenceLevel.sDiscouraged));
                model.addBackground(bg);
            }
        }
	}
	
	public static void computeIndexes(TimetableGridModel model, TimetableGridContext context) {
		Collections.sort(model.getCells());
		
		if (context.getDisplayMode() == DisplayMode.WeekByWeekHorizontal.ordinal()) {
			TimetableGridCell[][][] weekData = new TimetableGridCell[365][288][];
			for (int d = 0; d < 365; d++)
				for (int s = 0; s < 288; s++)
					weekData[d][s] = null;
			for (TimetableGridCell cell: model.getCells()) {
				for (int d = 0; d < 365; d++) {
					int date = d + model.getFirstSessionDay();
					int day = d % 7;
					if (day == cell.getDay() && cell.hasDate(date))
						addCell(weekData, d, cell.getSlot(), cell.getLength(), cell);
				}
			}
			for (int d = 0; d < 365; d++) {
				int date = d + model.getFirstSessionDay();
				int length = 0;
				for (int s = 0; s < 288; s++)
					if (weekData[d][s] != null && weekData[d][s].length > length) length = weekData[d][s].length;
				int[] maxLines = new int[length];
				int index = 0;
				for (int idx = 0; idx < length; idx++) {
					maxLines[idx] = 0;
					for (int s = 0; s < 288; s++) {
						TimetableGridCell c = getCell(weekData, d, s, idx);
						if (c != null && maxLines[idx] < c.getMinLines(context.isShowRoom(), context.isShowInstructor(), context.isShowTime(), context.isShowPreference(), context.isShowDate()))
							maxLines[idx] = c.getMinLines(context.isShowRoom(), context.isShowInstructor(), context.isShowTime(), context.isShowPreference(), context.isShowDate());
					}
					for (int s = 0; s < 288; s++) {
						TimetableGridCell c = getCell(weekData, d, s, idx);
						if (c != null) {
							c.setNrLines(date, maxLines[idx]); c.setIndex(date, index);
						}
					}
					index += maxLines[idx];
				}
				if (context.getResourceType() <= 1)
					for (int idx = 0; idx < length; idx++) {
						for (int s = 0; s < 288; s++) {
							TimetableGridCell c = getCell(weekData, d, s, idx);
							if (c != null && c.getSlot() == s) {
								int empty = nrEmptyAbove(weekData, d, s, idx, length, c);
								if (empty > 0) {
									int lines = maxLines[idx];
									for (int i = 1; i <= empty; i++)
										lines += maxLines[idx + i];
									c.setNrLines(date, lines);
								}
							}
						}
					}
			}
			if (context.isShowFreeTimes()) {
				TimetableGridCell[][][] data = new TimetableGridCell[7][288][];
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s++)
						data[d][s] = null;
				for (TimetableGridCell cell: model.getCells())
					addCell(data, cell.getDay(), cell.getSlot(), cell.getLength(), cell);
				initBgModeUselessSlots(model, data);
			}
		} else {
			TimetableGridCell[][][] data = new TimetableGridCell[7][288][];
			for (int d = 0; d < 7; d++)
				for (int s = 0; s < 288; s++)
					data[d][s] = null;
			for (TimetableGridCell cell: model.getCells()) {
				addCell(data, cell.getDay(), cell.getSlot(), cell.getLength(), cell);
			}
			if (context.isVertical()) {
				for (int d = 0; d < 7; d++) {
					int length = 0;
					for (int s = 0; s < 288; s++)
						if (data[d][s] != null && data[d][s].length > length) length = data[d][s].length;
					for (int idx = 0; idx < length; idx++) {
						for (int s = 0; s < 288; s++) {
							TimetableGridCell c = getCell(data, d, s, idx);
							if (c != null) {
								c.setIndex(idx); c.setNrLines(1);
							}
						}
					}
					if (context.getResourceType() <= 1)
						for (int idx = 0; idx < length; idx++) {
							for (int s = 0; s < 288; s++) {
								TimetableGridCell c = getCell(data, d, s, idx);
								if (c != null && c.getSlot() == s) {
									int empty = nrEmptyAbove(data, d, s, idx, length, c);
									if (empty > 0)
										c.setNrLines(1 + empty);
								}
							}
						}
				}
			} else {
				for (int d = 0; d < 7; d++) {
					int length = 0;
					for (int s = 0; s < 288; s++)
						if (data[d][s] != null && data[d][s].length > length) length = data[d][s].length;
					int[] maxLines = new int[length];
					int index = 0;
					for (int idx = 0; idx < length; idx++) {
						maxLines[idx] = 0;
						for (int s = 0; s < 288; s++) {
							TimetableGridCell c = getCell(data, d, s, idx);
							if (c != null && maxLines[idx] < c.getMinLines(context.isShowRoom(), context.isShowInstructor(), context.isShowTime(), context.isShowPreference(), context.isShowDate()))
								maxLines[idx] = c.getMinLines(context.isShowRoom(), context.isShowInstructor(), context.isShowTime(), context.isShowPreference(), context.isShowDate());
						}
						for (int s = 0; s < 288; s++) {
							TimetableGridCell c = getCell(data, d, s, idx);
							if (c != null) {
								c.setNrLines(maxLines[idx]); c.setIndex(index);
							}
						}
						index += maxLines[idx];
					}
					if (context.getResourceType() <= 1)
						for (int idx = 0; idx < length; idx++) {
							for (int s = 0; s < 288; s++) {
								TimetableGridCell c = getCell(data, d, s, idx);
								if (c != null && c.getSlot() == s) {
									int empty = nrEmptyAbove(data, d, s, idx, length, c);
									if (empty > 0) {
										int lines = maxLines[idx];
										for (int i = 1; i <= empty; i++)
											lines += maxLines[idx + i];
										c.setNrLines(lines);
									}
								}
							}
						}
				}
			}
			if (context.isShowFreeTimes())
				initBgModeUselessSlots(model, data);
		}
		
		if (context.getBgMode() == BgMode.None.ordinal())
			for (TimetableGridCell cell: model.getCells())
				if (!sBgColorNotAvailable.equals(cell.getBackground()))
					for (int s = 0; s < cell.getLength(); s++) {
						if (!model.isAvailable(cell.getDay(), cell.getSlot() + s)) {
							cell.setBackground(sBgColorNotAvailableButAssigned);
							break;
						}
					}
	}
}
