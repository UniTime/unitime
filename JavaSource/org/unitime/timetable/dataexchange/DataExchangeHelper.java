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
package org.unitime.timetable.dataexchange;

import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DataExchangeHelper {
    protected static Log sLog = LogFactory.getLog(DataExchangeHelper.class);
    protected Log iTextLog;
    protected org.hibernate.Session iHibSession = null;
    protected org.hibernate.Transaction iTx = null;
    protected int iFlushIfNeededCounter = 0;
    protected static int sBatchSize = 100;
    
    public static Hashtable<String,Class> sExportRegister;
    public static Hashtable<String,Class> sImportRegister;
    
    static {
        sExportRegister = new Hashtable<String, Class>();
        sExportRegister.put("exams", CourseOfferingExport.class);
        sExportRegister.put("offerings", CourseOfferingExport.class);
        sExportRegister.put("timetable", CourseTimetableExport.class);
        sExportRegister.put("curricula", CurriculaExport.class);
        sExportRegister.put("studentEnrollments", StudentEnrollmentExport.class);
        sExportRegister.put("students", StudentExport.class);
        sExportRegister.put("reservations", ReservationExport.class);
        sExportRegister.put("permissions", PermissionsExport.class);
        sExportRegister.put("traveltimes", TravelTimesExport.class);
        sExportRegister.put("lastLikeCourseDemand", LastLikeCourseDemandExport.class);
        sExportRegister.put("request", StudentSectioningExport.class);
        sExportRegister.put("roomSharing", RoomSharingExport.class);
        sExportRegister.put("pointInTimeData", PointInTimeDataExport.class);
        sExportRegister.put("preferences", PreferencesExport.class);
        sExportRegister.put("sessionSetup", AcademicSessionSetupExport.class);
        sImportRegister = new Hashtable<String, Class>();
        sImportRegister.put("academicAreas",AcademicAreaImport.class);
        sImportRegister.put("academicClassifications",AcademicClassificationImport.class);
        sImportRegister.put("buildingsRooms", BuildingRoomImport.class);
        sImportRegister.put("courseCatalog", CourseCatalogImport.class);
        sImportRegister.put("offerings", CourseOfferingImport.class);
        sImportRegister.put("courseOfferingReservations", CourseOfferingReservationImport.class);
        sImportRegister.put("departments",DepartmentImport.class);
        sImportRegister.put("posMajors", PosMajorImport.class);
        sImportRegister.put("posMinors", PosMinorImport.class);
        sImportRegister.put("session", SessionImport.class);
        sImportRegister.put("staff", StaffImport.class);
        sImportRegister.put("studentEnrollments", StudentEnrollmentImport.class);
        sImportRegister.put("students", StudentImport.class);
        sImportRegister.put("lastLikeCourseDemand", LastLikeCourseDemandImport.class);
        sImportRegister.put("subjectAreas",SubjectAreaImport.class);
        sImportRegister.put("request",StudentSectioningImport.class);
        sImportRegister.put("events",EventImport.class);
        sImportRegister.put("curricula",CurriculaImport.class);
        sImportRegister.put("studentGroups", StudentGroupImport.class);
        sImportRegister.put("studentAccomodations", StudentAccomodationImport.class);
        sImportRegister.put("reservations", ReservationImport.class);
        sImportRegister.put("permissions", PermissionsImport.class);
        sImportRegister.put("traveltimes", TravelTimesImport.class);
        sImportRegister.put("timetable", CourseTimetableImport.class);
        sImportRegister.put("roomSharing", RoomSharingImport.class);
        sImportRegister.put("scripts", ScriptImport.class);
        sImportRegister.put("script", ScriptImport.class);
        sImportRegister.put("reports", HQLImport.class);
        sImportRegister.put("report", HQLImport.class);
        sImportRegister.put("pointInTimeData", PointInTimeDataImport.class);
        sImportRegister.put("preferences", PreferencesImport.class);
        sImportRegister.put("sessionSetup", AcademicSessionSetupImport.class);
    }
    
    public DataExchangeHelper() {
    }
    
    public void setLog(Log out) {
        iTextLog = out;
    }
    public Log getLog() {
        return iTextLog;
    }

    public void debug(String msg) {
    	if (iTextLog != null) iTextLog.debug(msg);
        sLog.debug(msg);
    }
    public void info(String msg) {
    	if (iTextLog != null) iTextLog.info(msg);
        sLog.info(msg);
    }
    public void warn(String msg) {
    	if (iTextLog != null) iTextLog.warn(msg);
        sLog.warn(msg);
    }
    public void error(String msg) {
    	if (iTextLog != null) iTextLog.error(msg);
        sLog.error(msg);
    }
    public void fatal(String msg) {
    	if (iTextLog != null) iTextLog.fatal(msg);
        sLog.fatal(msg);
    }
    public void debug(String msg, Throwable t) {
    	if (iTextLog != null) iTextLog.debug(msg, t);
        sLog.debug(msg, t);
    }
    public void info(String msg, Throwable t) {
    	if (iTextLog != null) iTextLog.info(msg, t);
        sLog.info(msg, t);
    }
    public void warn(String msg, Throwable t) {
    	if (iTextLog != null) iTextLog.warn(msg, t);
        sLog.warn(msg, t);
    }
    public void error(String msg, Throwable t) {
    	if (iTextLog != null) iTextLog.error(msg, t);
        sLog.error(msg, t);
    }
    public void fatal(String msg, Throwable t) {
    	if (iTextLog != null) iTextLog.fatal(msg, t);
        sLog.fatal(msg, t);
    }
    
    public org.hibernate.Session getHibSession() {
        return iHibSession;
    }
    
    public boolean beginTransaction() {
        try {
            iHibSession = new _RootDAO().getSession();
            iTx = iHibSession.beginTransaction();
            debug("Transaction started.");
            return true;
        } catch (Exception e) {
            fatal("Unable to begin transaction, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean commitTransaction() {
        try {
            iTx.commit();
            debug("Transaction committed.");
            return true;
        } catch (Exception e) {
            fatal("Unable to commit transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (iHibSession!=null && iHibSession.isOpen())
                iHibSession.close();
        }
    }

    public boolean rollbackTransaction() {
        try {
            iTx.rollback();
            info("Transaction rollbacked.");
            return true;
        } catch (Exception e) {
            fatal("Unable to rollback transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (iHibSession!=null && iHibSession.isOpen())
                iHibSession.close();
        }
    }
    
    public boolean flush(boolean commit) {
        try {
            getHibSession().flush(); getHibSession().clear();
            if (commit && iTx!=null) {
                iTx.commit();
                iTx = getHibSession().beginTransaction();
            }
            return true;
        } catch (Exception e) {
            fatal("Unable to flush current session, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean flushIfNeeded(boolean commit) {
        iFlushIfNeededCounter++;
        if (iFlushIfNeededCounter>=sBatchSize) {
            iFlushIfNeededCounter = 0;
            return flush(commit);
        }
        return true;
    }
    
 
    public boolean flushDoNotClearSession(boolean commit) {
        try {
            getHibSession().flush(); 
            if (commit && iTx!=null) {
                iTx.commit();
                iTx = getHibSession().beginTransaction();
            }
            return true;
        } catch (Exception e) {
            fatal("Unable to flush current session, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean flushIfNeededDoNotClearSession(boolean commit) {
        iFlushIfNeededCounter++;
        if (iFlushIfNeededCounter>=sBatchSize) {
            iFlushIfNeededCounter = 0;
            return flushDoNotClearSession(commit);
        }
        return true;
    }
    
    public static BaseImport createImportBase(String type) throws Exception {
        if (type==null) throw new Exception("Import type not provided.");
        if (!sImportRegister.containsKey(type)) throw new Exception("Unknown import type "+type+".");
        return (BaseImport)sImportRegister.get(type).getConstructor().newInstance();
    }

    public static BaseExport createExportBase(String type) throws Exception {
        if (type==null) throw new Exception("Export type not provided.");
        if (!sExportRegister.containsKey(type)) throw new Exception("Unknown export type "+type+".");
        return (BaseExport)sExportRegister.get(type).getConstructor().newInstance();
    }
    
    public static void importDocument(Document document, String userId, Log log) throws Exception {
        BaseImport imp = createImportBase(document.getRootElement().getName());
        imp.setLog(log);
        if (userId != null)
        	imp.setManager(TimetableManager.findByExternalId(userId));
        imp.loadXml(document.getRootElement());
    }
    
    public static Document exportDocument(String rootName, Session session, Properties parameters, Log log) throws Exception {
        BaseExport exp = createExportBase(rootName);
        exp.setLog(log);
        return exp.saveXml(session, parameters);
    }
    
    public interface LogWriter {
    	public void println(String message);
    }
    
    protected String getExternalUniqueId(Class_ clazz) {
    	String externalId = clazz.getExternalUniqueId();
    	if (externalId != null && !externalId.isEmpty()) return externalId;
    	return getClassLabel(clazz);
	}
    
    protected String getClassLabel(Class_ clazz) {
    	return clazz.getCourseName() + " " + clazz.getItypeDesc().trim() + " " + getClassSuffix(clazz);
    }
    
    protected String getClassSuffix(Class_ clazz) {
    	String suffix = clazz.getClassSuffix();
    	if (suffix != null && !suffix.isEmpty()) return suffix;
    	return clazz.getSectionNumberString(getHibSession());
    }
}
