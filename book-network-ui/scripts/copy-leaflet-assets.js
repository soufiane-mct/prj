const fs = require('fs-extra');
const path = require('path');

// Source and destination paths
const srcDir = path.join('node_modules', 'leaflet', 'dist');
const destDir = path.join('src', 'assets', 'leaflet');

// Ensure destination directory exists
fs.ensureDirSync(destDir);

// Copy CSS file
fs.copyFileSync(
  path.join(srcDir, 'leaflet.css'),
  path.join(destDir, 'leaflet.css')
);

// Copy images directory
fs.copySync(
  path.join(srcDir, 'images'),
  path.join(destDir, 'images')
);

console.log('Leaflet assets copied successfully!');
