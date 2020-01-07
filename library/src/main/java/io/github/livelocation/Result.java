package io.github.livelocation;

import android.location.Location;

public class Result {

    private final State state;
    private final Location location;
    private final LocationError error;

    private boolean hasBeenHandled = false;

    private Result(State state, Location location, LocationError error) {
        this.state = state;
        this.location = location;
        this.error = error;
    }

    static Result success(Location location) {
        return new Result(State.SUCCESS, location, null);
    }

    static Result failure(ErrorType type, Exception exception) {
        return new Result(State.FAILURE, null, new LocationError(type, exception));
    }

    static Result failure(ErrorType type) {
        return failure(type, null);
    }

    public State getState() {
        return state;
    }

    public Location getLocation() {
        hasBeenHandled = true;
        return location;
    }

    public LocationError getLocationError() {
        hasBeenHandled = true;
        return error;
    }

    LocationError peekLocationError() {
        return error;
    }

    public boolean isSuccessful() {
        return state == State.SUCCESS;
    }

    public boolean isResultHandled() {
        return hasBeenHandled;
    }
}
