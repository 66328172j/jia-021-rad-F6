package com.fc.v2.common.exception;

/**
 * 业务校验异常：校验统一归服务层，不通过时抛出，
 * 由全局异常处理原样把消息回给页面（不附加「运行时异常」前缀）。
 *
 * @author fuce
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServiceException(String message) {
        super(message);
    }
}
