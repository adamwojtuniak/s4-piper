package io.s4.example.counter;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class CounterComponent {
	
	private Injector injector;
	
	public void activate(){
		injector = Guice.createInjector(new Module());
        MyApp myApp = injector.getInstance(MyApp.class);
        myApp.init();
        myApp.start();
	}
	
	public void deactivate(){
		
	}

}
