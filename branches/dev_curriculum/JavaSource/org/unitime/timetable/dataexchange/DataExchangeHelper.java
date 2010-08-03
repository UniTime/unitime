package org.unitime.timetable.dataexchange;

import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;

public class DataExchangeHelper {
    protected static Log sLog = LogFactory.getLog(DataExchangeHelper.class);
    public static String sLogLevelDebug = "DEBUG";
    public static String sLogLevelInfo = "INFO";
    public static String sLogLevelWarn = "WARN";
    public static String sLogLevelError = "ERROR";
    public static String sLogLevelFatal = "FATAL";
    protected LogWriter iTextLog;
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
        sImportRegister = new Hashtable<String, Class>();
        sImportRegister.put("academicAreaReservations", AcadAreaReservationImport.class);
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
    }
    
    public DataExchangeHelper() {
    }
    
    public void setLog(LogWriter out) {
        iTextLog = out;
    }
    public LogWriter getLog() {
        return iTextLog;
    }
    public void log(String level, String message, Throwable t) {
        if (iTextLog==null) return;
        if (message!=null) {
            if (sLogLevelDebug.equals(level)) iTextLog.println("<font color='gray'>&nbsp;&nbsp;--"+message+"</font><br>");
            else if (sLogLevelInfo.equals(level)) iTextLog.println(message+"<br>");
            else if (sLogLevelWarn.equals(level)) iTextLog.println("<font color='orange'>"+message+"</font><br>");
            else if (sLogLevelError.equals(level)) iTextLog.println("<font color='red'>"+message+"</font><br>");
            else if (sLogLevelFatal.equals(level)) iTextLog.println("<font color='red'><b>"+message+"</b></font><br>");
            else iTextLog.println(message);
        }
    }

    public void debug(String msg) {
        log(sLogLevelDebug, msg, null);
        sLog.debug(msg);
    }
    public void info(String msg) {
        log(sLogLevelInfo, msg, null);
        sLog.info(msg);
    }
    public void warn(String msg) {
        log(sLogLevelWarn, msg, null);
        sLog.warn(msg);
    }
    public void error(String msg) {
        log(sLogLevelError, msg, null);
        sLog.error(msg);
    }
    public void fatal(String msg) {
        log(sLogLevelFatal, msg, null);
        sLog.fatal(msg);
    }
    public void debug(String msg, Throwable t) {
        log(sLogLevelDebug, msg, t);
        sLog.debug(msg, t);
    }
    public void info(String msg, Throwable t) {
        log(sLogLevelInfo, msg, t);
        sLog.info(msg, t);
    }
    public void warn(String msg, Throwable t) {
        log(sLogLevelWarn, msg, t);
        sLog.warn(msg, t);
    }
    public void error(String msg, Throwable t) {
        log(sLogLevelError, msg, t);
        sLog.error(msg, t);
    }
    public void fatal(String msg, Throwable t) {
        log(sLogLevelFatal, msg, t);
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
    
    public static void importDocument(Document document, TimetableManager manager, LogWriter log) throws Exception {
        BaseImport imp = createImportBase(document.getRootElement().getName());
        imp.setLog(log);
        imp.setManager(manager);
        imp.loadXml(document.getRootElement());
    }
    
    public static Document exportDocument(String rootName, Session session, Properties parameters, LogWriter log) throws Exception {
        BaseExport exp = createExportBase(rootName);
        exp.setLog(log);
        return exp.saveXml(session, parameters);
    }
    
    public interface LogWriter {
    	public void println(String message);
    }
}
