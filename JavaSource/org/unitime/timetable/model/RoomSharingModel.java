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
package org.unitime.timetable.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTableModel;


public class RoomSharingModel extends net.sf.cpsolver.coursett.model.RoomSharingModel implements RequiredTimeTableModel {
	private boolean[][] iEditable = null;
	private int iDefaultSelection = 0;
	
	private Set iEditingDepartments = null;
	
	private boolean iAllowFreeForAll = true;
	private String[] iDepartmentNames = null;
	private String[] iDepartmentAbbvs = null;
	private Color[] iDepartmentColors = null;
	
	public static Color[] sDepartmentColors = new Color[] {
		new Color(240,50,240),
		new Color(50,240,240),
		new Color(240,240,50),
		new Color(240,50,50),
		new Color(50,240,50),
		new Color(50,50,240),
		new Color(150,100,50),
		new Color(50,100,150),
		new Color(150,50,100),
		new Color(100,150,50)};
	public static Color sNotAvailableColor = new Color(150,150,150);
	public static Color sFreeForAllColor = new Color(240,240,240);
	
	public static String sNotAvailableName = "Not Available";
	public static String sFreeForAllName = "Free For All";

	public static String sNotAvailableNameAbbv = "N/A";
	public static String sFreeForAllNameAbbv = "Free";
	
	public RoomSharingModel(Location location, Set editingDepartmentIds) {
		this(location, editingDepartmentIds, null);
	}
	
	
	public RoomSharingModel(Location location, Set editingDepartmentIds, Collection departments) {
		Collection givenDepartments = departments;
		iPreference = new Long[getNrDays()][getNrTimes()];
		iEditable = new boolean[getNrDays()][getNrTimes()];
		
		Hashtable oldDeptPrefs = new Hashtable(); char pref = '0';
		if (location.getManagerIds()!=null) {
			for (StringTokenizer stk = new StringTokenizer(location.getManagerIds(),",");stk.hasMoreTokens();) {
				Long id = Long.valueOf(stk.nextToken());
				oldDeptPrefs.put(new Character(pref++), id);
			}
		}
		
		if (departments==null) {
			departments = new Vector(location.getRoomDepts().size());
			TreeSet managers = new TreeSet(location.getRoomDepts());
			for (Iterator i=managers.iterator();i.hasNext();) {
				RoomDept mgr = (RoomDept)i.next();
				departments.add(mgr.getDepartment());
			}
		}
			
		iDepartmentIds = new Long[departments.size()];
		iDepartmentAbbvs = new String[departments.size()];
		iDepartmentNames = new String[departments.size()];
		iDepartmentColors = new Color[departments.size()];
		iEditingDepartments = editingDepartmentIds;
		iDepartmentIdx = new Hashtable();
		int idx = 0;
		for (Iterator i=departments.iterator();i.hasNext();idx++) {
			Object o = i.next();
			Department mgr = null;
			if (o instanceof RoomDept)
				mgr = ((RoomDept)o).getDepartment();
			else if (o instanceof Department)
				mgr = (Department)o;
			else if (o instanceof Long) 
				mgr = (new DepartmentDAO()).get((Long)o);
			if (mgr==null) throw new RuntimeException("Department "+o+" not found.");
			iDepartmentAbbvs[idx] = mgr.getShortLabel();
			iDepartmentNames[idx] = mgr.getDeptCode()+"-"+mgr.getName();
			iDepartmentIds[idx] = mgr.getUniqueId();
			if (givenDepartments==null && mgr.getRoomSharingColor()!=null)
				iDepartmentColors[idx] = Department.hex2color(mgr.getRoomSharingColor());
			else
				iDepartmentColors[idx] = Department.hex2color(mgr.getRoomSharingColor(departments));
			iDepartmentIdx.put(mgr.getUniqueId(),new Integer(idx));
		}
		
		if (iEditingDepartments!=null) {
			boolean all = true;
			for (int i=0;i<iDepartmentIds.length;i++)
				if (!iEditingDepartments.contains(iDepartmentIds[i])) all=false;
			if (all) iEditingDepartments=null;
		}
		
		boolean allEditable = (iEditingDepartments==null);
		if (!allEditable) {
			boolean all = true;
			for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
				RoomDept mgr = (RoomDept)i.next();
				if (!iEditingDepartments.contains(mgr.getDepartment().getUniqueId())) all=false;
			}
			if (all) allEditable=true;
		}
		
		String pattern = location.getPattern();
		idx = 0;
		for (int d=0;d<getNrDays();d++)
			for (int t=0;t<getNrTimes();t++) {
				pref = (pattern!=null && idx<pattern.length()?pattern.charAt(idx):sDefaultPrefChar);
				idx++;
				
				if (pref==sNotAvailablePrefChar) {
					iPreference[d][t]=sNotAvailablePref;
					iEditable[d][t]=allEditable;
				} else if (pref==sFreeForAllPrefChar) {
					iPreference[d][t]=sFreeForAllPref;
					iEditable[d][t]=allEditable;
				} else {
					Long id = (Long)oldDeptPrefs.get(new Character(pref));
					if (id!=null) {
						boolean containsId = false;
						for (int i=0;i<iDepartmentIds.length;i++) {
							if (iDepartmentIds[i].equals(id)) {
								containsId = true; break;
							}
						}
						if (!containsId) id = null;
					}
					if (id==null) {
						iPreference[d][t]=sDefaultPref;
						iEditable[d][t]=allEditable;
					} else {
						iPreference[d][t]=id;
						iEditable[d][t]=(allEditable?true:iEditingDepartments.contains(id));;
					}
				}
			}
	}
	
	public String getManagerIds() {
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<getNrDepartments();i++) {
			if (i>0) sb.append(",");
			sb.append(iDepartmentIds[i]);
		}
		return sb.toString();
	}
	
	public String getName() { return null; }
	
	public String getStartTime(int time) {
		int min = 6*time*Constants.SLOT_LENGTH_MIN+Constants.FIRST_SLOT_TIME_MIN / 10;
        int startHour = min / 60;
        int startMinute = min % 60;

        return (startHour>12?startHour-12:startHour)+":"+(startMinute<10?"0":"")+startMinute+(startHour>=12?"p":"a");
	}
	
	public String getEndTime(int time) {
		int min = 6*(time+1)*Constants.SLOT_LENGTH_MIN+Constants.FIRST_SLOT_TIME_MIN / 10;
        int endHour = min / 60;
        int endMinute = min % 60;

        return (endHour>12?endHour-12:endHour)+":"+(endMinute<10?"0":"")+endMinute+(endHour>=12?"p":"a");
	}
	
	public String getDayHeader(int day) {
		return Constants.DAY_NAME[day];
	}
	
	private static String xChars="0123456789abcdefghijklmnopqrstuvwxyz -";
    public String getPreferencesHex() {
    	int[] limit = getSelectionLimits(getDefaultSelection());
    	BigInteger idn = new BigInteger("0");
    	BigInteger mxColor = new BigInteger(String.valueOf(0x1000000));
    	for (int i=0;i<iDepartmentColors.length;i++) {
    		Color c = iDepartmentColors[i];
    		idn = idn.multiply(mxColor).add(new BigInteger(String.valueOf(c.getRGB()&0xFFFFFF)));
    	}
    	BigInteger mxLen = new BigInteger("16");
    	BigInteger mxPref = new BigInteger(String.valueOf(2+getPreferenceNames().length));
    	int nr = 0;
    	Long px = null;
        for (int d=limit[2];d<=limit[3];d++)
            for (int t=limit[0];t<=limit[1];t++) {
            	Long preference = iPreference[d][t];
            	if (px==null) {
            		px = preference; nr = 1;
            	} else if (px.equals(preference) && nr<16) {
            		nr++;
            	} else {
            		int x=0;
                	if (px.equals(sNotAvailablePref))
                		x = getNrDepartments();
                	else if (px.equals(sFreeForAllPref))
                		x = getNrDepartments()+1;
                	else
                		x = getIndex(px);
                	idn = idn.multiply(mxLen).add(new BigInteger(String.valueOf(nr-1))).multiply(mxPref).add(new BigInteger(String.valueOf(x)));
            		px=preference; nr=1;
            	}
            }
        if (px!=null) {
    		int x=0;
        	if (px.equals(sNotAvailablePref))
        		x = getNrDepartments();
        	else if (px.equals(sFreeForAllPref))
        		x = getNrDepartments()+1;
        	else
        		x = getIndex(px);
        	idn = idn.multiply(mxLen).add(new BigInteger(String.valueOf(nr-1))).multiply(mxPref).add(new BigInteger(String.valueOf(x)));
        }
        StringBuffer s = new StringBuffer("R"+iDepartmentColors.length+"_"+(limit[1]-limit[0])+"x"+(limit[3]-limit[2])+"_");
        BigInteger radix = new BigInteger(String.valueOf(xChars.length()));
        while (idn.bitLength()>0) {
        	int x = idn.mod(radix).intValue();
        	idn = idn.divide(radix);
        	s.append(xChars.charAt(x));
        }
        return s.toString();
    }
	
	
	public String getFileName() {
		return getPreferencesHex();
	}
	
	public void setPreference(int day, int time, String pref) {
		Long p = Long.valueOf(pref);
		if (p.equals(sFreeForAllPref) || p.equals(sNotAvailablePref) || getIndex(p)>=0)
			iPreference[day][time]=p;
		else
			iPreference[day][time]=sDefaultPref;
	}

	public String getPreference(int day, int time) {
		return String.valueOf(iPreference[day][time]);
	}
	
	public String getFieldText(int day, int time) {
		return getPreference(day, time);
	}

	public boolean isEditable(int day, int time) {
		return iEditable[day][time];
	}
	
	public void setPreferences(String pattern) {
		try {
			int idx = 0;
			for (int d=0;d<getNrDays();d++)
				for (int t=0;t<getNrTimes();t++) {
					char pref = (pattern!=null && idx<pattern.length()?pattern.charAt(idx):sDefaultPrefChar);
					idx++;
					if (pref==sNotAvailablePrefChar) {
						iPreference[d][t]=sNotAvailablePref;
						iEditable[d][t]=true;
					} else if (pref==sFreeForAllPrefChar) {
						iPreference[d][t]=sFreeForAllPref;
						iEditable[d][t]=true;
					} else {
						iPreference[d][t]=iDepartmentIds[(int)(pref-'0')];
						iEditable[d][t]=(iEditingDepartments==null?true:iEditingDepartments.contains(iPreference[d][t]));
					}
				}
		} catch (NullPointerException e) {
		} catch (IndexOutOfBoundsException e) {
		}
	}
	
	public boolean isExactTime() { return false; }
    public int getExactDays() { return -1; }
    public int getExactStartSlot() { return -1; }
    public void setExactDays(int days) {};
    public void setExactStartSlot(int slot) {};
    
	public String getDefaultPreference() { return String.valueOf(sDefaultPref); }
	
	public Color getBorder(int day, int time) { return null; }
	
	public String[] getPreferenceNames() {
		String[] ret = new String[getNrDepartments()+(iAllowFreeForAll?2:1)];
		int idx = 0;
		for (int i=0;i<getNrDepartments();i++)
			ret[idx++] = String.valueOf(iDepartmentIds[i]);
		ret[idx++] = String.valueOf(sNotAvailablePref);
		if (iAllowFreeForAll)
			ret[idx++] = String.valueOf(sFreeForAllPref);
		return ret;
	}
	
	public Color getPreferenceColor(String pref) {
		Long p = Long.valueOf(pref);
		if (p.equals(sFreeForAllPref)) return sFreeForAllColor;
		if (p.equals(sNotAvailablePref)) return sNotAvailableColor;
		int idx = getIndex(p);
		if (idx<0) return Color.BLACK;
		return iDepartmentColors[idx];
	}
	
	public String getPreferenceText(String pref) {
		Long p = Long.valueOf(pref);
		if (p.equals(sFreeForAllPref)) return sFreeForAllName;
		if (p.equals(sNotAvailablePref)) return sNotAvailableName;
		return (iDepartmentNames==null?"Department "+(1+getIndex(p)):iDepartmentNames[getIndex(p)]);
	}
	
	public String getPreferenceAbbv(Long deptId) {
		if (deptId.equals(sFreeForAllPref)) return sFreeForAllNameAbbv;
		if (deptId.equals(sNotAvailablePref)) return sNotAvailableNameAbbv;
		return (iDepartmentAbbvs==null?"D"+(1+getIndex(deptId)):iDepartmentAbbvs[getIndex(deptId)]);
	}

	public int getNrSelections() {
		return 5;
	}
	public String getSelectionName(int idx) {
		switch (idx) {
			case 0: return "Workdays &times; Daytime";
			case 1: return "All Week &times; Daytime";
			case 2: return "Workdays &times; Evening";
			case 3: return "All Week &times; Evening";
			case 4: return "All Week &times; All Times";
		}
		return null;
	}
	public int[] getSelectionLimits(int idx) {
		switch (idx) {
			case 0: return new int[] {15,36,0,4};
			case 1: return new int[] {15,36,0,6};
			case 2: return new int[] {37,47,0,4};
			case 3: return new int[] {37,47,0,6};
			case 4: return new int[] {0,47,0,6};
		}
		return new int[] {0,getNrTimes()-1,0,getNrDays()-1};
	}
	public void setDefaultSelection(int selection) {
		iDefaultSelection = selection;
	}
	public void setDefaultSelection(String selection) {
		iDefaultSelection = 0;
		if (selection==null) return;
		for (int i=0;i<getNrSelections();i++) {
			if (selection.equalsIgnoreCase(getSelectionName(i).replaceAll("&times;","x"))) {
				iDefaultSelection = i;
				break;
			}
		}
	}
	public int getDefaultSelection() {
		return iDefaultSelection;
	}
	
	public String getPreferenceCheck() {
		if (iEditingDepartments==null) {
			return null;
		} else {
			StringBuffer sb = new StringBuffer("if (pref!='"+sNotAvailablePref+"' && pref!='"+sFreeForAllPref+"'");
			for (Iterator i=iEditingDepartments.iterator();i.hasNext();) {
				Long editingDeptId = (Long)i.next();
				sb.append(" && pref!='"+editingDeptId+"'");
			}
			sb.append(") alert('WARNING: When saved, ownership of the selected time slots will be transferred to this department.');");
			sb.append("if (pref=='"+sNotAvailablePref+"' || pref=='"+sFreeForAllPref+"') "+
					"alert('WARNING: When saved, ownership of the selected time slots will be transferred to room administrator.');");
			return sb.toString();
		}
	}
	
	public boolean allAvailable(Long departmentId) {
		for (int d=0;d<getNrDays();d++)
			for (int t=0;t<getNrTimes();t++) {
				Long pref = iPreference[d][t];
				if (pref.equals(sFreeForAllPref)) continue;
				if (departmentId!=null && departmentId.equals(pref)) continue;
				return false;
			}
		return true;
	}
	
	public boolean isPreferenceEnabled(String pref) {
		return true;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean out[][] = new boolean [getNrDays()][getNrTimes()];
        for (int i = 0; i < getNrDays(); i++)
               for (int j = 0; j < getNrTimes(); j++)
            	   out[i][j]=false;
        for (int i = 0; i < getNrDays(); i++)
            for (int j = 0; j < getNrTimes(); j++) {
         	   if (out[i][j]) continue;
         	   out[i][j]=true;
         	   if (sFreeForAllPref.equals(iPreference[i][j])) continue;
         	   int endDay = i, endTime = j;
         	   while (endTime+1<getNrTimes() && !out[i][endTime+1] && iPreference[i][endTime+1].equals(iPreference[i][j]))
         		   endTime++;
         	   while (endDay+1<getNrDays()) {
         		   boolean same = true;
         		   for (int x=j;x<=endTime;x++)
         			   if (!out[endDay+1][x] && !iPreference[i][x].equals(iPreference[endDay+1][x])) {
         				   same = false; break;
         			   }
         		   if (!same) break;
         		   endDay++;
         	   }
         	   for (int a=i;a<=endDay;a++)
         		   for (int b=j;b<=endTime;b++)
         			   out[a][b]=true;
         	   if (sb.length()>0) sb.append(", ");
         	   sb.append(getPreferenceAbbv(iPreference[i][j])+" ");
         	   int nrDays = endDay-i+1;
         	   if (i==0 && endDay+1==Constants.DAY_CODES.length) {
         		   //all week
         	   } else {
         		   for (int a=i;a<=endDay;a++)
         			   sb.append(nrDays==1?Constants.DAY_NAME[a]:Constants.DAY_NAMES_SHORT[a]);
         	   }
         	   if (j==0 && endTime+1==getNrTimes()) {
         		   //all day
         	   } else {
         		  sb.append(" ");
         		  int startMins = 6*j*Constants.SLOT_LENGTH_MIN+Constants.FIRST_SLOT_TIME_MIN / 10;
         		  int startHour = startMins / 60;
         		  int startMinute = startMins % 60;
         		  sb.append((startHour>12?startHour-12:startHour)+":"+(startMinute<10?"0":"")+startMinute+(startHour>=12?"p":"a"));
         		  sb.append(" - ");
              
         		  int endMins = 6*(endTime+1)*Constants.SLOT_LENGTH_MIN+Constants.FIRST_SLOT_TIME_MIN / 10;
         		  int endHour = endMins / 60;
         		  int endMinute = endMins % 60;
         		  sb.append((endHour>12?endHour-12:endHour)+":"+(endMinute<10?"0":"")+endMinute+(endHour>=12?"p":"a"));
         	   }
            }
		return sb.toString();
	}
}
