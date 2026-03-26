package com.gameperf.plugin.ui

import com.gameperf.plugin.core.AndroidDevice
import com.gameperf.plugin.core.Metric
import com.gameperf.plugin.core.MetricType
import com.gameperf.plugin.core.MetricsExtractor
import com.gameperf.plugin.core.ReportGenerator
import com.gameperf.plugin.analysis.RulesEngine
import com.gameperf.plugin.analysis.RuleMatch
import com.gameperf.plugin.analysis.Severity
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel

class MetricsPanel : JPanel() {
    
    private val metricsExtractor = MetricsExtractor()
    private val rulesEngine = RulesEngine()
    private val ruleWarnings = mutableListOf<RuleMatch>()
    private val ruleErrors = mutableListOf<RuleMatch>()
    
    private val fpsLabel = JLabel("--")
    private val minFpsLabel = JLabel("--")
    private val maxFpsLabel = JLabel("--")
    private val frameDropsLabel = JLabel("0")
    private val memoryLabel = JLabel("--")
    
    private val metricsTable = JTable()
    private val tableModel = DefaultTableModel(arrayOf("Type", "Value", "Time"), 0)
    
    private val warningsTextArea = JTextArea(5, 20)
    private val generateReportButton = JButton("Generate Report")
    private val exportJsonButton = JButton("Export JSON")
    
    private var currentDeviceId: String? = null
    
    init {
        layout = BorderLayout(5, 5)
        border = BorderFactory.createTitledBorder("Performance Metrics")
        
        rulesEngine.loadDefaultRules()
        
        val metricsGrid = JPanel(GridLayout(2, 5, 10, 5))
        metricsGrid.add(createMetricBox("FPS", fpsLabel))
        metricsGrid.add(createMetricBox("Min FPS", minFpsLabel))
        metricsGrid.add(createMetricBox("Max FPS", maxFpsLabel))
        metricsGrid.add(createMetricBox("Frame Drops", frameDropsLabel))
        metricsGrid.add(createMetricBox("Memory (MB)", memoryLabel))
        
        add(metricsGrid, BorderLayout.NORTH)
        
        metricsTable.model = tableModel
        val scrollPane = JScrollPane(metricsTable)
        scrollPane.preferredSize = Dimension(0, 200)
        add(scrollPane, BorderLayout.CENTER)
        
        val bottomPanel = JPanel(BorderLayout(5, 5))
        
        val warningsPanel = JPanel(BorderLayout())
        warningsPanel.border = BorderFactory.createTitledBorder("Warnings & Issues")
        warningsTextArea.isEditable = false
        warningsPanel.add(JScrollPane(warningsTextArea), BorderLayout.CENTER)
        
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 5, 5))
        buttonPanel.add(generateReportButton)
        buttonPanel.add(exportJsonButton)
        
        bottomPanel.add(warningsPanel, BorderLayout.CENTER)
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH)
        
        add(bottomPanel, BorderLayout.SOUTH)
        
        generateReportButton.addActionListener { generateReport() }
        exportJsonButton.addActionListener { exportJson() }
    }
    
    private fun createMetricBox(title: String, valueLabel: JLabel): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createTitledBorder(title)
        valueLabel.font = Font("SansSerif", Font.BOLD, 24)
        valueLabel.horizontalAlignment = SwingConstants.CENTER
        panel.add(valueLabel, BorderLayout.CENTER)
        return panel
    }
    
    fun onDeviceConnected(device: AndroidDevice) {
        currentDeviceId = device.id
        clearMetrics()
    }
    
    fun onDeviceDisconnected() {
        currentDeviceId = null
    }
    
    fun addLogEntry(entry: com.gameperf.plugin.core.LogEntry) {
        val metric = metricsExtractor.extract(entry)
        
        // Evaluate rules against the log entry
        val logMatches = rulesEngine.evaluate(entry)
        for (match in logMatches) {
            when (match.rule.severity) {
                Severity.WARNING -> ruleWarnings.add(match)
                Severity.ERROR -> ruleErrors.add(match)
                else -> {}
            }
        }
        
        // Evaluate metric thresholds
        if (metric != null) {
            val metricMatch = rulesEngine.evaluateMetric(metric)
            if (metricMatch != null) {
                when (metricMatch.rule.severity) {
                    Severity.WARNING -> ruleWarnings.add(metricMatch)
                    Severity.ERROR -> ruleErrors.add(metricMatch)
                    else -> {}
                }
            }
            
            SwingUtilities.invokeLater {
                tableModel.addRow(arrayOf(
                    metric.type.name,
                    formatMetricValue(metric),
                    java.text.SimpleDateFormat("HH:mm:ss")
                        .format(java.util.Date(metric.timestamp))
                ))
                
                while (tableModel.rowCount > 100) {
                    tableModel.removeRow(0)
                }
                
                updateDisplay()
                updateWarningsDisplay()
            }
        } else if (logMatches.isNotEmpty()) {
            SwingUtilities.invokeLater { updateWarningsDisplay() }
        }
    }
    
    private fun formatMetricValue(metric: Metric): String {
        return when (metric.type) {
            MetricType.FPS -> "%.2f".format(metric.value)
            MetricType.FRAME_TIME -> "%.2f ms".format(metric.value)
            MetricType.MEMORY -> "%.2f MB".format(metric.value)
            MetricType.CPU -> "%.1f%%".format(metric.value)
        }
    }
    
    private fun updateDisplay() {
        metricsExtractor.getAverageFps()?.let { fpsLabel.text = "%.1f".format(it) }
        metricsExtractor.getMinFps()?.let { minFpsLabel.text = "%.1f".format(it) }
        
        val fpsMetrics = metricsExtractor.getMetricsByType(MetricType.FPS)
        if (fpsMetrics.isNotEmpty()) {
            maxFpsLabel.text = "%.1f".format(fpsMetrics.maxOf { it.value })
        }
        
        frameDropsLabel.text = metricsExtractor.getFrameDrops().toString()
        
        val memoryMetrics = metricsExtractor.getMetricsByType(MetricType.MEMORY)
        if (memoryMetrics.isNotEmpty()) {
            memoryLabel.text = "%.0f".format(memoryMetrics.map { it.value }.average())
        }
    }
    
    private fun updateWarningsDisplay() {
        val sb = StringBuilder()
        for (error in ruleErrors) {
            sb.appendLine("[ERROR] ${error.rule.name}: ${error.value}")
        }
        for (warning in ruleWarnings) {
            sb.appendLine("[WARN] ${warning.rule.name}: ${warning.value}")
        }
        warningsTextArea.text = sb.toString()
    }
    
    fun generateReport() {
        val deviceId = currentDeviceId ?: "Unknown"
        
        val reportGenerator = ReportGenerator(metricsExtractor, ruleWarnings, ruleErrors)
        val report = reportGenerator.generate(deviceId)
        val markdown = reportGenerator.toMarkdown(report)
        
        JOptionPane.showMessageDialog(
            this,
            JScrollPane(JTextArea(markdown, 20, 60)),
            "Performance Report",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
    
    private fun exportJson() {
        val deviceId = currentDeviceId ?: "Unknown"
        
        val reportGenerator = ReportGenerator(metricsExtractor, ruleWarnings, ruleErrors)
        val report = reportGenerator.generate(deviceId)
        val json = reportGenerator.toJson(report)
        
        val fileChooser = JFileChooser()
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.writeText(json)
        }
    }
    
    private fun clearMetrics() {
        metricsExtractor.clear()
        ruleWarnings.clear()
        ruleErrors.clear()
        tableModel.rowCount = 0
        fpsLabel.text = "--"
        minFpsLabel.text = "--"
        maxFpsLabel.text = "--"
        frameDropsLabel.text = "0"
        memoryLabel.text = "--"
        warningsTextArea.text = ""
    }
}
