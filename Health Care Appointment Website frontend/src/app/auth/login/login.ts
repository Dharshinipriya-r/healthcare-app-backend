import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class LoginComponent {
   showPassword = false;
  email: string = '';
  password: string = '';
togglePassword() {
    this.showPassword = !this.showPassword;
  }
  get credentials() {
    return {
      email: this.email,
      password: this.password,
    };
  }

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit() {
    console.log('Attempting login with:', this.credentials);

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        console.log('Login response received:', response);
        if (response && response.accessToken) {
          // Save user id and user object in local storage
          if (response.user && response.user.id) {
            localStorage.setItem('userId', response.user.id.toString());
            // Only save user object, not roles separately
            localStorage.setItem('user', JSON.stringify({
              id: response.user.id,
              email: response.user.email,
              role: response.user.role
            }));
            console.log('[LoginComponent] Saved user id:', response.user.id);
          }
          console.log('Token received, navigating to dashboard...');
          this.router
            .navigate(['/dashboard'])
            .then(() => {
              console.log('Navigation complete');
            })
            .catch((err) => {
              console.error('Navigation failed:', err);
            });
        } else {
          console.warn('No token in response:', response);
          alert('Login successful but no token received');
        }
      },
      error: (error) => {
        console.error('Login failed:', error);
        if (error.error?.message === 'User is disabled') {
          alert(
            'Please verify your email before logging in. Check your inbox for the verification link.'
          );
        } else {
          alert('Login failed. Please check your credentials.');
        }
      },
      complete: () => {
        console.log('Login request completed');
      },
    });
  }
}
