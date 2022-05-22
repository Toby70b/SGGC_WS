package com.sggc.models;

import com.google.gson.annotations.SerializedName;

public enum SteamGameCategory {
    @SerializedName("1")
    MULTIPLAYER(1),
    @SerializedName("2")
    SINGLE_PLAYER(2);

    private final int id;

    SteamGameCategory(int id) {
        this.id = id;
    }
}