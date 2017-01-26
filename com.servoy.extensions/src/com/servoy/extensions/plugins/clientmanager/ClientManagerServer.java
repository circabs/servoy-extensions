package com.servoy.extensions.plugins.clientmanager;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Properties;

import com.servoy.j2db.Messages;
import com.servoy.j2db.plugins.IServerAccess;
import com.servoy.j2db.plugins.IServerPlugin;
import com.servoy.j2db.plugins.PluginException;
import com.servoy.j2db.server.shared.IClientInformation;

public class ClientManagerServer implements IServerPlugin, IClientManagerService
{
	private IServerAccess application;

	private static IClientManagerService INSTANCE;

	public static IClientManagerService getInstance()
	{
		return INSTANCE;
	}

	public ClientManagerServer()
	{
	}

	@Override
	public Map<String, String> getRequiredPropertyNames()
	{
		return null;
	}

	@Override
	public void initialize(IServerAccess app) throws PluginException
	{
		application = app;
		INSTANCE = this;
		try
		{
			app.registerRemoteService(IClientManagerService.class.getName(), this);
		}
		catch (RemoteException ex)
		{
			throw new PluginException(ex);
		}
	}

	@Override
	public Properties getProperties()
	{
		Properties props = new Properties();
		props.put(DISPLAY_NAME, Messages.getString("servoy.plugin.maintenanceserver.displayname")); //$NON-NLS-1$
		return props;
	}

	@Override
	public void load() throws PluginException
	{
	}

	@Override
	public void unload() throws PluginException
	{
	}

	@Override
	public IClientInformation[] getConnectedClients()
	{
		return application.getConnectedClients();
	}

	@Override
	public void sendMessageToAllClients(String message)
	{
		application.sendMessageToAllClients(message);
	}

	@Override
	public void sendMessageToClient(String clientId, String message)
	{
		application.sendMessageToClient(clientId, message);
	}

	@Override
	public void shutDownAllClients(String skipClientId)
	{
		application.shutDownAllClients(skipClientId);
	}

	@Override
	public void shutDownClient(String clientId)
	{
		application.shutDownClient(clientId);
	}

}
