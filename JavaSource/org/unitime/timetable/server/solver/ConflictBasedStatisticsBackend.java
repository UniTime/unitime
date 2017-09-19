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
package org.unitime.timetable.server.solver;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CBSNode;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ConflictBasedStatisticsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SelectedAssignment;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.ConflictStatisticsInfo;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ConflictBasedStatisticsRequest.class)
public class ConflictBasedStatisticsBackend implements GwtRpcImplementation<ConflictBasedStatisticsRequest, GwtRpcResponseList<CBSNode>> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	public static boolean isAllSubjects(String subjects) {
		if (subjects == null || subjects.isEmpty() || subjects.equals(Constants.ALL_OPTION_VALUE)) return true;
		for (String id: subjects.split(","))
			if (Constants.ALL_OPTION_VALUE.equals(id)) return true;
		return false;
	}

	@Override
	public GwtRpcResponseList<CBSNode> execute(ConflictBasedStatisticsRequest request, SessionContext context) {
		if (request.hasClassId())
			context.checkPermission(Right.Suggestions);
		else
			context.checkPermission(Right.ConflictStatistics);
		
		if (!request.hasClassId()) {
			context.getUser().setProperty("Cbs.type", request.isVariableOriented() ? "0" : "1");
			context.getUser().setProperty("Cbs.limit", String.valueOf(request.getLimit()));
		}
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		ConflictStatisticsInfo info = null;
		if (solver != null) {
			if (request.hasClassId())
				info = solver.getCbsInfo(request.getClassId());
			else
				info = solver.getCbsInfo();
		} else {
			String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
    		if (solutionIdsStr != null) {
    			for (String solutionId: solutionIdsStr.split(",")) {
    				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
    				if (solution != null) {
    					ConflictStatisticsInfo x = (ConflictStatisticsInfo)solution.getInfo("CBSInfo");
    					if (x != null) {
    						if (info==null) info = x; else info.merge(x);
    					}
    				}
    			}
    		}
		}
		
		if (info == null || info.getCBS().isEmpty()) return null;
		return convert(info.getCBS(), request.getClassId(), request.isVariableOriented(), request.getLimit() / 100.0);
	}
	
	protected GwtRpcResponseList<CBSNode> convert(Collection<ConflictStatisticsInfo.CBSVariable> variables, Long classId, boolean variableOriented, double limit) {
		GwtRpcResponseList<CBSNode> response = new GwtRpcResponseList<CBSNode>();
		if (variableOriented) {
			if (classId != null)
				variables = ConflictStatisticsInfo.filter(variables, limit);
			for (ConflictStatisticsInfo.CBSVariable var: variables) {
				CBSNode varNode = null;
				if (classId == null) {
					varNode = variableNode(var);
					response.add(varNode);
				} else if (!classId.equals(var.getId())) {
					continue;
				}
				for (ConflictStatisticsInfo.CBSValue val: ConflictStatisticsInfo.filter(var.values(), limit)) {
					CBSNode valNode = valueNode(val);
					if (varNode != null)
						varNode.addNode(valNode);
					else
						response.add(valNode);
					for (ConflictStatisticsInfo.CBSConstraint con: ConflictStatisticsInfo.filter(val.constraints(), limit)) {
						CBSNode conNode = constraintNode(con);
						valNode.addNode(conNode);
						for (ConflictStatisticsInfo.CBSAssignment ass: ConflictStatisticsInfo.filter(con.assignments(), limit))
							conNode.addNode(assignmentNode(ass));
					}
				}
				
			}
		} else {
			Collection<ConflictStatisticsInfo.CBSConstraint> constraints = ConflictStatisticsInfo.transpose(variables, classId);
			for (ConflictStatisticsInfo.CBSConstraint consraint: ConflictStatisticsInfo.filter(constraints, limit)) {
				CBSNode conNode = constraintNode(consraint);
				response.add(conNode);
				for (ConflictStatisticsInfo.CBSVariable variable: ConflictStatisticsInfo.filter(consraint.variables(), limit)) {
					CBSNode varNode = null;
					if (classId == null) {
						varNode = variableNode(variable);
						conNode.addNode(varNode);
					}
					for (ConflictStatisticsInfo.CBSValue value: ConflictStatisticsInfo.filter(variable.values(), limit)) {
						CBSNode valNode = valueNode(value);
						if (varNode != null)
							varNode.addNode(valNode);
						else
							conNode.addNode(valNode);
						for (ConflictStatisticsInfo.CBSAssignment ass: ConflictStatisticsInfo.filter(value.assignments(), limit))
							valNode.addNode(assignmentNode(ass));
					}
				}
            }
		}
		return response;
	}
	
	private CBSNode variableNode(ConflictStatisticsInfo.CBSVariable variable) {
		CBSNode node = new CBSNode();
		node.setCount(variable.getCounter());
		node.setName(variable.getName());
		node.setPref(variable.getPref());
		node.setClassId(variable.getId());
		return node;
	}
	
	private CBSNode valueNode(ConflictStatisticsInfo.CBSValue value) {
		CBSNode node = new CBSNode();
		node.setCount(value.getCounter());
		SelectedAssignment sa = new SelectedAssignment();
		sa.setClassId(value.variable().getId());
		sa.setDatePatternId(value.getDatePatternId());
		sa.setDays(value.getDayCode());
		sa.setPatternId(value.getPatternId());
		sa.setStartSlot(value.getStartSlot());
		sa.setRoomIds(value.getRoomIds());
		String html = 
	    		"<font color='"+PreferenceLevel.int2color(value.getTimePref())+"'>"+
	    		value.getDays()+" "+value.getStartTime()+" - "+value.getEndTime()+" "+value.getDatePatternName()+
	    		"</font> ";
		String name = value.getDays()+" "+value.getStartTime()+" - "+value.getEndTime()+" "+value.getDatePatternName();
		for (int i=0;i<value.getRoomIds().size();i++) {
			html += (i > 0 ? ", " : "") + "<font color='"+PreferenceLevel.int2color(((Integer)value.getRoomPrefs().get(i)).intValue())+"'>"+ value.getRoomNames().get(i)+"</font>";
			name += (i > 0 ? ", " : "") + value.getRoomNames().get(i);
		}
		if (value.getInstructorName() != null) {
			html += " "+value.getInstructorName();
		}
		node.setName(name);
	    node.setHTML(html);
		node.setSelection(sa);
		return node;
	}
	
	private CBSNode constraintNode(ConflictStatisticsInfo.CBSConstraint constraint) {
		CBSNode node = new CBSNode();
		node.setCount(constraint.getCounter());
		node.setPref(constraint.getPref());
    	switch (constraint.getType()) {
    		case ConflictStatisticsInfo.sConstraintTypeBalanc : 
    			node.setName(MESSAGES.constraintDeptSpread(constraint.getName()));
    			break;
    		case ConflictStatisticsInfo.sConstraintTypeSpread : 
    			node.setName(MESSAGES.constraintSameSubpartSpread(constraint.getName()));
    			break;
    		case ConflictStatisticsInfo.sConstraintTypeGroup :
    			node.setName(MESSAGES.constraintDistribution(constraint.getName()));
    			break;
    		case ConflictStatisticsInfo.sConstraintTypeInstructor :
    			node.setName(MESSAGES.constraintInstructor(constraint.getName()));
    			try {
    				node.setLink("gwt.jsp?page=timetableGrid&resource=1&filter=" + URLEncoder.encode(constraint.getName(), "UTF-8") + "&search=1");
    			} catch (UnsupportedEncodingException e) {}
    			break;
    		case ConflictStatisticsInfo.sConstraintTypeRoom :
    			node.setName(MESSAGES.constraintRoom(constraint.getName()));
    			try {
    				node.setLink("gwt.jsp?page=timetableGrid&resource=0&filter=" + URLEncoder.encode(constraint.getName(), "UTF-8") + "&search=1");
    			} catch (UnsupportedEncodingException e) {}
    			break;
    		case ConflictStatisticsInfo.sConstraintTypeClassLimit :
    			node.setName(MESSAGES.constraintClassLimit(constraint.getName()));
    			break;
    		default: 
    			node.setName(constraint.getName() == null ? MESSAGES.constraintUnknown() : constraint.getName());
    	}
    	return node;
	}
	
	private CBSNode assignmentNode(ConflictStatisticsInfo.CBSAssignment assignment) {
		CBSNode node = new CBSNode();
		node.setCount(assignment.getCounter());
		SelectedAssignment sa = new SelectedAssignment();
		sa.setClassId(assignment.getId());
		sa.setDatePatternId(assignment.getDatePatternId());
		sa.setDays(assignment.getDayCode());
		sa.setPatternId(assignment.getPatternId());
		sa.setStartSlot(assignment.getStartSlot());
		sa.setRoomIds(assignment.getRoomIds());
		String name =
				assignment.getVariableName() + " " + assignment.getDays()+" "+assignment.getStartTime()+" - "+assignment.getEndTime()+" "+assignment.getDatePatternName();
		String html = 
				"<font color='"+PreferenceLevel.prolog2color(assignment.getPref())+"'>"+
				assignment.getVariableName()+ "</font> &larr; <font color='"+PreferenceLevel.int2color(assignment.getTimePref())+"'>"+
				assignment.getDays()+" "+assignment.getStartTime()+" - "+assignment.getEndTime()+" "+assignment.getDatePatternName()+"</font> ";
    	for (int i = 0; i < assignment.getRoomIds().size(); i++) {
    		html += (i>0?", ":"")+"<font color='"+PreferenceLevel.int2color(((Integer)assignment.getRoomPrefs().get(i)).intValue())+"'>"+ assignment.getRoomNames().get(i)+"</font>";
    		name += (i>0?", ":"")+assignment.getRoomNames().get(i);
    	}
    	if (assignment.getInstructorName()!=null) {
    		html += " "+assignment.getInstructorName();
    	}
    	node.setName(name);
		node.setHTML(html);
		node.setSelection(sa);
		return node;
	}
	
}