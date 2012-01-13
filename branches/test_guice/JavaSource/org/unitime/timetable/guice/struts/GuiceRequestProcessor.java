package org.unitime.timetable.guice.struts;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.RequestUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import fr.improve.struts.taglib.layout.workflow.LayoutRequestProcessor;

public class GuiceRequestProcessor extends LayoutRequestProcessor {
	
	private static @Inject Injector injector;
	
	public GuiceRequestProcessor() {
		super();
	}
	
	// If method injection on forms would suffice:
	@Override
	// Method injection on forms
	protected ActionForm processActionForm(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) {
		ActionForm form = super.processActionForm(request, response, mapping);
		if (form != null) injector.injectMembers(form);
		return form;
	}
	
	/*// If method injection on actions would suffice:
	@Override
	protected Action processActionCreate(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) throws IOException {
		Action action = super.processActionCreate(request, response, mapping);
		if (action != null) injector.injectMembers(action);
		return action;
	}
	*/
	
	// Full integration on actions
	// Most of the method is the same as in RequestProcessor.processActionCreate, except of the RequestUtils.applicationInstance(className)
    protected Action processActionCreate(HttpServletRequest request, HttpServletResponse response, ActionMapping mapping) throws IOException {
    	// Acquire the Action instance we will be using (if there is one)
    	String className = mapping.getType();

        if (log.isDebugEnabled()) {
            log.debug(" Looking for Action instance for class " + className);
        }

        // If there were a mapping property indicating whether
        // an Action were a singleton or not ([true]),
        // could we just instantiate and return a new instance here?
        Action instance;

        synchronized (actions) {
            // Return any existing Action instance of this class
            instance = (Action) actions.get(className);

            if (instance != null) {
                if (log.isTraceEnabled()) {
                    log.trace("  Returning existing Action instance");
                }

                return (instance);
            }

            // Create and return a new Action instance
            if (log.isTraceEnabled()) {
                log.trace("  Creating new Action instance");
            }

            try {
            	// Using Guice instant of: instance = (Action) RequestUtils.applicationInstance(className);
            	instance = (Action)injector.getInstance(RequestUtils.applicationClass(className));

            	// Maybe we should propagate this exception
            	// instead of returning null.
            } catch (Exception e) {
                log.error(getInternal().getMessage("actionCreate", mapping.getPath()), e);

                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    getInternal().getMessage("actionCreate", mapping.getPath()));

                return (null);
            }

            actions.put(className, instance);

            if (instance.getServlet() == null) {
                instance.setServlet(this.servlet);
            }
        }

        return (instance);
    }
}