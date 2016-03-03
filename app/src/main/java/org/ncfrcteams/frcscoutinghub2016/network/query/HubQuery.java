package org.ncfrcteams.frcscoutinghub2016.network.query;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import org.ncfrcteams.frcscoutinghub2016.network.Network;
import org.ncfrcteams.frcscoutinghub2016.network.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Admin on 2/26/2016.
 */
public class HubQuery extends Thread {
    BluetoothDevice device;
    BluetoothSocket bluetoothSocket;

    String hubName = null;

    /**
     * Creates and starts a thread that will attempt to find a host on a BluetoothDevice
     * @param device the BluetoothDevice you want to determine whether or not is hosting
     * @return the thread that is performing the check
     */
    public static HubQuery spawn(BluetoothDevice device) {
        HubQuery hubQuery = new HubQuery(device);
        hubQuery.start();
        return hubQuery;
    }

    private HubQuery(BluetoothDevice device) {
        this.device = device;
    }

    /**
     * Attempts to acquire a HostDetail from its BluetoothDevice
     */
    public void run() {
        ObjectOutputStream objectOutputStream;
        ObjectInputStream objectInputStream;

        Message message;
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(Network.SCOUTING_HUB_UUID);
            objectInputStream = new ObjectInputStream(bluetoothSocket.getInputStream());
            objectOutputStream = new ObjectOutputStream(bluetoothSocket.getOutputStream());

            objectOutputStream.writeObject(new HubQueryMessage());
            message = (Message) objectInputStream.readObject();

            if(message.getType() == Message.Type.HUBNAME) {
                hubName = ((HubNameMessage) message).getName();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return an object that describes the host that corresponds to its BluetoothDevice and name if there is one,
     * if not host has been found returns null
     */
    public HubDetails getHostDetails() {
        if(hubName != null)
            return new HubDetails(hubName,device);
        return null;
    }

    /**
     * Stops the HubQuery operation by closing the ObjectInputStream
     */
    public void kill() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}