package com.alphaka.blogservice.exception.custom;

import com.alphaka.blogservice.exception.CustomException;
import com.alphaka.blogservice.exception.ErrorCode;

public class InvalidReportTargetException extends CustomException {

    public InvalidReportTargetException() {
        super(ErrorCode.INVALID_REPORT_TARGET);
    }
}
