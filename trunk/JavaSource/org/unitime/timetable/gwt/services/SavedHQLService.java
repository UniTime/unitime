/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */
@RemoteServiceRelativePath("hql.gwt")
public interface SavedHQLService extends RemoteService {
	List<SavedHQLInterface.Flag> getFlags() throws SavedHQLException, PageAccessException;
	List<SavedHQLInterface.Option> getOptions() throws SavedHQLException, PageAccessException;
	Boolean editable() throws SavedHQLException, PageAccessException;
	List<SavedHQLInterface.Query> queries(String appearance) throws SavedHQLException, PageAccessException;
	List<String[]> execute(SavedHQLInterface.Query query, List<SavedHQLInterface.IdValue> options, int fromRow, int maxRows) throws SavedHQLException, PageAccessException;
	Long store(SavedHQLInterface.Query query) throws SavedHQLException, PageAccessException;
	Boolean delete(Long id) throws SavedHQLException, PageAccessException;
	Boolean setBack(String appearance, String history, List<Long> ids, String type) throws SavedHQLException, PageAccessException;
}
