package com.servoy.extensions.plugins.clientmanager;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.servoy.j2db.server.shared.IClientInformation;

public interface IClientManagerService extends Remote
{
	IClientInformation[] getConnectedClients() throws RemoteException;

	void sendMessageToAllClients(String message) throws RemoteException;

	void sendMessageToClient(String clientId, String message) throws RemoteException;

	void shutDownAllClients(String skipClientId) throws RemoteException;

	void shutDownClient(String clientId) throws RemoteException;

	void deregisterChannelListener(BroadCastInfo info) throws RemoteException;

	void broadcastMessage(BroadCastInfo info, String message) throws RemoteException;

	void registerChannelListener(BroadCastInfo info) throws RemoteException;

}
