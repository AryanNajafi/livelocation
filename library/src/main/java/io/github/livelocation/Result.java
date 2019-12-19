package io.github.livelocation;

import android.location.Location;

import com.google.android.gms.common.api.ResolvableApiException;

public class Result {

    private final State state;
    private final Location location;
    private final LocationError error;

    private boolean used;

    private Result(State state, Location location, LocationError error) {
        this.state = state;
        this.location = location;
        this.error = error;
    }

    static Result success(Location location) {
        return new Result(State.SUCCESS, location, null);
    }

    static Result failure(ErrorType type, ResolvableApiException apiException) {
        return new Result(State.FAILURE, null, new LocationError(type, apiException));
    }

    static Result failure(ErrorType type) {
        return failure(type, null);
    }

    public State getState() {
        return state;
    }

    public Location getLocation() {
        used = true;
        return location;
    }

    public LocationError getLocationError() {
        used = true;
        return error;
    }

    public boolean isUsed() {
        return used;
    }
}
