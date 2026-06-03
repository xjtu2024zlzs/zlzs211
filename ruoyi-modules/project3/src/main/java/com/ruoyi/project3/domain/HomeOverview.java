package com.ruoyi.project3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HomeOverview {

    @JsonProperty("summary")
    private HomeSummary summary;

    public HomeSummary get_sum() {
        return summary;
    }

    public void set_sum(HomeSummary summary) {
        this.summary = summary;
    }
}

