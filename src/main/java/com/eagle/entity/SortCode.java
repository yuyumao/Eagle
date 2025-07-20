package com.eagle.entity;

public enum SortCode {
    Branch1("10-10-10");

    public final String value;

    SortCode(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
