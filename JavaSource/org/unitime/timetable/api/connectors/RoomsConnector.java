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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.api.BinaryFileApiHelper;
import org.unitime.timetable.api.BinaryFileApiHelper.BinaryFile;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.FutureOperation;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomUpdateRpcRequest;
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.LocationPictureDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.rooms.RoomDetailsBackend;
import org.unitime.timetable.server.rooms.RoomPicturesBackend;
import org.unitime.timetable.server.rooms.RoomUpdateBackend;

/**
 * @author Tomas Muller
 */
@Service("/api/rooms")
public class RoomsConnector extends ApiConnector {
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		Long pictureId = helper.getOptinalParameterLong("pictureId", null);
		if (pictureId != null) {
			LocationPicture picture = LocationPictureDAO.getInstance().get(pictureId);
			if (picture == null)
				throw new IllegalArgumentException("Room picture of the given id does not exist.");
			
			helper.getSessionContext().checkPermissionAnyAuthority(picture.getLocation().getSession(), Right.ApiRetrieveRooms);
			
			helper.setResponse(new BinaryFile(picture.getDataFile(), picture.getContentType(), picture.getFileName()));
			return;
		}

		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		helper.getSessionContext().checkPermissionAnyAuthority(session, Right.ApiRetrieveRooms);
		
		RoomFilterRpcRequest request = new RoomFilterRpcRequest();
		request.setCommand(FilterRpcRequest.Command.ENUMERATE);
    	request.setSessionId(sessionId);
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.addOption(command.substring(2), value);
    		}
    	}
    	request.setOption("flag", "plain");
    	
    	EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
    	
    	FilterRpcResponse response = new FilterRpcResponse();
    	new RoomDetailsBackend().enumarate(request, response, context); 
    	
    	List<RoomDetailInterface> rooms = new ArrayList<RoomDetailInterface>();
    	if (response.hasResults()) {
    		for (Entity e: response.getResults())
    			rooms.add((RoomDetailInterface)e);
    	}
    	
    	helper.setResponse(rooms);
	}
	
	@Override
	public void doPut(ApiHelper helper) throws IOException {
		Location location = null;
		Long roomId = helper.getOptinalParameterLong("roomId", null);
		if (roomId != null) {
			location = LocationDAO.getInstance().get(roomId, helper.getHibSession());
			if (location == null)
				throw new IllegalArgumentException("Room " + roomId + " does not exist.");
		} else {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			String room = helper.getRequiredParameter("room");
			location = Location.findByName(helper.getHibSession(), sessionId, room);
			if (location == null)
				throw new IllegalArgumentException("Room " + room + " does not exist.");
		}
		
		helper.getSessionContext().checkPermissionAnyAuthority(location.getSession(), Right.ApiRoomPictureUpload);
		
		BinaryFile file = helper.getRequest(BinaryFile.class);
		updatePicture(helper, location, file);
		
		if (helper.getOptinalParameterBoolean("future", false)) {
			List<Location> futureLocations = helper.getHibSession().createQuery(
					"select l from Location l, Session s where " +
					"l.permanentId = :permanentId and s.uniqueId = :sessionId and s.sessionBeginDateTime < l.session.sessionBeginDateTime " + 
					"order by l.session.sessionBeginDateTime")
					.setLong("permanentId", location.getPermanentId()).setLong("sessionId", location.getSession().getUniqueId()).list();
			for (Location loc: futureLocations)
				updatePicture(helper, loc, file);
		}
	}
	
	@Override
	public void doDelete(ApiHelper helper) throws IOException {
		Long pictureId = helper.getOptinalParameterLong("pictureId", null);
		if (pictureId != null) {
			LocationPicture picture = LocationPictureDAO.getInstance().get(pictureId);
			if (picture == null)
				throw new IllegalArgumentException("Room picture of the given id does not exist.");
			
			helper.getSessionContext().checkPermissionAnyAuthority(picture.getLocation().getSession(), Right.ApiRoomPictureUpload);
			
			picture.getLocation().getPictures().remove(picture);
			helper.getHibSession().delete(picture);
			helper.getHibSession().flush();
			
			helper.setResponse(new RoomPictureInterface(picture.getUniqueId(), picture.getFileName(), picture.getContentType(), picture.getTimeStamp().getTime(), RoomPicturesBackend.getPictureType(picture.getType())));
			return;
		}
		
		Location location = null;
		Long roomId = helper.getOptinalParameterLong("roomId", null);
		if (roomId != null) {
			location = LocationDAO.getInstance().get(roomId, helper.getHibSession());
			if (location == null)
				throw new IllegalArgumentException("Room " + roomId + " does not exist.");
		} else {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			String room = helper.getRequiredParameter("room");
			location = Location.findByName(helper.getHibSession(), sessionId, room);
			if (location == null)
				throw new IllegalArgumentException("Room " + room + " does not exist.");
		}
		
		helper.getSessionContext().checkPermissionAnyAuthority(location.getSession(), Right.ApiRoomEdit);
		
		RoomUpdateRpcRequest request = new RoomUpdateRpcRequest();
		request.setLocationId(location.getUniqueId());
		request.setSessionId(location.getSession().getUniqueId());
		request.setOperation(RoomUpdateRpcRequest.Operation.DELETE);
		
		if (helper.getOptinalParameterBoolean("future", false)) {
			List<Location> futureLocations = helper.getHibSession().createQuery(
					"select l from Location l, Session s where " +
					"l.permanentId = :permanentId and s.uniqueId = :sessionId and s.sessionBeginDateTime < l.session.sessionBeginDateTime " + 
					"order by l.session.sessionBeginDateTime")
					.setLong("permanentId", location.getPermanentId()).setLong("sessionId", location.getSession().getUniqueId()).list();
			for (Location loc: futureLocations)
				request.setFutureFlag(loc.getUniqueId(), FutureOperation.getFlagAllEnabled());
		}
		
		helper.setResponse(new RoomUpdateBackend().execute(request, helper.getSessionContext()));
	}
	
	protected void updatePicture(ApiHelper helper, Location location, BinaryFile file) throws IOException {
		String name = helper.getOptinalParameter("name", file.getFileName());
		if (name == null) throw new IllegalArgumentException("Parameter 'name' was not provided.");
		String type = helper.getOptinalParameter("contentType", file.getContentType());
		if (type == null) throw new IllegalArgumentException("Parameter 'contentType' was not provided.");
		String reference = helper.getOptinalParameter("type", null);
		
		LocationPicture picture = null;
		for (LocationPicture p: location.getPictures()) {
			if (p.getFileName().equals(name)) { picture = p; break; }
		}
		if (picture == null) {
			if (location instanceof Room) {
				picture = new RoomPicture();
				picture.setFileName(name);
				picture.setLocation(location);
				((Room)location).getPictures().add((RoomPicture)picture);
			} else {
				picture = new NonUniversityLocationPicture();
				picture.setFileName(name);
				picture.setLocation(location);
				((NonUniversityLocation)location).getPictures().add((NonUniversityLocationPicture)picture);
			}
		}
		if (type != null) picture.setContentType(type);
		if (reference != null) picture.setType(AttachmentType.findByReference(helper.getHibSession(), reference));
		picture.setTimeStamp(new Date());
		picture.setDataFile(file.getBytes());
		helper.getHibSession().saveOrUpdate(picture);
		helper.getHibSession().flush();
		helper.setResponse(new RoomPictureInterface(picture.getUniqueId(), picture.getFileName(), picture.getContentType(), picture.getTimeStamp().getTime(), RoomPicturesBackend.getPictureType(picture.getType())));
	}
	
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		RoomDetailInterface room = helper.getRequest(RoomDetailInterface.class);
		if (room == null)
			throw new IllegalArgumentException("No room data provided.");
		Long roomId = helper.getOptinalParameterLong("roomId", null);
		if (roomId != null)
			room.setUniqueId(roomId);
		room.setSessionId(helper.getAcademicSessionId());
		if (room.getUniqueId() == null && room.getSessionId() == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");

		if (helper.getOptinalParameter("room", null) != null) {
			Location location = Location.findByName(helper.getHibSession(), room.getSessionId(), helper.getOptinalParameter("room", null));
			if (location != null)
				room.setUniqueId(location.getUniqueId());
		}
		
		helper.getSessionContext().checkPermissionAnyAuthority(room.getSessionId() != null ? room.getSessionId() : helper.getAcademicSessionId(), Right.ApiRoomEdit);
		
		RoomUpdateRpcRequest request = new RoomUpdateRpcRequest();
		request.setLocationId(room.getUniqueId());
		request.setSessionId(helper.getAcademicSessionId());
		request.setRoom(room);
		request.setOperation(room.getUniqueId() == null ? RoomUpdateRpcRequest.Operation.CREATE : RoomUpdateRpcRequest.Operation.UPDATE);
		
		String[] flags = helper.getParameterValues("flag");
		int f = 0;
		if (flags != null)
			for(String flag: flags)
				f = FutureOperation.valueOf(flag.toUpperCase()).set(f);
		if (f == 0)
			f = FutureOperation.PICTURES.clear(FutureOperation.getFlagAllEnabled());
		request.setFutureFlag(0l, f);
		
		if (helper.getOptinalParameterBoolean("future", false)) {
			if (room.getUniqueId() != null) {
				List<Location> futureLocations = helper.getHibSession().createQuery(
						"select l from Location l, Location o where " +
						"o.uniqueId = :uniqueId and l.permanentId = o.permanentId and o.session.sessionBeginDateTime < l.session.sessionBeginDateTime " + 
						"order by l.session.sessionBeginDateTime")
						.setLong("uniqueId", room.getUniqueId()).list();
				for (Location loc: futureLocations)
					request.setFutureFlag(loc.getUniqueId(), f);
			} else {
				List<Long> futureSessionIds = helper.getHibSession().createQuery(
						"select f.uniqueId from Session f, Session s where " +
						"s.uniqueId = :sessionId and s.sessionBeginDateTime < f.sessionBeginDateTime and s.academicInitiative = f.academicInitiative " +
						"order by f.sessionBeginDateTime")
						.setLong("sessionId", room.getSessionId()).list();
				for (Long id: futureSessionIds)
					request.setFutureFlag(-id, f);
			}
		}
		
		helper.setResponse(new RoomUpdateBackend().execute(request, helper.getSessionContext()));
	}
	
	@Override
	protected ApiHelper createHelper(HttpServletRequest request, HttpServletResponse response) {
		return new BinaryFileApiHelper(request, response, sessionContext, getCacheMode());
	}
	
	@Override
	protected String getName() {
		return "rooms";
	}
}
