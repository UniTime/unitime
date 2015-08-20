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
package org.unitime.timetable.export.rooms;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Font;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:roomgroups.pdf")
public class RoomGroupsExportPDF extends RoomGroupsExportCSV {

	@Override
	public String reference() { return "roomgroups.pdf"; }
	
	protected void print(ExportHelper helper, List<GroupInterface> groups, int dm, String department) throws IOException {
		Printer out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		print(out, groups, dm, department);
		out.flush(); out.close();
	}


	protected void printHeader(Printer out) throws IOException {
		out.printHeader(
				MESSAGES.colName(),
				MESSAGES.colAbbreviation(),
				MESSAGES.colDepartment(),
				MESSAGES.colRooms(),
				MESSAGES.colDescription()
				);
	}
	
	protected void printLine(Printer out, GroupInterface group, int dm) throws IOException {
		((PDFPrinter)out).printLine(
				new A(group.getLabel()),
				new A(group.getAbbreviation()),
				new A(group.isDepartmental() ? dept2string(group.getDepartment(), dm) : group.isDefault() ? MESSAGES.exportDefaultRoomGroup() : MESSAGES.exportGlobalRoomGroup()),
				rooms(group),
				new A(group.getDescription()).maxWidth(250f));
	}
	
	protected A rooms(GroupInterface group) {
		A ret = new A();
		if (group.hasRooms()) {
			Font font = PdfFont.getFont(true);
			String rooms = "";
			for (Iterator<Entity> i = group.getRooms().iterator(); i.hasNext(); ) {
				String chip = name(i.next()) + (i.hasNext() ? ", " : "");
				if (font.getBaseFont().getWidthPoint(rooms + chip, font.getSize()) < 500f)
					rooms += chip;
				else {
					ret.add(new A(rooms));
					rooms = chip;
				}
			}
			if (!rooms.isEmpty())
				ret.add(new A(rooms));
				
		}
		return ret;
	}
	
}
