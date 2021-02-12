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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingsColumn;
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
        
        List<Building> buildings = Building.findAll(helper.getAcademicSessionId());
		String sort = helper.getParameter("sort");
		if (sort != null && !"0".equals(sort)) {
			int sortBy = Integer.valueOf(sort);
			BuildingComparator cmp = null;
			if (sortBy == 0) {
				// no sort
			} else if (sortBy > 0) {
				cmp = new BuildingComparator(BuildingsColumn.values()[sortBy - 1], true);
			} else {
				cmp = new BuildingComparator(BuildingsColumn.values()[-1 - sortBy], false);
			}
			if (cmp != null)
				Collections.sort(buildings, cmp);
		}

        for (Building b: buildings) {
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
	
	public static class BuildingComparator implements Comparator<Building>{
		private BuildingsColumn iColumn;
		private boolean iAsc;
		
		public BuildingComparator(BuildingsColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		public int compareById(Building r1, Building r2) {
			return compare(r1.getUniqueId(), r2.getUniqueId());
		}
		
		public int compareByName(Building r1, Building r2) {
			return compare(r1.getName(), r2.getName());
		}

		public int compareByAbbreviation(Building r1, Building r2) {
			return compare(r1.getAbbreviation(), r2.getAbbreviation());
		}
		
		public int compareByExternalId(Building r1, Building r2) {
			return compare(r1.getExternalUniqueId(), r2.getExternalUniqueId());
		}
		
		protected int compareByColumn(Building r1, Building r2) {
			switch (iColumn) {
			case NAME: return compareByName(r1, r2);
			case ABBREVIATION: return compareByAbbreviation(r1, r2);
			case EXTERNAL_ID: return compareByExternalId(r1, r2);
			default: return compareByAbbreviation(r1, r2);
			}
		}
		
		public static boolean isApplicable(BuildingsColumn column) {
			switch (column) {
			case ABBREVIATION:
			case NAME:
			case EXTERNAL_ID:
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(Building r1, Building r2) {
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByAbbreviation(r1, r2);
			if (cmp == 0) cmp = compareById(r1, r2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
	}

}
