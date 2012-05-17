/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.command.generator;

import java.util.List;

import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.user.client.rpc.impl.RemoteServiceProxy;
import com.google.gwt.user.rebind.rpc.ProxyCreator;

public class GwtRpcProxyCreator extends ProxyCreator {
	private GeneratorContext iGeneratorContext;

	public GwtRpcProxyCreator(JClassType serviceIntf, GeneratorContext ctx) {
		super(serviceIntf);
		iGeneratorContext = ctx;
	}
	
	@Override
    protected Class<? extends RemoteServiceProxy> getProxySupertype() {
		try {
			PropertyOracle configurationProperties = iGeneratorContext.getPropertyOracle();
			List<String> values = configurationProperties.getConfigurationProperty("org.unitime.timetable.gwt.command.interceptor.class").getValues();
			return (Class<? extends RemoteServiceProxy>)Class.forName(values.get(0));
			// return (Class<? extends RemoteServiceProxy>)Class.forName("org.unitime.timetable.gwt.client.test.GwtRpcDebugProxy");
		} catch (Exception e) {
			return super.getProxySupertype();
		}
	}
}
