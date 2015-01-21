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
package org.unitime.timetable.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.heuristics.BacktrackNeighbourSelection;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solution.SolutionListener;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.solver.SolverListener;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.StudentSectioningXMLSaver;
import org.cpsolver.studentsct.check.InevitableStudentConflicts;
import org.cpsolver.studentsct.check.OverlapCheck;
import org.cpsolver.studentsct.check.SectionLimitCheck;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.report.CourseConflictTable;
import org.cpsolver.studentsct.report.DistanceConflictTable;
import org.unitime.commons.hibernate.util.HibernateUtil;


/**
 * @author Tomas Muller
 */
public class BatchStudentSectioningTest {
    private static Log sLog = LogFactory.getLog(BatchStudentSectioningTest.class);
    private static DecimalFormat sDF = new DecimalFormat("0.000");
    private static boolean sIncludeCourseDemands = true;
    private static boolean sIncludeLastLikeStudents = true;
    private static boolean sIncludeUseCommittedAssignments = false;
    
    public static void batchSectioning(DataProperties cfg) {
        StudentSectioningModel model = new StudentSectioningModel(cfg);
        DefaultSingleAssignment<Request, Enrollment> assignment = new DefaultSingleAssignment<Request, Enrollment>();
        try {
            new BatchStudentSectioningLoader(model, assignment).load();
        } catch (Exception e) {
            sLog.error("Unable to load problem, reason: "+e.getMessage(),e);
            return;
        }
        
        Solver solver = new Solver(cfg);
        Solution solution = new Solution(model, assignment, 0, 0);
        solver.setInitalSolution(solution);
        solver.addSolverListener(new SolverListener<Request, Enrollment>() {
            public boolean variableSelected(Assignment<Request, Enrollment> assignment, long iteration, Request variable) {
                return true;
            }
            public boolean valueSelected(Assignment<Request, Enrollment> assignment, long iteration, Request variable, Enrollment value) {
                return true;
            }
            public boolean neighbourSelected(Assignment<Request, Enrollment> assignment, long iteration, Neighbour<Request, Enrollment> neighbour) {
                sLog.debug("Select["+iteration+"]: "+neighbour);
                return true;
            }
			public void neighbourFailed(Assignment<Request, Enrollment> assignment, long iteration, Neighbour<Request, Enrollment> neighbour) {
			}
        });
        solution.addSolutionListener(new SolutionListener() {
            public void solutionUpdated(Solution solution) {}
            public void getInfo(Solution solution, java.util.Map info) {}
            public void getInfo(Solution solution, java.util.Map info, java.util.Collection variables) {}
            public void bestCleared(Solution solution) {}
            public void bestSaved(Solution solution) {
                StudentSectioningModel m = (StudentSectioningModel)solution.getModel();
                Assignment<Request, Enrollment> a = solution.getAssignment();
                sLog.debug("**BEST** V:"+m.nrAssignedVariables(a)+"/"+m.variables().size()+" - S:"+m.getContext(a).nrComplete()+"/"+m.getStudents().size()+" - TV:"+sDF.format(m.getTotalValue(a)));
            }
            public void bestRestored(Solution solution) {}
        });
        
        try {
            new StudentSectioningXMLSaver(solver).save(new File(new File(cfg.getProperty("General.Output",".")),"input.xml"));
        } catch (Exception e) {
            sLog.error("Unable to save input data, reason: "+e.getMessage(),e);
        }
        
        solver.start();
        try {
            solver.getSolverThread().join();
        } catch (InterruptedException e) {}
        
        solution = solver.lastSolution();
        solution.restoreBest();
        
        model = (StudentSectioningModel)solution.getModel();
        
        try {
            File outDir = new File(cfg.getProperty("General.Output","."));
            outDir.mkdirs();

            CourseConflictTable cct = new CourseConflictTable((StudentSectioningModel)solution.getModel());
            cct.createTable(assignment, true, false, true).save(new File(outDir, "conflicts-lastlike.csv"));
            cct.createTable(assignment, false, true, true).save(new File(outDir, "conflicts-real.csv"));
            
            DistanceConflictTable dct = new DistanceConflictTable((StudentSectioningModel)solution.getModel());
            dct.createTable(assignment, true, false, true).save(new File(outDir, "distances-lastlike.csv"));
            dct.createTable(assignment, false, true, true).save(new File(outDir, "distances-real.csv"));
            
            if (cfg.getPropertyBoolean("Test.InevitableStudentConflictsCheck", false)) {
                InevitableStudentConflicts ch = new InevitableStudentConflicts(model);
                if (!ch.check(assignment)) ch.getCSVFile().save(new File(outDir, "inevitable-conflicts.csv"));
            }
        } catch (IOException e) {
            sLog.error(e.getMessage(),e);
        }
        
        solution.saveBest();

        model.computeOnlineSectioningInfos(assignment);
        
        new OverlapCheck((StudentSectioningModel)solution.getModel()).check(assignment);
        
        new SectionLimitCheck((StudentSectioningModel)solution.getModel()).check(assignment);
        
        
        sLog.info("Best solution found after "+solution.getBestTime()+" seconds ("+solution.getBestIteration()+" iterations).");
        sLog.info("Number of assigned variables is "+solution.getModel().nrAssignedVariables(assignment));
        sLog.info("Number of students with complete schedule is "+model.getContext(assignment).nrComplete());
        sLog.info("Total value of the solution is "+solution.getModel().getTotalValue(assignment));
        sLog.info("Average unassigned priority "+sDF.format(model.avgUnassignPriority(assignment)));
        sLog.info("Average number of requests "+sDF.format(model.avgNrRequests()));
        sLog.info("Unassigned request weight "+sDF.format(model.getUnassignedRequestWeight(assignment))+" / "+sDF.format(model.getTotalRequestWeight()));
        sLog.info("Info: "+ToolBox.dict2string(solution.getExtendedInfo(),2));

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(new File(new File(cfg.getProperty("General.Output",".")),"info.properties")));
            TreeSet entrySet = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    Map.Entry e1 = (Map.Entry)o1;
                    Map.Entry e2 = (Map.Entry)o2;
                    return ((Comparable)e1.getKey()).compareTo(e2.getKey());
                }
            });
            entrySet.addAll(solution.getExtendedInfo().entrySet());
            for (Iterator i=entrySet.iterator();i.hasNext();) {
                Map.Entry entry = (Map.Entry)i.next();
                pw.println(entry.getKey().toString().toLowerCase().replace(' ','.')+"="+entry.getValue());
            }
            pw.flush();
        } catch (IOException e) {
            sLog.error("Unable to save info, reason: "+e.getMessage(),e);
        } finally {
            if (pw!=null) pw.close();
        }

        try {
            new StudentSectioningXMLSaver(solver).save(new File(new File(cfg.getProperty("General.Output",".")),"solution.xml"));
        } catch (Exception e) {
            sLog.error("Unable to save solution, reason: "+e.getMessage(),e);
        }
        
        try {
            new BatchStudentSectioningSaver(solver).save();
        } catch (Exception e) {
            sLog.error("Unable to save solution, reason: "+e.getMessage(),e);
        }

    }
    
    
    public static void main(String[] args) {
        try {
            DataProperties cfg = new DataProperties();
            cfg.setProperty("Termination.Class","org.cpsolver.ifs.termination.GeneralTerminationCondition");
            cfg.setProperty("Termination.StopWhenComplete","true");
            cfg.setProperty("Termination.TimeOut","600");
            cfg.setProperty("Comparator.Class","org.cpsolver.ifs.solution.GeneralSolutionComparator");
            cfg.setProperty("Value.Class","org.cpsolver.ifs.heuristics.GeneralValueSelection");
            cfg.setProperty("Value.WeightConflicts", "1.0");
            cfg.setProperty("Value.WeightNrAssignments", "0.0");
            cfg.setProperty("Variable.Class","org.cpsolver.ifs.heuristics.GeneralVariableSelection");
            cfg.setProperty("Neighbour.Class","org.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection");
            cfg.setProperty("General.SaveBestUnassigned", "-1");
            cfg.setProperty("Extensions.Classes","org.cpsolver.ifs.extension.ConflictStatistics;org.cpsolver.studentsct.extension.DistanceConflict");
            cfg.setProperty("Data.Initiative","woebegon");
            cfg.setProperty("Data.Term","Fal");
            cfg.setProperty("Data.Year","2007");
            cfg.setProperty("Load.IncludeCourseDemands", (sIncludeCourseDemands?"true":"false"));
            cfg.setProperty("Load.IncludeLastLikeStudents", (sIncludeLastLikeStudents?"true":"false"));
            cfg.setProperty("Load.IncludeUseCommittedAssignments", (sIncludeUseCommittedAssignments?"true":"false"));
            //cfg.setProperty("Load.MakeupAssignmentsFromRequiredPrefs", "true");
            if (args.length>=1) {
                cfg.load(new FileInputStream(args[0]));
            }
            cfg.putAll(System.getProperties());
            
            if (args.length>=2) {
                File logFile = new File(ToolBox.configureLogging(args[1], cfg, true, false));
                cfg.setProperty("General.Output", logFile.getParentFile().getAbsolutePath());
            } else {
                ToolBox.configureLogging();
                cfg.setProperty("General.Output", System.getProperty("user.home", ".")+File.separator+"Sectioning-Test");
            }
            Logger.getLogger(BacktrackNeighbourSelection.class).setLevel(cfg.getPropertyBoolean("Debug.BacktrackNeighbourSelection",false)?Level.DEBUG:Level.INFO);
            
            HibernateUtil.configureHibernate(cfg);
            
            batchSectioning(cfg);
        } catch (Exception e) {
            sLog.error(e.getMessage(),e);
            e.printStackTrace();
        }
    }
}
