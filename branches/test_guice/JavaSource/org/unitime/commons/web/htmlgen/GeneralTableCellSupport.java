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

/**
 * @author Stephanie Schluttenhofer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class GeneralTableCellSupport extends GeneralTableRowSupport {
	
	private String abbr;
	private String axis;
	private int colSpan;
	private String headers;
	private int rowSpan;
	private String scope;
	private String width;
	private boolean noWrap;
	

	
	/**
	 * 
	 */
	public GeneralTableCellSupport() {
		super();
		this.setNoWrap(false);
	}


	/**
	 * @return Returns the abbr.
	 */
	public String getAbbr() {
		return abbr;
	}
	/**
	 * @param abbr The abbr to set.
	 */
	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}
	/**
	 * @return Returns the axis.
	 */
	public String getAxis() {
		return axis;
	}
	/**
	 * @param axis The axis to set.
	 */
	public void setAxis(String axis) {
		this.axis = axis;
	}
	/**
	 * @return Returns the headers.
	 */
	public String getHeaders() {
		return headers;
	}
	/**
	 * @param headers The headers to set.
	 */
	public void setHeaders(String headers) {
		this.headers = headers;
	}
	/**
	 * @return Returns the noWrap.
	 */
	public boolean isNoWrap() {
		return noWrap;
	}
	/**
	 * @param noWrap The noWrap to set.
	 */
	public void setNoWrap(boolean noWrap) {
		this.noWrap = noWrap;
	}
	/**
	 * @return Returns the scope.
	 */
	public String getScope() {
		return scope;
	}
	/**
	 * @param scope The scope to set.
	 */
	public void setScope(String scope) {
		this.scope = scope;
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
	
	protected String htmlForAttributes() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.htmlForAttributes());
		sb.append(this.htmlForAttribute("abbr", this.getAbbr()));
		sb.append(this.htmlForAttribute("axis", this.getAxis()));
		if (this.getColSpan() > 1){
			sb.append(this.htmlForAttribute("colspan", this.getColSpan()));
		}
		sb.append(this.htmlForAttribute("headers", this.getHeaders()));
		if (this.getRowSpan() > 1){
			sb.append(this.htmlForAttribute("rowspan", this.getRowSpan()));
		}
		sb.append(this.htmlForAttribute("scope", this.getScope()));
		sb.append(this.htmlForAttribute("width", this.getWidth()));
		sb.append(this.htmlForAttribute("nowrap", this.isNoWrap()));
		return (sb.toString());
	}

	/**
	 * @return Returns the colSpan.
	 */
	public int getColSpan() {
		return colSpan;
	}
	/**
	 * @param colSpan The colSpan to set.
	 */
	public void setColSpan(int colSpan) {
		this.colSpan = colSpan;
	}
	/**
	 * @return Returns the rowSpan.
	 */
	public int getRowSpan() {
		return rowSpan;
	}
	/**
	 * @param rowSpan The rowSpan to set.
	 */
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}
}
