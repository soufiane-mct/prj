export const config = {
  // routes: [
  //   '/',
  //   '/products',
  //   '/login',
  //   '/register',
  //   '/activate-account'
  // ],
  discoverRoutes: false,
  postProcess(html: string) {
    return html.replace(
      '<head>',
      '<head><meta name="description" content="Book Network - Find and share books with your community">'
    );
  },
};
