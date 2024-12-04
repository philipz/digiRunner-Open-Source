// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
export const environment = {
  production: false,
  hmr: false,
  isv4: true,

  reportUrl: location.protocol + '//' + location.hostname + ':8601',
  // apiUrl: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : ''),
  // apiUrl: 'https://203.69.248.109:38452',
  // dpPath: 'https://203.69.248.109:38452',
  // netApiUrl: 'https://192.168.1.156:48083',
  // iframeDomain: 'http://192.168.1.149',
  // dpPath: location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '')
  // subPath: 'tsmpac3/',


  // http://10.20.30.88:18442/dgrv4/
  apiUrl: 'https://10.20.30.88:19442',
  dpPath: 'https://10.20.30.88:19442',
  netApiUrl: 'https://10.20.30.88:19442',

  cusHostName: `${location.protocol}//${location.hostname}:3003`,
  // iframeDomain: 'http://192.168.1.149',

  // apiUrl: 'https://localhost:18080',
  // dpPath: 'https://localhost:18080',
  // netApiUrl: 'https://localhost:18080',
};

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/plugins/zone-error';  // Included with Angular CLI.
