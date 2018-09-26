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
package org.unitime.timetable.export.events;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;


/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:event-enrollments.pdf")
public class EventEnrollmentExportPDF extends EventEnrollmentExport {
	
	@Override
	public String reference() { return "event-enrollments.pdf"; }
	
	@Override
	protected Printer createPrinter(ExportHelper helper) throws IOException {
		Printer out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		return out;
	}
}
