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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/** 
 * MyEclipse Struts
 * Creation date: 12-16-2005
 * 
 * XDoclet definition:
 * @struts:form name="hibernateQueryTestForm"
 *
 * @author Tomas Muller
 */
public class HibernateQueryTestForm extends ActionForm {

	private static final long serialVersionUID = 5970479864977610427L;

    // --------------------------------------------------------- Instance Variables

	/** query property */
    private String query;

    /** listSize property */
    private String listSize;

    // --------------------------------------------------------- Methods

    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();
        if(query==null || query.trim().length()==0)
            errors.add("query", new ActionMessage("errors.generic", "Invalid value for query" ));
        
        return errors;
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        query = "";
        listSize = "";
    }

    /** 
     * Returns the query.
     * @return String
     */
    public String getQuery() {
        return query;
    }

    /** 
     * Set the query.
     * @param query The query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /** 
     * Returns the listSize.
     * @return String
     */
    public String getListSize() {
        return listSize;
    }

    /** 
     * Set the listSize.
     * @param listSize The listSize to set
     */
    public void setListSize(String listSize) {
        this.listSize = listSize;
    }

}
