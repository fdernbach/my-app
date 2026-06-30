import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

// Injecte les credentials Basic Auth sur toutes les requêtes vers l'API (dev uniquement).
export const basicAuthInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(environment.apiUrl)) {
    return next(req);
  }
  return next(req.clone({
    setHeaders: { Authorization: `Basic ${btoa('admin:admin')}` }
  }));
};
