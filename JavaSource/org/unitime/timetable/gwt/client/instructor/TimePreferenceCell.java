package org.unitime.timetable.gwt.client.instructor;

import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;

public class TimePreferenceCell extends P implements UniTimeTable.HasRefresh {
	private TeachingRequestsPagePropertiesResponse iProperties;
	private String iPattern = null;
	private List<PreferenceInfo> iPreferences = null;
	private static InstructorAvailabilityWidget sAvailability = new InstructorAvailabilityWidget(); 
	
	public TimePreferenceCell(TeachingRequestsPagePropertiesResponse properties) {
		super("preferences");
		iProperties = properties;
		addMouseOverHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (iPattern != null) {
					iProperties.getInstructorAvailabilityModel().setPattern(iPattern);
					sAvailability.setModel(iProperties.getInstructorAvailabilityModel());
					GwtHint.showHint(getElement(), sAvailability);
				}
			}
		});
		addMouseOutHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				GwtHint.hideHint();
			}
		});
	}
	
	public TimePreferenceCell(TeachingRequestsPagePropertiesResponse properties, InstructorInfo instructor) {
		this(properties);
		setValue(instructor);
	}
	
	public void setValue(InstructorInfo instructor) {
		iPattern = instructor.getAvailability();
		iPreferences = instructor.getTimePreferences();
		refresh();
	}
	
	@Override
	public void refresh() {
		clear();
		RoomCookie cookie = RoomCookie.getInstance();
		if (iPattern != null && !iPattern.isEmpty() && !cookie.isGridAsText()) {
			final Image availability = new Image(GWT.getHostPageBaseURL() + "pattern?pref=" + iPattern + "&v=" + (cookie.areRoomsHorizontal() ? "0" : "1") + (cookie.hasMode() ? "&s=" + cookie.getMode() : ""));
			availability.setStyleName("grid");
			add(availability);
		} else {
			for (PreferenceInfo p: iPreferences) {
				P prf = new P("prf");
				prf.setText(p.getOwnerName());
				PreferenceInterface preference = iProperties.getPreference(p.getPreference());
				if (preference != null) {
					prf.getElement().getStyle().setColor(preference.getColor());
					prf.setTitle(preference.getName() + " " + p.getOwnerName());
				}
				add(prf);
			}
		}
	}
}
