import { readFileSync } from 'fs';
import { spawn } from 'child_process';

const content = readFileSync('e:/pp/doc/temp/api-mx.xml', 'utf8');
const proc = spawn('npx', ['-y', '@drawio/mcp'], {
  stdio: ['pipe', 'pipe', 'pipe'],
  shell: true,
});

let stdout = '';
proc.stdout.on('data', (d) => { stdout += d.toString(); });

const send = (obj) => proc.stdin.write(JSON.stringify(obj) + '\n');
const wait = (ms) => new Promise((r) => setTimeout(r, ms));

async function main() {
  send({ jsonrpc: '2.0', id: 1, method: 'initialize', params: { protocolVersion: '2024-11-05', capabilities: {}, clientInfo: { name: 'script', version: '1.0' } } });
  await wait(4000);
  send({ jsonrpc: '2.0', method: 'notifications/initialized' });
  await wait(500);
  send({ jsonrpc: '2.0', id: 2, method: 'tools/call', params: { name: 'open_drawio_xml', arguments: { content } } });
  await wait(10000);
  proc.stdin.end();
  await wait(2000);
  const lines = stdout.trim().split('\n');
  for (const line of lines) {
    try {
      const msg = JSON.parse(line);
      if (msg.id === 2 && msg.result) {
        const text = msg.result.content?.[0]?.text || '';
        const match = text.match(/https:\/\/[^\s]+/);
        if (match) console.log('URL:' + match[0]);
        console.log('RESPONSE:' + text);
      }
    } catch {}
  }
  if (!stdout.includes('Draw.io Editor URL')) console.log('RAW:' + stdout.slice(0, 2000));
}

main().catch((e) => { console.error(e); process.exit(1); });
