/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import net.sf.cpsolver.ifs.heuristics.BacktrackNeighbourSelection;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.model.Variable;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solution.SolutionListener;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.solver.SolverListener;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.StudentSectioningXMLSaver;
import net.sf.cpsolver.studentsct.check.InevitableStudentConflicts;
import net.sf.cpsolver.studentsct.check.OverlapCheck;
import net.sf.cpsolver.studentsct.check.SectionLimitCheck;
import net.sf.cpsolver.studentsct.report.CourseConflictTable;
import net.sf.cpsolver.studentsct.report.DistanceConflictTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
        try {
            new BatchStudentSectioningLoader(model).load();
        } catch (Exception e) {
            sLog.error("Unable to load problem, reason: "+e.getMessage(),e);
            return;
        }
        
        Solver solver = new Solver(cfg);
        Solution solution = new Solution(model,0,0);
        solver.setInitalSolution(solution);
        solver.addSolverListener(new SolverListener() {
            public boolean variableSelected(long iteration, Variable variable) {
                return true;
            }
            public boolean valueSelected(long iteration, Variable variable, Value value) {
                return true;
            }
            public boolean neighbourSelected(long iteration, Neighbour neighbour) {
                sLog.debug("Select["+iteration+"]: "+neighbour);
                return true;
            }
        });
        solution.addSolutionListener(new SolutionListener() {
            public void solutionUpdated(Solution solution) {}
            public void getInfo(Solution solution, java.util.Map info) {}
            public void getInfo(Solution solution, java.util.Map info, java.util.Collection variables) {}
            public void bestCleared(Solution solution) {}
            public void bestSaved(Solution solution) {
                StudentSectioningModel m = (StudentSectioningModel)solution.getModel();
                sLog.debug("**BEST** V:"+m.assignedVariables().size()+"/"+m.variables().size()+" - S:"+m.nrComplete()+"/"+m.getStudents().size()+" - TV:"+sDF.format(m.getTotalValue()));
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
            cct.createTable(true, false).save(new File(outDir, "conflicts-lastlike.csv"));
            cct.createTable(false, true).save(new File(outDir, "conflicts-real.csv"));
            
            DistanceConflictTable dct = new DistanceConflictTable((StudentSectioningModel)solution.getModel());
            dct.createTable(true, false).save(new File(outDir, "distances-lastlike.csv"));
            dct.createTable(false, true).save(new File(outDir, "distances-real.csv"));
            
            if (cfg.getPropertyBoolean("Test.InevitableStudentConflictsCheck", false)) {
                InevitableStudentConflicts ch = new InevitableStudentConflicts(model);
                if (!ch.check()) ch.getCSVFile().save(new File(outDir, "inevitable-conflicts.csv"));
            }
        } catch (IOException e) {
            sLog.error(e.getMessage(),e);
        }
        
        solution.saveBest();

        model.computeOnlineSectioningInfos();
        
        new OverlapCheck((StudentSectioningModel)solution.getModel()).check();
        
        new SectionLimitCheck((StudentSectioningModel)solution.getModel()).check();
        
        
        sLog.info("Best solution found after "+solution.getBestTime()+" seconds ("+solution.getBestIteration()+" iterations).");
        sLog.info("Number of assigned variables is "+solution.getModel().assignedVariables().size());
        sLog.info("Number of students with complete schedule is "+model.nrComplete());
        sLog.info("Total value of the solution is "+solution.getModel().getTotalValue());
        sLog.info("Average unassigned priority "+sDF.format(model.avgUnassignPriority()));
        sLog.info("Average number of requests "+sDF.format(model.avgNrRequests()));
        sLog.info("Unassigned request weight "+sDF.format(model.getUnassignedRequestWeight())+" / "+sDF.format(model.getTotalRequestWeight()));
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
            cfg.setProperty("Termination.Class","net.sf.cpsolver.ifs.termination.GeneralTerminationCondition");
            cfg.setProperty("Termination.StopWhenComplete","true");
            cfg.setProperty("Termination.TimeOut","600");
            cfg.setProperty("Comparator.Class","net.sf.cpsolver.ifs.solution.GeneralSolutionComparator");
            cfg.setProperty("Value.Class","net.sf.cpsolver.ifs.heuristics.GeneralValueSelection");
            cfg.setProperty("Value.WeightConflicts", "1.0");
            cfg.setProperty("Value.WeightNrAssignments", "0.0");
            cfg.setProperty("Variable.Class","net.sf.cpsolver.ifs.heuristics.GeneralVariableSelection");
            cfg.setProperty("Neighbour.Class","net.sf.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection");
            cfg.setProperty("General.SaveBestUnassigned", "-1");
            cfg.setProperty("Extensions.Classes","net.sf.cpsolver.ifs.extension.ConflictStatistics;net.sf.cpsolver.studentsct.extension.DistanceConflict");
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
