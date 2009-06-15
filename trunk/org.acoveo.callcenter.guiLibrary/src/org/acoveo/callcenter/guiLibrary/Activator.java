package org.acoveo.callcenter.guiLibrary;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acoveo.tools.NetworkTools;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.acoveo.callcenter.guiLibrary"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	// The logger for this plugin
	private static Logger logger;
	
	/**
	 * The constructor
	 */
	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		logger = Logger.getLogger(PLUGIN_ID);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/** Returns the shared logger
	 * 
	 * @return The logger
	 */
	public static Logger getLogger() {
		return logger;
	}
	
}
