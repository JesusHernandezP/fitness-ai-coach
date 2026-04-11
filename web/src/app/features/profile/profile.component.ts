import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService, MetabolicProfileDto } from '../../core/api/api.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <section class="profile">
      <article class="profile__card">
        <header class="profile__header">
          <div>
            <p class="profile__eyebrow">Metabolic Profile</p>
            <h2>Coach context</h2>
          </div>
          <p class="profile__copy">
            Keep your weight, diet type and goal aligned across web and Android.
          </p>
        </header>

        <form class="profile__form" [formGroup]="form" (ngSubmit)="save()">
          <label>
            <span>Age</span>
            <input type="number" formControlName="age" min="15" max="80" />
          </label>

          <label>
            <span>Height (cm)</span>
            <input type="number" formControlName="heightCm" min="120" max="220" step="0.1" />
          </label>

          <label>
            <span>Weight (kg)</span>
            <input type="number" formControlName="weightKg" min="30" max="400" step="0.1" />
          </label>

          <label>
            <span>Sex</span>
            <select formControlName="sex">
              <option value="">Select sex</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
            </select>
          </label>

          <label>
            <span>Activity level</span>
            <select formControlName="activityLevel">
              <option value="">Select activity</option>
              <option value="SEDENTARY">Sedentary</option>
              <option value="LIGHT">Light</option>
              <option value="MODERATE">Moderate</option>
              <option value="ACTIVE">Active</option>
              <option value="VERY_ACTIVE">Very active</option>
            </select>
          </label>

          <label>
            <span>Diet type</span>
            <select formControlName="dietType">
              <option value="">Select diet</option>
              <option value="STANDARD">Standard</option>
              <option value="KETO">Keto</option>
              <option value="VEGETARIAN">Vegetarian</option>
            </select>
          </label>

          <label class="profile__field--wide">
            <span>Goal</span>
            <select formControlName="goalType">
              <option value="">Select goal</option>
              <option value="LOSE_WEIGHT">Lose weight</option>
              <option value="BUILD_MUSCLE">Build muscle</option>
              <option value="MAINTAIN">Maintain</option>
            </select>
          </label>

          @if (errorMessage) {
            <p class="profile__message profile__message--error">{{ errorMessage }}</p>
          }

          @if (successMessage) {
            <p class="profile__message profile__message--success">{{ successMessage }}</p>
          }

          <div class="profile__actions">
            <button type="submit" [disabled]="isSaving || form.invalid">
              {{ isSaving ? 'Saving...' : 'Save profile' }}
            </button>
          </div>
        </form>
      </article>

      <article class="profile__card profile__card--summary">
        <h3>Daily intake targets</h3>
        <p class="profile__summary-copy">These are your goal-adjusted amounts to consume each day based on profile, diet type and objective.</p>
        <div class="profile__targets">
          <div>
            <span>Calories</span>
            <strong>{{ currentProfile?.targetCalories ?? '--' }}</strong>
          </div>
          <div>
            <span>Protein</span>
            <strong>{{ currentProfile?.targetProtein ?? '--' }}</strong>
          </div>
          <div>
            <span>Carbs</span>
            <strong>{{ currentProfile?.targetCarbs ?? '--' }}</strong>
          </div>
          <div>
            <span>Fat</span>
            <strong>{{ currentProfile?.targetFat ?? '--' }}</strong>
          </div>
        </div>
      </article>
    </section>
  `,
  styles: [`
    :host {
      display: block;
    }

    .profile {
      display: grid;
      grid-template-columns: minmax(0, 2fr) minmax(300px, 1fr);
      gap: 20px;
    }

    .profile__card {
      background: #1e1e1e;
      border: 1px solid #2a2a2a;
      border-radius: 20px;
      padding: 24px;
      box-shadow: 0 20px 50px rgba(0, 0, 0, 0.18);
    }

    .profile__header {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      align-items: start;
      margin-bottom: 24px;
    }

    .profile__header h2,
    .profile__card h3 {
      margin: 6px 0 0;
      color: #ffffff;
    }

    .profile__eyebrow {
      margin: 0;
      color: #ffe01e;
      text-transform: uppercase;
      letter-spacing: 0.12em;
      font-size: 0.75rem;
      font-weight: 700;
    }

    .profile__copy {
      max-width: 24rem;
      margin: 0;
      color: #a0a0a0;
      line-height: 1.5;
    }

    .profile__form {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
    }

    .profile__form label {
      display: grid;
      gap: 8px;
      color: #d4d4d4;
      font-weight: 600;
    }

    .profile__field--wide,
    .profile__actions,
    .profile__message {
      grid-column: 1 / -1;
    }

    .profile__form input,
    .profile__form select {
      min-height: 52px;
      border-radius: 14px;
      border: 1px solid #2a2a2a;
      background: #121212;
      color: #ffffff;
      font: inherit;
      padding: 0 16px;
      outline: none;
    }

    .profile__form input:focus,
    .profile__form select:focus {
      border-color: #ffe01e;
    }

    .profile__actions button {
      min-height: 52px;
      padding: 0 20px;
      border: none;
      border-radius: 14px;
      background: #ffe01e;
      color: #111318;
      font: inherit;
      font-weight: 700;
      cursor: pointer;
    }

    .profile__actions button:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }

    .profile__message {
      margin: 0;
      line-height: 1.5;
    }

    .profile__message--error {
      color: #ef4444;
    }

    .profile__message--success {
      color: #ffe01e;
    }

    .profile__targets {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
      margin-top: 16px;
    }

    .profile__summary-copy {
      margin: 12px 0 0;
      color: #a0a0a0;
      line-height: 1.5;
    }

    .profile__targets div {
      border: 1px solid #2a2a2a;
      border-radius: 16px;
      background: #121212;
      padding: 16px;
      display: grid;
      gap: 8px;
    }

    .profile__targets span {
      color: #a0a0a0;
    }

    .profile__targets strong {
      color: #ffe01e;
      font-size: 1.5rem;
    }

    @media (max-width: 900px) {
      .profile {
        grid-template-columns: 1fr;
      }
    }

    @media (max-width: 700px) {
      .profile__form {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class ProfileComponent implements OnInit {
  private readonly apiService = inject(ApiService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly form = this.formBuilder.nonNullable.group({
    age: [0, [Validators.required, Validators.min(15), Validators.max(80)]],
    heightCm: [0, [Validators.required, Validators.min(120), Validators.max(220)]],
    weightKg: [0, [Validators.required, Validators.min(30), Validators.max(400)]],
    sex: ['', Validators.required],
    activityLevel: ['', Validators.required],
    dietType: ['', Validators.required],
    goalType: ['', Validators.required]
  });

  protected currentProfile: MetabolicProfileDto | null = null;
  protected isSaving = false;
  protected errorMessage = '';
  protected successMessage = '';

  ngOnInit(): void {
    const subscription = this.apiService.getProfile().subscribe({
      next: (profile) => {
        this.currentProfile = profile;
        this.form.patchValue({
          age: profile.age ?? 0,
          heightCm: profile.heightCm ?? 0,
          weightKg: profile.weightKg ?? 0,
          sex: profile.sex ?? '',
          activityLevel: profile.activityLevel ?? '',
          dietType: profile.dietType ?? '',
          goalType: profile.goalType ?? ''
        });
      },
      error: () => {
        this.errorMessage = 'Unable to load your profile right now.';
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  protected save(): void {
    if (this.form.invalid || this.isSaving) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload: MetabolicProfileDto = {
      age: Number(this.form.getRawValue().age),
      heightCm: Number(this.form.getRawValue().heightCm),
      weightKg: Number(this.form.getRawValue().weightKg),
      sex: this.form.getRawValue().sex as MetabolicProfileDto['sex'],
      activityLevel: this.form.getRawValue().activityLevel as MetabolicProfileDto['activityLevel'],
      dietType: this.form.getRawValue().dietType as MetabolicProfileDto['dietType'],
      goalType: this.form.getRawValue().goalType as MetabolicProfileDto['goalType']
    };

    const subscription = this.apiService.updateProfile(payload).subscribe({
      next: (profile) => {
        this.currentProfile = profile;
        this.form.patchValue({
          age: profile.age ?? 0,
          heightCm: profile.heightCm ?? 0,
          weightKg: profile.weightKg ?? 0,
          sex: profile.sex ?? '',
          activityLevel: profile.activityLevel ?? '',
          dietType: profile.dietType ?? '',
          goalType: profile.goalType ?? ''
        });
        this.successMessage = 'Profile saved successfully.';
        this.isSaving = false;
      },
      error: () => {
        this.errorMessage = 'Unable to save your profile right now.';
        this.isSaving = false;
      }
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }
}
