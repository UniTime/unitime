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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Tomas Muller
 */
public interface CourseFinder extends HasValue<RequestedCourse>, HasSelectionHandlers<RequestedCourse>, IsWidget, HasEnabled {
	public void findCourse();
	
	public void setTabs(CourseFinderTab... tabs);
	
	public interface CourseFinderTab<E> extends HasValue<RequestedCourse>, HasSelectionHandlers<RequestedCourse>, IsWidget, KeyUpHandler, HasResponseHandlers, HasEnabled {
		public String getName();
		public void setDataProvider(DataProvider<String, E> provider);
		public boolean isCourseSelection();
		public void setCourseDetails(CourseFinderCourseDetails... details);
		public void changeTip();
	}
		
	public interface CourseFinderCourseDetails<T, E> extends TakesValue<T>, IsWidget, HasEnabled {
		public void setDataProvider(DataProvider<T, E> provider);
		public String getName();
		public void onSetValue(RequestedCourse course);
		public void onGetValue(RequestedCourse course);
	}
	
	public interface HasResponseHandlers extends HasHandlers {
		HandlerRegistration addResponseHandler(ResponseHandler handler);
	}
	
	public interface ResponseHandler extends EventHandler {
		void onResponse(ResponseEvent event);
	}
	
	public class ResponseEvent extends GwtEvent<ResponseHandler> {
		static Type<ResponseHandler> TYPE = new Type<ResponseHandler>();
		private boolean iValid;
		
		public ResponseEvent(boolean valid) { iValid = valid; }
		
		public boolean isValid() { return iValid; }

		@Override
		public Type<ResponseHandler> getAssociatedType() { return TYPE; }
		public static Type<ResponseHandler> getType() { return TYPE; }

		@Override
		protected void dispatch(ResponseHandler handler) {
			handler.onResponse(this);
		}
		
		public static void fire(HasHandlers source, boolean valid) {
			source.fireEvent(new ResponseEvent(valid));
		}
	}
}
