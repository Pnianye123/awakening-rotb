#!/usr/bin/env python3
"""《方块之下》测试包临时下载服务（只读、只服务这个目录）。"""
import http.server, socketserver, os, functools

DIR = "/home/yifan/file-share/public/files/awakening-rotb/v1.0"
PORT = 8099

class H(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *a, **kw):
        super().__init__(*a, directory=DIR, **kw)
    def log_message(self, fmt, *a):
        pass

socketserver.TCPServer.allow_reuse_address = True
with socketserver.TCPServer(("0.0.0.0", PORT), H) as httpd:
    print(f"serving {DIR} on :{PORT}", flush=True)
    httpd.serve_forever()
