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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.util.PdfFont;

import com.lowagie.text.Font;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:roomfeatures.pdf")
public class RoomFeaturesExportPDF extends RoomFeaturesExportCSV {

	@Override
	public String reference() { return "roomfeatures.pdf"; }
	
	protected void print(ExportHelper helper, List<FeatureInterface> features, int dm, String department) throws IOException {
		Printer out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		print(out, features, dm, department);
		out.flush(); out.close();
	}


	protected void printHeader(Printer out) throws IOException {
		out.printHeader(
				MESSAGES.colName(),
				MESSAGES.colAbbreviation(),
				MESSAGES.colType(),
				MESSAGES.colDepartment(),
				MESSAGES.colRooms(),
				MESSAGES.colDescription()
				);
	}
	
	protected void printLine(Printer out, FeatureInterface feature, int dm) throws IOException {
		((PDFPrinter)out).printLine(
				new A(feature.getLabel()),
				new A(feature.getAbbreviation()),
				new A(feature.getType() == null ? "" : feature.getType().getAbbreviation()),
				new A(feature.isDepartmental() ? dept2string(feature.getDepartment(), dm) : ""),
				rooms(feature),
				new A(feature.getDescription()).maxWidth(250f));
	}
	
	protected A rooms(FeatureInterface feature) {
		A ret = new A();
		if (feature.hasRooms()) {
			Font font = PdfFont.getFont(true);
			String rooms = "";
			for (Iterator<Entity> i = feature.getRooms().iterator(); i.hasNext(); ) {
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
