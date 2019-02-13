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
	public ReservationInterface.Offering getOffering(Long offeringId, Long courseId) throws ReservationException, PageAccessException;
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
