package com.bt.cqm.config.dsl;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 18/08/15
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */
public interface DslCheckerSharePointPathConfig {
    TemplateFileNameConfig getTemplateFileNameConfig();
    TemplatePathConfig getTemplatePathConfig();
    ImportPathConfig getImportPathConfig();
    ResultPathConfig getResultPathConfig();
    FailurePathConfig getFailurePathConfig();
    TempPathConfig getTempPathConfig();
    SharePointBasePathConfig getSharePointBasePathConfig();
}
