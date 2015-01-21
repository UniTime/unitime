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
