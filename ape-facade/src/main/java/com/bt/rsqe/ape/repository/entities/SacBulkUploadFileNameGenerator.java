package com.bt.rsqe.ape.repository.entities;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: 608026723
 * Date: 08/09/15
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class SacBulkUploadFileNameGenerator implements IdentifierGenerator {
    Logger LOG =LoggerFactory.getLogger(SacBulkUploadFileNameGenerator.class);
    @Override
    public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
        SacBulkUploadEntity entity = (SacBulkUploadEntity)object;
        Connection connection = session.connection();
        try {

            PreparedStatement ps = connection.prepareStatement("SELECT seq_file_name.nextval as nextval from dual");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("nextval");
                String code = entity.getCreateUser()+"_"+id;
                LOG.debug("Generated Stock Code: " + code);
                return code;
            }

        } catch (SQLException e) {
            LOG.error("Failed to Generate File Name for SAC Bulk Upload !!",e);
            throw new HibernateException(
                "Unable to generate Stock Code Sequence");
        }
        return null;
    }
}
