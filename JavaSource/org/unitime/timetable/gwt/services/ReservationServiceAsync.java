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
