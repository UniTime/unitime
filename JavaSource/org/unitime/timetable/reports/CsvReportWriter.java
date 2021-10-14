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
import java.io.PrintWriter;

import org.unitime.timetable.reports.AbstractReport.Cell;
import org.unitime.timetable.reports.AbstractReport.Line;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
public class CsvReportWriter implements ReportWriter {
	private PrintWriter iPrint = null;
	private Line[] iHeaderLine = null;
	
	public CsvReportWriter(OutputStream out, String title, String title2, String subject, String session) throws DocumentException, IOException {
		if (out != null) open(out);
	}

	@Override
	public void setFooter(String footer) {
	}

	@Override
	public void setHeader(Line... line) { iHeaderLine = line; }

	@Override
	public Line[] getHeader() { return iHeaderLine; }

	protected void println(String text) throws DocumentException {
		if (text == null)
			iPrint.println();
		else
			iPrint.println(text);
	}

	@Override
	public void close() throws IOException, DocumentException {
		iPrint.close();
	}

	@Override
	public void open(OutputStream out) throws DocumentException, IOException {
		iPrint = new PrintWriter(out);
	}

	@Override
	public void setPageName(String pageName) {}

	@Override
	public void setCont(String cont) {}

	@Override
	public void printHeader(boolean newPage) throws DocumentException {
        if (iHeaderLine != null)
        	for (int i=0;i<iHeaderLine.length;i++)
        		if (!iHeaderLine[i].isEmpty())
        			printLine(iHeaderLine[i]);
	}

	@Override
	public void newPage() throws DocumentException {}

	@Override
	public void lastPage() throws DocumentException {}

	@Override
	public int getLineNumber() { return 0; }

	@Override
	public int getNrLinesPerPage() { return 0; }

	@Override
	public int getNrCharsPerLine() { return 1000; }
	
    private String render(Cell cell) {
    	StringBuffer ret = new StringBuffer();
    	if (cell.getText() != null) {
    		if (cell.getPadding() != ' ' && cell.getText().length() < cell.getLength())
    			ret.append(cell.render());
    		else
    			ret.append(cell.getText());
    	}
    	if (cell.getCells() != null) {
    		if (cell.getText() != null) {
    			if (cell.getCellSeparator().isEmpty()) 
    				ret.append(" ");
    			else
    				ret.append(cell.getCellSeparator());
    		}
    		for (int i = 0; i < cell.getCells().length; i++) {
    			if (i > 0) {
    				if (cell.getCells()[i-1].getCellSeparator().isEmpty())
    					ret.append(" ");
    				else
    					ret.append(cell.getCells()[i-1].getCellSeparator());
    			}
    			ret.append(render(cell.getCells()[i]));
    		}
    	}
    	return ret.toString();
    }
    
    private String quote(String cell) {
    	if (cell == null) return "";
    	if (cell.indexOf('"') < 0 && cell.indexOf('\n') < 0 && cell.indexOf(',') < 0) return cell;
    	return "\"" + cell.replaceAll("\"", "\"\"") + "\"";
    }
    
    private String render(Line line) {
    	if (line == null) return "";
    	if (line.getLines() != null) {
    		StringBuffer ret = new StringBuffer();
    		for (int i = 0; i < line.getLines().length; i++) {
    			if (i > 0) ret.append(",,");
    			ret.append(render(line.getLines()[i]));
    		}
    		return ret.toString();
    	}
    	if (line.getCells() != null) {
    		StringBuffer ret = new StringBuffer();
    		for (int i = 0; i < line.getCells().length; i++) {
    			if (i > 0) {
    				if (line.getCells()[i - 1].getColSpan() == 0)
    					ret.append(" ");
    				else
    					ret.append(",");
    			}
    			ret.append(quote(render(line.getCells()[i])));
    			for (int j = 1; j < line.getCells()[i].getColSpan(); j++)
    				ret.append(",");
    		}
    		return ret.toString();
    	}
    	return "";
    }

	@Override
	public void printLine(Line line) throws DocumentException {
		println(render(line));
	}

	@Override
	public void printSeparator(Line line) throws DocumentException {}

	@Override
	public int getSeparatorNrLines() {
		return 0;
	}

	@Override
	public void setListener(Listener listener) {}
	
	@Override
	public boolean isSkipRepeating() { return false; }
}
