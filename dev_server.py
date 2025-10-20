#!/usr/bin/env python3
"""
Tiny local development server that serves a /resume JSON response on 127.0.0.1:3000
Run: python3 dev_server.py

This is for local emulator testing (Android AVD uses 10.0.2.2 -> host's 127.0.0.1).
"""
from http.server import BaseHTTPRequestHandler, HTTPServer
import json

RESUME = {
    "name": "Santosh Example",
    "title": "Android Developer",
    "summary": "A concise summary about me.",
    "skills": [
        {"name": "Kotlin", "level": "Expert"},
        {"name": "Jetpack Compose", "level": "Advanced"}
    ]
}

class ResumeHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/resume' or self.path == '/resume/':
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.end_headers()
            payload = json.dumps(RESUME).encode('utf-8')
            self.wfile.write(payload)
        else:
            self.send_response(404)
            self.send_header('Content-Type', 'text/plain')
            self.end_headers()
            self.wfile.write(b'Not found')

    def log_message(self, format, *args):
        # Simple logging to stdout
        print("[dev_server] " + (format % args))

if __name__ == '__main__':
    server_address = ('127.0.0.1', 3000)
    print(f"Starting dev server at http://{server_address[0]}:{server_address[1]}/resume")
    httpd = HTTPServer(server_address, ResumeHandler)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print('\nShutting down dev server')
        httpd.server_close()

