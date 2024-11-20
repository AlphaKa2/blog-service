package com.alphaka.blogservice.exception.custom;

public class InvalidReportTargetException extends CustomException {

    public InvalidReportTargetException() {
        super(ErrorCode.INVALID_REPORT_TARGET);
    }
}
