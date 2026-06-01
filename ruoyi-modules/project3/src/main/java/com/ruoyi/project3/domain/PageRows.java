package com.ruoyi.project3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PageRows {

    @JsonProperty("rows")
    private List<?> rows;

    @JsonProperty("total")
    private Long total;

    public List<?> get_rows() {
        return rows;
    }

    public void set_rows(List<?> rows) {
        this.rows = rows;
    }

    public Long get_tot() {
        return total;
    }

    public void set_tot(Long total) {
        this.total = total;
    }

    public Long get_total() {
        return total;
    }

    public void set_total(Long total) {
        this.total = total;
    }
}

