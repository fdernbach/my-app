import { Component, inject } from '@angular/core';
import { AsyncPipe, DatePipe, JsonPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, map, Observable, of, startWith, switchMap } from 'rxjs';
import { CourseService } from '../course.service';
import { Course } from '../../api/models';

interface CourseViewState {
  loading: boolean;
  course: Course | null;
  error: string | null;
}

@Component({
  selector: 'app-course-view',
  standalone: true,
  imports: [AsyncPipe, DatePipe, JsonPipe, RouterLink],
  templateUrl: './course-view.component.html',
  styleUrl: './course-view.component.scss'
})
export class CourseViewComponent {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);

  readonly state$: Observable<CourseViewState> = this.route.paramMap.pipe(
    switchMap(params => {
      const id = Number(params.get('id'));
      return this.courseService.getCourse(id).pipe(
        map(course => ({ loading: false, course, error: null })),
        catchError(() => of({ loading: false, course: null, error: 'Cours introuvable.' })),
        startWith({ loading: true, course: null, error: null })
      );
    })
  );
}
