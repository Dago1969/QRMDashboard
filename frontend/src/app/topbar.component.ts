import { NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ProjectContextService } from './core/project-context.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [NgIf],
  template: `
    <header class="topbar">
      <span class="topbar-title">QTMDashboard</span>
      <span *ngIf="selectedProject" class="topbar-project">Progetto: <b>{{ selectedProject }}</b></span>
    </header>
  `,
  styles: [`
    .topbar {
      width: 100vw;
      background: #1a2a3a;
      color: #fff;
      padding: 10px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 1.1em;
      box-shadow: 0 2px 8px #0002;
      z-index: 100;
    }
    .topbar-title {
      font-weight: 700;
      letter-spacing: 1px;
    }
    .topbar-project {
      font-weight: 500;
      color: #e0e8f8;
    }
  `]
})
export class TopbarComponent implements OnInit {
  selectedProject: string | null = null;
  constructor(private projectContext: ProjectContextService) {}
  ngOnInit(): void {
    this.selectedProject = this.projectContext.getSelectedProject();
  }
}
