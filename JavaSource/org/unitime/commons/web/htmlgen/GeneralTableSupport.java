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
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class GeneralTableSupport extends GeneralHtmlWithJavascript {

	/**
	 * 
	 */
	public GeneralTableSupport() {
		super();
	}

	private String align;
	private String background;
	private String bgColor;
	private String borderColor;
	private String height;
	
	/**
	 * @return Returns the align.
	 */
	public String getAlign() {
		return align;
	}
	/**
	 * @param align The align to set.
	 */
	public void setAlign(String align) {
		this.align = align;
	}
	/**
	 * @return Returns the background.
	 */
	public String getBackground() {
		return background;
	}
	/**
	 * @param background The background to set.
	 */
	public void setBackground(String background) {
		this.background = background;
	}
	/**
	 * @return Returns the bgColor.
	 */
	public String getBgColor() {
		return bgColor;
	}
	/**
	 * @param bgColor The bgColor to set.
	 */
	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}
	/**
	 * @return Returns the borderColor.
	 */
	public String getBorderColor() {
		return borderColor;
	}
	/**
	 * @param borderColor The borderColor to set.
	 */
	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}
	/**
	 * @return Returns the height.
	 */
	public String getHeight() {
		return height;
	}
	/**
	 * @param height The height to set.
	 */
	public void setHeight(String height) {
		this.height = height;
	}
	
	protected String htmlForAttributes() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.htmlForAttributes());
		sb.append(this.htmlForAttribute("align", this.getAlign()));
		sb.append(this.htmlForAttribute("background", this.getBackground()));
		sb.append(this.htmlForAttribute("bgcolor", this.getBgColor()));
		sb.append(this.htmlForAttribute("bordercolor", this.getBorderColor()));
		sb.append(this.htmlForAttribute("height", this.getHeight()));
		return (sb.toString());
	}
}
