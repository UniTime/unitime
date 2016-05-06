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
package org.unitime.timetable.solver.exam;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

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
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.extension.Extension;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ProblemLoader;
import org.cpsolver.ifs.util.ProblemSaver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.AbstractSolver;
import org.unitime.timetable.solver.SolverDisposeListener;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;
import org.unitime.timetable.solver.exam.ui.ExamProposedChange;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamSuggestionsInfo;


/**
 * @author Tomas Muller
 */
public class ExamSolver extends AbstractSolver<Exam, ExamPlacement, ExamModel> implements ExamSolverProxy {
    private ExamConflictStatisticsInfo iCbsInfo = null;
    
    public ExamSolver(DataProperties properties, SolverDisposeListener disposeListener) {
        super(properties, disposeListener);
    }
    
    @Override
    protected void disposeNoInherit(boolean unregister) {
    	super.disposeNoInherit(unregister);
        iCbsInfo = null;
    }
    
	@Override
	protected ProblemSaver<Exam, ExamPlacement, ExamModel> getDatabaseSaver(Solver<Exam, ExamPlacement> solver) {
		return new ExamDatabaseSaver(solver);
	}

	@Override
	protected ProblemLoader<Exam, ExamPlacement, ExamModel> getDatabaseLoader(ExamModel model, Assignment<Exam, ExamPlacement> assignment) {
		return new ExamDatabaseLoader(model, assignment);
	}

	@Override
	protected ExamModel createModel(DataProperties properties) {
		return new ExamModel(properties);
	}

	@Override
	protected Document createCurrentSolutionBackup(boolean anonymize, boolean idconv) {
		getProperties().setProperty("Xml.SaveConflictTable", "false");
        if (anonymize) {
            getProperties().setProperty("Xml.Anonymize", "true");
            getProperties().setProperty("Xml.ShowNames", "false");
            getProperties().setProperty("Xml.ConvertIds", idconv ? "true" : "false");
            getProperties().setProperty("Xml.Anonymize", "true");
            getProperties().setProperty("Xml.SaveInitial", "false");
            getProperties().setProperty("Xml.SaveBest", "false");
            getProperties().setProperty("Xml.SaveSolution", "true");
    	}
        Document document = ((ExamModel)currentSolution().getModel()).save(currentSolution().getAssignment());
        ExamConflictStatisticsInfo cbsInfo = getCbsInfo();
        if (cbsInfo!=null)
            cbsInfo.save(document.getRootElement().addElement("cbsInfo"));
        if (anonymize) {
        	Element log = document.getRootElement().element("log");
        	if (log != null)
        		document.getRootElement().remove(log);
        	Element notavailable = document.getRootElement().element("notavailable");
        	if (notavailable != null)
        		document.getRootElement().remove(notavailable);
            getProperties().setProperty("Xml.Anonymize", "false");
            getProperties().setProperty("Xml.ConvertIds", "false");
            getProperties().setProperty("Xml.ShowNames", "true");
            getProperties().remove("Xml.SaveInitial");
            getProperties().remove("Xml.SaveBest");
            getProperties().remove("Xml.SaveSolution");
        }
		return document;
	}

	@Override
	protected void restureCurrentSolutionFromBackup(Document document) {
		ExamModel model = (ExamModel)currentSolution().getModel();
		
        model.load(document, currentSolution().getAssignment());
        if (document.getRootElement().element("cbsInfo")!=null) {
            iCbsInfo = new ExamConflictStatisticsInfo();
            iCbsInfo.load(document.getRootElement().element("cbsInfo"));
        }
	}

    public Exam getExam(long examId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            for (Exam exam: currentSolution().getModel().variables()) {
                if (exam.getId()==examId) return exam;
            }
            return null;
        } finally {
        	lock.unlock();
        }
    }

    @Override
    public ExamInfo getInfo(long examId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            Exam exam = getExam(examId);
            return (exam==null?null: new ExamInfo(exam));
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public ExamAssignment getAssignment(long examId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            Exam exam = getExam(examId);
            ExamPlacement placement = (exam == null ? null : currentSolution().getAssignment().getValue(exam));
            return placement == null ? null : new ExamAssignment(placement, currentSolution().getAssignment());
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public ExamAssignmentInfo getAssignmentInfo(long examId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            Exam exam = getExam(examId);
            ExamPlacement placement = (exam == null ? null : currentSolution().getAssignment().getValue(exam));
            return placement == null ? null : new ExamAssignmentInfo(placement, currentSolution().getAssignment());
        } finally {
        	lock.unlock();
        }
    }
    
    public ExamPlacement getPlacement(ExamAssignment assignment) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public ExamAssignmentInfo getAssignment(Long examId, Long periodId, Collection<Long> roomIds) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public String assign(ExamAssignment assignment) {
    	Lock lock = currentSolution().getLock().writeLock();
    	lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public String unassign(ExamInfo examInfo) {
        Lock lock = currentSolution().getLock().writeLock();
        lock.lock();
        try {
            Exam exam = getExam(examInfo.getExamId());
            if (exam==null) return "Examination "+examInfo.getExamName()+" not found.";
            ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
            if (placement == null) return "Examination "+examInfo.getExamName()+" is not assigned.";
            Progress.getInstance(currentSolution().getModel()).info(exam.getName() + ": " + placement.getName() + " &rarr; not assigned");
            currentSolution().getAssignment().unassign(0, exam);
            return null;
        } finally {
        	lock.unlock();
        }
    }

    @Override
    public void load(DataProperties properties) {
        iCbsInfo = null;
        super.load(properties);
    }
    
    @Override
    public Callback getReloadingDoneCallback() {
        return new ReloadingDoneCallback();
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

    @Override
    public Collection<ExamAssignmentInfo> getAssignedExams() {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            Vector<ExamAssignmentInfo> ret = new Vector<ExamAssignmentInfo>();
            for (Exam exam: currentSolution().getModel().variables()) {
            	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
            	if (placement != null)
                    ret.add(new ExamAssignmentInfo(placement, currentSolution().getAssignment()));
            }
            return ret;
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public Collection<ExamInfo> getUnassignedExams() {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            Vector<ExamInfo> ret = new Vector<ExamInfo>();
            for (Exam exam: currentSolution().getModel().variables()) {
            	ExamPlacement placement = currentSolution().getAssignment().getValue(exam);
            	if (placement == null)
                    ret.add(new ExamInfo(exam));
            }
            return ret;
        } finally {
        	lock.unlock();
        }
    }

    @Override
    public Collection<ExamAssignmentInfo> getAssignedExams(Long subjectAreaId) {
        if (subjectAreaId==null || subjectAreaId<0) return getAssignedExams();
        String sa = new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ";
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public Collection<ExamInfo> getUnassignedExams(Long subjectAreaId) {
        if (subjectAreaId==null || subjectAreaId<0) return getUnassignedExams();
        String sa = new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ";
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public Collection<ExamAssignmentInfo> getAssignedExamsOfRoom(Long roomId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }

    @Override
    public Collection<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public Long getExamTypeId() {
    	return getProperties().getPropertyLong("Exam.Type", null);
    }
    
    @Override
    public Collection<ExamAssignmentInfo[]> getChangesToInitial(Long subjectAreaId) {
        String sa = (subjectAreaId!=null && subjectAreaId>=0 ? new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ":null);
        Vector<ExamAssignmentInfo[]> changes = new Vector<ExamAssignmentInfo[]>();
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
        return changes;
    }
    
    @Override
    public Collection<ExamAssignmentInfo[]> getChangesToBest(Long subjectAreaId) {
        String sa = (subjectAreaId!=null && subjectAreaId>=0 ? new SubjectAreaDAO().get(subjectAreaId).getSubjectAreaAbbreviation()+" ":null);
        Vector<ExamAssignmentInfo[]> changes = new Vector<ExamAssignmentInfo[]>();
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
        return changes;
    }
    

    @Override
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
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            info.load(cbs);
        } finally {
        	lock.unlock();
        }
        return info; 
    }
    
    @Override
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
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
            info.load(cbs, examId);
        } finally {
        	lock.unlock();
        }
        return info; 
    }
    
    @Override
    public ExamProposedChange update(ExamProposedChange change) {
        Lock lock = currentSolution().getLock().writeLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public Vector<ExamRoomInfo> getRooms(long examId, long periodId, ExamProposedChange change, int minRoomSize, int maxRoomSize, String filter, boolean allowConflicts) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public Collection<ExamAssignmentInfo> getPeriods(long examId, ExamProposedChange change) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    public ExamSuggestionsInfo getSuggestions(long examId, ExamProposedChange change, String filter, int depth, int limit, long timeOut) {
    	Lock lock = currentSolution().getLock().writeLock();
    	lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    @Override
    protected void autoConfigure() {
        super.autoConfigure();
        setPerturbationsCounter(null);
    }
    
    @Override
    public TreeSet<ExamAssignment> getExamsOfRoom(long locationId) {
        Lock lock = currentSolution().getLock().readLock();
        lock.lock();
        try {
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
        } finally {
        	lock.unlock();
        }
    }
    
    public void stopSolverImmediately() {
        super.stopSolver();
    }
    
    @Override
    public void stopSolver() {
        if (getNeighbourSelection() instanceof ExamNeighbourSelection) {
            ExamNeighbourSelection xn = (ExamNeighbourSelection)getNeighbourSelection();
            if (xn.isFinalPhase()) stopSolverImmediately();
            else xn.setFinalPhase(new Callback() {
                public void execute() { iStop=true; }
            });
        } else stopSolverImmediately();
    }
    
    @Override
    protected void beforeStart() {
    	iCbsInfo = null;
    }

	@Override
	public SolverType getType() {
		return SolverType.EXAM;
	}
}
