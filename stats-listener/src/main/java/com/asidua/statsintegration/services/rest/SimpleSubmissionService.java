package com.asidua.statsintegration.services.rest;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public interface SimpleSubmissionService {

    Response heartbeat(String options);

    Response triggerTestNamed(String name, String testid, boolean synchronous,MultivaluedMap<String,String> params);

    Response statusOfTest(String testid);

    Response purgeTest(String testId);
}
