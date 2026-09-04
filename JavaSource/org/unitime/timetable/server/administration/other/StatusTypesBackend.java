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
package org.unitime.timetable.server.administration.other;

import java.util.Iterator;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.StatusTypesPage.StatusTypesRequest;
import org.unitime.timetable.gwt.client.admin.StatusTypesPage.StatusTypesResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(StatusTypesRequest.class)
public class StatusTypesBackend implements GwtRpcImplementation<StatusTypesRequest, StatusTypesResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static GwtMessages GMSG = Localization.create(GwtMessages.class);

	@Override
	public StatusTypesResponse execute(StatusTypesRequest request, SessionContext context) {
		context.checkPermission(Right.StatusTypes);
		
		TableInterface table = new TableInterface();
		table.setId("AcademicSessions");
		table.setDefaultSortCookie("");

		LineInterface header = table.addHeader();
		header.addCell(GMSG.colOrder());
		header.addCell(MSG.fieldReference());
		header.addCell(MSG.fieldLabel());
		header.addCell(MSG.fieldApply());
		header.addCell(MSG.fieldRights());
		
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		
		TreeSet<DepartmentStatusType> statuses = DepartmentStatusType.findAll();
		if (statuses.isEmpty())
			table.setErrorMessage(MSG.infoNoStatusTypes());
		
		org.hibernate.Session hibSession = DepartmentStatusTypeDAO.getInstance().getSession(); 
		int ord = 0;
		for (Iterator<DepartmentStatusType> i = statuses.iterator(); i.hasNext(); ) {
			DepartmentStatusType s = i.next();
			if (ord != s.getOrd()) {
				s.setOrd(ord);
				hibSession.merge(s);
			}
			LineInterface line = table.addLine();
			line.setId(s.getUniqueId());
			line.setURL("#" + s.getUniqueId());
			line.setAnchor("A" + s.getUniqueId());
			CellInterface order = line.addCell().setComparable(ord);//.setText((1 + ord) + ". ");
			if (ord > 0) {
				order.addImage().setSource("images/arrow_up.png")
					.setAlt(MSG.altMoveUp())
					.setTitle(MSG.titleMoveUp());
				order.getItems().get(order.getNrItems() - 1).setUrl("#up-" + s.getUniqueId());
			} else {
				order.addImage().setSource("images/blank.png");
			}
			if (i.hasNext()) {
				order.addImage().setSource("images/arrow_down.png")
				.setAlt(MSG.altMoveDown())
				.setTitle(MSG.titleMoveDown());
				order.getItems().get(order.getNrItems() - 1).setUrl("#dw-" + s.getUniqueId());
			} else {
				order.addImage().setSource("images/blank.png");
			}
			line.addCell(s.getReference());
			line.addCell(s.getLabel());
			
        	String rights = "";
            String apply = "";
            if (s.applyDepartment()) {
                if (s.applySession()) {
                	if (s.applyExamStatus())
                		apply = MSG.applyToAll();
                	else
                		apply = MSG.applyToSessionAndDepartment().replace("&", "&amp;");
                } else
                    apply = MSG.applyToDepartment();
            } else if (s.applySession())
                apply = MSG.applyToSession();
            else if (s.applyExamStatus())
            	apply = MSG.applyToExaminations();
            if (s.isAllowRollForward()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightRollFoward();
            }
            if (s.isInstructorSurveyEnabled()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightInstructorSurvey();
            }
            if (s.canOwnerView() || s.canOwnerLimitedEdit() || s.canOwnerEdit()) {
                if (rights.length()>0) rights+="; ";
                if (s.canOwnerView() && s.canOwnerEdit())
                    rights += MSG.rightOwnerCan(MSG.rightViewAndEdit()); 
                else {
                	String r = null;
                    if (s.canOwnerView())
                    	r = MSG.rightView();
                    if (s.canOwnerEdit()) {
                        if (r != null) r = MSG.rightAnd(r, MSG.rightEdit());
                        else r = MSG.rightEdit();
                    } else if (s.canOwnerLimitedEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightLimitedEdit());
                    	else r = MSG.rightLimitedEdit();
                    }
                    rights += MSG.rightOwnerCan(r);
                }
            }
            if (s.canManagerView() || s.canManagerLimitedEdit() || s.canManagerEdit()) {
                if (rights.length()>0) rights+="; ";
                if (s.canManagerView() && s.canManagerEdit())
                    rights +=  MSG.rightManagerCan(MSG.rightViewAndEdit());
                else {
                	String r = null;
                    if (s.canManagerView())
                    	r = MSG.rightView();
                    if (s.canManagerEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightEdit());
                        else r = MSG.rightEdit();
                    } else if (s.canManagerLimitedEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightLimitedEdit());
                    	else r = MSG.rightLimitedEdit();
                    }
                    rights += MSG.rightManagerCan(r);
                }
            }
            if (s.canAudit()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightAudit();
            }
            if (s.canTimetable()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightTimetable();
            } 
            if (s.canCommit()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightCommit();
            }
            if (s.canExamView() || s.canExamEdit() || s.canExamTimetable()) {
                if (rights.length()>0) rights+="; ";
                if (s.canExamEdit() && s.canExamTimetable())
                    rights += MSG.rightExam(MSG.rightEditAndTimetable());
                else {
                	String r = null;
                    if (s.canExamView())
                        r = MSG.rightView();
                    if (s.canExamEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightEdit());
                        else r = MSG.rightEdit();
                    } else if (s.canExamTimetable()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightTimetable());
                        else r = MSG.rightTimetable();
                    }
                    rights += MSG.rightExam(r);
                }
            }
            if (s.canOnlineSectionStudents()) {
            	if (rights.length()>0) rights+="; ";
            	rights += MSG.rightSectioning();
            } else if (s.canSectionAssistStudents()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightAssitant();
            } else if (s.canPreRegisterStudents()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightRegistration();
            }
            if (s.isEventManagement()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightEvents();
            }
            if (s.isAllowNoRole() || s.canNoRoleReportExamFinal() || s.canNoRoleReportExamMidterm() || s.canNoRoleReportClass()) {
                if (rights.length()>0) rights+="; ";
                if (s.canNoRoleReportExamFinal() && s.canNoRoleReportExamMidterm() && s.canNoRoleReportClass())
                    rights += MSG.rightNoRoleCan(MSG.rightSeeAllEvents()) ;
                else {
                	String r = null;
                    if (s.canNoRoleReportClass()) r = MSG.rightSeeClasses();
                    if (s.canNoRoleReportExamFinal() && s.canNoRoleReportExamMidterm()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightSeeExams());
                        else r = MSG.rightSeeExams();
                    } else {
                        if (s.canNoRoleReportExamFinal()) {
                        	if (r != null) r = MSG.rightAnd(r, MSG.rightSeeFinalExams());
                            else r = MSG.rightSeeFinalExams();
                        }
                        if (s.canNoRoleReportExamMidterm()) {
                        	if (r != null) r = MSG.rightAnd(r, MSG.rightSeeMidtermExams());
                            else r = MSG.rightSeeMidtermExams();
                        }
                    }
                    if (r == null) r = "";
                    rights += MSG.rightNoRoleCan(r).trim();
                }
            }
            if (s.isTestSession()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightTestSession();
            }
            
			line.addCell(apply).setComparable(s.getApply(), s.getOrd());
			line.addCell(rights).setComparable(s.getStatus(), s.getOrd());
			ord++;
		}

		hibSession.flush();
		StatusTypesResponse response = new StatusTypesResponse();
		response.setStatusTypesTable(table);
		response.setCanAdd(true);
		return response;
	}

}
