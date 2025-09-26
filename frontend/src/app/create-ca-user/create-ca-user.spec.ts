import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateCaUser } from './create-ca-user';

describe('CreateCaUser', () => {
  let component: CreateCaUser;
  let fixture: ComponentFixture<CreateCaUser>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateCaUser]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateCaUser);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
