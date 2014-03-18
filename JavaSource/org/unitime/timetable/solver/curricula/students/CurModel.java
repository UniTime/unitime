/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.curricula.students;

import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.assignment.context.AssignmentConstraintContext;
import org.cpsolver.ifs.assignment.context.ModelWithContext;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solution.SolutionListener;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;


/**
 * @author Tomas Muller
 */
public class CurModel extends ModelWithContext<CurVariable, CurValue, CurModel.CurModelContext> {
	private static Log sLog = LogFactory.getLog(CurModel.class);
	private static DecimalFormat sDF = new DecimalFormat("0.000");
	private List<CurStudent> iStudents = new ArrayList<CurStudent>();
	private Map<Long, CurCourse> iCourses = new Hashtable<Long, CurCourse>();
	private List<CurCourse> iSwapableCourses = new ArrayList<CurCourse>();
	private CurStudentLimit iStudentLimit = null;
	private double iMinStudentWeight = Float.MAX_VALUE, iMaxStudentWeight = 0.0, iTotalStudentWeight = 0.0;
	private double iBestAssignedWeight  = 0.0, iMaxAssignedWeight = 0.0;
	
	public CurModel(Collection<CurStudent> students) {
		for (CurStudent student: students)
			student.setModel(this);
		iStudents.addAll(students);
		iStudentLimit = new CurStudentLimit(0, Integer.MAX_VALUE);
		for (CurStudent student: getStudents()) {
			iMinStudentWeight = Math.min(iMinStudentWeight, student.getWeight());
			iMaxStudentWeight = Math.max(iMaxStudentWeight, student.getWeight());
			iTotalStudentWeight += student.getWeight();
		}
	}
	
	public double getMinStudentWidth() {
		return iMinStudentWeight;
	}
	
	public double getMaxStudentWidth() {
		return iMaxStudentWeight;
	}

	public void addCourse(Long courseId, String courseName, double size, Double priority) {
		CurCourse course = new CurCourse(this, courseId, courseName, Math.min(iStudents.size(), (int)Math.round(size/getMinStudentWidth())), size, priority);
		iCourses.put(courseId, course);
		if (course.getNrStudents() < iStudents.size())
			iSwapableCourses.add(course);
		iMaxAssignedWeight += course.getOriginalMaxSize();
	}
	
	public void setTargetShare(Long c1, Long c2, double share, boolean round) {
		CurCourse course1 = iCourses.get(c1);
		CurCourse course2 = iCourses.get(c2);
		double ub = Math.min(course1.getOriginalMaxSize(), course2.getOriginalMaxSize());
		double lb = Math.max(0, course1.getOriginalMaxSize() + course2.getOriginalMaxSize() - iTotalStudentWeight);
		double ts = Math.max(lb, Math.min(ub, share));
		if (ts != share)
			sLog.debug("Target share between " + course1.getCourseName() + " and " + course2.getCourseName() + " changed to " + ts + " (was: " + share + ", lb:" + lb + ", ub:" + ub + ")");
		course1.setTargetShare(c2, round ? Math.round(ts) : ts);
		course2.setTargetShare(c1, round ? Math.round(ts) : ts);
	}
	
	protected void setTargetShareNoAdjustments(Long c1, Long c2, double share) {
		CurCourse course1 = iCourses.get(c1);
		CurCourse course2 = iCourses.get(c2);
		course1.setTargetShare(c2, share);
		course2.setTargetShare(c1, share);
	}
	
	public void setStudentLimits() {
		double nrStudentCourses = 0;
		for (CurCourse course: getCourses()) {
			nrStudentCourses += course.getOriginalMaxSize();
		}
		double studentWeight = 0;
		for (CurStudent student: getStudents()) {
			studentWeight += student.getWeight();
		}
		double avg = nrStudentCourses / studentWeight;
		int maxLimit = 1 + (int)Math.ceil(avg);
		int minLimit = (int)Math.floor(avg) - 1;
		sLog.debug("Student course limit <" + minLimit + "," + maxLimit + ">");
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
	
	@Override
	public Map<String, String> getInfo(Assignment<CurVariable, CurValue> assignment) {
		Map<String, String> ret = super.getInfo(assignment);
		ret.put("Students", String.valueOf(getStudents().size()));
		ret.put("Courses", String.valueOf(getCourses().size()));
		double avgEnrollment = ((double)variables().size()) / getCourses().size();
		double rmsEnrollment = 0.0;
		for (CurCourse c1: getCourses())
			rmsEnrollment += (c1.getNrStudents() - avgEnrollment) * (c1.getNrStudents() - avgEnrollment);
		ret.put("Course size", sDF.format(avgEnrollment) + " ± " + sDF.format(Math.sqrt(rmsEnrollment / getCourses().size())));
		int totalCourses = 0;
		for (CurStudent student: getStudents())
			totalCourses += student.getCourses(assignment).size();
		double avgCourses = ((double)totalCourses) / getStudents().size();
		double rmsCourses = 0.0;
		for (CurStudent student: getStudents())
			rmsCourses += (student.getCourses(assignment).size() - avgCourses) * (student.getCourses(assignment).size() - avgCourses);
		ret.put("Courses per student", sDF.format(avgCourses) + " ± " + sDF.format(Math.sqrt(rmsCourses / getStudents().size())) +
				" (limit: " + getStudentLimit().getMinLimit() + " .. " + getStudentLimit().getMaxLimit() + ")");
		int totalShare = 0;
		double totalError = 0;
		double rmsError = 0.0;
		int pairs = 0;
		for (CurCourse c1: getCourses())
			for (CurCourse c2: getCourses())
				if (c1.getCourseId() < c2.getCourseId()) {
					double share = c1.share(assignment, c2);
					double target = c1.getTargetShare(c2.getCourseId());
					totalError += Math.abs(share - target);
					rmsError += (share - target) * (share - target);
					pairs ++;
					totalShare += share;
				}
		ret.put("Errors", sDF.format(totalError) + " (" + sDF.format(100.0 * totalError / totalShare) + "% of total share, avg: " + sDF.format(((double)totalError) / pairs) + ", rms: " + sDF.format(Math.sqrt(rmsError / pairs)) + ")");
		ret.put("Assigned Student Weight", sDF.format(getContext(assignment).getAssignedWeight()) + "/" + sDF.format(getMaxWeight()));
		double totalStudentWeight = 0;
		for (CurStudent student: getStudents()) {
			totalStudentWeight += student.getWeight();
		}
		double avgStudentWeight = totalStudentWeight / getStudents().size();
		double rmsStudentWeight = 0;
		for (CurStudent student: getStudents()) {
			rmsStudentWeight += (student.getWeight() - avgStudentWeight) * (student.getWeight() - avgStudentWeight);
		}
		ret.put("Student Weight", sDF.format(getMinStudentWidth()) + " .. " + sDF.format(getMaxStudentWidth()) + " (avg: " + sDF.format(avgStudentWeight) + ", rms: " + sDF.format(Math.sqrt(rmsStudentWeight / getStudents().size())) + ")");
		return ret;
	}
	
    public double getTotalValue(Assignment<CurVariable, CurValue> assignment) {
        double value = 0;
        for (CurCourse c1: iCourses.values())
			for (CurCourse c2: iCourses.values())
				if (c1.getCourseId() < c2.getCourseId())
					value += c1.penalty(assignment, c2);
        return value;
    }
    
    public double getMaxWeight() {
    	return iMaxAssignedWeight;
    }

    public double getBestWeight() {
    	return iBestAssignedWeight;
    }
    
    @Override
    public void saveBest(Assignment<CurVariable, CurValue> assignment) {
    	super.saveBest(assignment);
    	iBestAssignedWeight = getContext(assignment).getAssignedWeight();
    }
    
    @Override
    public void clearBest() {
    	super.clearBest();
    	iBestAssignedWeight = 0.0;
    }
    
    public String toString(Assignment<CurVariable, CurValue> assignment) {
    	return assignedVariables(assignment).size() + "/" + variables().size() + " V:" + sDF.format(getTotalValue(assignment)) + " A:" + sDF.format(getContext(assignment).getAssignedWeight()) + "/" + sDF.format(getMaxWeight());
    }
    
    public void ifs(Assignment<CurVariable, CurValue> assignment) {
        org.cpsolver.ifs.util.DataProperties cfg = new org.cpsolver.ifs.util.DataProperties();
        cfg.setProperty("Termination.Class", "org.unitime.timetable.solver.curricula.students.CurTermination");
        cfg.setProperty("Termination.StopWhenComplete", "false");
        cfg.setProperty("Termination.TimeOut", "60");
        cfg.setProperty("Termination.MaxIdle", "1000");
        cfg.setProperty("Comparator.Class", "org.unitime.timetable.solver.curricula.students.CurComparator");
        cfg.setProperty("Variable.Class", "org.unitime.timetable.solver.curricula.students.CurVariableSelection");
        cfg.setProperty("Value.Class", "org.unitime.timetable.solver.curricula.students.CurValueSelection");
        cfg.setProperty("General.SaveBestUnassigned", "-1");

        Solver<CurVariable, CurValue> solver = new Solver<CurVariable, CurValue>(cfg);
        solver.setInitalSolution(new Solution<CurVariable, CurValue>(this, assignment));
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
				sLog.debug(((CurModel)solution.getModel()).toString(solution.getAssignment())+", i:" + solution.getIteration());
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

        sLog.debug("Best solution found after " + solution.getBestTime() + " seconds (" + solution.getBestIteration() + " iterations).");
        sLog.debug("Number of assigned variables is " + solution.getAssignment().nrAssignedVariables());
        sLog.debug("Total value of the solution is " + solution.getModel().getTotalValue(solution.getAssignment()));
    }

    public void saveAsXml(Element root, Assignment<CurVariable, CurValue> assignment) {
    	List<Long> courses = new ArrayList<Long>();
    	DecimalFormat df = new DecimalFormat("0.##########", new DecimalFormatSymbols(Locale.US));
    	for (CurCourse course: getCourses()) {
    		Element courseElement = root.addElement("course");
    		courseElement.addAttribute("id", course.getCourseId().toString());
    		courseElement.addAttribute("name", course.getCourseName());
    		courseElement.addAttribute("limit", df.format(course.getOriginalMaxSize()));
    		if (course.getPriority() != null)
    			courseElement.addAttribute("priority", course.getPriority().toString());
    		if (!courses.isEmpty()) {
        		String share = "";
    			for (Long other: courses) {
    				share += (share.isEmpty() ? "" : ",") +  df.format(course.getTargetShare(other));
    			}
				courseElement.addAttribute("share", share);
    		}
			courses.add(course.getCourseId());
    	}
		for (CurStudent student: getStudents()) {
			Element studentElement = root.addElement("student");
			studentElement.addAttribute("id", student.getStudentId().toString());
			if (student.getWeight() != 1.0)
				studentElement.addAttribute("weight", df.format(student.getWeight()));
			String courseIds = "";
			for (CurCourse course: student.getCourses(assignment)) {
				courseIds += (courseIds.isEmpty() ? "" : ",") + course.getCourseId();
			}
			studentElement.setText(courseIds);
		}
    }
    
    public static Solution<CurVariable, CurValue> loadFromXml(Element root) {
    	List<Element> studentElements = root.elements("student");
    	List<CurStudent> students = new ArrayList<CurStudent>();
		for (Element studentElement: studentElements) {
			students.add(new CurStudent(Long.valueOf(studentElement.attributeValue("id")), Float.parseFloat(studentElement.attributeValue("weight", "1.0"))));
		}
		CurModel m = new CurModel(students);
		Assignment<CurVariable, CurValue> a = new DefaultSingleAssignment<CurVariable, CurValue>();
    	List<Long> courses = new ArrayList<Long>();
		for (Iterator<Element> i = root.elementIterator("course"); i.hasNext();) {
			Element courseElement = i.next();
			Long courseId = Long.valueOf(courseElement.attributeValue("id"));
			String courseName = courseElement.attributeValue("name");
			double size = Float.parseFloat(courseElement.attributeValue("limit"));
			String priority = courseElement.attributeValue("priority");
			m.addCourse(courseId, courseName, size, priority == null ? null : Double.valueOf(priority));
    		if (!courses.isEmpty()) {
    			String share[] = courseElement.attributeValue("share").split(",");
    			for (int j = 0; j < courses.size(); j++)
    				m.setTargetShareNoAdjustments(courseId, courses.get(j), Float.parseFloat(share[j]));
    		}
    		courses.add(courseId);
		}
		int idx = 0;
		for (Element studentElement: studentElements) {
			CurStudent student = students.get(idx++);
			String courseIds = studentElement.getText();
			if (courseIds != null && !courseIds.isEmpty()) {
				for (String courseId: courseIds.split(",")) {
					CurCourse course = m.getCourse(Long.valueOf(courseId));
					CurVariable var = null;
					for (CurVariable v: course.variables())
						if (a.getValue(v) == null) { var = v; break; }
					a.assign(0, new CurValue(var, student));
				}
			}
		}
		return new Solution<CurVariable, CurValue>(m, a);
    }
    
    public void naive(DataProperties cfg, Assignment<CurVariable, CurValue> assignment) {
    	int maxIdle = cfg.getPropertyInt("Curriculum.Naive.MaxIdle", 1000);
    	sLog.debug("  -- running naive");
		int idle = 0, it = 0;
		double best = getTotalValue(assignment);
		CurStudentSwap sw = new CurStudentSwap(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this, assignment);
		while (!getSwapCourses().isEmpty() && idle < maxIdle) {
			Neighbour<CurVariable, CurValue> n = sw.selectNeighbour(solution);
			if (n == null) break;
			double value = n.value(assignment);
			if (value < 0.0) {
				idle = 0;
				n.assign(assignment, it);
			} else if (value == 0.0) {
				n.assign(assignment, it);
			}
			if (getTotalValue(assignment) < best) {
				best = getTotalValue(assignment);
				sLog.debug("  -- best value: " + toString(assignment));
			}
			it++; idle++;
		}
		sLog.debug("  -- final value: " + toString(assignment));
    }
    
    public void hc(DataProperties cfg, Assignment<CurVariable, CurValue> assignment) {
    	int maxIdle = cfg.getPropertyInt("Curriculum.HC.MaxIdle", 1000);
    	sLog.debug("  -- running hill climber");
		int it = 0, idle = 0;
		double total = getTotalValue(assignment);
		double best = total;
		CurHillClimber hc = new CurHillClimber(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this, assignment);
		while (idle < maxIdle) {
			Neighbour<CurVariable, CurValue> n = hc.selectNeighbour(solution);
			if (n == null) break;
			if (nrUnassignedVariables(assignment) == 0 && n.value(assignment) >= -1e7f) break;
			total += n.value(assignment);
			n.assign(assignment, it);
			if (total < best) {
				best = total;
				idle = 0;
				sLog.debug("  -- best value: " + toString(assignment));
			} else {
				idle++;
			}
			it++;
		}
		cfg.setProperty("Curriculum.HC.Iters", String.valueOf(it));
		cfg.setProperty("Curriculum.HC.Value", String.valueOf(getTotalValue(assignment)));
		sLog.debug("  -- final value: " + toString(assignment));
    }
    
    public void deluge(DataProperties cfg, Assignment<CurVariable, CurValue> assignment) {
    	double f = cfg.getPropertyDouble("Curriculum.Deluge.Factor", 0.999999);
    	double ub = cfg.getPropertyDouble("Curriculum.Deluge.UpperBound", 1.25);
    	double lb = cfg.getPropertyDouble("Curriculum.Deluge.LowerBound", 0.75);
    	sLog.debug("  -- running great deluge");
		int it = 0;
		double total = getTotalValue(assignment);
		double bound = ub * total;
		double best = getTotalValue(assignment);
		CurStudentSwap sw = new CurStudentSwap(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this, assignment);
		saveBest(assignment);
		while (!getSwapCourses().isEmpty() && bound > lb * total && total > 0) {
			Neighbour<CurVariable, CurValue> n = sw.selectNeighbour(solution);
			if (n != null) {
				double value = n.value(assignment);
				if (value <= 0.0 || total + value < bound) {
					n.assign(assignment, it);
					if (total + value < best) {
						best = total + value;
						saveBest(assignment);
						sLog.debug("  -- best value: " + toString(assignment) + ", bound: " + bound);
					}
					total += value;
				}
			}
			bound *= f;
			it++;
		}
		cfg.setProperty("Curriculum.Deluge.Iters", String.valueOf(it));
		restoreBest(assignment);
		cfg.setProperty("Curriculum.Deluge.Value", String.valueOf(getTotalValue(assignment)));
		sLog.debug("  -- final value: " + toString(assignment));
    }
    
    public void fast(DataProperties cfg, Assignment<CurVariable, CurValue> assignment) {
    	int maxIdle = cfg.getPropertyInt("Curriculum.Fast.MaxIdle", 1000);
    	sLog.debug("  -- running fast");
		int idle = 0, it = 0;
		double total = getTotalValue(assignment);
		double best = total;
		CurSimpleMove m = new CurSimpleMove(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this, assignment);
		while (idle < maxIdle) {
			Neighbour<CurVariable, CurValue> n = m.selectNeighbour(solution);
    		if (n != null) {
        		double value = n.value(assignment);
    			if (value < 0.0) {
    				idle = 0;
					n.assign(assignment, it);
    				total += value;
    			} else if (value == 0.0) {
					n.assign(assignment, it);
    			}
    		}
			if (total < best) {
				best = total;
				sLog.debug("  -- best value: " + toString(assignment));
			}
			it++; idle++;
		}
		sLog.debug("  -- final value: " + toString(assignment));
    }
    
    public DataProperties solve(Assignment<CurVariable, CurValue> assignment) {
    	return solve(new DataProperties(), assignment);
    }
    
    public DataProperties solve(DataProperties cfg, Assignment<CurVariable, CurValue> assignment) {
    	if (cfg == null) cfg = new DataProperties();
    	sLog.debug("  -- setting up the solver");
    	CurVariableSelection var = new CurVariableSelection(cfg);
    	CurValueSelection vs = new CurValueSelection(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this, assignment);
    	sLog.debug("  -- creating initial assignment");
    	boolean precise = cfg.getPropertyBoolean("Curriculum.Initial.PreciseSelection", true);
    	while (nrUnassignedVariables(assignment) > 0) {
    		CurVariable course = var.selectVariable(solution);
    		if (course.getCourse().isComplete(assignment)) {
    			sLog.debug("    -- all complete");
    			break;
    		}
    		CurValue student = (precise ? vs.selectValueSlow(solution, course) : vs.selectValueFast(solution, course));
    		if (student == null) {
    			sLog.debug("    -- no student for " + course.getCourse().getCourseName());
    			break;
    		}
    		assignment.assign(solution.getIteration(), student);
    	}
    	for (CurCourse course: getCourses()) {
    		if (!course.isComplete(assignment)) {
    			sLog.debug("    -- incomplete " + course.getCourseName() + ": " + getCourse(course.getCourseId()).getStudents(assignment) + " (" + course.getSize(assignment) + "/" + course.getOriginalMaxSize() + ")");
    		}
    	}
    	cfg.setProperty("Curriculum.Initial.Value", String.valueOf(getTotalValue(assignment)));
		sLog.debug("  -- initial value: " + toString(assignment));
		for (String phase: cfg.getProperty("Curriculum.Phases", "HC,Deluge").split(",")) {
			if ("hc".equalsIgnoreCase(phase)) hc(cfg, assignment);
			else if ("fast".equalsIgnoreCase(phase)) fast(cfg, assignment);
			else if ("deluge".equalsIgnoreCase(phase)) deluge(cfg, assignment);
			else if ("naive".equalsIgnoreCase(phase)) naive(cfg, assignment);
			else sLog.warn("Phase " + phase + " is not known");
		}
		return cfg;
    }
    
    public boolean isSameModel(Object o) {
    	if (o == null || !(o instanceof CurModel)) return false;
    	CurModel m = (CurModel)o;
    	if (getStudents().size() != m.getStudents().size()) return false;
    	if (getStudentLimit().getMaxLimit() != m.getStudentLimit().getMaxLimit()) return false;
    	if (getStudentLimit().getMinLimit() != m.getStudentLimit().getMinLimit()) return false;
    	if (getMinStudentWidth() != m.getMinStudentWidth()) return false;
    	if (getCourses().size() != m.getCourses().size()) return false;
    	students: for (CurStudent s: getStudents()) {
    		if (s.getStudentId() == null || s.getStudentId() < 0) continue;
			for (CurStudent z: m.getStudents()) {
				if (z.getStudentId().equals(s.getStudentId()) && z.getWeight() == s.getWeight()) continue students;
			}
    		return false;
    	}
    	students: for (CurStudent s: m.getStudents()) {
    		if (s.getStudentId() == null || s.getStudentId() < 0) continue;
			for (CurStudent z: getStudents()) {
				if (z.getStudentId().equals(s.getStudentId()) && z.getWeight() == s.getWeight()) continue students;
			}
    		return false;
    	}
    	for (int idx = 0; idx < getStudents().size(); idx++) {
    		CurStudent s = getStudents().get(idx);
    		if (s.getStudentId() != null && s.getStudentId() >= 0) continue;
    		if (s.getWeight() != m.getStudents().get(idx).getWeight()) {
    			return false;
    		}
    	}
    	for (CurCourse c1: getCourses()) {
    		CurCourse x1 = m.getCourse(c1.getCourseId());
    		if (x1 == null || x1.getNrStudents() != c1.getNrStudents() || !equals(x1.getPriority(), c1.getPriority())) return false;
    		for (CurCourse c2: getCourses())
    			if (c1.getCourseId() < c2.getCourseId() && Math.abs(c1.getTargetShare(c2.getCourseId()) - x1.getTargetShare(c2.getCourseId())) > 0.001) return false;
    	}
    	return true;
    }
    
    public static boolean equals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
    
    public static void main(String[] args) {
    	try {
    		Logger.getRootLogger().setLevel(Level.DEBUG);
    		
    		List<CurStudent> students = new ArrayList<CurStudent>();
    		for (int i = 0; i < 20; i++)
    			students.add(new CurStudent(new Long(1 + i), (i < 10 ? 0.5f: 2f)));
    		CurModel m = new CurModel(students);
    		for (int i = 1; i <= 10; i++)
    			m.addCourse((long)i, "C" + i,  2 * i, null);
    		for (int i = 1; i < 10; i++)
    			for (int j = i + 1; j <= 10; j++)
    				m.setTargetShare((long)i, (long)j, i, false);
    		m.setStudentLimits();
    		
			Document d0 = DocumentHelper.createDocument();
			Assignment<CurVariable, CurValue> a = new DefaultSingleAssignment<CurVariable, CurValue>();
			m.saveAsXml(d0.addElement("curriculum"), a);
			sLog.info(d0.asXML());
			
    		sLog.info("Loaded: " + ToolBox.dict2string(m.getInfo(a), 2));

    		m.solve(a);

    		sLog.info("Solution: " + ToolBox.dict2string(m.getInfo(a), 2));
    		
			Document d1 = DocumentHelper.createDocument();
			m.saveAsXml(d1.addElement("curriculum"), a);
			sLog.info(d1.asXML());
			
			Solution<CurVariable, CurValue> x = loadFromXml(d1.getRootElement());
			sLog.info("Reloaded: " + ToolBox.dict2string(x.getInfo(), 2));
            
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
    			sLog.info(course.getCourseName() + ": " + m.getCourse(course.getCourseId()).getStudents(a) + " (" + course.getSize(a) + "/" + course.getOriginalMaxSize() + ")");
        		for (CurCourse other: courses) {
        			if (other.getCourseId() <= course.getCourseId()) continue;
    				double share = course.share(a, other);
    				double target = course.getTargetShare(other.getCourseId());
    				sLog.info("  " + other.getCourseName() + ": share=" + share + ", target=" + target + ", penalty=" + Math.abs(target - share)); 
    				penalty += Math.abs(target - share);
    			}
    		}
    		sLog.info("Total penalty: " + penalty);
    		
			Document doc = DocumentHelper.createDocument();
			m.saveAsXml(doc.addElement("curriculum"), a);
			FileOutputStream fos = new FileOutputStream("/Users/muller/solution.xml");
            (new XMLWriter(fos, OutputFormat.createPrettyPrint())).write(doc);
            fos.flush();
            fos.close();

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public class CurModelContext implements AssignmentConstraintContext<CurVariable, CurValue> {
    	private double iAssignedWeight = 0.0;
    	
    	public CurModelContext(Assignment<CurVariable, CurValue> assignment) {
    		for (CurVariable var: variables()) {
    			CurValue val = assignment.getValue(var);
    			if (val != null)
    				assigned(assignment, val);
    		}
    	}

		@Override
		public void assigned(Assignment<CurVariable, CurValue> assignment, CurValue value) {
	    	iAssignedWeight += value.getStudent().getWeight();
		}

		@Override
		public void unassigned(Assignment<CurVariable, CurValue> assignment, CurValue value) {
	    	iAssignedWeight -= value.getStudent().getWeight();
		}
    	
	    public double getAssignedWeight() {
	    	return iAssignedWeight;
	    }
    }

	@Override
	public CurModelContext createAssignmentContext(Assignment<CurVariable, CurValue> assignment) {
		return new CurModelContext(assignment);
	}

}
