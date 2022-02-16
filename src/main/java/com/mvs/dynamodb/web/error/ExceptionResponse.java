package com.mvs.dynamodb.web.error;

import java.util.Date;

public record ExceptionResponse(Date timestamp, String message, String details) {
}