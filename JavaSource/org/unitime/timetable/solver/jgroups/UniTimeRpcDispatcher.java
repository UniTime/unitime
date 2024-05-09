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
package org.unitime.timetable.solver.jgroups;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.jgroups.Address;
import org.jgroups.BytesMessage;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestCorrelator;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.stack.Protocol;
import org.jgroups.util.RspList;

/**
 * A special version of the {@link RpcDispatcher} to avoid an endless loop when
 * a message fails to be parsed (see JGRP-2790 issue).
 * 
 * All RPC messages are sent as {@link BytesMessage} to ensure that the message
 * is serialized when {@link Message#getPayload()} {@link Message#setPayload(Object)}
 * methods are called.
 * 
 * Moreover, a custom {@link RpcRequestCorrelator#handleResponse(Message, Header)}
 * and {@link RpcRequestCorrelator#sendReply(Message, long, Object, boolean)} will also ensure
 * that any serialization errors that happen on the other side are passed back to the caller.
 * 
 * @author Tomas Muller
 */
public class UniTimeRpcDispatcher extends RpcDispatcher {
	// register the custom request correlator
	private static final short PROTOCOL_CUSTOM_CORRELATOR = 1234;
	static {
		ClassConfigurator.addProtocol(PROTOCOL_CUSTOM_CORRELATOR, RpcRequestCorrelator.class);
	}
	
	public UniTimeRpcDispatcher(JChannel channel, Object server_obj) {
		super(channel, server_obj);
		setCorrelator(new RpcRequestCorrelator(prot_adapter, req_handler, local_addr));
	}

	@Override
	public <T> RspList<T> callRemoteMethods(Collection<Address> dests, MethodCall method_call, RequestOptions opts) throws Exception {
		if (dests != null && dests.isEmpty()) { // don't send if dest list is empty
			log.trace("destination list of %s() is empty: no need to send message", method_call.getMethodName());
			return empty_rsplist;
		}
		// serialize immediately, not during transport
		Message msg=new BytesMessage(null, method_call);
		RspList<T> retval=super.castMessage(dests, msg, opts);
		if(log.isTraceEnabled())
			log.trace("dests=%s, method_call=%s, options=%s, responses: %s", dests, method_call, opts, retval);
		return retval;
	}
	
	@Override
    public <T> CompletableFuture<RspList<T>> callRemoteMethodsWithFuture(Collection<Address> dests, MethodCall call, RequestOptions options) throws Exception {
		if (dests != null && dests.isEmpty()) { // don't send if dest list is empty
			log.trace("destination list of %s() is empty: no need to send message", call.getMethodName());
			return CompletableFuture.completedFuture(empty_rsplist);
		}
		// serialize immediately, not during transport
		Message msg=new BytesMessage(null, call);
		CompletableFuture<RspList<T>> retval=super.castMessageWithFuture(dests, msg, options);
		if(log.isTraceEnabled())
			log.trace("dests=%s, method_call=%s, options=%s", dests, call, options);
		return retval;
	}
	
	@Override
	public <T> T callRemoteMethod(Address dest, MethodCall call, RequestOptions options) throws Exception {
		// serialize immediately, not during transport
		// also pass serialization errors back to the client
        Message req=new BytesMessage(dest, call);
        T retval=super.sendMessage(req, options);
        if(log.isTraceEnabled())
            log.trace("dest=%s, method_call=%s, options=%s, retval: %s", dest, call, options, retval);
        if (retval != null && retval instanceof InvocationTargetException)
        	throw (InvocationTargetException)retval;
        return retval;
    }
	
	@Override
	public <T> CompletableFuture<T> callRemoteMethodWithFuture(Address dest, MethodCall call, RequestOptions opts) throws Exception {
		if(log.isTraceEnabled())
			log.trace("dest=%s, method_call=%s, options=%s", dest, call, opts);
		// serialize immediately, not during transport
        Message msg=new BytesMessage(dest, call);
        return super.sendMessageWithFuture(msg, opts);
    }

	/**
	 * Custom request correlator that checks for serialization errors during
	 * {@link Message#getPayload()} {@link Message#setPayload(Object)} calls. When there is an error,
	 * it is passed along instead of the original message.
	 */
	public static class RpcRequestCorrelator extends RequestCorrelator {
		public RpcRequestCorrelator(Protocol down_prot, RequestHandler handler, Address local_addr) {
			super(down_prot, handler, local_addr);
		}
		
		@Override
		protected void handleResponse(Message rsp, Header hdr) {
			Request<?> req=requests.get(hdr.req_id);
	        if (req != null) {
	        	boolean threw_exception = false; 
	        	Object retval = null;
	        	try {
	        		retval = rsp.getPayload();
	        	} catch (Exception e) {
		        	// message cannot be read > return the serialization error as the message
		        	// this is to ensure that the serialization error gets returned back to the client
					retval = wrap_exceptions ? new InvocationTargetException(e) : e;
					threw_exception = true;
				}
	            req.receiveResponse(retval, rsp.getSrc(), hdr.type == Header.EXC_RSP || threw_exception);
	        }
		}
		
		@Override
		protected void sendReply(final Message req, final long req_id, Object reply, boolean is_exception) {
			Message rsp = makeReply(req).setFlag(req.getFlags(false), false, true);
			try {
		        rsp.setPayload(reply);
			} catch (Exception e) {
	        	// message cannot be written > send the serialization error as the message
	        	// this is to ensure that the serialization error gets returned back to the client
				rsp.setPayload(wrap_exceptions ? new InvocationTargetException(e) : e);
				is_exception = true;
			}
			rsp.clearFlag(Message.Flag.RSVP); // JGRP-1940
	        sendResponse(rsp, req_id, is_exception);
	    }
	}
}
