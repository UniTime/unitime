/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
public abstract class GeneralHtmlWithJavascript extends GeneralHtml {

	/**
	 * 
	 */
	public GeneralHtmlWithJavascript() {
		super();
	}

	private String onClick;
	private String onDblClick;
	private String onHelp;
	private String onKeyDown;
	private String onKeyPress;
	private String onKeyUp;
	private String onMouseDown;
	private String onMouseMove;
	private String onMouseOut;
	private String onMouseOver;
	private String onMouseUp;
	

	/**
	 * @return Returns the onClick.
	 */
	public String getOnClick() {
		return onClick;
	}
	/**
	 * @param onClick The onClick to set.
	 */
	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}
	/**
	 * @return Returns the onDblClick.
	 */
	public String getOnDblClick() {
		return onDblClick;
	}
	/**
	 * @param onDblClick The onDblClick to set.
	 */
	public void setOnDblClick(String onDblClick) {
		this.onDblClick = onDblClick;
	}
	/**
	 * @return Returns the onHelp.
	 */
	public String getOnHelp() {
		return onHelp;
	}
	/**
	 * @param onHelp The onHelp to set.
	 */
	public void setOnHelp(String onHelp) {
		this.onHelp = onHelp;
	}
	/**
	 * @return Returns the onKeyDown.
	 */
	public String getOnKeyDown() {
		return onKeyDown;
	}
	/**
	 * @param onKeyDown The onKeyDown to set.
	 */
	public void setOnKeyDown(String onKeyDown) {
		this.onKeyDown = onKeyDown;
	}
	/**
	 * @return Returns the onKeyPress.
	 */
	public String getOnKeyPress() {
		return onKeyPress;
	}
	/**
	 * @param onKeyPress The onKeyPress to set.
	 */
	public void setOnKeyPress(String onKeyPress) {
		this.onKeyPress = onKeyPress;
	}
	/**
	 * @return Returns the onKeyUp.
	 */
	public String getOnKeyUp() {
		return onKeyUp;
	}
	/**
	 * @param onKeyUp The onKeyUp to set.
	 */
	public void setOnKeyUp(String onKeyUp) {
		this.onKeyUp = onKeyUp;
	}
	/**
	 * @return Returns the onMouseDown.
	 */
	public String getOnMouseDown() {
		return onMouseDown;
	}
	/**
	 * @param onMouseDown The onMouseDown to set.
	 */
	public void setOnMouseDown(String onMouseDown) {
		this.onMouseDown = onMouseDown;
	}
	/**
	 * @return Returns the onMouseMove.
	 */
	public String getOnMouseMove() {
		return onMouseMove;
	}
	/**
	 * @param onMouseMove The onMouseMove to set.
	 */
	public void setOnMouseMove(String onMouseMove) {
		this.onMouseMove = onMouseMove;
	}
	/**
	 * @return Returns the onMouseOut.
	 */
	public String getOnMouseOut() {
		return onMouseOut;
	}
	/**
	 * @param onMouseOut The onMouseOut to set.
	 */
	public void setOnMouseOut(String onMouseOut) {
		this.onMouseOut = onMouseOut;
	}
	/**
	 * @return Returns the onMouseOver.
	 */
	public String getOnMouseOver() {
		return onMouseOver;
	}
	/**
	 * @param onMouseOver The onMouseOver to set.
	 */
	public void setOnMouseOver(String onMouseOver) {
		this.onMouseOver = onMouseOver;
	}
	/**
	 * @return Returns the onMouseUp.
	 */
	public String getOnMouseUp() {
		return onMouseUp;
	}
	/**
	 * @param onMouseUp The onMouseUp to set.
	 */
	public void setOnMouseUp(String onMouseUp) {
		this.onMouseUp = onMouseUp;
	}
	
	protected String htmlForAttributes() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.htmlForAttributes());
		sb.append(this.htmlForAttribute("onclick", this.getOnClick()));
		sb.append(this.htmlForAttribute("ondblclick", this.getOnDblClick()));
		sb.append(this.htmlForAttribute("onhelp", this.getOnHelp()));
		sb.append(this.htmlForAttribute("onkeydown", this.getOnKeyDown()));
		sb.append(this.htmlForAttribute("onkeypress", this.getOnKeyPress()));
		sb.append(this.htmlForAttribute("onkeyup", this.getOnKeyUp()));
		sb.append(this.htmlForAttribute("onmousedown", this.getOnMouseDown()));
		sb.append(this.htmlForAttribute("onmousemove", this.getOnMouseMove()));
		sb.append(this.htmlForAttribute("onmouseout", this.getOnMouseOut()));
		sb.append(this.htmlForAttribute("onmouseover", this.getOnMouseOver()));
		sb.append(this.htmlForAttribute("onmouseup", this.getOnMouseUp()));
		return (sb.toString());
	}
}
