package com.bt.rsqe.matchers.excel;

import com.bt.rsqe.matchers.CompositeMatcher;
import org.apache.poi.ss.usermodel.Sheet;
import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;

import static com.google.common.collect.ObjectArrays.*;

public abstract class ExcelSheetCompositeMatcher<T> extends CompositeMatcher<Sheet> {
    private final String name;
    protected ExcelTemplateSheetFields sheetFields;

    public ExcelSheetCompositeMatcher(String name) {
        this.name = name;
    }

    protected final void expectCellWithValue(final String templateFieldName, final String expected) {
        expectCellWithValueAndOffset(templateFieldName, expected, 0);
    }


    protected final void expectCellWithValue(final String templateFieldName, final String expected, final int siteCount) {
        expectCellWithValueAndOffset(templateFieldName, expected, siteCount - 1);
    }

    private void expectCellWithValueAndOffset(final String templateFieldName, final String expected, final int rowOffset) {
        expect(new TypeSafeMatcher<Sheet>() {
            private List<ExcelCellMatchFailure> cellMatchFailures;

            @Override
            public boolean matchesSafely(Sheet sheet) {
                cellMatchFailures = sheetFields.matchValueOrValues(sheet, templateFieldName, expected, rowOffset);
                return cellMatchFailures.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                appendDescription(description, name, templateFieldName, expected, cellMatchFailures);
            }


        });
    }

    protected final void expectCellWithValue(final int row, final int column, final String expected) {
        expect(new TypeSafeMatcher<Sheet>() {
            private List<ExcelCellMatchFailure> cellMatchFailures;

            @Override
            public boolean matchesSafely(Sheet sheet) {
                cellMatchFailures = sheetFields.matchValueOrValues(sheet, row, column, expected);
                return cellMatchFailures.isEmpty();
            }

            @Override
            public void describeTo(Description description) {
                appendDescription(description, name, String.format(" [%s,%s] ", row, column), expected, cellMatchFailures);
            }
        });
    }

    private static void appendDescription(Description description, String name, String templateFieldName, String expected, List<ExcelCellMatchFailure> cellMatchFailures) {
        description.appendText(name)
                   .appendText(" cell ")
                   .appendValue(templateFieldName)
                   .appendText(" containing ")
                   .appendValue(expected);

        int index = 0;
        for (ExcelCellMatchFailure failure : cellMatchFailures) {
            ExcelTemplateField field = failure.getField();
            if (index > 0) {
                description.appendText(", ");
            }
            description.appendText(" at row ")
                       .appendValue(field.getRowIndex())
                       .appendText(" column ")
                       .appendValue(field.getColumnIndex())
                       .appendText(" but was ")
                       .appendValue(failure.getValue());
            index++;
        }
    }

    protected final void expectRowWithValues(final String[] templateFieldNames, final String[] expected) {
        if(templateFieldNames.length != expected.length) {
            throw new RuntimeException(String.format("Expected values (%s) length does not match templateFieldNames (%s) length ", expected, templateFieldNames));
        }
        expectRowWithValues(templateFieldNames, expected, newArray(ColumnType.class, templateFieldNames.length));
    }

    protected final void expectRowWithValues(final String[] templateFieldNames, final String[] expected, final ColumnType[] types) {
        final Class clazz = this.getClass();
        expect(new TypeSafeMatcher<Sheet>() {

            @Override
            public boolean matchesSafely(Sheet sheet) {
                return sheetFields.matchRowValues(sheet, templateFieldNames, expected, types);
            }

            @Override
            public void describeTo(Description description) {
                description
                    .appendText(clazz.getSimpleName())
                    .appendText(": ")
                    .appendText(name)
                    .appendText(" row ")
                    .appendValue(templateFieldNames)
                    .appendText(" containing ")
                    .appendValue(expected);
            }
        });
    }

    public List<SelfDescribing> failures() {
        return failures;
    }

    public void setExcelTemplateSheetFields(ExcelTemplateSheetFields sheetFields) {
        this.sheetFields = sheetFields;
    }

    public T withCellValue(int row, int column, String expected) {
        expectCellWithValue(row,column, expected);
        return (T) this;
    }
}
