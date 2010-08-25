package org.unitime.timetable.gwt.services;

import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SimpleEditServiceAsync {
	public void load(SimpleEditInterface.Type type, AsyncCallback<SimpleEditInterface> callback) throws SimpleEditException;
	public void save(SimpleEditInterface data, AsyncCallback<SimpleEditInterface> callback) throws SimpleEditException;
}
