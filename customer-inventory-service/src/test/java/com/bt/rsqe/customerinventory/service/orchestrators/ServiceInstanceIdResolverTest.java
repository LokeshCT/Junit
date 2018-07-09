package com.bt.rsqe.customerinventory.service.orchestrators;

import com.bt.rsqe.customerinventory.dto.AssetDTO;
import com.bt.rsqe.customerinventory.dto.ExternalIdentifierDTO;
import com.bt.rsqe.customerinventory.fixtures.AssetDTOFixture;
import com.bt.rsqe.customerinventory.parameter.ProductCode;
import com.bt.rsqe.domain.product.parameters.ProductIdentifier;
import com.bt.rsqe.domain.product.parameters.RelationshipName;
import com.bt.rsqe.enums.AssetType;
import com.bt.rsqe.enums.IdentifierType;
import com.bt.rsqe.enums.ProductCodes;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;


public class ServiceInstanceIdResolverTest {
    @Test
    public void shouldResolveToAServiceInstanceId() {

        AssetDTO primaryRelatedAsset = new AssetDTOFixture().withProductCode(ProductCodes.IpConnectGlobalLeg).withAssetType(AssetType.STUB).withExternalIdentifier(new ExternalIdentifierDTO(IdentifierType.CLASSIC, "primaryServiceId")).build();
        AssetDTO secondaryRelatedAsset = new AssetDTOFixture().withProductCode(ProductCodes.IpConnectGlobalLeg).withAssetType(AssetType.STUB).withExternalIdentifier(new ExternalIdentifierDTO(IdentifierType.CLASSIC, "secondaryServiceId")).build();
        AssetDTO tertiaryRelatedAsset = new AssetDTOFixture().withProductCode(ProductCodes.IpConnectGlobalLeg).withAssetType(AssetType.STUB).withExternalIdentifier(new ExternalIdentifierDTO(IdentifierType.CLASSIC, "tertiaryServiceId")).build();
        AssetDTO quadRelatedAsset = new AssetDTOFixture().withProductCode(ProductCodes.IpConnectGlobalLeg).withAssetType(AssetType.STUB).withExternalIdentifier(new ExternalIdentifierDTO(IdentifierType.CLASSIC, "quadServiceId")).build();
        AssetDTO otherRelatedAsset = new AssetDTOFixture().withProductCode(new ProductCode("123")).withAssetType(AssetType.REAL).build();

        AssetDTO assetDTO = AssetDTOFixture.anAsset()
                .withRelatedToRelation(primaryRelatedAsset, RelationshipName.newInstance("WAN Connection Primary"))
                .withRelatedToRelation(secondaryRelatedAsset, RelationshipName.newInstance("WAN Connection Secondary"))
                .withRelatedToRelation(tertiaryRelatedAsset, RelationshipName.newInstance("WAN Connection Tertiary"))
                .withRelatedToRelation(quadRelatedAsset, RelationshipName.newInstance("WAN Connection Quaternary"))
                .withRelatedToRelation(otherRelatedAsset, RelationshipName.newInstance("someOtherRelation")).build();

        ServiceInstanceIdResolver serviceInstanceIdResolver = new ServiceInstanceIdResolver(assetDTO);

        assertThat(serviceInstanceIdResolver.get(new ProductIdentifier(ProductCodes.IpConnectGlobalCpe.productCode(), "A.1"), RelationshipName.newInstance("Primary CE Router")).get(), is("primaryServiceId"));
        assertThat(serviceInstanceIdResolver.get(new ProductIdentifier(ProductCodes.IpConnectGlobalCpe.productCode(), "A.1"), RelationshipName.newInstance("Secondary CE Router")).get(), is("secondaryServiceId"));
        assertThat(serviceInstanceIdResolver.get(new ProductIdentifier(ProductCodes.IpConnectGlobalCpe.productCode(), "A.1"), RelationshipName.newInstance("Tertiary CE Router")).get(), is("tertiaryServiceId"));
        assertThat(serviceInstanceIdResolver.get(new ProductIdentifier(ProductCodes.IpConnectGlobalCpe.productCode(), "A.1"), RelationshipName.newInstance("Quaternary CE Router")).get(), is("quadServiceId"));
        assertThat(serviceInstanceIdResolver.get(new ProductIdentifier("123", "A.1"), RelationshipName.newInstance("someOtherRelation")).isPresent(), is(false));

    }
}