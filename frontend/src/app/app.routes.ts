import { Routes } from '@angular/router';
import { UserListComponent } from './user/user-list/user-list.component';
import { UserCreateComponent } from './user/user-create/user-create.component';
import { UserViewComponent } from './user/user-view/user-view.component';
import { UserModifyComponent } from './user/user-modify/user-modify.component';
import { CourseListComponent } from './course/course-list/course-list.component';
import { CourseCreateComponent } from './course/course-create/course-create.component';
import { CourseViewComponent } from './course/course-view/course-view.component';
import { CourseModifyComponent } from './course/course-modify/course-modify.component';

export const routes: Routes = [
  { path: 'users/new',         component: UserCreateComponent },
  { path: 'users/:id/edit',    component: UserModifyComponent },
  { path: 'users/:id',         component: UserViewComponent },
  { path: 'users',             component: UserListComponent },
  { path: 'courses/new',       component: CourseCreateComponent },
  { path: 'courses/:id/edit',  component: CourseModifyComponent },
  { path: 'courses/:id',       component: CourseViewComponent },
  { path: 'courses',           component: CourseListComponent },
  { path: '',                  redirectTo: 'users', pathMatch: 'full' }
];
