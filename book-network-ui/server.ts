import express from 'express';
import { join } from 'path';
import { existsSync } from 'fs';

// Create Express server
const app = express();
const port = process.env['PORT'] || 4000;
const distFolder = join(process.cwd(), 'dist/book-network-ui/browser');
const indexPath = join(distFolder, 'index.html');

// Serve static files
app.use(express.static(distFolder));

// Handle all other routes
app.get('*', (req, res) => {
  if (!existsSync(indexPath)) {
    return res.status(500).send('index.html not found. Please build the Angular app first.');
  }
  res.sendFile(indexPath);
});

// Start the server
app.listen(port, () => {
  console.log(`Server running on http://localhost:${port}`);
});

// Export for testing
export default app;
