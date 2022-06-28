package com.sun.video.model.entity;

import java.util.List;

/**
 * @author: Harper
 * @date: 2022/6/22
 * @note: 视频画质信息
 */
public class VideoClassification {

    private String        id;
    private String        name;
    private List<Integer> definitionList;

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

    public List<Integer> getDefinitionList() {
        return definitionList;
    }

    public void setDefinitionList(List<Integer> definitionList) {
        this.definitionList = definitionList;
    }
}