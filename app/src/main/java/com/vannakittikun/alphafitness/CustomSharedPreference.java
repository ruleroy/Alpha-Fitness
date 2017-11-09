package com.vannakittikun.alphafitness;

/**
 * Created by Rule on 11/4/2017.
 */

import android.content.Context;

public class CustomSharedPreference {
    Context context;
    Boolean serviceState;

    public CustomSharedPreference(Context applicationContext) {
        this.context = applicationContext;
    }

    public void setServiceState(boolean serviceState) {
        this.serviceState = serviceState;
    }

    public boolean getServiceState() {
        return serviceState;
    }
}