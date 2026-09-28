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
package org.unitime.timetable.server.administration.solver;

import java.util.HashSet;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigEditRequest;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigEditResponse;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigInterface;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverParameterGroupInterface;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverParameterInterface;
import org.unitime.timetable.gwt.client.admin.SolverConfigsPage.SolverConfigInterface.Appearance;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(SolverConfigEditRequest.class)
public class SolverConfigEditBackend implements GwtRpcImplementation<SolverConfigEditRequest, SolverConfigEditResponse>{
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public SolverConfigEditResponse execute(SolverConfigEditRequest request, SessionContext context) {
		context.checkPermission(Right.SolverConfigurations);
		switch(request.getOperation()) {
		case ADD:
			SolverConfigEditResponse addResponse = new SolverConfigEditResponse();
			addResponse.setSolverConfig(new SolverConfigInterface());
			addResponse.getSolverConfig().setAppearance(request.getAppearance());
			if (addResponse.getSolverConfig().getAppearance() == null)
				addResponse.getSolverConfig().setAppearance(Appearance.SOLVER);
			loadSolverConfig(addResponse.getSolverConfig(), context);
			return addResponse;
		case EDIT:
			SolverConfigEditResponse editResponse = new SolverConfigEditResponse();
			editResponse.setSolverConfig(new SolverConfigInterface());
			editResponse.getSolverConfig().setSolverConfigId(request.getSolverConfigId());
			loadSolverConfig(editResponse.getSolverConfig(), context);
			return editResponse;
		case DELETE:
			deleteSolverConfig(request.getSolverConfigId(), context);
			return null;
		case SAVE:
        	SolverPredefinedSetting set = SolverPredefinedSetting.findByName(request.getSolverConfig().getReference());
        	if (request.getSolverConfigId() != null) { // update
        		if (set != null && !set.getUniqueId().equals(request.getSolverConfigId()))
        			throw new GwtRpcException(MSG.errorAlreadyExists(request.getSolverConfig().getName()));
        	} else { // save
        		if (set!=null)
        			throw new GwtRpcException(MSG.errorAlreadyExists(request.getSolverConfig().getName()));
        	}
			SolverConfigEditResponse saveResponse = new SolverConfigEditResponse();
			saveResponse.setSolverConfig(new SolverConfigInterface());
			saveResponse.getSolverConfig().setSolverConfigId(
					saveOrUpdateSolverConfig(request.getSolverConfig(), context));
			return saveResponse;
		default:
			return null;
		}
	}
	
	protected void loadSolverConfig(SolverConfigInterface config, SessionContext context) {
		SolverPredefinedSetting setting = (config.getSolverConfigId() == null ? null : SolverPredefinedSettingDAO.getInstance().get(config.getSolverConfigId()));
		if (setting != null) {
			config.setReference(setting.getName());
			config.setName(setting.getDescription());
			config.setAppearance(Appearance.values()[setting.getAppearance()]);
		}
		for (SolverParameterGroup group: SolverParameterGroupDAO.getInstance().getSession().createQuery(
				"from SolverParameterGroup order by order", SolverParameterGroup.class).list()) {
			SolverParameterGroupInterface g = null;
			for (SolverParameterDef def: new TreeSet<SolverParameterDef>(group.getParameters())) {
				if (!def.isVisible()) continue;
				if (g == null) {
					g = new SolverParameterGroupInterface();
					g.setId(group.getUniqueId());
					g.setLabel(group.getDescription());
					g.setSolverType(SolverType.values()[group.getType()]);
					config.addGroup(g);
				}
				SolverParameterInterface p = new SolverParameterInterface();
				p.setName(def.getName());
				p.setType(def.getType());
				p.setDescription(def.getDescription());
				p.setDefaultValue(def.getDefault());
				p.setUseDefault(true);
				if (setting != null)
					for (SolverParameter sp: setting.getParameters()) {
						if (sp.getDefinition().equals(def)) {
							p.setUseDefault(false);
							p.setValue(sp.getValue());
							break;
						}
					}
				g.addParameter(p);
			}
		}
	}
	
	protected void deleteSolverConfig(Long solverConfigId, SessionContext context) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        Transaction tx = null;
        
        try {
        	tx = hibSession.beginTransaction();
        	
        	SolverPredefinedSetting setting = SolverPredefinedSettingDAO.getInstance().get(solverConfigId, hibSession);
    		hibSession.remove(setting);

           	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected Long saveOrUpdateSolverConfig(SolverConfigInterface config, SessionContext context) {
        org.hibernate.Session hibSession = TimetableManagerDAO.getInstance().getSession();
        
        Transaction tx = null;
        Long ret = config.getSolverConfigId();
        try {
        	tx = hibSession.beginTransaction();
        	SolverPredefinedSetting setting = (config.getSolverConfigId() == null ? null : SolverPredefinedSettingDAO.getInstance().get(config.getSolverConfigId(), hibSession));
        	if (setting == null) {
        		setting = new SolverPredefinedSetting();
        		setting.setParameters(new HashSet<SolverParameter>());
        	}
    		setting.setAppearance(config.getAppearance().ordinal());
        	setting.setDescription(config.getName());
        	setting.setName(config.getReference());
        	
        	if (setting.getUniqueId() == null)
        		hibSession.persist(setting);
        	else
        		hibSession.merge(setting);
    		ret = setting.getUniqueId();
        	
        	for (SolverParameterDef def: SolverParameterDefDAO.getInstance().findAll(hibSession)) {
        		SolverParameter param = null;
        		for (SolverParameter p: setting.getParameters()) {
        			if (p.getDefinition().equals(def)) { param = p; break; }
        		}
        		if (!def.isVisible() || SolverType.values()[def.getGroup().getType()] != config.getAppearance().getSolverType()) {
        			if (param != null) {
        				setting.getParameters().remove(param);
        				hibSession.remove(param);
        			}
        			continue;
        		}
        		SolverParameterInterface p = config.getParameter(def.getGroup().getUniqueId(), def.getName());
        		if (p == null || p.isUseDefault()) {
        			if (param != null) {
        				setting.getParameters().remove(param);
        				hibSession.remove(param);
        			}
        		} else {
        			if (param != null) {
        				param.setValue(p.getValue());
        				hibSession.merge(param);
        			} else {
        				param = new SolverParameter();
        				param.setDefinition(def);
        				param.setValue(p.getValue());
        				setting.addToParameters(param);
        				hibSession.persist(param);
        			}
        		}
        	}
        	
        	hibSession.merge(setting);
        	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        return ret;
	}

}
