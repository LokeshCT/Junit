package com.rsqe.database.utils.importer;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTableMetaData;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.common.handlers.IllegalInputCharacterException;
import org.dbunit.dataset.common.handlers.PipelineException;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.csv.CsvParserException;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.stream.DefaultConsumer;
import org.dbunit.dataset.stream.IDataSetConsumer;
import org.dbunit.dataset.stream.IDataSetProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StreamingCsvSingleTableDataSetProducer implements IDataSetProducer {

    private static final Logger logger = LoggerFactory.getLogger(StreamingCsvSingleTableDataSetProducer.class);

    private static final IDataSetConsumer EMPTY_CONSUMER = new DefaultConsumer();
    private IDataSetConsumer _consumer = EMPTY_CONSUMER;
    private final String directory;
    private final String file;

    public StreamingCsvSingleTableDataSetProducer(String directory, String file) {
        this.directory = directory;
        this.file = file;
    }

    @Override
    public void setConsumer(IDataSetConsumer consumer) throws DataSetException {
        logger.debug("setConsumer(consumer) - start");
        _consumer = consumer;
    }

    @Override
    public void produce() throws DataSetException {
        logger.debug("produce() - start");
        File file = new File(this.directory, this.file);
        String absolutePath = file.getAbsolutePath();
        _consumer.startDataSet();
        try {
            produceFromFile(file);
        } catch (CsvParserException e) {
            throw new DataSetException("error producing dataset for table '" + absolutePath + "'", e);
        } catch (DataSetException e) {
            throw new DataSetException("error producing dataset for table '" + absolutePath + "'", e);
        }

        _consumer.endDataSet();
    }

    private void produceFromFile(File theDataFile) throws DataSetException, CsvParserException {
        logger.debug("produceFromFile(theDataFile={}) - start", theDataFile);

        try {
            StreamingCsvParser parser = new StreamingCsvParser(theDataFile);
            Object[] readColumns = parser.firstLine();
            Column[] columns = new Column[readColumns.length];

            for (int i = 0; i < readColumns.length; i++) {
                columns[i] = new Column((String) readColumns[i], DataType.UNKNOWN);
            }

            String tableName = theDataFile.getName().substring(0, theDataFile.getName().indexOf(".csv"));
            ITableMetaData metaData = new DefaultTableMetaData(tableName, columns);

            _consumer.startTable(metaData);

            Object[] row;
            int count = 1;
            while((row = parser.next())!=null) {
                for(int col = 0; col < row.length; col++) {
                    row[col] = row[col].equals(CsvDataSetWriter.NULL) ? null : row[col];
                }
                _consumer.row(row);
                if(count%50==0) {
                    logger.debug("{} Consumed row number {}", tableName, count);
                }
                count++;
            }
            _consumer.endTable();
        } catch (PipelineException e) {
            throw new DataSetException(e);
        } catch (IllegalInputCharacterException e) {
            throw new DataSetException(e);
        } catch (IOException e) {
            throw new DataSetException(e);
        }
    }

	public static List<String> getTableNamesIn(String tableList) throws IOException {

		List<String> orderedNames = new ArrayList<String>();
		InputStream tableListStream = new URL(tableList).openStream();
		BufferedReader reader = null;
		try {
    		reader = new BufferedReader(new InputStreamReader(tableListStream));
    		String line;
    		while((line = reader.readLine()) != null) {
    			String table = line.trim();
    			if (table.length() > 0) {
    				orderedNames.add(table);
    			}
    		}
		}
		finally {
		    if(reader != null) {
		        reader.close();
		    }
		}
		return orderedNames;
	}

}
