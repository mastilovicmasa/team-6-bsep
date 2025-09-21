import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CsrUpload } from './csr-upload';

describe('CsrUpload', () => {
  let component: CsrUpload;
  let fixture: ComponentFixture<CsrUpload>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CsrUpload]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CsrUpload);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
