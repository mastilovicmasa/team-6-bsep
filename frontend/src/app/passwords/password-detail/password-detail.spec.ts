import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordDetail } from './password-detail';

describe('PasswordDetail', () => {
  let component: PasswordDetail;
  let fixture: ComponentFixture<PasswordDetail>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PasswordDetail]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PasswordDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
