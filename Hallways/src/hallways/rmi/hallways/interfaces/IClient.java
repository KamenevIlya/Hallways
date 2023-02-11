package hallways.rmi.hallways.interfaces;

import hallways.rmi.hallways.common.PlayerItem;
import hallways.rmi.hallways.common.packages.ServerPackage;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote {

    void getMessage(ServerPackage message) throws RemoteException;

    void unbind(PlayerItem playerItem) throws RemoteException;;
}
