/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.exam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.exam.heuristics.ExamNeighbourSelection;
import org.cpsolver.exam.model.Exam;
import org.cpsolver.exam.model.ExamInstructor;
import org.cpsolver.exam.model.ExamOwner;
import org.cpsolver.exam.model.ExamPeriod;
import org.cpsolver.exam.model.ExamPeriodPlacement;
import org.cpsolver.exam.model.ExamPlacement;
import org.cpsolver.exam.model.ExamRoom;
import org.cpsolver.exam.model.ExamRoomPlacement;
import org.cpsolver.exam.model.ExamRoomSharing;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.extension.Extension;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.ParallelSolver;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;
import org.unitime.timetable.solver.exam.ui.ExamProposedChange;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamSuggestionsInfo;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
public class ExamSolver extends ParallelSolver<Exam, ExamPlacement> implements ExamSolverProxy {
    private static Log sLog = LogFactory.getLog(ExamSolver.class);
    private int iDebugLevel = Progress.MSGLEVEL_INFO;
    private boolean iWorking = false;
    private Date iLoadedDate = null;
    private ExamSolverDisposeListener iDisposeListener = null;
    private ExamConflictStatisticsInfo iCbsInfo = null;
    
    private long iLastTimeStamp = System.currentTimeMillis();
    private boolean iIsPassivated = false;
    private Map iProgressBeforePassivation = null;
    private Map<String,String> iCurrentSolutionInfoBeforePassivation = null;
    private Map<String,String> iBestSolutionInfoBeforePassivation = null;
    private File iPassivationFolder = null;
    private String iPassivationPuid = null;
    private Thread iWorkThread = null;

    
    public ExamSolver(DataProperties properties, ExamSolverDisposeListener disposeListener) {
        super(properties);
        iDisposeListener = disposeListener;
    }
    
    public Date getLoadedDate() {
        if (iLoadedDate==null && !isPassivated()) {
            List<Progress.Message> log = Progress.getInstance(currentSolution().getModel()).getLog();
            if (log!=null && !log.isEmpty()) {
                iLoadedDate = log.get(0).getDate();
            }
        }
        return iLoadedDate;
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
        if (isPassivated()) return iProgressBeforePassivation;
        try {
            Hashtable ret = new Hashtable(); 
            Progress p = Progress.getInstance(super.currentSolution().getModel());
            ret.put("STATUS",p.getStatus());
            ret.put("PHASE",p.getPhase());
            ret.put("PROGRESS",new Long(p.getProgress()));
            ret.put("MAX_PROGRESS",new Long(p.getProgressMax()));
            ret.put("VERSION", Constants.getVersion());
            return ret;
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            return null;
        }
    }
    
    public void setProperties(DataProperties properties) {
        activateIfNeeded();
        this.getProperties().putAll(properties);
    }
    
    public void dispose() {
        disposeNoInherit(true);
    }
    
    private void disposeNoInherit(boolean unregister) {
        super.dispose();
        if (currentSolution()!=null && currentSolution().getModel()!=null)
            Progress.removeInstance(currentSolution().getModel());
        setInitalSolution((org.cpsolver.ifs.solution.Solution)null);
        if (unregister && iDisposeListener!=null) iDisposeListener.onDispose();
        iCbsInfo = null;
    }
    
    public String getHost() {
        return "local";
    }
    
    public String getUser() {
    	return getProperties().getProperty("General.OwnerPuid");
    }
    
    public Object exec(Object[] cmd) throws Exception {
        Class[] types = new Class[(cmd.length-3)/2];
        Object[] args = new Object[(cmd.length-3)/2];
        for (int i=0;i<types.length;i++) {
            types[i]=(Class)cmd[2*i+3];
            args[i]=cmd[2*i+4];
        }
        
        return getClass().getMethod((String)cmd[0],types).invoke(this, args);
    }
    
    public Exam getExam(long examId) {
        synchronized (currentSolution()) {
            for (Exam exam: currentSolution().getModel().variables()) {
                if (exam.getId()==examId) return exam;
            }
            return null;
        }
    }

    public ExamInfo getInfo(long examId) {
        synchronized (currentSolution()) {
            Exam exam = getExam(examId);
            return (exam==null?null: new ExamInfo(exam));
        }
    }
    
    public ExamAssignment getAssignment(long examId) {
        synchronized (currentSolution()) {
            Exam exam = getExam(examId);
            ExamPlacement placement = (exam == null ? null : currentSolution().getAssignment().getValue(exam));
            return placement == null ? null : new ExamAssignment(placement, currentSolution().getAssignment());
        }
    }
    
    public ExamAssignmentInfo getAssignmentInfo(long examId) {
        synchronized (currentSolution()) {
            Exam exam = getExam(examId);
            ExamPlacement placement = (exam == null ? null : currentSolution().getAssignment().getValue(exam));
            return placement == null ? null : new ExamAssignmentInfo(placement, currentSolution().getAssignment());
        }
    }
    
    
    public ExamPlacement getPlacement(ExamAssignment assignment) {
        synchronized (currentSolution()) {
            Exam exam = getExam(assignment.getExamId());
            if (exam==null) return null;
            ExamPeriodPlacement period = null;
            for (ExamPeriodPlacement p: exam.getPeriodPlacements()) {
                if (p.getId().equals(assignment.getPeriodId())) { period = p; break; }
            }
            if (period==null) return null;
            HashSet rooms = new HashSet();
            for (ExamRoomInfo roomInfo : assignment.getRooms()) {
                Long roomId = roomInfo.getLocationId();
                ExamRoomPlacement room = null;
                for (ExamRoomPlacement r: exam.getRoomPlacements()) {
                    if (r.getId()==roomId) { room = r; break; }
                }
                if (room!=null) rooms.add(room);
            }
            return new ExamPlacement(exam, period, rooms);
        }
    }
    
    public ExamAssignmentInfo getAssignment(Long examId, Long periodId, Collection<Long> roomIds) {
        synchronized (currentSolution()) {
            Exam exam = getExam(examId);
            if (exam==null) return null;
            ExamPeriodPlacement period = null;
            for (ExamPeriodPlacement p: exam.getPeriodPlacements()) {
                if (p.getId().equals(periodId)) { period = p; break; }
            }
            if (period==null) return null;
            HashSet rooms = new HashSet();
            for (Long roomId: roomIds) {
                ExamRoomPlacement room = null;
                for (ExamRoomPlacement r: exam.getRoomPlacements()) {
                    if (r.getId()==roomId) { room = r; break; }
                }
                if (room!=null) rooms.add(room);
            }
            return new ExamAssignmentInfo(exam, new ExamPlacement(exam, period, rooms), currentSolution().getAssignment());
        }
    }
    
    public String assign(ExamAssignment assignment) {
        synchronized (currentSolution()) {
            Exam exam = getExam(assignment.getExamId());
            if (exam==null) return "Examination "+assignment.getExamName()+" not found.";
            ExamPeriodPlacement period = null;
            for (ExamPeriodPlacement p: exam.getPeriodPlacements()) {
                if (p.getId().equals(assignment.getPeriodId())) { period = p; break; }
            }
            if (period==null) return "Examination period "+assignment.getPeriodName()+" is not available for examination "+assignment.getExamName()+".";
            HashSet rooms = new HashSet();
            for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
                ExamRoomInfo ri = (ExamRoomInfo)i.next();
                ExamRoomPlacement room = null;
                for (ExamRoomPlacement r: exam.getRoomPlacements()) {
                    if (r.getId()==ri.getLocationId()) { room = r; break; }
                }
                if (room==null) return "Examination room "+ri.getName()+" not found.";
                if (!room.isAvailable(period.getPeriod())) return "Examination room "+ri.getName()+" is not available at "+assignment.getPeriodName()+".";
                rooms.add(room);
            }
            ExamPlacement p = new ExamPlacement(exam, period, rooms);
            Set conflicts = currentSolution().getModel().conflictValues(currentSolution().getAssignment(), p);
            if (conflicts.isEmpty()) {
            	ExamPlacement old = currentSolution().getAssignment().getValue(exam);
            	currentSolution().getAssignment().assign(0, p);
            	Progress.getInstance(currentSolution().getModel()).info(exam.getName() + ": " + (old == null ? "not assigned" : old.getName()) + " &rarr; " + p.getName());
                return null;
            } else {
                ExamPlacement other = (ExamPlacement)conflicts.iterator().next();
                return "Selected placement "+p.getName()+" is in conflict with exam "+other.variable().getName()+" that is assigned to "+other.getName()+"."; 
            }
        }
    }
    
    public String unassign(ExamInfo examInfo) {
        synchronized (currentSolution()) {
            Exam exam = getExam(examInfo.getExamId());
            if (exam==null) return "Examination "+examInfo.getExamName()+" not found.";
            ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
            if (placement == null) return "Examination "+examInfo.getExamName()+" is not assigned.";
            Progress.getInstance(currentSolution().getModel()).info(exam.getName() + ": " + placement.getName() + " &rarr; not assigned");
            currentSolution().getAssignment().unassign(0, exam);
            return null;
        }
    }

    
    public Map<String,String> currentSolutionInfo() {
        if (isPassivated()) return iCurrentSolutionInfoBeforePassivation;
        synchronized (super.currentSolution()) {
            return super.currentSolution().getInfo();
        }
    }

    public Map<String,String> bestSolutionInfo() {
        if (isPassivated()) return iBestSolutionInfoBeforePassivation;
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
        iWorkThread = new Thread(saver);
        iWorkThread.setPriority(THREAD_PRIORITY);
        iWorkThread.start();
    }
    
    public void load(DataProperties properties) {
        iCbsInfo = null;
        setProperties(properties);
        ExamModel model = new ExamModel(getProperties());
        Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
        
        iWorking = true;
        setInitalSolution(model);
        initSolver();
        
        ExamDatabaseLoader loader = new ExamDatabaseLoader(model, currentSolution().getAssignment());
        loader.setCallback(getLoadingDoneCallback());
        iWorkThread = new Thread(loader);
        iWorkThread.setPriority(THREAD_PRIORITY);
        iWorkThread.start();
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
        
        ExamDatabaseLoader loader = new ExamDatabaseLoader(model, currentSolution().getAssignment());
        loader.setCallback(callBack);
        iWorkThread = new Thread(loader);
        iWorkThread.start();
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
            for (Exam exam: currentSolution().getModel().variables()) {
            	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
                if (placement!=null)
                    iCurrentAssignmentTable.put(exam.getId(),placement);
                if (exam.getBestAssignment()!=null)
                    iBestAssignmentTable.put(exam.getId(),exam.getBestAssignment());
                if (exam.getInitialAssignment()!=null)
                    iInitialAssignmentTable.put(exam.getId(),exam.getInitialAssignment());
            }
        }
        private Exam getExam(long examId) {
            for (Exam exam: currentSolution().getModel().variables()) {
                if (exam.getId()==examId) return exam;
            }
            return null;
        }
        private ExamPlacement getPlacement(Exam exam, ExamPlacement placement) {
            ExamPeriodPlacement period = null;
            for (ExamPeriodPlacement p: exam.getPeriodPlacements()) {
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
            Map<Constraint<Exam,ExamPlacement>, Set<ExamPlacement>> conflictConstraints = currentSolution().getModel().conflictConstraints(currentSolution().getAssignment(), placement);
            if (conflictConstraints.isEmpty()) {
            	currentSolution().getAssignment().assign(0,placement);
            } else {
                iProgress.warn("Unable to assign "+placement.variable().getName()+" := "+placement.getName());
                iProgress.warn("&nbsp;&nbsp;Reason:");
                for (Constraint<Exam,ExamPlacement> c: conflictConstraints.keySet()) {
                	Set<ExamPlacement> vals = conflictConstraints.get(c);
                    for (ExamPlacement v: vals) {
                        iProgress.warn("&nbsp;&nbsp;&nbsp;&nbsp;"+v.variable().getName()+" = "+v.getName());
                    }
                    iProgress.debug("&nbsp;&nbsp;&nbsp;&nbsp;in constraint "+c);
                }
            }
        }
        private void unassignAll() {
            for (Exam exam: currentSolution().getModel().variables()) {
            	currentSolution().getAssignment().unassign(0, exam);
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
    
    public boolean backup(File folder, String puid) {
        folder.mkdirs();
        if (currentSolution()==null) return false;
        synchronized (currentSolution()) {
            File outXmlFile = new File(folder,"exam_"+puid+BackupFileFilter.sXmlExtension);
            File outPropertiesFile = new File(folder,"exam_"+puid+BackupFileFilter.sPropertiesExtension);
            try {
                getProperties().setProperty("Xml.SaveConflictTable", "false");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outXmlFile);
                    Document document = ((ExamModel)currentSolution().getModel()).save(currentSolution().getAssignment());
                    ExamConflictStatisticsInfo cbsInfo = getCbsInfo();
                    if (cbsInfo!=null)
                        cbsInfo.save(document.getRootElement().addElement("cbsInfo"));
                    (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(document);
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
    
    public boolean restore(File folder, String puid) {
        return restore(folder, puid, false);
    }
    
    public boolean restore(File folder, String puid, boolean removeFiles) {
        sLog.debug("restore(folder="+folder+","+puid+",exam)");
        iCbsInfo = null;
        File inXmlFile = new File(folder,"exam_"+puid+BackupFileFilter.sXmlExtension);
        File inPropertiesFile = new File(folder,"exam_"+puid+BackupFileFilter.sPropertiesExtension);
        
        ExamModel model = null;
        try {
            if (isRunning()) stopSolver();
            disposeNoInherit(false);
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

            Document document = (new SAXReader()).read(inXmlFile); 
            model.load(document, currentSolution().getAssignment(), new Callback() {
                public void execute() {
                    saveBest();
                }
            });
            if (document.getRootElement().element("cbsInfo")!=null) {
                iCbsInfo = new ExamConflictStatisticsInfo();
                iCbsInfo.load(document.getRootElement().element("cbsInfo"));
            }
            
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
            for (Exam exam: currentSolution().getModel().variables()) {
            	currentSolution().getAssignment().unassign(0, exam);
            }
            currentSolution().clearBest();
        }    
    }
    
    public Collection<ExamAssignmentInfo> getAssignedExams() {
        synchronized (currentSolution()) {
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (Exam exam: currentSolution().getModel().variables()) {
            	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
            	if (placement != null)
                    ret.add(new ExamAssignmentInfo(placement, currentSolution().getAssignment()));
            }
            return ret;
        }
    }
    public Collection<ExamInfo> getUnassignedExams() {
        synchronized (currentSolution()) {
            Vector<ExamInfo> ret = new Vector<ExamInfo>();
            for (Exam exam: currentSolution().getModel().variables()) {
            	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
            	if (placement == null)
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
            for (Exam exam: currentSolution().getModel().variables()) {
                boolean hasSubjectArea = false;
                for (Iterator<ExamOwner> f=exam.getOwners().iterator();!hasSubjectArea && f.hasNext();) {
                    ExamOwner ecs = (ExamOwner)f.next();
                    hasSubjectArea = ecs.getName().startsWith(sa);
                }
                if (hasSubjectArea) {
                	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
                	if (placement!=null)
                		ret.add(new ExamAssignmentInfo(placement, currentSolution().getAssignment()));
                }
            }
            return ret;
        }
    }
    public Collection<ExamInfo> getUnassignedExams(Long subjectAreaId) {
        if (subjectAreaId==null || subjectAreaId<0) return getUnassignedExams();
        String sa = new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ";
        synchronized (currentSolution()) {
            Vector<ExamInfo> ret = new Vector<ExamInfo>();
            for (Exam exam: currentSolution().getModel().variables()) {
                boolean hasSubjectArea = false;
                for (Iterator<ExamOwner> f=exam.getOwners().iterator();!hasSubjectArea && f.hasNext();) {
                    ExamOwner ecs = f.next();
                    hasSubjectArea = ecs.getName().startsWith(sa);
                }
                if (hasSubjectArea) {
                	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
                	if (placement==null)
                		ret.add(new ExamInfo(exam));
                }
            }
            return ret;
        }
    }
    
    public Collection<ExamAssignmentInfo> getAssignedExamsOfRoom(Long roomId) {
        synchronized (currentSolution()) {
            ExamRoom room = null;
            for (ExamRoom r: ((ExamModel)currentSolution().getModel()).getRooms()) {
                if (r.getId()==roomId) {
                    room = r; break;
                }
            }
            if (room==null) return null;
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (ExamPeriod period: ((ExamModel)currentSolution().getModel()).getPeriods()) {
                for (ExamPlacement placement: room.getPlacements(currentSolution().getAssignment(), period)) {
                	ret.add(new ExamAssignmentInfo(placement, currentSolution().getAssignment()));
                }
            }
            return ret;
        }
    }

    public Collection<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId) {
        synchronized (currentSolution()) {
            ExamInstructor instructor = null;
            for (ExamInstructor i: ((ExamModel)currentSolution().getModel()).getInstructors()) {
                if (i.getId()==instructorId) {
                    instructor = i; break;
                }
            }
            if (instructor==null) return null;
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (ExamPeriod period: ((ExamModel)currentSolution().getModel()).getPeriods()) {
                Set exams = instructor.getExams(currentSolution().getAssignment(), period);
                if (exams!=null)
                    for (Iterator i=exams.iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        ret.add(new ExamAssignmentInfo(currentSolution().getAssignment().getValue(exam), currentSolution().getAssignment()));                        
                    }
            }
            return ret;
        }
    }
    
    public Long getExamTypeId() {
    	return getProperties().getPropertyLong("Exam.Type", null);
    }
    
    public Collection<ExamAssignmentInfo[]> getChangesToInitial(Long subjectAreaId) {
        String sa = (subjectAreaId!=null && subjectAreaId>=0 ? new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ":null);
        Vector<ExamAssignmentInfo[]> changes = new Vector<ExamAssignmentInfo[]>();
        synchronized (currentSolution()) {
            for (Exam exam: currentSolution().getModel().variables()) {
                if (sa!=null) {
                    boolean hasSubjectArea = false;
                    for (Iterator<ExamOwner> f=exam.getOwners().iterator();!hasSubjectArea && f.hasNext();) {
                        ExamOwner ecs = f.next();
                        hasSubjectArea = ecs.getName().startsWith(sa);
                    }
                    if (!hasSubjectArea) continue;
                }
                if (!ToolBox.equals(exam.getInitialAssignment(),currentSolution().getAssignment().getValue(exam))) {
                    changes.add(new ExamAssignmentInfo[] {
                            new ExamAssignmentInfo(exam,exam.getInitialAssignment(), currentSolution().getAssignment()),
                            new ExamAssignmentInfo(exam,currentSolution().getAssignment().getValue(exam), currentSolution().getAssignment())});
                }
            }
        }
        return changes;
    }
    
    public Collection<ExamAssignmentInfo[]> getChangesToBest(Long subjectAreaId) {
        String sa = (subjectAreaId!=null && subjectAreaId>=0 ? new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ":null);
        Vector<ExamAssignmentInfo[]> changes = new Vector<ExamAssignmentInfo[]>();
        synchronized (currentSolution()) {
            for (Exam exam: currentSolution().getModel().variables()) {
                if (sa!=null) {
                    boolean hasSubjectArea = false;
                    for (Iterator<ExamOwner> f=exam.getOwners().iterator();!hasSubjectArea && f.hasNext();) {
                        ExamOwner ecs = f.next();
                        hasSubjectArea = ecs.getName().startsWith(sa);
                    }
                    if (!hasSubjectArea) continue;
                }
                if (!ToolBox.equals(exam.getBestAssignment(),currentSolution().getAssignment().getValue(exam))) {
                    changes.add(new ExamAssignmentInfo[] {
                            new ExamAssignmentInfo(exam,exam.getBestAssignment(),currentSolution().getAssignment()),
                            new ExamAssignmentInfo(exam,currentSolution().getAssignment().getValue(exam),currentSolution().getAssignment())});
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
        for (Extension ext: getExtensions()) {
            if (ext instanceof ConflictStatistics) {
                cbs = (ConflictStatistics)ext;
                break;
            }
        }
        if (cbs==null || cbs.getNoGoods().isEmpty()) {
            if (iCbsInfo!=null) return iCbsInfo;
            return null;
        }
        ExamConflictStatisticsInfo info = new ExamConflictStatisticsInfo();
        synchronized (currentSolution()) {
            info.load(cbs);
        }
        return info; 
    }
    
    public ExamConflictStatisticsInfo getCbsInfo(Long examId) {
        ConflictStatistics cbs = null;
        for (Extension ext: getExtensions()) {
            if (ext instanceof ConflictStatistics) {
                cbs = (ConflictStatistics)ext;
                break;
            }
        }
        if (cbs==null || cbs.getNoGoods().isEmpty()) {
            if (iCbsInfo!=null) return iCbsInfo;
            return null;
        }
        ExamConflictStatisticsInfo info = new ExamConflictStatisticsInfo();
        synchronized (currentSolution()) {
            info.load(cbs, examId);
        }
        return info; 
    }
    
    public ExamProposedChange update(ExamProposedChange change) {
        synchronized (currentSolution()) {
            Hashtable<Exam, ExamPlacement> undoAssign = new Hashtable();
            HashSet<Exam> undoUnassing = new HashSet();
            Vector<Exam> unassign = new Vector();
            Vector<ExamPlacement> assign = new Vector();
            HashSet<ExamPlacement> conflicts = new HashSet();
            /*
            for (ExamAssignment assignment: change.getConflicts()) {
                ExamPlacement placement = getPlacement(assignment);
                if (placement==null || !placement.equals(placement.variable().getAssignment())) return null;
                undoAssign.put((Exam)placement.variable(),placement);
                unassign.add((Exam)placement.variable());
            }
            */
            for (ExamAssignment assignment: change.getAssignments()) {
                ExamPlacement placement = getPlacement(assignment);
                if (placement==null) return null;
                if (currentSolution().getAssignment().getValue(placement.variable())!=null)
                    undoAssign.put((Exam)placement.variable(), currentSolution().getAssignment().getValue(placement.variable()));
                else
                    undoUnassing.add((Exam)placement.variable());
                assign.add(placement);
                unassign.add((Exam)placement.variable());
            }
            for (Exam exam : unassign) currentSolution().getAssignment().unassign(0, exam);
            for (ExamPlacement placement : assign) {
                for (Iterator i=placement.variable().getModel().conflictValues(currentSolution().getAssignment(), placement).iterator();i.hasNext();) {
                    ExamPlacement conflict = (ExamPlacement)i.next();
                    Exam conflictingExam = (Exam)conflict.variable();
                    if (!undoAssign.containsKey(conflictingExam) && !undoUnassing.contains(conflictingExam)) 
                        undoAssign.put(conflictingExam,conflict);
                    conflicts.add(conflict);
                    currentSolution().getAssignment().unassign(0, conflict.variable());
                }
                currentSolution().getAssignment().assign(0, placement);
            }
            change.getAssignments().clear();
            change.getConflicts().clear();
            for (ExamPlacement assignment : assign)
                if (!conflicts.contains(assignment)) 
                    change.getAssignments().add(new ExamAssignmentInfo(assignment, currentSolution().getAssignment()));
            for (Exam exam: undoUnassing)
                if (currentSolution().getAssignment().getValue(exam)!=null) currentSolution().getAssignment().unassign(0, exam);
            for (Map.Entry<Exam, ExamPlacement> entry : undoAssign.entrySet())
            	currentSolution().getAssignment().unassign(0, entry.getKey());
            for (Map.Entry<Exam, ExamPlacement> entry : undoAssign.entrySet())
            	currentSolution().getAssignment().assign(0, entry.getValue());
            for (ExamPlacement conflict : conflicts) {
                ExamPlacement original = undoAssign.get((Exam)conflict.variable());
                change.getConflicts().add(new ExamAssignment(original==null?conflict:original, currentSolution().getAssignment()));
            }
            return change;            
        }
    }
    
    public Vector<ExamRoomInfo> getRooms(long examId, long periodId, ExamProposedChange change, int minRoomSize, int maxRoomSize, String filter, boolean allowConflicts) {
        synchronized (currentSolution()) {
            //lookup exam, period etc.
            Exam exam = getExam(examId);
            if (exam==null) return null;
            ExamPeriodPlacement period = null;
            for (ExamPeriodPlacement p: exam.getPeriodPlacements()) {
                if (periodId==p.getId()) { period = p; break; }
            }
            if (period==null) return null;
            Vector<ExamRoomInfo> rooms = new Vector<ExamRoomInfo>();
            if (exam.getMaxRooms()==0) return rooms;
            
            //assign change
            Hashtable<Exam, ExamPlacement> undoAssign = new Hashtable();
            HashSet<Exam> undoUnassing = new HashSet();
            if (change!=null) {
                for (ExamAssignment assignment: change.getConflicts()) {
                    ExamPlacement placement = getPlacement(assignment);
                    if (placement==null) continue;
                    undoAssign.put((Exam)placement.variable(),placement);
                    currentSolution().getAssignment().unassign(0, placement.variable());
                }
                for (ExamAssignment assignment: change.getAssignments()) {
                    ExamPlacement placement = getPlacement(assignment);
                    if (placement==null) continue;
                    for (Iterator i=placement.variable().getModel().conflictValues(currentSolution().getAssignment(), placement).iterator();i.hasNext();) {
                        ExamPlacement conflict = (ExamPlacement)i.next();
                        if (conflict.variable().equals(placement.variable())) continue;
                        Exam conflictingExam = (Exam)conflict.variable();
                        if (!undoAssign.containsKey(conflictingExam) && !undoUnassing.contains(conflictingExam)) 
                            undoAssign.put(conflictingExam,conflict);
                        currentSolution().getAssignment().unassign(0, conflict.variable());
                    }
                    if (currentSolution().getAssignment().getValue(placement.variable())!=null)
                        undoAssign.put((Exam)placement.variable(), currentSolution().getAssignment().getValue(placement.variable()));
                    else
                        undoUnassing.add((Exam)placement.variable());
                    currentSolution().getAssignment().assign(0, placement);
                }
            }
            
            ExamRoomSharing sharing = ((ExamModel)currentSolution().getModel()).getRoomSharing();
            
            //compute rooms
            for (ExamRoomPlacement room: exam.getRoomPlacements()) {
                
                int cap = room.getSize(exam.hasAltSeating());
                if (minRoomSize>=0 && cap<minRoomSize) continue;
                if (maxRoomSize>=0 && cap>maxRoomSize) continue;
                if (!ExamInfoModel.match(room.getName(), filter)) continue;
                if (!room.isAvailable(period.getPeriod())) continue;
                
                boolean conf = !exam.checkDistributionConstraints(currentSolution().getAssignment(), room);
                if (sharing == null) {
                	for (ExamPlacement p: room.getRoom().getPlacements(currentSolution().getAssignment(), period.getPeriod()))
                		if (!p.variable().equals(exam)) conf = true;
                } else {
                	if (sharing.inConflict(exam, room.getRoom().getPlacements(currentSolution().getAssignment(), period.getPeriod()), room.getRoom()))
                		conf = true;
                }

                if (!allowConflicts && conf) continue;
                
                rooms.add(new ExamRoomInfo(room.getRoom(), (conf?100:0) + room.getPenalty(period.getPeriod())));
            }
            
            //undo change
            for (Exam undoExam: undoUnassing)
                if (currentSolution().getAssignment().getValue(undoExam)!=null) currentSolution().getAssignment().unassign(0, undoExam);
            for (Map.Entry<Exam, ExamPlacement> entry : undoAssign.entrySet())
            	currentSolution().getAssignment().unassign(0, entry.getKey());
            for (Map.Entry<Exam, ExamPlacement> entry : undoAssign.entrySet())
            	currentSolution().getAssignment().assign(0, entry.getValue());

            return rooms;
        }        
    }
    
    public Collection<ExamAssignmentInfo> getPeriods(long examId, ExamProposedChange change) {
        synchronized (currentSolution()) {
            //lookup exam
            Exam exam = getExam(examId);
            if (exam==null) return null;
            
            //assign change
            Hashtable<Exam, ExamPlacement> undoAssign = new Hashtable();
            HashSet<Exam> undoUnassing = new HashSet();
            if (change!=null) {
                for (ExamAssignment assignment: change.getConflicts()) {
                    ExamPlacement placement = getPlacement(assignment);
                    if (placement==null) continue;
                    undoAssign.put((Exam)placement.variable(),placement);
                    currentSolution().getAssignment().unassign(0, placement.variable());
                }
                for (ExamAssignment assignment: change.getAssignments()) {
                    ExamPlacement placement = getPlacement(assignment);
                    if (placement==null) continue;
                    for (Iterator i=placement.variable().getModel().conflictValues(currentSolution().getAssignment(), placement).iterator();i.hasNext();) {
                        ExamPlacement conflict = (ExamPlacement)i.next();
                        if (conflict.variable().equals(placement.variable())) continue;
                        Exam conflictingExam = (Exam)conflict.variable();
                        if (!undoAssign.containsKey(conflictingExam) && !undoUnassing.contains(conflictingExam)) 
                            undoAssign.put(conflictingExam,conflict);
                        currentSolution().getAssignment().unassign(0, conflict.variable());
                    }
                    if (currentSolution().getAssignment().getValue(placement.variable())!=null)
                        undoAssign.put((Exam)placement.variable(),currentSolution().getAssignment().getValue(placement.variable()));
                    else
                        undoUnassing.add((Exam)placement.variable());
                    currentSolution().getAssignment().assign(0, placement);
                }
            }

            Vector<ExamAssignmentInfo> periods = new Vector<ExamAssignmentInfo>();
            for (ExamPeriodPlacement period: exam.getPeriodPlacements()) {
                Set rooms = exam.findBestAvailableRooms(currentSolution().getAssignment(), period);
                if (rooms==null) rooms = new HashSet();
                boolean conf = !exam.checkDistributionConstraints(currentSolution().getAssignment(), period);
                ExamAssignmentInfo assignment = new ExamAssignmentInfo(new ExamPlacement(exam, period, rooms), currentSolution().getAssignment());
                if (conf) assignment.setPeriodPref("P");
                periods.add(assignment);
            }
            
            //undo change
            for (Exam undoExam: undoUnassing)
                if (currentSolution().getAssignment().getValue(undoExam)!=null) currentSolution().getAssignment().unassign(0, undoExam);
            for (Map.Entry<Exam, ExamPlacement> entry : undoAssign.entrySet())
            	currentSolution().getAssignment().unassign(0, entry.getKey());
            for (Map.Entry<Exam, ExamPlacement> entry : undoAssign.entrySet())
            	currentSolution().getAssignment().assign(0, entry.getValue());
            
            return periods;
        }
    }
    
    public ExamSuggestionsInfo getSuggestions(long examId, ExamProposedChange change, String filter, int depth, int limit, long timeOut) {
        synchronized (currentSolution()) {
            Exam exam = getExam(examId);
            if (exam==null) return null;
            ExamSuggestions s = new ExamSuggestions(this);
            s.setDepth(depth);
            s.setFilter(filter);
            s.setLimit(limit);
            s.setTimeOut(timeOut);
            TreeSet<ExamProposedChange> suggestions = s.computeSuggestions(exam, (change==null?null:change.getAssignments()));
            String message = null;
            if (s.wasTimeoutReached()) {
                message = "("+(timeOut/1000l)+"s timeout reached, "+s.getNrCombinationsConsidered()+" possibilities up to "+depth+" changes were considered, ";
            } else {
                message = "(all "+s.getNrCombinationsConsidered()+" possibilities up to "+depth+" changes were considered, ";
            }
            if (suggestions.isEmpty()) {
                message += "no suggestion found)";
            } else if (s.getNrSolutions()>suggestions.size()) {
                message += "top "+suggestions.size()+" of "+s.getNrSolutions()+" suggestions displayed)";
            } else {
                message += suggestions.size()+" suggestions displayed)";
            }
            return new ExamSuggestionsInfo(suggestions, message, s.wasTimeoutReached());
        }
    }
    
    protected void autoConfigure() {
        super.autoConfigure();
        setPerturbationsCounter(null);
    }
    
    public TreeSet<ExamAssignment> getExamsOfRoom(long locationId) {
        synchronized (currentSolution()) {
            ExamModel model = (ExamModel)currentSolution().getModel();
            ExamRoom room = null;
            for (ExamRoom r: model.getRooms()) {
                if (r.getId()==locationId) { room = r; break; }
            }
            if (room==null) return null;
            TreeSet<ExamAssignment> ret = new TreeSet();
            for (ExamPeriod period: model.getPeriods()) {
                for (ExamPlacement placement: room.getPlacements(currentSolution().getAssignment(), period))
                	ret.add(new ExamAssignment(placement, currentSolution().getAssignment()));
            }
            return ret;
        }
    }
    
    public void stopSolverImmediately() {
        super.stopSolver();
    }
    
    public void stopSolver() {
        if (getNeighbourSelection() instanceof ExamNeighbourSelection) {
            ExamNeighbourSelection xn = (ExamNeighbourSelection)getNeighbourSelection();
            if (xn.isFinalPhase()) stopSolverImmediately();
            else xn.setFinalPhase(new Callback() {
                public void execute() { iStop=true; }
            });
        } else stopSolverImmediately();
    }
    
    public Solution<Exam, ExamPlacement> currentSolution() {
        activateIfNeeded();
        return super.currentSolution();
    }
    
    public void start() {
        activateIfNeeded();
        iCbsInfo = null;
        super.start();
    }
    
    public synchronized boolean isPassivated() {
        return iIsPassivated;
    }
    
    public synchronized long timeFromLastUsed() {
        return System.currentTimeMillis()-iLastTimeStamp;
    }
    
    public synchronized boolean activateIfNeeded() {
        iLastTimeStamp = System.currentTimeMillis();
        if (!isPassivated()) return false;
        sLog.debug("<activate "+iPassivationPuid+">");

        iIsPassivated = false;

        System.gc();
        sLog.debug(" -- memory usage before activation:"+org.unitime.commons.Debug.getMem());
        restore(iPassivationFolder, iPassivationPuid, true);
        System.gc();
        sLog.debug(" -- memory usage after activation:"+org.unitime.commons.Debug.getMem());
        
        return true;
    }
    
    public synchronized boolean passivate(File folder, String puid) {
        if (isPassivated() || super.currentSolution()==null || super.currentSolution().getModel()==null) return false;
        sLog.debug("<passivate "+puid+">");
        System.gc();
        sLog.debug(" -- memory usage before passivation:"+org.unitime.commons.Debug.getMem());
        iProgressBeforePassivation = getProgress();
        if (iProgressBeforePassivation!=null)
            iProgressBeforePassivation.put("STATUS","Pasivated");
        iCurrentSolutionInfoBeforePassivation = currentSolutionInfo();
        iBestSolutionInfoBeforePassivation = bestSolutionInfo();
        
        iPassivationFolder = folder;
        iPassivationPuid = puid;
        backup(iPassivationFolder, iPassivationPuid);

        disposeNoInherit(false);
        
        System.gc();
        sLog.debug(" -- memory usage after passivation:"+org.unitime.commons.Debug.getMem());
        
        iIsPassivated = true;
        return true;
    }

    public synchronized boolean passivateIfNeeded(File folder, String puid) {
		long inactiveTimeToPassivate = 60000l * ApplicationProperty.SolverPasivationTime.intValue();
		if (isPassivated() || inactiveTimeToPassivate <= 0 || timeFromLastUsed() < inactiveTimeToPassivate || isWorking()) return false;
        return passivate(folder, puid);
    }
    
    public Date getLastUsed() {
        return new Date(iLastTimeStamp);
    }
    
    public void interrupt() {
    	try {
            if (iSolverThread != null) {
                iStop = true;
                if (iSolverThread.isAlive() && !iSolverThread.isInterrupted())
                	iSolverThread.interrupt();
            }
			if (iWorkThread != null && iWorkThread.isAlive() && !iWorkThread.isInterrupted()) {
				iWorkThread.interrupt();
			}
    	} catch (Exception e) {
    		sLog.error("Unable to interrupt the solver, reason: " + e.getMessage(), e);
    	}
    }

    public Map<String,String> statusSolutionInfo() {
    	if (isPassivated())
    		return (iBestSolutionInfoBeforePassivation == null ? iCurrentSolutionInfoBeforePassivation : iBestSolutionInfoBeforePassivation);
    	synchronized (super.currentSolution()) {
    		Map<String,String> info = super.currentSolution().getBestInfo();
    		try {
    			Solution<Exam, ExamPlacement> solution = getWorkingSolution();
    			if (info == null || getSolutionComparator().isBetterThanBestSolution(solution))
    				info = solution.getModel().getInfo(solution.getAssignment());
    		} catch (ConcurrentModificationException e) {}
    		return info;
    	}
    }
    
    public byte[] exportXml() throws Exception {
        synchronized (currentSolution()) {
            boolean anonymize = ApplicationProperty.SolverXMLExportNames.isFalse();
            boolean idconv = ApplicationProperty.SolverXMLExportConvertIds.isTrue();

            if (anonymize) {
                getProperties().setProperty("Xml.Anonymize", "true");
                getProperties().setProperty("Xml.ShowNames", "false");
                getProperties().setProperty("Xml.ConvertIds", idconv ? "true" : "false");
                getProperties().setProperty("Xml.Anonymize", "true");
                getProperties().setProperty("Xml.SaveInitial", "false");
                getProperties().setProperty("Xml.SaveBest", "false");
                getProperties().setProperty("Xml.SaveSolution", "true");
        	}

            ByteArrayOutputStream ret = new ByteArrayOutputStream();
            
            Document document = ((ExamModel)currentSolution().getModel()).save(currentSolution().getAssignment());
            
            if (anonymize) {
            	Element log = document.getRootElement().element("log");
            	if (log != null)
            		document.getRootElement().remove(log);
            	Element notavailable = document.getRootElement().element("notavailable");
            	if (notavailable != null)
            		document.getRootElement().remove(notavailable);
            }
            
            (new XMLWriter(ret, OutputFormat.createPrettyPrint())).write(document);
            
            ret.flush(); ret.close();
            
            if (anonymize) {
                getProperties().setProperty("Xml.Anonymize", "false");
                getProperties().setProperty("Xml.ConvertIds", "false");
                getProperties().setProperty("Xml.ShowNames", "true");
                getProperties().remove("Xml.SaveInitial");
                getProperties().remove("Xml.SaveBest");
                getProperties().remove("Xml.SaveSolution");
            }

            return ret.toByteArray();
        }
    }
}
