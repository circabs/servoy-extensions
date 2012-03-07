/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2010 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */
package com.servoy.extensions.plugins.dialog;

import java.awt.Dialog;
import java.awt.Window;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.servoy.j2db.Messages;
import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.IRuntimeWindow;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.scripting.IScriptable;

/**
 * Scritptable object for dialog plugin
 * @author jblok
 */
@ServoyDocumented(publicName = DialogPlugin.PLUGIN_NAME, scriptingName = "plugins." + DialogPlugin.PLUGIN_NAME)
public class DialogProvider implements IScriptable
{
	private final DialogPlugin plugin;

	public DialogProvider(DialogPlugin plugin)
	{
		this.plugin = plugin;
	}

	/**
	 * @deprecated Replaced by {@link #showWarningDialog(Object[])}.
	 */
	@Deprecated
	public String js_showDialog(String dialogTitle, String dialogMessage, Object buttonsText)//old one
	{
		return js_showWarningDialog(dialogTitle, dialogMessage, buttonsText);
	}

	@Deprecated
	public String js_showDialog(String dialogTitle, String dialogMessage)//old one
	{
		return js_showWarningDialog(dialogTitle, dialogMessage, null);
	}

	private String[] getButtonTexts(Object buttonsText)
	{
		if (buttonsText == null) return null;
		if (buttonsText instanceof String)
		{
			return new String[] { buttonsText.toString() };
		}
		else if (buttonsText.getClass().isArray())
		{
			String[] text = new String[((Object[])buttonsText).length];
			for (int i = 0; i < ((Object[])buttonsText).length; i++)
			{
				text[i] = (String)((Object[])buttonsText)[i];
			}
			return text;
		}
		else
		{
			return new String[] { buttonsText.toString() };
		}
	}

	/**
	 * @clonedesc js_showErrorDialog(String,String,Object)
	 *
	 * @sample
	 * //show dialog
	 * var thePressedButton = plugins.dialogs.showWarningDialog('Title', 'Value not allowed','OK');
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 * @param buttonsText
	 */
	public String js_showWarningDialog(String dialogTitle, String dialogMessage, Object buttonsText)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.WEB_CLIENT)
		{
			BrowserDialog.alert(plugin.getClientPluginAccess(), dialogTitle + "\\n" + dialogMessage);
			return (buttonsText != null ? getButtonTexts(buttonsText)[0] : "OK");
		}
		return js_showDialogEx(dialogTitle, dialogMessage, buttonsText, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * @clonedesc js_showErrorDialog(String,String,Object)
	 *
	 * @sampleas js_showWarningDialog(String,String,Object)
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 */
	public String js_showWarningDialog(String dialogTitle, String dialogMessage)
	{
		return js_showWarningDialog(dialogTitle, dialogMessage, null);
	}

	/**
	 * @clonedesc js_showErrorDialog(String,String,Object)
	 *
	 * @sample
	 * //show dialog
	 * var thePressedButton = plugins.dialogs.showInfoDialog('Title', 'Value not allowed','OK');
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 * @param buttonsText
	 */
	public String js_showInfoDialog(String dialogTitle, String dialogMessage, Object buttonsText)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.WEB_CLIENT)
		{
			BrowserDialog.alert(plugin.getClientPluginAccess(), dialogTitle + "\\n" + dialogMessage);
			return (buttonsText != null ? getButtonTexts(buttonsText)[0] : "OK");
		}
		return js_showDialogEx(dialogTitle, dialogMessage, buttonsText, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * @clonedesc js_showErrorDialog(String,String,Object)
	 *
	 * @sampleas js_showInfoDialog(String,String,Object)
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 */
	public String js_showInfoDialog(String dialogTitle, String dialogMessage)
	{
		return js_showInfoDialog(dialogTitle, dialogMessage, null);
	}

	/**
	 * Shows a message dialog with the specified title, message and a customizable set of buttons.
	 *
	 * @sample
	 * //show dialog
	 * var thePressedButton = plugins.dialogs.showErrorDialog('Title', 'Value not allowed','OK');
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 * @param buttonsText
	 */
	public String js_showErrorDialog(String dialogTitle, String dialogMessage, Object buttonsText)
	{
		if (plugin.getClientPluginAccess().getApplicationType() == IClientPluginAccess.WEB_CLIENT)
		{
			BrowserDialog.alert(plugin.getClientPluginAccess(), dialogTitle + "\\n" + dialogMessage);
			return (buttonsText != null ? getButtonTexts(buttonsText)[0] : "OK");
		}
		return js_showDialogEx(dialogTitle, dialogMessage, buttonsText, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * @clonedesc js_showErrorDialog(String,String,Object)
	 *
	 * @sampleas js_showErrorDialog(String,String,Object)
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 */
	public String js_showErrorDialog(String dialogTitle, String dialogMessage)
	{
		return js_showErrorDialog(dialogTitle, dialogMessage, null);
	}

	/**
	 * Shows a message dialog with the specified title, message and a customizable set of buttons.
	 *
	 * @sample
	 * //show dialog
	 * var thePressedButton = plugins.dialogs.showQuestionDialog('Title', 'Value not allowed','OK');
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 * @param buttonsText
	 */
	public String js_showQuestionDialog(String dialogTitle, String dialogMessage, Object buttonsText)
	{
		return js_showDialogEx(dialogTitle, dialogMessage, buttonsText, JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * @clonedesc js_showQuestionDialog(String,String,Object)
	 *
	 * @sampleas js_showQuestionDialog(String,String,Object)
	 *
	 * @param dialogTitle 
	 * @param dialogMessage 
	 */
	public String js_showQuestionDialog(String dialogTitle, String dialogMessage)
	{
		return js_showQuestionDialog(dialogTitle, dialogMessage, null);
	}

	private String js_showDialogEx(String dialogTitle, String dialogMessage, Object buttonsText, int type)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Can't use the dialog plugin in a none Swing thread/environment");
		}
		String title = Messages.getString("servoy.general.warning"); //$NON-NLS-1$
		if (dialogTitle != null)
		{
			title = Messages.getStringIfPrefix(dialogTitle);
		}
		String msg = Messages.getString("servoy.general.clickOk"); //$NON-NLS-1$
		if (dialogMessage != null) msg = Messages.getStringIfPrefix(dialogMessage);
		Vector<String> buttons = new Vector<String>();
		if (buttonsText != null)
		{
			for (String text : getButtonTexts(buttonsText))
			{
				if (text != null && !("".equals(text))) //$NON-NLS-1$
				{
					buttons.addElement(Messages.getStringIfPrefix(text));
				}
			}
		}
		String[] options = new String[buttons.size()];
		buttons.copyInto(options);
		if (options.length == 0) options = new String[] { Messages.getString("servoy.button.ok") }; //$NON-NLS-1$
		JOptionPane pane = new JOptionPane(msg, type, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
		pane.setInitialValue(options[0]);
		createAndShowDialog(title, pane);
		Object retValue = pane.getValue();
		if (retValue != null) return retValue.toString();
		return null;
	}

	/**
	 * @param title
	 * @param pane
	 * @return
	 */
	private void createAndShowDialog(String title, JOptionPane pane)
	{
		IClientPluginAccess access = plugin.getClientPluginAccess();
		IRuntimeWindow runtimeWindow = access.getCurrentRuntimeWindow();
		Window currentWindow = null;
		if (runtimeWindow instanceof ISmartRuntimeWindow) currentWindow = ((ISmartRuntimeWindow)runtimeWindow).getWindow();
		pane.setComponentOrientation(((currentWindow == null) ? JOptionPane.getRootFrame() : currentWindow).getComponentOrientation());
		JDialog dialog = pane.createDialog(currentWindow, title);
		modalizeDialog(dialog);
		pane.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();
	}

	/**
	 * @param dialog
	 */
	private void modalizeDialog(JDialog dialog)
	{
		Object setModalityTypeMethod = null;
		Object modalityDocumentModalValue = null;
		try
		{
			Class< ? > clz = Class.forName("java.awt.Dialog$ModalityType");
			modalityDocumentModalValue = clz.getField("DOCUMENT_MODAL").get(clz);
			setModalityTypeMethod = Dialog.class.getMethod("setModalityType", clz);
		}
		catch (Exception e)
		{
			setModalityTypeMethod = Boolean.FALSE;
		}

		if (setModalityTypeMethod instanceof Method)
		{
			try
			{
				((Method)setModalityTypeMethod).invoke(dialog, new Object[] { modalityDocumentModalValue });
			}
			catch (Exception e)
			{
				setModalityTypeMethod = Boolean.FALSE;
			}
		}
	}

	/**
	 * Shows a selection dialog, where the user can select an entry from a list of options. Returns the selected entry, or nothing when canceled.
	 *
	 * @sample
	 * //show select,returns nothing when canceled 
	 * var selectedValue = plugins.dialogs.showSelectDialog('Select','please select a name','jan','johan','sebastiaan');
	 * //also possible to pass array with options
	 * //var selectedValue = plugins.dialogs.showSelectDialog('Select','please select a name', new Array('jan','johan','sebastiaan'));
	 *
	 * @param dialog_title 
	 * @param msg 
	 * @param optionArray/option1 
	 * @param ...option optional
	 */
	public String js_showSelectDialog(Object[] array)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Can't use the dialog plugin in a none Swing thread/environment");
		}
		String title = Messages.getString("servoy.general.warning"); //$NON-NLS-1$
		if (array != null && array.length > 0 && array[0] != null) title = Messages.getStringIfPrefix(array[0].toString());
		String msg = Messages.getString("servoy.general.clickOk"); //$NON-NLS-1$
		if (array != null && array.length > 1 && array[1] != null) msg = Messages.getStringIfPrefix(array[1].toString());
		Vector buttons = new Vector();
		if (array != null)
		{
			if (array.length == 3 && array[2] instanceof Object[])
			{
				Object[] args = ((Object[])array[2]);
				for (Object element : args)
				{
					buttons.addElement(element == null ? "" : Messages.getStringIfPrefix(element.toString())); //$NON-NLS-1$
				}
			}
			else
			{
				for (int i = 2; i < array.length; i++)
				{
					buttons.addElement(array[i] == null ? "" : Messages.getStringIfPrefix(array[i].toString())); //$NON-NLS-1$
				}
			}
		}
		Object[] options = new String[buttons.size()];
		buttons.copyInto(options);
		if (options.length == 0) options = new String[] { Messages.getString("servoy.button.ok") }; //$NON-NLS-1$
		JOptionPane pane = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, options[0]);
		pane.setWantsInput(true);
		pane.setSelectionValues(options);
		pane.setInitialSelectionValue(options[0]);
		createAndShowDialog(title, pane);
		Object value = pane.getInputValue();
		return (value != JOptionPane.UNINITIALIZED_VALUE ? value.toString() : null);
	}

	/**
	 * Shows an input dialog where the user can enter data. Returns the entered data, or nothing when canceled.
	 *
	 * @sample
	 * //show input dialog ,returns nothing when canceled 
	 * var typedInput = plugins.dialogs.showInputDialog('Specify','Your name');
	 *
	 * @param dialog_title 
	 * @param msg 
	 * @param initialValue optional
	 */
	public String js_showInputDialog(Object[] array)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Can't use the dialog plugin in a none Swing thread/environment");
		}
		String title = Messages.getString("servoy.general.warning"); //$NON-NLS-1$
		if (array != null && array.length > 0 && array[0] != null) title = Messages.getStringIfPrefix(array[0].toString());
		String msg = Messages.getString("servoy.general.clickOk"); //$NON-NLS-1$
		if (array != null && array.length > 1 && array[1] != null) msg = Messages.getStringIfPrefix(array[1].toString());
		String val = null;
		if (array != null && array.length > 2 && array[2] != null) val = array[2].toString();
		JOptionPane pane = new JOptionPane(msg, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, val);
		pane.setWantsInput(true);
		pane.setInitialSelectionValue(val);
		createAndShowDialog(title, pane);
		Object value = pane.getInputValue();
		return (value != JOptionPane.UNINITIALIZED_VALUE ? value.toString() : null);
	}

}
