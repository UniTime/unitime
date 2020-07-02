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
package org.unitime.timetable.export;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.export.Exporter.Printer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Tomas Muller
 */
public class JSONPrinter implements Printer {
	private PrintWriter iOut;
	private Set<Integer> iHiddenColumns = new HashSet<Integer>();
	private String[] iHeader = null;
	private List<Map<String,Object>> iList = new ArrayList<Map<String,Object>>();
	
	public JSONPrinter(PrintWriter writer) {
		iOut = writer;
	}
	
	@Override
	public String getContentType() {
		return "application/json";
	}
	
	@Override
	public void hideColumn(int col) {
		iHiddenColumns.add(col);
	}
	
	@Override
	public void printHeader(String... fields) {
		iHeader = fields;
	}
	
	@Override
	public void printLine(String... fields) {
		Map<String, Object> entry = new HashMap<String, Object>();
		for (int idx = 0; idx < fields.length; idx++) {
			if (iHiddenColumns.contains(idx)) continue;
			String f = fields[idx];
			String h = iHeader[idx];
			if (f == null) continue;
			try {
				entry.put(h, Integer.parseInt(f));
				continue;
			} catch (NumberFormatException e) {}
			try {
				entry.put(h, Double.parseDouble(f));
				continue;
			} catch (NumberFormatException e) {}
			if ("true".equals(f)) entry.put(h, Boolean.TRUE);
			else if ("false".equals(f)) entry.put(h, Boolean.FALSE);
			else entry.put(h, f);
		}
		iList.add(entry);
	}
	
	protected Gson createGson() {
		return new GsonBuilder().setPrettyPrinting().create();
	}
	
	@Override
	public void flush() {
		iOut.print(createGson().toJson(iList));
		iOut.flush();
	}
	
	@Override
	public void close() {
	}
}
