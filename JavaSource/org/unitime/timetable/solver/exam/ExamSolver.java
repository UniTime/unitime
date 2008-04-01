/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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
package org.unitime.timetable.solver.exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamInstructor;
import net.sf.cpsolver.exam.model.ExamOwner;
import net.sf.cpsolver.exam.model.ExamPeriod;
import net.sf.cpsolver.exam.model.ExamPeriodPlacement;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;
import net.sf.cpsolver.exam.model.ExamRoomPlacement;
import net.sf.cpsolver.ifs.extension.ConflictStatistics;
import net.sf.cpsolver.ifs.extension.Extension;
import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Callback;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressWriter;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class ExamSolver extends Solver implements ExamSolverProxy {
    private static Log sLog = LogFactory.getLog(ExamSolver.class);
    private int iDebugLevel = Progress.MSGLEVEL_INFO;
    private boolean iWorking = false;
    private Date iLoadedDate = null;
    private ExamSolverDisposeListener iDisposeListener = null;
    
    public ExamSolver(DataProperties properties, ExamSolverDisposeListener disposeListener) {
        super(properties);
        iDisposeListener = disposeListener;
    }
    
    public Date getLoadedDate() {
        if (iLoadedDate!=null) return iLoadedDate;
        Vector log = Progress.getInstance(currentSolution().getModel()).getLog();
        if (log!=null && !log.isEmpty()) return ((Progress.Message)log.firstElement()).getDate();
        return null;
    }
    
    public String getLog() {
        return Progress.getInstance(currentSolution().getModel()).getHtmlLog(iDebugLevel, true);
    }
    public String getLog(int level, boolean includeDate) {
        return Progress.getInstance(currentSolution().getModel()).getHtmlLog(level, includeDate);
    }
    public String getLog(int level, boolean includeDate, String fromStage) {
        return Progress.getInstance(currentSolution().getModel()).getHtmlLog(level, includeDate, fromStage);
    }
    public String getNote() {
        return getProperties().getProperty("General.Note");
    }
    public void setNote(String note) {
        getProperties().setProperty("General.Note",note);
    }
    public void setDebugLevel(int level) { iDebugLevel = level; }
    public int getDebugLevel() { return iDebugLevel; }
    
    public boolean isWorking() {
        if (isRunning()) return true;
        return iWorking;
    }
    
    public void restoreBest() {
        currentSolution().restoreBest();
    }
    
    public void saveBest() {
        currentSolution().saveBest();
    }
    
    public Map getProgress() {
        try {
            Hashtable ret = new Hashtable(); 
            Progress p = Progress.getInstance(super.currentSolution().getModel());
            ret.put("STATUS",p.getStatus());
            ret.put("PHASE",p.getPhase());
            ret.put("PROGRESS",new Long(p.getProgress()));
            ret.put("MAX_PROGRESS",new Long(p.getProgressMax()));
            ret.put("VERSION", Constants.VERSION+"."+Constants.BLD_NUMBER);
            return ret;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            return null;
        }
    }
    
    public void setProperties(DataProperties properties) {
        this.getProperties().putAll(properties);
    }
    
    public void dispose() {
        disposeNoInherit();
    }
    
    private void disposeNoInherit() {
        super.dispose();
        if (currentSolution()!=null && currentSolution().getModel()!=null)
            Progress.removeInstance(currentSolution().getModel());
        setInitalSolution((net.sf.cpsolver.ifs.solution.Solution)null);
        if (iDisposeListener!=null) iDisposeListener.onDispose();
    }
    
    public String getHost() {
        return "local";
    }
    
    public String getHostLabel() {
        return getHost();
    }
    
    public Object exec(Object[] cmd) throws Exception {
        Class[] types = new Class[(cmd.length-2)/2];
        Object[] args = new Object[(cmd.length-2)/2];
        for (int i=0;i<types.length;i++) {
            types[i]=(Class)cmd[2*i+2];
            args[i]=cmd[2*i+3];
        }
        
        return getClass().getMethod((String)cmd[0],types).invoke(this, args);
    }
    
    public Exam getExam(long examId) {
        synchronized (super.currentSolution()) {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getId()==examId) return exam;
            }
            return null;
        }
    }

    public ExamInfo getInfo(long examId) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(examId);
            return (exam==null?null: new ExamInfo(exam));
        }
    }
    
    public ExamAssignment getAssignment(long examId) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(examId);
            return (exam==null || exam.getAssignment()==null?null:new ExamAssignment((ExamPlacement)exam.getAssignment()));
        }
    }
    
    public ExamAssignmentInfo getAssignmentInfo(long examId) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(examId);
            return (exam==null?null:new ExamAssignmentInfo(exam,(ExamPlacement)exam.getAssignment()));
        }
    }
    
    
    public Collection<ExamAssignmentInfo> getPeriods(long examId) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(examId);
            if (exam==null) return null;
            Vector<ExamAssignmentInfo> periods = new Vector<ExamAssignmentInfo>();
            for (Enumeration e=exam.getPeriodPlacements().elements();e.hasMoreElements();) {
                ExamPeriodPlacement period = (ExamPeriodPlacement)e.nextElement();
                Set rooms = exam.findBestAvailableRooms(period);
                if (rooms==null) rooms = new HashSet();
                if (!exam.checkDistributionConstraints(period)) continue;
                periods.add(new ExamAssignmentInfo(new ExamPlacement(exam, period, rooms)));
            }
            return periods;
        }
    }
    
    public Collection<ExamRoomInfo> getRooms(long examId, long periodId) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(examId);
            if (exam==null) return null;
            ExamPeriodPlacement period = null;
            for (Enumeration e=exam.getPeriodPlacements().elements();e.hasMoreElements();) {
                ExamPeriodPlacement p = (ExamPeriodPlacement)e.nextElement();
                if (periodId==p.getId()) { period = p; break; }
            }
            if (period==null) return null;
            Vector<ExamRoomInfo> rooms = new Vector<ExamRoomInfo>();
            if (exam.getMaxRooms()==0) return rooms;
            for (Enumeration e=exam.getRoomPlacements().elements();e.hasMoreElements();) {
                ExamRoomPlacement room = (ExamRoomPlacement)e.nextElement();
                
                int cap = room.getSize(exam.hasAltSeating());
                if (cap<exam.getStudents().size()/(exam.getMaxRooms()==1?1:2*exam.getMaxRooms())) continue;
                if (cap>Math.max(20,3*exam.getStudents().size())) continue;

                if (!room.isAvailable(period.getPeriod())) continue;
                if (!exam.checkDistributionConstraints(room)) continue;
                if (room.getRoom().getPlacement(period.getPeriod())!=null && !room.getRoom().getPlacement(period.getPeriod()).variable().equals(exam)) continue;
                rooms.add(new ExamRoomInfo(room.getRoom(), room.getPenalty(period.getPeriod())));
            }
            return rooms;
        }        
    }
    
    public ExamAssignmentInfo getAssignment(Long examId, Long periodId, Collection<Long> roomIds) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(examId);
            if (exam==null) return null;
            ExamPeriodPlacement period = null;
            for (Enumeration e=exam.getPeriodPlacements().elements();e.hasMoreElements();) {
                ExamPeriodPlacement p = (ExamPeriodPlacement)e.nextElement();
                if (p.getId().equals(periodId)) { period = p; break; }
            }
            if (period==null) return null;
            HashSet rooms = new HashSet();
            for (Iterator i=roomIds.iterator();i.hasNext();) {
                Long roomId = (Long)i.next();
                ExamRoomPlacement room = null;
                for (Enumeration e=exam.getRoomPlacements().elements();e.hasMoreElements();) {
                    ExamRoomPlacement r = (ExamRoomPlacement)e.nextElement();
                    if (r.getId()==roomId) { room = r; break; }
                }
                if (room!=null) rooms.add(room);
            }
            return new ExamAssignmentInfo(exam, new ExamPlacement(exam, period, rooms));
        }
    }
    
    public String assign(ExamAssignmentInfo assignment) {
        synchronized (super.currentSolution()) {
            Exam exam = getExam(assignment.getExamId());
            if (exam==null) return "Examination "+assignment.getExamName()+" not found.";
            ExamPeriodPlacement period = null;
            for (Enumeration e=exam.getPeriodPlacements().elements();e.hasMoreElements();) {
                ExamPeriodPlacement p = (ExamPeriodPlacement)e.nextElement();
                if (p.getId().equals(assignment.getPeriodId())) { period = p; break; }
            }
            if (period==null) return "Examination period "+assignment.getPeriodName()+" is not available for examination "+assignment.getExamName()+".";
            HashSet rooms = new HashSet();
            for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
                ExamRoomInfo ri = (ExamRoomInfo)i.next();
                ExamRoomPlacement room = null;
                for (Enumeration e=exam.getRoomPlacements().elements();e.hasMoreElements();) {
                    ExamRoomPlacement r = (ExamRoomPlacement)e.nextElement();
                    if (r.getId()==ri.getLocationId()) { room = r; break; }
                }
                if (room==null) return "Examination room "+ri.getName()+" not found.";
                if (!room.isAvailable(period.getPeriod())) return "Examination room "+ri.getName()+" is not available at "+assignment.getPeriodName()+".";
                rooms.add(room);
            }
            ExamPlacement p = new ExamPlacement(exam, period, rooms);
            Set conflicts = super.currentSolution().getModel().conflictValues(p);
            if (conflicts.isEmpty()) {
                exam.assign(0, p);
                return null;
            } else {
                ExamPlacement other = (ExamPlacement)conflicts.iterator().next();
                return "Selected placement "+p.getName()+" is in conflict with exam "+other.variable().getName()+" that is assigned to "+other.getName()+"."; 
            }
        }
    }

    public Hashtable currentSolutionInfo() {
        synchronized (super.currentSolution()) {
            return super.currentSolution().getInfo();
        }
    }

    public Hashtable bestSolutionInfo() {
        synchronized (super.currentSolution()) {
            return super.currentSolution().getBestInfo();
        }
    }
    
    protected void onFinish() {
        super.onFinish();
        try {
            iWorking = true;
            if (currentSolution().getBestInfo()!=null)
                currentSolution().restoreBest();
            if (currentSolution().getBestInfo()!=null && getProperties().getPropertyBoolean("General.Save",false)) {
                ExamDatabaseSaver saver = new ExamDatabaseSaver(this);
                synchronized (currentSolution()) {
                    saver.save();
                }
            }
            if (getProperties().getPropertyBoolean("General.Unload",false)) {
                dispose();
            } else {
                Progress.getInstance(currentSolution().getModel()).setStatus("Awaiting commands ...");
            }
        } finally {
            iWorking = false;
        }
    }
    
    protected void onStop() {
        super.onStop();
        if (currentSolution().getBestInfo()!=null)
            currentSolution().restoreBest();
    }

    public void save() {
        iWorking = true;
        ExamDatabaseSaver saver = new ExamDatabaseSaver(this);
        saver.setCallback(getSavingDoneCallback());
        Thread thread = new Thread(saver);
        thread.setPriority(THREAD_PRIORITY);
        thread.start();
    }
    
    public void load(DataProperties properties) {
        setProperties(properties);
        ExamModel model = new ExamModel(getProperties());
        Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
        
        iWorking = true;
        setInitalSolution(model);
        initSolver();
        
        ExamDatabaseLoader loader = new ExamDatabaseLoader(model);
        loader.setCallback(getLoadingDoneCallback());
        Thread thread = new Thread(loader);
        thread.setPriority(THREAD_PRIORITY);
        thread.start();
    }
    
    public void reload(DataProperties properties) {
        if (currentSolution()==null || currentSolution().getModel()==null) {
            load(properties);
            return;
        }
        
        Callback callBack = getReloadingDoneCallback();
        setProperties(properties);
        ExamModel model = new ExamModel(getProperties());
        
        iWorking = true;
        Progress.changeInstance(currentSolution().getModel(),model);
        setInitalSolution(model);
        initSolver();
        
        ExamDatabaseLoader loader = new ExamDatabaseLoader(model);
        loader.setCallback(callBack);
        (new Thread(loader)).start();
    }
    
    public Callback getLoadingDoneCallback() {
        return new LoadingDoneCallback();
    }
    
    public Callback getReloadingDoneCallback() {
        return new ReloadingDoneCallback();
    }

    public Callback getSavingDoneCallback() {
        return new SavingDoneCallback();
    }
    
    protected void afterSave() {
    }
    
    protected void afterLoad() {
    }

    protected void afterFinalSectioning() {
    }

    public class ReloadingDoneCallback implements Callback {
        Hashtable iCurrentAssignmentTable = new Hashtable();
        Hashtable iBestAssignmentTable = new Hashtable();
        Hashtable iInitialAssignmentTable = new Hashtable();
        String iSolutionId = null;
        Progress iProgress = null;
        public ReloadingDoneCallback() {
            iSolutionId = getProperties().getProperty("General.SolutionId");
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getAssignment()!=null)
                    iCurrentAssignmentTable.put(exam.getId(),exam.getAssignment());
                if (exam.getBestAssignment()!=null)
                    iBestAssignmentTable.put(exam.getId(),exam.getBestAssignment());
                if (exam.getInitialAssignment()!=null)
                    iInitialAssignmentTable.put(exam.getId(),exam.getInitialAssignment());
            }
        }
        private Exam getExam(long examId) {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getId()==examId) return exam;
            }
            return null;
        }
        private ExamPlacement getPlacement(Exam exam, ExamPlacement placement) {
            ExamPeriodPlacement period = null;
            for (Enumeration f=exam.getPeriodPlacements().elements();f.hasMoreElements();) {
                ExamPeriodPlacement p = (ExamPeriodPlacement)f.nextElement();
                if (placement.getPeriod().equals(p.getPeriod())) {
                    period = p; break;
                }
            }
            if (period==null) {
                iProgress.warn("WARNING: Period "+placement.getPeriod()+" is not available for class "+exam.getName());
                return null;
            }
            Set rooms = new HashSet();
            for (Iterator f=exam.getRoomPlacements().iterator();f.hasNext();) {
                ExamRoomPlacement r = (ExamRoomPlacement)f.next();
                if (r.isAvailable(period.getPeriod()) && placement.contains(r.getRoom())) {
                    rooms.add(r);
                }
            }
            if (rooms.size()!=placement.getRoomPlacements().size()) {
                iProgress.warn("WARNING: Room(s) "+placement.getRoomPlacements()+" are not available for exam "+exam.getName());
                return null;
            }
            return new ExamPlacement(exam,period,rooms);
        }
        private void assign(ExamPlacement placement) {
            Hashtable conflictConstraints = currentSolution().getModel().conflictConstraints(placement);
            if (conflictConstraints.isEmpty()) {
                placement.variable().assign(0,placement);
            } else {
                iProgress.warn("Unable to assign "+placement.variable().getName()+" := "+placement.getName());
                iProgress.warn("&nbsp;&nbsp;Reason:");
                for (Enumeration ex=conflictConstraints.keys();ex.hasMoreElements();) {
                    Constraint c = (Constraint)ex.nextElement();
                    Collection vals = (Collection)conflictConstraints.get(c);
                    for (Iterator j=vals.iterator();j.hasNext();) {
                        Value v = (Value) j.next();
                        iProgress.warn("&nbsp;&nbsp;&nbsp;&nbsp;"+v.variable().getName()+" = "+v.getName());
                    }
                    iProgress.debug("&nbsp;&nbsp;&nbsp;&nbsp;in constraint "+c);
                }
            }
        }
        private void unassignAll() {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getAssignment()!=null) exam.unassign(0);
            }
        }
        public void execute() {
            iProgress = Progress.getInstance(currentSolution().getModel());
            
            if (!iBestAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating best assignment ...",iBestAssignmentTable.size());
                unassignAll();
                for (Iterator i=iBestAssignmentTable.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    iProgress.incProgress();
                    Exam exam = getExam((Long)entry.getKey()); 
                    if (exam==null) continue;
                    ExamPlacement placement = getPlacement(exam,(ExamPlacement)entry.getValue());
                    if (placement!=null) assign(placement);
                }
                
                currentSolution().saveBest();
            }
            if (!iInitialAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating initial assignment ...",iInitialAssignmentTable.size());
                for (Iterator i=iInitialAssignmentTable.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    iProgress.incProgress();
                    Exam exam = getExam((Long)entry.getKey()); 
                    if (exam==null) continue;
                    ExamPlacement placement = getPlacement(exam,(ExamPlacement)entry.getValue());
                    if (placement!=null) exam.setInitialAssignment(placement);
                }
            }
            if (!iCurrentAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating current assignment ...",iCurrentAssignmentTable.size());
                unassignAll();
                for (Iterator i=iCurrentAssignmentTable.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    iProgress.incProgress();
                    Exam exam = getExam((Long)entry.getKey()); 
                    if (exam==null) continue;
                    ExamPlacement placement = getPlacement(exam,(ExamPlacement)entry.getValue());
                    if (placement!=null) assign(placement);
                }
            }
            iCurrentAssignmentTable.clear();
            iBestAssignmentTable.clear();
            iInitialAssignmentTable.clear();
            iProgress = null;
            
            if (iSolutionId!=null)
                getProperties().setProperty("General.SolutionId",iSolutionId);

            iLoadedDate = new Date();
            iWorking = false;
            afterLoad();
            Progress.getInstance(currentSolution().getModel()).setStatus("Awaiting commands ...");
        }
    }
    public class LoadingDoneCallback implements Callback {
        public void execute() {
            iLoadedDate = new Date();
            iWorking = false;
            afterLoad();
            Progress.getInstance(currentSolution().getModel()).setStatus("Awaiting commands ...");
            if (getProperties().getPropertyBoolean("General.StartSolver",false))
                start();
        }
    }
    public class SavingDoneCallback implements Callback {
        public void execute() {
            iWorking = false;
            afterSave();
            Progress.getInstance(currentSolution().getModel()).setStatus("Awaiting commands ...");
        }
    }
    
    public static interface ExamSolverDisposeListener {
        public void onDispose();
    }
    
    public boolean backup(File folder) {
        folder.mkdirs();
        if (currentSolution()==null) return false;
        synchronized (currentSolution()) {
            File outXmlFile = new File(folder,"exam"+BackupFileFilter.sXmlExtension);
            File outPropertiesFile = new File(folder,"exam"+BackupFileFilter.sPropertiesExtension);
            try {
                getProperties().setProperty("Xml.SaveConflictTable", "false");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outXmlFile);
                    (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(((ExamModel)currentSolution().getModel()).save());
                    fos.flush(); fos.close(); fos=null;
                } finally {
                    try {
                        if (fos!=null) fos.close();
                    } catch (IOException e) {}
                }
                for (Iterator i=getProperties().entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    if (!(entry.getKey() instanceof String)) {
                        sLog.error("Configuration key "+entry.getKey()+" is not of type String ("+entry.getKey().getClass()+")");
                        i.remove();
                    } else if (!(entry.getValue() instanceof String)) {
                        sLog.error("Value of configuration key "+entry.getKey()+" is not of type String ("+entry.getValue()+" is of type "+entry.getValue().getClass()+")");
                        i.remove();
                    }
                }
                try {
                    fos = new FileOutputStream(outPropertiesFile);
                    getProperties().store(fos,"Backup file");
                    fos.flush(); fos.close(); fos=null;
                } finally {
                    try {
                        if (fos!=null) fos.close();
                    } catch (IOException e) {}
                }
                return true;
            } catch (Exception e) {
                sLog.error(e.getMessage(),e);
                if (outXmlFile.exists()) outXmlFile.delete();
                if (outPropertiesFile.exists()) outPropertiesFile.delete();
            }
        }
        return false;
    }
    
    public boolean restore(File folder) {
        return restore(folder, false);
    }
    
    public boolean restore(File folder, boolean removeFiles) {
        sLog.debug("restore(folder="+folder+",exam)");
        File inXmlFile = new File(folder,"exam"+BackupFileFilter.sXmlExtension);
        File inPropertiesFile = new File(folder,"exam"+BackupFileFilter.sPropertiesExtension);
        
        ExamModel model = null;
        try {
            if (isRunning()) stopSolver();
            this.disposeNoInherit();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(inPropertiesFile);
                getProperties().load(fis);
            } finally {
                if (fis!=null) fis.close();
            }
            
            model = new ExamModel(getProperties());
            Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
            setInitalSolution(model);
            initSolver();

            model.load((new SAXReader()).read(inXmlFile));
            
            Progress.getInstance(model).setStatus("Awaiting commands ...");
            
            if (removeFiles) {
                inXmlFile.delete();
                inPropertiesFile.delete();
            }
            
            return true;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            if (model!=null) Progress.removeInstance(model);
        }
        
        return false;
    }
    
    public void clear() {
        synchronized (currentSolution()) {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getAssignment()!=null) exam.unassign(0);
            }
            currentSolution().clearBest();
        }    
    }
    
    public Collection<ExamAssignmentInfo> getAssignedExams() {
        synchronized (currentSolution()) {
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getAssignment()!=null)
                    ret.add(new ExamAssignmentInfo((ExamPlacement)exam.getAssignment()));
            }
            return ret;
        }
    }
    public Collection<ExamInfo> getUnassignedExams() {
        synchronized (currentSolution()) {
            Vector<ExamInfo> ret = new Vector<ExamInfo>();
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getAssignment()==null)
                    ret.add(new ExamInfo(exam));
            }
            return ret;
        }
    }

    public Collection<ExamAssignmentInfo> getAssignedExams(Long subjectAreaId) {
        if (subjectAreaId==null || subjectAreaId<0) return getAssignedExams();
        String sa = new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ";
        synchronized (currentSolution()) {
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                boolean hasSubjectArea = false;
                for (Enumeration f=exam.getOwners().elements();!hasSubjectArea && f.hasMoreElements();) {
                    ExamOwner ecs = (ExamOwner)f.nextElement();
                    hasSubjectArea = ecs.getName().startsWith(sa);
                }
                if (hasSubjectArea && exam.getAssignment()!=null)
                    ret.add(new ExamAssignmentInfo((ExamPlacement)exam.getAssignment()));
            }
            return ret;
        }
    }
    public Collection<ExamInfo> getUnassignedExams(Long subjectAreaId) {
        if (subjectAreaId==null || subjectAreaId<0) return getUnassignedExams();
        String sa = new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ";
        synchronized (currentSolution()) {
            Vector<ExamInfo> ret = new Vector<ExamInfo>();
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                boolean hasSubjectArea = false;
                for (Enumeration f=exam.getOwners().elements();!hasSubjectArea && f.hasMoreElements();) {
                    ExamOwner ecs = (ExamOwner)f.nextElement();
                    hasSubjectArea = ecs.getName().startsWith(sa);
                }
                if (hasSubjectArea && exam.getAssignment()==null)
                    ret.add(new ExamInfo(exam));
            }
            return ret;
        }
    }
    
    public Collection<ExamAssignmentInfo> getAssignedExamsOfRoom(Long roomId) {
        synchronized (currentSolution()) {
            ExamRoom room = null;
            for (Enumeration e=((ExamModel)currentSolution().getModel()).getRooms().elements();e.hasMoreElements();) {
                ExamRoom r = (ExamRoom)e.nextElement();
                if (r.getId()==roomId) {
                    room = r; break;
                }
            }
            if (room==null) return null;
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (Enumeration e=((ExamModel)currentSolution().getModel()).getPeriods().elements();e.hasMoreElements();) {
                ExamPeriod period = (ExamPeriod)e.nextElement();
                ExamPlacement placement = room.getPlacement(period);
                if (placement!=null)
                    ret.add(new ExamAssignmentInfo(placement));
            }
            return ret;
        }
    }

    public Collection<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId) {
        synchronized (currentSolution()) {
            ExamInstructor instructor = null;
            for (Enumeration e=((ExamModel)currentSolution().getModel()).getRooms().elements();e.hasMoreElements();) {
                ExamInstructor i = (ExamInstructor)e.nextElement();
                if (i.getId()==instructorId) {
                    instructor = i; break;
                }
            }
            if (instructor==null) return null;
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (Enumeration e=((ExamModel)currentSolution().getModel()).getPeriods().elements();e.hasMoreElements();) {
                ExamPeriod period = (ExamPeriod)e.nextElement();
                Set exams = instructor.getExams(period);
                if (exams!=null)
                    for (Iterator i=exams.iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        ret.add(new ExamAssignmentInfo((ExamPlacement)exam.getAssignment()));                        
                    }
            }
            return ret;
        }
    }
    
    public int getExamType() {
    	return getProperties().getPropertyInt("Exam.Type", org.unitime.timetable.model.Exam.sExamTypeFinal);
    }
    
    public Collection<ExamAssignmentInfo[]> getChangesToInitial(Long subjectAreaId) {
        String sa = (subjectAreaId!=null && subjectAreaId>=0 ? new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ":null);
        Vector<ExamAssignmentInfo[]> changes = new Vector<ExamAssignmentInfo[]>();
        synchronized (currentSolution()) {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (sa!=null) {
                    boolean hasSubjectArea = false;
                    for (Enumeration f=exam.getOwners().elements();!hasSubjectArea && f.hasMoreElements();) {
                        ExamOwner ecs = (ExamOwner)f.nextElement();
                        hasSubjectArea = ecs.getName().startsWith(sa);
                    }
                    if (!hasSubjectArea) continue;
                }
                if (!ToolBox.equals(exam.getInitialAssignment(),exam.getAssignment())) {
                    changes.add(new ExamAssignmentInfo[] {
                            new ExamAssignmentInfo(exam,(ExamPlacement)exam.getInitialAssignment()),
                            new ExamAssignmentInfo(exam,(ExamPlacement)exam.getAssignment())});
                }
            }
        }
        return changes;
    }
    
    public Collection<ExamAssignmentInfo[]> getChangesToBest(Long subjectAreaId) {
        String sa = (subjectAreaId!=null && subjectAreaId>=0 ? new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ":null);
        Vector<ExamAssignmentInfo[]> changes = new Vector<ExamAssignmentInfo[]>();
        synchronized (currentSolution()) {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (sa!=null) {
                    boolean hasSubjectArea = false;
                    for (Enumeration f=exam.getOwners().elements();!hasSubjectArea && f.hasMoreElements();) {
                        ExamOwner ecs = (ExamOwner)f.nextElement();
                        hasSubjectArea = ecs.getName().startsWith(sa);
                    }
                    if (!hasSubjectArea) continue;
                }
                if (!ToolBox.equals(exam.getBestAssignment(),exam.getAssignment())) {
                    changes.add(new ExamAssignmentInfo[] {
                            new ExamAssignmentInfo(exam,(ExamPlacement)exam.getInitialAssignment()),
                            new ExamAssignmentInfo(exam,(ExamPlacement)exam.getAssignment())});
                }
            }
        }
        return changes;
    }
    
    public Long getSessionId() {
        return getProperties().getPropertyLong("General.SessionId",null);
    }
    
    public ExamConflictStatisticsInfo getCbsInfo() {
        ConflictStatistics cbs = null;
        for (Enumeration e=getExtensions().elements();e.hasMoreElements();) {
            Extension ext = (Extension)e.nextElement();
            if (ext instanceof ConflictStatistics) {
                cbs = (ConflictStatistics)ext;
                break;
            }
        }
        if (cbs==null || cbs.getNoGoods().isEmpty()) return null;
        ExamConflictStatisticsInfo info = new ExamConflictStatisticsInfo();
        synchronized (currentSolution()) {
            info.load(cbs);
        }
        return info; 
    }
    
    public ExamConflictStatisticsInfo getCbsInfo(Long examId) {
        ConflictStatistics cbs = null;
        for (Enumeration e=getExtensions().elements();e.hasMoreElements();) {
            Extension ext = (Extension)e.nextElement();
            if (ext instanceof ConflictStatistics) {
                cbs = (ConflictStatistics)ext;
                break;
            }
        }
        if (cbs==null || cbs.getNoGoods().isEmpty()) return null;
        ExamConflictStatisticsInfo info = new ExamConflictStatisticsInfo();
        synchronized (currentSolution()) {
            info.load(cbs, examId);
        }
        return info; 
    }    
}
