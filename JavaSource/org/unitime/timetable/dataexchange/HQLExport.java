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
package org.unitime.timetable.dataexchange;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SavedHQLParameter;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public class HQLExport extends BaseExport{

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();

			document.addDocType("reports", "-//UniTime//UniTime HQL Reports DTD/EN", "http://www.unitime.org/interface/Reports.dtd");

			Element root = document.addElement("reports");
			root.addAttribute("created", new Date().toString());
			
			for (SavedHQL report: (List<SavedHQL>)getHibSession().createQuery("from SavedHQL order by name").list()) {
				exportReport(root, report);
			}
			
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	public static Element exportReport(Element parent, SavedHQL report) {
		Element reportEl = parent.addElement("report");
		reportEl.addAttribute("name", report.getName());
		for (SavedHQL.Flag flag : SavedHQL.Flag.values()) {
			if (report.isSet(flag))
				reportEl.addElement("flag").setText(flag.name());
		}
		if (report.getDescription() != null)
			reportEl.addElement("description").add(new DOMCDATA(report.getDescription()));
		if (report.getQuery() != null)
			reportEl.addElement("query").add(new DOMCDATA(report.getQuery()));
        for (SavedHQLParameter parameter: report.getParameters()) {
        	Element paramEl = reportEl.addElement("parameter");
        	paramEl.addAttribute("name", parameter.getName());
        	if (parameter.getLabel() != null)
        		paramEl.addAttribute("label", parameter.getLabel());
        	paramEl.addAttribute("type", parameter.getType());
        	if (parameter.getDefaultValue() != null)
        		paramEl.addAttribute("default", parameter.getDefaultValue());
        }
        return reportEl;
	}
}
