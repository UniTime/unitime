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
import org.unitime.timetable.gwt.shared.SavedHQLException;
import org.unitime.timetable.gwt.shared.SavedHQLInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface SavedHQLServiceAsync {
	void getFlags(AsyncCallback<List<SavedHQLInterface.Flag>> callback) throws SavedHQLException, PageAccessException;
	void getOptions(AsyncCallback<List<SavedHQLInterface.Option>> callback) throws SavedHQLException, PageAccessException;
	void editable(AsyncCallback<Boolean> callback) throws SavedHQLException, PageAccessException;
	void queries(String appearance, AsyncCallback<List<SavedHQLInterface.Query>> callback) throws SavedHQLException, PageAccessException;
	void execute(SavedHQLInterface.Query query, List<SavedHQLInterface.IdValue> options, int fromRow, int maxRows, AsyncCallback<List<String[]>> callback) throws SavedHQLException, PageAccessException;
	void store(SavedHQLInterface.Query query, AsyncCallback<Long> callback) throws SavedHQLException, PageAccessException;
	void delete(Long id, AsyncCallback<Boolean> callback) throws SavedHQLException, PageAccessException;
	void setBack(String appearance, String history, List<Long> ids, String type, AsyncCallback<Boolean> callback) throws SavedHQLException, PageAccessException;
}

