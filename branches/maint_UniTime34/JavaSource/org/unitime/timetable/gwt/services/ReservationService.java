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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */

@RemoteServiceRelativePath("reservation.gwt")
public interface ReservationService extends RemoteService {
	public ReservationInterface.Offering getOffering(Long offeringId) throws ReservationException, PageAccessException;
	public ReservationInterface.Offering getOfferingByCourseName(String course) throws ReservationException, PageAccessException;
	public List<ReservationInterface.Area> getAreas() throws ReservationException, PageAccessException;
	public List<ReservationInterface.IdName> getStudentGroups() throws ReservationException, PageAccessException;
	public List<ReservationInterface.Curriculum> getCurricula(Long offeringId) throws ReservationException, PageAccessException;

	public ReservationInterface getReservation(Long reservationId) throws ReservationException, PageAccessException;
	public List<ReservationInterface> getReservations(Long offeringId) throws ReservationException, PageAccessException;
	public Long save(ReservationInterface reservation) throws ReservationException, PageAccessException;
	public Boolean delete(Long reservationId) throws ReservationException, PageAccessException;
	public Boolean canAddReservation() throws ReservationException, PageAccessException;
	public String lastReservationFilter() throws ReservationException, PageAccessException;
	public List<ReservationInterface> findReservations(ReservationInterface.ReservationFilterRpcRequest filter) throws ReservationException, PageAccessException;

}
