package org.unitime.timetable.test;

import java.util.List;

import net.sf.cpsolver.ifs.util.ArrayList;

import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.LastLikeCourseDemand;

public class MakeLastLikeDemandsFromCurricula {
	
	public List<LastLikeCourseDemand> createDemandsForCurriculum(Curriculum c) {
		List<LastLikeCourseDemand> demands = new ArrayList<LastLikeCourseDemand>();
		
		for (CurriculumClassification clasf: c.getClassifications()) {
			for (CurriculumCourse course: clasf.getCourses()) {
				
				
			}
		}
		
		return demands;
	}

}
