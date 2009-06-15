package org.acoveo.callcenter.sipclient.preferences;

import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

@Deprecated
public class ShortcutButtonsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	public static final String ID = "org.acoveo.callcenter.sipclient.preferences.ShortcutButtonsPreferencePage"; //$NON-NLS-1$
	public static final int MAX_SHORTCUT_BUTTONS = 30;
	public static final int SHORTCUT_BUTTONS_COLUMNS = 3;
	public static final int MAX_SHORTCUT_BUTTONS_DIALPAGE = 10;
	private static final String TPL_GROUP_LABEL = Messages.ShortcutButtonsPreferencePage_1;
	
	// input field
	private static final String TPL_PREF_CAPTION = "Shortcut.Caption.%d"; //$NON-NLS-1$
	private static final String CAPTION_LABEL = Messages.ShortcutButtonsPreferencePage_3;
	
	// input field
	private static final String TPL_PREF_NUMBER = "Shortcut.Number.%d"; //$NON-NLS-1$
	private static final String NUMBER_LABEL = Messages.ShortcutButtonsPreferencePage_5;
	
	// check box
	private static final String TPL_PREF_ACTIVE = "Shortcut.Active.%d"; //$NON-NLS-1$
	private static final String ACTIVE_LABEL = Messages.ShortcutButtonsPreferencePage_7;
	
	// check box
	private static final String TPL_PREF_FORWARD = "Shortcut.Forward.%d"; //$NON-NLS-1$
	private static final String FORWARD_LABEL = Messages.ShortcutButtonsPreferencePage_9;
	
	// check box for suffix dialing
	private static final String TPL_PREF_SUFFIX_DIALING = "Shortcut.SuffixDialing.%d"; //$NON-NLS-1$
	private static final String SUFFIX_DIALING_LABEL = Messages.ShortcutButtonsPreferencePage_11;
	

	public ShortcutButtonsPreferencePage() {
		super(GRID);
	}
	
	public static boolean isActive(IPreferenceStore store, int pos) {
		return store.getBoolean(String.format(TPL_PREF_ACTIVE, pos));
	}
	public static boolean isForwardCall(IPreferenceStore store, int pos) {
		return store.getBoolean(String.format(TPL_PREF_FORWARD, pos));
	}
	public static String getNumber(IPreferenceStore store, int pos) {
		return store.getString(String.format(TPL_PREF_NUMBER, pos));
	}
	public static String getCaption(IPreferenceStore store, int pos) {
		return store.getString(String.format(TPL_PREF_CAPTION, pos));
	}
	public static boolean isSuffixDialing(IPreferenceStore store, int pos) {
		return store.getBoolean(String.format(TPL_PREF_SUFFIX_DIALING, pos));
	}
	
	@Override
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		
		Group[] groups = new Group[MAX_SHORTCUT_BUTTONS];
		StringFieldEditor[] captions = new StringFieldEditor[MAX_SHORTCUT_BUTTONS];
		StringFieldEditor[] numbers = new StringFieldEditor[MAX_SHORTCUT_BUTTONS];
		BooleanFieldEditor[] actives = new BooleanFieldEditor[MAX_SHORTCUT_BUTTONS];
		BooleanFieldEditor[] types = new BooleanFieldEditor[MAX_SHORTCUT_BUTTONS];
		BooleanFieldEditor[] suffixDialings = new BooleanFieldEditor[MAX_SHORTCUT_BUTTONS];
		
		for(int i=0;i<MAX_SHORTCUT_BUTTONS;i++) {
			groups[i] = createNewGroup(parent, String.format(TPL_GROUP_LABEL, i+1));
			
			// Caption
			captions[i] = new StringFieldEditor(String.format(TPL_PREF_CAPTION, i),CAPTION_LABEL, groups[i]);
			captions[i].setEmptyStringAllowed(true);
			addField(captions[i]);
			
			// Number
			numbers[i] = new StringFieldEditor(String.format(TPL_PREF_NUMBER, i),NUMBER_LABEL, groups[i]);
			numbers[i].setEmptyStringAllowed(true);
			addField(numbers[i]);
			
			// Type
			types[i] = new BooleanFieldEditor(String.format(TPL_PREF_FORWARD, i), FORWARD_LABEL,BooleanFieldEditor.SEPARATE_LABEL, groups[i]);
			addField(types[i]);
			
			// Active
			actives[i] = new BooleanFieldEditor(String.format(TPL_PREF_ACTIVE, i), ACTIVE_LABEL,BooleanFieldEditor.SEPARATE_LABEL, groups[i]);
            addField(actives[i]);
            
            // Suffix dialing
            suffixDialings[i] = new BooleanFieldEditor(String.format(TPL_PREF_SUFFIX_DIALING, i), SUFFIX_DIALING_LABEL,BooleanFieldEditor.SEPARATE_LABEL, groups[i]);
            addField(suffixDialings[i]);
		}
	}
	
	private Group createNewGroup(Composite parent, String text) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(text);
		GridLayoutFactory.swtDefaults().equalWidth(false).numColumns(2).applyTo(group);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(2,1).applyTo(group);
		return group;
	}
	

	@Override
	public void init(IWorkbench workbench) {
		// Do nothing
	}

	/**
	 * Initializes the default preference values for this preference store.
	 * 
	 * @param store
	 */
	public static void initDefaults(IPreferenceStore store) {
		
		for(int i=0;i<MAX_SHORTCUT_BUTTONS;i++) {
			store.setDefault(String.format(TPL_PREF_CAPTION, i), ""); //$NON-NLS-1$
			store.setDefault(String.format(TPL_PREF_NUMBER, i), ""); //$NON-NLS-1$
			store.setDefault(String.format(TPL_PREF_ACTIVE, i), false);
			store.setDefault(String.format(TPL_PREF_FORWARD, i), false);
			store.setDefault(String.format(TPL_PREF_SUFFIX_DIALING, i), false);
		}
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * The soft phone preference page implementation of a
	 * <code>PreferencePage</code> method loads all the field editors with
	 * their default values.
	 */
	protected void performDefaults() {
		initDefaults(getPreferenceStore());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		if(!super.performOk()) {
			return false;
		}
		return true;
	}

}
