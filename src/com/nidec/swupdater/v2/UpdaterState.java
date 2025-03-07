package com.nidec.swupdater.v2;

import android.util.Log;
import android.util.SparseArray;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * Controls Updater State
 */

public class UpdaterState {

    private static final String TAG_UPDATER_STATE = "UpdaterState";

    public static final int IDLE = 0;
    public static final int ERROR = 1;
    public static final int RUNNING = 2;
    public static final int PAUSED = 3;
    public static final int SLOT_SWITCH_REQUIRED = 4;
    public static final int REBOOT_REQUIRED = 5;

    private static final SparseArray<String> STATE_MAP = new SparseArray<>();


    static {
        STATE_MAP.put(0, "IDLE");
        STATE_MAP.put(1, "ERROR");
        STATE_MAP.put(2, "RUNNING");
        STATE_MAP.put(3, "PAUSED");
        STATE_MAP.put(4, "SLOT_SWITCH_REQUIRED");
        STATE_MAP.put(5, "REBOOT_REQUIRED");
    }


    /**
     * Allowed State Transitions.
     * It's a map : key is a state, value is a set of states that are allowed to transition to - from key.
     */

    private static final ImmutableMap<Integer, ImmutableSet<Integer>> TRANSITIONS =
            ImmutableMap.<Integer, ImmutableSet<Integer>>builder()
                    .put(IDLE, ImmutableSet.of(IDLE, ERROR, RUNNING))
                    .put(ERROR, ImmutableSet.of(IDLE))
                    .put(RUNNING, ImmutableSet.of(
                            IDLE, ERROR, PAUSED, REBOOT_REQUIRED, SLOT_SWITCH_REQUIRED))
                    .put(PAUSED, ImmutableSet.of(ERROR, RUNNING, IDLE))
                    .put(SLOT_SWITCH_REQUIRED, ImmutableSet.of(ERROR, REBOOT_REQUIRED, IDLE))
                    .put(REBOOT_REQUIRED, ImmutableSet.of(IDLE))
                    .build();


    private AtomicInteger mState;

    public UpdaterState(int state) {
        this.mState = new AtomicInteger(state);
    }


    // RETURN THE UPDATER's STATE.
    public int get() {
        return mState.get();
    }


    /**
     * SET THE UPDATER's STATE.
     * @throws InvalidTransitionException if transition is not allowed.
     */

    public void set(int newState) throws InvalidTransitionException {
        int oldState = mState.get();
        if(!TRANSITIONS.get(oldState).contains(newState)) {
            throw new InvalidTransitionException("Can't transition from " + oldState + " to " + newState);
        }
        mState.set(newState);
    }

    // Converts STATUS CODE to STATUS NAME

    public static String getStateText(int state) {
        Log.d(TAG_UPDATER_STATE, "getStateText() was called....");
        Log.d(TAG_UPDATER_STATE, "UpdaterState.getStateText() state = " + STATE_MAP.get(state));
        return STATE_MAP.get(state);
    }

    // Defines Invalid State Transition Exception.

    public static class InvalidTransitionException extends Exception {
        public InvalidTransitionException(String msg) {
            super(msg);
        }
    }
}





















