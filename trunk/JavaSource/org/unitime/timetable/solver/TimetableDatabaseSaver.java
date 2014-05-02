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
package org.unitime.timetable.solver;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.Test;
import org.cpsolver.coursett.TimetableSaver;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.ifs.extension.ConflictStatistics;
import org.cpsolver.ifs.extension.Extension;
import org.cpsolver.ifs.util.Progress;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Transaction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalSolutionCommitAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolutionInfo;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverInfoDef;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.StudentEnrollment;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.BtbInstructorConstraintInfo;
import org.unitime.timetable.solver.ui.ConflictStatisticsInfo;
import org.unitime.timetable.solver.ui.GroupConstraintInfo;
import org.unitime.timetable.solver.ui.JenrlInfo;
import org.unitime.timetable.solver.ui.LogInfo;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.solver.ui.TimetableInfoUtil;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class TimetableDatabaseSaver extends TimetableSaver {
	private static Log sLog = LogFactory.getLog(TimetableDatabaseSaver.class);
	private Long iSessionId;
	private Long[] iSolverGroupId;
	private Long[] iSolutionId;
	private boolean iCreateNew = true;
	private boolean iCommitSolution = false;
    private boolean iStudentSectioning = false;
	
	private Hashtable<Long, Assignment> iAssignments = new Hashtable<Long, Assignment>();
	private Hashtable<Long, Solution> iSolutions = new Hashtable<Long, Solution>();
	
	private Progress iProgress = null;
	
    private static int BATCH_SIZE = 100;
    
	public TimetableDatabaseSaver(org.cpsolver.ifs.solver.Solver solver) {
        super(solver);
        iProgress = Progress.getInstance(getModel());
        iSessionId = new Long(getModel().getProperties().getPropertyLong("General.SessionId",-1));
        iSolverGroupId = getModel().getProperties().getPropertyLongArry("General.SolverGroupId",null);
        iSolutionId = getModel().getProperties().getPropertyLongArry("General.SolutionId",null);
        iCreateNew = getModel().getProperties().getPropertyBoolean("General.CreateNewSolution",iCreateNew);
        iCommitSolution = getModel().getProperties().getPropertyBoolean("General.CommitSolution",iCommitSolution);
        iStudentSectioning = getModel().getProperties().getPropertyBoolean("General.RunStudentSectioningOnSave", iStudentSectioning);
    }
	
	public TimetableInfoFileProxy getFileProxy() {
		TimetableInfoFileProxy proxy = null;
		if (getSolver() instanceof TimetableSolver)
			proxy = ((TimetableSolver)getSolver()).getFileProxy();
		return (proxy != null ? proxy : TimetableInfoUtil.getInstance());
	}
	
	private Solution getSolution(Lecture lecture, org.hibernate.Session hibSession) {
		if (lecture.getSolverGroupId()!=null)
			return (Solution)iSolutions.get(lecture.getSolverGroupId());
		else {
			Class_ clazz = (new Class_DAO()).get(lecture.getClassId(), hibSession);
			if (clazz==null) return null;
			SolverGroup sg = clazz.getManagingDept().getSolverGroup();
			if (sg==null) return null;
			return (Solution)iSolutions.get(sg.getUniqueId());
		}
	}
	
	public void refreshCourseSolution(Long solutionId) {
		if (SolverServerImplementation.getInstance() != null)
			SolverServerImplementation.getInstance().refreshCourseSolution(solutionId);
	}
	
    public void save() {
    	org.hibernate.Session hibSession = null;
    	Transaction tx = null;
    	try {
    		TimetableManagerDAO dao = new TimetableManagerDAO();
    		hibSession = dao.getSession();
    		hibSession.setCacheMode(CacheMode.IGNORE);
    		hibSession.setFlushMode(FlushMode.COMMIT);

    		tx = hibSession.beginTransaction(); 
    		
    		Long[] solutionIds = save(hibSession);
    		
    		tx.commit();
    		
            HashSet refreshIds = new HashSet();
    		if (iCommitSolution && solutionIds!=null) {
    			HashSet<Solution> touchedSolutions = new HashSet<Solution>();
    			if (hibSession!=null && hibSession.isOpen()) hibSession.close();
    			hibSession = dao.getSession();
    			
    			iProgress.setPhase("Committing solution ...", 2*solutionIds.length);
    			tx = hibSession.beginTransaction();
    			for (int i=0;i<solutionIds.length;i++) {
    				Solution solution = (new SolutionDAO()).get(solutionIds[i]);
    				Solution committedSolution = solution.getOwner().getCommittedSolution();
    				if (committedSolution!=null) {
    					committedSolution.uncommitSolution(hibSession, getModel().getProperties().getProperty("General.OwnerPuid"));
                        refreshIds.add(committedSolution.getUniqueId());
                        touchedSolutions.add(committedSolution);
                    }
    				touchedSolutions.add(solution);
    				iProgress.incProgress();
    			}
    			for (int i=0;i<solutionIds.length;i++) {
    				Solution solution = (new SolutionDAO()).get(solutionIds[i]);
    				List<String> messages = new ArrayList<String>();
    				solution.commitSolution(messages,hibSession, getModel().getProperties().getProperty("General.OwnerPuid"));
    				touchedSolutions.add(solution);
    				for (String m: messages) {
    					iProgress.error("Unable to commit: "+m);
    				}
    				hibSession.update(solution);
    				iProgress.incProgress();
    			}
				tx.commit();
		    	String className = ApplicationProperty.ExternalActionSolutionCommit.value();
		    	if (className != null && className.trim().length() > 0){
		    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());
		    		commitAction.performExternalSolutionCommitAction(touchedSolutions, hibSession);
		    	}
    		}
    		
            iProgress.setPhase("Refreshing solution ...", solutionIds.length+refreshIds.size());
            for (Iterator i=refreshIds.iterator();i.hasNext();) {
                Long solutionId = (Long)i.next();
                refreshCourseSolution(solutionId);
                try {
                } catch (Exception e) {
                    iProgress.warn("Unable to refresh solution "+solutionId+", reason:"+e.getMessage(),e);
                }
                iProgress.incProgress();
            }
            for (int i=0;i<solutionIds.length;i++) {
                try {
                	refreshCourseSolution(solutionIds[i]);
                } catch (Exception e) {
                    iProgress.warn("Unable to refresh solution "+solutionIds[i]+", reason:"+e.getMessage(),e);
                }
                iProgress.incProgress();
            }
            
    		if (solutionIds!=null) {
    			getModel().getProperties().setProperty("General.SolutionId",solutionIds);
    			iProgress.info("Solution successfully saved.");

    			if (hibSession!=null && hibSession.isOpen()) hibSession.close();
    			hibSession = dao.getSession();
    			
    			for (int i=0;i<solutionIds.length;i++) {
        			tx = hibSession.beginTransaction();
        			Solution solution = (new SolutionDAO()).get(solutionIds[i]);
        			LogInfo lInfo = new LogInfo();
        			lInfo.setLog(iProgress.getLog());
        			SolutionInfo logInfo = new SolutionInfo();
        			logInfo.setDefinition(SolverInfoDef.findByName(hibSession,"LogInfo"));
        			logInfo.setOpt(null);
        			logInfo.setSolution(solution);
        			logInfo.setInfo(lInfo,getFileProxy());
        			hibSession.save(logInfo);
        			tx.commit();
    			}
    		}
    	} catch (Exception e) {
    		iProgress.fatal("Unable to save timetable, reason: "+e.getMessage(),e);
    		sLog.error(e.getMessage(),e);
            tx.rollback();
    	} finally {
    		// here we need to close the session since this code may run in a separate thread
    		if (hibSession!=null && hibSession.isOpen()) hibSession.close();
    	}
    }
    
    private Long[] save(org.hibernate.Session hibSession) throws Exception {
            
            if (iStudentSectioning) getModel().switchStudents(getAssignment());
        
    		iProgress.setStatus("Saving solution ...");
    		
    		if (iSolverGroupId==null || iSolverGroupId.length==0) {
    			iProgress.fatal("No solver group loaded.");
    			return null;
    		}
    		
    		Hashtable solverGroups = new Hashtable();
    		for (int i=0;i<iSolverGroupId.length;i++) {
    			SolverGroup solverGroup = SolverGroupDAO.getInstance().get(iSolverGroupId[i], hibSession);
    			if (solverGroup==null) {
        			iProgress.fatal("Unable to load solver group "+iSolverGroupId[i]+".");
        			return null;
    			}
    			solverGroups.put(solverGroup.getUniqueId(),solverGroup);
    			iProgress.debug("solver group ["+(i+1)+"]: "+solverGroup.getName());
    		}
    		
    		iSolutions = new Hashtable();
    		if (!iCreateNew && iSolutionId!=null && iSolutionId.length>=0) {
    			for (int i=0;i<iSolverGroupId.length;i++) {
    				if (i<iSolutionId.length && iSolutionId[i]!=null) {
    					Solution solution = (new SolutionDAO()).get(iSolutionId[i], hibSession);
    					if (solution==null) {
    						iProgress.warn("Unable to load solution "+iSolutionId[i]);
    						continue;
    					}
    					if (!solverGroups.containsKey(solution.getOwner().getUniqueId())) {
    						iProgress.warn("Solution "+iSolutionId[i]+" ignored -- it does not match with the owner(s) of the problem");
    						continue;
    					}
    					if (solution.isCommited().booleanValue()){
    						solution.uncommitSolution(hibSession, getModel().getProperties().getProperty("General.OwnerPuid"));
    						if (!iCommitSolution){
    							String className = ApplicationProperty.ExternalActionSolutionCommit.value();
    					    	if (className != null && className.trim().length() > 0){
    					    		HashSet<Solution> touchedSolutions = new HashSet<Solution>();
    					    		touchedSolutions.add(solution);
    					    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());
    					    		commitAction.performExternalSolutionCommitAction(touchedSolutions, hibSession);
    					    	}

    						}
    					}
    					solution.empty(hibSession, getFileProxy());
    					iSolutions.put(solution.getOwner().getUniqueId(),solution);
    				}
    			}
    		}
    		
    		Session session = SessionDAO.getInstance().get(iSessionId, hibSession);
    		if (session==null) {
    			iProgress.fatal("No session loaded.");
    			return null;
    		}
    		iProgress.debug("session: "+session.getLabel());
    		
    		for (Enumeration e=solverGroups.elements();e.hasMoreElements();) {
    			SolverGroup solverGroup = (SolverGroup)e.nextElement();
    			Solution solution = (Solution)iSolutions.get(solverGroup.getUniqueId());
    			if (solution==null) {
    				solution = new Solution();
    				iSolutions.put(solverGroup.getUniqueId(), solution);
    			}
        		solution.setCommitDate(null);
        		solution.setCreated(new Timestamp((new Date()).getTime()));
        		solution.setCreator(Test.getVersionString());
        		solution.setNote(getModel().getProperties().getProperty("General.Note"));
        		solution.setOwner(solverGroup);
        		solverGroup.getSolutions().add(solution);
        		solution.setValid(Boolean.TRUE);
        		solution.setCommited(Boolean.FALSE);
        		
        		iProgress.setPhase("Saving solver parameters ...", getModel().getProperties().size());
        		HashSet params = new HashSet();
        		for (Iterator i1=getModel().getProperties().entrySet().iterator();i1.hasNext();) {
        			Map.Entry entry = (Map.Entry)i1.next();
        			String name = (String)entry.getKey();
        			String value = (String)entry.getValue();
        			SolverParameterDef def = SolverParameterDef.findByNameType(hibSession, name, SolverParameterGroup.sTypeCourse);
        			if (def!=null) {
        				iProgress.trace("save "+name+"="+value);
        				SolverParameter param = new SolverParameter();
        				param.setDefinition(def);
        				param.setValue(value);
        				hibSession.save(param);
        				params.add(param);
        			}
        			iProgress.incProgress();
        		}
        		solution.setParameters(params);

        		hibSession.saveOrUpdate(solution);
    		}
    		
    		hibSession.flush(); hibSession.clear(); int batchIdx = 0;
    		
    		iProgress.setPhase("Saving assignments ...", getModel().variables().size());
    		for (Lecture lecture: getModel().variables()) {
    			Placement placement = getAssignment().getValue(lecture);
    			if (placement!=null) {
    				iProgress.trace("save "+lecture.getName()+" "+placement.getName());
    				Class_ clazz = (new Class_DAO()).get(lecture.getClassId(),hibSession);
    				if (clazz==null) {
    					iProgress.warn("Unable to save assignment for class "+lecture+" ("+placement.getLongName()+") -- class (id:"+lecture.getClassId()+") does not exist.");
    					continue;
    				}
        			HashSet rooms = new HashSet();
        			if (placement.isMultiRoom()) {
        				for (RoomLocation r: placement.getRoomLocations()) {
            				Location room = (new LocationDAO()).get(r.getId(), hibSession);
            				if (room==null) {
               					iProgress.warn("Unable to save assignment for class "+lecture+" ("+placement.getLongName()+") -- room (id:"+r.getId()+") does not exist.");
               					continue;
            				}
            				rooms.add(room);
        				}
        				if (rooms.size()!=placement.getRoomLocations().size())
        					continue;
        			} else {
        				Location room = (new LocationDAO()).get(placement.getRoomLocation().getId(), hibSession);
        				if (room==null) {
           					iProgress.warn("Unable to save assignment for class "+lecture+" ("+placement.getLongName()+") -- room (id:"+placement.getRoomLocation().getId()+") does not exist.");
           					continue;
        				}
        				rooms.add(room);
        			}
        			
        			HashSet instructors = new HashSet();
        			for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
            			DepartmentalInstructor instructor = null;
            			if (ic.getPuid()!=null && ic.getPuid().length()>0) {
            				instructor = DepartmentalInstructor.findByPuidDepartmentId(ic.getPuid(), clazz.getControllingDept().getUniqueId()); 
            			} else if (ic.getResourceId()!=null) {
        					instructor = (new DepartmentalInstructorDAO()).get(ic.getResourceId(), hibSession);
        				}
            			if (instructor!=null) instructors.add(instructor);
        			}
    				
    				TimePattern pattern = (new TimePatternDAO()).get(placement.getTimeLocation().getTimePatternId(),hibSession);
    				if (pattern==null) {
       					iProgress.warn("Unable to save assignment for class "+lecture+" ("+placement.getLongName()+") -- time pattern (id:"+placement.getTimeLocation().getTimePatternId()+") does not exist.");
       					continue;
    				}
    				Solution solution = getSolution(lecture, hibSession);
    				if (solution==null) {
   						iProgress.warn("Unable to save assignment for class "+lecture+" ("+placement.getLongName()+") -- none or wrong solution group assigned to the class");
   						continue;
    				}
        			Assignment assignment = new Assignment();
        			assignment.setClazz(clazz);
        			assignment.setClassId(clazz.getUniqueId());
        			assignment.setClassName(lecture.getName());
        			assignment.setDays(new Integer(placement.getTimeLocation().getDayCode()));
        			assignment.setStartSlot(new Integer(placement.getTimeLocation().getStartSlot()));
        			assignment.setTimePattern(pattern);
        			if (placement.getTimeLocation().getDatePatternId() != null)
        				assignment.setDatePattern(DatePatternDAO.getInstance().get(placement.getTimeLocation().getDatePatternId(), hibSession));
        			assignment.setRooms(rooms);
        			assignment.setInstructors(instructors);
        			assignment.setSolution(solution);
        			hibSession.save(assignment);
        			iAssignments.put(lecture.getClassId(),assignment);
    				if (++batchIdx % BATCH_SIZE == 0) {
    					hibSession.flush(); hibSession.clear();
    				}
    			}
    			iProgress.incProgress();
    		}
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		if (getModel().getProperties().getPropertyBoolean("General.SaveStudentEnrollments", true)) {
        		iProgress.setPhase("Saving student enrollments ...", getModel().variables().size());
        		for (Lecture lecture: getModel().variables()) {
        			Class_ clazz = (new Class_DAO()).get(lecture.getClassId(),hibSession);
        			if (clazz==null) continue;
        			iProgress.trace("save "+lecture.getName());
    				Solution solution = getSolution(lecture, hibSession);
    				if (solution==null) {
    						iProgress.warn("Unable to save student enrollments for class "+lecture+"  -- none or wrong solution group assigned to the class");
    						continue;
    				}
        			
        			for (Iterator i2=lecture.students().iterator();i2.hasNext();) {
        				Student student = (Student)i2.next();
        				StudentEnrollment enrl = new StudentEnrollment();
        				enrl.setStudentId(student.getId());
        				enrl.setClazz(clazz);
        				enrl.setSolution(solution);
        				hibSession.save(enrl);
        				if (++batchIdx % BATCH_SIZE == 0) {
        					hibSession.flush(); hibSession.clear();
        				}
        			}
        			
        			iProgress.incProgress();
        		}
        		
        		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		}
    		
    		/**  // is this needed?
    		iProgress.setPhase("Saving joint enrollments ...", getModel().getJenrlConstraints().size());
    		for (Enumeration e1=getModel().getJenrlConstraints().elements();e1.hasMoreElements();) {
    			JenrlConstraint jenrlConstraint = (JenrlConstraint)e1.nextElement();
    			
    			Class_ clazz1 = (new Class_DAO()).get(((Lecture)jenrlConstraint.first()).getClassId());
    			Class_ clazz2 = (new Class_DAO()).get(((Lecture)jenrlConstraint.second()).getClassId());
    			
    			JointEnrollment jenrl = new JointEnrollment();
    			jenrl.setJenrl(new Double(jenrlConstraint.getJenrl()));
    			jenrl.setClass1(clazz1);
    			jenrl.setClass2(clazz2);
    			jenrl.setSolution(solution);
    			hibSession.save(jenrl);
    			
    			iProgress.incProgress();
    		}
    		*/
    		
    		SolverInfoDef defGlobalInfo = SolverInfoDef.findByName(hibSession,"GlobalInfo");
    		if (defGlobalInfo==null)
    			iProgress.warn("Global info is not registered.");
    		SolverInfoDef defCbsInfo = SolverInfoDef.findByName(hibSession,"CBSInfo");
    		if (defCbsInfo==null)
    			iProgress.warn("Constraint-based statistics info is not registered.");
    		SolverInfoDef defAssignmentInfo = SolverInfoDef.findByName(hibSession,"AssignmentInfo");
    		if (defAssignmentInfo==null)
    			iProgress.warn("Assignment info is not registered.");
    		SolverInfoDef defDistributionInfo = SolverInfoDef.findByName(hibSession,"DistributionInfo");
    		if (defDistributionInfo==null)
    			iProgress.warn("Distribution constraint info is not registered.");
    		SolverInfoDef defJenrlInfo = SolverInfoDef.findByName(hibSession,"JenrlInfo");
    		if (defJenrlInfo==null)
    			iProgress.warn("Joint enrollments info is not registered.");
    		SolverInfoDef defLogInfo = SolverInfoDef.findByName(hibSession,"LogInfo");
    		if (defLogInfo==null)
    			iProgress.warn("Solver log info is not registered.");
    		SolverInfoDef defBtbInstrInfo = SolverInfoDef.findByName(hibSession,"BtbInstructorInfo");
    		if (defBtbInstrInfo==null)
    			iProgress.warn("Back-to-back instructor info is not registered.");
    		
    		Hashtable<Solution, List<Lecture>> lectures4solution = new Hashtable<Solution, List<Lecture>>();
    		for (Lecture lecture: getModel().variables()) {
       			Solution s = getSolution(lecture, hibSession);
       			if (s==null) continue;
       			List<Lecture> lectures = lectures4solution.get(s);
       			if (lectures==null) {
       				lectures = new ArrayList<Lecture>();
       				lectures4solution.put(s, lectures);
       			}
       			lectures.add(lecture);
    		}
    		
   			iProgress.setPhase("Saving global info ...", solverGroups.size());
   			for (Enumeration e=solverGroups.elements();e.hasMoreElements();) {
   				SolverGroup solverGroup = (SolverGroup)e.nextElement();
   				Solution solution = (Solution)iSolutions.get(solverGroup.getUniqueId());
   				List<Lecture> lectures = lectures4solution.get(solution);
   				if (lectures==null) lectures = new ArrayList<Lecture>(0);
           		SolutionInfo solutionInfo = new SolutionInfo();
           		solutionInfo.setDefinition(defGlobalInfo);
           		solutionInfo.setOpt(null);
           		solutionInfo.setSolution(solution);
           		solutionInfo.setInfo(new PropertiesInfo(getSolution().getInfo(lectures)),getFileProxy());
           		hibSession.save(solutionInfo);
                solution.setGlobalInfo(solutionInfo);
   	    		iProgress.incProgress();
    		}
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		ConflictStatistics cbs = null;
    		for (Extension ext: getSolver().getExtensions()) {
    			if (ext instanceof ConflictStatistics) {
    				cbs = (ConflictStatistics)ext; break;
    			}
    		}
    		if (cbs!=null && cbs.getNoGoods()!=null) {
    			ConflictStatisticsInfo cbsInfo = new ConflictStatisticsInfo();
    			cbsInfo.load(getSolver(), cbs);
    			iProgress.setPhase("Saving conflict-based statistics ...", 1);
    			for (Enumeration e=iSolutions.elements();e.hasMoreElements();) {
    				Solution solution = (Solution)e.nextElement();
    				List<Lecture> lectures = lectures4solution.get(solution);
    				if (lectures==null) lectures = new ArrayList<Lecture>(0);
            		SolutionInfo cbsSolutionInfo = new SolutionInfo();
            		cbsSolutionInfo.setDefinition(defCbsInfo);
            		cbsSolutionInfo.setOpt(null);
            		cbsSolutionInfo.setSolution(solution);
            		cbsSolutionInfo.setInfo(cbsInfo.getConflictStatisticsSubInfo(lectures),getFileProxy());
            		hibSession.save(cbsSolutionInfo);
    				if (++batchIdx % BATCH_SIZE == 0) {
    					hibSession.flush(); hibSession.clear();
    				}
    			}
        		iProgress.incProgress();
    		}
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		iProgress.setPhase("Saving variable infos ...", getModel().variables().size());
    		for (Lecture lecture: getModel().variables()) {
    			Placement placement = getAssignment().getValue(lecture);
    			if (placement!=null) {
    				Assignment assignment = (Assignment)iAssignments.get(lecture.getClassId()); 
    				AssignmentInfo assignmentInfo = new AssignmentInfo();
    				assignmentInfo.setAssignment(assignment);
    				assignmentInfo.setDefinition(defAssignmentInfo);
    				assignmentInfo.setOpt(null);
    				assignmentInfo.setInfo(new AssignmentPreferenceInfo(getSolver(),placement,true,true),getFileProxy());
    				hibSession.save(assignmentInfo);
    				if (++batchIdx % BATCH_SIZE == 0) {
    					hibSession.flush(); hibSession.clear();
    				}
    			}
    			iProgress.incProgress();
    		}
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		iProgress.setPhase("Saving btb instructor infos ...", getModel().variables().size());
    		for (Lecture lecture1: getModel().variables()) {
    			Placement placement1 = (Placement)getAssignment().getValue(lecture1);
    			iProgress.incProgress();
    			if (placement1 == null) continue;
    			for (InstructorConstraint ic: lecture1.getInstructorConstraints()) {
    				for (Lecture lecture2: ic.variables()) {
    					Placement placement2 = (Placement)getAssignment().getValue(lecture2);
    					if (placement2 == null || lecture2.getClassId().compareTo(lecture1.getClassId()) <= 0) continue;
    					int pref = ic.getDistancePreference(placement1, placement2);
    					if (pref==PreferenceLevel.sIntLevelNeutral) continue;
    					iProgress.trace("Back-to-back instructor constraint ("+pref+") between "+placement1+" and "+placement2);
    					BtbInstructorConstraintInfo biInfo = new BtbInstructorConstraintInfo();
    					biInfo.setPreference(pref);
    					biInfo.setInstructorId(ic.getResourceId());
    					ConstraintInfo constraintInfo = new ConstraintInfo();
    					constraintInfo.setDefinition(defBtbInstrInfo);
    					constraintInfo.setOpt(String.valueOf(ic.getResourceId()));
    					HashSet biAssignments = new HashSet();
    					Assignment assignment = (Assignment)iAssignments.get(lecture1.getClassId());
    					if (assignment!=null)
    						biAssignments.add(assignment);
    					assignment = (Assignment)iAssignments.get(lecture2.getClassId());
    					if (assignment!=null)
    						biAssignments.add(assignment);
    					if (!biAssignments.isEmpty()) {
    						constraintInfo.setAssignments(biAssignments);
    						constraintInfo.setInfo(biInfo,getFileProxy());
    						hibSession.save(constraintInfo);
    						if (++batchIdx % BATCH_SIZE == 0) {
    							hibSession.flush(); hibSession.clear();
    						}
    					} else {
    						iProgress.trace("   NO ASSIGNMENTS !!!");
    					}
    				}
    			}
    		}

    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		iProgress.setPhase("Saving group constraint infos ...", getModel().getGroupConstraints().size());
    		for (GroupConstraint gc: getModel().getGroupConstraints()) {
    			GroupConstraintInfo gcInfo = new GroupConstraintInfo(getAssignment(), gc);
    			ConstraintInfo constraintInfo = new ConstraintInfo();
    			constraintInfo.setDefinition(defDistributionInfo);
    			constraintInfo.setOpt(gcInfo.isSatisfied()?"1":"0");
    			iProgress.trace("Distribution constraint "+gcInfo.getName()+" (p:"+gcInfo.getPreference()+", s:"+gcInfo.isSatisfied()+") between");
    			HashSet gcAssignments = new HashSet();
    			for (Lecture lecture: gc.variables()) {
    				Assignment assignment = (Assignment)iAssignments.get(lecture.getClassId());
    				iProgress.trace("  "+getAssignment().getValue(lecture));
    				if (assignment!=null)
    					gcAssignments.add(assignment);
    			}
    			
    			if (!gcAssignments.isEmpty()) {
    				constraintInfo.setAssignments(gcAssignments);
        			constraintInfo.setInfo(gcInfo,getFileProxy());
    				hibSession.save(constraintInfo);
    				if (++batchIdx % BATCH_SIZE == 0) {
    					hibSession.flush(); hibSession.clear();
    				}
    			} else {
    				iProgress.trace("   NO ASSIGNMENTS !!!");
    			}
    			
    			iProgress.incProgress();
    		}
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		iProgress.setPhase("Saving student enrollment infos ...", getModel().getJenrlConstraints().size());
    		for (JenrlConstraint jc: getModel().getJenrlConstraints()) {
    			if (!jc.isInConflict(getAssignment()) || !jc.isOfTheSameProblem()) {
    				iProgress.incProgress();
    				continue;
    			}
    			JenrlInfo jInfo = new JenrlInfo(getSolver(), jc);
    			ConstraintInfo constraintInfo = new ConstraintInfo();
    			constraintInfo.setDefinition(defJenrlInfo);
    			constraintInfo.setOpt((jInfo.isSatisfied()?"S":"")+(jInfo.isHard()?"H":"")+(jInfo.isDistance()?"D":"")+(jInfo.isFixed()?"F":"")+(jInfo.isImportant()?"I":"")+(jInfo.isInstructor()?"X":""));
    			Assignment firstAssignment = (Assignment)iAssignments.get(((Lecture)jc.first()).getClassId());
    			Assignment secondAssignment = (Assignment)iAssignments.get(((Lecture)jc.second()).getClassId());
    			if (firstAssignment==null || secondAssignment==null) continue;
       			HashSet jAssignments = new HashSet();
    			jAssignments.add(firstAssignment);
    			jAssignments.add(secondAssignment);
   				constraintInfo.setAssignments(jAssignments);
       			constraintInfo.setInfo(jInfo,getFileProxy());
   				hibSession.save(constraintInfo);
   				if (++batchIdx % BATCH_SIZE == 0) {
   					hibSession.flush(); hibSession.clear();
   				}
    			
    			iProgress.incProgress();
    		}
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		iProgress.setPhase("Saving committed student enrollment infos ...", iSolutions.size());
    		for (Enumeration e=iSolutions.elements();e.hasMoreElements();) {
    			Solution solution = (Solution)e.nextElement();
    			solution.updateCommittedStudentEnrollmentInfos(hibSession);
    			iProgress.incProgress();
    		}
    		iProgress.incProgress();
    		
    		/*
    		iProgress.setPhase("Saving committed student enrollment infos ...", getModel().assignedVariables().size());
    		for (Enumeration e1=getModel().assignedVariables().elements();e1.hasMoreElements();) {
    			Lecture lecture = (Lecture)e1.nextElement();
    			Assignment assignment = (Assignment)iAssignments.get(lecture.getClassId());
				if (assignment==null) continue;
				Hashtable infos = JenrlInfo.getCommitedJenrlInfos(lecture);
    			for (Iterator i2=infos.entrySet().iterator();i2.hasNext();) {
    				Map.Entry entry = (Map.Entry)i2.next();
    				Integer assignmentId = (Integer)entry.getKey();
    				JenrlInfo jInfo = (JenrlInfo)entry.getValue();
    				Assignment other = (new AssignmentDAO()).get(assignmentId,hibSession);
    				if (other==null) continue;
        			ConstraintInfo constraintInfo = new ConstraintInfo();
        			constraintInfo.setDefinition(defJenrlInfo);
        			constraintInfo.setOpt("C"+(jInfo.isSatisfied()?"S":"")+(jInfo.isHard()?"H":"")+(jInfo.isDistance()?"D":"")+(jInfo.isFixed()?"F":""));
        			HashSet jAssignments = new HashSet();
        			jAssignments.add(assignment);
        			jAssignments.add(other);
        			constraintInfo.setAssignments(jAssignments);
        			constraintInfo.setInfo(jInfo,getFileProxy());
        			hibSession.save(constraintInfo);
    				if (++batchIdx % BATCH_SIZE == 0) {
    					hibSession.flush(); hibSession.clear();
    				}
    			}
    			iProgress.incProgress();
    		}
    		*/    	
    		
    		hibSession.flush(); hibSession.clear(); batchIdx = 0;
    		
    		iProgress.setPhase("Done",1);iProgress.incProgress();
    		
    		Long ret[] = new Long[iSolutions.size()]; int idx=0;
    		for (Enumeration e=iSolutions.elements();e.hasMoreElements();)
    			ret[idx++] = ((Solution)e.nextElement()).getUniqueId();
			
    		return ret;
    }

}
