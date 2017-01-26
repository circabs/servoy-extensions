package com.servoy.extensions.plugins.clientmanager;

import java.rmi.RemoteException;

import com.servoy.j2db.server.shared.IClientInformation;

public interface IClientManagerService
{
	IClientInformation[] getConnectedClients() throws RemoteException;

	void sendMessageToAllClients(String message) throws RemoteException;

	void sendMessageToClient(String clientId, String message) throws RemoteException;

	void shutDownAllClients(String skipClientId) throws RemoteException;

	void shutDownClient(String clientId) throws RemoteException;

}
