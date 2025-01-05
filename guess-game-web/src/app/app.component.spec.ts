import { Component, NgModule } from '@angular/core';
import { Router } from '@angular/router';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app.component';

@Component({
    template: '<h1>Welcome to {{title}}!</h1>',
    standalone: false
})
class MockHomeComponent {
  public title = 'guess-game';
}

@NgModule({
  declarations: [MockHomeComponent],
  exports: [MockHomeComponent]
})
class MockModule {
}

describe('AppComponent', () => {
  let router: Router;
  let fixture: ComponentFixture<MockHomeComponent>;
  let component: MockHomeComponent;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent
      ],
      imports: [
        MockModule,
        RouterTestingModule.withRoutes([
          {
            path: '', pathMatch: 'full', component: MockHomeComponent
          }
        ])
      ]
    }).compileComponents();

    router = TestBed.inject(Router);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MockHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    router.initialNavigation();
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it(`should have as title 'guess-game'`, () => {
    expect(component.title).toEqual('guess-game');
  });

  it('should render title in a h1 tag', () => {
    const h1 = fixture.debugElement.nativeElement.querySelector('h1');
    expect(h1.textContent).toContain('Welcome to guess-game!');
  });
});
