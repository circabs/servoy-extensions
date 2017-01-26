package com.servoy.extensions.plugins.clientmanager;

import java.beans.PropertyChangeEvent;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.servoy.j2db.plugins.IClientPlugin;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.scripting.IScriptable;

public class ClientManagerPlugin implements IClientPlugin
{
	public static final String PLUGIN_NAME = "clientManager"; //$NON-NLS-1$

	private IClientPluginAccess access;
	private ClientManagerProvider impl;

	public Icon getImage()
	{
		java.net.URL iconUrl = this.getClass().getResource("images/maintenance.gif"); //$NON-NLS-1$
		if (iconUrl != null) return new ImageIcon(iconUrl);
		else return null;
	}

	public String getName()
	{
		return PLUGIN_NAME;
	}

	public PreferencePanel[] getPreferencePanels()
	{
		return null;
	}

	public IScriptable getScriptObject()
	{
		if (impl == null) impl = new ClientManagerProvider(this);
		return impl;
	}

	public void initialize(IClientPluginAccess app) throws PluginException
	{
		this.access = app;
	}

	public Properties getProperties()
	{
		return null;
	}

	public void load() throws PluginException
	{
	}

	public void unload() throws PluginException
	{
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
	}

	public IClientPluginAccess getClientPluginAccess()
	{
		return access;
	}
}
