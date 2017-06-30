package com.iam360.dscvr.bus;

/**
 * @author Nilan Marktanner
 * @date 2016-02-13
 */
public class RecordFinishedEvent {
    private  boolean sucess;

    public RecordFinishedEvent(boolean wasSuccesful) {
        this.sucess = wasSuccesful;
    }

    public boolean wasSuccesful(){
        return sucess;
    }
}
