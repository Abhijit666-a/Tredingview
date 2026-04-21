import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './register.html',
})
export class RegisterComponent {
  readonly step = signal(1);
  readonly isLoading = signal(false);

  user = {
    fullname: '',
    email: '',
    phone: '',
    capital: 50000,
    experience: 'Beginner'
  };

  constructor(private router: Router) {}

  nextStep() {
    if (this.step() < 2) {
      this.step.set(2);
    } else {
      this.submit();
    }
  }

  submit() {
    this.isLoading.set(true);
    // Simulate API call
    setTimeout(() => {
      this.isLoading.set(false);
      this.router.navigate(['/dashboard']);
    }, 2000);
  }
}
