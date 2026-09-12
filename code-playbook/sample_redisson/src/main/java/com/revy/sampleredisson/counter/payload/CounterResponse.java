package com.revy.sampleredisson.counter.payload;

public record CounterResponse(String name,
                              long value) {
}

