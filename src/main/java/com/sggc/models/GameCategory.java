package com.sggc.models;

import lombok.Data;
import lombok.RequiredArgsConstructor;
/**
 * Represents a model of a Steam Game's category e.g. multiplayer, coop, workshop support etc.
 */
@Data
@RequiredArgsConstructor
public class GameCategory {
    private final int id;
}
