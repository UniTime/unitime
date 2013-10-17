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
public abstract class GeneralTableRowSupport extends GeneralTableSupport {

	/**
	 * 
	 */
	public GeneralTableRowSupport() {
		super();
	}
	
	private String charr;
	private String charOff;
	private String valign;
	

	/**
	 * @return Returns the charOff.
	 */
	public String getCharOff() {
		return charOff;
	}
	/**
	 * @param charOff The charOff to set.
	 */
	public void setCharOff(String charOff) {
		this.charOff = charOff;
	}
	/**
	 * @return Returns the charr.
	 */
	public String getCharr() {
		return charr;
	}
	/**
	 * @param charr The charr to set.
	 */
	public void setCharr(String charr) {
		this.charr = charr;
	}
	/**
	 * @return Returns the valign.
	 */
	public String getValign() {
		return valign;
	}
	/**
	 * @param valign The valign to set.
	 */
	public void setValign(String valign) {
		this.valign = valign;
	}
	
	protected String htmlForAttributes() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.htmlForAttributes());
		sb.append(this.htmlForAttribute("char", this.getCharr()));
		sb.append(this.htmlForAttribute("charoff", this.getCharOff()));
		sb.append(this.htmlForAttribute("valign", this.getValign()));
		return (sb.toString());
	}
}
