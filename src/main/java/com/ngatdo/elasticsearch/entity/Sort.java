package com.ngatdo.elasticsearch.entity;

import javax.validation.constraints.NotNull;

public class Sort {
    @NotNull
    private String sortBy;
    private int asc = 1;

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public int getAsc() {
        return asc;
    }

    public void setAsc(int asc) {
        this.asc = asc;
    }
}
