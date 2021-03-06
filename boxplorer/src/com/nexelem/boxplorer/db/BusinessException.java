package com.nexelem.boxplorer.db;

@SuppressWarnings("serial")
public class BusinessException extends Throwable {

	public BusinessException(Throwable exception) {
		super(exception);
	}

    public BusinessException(java.lang.String detailMessage) {
        super(detailMessage);
    }

	public BusinessException(Throwable exception, String message, Object... params) {
		super(System.out.printf(message, params).toString(), exception);
	}

}
