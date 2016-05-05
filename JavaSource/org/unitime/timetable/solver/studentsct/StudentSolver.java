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
package org.unitime.timetable.solver.studentsct;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.ParallelSolver;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.cpsolver.studentsct.StudentSectioningLoader;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.StudentSectioningSaver;
import org.cpsolver.studentsct.StudentSectioningXMLLoader;
import org.cpsolver.studentsct.StudentSectioningXMLSaver;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.online.expectations.NeverOverExpected;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.cpsolver.studentsct.report.SectionConflictTable;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.match.StudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.MemoryCounter;


/**
 * @author Tomas Muller
 */
public class StudentSolver extends ParallelSolver<Request, Enrollment> implements StudentSolverProxy {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private static Log sLog = LogFactory.getLog(StudentSolver.class);
    private int iDebugLevel = Progress.MSGLEVEL_INFO;
    private boolean iWorking = false;
    private Date iLoadedDate = null;
    private StudentSolverDisposeListener iDisposeListener = null;
    
    private long iLastTimeStamp = System.currentTimeMillis();
    private boolean iIsPassivated = false;
    private Map iProgressBeforePassivation = null;
    private Map<String, String> iCurrentSolutionInfoBeforePassivation = null;
    private Map<String, String> iBestSolutionInfoBeforePassivation = null;
    private File iPassivationFolder = null;
    private String iPassivationPuid = null;
    private Thread iWorkThread = null;

    private transient Map<Long, XCourse> iCourseInfoCache = null;
    private Map<String, Object> iOnlineProperties = new HashMap<String, Object>();
    
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
        if (currentSolution().getBestInfo() != null)
        	currentSolution().getBestInfo().putAll(currentSolution().getModel().getExtendedInfo(currentSolution().getAssignment()));
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
        clearCourseInfoTable();
    }
    
    public String getHost() {
        return "local";
    }
    
    public String getUser() {
    	return getProperties().getProperty("General.OwnerPuid");
    }
 
    public Object exec(Object[] cmd) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Class[] types = new Class[(cmd.length-3)/2];
        Object[] args = new Object[(cmd.length-3)/2];
        for (int i=0;i<types.length;i++) {
            types[i]=(Class)cmd[2*i+3];
            args[i]=cmd[2*i+4];
        }
        
        return getClass().getMethod((String)cmd[0],types).invoke(this, args);
    }
    
    public Map<String,String> currentSolutionInfo() {
        if (isPassivated()) return iCurrentSolutionInfoBeforePassivation;
        java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            return super.currentSolution().getExtendedInfo();
        } finally {
        	lock.unlock();
        }
    }

    public Map<String,String> bestSolutionInfo() {
        if (isPassivated()) return iBestSolutionInfoBeforePassivation;
        java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            return super.currentSolution().getBestInfo();
        } finally {
        	lock.unlock();
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
                java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
                lock.lock();
                try {
                    saver.save();
                } catch (Exception e) {
                    Progress.getInstance(currentSolution().getModel()).error(e.getMessage(),e);
                } finally {
                	lock.unlock();
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
        iWorkThread = new Thread(saver);
        iWorkThread.setPriority(THREAD_PRIORITY);
        iWorkThread.start();
    }
    
    public void load(DataProperties properties) {
        setProperties(properties);
        StudentSectioningModel model = new StudentSectioningModel(getProperties());
        Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
        
        iWorking = true;
        setInitalSolution(new Solution(model, new DefaultSingleAssignment<Request, Enrollment>()));
        initSolver();
        
        StudentSectioningLoader loader = new StudentSectioningDatabaseLoader(model, currentSolution().getAssignment());
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
        StudentSectioningModel model = new StudentSectioningModel(getProperties());
        
        iWorking = true;
        Progress.changeInstance(currentSolution().getModel(),model);
        setInitalSolution(new Solution(model, new DefaultSingleAssignment<Request, Enrollment>()));
        initSolver();
        
        StudentSectioningLoader loader = new StudentSectioningDatabaseLoader(model, currentSolution().getAssignment());
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
        Map<Long, Map<Long, Enrollment>> iCurrentAssignmentTable = new Hashtable<Long, Map<Long,Enrollment>>();
        Map<Long, Map<Long, Enrollment>> iBestAssignmentTable = new Hashtable<Long, Map<Long,Enrollment>>();
        Map<Long, Map<Long, Enrollment>> iInitialAssignmentTable = new Hashtable<Long, Map<Long,Enrollment>>();
        String iSolutionId = null;
        Progress iProgress = null;
        
        public ReloadingDoneCallback() {
            iSolutionId = getProperties().getProperty("General.SolutionId");
            for (Request request: currentSolution().getModel().variables()) {
            	Enrollment enrollment = currentSolution().getAssignment().getValue(request);
            	if (enrollment != null) {
                	Map<Long, Enrollment> assignments = iCurrentAssignmentTable.get(request.getStudent().getId());
                	if (assignments == null) {
                		assignments = new Hashtable<Long, Enrollment>();
                		iCurrentAssignmentTable.put(request.getStudent().getId(), assignments);
                	}
                	assignments.put(request.getId(), enrollment);
            	}
            	if (request.getBestAssignment() != null) {
                	Map<Long, Enrollment> assignments = iBestAssignmentTable.get(request.getStudent().getId());
                	if (assignments == null) {
                		assignments = new Hashtable<Long, Enrollment>();
                		iBestAssignmentTable.put(request.getStudent().getId(), assignments);
                	}
                	assignments.put(request.getId(), request.getBestAssignment());
            	}
            	if (request.getInitialAssignment() != null) {
                	Map<Long, Enrollment> assignments = iInitialAssignmentTable.get(request.getStudent().getId());
                	if (assignments == null) {
                		assignments = new Hashtable<Long, Enrollment>();
                		iInitialAssignmentTable.put(request.getStudent().getId(), assignments);
                	}
                	assignments.put(request.getId(), request.getInitialAssignment());
            	}
            }
        }
        
        private Enrollment getEnrollment(Request request, Enrollment enrollment) {
            if (request instanceof FreeTimeRequest) {
                return ((FreeTimeRequest)request).createEnrollment();
            } else {
                CourseRequest cr = (CourseRequest)request;
                Set<Section> sections = new HashSet<Section>();
                for (Section s: enrollment.getSections()) {
                    Section section = cr.getSection(s.getId());
                    if (section == null) {
                        iProgress.warn("WARNING: Section "+s.getName()+" is not available for "+cr.getName());
                        return null;
                    }
                    sections.add(section);
                }
                return cr.createEnrollment(currentSolution().getAssignment(), sections);
            }
        }
        
        private void assign(Enrollment enrollment) {
        	Map<Constraint<Request, Enrollment>, Set<Enrollment>> conflictConstraints = currentSolution().getModel().conflictConstraints(currentSolution().getAssignment(), enrollment);
            if (conflictConstraints.isEmpty()) {
            	currentSolution().getAssignment().assign(0, enrollment);
            } else {
                iProgress.warn("Unable to assign "+enrollment.variable().getName()+" := "+enrollment.getName());
                iProgress.warn("&nbsp;&nbsp;Reason:");
                for (Constraint<Request, Enrollment> c: conflictConstraints.keySet()) {
                	Set<Enrollment> vals = conflictConstraints.get(c);
                    for (Enrollment enrl: vals) {
                        iProgress.warn("&nbsp;&nbsp;&nbsp;&nbsp;"+enrl.getRequest().getName()+" = "+enrl.getName());
                    }
                    iProgress.debug("&nbsp;&nbsp;&nbsp;&nbsp;in constraint "+c);
                }
            }
        }
        
        private void unassignAll() {
            for (Request request: currentSolution().getModel().variables()) {
            	currentSolution().getAssignment().unassign(0l, request);
            }
        }
        
        public void execute() {
            iProgress = Progress.getInstance(currentSolution().getModel());
            
            Map<Long, Map<Long, Request>> requests = new Hashtable<Long, Map<Long,Request>>();
            for (Request request: currentSolution().getModel().variables()) {
            	Map<Long, Request> r = requests.get(request.getStudent().getId());
            	if (r == null) {
            		r = new Hashtable<Long, Request>();
            		requests.put(request.getStudent().getId(), r);
            	}
            	r.put(request.getId(), request);
            }
            
            if (!iBestAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating best assignment ...", iBestAssignmentTable.size());
                unassignAll();
                for (Map.Entry<Long, Map<Long, Enrollment>> e1: iBestAssignmentTable.entrySet()) {
                	Map<Long, Request> r = requests.get(e1.getKey());
                    iProgress.incProgress();
                	if (r == null) continue;
                	for (Map.Entry<Long, Enrollment> e2: e1.getValue().entrySet()) {
                		Request request = r.get(e2.getKey());
                		if (request == null) continue;
                		Enrollment enrollment = getEnrollment(request, e2.getValue());
                        if (enrollment!=null) assign(enrollment);
                	}
                }
                currentSolution().saveBest();
            }
            if (!iInitialAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating initial assignment ...", iInitialAssignmentTable.size());
                for (Map.Entry<Long, Map<Long, Enrollment>> e1: iInitialAssignmentTable.entrySet()) {
                	Map<Long, Request> r = requests.get(e1.getKey());
                    iProgress.incProgress();
                	if (r == null) continue;
                	for (Map.Entry<Long, Enrollment> e2: e1.getValue().entrySet()) {
                		Request request = r.get(e2.getKey());
                		if (request == null) continue;
                		Enrollment enrollment = getEnrollment(request, e2.getValue());
                        if (enrollment!=null) request.setInitialAssignment(enrollment);
                	}
                }
            }
            if (!iCurrentAssignmentTable.isEmpty()) {
                iProgress.setPhase("Creating current assignment ...", iCurrentAssignmentTable.size());
                unassignAll();
                for (Map.Entry<Long, Map<Long, Enrollment>> e1: iCurrentAssignmentTable.entrySet()) {
                	Map<Long, Request> r = requests.get(e1.getKey());
                    iProgress.incProgress();
                	if (r == null) continue;
                	for (Map.Entry<Long, Enrollment> e2: e1.getValue().entrySet()) {
                		Request request = r.get(e2.getKey());
                		if (request == null) continue;
                		Enrollment enrollment = getEnrollment(request, e2.getValue());
                        if (enrollment!=null) assign(enrollment);
                	}
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
        java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
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
            setInitalSolution(new Solution(model, new DefaultSingleAssignment<Request, Enrollment>()));
            initSolver();
            
            getProperties().setProperty("Xml.LoadBest", "true");
            getProperties().setProperty("Xml.LoadInitial", "true");
            getProperties().setProperty("Xml.LoadCurrent", "true");

            StudentSectioningXMLLoader loader = new StudentSectioningXMLLoader(model, currentSolution().getAssignment());
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
        java.util.concurrent.locks.Lock lock = currentSolution().getLock().writeLock();
        lock.lock();
        try {
            for (Request request: currentSolution().getModel().variables()) {
                currentSolution().getAssignment().unassign(0, request);
            }
            currentSolution().clearBest();
        } finally {
        	lock.unlock();
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
        java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
    		Map<String,String> info = super.currentSolution().getBestInfo();
    		try {
    			if (info == null || getSolutionComparator().isBetterThanBestSolution(super.currentSolution()))
    				info = super.currentSolution().getModel().getInfo(super.currentSolution().getAssignment());
    		} catch (ConcurrentModificationException e) {}
    		return info;
    	} finally {
    		lock.unlock();
    	}
    }

    private AcademicSessionInfo iSession = null;
	@Override
	public AcademicSessionInfo getAcademicSession() {
		if (iSession == null) {
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession(); 
			try {
				iSession = new AcademicSessionInfo(SessionDAO.getInstance().get(getSessionId(), hibSession));
				iSession.setSectioningEnabled(false);
			} finally {
				hibSession.close();
			}
		}
		return iSession;
	}

	private DistanceMetric iDistanceMetric = null;
	@Override
	public DistanceMetric getDistanceMetric() {
		if (iDistanceMetric == null) {
			iDistanceMetric = new DistanceMetric(getProperties());
			TravelTime.populateTravelTimes(iDistanceMetric);
		}
		return iDistanceMetric;
	}

	@Override
	public DataProperties getConfig() {
		return getProperties();
	}

	private Map<Long, XCourse> getCourseInfoTable() {
		if (iCourseInfoCache == null) {
			org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().createNewSession();
			try {
				iCourseInfoCache = new Hashtable<Long, XCourse>();
				for (CourseOffering course: (List<CourseOffering>)hibSession.createQuery(
						"from CourseOffering x where x.subjectArea.session.uniqueId = :sessionId"
						).setLong("sessionId", getSessionId()).setCacheable(true).list()) {
					iCourseInfoCache.put(course.getUniqueId(), new XCourse(course));
				}
			} finally {
				hibSession.close();
			}

		}
		return iCourseInfoCache;
	}
	private void clearCourseInfoTable() {
		iCourseInfoCache = null;
	}
	
	
	@Override
	public Collection<XCourseId> findCourses(String query, Integer limit, CourseMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XCourseId> ret = new ArrayList<XCourseId>(limit == null ? 100 : limit);
		String queryInLowerCase = query.toLowerCase();
		for (XCourse c : getCourseInfoTable().values()) {
			if (c.matchCourseName(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
			if (limit != null && ret.size() == limit) return ret;
		}
		if (queryInLowerCase.length() > 2) {
			for (XCourse c : getCourseInfoTable().values()) {
				if (c.matchTitle(queryInLowerCase) && (matcher == null || matcher.match(c))) ret.add(c);
				if (limit != null && ret.size() == limit) return ret;
			}
		}
		return ret;
	}

	@Override
	public Collection<XCourseId> findCourses(CourseMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XCourseId> ret = new ArrayList<XCourseId>();
		for (XCourse c : getCourseInfoTable().values())
			if (matcher.match(c)) ret.add(c);
		return ret;
	}

	@Override
	public Collection<XStudentId> findStudents(StudentMatcher matcher) {
		if (matcher != null) matcher.setServer(this);
		List<XStudentId> ret = new ArrayList<XStudentId>();
		for (Student student: ((StudentSectioningModel)currentSolution().getModel()).getStudents()) {
			if (student.isDummy()) continue;
			XStudentId s = new XStudentId(student);
			if (!student.isDummy() && matcher.match(s))
				ret.add(s);
		}
		return ret;
	}

	@Override
	public XCourse getCourse(Long courseId) {
		return getCourseInfoTable().get(courseId);
	}
	
	@Override
	public XCourse getCourse(String courseName) {
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			for (Course course: offering.getCourses())
				if (course.getName().equalsIgnoreCase(courseName)) return getCourse(course.getId());
		return null;
	}

	@Override
	public XStudent getStudent(Long studentId) {
		for (Student student: ((StudentSectioningModel)currentSolution().getModel()).getStudents())
			if (!student.isDummy() && student.getId() == studentId)
				return new XStudent(student, currentSolution().getAssignment());
		return null;
	}

	@Override
	public XOffering getOffering(Long offeringId) {
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			if (offering.getId() == offeringId)
				return new XOffering(offering, ((StudentSectioningModel)currentSolution().getModel()).getLinkedSections());
		return null;
	}

	@Override
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public <E> E execute(OnlineSectioningAction<E> action, Entity user) throws SectioningException {
		long c0 = OnlineSectioningHelper.getCpuTime();
		OnlineSectioningHelper h = new OnlineSectioningHelper(user);
		try {
			h.addMessageHandler(new OnlineSectioningHelper.DefaultMessageLogger(LogFactory.getLog(action.getClass().getName() + "." + action.name() + "[" + getAcademicSession().toCompactString() + "]")));
			h.addAction(action, getAcademicSession());
			E ret = action.execute(this, h);
			if (h.getAction() != null && !h.getAction().hasResult()) {
				if (ret == null)
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.NULL);
				else if (ret instanceof Boolean)
					h.getAction().setResult((Boolean)ret ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
				else
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.SUCCESS);
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException) {
				if (e.getCause() == null) {
					h.info("Execution failed: " + e.getMessage());
				} else {
					h.warn("Execution failed: " + e.getMessage(), e.getCause());
				}
			} else {
				h.error("Execution failed: " + e.getMessage(), e);
			}
			if (h.getAction() != null) {
				h.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
				if (e.getCause() != null && e instanceof SectioningException)
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
				else
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		} finally {
			if (h.getAction() != null)
				h.getAction().setEndTime(System.currentTimeMillis()).setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
			sLog.debug("Executed: " + h.getLog() + " (" + h.getLog().toByteArray().length + " bytes)");
		}
	}

	@Override
	public <E> void execute(OnlineSectioningAction<E> action, Entity user, ServerCallback<E> callback) throws SectioningException {
		try {
			callback.onSuccess(execute(action, user));
		} catch (Throwable t) {
			callback.onFailure(t);
		}
	}

	@Override
	public void clearAll() {
	}

	@Override
	public void clearAllStudents() {
	}

	@Override
	public Lock readLock() {
		return new NoLock();
	}

	@Override
	public Lock writeLock() {
		return new NoLock();
	}

	@Override
	public Lock lockAll() {
		return new NoLock();
	}

	@Override
	public Lock lockStudent(Long studentId, Collection<Long> offeringIds, String actionName) {
		return new NoLock();
	}

	@Override
	public Lock lockOffering(Long offeringId, Collection<Long> studentIds, String actionName) {
		return new NoLock();
	}

	@Override
	public Lock lockRequest(CourseRequestInterface request, String actionName) {
		return new NoLock();
	}

	@Override
	public boolean isOfferingLocked(Long offeringId) {
		return false;
	}

	@Override
	public void lockOffering(Long offeringId) {
	}

	@Override
	public void unlockOffering(Long offeringId) {
	}

	@Override
	public Collection<Long> getLockedOfferings() {
		return null;
	}

	@Override
	public void releaseAllOfferingLocks() {	
	}

	@Override
	public void persistExpectedSpaces(Long offeringId) {
	}

	@Override
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge) {
		return null;
	}

	@Override
	public void unload() {
	}
	
	public static class NoLock implements Lock {
		@Override
		public void release() {
		}
	}

	@Override
	public boolean needPersistExpectedSpaces(Long offeringId) {
		return false;
	}
	
	public byte[] exportXml() throws IOException {
        java.util.concurrent.locks.Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            File temp = File.createTempFile("student-" + getSessionId(), ".xml");
            boolean anonymize = ApplicationProperty.SolverXMLExportNames.isFalse(); 
            boolean idconv = ApplicationProperty.SolverXMLExportConvertIds.isTrue();
        	
            getProperties().setProperty("Xml.SaveBest", "true");
            getProperties().setProperty("Xml.SaveInitial", "true");
            getProperties().setProperty("Xml.SaveCurrent", "true");
            if (anonymize) {
                getProperties().setProperty("Xml.ConvertIds", idconv ? "true" : "false");
                getProperties().setProperty("Xml.SaveOnlineSectioningInfo", "true");
                getProperties().setProperty("Xml.SaveStudentInfo", "false");
                getProperties().setProperty("Xml.ShowNames", "false");
            }

            StudentSectioningXMLSaver saver = new StudentSectioningXMLSaver(this);
            ByteArrayOutputStream ret = new ByteArrayOutputStream();

            try {
                saver.save(temp);
                FileInputStream fis = new FileInputStream(temp);
                byte[] buf = new byte[16*1024]; int read = 0;
                while ((read=fis.read(buf, 0, buf.length))>0)
                    ret.write(buf,0,read);
                ret.flush();ret.close();
                fis.close();
            } catch (Exception e) {
                sLog.error(e.getMessage(),e);
            }
            
            temp.delete();
            
            if (anonymize) {
                getProperties().setProperty("Xml.ConvertIds", "false");
                getProperties().setProperty("Xml.SaveOnlineSectioningInfo", "true");
                getProperties().setProperty("Xml.SaveStudentInfo", "true");
                getProperties().setProperty("Xml.ShowNames", "true");
            }
            
            return ret.toByteArray();
        } finally {
        	lock.unlock();
        }
    }
	
	@Override
	public boolean isMaster() {
		return true;
	}
	
	@Override
	public void releaseMasterLockIfHeld() {
	}

	@Override
	public Collection<XCourseRequest> getRequests(Long offeringId) {
		List<XCourseRequest> ret = new ArrayList<XCourseRequest>();
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			if (offering.getId() == offeringId) {
				for (Course course: offering.getCourses())
					for (CourseRequest req: course.getRequests()) {
						if (!req.getStudent().isDummy())
							ret.add(new XCourseRequest(req, currentSolution().getAssignment().getValue(req)));
					}
				break;
			}
		return ret;
	}

	@Override
	public XEnrollments getEnrollments(Long offeringId) {
		return new XEnrollments(offeringId, getRequests(offeringId));
	}

	@Override
	public XExpectations getExpectations(Long offeringId) {
		for (Offering offering: ((StudentSectioningModel)currentSolution().getModel()).getOfferings())
			if (offering.getId() == offeringId)
				return new XExpectations(offering);
		return null;
	}

	@Override
	public void update(XExpectations expectations) {
	}

	@Override
	public void remove(XStudent student) {
	}

	@Override
	public void update(XStudent student, boolean updateRequests) {
	}

	@Override
	public void remove(XOffering offering) {
	}

	@Override
	public void update(XOffering offering) {
	}

	@Override
	public XCourseRequest assign(XCourseRequest request, XEnrollment enrollment) {
		return request;
	}

	@Override
	public XCourseRequest waitlist(XCourseRequest request, boolean waitlist) {
		return request;
	}

	@Override
	public boolean checkDeadline(Long courseId, XTime sectionTime, Deadline type) {
		return true;
	}

	@Override
	public String getCourseDetails(Long courseId, CourseDetailsProvider provider) {
		XCourse course = getCourse(courseId);
		return course == null ? null : course.getDetails(getAcademicSession(), provider);
	}

	@Override
	public boolean isReady() {
		return true;
	}
	
	@Override
	public long getMemUsage() {
		return new MemoryCounter().estimate(this);
	}

	@Override
	public <E> E getProperty(String name, E defaultValue) {
		E ret = (E)iOnlineProperties.get(name);
		return ret == null ? defaultValue : ret;
	}

	@Override
	public <E> void setProperty(String name, E value) {
		if (value == null)
			iOnlineProperties.remove(name);
		else
			iOnlineProperties.put(name, value);
	}
	
	@Override
	public CSVFile getReport(DataProperties parameters) {
		try {
			String name = parameters.getProperty("report", SectionConflictTable.class.getName());
			Class<StudentSectioningReport> clazz = (Class<StudentSectioningReport>) Class.forName(name);
			StudentSectioningReport report = clazz.getConstructor(StudentSectioningModel.class).newInstance(currentSolution().getModel());
			return report.create(currentSolution().getAssignment(), parameters);
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public OverExpectedCriterion getOverExpectedCriterion() {
		return new NeverOverExpected(getConfig());
	}
}
