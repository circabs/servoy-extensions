package com.servoy.extensions.plugins.clientmanager;

import com.servoy.j2db.documentation.ServoyDocumented;
import com.servoy.j2db.scripting.IReturnedTypesProvider;
import com.servoy.j2db.scripting.IScriptable;
import com.servoy.j2db.server.shared.IClientInformation;
import com.servoy.j2db.util.Debug;

/**
 * @author gerzse
 */
@ServoyDocumented(publicName = ClientManagerPlugin.PLUGIN_NAME, scriptingName = "plugins." + ClientManagerPlugin.PLUGIN_NAME)
@SuppressWarnings("boxing")
public class ClientManagerProvider implements IScriptable, IReturnedTypesProvider
{
	private final ClientManagerPlugin plugin;

	ClientManagerProvider(ClientManagerPlugin plugin)
	{
		this.plugin = plugin;
	}

	public ClientManagerProvider()
	{
		this.plugin = null;
	}

	public Class< ? >[] getAllReturnedTypes()
	{
		return new Class[] { JSClientInformation.class };
	}

	/**
	 * Returns an array of JSClientInformation elements describing the clients connected to the server.
	 *
	 * @sample
	 * // WARNING: maintenance plugin is only meant to run during solution import using before or after import hook(so not from Smart/Web client)
	 * //Returns an array of JSClientInformation elements describing the clients connected to the server.
	 * var clients = plugins.maintenance.getConnectedClients();
	 * application.output("There are " + clients.length + " connected clients.");
	 * for (var i = 0; i < clients.length; i++)
	 * 	application.output("Client has clientId '" + clients[i].getClientID() + "' and has connected from host '" + clients[i].getHostAddress() + "'.");
	 *
	 * @return JSClientInformation[]
	 */
	public JSClientInformation[] js_getConnectedClients()
	{
		try
		{
			IClientInformation[] connectedClients = ClientManagerServer.getInstance().getConnectedClients();
			JSClientInformation[] infos = new JSClientInformation[connectedClients == null ? 0 : connectedClients.length];
			for (int i = 0; i < infos.length; i++)
			{
				infos[i] = new JSClientInformation(connectedClients[i]);
			}
			return infos;
		}
		catch (Exception e)
		{
			Debug.error("Exception while retrieving connected clients information.", e); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Sends a message to all connected clients.
	 *
	 * @sample
	 * // WARNING: maintenance plugin is only meant to run during solution import using before or after import hook(so not from Smart/Web client)
	 * //Sends a message to all connected clients.
	 * plugins.maintenance.sendMessageToAllClients("Hello, all clients!");
	 *
	 * @param message
	 */
	public void js_sendMessageToAllClients(String message)
	{
		try
		{
			ClientManagerServer.getInstance().sendMessageToAllClients(message);
		}
		catch (Exception e)
		{
			Debug.error("Exception while sending message to connected clients.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Sends a message to a specific client, identified by its clientId. The clientIds are retrieved by calling the getConnectedClients method.
	 *
	 * @sample
	 * // WARNING: maintenance plugin is only meant to run during solution import using before or after import hook(so not from Smart/Web client)
	 * //Sends a message to a specific client, identified by its clientId. The clientIds are retrieved by calling the getConnectedClients method.
	 * var clients = plugins.maintenance.getConnectedClients();
	 * for (var i=0; i<clients.length; i++)
	 * 	plugins.maintenance.sendMessageToClient(clients[i].getClientId(), "Hello, client " + clients[i].getClientID() + "!");
	 *
	 * @param clientId
	 * @param message
	 */
	public void js_sendMessageToClient(String clientId, String message)
	{
		try
		{
			ClientManagerServer.getInstance().sendMessageToClient(clientId, message);
		}
		catch (Exception e)
		{
			Debug.error("Exception while sending message to client '" + clientId + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	/**
	 * Shuts down all connected clients. This method returns immediately, it does not wait until the client shuts down.
	 *
	 * @sample
	 * // WARNING: maintenance plugin is only meant to run during solution import using before or after import hook(so not from Smart/Web client)
	 * //Shuts down all connected clients. This method returns immediately, it does not wait until the client shuts down.
	 * plugins.maintenance.shutDownAllClients();
	 */
	public void js_shutDownAllClients()
	{
		try
		{
			ClientManagerServer.getInstance().shutDownAllClients(plugin.getClientPluginAccess().getClientID());
		}
		catch (Exception e)
		{
			Debug.error("Exception while shutting down connected clients.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Shuts down a specific client, identified by its clientId. The clientIds are retrieved by calling the getConnectedClients method. This method returns immediately, it does not wait until the client shuts down.
	 *
	 * @sample
	 * // WARNING: maintenance plugin is only meant to run during solution import using before or after import hook(so not from Smart/Web client)
	 * //Shuts down a specific client, identified by its clientId. The clientIds are retrieved by calling the getConnectedClients method. This method returns immediately, it does not wait until the client shuts down.
	 * var clients = plugins.maintenance.getConnectedClients();
	 * for (var i=0; i<clients.length; i++)
	 * 	plugins.maintenance.shutDownClient(clients[i].getClientId());
	 *
	 * @param clientId
	 */
	public void js_shutDownClient(String clientId)
	{
		try
		{
			ClientManagerServer.getInstance().shutDownClient(clientId);
		}
		catch (Exception e)
		{
			Debug.error("Exception while shutting down client '" + clientId + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
