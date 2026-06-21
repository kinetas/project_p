import json
from pathlib import Path

content = Path(r"e:\pp\doc\temp\api-mx.xml").read_text(encoding="utf-8")
payload = {"content": content}
Path(r"e:\pp\doc\temp\mcp-full-payload.json").write_text(
    json.dumps(payload, ensure_ascii=False), encoding="utf-8"
)
print(f"payload ready: {len(content)} chars")
