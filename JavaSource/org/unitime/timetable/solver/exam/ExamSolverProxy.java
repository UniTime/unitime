package org.unitime.timetable.solver.exam;

import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import net.sf.cpsolver.ifs.util.DataProperties;

public interface ExamSolverProxy extends ExamAssignmentProxy {

    public String getHost();
    public String getHostLabel();
    public void dispose() throws Exception;
    
    public void load(DataProperties properties) throws Exception;
    public void reload(DataProperties properties) throws Exception;
    public Date getLoadedDate() throws Exception;
    public void save() throws Exception;
    
    public void start() throws Exception;
    public boolean isRunning() throws Exception;
    public void stopSolver() throws Exception;
    public void restoreBest() throws Exception;
    public void saveBest() throws Exception;
    public void clear() throws Exception;
    public Hashtable currentSolutionInfo() throws Exception;
    public Hashtable bestSolutionInfo() throws Exception;
    public boolean isWorking() throws Exception;

    public DataProperties getProperties() throws Exception;
    public void setProperties(DataProperties properties) throws Exception;

    public String getNote() throws Exception;
    public void setNote(String note) throws Exception;
    public int getDebugLevel() throws Exception;
    public void setDebugLevel(int level) throws Exception;

    public Map getProgress() throws Exception;
    public String getLog() throws Exception;
    public String getLog(int level, boolean includeDate) throws Exception;
    public String getLog(int level, boolean includeDate, String fromStage) throws Exception;
    
    public boolean backup(File folder) throws Exception;
    public boolean restore(File folder) throws Exception;
    public boolean restore(File folder, boolean removeFiles) throws Exception;
}
