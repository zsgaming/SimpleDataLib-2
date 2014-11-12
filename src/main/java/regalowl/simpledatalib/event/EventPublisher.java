package regalowl.simpledatalib.event;

import java.lang.reflect.Method;
import java.util.concurrent.CopyOnWriteArrayList;


public class EventPublisher {

	private CopyOnWriteArrayList<Object> listeners = new CopyOnWriteArrayList<Object>();
	
    public void registerListener(Object listener) {
    	if (!listeners.contains(listener)) {
    		listeners.add(listener);
    	}
    }
    
    public void unRegisterListener(Object listener) {
    	if (listeners.contains(listener)) {
    		listeners.remove(listener);
    	}
    }
    
    public void unRegisterAllListeners() {
    	listeners.clear();
    }

    @SuppressWarnings("rawtypes")
	public Event fireEvent(Event event) {
        for (Object listener:listeners) {
            for (Method m:listener.getClass().getMethods()) {
				if (m.getAnnotation(EventHandler.class) == null) continue;
				Class[] params = m.getParameterTypes();
				if (params.length != 1) continue;
				if (!event.getClass().getSimpleName().equals(params[0].getSimpleName())) continue;
				try {
					m.invoke(listener, event);
					event.setFiredSuccessfully();
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
		return event;
    }
	
}