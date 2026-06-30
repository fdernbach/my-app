import { Component, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, of, startWith, switchMap } from 'rxjs';
import { UserService } from '../user.service';
import { User, UserPage } from '../../api/models';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

interface UserListState {
  loading: boolean;
  page: UserPage | null;
  error: string | null;
}

interface Query { page: number; size: number; }

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [AsyncPipe, RouterLink, ConfirmDialogComponent],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss'
})
export class UserListComponent {
  private readonly userService = inject(UserService);

  readonly pageSizeOptions = [1, 5, 10, 20];

  private readonly query$ = new BehaviorSubject<Query>({ page: 0, size: 10 });

  readonly state$: Observable<UserListState> = this.query$.pipe(
    switchMap(({ page, size }) => this.userService.getUsers(page, size).pipe(
      map(result => ({ loading: false, page: result, error: null })),
      catchError(() => of({ loading: false, page: null, error: 'Impossible de charger les utilisateurs.' })),
      startWith({ loading: true, page: null, error: null })
    ))
  );

  readonly pendingDelete = signal<User | null>(null);
  readonly deleting = signal(false);
  readonly deleteError = signal<string | null>(null);

  get currentPage(): number { return this.query$.value.page; }
  get currentSize(): number { return this.query$.value.size; }

  goToPage(page: number): void {
    this.query$.next({ ...this.query$.value, page });
  }

  changeSize(size: number): void {
    this.query$.next({ page: 0, size });
  }

  openDeleteDialog(user: User): void {
    this.deleteError.set(null);
    this.pendingDelete.set(user);
  }

  cancelDelete(): void {
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const user = this.pendingDelete();
    if (!user) return;

    this.deleting.set(true);
    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.pendingDelete.set(null);
        this.deleting.set(false);
        this.query$.next({ ...this.query$.value });
      },
      error: () => {
        this.deleteError.set('Erreur lors de la suppression.');
        this.deleting.set(false);
      }
    });
  }
}
