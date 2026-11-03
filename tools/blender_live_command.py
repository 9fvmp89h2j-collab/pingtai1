import json
import socket
import sys

if len(sys.argv) > 1:
    with open(sys.argv[1], "r", encoding="utf-8") as handle:
        code = handle.read()
else:
    code = sys.stdin.read()
payload = json.dumps({"type": "execute_code", "params": {"code": code}}).encode("utf-8")
with socket.create_connection(("127.0.0.1", 9876), timeout=10) as sock:
    sock.sendall(payload)
    sock.settimeout(30)
    chunks = []
    while True:
        try:
            part = sock.recv(65536)
        except socket.timeout:
            break
        if not part:
            break
        chunks.append(part)
        try:
            json.loads(b"".join(chunks))
            break
        except json.JSONDecodeError:
            pass
print(b"".join(chunks).decode("utf-8"))
