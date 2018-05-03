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
package org.unitime.timetable.server.rooms;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ClientResource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.RoomInterface.GeocodeRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.GeocodeResponse;
import org.unitime.timetable.onlinesectioning.custom.purdue.GsonRepresentation;
import org.unitime.timetable.security.SessionContext;

import com.google.gson.reflect.TypeToken;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GeocodeRequest.class)
public class GeocodingBackend implements GwtRpcImplementation<GeocodeRequest, GeocodeResponse>, InitializingBean, DisposableBean {
	private static Logger sLog = Logger.getLogger(GeocodingBackend.class);
	private Client iClient;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.HTTP);
		protocols.add(Protocol.HTTPS);
		iClient = new Client(protocols);
	}

	@Override
	public void destroy() throws Exception {
		iClient.stop();
	}

	@Override
	public GeocodeResponse execute(GeocodeRequest request, SessionContext context) {
		ClientResource resource = null;
		try {
			if (request.isReverse()) {
				resource = new ClientResource("https://nominatim.openstreetmap.org/reverse");
			} else {
				resource = new ClientResource("https://nominatim.openstreetmap.org/search");
			}
			resource.setNext(iClient);
			resource.addQueryParameter("format", "json");
			resource.addQueryParameter("limit", "1");
			if (request.hasQuery())
				resource.addQueryParameter("q", request.getQuery());
			if (request.hasViewBox())
				resource.addQueryParameter("viewbox", request.getViewBox());
			if (request.hasCoordinates()) {
				resource.addQueryParameter("lat", request.getLat().toString());
				resource.addQueryParameter("lon", request.getLon().toString());
			}
			resource.get(MediaType.APPLICATION_JSON);
			GeocodeResponseMessage message = null;
			if (request.isReverse()) {
				message = new GsonRepresentation<GeocodeResponseMessage>(resource.getResponseEntity(), GeocodeResponseMessage.class).getObject();
			} else {
				List<GeocodeResponseMessage> messages = new GsonRepresentation<List<GeocodeResponseMessage>>(resource.getResponseEntity(), GeocodeResponseMessage.TYPE_LIST).getObject();
				if (messages != null && !messages.isEmpty())
					message = messages.get(0);
			}
			if (message != null) {
				GeocodeResponse response = new GeocodeResponse();
				response.setQuery(message.display_name);
				response.setCoordinates(message.lat, message.lon);
				return response;
			}
			throw new GwtRpcException("No match.");
		} catch (Exception e) {
			sLog.error("Failed to geocode: " + e.getMessage(), e);
			throw new GwtRpcException("Failed to geocode: " + e.getMessage());
		} finally {
			if (resource != null) {
				if (resource.getResponse() != null) resource.getResponse().release();
				resource.release();
			}
		}
	}

	static class GeocodeResponseMessage {
		public static final Type TYPE_LIST = new TypeToken<ArrayList<GeocodeResponseMessage>>() {}.getType();
		String display_name;
		Double lat, lon;
	}
}
