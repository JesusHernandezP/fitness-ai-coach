const runtimeApiBaseUrl =
  typeof window !== 'undefined'
    ? (window as Window & { __fitnessEnv?: { apiBaseUrl?: string } }).__fitnessEnv
        ?.apiBaseUrl ?? '/api'
    : '/api';

export const environment = {
  production: true,
  apiBaseUrl: runtimeApiBaseUrl
};
