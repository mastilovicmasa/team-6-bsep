import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaSubordinates } from './ca-subordinates';

describe('CaSubordinates', () => {
  let component: CaSubordinates;
  let fixture: ComponentFixture<CaSubordinates>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaSubordinates]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaSubordinates);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
