# Game Performance Tool

Android Studio plugin for measuring game performance.

## Features

- **Real-time Log Capture** - Capture logs from Android devices via ADB
- **Performance Metrics** - Extract FPS, frame time, memory usage
- **Automatic Analysis** - Detect frame drops, GC pauses, performance issues
- **Custom Rules** - Extensible rule engine for custom analysis
- **Reports** - Generate performance reports in JSON or Markdown

## Requirements

- Android Studio 2024.1+
- JDK 17+
- ADB (Android Debug Bridge) installed and in PATH

## Installation

1. Build the plugin:
   ```bash
   ./gradlew build
   ```

2. The plugin will be generated at:
   `build/distributions/GamePerformanceTool-0.1.0.zip`

3. In Android Studio:
   - Go to `Settings > Plugins`
   - Click `Install plugin from disk...`
   - Select the generated ZIP file

## Usage

1. Connect your Android device via USB
2. Open the Game Performance Tool window (View > Tool Windows > GamePerformance)
3. Click "Connect Device" to establish ADB connection
4. Start capturing logs
5. View metrics in the dashboard
6. Generate reports

## Custom Rules

Edit `src/main/resources/rules-default.json` to add custom analysis rules:

```json
{
  "rules": [
    {
      "id": "my_rule",
      "name": "My Custom Rule",
      "pattern": "MyPattern[:\\s]+(\\d+)",
      "metricType": "FPS",
      "severity": "WARNING"
    }
  ]
}
```

## Architecture

```
src/main/kotlin/com/gameperf/plugin/
├── GamePerfPlugin.kt       # Plugin entry point
├── ui/
│   └── GamePerfToolWindowFactory.kt
├── core/
│   ├── AdbConnector.kt    # ADB device connection
│   ├── LogcatReader.kt     # Log capture
│   ├── MetricsExtractor.kt # Performance metrics
│   └── ReportGenerator.kt  # Report generation
└── analysis/
    └── RulesEngine.kt      # Analysis rules
```

## License

MIT
