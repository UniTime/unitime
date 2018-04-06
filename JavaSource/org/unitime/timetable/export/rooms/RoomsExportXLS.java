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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.XLSPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.gwt.shared.RoomInterface.AttachmentTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomsColumn;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:rooms.xls")
public class RoomsExportXLS extends RoomsExportPDF {

	@Override
	public String reference() { return "rooms.xls"; }
	
	@Override
	protected void print(ExportHelper helper, List<RoomDetailInterface> rooms, ExportContext context) throws IOException {
		helper.getSessionContext().hasPermission(Right.RoomsExportCsv);
		
		List<Column> columns = new ArrayList<Column>();
		for (RoomsColumn column: RoomsColumn.values()) {
			int nrCells = getNbrCells(column, context);
			for (int idx = 0; idx < nrCells; idx++) {
				Column c = new Column(column, idx);
				if (isColumnVisible(c, context))
					columns.add(c);
			}
		}
		
		context.setGridAsText(true);
		
		XLSPrinter printer = new XLSPrinter(helper.getOutputStream(), false);
		helper.setup(printer.getContentType(), reference(), true);
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i), context).replace("<br>", "\n");
		printer.printHeader(header);
		printer.flush();
		
		for (RoomDetailInterface room: rooms) {
			A[] row = new A[columns.size()];
			for (int i = 0; i < columns.size(); i++)
				row[i] = getCell(room, columns.get(i), context);
			printer.printLine(row);
			printer.flush();
		}
		printer.close();
	}
	
	@Override
	protected A getCell(RoomDetailInterface room, Column column, ExportContext context) {
		switch (column.getColumn()) {
		case MAP:
			if (room.hasMiniMapUrl()) {
				try {
					return new A(ImageIO.read(new URL(room.getMiniMapUrl()).openStream()));
				} catch (Exception e) {
					return new A();
				}
			} else {
				return new A();
			}
		case PICTURES:
			AttachmentTypeInterface type = (column.getIndex() == 0 ? null : context.getPictureTypes().get(column.getIndex() - 1));
			if (room.hasPictures(type)) {
				A a = new A();
				for (RoomPictureInterface picture: room.getPictures(type))
					a.add(new A(picture.getName()));
				return a;
			} else{
				return new A();
			}
		default:
			return super.getCell(room, column, context);
		}
	}
}
