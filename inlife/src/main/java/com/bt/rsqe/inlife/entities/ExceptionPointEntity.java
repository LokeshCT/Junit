package com.bt.rsqe.inlife.entities;

import com.bt.rsqe.monitoring.ExceptionPointDTO;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "EXCEPTION_POINT_DIMENSION")
public class ExceptionPointEntity
{
    @Id
    @SequenceGenerator(name = "EXCEPTION_POINT_DIMENSION_SEQ_GENERATOR", sequenceName = "EXCEPTION_POINT_DIMENSION_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXCEPTION_POINT_DIMENSION_SEQ_GENERATOR")
    private int id;

    @Column(name = "EXCEPTION_POINT", unique = true)
    private String exceptionPoint;

    @OneToMany (mappedBy = "exceptionPointId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ErrorFactEntity> errorFactEntities;

    public ExceptionPointEntity()
    {

    }

    public ExceptionPointEntity(String exceptionPoint)
    {
        this.exceptionPoint = exceptionPoint;
    }

    public ExceptionPointEntity(int id, String exceptionPoint)
    {
        this.id = id;
        this.exceptionPoint = exceptionPoint;
    }

    public List<ErrorFactEntity> getErrorFactEntities()
    {
        return errorFactEntities;
    }

    public void setErrorFactEntities(List<ErrorFactEntity> errorFactEntities)
    {
        this.errorFactEntities = errorFactEntities;
    }

    public int getId()
    {
        return id;
    }

    public String getExceptionPoint()
    {
        return exceptionPoint;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ExceptionPointEntity that = (ExceptionPointEntity) o;

        return exceptionPoint.equals(that.exceptionPoint);

    }

    @Override
    public int hashCode()
    {
        return exceptionPoint.hashCode();
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public ExceptionPointDTO toDto ()
    {
        return new ExceptionPointDTO(id, exceptionPoint);
    }

    public static ExceptionPointEntity fromDto (ExceptionPointDTO dto)
    {
        return new ExceptionPointEntity(dto.getId(), dto.getExceptionPoint());
    }
}
