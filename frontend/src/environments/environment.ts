/**
 * Configurazione ambiente frontend per endpoint backend locali.
 */
export const environment = {
  production: false,
  //apiBaseUrl: 'http://localhost:8086/api',
  // FIXME Francesco: mantenere apiBaseUrl relativo; il proxy nginx deve esporre le API sotto il base path dell'app.
  apiBaseUrl: 'api',
  // FIXME Francesco: rotta relativa condivisa tra proxy locale e routing Traefik del qtm-env.
  tenantsApiBaseUrl: '/api/tenants'
};
