/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;

/**
 * @author Tomas Muller
 */
public class ExamInfoForm extends ActionForm {
    private String iOp;
    private ExamInfoModel iModel;
    private String iMessage;
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iOp = null;
        iModel = null;
        iMessage = null;
    }
    
    public void load(HttpSession session) {}
    
    public void save(HttpSession session) {}
    
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public ExamInfoModel getModel() { return iModel; }
    public void setModel(ExamInfoModel model) { iModel = model; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
}
