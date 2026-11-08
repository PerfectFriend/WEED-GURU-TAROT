import http.server
import socketserver
import os
import mimetypes

PORT = 3000
DIRECTORY = "/app/applet"

HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Weed Guru (Шмаль) — Скачать APK</title>
    <style>
        :root {
            --primary: #39FF14;
            --secondary: #00FFFF;
            --bg: #0A0A12;
            --card-bg: #141422;
            --text: #F0F0FF;
            --muted: #8888AA;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background: radial-gradient(circle at 50% 20%, #181A2E 0%, #0A0A12 80%);
            color: var(--text);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 24px;
        }
        .container {
            max-width: 520px;
            width: 100%;
            background: var(--card-bg);
            border: 1px solid rgba(57, 255, 20, 0.3);
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.8), 0 0 20px rgba(57, 255, 20, 0.15);
            padding: 32px 24px;
            text-align: center;
        }
        .logo-badge {
            display: inline-block;
            font-size: 48px;
            margin-bottom: 12px;
            filter: drop-shadow(0 0 10px rgba(57, 255, 20, 0.5));
        }
        h1 {
            font-size: 26px;
            font-weight: 800;
            color: var(--primary);
            letter-spacing: 1px;
            margin-bottom: 6px;
            text-transform: uppercase;
        }
        .subtitle {
            font-size: 14px;
            color: var(--secondary);
            margin-bottom: 24px;
            font-family: monospace;
        }
        .meta-box {
            background: rgba(0, 0, 0, 0.4);
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: 12px;
            padding: 14px;
            margin-bottom: 24px;
            font-size: 13px;
            display: flex;
            justify-content: space-around;
            color: var(--muted);
        }
        .meta-item strong {
            display: block;
            color: #FFF;
            font-size: 15px;
            margin-top: 2px;
        }
        .btn {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            width: 100%;
            padding: 16px 20px;
            background: linear-gradient(135deg, #39FF14 0%, #00CC44 100%);
            color: #0A0A12;
            font-size: 17px;
            font-weight: 800;
            border-radius: 14px;
            text-decoration: none;
            transition: all 0.2s ease;
            box-shadow: 0 4px 20px rgba(57, 255, 20, 0.4);
            margin-bottom: 14px;
            border: none;
            cursor: pointer;
        }
        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 25px rgba(57, 255, 20, 0.6);
        }
        .btn-secondary {
            background: rgba(255, 255, 255, 0.08);
            color: #FFF;
            border: 1px solid rgba(255, 255, 255, 0.15);
            box-shadow: none;
            font-size: 14px;
            padding: 12px;
        }
        .btn-secondary:hover {
            background: rgba(255, 255, 255, 0.15);
            box-shadow: none;
        }
        .instructions {
            margin-top: 24px;
            text-align: left;
            background: rgba(255, 255, 255, 0.03);
            border-radius: 12px;
            padding: 16px;
            font-size: 12px;
            line-height: 1.6;
            color: var(--muted);
        }
        .instructions h3 {
            color: #FFF;
            font-size: 13px;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 6px;
        }
        .instructions ol {
            padding-left: 20px;
        }
        .footer {
            margin-top: 24px;
            font-size: 11px;
            color: #555577;
            font-family: monospace;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo-badge">🌿</div>
        <h1>ШМАЛЬ // WEED GURU</h1>
        <div class="subtitle">CYBER TAROT & LUNAR ORACLE v1.0</div>

        <div class="meta-box">
            <div class="meta-item">
                Формат
                <strong>.APK (Android)</strong>
            </div>
            <div class="meta-item">
                Размер
                <strong>~114 MB</strong>
            </div>
            <div class="meta-item">
                Архитектура
                <strong>Universal</strong>
            </div>
        </div>

        <a href="/shmall.apk" download="shmall.apk" class="btn">
            <span>📥 СКАЧАТЬ SHMALL.APK</span>
        </a>

        <a href="/shmall.apk.zip" download="shmall.apk.zip" class="btn btn-secondary">
            <span>📦 Скачать в ZIP-архиве (shmall.apk.zip)</span>
        </a>

        <a href="/downloads/WeedGuru.apk" download="WeedGuru.apk" class="btn btn-secondary">
            <span>🌿 Зеркало 2: WeedGuru.apk</span>
        </a>

        <div class="instructions">
            <h3>📱 Как установить на Android:</h3>
            <ol>
                <li>Нажмите кнопку <strong>«Скачать shmall.apk»</strong> выше.</li>
                <li>После завершения загрузки откройте скачанный файл.</li>
                <li>Если система безопасности спросит разрешение, нажмите <em>«Настройки» → «Разрешить установку из этого источника»</em>.</li>
                <li>Нажмите <strong>«Установить»</strong> и запускайте приложение!</li>
            </ol>
        </div>

        <div class="footer">
            Build Target: Android 7.0+ (API 24-36) • Firebase & Neural Matrix
        </div>
    </div>
</body>
</html>
"""

class ApkDownloadHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def do_GET(self):
        if self.path in ["/", "/index.html"]:
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
            content = HTML_TEMPLATE.encode("utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return

        if self.path.endswith(".apk") or self.path.endswith(".zip"):
            filename = os.path.basename(self.path.split("?")[0])
            filepath = os.path.join(DIRECTORY, filename)
            if not os.path.exists(filepath):
                for alt in ["/app/applet/" + filename, "/app/applet/downloads/" + filename, "/app/applet/apk/" + filename]:
                    if os.path.exists(alt):
                        filepath = alt
                        break

            if os.path.exists(filepath):
                size = os.path.getsize(filepath)
                contentType = "application/vnd.android.package-archive" if filepath.endswith(".apk") else "application/zip"
                self.send_response(200)
                self.send_header("Content-Type", contentType)
                self.send_header("Content-Disposition", f'attachment; filename="{filename}"')
                self.send_header("Content-Length", str(size))
                self.send_header("Accept-Ranges", "bytes")
                self.end_headers()
                with open(filepath, "rb") as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
                return

        return super().do_GET()

if __name__ == "__main__":
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("0.0.0.0", PORT), ApkDownloadHandler) as httpd:
        print(f"Serving APK downloads on port {PORT}...")
        httpd.serve_forever()
