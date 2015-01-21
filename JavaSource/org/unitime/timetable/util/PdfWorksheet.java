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
package org.unitime.timetable.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.solver.TimetableDatabaseLoader;
import org.unitime.timetable.util.Constants;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;


/**
 * @author Tomas Muller
 */
public class PdfWorksheet {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	private boolean iUseCommitedAssignments = true;
    private static int sNrChars = 133;
    private static int sNrLines = 50;
    private OutputStream iOut = null;
    private Document iDoc = null;
    private TreeSet<SubjectArea> iSubjectAreas;
    private String iCourseNumber = null;
    private int iPageNo = 0;
    private int iLineNo = 0;
    private StringBuffer iBuffer = new StringBuffer();
    private CourseOffering iCourseOffering = null;
    private SubjectArea iCurrentSubjectArea = null;
    
    private PdfWorksheet(OutputStream out, Collection<SubjectArea> subjectAreas, String courseNumber) throws IOException, DocumentException  {
        iUseCommitedAssignments = ApplicationProperty.WorksheetPdfUseCommittedAssignments.isTrue();
        iSubjectAreas = new TreeSet<SubjectArea>(new Comparator<SubjectArea>() {
    		@Override
    		public int compare(SubjectArea s1, SubjectArea s2) {
    			return s1.getSubjectAreaAbbreviation().compareTo(s2.getSubjectAreaAbbreviation());
    		}
    	});
        iSubjectAreas.addAll(subjectAreas);
        iCourseNumber = courseNumber;
        if (iCourseNumber!=null && (iCourseNumber.trim().length()==0 || "*".equals(iCourseNumber.trim().length())))
            iCourseNumber = null;
        iDoc = new Document(PageSize.LETTER.rotate());

        iOut = out;
        PdfWriter.getInstance(iDoc, iOut);

        String session = null;
        String subjects = "";
        for (SubjectArea sa: iSubjectAreas) {
        	if (subjects.isEmpty()) subjects += ", ";
        	subjects += sa.getSubjectAreaAbbreviation();
        	if (session == null) session += sa.getSession().getLabel();
        }
        iDoc.addTitle(subjects + (iCourseNumber==null?"":" "+iCourseNumber) + " Worksheet");
        iDoc.addAuthor(ApplicationProperty.WorksheetPdfAuthor.value().replace("%", Constants.getVersion()));
        iDoc.addSubject(subjects + (session == null ? "" : " -- " + session));
        iDoc.addCreator("UniTime "+Constants.getVersion()+", www.unitime.org");
        if (!iSubjectAreas.isEmpty())
        	iCurrentSubjectArea = iSubjectAreas.first();

        iDoc.open();
        
        printHeader();
    }
    
    public static boolean print(OutputStream out, Collection<SubjectArea> subjectAreas) throws IOException, DocumentException {
        TreeSet courses = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseOffering co1 = (CourseOffering)o1;
                CourseOffering co2 = (CourseOffering)o2;
                int cmp = co1.getCourseName().compareTo(co2.getCourseName());
                if (cmp != 0) return cmp;
                return co1.getUniqueId().compareTo(co2.getUniqueId());
            }
        });
        String subjectIds = "";
        for (SubjectArea sa: subjectAreas)
        	subjectIds += (subjectIds.isEmpty() ? "" : ",") + sa.getUniqueId();
        courses.addAll(SessionDAO.getInstance().getSession().createQuery(
        		"select co from CourseOffering co where  co.subjectArea.uniqueId in (" + subjectIds + ")").list());
        if (courses.isEmpty()) return false;
        PdfWorksheet w = new PdfWorksheet(out, subjectAreas, null);
        for (Iterator i=courses.iterator();i.hasNext();) {
            w.print((CourseOffering)i.next());
        }
        w.lastPage();
        w.close();
        return true;
    }
    
    public static boolean print(OutputStream out, Collection<SubjectArea> subjectAreas, String courseNumber) throws IOException, DocumentException {
        TreeSet courses = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseOffering co1 = (CourseOffering)o1;
                CourseOffering co2 = (CourseOffering)o2;
                int cmp = co1.getCourseName().compareTo(co2.getCourseName());
                if (cmp!=0) return cmp;
                return co1.getUniqueId().compareTo(co2.getUniqueId());
            }
        });
        String subjectIds = "";
        for (SubjectArea sa: subjectAreas)
        	subjectIds += (subjectIds.isEmpty() ? "" : ",") + sa.getUniqueId();
        String query = "select co from CourseOffering co where  co.subjectArea.uniqueId in (" + subjectIds + ")";
        if (courseNumber!=null && !courseNumber.trim().isEmpty()) {
            query += " and co.courseNbr ";
            if (courseNumber.indexOf('*')>=0)
                query += " like '"+courseNumber.trim().replace('*', '%').toUpperCase()+"'";
            else 
                query += " = '"+courseNumber.trim().toUpperCase()+"'";
        }
        courses.addAll(new SessionDAO().getSession().createQuery(query).list());
        if (courses.isEmpty()) return false;
        PdfWorksheet w = new PdfWorksheet(out, subjectAreas, courseNumber);
        for (Iterator i=courses.iterator();i.hasNext();) {
            w.print((CourseOffering)i.next());
        }
        w.lastPage();
        w.close();
        return true;
    }
    
    private String[] time(Class_ clazz) {
        String dpat = "";
        DatePattern dp = clazz.effectiveDatePattern();
        if (dp!=null && !dp.isDefault()) {
            if (dp.getType().intValue()==DatePattern.sTypeAlternate)
                dpat = " "+dp.getName();
            else {
                SimpleDateFormat dpf = new SimpleDateFormat("MM/dd");
                dpat = ", "+dpf.format(dp.getStartDate())+" - "+dpf.format(dp.getEndDate());
            }
        }
        Assignment assgn = (iUseCommitedAssignments?clazz.getCommittedAssignment():null);
        if (assgn==null) {
            Set timePrefs = clazz.getEffectiveTimePreferences();
            if (timePrefs.isEmpty()) {
                if (clazz.getSchedulingSubpart().getMinutesPerWk().intValue()>0)
                    return new String[]{"Arr "+((clazz.getSchedulingSubpart().getMinutesPerWk().intValue()+59)/60)+" Hrs"+dpat};
                else
                    return new String[]{"Arr Hrs"+dpat};
            }
            boolean onlyOneReq = true;
            TimeLocation req = null;
            for (Iterator x=timePrefs.iterator();onlyOneReq && x.hasNext();) {
                TimePref tp = (TimePref)x.next();
                TimePatternModel model = tp.getTimePatternModel();
                if (model.isExactTime()) {
                    if (req!=null) onlyOneReq=false;
                    else {
                        int length = ExactTimeMins.getNrSlotsPerMtg(model.getExactDays(),clazz.getSchedulingSubpart().getMinutesPerWk().intValue());
                        int breakTime = ExactTimeMins.getBreakTime(model.getExactDays(),clazz.getSchedulingSubpart().getMinutesPerWk().intValue()); 
                        req = new TimeLocation(model.getExactDays(), model.getExactStartSlot(), length,PreferenceLevel.sIntLevelNeutral,0,dp.getUniqueId(),dp.getName(),dp.getPatternBitSet(),breakTime);
                    }
                } else {
                    for (int d=0;d<model.getNrDays();d++)
                        for (int t=0;onlyOneReq && t<model.getNrTimes();t++) {
                            if (PreferenceLevel.sRequired.equals(model.getPreference(d,t))) {
                                if (req!=null) onlyOneReq=false;
                                else {
                                    req = new TimeLocation(
                                            model.getDayCode(d),
                                            model.getStartSlot(t),
                                            model.getSlotsPerMtg(),
                                            PreferenceLevel.prolog2int(model.getPreference(d, t)),
                                            0,
                                            dp.getUniqueId(),
                                            dp.getName(),
                                            dp.getPatternBitSet(),
                                            model.getBreakTime());                                                
                                }
                            }
                        }
                }
            }
            if (onlyOneReq && req!=null)
                return new String[] {req.getDayHeader()+" "+req.getStartTimeHeader(CONSTANTS.useAmPm())+" - "+req.getEndTimeHeader(CONSTANTS.useAmPm())+dpat};
            Vector t = new Vector();
            for (Iterator x=timePrefs.iterator();x.hasNext();) {
                TimePref tp = (TimePref)x.next();
                String tx = tp.getTimePatternModel().toString();
                for (StringTokenizer s=new StringTokenizer(tx,",");s.hasMoreTokens();)
                    t.add(s.nextToken().trim());
            }
            String[] time = new String[t.size()];
            for (int x=0;x<time.length;x++)
                time[x]=t.elementAt(x)+dpat;
            return time;
        }
        TimeLocation t = assgn.getTimeLocation();
        return new String[] {t.getDayHeader()+" "+t.getStartTimeHeader(CONSTANTS.useAmPm())+" - "+t.getEndTimeHeader(CONSTANTS.useAmPm())+dpat};
    }
    
    private String[] room(Class_ clazz) {
        Assignment assgn = (iUseCommitedAssignments?clazz.getCommittedAssignment():null);
        if (assgn==null || assgn.getRoomLocations().isEmpty()) {
            List<RoomLocation> roomLocations = TimetableDatabaseLoader.computeRoomLocations(clazz);
            if (roomLocations.size()==clazz.getNbrRooms().intValue()) {
                String[] rooms = new String[roomLocations.size()];
                for (int x=0;x<roomLocations.size();x++) {
                    RoomLocation r = (RoomLocation)roomLocations.get(x); 
                    rooms[x] = r.getName();
                }
                return rooms;
            }
            Vector roomPrefs = new Vector();
            boolean allRoomReq = true;
            for (Iterator i=clazz.effectivePreferences(BuildingPref.class).iterator();i.hasNext();) {
                Preference pref = (Preference)i.next();
                roomPrefs.add(PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText());
                allRoomReq=false;
            }
            for (Iterator i=clazz.effectivePreferences(RoomPref.class).iterator();i.hasNext();) {
                Preference pref = (Preference)i.next();
                roomPrefs.add(PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText());
                if (!PreferenceLevel.sRequired.equals(pref.getPrefLevel().getPrefProlog())) allRoomReq=false;
            }
            for (Iterator i=clazz.effectivePreferences(RoomFeaturePref.class).iterator();i.hasNext();) {
                Preference pref = (Preference)i.next();
                roomPrefs.add(PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText());
                allRoomReq=false;
            }
            for (Iterator i=clazz.effectivePreferences(RoomGroupPref.class).iterator();i.hasNext();) {
                Preference pref = (Preference)i.next();
                roomPrefs.add(PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText());
                allRoomReq=false;
            }
            if (allRoomReq) {
                roomPrefs.clear();
                for (Iterator i=clazz.effectivePreferences(RoomPref.class).iterator();i.hasNext();) {
                    Preference pref = (Preference)i.next();
                    roomPrefs.add(pref.preferenceText());
                }
            }
            String[] rooms = new String[roomPrefs.size()];
            for (int x=0;x<roomPrefs.size();x++) {
                rooms[x] = roomPrefs.elementAt(x).toString();
            }
            return rooms;
        }
        String[] rooms = new String[assgn.getRoomLocations().size()];
        for (int x=0;x<assgn.getRoomLocations().size();x++) {
            RoomLocation r = (RoomLocation)assgn.getRoomLocations().elementAt(x); 
            rooms[x] = r.getName();
        }
        return rooms;
    }
    
    private String[] instructor(Class_ clazz) {
        List<DepartmentalInstructor> leads = clazz.getLeadInstructors();
        String[] instr = new String[leads.size()];
        for (int x=0;x<clazz.getLeadInstructors().size();x++) {
            DepartmentalInstructor in = (DepartmentalInstructor)leads.get(x); 
            instr[x] = in.nameShort();
        }
        return instr;
    }
    
    protected void print(CourseOffering co) throws DocumentException {
    	if (!iCurrentSubjectArea.equals(co.getSubjectArea())) {
    		lastPage();
    		iCurrentSubjectArea = co.getSubjectArea();
    		iDoc.newPage();
    		printHeader();
    	} else {
    		if (iLineNo+5>=sNrLines) newPage();
    	}
        iCourseOffering = co;
        int courseLimit = -1;
        InstructionalOffering offering = co.getInstructionalOffering();
        if (co.getReservation() != null)
        	courseLimit = co.getReservation();
        if (courseLimit<0) {
            if (offering.getCourseOfferings().size()==1 && offering.getLimit()!=null)
                courseLimit = offering.getLimit().intValue();
        }
        boolean unlimited = false;
        String courseOrg = "";
        for (Iterator i=offering.getInstrOfferingConfigs().iterator();i.hasNext();) {
            InstrOfferingConfig config = (InstrOfferingConfig)i.next();
            if (config.isUnlimitedEnrollment().booleanValue()) unlimited=true;
            Hashtable creditPerIType = new Hashtable();
            for (Iterator j=config.getSchedulingSubparts().iterator();j.hasNext();) {
                SchedulingSubpart subpart = (SchedulingSubpart)j.next();
                if (subpart.getMinutesPerWk().intValue()<=0) continue;
                Integer credit = (Integer)creditPerIType.get(subpart.getItype());
                creditPerIType.put(subpart.getItype(), new Integer((credit==null?0:credit.intValue())+subpart.getMinutesPerWk().intValue()));
            }
            TreeSet itypes = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    ItypeDesc i1 = (ItypeDesc)o1;
                    ItypeDesc i2 = (ItypeDesc)o2;
                    return i1.getItype().compareTo(i2.getItype());
                }
            });
            itypes.addAll(creditPerIType.keySet());
            for (Iterator j=itypes.iterator();j.hasNext();) {
                ItypeDesc itype = (ItypeDesc)j.next();
                int minPerWeek = ((Integer)creditPerIType.get(itype)).intValue();
                if (courseOrg.length()>0) courseOrg+=", ";
                courseOrg+=itype.getAbbv().trim()+" "+((minPerWeek+49)/50);
            }
            break;
        }
        int enrl = -1;
        String s1 = co.getSubjectArea().getSession().getAcademicTerm().substring(0,1) + co.getSubjectArea().getSession().getAcademicYear().substring(2);
        String s2 = co.getSubjectArea().getSession().getAcademicTerm().substring(0,1) + 
            new DecimalFormat("00").format(Integer.parseInt(co.getSubjectArea().getSession().getAcademicYear().substring(2))-1);
        if (co.getProjectedDemand()!=null) enrl = co.getProjectedDemand().intValue();
        int lastLikeEnrl = co.getCourseOfferingDemands().size();
        String title = co.getTitle();
        if (title==null) title="*** Title not set";
        println("                                                                                              Proj  "+s2+"                     ");
        println("Course     Title/Notes                           Credit Course Organization             Limit Enrl  Enrl  Consent    Cross List");
        println("---------- ------------------------------------- ------ ------------------------------- ----- ----- ----- ---------- ----------");
        println(rpad(co.getCourseName(),10)+" "+
                rpad(title,37)+(title.length()>37?"-":" ")+" "+
                rpad(co.getCredit()==null?"":co.getCredit().creditAbbv(),5)+" "+
                rpad(courseOrg,31)+" "+
                lpad(courseLimit<=0?unlimited?"  inf":"":String.valueOf(courseLimit),5)+" "+
                lpad(enrl<=0?"":String.valueOf(enrl),5)+" "+
                lpad(lastLikeEnrl<=0?"":String.valueOf(lastLikeEnrl),5)+" "+
                rpad(co.getConsentType()==null?"":co.getConsentType().getAbbv(),10)+" "+
                rpad(offering.getCourseOfferings().size()>1?offering.getCourseName():"",10)
                );
        while (title.length()>37) {
            title = title.substring(37);
            println("           "+rpad(title,37)+(title.length()>37?"-":" "));
        }
        if (co.getScheduleBookNote()!=null && co.getScheduleBookNote().trim().length()>0) {
            String note = co.getScheduleBookNote();
            note = note.replaceAll("\\. ", "\\.\n");
            for (StringTokenizer s=new StringTokenizer(note,"\n\r");s.hasMoreTokens();) {
                String line = s.nextToken().trim();
                while (line.length()>sNrChars-7) {
                    println("   "+line.substring(0,sNrChars-7)+"-");
                    line = line.substring(sNrChars-7);
                }
                println("   "+line);
            }
        }
        if (iLineNo+5>=sNrLines) newPage();
        else println("");
        println("        "+s1+"   "+s2+"  Proj | Type");
        println("Curr  Reqst  Enrl  Enrl | Instr Number Time                                     Limit Bldg-Room          Instructor            Mgr");
        println("----  -----  ----  ---- | ----- ------ ---------------------------------------- ----- ------------------ --------------------- ------");

        Vector rTable = new Vector();
        //TODO: Print request data based on curricula
        /*
        int a=0,b=0,c=0;
        for (Iterator i=co.getAcadAreaReservations().iterator();i.hasNext();) {
            AcadAreaReservation ar = (AcadAreaReservation)i.next();
            rTable.add(
                    lpad(ar.getAcademicArea().getAcademicAreaAbbreviation(),4)+"  "+
                    lpad(ar.getRequested()==null?"":ar.getRequested().toString(),5)+" "+
                    lpad(ar.getPriorEnrollment()==null?"":ar.getPriorEnrollment().toString(),5)+" "+
                    lpad(ar.getProjectedEnrollment()==null?"":ar.getProjectedEnrollment().toString(),5));
            if (ar.getRequested()!=null) a+=ar.getRequested().intValue();
            if (ar.getPriorEnrollment()!=null) b+=ar.getPriorEnrollment().intValue();
            if (ar.getProjectedEnrollment()!=null) c+=ar.getProjectedEnrollment().intValue();
        }
        if (rTable.isEmpty()) {
            rTable.add(" *** No Request Data   ");
        } else {
            rTable.add(
                    " Tot  "+
                    lpad(String.valueOf(a),5)+" "+
                    lpad(String.valueOf(b),5)+" "+
                    lpad(String.valueOf(c),5));
            rTable.add("                       ");
            rTable.add(" *Please check requests");
        }
        */
        Vector cTable = new Vector();
        if (offering.isNotOffered().booleanValue())
            cTable.add(" ** Course not offered");
        Vector gTable = new Vector();
        TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(null));
        configs.addAll(offering.getInstrOfferingConfigs());
        for (Iterator i=configs.iterator();i.hasNext();) {
            InstrOfferingConfig config = (InstrOfferingConfig)i.next();
            if (offering.getInstrOfferingConfigs().size()>1)
                cTable.add("** Configuration "+config.getName());
            TreeSet subparts = new TreeSet(new SchedulingSubpartComparator());
            subparts.addAll(config.getSchedulingSubparts());
            for (Iterator j=subparts.iterator();j.hasNext();) {
                SchedulingSubpart subpart = (SchedulingSubpart)j.next();
                TreeSet classes = new TreeSet(new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
                classes.addAll(subpart.getClasses());
                String subpartLabel = subpart.getItype().getAbbv();
                boolean same = false;
                for (Iterator k=classes.iterator();k.hasNext();) {
                    Class_ clazz = (Class_)k.next();
                    String[] time = time(clazz);
                    String[] rooms = room(clazz);
                    String[] instr = instructor(clazz);
                    for (int x=0;x<Math.max(Math.max(1,time.length),Math.max(instr.length,rooms.length));x++) {
                        cTable.add(
                                rpad(same?"":x==0?subpartLabel:"",5)+" "+
                                lpad(x==0?clazz.getSectionNumberString():"",6)+" "+
                                rpad(time!=null && x<time.length?time[x]:"",40)+" "+
                                lpad(x==0 && clazz.getClassLimit()>0 && clazz.getNbrRooms().intValue()>0?(clazz.getNbrRooms().intValue()>1?clazz.getNbrRooms()+"x":"")+String.valueOf(clazz.getClassLimit()):"",5)+" "+
                                rpad(rooms!=null && x<rooms.length?rooms[x]:"",18)+" "+
                                rpad(instr!=null && x<instr.length?instr[x]:"",21)+" "+
                                rpad(x==0?clazz.getManagingDept().getShortLabel():"",6)
                                );
                    }
                    same=true;
                    if (clazz.getParentClass()!=null && clazz.getChildClasses().isEmpty()) {
                        String gr = clazz.getSchedulingSubpart().getItype().getAbbv().trim()+
                                    lpad(clazz.getSectionNumberString(),4);
                        Class_ parent = clazz.getParentClass();
                        while (parent!=null) {
                            gr = parent.getSchedulingSubpart().getItype().getAbbv().trim()+
                                lpad(parent.getSectionNumberString(),4)+
                                ", "+gr;
                            parent = parent.getParentClass();
                        }
                        gTable.add(gr);
                    }
                }
            }
        }
        for (int i=0;i<1+Math.max(rTable.size(), cTable.size());i++) {
            String res = null;
            String cl = null;
            if (i<rTable.size()) res = (String)rTable.elementAt(i);
            if (i<cTable.size()) cl = (String)cTable.elementAt(i);
            println(rpad(res,23)+" | "+(cl==null?"":cl));
        }
        if (!gTable.isEmpty()) {
            println(rep('-',sNrChars));
            println("     Course groups:");
            int half = (gTable.size()+1)/2;
            for (int i=0;i<half;i++) {
                String gr1 = (String)gTable.elementAt(i);
                String gr2 = (half+i<gTable.size()?(String)gTable.elementAt(half+i):"");
                println("     "+rpad(gr1,60)+" | "+rpad(gr2,60));
            }
        }
        println(rep('=',sNrChars));
        iCourseOffering = null;
    }
    
    private void out(String text) throws DocumentException {
        if (iBuffer.length()>0) iBuffer.append("\n");
        iBuffer.append(text);
    }
    
    private static String rep(char ch, int cnt) {
        String ret = "";
        for (int i=0;i<cnt;i++) ret+=ch;
        return ret;
    }
    
    private void outln(char ch) throws DocumentException {
        out(rep(ch,sNrChars));
    }
    
    private String lpad(String s, char ch, int len) {
        while (s.length()<len) s = ch + s;
        return s;
    }
    
    private String lpad(String s, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        return lpad(s,' ',len);
    }

    private String rpad(String s, char ch, int len) {
        while (s.length()<len) s = s + ch;
        return s;
    }
    
    private String rpad(String s, int len) {
        if (s==null) s="";
        if (s.length()>len) return s.substring(0,len);
        return rpad(s,' ',len);
    }

    private String mpad(String s1, String s2, char ch, int len) {
        String m = "";
        while ((s1+m+s2).length()<len) m += ch;
        return s1+m+s2;
    }
    
    private String render(String line, String s, int idx) {
        String a = (line.length()<=idx?rpad(line,' ',idx):line.substring(0,idx));
        String b = (line.length()<=idx+s.length()?"":line.substring(idx+s.length()));
        return a + s + b;
    }

    private String renderMiddle(String line, String s) {
        return render(line, s, (sNrChars - s.length())/2);
    }

    private String renderEnd(String line, String s) {
        return render(line, s, sNrChars-s.length());
    }
    
    protected void printHeader() throws DocumentException {
        out(renderMiddle(
                ApplicationProperty.WorksheetPdfAuthor.value().replace("%", Constants.getVersion()),
                ApplicationProperty.WorksheetPdfTitle.value()
                ));
        out(mpad(
                new SimpleDateFormat("EEE MMM dd, yyyy").format(new Date()),
                iCurrentSubjectArea.getSession().getAcademicInitiative()+" "+
                iCurrentSubjectArea.getSession().getAcademicTerm()+" "+
                iCurrentSubjectArea.getSession().getAcademicYear(),' ',sNrChars));
        outln('=');
        iLineNo=0;
        if (iCourseOffering!=null)
            println("("+iCourseOffering.getCourseName()+" Continued)");
    }
    
    protected void printFooter() throws DocumentException {
        out("");
        out(renderEnd(renderMiddle("","Page "+(iPageNo+1)),"<"+iCurrentSubjectArea.getSubjectAreaAbbreviation()+(iCourseNumber!=null?" "+iCourseNumber:"")+">  "));
    	//FIXME: For some reason when a line starts with space, the line is shifted by one space in the resulting PDF (when using iText 5.0.2)
        Paragraph p = new Paragraph(iBuffer.toString().replace("\n ", "\n  "), PdfFont.getFixedFont());
        p.setLeading(9.5f); //was 13.5f
        iDoc.add(p);
        iBuffer = new StringBuffer();
        iPageNo++;
    }
    protected void lastPage() throws DocumentException {
        while (iLineNo<sNrLines) {
            out(""); iLineNo++;
        }
        printFooter();
    }
    
    protected void newPage() throws DocumentException {
        while (iLineNo<sNrLines) {
            out(""); iLineNo++;
        }
        printFooter();
        iDoc.newPage();
        printHeader();
    }
    
    protected void println(String text) throws DocumentException {
        out(text);
        iLineNo++;
        if (iLineNo>=sNrLines) newPage();
    }
    
    private void close() throws IOException {
        iDoc.close();
    }

	public static void main(String[] args) {
        try {
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
            
            Long sessionId = Long.valueOf(ApplicationProperties.getProperty("tmtbl.pdf.worksheet.session", "165924"));
            Session session = new SessionDAO().get(sessionId);
            if (session==null) {
                System.err.println("Academic session "+sessionId+" not found, use property tmtbl.pdf.worksheet.session to set academic session.");
                System.exit(0);
            } else {
                System.out.println("Session: "+session);
            }
            TreeSet subjectAreas = null;
            if (args.length>0) {
                subjectAreas = new TreeSet();
                for (int i=0;i<args.length;i++) {
                    SubjectArea sa = SubjectArea.findByAbbv(sessionId, args[i]);
                    if (sa==null)
                        System.err.println("Subject area "+args[i]+" not found.");
                    else
                        subjectAreas.add(sa);
                }
            } else {
                subjectAreas = new TreeSet(SubjectArea.getSubjectAreaList(sessionId));
            }
            
            for (Iterator i=subjectAreas.iterator();i.hasNext();) {
                SubjectArea sa = (SubjectArea)i.next();
                System.out.println("Printing subject area "+sa.getSubjectAreaAbbreviation()+" ...");
                FileOutputStream out = new FileOutputStream(sa.getSubjectAreaAbbreviation()+".pdf");
                List<SubjectArea> sas = new ArrayList<SubjectArea>(); sas.add(sa);
                PdfWorksheet.print(out, sas);
                out.flush(); out.close();
            }
            
            HibernateUtil.closeHibernate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
