package com.ruoyi.project3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ModuleNode {

    @JsonProperty("id")
    private String id;

    @JsonProperty("parent_id")
    private String parent_id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("level")
    private Integer level;

    @JsonProperty("terminal")
    private Boolean terminal;

    @JsonProperty("child_count")
    private Integer child_count;

    @JsonProperty("part_count")
    private Integer part_count;

    @JsonProperty("children")
    private List<ModuleNode> children;

    public String get_id() {
        return id;
    }

    public void set_id(String id) {
        this.id = id;
    }

    public String get_parent_id() {
        return parent_id;
    }

    public void set_parent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String get_name() {
        return name;
    }

    public void set_name(String name) {
        this.name = name;
    }

    public Integer get_level() {
        return level;
    }

    public void set_level(Integer level) {
        this.level = level;
    }

    public Boolean get_terminal() {
        return terminal;
    }

    public void set_terminal(Boolean terminal) {
        this.terminal = terminal;
    }

    public Integer get_child_count() {
        return child_count;
    }

    public void set_child_count(Integer child_count) {
        this.child_count = child_count;
    }

    public Integer get_part_cnt() {
        return part_count;
    }

    public void set_part_cnt(Integer part_count) {
        this.part_count = part_count;
    }

    public List<ModuleNode> get_children() {
        return children;
    }

    public void set_children(List<ModuleNode> children) {
        this.children = children;
    }
}

