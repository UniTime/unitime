/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.services;

import java.util.List;

import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.ReservationException;
import org.unitime.timetable.gwt.shared.ReservationInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface ReservationServiceAsync {
	public void getOffering(Long offeringId, AsyncCallback<ReservationInterface.Offering> callback) throws ReservationException, PageAccessException;
	public void getOfferingByCourseName(String course, AsyncCallback<ReservationInterface.Offering> callback) throws ReservationException, PageAccessException;
	public void getAreas(AsyncCallback<List<ReservationInterface.Area>> callback) throws ReservationException, PageAccessException;
	public void getStudentGroups(AsyncCallback<List<ReservationInterface.IdName>> callback) throws ReservationException, PageAccessException;
	public void getCurricula(Long offeringId, AsyncCallback<List<ReservationInterface.Curriculum>> callback) throws ReservationException, PageAccessException;

	public void getReservation(Long reservationId, AsyncCallback<ReservationInterface> callback) throws ReservationException, PageAccessException;
	public void getReservations(Long offeringId, AsyncCallback<List<ReservationInterface>> callback) throws ReservationException, PageAccessException;
	public void save(ReservationInterface reservation, AsyncCallback<Long> callback) throws ReservationException, PageAccessException;
	public void delete(Long reservationId, AsyncCallback<Boolean> callback) throws ReservationException, PageAccessException;
	public void canAddReservation(AsyncCallback<Boolean> callback) throws ReservationException, PageAccessException;
	public void lastReservationFilter(AsyncCallback<String> callback) throws ReservationException, PageAccessException;
	public void findReservations(ReservationInterface.ReservationFilterRpcRequest filter, AsyncCallback<List<ReservationInterface>> callback) throws ReservationException, PageAccessException;
}
