package com.bt.rsqe.inlife.entities;

import com.bt.rsqe.monitoring.UserDTO;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "USER_DIMENSION")
public class UserEntity
{
    @Id
    @SequenceGenerator(name = "USER_DIMENSION_SEQ_GENERATOR", sequenceName = "USER_DIMENSION_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USER_DIMENSION_SEQ_GENERATOR")
    private int id;

    @Column(name = "USER_IDENTIFIER", unique = true)
    private String userIdentifier;

    @Column(name = "TYPE")
    private String type;

    @Column(name = "SALES_CHANEL")
    private String salesChanel;

    //@OneToMany (mappedBy = "userId", fetch = FetchType.LAZY)
    //private List<ErrorFactEntity> errorFactEntities;

    public UserEntity()
    {

    }

    public UserEntity(String userIdentifier, String type, String salesChanel)
    {
        this.userIdentifier = userIdentifier;
        this.type = type;
        this.salesChanel = salesChanel;
    }

    public UserEntity(int id, String userIdentifier, String type, String salesChanel)
    {
        this.id = id;
        this.userIdentifier = userIdentifier;
        this.type = type;
        this.salesChanel = salesChanel;
    }


    public int getId()
    {
        return id;
    }

    public String getUserIdentifier()
    {
        return userIdentifier;
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

        UserEntity that = (UserEntity) o;

        if (!userIdentifier.equals(that.userIdentifier))
        {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null)
        {
            return false;
        }
        return !(salesChanel != null ? !salesChanel.equals(that.salesChanel) : that.salesChanel != null);

    }

    @Override
    public int hashCode()
    {
        int result = userIdentifier.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (salesChanel != null ? salesChanel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    public UserDTO toDto ()
    {
        return new UserDTO(id, userIdentifier, type, salesChanel);
    }

    public static UserEntity fromDto (UserDTO dto)
    {
        return new UserEntity(dto.getId(), dto.getUserIdentifier(), dto.getType(), dto.getSalesChanel());
    }
}