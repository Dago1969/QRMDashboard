import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ProjectContextService {
  getSelectedProject(): string | null {
    if (typeof window === 'undefined') return null;
    const url = new URL(window.location.href);
    return url.searchParams.get('project') || sessionStorage.getItem('selectedProject') || null;
  }
  setSelectedProject(project: string): void {
    if (typeof window === 'undefined') return;
    sessionStorage.setItem('selectedProject', project);
  }
}
