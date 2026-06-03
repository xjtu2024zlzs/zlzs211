package com.ruoyi.project3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HomeSummary {

    @JsonProperty("aircraft_count")
    private Long aircraft_count;

    @JsonProperty("subsystem_count")
    private Long subsystem_count;

    @JsonProperty("device_count")
    private Long device_count;

    @JsonProperty("component_count")
    private Long component_count;

    @JsonProperty("part_count")
    private Long part_count;

    public Long get_air_count() {
        return aircraft_count;
    }

    public void set_air_count(Long aircraft_count) {
        this.aircraft_count = aircraft_count;
    }

    public Long get_sub_count() {
        return subsystem_count;
    }

    public void set_sub_count(Long subsystem_count) {
        this.subsystem_count = subsystem_count;
    }

    public Long get_dev_count() {
        return device_count;
    }

    public void set_dev_count(Long device_count) {
        this.device_count = device_count;
    }

    public Long get_comp_count() {
        return component_count;
    }

    public void set_comp_count(Long component_count) {
        this.component_count = component_count;
    }

    public Long get_part_cnt() {
        return part_count;
    }

    public void set_part_cnt(Long part_count) {
        this.part_count = part_count;
    }
}

