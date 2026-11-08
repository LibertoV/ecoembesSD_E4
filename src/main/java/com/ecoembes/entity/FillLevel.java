package com.ecoembes.entity;

/** Fill level reported by the dumpster sensor. */
public enum FillLevel {
    GREEN,   // there is room for more containers
    ORANGE,  // limited space
    RED      // dumpster full
}
