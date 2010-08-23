/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.curricula.students;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solution.SolutionListener;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.IdGenerator;
import net.sf.cpsolver.ifs.util.ToolBox;

public class CurModel extends Model<CurVariable, CurValue> {
	private static Log sLog = LogFactory.getLog(CurModel.class);
	private List<CurStudent> iStudents = new ArrayList<CurStudent>();
	private Map<Long, CurCourse> iCourses = new Hashtable<Long, CurCourse>();
	private List<CurCourse> iSwapableCourses = new ArrayList<CurCourse>();
	private CurStudentLimit iStudentLimit = null;
	
	public CurModel(int nrStudents, IdGenerator gen) {
		for (int i = 0; i < nrStudents; i++)
			iStudents.add(new CurStudent(-gen.newId()));
	}
	
	public void addCourse(Long courseId, String courseName, int nrStudents) {
		CurCourse course = new CurCourse(this, courseId, courseName, Math.min(iStudents.size(), nrStudents));
		iCourses.put(courseId, course);
		if (nrStudents > 0 && nrStudents < iStudents.size())
			iSwapableCourses.add(course);
	}
	
	public void setTargetShare(Long c1, Long c2, int share) {
		iCourses.get(c1).setTargetShare(c2, share);
		iCourses.get(c2).setTargetShare(c1, share);
	}
	
	public void setStudentLimits() {
		double nrStudentCourses = 0;
		for (CurCourse course: getCourses()) {
			nrStudentCourses += course.getNrStudents();
		}
		double avg = nrStudentCourses / getStudents().size();
		int maxLimit = 1 + (int)Math.ceil(avg);
		int minLimit = (int)Math.floor(avg) - 1;
		sLog.info("Student course limit <" + minLimit + "," + maxLimit + ">");
		iStudentLimit = new CurStudentLimit(minLimit, maxLimit);
		addGlobalConstraint(iStudentLimit);
	}
	
	public CurStudentLimit getStudentLimit() {
		return iStudentLimit;
	}
	
	public Collection<CurCourse> getCourses() {
		return iCourses.values();
	}
	
	public CurCourse getCourse(Long courseId) {
		return iCourses.get(courseId);
	}
	
	public List<CurStudent> getStudents() {
		return iStudents;
	}
	
	public List<CurCourse> getSwapCourses() {
		return iSwapableCourses;
	}
	
    public double getTotalValue() {
        int value = 0;
        for (CurCourse c1: iCourses.values()) {
			for (CurCourse c2: iCourses.values()) {
				if (c1.getCourseId() >= c2.getCourseId()) continue;
				int bucketValue = c1.penalty(c2);
				value += bucketValue;
			}
        }
        return value;
    }
    
    public String toString() {
    	return assignedVariables().size() + "/" + variables().size() + " V:" + getTotalValue();
    }
    
    public void ifs() {
        net.sf.cpsolver.ifs.util.DataProperties cfg = new net.sf.cpsolver.ifs.util.DataProperties();
        cfg.setProperty("Termination.Class", "org.unitime.timetable.solver.curricula.students.CurTermination");
        cfg.setProperty("Termination.StopWhenComplete", "false");
        cfg.setProperty("Termination.TimeOut", "60");
        cfg.setProperty("Termination.MaxIdle", "1000");
        cfg.setProperty("Comparator.Class", "net.sf.cpsolver.ifs.solution.GeneralSolutionComparator");
        // cfg.setProperty("Value.WeightConflicts", "1000");
        // cfg.setProperty("Value.Class", "net.sf.cpsolver.ifs.heuristics.GeneralValueSelection");
        // cfg.setProperty("Variable.Class", "net.sf.cpsolver.ifs.heuristics.GeneralVariableSelection");
        cfg.setProperty("Variable.Class", "org.unitime.timetable.solver.curricula.students.CurVariableSelection");
        cfg.setProperty("Value.Class", "org.unitime.timetable.solver.curricula.students.CurValueSelection");

        Solver<CurVariable, CurValue> solver = new Solver<CurVariable, CurValue>(cfg);
        solver.setInitalSolution(this);
        solver.currentSolution().addSolutionListener(new SolutionListener<CurVariable, CurValue>() {
			
			@Override
			public void solutionUpdated(Solution<CurVariable, CurValue> solution) {
			}
			
			@Override
			public void getInfo(Solution<CurVariable, CurValue> solution,
					Map<String, String> info, Collection<CurVariable> variables) {
			}
			
			@Override
			public void getInfo(Solution<CurVariable, CurValue> solution,
					Map<String, String> info) {
			}
			
			@Override
			public void bestSaved(Solution<CurVariable, CurValue> solution) {
				sLog.info(solution.getModel().toString()+", i:" + solution.getIteration());
			}
			
			@Override
			public void bestRestored(Solution<CurVariable, CurValue> solution) {
			}
			
			@Override
			public void bestCleared(Solution<CurVariable, CurValue> solution) {
			}
		});
        
        solver.start();
        try {
            solver.getSolverThread().join();
        } catch (InterruptedException e) {
        }

        Solution<CurVariable, CurValue> solution = solver.lastSolution();
        solution.restoreBest();

        sLog.info("Best solution found after " + solution.getBestTime() + " seconds ("
                + solution.getBestIteration() + " iterations).");
        sLog.info("Number of assigned variables is " + solution.getModel().assignedVariables().size());
        sLog.info("Total value of the solution is " + solution.getModel().getTotalValue());
    }

    private static String lpad(String s, int len, char ch) {
    	while (s.length() < len) s = ch + s;
    	return s;
    }
    
    public void save(PrintWriter out) throws IOException {
		out.println(String.valueOf(getStudents().size()));
		TreeSet<CurCourse> courses = new TreeSet<CurCourse>(new Comparator<CurCourse>() {
			public int compare(CurCourse c1, CurCourse c2) {
				int cmp = c1.getCourseName().compareTo(c2.getCourseName());
				if (cmp != 0) return cmp;
				return c1.getCourseId().compareTo(c2.getCourseId());
			}
		});
		courses.addAll(getCourses());
		for (CurCourse course: courses) {
			String line = lpad(course.getCourseId().toString(), 6, ' ') + "," + 
				lpad(course.getCourseName(), 20, ' ') + "," + lpad(String.valueOf(course.getNrStudents()), 4, ' ');
			for (CurCourse other: courses) {
				if (other.equals(course)) break;
				line += "," + lpad(String.valueOf(course.getTargetShare(other.getCourseId())), 4, ' ');
			}
			out.println(line);
		}
		out.flush();
    }

    public void save(String f) throws IOException {
    	PrintWriter out = null;
    	try {
    		out = new PrintWriter(new FileWriter(f));
    		save(out);
    	} finally {
        	out.close();
    	}
    }
    
    public String save() throws IOException {
    	StringWriter out = new StringWriter();
    	save(new PrintWriter(out));
    	out.close();
    	return out.getBuffer().toString();
    }
    
    public static CurModel load(String f) throws IOException {
    	BufferedReader in = null;
    	try {
    		in = new BufferedReader(new FileReader(f));
    		String line = in.readLine();
    		CurModel m = new CurModel(Integer.parseInt(line), new IdGenerator());
    		ArrayList<Long> courses = new ArrayList<Long>();
    		while ((line = in.readLine()) != null) {
    			String[] fields = line.split(",");
    			int idx = 0;
    			Long courseId = Long.valueOf(fields[idx++].trim());
    			String courseName = fields[idx++].trim();
    			int nrStudents = Integer.parseInt(fields[idx++].trim());
    			m.addCourse(courseId, courseName, nrStudents);
    			for (Long course: courses)
    				m.setTargetShare(course, courseId, Integer.parseInt(fields[idx++].trim()));
    			courses.add(courseId);
    		}
    		return m;
    	} finally {
    		in.close();
    	}
    }
    
    public void naive(DataProperties cfg) {
		int idle = 0, it = 0;
		sLog.info("  -- initial value: " + getTotalValue());
		double best = getTotalValue();
		CurStudentSwap sw = new CurStudentSwap(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
		while (!getSwapCourses().isEmpty() && idle < 1000) {
			Neighbour<CurVariable, CurValue> n = sw.selectNeighbour(solution);
			if (n == null) break;
			double value = n.value();
			if (value < 0.0) {
				idle = 0;
				n.assign(it);
			} else if (value == 0.0) {
				n.assign(it);
			}
			if (getTotalValue() < best) {
				best = getTotalValue();
				sLog.info("  -- best value: " + getTotalValue());
			}
			it++; idle++;
		}
		sLog.info("  -- final value: " + getTotalValue());
    }
    
    public void hc(DataProperties cfg) {
		int it = 0, idle = 0;
		double total = getTotalValue();
		double best = total;
		sLog.info("  -- initial value: " + total);
		CurHillClimber hc = new CurHillClimber(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
		while (idle < 1000) {
			Neighbour<CurVariable, CurValue> n = hc.selectNeighbour(solution);
			if (n == null) break;
			if (unassignedVariables().isEmpty() && n.value() >= 0.0) break;
			total += n.value();
			n.assign(it);
			if (total < best) {
				best = total;
				sLog.info("  -- best value: " + getTotalValue());
			}
			it++;
		}
		sLog.info("  -- final value: " + getTotalValue());
    }
    
    public void deluge(DataProperties cfg) {
		int it = 0;
		double total = getTotalValue();
		double bound = 1.25 * total;
		sLog.info("  -- initial value: " + total);
		double best = getTotalValue();
		CurStudentSwap sw = new CurStudentSwap(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
		while (!getSwapCourses().isEmpty() && bound > 0.75 * total && total > 0) {
			Neighbour<CurVariable, CurValue> n = sw.selectNeighbour(solution);
			if (n != null) {
				double value = n.value();
				if (value <= 0.0 || total + value < bound) {
					n.assign(it);
					if (total + value < best) {
						best = total + value;
						sLog.info("  -- best value: " + getTotalValue() + ", bound: " + bound);
					}
					total += value;
				}
			}
			bound *= 0.999999;
			it++;
		}
		sLog.info("  -- final value: " + getTotalValue());
    }
    
    public void fast(DataProperties cfg) {
		int idle = 0, it = 0;
		double total = getTotalValue();
		sLog.info("  -- initial value: " + total);
		double best = total;
		CurSimpleMove m = new CurSimpleMove(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
		while (idle < 1000) {
			Neighbour<CurVariable, CurValue> n = m.selectNeighbour(solution);
    		if (n != null) {
        		double value = n.value();
    			if (value < 0.0) {
    				idle = 0;
					n.assign(it);
    				total += value;
    			} else if (value == 0.0) {
					n.assign(it);
    			}
    		}
			if (total < best) {
				best = total;
				sLog.info("  -- best value: " + best);
			}
			it++; idle++;
		}
		sLog.info("  -- final value: " + getTotalValue());
    }

    
    public void solve() {
    	DataProperties cfg = new DataProperties();
    	CurVariableSelection var = new CurVariableSelection(cfg);
    	CurValueSelection vs = new CurValueSelection(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
    	while (!unassignedVariables().isEmpty()) {
    		CurValue student = vs.selectValueSlow(solution, var.selectVariable(solution));
    		if (student == null) break;
    		student.variable().assign(solution.getIteration(), student);
    	}
		sLog.info("  -- initial value: " + getTotalValue());
		hc(cfg); // or fast(cfg);
		deluge(cfg); // or naive(cfg);
    }
    
    public static void main(String[] args) {
    	try {
    		/*
    		CurModel m = new CurModel(20, new IdGenerator());
    		for (int i = 1; i <= 10; i++)
    			m.addCourse((long)i, "C" + i,  2 * i);
    		for (int i = 1; i < 10; i++)
    			for (int j = i + 1; j <= 10; j++)
    				m.setTargetShare((long)i, (long)j, i);
    		sLog.info(m.save());
    		*/
    		
    		CurModel m = CurModel.load("/Users/muller/test.cur");
    		m.setStudentLimits();
    		m.solve();

    		sLog.info("Solution: " + ToolBox.dict2string(m.getInfo(), 2));
            
    		/*
    		TreeSet<CurCourse> courses = new TreeSet<CurCourse>(new Comparator<CurCourse>() {
    			public int compare(CurCourse c1, CurCourse c2) {
    				int cmp = c1.getCourseName().compareTo(c2.getCourseName());
    				if (cmp != 0) return cmp;
    				return c1.getCourseId().compareTo(c2.getCourseId());
    			}
    		});
    		courses.addAll(m.getCourses());
            int penalty = 0;
    		for (CurCourse course: courses) {
    			sLog.info(course.getCourseName() + ": " + m.getCourse(course.getCourseId()).getStudents());
        		for (CurCourse other: courses) {
        			if (other.getCourseId() <= course.getCourseId()) continue;
    				int share = course.share(other);
    				int target = course.getTargetShare(other.getCourseId());
    				sLog.info("  " + other.getCourseName() + ": share=" + share + ", target=" + target + ", penalty=" + Math.abs(target - share)); 
    				penalty += Math.abs(target - share);
    			}
    		}
    		sLog.info("Total penalty: " + penalty);
    		*/
    		for (CurStudent student: m.getStudents()) {
    			sLog.info(student.getStudentId() + ": " + student.getCourses().size() + "/" + student.getCourses());
    		}
    		    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

}
