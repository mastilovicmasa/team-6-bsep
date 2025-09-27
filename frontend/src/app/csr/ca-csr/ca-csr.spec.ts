import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaCsr } from './ca-csr';

describe('CaCsr', () => {
  let component: CaCsr;
  let fixture: ComponentFixture<CaCsr>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaCsr]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaCsr);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
