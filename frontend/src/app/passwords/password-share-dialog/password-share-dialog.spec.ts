import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordShareDialog } from './password-share-dialog';

describe('PasswordShareDialog', () => {
  let component: PasswordShareDialog;
  let fixture: ComponentFixture<PasswordShareDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordShareDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordShareDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
