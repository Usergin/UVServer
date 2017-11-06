package utils;

import data.ClientServerState;
import data.DeviceServerState;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import network.robot.DeviceThread;

public class RxBus {
    private static RxBus instance;

    private PublishSubject<String> subjectInfoDevice = PublishSubject.create();
    private BehaviorSubject<String> subjectSettingsDevice = BehaviorSubject.create();

    private BehaviorSubject<DeviceServerState> subjectDeviceServerState = BehaviorSubject.create();
    private BehaviorSubject<DeviceThread> subjectDeviceState = BehaviorSubject.create();
    private BehaviorSubject<ClientServerState> subjectClientServerState = BehaviorSubject.create();

//    private BehaviorSubject<ObservableList<Device>> subjectDeviceList = BehaviorSubject.create();
//
    public static RxBus instanceOf() {
        if (instance == null) {
            instance = new RxBus();
        }
        return instance;
    }

    /**
     * Pass any event down to event listeners.
     */
    public void setDeviceServerState(DeviceServerState object) {
        subjectDeviceServerState.onNext(object);
    }

    public Observable<DeviceServerState> getDeviceServerState() {
        return subjectDeviceServerState;
    }

    public void setClientServerState(ClientServerState object) {
        subjectClientServerState.onNext(object);
    }

    public Observable<ClientServerState> getClientServerState() {
        return subjectClientServerState;
    }

    public void setDeviceState(DeviceThread object) {
        subjectDeviceState.onNext(object);
    }

    public Observable<DeviceThread> getDeviceState() {
        return subjectDeviceState;
    }

//
//    public void setDeviceList(ObservableList<Device> object) {
//        subjectDeviceList.onNext(object);
//    }
//
//    public Observable<ObservableList<Device>> getDeviceList() {
//        return subjectDeviceList;
//    }

    public void setDeviceInfo(String device_id) {
        subjectInfoDevice.onNext(device_id);
    }

    public Observable<String> getDeviceInfo() {
        return subjectInfoDevice;
    }

    public BehaviorSubject<String> getSubjectSettingsDevice() {
        return subjectSettingsDevice;
    }

    public void setSubjectSettingsDevice(BehaviorSubject<String> subjectSettingsDevice) {
        this.subjectSettingsDevice = subjectSettingsDevice;
    }
}
