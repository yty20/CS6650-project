package org.CS6650;

import java.util.List;

public record PostRequestResult(int successfulRequests, int failedRequests, List<PerformanceData> performanceDataList) { }
