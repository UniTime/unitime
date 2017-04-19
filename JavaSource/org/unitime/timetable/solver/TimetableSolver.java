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
package org.unitime.timetable.solver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.locks.Lock;

import org.cpsolver.coursett.TimetableXMLLoader;
import org.cpsolver.coursett.TimetableXMLSaver;
import org.cpsolver.coursett.constraint.ClassLimitConstraint;
import org.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.RoomConstraint;
import org.cpsolver.coursett.constraint.SpreadConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.StudentGroup;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.extension.Extension;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.Callback;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ProblemLoader;
import org.cpsolver.ifs.util.ProblemSaver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.type.LongType;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.server.solver.TimetableGridHelper.ResourceType;
import org.unitime.timetable.server.solver.TimetableGridSolverHelper;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.Hint;
import org.unitime.timetable.solver.interactive.Suggestion;
import org.unitime.timetable.solver.interactive.Suggestions;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.ConflictStatisticsInfo;
import org.unitime.timetable.solver.ui.DeptBalancingReport;
import org.unitime.timetable.solver.ui.DiscouragedInstructorBtbReport;
import org.unitime.timetable.solver.ui.PerturbationReport;
import org.unitime.timetable.solver.ui.RoomReport;
import org.unitime.timetable.solver.ui.SameSubpartBalancingReport;
import org.unitime.timetable.solver.ui.SolverUnassignedClassesModel;
import org.unitime.timetable.solver.ui.StudentConflictsReport;
import org.unitime.timetable.solver.ui.ViolatedDistrPreferencesReport;
import org.unitime.timetable.webutil.timegrid.SolverGridModel;
import org.unitime.timetable.webutil.timegrid.TimetableGridContext;
import org.unitime.timetable.webutil.timegrid.TimetableGridModel;


/**
 * @author Tomas Muller
 */
public class TimetableSolver extends AbstractSolver<Lecture, Placement, TimetableModel> implements SolverProxy {
	private Vector iAssignmentRecords = new Vector();
	private Vector iBestAssignmentRecords = new Vector();
	private ConflictStatisticsInfo iCbsInfo = null;
	private CommitedClassAssignmentProxy iCommitedClassAssignmentProxy;

	public TimetableSolver(DataProperties properties, SolverDisposeListener solverDisposeListener) {
		super(properties, solverDisposeListener);
		iCommitedClassAssignmentProxy = new CommitedClassAssignmentProxy();
	}
	
	@Override
	protected ProblemSaver<Lecture, Placement, TimetableModel> getDatabaseSaver(Solver<Lecture, Placement> solver) {
		return new TimetableDatabaseSaver(solver);
	}

	@Override
	protected ProblemLoader<Lecture, Placement, TimetableModel> getDatabaseLoader(TimetableModel model, org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment) {
		return new TimetableDatabaseLoader(model, assignment);
	}

	@Override
	protected TimetableModel createModel(DataProperties properties) {
		return new TimetableModel(properties);
	}

	@Override
	protected Document createCurrentSolutionBackup(boolean anonymize, boolean idconv) {
		
        if (anonymize) {
            getProperties().setProperty("Xml.ConvertIds", idconv ? "true" : "false");
            getProperties().setProperty("Xml.ShowNames", "false");
        }

        TimetableXMLSaver saver = new TimetableXMLSaver(this);
		Document document = saver.saveDocument();
		if (!anonymize) {
			Progress.getInstance(saver.getModel()).save(document.getRootElement());
			if (!iAssignmentRecords.isEmpty()) {
				Element assignmentRecords = document.getRootElement().addElement("assignmentRecords");
				for (Enumeration e=iAssignmentRecords.elements();e.hasMoreElements();) {
					AssignmentRecord r = (AssignmentRecord)e.nextElement();
					r.toXml(assignmentRecords.addElement("record"));
				}
			}
			if (!iBestAssignmentRecords.isEmpty()) {
				Element bestAssignmentRecords = document.getRootElement().addElement("bestAssignmentRecords");
				for (Enumeration e=iBestAssignmentRecords.elements();e.hasMoreElements();) {
					AssignmentRecord r = (AssignmentRecord)e.nextElement();
					r.toXml(bestAssignmentRecords.addElement("record"));
				}
			}
			ConflictStatisticsInfo cbsInfo = getCbsInfo();
			if (cbsInfo!=null)
				cbsInfo.save(document.getRootElement().addElement("cbsInfo"));
		}

		if (anonymize) {
			getProperties().setProperty("Xml.ConvertIds", "false");
			getProperties().setProperty("Xml.ShowNames", "true");
		}

		return document;
	}

	@Override
	protected void restureCurrentSolutionFromBackup(Document document) {
		TimetableXMLLoader loader = new TimetableXMLLoader((TimetableModel)currentSolution().getModel(), currentSolution().getAssignment());
		loader.load(currentSolution(), document);
		iAssignmentRecords.clear(); iBestAssignmentRecords.clear();
		Element assignmentRecords = document.getRootElement().element("assignmentRecords");
		if (assignmentRecords!=null) {
			for (Iterator i=assignmentRecords.elementIterator("record");i.hasNext();) {
				iAssignmentRecords.add(AssignmentRecord.fromXml((Element)i.next()));
			}
		}
		Element bestAssignmentRecords = document.getRootElement().element("bestAssignmentRecords");
		if (bestAssignmentRecords!=null) {
			for (Iterator i=bestAssignmentRecords.elementIterator("record");i.hasNext();) {
				iBestAssignmentRecords.add(AssignmentRecord.fromXml((Element)i.next()));
			}
		}
		if (document.getRootElement().element("cbsInfo")!=null) {
			iCbsInfo = new ConflictStatisticsInfo();
			iCbsInfo.load(document.getRootElement().element("cbsInfo"));
		}
	}
	
	@Override
	protected void beforeStart() {
		iCbsInfo = null;
	}

	@Override
	public String getNote() {
		return getProperties().getProperty("General.Note");
	}
	
	@Override
	public void setNote(String note) {
		getProperties().setProperty("General.Note",note);
	}
	
	@Override
	public void restoreBest() {
		iAssignmentRecords = new Vector(iBestAssignmentRecords);
		currentSolution().restoreBest();
	}
	
	public void saveBest() {
		iBestAssignmentRecords = new Vector(iAssignmentRecords);
		currentSolution().saveBest();
	}

	@Override
	public void finalSectioning() {
		iWorkThread = new FinalSectioning();
		iWorkThread.start();
	}
	
	protected void afterFinalSectioning() {}
    
    public class FinalSectioning extends InterruptibleThread<Lecture, Placement> {
    	public void run() {
    		setName("FinalSectioning");
    		iWorking = true;
    		try {
    			((TimetableModel)currentSolution().getModel()).switchStudents(currentSolution().getAssignment(), this);
    		} finally {
    			iWorking = false;
    			Progress.getInstance(currentSolution().getModel()).setStatus("Awaiting commands ...");
    		}
    		afterFinalSectioning();
    	}
    }
    
    @Override
    protected void disposeNoInherit(boolean unregister) {
    	iAssignmentRecords.clear(); iBestAssignmentRecords.clear(); iCbsInfo = null;
    	super.disposeNoInherit(unregister);
    }
    
    @Override
    protected void finishBeforeSave() {
		if (getProperties().getPropertyBoolean("General.SwitchStudents",true)) {
			((TimetableModel)currentSolution().getModel()).switchStudents(currentSolution().getAssignment(), null);
			currentSolution().saveBest();
		}
    }
    
    @Override
    public void save(boolean createNewSolution, boolean commitSolution) {
		getProperties().setProperty("General.CreateNewSolution", (createNewSolution ? "true" : "false"));
		if (createNewSolution)
			getProperties().remove("General.SolutionId");
		getProperties().setProperty("General.CommitSolution", (commitSolution ? "true" : "false"));
		super.save();
    }

    @Override
    public void load(DataProperties properties) {
    	iAssignmentRecords.clear(); iBestAssignmentRecords.clear(); iCbsInfo = null;
    	super.load(properties);
    }
    
    @Override
    public Callback getReloadingDoneCallback() {
    	return new ReloadingDoneCallback();
    }

    protected boolean useAmPm() {
    	return getProperties().getPropertyBoolean("General.UseAmPm", true);
    }

    public class ReloadingDoneCallback implements Callback {
    	Hashtable iCurrentAssignmentTable = new Hashtable();
    	Hashtable iBestAssignmentTable = new Hashtable();
    	Hashtable iInitialAssignmentTable = new Hashtable();
    	String iSolutionId = null;
    	Progress iProgress = null;
    	public ReloadingDoneCallback() {
    		iSolutionId = getProperties().getProperty("General.SolutionId");
    		for (Lecture lecture: currentSolution().getModel().variables()) {
    			Placement current = currentSolution().getAssignment().getValue(lecture); 
    			if (current!=null)
    				iCurrentAssignmentTable.put(lecture.getClassId(),current);
    			if (lecture.getBestAssignment()!=null)
    				iBestAssignmentTable.put(lecture.getClassId(),lecture.getBestAssignment());
    			if (lecture.getInitialAssignment()!=null)
    				iInitialAssignmentTable.put(lecture.getClassId(),lecture.getInitialAssignment());
    		}
    	}
    	private Lecture getLecture(Long classId) {
    		for (Lecture lecture: currentSolution().getModel().variables()) {
    			if (lecture.getClassId().equals(classId)) 
    				return lecture;
    		}
    		return null;
    	}
    	private Placement getPlacement(Lecture lecture, Placement placement) {
    		TimeLocation time = null;
    		for (TimeLocation t: lecture.timeLocations()) {
    			if (placement.getTimeLocation().equals(t)) {
    				time = t; break;
    			}
    		}
    		if (time==null) {
    			iProgress.warn("WARNING: Time "+placement.getTimeLocation().getLongName(useAmPm())+" is no longer valid for class "+lecture.getName());
    			return null;
    		}
    		Vector rooms = new Vector();
    		for (RoomLocation r: lecture.roomLocations()) {
    			if (placement.isMultiRoom() && placement.getRoomLocations().contains(r)) {
    				rooms.add(r);
    			}
    			if (!placement.isMultiRoom() && placement.getRoomLocation().equals(r)) {
    				rooms.add(r); break;
    			}
    		}
    		if (rooms.size()!=lecture.getNrRooms()) {
    			iProgress.warn("WARNING: Room(s) "+(placement.isMultiRoom()?placement.getRoomLocations().toString():placement.getRoomLocation().getName())+" are no longer valid for class "+lecture.getName());
    			return null;
    		}
    		return new Placement(lecture,time,rooms);
    	}
    	private void assign(Placement placement) {
    		((TimetableModel)currentSolution().getModel()).weaken(currentSolution().getAssignment(), placement);
    		if (placement.isValid()) {
                Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = currentSolution().getModel().conflictConstraints(currentSolution().getAssignment(), placement);
                if (conflictConstraints.isEmpty()) {
                	currentSolution().getAssignment().assign(0, placement);
                } else {
                    iProgress.warn("Unable to assign "+placement.variable().getName()+" &larr; "+placement.getLongName(useAmPm()));
                    iProgress.warn("&nbsp;&nbsp;Reason:");
                    for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
                        Collection vals = (Collection)conflictConstraints.get(c);
                        for (Iterator j=vals.iterator();j.hasNext();) {
                            Placement v = (Placement) j.next();
                            iProgress.warn("&nbsp;&nbsp;&nbsp;&nbsp;"+v.variable().getName()+" = "+v.getLongName(useAmPm()));
                        }
                        iProgress.debug("&nbsp;&nbsp;&nbsp;&nbsp;in constraint "+c);
                    }
                }
    		} else {
    			Lecture lecture = placement.variable();
    			String reason = "";
               	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
        			if (!ic.isAvailable(lecture, placement))
        				reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;instructor "+ic.getName()+" not available";
               	}
    	    	if (lecture.getNrRooms()>0) {
    	    		if (placement.isMultiRoom()) {
    	    			for (RoomLocation roomLocation: placement.getRoomLocations()) {
    	    				if (!roomLocation.getRoomConstraint().isAvailable(lecture,placement.getTimeLocation(),lecture.getScheduler()))
    	    					reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;room "+roomLocation.getName()+" not available";
    	    			}
    	    		} else {
    					if (!placement.getRoomLocation().getRoomConstraint().isAvailable(lecture,placement.getTimeLocation(),lecture.getScheduler()))
    						reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;room "+placement.getRoomLocation().getName()+" not available";
    	    		}
    	    	}
    	    	Map<Constraint<Lecture, Placement>, Set<Placement>> conflictConstraints = currentSolution().getModel().conflictConstraints(currentSolution().getAssignment(), placement);
                if (!conflictConstraints.isEmpty()) {
                    for (Constraint<Lecture, Placement> c: conflictConstraints.keySet()) {
                    	Set<Placement> vals = conflictConstraints.get(c);
                        for (Placement p: vals) {
                            Lecture l = p.variable();
                            if (l.isCommitted())
                            	reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;conflict with committed assignment "+l.getName()+" = "+p.getLongName(useAmPm())+" (in constraint "+c+")";
                            if (p.equals(placement))
                            	reason += "<br>&nbsp;&nbsp;&nbsp;&nbsp;constraint "+c;
                        }
                    }
                }
    	    	iProgress.warn("Unable to assign "+lecture.getName()+" &larr; "+placement.getLongName(useAmPm())+(reason.length()==0?".":":"+reason));
    		}
    	}
    	private void unassignAll() {
    		for (Lecture lecture: currentSolution().getModel().variables())
    			currentSolution().getAssignment().unassign(0, lecture);
    	}
    	public void execute() {
    		iProgress = Progress.getInstance(currentSolution().getModel());
    		
            if (!iBestAssignmentTable.isEmpty()) {
            	iProgress.setPhase("Creating best assignment ...",iBestAssignmentTable.size());
            	unassignAll();
            	for (Iterator i=iBestAssignmentTable.entrySet().iterator();i.hasNext();) {
            		Map.Entry entry = (Map.Entry)i.next();
            		iProgress.incProgress();
            		Lecture lecture = getLecture((Long)entry.getKey()); 
            		if (lecture==null) continue;
            		Placement placement = getPlacement(lecture,(Placement)entry.getValue());
            		if (placement!=null) assign(placement);
            	}
            	
            	currentSolution().saveBest();
            }
            if (!iInitialAssignmentTable.isEmpty()) {
            	iProgress.setPhase("Creating initial assignment ...",iInitialAssignmentTable.size());
            	for (Iterator i=iInitialAssignmentTable.entrySet().iterator();i.hasNext();) {
            		Map.Entry entry = (Map.Entry)i.next();
            		iProgress.incProgress();
            		Lecture lecture = getLecture((Long)entry.getKey()); 
            		if (lecture==null) continue;
            		Placement placement = getPlacement(lecture,(Placement)entry.getValue());
            		if (placement!=null)  lecture.setInitialAssignment(placement);
            	}
            }
            if (!iCurrentAssignmentTable.isEmpty()) {
            	iProgress.setPhase("Creating current assignment ...",iCurrentAssignmentTable.size());
            	unassignAll();
            	for (Iterator i=iCurrentAssignmentTable.entrySet().iterator();i.hasNext();) {
            		Map.Entry entry = (Map.Entry)i.next();
            		iProgress.incProgress();
            		Lecture lecture = getLecture((Long)entry.getKey()); 
            		if (lecture==null) continue;
            		Placement placement = getPlacement(lecture,(Placement)entry.getValue());
            		if (placement!=null) assign(placement);
            	}
            }
            iCurrentAssignmentTable.clear();
            iBestAssignmentRecords.clear();
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
    public ConflictStatisticsInfo getCbsInfo() {
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
    	ConflictStatisticsInfo info = new ConflictStatisticsInfo();
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		info.load(this, cbs);
    	} finally {
    		lock.unlock();
    	}
    	return info; 
    }
    
    @Override
    public ConflictStatisticsInfo getCbsInfo(Long classId) {
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
    	ConflictStatisticsInfo info = new ConflictStatisticsInfo();
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		info.load(this, cbs, classId);
    	} finally {
    		lock.unlock();
    	}
    	return info; 
    }    
    
    @Override
    public SolverUnassignedClassesModel getUnassignedClassesModel(String... prefix) {
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		return new SolverUnassignedClassesModel(this, prefix);
    	} finally {
    		lock.unlock();
    	}
    }

    private boolean match(Query q, final String name) {
    	return q == null || q.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					term: for (StringTokenizer s = new StringTokenizer(term, " ,"); s.hasMoreTokens(); ) {
						String termToken = s.nextToken();
						for (StringTokenizer t = new StringTokenizer(name, " ,"); t.hasMoreTokens(); ) {
							String token = t.nextToken();
							if (token.toLowerCase().startsWith(termToken.toLowerCase())) continue term;
						}
						return false;
					}
					return true;
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return name.matches(term);
				} else if ("find".equals(attr)) {
					return name.toLowerCase().indexOf(term.toLowerCase()) >= 0;
				}
				return false;
			}
		});
	}
    
    private static enum Size {
		eq, lt, gt, le, ge
	};
    
    private boolean match(Query q, final RoomConstraint rc) {
    	return q == null || q.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					for (StringTokenizer s = new StringTokenizer(rc.getName(), " ,"); s.hasMoreTokens(); ) {
						String token = s.nextToken();
						if (term.equalsIgnoreCase(token)) return true;
					}
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return rc.getName().matches(term);
				} else if ("find".equals(attr)) {
					return rc.getName().toLowerCase().indexOf(term.toLowerCase()) >= 0;
				} else if ("type".equals(attr) && rc.getType() != null) {
					RoomType type = RoomTypeDAO.getInstance().get(rc.getType());
					return type != null && (term.equalsIgnoreCase(type.getReference()) || term.equalsIgnoreCase(type.getLabel()));
				} else if ("size".equals(attr)) {
					int min = 0, max = Integer.MAX_VALUE;
					Size prefix = Size.eq;
					String number = term;
					if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
					else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
					else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
					else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
					else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
					try {
						int a = Integer.parseInt(number);
						switch (prefix) {
							case eq: min = max = a; break; // = a
							case le: max = a; break; // <= a
							case ge: min = a; break; // >= a
							case lt: max = a - 1; break; // < a
							case gt: min = a + 1; break; // > a
						}
					} catch (NumberFormatException e) {}
					if (term.contains("..")) {
						try {
							String a = term.substring(0, term.indexOf('.'));
							String b = term.substring(term.indexOf("..") + 2);
							min = Integer.parseInt(a); max = Integer.parseInt(b);
						} catch (NumberFormatException e) {}
					}
					return min <= rc.getCapacity() && rc.getCapacity() <= max;
				}
				return false;
			}
		});
	}
    
    @Override
    public Vector getTimetableGridTables(TimetableGridContext context) {
    	Vector models = new Vector();
    	Query q = (context.getFilter() == null ? null : new Query(context.getFilter()));
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		TimetableModel model = (TimetableModel)currentSolution().getModel();
    		switch (context.getResourceType()) {
    		case TimetableGridModel.sResourceTypeRoom:
    			for (RoomConstraint rc: model.getRoomConstraints()) {
    				if (!match(q, rc)) continue;
    				models.add(new SolverGridModel(this,rc,context));
    			}
    			break;
    		case TimetableGridModel.sResourceTypeInstructor:
    			for (InstructorConstraint ic: model.getInstructorConstraints()) {
    				if (!match(q, ic.getName())) continue;
    				models.add(new SolverGridModel(this,ic,context));
    			}
    			break;
    		case TimetableGridModel.sResourceTypeDepartment:
    			for (DepartmentSpreadConstraint dc: model.getDepartmentSpreadConstraints()) {
    				if (!match(q, dc.getName())) continue;
    				models.add(new SolverGridModel(this,dc,context));
    			}
    			if (model.getDepartmentSpreadConstraints().isEmpty()) {
    				org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment = currentSolution().getAssignment();
    				Map<Department, Set<Long>> dept2class = new HashMap<Department, Set<Long>>();
    				for (Object[] pair: (List<Object[]>)DepartmentDAO.getInstance().getSession().createQuery(
    						"select c.controllingDept, c.uniqueId from Class_ c where c.managingDept.solverGroup.uniqueId in :solverGroupIds"
    						).setParameterList("solverGroupIds", getOwnerId(), new LongType()).list()) {
    					Department dept = (Department)pair[0];
    					Long classId = (Long)pair[1];
    					Set<Long> classIds = dept2class.get(dept);
    					if (classIds == null) { classIds = new HashSet<Long>(); dept2class.put(dept, classIds); }
    					classIds.add(classId);
    				}
    				for (Department d: new TreeSet<Department>(dept2class.keySet())) {
    					if (!match(q, d.getShortLabel())) continue;
    					Set<Long> classIds = dept2class.get(d);
    					int size = 0;
    					List<Placement> placements = new ArrayList<Placement>();
    					for (Lecture lecture: getModel().variables()) {
    						if (classIds.contains(lecture.getClassId())) {
    							size ++;
    							Placement placement = assignment.getValue(lecture);
								if (placement != null) placements.add(placement);
    						}
    					}
    					if (size > 0)
    						models.add(new SolverGridModel(this, TimetableGridModel.sResourceTypeDepartment, d.getUniqueId(), d.getShortLabel(), size, placements, context));
    				}
    			}
    			break;
    		case TimetableGridModel.sResourceTypeCurriculum:
    			Hashtable<String, List<Student>> curricula = new Hashtable<String, List<Student>>();
    			boolean hasCurricula = false;
    			HashSet<String> ignore = new HashSet<String>(), tested = new HashSet<String>();
    			for (Student student: model.getAllStudents()) {
    				if (student.getCurriculum() != null && !student.getCurriculum().isEmpty()) {
    					if (!hasCurricula) {
    						curricula.clear(); hasCurricula = true;
    					}
    					for (String c: student.getCurriculum().split("\\|")) {
        					if (tested.add(c) && !match(q, c)) ignore.add(c);
        					if (ignore.contains(c)) continue;
        					List<Student> students = curricula.get(c);
        					if (students == null) {
        						students = new ArrayList<Student>();
        						curricula.put(c, students);
        					}
        					students.add(student);
    					}
    				} else if (!hasCurricula && student.getAcademicArea() != null && student.getAcademicClassification() != null) {
    					String c = student.getAcademicArea() + (student.getMajor() == null ? "" : " " + student.getMajor()) + " " + student.getAcademicClassification();
    					if (tested.add(c) && !match(q, c)) ignore.add(c);
    					if (ignore.contains(c)) continue;
    					List<Student> students = curricula.get(c);
    					if (students == null) {
    						students = new ArrayList<Student>();
    						curricula.put(c, students);
    					}
    					students.add(student);
    				}
    			}
				for (Map.Entry<String, List<Student>> curriculum: curricula.entrySet()) {
					models.add(new SolverGridModel(this, curriculum.getKey(), curriculum.getValue(), context));
				}
    			break;
    		case TimetableGridModel.sResourceTypeSubjectArea:
    			org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment = currentSolution().getAssignment();
				Map<SubjectArea, Set<Long>> sa2class = new HashMap<SubjectArea, Set<Long>>();
				for (Object[] pair: (List<Object[]>)DepartmentDAO.getInstance().getSession().createQuery(
						"select co.subjectArea, c.uniqueId from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where co.isControl = true and c.managingDept.solverGroup.uniqueId in :solverGroupIds"
						).setParameterList("solverGroupIds", getOwnerId(), new LongType()).list()) {
					SubjectArea sa = (SubjectArea)pair[0];
					Long classId = (Long)pair[1];
					Set<Long> classIds = sa2class.get(sa);
					if (classIds == null) { classIds = new HashSet<Long>(); sa2class.put(sa, classIds); }
					classIds.add(classId);
				}
				for (SubjectArea sa: new TreeSet<SubjectArea>(sa2class.keySet())) {
					if (!match(q, sa.getSubjectAreaAbbreviation())) continue;
					Set<Long> classIds = sa2class.get(sa);
					int size = 0;
					List<Placement> placements = new ArrayList<Placement>();
					for (Lecture lecture: getModel().variables()) {
						if (classIds.contains(lecture.getClassId())) {
							size ++;
							Placement placement = assignment.getValue(lecture);
							if (placement != null) placements.add(placement);
						}
					}
					if (size > 0)
						models.add(new SolverGridModel(this, TimetableGridModel.sResourceTypeSubjectArea, sa.getUniqueId(), sa.getSubjectAreaAbbreviation(), size, placements, context));
				}
				break;
    		case TimetableGridModel.sResourceTypeStudentGroup:
    			for (StudentGroup group: model.getStudentGroups()) {
					if (match(q, group.getName())) 
						models.add(new SolverGridModel(this, group, context));
    			}
    			break;
    		}
    	} finally {
    		lock.unlock();
    	}
		return models;
    }
    
    @Override
    public ClassAssignmentDetails getClassAssignmentDetails(Long classId, boolean includeConstraints) {
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		TimetableModel model = (TimetableModel)currentSolution().getModel();
    		for (Lecture lecture: model.variables()) {
    			if (lecture.getClassId().equals(classId))
    				return new ClassAssignmentDetails(this,lecture,includeConstraints);
    		}
   			return null;
    	} finally {
    		lock.unlock();
    	}
    }
    
    @Override
    public Suggestions getSuggestions(SuggestionsModel model) {
    	if (iWorking) return null;
    	Lock lock = currentSolution().getLock().writeLock();
		lock.lock();
		try {
    		return new Suggestions(this,model);
    	} finally {
    		lock.unlock();
    	}
    }
    
    @Override
    public AssignmentPreferenceInfo getInfo(Hint hint) {
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		return hint.getInfo(this);
    	} finally {
    		lock.unlock();
    	}
    }
    
    @Override
    public String getNotValidReason(Hint hint) {
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		return hint.getNotValidReason(this);
    	} finally {
    		lock.unlock();
    	}
    }

    @Override
    public void assign(Collection hints) {
    	Lock lock = currentSolution().getLock().writeLock();
		lock.lock();
		try {
			Hashtable initialAssignments = new Hashtable();
			for (Placement placement: currentSolution().getAssignment().assignedValues()) {
				initialAssignments.put(placement.variable(), placement);
			}
			AssignmentRecord record = new AssignmentRecord(this);
			for (Iterator i=hints.iterator();i.hasNext();) {
				Hint hint = (Hint)i.next();
				Placement p = hint.getPlacement((TimetableModel)currentSolution().getModel());
				if (p!=null) {
					Placement ini = (Placement)initialAssignments.get(p.variable());
					record.add(ini,p);
					Progress.getInstance(currentSolution().getModel()).info(p.variable().getName()+": "+(ini==null?"not assigned":ini.getLongName(useAmPm()))+" &rarr; "+p.getLongName(useAmPm()));
                    if (ini!=null) currentSolution().getAssignment().unassign(0, p.variable());
				} else if (hint.getDays() == 0) {
					Lecture lecture = null;
					for (Lecture l: currentSolution().getModel().variables())
						if (l.getClassId().equals(hint.getClassId())) {
							lecture = l;
						}
					if (lecture != null && !lecture.isCommitted())
						currentSolution().getAssignment().unassign(0, lecture);
				}
			}
            for (Iterator i=hints.iterator();i.hasNext();) {
                Hint hint = (Hint)i.next();
                Placement p = hint.getPlacement((TimetableModel)currentSolution().getModel());
                if (p!=null) currentSolution().getAssignment().assign(0,p);
            }
			for (Lecture lec: currentSolution().getModel().unassignedVariables(currentSolution().getAssignment())) {
				Placement p = (Placement)initialAssignments.get(lec);
				if (p!=null) { 
					record.add(p,null);
					Progress.getInstance(currentSolution().getModel()).info(p.variable().getName()+": "+p.getLongName(useAmPm())+" &rarr; not assigned");
				}
			}
			record.done();
			iAssignmentRecords.addElement(record);
		} finally {
			lock.unlock();
		}
    }
    
    @Override
    public Hashtable conflictInfo(Collection hints) {
    	Hashtable conflictTable = new Hashtable();
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			HashSet done = new HashSet();
			for (Iterator i=hints.iterator();i.hasNext();) {
				Hint hint = (Hint)i.next();
				Placement p = hint.getPlacement((TimetableModel)currentSolution().getModel());
				if (p==null) continue;
		        for (Constraint constraint: p.variable().hardConstraints()) {
		            HashSet conflicts = new HashSet();
		            constraint.computeConflicts(currentSolution().getAssignment(), p, conflicts);
		            if (conflicts!=null && !conflicts.isEmpty()) {
		            	for (Iterator j=conflicts.iterator();j.hasNext();) {
		            		Placement conflict = (Placement)j.next();
		            		Hint confHint = new Hint(this, conflict);
		            		if (done.contains(confHint)) continue;
		            		if (!conflictTable.containsKey(confHint)) {
		            			String name = constraint.getName();
		        				if (constraint instanceof RoomConstraint) {
		        					name = "Room "+constraint.getName();
		        				} else if (constraint instanceof InstructorConstraint) {
		        					name = "Instructor "+constraint.getName();
		        				} else if (constraint instanceof GroupConstraint) {
		        					name = "Distribution "+constraint.getName();
		        				} else if (constraint instanceof DepartmentSpreadConstraint) {
		        					name = "Balancing of department "+constraint.getName();
		        				} else if (constraint instanceof SpreadConstraint) {
		        					name = "Same subpart spread "+constraint.getName();
		        				} else if (constraint instanceof ClassLimitConstraint) {
		        					name = "Class limit "+constraint.getName();
		        				}
		        				conflictTable.put(confHint, name);
		            		}
		            	}
		            }
		        }
		        done.add(hint);
			}
		} finally {
			lock.unlock();
		}
		return conflictTable;
    }

    public Long[] getOwnerId() {
    	return getProperties().getPropertyLongArry("General.SolverGroupId", null);
    }
    
    private HashSet iDepartmentIds = null;
    @Override
    public Set getDepartmentIds() {
    	if (iDepartmentIds!=null) return iDepartmentIds;
    	iDepartmentIds = new HashSet();
    	Long ownerId[] = getOwnerId();
    	for (int i=0;i<ownerId.length;i++) {
    		SolverGroup sg = (new SolverGroupDAO()).get(ownerId[i]);
    		for (Iterator j=sg.getDepartments().iterator();j.hasNext();) {
    			iDepartmentIds.add(((Department)j.next()).getUniqueId());
    		}
    	}
    	return iDepartmentIds;
    }
    
    @Override
	public Assignment getAssignment(Class_ clazz) {
		Department dept = clazz.getManagingDept();
		if (dept!=null && getDepartmentIds().contains(dept.getUniqueId()))
			return getAssignment(clazz.getUniqueId());
        return iCommitedClassAssignmentProxy.getAssignment(clazz);
    }
    
    @Override
    public Assignment getAssignment(Long classId) {
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		Lecture lecture = null;
    		for (Lecture l: currentSolution().getModel().variables()) {
    			if (l.getClassId().equals(classId)) {
    				lecture = l; break;
    			}
    		}
    		if (lecture==null) return null;
    		Placement placement = (Placement)currentSolution().getAssignment().getValue(lecture);
    		if (placement==null) return null;
        	Assignment assignment = new Assignment();
        	assignment.setClassName(lecture.getName());
    		assignment.setDays(new Integer(placement.getTimeLocation().getDayCode()));
    		assignment.setStartSlot(new Integer(placement.getTimeLocation().getStartSlot()));
    		if (placement.getTimeLocation().getDatePatternId()!=null) {
    			assignment.setDatePattern(DatePatternDAO.getInstance().get(placement.getTimeLocation().getDatePatternId()));
    		}
    		assignment.setSlotsPerMtg(placement.getTimeLocation().getLength());
    		assignment.setBreakTime(placement.getTimeLocation().getBreakTime());
    		HashSet rooms = new HashSet();
    		if (placement.isMultiRoom()) {
    			for (RoomLocation r: placement.getRoomLocations()) {
    				Location room = (new LocationDAO()).get(r.getId());
    				if (room!=null) rooms.add(room);
    			}
    		} else {
    			Location room = (new LocationDAO()).get(placement.getRoomLocation().getId());
    			if (room!=null) rooms.add(room);
    		}
    		assignment.setRooms(rooms);
    		TimePattern pattern = (new TimePatternDAO()).get(placement.getTimeLocation().getTimePatternId());
    		assignment.setTimePattern(pattern);
    		HashSet instructors = new HashSet();
    		for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
    			DepartmentalInstructor instructor = null;
    			if (ic.getResourceId()!=null) {
					instructor = (new DepartmentalInstructorDAO()).get(ic.getResourceId());
				}
    			if (instructor!=null) instructors.add(instructor);
    			
    		}
    		assignment.setInstructors(instructors);
    		return assignment;
    	} finally {
    		lock.unlock();
    	}
    }

    @Override
    public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) {
		Department dept = clazz.getManagingDept();
		if (dept!=null && getDepartmentIds().contains(dept.getUniqueId()))
				return getAssignmentInfo(clazz.getUniqueId());
		return iCommitedClassAssignmentProxy.getAssignmentInfo(clazz);
    }
    
    @Override
    public AssignmentPreferenceInfo getAssignmentInfo(Long classId) {
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		Lecture lecture = null;
    		for (Lecture l: currentSolution().getModel().variables()) {
    			if (l.getClassId().equals(classId)) {
    				lecture = l; break;
    			}
    		}
    		if (lecture==null) return null;
    		Placement placement = (Placement)currentSolution().getAssignment().getValue(lecture);
    		if (placement==null) return null;
    		return new AssignmentPreferenceInfo(this,placement);
    	} finally {
    		lock.unlock();
    	}
    }

    @Override
	public Hashtable getAssignmentTable(Collection classesOrClassIds) {
		Hashtable assignments = new Hashtable();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Assignment assignment = (classOrClassId instanceof Class_ ? getAssignment((Class_)classOrClassId) : getAssignment((Long)classOrClassId));
			if (assignment!=null)
				assignments.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, assignment);
		}
		return assignments;
	}
    
    @Override
    public Hashtable getAssignmentTable2(Collection classesOrClassIds) {
        return getAssignmentTable(classesOrClassIds);
    }
	
    @Override
	public Hashtable getAssignmentInfoTable(Collection classesOrClassIds) {
		Hashtable infos = new Hashtable();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			AssignmentPreferenceInfo info = (classOrClassId instanceof Class_ ? getAssignmentInfo((Class_)classOrClassId) : getAssignmentInfo((Long)classOrClassId));
			if (info!=null)
				infos.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, info);
		}
		return infos;
	}
    
    @Override
    public Hashtable getAssignmentInfoTable2(Collection classesOrClassIds) {
        return getAssignmentInfoTable(classesOrClassIds);
    }

    @Override
	public Vector getAssignmentRecords() {
		return iAssignmentRecords;
	}
	
    @Override
	public Vector getChangesToInitial() {
		Vector ret = new Vector();
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			for (Lecture lecture: currentSolution().getModel().variables()) {
				if (!ToolBox.equals(lecture.getInitialAssignment(),currentSolution().getAssignment().getValue(lecture))) {
					RecordedAssignment a = new RecordedAssignment(this,(Placement)lecture.getInitialAssignment(),currentSolution().getAssignment().getValue(lecture)); 
					if (lecture.getInitialAssignment()!=null) {
						a.getBefore().setDetails(new ClassAssignmentDetails(this,lecture,(Placement)lecture.getInitialAssignment(),false));
					}
					if (currentSolution().getAssignment().getValue(lecture)!=null) {
						a.getAfter().setDetails(new ClassAssignmentDetails(this,lecture,false));
					}
					ret.addElement(a);
				}
			}
		} finally {
			lock.unlock();
		}
		return ret;
	}
	
    @Override
	public List<ClassAssignmentDetails> getAssignedClasses(String... prefix) {
    	List<ClassAssignmentDetails> ret = new ArrayList<ClassAssignmentDetails>();
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			for (Lecture lecture: currentSolution().getAssignment().assignedVariables()) {
				if (prefix != null && prefix.length > 0) {
					boolean hasPrefix = false;
					for (String p: prefix)
						if (p == null || lecture.getName().startsWith(p)) { hasPrefix = true; break; }
					if (!hasPrefix) continue;
				}
				ret.add(new ClassAssignmentDetails(this,lecture,false));
			}
		} finally {
			lock.unlock();
		}
		return ret;
	}

    @Override
	public Vector getChangesToBest() {
		Vector ret = new Vector();
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			for (Lecture lecture: currentSolution().getModel().variables()) {
				Placement placement = currentSolution().getAssignment().getValue(lecture);
				if (!ToolBox.equals(lecture.getBestAssignment(), placement)) {
					RecordedAssignment a = new RecordedAssignment(this,(Placement)lecture.getBestAssignment(),placement);
					if (lecture.getBestAssignment()!=null) {
						a.getBefore().setDetails(new ClassAssignmentDetails(this,lecture,(Placement)lecture.getBestAssignment(),false));
					}
					if (placement!=null) {
						a.getAfter().setDetails(new ClassAssignmentDetails(this,lecture,false));
					}
					ret.addElement(a);
				}
			}
		} finally {
			lock.unlock();
		}
		return ret;
	}
	
    @Override
	public Vector getChangesToSolution(Long solutionId) {
		return getChangesToSolution(solutionId, false);
	}
	
	public Vector getChangesToSolution(Long solutionId, boolean closeSession) {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			Session hibSession = (new SolutionDAO()).getSession();
			Transaction tx = null;
			Vector ret = new Vector();
			try {
				tx = hibSession.beginTransaction();
				Solution solution = (new SolutionDAO()).get(solutionId, hibSession);
				if (solution==null) return null;
				Long ownerId = solution.getOwner().getUniqueId();
				Long[] ownerIds = getOwnerId();
				boolean sameOwner = false;
				for (int i=0;i<ownerIds.length;i++)
					if (ownerId.equals(ownerIds[i])) {
						sameOwner = true; break;
					}
				if (!sameOwner) return null;
				HashSet ids = new HashSet();
				for (Iterator i=solution.getAssignments().iterator();i.hasNext();) {
					Assignment assignment = (Assignment)i.next();
					Lecture lecture = null;
					for (Lecture l: currentSolution().getModel().variables()) {
						if (l.getClassId().equals(assignment.getClassId())) {
							lecture = l; break;
						}
					}
					ids.add(assignment.getClassId());
					Placement placement = (lecture == null ? null : currentSolution().getAssignment().getValue(lecture));
					if (lecture==null || placement==null) {
						RecordedAssignment a = new RecordedAssignment(this, assignment.getPlacement(), null);
						a.getBefore().setDetails(new ClassAssignmentDetails(solution, assignment, false, hibSession, null));
						ret.addElement(a);
					} else {
						if (placement.equals(assignment.getPlacement())) continue;
						RecordedAssignment a = new RecordedAssignment(this, assignment.getPlacement(), placement);
						a.getBefore().setDetails(new ClassAssignmentDetails(solution, assignment, false, hibSession, null));
						a.getAfter().setDetails(new ClassAssignmentDetails(this,lecture,false));
						ret.addElement(a);
					}
				}
				for (Lecture lecture: currentSolution().getModel().variables()) {
					Placement placement = currentSolution().getAssignment().getValue(lecture);
					if (ids.contains(lecture.getClassId()) || placement==null) continue;
					if (!ownerId.equals(lecture.getSolverGroupId())) continue;
					RecordedAssignment a = new RecordedAssignment(this, null, placement);
					a.getAfter().setDetails(new ClassAssignmentDetails(this,lecture,false));
					ret.addElement(a);
				}			
				if (tx!=null) tx.commit();
			} catch (Exception e) {
				if (tx!=null) tx.rollback();
			} finally {
				//here we still need to close the session since it can be called by the remote solver as well
				if (closeSession && hibSession!=null && hibSession.isOpen())
					hibSession.close();
			}
			return ret;
		} finally {
			lock.unlock();
		}
	}

	public static class AssignmentRecord implements Serializable {
		private static final long serialVersionUID = 1L;
		private transient Solver iSolver; 
		private Date iTimeStamp = new Date();
		private Suggestion iBefore = null, iAfter = null;
		private Vector iAssignments = new Vector(); 
		
		public AssignmentRecord() {}
		public AssignmentRecord(Solver solver) {
			iSolver = solver;
			iBefore = new Suggestion(iSolver);
		}
		public void done() {
			iAfter = new Suggestion(iSolver);
		}
		public void add(Placement before, Placement after) {
			iAssignments.add(new RecordedAssignment(iSolver, before, after));
		}
		public Date getTimeStamp() {
			return iTimeStamp;
		}
		public Suggestion getBefore() { return iBefore; }
		public Suggestion getAfter() { return iAfter; }
		public Vector getAssignments() { return iAssignments; }
		public String toString() {
			return "Record{TS="+iTimeStamp+", before="+iBefore+", after="+iAfter+", assignments="+iAssignments.size()+"}";
		}
		public void toXml(Element element) {
			if (iTimeStamp!=null)
				element.addAttribute("timeStamp", String.valueOf(iTimeStamp.getTime()));
			if (iBefore!=null)
				iBefore.toXml(element.addElement("before"));
			if (iAfter!=null)
				iAfter.toXml(element.addElement("after"));
			if (iAssignments!=null) {
				for (Enumeration e=iAssignments.elements();e.hasMoreElements();) {
					RecordedAssignment ra = (RecordedAssignment)e.nextElement();
					ra.toXml(element.addElement("assignment"));
				}
			}
		}
		public static AssignmentRecord fromXml(Element element) {
			AssignmentRecord r = new AssignmentRecord();
			if (element.attributeValue("timeStamp")!=null)
				r.iTimeStamp = new Date(Long.parseLong(element.attributeValue("timeStamp")));
			if (element.element("before")!=null)
				r.iBefore = Suggestion.fromXml(element.element("before"));
			if (element.element("after")!=null)
				r.iAfter = Suggestion.fromXml(element.element("after"));
			for (Iterator i=element.elementIterator("assignment");i.hasNext();) {
				r.iAssignments.add(RecordedAssignment.fromXml((Element)i.next()));
			}
			return r;
		}
	}
	
	public static class RecordedAssignment implements Serializable {
		private static final long serialVersionUID = 1L;
		private Hint iBefore = null, iAfter = null;
		public RecordedAssignment(Solver solver, Placement before, Placement after) {
			if (before!=null)
				iBefore = new Hint(solver,before,false);
			if (after!=null)
				iAfter = new Hint(solver,after,false);
		}
		public RecordedAssignment(Hint before, Hint after) {
			iBefore = before; iAfter = after;
		}
		public Hint getBefore() { return iBefore; }
		public Hint getAfter() { return iAfter; }
		public void toXml(Element element) {
			if (iBefore!=null)
				iBefore.toXml(element.addElement("before"));
			if (iAfter!=null)
				iAfter.toXml(element.addElement("after"));
		}
		public static RecordedAssignment fromXml(Element element) {
			Hint before = null, after = null;
			if (element.element("before")!=null) {
				before = Hint.fromXml(element.element("before"));
			}
			if (element.element("after")!=null) {
				after = Hint.fromXml(element.element("after"));
			}
			return new RecordedAssignment(before, after);
		}
	}
	
	@Override
	public RoomReport getRoomReport(BitSet sessionDays, int startDayDayOfWeek, Long roomType, Float nrWeeks) {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new RoomReport(this, sessionDays, startDayDayOfWeek, roomType, nrWeeks);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public DeptBalancingReport getDeptBalancingReport() {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new DeptBalancingReport(this);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public ViolatedDistrPreferencesReport getViolatedDistrPreferencesReport() {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new ViolatedDistrPreferencesReport(this);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public DiscouragedInstructorBtbReport getDiscouragedInstructorBtbReport() {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new DiscouragedInstructorBtbReport(this);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public StudentConflictsReport getStudentConflictsReport() {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new StudentConflictsReport(this);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public SameSubpartBalancingReport getSameSubpartBalancingReport() {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new SameSubpartBalancingReport(this);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public PerturbationReport getPerturbationReport() {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			return new PerturbationReport(this);
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public CSVFile export(boolean useAmPm) {
		Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
			CSVFile file = new CSVFile();
			file.setSeparator(",");
			file.setQuotationMark("\"");
			file.setHeader(new CSVField[] {
					new CSVField("COURSE"),
					new CSVField("ITYPE"),
					new CSVField("SECTION"),
					new CSVField("SUFFIX"),
					new CSVField("DATE_PATTERN"),
					new CSVField("DAY"),
					new CSVField("START_TIME"),
					new CSVField("END_TIME"),
					new CSVField("ROOM"),
					new CSVField("INSTRUCTOR")
			});
			
			Vector lectures = new Vector(currentSolution().getModel().variables());
			Collections.sort(lectures);
			for (Iterator i=lectures.iterator();i.hasNext();) {
				Lecture lecture = (Lecture)i.next();
				Placement placement = currentSolution().getAssignment().getValue(lecture);
				String name = lecture.getName();
				String itype = ""; String section = "";
				if (name.indexOf(' ')>=0) {
					section = name.substring(name.lastIndexOf(' ')+1);
					name = name.substring(0, name.lastIndexOf(' ')).trim();
				}
				String suffix="";
				while (section.charAt(section.length()-1)>='a' && section.charAt(section.length()-1)<='z') {
					suffix = section.charAt(section.length()-1)+suffix;
					section = section.substring(0,section.length()-1);
				}
				if (name.indexOf(' ')>=0) {
					itype = name.substring(name.lastIndexOf(' ')+1);
					name = name.substring(0, name.lastIndexOf(' ')).trim();
				}
				try {
					Integer.parseInt(itype);
					itype = name.substring(name.lastIndexOf(' ')+1)+" "+itype;
					name = name.substring(0, name.lastIndexOf(' ')).trim();
				} catch (NumberFormatException e) {}
				file.addLine(new CSVField[] {
						new CSVField(name),
						new CSVField(itype),
						new CSVField(section),
						new CSVField(suffix),
						new CSVField(placement==null?"":placement.getTimeLocation().getDatePatternName()),
						new CSVField(placement==null?"":placement.getTimeLocation().getDayHeader()),
						new CSVField(placement==null?"":placement.getTimeLocation().getStartTimeHeader(useAmPm)),
						new CSVField(placement==null?"":placement.getTimeLocation().getEndTimeHeader(useAmPm)),
						new CSVField(placement==null?"":placement.getRoomName(",")),
						new CSVField(lecture.getInstructorName()==null?"":lecture.getInstructorName())
				});
			}
			return file;
		} finally {
			lock.unlock();
		}
	}
    
	@Override
	public boolean hasFinalSectioning() {
		return ((TimetableModel)currentSolution().getModel()).getStudentSectioning().hasFinalSectioning();
	}
	
	@Override
	public boolean hasConflicts(Long offeringId) {
		return false;
	}

	@Override
	public Set<Assignment> getConflicts(Long classId) {
		return null;
	}
	
	@Override
	public Set<TimeBlock> getConflictingTimeBlocks(Long classId) {
		return null;
	}

	@Override
	public void save() {
		save(false, false);
	}
	
	@Override
	public SolverType getType() {
		return SolverType.COURSE;
	}
	
	@Override
	public boolean isRunning() {
		if (super.isRunning()) return true;
		if (iWorking && iWorkThread != null && iWorkThread instanceof InterruptibleThread && iWorkThread.isAlive() && !iWorkThread.isInterrupted())
			return true;
		return false;
	}
	
	@Override
	public void stopSolver() {
		if (super.isRunning()) super.stopSolver();
		if (iWorking && iWorkThread != null && iWorkThread instanceof InterruptibleThread && iWorkThread.isAlive() && !iWorkThread.isInterrupted()) {
			try {
				iWorkThread.interrupt();
				iWorkThread.join();
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public List<org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel> getTimetableGridTables(org.unitime.timetable.server.solver.TimetableGridContext context) {
		context.ensureLocalizationIsSet();
		List<org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel> models = new ArrayList<org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel>();
    	Query q = (context.getFilter() == null ? null : new Query(context.getFilter()));
    	Lock lock = currentSolution().getLock().readLock();
		lock.lock();
		try {
    		TimetableModel model = (TimetableModel)currentSolution().getModel();
    		switch (ResourceType.values()[context.getResourceType()]) {
    		case ROOM:
    			for (RoomConstraint rc: model.getRoomConstraints()) {
    				if (!match(q, rc)) continue;
    				models.add(TimetableGridSolverHelper.createModel(this, rc, context));
    			}
    			break;
    		case INSTRUCTOR:
    			for (InstructorConstraint ic: model.getInstructorConstraints()) {
    				if (!match(q, ic.getName())) continue;
    				models.add(TimetableGridSolverHelper.createModel(this, ic, context));
    			}
    			break;
    		case DEPARTMENT:
    			for (DepartmentSpreadConstraint dc: model.getDepartmentSpreadConstraints()) {
    				if (!match(q, dc.getName())) continue;
    				models.add(TimetableGridSolverHelper.createModel(this, dc, context));
    			}
    			if (model.getDepartmentSpreadConstraints().isEmpty()) {
    				org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment = currentSolution().getAssignment();
    				Map<Department, Set<Long>> dept2class = new HashMap<Department, Set<Long>>();
    				for (Object[] pair: (List<Object[]>)DepartmentDAO.getInstance().getSession().createQuery(
    						"select c.controllingDept, c.uniqueId from Class_ c where c.managingDept.solverGroup.uniqueId in :solverGroupIds"
    						).setParameterList("solverGroupIds", getOwnerId(), new LongType()).list()) {
    					Department dept = (Department)pair[0];
    					Long classId = (Long)pair[1];
    					Set<Long> classIds = dept2class.get(dept);
    					if (classIds == null) { classIds = new HashSet<Long>(); dept2class.put(dept, classIds); }
    					classIds.add(classId);
    				}
    				for (Department d: new TreeSet<Department>(dept2class.keySet())) {
    					if (!match(q, d.getShortLabel())) continue;
    					Set<Long> classIds = dept2class.get(d);
    					int size = 0;
    					List<Placement> placements = new ArrayList<Placement>();
    					for (Lecture lecture: getModel().variables()) {
    						if (classIds.contains(lecture.getClassId())) {
    							size ++;
    							Placement placement = assignment.getValue(lecture);
								if (placement != null) placements.add(placement);
    						}
    					}
    					if (size > 0)
    						models.add(TimetableGridSolverHelper.createModel(this, ResourceType.DEPARTMENT.ordinal(),
    								d.getUniqueId(), d.getShortLabel(), size, placements, context));
    				}
    			}
    			break;
    		case CURRICULUM:
    			Hashtable<String, List<Student>> curricula = new Hashtable<String, List<Student>>();
    			boolean hasCurricula = false;
    			HashSet<String> ignore = new HashSet<String>(), tested = new HashSet<String>();
    			for (Student student: model.getAllStudents()) {
    				if (student.getCurriculum() != null && !student.getCurriculum().isEmpty()) {
    					if (!hasCurricula) {
    						curricula.clear(); hasCurricula = true;
    					}
    					for (String c: student.getCurriculum().split("\\|")) {
        					if (tested.add(c) && !match(q, c)) ignore.add(c);
        					if (ignore.contains(c)) continue;
        					List<Student> students = curricula.get(c);
        					if (students == null) {
        						students = new ArrayList<Student>();
        						curricula.put(c, students);
        					}
        					students.add(student);
    					}
    				} else if (!hasCurricula && student.getAcademicArea() != null && student.getAcademicClassification() != null) {
    					String c = student.getAcademicArea() + (student.getMajor() == null ? "" : "/" + student.getMajor()) + " " + student.getAcademicClassification();
    					if (tested.add(c) && !match(q, c)) ignore.add(c);
    					if (ignore.contains(c)) continue;
    					List<Student> students = curricula.get(c);
    					if (students == null) {
    						students = new ArrayList<Student>();
    						curricula.put(c, students);
    					}
    					students.add(student);
    				}
    			}
				for (Map.Entry<String, List<Student>> curriculum: curricula.entrySet()) {
					models.add(TimetableGridSolverHelper.createModel(this, curriculum.getKey(), curriculum.getValue(), context));
				}
    			break;
    		case SUBJECT_AREA:
    			org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment = currentSolution().getAssignment();
				Map<SubjectArea, Set<Long>> sa2class = new HashMap<SubjectArea, Set<Long>>();
				for (Object[] pair: (List<Object[]>)DepartmentDAO.getInstance().getSession().createQuery(
						"select co.subjectArea, c.uniqueId from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where co.isControl = true and c.managingDept.solverGroup.uniqueId in :solverGroupIds"
						).setParameterList("solverGroupIds", getOwnerId(), new LongType()).list()) {
					SubjectArea sa = (SubjectArea)pair[0];
					Long classId = (Long)pair[1];
					Set<Long> classIds = sa2class.get(sa);
					if (classIds == null) { classIds = new HashSet<Long>(); sa2class.put(sa, classIds); }
					classIds.add(classId);
				}
				for (SubjectArea sa: new TreeSet<SubjectArea>(sa2class.keySet())) {
					if (!match(q, sa.getSubjectAreaAbbreviation())) continue;
					Set<Long> classIds = sa2class.get(sa);
					int size = 0;
					List<Placement> placements = new ArrayList<Placement>();
					for (Lecture lecture: getModel().variables()) {
						if (classIds.contains(lecture.getClassId())) {
							size ++;
							Placement placement = assignment.getValue(lecture);
							if (placement != null) placements.add(placement);
						}
					}
					if (size > 0)
						models.add(TimetableGridSolverHelper.createModel(this, ResourceType.SUBJECT_AREA.ordinal(),
								sa.getUniqueId(), sa.getSubjectAreaAbbreviation(), size, placements, context));
				}
				break;
    		case STUDENT_GROUP:
    			for (StudentGroup group: model.getStudentGroups()) {
					if (match(q, group.getName())) 
						models.add(TimetableGridSolverHelper.createModel(this, group, context));
    			}
    			break;
    		}
    	} finally {
    		lock.unlock();
    	}
		return models;
	}
}
