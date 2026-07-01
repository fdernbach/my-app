import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CourseService } from '../course.service';
import { CourseRequest } from '../../api/models';
import { CourseEditorComponent } from '../course-editor/course-editor.component';

interface Toast { type: 'success' | 'error'; message: string; }

@Component({
  selector: 'app-course-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, CourseEditorComponent],
  templateUrl: './course-create.component.html',
  styleUrl: './course-create.component.scss'
})
export class CourseCreateComponent {
  private readonly courseService = inject(CourseService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly toast = signal<Toast | null>(null);

  readonly form = new FormGroup({
    title:  new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(256)] }),
    author: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  documentJson: Record<string, unknown> | null = null;

  onEditorChange(content: Record<string, unknown>): void {
    this.documentJson = content;
  }

  private buildRequest(): CourseRequest | null {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return null;
    }
    const { title, author } = this.form.getRawValue();
    return {
      title,
      author,
      ...(this.documentJson && { documentJson: this.documentJson as { [key: string]: any } }),
    };
  }

  private showToast(type: 'success' | 'error', message: string, durationMs = 3000): void {
    this.toast.set({ type, message });
    setTimeout(() => this.toast.set(null), durationMs);
  }

  /** Bouton toolbar : crée le cours et bascule sur la page d'édition */
  quickSave(): void {
    const request = this.buildRequest();
    if (!request) {
      this.showToast('error', 'Veuillez remplir tous les champs obligatoires.', 5000);
      return;
    }
    if (this.submitting()) return;

    this.submitting.set(true);
    this.courseService.createCourse(request).subscribe({
      next: (course) => {
        this.submitting.set(false);
        this.showToast('success', 'Cours créé. Redirection vers l\'éditeur…', 1500);
        setTimeout(() => this.router.navigate(['/courses', course.id, 'edit']), 1500);
      },
      error: (err) => {
        this.submitting.set(false);
        const msg = err?.error?.detail ?? 'Erreur lors de la création du cours.';
        this.showToast('error', msg, 5000);
      }
    });
  }

  /** Bouton "Créer" en bas : crée le cours et retourne à la liste */
  submit(): void {
    const request = this.buildRequest();
    if (!request) return;

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
