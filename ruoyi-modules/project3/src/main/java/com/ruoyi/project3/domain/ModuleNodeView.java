package com.ruoyi.project3.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ModuleNodeView {

    @JsonProperty("current_node")
    private ModuleNode current_node;

    @JsonProperty("breadcrumb_list")
    private List<ModuleNode> breadcrumb_list;

    @JsonProperty("children")
    private List<ModuleNode> children;

    public ModuleNode get_cur_node() {
        return current_node;
    }

    public void set_cur_node(ModuleNode current_node) {
        this.current_node = current_node;
    }

    public List<ModuleNode> get_bread_list() {
        return breadcrumb_list;
    }

    public void set_bread_list(List<ModuleNode> breadcrumb_list) {
        this.breadcrumb_list = breadcrumb_list;
    }

    public List<ModuleNode> get_children() {
        return children;
    }

    public void set_children(List<ModuleNode> children) {
        this.children = children;
    }
}

