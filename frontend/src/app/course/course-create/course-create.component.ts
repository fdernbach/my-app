import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CourseService } from '../course.service';
import { CourseRequest } from '../../api/models';

@Component({
  selector: 'app-course-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './course-create.component.html',
  styleUrl: './course-create.component.scss'
})
export class CourseCreateComponent {
  private readonly courseService = inject(CourseService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = new FormGroup({
    title:        new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(256)] }),
    author:       new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    documentJson: new FormControl('', { nonNullable: true }),
  });

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
        this.error.set('Le document JSON est invalide.');
        return;
      }
    }

    const request: CourseRequest = {
      title,
      author,
      ...(parsedDocument !== undefined && { documentJson: parsedDocument }),
    };

    this.submitting.set(true);
    this.error.set(null);

    this.courseService.createCourse(request).subscribe({
      next: (course) => this.router.navigate(['/courses', course.id]),
      error: () => {
        this.error.set('Erreur lors de la création du cours.');
        this.submitting.set(false);
      }
    });
  }
}
