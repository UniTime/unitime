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
package org.unitime.timetable.server.admin;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.UserDataInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.LoadDataRpcRequest;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.gwt.shared.UserDataInterface.GetUserDataRpcRequest;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:admin-report.csv")
public class AdminExportToCSV implements Exporter {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired ApplicationContext applicationContext;

	@Override
	public String reference() {
		return "admin-report.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		String type = helper.getParameter("type");
		if (type == null)
			throw new IllegalArgumentException("Admin page type not provided, please set the type parameter.");
		
		String[] filter = helper.getParameterValues("filter");
		
		LoadDataRpcRequest request = new LoadDataRpcRequest();
		request.setFilter(filter);
		request.setType(type);
		
		final SimpleEditInterface data = GwtRpcServlet.execute(request, applicationContext, helper.getSessionContext());
		
		if (data == null)
			throw new IllegalArgumentException("No data returned.");
		
		final Comparator<Record> cmp = data.getComparator();
		boolean hasDetails = hasDetails(data);
		String hidden = null;
		
		if (data.isSaveOrder()) {
			GetUserDataRpcRequest ordRequest = new GetUserDataRpcRequest();
			ordRequest.add("SimpleEdit.Order[" + type + "]");
			if (hasDetails)
				ordRequest.add("SimpleEdit.Open[" + type + "]");
			ordRequest.add("SimpleEdit.Hidden[" + type + "]");
			UserDataInterface result = GwtRpcServlet.execute(ordRequest, applicationContext, helper.getSessionContext());
			if (result != null) {
				if (helper.getParameter("order") != null)
					result.put("SimpleEdit.Order[" + type + "]", helper.getParameter("order"));
				if (helper.getParameter("open") != null)
					result.put("SimpleEdit.Open[" + type + "]", helper.getParameter("open"));
				if (helper.getParameter("hidden") != null)
					result.put("SimpleEdit.Hidden[" + type + "]", helper.getParameter("hidden"));
				final String order = "|" + result.get("SimpleEdit.Order[" + type + "]") + "|";
				if (hasDetails) {
					String open = "|" + result.get("SimpleEdit.Open[" + type + "]") + "|";
					for (Record r: data.getRecords()) {
						if (isParent(data, r))
								r.setField(0, open.indexOf("|" + r.getUniqueId() + "|") >= 0 ? "-" : "+");
						}
					}
				Collections.sort(data.getRecords(), new Comparator<Record>() {
					public int compare(Record r1, Record r2) {
						if (data.getFields()[0].getType() == FieldType.parent) {
							Record p1 = ("+".equals(r1.getField(0)) || "-".equals(r1.getField(0)) ? null : data.getRecord(Long.valueOf(r1.getField(0))));
							Record p2 = ("+".equals(r2.getField(0)) || "-".equals(r2.getField(0)) ? null : data.getRecord(Long.valueOf(r2.getField(0))));
							if ((p1 == null ? r1 : p1).equals(p2 == null ? r2 : p2)) { // same parents
								if (p1 != null && p2 == null) return 1; // r1 is already a parent
								if (p1 == null && p2 != null) return -1; // r2 is already a parent
								// same level
							} else if (p1 != null || p2 != null) { // different parents
								return compare(p1 == null ? r1 : p1, p2 == null ? r2 : p2); // compare parents
							}
						}
						int i1 = (r1.getUniqueId() == null ? -1 : order.indexOf("|" + r1.getUniqueId() + "|"));
						if (i1 >= 0) {
							int i2 = (r2.getUniqueId() == null ? -1 : order.indexOf("|" + r2.getUniqueId() + "|"));
							if (i2 >= 0) {
								return (i1 < i2 ? -1 : i1 > i2 ? 1 : cmp.compare(r1, r2));
							}
						}
						return cmp.compare(r1, r2);
					}
				});
				hidden = "|" + result.get("SimpleEdit.Hidden[" + type + "]") + "|";
			} else {
				Collections.sort(data.getRecords(), cmp);
			}
		} else {
			Collections.sort(data.getRecords(), cmp);
		}
		
		export(data, helper, hidden);
	}
	
	protected void export(SimpleEditInterface data, ExportHelper helper, String hidden) throws IOException {
		CSVPrinter out = new CSVPrinter(helper, false);
		try {
			helper.setup(out.getContentType(), helper.getParameter("type") + ".csv", false);
			
			boolean hasDetails = hasDetails(data);
			
			for (int i = 0; i < data.getFields().length; i++) {
				boolean visible = data.getFields()[i].isVisible() && (hidden == null || !hidden.contains("|" + data.getFields()[i].getName() + "|"));
				if (data.getFields()[i].isNoList()) visible = false;
				if (!visible)
					out.hideColumn(i);
			}
			
			String[] header = new String[data.getFields().length];
			for (int i = 0; i < data.getFields().length; i++)
				header[i] = header(data, data.getFields()[i]);
			out.printHeader(header);
			
			boolean visible = true;
			for (Record r: data.getRecords()) {
				if (hasDetails) {
					if ("-".equals(r.getField(0))) visible = true;
					else if ("+".equals(r.getField(0))) visible = false;
					else if (!visible) continue;
				}
				
				String[] line = new String[data.getFields().length];
				for (int i = 0; i < data.getFields().length; i++)
					line[i] = cell(data.getFields()[i], r, i);
				out.printLine(line);
			}
		} finally {
			out.close();
		}
	}
	
	protected String header(SimpleEditInterface data, Field field) {
		String name = field.getName();
		if (hasDetails(data) && name.contains("|"))
			name = name.replace("|", "\n  ");
		if ("&otimes;".equals(name)) name = "\u2297";
		return name;
	}
	
	public static String slot2time(int slot) {
		if (CONSTANTS.useAmPm()) {
			if (slot == 0) return CONSTANTS.timeMidnight();
			if (slot == 144) return CONSTANTS.timeNoon();
			if (slot == 288) return CONSTANTS.timeMidnightEnd();
		}
		int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONSTANTS.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + " " + (h == 24 ? CONSTANTS.timeAm() : h >= 12 ? CONSTANTS.timePm() : CONSTANTS.timeAm());
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}
	
	protected String getValue(Field field, Record record, int index) {
		String value = record.getField(index);
		if (value == null) return "";
		if (field.getType() == FieldType.list) {
			for (ListItem item: field.getValues()) {
				if (item.getValue().equals(value)) return item.getText();
			}
		} else if (field.getType() == FieldType.multi) {
			String text = "";
			for (String val: record.getValues(index)) {
				for (ListItem item: field.getValues()) {
					if (item.getValue().equals(val)) {
						if (!text.isEmpty()) text += ", ";
						text += item.getText();
					}
				}
			}
			return text;
		} else if (field.getType() == FieldType.time) {
			if (value == null || value.isEmpty()) return "";
			return slot2time(Integer.valueOf(value));
		}
		return value;
	}
	
	protected boolean hasDetails(SimpleEditInterface data) {
		return (data != null && data.getFields().length > 0 && data.getFields()[0].getType() == FieldType.parent);
	}
	
	protected boolean isParent(SimpleEditInterface data, Record r) {
		return hasDetails(data) && ("+".equals(r.getField(0)) || "-".equals(r.getField(0)));
	}
	
	protected String cell(Field field, Record record, int index) {
		String value = getValue(field, record, index);
		switch (field.getType()) {
		case parent:
			if (!"+".equals(value) && !"-".equals(value)) return "";
			return value;
		case toggle:
			if (!value.isEmpty() && !"true".equals(value) && !"false".equals(value))
				return record.getField(index);
			if ((value.isEmpty() && field.isCheckedByDefault()) || (!value.isEmpty() && "true".equalsIgnoreCase(record.getField(index))))
				return MESSAGES.exportTrue();
			else
				return MESSAGES.exportFalse();
		case person:
			String[] name = record.getValues(index);
			return (name.length <= 2 ? "<i>" + MESSAGES.notSet() + "</i>" : name.length >= 6 && !name[6].isEmpty() ? name[6] : name[0] + ", " + name[1] + (name[2].isEmpty() ? "" : " " + name[2]));
		default:
			return value;
		}
	}

}
