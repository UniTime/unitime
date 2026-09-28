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
package org.unitime.timetable.export.solver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.CourseTimetablingSolverService;
import org.unitime.timetable.solver.service.ExaminationSolverService;
import org.unitime.timetable.solver.service.InstructorSchedulingSolverService;
import org.unitime.timetable.solver.service.StudentSectioningSolverService;

@Service("org.unitime.timetable.export.Exporter:solver-config.properties")
public class ExportSolverConfig implements Exporter {
	
	@Autowired StudentSectioningSolverService studentSectioningSolverService;
	@Autowired ExaminationSolverService examinationSolverService;
	@Autowired InstructorSchedulingSolverService instructorSchedulingSolverService;
	@Autowired CourseTimetablingSolverService courseTimetablingSolverService;

	@Override
	public String reference() {
		return "solver-config.properties";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		helper.getSessionContext().checkPermission(Right.SolverConfigurations);
		String configId = helper.getParameter("configId");
		if (configId != null) {
			SolverPredefinedSetting setting = SolverPredefinedSettingDAO.getInstance().get(Long.valueOf(configId));
			helper.setup("text/x-java-properties", setting.getName() + ".properties", false);
			
			DataProperties properties = null;
	        switch (setting.getAppearanceType().getSolverType()) {
	        case STUDENT:
	        	properties = studentSectioningSolverService.createConfig(setting.getUniqueId(), null);
	        	break;
	        case EXAM:
	        	properties = examinationSolverService.createConfig(setting.getUniqueId(), null);
	        	break;
	        case INSTRUCTOR:
	        	properties = instructorSchedulingSolverService.createConfig(setting.getUniqueId(), null);
	        	break;
	        default:
	        	properties = courseTimetablingSolverService.createConfig(setting.getUniqueId(), null);
	        }
	        
	        PrintWriter pw = new PrintWriter(helper.getWriter());
	        pw.println("## Solver Configuration File");
	        pw.println("## Reference: " + setting.getName());
	        pw.println("## Name: " + setting.getDescription());
	        pw.println("## Appearance: " + SolverPredefinedSetting.Appearance.values()[setting.getAppearance()].getLabel());
	        pw.println("## Date: " + new Date());
	        pw.println("######################################");
	        for (SolverParameterGroup g: SolverPredefinedSettingDAO.getInstance().getSession().createQuery("select g from SolverParameterGroup g order by g.order", SolverParameterGroup.class).list()) {
	            if (setting.getAppearanceType() == SolverPredefinedSetting.Appearance.STUDENT_SOLVER) {
	                if (g.getSolverType() != SolverParameterGroup.SolverType.STUDENT) continue;
	            } else if (setting.getAppearanceType() == SolverPredefinedSetting.Appearance.EXAM_SOLVER) {
	            	if (g.getSolverType() != SolverParameterGroup.SolverType.EXAM) continue;
	            } else if (setting.getAppearanceType() == SolverPredefinedSetting.Appearance.INSTRUCTOR_SOLVER) {
	            	if (g.getSolverType() != SolverParameterGroup.SolverType.INSTRUCTOR) continue;
	            } else {
	            	if (g.getSolverType() != SolverParameterGroup.SolverType.COURSE) continue;
	            }
	            pw.println();
	            pw.println("## "+g.getDescription().replaceAll("<br>", "\n#"));
	            pw.println("######################################");
	            TreeSet<SolverParameterDef> parameters = new TreeSet<SolverParameterDef>(g.getParameters());
	            for (Iterator<SolverParameterDef> j=parameters.iterator();j.hasNext();) {
	                SolverParameterDef p = j.next();
	                String value = properties.getProperty(p.getName(),p.getDefault());
	                if (value==null) continue;
	                pw.println("## "+p.getDescription().replaceAll("<br>", "\n#"));
	                pw.println("## Type: "+p.getType());
	                if (value!=null && !value.equals(p.getDefault()))
	                    pw.println("## Default: "+p.getDefault());
	                pw.println(p.getName()+"="+properties.getProperty(p.getName(),p.getDefault()));
	                properties.remove(p.getName());
	            }
	        }
	        pw.println();
	        pw.println("## Other Properties");
	        pw.println("######################################");
	        for (Enumeration e=properties.propertyNames();e.hasMoreElements();) {
	            String name = (String)e.nextElement();
	            pw.println(name+"="+properties.getProperty(name));
	        }
	        pw.flush(); pw.close();
	        return;
		}
		
		String solutionId = helper.getParameter("solutionId");
		if (solutionId != null) {
			Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
			Map<Long, String> options = new HashMap<Long, String>();
			for (SolverParameter p: solution.getParameters())
				options.put(p.getUniqueId(), p.getValue());

			SolverPredefinedSetting setting = null;
			for (SolverParameter p: solution.getParameters())
				if ("General.SettingsId".equals(p.getDefinition().getName()))
					setting = SolverPredefinedSettingDAO.getInstance().get(Long.valueOf(p.getValue()));

			helper.setup("text/x-java-properties", (setting == null ? reference() : setting.getName() + ".properties"), false);
			
			DataProperties properties = null;
			if (setting != null)
				properties = courseTimetablingSolverService.createConfig(setting.getUniqueId(), options);
			else
				properties = new DataProperties();
			
			for (SolverParameter p: solution.getParameters())
				properties.setProperty(p.getDefinition().getName(), p.getValue());
			
	        PrintWriter pw = new PrintWriter(helper.getWriter());
	        pw.println("## Solver Configuration File");
	        if (setting != null)
	        	pw.println("## Reference: " + setting.getName());
	        if (setting != null)
	        	pw.println("## Name: " + setting.getDescription());
	        pw.println("## Date: " + solution.getCreated());
	        pw.println("######################################");
	        for (SolverParameterGroup g: SolverPredefinedSettingDAO.getInstance().getSession().createQuery("select g from SolverParameterGroup g order by g.order", SolverParameterGroup.class).list()) {
	            if (g.getSolverType() != SolverParameterGroup.SolverType.COURSE) continue;
	            pw.println();
	            pw.println("## "+g.getDescription().replaceAll("<br>", "\n#"));
	            pw.println("######################################");
	            TreeSet<SolverParameterDef> parameters = new TreeSet<SolverParameterDef>(g.getParameters());
	            for (Iterator<SolverParameterDef> j=parameters.iterator();j.hasNext();) {
	                SolverParameterDef p = j.next();
	                String value = properties.getProperty(p.getName(),p.getDefault());
	                if (value==null) continue;
	                pw.println("## "+p.getDescription().replaceAll("<br>", "\n#"));
	                pw.println("## Type: "+p.getType());
	                if (value!=null && !value.equals(p.getDefault()))
	                    pw.println("## Default: "+p.getDefault());
	                pw.println(p.getName()+"="+properties.getProperty(p.getName(),p.getDefault()));
	                properties.remove(p.getName());
	            }
	        }
	        pw.println();
	        pw.println("## Other Properties");
	        pw.println("######################################");
	        for (Enumeration e=properties.propertyNames();e.hasMoreElements();) {
	            String name = (String)e.nextElement();
	            pw.println(name+"="+properties.getProperty(name));
	        }
	        pw.flush(); pw.close();
	        return;
		}
		throw new IllegalArgumentException("Solver configuration id not provided.");
	}
}
