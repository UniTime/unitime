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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;

/** 
 * @author Tomas Muller
 */
public class HibernateQueryTestForm implements UniTimeForm {
	private static final long serialVersionUID = 5970479864977610427L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	/** query property */
    private String query;

    /** listSize property */
    private String listSize;
    
    private int start = 0;
    private boolean next = false;
    private boolean export = false;
    
    public HibernateQueryTestForm() {
    	reset();
    }

    @Override
    public void validate(UniTimeAction action) {
        if (query==null || query.trim().isEmpty())
        	action.addFieldError("form.query", MSG.errorQueryIsRequired());
    }

    @Override
    public void reset() {
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
