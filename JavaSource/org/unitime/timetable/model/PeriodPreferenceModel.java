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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTableModel;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class PeriodPreferenceModel implements RequiredTimeTableModel {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
    private TreeSet iDates = new TreeSet();
    private TreeSet iStarts = new TreeSet();
    private Hashtable iPreferences = new Hashtable();
    private TreeSet iPeriods = null;
    private Date iFirstDate = null;
    private boolean iAllowHard = true;
    private boolean iAllowRequired = true;
    
    private ExamPeriod iPeriod = null;
    private Long iExamType = null;
    
    private SimpleDateFormat[] iDF = null;
    
    public PeriodPreferenceModel(Session session, Long examType) {
        this(session, (ExamPeriod) null, examType);
    }
    
    public PeriodPreferenceModel(Session session, ExamAssignment assignment, Long examType) {
    	this(session, assignment == null ? null : assignment.getPeriod(), examType);
    }

    public PeriodPreferenceModel(Session session, ExamPeriod assignedPeriod, Long examType) {
    	iDF = new SimpleDateFormat[] {
    			new SimpleDateFormat(CONSTANTS.examPeriodPreferenceDateFormat()[0], Localization.getJavaLocale()),
    			new SimpleDateFormat(CONSTANTS.examPeriodPreferenceDateFormat()[1], Localization.getJavaLocale())
    		};
        iPeriod = assignedPeriod;
        iFirstDate = session.getExamBeginDate();
        iPeriods = ExamPeriod.findAll(session.getUniqueId(), examType);
        iExamType = examType;
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            iPreferences.put(period, PreferenceLevel.sNeutral);
            iStarts.add(period.getStartSlot());
            iDates.add(period.getDateOffset());
        }
    }
    
    public void load(PreferenceGroup pg) {
        for (Iterator i=pg.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
            ExamPeriodPref pref = (ExamPeriodPref)i.next();
            iPreferences.put(pref.getExamPeriod(), pref.getPrefLevel().getPrefProlog());
        }
    }
    
    public void save(Set preferences, PreferenceGroup pg) {
        for (Iterator i=iPreferences.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            ExamPeriod period = (ExamPeriod)entry.getKey();
            String pref = (String)entry.getValue();
            if (!PreferenceLevel.sNeutral.equals(pref)) {
                ExamPeriodPref p = new ExamPeriodPref();
                p.setOwner(pg);
                p.setExamPeriod(period);
                p.setPrefLevel(PreferenceLevel.getPreferenceLevel(pref));
                preferences.add(p);
            }
        }
    }
    
    public void load(Location location) {
        for (Iterator i=location.getExamPreferences().iterator();i.hasNext();) {
            ExamLocationPref pref = (ExamLocationPref)i.next();
            if (!iExamType.equals(pref.getExamPeriod().getExamType().getUniqueId())) continue;
            iPreferences.put(pref.getExamPeriod(), pref.getPrefLevel().getPrefProlog());
        }
    }

    public void save(Location location) {
        location.clearExamPreferences(iExamType);
        for (Iterator i=iPreferences.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry)i.next();
            ExamPeriod period = (ExamPeriod)entry.getKey();
            String pref = (String)entry.getValue();
            if (!PreferenceLevel.sNeutral.equals(pref)) {
                location.addExamPreference(period, PreferenceLevel.getPreferenceLevel(pref));
            }
        }
    }

    public void setAllowHard(boolean allowHard) { iAllowHard = allowHard; }
    
    public void setAllowRequired(boolean allowReq) { iAllowRequired = allowReq; }
    
    public String getName() {
        return null;
    }
    
    public int getNrDays() {
        return iDates.size();
    }
    public int getNrTimes() {
        return iStarts.size();
    }
    public String getStartTime(int time) {
        Integer slot = (Integer)iStarts.toArray()[time];
        int start = slot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return Constants.toTime(start);
    }
    
    public String getEndTime(int time) {
        Integer slot = (Integer)iStarts.toArray()[time];
        ExamPeriod period = null;
        for (Iterator i=new TreeSet(iPreferences.keySet()).iterator();i.hasNext();) {
            ExamPeriod p = (ExamPeriod)i.next();
            if (p.getStartSlot().equals(slot)) { period = p; break; }
        }
        int end = (slot+period.getLength())*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return Constants.toTime(end);
    }
    
    public String getDayHeader(int day) {
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Calendar cal = Calendar.getInstance();
        cal.setTime(iFirstDate);
        cal.add(Calendar.DAY_OF_YEAR, dateOffset);
        Date date = new Date(cal.getTime().getTime());
        return iDF[0].format(date)+"<br>"+iDF[1].format(date);
    }
    
    public String getFileName() {
        return "PPx"+getPreferences()+(iPeriod==null?"":"_"+iPeriod.getUniqueId());
    }
    
    public void setPreference(int day, int time, String pref) {
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Integer slot = (Integer)iStarts.toArray()[time];
        for (Enumeration e=iPreferences.keys();e.hasMoreElements();) {
            ExamPeriod p = (ExamPeriod)e.nextElement();
            if (p.getStartSlot().equals(slot) && p.getDateOffset().equals(dateOffset)) {
                iPreferences.put(p, pref); return;
            }
        }
    }
    
    public String getPreference(int day, int time) {
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Integer slot = (Integer)iStarts.toArray()[time];
        for (Enumeration e=iPreferences.keys();e.hasMoreElements();) {
            ExamPeriod p = (ExamPeriod)e.nextElement();
            if (p.getStartSlot().equals(slot) && p.getDateOffset().equals(dateOffset)) {
                return (String)iPreferences.get(p);
            }
        }
        return "@";
    }

    public String getFieldText(int day, int time) {
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Integer slot = (Integer)iStarts.toArray()[time];
        for (Enumeration e=iPreferences.keys();e.hasMoreElements();) {
            ExamPeriod p = (ExamPeriod)e.nextElement();
            if (p.getStartSlot().equals(slot) && p.getDateOffset().equals(dateOffset))
                return p.getAbbreviation();
        }
        return null;
    }
    
    public boolean isEditable(int day, int time) {
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Integer slot = (Integer)iStarts.toArray()[time];
        for (Enumeration e=iPreferences.keys();e.hasMoreElements();) {
            ExamPeriod p = (ExamPeriod)e.nextElement();
            if (p.getStartSlot().equals(slot) && p.getDateOffset().equals(dateOffset))
                return true;
        }
        return false;
    }
    
    public String getPreferences() {
        String prefs = "";
        for (Iterator i=new TreeSet(iPreferences.keySet()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            String pref = (String)iPreferences.get(period);
            prefs += PreferenceLevel.prolog2char(pref);
        }
        return prefs;
    }
    public void setPreferences(String pref) {
        int idx = 0;
        for (Iterator i=new TreeSet(iPreferences.keySet()).iterator();i.hasNext();idx++) {
            ExamPeriod period = (ExamPeriod)i.next();
            char p = pref.charAt(idx);
            iPreferences.put(period, PreferenceLevel.char2prolog(p));
        }
    }
    
    public boolean isExactTime() { return false; }
    public int getExactDays() { return 0; }
    public int getExactStartSlot() { return 0; }
    public void setExactDays(int days) {}
    public void setExactStartSlot(int slot) {}
    
    public String getDefaultPreference() {
        return PreferenceLevel.sNeutral;
    }
    
    public Color getBorder(int day, int time) {
        if (iPeriod!=null) {
            Integer slot = (Integer)iStarts.toArray()[time];
            Integer dateOffset = (Integer)iDates.toArray()[day];
            if (iPeriod.getStartSlot().equals(slot) && iPeriod.getDateOffset().equals(dateOffset))
                return new Color(0,0,242);
        }
        return null;
    }
    
    public boolean hasPreference(String pref) {
        for (Iterator i=iPreferences.values().iterator();i.hasNext();)
            if (pref.equals(i.next())) return true;
        return false;
    }
    
    public boolean hasNotAvailable() {
        return iPreferences.size()<iDates.size()*iStarts.size();
    }
    
    public String[] getPreferenceNames() {
        ArrayList<String> ret = new ArrayList<String>();
        for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
            if (!iAllowRequired && PreferenceLevel.sRequired.equals(pref.getPrefProlog())) continue;
            if (!iAllowHard && pref.isHard() && !hasPreference(pref.getPrefProlog())) continue;
            ret.add(pref.getPrefProlog());
        }
        if (hasNotAvailable())
            ret.add("@");
        return ret.toArray(new String[ret.size()]);
    }
    
    public Color getPreferenceColor(String pref) {
        if ("@".equals(pref)) return new Color(150,150,150);
        return PreferenceLevel.prolog2awtColor(pref);
    }

    public String getPreferenceText(String pref) {
        if ("@".equals(pref)) return "Period Not Available";
        return PreferenceLevel.prolog2string(pref);
    }
    
    public int getNrSelections() { return 0; }
    public String getSelectionName(int idx) { return null; }
    public int[] getSelectionLimits(int idx) { return new int[] {0,getNrTimes()-1,0,getNrDays()-1}; }
    public int getDefaultSelection() { return -1; }
    public void setDefaultSelection(int selection) {}
    public void setDefaultSelection(String selectionName) {}
    
    public String getPreferenceCheck() {
        return null;
    }
    
    public boolean isPreferenceEnabled(String pref) {
        if ("@".equals(pref)) return false;
        return (iAllowHard || !PreferenceLevel.getPreferenceLevel(pref).isHard());
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int ld = -1;
        for (int d=0;d<getNrDays();d++) {
            String pref = null; int a = 0, b = 0;
            for (int t=0;t<getNrTimes();t++) {
                String p = getPreference(d,t);
                if (pref==null || !pref.equals(p)) {
                    if (pref!=null && !"@".equals(pref) && !PreferenceLevel.sNeutral.equals(pref)) {
                        if (sb.length()>0) sb.append(", ");
                        if (ld!=d) { sb.append(getDayHeader(d)+" "); ld = d; }
                        sb.append(PreferenceLevel.prolog2abbv(pref)+" ");
                        sb.append(getStartTime(a)+" - "+getEndTime(b));
                        ld = d;
                    }
                    pref = p; a = b = t;
                } else {
                    b = t;
                }
            }
            if (pref!=null && !"@".equals(pref) && !PreferenceLevel.sNeutral.equals(pref)) {
                if (sb.length()>0) sb.append(", ");
                if (ld!=d) { sb.append(getDayHeader(d)+" "); ld = d; }
                sb.append(PreferenceLevel.prolog2abbv(pref)+" ");
                sb.append(getStartTime(a)+" - "+getEndTime(b));
            }
        }
        return sb.toString();
    }
}
