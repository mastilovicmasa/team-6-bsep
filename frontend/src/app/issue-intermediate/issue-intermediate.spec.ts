import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IssueIntermediate } from './issue-intermediate';

describe('IssueIntermediate', () => {
  let component: IssueIntermediate;
  let fixture: ComponentFixture<IssueIntermediate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IssueIntermediate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(IssueIntermediate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
