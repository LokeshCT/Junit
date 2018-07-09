package com.bt.rsqe.ape.repository;

import com.bt.rsqe.ApeFacadeDatabaseConfigProvider;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.bt.rsqe.ape.dto.ApeQrefIdentifier;
import com.bt.rsqe.ape.repository.entities.AccessStaffCommentEntity;
import com.bt.rsqe.ape.repository.entities.AccessUserCommentsEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeQrefErrorEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestDetailEntity;
import com.bt.rsqe.ape.repository.entities.ApeRequestEntity;
import com.bt.rsqe.domain.QrefRequestStatus;
import com.bt.rsqe.domain.product.ProductOffering;
import com.bt.rsqe.dto.SearchCriteria;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.persistence.JPATestProvider;
import com.bt.rsqe.utils.Environment;
import com.bt.rsqe.web.rest.exception.ResourceNotFoundException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class APEQrefJPARepositoryIntegrationTest {
    APEQrefRepository apeQrefRepository;
    JPAPersistenceManager persistenceManager;
    JPAEntityManagerProvider provider;

    private String uuid() {
        return UUID.randomUUID().toString();
    }

    @Before
    public void setup() {
        provider = JPATestProvider.provider(new ApeFacadeDatabaseConfigProvider(null,Environment.env()), JPATestProvider.APEFACADE);
        persistenceManager = new JPAPersistenceManager();
        apeQrefRepository = new APEQrefJPARepository(persistenceManager);

        EntityManager entityManager = provider.entityManager();
        persistenceManager.bind(entityManager);
        persistenceManager.start();
    }

    @Test
    public void shouldSaveQrefRequest() {
        String requestId = uuid();
        String uniqueId = uuid();
        String userLogin = "aUserLogin";
        String currency = "GBP";

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency);
        apeRequestEntity = saveAndGet(apeRequestEntity);

        assertThat(apeRequestEntity.getUserLogin(), is(userLogin));
        assertNotNull(apeRequestEntity.getCreatedDate());
        assertNotNull(apeRequestEntity.getUpdatedDate());
    }

    @Test
    public void shouldSetCreatedAndUpdatedDates() {
        String requestId = uuid();
        String uniqueId = uuid();

        ApeRequestEntity apeRequestEntity = new ApeRequestEntity(requestId, uniqueId, null, null);
        apeRequestEntity = saveAndGet(apeRequestEntity);

        java.util.Date createdDate = apeRequestEntity.getCreatedDate();
        java.util.Date updatedDate = apeRequestEntity.getUpdatedDate();

        assertNotNull(createdDate);
        assertNotNull(updatedDate);

        apeRequestEntity.setStatus(QrefRequestStatus.Status.CANCELLED);
        apeRequestEntity = saveAndGet(apeRequestEntity);
        assertTrue(createdDate.equals(apeRequestEntity.getCreatedDate()));
        assertFalse(updatedDate.equals(apeRequestEntity.getUpdatedDate()));
    }

    private ApeRequestEntity saveAndGet(ApeRequestEntity apeRequestEntity) {
        apeQrefRepository.save(apeRequestEntity);
        commitAndReconnect();
        return apeQrefRepository.getAPERequestByRequestId(apeRequestEntity.getRequestId());
    }

    @Test
    public void shouldReturnAvailableQrefsForUniqueId() {

        String requestId = uuid();
        String uniqueId = uuid();
        String qrefId1 = uuid();
        String qrefId2 = uuid();
        String userLogin = "aUserLogin";
        String currency = "USD";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency);
        apeQrefRepository.save(requestEntity);

        ApeQrefDetailEntity apeQrefDetailEntity1 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId1,
                                                                           "QREF",
                                                                           "Qref 1",
                                                                           2);

        ApeQrefDetailEntity apeQrefDetailEntity2 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId2,
                                                                           "QREF",
                                                                           "Qref 2",
                                                                           1);
        apeQrefRepository.save(apeQrefDetailEntity1);
        apeQrefRepository.save(apeQrefDetailEntity2);

        List<ApeQrefIdentifier> qrefIdentifiers = apeQrefRepository.getQrefIdentifiers(uniqueId, null);
        assertThat(qrefIdentifiers.size(), is(2));
        assertThat(qrefIdentifiers.get(0), is(new ApeQrefIdentifier(qrefId2, "Qref 2")));
        assertThat(qrefIdentifiers.get(1), is(new ApeQrefIdentifier(qrefId1, "Qref 1")));
    }

    @Test
    public void shouldReturnAvailableQrefsForUniqueIdAndSearchCriteria() {

        String requestId = uuid();
        String uniqueId = uuid();
        String qrefId1 = uuid();
        String qrefId2 = uuid();
        String userLogin = "aUserLogin";
        String currency = "USD";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency);
        apeQrefRepository.save(requestEntity);

        ApeQrefDetailEntity apeQrefDetailEntity1 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId1,
                                                                           "QREF",
                                                                           "Qref 1",
                                                                           2);

        ApeQrefDetailEntity apeQrefDetailEntity2 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId2,
                                                                           "QREF",
                                                                           "Qref 2",
                                                                           1);
        apeQrefRepository.save(apeQrefDetailEntity1);
        apeQrefRepository.save(apeQrefDetailEntity2);

        List<ApeQrefIdentifier> qrefIdentifiers = apeQrefRepository.getQrefIdentifiers(uniqueId, new SearchCriteria("Qref 1"));
        assertThat(qrefIdentifiers.size(), is(1));
        assertThat(qrefIdentifiers.get(0), is(new ApeQrefIdentifier(qrefId1, "Qref 1")));
    }

      @Test
    public void shouldReturnQREFPairWhenPrimaryLegMatchesSearchCriteria() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String qrefId1 = uuid();
        String qrefId2 = uuid();
        String userLogin = "aUserLogin";
        String currency = "USD";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency);
        apeQrefRepository.save(requestEntity);

        ApeQrefDetailEntity apeQrefDetailEntity1 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId1,
                                                                           "QREF",
                                                                           "Qref 1",
                                                                           1);

        ApeQrefDetailEntity apeQrefDetailEntity2 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId1,
                                                                           "Pair",
                                                                           "1",
                                                                           1);

        ApeQrefDetailEntity apeQrefDetailEntity3 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId2,
                                                                           "QREF",
                                                                           "Qref 2",
                                                                           2);

        ApeQrefDetailEntity apeQrefDetailEntity4 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId2,
                                                                           "Pair",
                                                                           "1",
                                                                           2);
        apeQrefRepository.save(apeQrefDetailEntity1);
        apeQrefRepository.save(apeQrefDetailEntity2);
        apeQrefRepository.save(apeQrefDetailEntity3);
        apeQrefRepository.save(apeQrefDetailEntity4);

        List<ApeQrefIdentifier> qrefIdentifiers = apeQrefRepository.getQrefIdentifiers(uniqueId, new SearchCriteria("Qref 1"));
        assertThat(qrefIdentifiers.size(), is(2));
        assertThat(qrefIdentifiers.get(0), is(new ApeQrefIdentifier(qrefId1, "Qref 1")));
        assertThat(qrefIdentifiers.get(1), is(new ApeQrefIdentifier(qrefId2, "Qref 2")));
    }

    @Test
    public void shouldRemoveSecondaryLegQrefWhenPrimaryHasAlreadyBeenRemoved() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String qrefId1 = uuid();
        String qrefId2 = uuid();
        String userLogin = "aUserLogin";
        String currency = "USD";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency);
        apeQrefRepository.save(requestEntity);

        ApeQrefDetailEntity apeQrefDetailEntity1 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId1,
                                                                           "QREF",
                                                                           "Qref 1",
                                                                           1);

        ApeQrefDetailEntity apeQrefDetailEntity2 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId1,
                                                                           "Pair",
                                                                           "1",
                                                                           1);

        ApeQrefDetailEntity apeQrefDetailEntity3 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId2,
                                                                           "QREF",
                                                                           "Qref 2",
                                                                           2);

        ApeQrefDetailEntity apeQrefDetailEntity4 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId2,
                                                                           "Pair",
                                                                           "1",
                                                                           2);
        apeQrefRepository.save(apeQrefDetailEntity1);
        apeQrefRepository.save(apeQrefDetailEntity2);
        apeQrefRepository.save(apeQrefDetailEntity3);
        apeQrefRepository.save(apeQrefDetailEntity4);

        List<ApeQrefIdentifier> qrefIdentifiers = apeQrefRepository.getQrefIdentifiers(uniqueId, new SearchCriteria("Qref 2"));
        assertThat(qrefIdentifiers.size(), is(0));
    }

    @Test
    public void shouldGetApeRequest() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String userLogin = "aUserLogin";
        String currency = "GBP";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency, new ApeRequestDetailEntity(requestId, "req att1", "value1"),
                                                              new ApeRequestDetailEntity(requestId, "req att2", "value2"));

        apeQrefRepository.save(requestEntity);
        commitAndReconnect();

        ApeRequestEntity returnedEntity = apeQrefRepository.getAPERequestByUniqueId(uniqueId);

        assertThat(returnedEntity.getUniqueId(), is(uniqueId));
        assertThat(returnedEntity.getRequestId(), is(requestId));
        assertThat(returnedEntity.getUserLogin(), is(userLogin));
        assertThat(returnedEntity.getCurrency(), is(currency));

        List<ApeRequestDetailEntity> apeRequestAttributes = returnedEntity.getApeRequestAttributes();
        assertThat(apeRequestAttributes.size(), is(2));
        assertThat(apeRequestAttributes, hasItems(new ApeRequestDetailEntity(requestId, "req att1", "value1"), new ApeRequestDetailEntity(requestId, "req att2", "value2")));
    }

    @Test
    public void shouldNotReturnAnyCancelledApeRequests() throws Exception {

        String requestId = uuid();
        String uniqueId = uuid();
        String userLogin = "aUserLogin";
        String currency = "EUR";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency);
        requestEntity.setStatus(QrefRequestStatus.Status.CANCELLED);

        apeQrefRepository.save(requestEntity);

        assertThat(apeQrefRepository.getAPERequestByUniqueId(uniqueId), is(nullValue()));

    }

    @Test
    public void shouldReturnIsSimulatedRequestAsTrueWhenApeFlagSetToNo() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String userLogin = "aUserLogin";
        String currency = "GBP";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency,
                                                              new ApeRequestDetailEntity(requestId, ProductOffering.APE_FLAG, "No"));

        apeQrefRepository.save(requestEntity);
        commitAndReconnect();

        ApeRequestEntity returnedEntity = apeQrefRepository.getAPERequestByUniqueId(uniqueId);

        assertThat(returnedEntity.isSimulatedRequest(), is(true));
    }

    @Test
    public void shouldReturnIsSimulatedRequestAsFalseWhenApeFlagSetToYes() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String userLogin = "aUserLogin";
        String currency = "GBP";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency,
                                                              new ApeRequestDetailEntity(requestId, ProductOffering.APE_FLAG, "Yes"));

        apeQrefRepository.save(requestEntity);
        commitAndReconnect();

        ApeRequestEntity returnedEntity = apeQrefRepository.getAPERequestByUniqueId(uniqueId);

        assertThat(returnedEntity.isSimulatedRequest(), is(false));
    }

    @Test
    public void shouldGetApeQref() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String qrefId = uuid();
        String userLogin = "aUserLogin";
        String currency = "USD";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency,
                                                              new ApeRequestDetailEntity(requestId, "req att1", "value"),
                                                              new ApeRequestDetailEntity(requestId, "req att2", null));
        requestEntity.setStatus(QrefRequestStatus.Status.PROCESSED);
        apeQrefRepository.save(requestEntity);

        ApeQrefDetailEntity apeQrefDetailEntity1 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId,
                                                                           "attribute1",
                                                                           "value1",
                                                                           0);

        ApeQrefDetailEntity apeQrefDetailEntity2 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId,
                                                                           "attribute2",
                                                                           "value2",
                                                                           0);
        apeQrefRepository.save(apeQrefDetailEntity1);
        apeQrefRepository.save(apeQrefDetailEntity2);

        ApeQrefErrorEntity apeQrefErrorEntity1 = new ApeQrefErrorEntity(qrefId, "code1", "msg1");
        ApeQrefErrorEntity apeQrefErrorEntity2 = new ApeQrefErrorEntity(qrefId, "code2", "msg2");

        apeQrefRepository.save(apeQrefErrorEntity1);
        apeQrefRepository.save(apeQrefErrorEntity2);

        ApeQref apeQref = apeQrefRepository.getApeQref(qrefId);

        assertThat(apeQref.getRequestId(), is(requestId));
        assertThat(apeQref.getQrefId(), is(qrefId));
        assertThat(apeQref.getAttributes(), hasItems(apeQrefDetailEntity1.dto(), apeQrefDetailEntity2.dto()));
        assertThat(apeQref.getErrors(), hasItems(apeQrefErrorEntity1.toDto(), apeQrefErrorEntity2.toDto()));
        assertThat(apeQref.getCurrency(), is(currency));

        assertThat(apeQref.getRequestAttributes().size(), is(2));
        assertThat(apeQref.getRequestAttributes(), hasItems( new ApeQrefAttributeDetail("req att1", "value"),
                                                             new ApeQrefAttributeDetail("req att2", null)));
    }


    @Test
    public void shouldGetApeQrefWithItsCancelledRequest() throws Exception {
        String requestId = uuid();
        String uniqueId = uuid();
        String qrefId = uuid();
        String userLogin = "aUserLogin";
        String currency = "USD";

        ApeRequestEntity requestEntity = new ApeRequestEntity(requestId, uniqueId, userLogin, currency,
                                                              new ApeRequestDetailEntity(requestId, "req att1", "value"),
                                                              new ApeRequestDetailEntity(requestId, "req att2", null));
        requestEntity.setStatus(QrefRequestStatus.Status.CANCELLED);
        apeQrefRepository.save(requestEntity);

        ApeQrefDetailEntity apeQrefDetailEntity1 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId,
                                                                           "attribute1",
                                                                           "value1",
                                                                           0);

        ApeQrefDetailEntity apeQrefDetailEntity2 = new ApeQrefDetailEntity(requestId,
                                                                           qrefId,
                                                                           "attribute2",
                                                                           "value2",
                                                                           0);
        apeQrefRepository.save(apeQrefDetailEntity1);
        apeQrefRepository.save(apeQrefDetailEntity2);

        ApeQrefErrorEntity apeQrefErrorEntity1 = new ApeQrefErrorEntity(qrefId, "code1", "msg1");
        ApeQrefErrorEntity apeQrefErrorEntity2 = new ApeQrefErrorEntity(qrefId, "code2", "msg2");

        apeQrefRepository.save(apeQrefErrorEntity1);
        apeQrefRepository.save(apeQrefErrorEntity2);

        ApeQref apeQref = apeQrefRepository.getApeQref(qrefId);

        assertThat(apeQref.getRequestId(), is(requestId));
        assertThat(apeQref.getQrefId(), is(qrefId));
        assertThat(apeQref.getAttributes(), hasItems(apeQrefDetailEntity1.dto(), apeQrefDetailEntity2.dto()));
        assertThat(apeQref.getErrors(), hasItems(apeQrefErrorEntity1.toDto(), apeQrefErrorEntity2.toDto()));
        assertThat(apeQref.getCurrency(), is(currency));

        assertThat(apeQref.getRequestAttributes().size(), is(2));
        assertThat(apeQref.getRequestAttributes(), hasItems( new ApeQrefAttributeDetail("req att1", "value"),
                                                             new ApeQrefAttributeDetail("req att2", null)));
    }

    @Test
    public void shouldGetQrefErrorsForQref() throws Exception {
        String qref = uuid();

        ApeQrefErrorEntity error = new ApeQrefErrorEntity(qref, "1", "errorMsg1");
        ApeQrefErrorEntity error2 = new ApeQrefErrorEntity(qref, "2", "errorMsg2");

        apeQrefRepository.save(error);
        apeQrefRepository.save(error2);

        assertThat(apeQrefRepository.getApeQrefErrors(qref), hasItems(error, error2));
    }

    @Test
    public void shouldGetStaffCommentsForQref() throws Exception {
        String qref = uuid();

        AccessStaffCommentEntity commentEntity1 = new AccessStaffCommentEntity(qref, "comment1", "staffEmail1", "staffName1", Date.valueOf("2000-01-01"));
        AccessStaffCommentEntity commentEntity2 = new AccessStaffCommentEntity(qref, "comment2", "staffEmail2", "staffName2", Date.valueOf("2000-01-02"));

        apeQrefRepository.save(commentEntity1);
        apeQrefRepository.save(commentEntity2);

        assertThat(apeQrefRepository.getStaffComments(qref), hasItems(commentEntity1, commentEntity2));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundExceptionWhenQrefCannotBeRetrieved() throws Exception {
        apeQrefRepository.getApeQref("this does not exist");
    }

    @Test
    public void shouldReturnEmptyListIfThereAreNoQrefsFound() {
        List<ApeQrefDetailEntity> qrefList = apeQrefRepository.getAPEQrefsByUniqueId("non-exist");
        assertThat(qrefList.size(), is(0));
    }

    @Test
    public void shouldGetApeUserComments() throws Exception {
        String comment = "comment";
        String username= "Thina";
        String qrefId = uuid();
        AccessUserCommentsEntity requestEntity = new AccessUserCommentsEntity(UUID.randomUUID().toString(),qrefId,username, comment, new java.sql.Date(System.currentTimeMillis()));
        apeQrefRepository.save(requestEntity);

       List<AccessUserCommentsEntity> userCommentsEntity = apeQrefRepository.getUserCommentsForQrefId(qrefId);

        assertThat(userCommentsEntity.get(0).getComment(), is(comment));
        assertThat(userCommentsEntity.get(0).getQrefId(), is(qrefId));
        assertThat(userCommentsEntity.get(0).getUsername(),is(username));
    }

    protected void commitAndReconnect() {
        persistenceManager.done();
        persistenceManager.bind(provider.entityManager());
        persistenceManager.start();
    }

    @After
    public void tearDown() {
        persistenceManager.done();
        persistenceManager.unbind();
    }
}
