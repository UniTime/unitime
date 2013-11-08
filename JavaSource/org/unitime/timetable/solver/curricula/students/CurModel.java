/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solution.SolutionListener;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class CurModel extends Model<CurVariable, CurValue> {
	private static Log sLog = LogFactory.getLog(CurModel.class);
	private static DecimalFormat sDF = new DecimalFormat("0.000");
	private List<CurStudent> iStudents = new ArrayList<CurStudent>();
	private Map<Long, CurCourse> iCourses = new Hashtable<Long, CurCourse>();
	private List<CurCourse> iSwapableCourses = new ArrayList<CurCourse>();
	private CurStudentLimit iStudentLimit = null;
	private double iMinStudentWeight = Float.MAX_VALUE, iMaxStudentWeight = 0.0, iTotalStudentWeight = 0.0;
	private double iAssignedWeight = 0.0, iBestAssignedWeight  = 0.0, iMaxAssignedWeight = 0.0;
	
	public CurModel(Collection<CurStudent> students) {
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
	public Map<String, String> getInfo() {
		Map<String, String> ret = super.getInfo();
		ret.put("Students", String.valueOf(getStudents().size()));
		ret.put("Courses", String.valueOf(getCourses().size()));
		double avgEnrollment = ((double)variables().size()) / getCourses().size();
		double rmsEnrollment = 0.0;
		for (CurCourse c1: getCourses())
			rmsEnrollment += (c1.getNrStudents() - avgEnrollment) * (c1.getNrStudents() - avgEnrollment);
		ret.put("Course size", sDF.format(avgEnrollment) + " ± " + sDF.format(Math.sqrt(rmsEnrollment / getCourses().size())));
		int totalCourses = 0;
		for (CurStudent student: getStudents())
			totalCourses += student.getCourses().size();
		double avgCourses = ((double)totalCourses) / getStudents().size();
		double rmsCourses = 0.0;
		for (CurStudent student: getStudents())
			rmsCourses += (student.getCourses().size() - avgCourses) * (student.getCourses().size() - avgCourses);
		ret.put("Courses per student", sDF.format(avgCourses) + " ± " + sDF.format(Math.sqrt(rmsCourses / getStudents().size())) +
				" (limit: " + getStudentLimit().getMinLimit() + " .. " + getStudentLimit().getMaxLimit() + ")");
		int totalShare = 0;
		double totalError = 0;
		double rmsError = 0.0;
		int pairs = 0;
		for (CurCourse c1: getCourses())
			for (CurCourse c2: getCourses())
				if (c1.getCourseId() < c2.getCourseId()) {
					double share = c1.share(c2);
					double target = c1.getTargetShare(c2.getCourseId());
					totalError += Math.abs(share - target);
					rmsError += (share - target) * (share - target);
					pairs ++;
					totalShare += share;
				}
		ret.put("Errors", sDF.format(totalError) + " (" + sDF.format(100.0 * totalError / totalShare) + "% of total share, avg: " + sDF.format(((double)totalError) / pairs) + ", rms: " + sDF.format(Math.sqrt(rmsError / pairs)) + ")");
		ret.put("Assigned Student Weight", sDF.format(getAssignedWeight()) + "/" + sDF.format(getMaxWeight()));
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
	
    public double getTotalValue() {
        double value = 0;
        for (CurCourse c1: iCourses.values())
			for (CurCourse c2: iCourses.values())
				if (c1.getCourseId() < c2.getCourseId())
					value += c1.penalty(c2);
        return value;
    }
    
    public double getAssignedWeight() {
    	return iAssignedWeight;
    }
    
    public double getMaxWeight() {
    	return iMaxAssignedWeight;
    }

    public double getBestWeight() {
    	return iBestAssignedWeight;
    }
    
    @Override
    public void saveBest() {
    	super.saveBest();
    	iBestAssignedWeight = iAssignedWeight;
    }
    
    @Override
    public void clearBest() {
    	super.clearBest();
    	iBestAssignedWeight = 0.0;
    }
    
    @Override
    public void afterAssigned(long iteration, CurValue value) {
    	super.afterAssigned(iteration, value);
    	iAssignedWeight += value.getStudent().getWeight();
    }
    
    @Override
    public void afterUnassigned(long iteration, CurValue value) {
    	super.afterUnassigned(iteration, value);
    	iAssignedWeight -= value.getStudent().getWeight();
    }

    public String toString() {
    	return assignedVariables().size() + "/" + variables().size() + " V:" + sDF.format(getTotalValue()) + " A:" + sDF.format(getAssignedWeight()) + "/" + sDF.format(getMaxWeight());
    }
    
    public void ifs() {
        net.sf.cpsolver.ifs.util.DataProperties cfg = new net.sf.cpsolver.ifs.util.DataProperties();
        cfg.setProperty("Termination.Class", "org.unitime.timetable.solver.curricula.students.CurTermination");
        cfg.setProperty("Termination.StopWhenComplete", "false");
        cfg.setProperty("Termination.TimeOut", "60");
        cfg.setProperty("Termination.MaxIdle", "1000");
        cfg.setProperty("Comparator.Class", "org.unitime.timetable.solver.curricula.students.CurComparator");
        cfg.setProperty("Variable.Class", "org.unitime.timetable.solver.curricula.students.CurVariableSelection");
        cfg.setProperty("Value.Class", "org.unitime.timetable.solver.curricula.students.CurValueSelection");
        cfg.setProperty("General.SaveBestUnassigned", "-1");

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
				sLog.debug(solution.getModel().toString()+", i:" + solution.getIteration());
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

        sLog.debug("Best solution found after " + solution.getBestTime() + " seconds ("
                + solution.getBestIteration() + " iterations).");
        sLog.debug("Number of assigned variables is " + solution.getModel().assignedVariables().size());
        sLog.debug("Total value of the solution is " + solution.getModel().getTotalValue());
    }

    public void saveAsXml(Element root) {
    	List<Long> courses = new ArrayList<Long>();
    	DecimalFormat df = new DecimalFormat("0.##########");
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
			for (CurCourse course: student.getCourses()) {
				courseIds += (courseIds.isEmpty() ? "" : ",") + course.getCourseId();
			}
			studentElement.setText(courseIds);
		}
    }
    
    public static CurModel loadFromXml(Element root) {
    	List<Element> studentElements = root.elements("student");
    	List<CurStudent> students = new ArrayList<CurStudent>();
		for (Element studentElement: studentElements) {
			students.add(new CurStudent(Long.valueOf(studentElement.attributeValue("id")), Float.parseFloat(studentElement.attributeValue("weight", "1.0"))));
		}
		CurModel m = new CurModel(students);
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
    				m.setTargetShare(courseId, courses.get(j), Float.parseFloat(share[j]), false);
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
						if (v.getAssignment() == null) { var = v; break; }
					var.assign(0, new CurValue(var, student));
				}
			}
		}
		return m;
    }
    
    public void naive(DataProperties cfg) {
    	sLog.debug("  -- running naive");
		int idle = 0, it = 0;
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
				sLog.debug("  -- best value: " + this);
			}
			it++; idle++;
		}
		sLog.debug("  -- final value: " + this);
    }
    
    public void hc(DataProperties cfg) {
    	sLog.debug("  -- running hill climber");
		int it = 0, idle = 0;
		double total = getTotalValue();
		double best = total;
		CurHillClimber hc = new CurHillClimber(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
		while (idle < 1000) {
			Neighbour<CurVariable, CurValue> n = hc.selectNeighbour(solution);
			if (n == null) break;
			if (unassignedVariables().isEmpty() && n.value() >= -1e7f) break;
			total += n.value();
			n.assign(it);
			if (total < best) {
				best = total;
				idle = 0;
				sLog.debug("  -- best value: " + this);
			} else {
				idle++;
			}
			it++;
		}
		cfg.setProperty("Curriculum.HC.Iters", String.valueOf(it));
		cfg.setProperty("Curriculum.HC.Value", String.valueOf(getTotalValue()));
		sLog.debug("  -- final value: " + this);
    }
    
    public void deluge(DataProperties cfg) {
    	double f = cfg.getPropertyDouble("Curriculum.Deluge.Factor", 0.999999);
    	sLog.debug("  -- running great deluge");
		int it = 0;
		double total = getTotalValue();
		double bound = 1.25 * total;
		double best = getTotalValue();
		CurStudentSwap sw = new CurStudentSwap(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
		saveBest();
		while (!getSwapCourses().isEmpty() && bound > 0.75 * total && total > 0) {
			Neighbour<CurVariable, CurValue> n = sw.selectNeighbour(solution);
			if (n != null) {
				double value = n.value();
				if (value <= 0.0 || total + value < bound) {
					n.assign(it);
					if (total + value < best) {
						best = total + value;
						saveBest();
						sLog.debug("  -- best value: " + this + ", bound: " + bound);
					}
					total += value;
				}
			}
			bound *= f;
			it++;
		}
		cfg.setProperty("Curriculum.Deluge.Iters", String.valueOf(it));
		restoreBest();
		cfg.setProperty("Curriculum.Deluge.Value", String.valueOf(getTotalValue()));
		sLog.debug("  -- final value: " + this);
    }
    
    public void fast(DataProperties cfg) {
    	sLog.debug("  -- running fast");
		int idle = 0, it = 0;
		double total = getTotalValue();
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
				sLog.debug("  -- best value: " + this);
			}
			it++; idle++;
		}
		sLog.debug("  -- final value: " + this);
    }
    
    public DataProperties solve() {
    	return solve(new DataProperties());
    }
    
    public DataProperties solve(DataProperties cfg) {
    	if (cfg == null) cfg = new DataProperties();
    	sLog.debug("  -- setting up the solver");
    	CurVariableSelection var = new CurVariableSelection(cfg);
    	CurValueSelection vs = new CurValueSelection(cfg);
		Solution<CurVariable, CurValue> solution = new Solution<CurVariable, CurValue>(this);
    	sLog.debug("  -- creating initial assignment");
    	while (!unassignedVariables().isEmpty()) {
    		CurVariable course = var.selectVariable(solution);
    		if (course.getCourse().isComplete()) {
    			sLog.debug("    -- all complete");
    			break;
    		}
    		CurValue student = vs.selectValueSlow(solution, course);
    		if (student == null) {
    			sLog.debug("    -- no student for " + course.getCourse().getCourseName());
    			break;
    		}
    		student.variable().assign(solution.getIteration(), student);
    	}
    	for (CurCourse course: getCourses()) {
    		if (!course.isComplete()) {
    			sLog.debug("    -- incomplete " + course.getCourseName() + ": " + getCourse(course.getCourseId()).getStudents() + " (" + course.getSize() + "/" + course.getOriginalMaxSize() + ")");
    		}
    	}
    	cfg.setProperty("Curriculum.Initial.Value", String.valueOf(getTotalValue()));
		sLog.debug("  -- initial value: " + this);
		hc(cfg); // or fast(cfg);
		deluge(cfg); // or naive(cfg);
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
    		if (s.getWeight() != m.getStudents().get(idx).getWeight()) return false;
    	}
    	for (CurCourse c1: getCourses()) {
    		CurCourse x1 = m.getCourse(c1.getCourseId());
    		if (x1 == null || x1.getNrStudents() != c1.getNrStudents() || !equals(x1.getPriority(), c1.getPriority())) return false;
    		for (CurCourse c2: getCourses())
    			if (c1.getCourseId() < c2.getCourseId() && Math.abs(c1.getTargetShare(c2.getCourseId()) - x1.getTargetShare(c2.getCourseId())) > 0.001) {
    				return false;
    			}
    	}
    	return true;
    }
    
    public static boolean equals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }
    
    public static void main(String[] args) {
    	try {
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
			m.saveAsXml(d0.addElement("curriculum"));
			sLog.info(d0.asXML());
			
    		sLog.info("Loaded: " + ToolBox.dict2string(m.getInfo(), 2));

    		m.solve();

    		sLog.info("Solution: " + ToolBox.dict2string(m.getInfo(), 2));
    		
			Document d1 = DocumentHelper.createDocument();
			m.saveAsXml(d1.addElement("curriculum"));
			sLog.info(d1.asXML());

            
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
    			sLog.info(course.getCourseName() + ": " + m.getCourse(course.getCourseId()).getStudents() + " (" + course.getSize() + "/" + course.getOriginalMaxSize() + ")");
        		for (CurCourse other: courses) {
        			if (other.getCourseId() <= course.getCourseId()) continue;
    				double share = course.share(other);
    				double target = course.getTargetShare(other.getCourseId());
    				sLog.info("  " + other.getCourseName() + ": share=" + share + ", target=" + target + ", penalty=" + Math.abs(target - share)); 
    				penalty += Math.abs(target - share);
    			}
    		}
    		sLog.info("Total penalty: " + penalty);
    		
			Document doc = DocumentHelper.createDocument();
			m.saveAsXml(doc.addElement("curriculum"));
			FileOutputStream fos = new FileOutputStream("/Users/muller/solution.xml");
            (new XMLWriter(fos, OutputFormat.createPrettyPrint())).write(doc);
            fos.flush();
            fos.close();

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

}
