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
import java.util.Collections;
import java.util.Comparator;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.TableInterface;

/**
 * @author Tomas Muller
 */
public abstract class TableExporter implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	protected void fillInFilter(FilterInterface filter, ExportHelper helper) {
		for (FilterParameterInterface parameter: filter.getParameters()) {
			if (parameter.isMultiSelect()) {
				parameter.setValue("");
				String[] values = helper.getParameterValues(parameter.getName());
				if (values != null)
					for (String v: values)
						parameter.setValue((parameter.getValue().isEmpty() ? "" : ",") + v);
				else
					parameter.setValue(parameter.getDefaultValue());
			} else {
				String value = helper.getParameter(parameter.getName());
				if (value != null)
					parameter.setValue(value);
				else
					parameter.setValue(parameter.getDefaultValue());
			}
		}
	}
	
	protected void sort(TableInterface table, ExportHelper helper) throws IOException {
		String sortParameter = helper.getParameter("sort");
		if (sortParameter == null || sortParameter.isEmpty()) return;
		int sort = Integer.parseInt(sortParameter);
		if (sort == 0) return;
		final int column = (sort > 0 ? sort - 1 : -sort - 1);
		final boolean asc = (sort > 0);
		Collections.sort(table.getRows(), new Comparator<TableInterface.TableRowInterface>() {
			@Override
			public int compare(TableInterface.TableRowInterface r1, TableInterface.TableRowInterface r2) {
				return (asc ? r1.compareTo(r2, column, true) : r2.compareTo(r1, column, true));
			}
		});
	}
	
	protected void printTableCSV(TableInterface table, ExportHelper helper) throws IOException {
		sort(table, helper);
		
		Printer out = new CSVPrinter(helper.getWriter(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		if (table.hasName())
			out.printLine(table.getName());
		
		String[] header = new String[table.getHeader().length];
		for (int i = 0; i < table.getHeader().length; i++) {
			header[i] = table.getHeader(i).getName().replace("<br>", "\n");
		}	
		out.printHeader(header);
		out.flush();
		
		for (TableInterface.TableRowInterface row: table.getRows()) {
			String[] line = new String[row.getNrCells()];
			for (int i = 0; i < row.getNrCells(); i++) {
				line[i] = convertCSV(row.getCell(i));
			}
			out.printLine(line);
		}
		
		if (table.hasColumnDescriptions()) {
			out.printLine();
			for (TableInterface.TableHeaderIterface h: table.getHeader())
				if (h.hasDescription())
					out.printLine(h.getName(), h.getDescription());
		}
		
		out.flush(); out.close();
	}
	
	protected String convertCSV(TableInterface.TableCellInterface cell) {
		if (cell == null || cell.getValue() == null) return null;
		if (cell instanceof TableInterface.TableCellMultiLine) {
			TableInterface.TableCellMultiLine history = (TableInterface.TableCellMultiLine)cell;
			String ret = "";
			for (int i = 0; i < history.getNrChunks(); i++) {
				if (i > 0) ret += "\n";
				ret += convertCSV(history.get(i));
			}
			return ret;
		}
		if (cell instanceof TableInterface.TableCellItems) {
			TableInterface.TableCellItems items = (TableInterface.TableCellItems)cell;
			return items.getFormattedValue("\n");
		}
		if (cell instanceof TableInterface.TableCellRooms) {
			TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)cell;
			return rooms.getValue("\n");
		}
		if (cell instanceof TableInterface.TableCellClassName) {
			TableInterface.TableCellClassName names = (TableInterface.TableCellClassName)cell;
			String ret = cell.getFormattedValue();
			if (names.hasAlternatives())
				for (String alternative: names.getAlternatives())
					ret += "\n" + alternative;
			return ret;
		}
		if (cell instanceof TableInterface.TableCellChange) {
			TableInterface.TableCellChange change = (TableInterface.TableCellChange)cell;
			if (change.getFirst() != null && change.getSecond() != null && change.getFirst().compareTo(change.getSecond()) == 0) {
				if (change.getFirst() instanceof TableInterface.TableCellRooms) {
					TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getFirst();
					String ret = "";
					for (int i = 0; i < rooms.getNrRooms(); i++) {
						if (i > 0) ret += CONSTANTS.itemSeparator();
						ret += rooms.getName(i);
					}
					return ret;
				} else {
					return change.getFirst().getFormattedValue();
				}
			}
			String ret = "";
			if (change.getFirst() == null) {
				ret += MESSAGES.notAssigned();
			} else {
				if (change.getFirst() instanceof TableInterface.TableCellRooms) {
					TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getFirst();
					for (int i = 0; i < rooms.getNrRooms(); i++) {
						if (i > 0) ret += CONSTANTS.itemSeparator();
						ret += rooms.getName(i);
					}
					if (rooms.getNrRooms() == 0) ret += MESSAGES.notApplicable();
				} else {
					ret += change.getFirst().getFormattedValue();
				}
			}
			ret += " \u2192 ";
			if (change.getSecond() == null) {
				ret += MESSAGES.notAssigned();
			} else {
				if (change.getSecond() instanceof TableInterface.TableCellRooms) {
					TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getSecond();
					for (int i = 0; i < rooms.getNrRooms(); i++) {
						if (i > 0) ret += CONSTANTS.itemSeparator();
						ret += rooms.getName(i);
					}
					if (rooms.getNrRooms() == 0) ret += MESSAGES.notApplicable();
				} else {
					ret += change.getSecond().getFormattedValue();
				}
			}
			return ret;
		}
		return cell.getFormattedValue();
	}
	
	protected void printTablePDF(TableInterface table, ExportHelper helper) throws IOException {
		sort(table, helper);
		
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), true);
		
		String[] header = new String[table.getHeader().length];
		for (int i = 0; i < table.getHeader().length; i++) {
			header[i] = table.getHeader(i).getName().replace("<br>", "\n");
		}	
		out.printHeader(header);
		out.flush();
		
		for (TableInterface.TableRowInterface row: table.getRows()) {
			PDFPrinter.A[] line = new PDFPrinter.A[row.getNrCells()];
			for (int i = 0; i < row.getNrCells(); i++) {
				line[i] = convertPDF(row.getCell(i));
			}
			out.printLine(line);
		}
		
		out.flush(); out.close();
	}
	
	protected PDFPrinter.A convertPDF(TableInterface.TableCellInterface cell) {
		if (cell == null || cell.getValue() == null) return null;
		if (cell instanceof TableInterface.TableCellMultiLine){
			TableInterface.TableCellMultiLine history = (TableInterface.TableCellMultiLine)cell;
			PDFPrinter.A a = new PDFPrinter.A();
			for (int i = 0; i < history.getNrChunks(); i++) {
				TableInterface.TableCellInterface chunk = history.get(i);
				if (chunk instanceof TableInterface.TableCellChange) {
					TableInterface.TableCellChange change = (TableInterface.TableCellChange)chunk;
					if (change.getFirst() != null && change.getSecond() != null && change.getFirst().compareTo(change.getSecond()) == 0) {
						if (change.getFirst() instanceof TableInterface.TableCellRooms) {
							TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getFirst();
							PDFPrinter.A b = new PDFPrinter.A(); b.set(PDFPrinter.F.INLINE);
							for (int j = 0; j < rooms.getNrRooms(); j++) {
								if (j > 0) b.add(new PDFPrinter.A(CONSTANTS.itemSeparator()));
								PDFPrinter.A c = new PDFPrinter.A(rooms.getName(j));
								c.setColor(rooms.getColor(j));
								b.add(c);
							}
							a.add(b);
						} else {
							PDFPrinter.A b = new PDFPrinter.A(change.getFirst().getFormattedValue());
							if (change.getFirst().hasColor()) b.setColor(change.getFirst().getColor());
							a.add(b);
						}
						continue;
					}
					PDFPrinter.A b = new PDFPrinter.A(); b.set(PDFPrinter.F.INLINE);
					if (change.getFirst() == null) {
						PDFPrinter.A c = new PDFPrinter.A(MESSAGES.notAssigned());
						c.setColor("ff0000");
						c.set(PDFPrinter.F.ITALIC);
						b.add(c);
					} else {
						if (change.getFirst() instanceof TableInterface.TableCellRooms) {
							TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getFirst();
							for (int j = 0; j < rooms.getNrRooms(); j++) {
								if (j > 0) b.add(new PDFPrinter.A(CONSTANTS.itemSeparator()));
								PDFPrinter.A c = new PDFPrinter.A(rooms.getName(j));
								c.setColor(rooms.getColor(j));
								b.add(c);
							}
							if (rooms.getNrRooms() == 0)
								b.add(new PDFPrinter.A(MESSAGES.notApplicable(), PDFPrinter.F.ITALIC));
						} else {
							PDFPrinter.A c = new PDFPrinter.A(change.getFirst().getFormattedValue());
							if (change.getFirst().hasColor()) c.setColor(change.getFirst().getColor());
							b.add(c);
						}
					}
					b.add(new PDFPrinter.A(" \u2192 "));
					if (change.getSecond() == null) {
						PDFPrinter.A c = new PDFPrinter.A(MESSAGES.notAssigned());
						c.setColor("ff0000");
						c.set(PDFPrinter.F.ITALIC);
						b.add(c);
					} else {
						if (change.getSecond() instanceof TableInterface.TableCellRooms) {
							TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getSecond();
							for (int j = 0; j < rooms.getNrRooms(); j++) {
								if (j > 0) b.add(new PDFPrinter.A(CONSTANTS.itemSeparator()));
								PDFPrinter.A c = new PDFPrinter.A(rooms.getName(i));
								c.setColor(rooms.getColor(i));
								b.add(c);
							}
							if (rooms.getNrRooms() == 0)
								b.add(new PDFPrinter.A(MESSAGES.notApplicable(), PDFPrinter.F.ITALIC));
						} else {
							PDFPrinter.A c = new PDFPrinter.A(change.getSecond().getFormattedValue());
							if (change.getSecond().hasColor()) c.setColor(change.getSecond().getColor());
							b.add(c);
						}
					}
					a.add(b);
				} else {
					PDFPrinter.A b = new PDFPrinter.A(chunk.getFormattedValue());
					if (chunk.hasColor()) b.setColor(chunk.getColor());
					if (chunk.isUnderlined()) b.set(F.UNDERLINE);
					a.add(b);
				}
			}
			return a;
		}
		if (cell instanceof TableInterface.TableCellMulti) {
			TableInterface.TableCellMulti multi = (TableInterface.TableCellMulti)cell;
			PDFPrinter.A a = new PDFPrinter.A(); a.set(PDFPrinter.F.INLINE);
			if (cell.hasColor()) a.setColor(cell.getColor());
			for (int i = 0; i < multi.getNrChunks(); i++) {
				PDFPrinter.A b = new PDFPrinter.A(multi.get(i).getFormattedValue());
				if (multi.get(i).hasColor()) b.setColor(multi.get(i).getColor());
				a.add(b);
			}
			return a;
		}
		if (cell instanceof TableInterface.TableCellItems) {
			TableInterface.TableCellItems items = (TableInterface.TableCellItems)cell;
			PDFPrinter.A a = new PDFPrinter.A();
			if (cell.hasColor()) a.setColor(cell.getColor());
			for (int i = 0; i < items.getNrItems(); i++) {
				PDFPrinter.A b = new PDFPrinter.A(items.getFormattedValue(i));
				b.setColor(items.getColor(i));
				a.add(b);
			}
			return a;
		}
		if (cell instanceof TableInterface.TableCellRooms) {
			TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)cell;
			PDFPrinter.A a = new PDFPrinter.A();
			if (cell.hasColor()) a.setColor(cell.getColor());
			for (int i = 0; i < rooms.getNrRooms(); i++) {
				PDFPrinter.A b = new PDFPrinter.A(rooms.getName(i));
				b.setColor(rooms.getColor(i));
				a.add(b);
			}
			return a;
		}
		if (cell instanceof TableInterface.TableCellClassName) {
			TableInterface.TableCellClassName names = (TableInterface.TableCellClassName)cell;
			PDFPrinter.A a = new PDFPrinter.A();
			PDFPrinter.A b = new PDFPrinter.A(cell.getFormattedValue());
			if (cell.hasColor()) b.setColor(cell.getColor());
			a.add(b);
			if (names.hasAlternatives())
				for (String name: names.getAlternatives()) {
					PDFPrinter.A alternative = new PDFPrinter.A("  " + name);
					alternative.setColor("777777");
					a.add(alternative);
				}
			return a;
		}
		if (cell instanceof TableInterface.TableCellChange) {
			TableInterface.TableCellChange change = (TableInterface.TableCellChange)cell;
			if (change.getFirst() != null && change.getSecond() != null && change.getFirst().compareTo(change.getSecond()) == 0) {
				if (change.getFirst() instanceof TableInterface.TableCellRooms) {
					TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getFirst();
					PDFPrinter.A a = new PDFPrinter.A(); a.set(PDFPrinter.F.INLINE);
					for (int i = 0; i < rooms.getNrRooms(); i++) {
						if (i > 0) a.add(new PDFPrinter.A(CONSTANTS.itemSeparator()));
						PDFPrinter.A b = new PDFPrinter.A(rooms.getName(i));
						b.setColor(rooms.getColor(i));
						a.add(b);
					}
					return a;
				} else {
					return convertPDF(change.getFirst());
				}
			}
			PDFPrinter.A a = new PDFPrinter.A(); a.set(PDFPrinter.F.INLINE);
			if (change.getFirst() == null) {
				PDFPrinter.A b = new PDFPrinter.A(MESSAGES.notAssigned());
				b.setColor("ff0000");
				b.set(PDFPrinter.F.ITALIC);
				a.add(b);
			} else {
				if (change.getFirst() instanceof TableInterface.TableCellRooms) {
					TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getFirst();
					for (int i = 0; i < rooms.getNrRooms(); i++) {
						if (i > 0) a.add(new PDFPrinter.A(CONSTANTS.itemSeparator()));
						PDFPrinter.A b = new PDFPrinter.A(rooms.getName(i));
						b.setColor(rooms.getColor(i));
						a.add(b);
					}
					if (rooms.getNrRooms() == 0)
						a.add(new PDFPrinter.A(MESSAGES.notApplicable(), PDFPrinter.F.ITALIC));
				} else {
					a.add(convertPDF(change.getFirst()));
				}
			}
			a.add(new PDFPrinter.A(" \u2192 "));
			if (change.getSecond() == null) {
				PDFPrinter.A c = new PDFPrinter.A(MESSAGES.notAssigned());
				c.setColor("ff0000");
				c.set(PDFPrinter.F.ITALIC);
				a.add(c);
			} else {
				if (change.getSecond() instanceof TableInterface.TableCellRooms) {
					TableInterface.TableCellRooms rooms = (TableInterface.TableCellRooms)change.getSecond();
					for (int i = 0; i < rooms.getNrRooms(); i++) {
						if (i > 0) a.add(new PDFPrinter.A(CONSTANTS.itemSeparator()));
						PDFPrinter.A b = new PDFPrinter.A(rooms.getName(i));
						b.setColor(rooms.getColor(i));
						a.add(b);
					}
					if (rooms.getNrRooms() == 0)
						a.add(new PDFPrinter.A(MESSAGES.notApplicable(), PDFPrinter.F.ITALIC));
				} else {
					a.add(convertPDF(change.getSecond()));
				}
			}
			return a;
		}
		PDFPrinter.A a = new PDFPrinter.A(cell.getFormattedValue());
		if (cell.hasColor())
			a.setColor(cell.getColor());
		return a;
	}
}
