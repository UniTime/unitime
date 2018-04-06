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
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:meetings.xls")
public class EventsExportMeetingsToXLS extends EventsExportMeetingsToPDF {

	@Override
	public String reference() {
		return "meetings.xls";
	}

	@Override
	protected void print(ExportHelper helper, EventLookupRpcRequest request, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException {
		Printer printer = new XLSPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), true);
		hideColumns(printer, events, eventCookieFlags);
		print(printer, meetings(events, sort, asc));
	}
}
