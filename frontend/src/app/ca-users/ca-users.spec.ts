import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaUsers } from './ca-users';

describe('CaUsers', () => {
  let component: CaUsers;
  let fixture: ComponentFixture<CaUsers>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaUsers]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaUsers);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
