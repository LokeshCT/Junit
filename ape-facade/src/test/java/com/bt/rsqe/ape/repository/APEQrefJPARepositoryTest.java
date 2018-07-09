package com.bt.rsqe.ape.repository;

import com.bt.rsqe.ape.repository.entities.AccessUserCommentsEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefErrorEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class APEQrefJPARepositoryTest {
    private static final String QREF_ID = "qrefId";
    private static final String USER_QREF_ID = "accesUserQrefId";
    private static final String REQUEST_ID = "requestId";
    private static final String UNIQUE_ID = "uniqueId";
    private static final String USER_LOGIN = "aSalesUser";
    private static final String QUOTE_CURRENCY = "aCurrencyCode";
    private static final String ASCCES_USER_QREF_ID = "accessuserID";

    private JPAPersistenceManager jpaPersistanceManager;
    private EntityManager entityManager;
    private APEQrefRepository apeQrefJPARepository;

    @Before
    public void before() {
        jpaPersistanceManager = mock(JPAPersistenceManager.class);
        entityManager = mock(EntityManager.class);

        when(jpaPersistanceManager.entityManager()).thenReturn(entityManager);
        apeQrefJPARepository = new APEQrefJPARepository(jpaPersistanceManager);
    }

    @Test
    public void shouldReturnTheRequestEntityForTheGivenUniqueId() throws Exception {
        final ApeRequestEntity apeRequestEntity = new ApeRequestEntity("123", "456", USER_LOGIN, QUOTE_CURRENCY);
        apeRequestEntity.setStatus(QrefRequestStatus.Status.WAITING);

        final String uniqueId = "uniqueId";

        TypedQuery query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), Matchers.<Class<Object>>anyObject())).thenReturn(query);
        when(query.setParameter("uniqueId", uniqueId)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(apeRequestEntity);

        assertThat(apeQrefJPARepository.getAPERequestByUniqueId(uniqueId), is(apeRequestEntity));
    }

    @Test
    public void shouldRemoveAnyApeQrefDetailEntitiesAssociatedWithTheQrefId() throws Exception {

        final ApeQrefDetailEntity detailEntity = new ApeQrefDetailEntity();
        final List<ApeQrefDetailEntity> details = newArrayList();
        details.add(detailEntity);

        TypedQuery query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), Matchers.<Class<Object>>anyObject())).thenReturn(query);
        when(query.setParameter("qrefId", QREF_ID)).thenReturn(query);
        when(query.getResultList()).thenReturn(details);

        apeQrefJPARepository.deleteApeQref(QREF_ID);

        verify(jpaPersistanceManager).remove(detailEntity);

    }


    @Test
    public void shouldSaveUserCommentEntity() {
        AccessUserCommentsEntity accessUserCommentsEntity = new AccessUserCommentsEntity(USER_QREF_ID,QREF_ID,"username", "comment",null);
        apeQrefJPARepository.save(accessUserCommentsEntity);

        verify(jpaPersistanceManager, times(1)).save(accessUserCommentsEntity);
    }

    @Test
    public void shouldSaveApeRequestEntity() {
        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(REQUEST_ID, UNIQUE_ID, USER_LOGIN, QUOTE_CURRENCY);
        apeQrefJPARepository.save(apeRequestEntity);

        verify(jpaPersistanceManager, times(1)).save(apeRequestEntity);
    }


    @Test
    public void shouldGetNullForQrefIdWithNoEntryInDB() {
        List<AccessUserCommentsEntity> accessUserCommentsEntity = apeQrefJPARepository.getUserCommentsForQrefId(QREF_ID);

        assertNull(accessUserCommentsEntity);
    }

    @Test
    public void shouldGetUserCommentForQrefIdWithEntryInDB() {
        AccessUserCommentsEntity accessUserCommentEnt = new AccessUserCommentsEntity(ASCCES_USER_QREF_ID,QREF_ID,USER_LOGIN,"test", new java.sql.Date(System.currentTimeMillis()));
        List<AccessUserCommentsEntity> userCommentsEntityList = new ArrayList();
        userCommentsEntityList.add(accessUserCommentEnt);

        when(jpaPersistanceManager.query(eq(AccessUserCommentsEntity.class), any(String.class), eq(QREF_ID))).thenReturn(userCommentsEntityList);
        List<AccessUserCommentsEntity> accessUserCommentsEntity = apeQrefJPARepository.getUserCommentsForQrefId(QREF_ID);

        assertThat(accessUserCommentsEntity.get(0).getComment(), is("test"));
    }

    @Test
    public void shouldPersistQrefError() throws Exception {
        ApeQrefErrorEntity error = new ApeQrefErrorEntity(QREF_ID, "errorCode", "errorMsg");
        apeQrefJPARepository.save(error);
        verify(jpaPersistanceManager, times(1)).save(error);
    }

    @Test
    public void shouldReturnQrefErrorsForAGivenQref() throws Exception {
        ApeQrefErrorEntity error = new ApeQrefErrorEntity(QREF_ID, "errorCode1", "errorMsg1");
        ApeQrefErrorEntity error2 = new ApeQrefErrorEntity(QREF_ID, "errorCode2", "errorMsg2");

        TypedQuery query = mock(TypedQuery.class);
        when(entityManager.createQuery("SELECT entity FROM ApeQrefErrorEntity entity WHERE entity.qrefId=:qrefId", ApeQrefErrorEntity.class)).thenReturn(query);
        when(query.setParameter("qrefId", QREF_ID)).thenReturn(query);
        when(query.getResultList()).thenReturn(Lists.newArrayList(error, error2));

        assertThat(apeQrefJPARepository.getApeQrefErrors(QREF_ID), hasItems(error, error2));
    }

    @Test
    public void shouldReturnApeQrefDetailEntityForTheGivenUniqueId() {
        ApeRequestEntity apeRequestEntity = mock(ApeRequestEntity.class);
        ApeQrefDetailEntity apeQrefDetailEntity = new ApeQrefDetailEntity();

        TypedQuery query = mock(TypedQuery.class);
        when(entityManager.createQuery("SELECT entity FROM ApeRequestEntity entity WHERE entity.uniqueId=:uniqueId AND entity.status NOT IN ('CANCELLED')", ApeRequestEntity.class)).thenReturn(query);
        when(query.setParameter("uniqueId", UNIQUE_ID)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(apeRequestEntity);
        List<ApeQrefDetailEntity> apeQrefDetailEntities = new ArrayList();
        apeQrefDetailEntities.add(apeQrefDetailEntity);

        when(apeRequestEntity.getApeQrefDetailsByRequestId()).thenReturn(apeQrefDetailEntities);
        assertThat(apeQrefJPARepository.getAPEQrefsByUniqueId(UNIQUE_ID), is(apeQrefDetailEntities));
    }
}
