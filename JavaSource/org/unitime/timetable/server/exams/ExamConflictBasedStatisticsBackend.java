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
package org.unitime.timetable.server.exams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamConflictBasedStatisticsRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CBSNode;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.webutil.timegrid.ExamGridTable;

@GwtRpcImplements(ExamConflictBasedStatisticsRequest.class)
public class ExamConflictBasedStatisticsBackend implements GwtRpcImplementation<ExamConflictBasedStatisticsRequest, GwtRpcResponseList<CBSNode>> {
	protected static final ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GMSG = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public GwtRpcResponseList<CBSNode> execute(ExamConflictBasedStatisticsRequest request, SessionContext context) {
		if (request.hasClassId())
			context.checkPermission(request.getClassId(), Right.ExaminationAssignment);
		else
			context.checkPermission(Right.ExaminationConflictStatistics);
		
		if (!request.hasClassId()) {
			context.getUser().setProperty("Ecbs.type", request.isVariableOriented() ? "0" : "1");
			context.getUser().setProperty("Ecbs.limit", String.valueOf(request.getLimit()));
		}
		boolean usePrefStyles = CommonValues.Yes.eq(UserProperty.HighContrastPreferences.get(context.getUser()));

		ExamSolverProxy solver = examinationSolverService.getSolver();
		ExamConflictStatisticsInfo info = null;
		if (solver != null) {
			if (request.hasClassId())
				info = solver.getCbsInfo(request.getClassId());
			else
				info = solver.getCbsInfo();
		}
		
		if (solver == null)
			throw new GwtRpcException(MESSAGES.warnCbsNoSolver());
		else if (info == null || info.getCBS().isEmpty())
			return null;

		return convert(info.getCBS(), request.getClassId(), request.isVariableOriented(), request.getLimit() / 100.0, usePrefStyles);
	}
	
	protected GwtRpcResponseList<CBSNode> convert(Collection<ExamConflictStatisticsInfo.CBSVariable> variables, Long classId, boolean variableOriented, double limit, boolean usePrefStyles) {
		GwtRpcResponseList<CBSNode> response = new GwtRpcResponseList<CBSNode>();
		if (variableOriented) {
			variables = ExamConflictStatisticsInfo.filter(variables, limit);
			for (ExamConflictStatisticsInfo.CBSVariable var: variables) {
				CBSNode varNode = null;
				if (classId == null) {
					varNode = variableNode(var);
					response.add(varNode);
				} else if (!classId.equals(var.getId())) {
					continue;
				}
				for (ExamConflictStatisticsInfo.CBSValue val: ExamConflictStatisticsInfo.filter(var.values(), limit)) {
					CBSNode valNode = valueNode(val, usePrefStyles);
					if (varNode != null)
						varNode.addNode(valNode);
					else
						response.add(valNode);
					for (ExamConflictStatisticsInfo.CBSConstraint con: ExamConflictStatisticsInfo.filter(val.constraints(), limit)) {
						CBSNode conNode = constraintNode(con);
						valNode.addNode(conNode);
						for (ExamConflictStatisticsInfo.CBSAssignment ass: ExamConflictStatisticsInfo.filter(con.assignments(), limit))
							conNode.addNode(assignmentNode(ass, usePrefStyles));
					}
				}
				
			}
		} else {
			Collection<ExamConflictStatisticsInfo.CBSConstraint> constraints = ExamConflictStatisticsInfo.transpose(variables, classId);
			for (ExamConflictStatisticsInfo.CBSConstraint consraint: ExamConflictStatisticsInfo.filter(constraints, limit)) {
				CBSNode conNode = constraintNode(consraint);
				response.add(conNode);
				for (ExamConflictStatisticsInfo.CBSVariable variable: ExamConflictStatisticsInfo.filter(consraint.variables(), limit)) {
					CBSNode varNode = null;
					if (classId == null) {
						varNode = variableNode(variable);
						conNode.addNode(varNode);
					}
					for (ExamConflictStatisticsInfo.CBSValue value: ExamConflictStatisticsInfo.filter(variable.values(), limit)) {
						CBSNode valNode = valueNode(value, usePrefStyles);
						if (varNode != null)
							varNode.addNode(valNode);
						else
							conNode.addNode(valNode);
						for (ExamConflictStatisticsInfo.CBSAssignment ass: ExamConflictStatisticsInfo.filter(value.assignments(), limit))
							valNode.addNode(assignmentNode(ass, usePrefStyles));
					}
				}
            }
		}
		return response;
	}
	
	private CBSNode variableNode(ExamConflictStatisticsInfo.CBSVariable variable) {
		CBSNode node = new CBSNode();
		node.setCount(variable.getCounter());
		node.setName(variable.getName());
		node.setPref(variable.getPref());
		node.setClassId(variable.getId());
		return node;
	}
	
	private CBSNode valueNode(ExamConflictStatisticsInfo.CBSValue value, boolean usePrefStyles) {
		CBSNode node = new CBSNode();
		node.setCount(value.getCounter());
		SelectedAssignment sa = new SelectedAssignment();
		sa.setClassId(value.variable().getId());
		sa.setPatternId(value.getPeriodId());
		sa.setRoomIds(value.getRoomIds());
		String html = null;
		if (usePrefStyles) {
			html = "<span class='pref-"+PreferenceLevel.int2char(value.getPeriodPref())+"'>"+value.getPeriodName()+"</span> ";
		} else {
			html = "<span style='color:'"+PreferenceLevel.int2color(value.getPeriodPref())+";'>"+value.getPeriodName()+"</span> ";
		}
		String name = value.getPeriodName();
		for (int i=0;i<value.getRoomIds().size();i++) {
			if (usePrefStyles)
				html += (i > 0 ? ", " : "") + "<span class='pref-"+PreferenceLevel.int2char(((Integer)value.getRoomPrefs().get(i)).intValue())+"'>"+ value.getRoomNames().get(i)+"</span>";
			else
				html += (i > 0 ? ", " : "") + "<span style='color:'"+PreferenceLevel.int2color(((Integer)value.getRoomPrefs().get(i)).intValue())+";'>"+ value.getRoomNames().get(i)+"</span>";
			name += (i > 0 ? ", " : "") + value.getRoomNames().get(i);
		}
		node.setName(name);
	    node.setHTML(html);
		node.setSelection(sa);
		return node;
	}
	
	private CBSNode constraintNode(ExamConflictStatisticsInfo.CBSConstraint constraint) {
		CBSNode node = new CBSNode();
		node.setCount(constraint.getCounter());
		node.setPref(constraint.getPref());
    	switch (constraint.getType()) {
    		case ExamConflictStatisticsInfo.sConstraintTypeStudent : 
    			node.setName(GMSG.constraintStudent(constraint.getName()));
    			break;
    		case ExamConflictStatisticsInfo.sConstraintTypeGroup :
    			node.setName(GMSG.constraintDistribution(constraint.getName()));
    			break;
    		case ExamConflictStatisticsInfo.sConstraintTypeInstructor :
    			node.setName(GMSG.constraintInstructor(constraint.getName()));
    			try {
    				node.setLink("examGrid.action?filter="+URLEncoder.encode(constraint.getName(), "UTF-8")+"&resource="+ExamGridTable.Resource.Instructor.ordinal()+"&op=Cbs");
    			} catch (UnsupportedEncodingException e) {}
    			break;
    		case ExamConflictStatisticsInfo.sConstraintTypeRoom :
    			node.setName(GMSG.constraintRoom(constraint.getName()));
    			try {
    				node.setLink("examGrid.action?filter="+URLEncoder.encode(constraint.getName(), "UTF-8")+"&resource="+ExamGridTable.Resource.Room.ordinal()+"&op=Cbs");
    			} catch (UnsupportedEncodingException e) {}
    			break;
    		default: 
    			node.setName(constraint.getName() == null ? GMSG.constraintUnknown() : constraint.getName());
    	}
    	return node;
	}
	
	private CBSNode assignmentNode(ExamConflictStatisticsInfo.CBSAssignment assignment, boolean usePrefStyles) {
		CBSNode node = new CBSNode();
		node.setCount(assignment.getCounter());
		SelectedAssignment sa = new SelectedAssignment();
		sa.setClassId(assignment.getId());
		sa.setPatternId(assignment.getPeriodId());
		sa.setRoomIds(assignment.getRoomIds());
		String name =
				assignment.getPeriodName();
		String html = null;
		if (usePrefStyles) {
				html = "<span style='color:"+PreferenceLevel.prolog2color(assignment.getPref())+";'>"+
				assignment.getName()+ "</span> &larr; <span class='pref-"+PreferenceLevel.int2char(assignment.getPeriodPref())+"'>"+
				assignment.getPeriodName()+"</span> ";
		} else {
			html = "<span style='color:'"+PreferenceLevel.prolog2color(assignment.getPref())+";'>"+
					assignment.getName()+ "</span> &larr; <span style='color:'"+PreferenceLevel.int2color(assignment.getPeriodPref())+";'>"+
					assignment.getPeriodName()+"</span> ";
		}
    	for (int i = 0; i < assignment.getRoomIds().size(); i++) {
    		if (usePrefStyles)
    			html += (i>0?", ":"")+"<span class='pref-"+PreferenceLevel.int2color(((Integer)assignment.getRoomPrefs().get(i)).intValue())+"'>"+ assignment.getRoomNames().get(i)+"</span>";
    		else
    			html += (i>0?", ":"")+"<span style='color:'"+PreferenceLevel.int2color(((Integer)assignment.getRoomPrefs().get(i)).intValue())+"'>"+ assignment.getRoomNames().get(i)+"</span>";
    		name += (i>0?", ":"")+assignment.getRoomNames().get(i);
    	}
    	node.setName(name);
		node.setHTML(html);
		node.setSelection(sa);
		return node;
	}

}
