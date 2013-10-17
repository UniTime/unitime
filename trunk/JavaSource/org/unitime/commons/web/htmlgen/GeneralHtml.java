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

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Stephanie Schluttenhofer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class GeneralHtml {

	/**
	 * 
	 */
	public GeneralHtml() {
		super();
		this.setContents(new ArrayList());
	}
	
	private String styleClass;
	private String style;
	private String title;
	private String id;
	private String dir;
	private String lang;

	// subclasses should override and set tag equal to the tag used in their html
	private String tag;

	private ArrayList contents;
	/**
	 * @return Returns the contents.
	 */
	public ArrayList getContents() {
		return contents;
	}
	/**
	 * @param contents The contents to set.
	 */
	public void setContents(ArrayList contents) {
		this.contents = contents;
	}
	
	public void addContent(Object obj){
		this.getContents().add(obj);
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return Returns the style.
	 */
	public String getStyle() {
		return style;
	}
	/**
	 * @param style The style to set.
	 */
	public void setStyle(String style) {
		this.style = style;
	}
	/**
	 * @return Returns the styleClass.
	 */
	public String getStyleClass() {
		return styleClass;
	}
	/**
	 * @param styleClass The styleClass to set.
	 */
	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return Returns the tag.
	 */
	public String getTag() {
		return tag;
	}
	/**
	 * @param tag The tag to set.
	 */
	protected void setTag(String tag) {
		this.tag = tag;
	}
	
	public String startTagHtml(){
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(this.getTag());
        sb.append(this.htmlForAttributes());		
		sb.append(">\n");
		return(sb.toString());
	}
	
	public String endTagHtml(){
		StringBuffer sb = new StringBuffer();
		sb.append("</");
		sb.append(this.getTag());
		sb.append(">\n");
		return (sb.toString());
	}
	
	public String toHtml(){
		StringBuffer sb = new StringBuffer();
		sb.append(startTagHtml());
		sb.append(this.contentHtml());
		sb.append(this.endTagHtml());
		return (sb.toString());
	}
	/**
	 * @return html for any non null attributes and not empty string attributes
	 */
	protected String htmlForAttributes() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.htmlForAttribute("class", this.getStyleClass()));
		sb.append(this.htmlForAttribute("style", this.getStyle()));
		sb.append(this.htmlForAttribute("id", this.getId()));
		sb.append(this.htmlForAttribute("title", this.getTitle()));
		sb.append(this.htmlForAttribute("dir", this.getDir()));
		sb.append(this.htmlForAttribute("lang", this.getLang()));

		return (sb.toString());
	}
	
	protected String htmlForObject(Object obj) {
		StringBuffer sb = new StringBuffer();
		if (obj != null){
			if (GeneralHtml.class.isInstance(obj)) {
				sb.append(((GeneralHtml) obj).toHtml());				
			} else if (AbstractCollection.class.isInstance(obj)) {
				Iterator colIt = ((AbstractCollection) obj).iterator();
				while(colIt.hasNext()) {
					sb.append(this.htmlForObject(colIt.next()));
				}			
			} else {
				sb.append(obj.toString());
			}
		}
		return(sb.toString());
	}
	
	/**
	 * @return html for any contents of this html container
	 */
	protected String contentHtml() {
		StringBuffer sb = new StringBuffer();
		Iterator it = this.getContents().iterator();
		while (it.hasNext()){
			sb.append(htmlForObject(it.next()));
		}
		return (sb.toString());
	}
	
	protected String htmlForAttribute(String attributeName, String attributeValue){
        StringBuffer sb = new StringBuffer();
        if (attributeName != null && attributeName.length() != 0 
        		&& attributeValue != null && attributeValue.length() != 0){
       		sb.append(" " + attributeName + "=" + "\"" + attributeValue + "\"");
        }
        return (sb.toString());
	}
	protected String htmlForAttribute(String attributeName, boolean attributeValue){
        StringBuffer sb = new StringBuffer();
        if (attributeName != null && attributeName.length() != 0 
        		&& attributeValue){
       		sb.append(" " + attributeName);
        }
        return (sb.toString());
	}
	protected String htmlForAttribute(String attributeName, int attributeValue){
        StringBuffer sb = new StringBuffer();
        if (attributeName != null && attributeName.length() != 0){
       		sb.append(" " + attributeName + "=" + "\"" + Integer.toString(attributeValue) + "\"");
        }
        return (sb.toString());
	}
	/**
	 * @return Returns the dir.
	 */
	public String getDir() {
		return dir;
	}
	/**
	 * @param dir The dir to set.
	 */
	public void setDir(String dir) {
		this.dir = dir;
	}
	/**
	 * @return Returns the lang.
	 */
	public String getLang() {
		return lang;
	}
	/**
	 * @param lang The lang to set.
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}
}
