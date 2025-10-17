import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddSubordinates } from './add-subordinates';

describe('AddSubordinates', () => {
  let component: AddSubordinates;
  let fixture: ComponentFixture<AddSubordinates>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddSubordinates]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddSubordinates);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
