package com.servoy.extensions.plugins.clientmanager;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.servoy.j2db.plugins.IClientPlugin;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.preference.PreferencePanel;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.util.Debug;

public class ClientManagerPlugin implements IClientPlugin
{
	public static final String PLUGIN_NAME = "clientManager"; //$NON-NLS-1$

	private IClientPluginAccess access;
	private ClientManagerProvider impl;
	private final List<BroadCaster> liveBroadCasters = new ArrayList<>();


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

	IClientManagerService getClientService()
	{
		try
		{
			return (IClientManagerService)access.getRemoteService(IClientManagerService.class.getName());
		}
		catch (Exception e)
		{
			Debug.error(e);
		}
		return null;
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
		for (BroadCaster broadCaster : liveBroadCasters.toArray(new BroadCaster[liveBroadCasters.size()]))
		{
			try
			{
				broadCaster.js_destroy();
			}
			catch (Exception e)
			{
				Debug.error(e);
			}
		}
	}

	void addLiveBroadCaster(BroadCaster bc)
	{
		liveBroadCasters.add(bc);
	}

	void removeLiveBroadCaster(BroadCaster bc)
	{
		liveBroadCasters.remove(bc);
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
	}

	public IClientPluginAccess getClientPluginAccess()
	{
		return access;
	}
}
