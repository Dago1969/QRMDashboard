/**
 * Configurazione ambiente frontend per endpoint backend locali.
 */
export const environment = {
  production: false,
  //apiBaseUrl: 'http://localhost:8086/api',
  // FIXME Francesco: mantenere apiBaseUrl relativo; il proxy nginx deve esporre le API sotto il base path dell'app.
  apiBaseUrl: 'api',
  // FIXME Francesco: rendere relativo o passare da proxy anche tenantsApiBaseUrl; localhost nel browser punta alla macchina utente.
  tenantsApiBaseUrl: 'http://localhost:8087/api/tenants'
};
