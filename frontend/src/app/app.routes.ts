import { Routes } from '@angular/router';
import { UserListComponent } from './user/user-list/user-list.component';
import { UserCreateComponent } from './user/user-create/user-create.component';
import { UserViewComponent } from './user/user-view/user-view.component';
import { UserModifyComponent } from './user/user-modify/user-modify.component';

export const routes: Routes = [
  { path: 'users/new',       component: UserCreateComponent },
  { path: 'users/:id/edit',  component: UserModifyComponent },
  { path: 'users/:id',       component: UserViewComponent },
  { path: 'users',           component: UserListComponent },
  { path: '',                redirectTo: 'users', pathMatch: 'full' }
];
