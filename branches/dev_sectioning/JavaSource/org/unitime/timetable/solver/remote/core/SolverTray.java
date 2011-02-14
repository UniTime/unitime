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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

/**
 * @author Tomas Muller
 */
public class SolverTray {
	private static SolverTray sInstance = null;
	
	public static SolverTray getInstance() { return sInstance; }
	public static boolean isInitialized() { return (sInstance!=null); }
	
	public static void init() {
		try {
			sInstance = new SolverTray();
		} catch (Exception e) {
			System.err.println("Unable to initialize solver tray -- "+e.getMessage());
		}
	}
	
	
	private Object iTray = null;
	private Object iTrayIcon = null;
	private JPopupMenu iMenu = null;
	private String iStatus = "starting";
	private String iLogFile = null;
	private JMenuItem iLogFileMenu = null;
	private Class iSystemTrayClass = null;
	private Class iTrayIconClass = null;
	
	public static Icon sSolverIcon = null;
	public static Icon sSolverDiscIcon = null;
	public static Icon sSolverPauseIcon = null;
	public static Icon sSolverRunIcon = null;
	public static Icon sSolverStopIcon = null;
	
	private SolverTray() throws Exception {
		iSystemTrayClass = Class.forName("org.jdesktop.jdic.tray.SystemTray");
		iTrayIconClass = Class.forName("org.jdesktop.jdic.tray.TrayIcon");
		iTray = iSystemTrayClass.getMethod("getDefaultSystemTray",new Class[]{}).invoke(null, new Object[]{});
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        
        sSolverIcon = new ImageIcon(StartupMinimal.class.getClassLoader().getResource("images/solver.gif"));
        sSolverDiscIcon = new ImageIcon(StartupMinimal.class.getClassLoader().getResource("images/solver-disc.gif"));
        sSolverPauseIcon = new ImageIcon(StartupMinimal.class.getClassLoader().getResource("images/solver-pause.gif"));
        sSolverRunIcon = new ImageIcon(StartupMinimal.class.getClassLoader().getResource("images/solver-run.gif"));
        sSolverStopIcon = new ImageIcon(StartupMinimal.class.getClassLoader().getResource("images/solver-stop.gif"));
        
        iMenu = new JPopupMenu("A Menu");
        
        iLogFileMenu = new JMenuItem("Log File");
        iLogFileMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	try {
            		Runtime.getRuntime().exec("\""+iLogFile+"\"");
            	} catch (IOException x) {
            		try {
            			Runtime.getRuntime().exec("notepad \""+iLogFile+"\"");
            		} catch (IOException y) {
            			y.printStackTrace();
            		}
            	}
            }
        });
        iLogFileMenu.setEnabled(false);
        iMenu.add(iLogFileMenu);
        
        JMenuItem menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	System.exit(0);
            }
        });
        iMenu.add(menuItem);
        
        iTrayIcon = iTrayIconClass.
        	getConstructor(new Class[] {Icon.class, String.class, JPopupMenu.class}).
        	newInstance(new Object[] {sSolverIcon, iStatus, iMenu});

        iTrayIconClass.getMethod("setIconAutoSize", new Class[] {boolean.class}).invoke(iTrayIcon,new Object[]{Boolean.TRUE});
        
        iTrayIconClass.getMethod("addActionListener", new Class[] {ActionListener.class}).invoke(iTrayIcon, new Object[] { 
        		new ActionListener() {
            		public void actionPerformed(ActionEvent e) {
            			JOptionPane.showMessageDialog(null, 
            					"Status: "+iStatus, "Purdue University Timetabling Solver", JOptionPane.INFORMATION_MESSAGE);
            		}
        	}});
        
        
        iSystemTrayClass.getMethod("addTrayIcon", new Class[]{iTrayIconClass}).invoke(iTray, new Object[]{iTrayIcon});
	}
	
	public void setStatus(String status, Icon icon) {
		try {
			iStatus = status;
			iTrayIconClass.getMethod("setCaption", new Class[] {String.class}).invoke(iTrayIcon,new Object[]{iStatus});
			if (icon!=null)
				iTrayIconClass.getMethod("setIcon", new Class[] {Icon.class}).invoke(iTrayIcon,new Object[]{icon});
		} catch (Exception e) {
			System.err.println("Unable to change tray icon status -- "+e.getMessage());
		}
	}
	public void setLogFile(String logFile) {
        iLogFile = logFile;
        iLogFileMenu.setEnabled(iLogFile!=null);
	}
	public JPopupMenu getMenu() { return iMenu; }
}
