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
package org.unitime.timetable.solver.studentsct;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Callback;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressWriter;
import net.sf.cpsolver.studentsct.StudentSectioningLoader;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.StudentSectioningSaver;
import net.sf.cpsolver.studentsct.StudentSectioningXMLLoader;
import net.sf.cpsolver.studentsct.StudentSectioningXMLSaver;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

/**
 * @author Tomas Muller
 */
public class StudentSolver extends Solver implements StudentSolverProxy {
    private static Log sLog = LogFactory.getLog(StudentSolver.class);
    private int iDebugLevel = Progress.MSGLEVEL_INFO;
    private boolean iWorking = false;
    private Date iLoadedDate = null;
    private StudentSolverDisposeListener iDisposeListener = null;
    
    private long iLastTimeStamp = System.currentTimeMillis();
    private boolean iIsPassivated = false;
    private Map iProgressBeforePassivation = null;
    private Hashtable iCurrentSolutionInfoBeforePassivation = null;
    private Hashtable iBestSolutionInfoBeforePassivation = null;
    private File iPassivationFolder = null;
    private String iPassivationPuid = null;

    
    public StudentSolver(DataProperties properties, StudentSolverDisposeListener disposeListener) {
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
            ret.put("VERSION", Constants.VERSION+"."+Constants.BLD_NUMBER);
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
        setInitalSolution((net.sf.cpsolver.ifs.solution.Solution)null);
        if (unregister && iDisposeListener!=null) iDisposeListener.onDispose();
    }
    
    public String getHost() {
        return "local";
    }
    
    public String getHostLabel() {
        return getHost();
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
    
    public Hashtable currentSolutionInfo() {
        if (isPassivated()) return iCurrentSolutionInfoBeforePassivation;
        synchronized (super.currentSolution()) {
            return super.currentSolution().getInfo();
        }
    }

    public Hashtable bestSolutionInfo() {
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
                StudentSectioningSaver saver = new StudentSectioningDatabaseSaver(this);
                synchronized (currentSolution()) {
                    try {
                        saver.save();
                    } catch (Exception e) {
                        Progress.getInstance(currentSolution().getModel()).error(e.getMessage(),e);
                    }
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
        StudentSectioningSaver saver = new StudentSectioningDatabaseSaver(this);
        saver.setCallback(getSavingDoneCallback());
        Thread thread = new Thread(saver);
        thread.setPriority(THREAD_PRIORITY);
        thread.start();
    }
    
    public void load(DataProperties properties) {
        setProperties(properties);
        StudentSectioningModel model = new StudentSectioningModel(getProperties());
        Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
        
        iWorking = true;
        setInitalSolution(model);
        initSolver();
        
        StudentSectioningLoader loader = new StudentSectioningDatabaseLoader(model);
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
        StudentSectioningModel model = new StudentSectioningModel(getProperties());
        
        iWorking = true;
        Progress.changeInstance(currentSolution().getModel(),model);
        setInitalSolution(model);
        initSolver();
        
        StudentSectioningLoader loader = new StudentSectioningDatabaseLoader(model);
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
            for (Request request: currentSolution().getModel().variables()) {
                if (request.getAssignment()!=null)
                    iCurrentAssignmentTable.put(request.getId(),request.getAssignment());
                if (request.getBestAssignment()!=null)
                    iBestAssignmentTable.put(request.getId(),request.getBestAssignment());
                if (request.getInitialAssignment()!=null)
                    iInitialAssignmentTable.put(request.getId(),request.getInitialAssignment());
            }
        }
        private Request getRequest(long id) {
            for (Request request: currentSolution().getModel().variables()) {
                if (request.getId()==id) return request;
            }
            return null;
        }
        
        private Enrollment getEnrollment(Request request, Enrollment enrollment) {
            if (request instanceof FreeTimeRequest) {
                return ((FreeTimeRequest)request).createEnrollment();
            } else {
                CourseRequest cr = (CourseRequest)request;
                Set sections = new HashSet();
                for (Iterator i=enrollment.getAssignments().iterator();i.hasNext();) {
                    Section s = (Section)i.next();
                    Section section = cr.getSection(s.getId());
                    if (section==null) {
                        iProgress.warn("WARNING: Section "+s.getName()+" is not available for "+cr.getName());
                        return null;
                    }
                    sections.add(section);
                }
                return cr.createEnrollment(sections);
            }
        }
        private void assign(Enrollment enrollment) {
            Hashtable conflictConstraints = currentSolution().getModel().conflictConstraints(enrollment);
            if (conflictConstraints.isEmpty()) {
                enrollment.variable().assign(0,enrollment);
            } else {
                iProgress.warn("Unable to assign "+enrollment.variable().getName()+" := "+enrollment.getName());
                iProgress.warn("&nbsp;&nbsp;Reason:");
                for (Enumeration ex=conflictConstraints.keys();ex.hasMoreElements();) {
                    Constraint c = (Constraint)ex.nextElement();
                    Collection vals = (Collection)conflictConstraints.get(c);
                    for (Iterator j=vals.iterator();j.hasNext();) {
                        Enrollment enrl = (Enrollment) j.next();
                        iProgress.warn("&nbsp;&nbsp;&nbsp;&nbsp;"+enrl.getRequest().getName()+" = "+enrl.getName());
                    }
                    iProgress.debug("&nbsp;&nbsp;&nbsp;&nbsp;in constraint "+c);
                }
            }
        }
        private void unassignAll() {
            for (Request request: currentSolution().getModel().variables()) {
                if (request.getAssignment()!=null) request.unassign(0);
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
                    Request request = getRequest((Long)entry.getKey()); 
                    if (request==null) continue;
                    Enrollment enrollment = getEnrollment(request,(Enrollment)entry.getValue());
                    if (enrollment!=null) assign(enrollment);
                }
                
                currentSolution().saveBest();
            }
            if (!iInitialAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating initial assignment ...",iInitialAssignmentTable.size());
                for (Iterator i=iInitialAssignmentTable.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    iProgress.incProgress();
                    Request request = getRequest((Long)entry.getKey()); 
                    if (request==null) continue;
                    Enrollment enrollment = getEnrollment(request,(Enrollment)entry.getValue());
                    if (enrollment!=null) request.setInitialAssignment(enrollment);
                }
            }
            if (!iCurrentAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating current assignment ...",iCurrentAssignmentTable.size());
                unassignAll();
                for (Iterator i=iCurrentAssignmentTable.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    iProgress.incProgress();
                    Request request = getRequest((Long)entry.getKey()); 
                    if (request==null) continue;
                    Enrollment enrollment = getEnrollment(request,(Enrollment)entry.getValue());
                    if (enrollment!=null) assign(enrollment);
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
    
    public static interface StudentSolverDisposeListener {
        public void onDispose();
    }
    
    public boolean backup(File folder, String puid) {
        folder.mkdirs();
        if (currentSolution()==null) return false;
        synchronized (currentSolution()) {
            getProperties().setProperty("Xml.SaveBest", "true");
            getProperties().setProperty("Xml.SaveInitial", "true");
            getProperties().setProperty("Xml.SaveCurrent", "true");
            File outXmlFile = new File(folder,"sct_"+puid+BackupFileFilter.sXmlExtension);
            File outPropertiesFile = new File(folder,"sct_"+puid+BackupFileFilter.sPropertiesExtension);
            try {
                new StudentSectioningXMLSaver(this).save(outXmlFile);
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
                FileOutputStream fos = null;
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
        sLog.debug("restore(folder="+folder+","+puid+",sct)");
        File inXmlFile = new File(folder,"sct_"+puid+BackupFileFilter.sXmlExtension);
        File inPropertiesFile = new File(folder,"sct_"+puid+BackupFileFilter.sPropertiesExtension);
        
        StudentSectioningModel model = null;
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
            
            model = new StudentSectioningModel(getProperties());
            Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
            setInitalSolution(model);
            initSolver();
            
            getProperties().setProperty("Xml.LoadBest", "true");
            getProperties().setProperty("Xml.LoadInitial", "true");
            getProperties().setProperty("Xml.LoadCurrent", "true");

            StudentSectioningXMLLoader loader = new StudentSectioningXMLLoader(model);
            loader.setInputFile(inXmlFile);
            loader.setCallback(new Callback() {
                public void execute() {
                    saveBest();
                }
            });
            loader.load();
            
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
            for (Request request: currentSolution().getModel().variables()) {
                if (request.getAssignment()!=null) request.unassign(0);
            }
            currentSolution().clearBest();
        }    
    }
    
    public Long getSessionId() {
        return getProperties().getPropertyLong("General.SessionId",null);
    }
    
    public Solution<Request, Enrollment> currentSolution() {
        activateIfNeeded();
        return super.currentSolution();
    }
    
    public void start() {
        activateIfNeeded();
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
		long inactiveTimeToPassivate = Long.parseLong(ApplicationProperties.getProperty("unitime.solver.passivation.time", "30")) * 60000l;
		if (isPassivated() || inactiveTimeToPassivate <= 0 || timeFromLastUsed() < inactiveTimeToPassivate || isWorking()) return false;
        return passivate(folder, puid);
    }
    
    public Date getLastUsed() {
        return new Date(iLastTimeStamp);
    }
}
