import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TopbarComponent } from './topbar.component';

/**
 * Componente root che ospita il router outlet dell'applicazione.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, TopbarComponent],
  template: `
    <app-topbar></app-topbar>
    <router-outlet></router-outlet>
  `
})
export class AppComponent {}
