/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.commons.web.htmlgen;

import java.util.ArrayList;

/**
 * @author Stephanie Schluttenhofer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Table extends GeneralTableSupport {
	
	private int border;
	private int cellPadding;
	private int cellSpacing;
	private String dataPageSize;
	private String frame;
	private String rules;
	private String summary;
	private String width;
	
	private ArrayList rows;

	/**
	 * @return Returns the border.
	 */
	public int getBorder() {
		return border;
	}
	/**
	 * @param border The border to set.
	 */
	public void setBorder(int border) {
		this.border = border;
	}
	/**
	 * @return Returns the cellPadding.
	 */
	public int getCellPadding() {
		return cellPadding;
	}
	/**
	 * @param cellPadding The cellPadding to set.
	 */
	public void setCellPadding(int cellPadding) {
		this.cellPadding = cellPadding;
	}
	/**
	 * @return Returns the cellSpacing.
	 */
	public int getCellSpacing() {
		return cellSpacing;
	}
	/**
	 * @param cellSpacing The cellSpacing to set.
	 */
	public void setCellSpacing(int cellSpacing) {
		this.cellSpacing = cellSpacing;
	}
	/**
	 * @return Returns the rows.
	 */
	public ArrayList getRows() {
		return rows;
	}
	/**
	 * @param rows The rows to set.
	 */
	public void setRows(ArrayList rows) {
		this.rows = rows;
	}
	/**
	 * @return Returns the dataPageSize.
	 */
	public String getDataPageSize() {
		return dataPageSize;
	}
	/**
	 * @param dataPageSize The dataPageSize to set.
	 */
	public void setDataPageSize(String dataPageSize) {
		this.dataPageSize = dataPageSize;
	}
	/**
	 * @return Returns the frame.
	 */
	public String getFrame() {
		return frame;
	}
	/**
	 * @param frame The frame to set.
	 */
	public void setFrame(String frame) {
		this.frame = frame;
	}
	/**
	 * @return Returns the rules.
	 */
	public String getRules() {
		return rules;
	}
	/**
	 * @param rules The rules to set.
	 */
	public void setRules(String rules) {
		this.rules = rules;
	}
	/**
	 * @return Returns the summary.
	 */
	public String getSummary() {
		return summary;
	}
	/**
	 * @param summary The summary to set.
	 */
	public void setSummary(String summary) {
		this.summary = summary;
	}
	/**
	 * @return Returns the width.
	 */
	public String getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(String width) {
		this.width = width;
	}
	/**
	 * 
	 */
	public Table() {
		super();
		this.setTag("table");
	}
		
	protected String htmlForAttributes() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.htmlForAttributes());
		sb.append(this.htmlForAttribute("border", this.getBorder()));
		sb.append(this.htmlForAttribute("cellpadding", this.getCellPadding()));
		sb.append(this.htmlForAttribute("cellspacing", this.getCellSpacing()));
		sb.append(this.htmlForAttribute("datapagesize", this.getDataPageSize()));
		sb.append(this.htmlForAttribute("frame", this.getFrame()));
		sb.append(this.htmlForAttribute("rules", this.getRules()));
		sb.append(this.htmlForAttribute("summary", this.getSummary()));
		sb.append(this.htmlForAttribute("width", this.getWidth()));
		return (sb.toString());
	}
}
