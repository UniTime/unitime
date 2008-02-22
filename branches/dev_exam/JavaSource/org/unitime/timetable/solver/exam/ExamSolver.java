package org.unitime.timetable.solver.exam;

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
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamModel;
import net.sf.cpsolver.exam.model.ExamPeriod;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;
import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Callback;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressWriter;

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
    
    public ExamPlacement getPlacement(long examId) {
        synchronized (super.currentSolution()) {
            for (Enumeration e=currentSolution().getModel().variables().elements();e.hasMoreElements();) {
                Exam exam = (Exam)e.nextElement();
                if (exam.getId()==examId) return (ExamPlacement)exam.getAssignment();
            }
            return null;
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
                iCurrentAssignmentTable.put(exam.getId(),exam.getAssignment());
                iBestAssignmentTable.put(exam.getId(),exam.getBestAssignment());
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
            ExamPeriod period = null;
            for (Enumeration f=exam.getPeriods().elements();f.hasMoreElements();) {
                ExamPeriod p = (ExamPeriod)f.nextElement();
                if (placement.getPeriod().equals(p)) {
                    period = p; break;
                }
            }
            if (period==null) {
                iProgress.warn("WARNING: Period "+placement.getPeriod()+" is no longer valid for class "+exam.getName());
                return null;
            }
            Set rooms = new HashSet();
            for (Iterator f=exam.getRooms().iterator();f.hasNext();) {
                ExamRoom r = (ExamRoom)f.next();
                if (placement.getRooms().contains(r)) {
                    rooms.add(r);
                }
            }
            if (rooms.size()!=placement.getRooms().size()) {
                iProgress.warn("WARNING: Room(s) "+placement.getRooms()+" are no longer valid for exam "+exam.getName());
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
}
