package cn.uway.ucloude.ec;

import java.util.Observer;

/**
 * 事件订阅者
 * @author uway
 *
 */
public class EventSubscriber {
	public EventSubscriber(String id, IObserver observer) {
        this.id = id;
        this.observer = observer;
    }

	    private String id;

	    private IObserver observer;

	    public String getId() {
	        return id;
	    }

	    public void setId(String id) {
	        this.id = id;
	    }

	    public IObserver getObserver() {
	        return observer;
	    }

	    public void setObserver(IObserver observer) {
	        this.observer = observer;
	    }
}
