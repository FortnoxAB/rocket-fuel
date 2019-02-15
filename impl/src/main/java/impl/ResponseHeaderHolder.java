package impl;

import java.util.Map;

public interface ResponseHeaderHolder {
	void addHeaders(Object result, Map<String,Object> headers);
}
