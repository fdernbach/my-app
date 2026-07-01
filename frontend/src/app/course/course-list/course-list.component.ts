import { Component, inject, signal } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, map, Observable, of, startWith, switchMap } from 'rxjs';
import { CourseService } from '../course.service';
import { Course, CoursePage } from '../../api/models';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

interface CourseListState {
  loading: boolean;
  page: CoursePage | null;
  error: string | null;
}

interface Query { page: number; size: number; }

@Component({
  selector: 'app-course-list',
  standalone: true,
  imports: [AsyncPipe, RouterLink, ConfirmDialogComponent],
  templateUrl: './course-list.component.html',
  styleUrl: './course-list.component.scss'
})
export class CourseListComponent {
  private readonly courseService = inject(CourseService);

  readonly pageSizeOptions = [1, 5, 10, 20];

  private readonly query$ = new BehaviorSubject<Query>({ page: 0, size: 10 });

  readonly state$: Observable<CourseListState> = this.query$.pipe(
    switchMap(({ page, size }) => this.courseService.getCourses(page, size).pipe(
      map(result => ({ loading: false, page: result, error: null })),
      catchError(() => of({ loading: false, page: null, error: 'Impossible de charger les cours.' })),
      startWith({ loading: true, page: null, error: null })
    ))
  );

  readonly pendingDelete = signal<Course | null>(null);
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

  openDeleteDialog(course: Course): void {
    this.deleteError.set(null);
    this.pendingDelete.set(course);
  }

  cancelDelete(): void {
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const course = this.pendingDelete();
    if (!course) return;

    this.deleting.set(true);
    this.courseService.deleteCourse(course.id).subscribe({
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
