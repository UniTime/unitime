/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.EventInterface.EventRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ExaminationEnrollmentTable extends EnrollmentTable {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);

	public ExaminationEnrollmentTable(boolean showHeader, boolean online) {
		super(showHeader, online);
		getTable().setStyleName("unitime-Enrollments");
	}
	
	@Override
	protected void refresh() {
		clear();
		getHeader().showLoading();
		if (getId() != null) {
			RPC.execute(ExaminationEnrollmentsRpcRequest.getEnrollmentsForExam(getId()), new AsyncCallback<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>>() {
				@Override
				public void onFailure(Throwable caught) {
					getHeader().setErrorMessage(MESSAGES.failedNoEnrollments(caught.getMessage()));
				}

				@Override
				public void onSuccess(GwtRpcResponseList<Enrollment> result) {
					getHeader().clearMessage();
					populate(result, null);
				}
			});
		}
	}
	
	public static class ExaminationEnrollmentsRpcRequest extends EventRpcRequest<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>> {
		private Long iExamId;
		
		public ExaminationEnrollmentsRpcRequest() {}
		
		public boolean hasExamId() { return iExamId != null; }
		public Long getExamId() { return iExamId; }
		public void setExamId(Long examId) { iExamId = examId; }
		
		@Override
		public String toString() {
			return (hasExamId() ? getExamId().toString() : "NULL");
		}
		
		public static ExaminationEnrollmentsRpcRequest getEnrollmentsForExam(Long examId) {
			ExaminationEnrollmentsRpcRequest request = new ExaminationEnrollmentsRpcRequest();
			request.setExamId(examId);
			return request;
		}
	}

}
