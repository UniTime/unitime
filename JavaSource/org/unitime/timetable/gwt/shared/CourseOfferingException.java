package org.unitime.timetable.gwt.shared;

import org.unitime.timetable.gwt.command.client.GwtRpcException;

import com.google.gwt.user.client.rpc.IsSerializable;

public class CourseOfferingException extends GwtRpcException implements IsSerializable {
	private static final long serialVersionUID = -4954066614042163548L;

	public CourseOfferingException() {
		super();
	}
	
	public CourseOfferingException(String message) {
		super(message);
	}
}
