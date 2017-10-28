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

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTableModel;



/**
 * @author Tomas Muller
 */
public class TimePatternModel implements RequiredTimeTableModel {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	private TimePattern iTimePattern = null;
	private int iDefaultSelection = 0;
	
    protected String iDays[] = null;
    protected int iDayCodes[] = null;
    protected int iMinutes[] = null;
    protected String iPreferences[][];
    protected String iPref = null;
    protected boolean iAllowHard = true;
    protected int iBreakTime = 0;
    
    protected TimeLocation iAssignment = null;
    
    private List<RoomInterface.RoomSharingDisplayMode> iModes = new ArrayList<RoomInterface.RoomSharingDisplayMode>();
    
    protected TimePatternModel(TimePattern pattern, TimeLocation assignment, boolean allowHard) {
		iTimePattern = pattern;
		iAssignment = assignment;
		iAllowHard = allowHard;
		iBreakTime = (pattern==null?0:pattern.getBreakTime().intValue());

		if (iTimePattern==null || iTimePattern.getUniqueId()==null || iTimePattern.getUniqueId().longValue()<0) {
			iTimePattern=null;
	    	iDays = new String[Constants.NR_DAYS];
	    	iDayCodes = new int[Constants.NR_DAYS];
	    	for (int i=0;i<Constants.NR_DAYS;i++) {
	    		iDayCodes[i]=Constants.DAY_CODES[i];
	    		iDays[i]=CONSTANTS.days()[i];
	    	}
	    	iMinutes = new int[Constants.SLOTS_PER_DAY];
	    	for (int i=0;i<Constants.SLOTS_PER_DAY;i++) {
	    		iMinutes[i] = Constants.FIRST_SLOT_TIME_MIN + (Constants.SLOT_LENGTH_MIN*i);
	    	}
	        iPreferences = new String[Constants.NR_DAYS][Constants.SLOTS_PER_DAY];
	        for (int i=0;i<iPreferences.length;i++)
	        	for (int j=0;j<iPreferences[i].length;j++)
	        		iPreferences[i][j]=PreferenceLevel.sNeutral;
		} else {
	    	Vector days = new Vector(pattern.getDays());
	    	Collections.sort(days);
	    	iDays = new String[days.size()];
	    	iDayCodes = new int[days.size()];
	    	int idx=0;
	    	Integer firstDayOfWeek = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
	    	for (Enumeration e=days.elements();e.hasMoreElements();idx++) {
	    		int dayCode = ((TimePatternDays)e.nextElement()).getDayCode().intValue();
	    		iDayCodes[idx] = dayCode;
	    		iDays[idx] = "";
	    		for (int i=0;i<Constants.DAY_CODES.length;i++) {
	    			int j = (firstDayOfWeek == null ? i : (i + firstDayOfWeek) % 7);
	    			if ((Constants.DAY_CODES[j]&dayCode)==0) continue;
	    			if (pattern.getNrMeetings().intValue()<=1)
	    				iDays[idx] += CONSTANTS.days()[j];
	    			else {
	    				iDays[idx] += CONSTANTS.shortDays()[j];
	    			}
	    		}
	    	}
	    	
	    	Vector times = new Vector(pattern.getTimes());
	    	Collections.sort(times);
	    	iMinutes = new int[times.size()];
	    	idx=0;
	    	for (Enumeration e=times.elements();e.hasMoreElements();idx++) {
	    		int startSlot = ((TimePatternTime)e.nextElement()).getStartSlot().intValue();
	    		iMinutes[idx] = Constants.FIRST_SLOT_TIME_MIN + (Constants.SLOT_LENGTH_MIN*startSlot);
	    	}
	    	
	        iPreferences = new String[days.size()][times.size()];
	        for (int i=0;i<iPreferences.length;i++)
	        	for (int j=0;j<iPreferences[i].length;j++)
	        		iPreferences[i][j]=PreferenceLevel.sNeutral;
		}
		
		if (iTimePattern == null) {
			for (int i = 0; true; i++) {
				String mode =ApplicationProperty.RoomSharingMode.value(String.valueOf(1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
				if (mode == null || mode.isEmpty()) break;
				iModes.add(new RoomInterface.RoomSharingDisplayMode(mode));
			}
		}
	}
	
	/** 1x30 time pattern */
	protected TimePatternModel() {
		this(null, null, true);
	}
	
	public TimePattern getTimePattern() { return iTimePattern; }
	
	public int getNrMeetings() {
		return (iTimePattern==null?Constants.NR_DAYS:iTimePattern.getNrMeetings().intValue());
	}
	
	public int getSlotsPerMtg() {
		return (iTimePattern==null?1:iTimePattern.getSlotsPerMtg().intValue());
	}
	
	public int getMinPerMtg() {
		return (iTimePattern==null?Constants.SLOT_LENGTH_MIN:iTimePattern.getMinPerMtg().intValue());
	}
	
	public int getType() {
		return (iTimePattern==null?TimePattern.sTypeStandard:iTimePattern.getType().intValue());
	}
	
	public String getName() {
		return (iTimePattern==null?null:iTimePattern.getName());
	}

	public int getDayCode(int day) {
        return iDayCodes[day];
    }

    public String getDayHeader(int day) {
        return iDays[day];
    }

    public int getHour(int time) {
        return iMinutes[time] / 60;
    }

    public int getMinute(int time) {
        return iMinutes[time] % 60;
    }

    public int getNrDays() {
        return iDays.length;
    }

    public int getNrTimes() {
        return iMinutes.length;
    }

    public String getPreference(int day, int time) {
        return iPreferences[day][time];
    }

    public String getStartTime(int time) {
        return Constants.toTime(iMinutes[time]);
    }
    
    public String getEndTime(int time) {
        return Constants.toTime(iMinutes[time] + getSlotsPerMtg()*Constants.SLOT_LENGTH_MIN - iBreakTime);
    }

    public String getTimeHeaderShort(int time) {
        return Constants.toTime(iMinutes[time]);
    }
    
    public static int slot2min(int slot) {
    	return (slot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) % 60;  
    }

    public static int slot2hour(int slot) {
    	return (slot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN) / 60;  
    }
    
    public static int time2slot(int hour, int min) {
    	return (hour*60+min)/Constants.SLOT_LENGTH_MIN - Constants.FIRST_SLOT_TIME_MIN;
    }

    public void setDefaults(User user) throws Exception {
        long startMin = 7*60 + 30;
        long endMin = (getMinPerMtg()<=90? 16*60 + 30: getMinPerMtg()<=120? 15*60 + 30: 15*60);

        for (int i = 0; i < iDays.length; i++)
            for (int j = 0; j < iMinutes.length; j++)
            	iPreferences[i][j] = (iMinutes[j]<=startMin ? PreferenceLevel.sStronglyDiscouraged :
            			iMinutes[j]>=endMin ? PreferenceLevel.sDiscouraged :
            			PreferenceLevel.sNeutral);
    }
    
    public boolean setPreferenceUsingDayCodeStartSlot(int dayCode, int startSlot, String pref){
     	if (isExactTime()){
    		iPref = Integer.toString(dayCode) + "," + Integer.toString(startSlot);
    		return(true);
    	} else {
	    	int day = -1;
	    	int time = -1;
	    	for(int i = 0; i < iDayCodes.length; i++){
	    		if (iDayCodes[i] == dayCode){
	    			day = i;
	    			break;
	    		}
	    	}
	    	for(int i = 0; i < iMinutes.length; i++){
	    		if ((Constants.FIRST_SLOT_TIME_MIN + (Constants.SLOT_LENGTH_MIN*startSlot)) == iMinutes[i]){
	    			time = i;
	    			break;
	    		}
	    	}
	    	if (time < 0 || day < 0){
	    		return(false);
	    	} else {
	    		setPreference(day, time, pref);
	    		return(true);
	    	}
    	}
    }

    public void setPreference(int day, int time, PreferenceLevel pref) {
    	iPreferences[day][time] = pref.getPrefProlog();
    }

    public void setPreference(int day, int time, String prefProlog) {
    	iPreferences[day][time] = prefProlog;
    }

    public void clear() {
    	for (int i = 0; i < iDays.length; i++)
            for (int j = 0; j < iMinutes.length; j++)
                iPreferences[i][j] = PreferenceLevel.sNeutral;
    }

    public Vector getSlots(int day, int time) {
        Vector slots = new Vector();
        
        int dayCode = iDayCodes[day];
        int slot = getStartSlot(time);

        for (int i=0; i<Constants.DAY_CODES.length; i++) {
        	if ((dayCode & Constants.DAY_CODES[i])==0) continue;
        	int s = slot + i*Constants.SLOTS_PER_DAY;
        	for (int j=0; j<getSlotsPerMtg(); j++)
        		slots.addElement(new Integer(s+j));
        }

        return slots;
    }
    
    public int getStartSlot(int time) {
    	return time2slot(0,iMinutes[time]); 
    }

    public java.util.Collection getStartSlots(int day, int time) {
        Vector slots = new Vector();
        
        int dayCode = iDayCodes[day];
        int slot = getStartSlot(time);

        for (int i=0; i<Constants.DAY_CODES.length; i++) {
        	if ((dayCode & Constants.DAY_CODES[i])==0) continue;
        	slots.addElement(new Integer(slot + i*Constants.SLOTS_PER_DAY));
        }

        return slots;
    }

    public String getText(int day, int time) {
        return getDayHeader(day) + " " + getTimeHeaderShort(time);
    }
    
    public String getPreferences() {
    	if (isExactTime()) return iPref;
    	StringBuffer pref = new StringBuffer(); 
    	for (int i=0; i<iDays.length; i++)
            for (int j=0; j<iMinutes.length; j++)
            	pref.append(PreferenceLevel.prolog2char(iPreferences[i][j]));
    	return pref.toString();
    }
    
    private static String xChars="0123456789abcdefghijklmnopqrstuvwxyz -"; 
    public String getPreferencesHex() {
    	if (isExactTime()) return iPref;
    	int[] limit = getSelectionLimits(getDefaultSelection());
    	BigInteger idn = new BigInteger("0");
    	List<PreferenceLevel> prefs = PreferenceLevel.getPreferenceLevelList();
    	BigInteger mx = new BigInteger(String.valueOf(prefs.size()));
        for (int d=limit[2];d<=limit[3];d++)
            for (int t=limit[0];t<=limit[1];t++) {
            	BigInteger add = new BigInteger(String.valueOf(prefs.indexOf(PreferenceLevel.getPreferenceLevel(getPreference(d,t)))));
            	idn = idn.multiply(mx).add(add);
            }
        StringBuffer s = new StringBuffer();
        BigInteger radix = new BigInteger(String.valueOf(xChars.length()));
        while (idn.bitLength()>0) {
        	int x = idn.mod(radix).intValue();
        	idn = idn.divide(radix);
        	s.append(xChars.charAt(x));
        }
        return s.toString();
    }

    public void setPreferences(String pref) {
    	try {
    		if (isExactTime()) {
    			iPref = pref;
    		} if (pref != null && pref.length() <= 336 && iDays.length * iMinutes.length == 2016) {
    			boolean req = pref.indexOf('R') >= 0;
    			for (int i=0; i<iDays.length; i++)
    				for (int j=0; j<iMinutes.length; j++) {
    					char ch = '2';
    					try {
    						ch = pref.charAt(48 * i + j / 6);
    					} catch (ArrayIndexOutOfBoundsException e) {}
    					iPreferences[i][j] = PreferenceLevel.char2prolog(req ? ch == 'R' ? '2' : 'P' : ch);
    				}
    		} else {
    			int idx = 0;
    			for (int i=0; i<iDays.length; i++)
    				for (int j=0; j<iMinutes.length; j++)
    					iPreferences[i][j] = (pref==null?PreferenceLevel.sNeutral:PreferenceLevel.char2prolog(pref.charAt(idx++)));
    		}
    	} catch (IndexOutOfBoundsException e) {}
    }

    public static double sDefaultDecreaseFactor = 0.77;
    
    public String getFieldText(int day, int time) {
    	return String.valueOf(Math.round(getNormalizedPreference(day, time, sDefaultDecreaseFactor)));
    }
    
    public double getNormalizedPreference(int day, int time, double decreaseFactor) {
        if (iPreferences[day][time].equalsIgnoreCase(PreferenceLevel.sRequired)) return 0.0;
        if (iPreferences[day][time].equalsIgnoreCase(PreferenceLevel.sNotAvailable)) {
        	return 100.0;
        }
        if (iPreferences[day][time].equalsIgnoreCase(PreferenceLevel.sProhibited)) return 100.0;
        
        int pref = Integer.parseInt(iPreferences[day][time]);
        if (pref==0) return 0.0;
        
        double nrOfPreferences = 0;
        // case A -- constant increment (except of 1x50, where 1/2 is taken)
        double increment = (getMinPerMtg()<=30?0.5:1.0);
        //case B -- proportional increment
        //double increment = 10.0/getNrTimes();

        for (int i = 0; i < iDays.length; i++) {
        	double nrOfPreferencesThisDay = 0;
        	for (int j = 0; j < iMinutes.length; j++) {
        		String p = iPreferences[i][j];
                if (PreferenceLevel.sRequired.equalsIgnoreCase(p)) continue;
                if (PreferenceLevel.sNotAvailable.equalsIgnoreCase(p)) continue;
                if (PreferenceLevel.sProhibited.equalsIgnoreCase(p) || Integer.parseInt(p)!=0) nrOfPreferencesThisDay+=increment;
            }
            nrOfPreferences = Math.max(nrOfPreferences,nrOfPreferencesThisDay);
        }
        
        //double norm = Math.max(1.0,Math.round(11.0 - nrOfPreferences));
        double norm = Math.max(1.0,Math.round(10.0*Math.pow(decreaseFactor,Math.max(0,nrOfPreferences-1))));
        double sign = (pref<0?-1.0:1.0);
        double mux = (Math.abs(pref)==1?1.0:4.0);

        return sign * norm * mux;
    }
    
    public int countPreferences(String prologPref) {
        int ret = 0;

        for (int i = 0; i < iDays.length; i++)
            for (int j = 0; j < iMinutes.length; j++)
                if (iPreferences[i][j].equals(prologPref))
                    ret++;

        return ret;
    }
    
    public boolean isDefault() {
        for (int i = 0; i < iDays.length; i++)
            for (int j = 0; j < iMinutes.length; j++)
                if (!iPreferences[i][j].equals(getDefaultPreference()))
                    return false;
        return true;
    }

    public boolean contains(Assignment assignment) {
        if (assignment == null) {
            return false;
        }

        int startSlots[] = assignment.getStartSlots();

        for (int day = 0; day < iDays.length; day++) {
            for (int time = 0; time < iMinutes.length; time++) {
                int slot = getStartSlot(time);
                int j = 0;
                boolean equal = true;

                for (int i=0; equal && (i<Constants.DAY_CODES.length); i++)
                    if ((iDayCodes[day]&Constants.DAY_CODES[i]) == Constants.DAY_CODES[i]) {
                    	int s = slot + (i*Constants.SLOTS_PER_DAY);

                        if (startSlots[j] != s) {
                            equal = false;
                            break;
                        }

                        j++;
                    }

                if (equal) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public long getTime(int time) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR_OF_DAY, iMinutes[time]/60);
        cal.set(Calendar.MINUTE, iMinutes[time]%60);

        return cal.getTimeInMillis();
    }
    
    public void setInOldFormat(String inDays, Date startTime, Date endTime, PreferenceLevel pref) {
    	int days = Integer.parseInt(inDays, 2);

        for (int i = 0; i < iDays.length; i++)
            if ((days & iDayCodes[i]) == iDayCodes[i]) {
                for (int j = 0; j < iMinutes.length; j++)
                    if (((endTime == null)
                            && (startTime.getTime() == getTime(j)))
                            || ((endTime != null)
                            && (startTime.getTime() <= getTime(j))
                            && (endTime.getTime() >= getTime(j)))) {
                        iPreferences[i][j] = pref.getPrefProlog();
                    }
            }
    }    
    
    public String toString() {
    	Integer firstDayOfWeek = ApplicationProperty.TimePatternFirstDayOfWeek.intValue();
    	if (isExactTime()) {
    		if (iPref==null) return "not set";
    		int days = getExactDays();
    		int startSlot = getExactStartSlot();
    		StringBuffer sb = new StringBuffer();
    		for (int i=0;i<Constants.DAY_CODES.length;i++) {
    			int j = (firstDayOfWeek == null ? i : (i + firstDayOfWeek) % 7);
    			if ((Constants.DAY_CODES[j]&days)!=0)
    				sb.append(CONSTANTS.shortDays()[j]);
    		}
    		sb.append(" ");
    		sb.append(Constants.toTime(startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN));
    		return sb.toString();
    	} else {
    		boolean canMergeDays = true;
    		for (int i = 0; canMergeDays && i+1 < iDays.length; i++) {
    			for (int j = i+1; canMergeDays && j < iDays.length; j++) {
    				if ((iDayCodes[i] & iDayCodes[j])!=0) canMergeDays = false;
    			}
    		}
    		StringBuffer sb = new StringBuffer();
    		boolean out[][] = new boolean [iDays.length][iMinutes.length];
            for (int i = 0; i < iDays.length; i++)
                   for (int j = 0; j < iMinutes.length; j++)
                	   out[i][j]=false;
            for (int i = 0; i < iDays.length; i++)
                for (int j = 0; j < iMinutes.length; j++) {
             	   if (out[i][j]) continue;
             	   out[i][j]=true;
             	   if (PreferenceLevel.sNeutral.equals(iPreferences[i][j])) continue;
             	   int endDay = i, endTime = j;
             	   while (endTime+1<iMinutes.length && !out[i][endTime+1] && iPreferences[i][endTime+1].equals(iPreferences[i][j]))
             		   endTime++;
             	   if (i==0) {
             		   boolean same = true;
             		   for (int k=i;k+1<iDays.length;k++) {
                 		   for (int x=j;x<=endTime;x++) {
                 			   if (!out[k+1][x] && !iPreferences[i][x].equals(iPreferences[k+1][x])) {
                 				   same = false; break;
                 			   }
                 			   if (!same) break;
                 		   }
                 		   if (!same) break;
             		   }
             		   if (same) endDay = iDays.length-1;
             	   }
             	   while (canMergeDays && endDay+1<iDays.length) {
             		   boolean same = true;
             		   for (int x=j;x<=endTime;x++)
             			   if (!out[endDay+1][x] && !iPreferences[i][x].equals(iPreferences[endDay+1][x])) {
             				   same = false; break;
             			   }
             		   if (!same) break;
             		   endDay++;
             	   }
             	   for (int a=i;a<=endDay;a++)
             		   for (int b=j;b<=endTime;b++)
             			   out[a][b]=true;
             	   if (sb.length()>0) sb.append(", ");
             	   sb.append(PreferenceLevel.prolog2abbv(iPreferences[i][j])+" ");
             	   int nrDays = 0;
              	  for (int x=0;x<Constants.DAY_CODES.length;x++) {
             		  boolean thisDay = false;
             		  for (int a=i;a<=endDay;a++)
             			  if ((iDayCodes[a] & Constants.DAY_CODES[x])!=0)
             				 thisDay = true;
             		  if (thisDay) nrDays++;
             	  }
             	   
             	  for (int x=0;x<Constants.DAY_CODES.length;x++) {
             		  int y = (firstDayOfWeek == null ? x : (x + firstDayOfWeek) % 7);
             		  boolean thisDay = false;
             		  for (int a=i;a<=endDay;a++)
             			  if ((iDayCodes[a] & Constants.DAY_CODES[y])!=0)
             				 thisDay = true;
             		  if (thisDay)
             			  sb.append(nrDays==1?CONSTANTS.days()[y]:CONSTANTS.shortDays()[y]);
             	  }
             	  String d1 = " ";
             	  String d2 = " ";
             	  for (int x = 0; x < 7; x++) {
             		 int y = (firstDayOfWeek == null ? x : (x + firstDayOfWeek) % 7);
             		 if (x < 5) d1 += CONSTANTS.shortDays()[y];
             		  d2 += CONSTANTS.shortDays()[y];
             	  }
             	  if (iTimePattern!=null && sb.toString().endsWith(d1))
             		  sb.delete(sb.length()-d1.length(), sb.length());
             	  if (iTimePattern==null && sb.toString().endsWith(d2))
             		  sb.delete(sb.length()-d2.length(), sb.length());
             	  if (j==0 && endTime+1==iMinutes.length) {
             		  //all day
             	  } else {
             		  sb.append(" ");
             		  sb.append(Constants.toTime(iMinutes[j]));
             		  sb.append(" - ");
                      sb.append(Constants.toTime(iMinutes[endTime] + getSlotsPerMtg()*Constants.SLOT_LENGTH_MIN - iBreakTime));
             	  }
                }
    		return sb.toString();
    	}
    }
    
    public boolean isExactTime() {
   		return getType()==TimePattern.sTypeExactTime;
    }
    
    public int getExactDays() {
		if (iPref==null || iPref.indexOf(',')<0) return 0;
		return Integer.parseInt(iPref.substring(0, iPref.indexOf(',')));
    }
    
    public int getExactStartSlot() {
    	if (iPref==null || iPref.indexOf(',')<0) return -1;
    	return Integer.parseInt(iPref.substring(iPref.indexOf(',')+1));
    }
    
    public void setExactDays(int days) {
    	iPref = days+","+getExactStartSlot(); 
    }
    
    public void setExactStartSlot(int slot) {
    	iPref = getExactDays()+","+slot;
    }
    
    public boolean hasRequiredPreferences() {
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++)
    			if (PreferenceLevel.sRequired.equals(iPreferences[d][t]))
    				return true;
    	return false;
    }
    
    public boolean hasNotAvailablePreference() {
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++)
    			if (PreferenceLevel.sNotAvailable.equals(iPreferences[d][t]))
    				return true;
    	return false;
    }
    
    public boolean changeRequired2Prohibited() {
    	if (isExactTime()) return false;
    	boolean hasReq = false;
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++)
    			if (PreferenceLevel.sRequired.equals(iPreferences[d][t])) {
    				hasReq = true; break;
    			}
    	if (!hasReq) return false;
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++)
    			iPreferences[d][t] = (PreferenceLevel.sRequired.equals(iPreferences[d][t])?PreferenceLevel.sNeutral:PreferenceLevel.sProhibited);
    	return true;
    }
    
    public boolean changeProhibited2Required() {
    	if (isExactTime()) return false;
    	boolean hasElseThanNeutralProhibited = false;
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++)
    			if (!PreferenceLevel.sProhibited.equals(iPreferences[d][t]) && !PreferenceLevel.sNeutral.equals(iPreferences[d][t])) {
    				hasElseThanNeutralProhibited = true; break;
    			}
    	if (hasElseThanNeutralProhibited) return false;
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++)
    			iPreferences[d][t] = (PreferenceLevel.sProhibited.equals(iPreferences[d][t])?PreferenceLevel.sNeutral:PreferenceLevel.sRequired);
    	return true;
    }
    
    public static int getNrSharedSlots(int dayCode1, int slot1, int nrSlots1, int dayCode2, int slot2, int nrSlots2) {
    	int start = Math.max(slot1, slot2);
    	int end = Math.min(slot1+nrSlots1, slot2+nrSlots2);
    	if (start>=end) return 0;
    	int sharedTimes = end-start;
    	int sharedDays = 0;
    	for (int i=0; i<Constants.NR_DAYS; i++) {
    		int dayCode = Constants.DAY_CODES[i];
    		if ((dayCode1 & dayCode)==0) continue;
    		if ((dayCode2 & dayCode)==0) continue;
    		sharedDays++;
    	}
    	return sharedDays * sharedTimes;
    }
    
    public String getCombinedPreference(int givenDayCode, int givenSlot, int givenNrSlots, int alg) {
    	PrefMix mix = getMixAlg(alg);
    	for (int i=0; i<iDayCodes.length; i++) {
    		int dayCode = iDayCodes[i];
    		for (int j=0; j<iMinutes.length; j++) {
    			int min = iMinutes[j];
    			int slot = (min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
    			int share = getNrSharedSlots(dayCode, slot, getSlotsPerMtg(), givenDayCode, givenSlot, givenNrSlots);
    			if (share>0)
    				mix.addPref(iPreferences[i][j],share);
    		}
    	}
    	return mix.getPref();
    }
    
    public void combineWith(TimePatternModel model, boolean clear) {
    	combineWith(model, clear, sMixAlgMinMax);
    }
    
    /**
     * Combines preferences of two (different) time patterns into one
     * @param model another model to take preferences from (to put into the current one)
     * @param clear if true, preferences in current model are cleared first (it transfers preferences from the given model to the current one)
     * @param alg algorithm that should be used for combining preferences (one of sMixAlgXxx constants)
     */
    public void combineWith(TimePatternModel model, boolean clear, int alg) {
    	boolean changed = false;
    	if (clear) clear(); else changed = changeRequired2Prohibited();
    	changed = model.changeRequired2Prohibited() || changed;
    	for (int i=0; i<iDayCodes.length; i++) {
    		int dayCode = iDayCodes[i];
    		for (int j=0; j<iMinutes.length; j++) {
    			int min = iMinutes[j];
    			int slot = (min - Constants.FIRST_SLOT_TIME_MIN)/Constants.SLOT_LENGTH_MIN;
    			String pref = model.getCombinedPreference(dayCode, slot, getSlotsPerMtg(), alg);
    			if (clear) {
    				iPreferences[i][j]=pref;
    			} else {
    				PreferenceCombination com = new MinMaxPreferenceCombination();
    				com.addPreferenceProlog(iPreferences[i][j]==null?PreferenceLevel.sNeutral:iPreferences[i][j]);
    				com.addPreferenceProlog(pref);
    				iPreferences[i][j]=com.getPreferenceProlog();
    			}
    		}
    	}
    	if (changed) changeProhibited2Required();
    }
    
    public void combineMatching(TimePatternModel model) {
        combineMatching(model, true, sMixAlgFullCover);
    }
    
    /**
     * Combines preferences of two (different, but matching -- see {@link TimePattern#getMatchingTimePattern(Long, TimePattern)}) time patterns into one.
     * Unlike {@link TimePatternModel#combineWith(TimePatternModel, boolean, int)}, if the same field (days x time) is present in both time patterns,
     * its preference is copied from one time pattern to the other. If the field is not present, its preference is computed the same way as in
     * {@link TimePatternModel#combineWith(TimePatternModel, boolean, int)}.
     * @param model another model to take preferences from (to put into the current one)
     * @param clear if true, preferences in current model are cleared first (it transfers preferences from the given model to the current one)
     * @param alg algorithm that should be used for combining preferences (one of sMixAlgXxx constants)
     */
    public void combineMatching(TimePatternModel model, boolean clear, int alg) {
        for (int t1=0;t1<getNrTimes();t1++) {
            for (int d1=0;d1<getNrDays();d1++) {
                int t2=-1,d2=-1;
                for (int t=0;t<model.getNrTimes();t++) {
                    for (int d=0;d<model.getNrDays();d++) {
                        if (getTime(t1)==model.getTime(t) && getDayCode(d1)==model.getDayCode(d)) {
                            t2=t;d2=d;break;
                        }
                    }
                }
                if (t2>=0 && d2>=0)
                    if (clear) {
                        setPreference(d1,t1,model.getPreference(d2,t2));
                    } else {
                        PreferenceCombination com = new MinMaxPreferenceCombination();
                        com.addPreferenceProlog(getPreference(d1,t1)==null?PreferenceLevel.sNeutral:getPreference(d1,t1));
                        com.addPreferenceProlog(model.getPreference(d2,t2)==null?PreferenceLevel.sNeutral:getPreference(d2,t2));
                        setPreference(d1,t1,com.getPreferenceProlog());
                    }
                else {
                    String pref = model.getCombinedPreference(getDayCode(d1), getStartSlot(t1), getSlotsPerMtg(), alg);
                    if (clear) {
                        setPreference(d1,t1,pref);
                    } else {
                        PreferenceCombination com = new MinMaxPreferenceCombination();
                        com.addPreferenceProlog(getPreference(d1,t1)==null?PreferenceLevel.sNeutral:getPreference(d1,t1));
                        com.addPreferenceProlog(pref);
                        setPreference(d1,t1,com.getPreferenceProlog());
                    }
                }
            }
        }
    }
    
    public void weakenHardPreferences() {
    	for (int d=0;d<getNrDays();d++)
    		for (int t=0;t<getNrTimes();t++) {
    			String p = iPreferences[d][t];
    			if (PreferenceLevel.sRequired.equals(p))
    				iPreferences[d][t]=PreferenceLevel.sStronglyPreferred;
    			else if (PreferenceLevel.sProhibited.equals(p))
    				iPreferences[d][t]=PreferenceLevel.sStronglyDiscouraged;
    		}
    }
    
    public static final int sMixAlgAverage   = 0;
    public static final int sMixAlgSum       = 1;
    public static final int sMixAlgMinMax    = 2;
    public static final int sMixAlgMaxUse    = 3;
    public static final int sMixAlgFullCover = 4;
    public static final String[] sMixAlgs = new String[] {"Average","Sum","Min-Max","MaxUsed", "FullCover"};
    public static PrefMix getMixAlg(int algNum) {
    	switch (algNum) {
    		case sMixAlgAverage : return new AvgPrefMix();
    		case sMixAlgSum : return new SumPrefMix();
    		case sMixAlgMaxUse : return new MaxUsagePrefMix();
    		case sMixAlgMinMax : return new MinMaxPrefMix();
    		case sMixAlgFullCover : return new FullCoverPrefMix();
    		default :
    			return new AvgPrefMix();
    	}
    }
    
    public static interface PrefMix {
    	public void addPref(String prologPref, int count);
    	public String getPref();
    }
    
    public static class AvgPrefMix implements PrefMix {
    	int iPref = 0, iCnt = 0;
    	public void addPref(String prologPref, int count) {
    		int pref = PreferenceLevel.prolog2int(prologPref);
    		iPref += pref * count;
    		iCnt += count;
    	}
    	public String getPref() {
    		if (iCnt==0) return PreferenceLevel.sNeutral;
    		return PreferenceLevel.int2prolog(Math.round(((float)iPref)/iCnt));
    	}
    }

    public static class SumPrefMix implements PrefMix {
    	int iPref = 0, iCnt = 0;
    	public void addPref(String prologPref, int count) {
    		int pref = PreferenceLevel.prolog2int(prologPref);
    		iPref += pref * count;
    		iCnt += count;
    	}
    	public String getPref() {
    		if (iCnt==0) return PreferenceLevel.sNeutral;
    		return PreferenceLevel.int2prolog(iPref/4);
    	}
    }
    
    public static class MinMaxPrefMix implements PrefMix {
    	int iMin = Integer.MAX_VALUE, iMax = Integer.MIN_VALUE, iCnt = 0;
    	public void addPref(String prologPref, int count) {
    		int pref = PreferenceLevel.prolog2int(prologPref);
    		iMin = Math.min(iMin, pref);
    		iMax = Math.max(iMax, pref);
    		iCnt += count;
    	}
    	public String getPref() {
    		if (iCnt==0) return PreferenceLevel.sNeutral;
    		if (Math.abs(iMin)>Math.abs(iMax))
    			return PreferenceLevel.int2prolog(iMin);
    		else
    			return PreferenceLevel.int2prolog(iMax);
    	}
    }
    public static class MaxUsagePrefMix implements PrefMix {
    	Hashtable iUsage = new Hashtable();
    	int iCnt = 0;
    	public void addPref(String prologPref, int count) {
    		Integer use = (Integer)iUsage.get(prologPref);
    		use = new Integer((use==null?0:use.intValue())+count);
    		iUsage.put(prologPref, use);
    		iCnt += count;
    	}
    	public String getPref() {
    		if (iCnt==0) return PreferenceLevel.sNeutral;
    		int bestUse = 0;
    		String bestPref = null;
    		for (Iterator i=iUsage.entrySet().iterator();i.hasNext();) {
    			Map.Entry entry = (Map.Entry)i.next();
    			String pref = (String)entry.getKey();
    			int use = ((Integer)entry.getValue()).intValue();
    			if (bestPref==null || bestUse<use) {
    				bestPref = pref; bestUse=use;
    			} else if (bestUse==use) {
    				if (Math.abs(PreferenceLevel.prolog2int(pref))>Math.abs(PreferenceLevel.prolog2int(bestPref))) {
    					bestPref = pref;
    				} else if (Math.abs(PreferenceLevel.prolog2int(pref))==Math.abs(PreferenceLevel.prolog2int(bestPref)) && PreferenceLevel.prolog2int(pref)>0) {
    					bestPref = pref;
    				}
    			}
    		}
    		return bestPref;
    	}
    }    
    public static class FullCoverPrefMix implements PrefMix {
    	String iPref = null; boolean iFullCover = true;
    	public void addPref(String prologPref, int count) {
    		if (iPref==null)
    			iPref = prologPref;
    		else if (!iPref.equals(prologPref))
    			iFullCover = false;
    	}
    	public String getPref() {
    		if (iPref==null || !iFullCover) return PreferenceLevel.sNeutral;
    		return iPref;
    	}
    }
    
    public String getDefaultPreference() {
    	return PreferenceLevel.sNeutral;
    }
    
    public String getFileName() {
    	StringBuffer fileName = new StringBuffer();
    	if (getTimePattern()!=null && getTimePattern().getUniqueId()!=null)
    		fileName.append(Integer.toHexString(getTimePattern().getUniqueId().intValue()));
    	else
    		fileName.append(getNrMeetings()+"x"+getSlotsPerMtg());
    	fileName.append("_");
    	fileName.append(getPreferencesHex());
    	if (getAssignment()!=null) {
    		fileName.append("_"+Integer.toHexString(getAssignment().getStartSlot()*256+getAssignment().getDayCode()));
    	}
    	return fileName.toString();
    }
    
    public TimeLocation getAssignment() { return iAssignment; }
    
	public Color getBorder(int day, int time) {
    	if (iAssignment!=null && iAssignment.getStartSlot()==getStartSlot(time) && iAssignment.getDayCode()==getDayCode(day))
    		return new Color(0,0,242);
    	return null;
	}
    
	public String[] getPreferenceNames() {
		List<PreferenceLevel> prefs = PreferenceLevel.getPreferenceLevelList();
		if (!iAllowHard) {
	    	boolean hasRequired = false, hasProhibited = false;
	    	for (int d=0;d<getNrDays();d++)
	    		for (int t=0;t<getNrTimes();t++) {
	    			if (PreferenceLevel.sRequired.equals(iPreferences[d][t]))
	    				hasRequired = true;
	    			if (PreferenceLevel.sProhibited.equals(iPreferences[d][t]))
	    				hasProhibited = true;
	    		}
	    	if (!hasRequired)
	    		prefs.remove(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
	    	if (!hasProhibited)
	    		prefs.remove(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited));
		}
		if (hasNotAvailablePreference()) prefs.add(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNotAvailable));
		String[] ret = new String[prefs.size()];
		int idx=0;
		for (PreferenceLevel p: prefs) {
			ret[idx++] = p.getPrefProlog();
		}
		return ret;
	}
	public Color getPreferenceColor(String pref) {
		return PreferenceLevel.prolog2awtColor(pref);
	}
	public String getPreferenceText(String pref) {
		return PreferenceLevel.prolog2string(pref);
	}
	
	public int getNrSelections() {
		return (iTimePattern == null ? iModes.size() : 0);
	}
	public String getSelectionName(int idx) {
		return iModes.get(idx).getName();
	}
	public int[] getSelectionLimits(int idx) {
		if (iTimePattern != null)
			return new int[] {0, getNrTimes() - 1, 0, getNrDays() - 1};
		RoomInterface.RoomSharingDisplayMode mode = iModes.get(idx);
		return new int[] {mode.getFirstSlot(), mode.getLastSlot() - 1, mode.getFirstDay(), mode.getLastDay(), mode.getStep()};
	}
	public void setDefaultSelection(int selection) {
		iDefaultSelection = selection;
	}
	public void setDefaultSelection(String selection) {
		iDefaultSelection = 0;
		if (selection==null) return;
		for (int i=0;i<getNrSelections();i++) {
			if (selection.equals(iModes.get(i).toHex()) || selection.equalsIgnoreCase(getSelectionName(i).replaceAll("&times;","x").replaceAll("×", "x"))) {
				iDefaultSelection = i;
				break;
			}
		}
	}
	public int getDefaultSelection() {
		return (iTimePattern==null?iDefaultSelection:-1);
	}
	public boolean isEditable(int day, int time) {
		return true;
	}
	public String getPreferenceCheck() {
		return null;
	}
	
	public boolean getAllowHard() {
		return iAllowHard;
	}
	public void setAllowHard(boolean allowHard) {
		iAllowHard = allowHard;
	}
	
	public boolean isPreferenceEnabled(String pref) {
		return (iAllowHard || !PreferenceLevel.getPreferenceLevel(pref).isHard());
	}
	
	public int getBreakTime() {
		return iBreakTime;
	}
}
