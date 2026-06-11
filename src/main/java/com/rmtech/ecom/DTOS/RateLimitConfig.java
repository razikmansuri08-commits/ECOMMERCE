package com.rmtech.ecom.DTOS;

import java.time.Duration;

public record RateLimitConfig(int maxRequests, Duration duration) {


}
