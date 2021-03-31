package com.orm.demo.dynamic;

import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;

/**
 * Created by oyhk on 15/11/21.
 */
@Controller
public class BaseController {

    /**
     * 获取分页类
     *
     * @param page
     * @param size
     * @return
     */
    protected PageRequest getPageRequest(Integer page, Integer size) {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size < 0) {
            size = 10;
        }
        return new PageRequest(page, size);
    }

    protected PageRequest getJpaPageRequest() {
        return getJpaPageRequest(null, null);

    }

    /**
     * jpa 的写法
     *
     * @param offset
     * @param pageSize
     * @return
     */
    protected PageRequest getJpaPageRequest(Integer offset, Integer pageSize) {
        int page = 0;

        if (offset == null) {
            offset = 0;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        if (offset > 0) {
            page = offset / pageSize;
        }
        return new PageRequest(page, pageSize);

    }

    protected PageResult getPage(Integer pageNo, Integer pageSize) {
        return new PageResult(pageNo, pageSize);
    }

    protected Integer getOffset(Integer offset) {
        if (offset == null || offset <= 0) {
            offset = 0;
        }
        return offset;
    }

    protected Integer getSize(Integer size) {
        if (size == null || size <= 0) {
            size = 10;
        }
        return size;
    }

    protected void showErrorLog(Exception e, Logger log){
        log.error(e.getMessage());
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement stackTraceElement = stackTraceElements[i];
            log.error(stackTraceElement.toString());
        }
    }


}
