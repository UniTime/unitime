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
    
    private int start = 0;
    private boolean next = false;
    private boolean export = false;

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
        start = 0;
        next = false;
        export = false;
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
    
    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }
    
    public boolean isNext() { return next; }
    public void setNext(boolean next) { this.next = next; }
    
    public boolean isExport() { return export; }
    public void setExport(boolean export) { this.export = export; }
}
