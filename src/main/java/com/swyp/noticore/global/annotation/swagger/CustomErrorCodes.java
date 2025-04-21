package com.swyp.noticore.global.annotation.swagger;

import com.swyp.noticore.global.response.code.CommonErrorCode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomErrorCodes {

    CommonErrorCode[] commonErrorCodes() default {};
}