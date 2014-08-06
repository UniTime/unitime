/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.instructor;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomSharingWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class InstructorAvailabilityWidget extends RoomSharingWidget {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private Hidden iPattern = null;
	
	public InstructorAvailabilityWidget() {
		super(false);
	}

	public void insert(final RootPanel panel) {
		RPC.execute(InstructorAvailabilityRequest.load(null), new AsyncCallback<InstructorAvailabilityModel>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught);
			}

			@Override
			public void onSuccess(final InstructorAvailabilityModel model) {
				if (panel.getElement().getFirstChildElement() != null) {
					iPattern = Hidden.wrap(panel.getElement().getFirstChildElement());
					model.setPattern(iPattern.getValue());
				
					addValueChangeHandler(new ValueChangeHandler<RoomInterface.RoomSharingModel>() {
						@Override
						public void onValueChange(ValueChangeEvent<RoomSharingModel> event) {
							iPattern.setValue(event.getValue().toString());
						}
					});
					iEditable = true;
				} else {
					String pattern = panel.getElement().getInnerText().trim();
					panel.getElement().setInnerText(null);
					model.setPattern(pattern);
					iEditable = false;
				}
				
				setModel(model);

				panel.add(InstructorAvailabilityWidget.this);
				panel.setVisible(true);				
			}
		});
	}
	
	public static class InstructorAvailabilityRequest implements GwtRpcRequest<InstructorAvailabilityModel> {
		private String iInstructorId;
		
		public InstructorAvailabilityRequest() {}
		
		public String getInstructorId() { return iInstructorId; }
		public void setInstructorId(String instructorId) { iInstructorId = instructorId; }

		public static InstructorAvailabilityRequest load(String instructorId) {
			InstructorAvailabilityRequest request = new InstructorAvailabilityRequest();
			request.setInstructorId(instructorId);
			return request;
		}
		
		@Override
		public String toString() {
			return iInstructorId == null ? "NULL" : iInstructorId;
		}
	};
	
	public static class InstructorAvailabilityModel extends RoomInterface.RoomSharingModel {
		public char id2char(Long id) {
			if (id == null) return '2';
			switch (id.intValue()) {
			case -1: return 'R';
			case -2: return '0';
			case -3: return '1';
			case -4: return '2';
			case -5: return '3';
			case -6: return '4';
			case -7: return 'P';
			default: return '2';
			}
		}
		
		public Long char2id(char ch) {
			switch (ch) {
			case 'R': return -1l;
			case '0': return -2l;
			case '1': return -3l;
			case '2': return -4l;
			case '3': return -5l;
			case '4': return -6l;
			case 'P': return -7l;
			default: return -4l;
			}
		}
		
		public String getPattern() {
			String pattern = "";
			for (int d = 0; d < 7; d++)
				for (int s = 0; s < 288; s++) {
					RoomSharingOption option = getOption(d, s);
					pattern += id2char(option == null ? null : option.getId());
				}
			return pattern;
		}
		public void setPattern(String pattern) {
			if (pattern.length() <= 336) {
				boolean req = pattern.indexOf('R') >= 0;
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s++) {
						char ch = '2';
						try {
							ch = pattern.charAt(48 * d + s / 6);
						} catch (IndexOutOfBoundsException e) {}
						setOption(d, s, char2id(req ? ch == 'R' ? '2' : 'P' : ch));
					}
			} else {
				for (int d = 0; d < 7; d++)
					for (int s = 0; s < 288; s++) {
						char ch = '2';
						try {
							ch = pattern.charAt(288 * d + s);
						} catch (IndexOutOfBoundsException e) {}
						setOption(d, s, char2id(ch));
					}
			}
		}
		
		@Override
		public String toString() {
			return getPattern();
		}
	}
}
