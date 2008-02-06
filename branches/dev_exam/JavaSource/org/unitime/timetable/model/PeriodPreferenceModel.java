package org.unitime.timetable.model;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTableModel;

public class PeriodPreferenceModel implements RequiredTimeTableModel {
    private TreeSet iDates = new TreeSet();
    private TreeSet iStarts = new TreeSet();
    private Hashtable iPreferences = new Hashtable();
    private TreeSet iPeriods = null;
    private Date iFirstDate = null;
    private boolean iAllowHard = true;
    
    public static SimpleDateFormat sDF = new SimpleDateFormat("EEE MM/dd");

    public PeriodPreferenceModel(Session session) {
        iFirstDate = session.getExamBeginDate();
        iPeriods = ExamPeriod.findAll(session.getUniqueId());
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            iPreferences.put(period, PreferenceLevel.sNeutral);
            iStarts.add(period.getStartSlot());
            iDates.add(period.getDateOffset());
        }
        System.out.println("MODEL[init]:"+iPreferences);
    }
    
    public void load(PreferenceGroup pg) {
        for (Iterator i=pg.getPreferences(ExamPeriodPref.class).iterator();i.hasNext();) {
            ExamPeriodPref pref = (ExamPeriodPref)i.next();
            iPreferences.put(pref.getExamPeriod(), pref.getPrefLevel().getPrefProlog());
        }
        System.out.println("MODEL[load]:"+iPreferences);
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
        for (Iterator i=iPeriods.iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            iPreferences.put(period, location.getExamPreference(period).getPrefProlog());
        }
    }

    public void save(Location location) {
        location.clearExamPreferences();
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
        int startHour = start / 60;
        int startMinute = start % 60;
        return (startHour>12?startHour-12:startHour)+":"+(startMinute<10?"0":"")+startMinute+(startHour>=12?"p":"a");
    }
    
    public String getEndTime(int time) {
        Integer slot = (Integer)iStarts.toArray()[time];
        ExamPeriod period = null;
        for (Iterator i=new TreeSet(iPreferences.keySet()).iterator();i.hasNext();) {
            ExamPeriod p = (ExamPeriod)i.next();
            if (p.getStartSlot().equals(slot)) { period = p; break; }
        }
        int end = (slot+period.getLength())*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        int endHour = end / 60;
        int endMinute = end % 60;
        return (endHour>12?endHour-12:endHour)+":"+(endMinute<10?"0":"")+endMinute+(endHour>=12?"p":"a");
    }
    
    public String getDayHeader(int day) {
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Date date = new Date(iFirstDate.getTime() + dateOffset * 24 * 3600 * 1000);
        return sDF.format(date);
    }
    
    public String getFileName() {
        return "PPx"+getPreferences();
    }
    
    public void setPreference(int day, int time, String pref) {
        System.out.println("set("+day+","+time+","+pref+")");
        Integer dateOffset = (Integer)iDates.toArray()[day];
        Integer slot = (Integer)iStarts.toArray()[time];
        for (Enumeration e=iPreferences.keys();e.hasMoreElements();) {
            ExamPeriod p = (ExamPeriod)e.nextElement();
            if (p.getStartSlot().equals(slot) && p.getDateOffset().equals(dateOffset)) {
                iPreferences.put(p, pref); return;
            }
        }
        System.out.println("MODEL[set]:"+iPreferences);
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
        return null;
    }
    
    public String[] getPreferenceNames() {
        Vector prefs = PreferenceLevel.getPreferenceLevelList(false);
        boolean hasNotAvailable = (iPreferences.size()<iDates.size()*iStarts.size());
        String[] ret = new String[prefs.size()+(hasNotAvailable?1:0)];
        int idx=0;
        for (Enumeration e=prefs.elements();e.hasMoreElements();idx++) {
            ret[idx]=((PreferenceLevel)e.nextElement()).getPrefProlog();
        }
        if (hasNotAvailable)
            ret[prefs.size()] = "@";
        return ret;
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
