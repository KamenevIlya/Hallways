package hallways.rmi.hallways.interfaces;

import hallways.rmi.hallways.common.packages.ServerPackage;
import hallways.rmi.hallways.common.packages.ClientPackage;
import hallways.rmi.hallways.common.PlayerItem;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer extends Remote {

    PlayerItem register(IClient player) throws RemoteException;

    void getMessage(ClientPackage clientPackage) throws RemoteException;
    void sendMessage(ServerPackage message, PlayerItem playerItem) throws RemoteException;

    void unbind(PlayerItem playerItem) throws RemoteException;

}
