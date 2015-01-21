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
