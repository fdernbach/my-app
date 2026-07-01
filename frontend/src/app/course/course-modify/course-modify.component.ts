import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CourseService } from '../course.service';
import { CourseRequest } from '../../api/models';
import { CourseEditorComponent } from '../course-editor/course-editor.component';

@Component({
  selector: 'app-course-modify',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CourseEditorComponent],
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
    title:  new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(256)] }),
    author: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  initialDocumentJson: Record<string, unknown> | null = null;
  documentJson: Record<string, unknown> | null = null;

  ngOnInit(): void {
    this.courseId.set(Number(this.route.snapshot.paramMap.get('id')));
    this.courseService.getCourse(this.courseId()).subscribe({
      next: course => {
        this.form.patchValue({ title: course.title, author: course.author });
        this.initialDocumentJson = (course.documentJson as Record<string, unknown>) ?? null;
        this.documentJson = this.initialDocumentJson;
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Cours introuvable.');
        this.loading.set(false);
      }
    });
  }

  onEditorChange(content: Record<string, unknown>): void {
    this.documentJson = content;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { title, author } = this.form.getRawValue();
    const request: CourseRequest = {
      title,
      author,
      ...(this.documentJson && { documentJson: this.documentJson as { [key: string]: any } }),
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
