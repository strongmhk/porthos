package com.swyp.noticore.global.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@RequiredArgsConstructor
public enum NationNumber {

    KOREA("+82"),
    USA("+12");

    private final String value;



}
