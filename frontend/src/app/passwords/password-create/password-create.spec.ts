import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordCreate } from './password-create';

describe('PasswordCreate', () => {
  let component: PasswordCreate;
  let fixture: ComponentFixture<PasswordCreate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordCreate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
