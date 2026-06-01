const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 8090;
const DIR = __dirname;

http.createServer((req, res) => {
  const filePath = path.join(DIR, req.url === '/' ? 'social-hub-mockup-1.html' : req.url);
  const ext = path.extname(filePath);
  const contentType = ext === '.html' ? 'text/html' : 'text/plain';

  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404);
      res.end('Not found');
      return;
    }
    res.writeHead(200, { 'Content-Type': contentType });
    res.end(data);
  });
}).listen(PORT, () => console.log(`Mockup server on http://localhost:${PORT}`));
