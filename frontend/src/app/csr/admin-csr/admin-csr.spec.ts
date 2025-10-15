import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminCSR } from './admin-csr';

describe('AdminCSR', () => {
  let component: AdminCSR;
  let fixture: ComponentFixture<AdminCSR>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCSR]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminCSR);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
