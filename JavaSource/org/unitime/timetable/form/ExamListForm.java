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

import java.util.Collection;

import org.unitime.timetable.action.UniTimeAction;

/**
 * @author Tomas Muller
 */
public class ExamListForm implements UniTimeForm {
    private static final long serialVersionUID = 0L;
    private Long iSubjectAreaId = null;
    private String iCourseNbr = null;
    private String iOp = null;
    private Collection iSubjectAreas = null;
    private Long iExamType = null;
    
    public Long getSubjectAreaId() { return iSubjectAreaId; }
    public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }
    public String getCourseNbr() { return iCourseNbr; }
    public void setCourseNbr(String courseNbr) { 
        iCourseNbr = courseNbr;
        if ("null".equals(iCourseNbr)) iCourseNbr = "";
    }
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public Collection getSubjectAreas() { return iSubjectAreas; }
    public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
    
    @Override
	public void reset() {
        iSubjectAreaId = null; iCourseNbr = null; iOp = null;
        iExamType = null;
    }
    
    @Override
	public void validate(UniTimeAction action) {
    }
    
    public Long getExamType() { return iExamType; }
    public void setExamType(Long type) { iExamType = type; }
}
