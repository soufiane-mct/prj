"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const path_1 = require("path");
const fs_1 = require("fs");
// Create Express server
const app = (0, express_1.default)();
const port = process.env['PORT'] || 4000;
const distFolder = (0, path_1.join)(process.cwd(), 'dist/book-network-ui/browser');
const indexPath = (0, path_1.join)(distFolder, 'index.html');
// Serve static files
app.use(express_1.default.static(distFolder));
// Handle all other routes
app.get('*', (req, res) => {
    if (!(0, fs_1.existsSync)(indexPath)) {
        return res.status(500).send('index.html not found. Please build the Angular app first.');
    }
    res.sendFile(indexPath);
});
// Start the server
app.listen(port, () => {
    console.log(`Server running on http://localhost:${port}`);
});
// Export for testing
exports.default = app;
