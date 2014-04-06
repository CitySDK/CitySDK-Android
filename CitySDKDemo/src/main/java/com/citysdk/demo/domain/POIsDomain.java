package com.citysdk.demo.domain;

public class POIsDomain {

    private String id;

    private String name;

    private String category;

    private String coord;

    public POIsDomain(String id, String name, String category, String coord) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.coord = coord;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCoord() {
        return coord;
    }

    public void setCoord(String coord) {
        this.coord = coord;
    }
}
