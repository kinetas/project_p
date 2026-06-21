"""Generate draw.io browser URL from api-mx.xml (same as MCP open_drawio_xml)."""
import base64
import json
import urllib.parse
import webbrowser
import zlib
from pathlib import Path

xml = Path(r"e:\pp\doc\temp\api-mx.xml").read_text(encoding="utf-8")

compressor = zlib.compressobj(9, zlib.DEFLATED, -zlib.MAX_WBITS, 8, 0)
compressed = compressor.compress(xml.encode("utf-8")) + compressor.flush()
encoded = base64.b64encode(compressed).decode("ascii")

payload = json.dumps(
    {"type": "xml", "compressed": True, "data": encoded},
    separators=(",", ":"),
)
url = (
    "https://app.diagrams.net/?grid=0&pv=0&border=10&edit=_blank#create="
    + urllib.parse.quote(payload, safe="")
)
Path(r"e:\pp\doc\temp\api-drawio-url.txt").write_text(url, encoding="utf-8")
print(url[:120] + "...")
print(f"URL length: {len(url)}")
webbrowser.open(url)
