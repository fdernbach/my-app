import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '../course.service';
import { CourseRequest } from '../../api/models';

@Component({
  selector: 'app-course-modify',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './course-modify.component.html',
  styleUrl: './course-modify.component.scss'
})
export class CourseModifyComponent implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly courseId = signal(0);

  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);

  readonly form = new FormGroup({
    title:        new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(256)] }),
    author:       new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    documentJson: new FormControl('', { nonNullable: true }),
  });

  ngOnInit(): void {
    this.courseId.set(Number(this.route.snapshot.paramMap.get('id')));
    this.courseService.getCourse(this.courseId()).subscribe({
      next: course => {
        this.form.patchValue({
          title:  course.title,
          author: course.author,
          documentJson: course.documentJson
            ? JSON.stringify(course.documentJson, null, 2)
            : '',
        });
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Cours introuvable.');
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { title, author, documentJson } = this.form.getRawValue();

    let parsedDocument: { [key: string]: any } | undefined;
    if (documentJson.trim()) {
      try {
        parsedDocument = JSON.parse(documentJson);
      } catch {
        this.submitError.set('Le document JSON est invalide.');
        return;
      }
    }

    const request: CourseRequest = {
      title,
      author,
      ...(parsedDocument !== undefined && { documentJson: parsedDocument }),
    };

    this.submitting.set(true);
    this.submitError.set(null);

    this.courseService.updateCourse(this.courseId(), request).subscribe({
      next: () => this.router.navigate(['/courses', this.courseId()]),
      error: () => {
        this.submitError.set('Erreur lors de la modification du cours.');
        this.submitting.set(false);
      }
    });
  }
}
