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
package com.servoy.extensions.plugins.spellcheck;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.servoy.extensions.plugins.spellcheck2.GoogleSpellUtils;
import com.servoy.extensions.plugins.spellcheck2.RapidSpellUtils;
import com.servoy.extensions.plugins.spellcheck2.SpellCheckerUtils;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.preference.PreferencePanel;

/**
 * @author	sbaciuna
 */
public class SpellCheckerPreferencePanel extends PreferencePanel implements ActionListener
{
	protected IClientPluginAccess application;
	private final JComboBox languageBox;
	private static String desiredLanguage;

	/**
	 * Constructor for SpellCheckerPreferencePanel.
	 */
	public SpellCheckerPreferencePanel(IClientPluginAccess app)
	{
		super();
		application = app;

		this.setLayout(new BorderLayout());

		JComboBox engineBox = new JComboBox();
		engineBox.addItem(SpellCheckerUtils.RAPID_SPELL);
		engineBox.addItem(SpellCheckerUtils.GOOGLE_SPELL);
		engineBox.addActionListener(this);
		this.add(engineBox, BorderLayout.NORTH);

		languageBox = new JComboBox();
		languageBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JComboBox cb = (JComboBox)e.getSource();
				String selectedLang = (String)cb.getSelectedItem();
				setDesiredLanguage(selectedLang);
			}
		});
		this.add(languageBox, BorderLayout.SOUTH);

		//FIXME: adding by default RapidShell and en_us; this is too hard-coded, should change
		populateLanguageBox(RapidSpellUtils.getAvailableDictionaries());
		setDesiredLanguage(SpellCheckerUtils.DEFAULT);
	}

	private ChangeListener listener;

	@Override
	public void addChangeListener(ChangeListener l)
	{
		listener = l;
	}

	private void fireChangeEvent()
	{
		changed = true;
		listener.stateChanged(new ChangeEvent(this));
	}

	private boolean changed = false;

	@Override
	public int getRequiredUserAction()
	{
		int retval = PreferencePanel.NO_USER_ACTION_REQUIRED;
		if (changed)
		{
			retval = PreferencePanel.APPLICATION_RESTART_NEEDED;
		}
		changed = false;
		return retval;
	}

	/*
	 * @see PreferencePanel#cancel()
	 */
	@Override
	public boolean handleCancel()
	{
		return true;
	}

	/*
	 * @see PreferencePanel#ok()
	 */
	@Override
	public boolean handleOK()
	{
		return true;
	}

	/*
	 * @see PreferencePanel#getTabName()
	 */
	@Override
	public String getTabName()
	{
		return "SpellCheck"; //$NON-NLS-1$
	}

	/**
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		fireChangeEvent();
		JComboBox cb = (JComboBox)e.getSource();
		String serviceProvider = (String)cb.getSelectedItem();
		if (serviceProvider.equals(SpellCheckerUtils.GOOGLE_SPELL))
		{
			application.getSettings().setProperty("plugin.spellcheck2.googleServiceProvider", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			populateLanguageBox(GoogleSpellUtils.getAvailableDictionaries());
			setDesiredLanguage(SpellCheckerUtils.DEFAULT);
		}
		else
		{
			application.getSettings().setProperty("plugin.spellcheck2.googleServiceProvider", "false"); //$NON-NLS-1$ //$NON-NLS-2$
			populateLanguageBox(RapidSpellUtils.getAvailableDictionaries());
			setDesiredLanguage(SpellCheckerUtils.DEFAULT);
		}
	}

	public static String getDesiredLanguage()
	{
		return desiredLanguage;
	}

	public static void setDesiredLanguage(String s)
	{
		desiredLanguage = s;
	}

	private void populateLanguageBox(List<String> langLst)
	{
		languageBox.removeAllItems();
		Iterator<String> it = langLst.iterator();
		while (it.hasNext())
		{
			languageBox.addItem(it.next());
		}
	}

}
