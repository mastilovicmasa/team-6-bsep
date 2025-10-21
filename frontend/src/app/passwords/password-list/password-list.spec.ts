import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordList } from './password-list';

describe('PasswordList', () => {
  let component: PasswordList;
  let fixture: ComponentFixture<PasswordList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
