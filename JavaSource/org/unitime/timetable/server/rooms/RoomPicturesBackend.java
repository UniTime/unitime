/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.server.rooms;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.fileupload.FileItem;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureResponse;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.LocationPictureDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomPictureRequest.class)
public class RoomPicturesBackend implements GwtRpcImplementation<RoomPictureRequest, RoomPictureResponse> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Override
	public RoomPictureResponse execute(RoomPictureRequest request, SessionContext context) {
		RoomPictureResponse response = new RoomPictureResponse();
		Location location = LocationDAO.getInstance().get(request.getLocationId());
		if (location == null)
			throw new GwtRpcException(MESSAGES.errorRoomDoesNotExist(request.getLocationId().toString()));
		response.setName(location.getLabel());
		
		context.checkPermission(location, Right.RoomEditChangePicture);

		Map<Long, LocationPicture> temp = (Map<Long, LocationPicture>)context.getAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES);

		switch (request.getOperation()) {
		case LOAD:
			for (LocationPicture p: new TreeSet<LocationPicture>(location.getPictures()))
				response.addPicture(new RoomPictureInterface(p.getUniqueId(), p.getFileName(), p.getContentType()));
			context.setAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES, null);
			break;
		case SAVE:
			Map<Long, LocationPicture> pictures = new HashMap<Long, LocationPicture>();
			for (LocationPicture p: location.getPictures())
				pictures.put(p.getUniqueId(), p);
			org.hibernate.Session hibSession = LocationPictureDAO.getInstance().getSession();
			for (RoomPictureInterface p: request.getPictures()) {
				LocationPicture picture = pictures.remove(p.getUniqueId());
				if (picture == null && temp != null) {
					picture = temp.get(p.getUniqueId());
					if (picture != null) {
						if (location instanceof Room) {
							((RoomPicture)picture).setLocation((Room)location);
							((Room)location).getPictures().add((RoomPicture)picture);
						} else {
							((NonUniversityLocationPicture)picture).setLocation((NonUniversityLocation)location);
							((NonUniversityLocation)location).getPictures().add((NonUniversityLocationPicture)picture);
						}
						hibSession.saveOrUpdate(picture);
					}
				}
			}
			for (LocationPicture picture: pictures.values()) {
				location.getPictures().remove(picture);
				hibSession.delete(picture);
			}
			hibSession.saveOrUpdate(location);
			hibSession.flush();
			context.setAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES, null);
			break;
		case UPLOAD:
			final FileItem file = (FileItem)context.getAttribute(UploadServlet.SESSION_LAST_FILE);
			if (file != null) {
				if (temp == null) {
					temp = new HashMap<Long, LocationPicture>();
					context.setAttribute(RoomPictureServlet.TEMP_ROOM_PICTURES, temp);
				}
				LocationPicture picture = null;
				if (location instanceof Room)
					picture = new RoomPicture();
				else
					picture = new NonUniversityLocationPicture();
				picture.setDataFile(file.get());
				String name = file.getName();
				if (name.indexOf('.') >= 0)
					name = name.substring(0, name.lastIndexOf('.'));
				picture.setFileName(name);
				picture.setContentType(file.getContentType());
				picture.setTimeStamp(new Date());
				temp.put(- picture.getTimeStamp().getTime(), picture);
				response.addPicture(new RoomPictureInterface(- picture.getTimeStamp().getTime(), picture.getFileName(), picture.getContentType()));
			}
			break;
		}
		
		return response;
	}

}
