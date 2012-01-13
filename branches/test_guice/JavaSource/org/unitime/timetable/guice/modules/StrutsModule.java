package org.unitime.timetable.guice.modules;

import org.unitime.timetable.action.ClassEditAction;
import org.unitime.timetable.guice.struts.GuiceRequestProcessor;

import com.google.inject.AbstractModule;

public class StrutsModule extends AbstractModule {

	@Override
	protected void configure() {
		requestStaticInjection(GuiceRequestProcessor.class);
		bind(ClassEditAction.class);
	}

}
