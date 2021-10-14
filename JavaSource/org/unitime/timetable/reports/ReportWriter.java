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
package org.unitime.timetable.reports;

import java.io.IOException;
import java.io.OutputStream;

import org.unitime.timetable.reports.AbstractReport.Line;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public interface ReportWriter {
	public void setFooter(String footer);
	
	public void setHeader(Line... line);
	
	public Line[] getHeader();
		
	public void printLine(Line line) throws DocumentException;
	
	public void close() throws IOException, DocumentException;
	
	public void open(OutputStream out) throws DocumentException, IOException;
	
	public void setPageName(String pageName);
	
	public void setCont(String cont);
	
	public void printHeader(boolean newPage) throws DocumentException;
	
	public void newPage() throws DocumentException;
	
	public void lastPage() throws DocumentException;
	
	public int getLineNumber();
	
	public int getNrLinesPerPage();
	
	public int getNrCharsPerLine();
	
	public void printSeparator(Line line) throws DocumentException;
	
	public int getSeparatorNrLines();
	
	public void setListener(Listener listener);
	
	public boolean isSkipRepeating();
	
	public interface Listener {
		public void headerPrinted();
	}
}
