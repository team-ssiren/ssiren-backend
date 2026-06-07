package com.ssaika.ssiren.domain.agency.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ComplaintCategoryGroup {

    TRAFFIC("교통"),
    ENVIRONMENT("환경"),
    FACILITY("시설물"),
    LIFE_INCONVENIENCE("생활불편"),
    PUBLIC_SAFETY("치안"),
    WELFARE("복지"),
    DISASTER_SAFETY("재난안전"),




    ETC("기타");


    private final String description;
}
