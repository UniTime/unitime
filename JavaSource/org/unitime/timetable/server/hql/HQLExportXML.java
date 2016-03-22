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
package org.unitime.timetable.server.hql;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.dao.SavedHQLDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:hql.xml")
public class HQLExportXML implements Exporter {

	@Override
	public String reference() {
		return "report.xml";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		String s = helper.getParameter("id");
		if (s == null)
			throw new IllegalArgumentException("No report provided, please set the id parameter.");
		SavedHQL report = SavedHQLDAO.getInstance().get(Long.valueOf(s));
		if (report == null)
			throw new IllegalArgumentException("Report " + s + " does not exist.");

		helper.getSessionContext().checkPermission(report, Right.HQLReportEdit);

		helper.setup("text/xml", report.getName().replace('/', '-').replace('\\', '-').replace(':', '-') + ".xml",
				false);

		Document document = DocumentHelper.createDocument();
		document.addDocType("report", "-//UniTime//UniTime HQL Reports DTD/EN",
				"http://www.unitime.org/interface/Reports.dtd");
		Element reportEl = document.addElement("report");
		reportEl.addAttribute("name", report.getName());
		for (SavedHQL.Flag flag : SavedHQL.Flag.values()) {
			if (report.isSet(flag))
				reportEl.addElement("flag").setText(flag.name());
		}
		if (report.getDescription() != null)
			reportEl.addElement("description").add(new DOMCDATA(report.getDescription()));
		if (report.getQuery() != null)
			reportEl.addElement("query").add(new DOMCDATA(report.getQuery()));
		reportEl.addAttribute("created", new Date().toString());

		OutputStream out = helper.getOutputStream();
		new XMLWriter(out, OutputFormat.createPrettyPrint()).write(document);
	}
}
