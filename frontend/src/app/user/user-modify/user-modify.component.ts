import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserService } from '../user.service';
import { UserRequest } from '../../api/models';

@Component({
  selector: 'app-user-modify',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-modify.component.html',
  styleUrl: './user-modify.component.scss'
})
export class UserModifyComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly userId = signal('');

  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);

  readonly form = new FormGroup({
    userName:  new FormControl({ value: '', disabled: true }, { nonNullable: true }),
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
      country:        new FormControl('', { nonNullable: true }),
    })
  });

  ngOnInit(): void {
    this.userId.set(this.route.snapshot.paramMap.get('id')!);
    this.userService.getUser(this.userId()).subscribe({
      next: user => {
        this.form.patchValue({
          userName:  user.userName,
          lastName:  user.lastName,
          firstName: user.firstName,
          email:     user.email,
          birthDate: user.birthDate ?? '',
          address: {
            streetNumber:   user.address?.streetNumber   ?? '',
            streetName:     user.address?.streetName     ?? '',
            additionalInfo: user.address?.additionalInfo ?? '',
            postalCode:     user.address?.postalCode     ?? '',
            city:           user.address?.city           ?? '',
            country:        user.address?.country        ?? '',
          }
        });
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set('Utilisateur introuvable.');
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.submitError.set(null);

    const { userName, lastName, firstName, email, birthDate, address } = this.form.getRawValue();
    const hasAddress = !!(address.streetName || address.city || address.postalCode);

    const request: UserRequest = {
      userName,
      lastName,
      firstName,
      email,
      ...(birthDate && { birthDate }),
      ...(hasAddress && {
        address: {
          streetName: address.streetName,
          postalCode: address.postalCode,
          city:       address.city,
          ...(address.streetNumber   && { streetNumber:   address.streetNumber }),
          ...(address.additionalInfo && { additionalInfo: address.additionalInfo }),
          ...(address.country        && { country:        address.country }),
        }
      })
    };

    this.userService.updateUser(this.userId(), request).subscribe({
      next: () => this.router.navigate(['/users', this.userId()]),
      error: () => {
        this.submitError.set('Erreur lors de la modification de l\'utilisateur.');
        this.submitting.set(false);
      }
    });
  }
}
