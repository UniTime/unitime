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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import org.cpsolver.coursett.TimetableLoader;
import org.cpsolver.coursett.TimetableXMLLoader;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.ProgressWriter;
import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.solver.interactive.Suggestion;
import org.unitime.timetable.solver.interactive.Suggestions;
import org.unitime.timetable.solver.interactive.SuggestionsModel;


/**
 * @author Tomas Muller
 */
public class InteractiveTimetablingTest {
    private static java.text.SimpleDateFormat sDateFormat = new java.text.SimpleDateFormat("yyMMdd_HHmmss",java.util.Locale.US);
    private static java.text.DecimalFormat sDoubleFormat = new java.text.DecimalFormat("0.000",new java.text.DecimalFormatSymbols(Locale.US));
    private static org.apache.log4j.Logger sLogger = org.apache.log4j.Logger.getLogger(InteractiveTimetablingTest.class);

	public static void main(String[] args) {
        try {
            DataProperties properties = ToolBox.loadProperties(new java.io.File(args[0]));
            properties.putAll(System.getProperties());
            properties.setProperty("General.Output", properties.getProperty("General.Output",".")+File.separator+(sDateFormat.format(new Date())));
            if (args.length>1)
                properties.setProperty("General.Input", args[1]);
            if (args.length>2)
                properties.setProperty("General.Output", args[2]+File.separator+(sDateFormat.format(new Date())));
            System.out.println("Output folder: "+properties.getProperty("General.Output"));
            ToolBox.configureLogging(properties.getProperty("General.Output"),properties,false,false);
            
            File outDir = new File(properties.getProperty("General.Output","."));
            outDir.mkdirs();
            
            Solver solver = new Solver<Lecture, Placement>(properties);
            TimetableModel model = new TimetableModel(properties);
            solver.setInitalSolution(model);
            Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
            
            TimetableLoader loader = new TimetableXMLLoader(model, solver.currentSolution().getAssignment());
            loader.load();
            
            solver.initSolver();
            
            sLogger.info("Starting from: "+ToolBox.dict2string(model.getExtendedInfo(solver.currentSolution().getAssignment()),2));
            
            PrintWriter csv = new PrintWriter(new FileWriter(outDir.toString()+File.separator+"stat.csv"));
            csv.println("class,timeout,#sol,#comb,time,best,timeout,#sol,#comb,time,best");
            csv.flush();
            
            int depth = Integer.parseInt(System.getProperty("depth", "3"));
            
            int nrClasses1 = 0, nrClasses2 = 0;
            double bestValue1 = 0, bestValue2 = 0;
            long combinations1 = 0, combinations2 = 0;
            long solutions1 = 0, solutions2 = 0;
            double time1 = 0, time2 = 0;
            int timeout = 0;
            for (Lecture lect: model.variables()) {
            	SuggestionsModel m1 = new SuggestionsModel();
                m1.setDepth(depth);
                m1.setTimeout(5000);
                m1.setClassId(lect.getClassId());
                Suggestions s1 = new Suggestions(solver, m1);
                long t0 = System.currentTimeMillis();
                s1.computeSuggestions();
                long t1 = System.currentTimeMillis();
                csv.print(lect.getName()+","+(s1.getTimeoutReached()?"T":"F")+","+s1.getNrSolutions()+","+s1.getNrCombinationsConsidered()+","+(t1-t0)+","+
                		(s1.getSuggestions().isEmpty()?"-":((Suggestion)s1.getSuggestions().first()).getValue())+",");
                csv.flush();
                SuggestionsModel m2 = new SuggestionsModel();
                m2.setDepth(m1.getDepth());
                m2.setTimeout(360000);
                m2.setClassId(lect.getClassId());
                Suggestions s2 = new Suggestions(solver, m2);
                long t2 = System.currentTimeMillis();
                s2.computeSuggestions();
                long t3 = System.currentTimeMillis();
                combinations1 += s1.getNrCombinationsConsidered();
                combinations2 += s2.getNrCombinationsConsidered();
                time1 += (t1-t0)/1000.0;
                time2 += (t3-t2)/1000.0;
                solutions1 += s1.getNrSolutions();
                solutions2 += s2.getNrSolutions();
                if (s1.getTimeoutReached()) timeout++;
                if (!s1.getSuggestions().isEmpty() && !s2.getSuggestions().isEmpty()) {
                	Suggestion x1 = (Suggestion)s1.getSuggestions().first();
                	Suggestion x2 = (Suggestion)s2.getSuggestions().first();
                	bestValue1 += x1.getValue();
                	bestValue2 += x2.getValue();
                	nrClasses1++; nrClasses2++;
                } else if (!s2.getSuggestions().isEmpty()) {
                	nrClasses2++;
                }
                csv.println(
                		(s2.getTimeoutReached()?"T":"F")+","+
                		s2.getNrSolutions()+","+s2.getNrCombinationsConsidered()+","+(t3-t2)+","+
                		(s2.getSuggestions().isEmpty()?"-":((Suggestion)s2.getSuggestions().first()).getValue()));
                csv.flush();
            }
            
            csv.close();
            sLogger.info("Number of solutions: "+sDoubleFormat.format(100.0*solutions1/solutions2)+"% ("+solutions1+" of "+solutions2+")");
            sLogger.info("Number of combinations: "+sDoubleFormat.format(100.0*combinations1/combinations2)+"% ("+combinations1+" of "+combinations2+")");
            sLogger.info("Average time needed: "+sDoubleFormat.format(time1/model.variables().size())+" (versus "+sDoubleFormat.format(time2/model.variables().size())+")");
            sLogger.info("Timeout reached: "+sDoubleFormat.format(100.0*timeout/model.variables().size())+" ("+timeout+"x)");
            sLogger.info("Improvement found: "+sDoubleFormat.format(100.0*nrClasses1/model.variables().size())+" ("+nrClasses1+")");
            sLogger.info("Improvement found (w/o time limit): "+sDoubleFormat.format(100.0*nrClasses2/model.variables().size())+" ("+nrClasses2+")");
            sLogger.info("Average improvement: "+sDoubleFormat.format(bestValue1/nrClasses1));
            sLogger.info("Average improvement (w/o time limit): "+sDoubleFormat.format(bestValue2/nrClasses1));
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
}
