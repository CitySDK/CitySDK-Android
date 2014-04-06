package com.citysdk.demo.domain;

public class CategoryDomain {

    private String id;

    private String option;

    private String name;

    public CategoryDomain(String id, String option, String name) {
        this.id = id;
        this.option = option;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
