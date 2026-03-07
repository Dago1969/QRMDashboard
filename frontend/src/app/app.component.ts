import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Componente root che ospita il router outlet dell'applicazione.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`
})
export class AppComponent {}
