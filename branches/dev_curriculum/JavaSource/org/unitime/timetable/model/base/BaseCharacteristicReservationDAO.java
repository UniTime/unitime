/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;

import org.unitime.timetable.model.CharacteristicReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CharacteristicReservationDAO;

public abstract class BaseCharacteristicReservationDAO extends _RootDAO {

	private static CharacteristicReservationDAO sInstance;

	public static CharacteristicReservationDAO getInstance () {
		if (sInstance == null) sInstance = new CharacteristicReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CharacteristicReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CharacteristicReservation get(Long uniqueId) {
		return (CharacteristicReservation) get(getReferenceClass(), uniqueId);
	}

	public CharacteristicReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CharacteristicReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CharacteristicReservation load(Long uniqueId) {
		return (CharacteristicReservation) load(getReferenceClass(), uniqueId);
	}

	public CharacteristicReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CharacteristicReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CharacteristicReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CharacteristicReservation characteristicReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(characteristicReservation)) Hibernate.initialize(characteristicReservation);
		return characteristicReservation;
	}

	public void save(CharacteristicReservation characteristicReservation) {
		save((Object) characteristicReservation);
	}

	public void save(CharacteristicReservation characteristicReservation, org.hibernate.Session hibSession) {
		save((Object) characteristicReservation, hibSession);
	}

	public void saveOrUpdate(CharacteristicReservation characteristicReservation) {
		saveOrUpdate((Object) characteristicReservation);
	}

	public void saveOrUpdate(CharacteristicReservation characteristicReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) characteristicReservation, hibSession);
	}


	public void update(CharacteristicReservation characteristicReservation) {
		update((Object) characteristicReservation);
	}

	public void update(CharacteristicReservation characteristicReservation, org.hibernate.Session hibSession) {
		update((Object) characteristicReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CharacteristicReservation characteristicReservation) {
		delete((Object) characteristicReservation);
	}

	public void delete(CharacteristicReservation characteristicReservation, org.hibernate.Session hibSession) {
		delete((Object) characteristicReservation, hibSession);
	}

	public void refresh(CharacteristicReservation characteristicReservation, org.hibernate.Session hibSession) {
		refresh((Object) characteristicReservation, hibSession);
	}
}
