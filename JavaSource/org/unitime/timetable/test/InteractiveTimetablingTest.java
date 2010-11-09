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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import org.unitime.timetable.solver.interactive.Suggestion;
import org.unitime.timetable.solver.interactive.Suggestions;
import org.unitime.timetable.solver.interactive.SuggestionsModel;

import net.sf.cpsolver.coursett.TimetableLoader;
import net.sf.cpsolver.coursett.TimetableSolver;
import net.sf.cpsolver.coursett.TimetableXMLLoader;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ProgressWriter;
import net.sf.cpsolver.ifs.util.ToolBox;

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
            
            Solver solver = new TimetableSolver(properties);
            TimetableModel model = new TimetableModel(properties);
            Progress.getInstance(model).addProgressListener(new ProgressWriter(System.out));
            
            TimetableLoader loader = new TimetableXMLLoader(model);
            loader.load();
            
            solver.setInitalSolution(model);
            solver.initSolver();
            
            sLogger.info("Starting from: "+ToolBox.dict2string(model.getExtendedInfo(),2));
            
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
