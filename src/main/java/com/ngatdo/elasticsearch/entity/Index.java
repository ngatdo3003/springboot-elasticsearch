package com.ngatdo.elasticsearch.entity;

public class Index {
    private String name;
    private Integer number_of_shards;
    private Integer number_of_replicas;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getNumber_of_shards()
    {
        return number_of_shards;
    }

    public void setNumber_of_shards(Integer number_of_shards)
    {
        this.number_of_shards = number_of_shards;
    }

    public Integer getNumber_of_replicas()
    {
        return number_of_replicas;
    }

    public void setNumber_of_replicas(Integer number_of_replicas)
    {
        this.number_of_replicas = number_of_replicas;
    }
}
