package com.sggc.models;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Represents a model of a Steam Game's category e.g. multiplayer, coop, workshop support etc.
 */
@RequiredArgsConstructor
@Data
public class GameCategory {

    private final SteamGameCategory id;



}
