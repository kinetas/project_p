
const fs = require('fs');
const { spawn } = require('child_process');
const content = fs.readFileSync(String.raw:\pp\doc\temp\api-mx.xml, 'utf8');

async function main() {
  const proc = spawn(process.platform === 'win32' ? 'npx.cmd' : 'npx', ['-y', '@drawio/mcp'], { stdio: ['pipe', 'pipe', 'inherit'] });
  let buf = '';
  proc.stdout.on('data', d => { buf += d; });
  const send = (obj) => proc.stdin.write(JSON.stringify(obj) + '\n');
  send({jsonrpc:'2.0', id:1, method:'initialize', params:{protocolVersion:'2024-11-05', capabilities:{}, clientInfo:{name:'test', version:'1.0'}}});
  await new Promise(r => setTimeout(r, 2000));
  send({jsonrpc:'2.0', method:'notifications/initialized'});
  send({jsonrpc:'2.0', id:2, method:'tools/call', params:{name:'open_drawio_xml', arguments:{content}}});
  await new Promise(r => setTimeout(r, 5000));
  console.log(buf);
  proc.kill();
}
main().catch(e => { console.error(e); process.exit(1); });
