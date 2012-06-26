package org.unitime.timetable.onlinesectioning.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.unitime.timetable.onlinesectioning.reports.OnlineSectioningReport.Counter;
import org.unitime.timetable.onlinesectioning.solver.OnlineSectioningSelection;
import org.unitime.timetable.onlinesectioning.solver.StudentSchedulingAssistantWeights;
import org.unitime.timetable.onlinesectioning.solver.SuggestionSelection;
import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection;

import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;
import net.sf.cpsolver.ifs.util.JProf;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.StudentSectioningXMLLoader;
import net.sf.cpsolver.studentsct.StudentSectioningXMLSaver;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

public class InMemorySectioningTest {
	public static Logger sLog = Logger.getLogger(InMemorySectioningTest.class);
	
	private StudentSectioningModel iModel;
	private OnlineSectioningSelection iSelection;
	private StudentSchedulingAssistantWeights iWeights;
	
	private Map<String, Counter> iCounters = new HashMap<String, Counter>();
	
	public InMemorySectioningTest(DataProperties config) {
		iModel = new TestModel(config);
		iModel.setDistanceConflict(new DistanceConflict(new DistanceMetric(iModel.getProperties()), iModel.getProperties()));
		iModel.addModelListener(iModel.getDistanceConflict());
		iModel.setTimeOverlaps(new TimeOverlapsCounter(null, iModel.getProperties()));
		iModel.addModelListener(iModel.getTimeOverlaps());

		if (config.getPropertyBoolean("StudentWeights.MultiCriteria", true)) {
			iSelection = new MultiCriteriaBranchAndBoundSelection(config);
		} else {
			iSelection = new SuggestionSelection(config);
		}
		iSelection.setModel(model());
		
		iWeights = new StudentSchedulingAssistantWeights(model().getProperties());
		model().setStudentWeights(iWeights);
		
		sLog.info("Using " + (config.getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
				(config.getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal") + " weighting model" +
				" with " + config.getPropertyInt("Neighbour.BranchAndBoundTimeout", 1000) +" ms time limit.");

	}
	
	public StudentSectioningModel model() { return iModel; }
	
	public void inc(String name, double value) {
		Counter c = iCounters.get(name);
		if (c == null) {
			c = new Counter();
			iCounters.put(name, c);
		}
		c.inc(value);
	}
	
	public void inc(String name) {
		inc(name, 1.0);
	}
	
	public void section(Student student) {
		Hashtable<CourseRequest, Set<Section>> preferredSectionsForCourse = new Hashtable<CourseRequest, Set<Section>>();
		for (Request r: student.getRequests()) {
			if (r.getAssignment() != null && r.getAssignment().isCourseRequest()) {
				preferredSectionsForCourse.put((CourseRequest)r, r.getAssignment().getSections());
				updateSpace(r.getAssignment(), true); 
			}
			r.setInitialAssignment(r.getAssignment());
			if (r instanceof CourseRequest) {
				for (Course course: ((CourseRequest)r).getCourses()) {
					for (Config config: course.getOffering().getConfigs()) {
						for (Subpart subpart: config.getSubparts()) {
							for (Section section: subpart.getSections()) {
								if (section.getLimit() <= 0) {
									section.setPenalty(0);
								} else {
									int limit = section.getLimit();
									int enrolled = section.getEnrollments().size();
									if (r.getAssignment() != null && r.getAssignment().getSections().contains(section)) enrolled --;
									double available = Math.round(limit - enrolled - section.getSpaceExpected());
									section.setPenalty(- available / limit);
						        }
							}
						}
					}
				}
			}
		}
				
		iSelection.setPreferredSections(preferredSectionsForCourse);
		iSelection.setRequiredSections(new Hashtable<CourseRequest, Set<Section>>());
		iSelection.setRequiredFreeTimes(new HashSet<FreeTimeRequest>());
		iModel.clearBest();
		
		long t0 = JProf.currentTimeMillis();
		BranchBoundNeighbour neighbour = iSelection.select(student);
		long time = JProf.currentTimeMillis() - t0;
		inc("[C] CPU Time", time);
		inc("[S] Student");
		if (neighbour == null) {
			inc("[F] Failure");
		} else {
			neighbour.assign(0);
			int a = 0, u = 0, c = 0;
			for (Request r: student.getRequests()) {
				if (r instanceof CourseRequest) {
					if (r.getAssignment() != null) {
						a++;
						updateSpace(r.getAssignment(), false);
					} else {
						u++;
					}
					if (r.getInitialAssignment() != null && !r.getInitialAssignment().equals(r.getAssignment()))
						c++;
				}
			}
			if (a > 0)
				inc("[A] Assigned", a);
			if (u > 0)
				inc("[A] Not Assigned", u);
			if (c > 0)
				inc("[A] Changed", c);
			inc("[V] Value", neighbour.value());
		}
		inc("[T0] Time <10ms", time < 10 ? 1 : 0);
		inc("[T1] Time <100ms", time < 100 ? 1 : 0);
		inc("[T2] Time <250ms", time < 250 ? 1 : 0);
		inc("[T3] Time <500ms", time < 500 ? 1 : 0);
		inc("[T4] Time <1s", time < 1000 ? 1 : 0);
		inc("[T5] Time >=1s", time >= 1000 ? 1 : 0);
	}
	
	public static void updateSpace(Enrollment enrollment, boolean increment) {
    	if (enrollment == null || !enrollment.isCourseRequest()) return;
        for (Section section : enrollment.getSections())
            section.setSpaceHeld(section.getSpaceHeld() + (increment ? 1.0 : -1.0));
        List<Enrollment> feasibleEnrollments = new ArrayList<Enrollment>();
        for (Enrollment enrl : enrollment.getRequest().values()) {
        	if (!enrl.getCourse().equals(enrollment.getCourse())) continue;
            boolean overlaps = false;
            for (Request otherRequest : enrollment.getRequest().getStudent().getRequests()) {
                if (otherRequest.equals(enrollment.getRequest()) || !(otherRequest instanceof CourseRequest))
                    continue;
                Enrollment otherErollment = otherRequest.getAssignment();
                if (otherErollment == null)
                    continue;
                if (enrl.isOverlapping(otherErollment)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps)
                feasibleEnrollments.add(enrl);
        }
        double change = 1.0 / feasibleEnrollments.size();
        for (Enrollment feasibleEnrollment : feasibleEnrollments)
            for (Section section : feasibleEnrollment.getSections())
                section.setSpaceExpected(section.getSpaceExpected() + (increment ? +change : -change));
    }
	
	public void run() {
        sLog.info("Input: " + ToolBox.dict2string(model().getExtendedInfo(), 2));

        List<Student> students = new ArrayList<Student>(model().getStudents());
        Collections.shuffle(students);

        long t0 = System.currentTimeMillis();
        int i = 1;
        for (Student student: students) {
        	section(student);
        	long time = System.currentTimeMillis() - t0;
        	if (time > 60000 * i) {
        		i++;
        		sLog.info("Progress [" + (time / 60000) + "m]: " + ToolBox.dict2string(model().getExtendedInfo(), 2));
        	}
        }
        
        sLog.info("Output: " + ToolBox.dict2string(model().getExtendedInfo(), 2));
	}
	
	
	public class TestModel extends StudentSectioningModel {
		public TestModel(DataProperties config) {
			super(config);
		}

		@Override
		public Map<String,String> getExtendedInfo() {
			Map<String, String> ret = super.getExtendedInfo();
			for (Map.Entry<String, Counter> e: iCounters.entrySet())
				ret.put(e.getKey(), e.getValue().toString());
			ret.put("Weighting model",
					(model().getProperties().getPropertyBoolean("StudentWeights.MultiCriteria", true) ? "multi-criteria ": "") +
					(model().getProperties().getPropertyBoolean("StudentWeights.PriorityWeighting", true) ? "priority" : "equal"));
			ret.put("B&B time limit", model().getProperties().getPropertyInt("Neighbour.BranchAndBoundTimeout", 1000) +" ms");
			return ret;
		}
	}
	
	public static void main(String[] args) {
		try {
			System.setProperty("jprof", "cpu");
			BasicConfigurator.configure();
			
            DataProperties cfg = new DataProperties();
			cfg.setProperty("Neighbour.BranchAndBoundTimeout", "1000");
			cfg.setProperty("Suggestions.Timeout", "1000");
			cfg.setProperty("Extensions.Classes", DistanceConflict.class.getName() + ";" + TimeOverlapsCounter.class.getName());
			cfg.setProperty("StudentWeights.Class",  StudentSchedulingAssistantWeights.class.getName());
			cfg.setProperty("StudentWeights.PriorityWeighting", "true");
			cfg.setProperty("StudentWeights.LeftoverSpread", "true");
			cfg.setProperty("StudentWeights.BalancingFactor", "0.0");
			cfg.setProperty("Reservation.CanAssignOverTheLimit", "true");
			cfg.setProperty("Distances.Ellipsoid", DistanceMetric.Ellipsoid.WGS84.name());
			cfg.setProperty("StudentWeights.MultiCriteria", "true");
			
            cfg.setProperty("log4j.rootLogger", "INFO, A1");
            cfg.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            cfg.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            cfg.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            cfg.setProperty("log4j.logger.org.hibernate","INFO");
            cfg.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            cfg.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            cfg.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            cfg.setProperty("log4j.logger.net","INFO");
            
            cfg.putAll(System.getProperties());

            File input = new File(args[0]);
            String run = cfg.getProperty("run", "r0");
            cfg.setProperty("log4j.rootLogger", "INFO, A1");
            cfg.setProperty("log4j.appender.A1", "org.apache.log4j.FileAppender");
            cfg.setProperty("log4j.appender.A1.File", new File(input.getParentFile(), input.getName().substring(0, input.getName().lastIndexOf('.')) + "-" + run + ".txt").getAbsolutePath());
            
            PropertyConfigurator.configure(cfg);
            
            InMemorySectioningTest test = new InMemorySectioningTest(cfg);
            
            StudentSectioningXMLLoader loader = new StudentSectioningXMLLoader(test.model());
            loader.setInputFile(input);
            loader.load();
            
            test.run();
            
            Solver<Request, Enrollment> s = new Solver<Request, Enrollment>(cfg);
            s.setInitalSolution(test.model());
            StudentSectioningXMLSaver saver = new StudentSectioningXMLSaver(s);
            File output = new File(input.getParentFile(), input.getName().substring(0, input.getName().lastIndexOf('.')) + "-" + run + ".xml");
            saver.save(output);
		} catch (Exception e) {
			sLog.error("Test failed: " + e.getMessage(), e);
		}
	}
}
