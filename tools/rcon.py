import socket, struct, sys

def recv_exact(sock, n):
    buf = b''
    while len(buf) < n:
        c = sock.recv(n - len(buf))
        if not c:
            raise ConnectionError('closed')
        buf += c
    return buf

class Rcon:
    """Minecraft 1.21.4 RCON（与服务端字节码一致）：
       读取端 getIntLE → 全小端
       布局：[len][id][type][body]
       len 必须 == 实际读取字节数 - 4
       type: 2 = COMMAND, 3 = AUTH"""
    def __init__(self, host, port, password, timeout=10):
        self.s = socket.create_connection((host, port), timeout=timeout)
        self.rid = 0
        self._send(3, "AUTH " + password)
        r = self._recv()
        rid, typ = struct.unpack('<ii', r[:8])
        body = r[8:]
        if typ == 2 and body.strip(b'\x00'):
            raise Exception("AUTH failed: " + body.decode('utf-8', 'ignore'))

    def _send(self, typ, body):
        self.rid += 1
        raw = body.encode('utf-8') + b'\x00\x00'
        # len 字段 = id(4) + type(4) + body长度
        payload = struct.pack('<ii', self.rid, typ) + raw
        self.s.sendall(struct.pack('<i', len(payload)) + payload)

    def _recv(self):
        ln = struct.unpack('<i', recv_exact(self.s, 4))[0]
        return recv_exact(self.s, ln)

    def cmd(self, cmd):
        self._send(2, cmd)
        out = b''
        while True:
            try:
                self.s.settimeout(1.5)
                data = self._recv()
            except socket.timeout:
                break
            if len(data) < 8:
                break
            rid, typ = struct.unpack('<ii', data[:8])
            out += data[8:]
        return out.decode('utf-8', 'ignore').replace('\x00', '').strip()

if __name__ == '__main__':
    r = Rcon('127.0.0.1', 25575, 'awakening123')
    args = sys.argv[1:]
    if len(args) == 1:
        # ⚠️ 单命令模式：只输出原始响应体，绝不回显命令本身。
        #    之前带 "$ cmd" 回显，导致 e2e 里 includes('BEACON_PLACED')
        #    匹配到回显而非执行结果 → 假阳性。
        sys.stdout.write(r.cmd(args[0]))
    else:
        for c in args:
            print(f"$ {c}")
            print(r.cmd(c))
            print("-" * 60)
