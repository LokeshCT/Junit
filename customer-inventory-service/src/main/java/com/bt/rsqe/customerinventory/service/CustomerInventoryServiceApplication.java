package com.bt.rsqe.customerinventory.service;

import com.bt.rsqe.ComponentNames;
import com.bt.rsqe.ape.ApeFacade;
import com.bt.rsqe.ape.ApeFacadeSingleton;
import com.bt.rsqe.bfgfacade.config.BfgParameterMappings;
import com.bt.rsqe.bfgfacade.readers.BearerExtensionReader;
import com.bt.rsqe.bfgfacade.readers.BearerReader;
import com.bt.rsqe.bfgfacade.readers.BespokeAttributeReader;
import com.bt.rsqe.bfgfacade.readers.BucketAttributeReader;
import com.bt.rsqe.bfgfacade.readers.CIFAssetVpnReader;
import com.bt.rsqe.bfgfacade.readers.COSReader;
import com.bt.rsqe.bfgfacade.readers.CrossLayerMappingReader;
import com.bt.rsqe.bfgfacade.readers.FeatureOptionReader;
import com.bt.rsqe.bfgfacade.readers.IpAddressAttributeReader;
import com.bt.rsqe.bfgfacade.readers.LegacyVPNReader;
import com.bt.rsqe.bfgfacade.readers.NetworkNodeComponentReader;
import com.bt.rsqe.bfgfacade.readers.NetworkNodeReader;
import com.bt.rsqe.bfgfacade.readers.NetworkServiceReader;
import com.bt.rsqe.bfgfacade.readers.PackageInstanceReader;
import com.bt.rsqe.bfgfacade.readers.SoftwareReader;
import com.bt.rsqe.bfgfacade.repository.BfgRepositoryJPA;
import com.bt.rsqe.bfgfacade.repository.SqlQueryOwningUserNameResolver;
import com.bt.rsqe.bfgfacade.write.BfgStoredProcedureInvoker;
import com.bt.rsqe.bfgfacade.write.ParameterNameBasedProcedureOutputExtractor;
import com.bt.rsqe.bfgfacade.write.sp.StoredProcedureRunner;
import com.bt.rsqe.container.RestApplication;
import com.bt.rsqe.container.ioc.ResourceHandlerFactory;
import com.bt.rsqe.container.ioc.RestResourceHandlerFactory;
import com.bt.rsqe.customerinventory.bfg.BfgFacade;
import com.bt.rsqe.customerinventory.bfg.readers.AssetReader;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManager;
import com.bt.rsqe.customerinventory.client.CustomerInventoryClientManagerFactory;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.filter.BfgDbTransactionContainerFilter;
import com.bt.rsqe.customerinventory.repository.ProductInstanceRepository;
import com.bt.rsqe.customerinventory.repository.jpa.ExternalAssetRepository;
import com.bt.rsqe.customerinventory.repository.jpa.ProductInstanceJPARepository;
import com.bt.rsqe.customerinventory.service.comparisons.ActionCalculator;
import com.bt.rsqe.customerinventory.service.evaluators.CIFAssetCharacteristicEvaluatorFactory;
import com.bt.rsqe.customerinventory.service.extenders.AccessDetailExtender;
import com.bt.rsqe.customerinventory.service.extenders.ActionExtender;
import com.bt.rsqe.customerinventory.service.extenders.AsIsAssetExtender;
import com.bt.rsqe.customerinventory.service.extenders.CIFAssetExtender;
import com.bt.rsqe.customerinventory.service.extenders.CharacteristicExtender;
import com.bt.rsqe.customerinventory.service.extenders.JourneySpecificDetailExtender;
import com.bt.rsqe.customerinventory.service.extenders.ProductCategoryExtender;
import com.bt.rsqe.customerinventory.service.extenders.ProductOfferingExtender;
import com.bt.rsqe.customerinventory.service.extenders.QuoteOptionDetailExtender;
import com.bt.rsqe.customerinventory.service.extenders.SalesRelationshipExtender;
import com.bt.rsqe.customerinventory.service.extenders.SiteDetailExtender;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidExtender;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidMandatoryAttributeProvider;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidTemplateAttributeProvider;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetUpdateRequestFilter;
import com.bt.rsqe.customerinventory.service.filters.CIFAssetUpdateResponseFilter;
import com.bt.rsqe.customerinventory.service.rootAssetUpdater.RootAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.updates.*;
import com.bt.rsqe.customerinventory.service.rules.RelateToRuleExecutor;
import com.bt.rsqe.customerinventory.service.extenders.SpecialBidWellKnownAttributeProvider;
import com.bt.rsqe.customerinventory.service.extenders.StencilDetailExtender;
import com.bt.rsqe.customerinventory.service.extenders.ValidationExtender;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.SpecialBidReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.extenders.reservedattributes.StencilReservedAttributesHelper;
import com.bt.rsqe.customerinventory.service.externals.PmrHelper;
import com.bt.rsqe.customerinventory.service.externals.QuoteEngineHelper;
import com.bt.rsqe.customerinventory.service.handlers.AssetCandidateHandler;
import com.bt.rsqe.customerinventory.service.handlers.CIFAssetHandler;
import com.bt.rsqe.customerinventory.service.handlers.UpdateHandler;
import com.bt.rsqe.customerinventory.service.handlers.ValidationHandler;
import com.bt.rsqe.customerinventory.service.orchestrators.AssetModelOrchestrator;
import com.bt.rsqe.customerinventory.service.orchestrators.AssetUpdateOrchestrator;
import com.bt.rsqe.customerinventory.service.orchestrators.CIFAssetOrchestrator;
import com.bt.rsqe.customerinventory.service.orchestrators.ValidationOrchestrator;
import com.bt.rsqe.customerinventory.service.providers.AssetCandidateProviderFactory;
import com.bt.rsqe.customerinventory.service.providers.AssociatedAssetKeyProvider;
import com.bt.rsqe.customerinventory.service.providers.CIFAssetCreator;
import com.bt.rsqe.customerinventory.service.providers.ChoosableCandidateProvider;
import com.bt.rsqe.customerinventory.service.providers.CreatableCandidateProvider;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.customerinventory.service.repository.ExternalAssetReader;
import com.bt.rsqe.customerinventory.service.repository.LegacySqeFacade;
import com.bt.rsqe.customerinventory.service.repository.UniqueIdJPARepository;
import com.bt.rsqe.customerinventory.service.updates.AutoDefaultRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CancelRelationshipRequestBuilder;
import com.bt.rsqe.customerinventory.service.updates.CancelRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicChangeRequestBuilder;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicReloadUpdater;
import com.bt.rsqe.customerinventory.service.updates.CharacteristicsUpdater;
import com.bt.rsqe.customerinventory.service.updates.ChooseRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.ContributesToChangeRequestBuilder;
import com.bt.rsqe.customerinventory.service.updates.CreateRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.ExecutionRequestBuilder;
import com.bt.rsqe.customerinventory.service.updates.ReprovideAssetUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsReloadUpdater;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidCharacteristicsUpdater;
import com.bt.rsqe.customerinventory.service.updates.ExternalAttributesHelper;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidTemplateAttributeMapper;
import com.bt.rsqe.customerinventory.service.updates.SpecialBidWellKnownAttributeMapper;
import com.bt.rsqe.customerinventory.service.updates.UpdateRelationshipUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdateStencilUpdater;
import com.bt.rsqe.customerinventory.service.updates.UpdationIgnoreRequestUpdater;
import com.bt.rsqe.customerinventory.service.validation.AssetValidator;
import com.bt.rsqe.customerinventory.sqe.facade.SqeIvpnFacade;
import com.bt.rsqe.customerrecord.ExpedioClientResources;
import com.bt.rsqe.domain.project.ExpedioCountryResolver;
import com.bt.rsqe.logging.LoggingHandler;
import com.bt.rsqe.monitoring.StatisticsCollector;
import com.bt.rsqe.persistence.DBTransactionContainerFilter;
import com.bt.rsqe.persistence.DatabaseConfig;
import com.bt.rsqe.persistence.JPAEntityManagerProvider;
import com.bt.rsqe.persistence.JPAPersistenceManager;
import com.bt.rsqe.pmr.client.PmrClient;
import com.bt.rsqe.pmr.client.PmrClientSingleton;
import com.bt.rsqe.projectengine.OrderLineItemResource;
import com.bt.rsqe.projectengine.ProjectEngineClientResources;
import com.bt.rsqe.projectengine.ProjectResource;
import com.bt.rsqe.projectengine.migration.QuoteMigrationDetailsProvider;
import com.bt.rsqe.sqefacade.InProgressAssetResource;
import com.bt.rsqe.tpe.client.TemplateTpeClient;
import com.bt.rsqe.utils.countries.Countries;

import static com.bt.rsqe.bfgfacade.config.BfgParameterMappingConfigurationLoader.*;

public class CustomerInventoryServiceApplication extends RestApplication {
    private JPAPersistenceManager customerInventoryModelPersistence, bfgPersistence;
    private JPAEntityManagerProvider customerInventoryJpa, bfgJpa;
    private CustomerInventoryServiceConfig config;
    private DatabaseConfig bfgDatabaseConfig;
    private PmrClient pmr;
    private BfgRepositoryJPA bfgReadRepository;

    public CustomerInventoryServiceApplication(CustomerInventoryServiceConfig config) {
        super(config.getApplicationConfig(), config.getRestAuthenticationFilterConfig(), ComponentNames.CIFSERVICE);
        this.config = config;
        this.bfgDatabaseConfig = config.getDatabaseConfig("BfgDatabase");

        customerInventoryModelPersistence = new JPAPersistenceManager();
        customerInventoryJpa = new JPAEntityManagerProvider(config.getDatabaseConfig("CustomerInventoryDatabase"), "customerinventory");
        bfgJpa = new JPAEntityManagerProvider(bfgDatabaseConfig, "bfgFacade-CustomerInventoryService");

        bfgPersistence = new JPAPersistenceManager();

        StatisticsCollector statsCollector = new StatisticsCollector(ComponentNames.CIFSERVICE, config.getStatsMode());

        registerRestAuthenticationFilterIfEnabled();
        applicationContainerInstance().setTransactionAware(new DBTransactionContainerFilter(customerInventoryModelPersistence, customerInventoryJpa));
        applicationContainerInstance().setTransactionAware(new BfgDbTransactionContainerFilter(bfgPersistence, bfgJpa));
        applicationContainerInstance().registerBeforeHandler(new LoggingHandler());
        applicationContainerInstance().registerBeforeHandler(statsCollector);
        applicationContainerInstance().registerAfterHandler(new LoggingHandler());
        applicationContainerInstance().registerAfterHandler(statsCollector);

        applicationContainerInstance().addStandardHandlersForComponent(ComponentNames.CIFSERVICE);
    }

    @Override
    protected ResourceHandlerFactory createResourceHandlerFactory() {
        pmr = PmrClientSingleton.getPmrClient(config.getPmrClientConfig());
        bfgReadRepository = new BfgRepositoryJPA(bfgPersistence,
                new ParameterNameBasedProcedureOutputExtractor(),
                new BfgStoredProcedureInvoker(),
                bfgDatabaseConfig);
        final ProjectResource projectResource = new ProjectEngineClientResources(config.getProjectEngineClientConfig()).projectResource();
        final InProgressAssetResource inProgressAssetResource = new InProgressAssetResource(config.getSqeIvpnFacadeConfig());
        final SqeIvpnFacade sqeIvpnFacade = new SqeIvpnFacade(inProgressAssetResource);
        final BfgFacade bfgFacade = new BfgFacade(bfgReadRepository);
        final ProductInstanceRepository assetRepository = new ProductInstanceJPARepository(customerInventoryModelPersistence);
        final BfgParameterMappings bfgParameterMappings = loadBfgAssetMappingConfiguration();
        final SqlQueryOwningUserNameResolver userNameResolver = new SqlQueryOwningUserNameResolver(bfgDatabaseConfig.getRequiredDbObjects());
        final LegacyVPNReader legacyVpnReader = new LegacyVPNReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        final CIFAssetVpnReader cifAssetVpnReader = new CIFAssetVpnReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);

        final CIFAssetJPARepository cifAssetRepository = new CIFAssetJPARepository(customerInventoryModelPersistence,
                new ExternalAssetReader(new LegacySqeFacade(inProgressAssetResource),
                        bfgReadRepository, cifAssetVpnReader));
        final ExternalAssetRepository externalAssetRepository = new ExternalAssetRepository(bfgFacade, sqeIvpnFacade);
        final AssetReader assetReader = initAssetReader(bfgParameterMappings, userNameResolver, legacyVpnReader);
        ExpedioClientResources expedioClientResources = new ExpedioClientResources(config.getExpedioFacadeConfig());
        ExpedioCountryResolver countryResolver = new ExpedioCountryResolver(expedioClientResources, new Countries());

        CIFAssetCharacteristicEvaluatorFactory evaluatorFactory = new CIFAssetCharacteristicEvaluatorFactory(pmr,
                expedioClientResources,
                projectResource, cifAssetRepository);
        StencilReservedAttributesHelper stencilReservedAttributesHelper = new StencilReservedAttributesHelper();
        SpecialBidReservedAttributesHelper specialBidReservedAttributesHelper = new SpecialBidReservedAttributesHelper();

        final CIFAssetOrchestrator cifAssetOrchestrator = new CIFAssetOrchestrator(cifAssetRepository);
        final PmrHelper pmrHelper = new PmrHelper(pmr, cifAssetOrchestrator, countryResolver, evaluatorFactory, stencilReservedAttributesHelper, cifAssetRepository);
        final ValidationOrchestrator validationOrchestrator = new ValidationOrchestrator(new AssetValidator(cifAssetOrchestrator, evaluatorFactory));
        final OrderLineItemResource orderLineItemResource = projectResource.orderLineItemResource();

        ApeFacade apeFacade = ApeFacadeSingleton.get(config.getApeFacadeConfig());
        QuoteMigrationDetailsProvider migrationDetailsProvider = new QuoteMigrationDetailsProvider(pmr, projectResource);
        final String templateSelectionGuideUrl = config.getTpeConfig().getTemplateSelectionGuideConfig().getUrl();

        final SpecialBidWellKnownAttributeProvider wellKnownAttributeProvider = new SpecialBidWellKnownAttributeProvider(templateSelectionGuideUrl,
                specialBidReservedAttributesHelper);
        final SpecialBidMandatoryAttributeProvider mandatoryAttributeProvider = new SpecialBidMandatoryAttributeProvider();
        final TemplateTpeClient tpeClient = new TemplateTpeClient(config.getTpeConfig());
        final SpecialBidTemplateAttributeProvider templateAttributeProvider = new SpecialBidTemplateAttributeProvider(tpeClient,
                specialBidReservedAttributesHelper);

        final CIFAssetExtender cifAssetExtender = new CIFAssetExtender(pmrHelper,
                new CharacteristicExtender(pmrHelper),
                new ProductOfferingExtender(),
                new QuoteOptionDetailExtender(projectResource, cifAssetRepository),
                new StencilDetailExtender(stencilReservedAttributesHelper),
                new SiteDetailExtender(expedioClientResources.getCustomerResource()),
                new ValidationExtender(validationOrchestrator),
                new AsIsAssetExtender(cifAssetOrchestrator),
                new SalesRelationshipExtender(new CreatableCandidateProvider(pmrHelper, cifAssetOrchestrator, evaluatorFactory),
                        new ChoosableCandidateProvider(cifAssetOrchestrator, evaluatorFactory, stencilReservedAttributesHelper),
                        cifAssetOrchestrator, evaluatorFactory,
                        pmrHelper),
                new AccessDetailExtender(apeFacade),
                new SpecialBidExtender(specialBidReservedAttributesHelper,
                        projectResource,
                        wellKnownAttributeProvider, templateAttributeProvider, expedioClientResources.getUserResource()),
                new ActionExtender(new ActionCalculator()),
                new JourneySpecificDetailExtender(pmrHelper, orderLineItemResource),
                new ProductCategoryExtender(migrationDetailsProvider));

        final CharacteristicChangeRequestBuilder characteristicChangeRequestBuilder = new CharacteristicChangeRequestBuilder(pmrHelper, cifAssetOrchestrator);
        final CancelRelationshipRequestBuilder cancelRelationshipRequestBuilder = new CancelRelationshipRequestBuilder();
        final ExecutionRequestBuilder executionRequestBuilder = new ExecutionRequestBuilder(new RelateToRuleExecutor(cifAssetOrchestrator, evaluatorFactory, pmrHelper));
        final ContributesToChangeRequestBuilder changeRequestBuilder = new ContributesToChangeRequestBuilder(pmrHelper,
                                                                                                             new AssociatedAssetKeyProvider(customerInventoryModelPersistence),
                                                                                                             cifAssetOrchestrator, evaluatorFactory);
        final InvalidatePriceRequestBuilder invalidatePriceRequestBuilder = new InvalidatePriceRequestBuilder(pmrHelper);
        final CancellationContributesToRequestBuilder cancellationContributesToRequestBuilder = new CancellationContributesToRequestBuilder(changeRequestBuilder);
        final RestoreAssetRequestBuilder restoreAssetRequestBuilder = new RestoreAssetRequestBuilder() ;
        final DependentUpdateBuilderFactory dependentUpdateBuilderFactory = new DependentUpdateBuilderFactory (
                cancelRelationshipRequestBuilder,
                changeRequestBuilder,
                characteristicChangeRequestBuilder,
                invalidatePriceRequestBuilder,
                executionRequestBuilder,
                cancellationContributesToRequestBuilder,
                restoreAssetRequestBuilder) ;

        final QuoteEngineHelper quoteEngineHelper = new QuoteEngineHelper(projectResource) ;
        final CreateRelationshipUpdater createRelationshipUpdater = new CreateRelationshipUpdater(cifAssetOrchestrator, quoteEngineHelper,
                dependentUpdateBuilderFactory, pmrHelper);
        final CancelRelationshipUpdater cancelRelationshipUpdater = new CancelRelationshipUpdater(cifAssetOrchestrator, quoteEngineHelper, dependentUpdateBuilderFactory, pmrHelper);
        final UpdateRelationshipUpdater updateRelationshipUpdater = new UpdateRelationshipUpdater(cifAssetOrchestrator);
        final ReprovideAssetUpdater reprovideAssetUpdater = new ReprovideAssetUpdater(cifAssetOrchestrator, createRelationshipUpdater, cancelRelationshipUpdater, dependentUpdateBuilderFactory);

        ExternalAttributesHelper externalAttributesHelper = new ExternalAttributesHelper(projectResource);
        final AssetModelOrchestrator assetModelOrchestrator = new AssetModelOrchestrator(assetRepository, externalAssetRepository, assetReader, pmr, projectResource);
        final AssetCandidateProviderFactory candidateProviderFactory = new AssetCandidateProviderFactory(assetModelOrchestrator);
        final SpecialBidTemplateAttributeMapper templateAttributeMapper = new SpecialBidTemplateAttributeMapper();
        final SpecialBidWellKnownAttributeMapper wellKnownAttributeMapper = new SpecialBidWellKnownAttributeMapper();

        final SpecialBidCharacteristicsCreationUpdater specialBidCharacteristicsCreationUpdater = new SpecialBidCharacteristicsCreationUpdater(cifAssetOrchestrator,
                                                                                                                                               tpeClient,
                                                                                                                                               externalAttributesHelper,
                                                                                                                                               specialBidReservedAttributesHelper,
                                                                                                                                               templateAttributeMapper,
                                                                                                                                               wellKnownAttributeMapper,
                                                                                                                                               wellKnownAttributeProvider,
                                                                                                                                               mandatoryAttributeProvider);


        final AssetUpdateOrchestrator assetUpdateOrchestrator = new AssetUpdateOrchestrator(
                createRelationshipUpdater,
                new CharacteristicsUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory),
                new UpdateStencilUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory),
                new ChooseRelationshipUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory, candidateProviderFactory.choosableProvider(), pmrHelper),
                new AutoDefaultRelationshipUpdater(cifAssetOrchestrator, evaluatorFactory, pmrHelper),
                new SpecialBidCharacteristicsUpdater(cifAssetOrchestrator, wellKnownAttributeMapper, templateAttributeMapper, externalAttributesHelper, dependentUpdateBuilderFactory),
                cancelRelationshipUpdater,
                reprovideAssetUpdater,
                updateRelationshipUpdater,
                new UpdationIgnoreRequestUpdater(),
                new CharacteristicReloadUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory, pmrHelper),
                new SpecialBidCharacteristicsReloadUpdater(cifAssetOrchestrator, tpeClient, externalAttributesHelper, pmrHelper, dependentUpdateBuilderFactory),
                new InvalidatePriceUpdater(cifAssetOrchestrator, pmrHelper, new PricingStatusHelper(pmrHelper)),
                new CIFAssetUpdateRequestFilter(),
                new CIFAssetUpdateResponseFilter(),
                specialBidCharacteristicsCreationUpdater,
                new RestoreAssetUpdater(cifAssetOrchestrator, dependentUpdateBuilderFactory));

        // Have to set these late to cover the circular dependencies.
        cifAssetOrchestrator.setCifAssetExtender(cifAssetExtender);
        cifAssetOrchestrator.setCifAssetProvider(new CIFAssetCreator(pmrHelper, new UniqueIdJPARepository(customerInventoryModelPersistence), new AttributeSorter()));
        final CustomerInventoryClientManager customerInventoryClientManager =
            CustomerInventoryClientManagerFactory.getClientManager(config.getCustomerInventoryClientConfig(), pmr, countryResolver);
        ProductInstanceClient futureProductInstanceClient = customerInventoryClientManager.getProductInstanceClient();
        StoredProcedureRunner storedProcedureRunner = new StoredProcedureRunner(bfgPersistence,bfgDatabaseConfig);

        final RootAssetOrchestrator rootAssetHandler = new RootAssetOrchestrator(futureProductInstanceClient,cifAssetRepository,bfgReadRepository,storedProcedureRunner,pmr);

        return new RestResourceHandlerFactory() {
            {
                withSingleton(new AssetCandidateHandler(candidateProviderFactory));
                withSingleton(new CIFAssetHandler(cifAssetOrchestrator));
                withSingleton(new ValidationHandler(validationOrchestrator));
                withSingleton(new UpdateHandler(assetUpdateOrchestrator, new AssetSaveExceptionManager(quoteEngineHelper)));
                withSingleton(rootAssetHandler);
            }
        };
    }

    private AssetReader initAssetReader(BfgParameterMappings bfgParameterMappings, SqlQueryOwningUserNameResolver userNameResolver, LegacyVPNReader legacyVpnReader) {

        BucketAttributeReader bucketAttributeReader = new BucketAttributeReader(bfgPersistence, userNameResolver);
        COSReader cosReader = new COSReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        BespokeAttributeReader bespokeAttributeReader = new BespokeAttributeReader(bfgPersistence, userNameResolver);
        IpAddressAttributeReader ipAddressAttributeReader = new IpAddressAttributeReader(bfgPersistence, userNameResolver);
        CrossLayerMappingReader crossLayerMappingReader = new CrossLayerMappingReader(bfgPersistence, userNameResolver);
        FeatureOptionReader featureOptionReader = new FeatureOptionReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        NetworkNodeComponentReader networkNodeComponentReader = new NetworkNodeComponentReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        NetworkNodeReader networkNodeReader = new NetworkNodeReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        SoftwareReader softwareReader = new SoftwareReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        BearerReader bearerReader = new BearerReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        BearerExtensionReader bearerExtensionReader = new BearerExtensionReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        NetworkServiceReader networkServiceReader = new NetworkServiceReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);
        PackageInstanceReader packageInstanceReader = new PackageInstanceReader(bfgPersistence, pmr, bfgParameterMappings, userNameResolver);

        //register Readers
        packageInstanceReader.registerReaders(bucketAttributeReader, featureOptionReader, softwareReader, networkNodeReader, networkNodeComponentReader, bearerReader, networkServiceReader, bespokeAttributeReader, cosReader);
        softwareReader.registerReaders(bucketAttributeReader, featureOptionReader, networkNodeReader, networkNodeComponentReader);
        featureOptionReader.registerReaders(bucketAttributeReader, softwareReader, networkNodeReader, networkNodeComponentReader, ipAddressAttributeReader, featureOptionReader);
        networkNodeReader.registerReaders(bucketAttributeReader, networkNodeComponentReader, softwareReader, featureOptionReader, ipAddressAttributeReader);
        networkNodeComponentReader.registerReaders(bucketAttributeReader, featureOptionReader, softwareReader, networkNodeReader);
        bearerReader.registerReaders(bearerExtensionReader, crossLayerMappingReader, cosReader);
        bearerExtensionReader.registerReaders(networkNodeReader);
        networkServiceReader.registerReaders(featureOptionReader, crossLayerMappingReader, cosReader);

        return new AssetReader(bfgReadRepository, packageInstanceReader, legacyVpnReader, bfgParameterMappings.getDummyPackageProductMapping(), pmr);
    }

    // Ignoring as there is no way to test without altering production code
    // for the benefit of the test i.e. adding a new constructor or exposing the
    // persistence members which would never be used in production
    ///CLOVER:OFF
    @Override
    protected void doStop() {
        customerInventoryModelPersistence.undo();
        customerInventoryModelPersistence.unbind();
        customerInventoryJpa.close();
        bfgJpa.close();

        bfgPersistence.undo();
        bfgPersistence.unbind();
    }
    ///CLOVER:ON
}
