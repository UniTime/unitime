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
package org.unitime.timetable.server.rooms;

import java.io.IOException;
import java.text.DecimalFormat;

import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.security.rights.Right;

@Service("org.unitime.timetable.export.Exporter:buildings.pdf")
public class ExportBuildingsPDF implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public String reference() {
		return "buildings.pdf";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		helper.getSessionContext().checkPermission(Right.BuildingExportPdf);
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		
        DecimalFormat df5 = new DecimalFormat("####0.######");
        out.printHeader(MESSAGES.colAbbreviation(), MESSAGES.colName(), MESSAGES.colExternalId(), MESSAGES.colCoordinateX(), MESSAGES.colCoordinateY());

        for (Building b: Building.findAll(helper.getAcademicSessionId())) {
        	out.printLine(
        			b.getAbbreviation(),
                    b.getName(),
                    b.getExternalUniqueId()==null ? "" : b.getExternalUniqueId().toString(),
                    (b.getCoordinateX()==null ? "" : df5.format(b.getCoordinateX())),
                    (b.getCoordinateY()==null ? "" : df5.format(b.getCoordinateY()))
                    );
        }
		out.close();
	}

}
