"""Parse doc/api.md and generate doc/api.drawio — full API spec table."""
import html
import re
from pathlib import Path

API_MD = Path(r"e:\pp\doc\api.md")
OUT_DRAWIO = Path(r"e:\pp\doc\api.drawio")
OUT_MX = Path(r"e:\pp\doc\temp\api-mx.xml")

API_COLS = [
    "기능명",
    "URL",
    "서비스명",
    "엔드포인트",
    "요청방식",
    "요청파라미터",
    "요청헤더",
    "요청바디",
    "응답형식",
]
API_WIDTHS = [95, 175, 88, 195, 58, 175, 130, 155, 120]
ROW_H = 52
HEADER_H = 36


def esc(s):
    return html.escape(str(s), quote=True)


def cell_style(header=False):
    if header:
        return (
            "text;html=1;align=center;verticalAlign=middle;"
            "fillColor=#4472C4;fontColor=#ffffff;fontStyle=1;whiteSpace=wrap;"
        )
    return "text;html=1;align=left;verticalAlign=middle;spacingLeft=4;whiteSpace=wrap;fontSize=10;"


def field(block, key):
    m = re.search(rf"\| \*\*{re.escape(key)}\*\* \| (.+?) \|", block)
    return m.group(1).strip() if m else ""


def param_section_text(block):
    header = re.search(r"\*\*요청파라미터\*\*(?: \(([^)]+)\))?\s*\n", block)
    if not header:
        return None, ""
    loc_hint = header.group(1) or ""
    rest = block[header.end() :]
    stop = re.search(r"\n\*\*(?:요청헤더|요청바디|응답형식|에러|설명)\*\*", rest)
    return loc_hint, rest[: stop.start() if stop else len(rest)].strip()


def summarize_params(block):
    direct = field(block, "요청파라미터")
    if direct and direct not in ("없음", ""):
        return direct.replace("`", "")

    loc_hint, param_text = param_section_text(block)
    if not param_text:
        return "없음"

    if not param_text.startswith("|"):
        return param_text.replace("`", "")[:120]

    names = []
    for line in param_text.split("\n"):
        if not line.strip().startswith("|"):
            continue
        cols = [c.strip() for c in line.strip("|").split("|")]
        if not cols or cols[0] in ("파라미터", "---", ""):
            continue
        loc = ""
        if len(cols) >= 3 and cols[2] in ("Path", "Query"):
            loc = f"({cols[2]})"
        elif loc_hint:
            loc = f"({loc_hint})"
        names.append(f"{cols[0]}{loc}")

    if names:
        text = ", ".join(names)
        return text[:120] + ("…" if len(text) > 120 else "")
    return "없음"


def summarize_body(block):
    direct = field(block, "요청바디")
    if direct and direct != "없음":
        return direct

    body_m = re.search(
        r"\*\*요청바디\*\*\s*\n\n```(?:json)?\s*\n([\s\S]*?)```",
        block,
    )
    if not body_m:
        return "없음"

    raw = body_m.group(1).strip()
    if raw.startswith("{"):
        keys = re.findall(r'"(\w+)"\s*:', raw)
        if keys:
            return "{" + ", ".join(keys) + "}"
    if raw.startswith("["):
        return "Array"
    return raw[:80].replace("\n", " ") + ("…" if len(raw) > 80 else "")


def summarize_response(block):
    resp_m = re.search(r"\*\*응답형식\*\*\s*(.+?)(?:\n\n|\n```|\n\*\*에러|\n\*\*설명)", block, re.DOTALL)
    if not resp_m:
        return ""
    line = resp_m.group(1).strip().replace("`", "")
    line = re.sub(r"\s+", " ", line)
    if line.startswith("json") or line.startswith("{"):
        return "JSON"
    if len(line) > 100:
        line = line[:97] + "…"
    return line


def parse_endpoint(ep_raw):
    ep_raw = ep_raw.strip()
    if "또는" in ep_raw:
        results = []
        for part in ep_raw.split("또는"):
            results.extend(parse_endpoint(part.strip()))
        return results
    m = re.match(r"([A-Z]+)\s+`([^`]+)`", ep_raw)
    if m:
        return [(m.group(1), m.group(2))]
    return []


def parse_api_blocks(section_text):
    blocks = re.split(r"\n---\n", section_text)
    items = []
    current_page = ""

    for block in blocks:
        page_url = re.search(r"\*\*페이지 URL\*\* `([^`]+)`", block)
        if page_url:
            current_page = page_url.group(1)

        ep_raw = field(block, "엔드포인트")
        if not ep_raw:
            continue

        title_m = re.search(r"^### (.+)$", block, re.MULTILINE)
        func_title = title_m.group(1).strip() if title_m else ""
        name = field(block, "기능명") or func_title or field(block, "페이지명")
        url = field(block, "URL").replace("`", "")
        service = field(block, "서비스명")
        method = field(block, "요청방식")
        headers = field(block, "요청헤더") or "없음"
        params = summarize_params(block)
        body = summarize_body(block)
        response = summarize_response(block)

        for m, path in parse_endpoint(ep_raw):
            items.append(
                {
                    "name": name,
                    "url": url or path,
                    "service": service,
                    "endpoint": f"{m} {path}",
                    "method": method or m,
                    "params": params,
                    "headers": headers.replace("`", ""),
                    "body": body,
                    "response": response,
                    "page": current_page,
                }
            )
    return items


def build_table(table_id, x, y, col_names, col_widths, rows):
    row_h = ROW_H
    header_h = HEADER_H
    table_w = sum(col_widths)
    table_h = header_h + row_h * len(rows)
    parts = [
        f'<mxCell id="{table_id}" style="shape=table;childLayout=tableLayout;startSize=0;collapsible=0;fillColor=none;strokeColor=#666666;html=1;" vertex="1" parent="1">',
        f'  <mxGeometry x="{x}" y="{y}" width="{table_w}" height="{table_h}" as="geometry"/>',
        "</mxCell>",
    ]

    parts.append(
        f'<mxCell id="{table_id}-r0" style="shape=tableRow;horizontal=0;startSize=0;collapsible=0;" vertex="1" parent="{table_id}">'
        f'<mxGeometry width="{table_w}" height="{header_h}" as="geometry"/></mxCell>'
    )
    cx = 0
    for i, (name, w) in enumerate(zip(col_names, col_widths)):
        geo = f'width="{w}" height="{header_h}"' if i == 0 else f'x="{cx}" width="{w}" height="{header_h}"'
        parts.append(
            f'<mxCell id="{table_id}-h{i}" value="{esc(name)}" style="{cell_style(header=True)}" vertex="1" parent="{table_id}-r0">'
            f'<mxGeometry {geo} as="geometry"/></mxCell>'
        )
        cx += w

    for ri, row in enumerate(rows, 1):
        parts.append(
            f'<mxCell id="{table_id}-r{ri}" style="shape=tableRow;horizontal=0;startSize=0;collapsible=0;" vertex="1" parent="{table_id}">'
            f'<mxGeometry y="{header_h + (ri - 1) * row_h}" width="{table_w}" height="{row_h}" as="geometry"/></mxCell>'
        )
        cx = 0
        for ci, val in enumerate(row):
            geo = f'width="{col_widths[ci]}" height="{row_h}"' if ci == 0 else f'x="{cx}" width="{col_widths[ci]}" height="{row_h}"'
            parts.append(
                f'<mxCell id="{table_id}-c{ri}_{ci}" value="{esc(val)}" style="{cell_style()}" vertex="1" parent="{table_id}-r{ri}">'
                f'<mxGeometry {geo} as="geometry"/></mxCell>'
            )
            cx += col_widths[ci]

    return "\n".join(parts), table_w, table_h


def item_to_row(it):
    return (
        it["name"],
        it["url"],
        it["service"],
        it["endpoint"],
        it["method"],
        it["params"],
        it["headers"],
        it["body"],
        it["response"],
    )


def main():
    text = API_MD.read_text(encoding="utf-8")
    parts = text.split("# 프론트 API")
    backend_text = parts[0].split("# 백엔드 API")[1] if "# 백엔드 API" in parts[0] else parts[0]
    backend_items = parse_api_blocks(backend_text)
    frontend_items = parse_api_blocks(parts[1] if len(parts) > 1 else "")

    seen = set()
    backend_rows = []
    for it in backend_items:
        key = (it["method"], it["url"])
        if key in seen:
            continue
        seen.add(key)
        backend_rows.append(item_to_row(it))

    frontend_rows = [item_to_row(it) for it in frontend_items]

    y = 120
    be_table, be_w, be_h = build_table("tbl-be", 40, y, API_COLS, API_WIDTHS, backend_rows)
    y2 = y + be_h + 40
    fe_table, fe_w, fe_h = build_table("tbl-fe", 40, y2, API_COLS, API_WIDTHS, frontend_rows)

    table_w = max(be_w, fe_w)
    page_w = table_w + 80
    page_h = y2 + fe_h + 60

    body = f"""
        <mxCell id="title" value="API 명세서 — doc/api.md" style="text;html=1;fontSize=20;fontStyle=1;align=left;" vertex="1" parent="1">
          <mxGeometry x="40" y="16" width="500" height="28" as="geometry"/>
        </mxCell>
        <mxCell id="info" value="Base URL: http://localhost:8080  |  인증: 세션 쿠키 (JSESSIONID)&#xa;기능명 · URL · 서비스명 · 엔드포인트 · 요청방식 · 요청파라미터 · 요청헤더 · 요청바디 · 응답형식" style="text;html=1;align=left;fillColor=#f5f5f5;strokeColor=#cccccc;spacingLeft=8;spacingTop=4;" vertex="1" parent="1">
          <mxGeometry x="40" y="48" width="{table_w}" height="42" as="geometry"/>
        </mxCell>
        <mxCell id="sec-be" value="백엔드 API ({len(backend_rows)}개)" style="text;html=1;fontSize=14;fontStyle=1;align=left;" vertex="1" parent="1">
          <mxGeometry x="40" y="98" width="300" height="18" as="geometry"/>
        </mxCell>
        {be_table}
        <mxCell id="sec-fe" value="프론트 API — 페이지별 백엔드 호출 ({len(frontend_rows)}개)" style="text;html=1;fontSize=14;fontStyle=1;align=left;" vertex="1" parent="1">
          <mxGeometry x="40" y="{y2 - 22}" width="500" height="18" as="geometry"/>
        </mxCell>
        {fe_table}
    """

    mx = f"""<mxfile host="app.diagrams.net" version="29.6.6">
  <diagram name="API 명세서" id="api-doc">
    <mxGraphModel dx="1800" dy="1400" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="{page_w}" pageHeight="{page_h}" math="0" shadow="0" adaptiveColors="auto">
      <root>
        <mxCell id="0"/>
        <mxCell id="1" parent="0"/>
        {body}
      </root>
    </mxGraphModel>
  </diagram>
</mxfile>"""

    OUT_DRAWIO.write_text(mx, encoding="utf-8")
    m = re.search(r"(<mxGraphModel[\s\S]*?</mxGraphModel>)", mx)
    OUT_MX.write_text(m.group(1), encoding="utf-8")
    print(f"backend={len(backend_rows)} frontend={len(frontend_rows)}")
    print(f"Written {OUT_DRAWIO} ({len(mx)} bytes)")


if __name__ == "__main__":
    main()
