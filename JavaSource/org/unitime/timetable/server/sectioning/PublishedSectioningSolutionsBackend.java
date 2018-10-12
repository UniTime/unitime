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
package org.unitime.timetable.server.sectioning;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.cpsolver.ifs.util.DataProperties;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface.PublishedSectioningSolutionsRequest;
import org.unitime.timetable.model.SectioningSolutionLog;
import org.unitime.timetable.model.dao.SectioningSolutionLogDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.StudentSectioningSolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.NameFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(PublishedSectioningSolutionsRequest.class)
public class PublishedSectioningSolutionsBackend implements GwtRpcImplementation<PublishedSectioningSolutionsRequest, GwtRpcResponseList<PublishedSectioningSolutionInterface>> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired StudentSectioningSolverService studentSectioningSolverService;

	@Override
	public GwtRpcResponseList<PublishedSectioningSolutionInterface> execute(PublishedSectioningSolutionsRequest request, SessionContext context) {
		context.checkPermission(Right.StudentSectioningSolverPublish);
		
		SectioningSolutionLog solution = null;
		if (request.getUniqueId() != null) {
			solution = SectioningSolutionLogDAO.getInstance().get(request.getUniqueId());
			if (solution == null)
				throw new GwtRpcException(MESSAGES.errorSolutionDoesNotExist(request.getUniqueId().toString()));
		}
		StudentSolverProxy publishedSolver = studentSectioningSolverService.getSolver("PUBLISHED_" + context.getUser().getCurrentAcademicSessionId(), context.getUser().getCurrentAcademicSessionId());
		DataProperties publishedSolverProperties  = (publishedSolver == null ? null : publishedSolver.getProperties());
		Long selectedId = (publishedSolverProperties == null ? null : publishedSolverProperties.getPropertyLong("StudentSct.PublishId", null));

		StudentSolverProxy mySolver = studentSectioningSolverService.getSolver(context.getUser().getExternalUserId(), context.getUser().getCurrentAcademicSessionId());
		DataProperties mySolverProperties = (mySolver == null ? null : mySolver.getProperties());
		Long mySolverId = null;
		if (mySolverProperties != null && context.getUser().getExternalUserId().equals(mySolverProperties.getProperty("General.OwnerPuid"))) {
			mySolverId = mySolverProperties.getPropertyLong("StudentSct.PublishId", null);
		}
		
		switch (request.getOperation()) {
		case LIST:
			break;
		case PUBLISH:
			publishedSolverProperties = getConfig(solution.getData());
			selectedId = solution.getUniqueId();
			publishedSolver = studentSectioningSolverService.publishSolver(solution.getUniqueId(), publishedSolverProperties, solution.getData());
        	break;
		case UNPUBLISH:
			if (publishedSolver != null) {
				publishedSolver.interrupt();
				publishedSolver.dispose();
			}
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
			context.removeAttribute(SessionAttribute.StudentSectioningUser);
            publishedSolver = null;
            publishedSolverProperties = null;
            selectedId = null;
            break;
		case LOAD:
        	if (mySolver != null && mySolver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	mySolverProperties = getConfig(solution.getData());
        	mySolverProperties.setProperty("StudentSct.PublishId", solution.getUniqueId().toString());
        	mySolver = studentSectioningSolverService.createSolver(mySolverProperties, solution.getData());
        	mySolverId = solution.getUniqueId();
        	break;
		case UNLOAD:
			if (mySolver != null) {
				mySolver.interrupt();
				mySolver.dispose();
			}
			context.setAttribute(SessionAttribute.StudentSectioningUser, context.getUser().getExternalUserId());
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
        	mySolver = null;
			mySolverProperties = null;
        	mySolverId = null;
			break;
		case SELECT:
			context.setAttribute(SessionAttribute.StudentSectioningUser, "PUBLISHED_" + context.getUser().getCurrentAcademicSessionId());
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
            break;
		case DESELECT:
			context.removeAttribute(SessionAttribute.StudentSectioningUser);
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
			break;
		case REMOVE:
			SectioningSolutionLogDAO.getInstance().delete(solution);
			break;
		}
		
		GwtRpcResponseList<PublishedSectioningSolutionInterface> ret = new GwtRpcResponseList<PublishedSectioningSolutionInterface>();
		NameFormat nf = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
		Gson gson = getGson();
		for (SectioningSolutionLog log: (List<SectioningSolutionLog>)SectioningSolutionLogDAO.getInstance().getSession().createQuery(
				"from SectioningSolutionLog where session = :sessionId order by timeStamp")
				.setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setCacheable(true).list()) {
			PublishedSectioningSolutionInterface pss = new PublishedSectioningSolutionInterface();
			pss.setUniqueId(log.getUniqueId());
			pss.setInfo(new HashMap<String, String>(gson.fromJson(log.getInfo(), Map.class)));
			pss.setOwner(nf.format(log.getOwner()));
			pss.setTimeStamp(log.getTimeStamp());
			pss.setLoaded(log.getUniqueId().equals(selectedId));
			pss.setCanLoad(!pss.isLoaded());
			pss.setClonned(log.getUniqueId().equals(mySolverId));
			pss.setCanClone(mySolver == null);
			if (pss.isLoaded())
				pss.setSelected(("PUBLISHED_" + context.getUser().getCurrentAcademicSessionId()).equals(context.getAttribute(SessionAttribute.StudentSectioningUser)));
			else
				pss.setSelected(false);
			pss.setCanSelect(mySolver != null);
			ret.add(pss);
		}
		return ret;
	}

	protected Gson getGson() {
		GsonBuilder builder = new GsonBuilder()
		.registerTypeAdapter(DateTime.class, new JsonSerializer<DateTime>() {
			@Override
			public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			}
		})
		.registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {
			@Override
			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return new DateTime(json.getAsJsonPrimitive().getAsString(), DateTimeZone.UTC);
			}
		})
		.registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(src));
			}
		})
		.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				try {
					return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(json.getAsJsonPrimitive().getAsString());
				} catch (ParseException e) {
					throw new JsonParseException(e.getMessage(), e);
				}
			}
		});
		builder.setPrettyPrinting();
		return builder.create();
	}
	
	protected DataProperties getConfig(byte[] data) {
		try {
			Document document = (new SAXReader()).read(new GZIPInputStream(new ByteArrayInputStream(data)));
			DataProperties config = new DataProperties();
			for (Iterator i = document.getRootElement().element("configuration").elementIterator("property"); i.hasNext(); ) {
    			Element e = (Element)i.next();
    			config.setProperty(e.attributeValue("name"), e.getText());
    		}	    		
			return config;
		} catch (Exception e) {
			throw new GwtRpcException(MESSAGES.failedLoadData(e.getMessage()), e);
		}
		
	}
}
