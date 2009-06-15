package org.acoveo.callcenter.iconstore;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.acoveo.tools.Filesystem;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class IconStore extends AbstractUIPlugin {

	// General icons
	public static final String COLLAPSE_ICON_CONSTANT = "icons.eclipse.collapse.gif";
	public static final String COLLAPSED_ICON_CONSTANT = "icons.eclipse.collapsed.gif";
	public static final String EXPAND_ICON_CONSTANT = "icons.eclipse.expand.gif";
	public static final String EXPANDED_ICON_CONSTANT = "icons.eclipse.expanded.gif";
	
	public static final String REMOVE_PROPERTY_ROW_CONSTANT = "icons.tango-icon-theme.16x16.actions.edit-clear.png";
	
	public static final String PHONE_SETTINGS = "icons.wengoicons.actions.config.png";
	public static final String PHONE_RUN_COMMAND = "icons.tango-icon-theme.16x16.devices.audio-input-microphone.png";
	
	public static final String PHONE_CALL = "icons.acoveo.16x16.phone_call.png";
	public static final String PHONE_RINGING = "icons.acoveo.16x16.phone_ringing.png";
	public static final String PHONE_HANGUP = "icons.acoveo.16x16.phone_hangup.png";
	public static final String PHONE_TRANSFER = "icons.tango-icon-theme.16x16.actions.edit-redo.png";
	public static final String PHONE_PICKUP = "icons.tango-icon-theme.16x16.actions.go-jump.png";
	public static final String PHONE_WRAPUP = "icons.tango-icon-theme.16x16.actions.appointment-new.png";
	public static final String PHONE_WRAPUP_OFF = "icons.acoveo.16x16.wrapup_off.png";
	public static final String PHONE_WRAPUP_EXTEND = "icons.acoveo.16x16.wrapup_extend.png";
	public static final String PHONE_BACKUP = "icons.lulacons.user-plain-grey.png";
	public static final String PHONE_BACKUP_OFF = "icons.lulacons.user-plain-grey-crossed.png";
	public static final String PHONE_VOICEMAIL = "icons.wengoicons.actions.chat.png";
	public static final String PHONE_STATUS_NOT_REGISTERED = "icons.tango-icon-theme.16x16.emblems.emblem-important.png";
	public static final String PHONE_STATUS_REGISTER_FAILED = "icons.tango-icon-theme.16x16.emblems.emblem-unreadable.png";
	public static final String PHONE_STATUS_REGISTER_WARNING = "icons.acoveo.16x16.face-call-agent-grey.png";
	public static final String PHONE_STATUS_REGISTER_SUCCESS = "icons.tango-icon-theme.16x16.emotes.face-call-agent.png";
	public static final String PHONE_CLEAR_DESTINATION = "icons.tango-icon-theme.16x16.actions.edit-clear.png";
	public static final String PHONE_HOLD = "icons.wengoicons.actions.hold-phone.png";
	public static final String PHONE_UNHOLD = "icons.wengoicons.actions.resume-phone.png";
	public static final String PHONE_CONFERENCE = "icons.acoveo.16x16.conference.png";
	public static final String PHONE_CONFERENCE_OFF = "icons.acoveo.16x16.conference_off.png";
	
	public static final String PHONE_CALL_HISTORY = "icons.org-eclipse-mylyn.org-eclipse-mylyn-tasks-ui.icons.eview16.perspective-planning.png";
	public static final String PHONE_HIDE_CALL_HISTORY = "icons.wengoicons.contact.landline.png";
	public static final String PHONE_HISTORY_SHOW_TIME = "icons.lulacons.date-option-add.png";
	public static final String PHONE_HISTORY_HIDE_TIME = "icons.lulacons.date-option-remove.png";
	public static final String PHONE_HISTORY_TIME = "icons.lulacons.date-plain.png";
	public static final String PHONE_HISTORY_SORT_UP = "icons.tango-icon-theme.16x16.actions.go-up.png";
	public static final String PHONE_HISTORY_SORT_DOWN = "icons.tango-icon-theme.16x16.actions.go-down.png";
	public static final String PHONE_HISTORY_FILTER_ON = "icons.wengoicons.actions.change-profile.png";
	public static final String PHONE_HISTORY_FILTER_OFF = "icons.wengoicons.actions.log-off.png";
	public static final String PHONE_HISTORY_CLEAR = "icons.wengoicons.actions.history-clear.png";
	
	public static final String PHONE_CALL_MISSED = "icons.wengoicons.history.call_missed.png";
	public static final String PHONE_CALL_OUTGOING = "icons.wengoicons.history.call_outgoing.png";
	public static final String PHONE_CALL_INCOMING = "icons.wengoicons.history.call_incoming.png";
	
	public static final String QUEUE_TOTAL_CALLS = "icons.wengoicons.actions.edit-contact.png";
	public static final String QUEUE_HANDLED_CALLS = "icons.wengoicons.actions.show-available-contacts.png";
	public static final String QUEUE_ABANDONED_CALLS = "icons.wengoicons.actions.hangup-phone.png";
	public static final String QUEUE_EXITWITHTIMEOUT_CALLS = "icons.wengoicons.actions.history-clear.png";
	
	public static final String SOFT_PHONE_VIEW = "icons.gnome-icon-theme.stock.generic.stock_landline-phone.png";
	
	public static final String NOTIFICATION_CLOSE = "icons.org-eclipse-mylyn.org-eclipse-mylyn-tasks-ui.icons.eview16.notification-close.gif";
	public static final String NOTIFICATION_CLOSE_ACTIVE = "icons.org-eclipse-mylyn.org-eclipse-mylyn-tasks-ui.icons.eview16.notification-close-active.gif";

	public static final String FORWARD_CALL_CONFIG = "icons.wengoicons.profilebar.credit.call_forward.png";
	public static final String FORWARD_CALL_CONFIG_ACTIVE = "icons.tango-icon-theme.16x16.actions.edit-redo.png";
	
	
	// Colours
	public static  List<Color> colourList = new LinkedList<Color>();
	public static  Color LOGGED_OFF_COLOUR;
	public static Color LOGGED_ON_COLOUR;
	public static Color ACTIVE_CALL_COLOUR;
	public static Color ACTIVE_CALL_CURR_GROUP_COLOUR;
	public static Color RINGING_COLOUR;
	public static Color PAUSE_COLOUR;
	public static Color WRAPUP_COLOUR;
	public static Color BACKUP_COLOUR;
	public static Color AUTOPAUSE_COLOUR;
	public static Color NO_QUEUE_COLOUR;
	public static Color AGENT_COLUMN_COLOUR;
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.callcenter.iconStore";

	// The shared instance
	private static IconStore plugin;

	/**
	 * The constructor
	 */
	public IconStore() {
	}

	protected void createColours() {
		destroyColours();
		Display display = null;
		// If we are in the UI Thread use that
		if (Display.getCurrent() != null) {
			display = Display.getCurrent();
		}else if (PlatformUI.isWorkbenchRunning()) {
			display = PlatformUI.getWorkbench().getDisplay();
		}
		if (display == null) {
			// Invalid thread access if it is not the UI Thread
			// and the workbench is not created.
			throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
		}
		LOGGED_OFF_COLOUR = new Color(display, 253, 129, 129);
		colourList.add(LOGGED_OFF_COLOUR);
		LOGGED_ON_COLOUR = new Color(display, 255, 187, 98);
		colourList.add(LOGGED_ON_COLOUR);
		ACTIVE_CALL_COLOUR = new Color(display, 123, 255, 116);
		colourList.add(ACTIVE_CALL_COLOUR);
		ACTIVE_CALL_CURR_GROUP_COLOUR = new Color(display, 70, 194, 63);
		colourList.add(ACTIVE_CALL_CURR_GROUP_COLOUR);
		RINGING_COLOUR = new Color(display, 151, 195, 255);
		colourList.add(RINGING_COLOUR);
		PAUSE_COLOUR = new Color(display, 238, 238, 238);
		colourList.add(PAUSE_COLOUR);
		WRAPUP_COLOUR = new Color(display, 177, 135, 53);
		colourList.add(WRAPUP_COLOUR);
		BACKUP_COLOUR = new Color(display, 190, 96, 244);
		colourList.add(BACKUP_COLOUR);
		AUTOPAUSE_COLOUR = new Color(display, 254, 0, 0);
		colourList.add(AUTOPAUSE_COLOUR);
		NO_QUEUE_COLOUR = new Color(display, 189, 189, 189);
		colourList.add(BACKUP_COLOUR);
		AGENT_COLUMN_COLOUR = new Color(display, 200, 200, 200);
		colourList.add(AGENT_COLUMN_COLOUR);
	}
	
	protected void destroyColours() {
		for(Color colour : colourList) {
			colour.dispose();
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		createColours();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		destroyColours();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static IconStore getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		loadIconsRecursively(registry, "icons/tango-icon-theme/16x16", "icons.tango-icon-theme.16x16",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/images_industrial", "icons.images_industrial",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/eclipse", "icons.eclipse",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/acoveo", "icons.acoveo",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/lulacons", "icons.lulacons",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/gnome-icon-theme", "icons.gnome-icon-theme",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/org-eclipse-mylyn", "icons.org-eclipse-mylyn",
				new GraphicsFileFilter());
		loadIconsRecursively(registry, "icons/wengoicons", "icons.wengoicons",
				new GraphicsFileFilter());
	}

	protected void loadIconsRecursively(ImageRegistry registry, String resourcePath, String iconKeyPrefix,
			FilenameFilter filter) {
		try {
			URL directoryURL = FileLocator.toFileURL(FileLocator.find(getBundle(), new Path(resourcePath), null));
			File directory = new File(directoryURL.getFile());
			// In order to generate a valid icon key we need the icon path,
			// relative to resourcePath, therefore we need to strip it from 
			// the actual icon path later on
			String unwantedDirectoryPrefix = directory.getAbsolutePath();
			if (directory.isDirectory()) {
				// Find all icons below resourcePath
				List<File> icons = Filesystem.findFilesRecursive(directory, filter);
				// Compute the icon key for each icon and insert it into the registry
				for (File iconPath : icons) {
					String iconKey = computeIconKey(iconKeyPrefix, iconPath.getAbsolutePath(), unwantedDirectoryPrefix);
					ImageDescriptor descriptor = ImageDescriptor.createFromURL(iconPath.toURI().toURL());
					if (descriptor != null) {
						registry.put(iconKey, descriptor);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected String computeIconKey(String iconKeyPrefix, String iconAbsolutePath, String unwantedAbsolutePathPrefix) {
		if (iconAbsolutePath.regionMatches(0, unwantedAbsolutePathPrefix, 0, unwantedAbsolutePathPrefix.length())) {
			// Strip the prefix from the path to compute the icon key
			String relativeIconPath = iconAbsolutePath.substring(unwantedAbsolutePathPrefix.length(), iconAbsolutePath
					.length());
			File relativeIconPathFile = new File(relativeIconPath);

			// Compute the icon key
			StringBuilder iconKey = new StringBuilder(iconKeyPrefix);
			Deque<String> pathStack = new LinkedList<String>();
			do {
				pathStack.addFirst(relativeIconPathFile.getName());
				relativeIconPathFile = relativeIconPathFile.getParentFile();
			} while (relativeIconPathFile != null);
			for (String pathComponent : pathStack) {
				if (!pathComponent.equals("")) {
					iconKey.append('.');
					iconKey.append(pathComponent);
				}
			}
			return iconKey.toString();
		}
		// TODO Throw an appropriate exception
		return null;
	}

	class GraphicsFileFilter implements FilenameFilter {
		public boolean accept(File dir, String s) {
			if (s.endsWith(".png") || s.endsWith(".gif"))
				return true;
			return false;
		}
	};
}
