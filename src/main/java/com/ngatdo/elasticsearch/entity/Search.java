package com.ngatdo.elasticsearch.entity;

import java.util.List;

public class Search {

    private List<Map> match;
    private Sort sort;

    public List<Map> getMatch() {
        return match;
    }

    public void setMatch(List<Map> match) {
        this.match = match;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }
}
