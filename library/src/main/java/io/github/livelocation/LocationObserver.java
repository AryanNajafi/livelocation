package io.github.livelocation;

import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

public abstract class LocationObserver implements Observer<Result> {

    @Override
    public void onChanged(Result result) {
        if (result.isUsed()) {
            return;
        }

        if (result.getState() == State.SUCCESS) {
            onSuccess(result.getLocation());
        } else {
            onFailure(result.getLocationError());
        }
    }

    protected abstract void onSuccess(@NonNull Location location);

    protected abstract void onFailure(@NonNull LocationError error);
}
