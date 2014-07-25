/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Tomas Muller
 */
public interface CourseFinder extends HasValue<String>, HasSelectionHandlers<String>, IsWidget {
	public void findCourse();
	
	public void setTabs(CourseFinderTab... tabs);
	
	public interface CourseFinderTab<E> extends HasValue<String>, HasSelectionHandlers<String>, IsWidget, KeyUpHandler, HasResponseHandlers {
		public String getName();
		public void setDataProvider(DataProvider<String, E> provider);
		public boolean isCourseSelection();
		public void setCourseDetails(CourseFinderCourseDetails... details);
		public void changeTip();
	}
		
	public interface CourseFinderCourseDetails<E> extends TakesValue<String>, IsWidget {
		public void setDataProvider(DataProvider<String, E> provider);
		public String getName();
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
