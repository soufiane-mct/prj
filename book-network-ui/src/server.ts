// import {
//   AngularNodeAppEngine,
//   createNodeRequestHandler,
//   isMainModule,
//   writeResponseToNodeResponse,
// } from '@angular/ssr/node';
// import express from 'express';
// import { dirname, resolve } from 'path';
// import { fileURLToPath } from 'url';

// const serverDistFolder = dirname(fileURLToPath(import.meta.url));
// const browserDistFolder = resolve(serverDistFolder, '../browser');

// const app = express();
// const angularApp = new AngularNodeAppEngine();

// // Add request logging middleware
// app.use((req, res, next) => {
//   console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
//   next();
// });

// /**
//  * Serve static files from /browser
//  */
// app.use(
//   express.static(browserDistFolder, {
//     maxAge: '1y',
//     index: false,
//     redirect: false,
//   }),
// );

// // Add error handling middleware
// app.use((err: any, req: any, res: any, next: any) => {
//   console.error('Server error:', err);
//   res.status(500).send('Internal Server Error');
// });

// /**
//  * Handle all other requests by rendering the Angular application.
//  */
// app.get('*', (req, res, next) => {
//   console.log('Handling request for:', req.url);
  
//   // Skip favicon.ico to prevent unnecessary processing
//   if (req.url === '/favicon.ico') {
//     return res.status(204).end();
//   }

//   // Let Angular handle the request
//   return angularApp
//     .handle(req)
//     .then((response) => {
//       if (response) {
//         return writeResponseToNodeResponse(response, res);
//       }
//       // If no response, try to serve index.html
//       return res.sendFile('index.html', { root: browserDistFolder });
//     })
//     .catch((err) => {
//       console.error('Error handling request:', err);
//       return next(err);
//     });
// });

// /**
//  * Start the server if this module is the main entry point.
//  */
// if (isMainModule(import.meta.url)) {
//   const port = process.env['PORT'] || 4000;
//   app.listen(port, () => {
//     console.log(`Node Express server listening on http://localhost:${port}`);
//     console.log('Serving static files from:', browserDistFolder);
//   });
// }

// /**
//  * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
//  */
// export const reqHandler = createNodeRequestHandler(app);
