import { Component, inject } from '@angular/core';
import { AsyncPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, Observable, of, startWith, switchMap } from 'rxjs';
import { UserService } from '../user.service';
import { User } from '../../api/models';

interface UserViewState {
  loading: boolean;
  user: User | null;
  error: string | null;
}

@Component({
  selector: 'app-user-view',
  standalone: true,
  imports: [AsyncPipe, DatePipe, RouterLink],
  templateUrl: './user-view.component.html',
  styleUrl: './user-view.component.scss'
})
export class UserViewComponent {
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);

  readonly state$: Observable<UserViewState> = this.route.paramMap.pipe(
    switchMap(params => {
      const id = params.get('id')!;
      return this.userService.getUser(id).pipe(
        map(user => ({ loading: false, user, error: null })),
        catchError(() => of({ loading: false, user: null, error: 'Utilisateur introuvable.' })),
        startWith({ loading: true, user: null, error: null })
      );
    })
  );
}
