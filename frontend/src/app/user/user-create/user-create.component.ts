import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UserService } from '../user.service';
import { UserRequest } from '../../api/models';

@Component({
  selector: 'app-user-create',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-create.component.html',
  styleUrl: './user-create.component.scss'
})
export class UserCreateComponent {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = new FormGroup({
    lastName:  new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    firstName: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    email:     new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    birthDate: new FormControl('', { nonNullable: true }),
    address: new FormGroup({
      streetNumber:   new FormControl('', { nonNullable: true }),
      streetName:     new FormControl('', { nonNullable: true }),
      additionalInfo: new FormControl('', { nonNullable: true }),
      postalCode:     new FormControl('', { nonNullable: true, validators: [Validators.pattern(/^\d{5}$/)] }),
      city:           new FormControl('', { nonNullable: true }),
      country:        new FormControl('France', { nonNullable: true }),
    })
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.error.set(null);

    const { lastName, firstName, email, birthDate, address } = this.form.getRawValue();
    const hasAddress = !!(address.streetName || address.city || address.postalCode);

    const request: UserRequest = {
      lastName,
      firstName,
      email,
      ...(birthDate && { birthDate }),
      ...(hasAddress && {
        address: {
          streetName: address.streetName,
          postalCode: address.postalCode,
          city: address.city,
          ...(address.streetNumber  && { streetNumber: address.streetNumber }),
          ...(address.additionalInfo && { additionalInfo: address.additionalInfo }),
          ...(address.country       && { country: address.country }),
        }
      })
    };

    this.userService.createUser(request).subscribe({
      next: () => this.router.navigate(['/users']),
      error: () => {
        this.error.set('Erreur lors de la création de l\'utilisateur.');
        this.submitting.set(false);
      }
    });
  }
}
