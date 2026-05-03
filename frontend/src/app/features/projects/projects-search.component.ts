import { CommonModule } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { ProjectApiService, ProjectDto, ProjectSearchFilters } from '../../core/project-api.service';
import {
  SearchFilterField,
  SearchPageActionEvent,
  SearchPageComponent,
  SearchResultColumn
} from '../../shared/search-page.component';

/**
 * Gestione progetti con lista centralizzata nel body del dashboard.
 */
@Component({
  selector: 'app-projects-search',
  standalone: true,
  imports: [CommonModule, SearchPageComponent],
  template: `
    <app-search-page
      #searchPage
      [titleKey]="'projects.search.title'"
      [subtitleKey]="'projects.search.subtitle'"
      [emptyStateKey]="'projects.search.noResults'"
      [createActionLabelKey]="'projects.actions.new'"
      [filters]="searchFilters"
      [columns]="searchColumns"
      [fetchResults]="fetchProjects"
      [showViewAction]="false"
      [showEditAction]="true"
      [showDeleteAction]="true"
      (createAction)="openCreate()"
      (editAction)="openEdit($event)"
      (deleteAction)="delete($event)"
    />
  `
})
export class ProjectsSearchComponent {
  @ViewChild('searchPage') private searchPage?: SearchPageComponent<ProjectDto>;

  readonly searchFilters: SearchFilterField[] = [
    { key: 'code', labelKey: 'projects.field.code', type: 'text' },
    { key: 'tenant', labelKey: 'projects.field.tenant', type: 'text' }
  ];

  readonly searchColumns: SearchResultColumn<ProjectDto>[] = [
    { key: 'code', labelKey: 'projects.field.code' },
    { key: 'descrizione', labelKey: 'projects.field.descrizione' },
    { key: 'tenant', labelKey: 'projects.field.tenant' },
    { key: 'clientCode', labelKey: 'projects.field.clientCode' },
    { key: 'emailSender', labelKey: 'projects.field.emailSender' },
    { key: 'dataInizio', labelKey: 'projects.field.dataInizio' },
    { key: 'dataFine', labelKey: 'projects.field.dataFine' }
  ];

  readonly fetchProjects = (filters: Record<string, string>): Observable<ProjectDto[]> =>
    this.projectApiService.searchProjects(filters as ProjectSearchFilters);

  constructor(
    private readonly router: Router,
    private readonly projectApiService: ProjectApiService
  ) {}

  openCreate(): void {
    void this.router.navigateByUrl('/dashboard/projects/new');
  }

  openEdit(event: SearchPageActionEvent<ProjectDto>): void {
    void this.router.navigateByUrl(`/dashboard/projects/${event.id}`);
  }

  delete(event: SearchPageActionEvent<ProjectDto>): void {
    if (!window.confirm(this.searchPage?.translate('projects.search.confirmDelete') ?? '')) {
      return;
    }

    this.projectApiService.deleteProject(Number(event.id)).subscribe({
      next: () => {
        this.searchPage?.reload(false);
        this.searchPage?.showExternalMessage(this.searchPage?.translate('projects.success.delete') ?? '', 'success');
      },
      error: () => {
        this.searchPage?.showExternalMessage(this.searchPage?.translate('projects.error.delete') ?? '', 'error');
      }
    });
  }
}