package com.rsqe.database.utils.importer;

import org.dbunit.dataset.common.handlers.EscapeHandler;
import org.dbunit.dataset.common.handlers.IllegalInputCharacterException;
import org.dbunit.dataset.common.handlers.IsAlnumHandler;
import org.dbunit.dataset.common.handlers.Pipeline;
import org.dbunit.dataset.common.handlers.PipelineException;
import org.dbunit.dataset.common.handlers.QuoteHandler;
import org.dbunit.dataset.common.handlers.SeparatorHandler;
import org.dbunit.dataset.common.handlers.TransparentHandler;
import org.dbunit.dataset.common.handlers.WhitespacesHandler;
import org.dbunit.dataset.csv.CsvParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class StreamingCsvParser {

    private static final Logger logger = LoggerFactory.getLogger(StreamingCsvParser.class);

    private Pipeline pipeline;
    private File file;
    private BufferedReader reader;
    private LineNumberReader lineNumberReader;
    private Object[] firstLine;

    public StreamingCsvParser(File file) {
        resetThePipeline();
        this.file = file;
        try {
            this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.lineNumberReader = new LineNumberReader(reader);
        try {
            this.firstLine = parseFirstLine(lineNumberReader, file.getAbsolutePath().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetThePipeline() {
        logger.debug("resetThePipeline() - start");

        pipeline = new Pipeline();
        getPipeline().putFront(SeparatorHandler.ENDPIECE());
        getPipeline().putFront(EscapeHandler.ACCEPT());
        getPipeline().putFront(IsAlnumHandler.QUOTE());
        getPipeline().putFront(QuoteHandler.QUOTE());
        getPipeline().putFront(EscapeHandler.ESCAPE());
        getPipeline().putFront(WhitespacesHandler.IGNORE());
        getPipeline().putFront(TransparentHandler.IGNORE());
    }

    private Object[] parse(String csv) throws PipelineException, IllegalInputCharacterException {
        logger.debug("parse(csv={}) - start", csv);

        getPipeline().resetProducts();
        CharacterIterator iterator = new StringCharacterIterator(csv);
        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            getPipeline().handle(c);
        }
        getPipeline().noMoreInput();
        getPipeline().thePieceIsDone();
        return getPipeline().getProducts().toArray();
    }

    public Object[] next() throws IOException, CsvParserException {
        int nColumns = firstLine.length;
        return collectExpectedNumberOfColumns(nColumns, lineNumberReader);
    }

    public Object[] firstLine() throws CsvParserException {
        return firstLine;
    }

    private Object[] parseFirstLine(LineNumberReader lineNumberReader, String source) throws IOException, CsvParserException {
        if (logger.isDebugEnabled()) {
            logger.debug("parseFirstLine(lineNumberReader={}, source={}, rows={}) - start",
                         new Object[]{lineNumberReader, source});
        }

        String firstLine = lineNumberReader.readLine();
        if (firstLine == null) {
            throw new CsvParserException("The first line of " + source + " is null");
        }

        return parse(firstLine);
    }

    private Object[] collectExpectedNumberOfColumns(int expectedNumberOfColumns, LineNumberReader lineNumberReader) throws IOException, CsvParserException {
        if (logger.isDebugEnabled()) {
            logger.debug("collectExpectedNumberOfColumns(expectedNumberOfColumns={}, lineNumberReader={}) - start",
                         String.valueOf(expectedNumberOfColumns), lineNumberReader);
        }

        List columns = null;
        int columnsCollectedSoFar = 0;
        StringBuffer buffer = new StringBuffer();
        String anotherLine = lineNumberReader.readLine();
        if (anotherLine == null) {
            return null;
        }
        boolean shouldProceed = false;
        while (columnsCollectedSoFar < expectedNumberOfColumns) {
            try {
                buffer.append(anotherLine);
                columns = Arrays.asList(parse(buffer.toString()));
                columnsCollectedSoFar = columns.size();
            } catch (IllegalStateException e) {
                resetThePipeline();
                anotherLine = lineNumberReader.readLine();
                if (anotherLine == null) {
                    break;
                }
                buffer.append("\n");
                shouldProceed = true;
            }
            if (!shouldProceed) {
                break;
            }
        }
        if (columnsCollectedSoFar != expectedNumberOfColumns) {
            String message = new StringBuffer("Expected ").append(expectedNumberOfColumns)
                                                          .append(" columns on line ").append(lineNumberReader.getLineNumber())
                                                          .append(", got ").append(columnsCollectedSoFar).append(". Offending line: ").append(buffer).toString();
            throw new CsvParserException(message);
        }
        return new LinkedList<Object>(columns).toArray();
    }

    Pipeline getPipeline() {
        logger.debug("getPipeline() - start");

        return pipeline;
    }

    void setPipeline(Pipeline pipeline) {
        logger.debug("setPipeline(pipeline={}) - start", pipeline);

        this.pipeline = pipeline;
    }
}
