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

import urllib.parse

ALLOWED_DOWNLOADS = {
    "/shmall.apk": os.path.realpath(os.path.join(DIRECTORY, "shmall.apk")),
    "/shmall.apk.zip": os.path.realpath(os.path.join(DIRECTORY, "shmall.apk.zip")),
    "/downloads/WeedGuru.apk": os.path.realpath(os.path.join(DIRECTORY, "downloads", "WeedGuru.apk")),
}

class ApkDownloadHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        parsed_path = urllib.parse.urlparse(self.path).path

        if parsed_path in ("/", "/index.html"):
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
            self.send_header("X-Content-Type-Options", "nosniff")
            content = HTML_TEMPLATE.encode("utf-8")
            self.send_header("Content-Length", str(len(content)))
            self.end_headers()
            self.wfile.write(content)
            return

        filepath = ALLOWED_DOWNLOADS.get(parsed_path)
        if not filepath or not os.path.isfile(filepath):
            self.send_error(404, "File not found")
            return

        size = os.path.getsize(filepath)
        filename = os.path.basename(filepath)
        content_type = "application/vnd.android.package-archive" if filepath.endswith(".apk") else "application/zip"

        start = 0
        end = size - 1
        status_code = 200
        range_header = self.headers.get("Range")
        if range_header and range_header.startswith("bytes="):
            spec = range_header[len("bytes="):].split(",", 1)[0].strip()
            parts = spec.split("-", 1)
            if len(parts) == 2:
                try:
                    if parts[0]:
                        start = int(parts[0])
                        end = int(parts[1]) if parts[1] else size - 1
                    elif parts[1]:
                        suffix = int(parts[1])
                        start = max(0, size - suffix)
                        end = size - 1
                    if start < 0 or end >= size or start > end:
                        self.send_response(416)
                        self.send_header("Content-Range", f"bytes */{size}")
                        self.end_headers()
                        return
                    status_code = 206
                except ValueError:
                    start, end, status_code = 0, size - 1, 200

        length = (end - start) + 1
        self.send_response(status_code)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Disposition", f'attachment; filename="{filename}"')
        self.send_header("Accept-Ranges", "bytes")
        self.send_header("X-Content-Type-Options", "nosniff")
        if status_code == 206:
            self.send_header("Content-Range", f"bytes {start}-{end}/{size}")
        self.send_header("Content-Length", str(length))
        self.end_headers()

        remaining = length
        with open(filepath, "rb") as f:
            f.seek(start)
            while remaining > 0:
                chunk = f.read(min(65536, remaining))
                if not chunk:
                    break
                self.wfile.write(chunk)
                remaining -= len(chunk)


class ThreadingApkServer(http.server.ThreadingHTTPServer):
    allow_reuse_address = True


if __name__ == "__main__":
    with ThreadingApkServer(("0.0.0.0", PORT), ApkDownloadHandler) as httpd:
        print(f"Serving allowlisted APK downloads on port {PORT}...")
        httpd.serve_forever()
