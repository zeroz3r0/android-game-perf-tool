package com.gameperf.plugin.core

import com.gameperf.plugin.analysis.RuleMatch

data class PerformanceReport(
    val deviceId: String,
    val averageFps: Double?,
    val minFps: Double?,
    val maxFps: Double?,
    val frameDrops: Int,
    val averageMemory: Double?,
    val warnings: List<String>,
    val errors: List<String>,
    val timestamp: Long
)

class ReportGenerator(
    private val metricsExtractor: MetricsExtractor,
    private val warnings: List<RuleMatch>,
    private val errors: List<RuleMatch>
) {
    
    fun generate(deviceId: String): PerformanceReport {
        val fpsMetrics = metricsExtractor.getMetricsByType(MetricType.FPS)
        val memoryMetrics = metricsExtractor.getMetricsByType(MetricType.MEMORY)
        
        return PerformanceReport(
            deviceId = deviceId,
            averageFps = metricsExtractor.getAverageFps(),
            minFps = metricsExtractor.getMinFps(),
            maxFps = fpsMetrics.maxOfOrNull { it.value },
            frameDrops = metricsExtractor.getFrameDrops(),
            averageMemory = if (memoryMetrics.isNotEmpty()) 
                memoryMetrics.map { it.value }.average() else null,
            warnings = warnings.map { it.rule.name },
            errors = errors.map { it.rule.name },
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun toJson(report: PerformanceReport): String {
        return """
        {
            "deviceId": "${report.deviceId}",
            "timestamp": ${report.timestamp},
            "fps": {
                "average": ${report.averageFps},
                "min": ${report.minFps},
                "max": ${report.maxFps},
                "frameDrops": ${report.frameDrops}
            },
            "memory": {
                "averageMb": ${report.averageMemory}
            },
            "warnings": ${report.warnings.map { "\"$it\"" }},
            "errors": ${report.errors.map { "\"$it\"" }}
        }
        """.trimIndent()
    }
    
    fun toMarkdown(report: PerformanceReport): String {
        return """
# Performance Report

**Device**: ${report.deviceId}
**Generated**: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(report.timestamp))}

## FPS Analysis
- **Average**: ${report.averageFps?.let { "%.2f".format(it) } ?: "N/A"}
- **Min**: ${report.minFps?.let { "%.2f".format(it) } ?: "N/A"}
- **Max**: ${report.maxFps?.let { "%.2f".format(it) } ?: "N/A"}
- **Frame Drops**: ${report.frameDrops}

## Memory
- **Average**: ${report.averageMemory?.let { "%.2f MB".format(it) } ?: "N/A"}

## Issues
${if (report.errors.isNotEmpty()) "### Errors\n${report.errors.joinToString("\n") { "- $it" }}"} else "### Errors\nNone"}
${if (report.warnings.isNotEmpty()) "### Warnings\n${report.warnings.joinToString("\n") { "- $it" }}"} else "### Warnings\nNone"}
        """.trimIndent()
    }
}
