/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.remote.core;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * @author Tomas Muller
 */
public class PingTest {
	private static int sPort = 1205;

	public static class Listener extends Thread {
		private ServerSocket iSocket = null;
		public void run() {
			try {
				setName("Listener");
				iSocket = new ServerSocket(sPort);
				System.out.println("Listener started at port "+sPort+".");
				while (true) {
					(new PingServer(iSocket.accept())).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
		        try {
		            if (iSocket!=null)
		                iSocket.close();
		        } catch (Exception e) { }
		        iSocket = null;
			}
		}
		
	    protected void finalize() throws Throwable {
	        try {
	            if (iSocket!=null)
	                iSocket.close();
	        }
	        catch (Exception e) { }
	        iSocket = null;
	        super.finalize();
	    }
	}
	
	public static class PingServer extends Thread {
		Socket iSocket = null;
		public PingServer(Socket socket) { iSocket = socket; }
		public void run() {
			try {
				setName("PingServer");
				System.out.println("Ping server created.");
				while (true) {
					Object command = RemoteIo.readObject(iSocket);
					if ("ping".equals(command)) {
						RemoteIo.writeObject(iSocket,"ack");
					} else break;
				}
				iSocket.close();
		        iSocket = null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
		        try {
		            if (iSocket!=null)
		                iSocket.close();
		        } catch (Exception e) { }
		        iSocket = null;
			}
		}
		
	    protected void finalize() throws Throwable {
	        try {
	            if (iSocket!=null)
	                iSocket.close();
	        }
	        catch (Exception e) { }
	        iSocket = null;
	        super.finalize();
	    }
	}
	
	public static class PingClient extends Thread {
	    Socket socket = null;
	    
		public void run() {
			try {
				setName("PingClient");
				socket = new Socket("localhost",sPort);
				System.out.println("Client connected.");
				for (int i=0;i<25;i++) {
					long t0 = System.currentTimeMillis();
					RemoteIo.writeObject(socket,"ping");
					RemoteIo.readObject(socket);
					long t1 = System.currentTimeMillis();
					System.out.println("Ping received in "+(t1-t0)+" ms.");
					sleep(250);
				}
				RemoteIo.writeObject(socket, "quit");
				socket.close();
		        socket = null;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
		        try {
		            if (socket!=null)
		            	socket.close();
		        } catch (Exception e) { }
		        socket = null;
			}
		}
		
	    protected void finalize() throws Throwable {
	        try {
	            if (socket!=null)
	                socket.close();
	        }
	        catch (Exception e) { }
	        socket = null;
	        super.finalize();
	    }
	}
	
	public static void main(String[] args) {
		try {
			if ("server".equals(args[0])) {
				Listener listener = new Listener();
				listener.start();
				listener.join();
			}
			
			if ("client".equals(args[0])) {
				PingClient clt = new PingClient();
				clt.start();
				clt.join();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
