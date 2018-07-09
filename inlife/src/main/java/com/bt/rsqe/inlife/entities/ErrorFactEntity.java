package com.bt.rsqe.inlife.entities;

import com.bt.rsqe.monitoring.ErrorFactDTO;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "ERROR_FACT")
public class ErrorFactEntity
{
    @Id
    @SequenceGenerator(name = "ERROR_FACT_SEQ_GENERATOR", sequenceName = "ERROR_FACT_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ERROR_FACT_SEQ_GENERATOR")
    private int id;

    @Column(name = "TIME_STAMP")
    private Timestamp timestamp;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    private UserEntity userId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "EXCEPTION_POINT_ID", referencedColumnName = "ID")
    private ExceptionPointEntity exceptionPointId;

    @Column(name = "QUOTE_OPTION_ID")
    private String quoteOptionId;

    @Column(name = "QUOTE_LINE_ITEM_ID")
    private String quoteLineItemId;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "URL")
    private String url;

    public ErrorFactEntity()
    {

    }

    public ErrorFactEntity(String quoteOptionId, String quoteLineItemId, String errorMessage, String url)
    {
        this.quoteOptionId = quoteOptionId;
        this.quoteLineItemId = quoteLineItemId;
        this.errorMessage = errorMessage;
        this.url = url;
    }

    public ErrorFactEntity(int id, Timestamp timestamp, UserEntity userId, ExceptionPointEntity exceptionPointId, String quoteOptionId, String quoteLineItemId, String errorMessage, String url)
    {
        this.id = id;
        this.timestamp = timestamp;
        this.userId = userId;
        this.exceptionPointId = exceptionPointId;
        this.quoteOptionId = quoteOptionId;
        this.quoteLineItemId = quoteLineItemId;
        this.errorMessage = errorMessage;
        this.url = url;
    }

    public int getId()
    {
        return id;
    }

    public UserEntity getUserId()
    {
        return userId;
    }

    public void setUserId(UserEntity userId)
    {
        this.userId = userId;
    }

    public ExceptionPointEntity getExceptionPointId()
    {
        return exceptionPointId;
    }

    public void setExceptionPointId(ExceptionPointEntity exceptionPointId)
    {
        this.exceptionPointId = exceptionPointId;
    }

    @PrePersist
    protected void onCreate()
    {
        this.timestamp = new Timestamp(new Date().getTime());
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that, new String[]{});
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public ErrorFactDTO toDto ()
    {
        return new ErrorFactDTO(id, timestamp, userId.toDto(), exceptionPointId.toDto(), quoteOptionId, quoteLineItemId, errorMessage, url);
    }

    public static ErrorFactEntity fromDto (ErrorFactDTO dto)
    {
        return new ErrorFactEntity(dto.getId(), dto.getTimestamp(), UserEntity.fromDto(dto.getUserId()), ExceptionPointEntity.fromDto(dto.getExceptionPointId()), dto.getQuoteOptionId(), dto.getQuoteLineItemId(), dto.getErrorMessage(), dto.getUrl());
    }
}
