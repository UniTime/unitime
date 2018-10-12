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
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.model.SectioningSolutionLog;
import org.unitime.timetable.model.dao.SectioningSolutionLogDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:published-solution.xml.gz")
public class ExportPublishedSectioningSolutionXML implements Exporter {

	@Override
	public String reference() {
		return "published-solution.xml.gz";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		String id = helper.getParameter("id");
		if (id == null || id.isEmpty())
			throw new IllegalArgumentException("Id parameter was not provided.");
		SectioningSolutionLog solution = SectioningSolutionLogDAO.getInstance().get(Long.valueOf(id));
		if (solution == null)
			throw new IllegalArgumentException("Solution " + id + " does not exist.");
		helper.getSessionContext().checkPermission(solution.getSession(), Right.StudentSectioningSolverPublish);
		helper.setup("application/x-gzip", "published-solution-" + (new SimpleDateFormat("yyyy-MM-dd-HHmm").format(solution.getTimeStamp())) + ".xml.gz", true);
        OutputStream out = helper.getOutputStream(); 
        out.write(solution.getData());
        out.flush(); out.close();
	}
}
