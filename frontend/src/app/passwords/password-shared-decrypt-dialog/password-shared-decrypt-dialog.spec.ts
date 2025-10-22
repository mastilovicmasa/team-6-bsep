import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordSharedDecriptDialog } from './password-shared-decript-dialog';

describe('PasswordSharedDecriptDialog', () => {
  let component: PasswordSharedDecriptDialog;
  let fixture: ComponentFixture<PasswordSharedDecriptDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordSharedDecriptDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordSharedDecriptDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
