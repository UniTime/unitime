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
package org.unitime.timetable.server.administration.dataexchange;

import org.unitime.timetable.backup.BackupProgress;
import org.unitime.timetable.dataexchange.DataExchangeHelper.LogWriter;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.queue.QueueItem;

public abstract class DataExchangeQueueItem extends QueueItem implements LogWriter, BackupProgress {
	
	public DataExchangeQueueItem(SessionContext context) {
		super(context.getUser());
	}

	private static final long serialVersionUID = 1L;

	@Override
	public String type() {
		return "Data Exchange";
	}
	
	public void println(String message) {
		log(message);
	}
	
	abstract void executeDataExchange() throws Exception;
	
	@Override
	public void info(String message) {
		super.info(message);
	}
	
	@Override
	public void warn(String message) {
		super.warn(message);
	}
	
	@Override
	public void error(String message) {
		super.error(message);
	}
	
	@Override
	public void setPhase(String phase, double max) {
		super.setStatus(phase, max);
	}
}