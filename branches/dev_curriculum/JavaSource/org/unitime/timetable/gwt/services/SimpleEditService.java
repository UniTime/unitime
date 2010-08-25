package org.unitime.timetable.gwt.services;

import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("simpleEditService")
public interface SimpleEditService extends RemoteService {
	public SimpleEditInterface load(SimpleEditInterface.Type type) throws SimpleEditException;
	public SimpleEditInterface save(SimpleEditInterface data) throws SimpleEditException;
}
